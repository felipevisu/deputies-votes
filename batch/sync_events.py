"""
Syncs events from the Camara dos Deputados API.

Flow:
1. Pre-load all deputies into a cache (one call)
2. Fetch recent events from GET /eventos
3. Process events in parallel:
   a. Fetch details from GET /eventos/{id}
   b. Create event in the backend
   c. Fetch attendees from GET /eventos/{id}/deputados
   d. Batch-link deputies via POST /events/{id}/deputies/batch
   e. For deliberative events, fetch agenda and build summary

Safe to run multiple times — skips events that already exist.
"""

import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import date, timedelta
import requests
from config import CAMARA_API_BASE, BACKEND_BASE, REQUEST_DELAY


RELEVANT_TYPES = {
    "Sessão Deliberativa",
    "Sessão Deliberativa Extraordinária",
    "Reunião Deliberativa",
    "Reunião Deliberativa Extraordinária",
    "Audiência Pública",
    "Audiência Pública e Deliberação",
}

PARALLEL_WORKERS = 4


def load_deputy_cache():
    """Load all deputies from backend into an external_id -> id map."""
    resp = requests.get(f"{BACKEND_BASE}/deputies", params={"page": 0, "size": 1000})
    resp.raise_for_status()
    deputies = resp.json().get("content", [])
    cache = {}
    for d in deputies:
        ext_id = d.get("externalId")
        if ext_id:
            cache[int(ext_id)] = d["id"]
    return cache


def fetch_events_paged(start_date, end_date):
    """Yield event pages from the Camara API."""
    page = 1
    while True:
        print(f"  Fetching events page {page}...")
        resp = requests.get(f"{CAMARA_API_BASE}/eventos", params={
            "dataInicio": start_date,
            "dataFim": end_date,
            "itens": 100,
            "pagina": page,
            "ordem": "DESC",
            "ordenarPor": "dataHoraInicio",
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


def fetch_event_details(event_id):
    """Fetch full details of an event."""
    resp = requests.get(f"{CAMARA_API_BASE}/eventos/{event_id}")
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json().get("dados", {})


def fetch_event_deputies(event_id):
    """Fetch deputies who attended an event."""
    resp = requests.get(f"{CAMARA_API_BASE}/eventos/{event_id}/deputados")
    if resp.status_code == 404:
        return []
    resp.raise_for_status()
    return resp.json().get("dados", [])


def fetch_event_pauta(event_id):
    """Fetch agenda items for a deliberative event."""
    resp = requests.get(f"{CAMARA_API_BASE}/eventos/{event_id}/pauta")
    if resp.status_code == 404:
        return []
    resp.raise_for_status()
    return resp.json().get("dados", [])


def event_exists(external_id):
    """Check if an event with this external ID already exists."""
    resp = requests.get(f"{BACKEND_BASE}/events/external/{external_id}")
    return resp.status_code == 200


def build_location(event):
    """Build location string from event data."""
    local_externo = event.get("localExterno")
    if local_externo:
        return local_externo
    local_camara = event.get("localCamara") or {}
    return local_camara.get("nome") or "Camara dos Deputados"


def build_agenda_summary(pauta_items):
    """Summarize agenda items into a single string."""
    if not pauta_items:
        return None

    parts = []
    for item in pauta_items:
        related = item.get("proposicaoRelacionada_") or {}
        sigla = related.get("siglaTipo", "")
        numero = related.get("numero", "")
        ano = related.get("ano", "")
        ementa = related.get("ementa", "")

        if not sigla:
            prop = item.get("proposicao_") or {}
            sigla = prop.get("siglaTipo", "")
            ementa = ementa or prop.get("ementa", "")

        title = item.get("titulo", "")
        situation = item.get("situacaoItem", "")

        bill = f"{sigla} {numero}/{ano}" if sigla and numero and ano else title
        desc = ementa[:120] if ementa else ""

        entry = bill
        if desc:
            entry += f" — {desc}"
        if situation:
            entry += f" ({situation})"

        if entry.strip():
            parts.append(entry.strip())

    return " · ".join(parts) if parts else None


def create_event(event_data, details, agenda_summary):
    """Create an event in the backend."""
    orgaos = event_data.get("orgaos", [])
    organ = orgaos[0] if orgaos else {}

    start_time = event_data.get("dataHoraInicio", "")
    event_date = start_time[:10] if start_time else ""

    payload = {
        "externalId": event_data["id"],
        "eventType": event_data.get("descricaoTipo", ""),
        "description": event_data.get("descricao", ""),
        "agendaSummary": agenda_summary,
        "situation": event_data.get("situacao", ""),
        "startTime": start_time,
        "endTime": event_data.get("dataHoraFim", ""),
        "location": build_location(event_data),
        "organCode": organ.get("sigla", ""),
        "organName": organ.get("nome", ""),
        "videoUrl": event_data.get("urlRegistro", ""),
        "eventDate": event_date,
    }
    resp = requests.post(f"{BACKEND_BASE}/events", json=payload)
    resp.raise_for_status()
    return resp.json()


def link_deputies_batch(backend_event_id, deputy_ids):
    """Batch-link deputies to an event."""
    if not deputy_ids:
        return 0
    payload = [{"deputyId": did} for did in deputy_ids]
    resp = requests.post(f"{BACKEND_BASE}/events/{backend_event_id}/deputies/batch", json=payload)
    if resp.status_code in (200, 201):
        return len(deputy_ids)
    return 0


def process_event(event_data, deputy_cache):
    """Process a single event. Returns (ext_id, status, deputies_linked)."""
    ext_id = event_data["id"]

    event_type = event_data.get("descricaoTipo", "")
    if event_type not in RELEVANT_TYPES:
        return ext_id, "skipped_type", 0

    if event_exists(ext_id):
        return ext_id, "exists", 0

    time.sleep(REQUEST_DELAY)
    details = fetch_event_details(ext_id)
    if details is None:
        return ext_id, "not_found", 0

    # Fetch agenda for deliberative events
    agenda_summary = None
    is_deliberative = "Deliberativa" in event_type
    if is_deliberative:
        time.sleep(REQUEST_DELAY)
        pauta = fetch_event_pauta(ext_id)
        agenda_summary = build_agenda_summary(pauta)

    try:
        created = create_event(event_data, details, agenda_summary)
        backend_id = created["id"]
    except Exception as e:
        return ext_id, f"error: {e}", 0

    # Fetch and link deputies
    time.sleep(REQUEST_DELAY)
    camara_deputies = fetch_event_deputies(ext_id)

    matched_ids = []
    for dep in camara_deputies:
        dep_ext_id = dep.get("id")
        if dep_ext_id and int(dep_ext_id) in deputy_cache:
            matched_ids.append(deputy_cache[int(dep_ext_id)])

    deputies_linked = link_deputies_batch(backend_id, matched_ids)

    return ext_id, "created", deputies_linked


def sync(days_back=7, start_date=None, end_date=None, cancel_check=None):
    if start_date and end_date:
        start = start_date
        end = end_date
    else:
        end = date.today()
        start = end - timedelta(days=days_back)

    print("=== Syncing Events ===")
    print(f"Date range: {start} to {end}")
    print(f"Source: {CAMARA_API_BASE}/eventos")
    print(f"Target: {BACKEND_BASE}")
    print(f"Workers: {PARALLEL_WORKERS}\n")

    print("Loading deputy cache...")
    deputy_cache = load_deputy_cache()
    print(f"Cached {len(deputy_cache)} deputies\n")

    print("Fetching all events...")
    all_events = []
    for page in fetch_events_paged(str(start), str(end)):
        all_events.extend(page)
    print(f"Found {len(all_events)} events to process\n")

    events_created = 0
    events_skipped = 0
    deputies_linked = 0
    cancelled = False

    with ThreadPoolExecutor(max_workers=PARALLEL_WORKERS) as executor:
        futures = {}
        for event_data in all_events:
            if cancel_check and cancel_check():
                cancelled = True
                break
            future = executor.submit(process_event, event_data, deputy_cache)
            futures[future] = event_data["id"]

        for future in as_completed(futures):
            if cancel_check and cancel_check():
                cancelled = True
                executor.shutdown(wait=False, cancel_futures=True)
                break

            ext_id, status, linked = future.result()

            if status == "exists":
                events_skipped += 1
                print(f"  [{ext_id}] Already exists")
            elif status == "skipped_type":
                pass
            elif status == "not_found":
                print(f"  [{ext_id}] Skipped (404)")
            elif status.startswith("error"):
                print(f"  [{ext_id}] {status}")
            else:
                events_created += 1
                deputies_linked += linked
                print(f"  [{ext_id}] Created — {linked} deputies linked")

    print(f"\n=== Summary ===")
    if cancelled:
        print("Cancelled by user")
    print(f"Events: {events_created} created, {events_skipped} already existed")
    print(f"Deputies linked: {deputies_linked}")

    return events_created


if __name__ == "__main__":
    days = int(sys.argv[1]) if len(sys.argv) > 1 else 7
    sync(days_back=days)
