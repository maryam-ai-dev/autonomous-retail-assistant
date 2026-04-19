"""Unit tests for the intent parser.

We do not call the real Anthropic API from tests — we stub the client and assert
the parser's business rules (budget null when not stated, default category,
hallucinated field handling).
"""

from __future__ import annotations

from decimal import Decimal

import pytest

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


def test_full_grocery_intent_with_budget() -> None:
    parser, client = _parser(
        '{"budget": 70, "currency": "GBP", "category": "GROCERY",'
        ' "subcategories": ["DAIRY"], "dietary_requirements": ["HALAL"],'
        ' "retailer_hints": ["TESCO"], "item_hints": ["chicken"], "timing": "weekly",'
        ' "notes": null}'
    )
    intent, warnings = parser.parse("weekly groceries under £70, halal, from tesco")
    assert intent.budget == Decimal("70")
    assert intent.category == IntentCategory.GROCERY
    assert "HALAL" in intent.dietary_requirements
    assert "TESCO" in intent.retailer_hints
    assert warnings == []
    assert client.calls[0]["max_tokens"] == 500


def test_no_budget_stays_null() -> None:
    parser, _ = _parser(
        '{"budget": null, "category": "GROCERY", "subcategories": [],'
        ' "dietary_requirements": [], "retailer_hints": [], "item_hints": [],'
        ' "timing": null, "notes": null}'
    )
    intent, _ = parser.parse("some milk and bread please")
    assert intent.budget is None


def test_unknown_category_defaults_to_grocery() -> None:
    parser, _ = _parser(
        '{"budget": null, "category": "NOT_A_CATEGORY"}'
    )
    intent, warnings = parser.parse("???")
    assert intent.category == IntentCategory.GROCERY
    assert any("NOT_A_CATEGORY" in w for w in warnings)


def test_mixed_category() -> None:
    parser, _ = _parser(
        '{"budget": 120, "category": "MIXED", "subcategories": ["DAIRY", "TOPS"]}'
    )
    intent, _ = parser.parse("weekly shop and a couple of t-shirts under £120")
    assert intent.category == IntentCategory.MIXED


def test_fashion_intent() -> None:
    parser, _ = _parser(
        '{"budget": 60, "category": "FASHION", "subcategories": ["DRESSES"]}'
    )
    intent, _ = parser.parse("summer dress under £60")
    assert intent.category == IntentCategory.FASHION


def test_ambiguous_input_defaults_grocery() -> None:
    parser, _ = _parser(
        '{"budget": null, "category": "GROCERY"}'
    )
    intent, _ = parser.parse("stuff")
    assert intent.category == IntentCategory.GROCERY
    assert intent.budget is None


def test_budget_as_string() -> None:
    parser, _ = _parser('{"budget": "45.50", "category": "GROCERY"}')
    intent, _ = parser.parse("weekly groceries around £45.50")
    assert intent.budget == Decimal("45.50")


def test_non_numeric_budget_becomes_null_with_warning() -> None:
    parser, _ = _parser('{"budget": "flexible", "category": "GROCERY"}')
    intent, warnings = parser.parse("any budget")
    assert intent.budget is None
    assert any("numeric" in w for w in warnings)


def test_llm_wraps_json_in_markdown_fence() -> None:
    response = '```json\n{"budget": 30, "category": "GROCERY"}\n```'
    parser, _ = _parser(response)
    intent, _ = parser.parse("weekly under £30")
    assert intent.budget == Decimal("30")


def test_invalid_json_falls_back_to_defaults() -> None:
    parser, _ = _parser("sorry I cannot comply")
    intent, warnings = parser.parse("x")
    assert intent.budget is None
    assert intent.category == IntentCategory.GROCERY
    assert any("parseable" in w for w in warnings)


def test_prompt_file_is_used_not_hardcoded() -> None:
    parser, client = _parser('{"budget": null, "category": "GROCERY"}')
    parser.parse("hello")
    assert "hello" in client.calls[0]["prompt"]
    assert "budget" in client.calls[0]["prompt"]
