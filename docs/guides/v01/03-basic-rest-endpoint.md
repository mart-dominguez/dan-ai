# 03 - Basic REST Endpoint

This milestone exposes CRUD endpoints for the normalized wine catalog model defined in `02-wine-catalog-model.md`.

The goal is still educational: implement the API in small steps, starting with read-only endpoints and then adding create, update, and delete operations.

## Goal

Expose REST endpoints for:

- wines
- wine types
- producers
- countries
- regions
- varietals
- food pairings
- body levels
- acidity levels
- sweetness levels

These endpoints should let us manage the relational catalog before adding embeddings, vector search, or RAG.

## Theory

The REST layer should be separated into:

```text
Controller -> Service -> Repository
```

Responsibilities:

- Controller: HTTP mapping, request validation, response codes.
- Service: business rules and relationship validation.
- Repository: database access.

Use DTOs instead of exposing JPA entities directly.

Recommended DTO types:

- `CreateRequest`
- `UpdateRequest`
- `Response`
- optional `SummaryResponse` for nested references

Example:

```text
WineCreateRequest
WineUpdateRequest
WineResponse
WineSummaryResponse
```

## API Conventions

Base path:

```text
/api
```

HTTP status codes:

| Operation | Success Status | Notes |
| --- | --- | --- |
| List | `200 OK` | Returns array |
| Get by id | `200 OK` | Returns one object |
| Create | `201 Created` | Returns created object |
| Full update | `200 OK` | Returns updated object |
| Delete | `204 No Content` | Empty response |
| Missing record | `404 Not Found` | Use consistent error body later |
| Invalid request | `400 Bad Request` | Validation or relationship errors |
| Conflict | `409 Conflict` | Duplicate code/name or unsafe delete |

## Resource Overview

| Entity | Path |
| --- | --- |
| `Wine` | `/api/wines` |
| `WineType` | `/api/wine-types` |
| `Producer` | `/api/producers` |
| `Country` | `/api/countries` |
| `Region` | `/api/regions` |
| `Varietal` | `/api/varietals` |
| `FoodPairing` | `/api/food-pairings` |
| `BodyLevel` | `/api/body-levels` |
| `AcidityLevel` | `/api/acidity-levels` |
| `SweetnessLevel` | `/api/sweetness-levels` |

## Standard CRUD Pattern

Each catalog resource should follow the same basic pattern:

```text
GET    /api/<resource>
GET    /api/<resource>/{id}
POST   /api/<resource>
PUT    /api/<resource>/{id}
DELETE /api/<resource>/{id}
```

Example for wine types:

```bash
curl http://localhost:8080/api/wine-types

curl http://localhost:8080/api/wine-types/1

curl -X POST http://localhost:8080/api/wine-types \
  -H "Content-Type: application/json" \
  -d '{"code":"RED","name":"Red","description":"Red wines"}'

curl -X PUT http://localhost:8080/api/wine-types/1 \
  -H "Content-Type: application/json" \
  -d '{"code":"RED","name":"Red wine","description":"Still red wines"}'

curl -X DELETE http://localhost:8080/api/wine-types/1
```

## Wine Endpoints

### List Wines

```text
GET /api/wines
```

Optional query filters:

```text
typeId
producerId
countryId
regionId
varietalId
foodPairingId
bodyLevelId
acidityLevelId
sweetnessLevelId
maxPrice
stockAvailable
```

Example:

```bash
curl "http://localhost:8080/api/wines?typeId=1&maxPrice=20&stockAvailable=true"
```

### Get Wine By Id

```text
GET /api/wines/{id}
```

Example:

```bash
curl http://localhost:8080/api/wines/1
```

### Create Wine

```text
POST /api/wines
```

Example request:

```json
{
  "name": "Malbec Reserva",
  "vintageYear": 2021,
  "price": 18.00,
  "stockAvailable": true,
  "stockQuantity": 12,
  "tasteNotes": "plum, blackberry, soft tannins",
  "description": "Smooth full-bodied red wine for grilled meats.",
  "labelUrl": "malbec-reserva.png",
  "alcoholPercentage": 13.8,
  "servingTemperature": "16-18 C",
  "wineTypeId": 1,
  "producerId": 1,
  "regionId": 1,
  "bodyLevelId": 3,
  "acidityLevelId": 2,
  "sweetnessLevelId": 1,
  "varietals": [
    {
      "varietalId": 1,
      "percentage": 100,
      "primaryVarietal": true
    }
  ],
  "foodPairingIds": [1, 2, 3]
}
```

Important rule:

The request should pass relationship ids, not nested entities. The service must load and validate those ids.

### Update Wine

```text
PUT /api/wines/{id}
```

Use the same body as create. For the first version, prefer full replacement with `PUT`.

Later, add `PATCH` only if partial updates are genuinely needed.

### Delete Wine

```text
DELETE /api/wines/{id}
```

Deleting a wine should remove join-table rows such as:

- `wine_varietals`
- `wine_food_pairings`

If embeddings already exist, deleting or updating a wine should also trigger reindexing later.

## Reference Entity CRUD

Reference entities are simpler than wines. They usually have `code`, `name`, and `description`.

### Wine Types

```text
GET    /api/wine-types
GET    /api/wine-types/{id}
POST   /api/wine-types
PUT    /api/wine-types/{id}
DELETE /api/wine-types/{id}
```

Example body:

```json
{
  "code": "RED",
  "name": "Red",
  "description": "Red still wines"
}
```

Delete rule:

Do not allow deleting a wine type if wines still reference it. Return `409 Conflict`.

### Producers

```text
GET    /api/producers
GET    /api/producers/{id}
POST   /api/producers
PUT    /api/producers/{id}
DELETE /api/producers/{id}
```

Example body:

```json
{
  "name": "Bodega Los Andes",
  "countryId": 1,
  "website": "https://example.com",
  "description": "Small Mendoza producer focused on Malbec."
}
```

Delete rule:

Do not allow deleting a producer if wines still reference it.

### Countries

```text
GET    /api/countries
GET    /api/countries/{id}
POST   /api/countries
PUT    /api/countries/{id}
DELETE /api/countries/{id}
```

Example body:

```json
{
  "code": "AR",
  "name": "Argentina"
}
```

Delete rule:

Do not allow deleting a country if regions still reference it.

### Regions

```text
GET    /api/regions
GET    /api/regions/{id}
POST   /api/regions
PUT    /api/regions/{id}
DELETE /api/regions/{id}
```

Example body:

```json
{
  "name": "Mendoza",
  "countryId": 1,
  "description": "Important high-altitude wine region in Argentina."
}
```

Optional filter:

```text
GET /api/regions?countryId=1
```

Delete rule:

Do not allow deleting a region if wines still reference it.

### Varietals

```text
GET    /api/varietals
GET    /api/varietals/{id}
POST   /api/varietals
PUT    /api/varietals/{id}
DELETE /api/varietals/{id}
```

Example body:

```json
{
  "name": "Malbec",
  "description": "Red grape variety known for dark fruit and soft tannins.",
  "typicalNotes": "plum, blackberry, violet, cocoa"
}
```

Delete rule:

Do not allow deleting a varietal if any `wine_varietals` row references it.

### Food Pairings

```text
GET    /api/food-pairings
GET    /api/food-pairings/{id}
POST   /api/food-pairings
PUT    /api/food-pairings/{id}
DELETE /api/food-pairings/{id}
```

Example body:

```json
{
  "name": "grilled meat",
  "description": "Beef, lamb, barbecue, and smoky grilled dishes"
}
```

Delete rule:

Do not allow deleting a food pairing if wines still reference it.

### Body Levels

```text
GET    /api/body-levels
GET    /api/body-levels/{id}
POST   /api/body-levels
PUT    /api/body-levels/{id}
DELETE /api/body-levels/{id}
```

Example body:

```json
{
  "code": "FULL",
  "name": "Full"
}
```

### Acidity Levels

```text
GET    /api/acidity-levels
GET    /api/acidity-levels/{id}
POST   /api/acidity-levels
PUT    /api/acidity-levels/{id}
DELETE /api/acidity-levels/{id}
```

Example body:

```json
{
  "code": "HIGH",
  "name": "High"
}
```

### Sweetness Levels

```text
GET    /api/sweetness-levels
GET    /api/sweetness-levels/{id}
POST   /api/sweetness-levels
PUT    /api/sweetness-levels/{id}
DELETE /api/sweetness-levels/{id}
```

Example body:

```json
{
  "code": "DRY",
  "name": "Dry"
}
```

## Relationship Management

For the first implementation, manage relationships through the main `WineCreateRequest` and `WineUpdateRequest`.

That means:

- assign wine type with `wineTypeId`
- assign producer with `producerId`
- assign region with `regionId`
- assign profile levels with `bodyLevelId`, `acidityLevelId`, `sweetnessLevelId`
- assign varietals with a `varietals` array
- assign food pairings with `foodPairingIds`

Avoid separate relationship endpoints at first.

Later, if needed, add focused endpoints:

```text
PUT    /api/wines/{wineId}/food-pairings
PUT    /api/wines/{wineId}/varietals
POST   /api/wines/{wineId}/varietals
DELETE /api/wines/{wineId}/varietals/{varietalId}
```

These are useful when a UI edits relationships independently.

## Response Shape

`WineResponse` should include nested summaries, not full recursive entities.

Example:

```json
{
  "id": 1,
  "name": "Malbec Reserva",
  "vintageYear": 2021,
  "price": 18.00,
  "stockAvailable": true,
  "wineType": {
    "id": 1,
    "code": "RED",
    "name": "Red"
  },
  "producer": {
    "id": 1,
    "name": "Bodega Los Andes"
  },
  "region": {
    "id": 1,
    "name": "Mendoza",
    "country": {
      "id": 1,
      "code": "AR",
      "name": "Argentina"
    }
  },
  "varietals": [
    {
      "id": 1,
      "name": "Malbec",
      "percentage": 100,
      "primaryVarietal": true
    }
  ],
  "foodPairings": [
    {
      "id": 1,
      "name": "grilled meat"
    }
  ],
  "profile": {
    "body": "Full",
    "acidity": "Medium",
    "sweetness": "Dry"
  }
}
```

Do not return full bidirectional object graphs. That can cause recursion and oversized responses.

## Validation Rules

Basic validation:

- `name` is required for wines, producers, regions, varietals, and food pairings.
- `code` is required for coded reference tables.
- `price` must be zero or positive.
- `vintageYear` should be reasonable, for example between `1800` and the current year.
- relationship ids must exist.
- percentages in `varietals` should be between `0` and `100`.
- exactly one varietal may be marked as primary.
- total varietal percentage should be `100` when percentages are provided.

Use Bean Validation annotations where possible:

```java
@NotBlank
@PositiveOrZero
@Min
@Max
```

Use service-level validation for relationship rules.

## Error Handling

Add a global exception handler later:

```text
@RestControllerAdvice
```

Recommended error response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Wine not found: 99",
  "path": "/api/wines/99"
}
```

For this milestone, it is enough to consistently return:

- `404` for missing ids
- `400` for invalid request bodies
- `409` for unsafe deletes or duplicate values

## Implementation Order

Keep the implementation small:

1. Implement read-only endpoints for all reference tables.
2. Add `POST`, `PUT`, and `DELETE` for reference tables.
3. Implement `GET /api/wines` and `GET /api/wines/{id}` with normalized response DTOs.
4. Add `POST /api/wines`.
5. Add `PUT /api/wines/{id}`.
6. Add `DELETE /api/wines/{id}`.
7. Add filters to `GET /api/wines`.
8. Add relationship update endpoints only if the UI needs them.

## Manual Checks

Run tests:

```bash
./mvnw test
```

Start the app:

```bash
./mvnw spring-boot:run
```

Check reference data:

```bash
curl http://localhost:8080/api/wine-types
curl http://localhost:8080/api/producers
curl http://localhost:8080/api/countries
curl http://localhost:8080/api/regions
curl http://localhost:8080/api/varietals
curl http://localhost:8080/api/food-pairings
```

Check wines:

```bash
curl http://localhost:8080/api/wines
curl http://localhost:8080/api/wines/1
```

Create a wine type:

```bash
curl -X POST http://localhost:8080/api/wine-types \
  -H "Content-Type: application/json" \
  -d '{"code":"SPARKLING","name":"Sparkling","description":"Sparkling wines"}'
```

Create a wine:

```bash
curl -X POST http://localhost:8080/api/wines \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Sauvignon Blanc Costa",
    "vintageYear":2023,
    "price":16.50,
    "stockAvailable":true,
    "stockQuantity":18,
    "tasteNotes":"citrus, herbs, green apple",
    "description":"Fresh white wine with crisp acidity for seafood.",
    "wineTypeId":2,
    "producerId":1,
    "regionId":2,
    "bodyLevelId":1,
    "acidityLevelId":3,
    "sweetnessLevelId":1,
    "varietals":[{"varietalId":4,"percentage":100,"primaryVarietal":true}],
    "foodPairingIds":[4,5]
  }'
```

## Expected Result

At the end of this milestone:

- every catalog entity has CRUD endpoints
- wines can be created with normalized relationships
- reference entities cannot be deleted when still in use
- wine responses are stable DTOs
- the catalog API is ready to feed the embedding and RAG milestones
