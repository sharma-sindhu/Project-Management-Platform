# API curl examples (exact commands)

**Base URL (deployed):** `http://3.25.57.97:8080`

- Use **`http://`** (not `https://`) unless you terminate TLS elsewhere.
- Omit the trailing slash on the base; paths below are complete.
- **`Content-Type: application/json`** is required for POST/PATCH bodies.

**Swagger UI:** [http://3.25.57.97:8080/swagger-ui/index.html](http://3.25.57.97:8080/swagger-ui/index.html)

**OpenAPI JSON:** [http://3.25.57.97:8080/api-docs](http://3.25.57.97:8080/api-docs)

---

## Copy-paste curl (Postman-style)

**Host:** change `http://localhost:8080` in each URL if your server is different.

**What to copy:** the **whole `curl` block** for each step (from `curl --location` through the last line). Paste into a terminal, or Postman **Import → Raw text**.

Run **1 → 7 in order** the first time. From step **5**’s JSON response, copy the **`id`** value and replace **`REPLACE_ISSUE_ROW_ID`** in steps 6 and 10–14. On a second run, change `project_id` / `user_id` / `sprint_id` values or you will get duplicate errors.

### 1. Create project

```bash
curl --location 'http://localhost:8080/api/projects' \
--header 'Content-Type: application/json' \
--data '{"key":"DEMO","name":"Demo project","description":"From curl","project_id":"my-first-project"}'
```

### 2. Create user Jane

```bash
curl --location 'http://localhost:8080/api/users' \
--header 'Content-Type: application/json' \
--data '{"email":"jane@example.com","display_name":"Jane","user_id":"jane"}'
```

### 3. Create user Bob

```bash
curl --location 'http://localhost:8080/api/users' \
--header 'Content-Type: application/json' \
--data '{"email":"bob@example.com","display_name":"Bob","user_id":"bob"}'
```

### 4. Create sprint

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/sprints' \
--header 'Content-Type: application/json' \
--data '{"name":"Sprint 1","start_date":"2026-04-01","end_date":"2026-04-14","sprint_id":"sprint-1"}'
```

### 5. Create issue

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/issues' \
--header 'Content-Type: application/json' \
--data '{"type":"STORY","title":"First story","description":"","priority":"HIGH","reporter_user_id":"jane","assignee_user_id":"bob","sprint_id":"sprint-1","parent_issue_id":null,"story_points":5,"labels":["bootstrap"],"initial_status_name":"To Do"}'
```

### 6. Get issue (`REPLACE_ISSUE_ROW_ID` = `id` from step 5 response)

```bash
curl --location 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID'
```

### 7. Start sprint

```bash
curl --location --request POST 'http://localhost:8080/api/sprints/sprint-1/start'
```

### 8. Board

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/board'
```

### 9. List sprints

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/sprints'
```

### 10. Patch issue

```bash
curl --location --request PATCH 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID' \
--header 'Content-Type: application/json' \
--data '{"title":"First story (updated)","expected_version":0}'
```

(`expected_version` ko last GET/PATCH response ke `version` se match karo.)

### 11. Transition issue

```bash
curl --location 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID/transitions' \
--header 'Content-Type: application/json' \
--data '{"to_status_name":"In Progress","actor_user_id":"jane"}'
```

### 12. Add comment

```bash
curl --location 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID/comments' \
--header 'Content-Type: application/json' \
--data '{"body":"Looks good","parent_comment_id":null,"author_user_id":"bob"}'
```

### 13. List comments

```bash
curl --location 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID/comments'
```

### 14. Watch issue

```bash
curl --location --request POST 'http://localhost:8080/api/issues/REPLACE_ISSUE_ROW_ID/watch?user_id=jane'
```

### 15. Search

```bash
curl --location 'http://localhost:8080/api/search?projectId=my-first-project&q=First&limit=10'
```

### 16. Activity

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/activity?page=0&size=20'
```

### 17. Events replay

```bash
curl --location 'http://localhost:8080/api/projects/my-first-project/events/replay'
```

### 18. Complete sprint (optional body)

```bash
curl --location 'http://localhost:8080/api/sprints/sprint-1/complete' \
--header 'Content-Type: application/json' \
--data '{"carry_over_issue_ids":[],"target_sprint_id":null,"move_to_backlog_issue_ids":[]}'
```

---

## 0. End-to-end bootstrap (run in this order)

Use a shell; set **`BASE`** to your server (`http://localhost:8080` or the deployed host). **`jq`** reads ids from JSON. After this section, **`$PROJECT_ID`**, **`$SPRINT_ID`**, and **`$ISSUE_ROW_ID`** are set for later commands.

**JSON enums:** Issue `type` = `EPIC` \| `STORY` \| `TASK` \| `BUG` \| `SUB_TASK`.  
**Priority:** `LOWEST` \| `LOW` \| `MEDIUM` \| `HIGH` \| `HIGHEST`.

```bash
export BASE="http://localhost:8080"
```

### 0.1 — Create project (workflow + issue counter are created automatically)

```bash
export PROJECT_JSON="$(curl -s -X POST "$BASE/api/projects" \
  -H "Content-Type: application/json" \
  -d '{"key":"DEMO","name":"Demo project","description":"From curl","project_id":"my-first-project"}')"
export PROJECT_ID="$(echo "$PROJECT_JSON" | jq -r .project_id)"
echo "PROJECT_ID=$PROJECT_ID"
```

### 0.2 — Create users (`reporter_user_id` / `assignee_user_id`)

```bash
curl -s -X POST "$BASE/api/users" \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","display_name":"Jane","user_id":"jane"}' | jq .

curl -s -X POST "$BASE/api/users" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","display_name":"Bob","user_id":"bob"}' | jq .
```

### 0.3 — Create sprint (optional; omit `sprint_id` on the issue if you skip this)

```bash
export SPRINT_JSON="$(curl -s -X POST "$BASE/api/projects/$PROJECT_ID/sprints" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sprint 1","start_date":"2026-04-01","end_date":"2026-04-14","sprint_id":"sprint-1"}')"
export SPRINT_ID="$(echo "$SPRINT_JSON" | jq -r .sprint_id)"
echo "SPRINT_ID=$SPRINT_ID"
```

### 0.4 — Create issue

```bash
export ISSUE_JSON="$(curl -s -X POST "$BASE/api/projects/$PROJECT_ID/issues" \
  -H "Content-Type: application/json" \
  -d "{
    \"type\": \"STORY\",
    \"title\": \"First story\",
    \"description\": \"\",
    \"priority\": \"HIGH\",
    \"reporter_user_id\": \"jane\",
    \"assignee_user_id\": \"bob\",
    \"sprint_id\": \"$SPRINT_ID\",
    \"parent_issue_id\": null,
    \"story_points\": 5,
    \"labels\": [\"bootstrap\"],
    \"initial_status_name\": \"To Do\"
  }")"
export ISSUE_ROW_ID="$(echo "$ISSUE_JSON" | jq -r .id)"
echo "ISSUE_ROW_ID=$ISSUE_ROW_ID (human key: $(echo "$ISSUE_JSON" | jq -r .issue_id))"
```

Use **`ISSUE_ROW_ID`** in **`/api/issues/{id}`** paths. The JSON field **`issue_id`** is the human key (e.g. `DEMO-1`).

### 0.5 — Board, get issue, list sprints

```bash
curl -s "$BASE/api/projects/$PROJECT_ID/board" | jq .
curl -s "$BASE/api/issues/$ISSUE_ROW_ID" | jq .
curl -s "$BASE/api/projects/$PROJECT_ID/sprints" | jq .
```

### 0.6 — Start sprint

```bash
curl -s -X POST "$BASE/api/sprints/$SPRINT_ID/start" | jq .
```

### 0.7 — Patch issue

```bash
curl -s -X PATCH "$BASE/api/issues/$ISSUE_ROW_ID" \
  -H "Content-Type: application/json" \
  -d '{"title":"First story (updated)","expected_version":0}' | jq .
```

Replace **`expected_version`** with the **`version`** from your latest GET/PATCH response when you use optimistic locking.

### 0.8 — Transition issue

```bash
curl -s -X POST "$BASE/api/issues/$ISSUE_ROW_ID/transitions" \
  -H "Content-Type: application/json" \
  -d '{"to_status_name":"In Progress","actor_user_id":"jane"}' | jq .
```

### 0.9 — Comments

```bash
curl -s -X POST "$BASE/api/issues/$ISSUE_ROW_ID/comments" \
  -H "Content-Type: application/json" \
  -d '{"body":"Looks good","parent_comment_id":null,"author_user_id":"bob"}' | jq .

curl -s "$BASE/api/issues/$ISSUE_ROW_ID/comments" | jq .
```

### 0.10 — Watch / unwatch

```bash
curl -s -X POST "$BASE/api/issues/$ISSUE_ROW_ID/watch?user_id=jane" -w "\n%{http_code}\n"
curl -s -X DELETE "$BASE/api/issues/$ISSUE_ROW_ID/watch?user_id=jane" -w "\n%{http_code}\n"
```

### 0.11 — Search

```bash
curl -s -G "$BASE/api/search" \
  --data-urlencode "projectId=$PROJECT_ID" \
  --data-urlencode "q=First" \
  --data-urlencode "limit=10" | jq .
```

### 0.12 — Activity & replay

```bash
curl -s "$BASE/api/projects/$PROJECT_ID/activity?page=0&size=20" | jq .
curl -s "$BASE/api/projects/$PROJECT_ID/events/replay" | jq .
```

### 0.13 — Complete sprint

```bash
curl -s -X POST "$BASE/api/sprints/$SPRINT_ID/complete" \
  -H "Content-Type: application/json" \
  -d '{"carry_over_issue_ids":[],"target_sprint_id":null,"move_to_backlog_issue_ids":[]}' | jq .
```

---

## IDs in later sections (fixed examples)

Sections **1+** below use **fixed example** ids (`demo-project`, `jane`, …). They only match your DB if you used those same ids when creating data, or after you edit the URLs. Prefer **section 0** for a full ordered flow on an empty database.

---

## 1. Board (all issues by column)

```bash
curl -s "http://3.25.57.97:8080/api/projects/demo-project/board"
```

Pretty-print with jq:

```bash
curl -s "http://3.25.57.97:8080/api/projects/demo-project/board" | jq .
```

---

## 2. Get one issue

```bash
curl -s "http://3.25.57.97:8080/api/issues/issue-oauth-story"
```

```bash
curl -s "http://3.25.57.97:8080/api/issues/issue-npe-bug"
```

---

## 3. List sprints

```bash
curl -s "http://3.25.57.97:8080/api/projects/demo-project/sprints"
```

---

## 4. Activity feed

```bash
curl -s "http://3.25.57.97:8080/api/projects/demo-project/activity?page=0&size=20"
```

---

## 5. Search

Text search:

```bash
curl -s -G "http://3.25.57.97:8080/api/search" \
  --data-urlencode "projectId=demo-project" \
  --data-urlencode "q=oauth" \
  --data-urlencode "limit=20"
```

With filters (optional):

```bash
curl -s -G "http://3.25.57.97:8080/api/search" \
  --data-urlencode "projectId=demo-project" \
  --data-urlencode "status=In Progress" \
  --data-urlencode "assignee=jane" \
  --data-urlencode "minPriority=MEDIUM" \
  --data-urlencode "limit=10"
```

Cursor pagination (optional): add `&cursor=<value from previous response next_cursor>`.

---

## 6. Create issue

```bash
curl -s -X POST "http://3.25.57.97:8080/api/projects/demo-project/issues" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TASK",
    "title": "Write API docs",
    "description": "Document endpoints for reviewers",
    "priority": "MEDIUM",
    "reporter_user_id": "bob",
    "assignee_user_id": "jane",
    "sprint_id": "sprint-10",
    "parent_issue_id": null,
    "story_points": 3,
    "labels": ["docs", "api"],
    "initial_status_name": "To Do"
  }'
```

---

## 7. Patch issue

```bash
curl -s -X PATCH "http://3.25.57.97:8080/api/issues/issue-npe-bug" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Fix null pointer in token refresh (updated)",
    "priority": "HIGH",
    "expected_version": null
  }'
```

Optional: set `"expected_version": <number>` from a prior GET to test optimistic locking.

---

## 8. Transition status

**Example A — bug in To Do → In Progress**

```bash
curl -s -X POST "http://3.25.57.97:8080/api/issues/issue-npe-bug/transitions" \
  -H "Content-Type: application/json" \
  -d '{
    "to_status_name": "In Progress",
    "actor_user_id": "bob",
    "override_reviewer_id": null
  }'
```

**Example B — story in In Progress → In Review**

```bash
curl -s -X POST "http://3.25.57.97:8080/api/issues/issue-oauth-story/transitions" \
  -H "Content-Type: application/json" \
  -d '{
    "to_status_name": "In Review",
    "actor_user_id": "bob"
  }'
```

**Example C — illegal transition (expect HTTP 422)**

Only valid if the issue is still in **To Do** (adjust `issueId` to match current state):

```bash
curl -s -w "\nhttp_code:%{http_code}\n" -X POST "http://3.25.57.97:8080/api/issues/issue-npe-bug/transitions" \
  -H "Content-Type: application/json" \
  -d '{
    "to_status_name": "Done",
    "actor_user_id": "bob"
  }'
```

---

## 9. Comments

Add comment (optional `@user_<id>` mentions in `body`, where `<id>` is the user’s string id):

```bash
curl -s -X POST "http://3.25.57.97:8080/api/issues/issue-oauth-story/comments" \
  -H "Content-Type: application/json" \
  -d '{
    "body": "Please add tests. cc @user_jane",
    "parent_comment_id": null,
    "author_user_id": "bob"
  }'
```

List comments:

```bash
curl -s "http://3.25.57.97:8080/api/issues/issue-oauth-story/comments"
```

---

## 10. Watch / unwatch

Watch:

```bash
curl -s -X POST "http://3.25.57.97:8080/api/issues/issue-oauth-story/watch?user_id=jane" \
  -w "\nhttp_code:%{http_code}\n"
```

Unwatch:

```bash
curl -s -X DELETE "http://3.25.57.97:8080/api/issues/issue-oauth-story/watch?user_id=jane" \
  -w "\nhttp_code:%{http_code}\n"
```

---

## 11. Sprints

Start sprint:

```bash
curl -s -X POST "http://3.25.57.97:8080/api/sprints/sprint-10/start"
```

Complete sprint (empty body allowed):

```bash
curl -s -X POST "http://3.25.57.97:8080/api/sprints/sprint-10/complete" \
  -H "Content-Type: application/json" \
  -d '{}'
```

Complete with carry-over (example shape):

```bash
curl -s -X POST "http://3.25.57.97:8080/api/sprints/sprint-10/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "carry_over_issue_ids": ["issue-oauth-story"],
    "target_sprint_id": "sprint-10",
    "move_to_backlog_issue_ids": null
  }'
```

---

## 12. Event replay (in-memory buffer)

```bash
curl -s "http://3.25.57.97:8080/api/projects/demo-project/events/replay"
```

---

## Recommended order (smoke + product flow)

1. GET board  
2. GET issue (DEMO-1)  
3. GET sprints  
4. GET search with `q=oauth`  
5. POST create issue  
6. GET board again  
7. PATCH issue  
8. POST transition (legal)  
9. POST transition (illegal) → 422  
10. POST comment → GET comments  
11. GET activity  
12. POST watch → DELETE watch  

---

## Troubleshooting

| Symptom | Check |
|--------|--------|
| Connection timeout from browser/curl | EC2 **security group** inbound **TCP 8080** from `0.0.0.0/0` (or your IP). |
| 422 on transition | Issue’s current status must allow the target; use GET issue or board to see status. |
| 409 on PATCH | Concurrent update; send `expected_version` from latest GET. |
| Empty DB / wrong ids | No seed data: create projects/users/issues first, or fix ids in the curl to match your H2 data. |

---

## Changing the base URL locally

Replace the host in every URL, or set a variable in bash:

```bash
export BASE="http://3.25.57.97:8080"
curl -s "${BASE}/api/projects/demo-project/board"
```

If your instance **public IP changes** (stop/start without Elastic IP), update this document or use the new IP in `BASE`.
