"""
Syncs deputies from the Camara dos Deputados API into the backend.

Fetches all current-legislature deputies, checks if each already exists
(by external ID), and creates new ones via POST /deputies.

Safe to run multiple times — skips deputies that already exist.
"""

import time
import requests
from config import CAMARA_API_BASE, BACKEND_BASE, LEGISLATURE_ID, REQUEST_DELAY


def fetch_all_deputies():
    """Fetch all deputies from the Camara API, paginating through all pages."""
    deputies = []
    page = 1

    while True:
        print(f"  Fetching deputies page {page}...")
        resp = requests.get(f"{CAMARA_API_BASE}/deputados", params={
            "idLegislatura": LEGISLATURE_ID,
            "itens": 100,
            "pagina": page,
            "ordenarPor": "nome",
            "ordem": "ASC",
        })
        resp.raise_for_status()
        data = resp.json()

        batch = data.get("dados", [])
        if not batch:
            break

        deputies.extend(batch)

        # Check if there's a next page
        links = data.get("links", [])
        has_next = any(link.get("rel") == "next" for link in links)
        if not has_next:
            break

        page += 1
        time.sleep(REQUEST_DELAY)

    return deputies


def deputy_exists(external_id):
    """Check if a deputy with this external ID already exists in the backend."""
    resp = requests.get(f"{BACKEND_BASE}/deputies/external/{external_id}")
    return resp.status_code == 200


def create_deputy(camara_deputy):
    """Create a deputy in the backend from Camara API data."""
    payload = {
        "name": camara_deputy["nome"],
        "party": camara_deputy["siglaPartido"],
        "state": camara_deputy["siglaUf"],
        "avatar": camara_deputy.get("urlFoto"),
        "externalId": camara_deputy["id"],
    }
    resp = requests.post(f"{BACKEND_BASE}/deputies", json=payload)
    resp.raise_for_status()
    return resp.json()


def sync():
    print("=== Syncing Deputies ===")
    print(f"Source: {CAMARA_API_BASE}/deputados (legislature {LEGISLATURE_ID})")
    print(f"Target: {BACKEND_BASE}/deputies\n")

    deputies = fetch_all_deputies()
    print(f"\nFetched {len(deputies)} deputies from Camara API\n")

    created = 0
    skipped = 0

    for dep in deputies:
        ext_id = dep["id"]
        name = dep["nome"]

        if deputy_exists(ext_id):
            skipped += 1
            continue

        try:
            create_deputy(dep)
            created += 1
            print(f"  + Created: {name} ({dep['siglaPartido']}-{dep['siglaUf']}) [ext:{ext_id}]")
        except Exception as e:
            print(f"  ! Error creating {name}: {e}")

    print(f"\nDone: {created} created, {skipped} skipped (already exist)")
    return created


if __name__ == "__main__":
    sync()
