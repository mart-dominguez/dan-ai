# 13 - Structured Output

The recommendation endpoint returns a structured JSON wrapper:

- `question`
- `answer`
- `recommendedWines`

Each recommended wine includes:

- `wineId`
- `name`
- `type`
- `price`
- `reason`
- `pairingExplanation`
- `confidence`

## Theory

Structured output is easier for a frontend or tests to consume than free text alone.

This version keeps the LLM answer as text and wraps retrieved catalog wines in a predictable API shape.
