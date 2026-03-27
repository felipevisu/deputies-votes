# Câmara dos Deputados — API Integration Guide

## Overview

| Property        | Value                                       |
| --------------- | ------------------------------------------- |
| Base URL        | `https://dadosabertos.camara.leg.br/api/v2` |
| Authentication  | None required                               |
| Response format | JSON (default) or XML                       |
| Rate limit      | Not officially documented; use responsibly  |

All endpoints are read-only (`GET`). No API key is needed.

---

## Pagination

Most list endpoints support pagination via query parameters:

| Parameter    | Type    | Description                            |
| ------------ | ------- | -------------------------------------- |
| `itens`      | integer | Records per page. Max: `100`           |
| `pagina`     | integer | Page number, starting at `1`           |
| `ordem`      | string  | `ASC` or `DESC`                        |
| `ordenarPor` | string  | Field to sort by (varies per endpoint) |

The response body always includes a `links` array with `rel: "next"` and `rel: "last"` URLs to assist pagination.

```json
{
  "dados": [...],
  "links": [
    { "rel": "self",  "href": "https://..." },
    { "rel": "next",  "href": "https://...&pagina=2" },
    { "rel": "last",  "href": "https://...&pagina=16" }
  ]
}
```

---

## Endpoints

### Deputies (Deputados)

#### List Deputies

```
GET /deputados
```

**Query parameters:**

| Parameter       | Type    | Description                             |
| --------------- | ------- | --------------------------------------- |
| `nome`          | string  | Filter by name (partial match)          |
| `idLegislatura` | integer | Legislature ID (current: `57`)          |
| `siglaUf`       | string  | State code, e.g. `SP`, `RJ`, `MG`       |
| `siglaPartido`  | string  | Party acronym, e.g. `PT`, `PL`, `UNIÃO` |
| `siglaSexo`     | string  | `M` or `F`                              |
| `itens`         | integer | Records per page (max `100`)            |
| `pagina`        | integer | Page number                             |
| `ordenarPor`    | string  | `nome`, `siglaUf`, `siglaPartido`       |
| `ordem`         | string  | `ASC` or `DESC`                         |

**Example request:**

```
GET /deputados?siglaUf=SP&siglaPartido=PT&itens=20&pagina=1
```

**Response fields:**

| Field           | Type    | Description        |
| --------------- | ------- | ------------------ |
| `id`            | integer | Unique deputy ID   |
| `nome`          | string  | Parliamentary name |
| `siglaPartido`  | string  | Party acronym      |
| `siglaUf`       | string  | State              |
| `idLegislatura` | integer | Legislature ID     |
| `urlFoto`       | string  | Photo URL          |
| `email`         | string  | Official email     |

---

#### Get Deputy Details

```
GET /deputados/{id}
```

**Path parameters:**

| Parameter | Type    | Description                    |
| --------- | ------- | ------------------------------ |
| `id`      | integer | Deputy ID (from list endpoint) |

**Example request:**

```
GET /deputados/204554
```

**Response fields (in addition to list fields):**

| Field                 | Type   | Description                |
| --------------------- | ------ | -------------------------- |
| `nomeCivil`           | string | Full civil name            |
| `dataNascimento`      | string | Date of birth (YYYY-MM-DD) |
| `municipioNascimento` | string | City of birth              |
| `ufNascimento`        | string | State of birth             |
| `escolaridade`        | string | Education level            |
| `redeSocial`          | array  | Social media URLs          |

---

#### Get Deputy Expenses (CEAP)

Returns expenses paid through the _Cota para Exercício da Atividade Parlamentar_ — the parliamentary expense allowance.

```
GET /deputados/{id}/despesas
```

**Path parameters:**

| Parameter | Type    | Description |
| --------- | ------- | ----------- |
| `id`      | integer | Deputy ID   |

**Query parameters:**

| Parameter    | Type    | Description                                               |
| ------------ | ------- | --------------------------------------------------------- |
| `ano`        | integer | Year (e.g. `2026`). Multiple allowed: `ano=2025&ano=2026` |
| `mes`        | integer | Month (1–12). Multiple allowed                            |
| `itens`      | integer | Records per page (max `100`)                              |
| `pagina`     | integer | Page number                                               |
| `ordenarPor` | string  | `ano`, `mes`, `tipoDespesa`, `valorDocumento`             |
| `ordem`      | string  | `ASC` or `DESC`                                           |

**Example request:**

```
GET /deputados/204554/despesas?ano=2026&mes=3&itens=100&pagina=1
```

**Response fields:**

| Field               | Type    | Description                                    |
| ------------------- | ------- | ---------------------------------------------- |
| `ano`               | integer | Year of expense                                |
| `mes`               | integer | Month of expense                               |
| `tipoDespesa`       | string  | Category (e.g. "COMBUSTÍVEIS E LUBRIFICANTES") |
| `codDocumento`      | integer | Document ID                                    |
| `tipoDocumento`     | string  | Document type                                  |
| `dataDocumento`     | string  | Invoice date (YYYY-MM-DD)                      |
| `numDocumento`      | string  | Invoice number                                 |
| `valorDocumento`    | number  | Gross amount                                   |
| `urlDocumento`      | string  | Link to original invoice                       |
| `nomeFornecedor`    | string  | Supplier name                                  |
| `cnpjCpfFornecedor` | string  | Supplier tax ID                                |
| `valorLiquido`      | number  | Net amount effectively paid by the allowance   |
| `valorGlosa`        | number  | Amount disallowed/rejected                     |
| `numRessarcimento`  | string  | Reimbursement number                           |
| `codLote`           | integer | Batch code                                     |
| `parcela`           | integer | Installment number                             |

---

### Votes (Votações)

#### List Votes

```
GET /votacoes
```

**Query parameters:**

| Parameter    | Type    | Description                                     |
| ------------ | ------- | ----------------------------------------------- |
| `dataInicio` | string  | Start date (YYYY-MM-DD)                         |
| `dataFim`    | string  | End date (YYYY-MM-DD)                           |
| `idOrgao`    | integer | Filter by legislative body (Plenário ID: `180`) |
| `idEvento`   | integer | Filter by session event                         |
| `itens`      | integer | Records per page (max `100`)                    |
| `pagina`     | integer | Page number                                     |
| `ordenarPor` | string  | `dataHoraRegistro`, `idVotacao`                 |
| `ordem`      | string  | `ASC` or `DESC`                                 |

**Example request:**

```
GET /votacoes?dataInicio=2026-03-01&dataFim=2026-03-27&itens=20&ordem=DESC
```

**Response fields:**

| Field              | Type    | Description                          |
| ------------------ | ------- | ------------------------------------ |
| `id`               | string  | Unique vote ID (e.g. `"2490384-76"`) |
| `uri`              | string  | Full URI for this vote               |
| `data`             | string  | Date (YYYY-MM-DD)                    |
| `dataHoraRegistro` | string  | ISO datetime of registration         |
| `siglaOrgao`       | string  | Legislative body acronym             |
| `uriOrgao`         | string  | URI of the legislative body          |
| `uriEvento`        | string  | URI of the session event             |
| `proposicaoTexto`  | string  | Text description of the proposal     |
| `aprovacao`        | integer | `1` = approved, `0` = rejected       |

---

#### Get Vote Details

```
GET /votacoes/{id}
```

**Path parameters:**

| Parameter | Type   | Description                  |
| --------- | ------ | ---------------------------- |
| `id`      | string | Vote ID (from list endpoint) |

**Example request:**

```
GET /votacoes/2490384-76
```

**Additional response fields:**

| Field                        | Type    | Description                     |
| ---------------------------- | ------- | ------------------------------- |
| `descricao`                  | string  | Full vote description           |
| `codTipoVotacao`             | integer | Vote type code                  |
| `ultimaAberturaVotacao`      | string  | ISO datetime vote opened        |
| `objVotacao`                 | string  | Subject matter text             |
| `uriProposicaoDesdobramento` | string  | URI of the specific proposition |

---

#### Get Individual Votes per Deputy

Returns each deputy's individual vote for a specific voting session.

```
GET /votacoes/{id}/votos
```

**Path parameters:**

| Parameter | Type   | Description |
| --------- | ------ | ----------- |
| `id`      | string | Vote ID     |

**Example request:**

```
GET /votacoes/2490384-76/votos
```

**Response fields (per deputy):**

| Field                     | Type    | Description                                       |
| ------------------------- | ------- | ------------------------------------------------- |
| `tipoVoto`                | string  | `Sim`, `Não`, `Abstenção`, `Obstrução`, `Art. 17` |
| `dataRegistroVoto`        | string  | ISO datetime of vote registration                 |
| `deputado_.id`            | integer | Deputy ID                                         |
| `deputado_.nome`          | string  | Deputy name                                       |
| `deputado_.siglaPartido`  | string  | Party acronym                                     |
| `deputado_.siglaUf`       | string  | State                                             |
| `deputado_.idLegislatura` | integer | Legislature ID                                    |
| `deputado_.urlFoto`       | string  | Deputy photo URL                                  |
| `deputado_.uri`           | string  | Deputy URI                                        |

---

#### Get Party Orientations for a Vote

```
GET /votacoes/{id}/orientacoes
```

Returns how each party/bloc recommended their members to vote.

**Response fields:**

| Field               | Type   | Description                                        |
| ------------------- | ------ | -------------------------------------------------- |
| `codPartidoBloco`   | string | Party/bloc code                                    |
| `siglaPartidoBloco` | string | Party/bloc acronym                                 |
| `nomePartidoBloco`  | string | Party/bloc full name                               |
| `orientacaoVoto`    | string | `Sim`, `Não`, `Abstenção`, `Liberado`, `Obstrução` |

---

### Parties (Partidos)

#### List Parties

```
GET /partidos
```

**Query parameters:**

| Parameter    | Type    | Description      |
| ------------ | ------- | ---------------- |
| `itens`      | integer | Records per page |
| `pagina`     | integer | Page number      |
| `ordenarPor` | string  | `sigla`, `nome`  |
| `ordem`      | string  | `ASC` or `DESC`  |

**Response fields:**

| Field   | Type    | Description           |
| ------- | ------- | --------------------- |
| `id`    | integer | Party ID              |
| `sigla` | string  | Party acronym         |
| `nome`  | string  | Full party name       |
| `uri`   | string  | URI for party details |

---

#### Get Party Members

```
GET /partidos/{id}/membros
```

**Path parameters:**

| Parameter | Type    | Description                   |
| --------- | ------- | ----------------------------- |
| `id`      | integer | Party ID (from list endpoint) |

**Common party IDs:**

| Party        | ID      |
| ------------ | ------- |
| PT           | `36769` |
| PL           | `37906` |
| UNIÃO        | `36899` |
| PP           | `36948` |
| PSD          | `37900` |
| REPUBLICANOS | `36965` |

---

## Bulk Data Files (Alternative to API)

When you need to process large datasets or cross-reference votes with deputies, downloading annual bulk files is more efficient than paginating through the API.

| Dataset                      | URL Pattern                                                                                              |
| ---------------------------- | -------------------------------------------------------------------------------------------------------- |
| All votes                    | `https://dadosabertos.camara.leg.br/arquivos/votacoes/{fmt}/votacoes-{year}.{fmt}`                       |
| Individual votes per deputy  | `https://dadosabertos.camara.leg.br/arquivos/votacoesVotos/{fmt}/votacoesVotos-{year}.{fmt}`             |
| Vote orientations            | `https://dadosabertos.camara.leg.br/arquivos/votacoesOrientacoes/{fmt}/votacoesOrientacoes-{year}.{fmt}` |
| Votes linked to propositions | `https://dadosabertos.camara.leg.br/arquivos/votacoesProposicoes/{fmt}/votacoesProposicoes-{year}.{fmt}` |
| All deputies (historical)    | `https://dadosabertos.camara.leg.br/arquivos/deputados/{fmt}/deputados.{fmt}`                            |
| Deputy expenses              | `https://www.camara.leg.br/cotas/Ano-{year}.{fmt}`                                                       |

Replace `{fmt}` with `json`, `csv`, `xlsx`, `ods`, or `xml`. Replace `{year}` with the 4-digit year (e.g. `2026`).

---

## Known Limitations

| Issue                          | Details                                                                                                                         |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| `GET /deputados/{id}/votacoes` | Returns **405 Method Not Allowed** — this endpoint was removed. Use `GET /votacoes/{id}/votos` instead and filter by deputy ID. |
| Vote-to-proposition link       | Not always direct. Use `votacoesProposicoes` bulk file to cross-reference.                                                      |
| Symbolic votes                 | Some symbolic votes have no individual records. Only nominal votes guarantee individual deputy data.                            |
| Max page size                  | `100` items per page across all endpoints.                                                                                      |

---

## Common Integration Patterns

### Get all votes by a specific deputy in a date range

Since `/deputados/{id}/votacoes` is unavailable, use this two-step approach:

1. Fetch all voting sessions: `GET /votacoes?dataInicio=...&dataFim=...`
2. For each session, fetch individual votes: `GET /votacoes/{id}/votos`
3. Filter results by `deputado_.id`

For large date ranges, prefer downloading the annual bulk file `votacoesVotos-{year}.json` and filtering locally.

### Check if a deputy followed party orientation

1. Get party orientation: `GET /votacoes/{id}/orientacoes`
2. Get deputy vote: `GET /votacoes/{id}/votos` — filter by deputy ID
3. Compare `orientacaoVoto` vs `tipoVoto`

### Get total expenses by category for a deputy

```
GET /deputados/{id}/despesas?ano=2026&itens=100&pagina=1
```

Iterate all pages and aggregate `valorLiquido` grouped by `tipoDespesa`.

---

## Error Reference

| HTTP Status                 | Meaning                                      |
| --------------------------- | -------------------------------------------- |
| `200 OK`                    | Success                                      |
| `404 Not Found`             | Resource not found (e.g. invalid deputy ID)  |
| `405 Method Not Allowed`    | Endpoint does not exist or was removed       |
| `422 Unprocessable Entity`  | Invalid query parameter value                |
| `500 Internal Server Error` | Server-side error; retry after a few seconds |
