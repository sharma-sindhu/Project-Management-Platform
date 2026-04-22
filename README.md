# Project Management Platform (Jira-like backend)

Spring Boot backend for planning and tracking work: **projects**, **issues** (Epic, Story, Task, Bug, Sub-task), **sprints**, **workflow transitions**, **comments**, **activity**, **search**, and **real-time events** over **WebSockets**. Persistence uses **file-backed H2** under `./data/` so data survives restarts (see `application.yml`).

---

## Architecture overview

### High-level

The service follows a **layered architecture**: HTTP controllers delegate to **domain services**, which use **Spring Data JPA repositories** to read and write a **relational schema** in H2. Side effects such as **activity logging**, **in-app notifications**, and **WebSocket broadcasts** run inside the same transactional flows where appropriate.

```text
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────┐
│   Clients   │────▶│ Controllers  │────▶│  Services   │────▶│    H2    │
│ (Swagger/UI)│     │   (REST)     │     │  (domain)   │     │   (JPA)  │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────┘
                                               │
                                               ▼
                                        ┌──────────────┐
                                        │ WebSockets   │
                                        │ (STOMP topics)│
                                        └──────────────┘
```

### Package layout

| Layer | Package / role |
|--------|------------------|
| **API** | `com.pm.platform.web` — REST controllers, global exception handling (`GlobalExceptionHandler`), CORS, STOMP/WebSocket config. |
| **Application** | `com.pm.platform.service` — issue lifecycle, board aggregation, sprint lifecycle, search (JPA Specifications), comments, watchers, notifications, activity logging, workflow validation, optimistic locking on updates. |
| **Persistence** | `com.pm.platform.repository` — Spring Data JPA interfaces. |
| **Domain** | `com.pm.platform.domain` — JPA entities (projects, issues, sprints, workflow statuses/transitions, comments, activity logs, custom fields, notifications, watchers, issue counters). |
| **DTOs** | `com.pm.platform.dto` — request/response models for REST JSON. |

### Key design choices

- **Workflow**: Statuses and allowed transitions are stored per project. Illegal transitions return **HTTP 422** with allowed next statuses.
- **Concurrency**: Issues use **JPA optimistic locking** (`@Version`); conflicting updates can return **HTTP 409**.
- **Search**: Parameterized queries with optional text, status, assignee, and minimum priority filters; cursor-style pagination via encoded page tokens.
- **Real-time**: STOMP broker with topics such as `/topic/projects/{projectId}/events` and presence on `/topic/projects/{projectId}/presence`; an in-memory ring buffer supports a simple **replay** endpoint for reconnects (best-effort, not durable).
- **Database**: **H2 file** (`./data/pmplatform`) with **`ddl-auto: update`** — schema and data persist across restarts. There is **no built-in seed data**; populate projects, users, workflow, etc. yourself (for example via the H2 console) before using the REST API. For production, replace with a managed database (for example PostgreSQL) and Flyway/Liquibase migrations.

### Dependencies (Gradle)

- Spring Boot Web, Data JPA, Validation, WebSocket  
- H2  
- SpringDoc OpenAPI (Swagger UI)

---

## Setup instructions

### Prerequisites

- **JDK 17**  
- Optional: **Gradle** (or use the included **Gradle Wrapper** `./gradlew`)

### Clone and run locally

```bash
git clone https://github.com/ashish6523/Project-Management-Platform.git
cd Project-Management-Platform
chmod +x gradlew
./gradlew bootRun
```

Application listens on **http://localhost:8080** by default.

### Build an executable JAR

```bash
./gradlew bootJar
java -jar build/libs/pm-platform-0.0.1-SNAPSHOT.jar
```

Use the **large** fat JAR (`pm-platform-0.0.1-SNAPSHOT.jar`), not the `-plain` JAR.

### H2 console (development)

When enabled in configuration:

- URL: **http://localhost:8080/h2-console**  
- JDBC URL: **`jdbc:h2:file:./data/pmplatform`** (same as `spring.datasource.url` in `application.yml`; path is relative to the process working directory, usually the project root when using `./gradlew bootRun`)  
- User: `sa` / empty password (defaults from `application.yml`)

### Configuration

Main settings live in **`src/main/resources/application.yml`** (port `8080`, H2 URL, JPA `ddl-auto`, SpringDoc paths). Override for production with **`application-prod.yml`**, environment variables, or `-D` system properties as needed.

---

## API documentation

### Interactive docs (recommended)

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |

*(If you deploy elsewhere, replace `localhost:8080` with your host and port.)*

The OpenAPI path is configured as **`/api-docs`** (not the default `/v3/api-docs`).

### Endpoint summary

Base path: **`/api`**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/projects` | Create project (`key`, `name`, optional `description`, optional `project_id`); bootstraps workflow + issue counter |
| `POST` | `/api/users` | Create user (`email`, `display_name`, optional `user_id`) |
| `POST` | `/api/projects/{projectId}/issues` | Create issue |
| `GET` | `/api/projects/{projectId}/board` | Board columns with issues |
| `GET` | `/api/projects/{projectId}/sprints` | List sprints |
| `POST` | `/api/projects/{projectId}/sprints` | Create sprint (`name`, `start_date`, `end_date`; optional `state`, `sprint_id`) |
| `GET` | `/api/projects/{projectId}/activity` | Paginated activity feed |
| `GET` | `/api/projects/{projectId}/events/replay` | Recent WebSocket events (in-memory replay) |
| `GET` | `/api/issues/{issueId}` | Get issue |
| `PATCH` | `/api/issues/{issueId}` | Update issue fields |
| `POST` | `/api/issues/{issueId}/transitions` | Transition status (workflow) |
| `GET` | `/api/issues/{issueId}/comments` | List comments |
| `POST` | `/api/issues/{issueId}/comments` | Add comment |
| `POST` | `/api/issues/{issueId}/watch` | Watch issue (`user_id` query param) |
| `DELETE` | `/api/issues/{issueId}/watch` | Unwatch issue (`user_id` query param) |
| `GET` | `/api/search` | Search/filter issues (`projectId` required; `q`, `status`, `assignee`, `minPriority`, `limit`, `cursor`) |
| `POST` | `/api/sprints/{sprintId}/start` | Start sprint |
| `POST` | `/api/sprints/{sprintId}/complete` | Complete sprint (optional carry-over body) |

**WebSocket (STOMP):** connect to **`/ws`** (SockJS-enabled). Application destination prefix: `/app`. Broker destinations prefixed with `/topic` (see `WebSocketConfig` and `PresenceController`).

### Copy-paste examples

See **`docs/API_CURL_EXAMPLES.md`** for **`curl`** examples and JSON bodies (substitute ids from your own database).

### Design & trade-offs

See **`docs/DESIGN.md`** for **architecture rationale**, **ERD (diagram)**, and **trade-offs** (what this codebase optimized for vs. what was deferred).

---

## License

No license file is included; add one if you open-source the project.
