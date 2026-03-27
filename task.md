I need backend built with java and spring that returns these endpoints
The pagination of all list endpoins should support infinity scrooling

Implement the models, endpoints, database for the following oprations:

## Endpoints

### List Deputies

/deputies

- A list of brazilian deputies containing name, part, legend
  This endpoint should have a query parameter to filter deputies that I follow or not

```json
[
  {
    "id": "number",
    "name": "string",
    "party": "string",
    "legend": "string",
    "avatar": "string",
    "follow": true
  }
]
```

### Feed

/feed

- A list of deputies activity containing proposal name, deputie vote, author, description, return items only of deputies I follow
- This endpoint accept a POST request in the body a list of deputies ids where I can filter the feed only with activities of selected deputies.

```json
[
  {
    "id": "number",
    "name": "PL 1234/2026 - Reforma Tributária",
    "description": "Simplifica o sistema tributário brasileiro, unificando cinco impostos em um único Imposto sobre Bens e Serviços (IBS). Prevê período de transição de 7 anos e cashback para famílias de baixa renda.",
    "deputieName": "Ana Souza",
    "deputieParty": "PSD - SP",
    "vote": "YES",
    "author": "Dep. Juliana Costa (PSOL-RS)"
  }
]
```
