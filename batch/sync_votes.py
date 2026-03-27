"""
Syncs voting sessions and individual deputy votes from the Camara API.

Flow:
1. Fetch recent voting sessions from GET /votacoes
2. For each session, fetch details from GET /votacoes/{id}
3. Create a proposal in the backend (if not already present by external ID)
4. Fetch individual votes from GET /votacoes/{id}/votos
5. For each deputy vote, look up the deputy by external ID and create the vote

Safe to run multiple times — skips proposals and votes that already exist.
"""

import sys
import time
from datetime import date, timedelta
import requests
from config import CAMARA_API_BASE, BACKEND_BASE, REQUEST_DELAY


VOTE_TYPE_MAP = {
    "Sim": "SIM",
    "Não": "NÃO",
    "Abstenção": "ABSTENÇÃO",
    "Obstrução": "AUSENTE",
    "Art. 17": "AUSENTE",
}

CATEGORY_MAP = {
    "PLEN": "Plenario",
    "CCJC": "Justica",
    "CFT": "Economia",
    "CSSF": "Saude",
    "CE": "Educacao",
    "CMADS": "Meio Ambiente",
    "CSPCCO": "Seguranca",
    "CCTCI": "Tecnologia",
    "CDU": "Habitacao",
    "CVT": "Transporte",
    "CTASP": "Trabalho",
    "CDC": "Comunicacao",
}


def fetch_voting_sessions(start_date, end_date):
    """Fetch voting sessions from the Camara API within a date range."""
    sessions = []
    page = 1

    while True:
        print(f"  Fetching voting sessions page {page}...")
        resp = requests.get(f"{CAMARA_API_BASE}/votacoes", params={
            "dataInicio": start_date,
            "dataFim": end_date,
            "itens": 100,
            "pagina": page,
            "ordem": "DESC",
            "ordenarPor": "dataHoraRegistro",
        })
        resp.raise_for_status()
        data = resp.json()

        batch = data.get("dados", [])
        if not batch:
            break

        sessions.extend(batch)

        links = data.get("links", [])
        has_next = any(link.get("rel") == "next" for link in links)
        if not has_next:
            break

        page += 1
        time.sleep(REQUEST_DELAY)

    return sessions


def fetch_vote_details(vote_id):
    """Fetch full details of a voting session. Returns None on 404."""
    resp = requests.get(f"{CAMARA_API_BASE}/votacoes/{vote_id}")
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json().get("dados", {})


def fetch_individual_votes(vote_id):
    """Fetch individual deputy votes for a voting session. Returns empty on 404."""
    resp = requests.get(f"{CAMARA_API_BASE}/votacoes/{vote_id}/votos")
    if resp.status_code == 404:
        return []
    resp.raise_for_status()
    return resp.json().get("dados", [])


def proposal_exists(external_id):
    """Check if a proposal with this external ID already exists."""
    resp = requests.get(f"{BACKEND_BASE}/proposals/external/{external_id}")
    return resp.status_code == 200, resp.json() if resp.status_code == 200 else None


def find_deputy_by_external_id(external_id):
    """Look up a deputy by their Camara API ID."""
    resp = requests.get(f"{BACKEND_BASE}/deputies/external/{external_id}")
    if resp.status_code == 200:
        return resp.json()
    return None


def create_proposal(session, details):
    """Create a proposal from a Camara voting session."""
    vote_date = session.get("data", "")[:10]
    title = session.get("proposicaoTexto") or details.get("descricao") or "Voting Session"
    summary = details.get("objVotacao") or details.get("descricao") or title
    organ = session.get("siglaOrgao", "")
    category = CATEGORY_MAP.get(organ, "Legislativo")

    # Truncate title if too long
    if len(title) > 500:
        title = title[:497] + "..."

    payload = {
        "title": title,
        "summary": summary,
        "author": organ or "Camara dos Deputados",
        "category": category,
        "voteDate": vote_date,
        "externalId": str(session["id"]),
    }
    resp = requests.post(f"{BACKEND_BASE}/proposals", json=payload)
    resp.raise_for_status()
    return resp.json()


def create_vote(deputy_id, proposal_id, vote_type):
    """Create a deputy vote in the backend."""
    payload = {
        "deputyId": deputy_id,
        "proposalId": proposal_id,
        "vote": vote_type,
    }
    resp = requests.post(f"{BACKEND_BASE}/votes", json=payload)
    if resp.status_code == 201:
        return True
    # 500 likely means duplicate — skip silently
    return False


def map_vote_type(camara_vote):
    """Map Camara API vote type to our backend enum."""
    return VOTE_TYPE_MAP.get(camara_vote, "AUSENTE")


def sync(days_back=7):
    end = date.today()
    start = end - timedelta(days=days_back)

    print("=== Syncing Votes ===")
    print(f"Date range: {start} to {end}")
    print(f"Source: {CAMARA_API_BASE}/votacoes")
    print(f"Target: {BACKEND_BASE}\n")

    sessions = fetch_voting_sessions(str(start), str(end))
    print(f"\nFetched {len(sessions)} voting sessions\n")

    proposals_created = 0
    proposals_skipped = 0
    votes_created = 0
    votes_skipped = 0
    deputies_not_found = 0

    for session in sessions:
        session_id = str(session["id"])
        title = session.get("proposicaoTexto", "")[:80] or session_id
        print(f"Processing: {title}")

        # Check if proposal already exists
        exists, existing_proposal = proposal_exists(session_id)
        if exists:
            proposal_id = existing_proposal["id"]
            proposals_skipped += 1
            print(f"  Proposal already exists (id={proposal_id}), checking votes...")
        else:
            # Fetch details and create proposal
            time.sleep(REQUEST_DELAY)
            details = fetch_vote_details(session_id)
            if details is None:
                print(f"  Skipped (404 from Camara API)")
                continue
            try:
                proposal = create_proposal(session, details)
                proposal_id = proposal["id"]
                proposals_created += 1
                print(f"  + Created proposal (id={proposal_id})")
            except Exception as e:
                print(f"  ! Error creating proposal: {e}")
                continue

        # Fetch and create individual votes
        time.sleep(REQUEST_DELAY)
        individual_votes = fetch_individual_votes(session_id)
        print(f"  {len(individual_votes)} individual votes found")

        for iv in individual_votes:
            deputy_ext_id = iv.get("deputado_", {}).get("id")
            if not deputy_ext_id:
                continue

            deputy = find_deputy_by_external_id(deputy_ext_id)
            if not deputy:
                deputies_not_found += 1
                continue

            vote_type = map_vote_type(iv.get("tipoVoto", ""))
            if create_vote(deputy["id"], proposal_id, vote_type):
                votes_created += 1
            else:
                votes_skipped += 1

        time.sleep(REQUEST_DELAY)

    print(f"\n=== Summary ===")
    print(f"Proposals: {proposals_created} created, {proposals_skipped} skipped")
    print(f"Votes: {votes_created} created, {votes_skipped} skipped (duplicates)")
    if deputies_not_found:
        print(f"Deputies not found: {deputies_not_found} (run sync_deputies.py first)")

    return proposals_created, votes_created


if __name__ == "__main__":
    days = int(sys.argv[1]) if len(sys.argv) > 1 else 7
    sync(days_back=days)
