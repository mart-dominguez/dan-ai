# 02 - Wine Catalog Model

This milestone defines the relational catalog model that will be the source of truth for the assistant.

The first implementation can start with a simple `Wine` table, but the target model should be normalized enough to avoid repeating values such as wine type, producer, grape varieties, regions, and food pairing categories.

## Goal

Create a clear catalog model for wines and the related entities that describe them.

The model should support:

- listing and searching wines through REST endpoints
- filtering by structured values such as type, price, stock, producer, region, and grape
- generating high-quality text documents for embeddings
- rebuilding the vector index from relational data

## Core Entity: `Wine`

`Wine` is the main business entity. It represents one sellable catalog item.

Suggested fields:

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `name` | `String` | Commercial wine name |
| `vintageYear` | `Integer` | Use `vintage_year` as DB column; avoid reserved word `year` |
| `price` | `BigDecimal` | Current catalog price |
| `stockAvailable` | `boolean` | Whether it can be recommended as available |
| `stockQuantity` | `Integer` | Optional, useful later |
| `tasteNotes` | `String` | Free text notes such as plum, citrus, oak |
| `description` | `String` | Human-readable catalog description |
| `labelUrl` | `String` | Image URL or local label asset |
| `alcoholPercentage` | `BigDecimal` | Optional but useful for recommendations |
| `servingTemperature` | `String` | Optional, for richer recommendations |

Suggested relationships:

| Relationship | Cardinality | Example |
| --- | --- | --- |
| `wineType` | many wines to one type | red, white, rose, sparkling, dessert |
| `producer` | many wines to one producer | Catena Zapata, Concha y Toro |
| `region` | many wines to one region | Mendoza, Patagonia, Rioja |
| `country` | through region or direct many-to-one | Argentina, Chile, Spain |
| `varietals` | many wines to many varietals | Malbec, Cabernet Sauvignon |
| `foodPairings` | many wines to many food categories | grilled meat, seafood, pasta |
| `profile` | many wines to one profile or embedded fields | body, acidity, sweetness |

## Normalized Entities

### `WineType`

Represents the broad type of wine.

Fields:

- `id`
- `code`
- `name`
- `description`

Examples:

- `RED`
- `WHITE`
- `ROSE`
- `SPARKLING`
- `DESSERT`

Relationship:

```text
WineType 1 --- N Wine
```

Why normalize it:

- avoids inconsistent values like `red`, `Red`, `tinto`
- enables filters such as `type=RED`
- makes translations easier later

### `Producer`

Represents the winery, manufacturer, or vendor responsible for the wine.

Fields:

- `id`
- `name`
- `country`
- `website`
- `description`

Relationship:

```text
Producer 1 --- N Wine
```

Naming note:

- Use `Producer` or `Winery` if the entity means who makes the wine.
- Use `Vendor` if the entity means who sells or distributes the wine.
- If the business needs both, model them separately:

```text
Producer 1 --- N Wine
Vendor   1 --- N WineOffering
Wine     1 --- N WineOffering
```

For the learning project, start with `Producer`.

### `Country`

Represents countries in a normalized way.

Fields:

- `id`
- `code`
- `name`

Examples:

- `AR`, Argentina
- `CL`, Chile
- `ES`, Spain

Relationship:

```text
Country 1 --- N Region
```

### `Region`

Represents wine regions.

Fields:

- `id`
- `name`
- `country`
- `description`

Relationship:

```text
Country 1 --- N Region
Region  1 --- N Wine
```

Examples:

- Mendoza, Argentina
- Patagonia, Argentina
- Casablanca Valley, Chile
- Rioja, Spain

### `Varietal` or `Grape`

Represents grape varieties.

Fields:

- `id`
- `name`
- `description`
- `typicalNotes`

Examples:

- Malbec
- Cabernet Sauvignon
- Pinot Noir
- Chardonnay
- Sauvignon Blanc
- Torrontes

Relationship:

```text
Wine N --- N Varietal
```

Use a join table:

```text
wine_varietals
- wine_id
- varietal_id
- percentage
- primary_varietal
```

Why many-to-many:

- some wines are single-varietal
- some wines are blends
- percentages and primary grape can matter for recommendations

Example:

```text
Wine: Bordeaux Blend
- Cabernet Sauvignon 60%, primary
- Merlot 30%
- Cabernet Franc 10%
```

### `FoodPairing`

Represents normalized food categories.

Fields:

- `id`
- `name`
- `description`

Examples:

- grilled meat
- seafood
- pasta with tomato sauce
- creamy pasta
- hard cheese
- salads
- spicy food
- dessert

Relationship:

```text
Wine N --- N FoodPairing
```

Use a join table:

```text
wine_food_pairings
- wine_id
- food_pairing_id
```

Why normalize it:

- supports filters like `foodCategory=seafood`
- avoids inconsistent free text
- improves embedding text because pairings are curated

### `WineProfile`

Represents sensory structure. This can be a separate entity or simple fields on `Wine`.

For the first normalized version, simple reference tables are enough:

```text
BodyLevel
- id
- code
- name

AcidityLevel
- id
- code
- name

SweetnessLevel
- id
- code
- name
```

Examples:

- body: light, medium, full
- acidity: low, medium, high
- sweetness: dry, off-dry, sweet

Relationships:

```text
BodyLevel     1 --- N Wine
AcidityLevel  1 --- N Wine
SweetnessLevel 1 --- N Wine
```

This makes queries like "soft red wine" or "dry white wine" easier to map to structured filters.

## Suggested ER Model

```text
Country
  1
  |
  N
Region
  1
  |
  N
Wine
  N -------- N Varietal
  |          through wine_varietals
  |
  N -------- N FoodPairing
  |          through wine_food_pairings
  |
  N
  |
  1
WineType

Producer
  1
  |
  N
Wine

BodyLevel
  1
  |
  N
Wine

AcidityLevel
  1
  |
  N
Wine

SweetnessLevel
  1
  |
  N
Wine
```

## Suggested Tables

### `wines`

```text
id
name
vintage_year
price
stock_available
stock_quantity
taste_notes
description
label_url
alcohol_percentage
serving_temperature
wine_type_id
producer_id
region_id
body_level_id
acidity_level_id
sweetness_level_id
```

### Reference Tables

```text
wine_types
producers
countries
regions
varietals
food_pairings
body_levels
acidity_levels
sweetness_levels
```

### Join Tables

```text
wine_varietals
- wine_id
- varietal_id
- percentage
- primary_varietal

wine_food_pairings
- wine_id
- food_pairing_id
```

## JPA Mapping Guidance

Use `@ManyToOne` for reference data:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "wine_type_id", nullable = false)
private WineType wineType;
```

Use `@ManyToMany` only when the join table has no extra fields.

For varietals, prefer an explicit join entity because percentage and primary grape are useful:

```text
Wine 1 --- N WineVarietal N --- 1 Varietal
```

That maps better than plain `@ManyToMany`:

```java
@OneToMany(mappedBy = "wine", cascade = CascadeType.ALL, orphanRemoval = true)
private List<WineVarietal> varietals;
```

For food pairings, a plain many-to-many is acceptable at first:

```java
@ManyToMany
@JoinTable(
    name = "wine_food_pairings",
    joinColumns = @JoinColumn(name = "wine_id"),
    inverseJoinColumns = @JoinColumn(name = "food_pairing_id")
)
private Set<FoodPairing> foodPairings;
```

## Embedding Document Impact

The normalized model should still be converted into one readable wine document for embeddings.

Example document:

```text
Name: Malbec Reserva.
Type: Red wine.
Producer: Bodega Los Andes.
Grape varieties: Malbec 100%.
Region: Mendoza, Argentina.
Vintage: 2021.
Price: 18.00.
Profile: full body, medium acidity, dry.
Taste notes: plum, blackberry, soft tannins.
Food pairings: grilled meat, barbecue, hard cheese.
Description: Smooth full-bodied red wine for grilled meats and relaxed dinners.
Stock available: true.
```

Important rule:

The vector database should store the generated document and metadata, but the normalized relational database remains the source of truth.

Useful vector metadata:

```text
wineId
wineType
producerId
regionId
countryCode
varietalIds
foodPairingIds
price
stockAvailable
bodyLevel
acidityLevel
sweetnessLevel
```

## Implementation Scope For This Milestone

For a learning-first implementation, do not build every table at once.

Recommended progression:

1. Start with `Wine` as a single table.
2. Extract `WineType`.
3. Extract `Producer`.
4. Extract `Country` and `Region`.
5. Extract `Varietal` with `WineVarietal`.
6. Extract `FoodPairing`.
7. Extract body, acidity, and sweetness levels if filtering needs become important.

Each extraction should preserve the existing REST behavior before moving to the next one.

## Expected Result

At the end of the complete catalog-model phase, the project should have a normalized model that can answer questions like:

- "Show me red wines from Mendoza."
- "Find Malbec blends under 20 dollars."
- "Recommend available white wines for seafood."
- "Which wines from this producer pair with grilled meat?"

## Manual Check

After each extraction:

```bash
./mvnw test
curl http://localhost:8080/api/wines
```

Verify that:

- sample data still loads
- wine JSON still contains the fields needed by the API
- generated wine documents still include type, producer, region, grapes, profile, and pairings

## Notes For Future PostgreSQL Design

When moving from H2 to PostgreSQL:

- keep `vintage_year`, not `year`
- use foreign keys for normalized references
- add indexes on common filters: `wine_type_id`, `producer_id`, `region_id`, `price`, `stock_available`
- consider pgvector later for embeddings
- keep vector data rebuildable from relational data
