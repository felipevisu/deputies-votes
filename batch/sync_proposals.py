"""
Syncs proposals authored by deputies from the Camara API.

Flow:
1. Fetch all deputies from the backend
2. For each deputy, fetch proposals they authored from GET /proposicoes
3. For each new proposal, fetch details and authors
4. Create proposal and author links in the backend

Safe to run multiple times — skips proposals that already exist.
"""

import time
from datetime import date, timedelta
import requests
from config import CAMARA_API_BASE, BACKEND_BASE, REQUEST_DELAY


def fetch_deputies_from_backend():
    """Fetch all deputies from our backend."""
    resp = requests.get(f"{BACKEND_BASE}/deputies", params={"page": 0, "size": 1000})
    resp.raise_for_status()
    return resp.json().get("content", [])


def fetch_proposals_for_deputy_paged(deputy_external_id, start_date, end_date):
    """Yield pages of proposals authored by a deputy (newest first)."""
    page = 1
    while True:
        resp = requests.get(f"{CAMARA_API_BASE}/proposicoes", params={
            "idDeputadoAutor": deputy_external_id,
            "dataApresentacaoInicio": start_date,
            "dataApresentacaoFim": end_date,
            "itens": 100,
            "pagina": page,
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


def fetch_proposal_details(proposal_id):
    """Fetch full details of a proposal. Returns None on 404."""
    resp = requests.get(f"{CAMARA_API_BASE}/proposicoes/{proposal_id}")
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json().get("dados", {})


def fetch_proposal_authors(proposal_id):
    """Fetch authors of a proposal. Returns empty on 404."""
    resp = requests.get(f"{CAMARA_API_BASE}/proposicoes/{proposal_id}/autores")
    if resp.status_code == 404:
        return []
    resp.raise_for_status()
    return resp.json().get("dados", [])


def proposal_exists(external_id):
    """Check if a proposal with this external ID already exists in the backend."""
    resp = requests.get(f"{BACKEND_BASE}/proposals/external/{external_id}")
    return resp.status_code == 200, resp.json() if resp.status_code == 200 else None


def create_proposal(details):
    """Create a proposal in the backend."""
    presentation_date = (details.get("dataApresentacao") or "")[:10]
    status = details.get("statusProposicao") or {}
    status_desc = status.get("descricaoSituacao", "")

    payload = {
        "externalId": details["id"],
        "typeCode": details.get("siglaTipo", ""),
        "number": details.get("numero", 0),
        "year": details.get("ano", 0),
        "ementa": details.get("ementa", ""),
        "keywords": details.get("keywords"),
        "presentationDate": presentation_date,
        "statusDescription": status_desc,
    }
    resp = requests.post(f"{BACKEND_BASE}/proposals", json=payload)
    resp.raise_for_status()
    return resp.json()


def add_proposal_author(proposal_id, deputy_id, signing_order, proponent):
    """Link a deputy as author of a proposal."""
    payload = {
        "deputyId": deputy_id,
        "signingOrder": signing_order,
        "proponent": proponent,
    }
    resp = requests.post(f"{BACKEND_BASE}/proposals/{proposal_id}/authors", json=payload)
    if resp.status_code == 201:
        return True
    return False


def extract_deputy_id_from_uri(uri):
    """Extract the numeric deputy ID from a Camara API URI."""
    if not uri:
        return None
    try:
        return int(uri.rstrip("/").split("/")[-1])
    except (ValueError, IndexError):
        return None


def find_deputy_by_external_id(external_id):
    """Look up a deputy by their Camara API ID."""
    resp = requests.get(f"{BACKEND_BASE}/deputies/external/{external_id}")
    if resp.status_code == 200:
        return resp.json()
    return None


def sync(days_back=30, start_date=None, end_date=None, cancel_check=None):
    if start_date and end_date:
        start = start_date
        end = end_date
    else:
        end = date.today()
        start = end - timedelta(days=days_back)

    print("=== Syncing Proposals ===")
    print(f"Date range: {start} to {end}")
    print(f"Source: {CAMARA_API_BASE}/proposicoes")
    print(f"Target: {BACKEND_BASE}\n")

    deputies = fetch_deputies_from_backend()
    print(f"Found {len(deputies)} deputies in backend\n")

    proposals_created = 0
    proposals_skipped = 0
    authors_linked = 0
    cancelled = False

    for dep in deputies:
        if cancel_check and cancel_check():
            print("\n⛔ Cancelled by user")
            cancelled = True
            break

        dep_ext_id = dep.get("externalId")
        if not dep_ext_id:
            continue

        dep_name = dep.get("name", "?")
        print(f"Deputy: {dep_name} (ext:{dep_ext_id})")

        stopped_early = False
        dep_proposals = 0

        for page in fetch_proposals_for_deputy_paged(dep_ext_id, str(start), str(end)):
            for prop in page:
                if cancel_check and cancel_check():
                    print("\n⛔ Cancelled by user")
                    cancelled = True
                    break

                prop_id = prop["id"]

                exists, existing = proposal_exists(prop_id)
                if exists:
                    proposals_skipped += 1
                    print(f"  Already exists ({prop.get('siglaTipo', '')} {prop.get('numero', '')}/{prop.get('ano', '')}), stopping for this deputy")
                    stopped_early = True
                    break

                time.sleep(REQUEST_DELAY)
                details = fetch_proposal_details(prop_id)
                if details is None:
                    print(f"  Skipped {prop_id} (404)")
                    continue

                try:
                    created = create_proposal(details)
                    backend_id = created["id"]
                    proposals_created += 1
                    dep_proposals += 1
                    sigla = details.get("siglaTipo", "")
                    numero = details.get("numero", "")
                    ano = details.get("ano", "")
                    print(f"  + {sigla} {numero}/{ano} (id={backend_id})")
                except Exception as e:
                    print(f"  ! Error creating proposal {prop_id}: {e}")
                    continue

                # Fetch and link authors
                time.sleep(REQUEST_DELAY)
                camara_authors = fetch_proposal_authors(prop_id)
                for author in camara_authors:
                    if author.get("codTipo") != 10000:
                        continue
                    author_ext_id = extract_deputy_id_from_uri(author.get("uri"))
                    if not author_ext_id:
                        continue
                    deputy = find_deputy_by_external_id(author_ext_id)
                    if not deputy:
                        continue
                    order = author.get("ordemAssinatura", 1)
                    is_proponent = author.get("proponente", 0) == 1
                    if add_proposal_author(backend_id, deputy["id"], order, is_proponent):
                        authors_linked += 1

                time.sleep(REQUEST_DELAY)

            if stopped_early or cancelled:
                break

        if dep_proposals > 0:
            print(f"  {dep_proposals} proposals synced")

        if cancelled:
            break

    print(f"\n=== Summary ===")
    if cancelled:
        print("Cancelled by user")
    print(f"Proposals: {proposals_created} created, {proposals_skipped} already existed")
    print(f"Authors linked: {authors_linked}")

    return proposals_created


if __name__ == "__main__":
    import sys
    days = int(sys.argv[1]) if len(sys.argv) > 1 else 90
    sync(days_back=days)
