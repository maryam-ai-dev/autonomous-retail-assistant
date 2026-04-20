from __future__ import annotations

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_search_endpoint_unknown_retailer_returns_400() -> None:
    response = client.post(
        "/scrapers/search",
        json={"query": "milk", "retailer": "UNKNOWN", "maxResults": 5},
    )
    assert response.status_code == 400


def test_search_endpoint_empty_for_stub_connectors() -> None:
    # Live browser fetch is not wired, so the stub returns [] but the endpoint
    # must still respond 200 and return an empty list (not 500).
    response = client.post(
        "/scrapers/search",
        json={"query": "shampoo", "retailer": "BOOTS", "maxResults": 5},
    )
    assert response.status_code == 200
    assert response.json() == []


def test_status_endpoint_returns_all_connectors() -> None:
    response = client.get("/scrapers/status")
    assert response.status_code == 200
    retailers = {item["retailer"] for item in response.json()}
    assert retailers == {"BOOTS", "ARGOS", "ASOS"}
