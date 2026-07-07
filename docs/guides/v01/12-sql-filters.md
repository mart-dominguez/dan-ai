# 12 - SQL Filters

The recommendation and search requests accept structured filters:

- `maxPrice`
- `type`
- `stockAvailable`
- `foodCategory`
- `topK`

## Example

```bash
curl "http://localhost:8080/api/wines/search?q=pasta%20with%20tomato%20sauce&maxPrice=20&stockAvailable=true"
```

## Theory

Semantic search handles meaning. Structured filters handle hard rules.

This version applies filters in the vector index metadata. A production PostgreSQL/pgvector version could push these constraints into SQL.
