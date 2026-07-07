# 03 - Endpoint REST básico

Expone un endpoint REST simple para listar y buscar vinos, **sin IA todavía**.

## Objetivo

Exponer un endpoint REST simple para listar y buscar vinos, sin usar IA todavía.

## Teoría

- **`@RestController` y `@GetMapping`**: exponer recursos HTTP en Spring Boot.
- **DTO vs entidad**: por qué conviene separar la representación de API de la entidad de persistencia.
- **`ResponseEntity`**: control del código de estado HTTP.
- **Capas**: controller → service → repository.

El controlador no debe conocer la entidad JPA directamente: el servicio la convierte a un DTO estable.

## Alcance de implementación

- `WineController` con `GET /api/wines` y `GET /api/wines/{id}`.
- `WineService` con la lógica de negocio.
- (Opcional) `WineDTO` para la respuesta.

## Capas

```text
Controller -> Service -> Repository
```

- **Controller**: mapeo HTTP, validación de request, códigos de estado.
- **Service**: reglas de negocio y conversión entidad → DTO.
- **Repository**: acceso a BD.

## Endpoints

```bash
curl http://localhost:8080/api/wines
curl http://localhost:8080/api/wines/1
```

### Respuesta esperada (`GET /api/wines/1`)

```json
{
  "id": 1,
  "name": "Malbec Reserva",
  "type": "Red",
  "grapeVariety": "Malbec",
  "region": "Mendoza",
  "country": "Argentina",
  "vintageYear": 2021,
  "price": 18.00,
  "stockAvailable": true,
  "tasteNotes": "plum, blackberry, soft tannins",
  "foodPairings": "grilled meat, empanadas, hard cheese",
  "description": "Smooth red wine for casual dinners."
}
```

## Códigos de estado

| Operación | Estado | Notas |
| --- | --- | --- |
| List | `200 OK` | Devuelve un array |
| Get por id | `200 OK` | Devuelve un objeto |
| No encontrado | `404 Not Found` | Devuelve cuerpo de error consistente |

## Resultado esperado

`curl http://localhost:8080/api/wines` devuelve la lista de vinos en JSON.

## Verificación manual

Usar `curl` o Bruno (ya existe carpeta `bruno/` en el proyecto) para hacer GET y validar el JSON.

```bash
./mvnw spring-boot:run
curl http://localhost:8080/api/wines
curl http://localhost:8080/api/wines/1
```