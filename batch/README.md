# Batch Scripts

Python scripts that fetch data from the [Camara dos Deputados Open Data API](https://dadosabertos.camara.leg.br/api/v2) and persist it to the backend.

## Setup

```bash
cd batch
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Usage

### Sync everything (recommended)

```bash
python run_sync.py          # Deputies + votes from last 7 days
python run_sync.py 30       # Deputies + votes from last 30 days
```

### Sync individually

```bash
python sync_deputies.py     # Sync all current-legislature deputies
python sync_votes.py        # Sync votes from last 7 days
python sync_votes.py 30     # Sync votes from last 30 days
```

## How it works

### sync_deputies.py

1. Fetches all deputies from the current legislature (57th) via `GET /deputados`
2. For each deputy, checks if it already exists in the backend by external ID (`GET /deputies/external/{id}`)
3. If new, creates it via `POST /deputies` with the Camara API ID stored as `externalId`

### sync_votes.py

1. Fetches voting sessions from the last N days via `GET /votacoes`
2. For each session:
   - Checks if a proposal with that external ID already exists
   - If new, fetches details (`GET /votacoes/{id}`) and creates a proposal via `POST /proposals`
   - Fetches individual deputy votes (`GET /votacoes/{id}/votos`)
   - For each vote, looks up the deputy by external ID and creates the vote via `POST /votes`

### Idempotency

All scripts are safe to run repeatedly:
- Deputies are skipped if their `externalId` already exists
- Proposals are skipped if their `externalId` already exists
- Votes are skipped if the `(deputyId, proposalId)` pair already exists

### Order matters

Run `sync_deputies.py` before `sync_votes.py` — votes reference deputies by external ID. `run_sync.py` handles this automatically.

## Configuration

Edit `config.py` or set environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_URL` | `http://localhost:8080` | Backend API base URL |

## Rate limiting

The scripts add a 0.5s delay between Camara API calls to avoid overloading the public API.
