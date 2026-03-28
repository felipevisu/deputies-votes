# De Olho Neles - Backend

REST API for the De Olho Neles application, a Brazilian deputies voting tracker.

## Tech Stack

- Java 21
- Spring Boot 3.4.4
- Spring Data JPA (Hibernate)
- PostgreSQL 17
- Flyway (database migrations)
- Maven
- Docker

## Getting Started

### With Docker Compose (recommended)

From the **project root** (`de-olho-neles/`):

```bash
docker-compose up --build -d
```

This starts:
- **PostgreSQL** on port `5433` (mapped from container's `5432`)
- **Backend** on port `8080`

Flyway runs automatically on startup, creating the schema and seeding data.

### Local Development

Requires a running PostgreSQL instance on `localhost:5432` with database `deolhoneles`.

```bash
cd backend
./mvnw spring-boot:run
```

### Configuration

| Profile | File | Database Host |
|---------|------|---------------|
| default | `application.yml` | `localhost:5432` |
| docker | `application-docker.yml` | `postgres:5432` |

## Database Schema

```
deputy
├── id (PK)
├── name
├── party
├── state
├── avatar
└── external_id (unique)

legislative_activity
├── id (PK)
├── title
├── subtitle
├── description
├── summary
├── author
├── category
├── vote_date
├── external_id (unique)
├── vote_round
└── source_proposal_id

proposal
├── id (PK)
├── external_id (unique)
├── type_code
├── number
├── year
├── ementa
├── keywords
├── presentation_date
└── status_description

proposal_author
├── proposal_id (PK, FK → proposal)
├── deputy_id (PK, FK → deputy)
├── signing_order
└── proponent

deputy_vote
├── id (PK)
├── deputy_id (FK → deputy)
├── activity_id (FK → legislative_activity)
└── vote (SIM | NAO | ABSTENCAO | AUSENTE)
```

### Entity Relationships

```
Deputy  <──1:N── DeputyVote
LegislativeActivity <──1:N── DeputyVote
Proposal <──1:N── ProposalAuthor ──N:1──> Deputy
```

## API Reference

All list endpoints return a paginated response:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 10,
  "totalPages": 1,
  "last": true
}
```

**Vote values:** `SIM`, `NÃO`, `ABSTENÇÃO`, `AUSENTE`

---

### Deputies

#### List deputies

```
GET /deputies?page=0&size=20
```

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `page` | int | No | Page number (default: `0`) |
| `size` | int | No | Page size (default: `20`) |

**Response item:**

```json
{
  "id": 1,
  "name": "Ana Souza",
  "party": "PSD",
  "legend": "SP",
  "avatar": null,
  "externalId": 12345
}
```

#### Get deputy

```
GET /deputies/{id}
```

#### Find deputy by external ID

```
GET /deputies/external/{externalId}
```

**Response:** `200` with `DeputyResponse`, or `404 Not Found`

#### Create deputy

```
POST /deputies
Content-Type: application/json

{
  "name": "New Deputy",
  "party": "PT",
  "state": "SP",
  "avatar": null,
  "externalId": 12345
}
```

**Response:** `201 Created`

#### Update deputy

```
PUT /deputies/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "party": "MDB",
  "state": "RJ",
  "avatar": null,
  "externalId": 12345
}
```

#### Delete deputy

```
DELETE /deputies/{id}
```

**Response:** `204 No Content`

---

### Activities

Legislative activities (voting sessions).

#### List activities

```
GET /activities?page=0&size=20
```

#### Get activity

```
GET /activities/{id}
```

**Response:**

```json
{
  "id": 1,
  "title": "PL 1234/2026 - Reforma Tributaria",
  "subtitle": "Simplifica impostos sobre consumo",
  "description": "Aprovado o Projeto. Sim: 391; Não: 33; Abstenção: 2; Total: 426.",
  "summary": "Simplifica o sistema tributario brasileiro...",
  "author": "Comissao Especial da Camara",
  "category": "Economia",
  "voteDate": "2026-03-25",
  "externalId": "2345678-9",
  "sourceProposalId": "1234567"
}
```

#### Find activity by external ID

```
GET /activities/external/{externalId}
```

**Response:** `200` with `ActivityResponse`, or `404 Not Found`

#### Create activity

```
POST /activities
Content-Type: application/json

{
  "title": "PL 1234/2026 - Reforma Tributaria",
  "summary": "Simplifica o sistema tributario brasileiro...",
  "author": "Comissao Especial da Camara",
  "category": "Economia",
  "voteDate": "2026-03-25",
  "externalId": "2345678-9",
  "description": "Aprovado o Projeto. Sim: 391; Não: 33; Abstenção: 2; Total: 426.",
  "voteRound": "Turno único"
}
```

**Response:** `201 Created`

#### Update activity

```
PUT /activities/{id}
Content-Type: application/json

{
  "title": "PL 1234/2026 - Reforma Tributaria",
  "summary": "Updated summary...",
  "author": "Comissao Especial da Camara",
  "category": "Economia",
  "voteDate": "2026-03-25",
  "externalId": "2345678-9",
  "description": "Aprovado o Projeto. Sim: 391; Não: 33; Abstenção: 2; Total: 426.",
  "voteRound": "Turno único"
}
```

#### Enrich activity

Adds subtitle, summary, and source proposal ID to an existing activity.

```
PATCH /activities/{id}/enrich
Content-Type: application/json

{
  "subtitle": "Simplifica impostos sobre consumo",
  "summary": "Simplifica o sistema tributario brasileiro...",
  "sourceProposalId": "1234567"
}
```

**Response:** `204 No Content`

#### Delete activity

```
DELETE /activities/{id}
```

**Response:** `204 No Content`

---

### Proposals

Legislative proposals authored by deputies.

#### Find proposal by external ID

```
GET /proposals/external/{externalId}
```

**Response:** `200` with `ProposalResponse`, or `404 Not Found`

```json
{
  "id": 1,
  "externalId": 1234567,
  "typeCode": "PL",
  "number": 1234,
  "year": 2026,
  "ementa": "Dispoe sobre a reforma tributaria...",
  "keywords": "tributos, impostos, reforma",
  "presentationDate": "2026-01-15",
  "statusDescription": "Aprovada"
}
```

#### Create proposal

```
POST /proposals
Content-Type: application/json

{
  "externalId": 1234567,
  "typeCode": "PL",
  "number": 1234,
  "year": 2026,
  "ementa": "Dispoe sobre a reforma tributaria...",
  "keywords": "tributos, impostos, reforma",
  "presentationDate": "2026-01-15",
  "statusDescription": "Em tramitacao"
}
```

**Response:** `201 Created`

#### Add author to proposal

```
POST /proposals/{id}/authors
Content-Type: application/json

{
  "deputyId": 1,
  "signingOrder": 1,
  "proponent": true
}
```

**Response:** `201 Created`

---

### Votes

#### Create vote

```
POST /votes
Content-Type: application/json

{
  "deputyId": 1,
  "activityId": 1,
  "vote": "SIM"
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "deputyId": 1,
  "activityId": 1,
  "vote": "SIM"
}
```

---

### Feed

Unified feed combining voting activities and proposals authored by followed deputies, sorted by date descending.

```
POST /feed?page=0&size=10
Content-Type: application/json

{
  "deputyIds": [1, 2, 4, 6, 8]
}
```

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `page` | int | No | Page number (default: `0`) |
| `size` | int | No | Page size (default: `10`) |

**Request body:**

| Field | Type | Description |
|-------|------|-------------|
| `deputyIds` | Long[] | List of deputy IDs whose activity to return. Returns empty if null/empty. |

The response contains two types of items distinguished by the `type` field. Null objects are omitted from the response.

#### VOTING item

```json
{
  "type": "VOTING",
  "date": "2026-03-25",
  "activity": {
    "id": 1,
    "externalId": "2345678-9",
    "title": "PL 1234/2026 - Reforma Tributaria",
    "subtitle": "Simplifica impostos sobre consumo",
    "description": "Aprovado o Projeto. Sim: 391; Não: 33; Abstenção: 2; Total: 426.",
    "voteRound": "Turno único",
    "summary": "Simplifica o sistema tributario brasileiro...",
    "author": "Comissao Especial da Camara",
    "category": "Economia",
    "votes": [
      {
        "deputyId": 1,
        "name": "Ana Souza",
        "party": "PSD",
        "state": "SP",
        "photo": "https://...",
        "vote": "SIM"
      }
    ]
  }
}
```

#### PROPOSAL item

```json
{
  "type": "PROPOSAL",
  "date": "2026-01-15",
  "proposal": {
    "id": 1,
    "externalId": 1234567,
    "typeCode": "PL",
    "number": 1234,
    "year": 2026,
    "ementa": "Dispoe sobre a reforma tributaria...",
    "status": "Aprovada",
    "authors": [
      {
        "deputyId": 1,
        "name": "Ana Souza",
        "party": "PSD",
        "state": "SP",
        "photo": "https://...",
        "signingOrder": 1,
        "proponent": true
      }
    ]
  }
}
```

---

## Project Structure

```
backend/
├── Dockerfile
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main/
    ├── java/com/deolhoneles/
    │   ├── DeOlhoNelesApplication.java
    │   ├── config/
    │   │   └── WebConfig.java              # CORS (allows localhost:3000)
    │   ├── controller/
    │   │   ├── ActivityController.java
    │   │   ├── DeputyController.java
    │   │   ├── FeedController.java
    │   │   ├── ProposalController.java
    │   │   └── VoteController.java
    │   ├── dto/
    │   │   ├── ActivityRequest.java
    │   │   ├── ActivityResponse.java
    │   │   ├── DeputyRequest.java
    │   │   ├── DeputyResponse.java
    │   │   ├── DeputyVoteSummary.java
    │   │   ├── EnrichRequest.java
    │   │   ├── FeedActivityItem.java
    │   │   ├── FeedItemResponse.java
    │   │   ├── FeedProposalItem.java
    │   │   ├── FeedRequest.java
    │   │   ├── PageResponse.java
    │   │   ├── ProposalAuthorRequest.java
    │   │   ├── ProposalAuthorSummary.java
    │   │   ├── ProposalRequest.java
    │   │   ├── ProposalResponse.java
    │   │   ├── UnifiedFeedItemResponse.java
    │   │   ├── VoteRequest.java
    │   │   └── VoteResponse.java
    │   ├── entity/
    │   │   ├── Deputy.java
    │   │   ├── DeputyVote.java
    │   │   ├── LegislativeActivity.java
    │   │   ├── Proposal.java
    │   │   ├── ProposalAuthor.java
    │   │   ├── ProposalAuthorId.java
    │   │   └── VoteType.java
    │   ├── repository/
    │   │   ├── DeputyRepository.java
    │   │   ├── DeputyVoteRepository.java
    │   │   ├── LegislativeActivityRepository.java
    │   │   ├── ProposalAuthorRepository.java
    │   │   └── ProposalRepository.java
    │   └── service/
    │       ├── ActivityService.java
    │       ├── DeputyService.java
    │       ├── FeedService.java
    │       ├── ProposalService.java
    │       └── VoteService.java
    └── resources/
        ├── application.yml
        ├── application-docker.yml
        └── db/migration/
            ├── V1__create_schema.sql
            ├── V2__seed_data.sql
            ├── V3__add_account_and_follow.sql
            ├── V4__add_external_id.sql
            ├── V5__remove_mock_data.sql
            ├── V6__widen_proposal_columns.sql
            ├── V7__rename_proposal_to_activity.sql
            ├── V8__add_proposal_tables.sql
            ├── V9__add_enriched_title_and_source.sql
            ├── V10__rename_enriched_title_to_subtitle.sql
            ├── V11__add_vote_round.sql
            ├── V12__drop_account_tables.sql
            └── V13__add_activity_description.sql
```
