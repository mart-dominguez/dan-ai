# 07 - Embeddings Concept

Each wine is converted into a searchable text document by `WineDocumentMapper`.

## Example

```text
Name: Malbec Reserva.
Type: red wine.
Grape variety: Malbec.
Region: Mendoza, Argentina.
Taste notes: plum, blackberry, soft tannins.
Food pairings: grilled meat, barbecue, hard cheese.
```

## Theory

An embedding model converts this text into a vector. Similar meanings should produce nearby vectors.

The document should include fields that help users search naturally: taste, body, acidity, sweetness, pairings, region, and description.

## Manual Check

Call the embedding preview endpoint after Ollama is running:

```bash
curl http://localhost:8080/api/wines/1/embedding
```
