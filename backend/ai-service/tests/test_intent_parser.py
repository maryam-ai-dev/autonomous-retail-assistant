"""Unit tests for the intent parser.

We do not call the real Anthropic API from tests — we stub the client and assert
the parser's business rules (budget null when not stated, default category
HEALTH_BEAUTY, out-of-scope coercion for grocery, hallucinated field handling).
"""

from __future__ import annotations

from decimal import Decimal

from app.intent_parsing.parser import IntentParser
from app.schemas.intent import IntentCategory


class FakeClient:
    def __init__(self, response: str) -> None:
        self._response = response
        self.calls: list[dict] = []

    def complete(self, *, prompt: str, max_tokens: int, system: str = "") -> str:
        self.calls.append({"prompt": prompt, "max_tokens": max_tokens, "system": system})
        return self._response


def _parser(response: str) -> tuple[IntentParser, FakeClient]:
    client = FakeClient(response)
    return IntentParser(client=client), client


def test_full_health_beauty_intent_with_budget() -> None:
    parser, client = _parser(
        '{"budget": 20, "currency": "GBP", "category": "HEALTH_BEAUTY",'
        ' "subcategories": ["SKINCARE"], "dietary_requirements": ["HALAL"],'
        ' "retailer_hints": ["BOOTS"], "item_hints": ["moisturiser"],'
        ' "timing": null, "notes": null}'
    )
    intent, warnings = parser.parse("halal moisturiser under £20 from boots")
    assert intent.budget == Decimal("20")
    assert intent.category == IntentCategory.HEALTH_BEAUTY
    assert "HALAL" in intent.dietary_requirements
    assert "BOOTS" in intent.retailer_hints
    assert intent.out_of_scope is False
    assert warnings == []
    assert client.calls[0]["max_tokens"] == 500


def test_no_budget_stays_null() -> None:
    parser, _ = _parser(
        '{"budget": null, "category": "HEALTH_BEAUTY", "subcategories": [],'
        ' "dietary_requirements": [], "retailer_hints": [], "item_hints": [],'
        ' "timing": null, "notes": null}'
    )
    intent, _ = parser.parse("some shampoo please")
    assert intent.budget is None


def test_unknown_category_defaults_to_health_beauty() -> None:
    parser, _ = _parser('{"budget": null, "category": "NOT_A_CATEGORY"}')
    intent, warnings = parser.parse("???")
    assert intent.category == IntentCategory.HEALTH_BEAUTY
    assert any("NOT_A_CATEGORY" in w for w in warnings)


def test_mixed_category_preserved() -> None:
    parser, _ = _parser(
        '{"budget": 120, "category": "MIXED", "subcategories": ["TOPS", "PHONES"]}'
    )
    intent, _ = parser.parse("a couple of t-shirts and a phone case under £120")
    assert intent.category == IntentCategory.MIXED


def test_fashion_intent() -> None:
    parser, _ = _parser(
        '{"budget": 60, "category": "FASHION", "subcategories": ["DRESSES"]}'
    )
    intent, _ = parser.parse("summer dress under £60")
    assert intent.category == IntentCategory.FASHION


def test_grocery_response_coerced_to_out_of_scope() -> None:
    parser, _ = _parser(
        '{"budget": 50, "category": "GROCERY", "subcategories": ["DAIRY"],'
        ' "dietary_requirements": ["HALAL"], "retailer_hints": ["TESCO"]}'
    )
    intent, warnings = parser.parse("weekly groceries under £50, halal")
    assert intent.out_of_scope is True
    assert intent.out_of_scope_reason == "grocery"
    assert intent.category == IntentCategory.HEALTH_BEAUTY
    assert any("GROCERY" in w for w in warnings)


def test_llm_returns_out_of_scope_explicitly() -> None:
    parser, _ = _parser(
        '{"budget": null, "category": "HEALTH_BEAUTY",'
        ' "outOfScope": true, "reason": "grocery"}'
    )
    intent, _ = parser.parse("a pint of milk")
    assert intent.out_of_scope is True
    assert intent.out_of_scope_reason == "grocery"


def test_budget_as_string() -> None:
    parser, _ = _parser('{"budget": "45.50", "category": "HEALTH_BEAUTY"}')
    intent, _ = parser.parse("skincare around £45.50")
    assert intent.budget == Decimal("45.50")


def test_non_numeric_budget_becomes_null_with_warning() -> None:
    parser, _ = _parser('{"budget": "flexible", "category": "HEALTH_BEAUTY"}')
    intent, warnings = parser.parse("any budget")
    assert intent.budget is None
    assert any("numeric" in w for w in warnings)


def test_llm_wraps_json_in_markdown_fence() -> None:
    response = '```json\n{"budget": 30, "category": "HEALTH_BEAUTY"}\n```'
    parser, _ = _parser(response)
    intent, _ = parser.parse("shampoo under £30")
    assert intent.budget == Decimal("30")


def test_invalid_json_falls_back_to_defaults() -> None:
    parser, _ = _parser("sorry I cannot comply")
    intent, warnings = parser.parse("x")
    assert intent.budget is None
    assert intent.category == IntentCategory.HEALTH_BEAUTY
    assert any("parseable" in w for w in warnings)


def test_prompt_file_is_used_not_hardcoded() -> None:
    parser, client = _parser('{"budget": null, "category": "HEALTH_BEAUTY"}')
    parser.parse("hello")
    assert "hello" in client.calls[0]["prompt"]
    assert "budget" in client.calls[0]["prompt"]
