# Architecture decisions, ERD, and trade-offs

This document answers: **why this design**, **how the data fits together (ERD)**, and **what we optimized for** versus what we deferred.

---

## 1. Architecture decisions — why this design?

### Layered Spring Boot + JPA

We use a **classic three-layer** style: **REST controllers** → **application services** → **repositories** → **relational database**.

**Why**

- **Separation of concerns**: HTTP/JSON stays thin; business rules (workflow, sprint completion, notifications) live in **services** where they are easier to test and evolve.
- **Spring Data JPA** reduces boilerplate for CRUD and queries while keeping a **clear domain model** (entities) aligned with tables.
- **DTOs** decouple API contracts from persistence (version fields, external IDs like `DEMO-123`, nested assignee objects) without polluting entities.

### Workflow as data, not hard-coded enums only

Statuses and **allowed transitions** are stored **per project** (`workflow_statuses`, `workflow_transitions`), not only as string columns on `issues`.

**Why**

- Matches the assignment: **configurable columns** and **transition rules** (e.g. no direct To Do → Done).
- New projects can have different boards without code changes.
- Optional **automation** (e.g. assign reviewer on “In Review”) is modeled on the transition row.

### Optimistic locking on issues

Issues use a **version** column (`@Version`) for concurrent updates.

**Why**

- The assignment scenario: two users editing the same issue. **Optimistic locking** detects conflicts at commit time; we surface **409** / conflict messaging instead of silent last-write-wins on critical fields.

### Issue numbers via a dedicated counter

Per-project **issue number** is allocated through an **`issue_counters`** row (with **pessimistic lock** when allocating in the service) to avoid duplicate `PROJ-123` keys under concurrency.

**Why**

- Safer than `MAX(issue_number)+1` without locking under load.
- Keeps human-readable **issue keys** stable and unique per project.

### WebSockets (STOMP) for real-time

Board/issue updates are pushed to **`/topic/...`** destinations; HTTP APIs remain the source of truth.

**Why**

- Matches the requirement for **live updates** (issue created/updated/moved, sprint updated).
- STOMP on SockJS is a standard Spring pattern and works well with browser clients.

### H2 for the prototype

**Why**

- **Zero install**, fast feedback, assignment-friendly **demo** and CI.
- **Trade-off**: not suitable for durable production data (see [Trade-offs](#3-trade-offs-what-we-optimized-for-and-why)).

---

## 2. Database schema diagram (ERD)

Below is a **logical ERD** of the main tables. Cardinality: one **project** owns many **issues**, **sprints**, **workflow** rows, etc.

```mermaid
erDiagram
    users ||--o{ issues : "reporter / assignee"
    users ||--o{ comments : author
    users ||--o{ activity_logs : actor
    users ||--o{ notifications : recipient
    users ||--o{ issue_watchers : watches

    projects ||--o{ issues : contains
    projects ||--o{ sprints : contains
    projects ||--o{ workflow_statuses : defines
    projects ||--o{ workflow_transitions : defines
    projects ||--o{ custom_field_definitions : defines
    projects ||--|| issue_counters : "next issue #"

    workflow_statuses ||--o{ issues : "current status"
    workflow_statuses ||--o{ workflow_transitions : from_status
    workflow_statuses ||--o{ workflow_transitions : to_status

    sprints ||--o{ issues : "optional sprint"

    issues ||--o{ issues : parent
    issues ||--o{ comments : has
    issues ||--o{ activity_logs : has
    issues ||--o{ custom_field_values : has
    issues ||--o{ notifications : triggers
    issues ||--o{ issue_watchers : watched_by

    custom_field_definitions ||--o{ custom_field_values : values

    users {
        uuid id PK
        string email
        string display_name
    }

    projects {
        uuid id PK
        string project_key UK
        string name
    }

    workflow_statuses {
        uuid id PK
        uuid project_id FK
        string name
        int sort_order
    }

    workflow_transitions {
        uuid id PK
        uuid project_id FK
        uuid from_status_id FK
        uuid to_status_id FK
        uuid assign_reviewer_id FK "optional"
    }

    sprints {
        uuid id PK
        uuid project_id FK
        string state
        date start_date
        date end_date
    }

    issues {
        uuid id PK
        uuid project_id FK
        int issue_number
        string type
        string title
        uuid status_id FK
        uuid assignee_id FK "nullable"
        uuid reporter_id FK
        uuid sprint_id FK "nullable"
        uuid parent_id FK "nullable"
        long version "optimistic lock"
    }

    comments {
        uuid id PK
        uuid issue_id FK
        uuid author_id FK
        uuid parent_comment_id FK "nullable"
    }

    activity_logs {
        uuid id PK
        uuid issue_id FK
        uuid actor_id FK
        string action
        string payload
    }

    issue_watchers {
        uuid issue_id FK
        uuid user_id FK
    }

    issue_counters {
        uuid project_id PK FK
        bigint next_number
    }

    custom_field_definitions {
        uuid id PK
        uuid project_id FK
        string field_type
    }

    custom_field_values {
        uuid id PK
        uuid issue_id FK
        uuid definition_id FK
        string field_value
    }

    notifications {
        uuid id PK
        uuid user_id FK
        uuid issue_id FK "nullable"
        string type
        bool is_read
    }
```

**`issue_labels`** is a separate collection table (issue id + label string) — omitted above for size; it links **issues** to many label strings.

---

## 3. Trade-offs: what we optimized for and why?

| Optimized for | Why | Cost / downside |
|---------------|-----|------------------|
| **Clarity & assignment fit** | Clear REST surface, workflow rules, audit-style activity, Swagger | Not every enterprise feature is as deep as Jira Cloud |
| **Correctness on concurrent edits** | Optimistic locking + locked issue counter | Users may need to **retry** on conflict; no automatic merge of fields |
| **Fast local demo & CI** | H2 in-memory, `ddl-auto` | **Data lost on restart**; no multi-instance shared DB |
| **Simple real-time** | In-memory event ring for replay | Replay is **best-effort**, not durable across restarts or horizontal scaling |
| **Flexible search v1** | JPA Specifications + indexes via schema | Very large tenants might need **Elasticsearch** + async indexing later |
| **Workflow in DB** | Change behavior without redeploy | More tables and joins than a single `status` string |
| **Fat JAR deployment** | One artifact to EC2 / container | Larger image than native compile; still fine for this scale |

### What we would do with more time (typical next steps)

- **PostgreSQL** (or RDS) + **Flyway** migrations; remove reliance on `create-drop`.
- **Durable outbox** or message broker for WebSocket fan-out and reliable notifications at scale.
- **Full-text search** (OpenSearch/Elasticsearch) for comments + descriptions at volume.
- **Authentication** (OAuth2/JWT) and **tenant/workspace** isolation for multi-team SaaS.

---

## Related docs

- [API curl examples](./API_CURL_EXAMPLES.md) — exact HTTP examples  
- [README](../README.md) — setup and endpoint summary  
