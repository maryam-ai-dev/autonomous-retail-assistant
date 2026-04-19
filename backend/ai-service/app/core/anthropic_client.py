"""Thin wrapper around the Anthropic SDK used by every LLM-backed module.

Centralising the client lets us enforce the model pin (`claude-sonnet-4-20250514`)
and makes it straightforward to unit-test callers by substituting a fake.
"""

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Protocol

try:
    import anthropic
except ImportError:  # pragma: no cover — runtime-only
    anthropic = None  # type: ignore[assignment]


CLAUDE_MODEL = "claude-sonnet-4-20250514"


class AnthropicClientProtocol(Protocol):
    def complete(self, *, prompt: str, max_tokens: int, system: str = "") -> str: ...


@dataclass
class AnthropicClient:
    """Real Anthropic client. Raises if ANTHROPIC_API_KEY is missing."""

    api_key: str | None = None

    def __post_init__(self) -> None:
        self.api_key = self.api_key or os.getenv("ANTHROPIC_API_KEY")
        if anthropic is None:
            raise RuntimeError("anthropic SDK is not installed")
        if not self.api_key:
            self._client = None
        else:
            self._client = anthropic.Anthropic(api_key=self.api_key)

    def complete(self, *, prompt: str, max_tokens: int, system: str = "") -> str:
        if self._client is None:
            raise RuntimeError(
                "ANTHROPIC_API_KEY is not configured — cannot call Claude."
            )
        message = self._client.messages.create(
            model=CLAUDE_MODEL,
            max_tokens=max_tokens,
            system=system or "You are a helpful assistant.",
            messages=[{"role": "user", "content": prompt}],
        )
        parts = [block.text for block in message.content if getattr(block, "type", None) == "text"]
        return "".join(parts).strip()
