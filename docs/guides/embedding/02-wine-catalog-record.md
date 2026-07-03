# 2. Wine Catalog Record (PostgreSQL)

This file describes the relational source of truth: the `wine` table in
PostgreSQL. Everything else in the guide references this schema.

If you have not read the conceptual overview yet, start at
[01-concepts.md](01-concepts.md).

## 2.1 Role of the relational database

PostgreSQL is the **source of truth** for wine data. It holds:

- Structured business fields (price, stock, year, type).
- Descriptive fields that will be embedded later (taste notes, food pairings,
  description) — see [03-vector-store-record.md](03-vector-store-record.md).

The embedding column (if you use pgvector) is an **index**, not business data.
Application code reads and writes business fields; indexing is a separate
concern handled by an indexing service.

## 2.2 Schema

```sql
CREATE TABLE wine (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    grape_variety   VARCHAR(60),
    region          VARCHAR(80),
    country         VARCHAR(60),
    year            INT,
    price           NUMERIC(10,2),
    taste_notes     TEXT,
    body            VARCHAR(20),
    acidity         VARCHAR(20),
    sweetness       VARCHAR(20),
    food_pairings   TEXT,
    description     TEXT,
    label           VARCHAR(80),
    stock_available INT
);
```

Field responsibilities at a glance:

| Field | Source of truth? | Embedded? | Used as metadata filter? |
| --- | :---: | :---: | :---: |
| `id` | yes | no | yes (`wineId`) |
| `name`, `description`, `taste_notes`, `food_pairings`, `body` | yes | yes | no |
| `grape_variety`, `region`, `country` | yes | yes | optionally |
| `type` | yes | yes | yes |
| `price`, `stock_available`, `year` | yes | **no** | yes |

The "Embedded? = no" rows are deliberate, as explained in
[01-concepts.md §1.6](01-concepts.md). Price is a number; embeddings are bad
at numbers.

## 2.3 Sample row

```sql
INSERT INTO wine (name, type, grape_variety, region, country, year,
                  price, taste_notes, body, acidity, sweetness,
                  food_pairings, description, label, stock_available)
VALUES (
    'Malbec Reserva',
    'Red',
    'Malbec',
    'Mendoza',
    'Argentina',
    2021,
    18.50,
    'plum, blackberry, soft tannins, vanilla',
    'medium',
    'medium',
    'dry',
    'grilled meat, empanadas, hard cheese',
    'Smooth red wine for casual dinners, aged 12 months in French oak.',
    'Trapiche',
    120
);
```

Query result:

```
 id |     name      | type | grape_variety | region  | year | price | ...
----+---------------+------+---------------+---------+------+-------+-----
  1 | Malbec Reserva| Red  | Malbec        | Mendoza | 2021 | 18.50 | ...
```

## 2.4 Sample catalog file

The project ships a curated set of 25 Argentine wines ready to load into this
table, matching every field above:

- Path: `src/main/resources/data/wines.json`
- Mix: 15 Red, 7 White, 1 Rosé, 2 Sparkling
- Price range: $8.50 – $55.00
- Vintages: 2018 – 2023

Loading this file is part of roadmap milestone 2
([../learning-roadmap.md](../learning-roadmap.md)). The roadmap deliberately
defers application code, so this guide only documents what the records look
like.

## 2.5 What stays out of this table

- **Embeddings** are not stored here (unless using pgvector, in which case they
  live in a dedicated column managed by the indexing service, not by business
  code). See [03-vector-store-record.md](03-vector-store-record.md).
- **Search rankings / similarity scores** are runtime computations of the
  vector database, not persisted business state.
- **User queries and answer logs** may live in a separate table later but are
  out of scope for the current milestones.

## 2.6 Updating rows without touching embeddings

When a business field that is **not embedded** changes (price, stock), you
`UPDATE` PostgreSQL normally and update the corresponding metadata in the
vector store. The embedding vector itself is unchanged because the
descriptive text is unchanged.

When an embedded field changes (description, taste notes), you must re-embed
that wine and replace the stored document. This is rare in practice.

See [03-vector-store-record.md §3.4](03-vector-store-record.md) for the
update flow.

Next: [03-vector-store-record.md](03-vector-store-record.md) — turning a row
into a vector store document.