# 05 - System Prompts

The chat endpoint now uses a default system prompt that frames the model as a wine recommendation assistant.

## Theory

A system prompt gives the model role and behavior instructions. It does not add catalog knowledge by itself.

The assistant is instructed to be concise and to say when catalog context is missing.

## Manual Check

Ask:

```text
Recommend a red wine for a beginner.
```

The answer should sound like a wine assistant, but it should not be treated as catalog-grounded unless RAG context is used.
