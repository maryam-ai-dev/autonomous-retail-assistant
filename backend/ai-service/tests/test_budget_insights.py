"""Unit tests for the budget-insights generator.

We stub the Anthropic client and assert the hallucination guard:
insights citing figures NOT present in the BudgetSummary are dropped.
"""

from __future__ import annotations

from decimal import Decimal

from app.budget_insights import BudgetInsightsGenerator
from app.schemas.budget import BudgetSummary


class FakeClient:
    def __init__(self, response: str) -> None:
        self._response = response
        self.calls: list[dict] = []

    def complete(self, *, prompt: str, max_tokens: int, system: str = "") -> str:
        self.calls.append({"prompt": prompt, "max_tokens": max_tokens, "system": system})
        return self._response


def _summary() -> BudgetSummary:
    return BudgetSummary(
        month="2026-04",
        spent=Decimal("65.20"),
        budget=Decimal("70.00"),
        savedVsFullPrice=Decimal("0"),
        byRetailer={"TESCO": Decimal("50.00"), "BOOTS": Decimal("15.20")},
        basketCount=2,
    )


def test_max_tokens_is_300() -> None:
    client = FakeClient('{"insights":[]}')
    BudgetInsightsGenerator(client=client).insights(_summary())
    assert client.calls[0]["max_tokens"] == 300


def test_keeps_insight_with_supported_figure() -> None:
    client = FakeClient(
        '{"insights": ["You spent £15.20 at Boots this month — well done."]}'
    )
    out = BudgetInsightsGenerator(client=client).insights(_summary())
    assert out.insights == ["You spent £15.20 at Boots this month — well done."]
    assert out.warnings == []


def test_drops_insight_citing_hallucinated_figure() -> None:
    # £12 is not present in byRetailer.BOOTS (which is £15.20)
    client = FakeClient('{"insights": ["You saved £12 at Boots this month."]}')
    out = BudgetInsightsGenerator(client=client).insights(_summary())
    assert out.insights == []
    assert any("unsupported" in w for w in out.warnings)


def test_keeps_insight_using_derived_difference_budget_minus_spent() -> None:
    # 70.00 - 65.20 = 4.80 — derived figure should be allowed
    client = FakeClient(
        '{"insights": ["You came in £4.80 under budget this month."]}'
    )
    out = BudgetInsightsGenerator(client=client).insights(_summary())
    assert out.insights == ["You came in £4.80 under budget this month."]


def test_unparseable_llm_output_returns_empty_with_warning() -> None:
    client = FakeClient("not json at all, just a paragraph")
    out = BudgetInsightsGenerator(client=client).insights(_summary())
    assert out.insights == []
    assert any("not parseable" in w for w in out.warnings)


def test_mix_of_kept_and_dropped_insights() -> None:
    client = FakeClient(
        '{"insights": ['
        '"You spent £65.20 in total — within your £70 budget.",'
        '"You saved £999 by shopping smart."'
        "]}"
    )
    out = BudgetInsightsGenerator(client=client).insights(_summary())
    assert out.insights == [
        "You spent £65.20 in total — within your £70 budget."
    ]
    assert any("999" in w for w in out.warnings)
