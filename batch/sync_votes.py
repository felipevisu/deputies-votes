"""
Syncs voting sessions and individual deputy votes from the Camara API.

Flow:
1. Pre-load all deputies into a cache (one call)
2. Fetch recent voting sessions from GET /votacoes
3. Process sessions in parallel using a thread pool:
   a. Fetch details from GET /votacoes/{id}
   b. Create an activity in the backend (if not already present by external ID)
   c. Fetch individual votes from GET /votacoes/{id}/votos
   d. Bulk-create all votes in one request via POST /votes/batch

Safe to run multiple times — skips activities and votes that already exist.
"""

import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
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

ORGAN_DISPLAY = {
    "PLEN": "Plenario da Camara",
    "CCJC": "Comissao de Constituicao e Justica",
    "CFT": "Comissao de Financas e Tributacao",
    "CSSF": "Comissao de Seguridade Social",
    "CE": "Comissao de Educacao",
    "CMADS": "Comissao de Meio Ambiente",
    "CSPCCO": "Comissao de Seguranca Publica",
    "CCTCI": "Comissao de Ciencia e Tecnologia",
    "CDU": "Comissao de Desenvolvimento Urbano",
    "CVT": "Comissao de Viacao e Transportes",
    "CTASP": "Comissao de Trabalho",
    "CDC": "Comissao de Comunicacao",
    "CPASF": "Comissao de Assistencia Social",
    "CDEICS": "Comissao de Desenvolvimento Economico",
    "CAPADR": "Comissao de Agricultura",
    "CREDN": "Comissao de Relacoes Exteriores",
    "CTUR": "Comissao de Turismo",
    "CMULHER": "Comissao de Defesa dos Direitos da Mulher",
    "CINDRA": "Comissao de Integracao Nacional",
    "CSEC": "Comissao de Seguranca Publica",
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

PARALLEL_WORKERS = 4


def load_deputy_cache():
    """Load all deputies from backend into an external_id → id map."""
    resp = requests.get(f"{BACKEND_BASE}/deputies", params={"page": 0, "size": 1000})
    resp.raise_for_status()
    deputies = resp.json().get("content", [])
    cache = {}
    for d in deputies:
        ext_id = d.get("externalId")
        if ext_id:
            cache[int(ext_id)] = d["id"]
    return cache


def fetch_voting_sessions_paged(start_date, end_date):
    """Yield voting sessions one page at a time from the Camara API (newest first)."""
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

        yield batch

        links = data.get("links", [])
        has_next = any(link.get("rel") == "next" for link in links)
        if not has_next:
            break

        page += 1
        time.sleep(REQUEST_DELAY)


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


def activity_exists(external_id):
    """Check if an activity with this external ID already exists."""
    resp = requests.get(f"{BACKEND_BASE}/activities/external/{external_id}")
    return resp.status_code == 200, resp.json() if resp.status_code == 200 else None


def extract_activity_info(session, details):
    affected = details.get("proposicoesAfetadas", [])

    if affected:
        prop = affected[0]
        sigla = prop.get("siglaTipo", "")
        numero = prop.get("numero", "")
        ano = prop.get("ano", "")
        ementa = prop.get("ementa", "")

        bill_id = f"{sigla} {numero}/{ano}" if sigla and numero and ano else ""
        title = bill_id or ementa or None
        summary = ementa or None
    else:
        title = None
        summary = None

    if not title:
        title = (details.get("objVotacao")
                 or session.get("proposicaoTexto")
                 or details.get("descricao")
                 or "Votacao")
    if not summary:
        summary = (details.get("objVotacao")
                   or details.get("descricao")
                   or title)

    organ = session.get("siglaOrgao", "")
    author = ORGAN_DISPLAY.get(organ, organ) or "Camara dos Deputados"

    return title, summary, author


def create_activity(session, details):
    """Create an activity from a Camara voting session."""
    vote_date = session.get("data", "")[:10]
    organ = session.get("siglaOrgao", "")
    category = CATEGORY_MAP.get(organ, "Legislativo")

    title, summary, author = extract_activity_info(session, details)

    if len(title) > 500:
        title = title[:497] + "..."

    vote_round = session.get("descUltimaAberturaVotacao") or details.get("descUltimaAberturaVotacao") or ""
    vote_round = vote_round.strip().rstrip(".")

    description = details.get("descricao", "")

    payload = {
        "title": title,
        "summary": summary,
        "author": author,
        "category": category,
        "voteDate": vote_date,
        "externalId": str(session["id"]),
        "description": description,
        "voteRound": vote_round,
    }
    resp = requests.post(f"{BACKEND_BASE}/activities", json=payload)
    resp.raise_for_status()
    return resp.json()


def create_votes_batch(votes_payload):
    """Create multiple votes in one request. Returns count of created votes."""
    if not votes_payload:
        return 0
    resp = requests.post(f"{BACKEND_BASE}/votes/batch", json=votes_payload)
    if resp.status_code in (200, 201):
        return len(resp.json())
    return 0


def map_vote_type(camara_vote):
    """Map Camara API vote type to our backend enum."""
    return VOTE_TYPE_MAP.get(camara_vote, "AUSENTE")


def process_session(session, deputy_cache):
    """Process a single voting session. Returns (activities_created, votes_created, deputies_not_found)."""
    session_id = str(session["id"])

    exists, _ = activity_exists(session_id)
    if exists:
        return session_id, "exists", 0, 0, 0

    time.sleep(REQUEST_DELAY)
    details = fetch_vote_details(session_id)
    if details is None:
        return session_id, "skipped", 0, 0, 0

    try:
        activity = create_activity(session, details)
        activity_id = activity["id"]
    except Exception as e:
        return session_id, f"error: {e}", 0, 0, 0

    # Enrich with AI summary
    try:
        from enrich_activities import enrich_activity
        enrich_activity(activity)
    except Exception:
        pass

    # Fetch individual votes and build batch payload
    time.sleep(REQUEST_DELAY)
    individual_votes = fetch_individual_votes(session_id)

    batch_payload = []
    deputies_not_found = 0

    for iv in individual_votes:
        deputy_ext_id = iv.get("deputado_", {}).get("id")
        if not deputy_ext_id:
            continue

        deputy_id = deputy_cache.get(int(deputy_ext_id))
        if not deputy_id:
            deputies_not_found += 1
            continue

        vote_type = map_vote_type(iv.get("tipoVoto", ""))
        batch_payload.append({
            "deputyId": deputy_id,
            "activityId": activity_id,
            "vote": vote_type,
        })

    votes_created = create_votes_batch(batch_payload)

    return session_id, "created", 1, votes_created, deputies_not_found


def sync(days_back=7, start_date=None, end_date=None, cancel_check=None):
    if start_date and end_date:
        start = start_date
        end = end_date
    else:
        end = date.today()
        start = end - timedelta(days=days_back)

    print("=== Syncing Votes ===")
    print(f"Date range: {start} to {end}")
    print(f"Source: {CAMARA_API_BASE}/votacoes")
    print(f"Target: {BACKEND_BASE}")
    print(f"Workers: {PARALLEL_WORKERS}\n")

    # Pre-load deputy cache
    print("Loading deputy cache...")
    deputy_cache = load_deputy_cache()
    print(f"Cached {len(deputy_cache)} deputies\n")

    # Collect all sessions first
    print("Fetching all voting sessions...")
    all_sessions = []
    for page in fetch_voting_sessions_paged(str(start), str(end)):
        all_sessions.extend(page)
    print(f"Found {len(all_sessions)} sessions to process\n")

    activities_created = 0
    activities_skipped = 0
    votes_created = 0
    deputies_not_found = 0
    cancelled = False

    # Process sessions in parallel
    with ThreadPoolExecutor(max_workers=PARALLEL_WORKERS) as executor:
        futures = {}
        for session in all_sessions:
            if cancel_check and cancel_check():
                cancelled = True
                break
            future = executor.submit(process_session, session, deputy_cache)
            futures[future] = session["id"]

        for future in as_completed(futures):
            if cancel_check and cancel_check():
                cancelled = True
                executor.shutdown(wait=False, cancel_futures=True)
                break

            session_id, status, acts, votes, not_found = future.result()

            if status == "exists":
                activities_skipped += 1
                print(f"  [{session_id}] Already exists")
            elif status == "skipped":
                print(f"  [{session_id}] Skipped (404)")
            elif status.startswith("error"):
                print(f"  [{session_id}] {status}")
            else:
                activities_created += acts
                votes_created += votes
                deputies_not_found += not_found
                print(f"  [{session_id}] Created — {votes} votes")

    print(f"\n=== Summary ===")
    if cancelled:
        print(f"Cancelled by user")
    print(f"Activities: {activities_created} created, {activities_skipped} already existed")
    print(f"Votes: {votes_created} created")
    if deputies_not_found:
        print(f"Deputies not found: {deputies_not_found} (run sync_deputies.py first)")

    return activities_created, votes_created


if __name__ == "__main__":
    days = int(sys.argv[1]) if len(sys.argv) > 1 else 7
    sync(days_back=days)
