"""
Enriches legislative activities with AI-generated summaries.

Flow per activity:
1. Fetch voting session details from Camara API (GET /votacoes/{externalId})
2. Get proposicoesAfetadas[0].uri → fetch proposal details → get urlInteiroTeor (PDF)
3. Download PDF, extract text with Docling
4. Send PDF text + API JSON to OpenAI for a simple 2-paragraph summary in Portuguese
5. Update the activity's summary via PUT /activities/{id}

Usage:
    python enrich_activities.py              # Enrich all activities without enriched summaries
    python enrich_activities.py --id 42      # Enrich a single activity by backend ID
"""

import argparse
import json
import os
import tempfile
import time

from dotenv import load_dotenv
load_dotenv()

import anthropic
import requests

from config import CAMARA_API_BASE, BACKEND_BASE, REQUEST_DELAY

ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")

SYSTEM_PROMPT = """Você é um assistente que explica projetos de lei e votações do Congresso brasileiro em linguagem simples.

Você deve retornar EXATAMENTE neste formato (duas linhas separadas por uma linha em branco):

SUBTITULO: (descrição curta e clara do que está sendo votado, máximo 80 caracteres, sem siglas como PL/PEC/MPV)

RESUMO: (resumo de no máximo dois parágrafos curtos)

Regras:
- O subtítulo deve ser descritivo e acessível (ex: "Reajuste salarial das forças de segurança do DF").
- NÃO inclua a sigla e número do projeto no subtítulo (ex: NÃO escreva "PLP 77/2026 — ...").
- Use linguagem acessível para alguém sem conhecimento político.
- Explique O QUE está sendo votado e POR QUE isso importa para o cidadão comum.
- Mencione o estágio atual da tramitação (se aprovado, rejeitado, em análise, etc).
- Não use jargão jurídico ou legislativo sem explicar.
- Escreva em português brasileiro.
- NÃO use formatação Markdown (sem #, *, **, ```, etc). Retorne apenas texto puro.

IMPORTANTE — Diferenciação entre rodadas de votação:
- Um mesmo projeto de lei pode ter MÚLTIPLAS votações (turno único, destaques, emendas, requerimentos).
- O campo "descUltimaAberturaVotacao" indica QUAL ETAPA específica está sendo votada.
- O campo "descricao" indica o RESULTADO dessa etapa específica.
- O subtítulo e o resumo DEVEM refletir a etapa específica, não apenas o projeto em geral.
- Se for um destaque (DTQ), explique que é uma votação separada de um trecho específico do projeto.
- Se for votação em turno único, diga que é a votação principal do projeto.
- Se for requerimento, explique o que o requerimento pede.
- Inclua no resumo o resultado da votação (aprovado/rejeitado/mantido, com placar)."""


def fetch_all_activities():
    """Fetch all activities from the backend."""
    activities = []
    page = 0
    while True:
        resp = requests.get(f"{BACKEND_BASE}/activities", params={"page": page, "size": 100})
        resp.raise_for_status()
        data = resp.json()
        activities.extend(data["content"])
        if data["last"]:
            break
        page += 1
    return activities


def fetch_voting_details(external_id):
    """Fetch voting session details from Camara API."""
    resp = requests.get(f"{CAMARA_API_BASE}/votacoes/{external_id}")
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json().get("dados", {})


def fetch_proposal_details(proposal_uri):
    """Fetch proposal details from a Camara API URI."""
    resp = requests.get(proposal_uri)
    if resp.status_code == 404:
        return None
    resp.raise_for_status()
    return resp.json().get("dados", {})


def download_pdf(url, dest_path):
    """Download a PDF file."""
    resp = requests.get(url, timeout=60)
    resp.raise_for_status()
    with open(dest_path, "wb") as f:
        f.write(resp.content)


def extract_pdf_text(pdf_path):
    """Extract text from PDF using pymupdf4llm."""
    import pymupdf4llm

    return pymupdf4llm.to_markdown(pdf_path)


def generate_summary(pdf_text, api_json):
    """Send PDF text + API JSON to Claude and get a simple summary."""
    client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)

    user_content = f"""Aqui estão os dados da votação na Câmara dos Deputados:

## Dados da API (JSON):
```json
{json.dumps(api_json, ensure_ascii=False, indent=2)[:3000]}
```

## Texto completo do projeto de lei (extraído do PDF):
{pdf_text[:8000]}

Com base nessas informações, gere um SUBTITULO e um RESUMO no formato solicitado."""

    response = client.messages.create(
        model="claude-haiku-4-5-20251001",
        system=SYSTEM_PROMPT,
        messages=[{"role": "user", "content": user_content}],
        max_tokens=500,
        temperature=0.3,
    )

    text = response.content[0].text.strip()
    return parse_subtitle_and_summary(text)


def parse_subtitle_and_summary(text):
    """Parse the AI response into subtitle and summary."""
    subtitle = None
    summary = None

    for line in text.split("\n"):
        stripped = line.strip()
        if stripped.upper().startswith("SUBTITULO:"):
            subtitle = stripped[10:].strip()
        elif stripped.upper().startswith("RESUMO:"):
            summary = stripped[7:].strip()

    # Collect remaining lines after RESUMO as part of summary
    if "RESUMO:" in text:
        parts = text.split("RESUMO:", 1)
        summary = parts[1].strip()

    if not subtitle:
        subtitle = text[:80]
    if not summary:
        summary = text

    return subtitle, summary


def update_activity(activity_id, subtitle, summary, source_proposal_id):
    """Update the activity via PATCH /activities/{id}/enrich."""
    payload = {
        "subtitle": subtitle,
        "summary": summary,
        "sourceProposalId": source_proposal_id,
    }
    resp = requests.patch(f"{BACKEND_BASE}/activities/{activity_id}/enrich", json=payload)
    resp.raise_for_status()


def enrich_activity(activity):
    """Enrich a single activity with an AI-generated summary."""
    ext_id = activity["externalId"]
    print(f"  Fetching voting details for {ext_id}...")

    time.sleep(REQUEST_DELAY)
    details = fetch_voting_details(ext_id)
    if not details:
        print(f"  ! Voting details not found (404)")
        return False

    affected = details.get("proposicoesAfetadas", [])
    if not affected:
        print(f"  ! No proposicoesAfetadas — skipping")
        return False

    proposal_uri = affected[0].get("uri")
    if not proposal_uri:
        print(f"  ! No proposal URI — skipping")
        return False

    # Fetch proposal details to get PDF URL
    time.sleep(REQUEST_DELAY)
    print(f"  Fetching proposal details...")
    proposal = fetch_proposal_details(proposal_uri)
    if not proposal:
        print(f"  ! Proposal details not found")
        return False

    pdf_url = proposal.get("urlInteiroTeor")
    if not pdf_url:
        print(f"  ! No PDF URL (urlInteiroTeor) — skipping")
        return False

    # Download and extract PDF
    print(f"  Downloading PDF: {pdf_url}")
    with tempfile.NamedTemporaryFile(suffix=".pdf", delete=False) as tmp:
        tmp_path = tmp.name

    try:
        download_pdf(pdf_url, tmp_path)
        print("  Extracting text from PDF...")
        pdf_text = extract_pdf_text(tmp_path)
        if not pdf_text or len(pdf_text.strip()) < 50:
            print(f"  ! PDF text too short — skipping")
            return False
    except Exception as e:
        print(f"  ! Error processing PDF: {e}")
        return False
    finally:
        os.unlink(tmp_path)

    # Build context JSON for AI
    api_context = {
        "votacao": {
            "descricao": details.get("descricao"),
            "aprovacao": details.get("aprovacao"),
            "data": details.get("data"),
            "siglaOrgao": details.get("siglaOrgao"),
            "descUltimaAberturaVotacao": details.get("descUltimaAberturaVotacao"),
        },
        "proposicao": {
            "siglaTipo": affected[0].get("siglaTipo"),
            "numero": affected[0].get("numero"),
            "ano": affected[0].get("ano"),
            "ementa": affected[0].get("ementa"),
        },
    }

    # Generate AI subtitle + summary
    print("  Generating AI subtitle + summary...")
    try:
        subtitle, summary = generate_summary(pdf_text, api_context)
    except Exception as e:
        print(f"  ! AI error: {e}")
        return False

    # Source proposal ID for debugging
    source_id = str(affected[0].get("id", ""))

    # Update in backend
    print(f"  Subtitle: {subtitle}")
    update_activity(activity["id"], subtitle, summary, source_id)
    print("  Done!")
    return True


def main():
    parser = argparse.ArgumentParser(description="Enrich activities with AI summaries")
    parser.add_argument("--id", type=int, help="Enrich a single activity by backend ID")
    args = parser.parse_args()

    if not ANTHROPIC_API_KEY:
        print("Error: ANTHROPIC_API_KEY environment variable is required")
        print("  export ANTHROPIC_API_KEY=sk-ant-...")
        return

    if args.id:
        # Single activity mode
        resp = requests.get(f"{BACKEND_BASE}/activities/{args.id}")
        if resp.status_code != 200:
            print(f"Activity {args.id} not found")
            return
        activity = resp.json()
        print(f"Enriching: {activity['title']}")
        enrich_activity(activity)
    else:
        # Batch mode — all activities
        print("=== Enriching Activities ===\n")
        activities = fetch_all_activities()
        print(f"Found {len(activities)} activities\n")

        enriched = 0
        skipped = 0
        failed = 0

        for activity in activities:
            print(f"[{activity['id']}] {activity['title']}")
            try:
                if enrich_activity(activity):
                    enriched += 1
                else:
                    skipped += 1
            except Exception as e:
                print(f"  ! Unexpected error: {e}")
                failed += 1

        print(f"\n=== Summary ===")
        print(f"Enriched: {enriched}")
        print(f"Skipped: {skipped}")
        print(f"Failed: {failed}")


if __name__ == "__main__":
    main()
