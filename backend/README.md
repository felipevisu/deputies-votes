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
account
├── id (PK)
├── name
├── last_name
└── email (unique)

deputy
├── id (PK)
├── name
├── party
├── state
└── avatar

legislative_proposal
├── id (PK)
├── title
├── summary
├── author
├── category
└── vote_date

deputy_vote
├── id (PK)
├── deputy_id (FK → deputy)
├── proposal_id (FK → legislative_proposal)
└── vote (SIM | NAO | ABSTENCAO | AUSENTE)

account_deputy_follow
├── account_id (FK → account)
└── deputy_id (FK → deputy)
```

### Entity Relationships

```
Account ──M:N──> Deputy       (via account_deputy_follow)
Deputy  <──1:N── DeputyVote
LegislativeProposal <──1:N── DeputyVote
```

## Seed Data

The migrations seed:
- **1 account**: Default User (`user@deolhoneles.com`)
- **10 deputies**: Ana Souza (PSD-SP), Carlos Mendes (PT-RJ), Fernanda Lima (MDB-MG), etc.
- **15 legislative proposals**: across 10 categories (Economia, Tecnologia, Saude, etc.)
- **150 deputy votes**: one per deputy per proposal
- **5 follows**: default account follows deputies 1, 2, 4, 6, 8

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

---

### Accounts

#### List accounts

```
GET /accounts?page=0&size=20
```

#### Get account

```
GET /accounts/{id}
```

**Response:**

```json
{
  "id": 1,
  "name": "Default",
  "lastName": "User",
  "email": "user@deolhoneles.com"
}
```

#### Create account

```
POST /accounts
Content-Type: application/json

{
  "name": "Felipe",
  "lastName": "Faria",
  "email": "felipe@example.com"
}
```

**Response:** `201 Created`

#### Update account

```
PUT /accounts/{id}
Content-Type: application/json

{
  "name": "Felipe",
  "lastName": "Faria",
  "email": "felipe@example.com"
}
```

#### Delete account

```
DELETE /accounts/{id}
```

**Response:** `204 No Content`

#### Toggle follow

Toggles whether the account follows a deputy. If already following, unfollows. If not following, follows.

```
PUT /accounts/{accountId}/deputies/{deputyId}/follow
```

**Response:**

```json
{
  "id": 3,
  "name": "Fernanda Lima",
  "party": "MDB",
  "legend": "MG",
  "avatar": null,
  "follow": true
}
```

#### List followed deputies

```
GET /accounts/{accountId}/deputies
```

**Response:** array of `DeputyResponse`

---

### Deputies

#### List deputies

```
GET /deputies?accountId=1&followed=true&page=0&size=20
```

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `accountId` | Long | No | Account ID to resolve follow state. Without it, `follow` is always `false`. |
| `followed` | Boolean | No | Filter by follow state (`true`/`false`). Requires `accountId`. |
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
  "follow": true
}
```

#### Get deputy

```
GET /deputies/{id}?accountId=1
```

#### Create deputy

```
POST /deputies
Content-Type: application/json

{
  "name": "New Deputy",
  "party": "PT",
  "state": "SP",
  "avatar": null
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
  "avatar": null
}
```

#### Delete deputy

```
DELETE /deputies/{id}
```

**Response:** `204 No Content`

---

### Proposals

#### List proposals

```
GET /proposals?page=0&size=20
```

Returns proposals sorted by `voteDate` descending (most recent first).

**Response item:**

```json
{
  "id": 1,
  "title": "PL 1234/2026 - Reforma Tributaria",
  "summary": "Simplifica o sistema tributario brasileiro...",
  "author": "Comissao Especial da Camara",
  "category": "Economia",
  "voteDate": "2026-03-25"
}
```

#### Get proposal

```
GET /proposals/{id}
```

#### Create proposal

```
POST /proposals
Content-Type: application/json

{
  "title": "PL 9999/2026 - New Proposal",
  "summary": "Description of the proposal",
  "author": "Dep. Someone (PT-SP)",
  "category": "Economia",
  "voteDate": "2026-03-27"
}
```

**Response:** `201 Created`

#### Update proposal

```
PUT /proposals/{id}
Content-Type: application/json

{
  "title": "PL 9999/2026 - Updated Title",
  "summary": "Updated description",
  "author": "Dep. Someone (PT-SP)",
  "category": "Tecnologia",
  "voteDate": "2026-03-27"
}
```

#### Delete proposal

```
DELETE /proposals/{id}
```

**Response:** `204 No Content`

---

### Feed

Returns vote activity for deputies. Supports infinite scroll via pagination.

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

**Body:**

| Field | Type | Description |
|-------|------|-------------|
| `deputyIds` | Long[] | List of deputy IDs whose vote activity to return. Returns empty if null/empty. |

**Response item:**

```json
{
  "id": 1,
  "name": "PL 1234/2026 - Reforma Tributaria",
  "description": "Simplifica o sistema tributario brasileiro...",
  "deputieName": "Ana Souza",
  "deputieParty": "PSD - SP",
  "vote": "SIM",
  "category": "Economia",
  "author": "Comissao Especial da Camara",
  "voteDate": "2026-03-25",
  "deputyId": 1
}
```

**Vote values:** `SIM`, `NÃO`, `ABSTENÇÃO`, `AUSENTE`

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
    │   │   ├── AccountController.java
    │   │   ├── DeputyController.java
    │   │   ├── FeedController.java
    │   │   └── ProposalController.java
    │   ├── dto/
    │   │   ├── AccountRequest.java
    │   │   ├── AccountResponse.java
    │   │   ├── DeputyRequest.java
    │   │   ├── DeputyResponse.java
    │   │   ├── FeedItemResponse.java
    │   │   ├── FeedRequest.java
    │   │   ├── PageResponse.java
    │   │   ├── ProposalRequest.java
    │   │   └── ProposalResponse.java
    │   ├── entity/
    │   │   ├── Account.java
    │   │   ├── Deputy.java
    │   │   ├── DeputyVote.java
    │   │   ├── LegislativeProposal.java
    │   │   └── VoteType.java
    │   ├── repository/
    │   │   ├── AccountRepository.java
    │   │   ├── DeputyRepository.java
    │   │   ├── DeputyVoteRepository.java
    │   │   └── LegislativeProposalRepository.java
    │   └── service/
    │       ├── AccountService.java
    │       ├── DeputyService.java
    │       ├── FeedService.java
    │       └── ProposalService.java
    └── resources/
        ├── application.yml
        ├── application-docker.yml
        └── db/migration/
            ├── V1__create_schema.sql
            ├── V2__seed_data.sql
            └── V3__add_account_and_follow.sql
```
