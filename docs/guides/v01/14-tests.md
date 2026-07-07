# 14 - Tests

The test strategy keeps normal automated tests independent from live Ollama calls.

## Covered Areas

- JPA context and sample data loading.
- Wine document mapping.
- Controller behavior for catalog endpoints.

## Theory

LLM calls are non-deterministic and require local infrastructure. Automated tests should mock or avoid them unless explicitly marked as integration tests.

## Command

```bash
./mvnw test
```
