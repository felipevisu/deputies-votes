import os

CAMARA_API_BASE = "https://dadosabertos.camara.leg.br/api/v2"
BACKEND_BASE = os.getenv("BACKEND_URL", "http://localhost:8080")
LEGISLATURE_ID = 57  # Current legislature
REQUEST_DELAY = 0.5  # Seconds between Camara API calls (be polite)
