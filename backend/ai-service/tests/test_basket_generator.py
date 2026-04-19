"""Unit tests for the advisory basket generator.

We stub the Anthropic client and assert the generator's business rules:
retry on over-budget, hallucinated-id removal, empty-candidates path.
"""

from __future__ import annotations

from decimal import Decimal

from app.basket_generation.generator import BasketGenerator
from app.schemas.basket import BasketGenerateRequest, CandidateProduct


class QueuedClient:
    """Stub client that returns responses from a queue in order."""

    def __init__(self, responses: list[str]) -> None:
        self._responses = list(responses)
        self.calls: list[dict] = []

    def complete(self, *, prompt: str, max_tokens: int, system: str = "") -> str:
        self.calls.append(
            {"prompt": prompt, "max_tokens": max_tokens, "system": system}
        )
        if not self._responses:
            raise AssertionError("QueuedClient ran out of canned responses")
        return self._responses.pop(0)


def _candidates() -> list[CandidateProduct]:
    return [
        CandidateProduct(
            candidate_id="a",
            name="oat milk 1L",
            brand="Alpro",
            retailer="TESCO",
            price=Decimal("1.80"),
            subcategory="DAIRY",
            dietary_tags=["VEGAN"],
        ),
        CandidateProduct(
            candidate_id="b",
            name="chicken breast 500g",
            brand="Tesco",
            retailer="TESCO",
            price=Decimal("4.50"),
            subcategory="MEAT_POULTRY",
            dietary_tags=["HALAL_UNKNOWN"],
        ),
        CandidateProduct(
            candidate_id="c",
            name="banana 6pk",
            brand="Tesco",
            retailer="TESCO",
            price=Decimal("0.90"),
            subcategory="FRUIT_VEG",
            dietary_tags=[],
        ),
    ]


def test_generator_returns_items_within_budget() -> None:
    client = QueuedClient(
        [
            '{"items": [{"candidateId": "a", "quantity": 1, "reasoning": "staple"},'
            ' {"candidateId": "c", "quantity": 2, "reasoning": "fruit"}],'
            ' "totalCost": 3.60}'
        ]
    )
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(
            raw_text="weekly grocery",
            budget=Decimal("10"),
            candidates=_candidates(),
        )
    )

    assert [i.candidate_id for i in draft.items] == ["a", "c"]
    assert draft.total_cost == Decimal("3.60")
    assert draft.retry_count == 0
    assert client.calls[0]["max_tokens"] == 2000


def test_retry_fires_once_on_overage_and_keeps_cheaper() -> None:
    first = (
        '{"items": [{"candidateId": "a", "quantity": 5, "reasoning": ""},'
        ' {"candidateId": "b", "quantity": 1, "reasoning": ""}],'
        ' "totalCost": 13.50}'
    )  # actual = 1.80*5 + 4.50 = 13.50
    second = (
        '{"items": [{"candidateId": "a", "quantity": 1, "reasoning": ""},'
        ' {"candidateId": "c", "quantity": 1, "reasoning": ""}],'
        ' "totalCost": 2.70}'
    )  # actual = 1.80 + 0.90 = 2.70
    client = QueuedClient([first, second])
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(
            raw_text="budget weekly grocery",
            budget=Decimal("5.00"),
            candidates=_candidates(),
        )
    )

    assert draft.retry_count == 1
    assert draft.total_cost == Decimal("2.70")
    # second prompt should have contained the nudge phrase
    assert any("reduce" in call["prompt"].lower() for call in client.calls[1:])


def test_retry_not_used_if_it_is_more_expensive() -> None:
    first = (
        '{"items": [{"candidateId": "a", "quantity": 2, "reasoning": ""}],'
        ' "totalCost": 3.60}'
    )  # actual = 1.80*2 = 3.60 — over a 2.00 budget
    second = (
        '{"items": [{"candidateId": "b", "quantity": 1, "reasoning": ""}],'
        ' "totalCost": 4.50}'
    )  # actual = 4.50 — more expensive than first
    client = QueuedClient([first, second])
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(
            raw_text="tiny budget",
            budget=Decimal("2.00"),
            candidates=_candidates(),
        )
    )

    assert draft.retry_count == 1
    # keeps first pass (cheaper of the two over-budget outputs)
    assert [i.candidate_id for i in draft.items] == ["a"]
    assert any("still exceeds" in w for w in draft.warnings)


def test_hallucinated_candidate_ids_are_dropped() -> None:
    client = QueuedClient(
        [
            '{"items": [{"candidateId": "a", "quantity": 1, "reasoning": ""},'
            ' {"candidateId": "nope", "quantity": 1, "reasoning": "made up"}],'
            ' "totalCost": 3.60}'
        ]
    )
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(
            raw_text="x",
            budget=Decimal("10"),
            candidates=_candidates(),
        )
    )

    assert [i.candidate_id for i in draft.items] == ["a"]
    assert any("hallucinated" in w for w in draft.warnings)


def test_empty_candidates_returns_empty_draft() -> None:
    gen = BasketGenerator(client=QueuedClient([]))
    draft = gen.generate(BasketGenerateRequest(raw_text="x", candidates=[]))
    assert draft.items == []
    assert draft.total_cost == Decimal("0")
    assert any("no candidates" in w for w in draft.warnings)


def test_no_budget_means_no_retry() -> None:
    client = QueuedClient(
        [
            '{"items": [{"candidateId": "a", "quantity": 1, "reasoning": ""}],'
            ' "totalCost": 1.80}'
        ]
    )
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(raw_text="no budget", budget=None, candidates=_candidates())
    )

    assert draft.retry_count == 0
    assert len(client.calls) == 1


def test_invalid_json_returns_empty_with_warning() -> None:
    client = QueuedClient(["sorry I cannot comply"])
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(raw_text="x", candidates=_candidates())
    )

    assert draft.items == []
    assert any("parseable" in w for w in draft.warnings)


def test_reasoning_preserved_per_item() -> None:
    client = QueuedClient(
        [
            '{"items": [{"candidateId": "a", "quantity": 1, "reasoning": "good unit price"}],'
            ' "totalCost": 1.80}'
        ]
    )
    gen = BasketGenerator(client=client)

    draft = gen.generate(
        BasketGenerateRequest(raw_text="x", candidates=_candidates())
    )

    assert draft.items[0].reasoning == "good unit price"
