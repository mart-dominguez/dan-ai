# 02 - Modelo del catálogo de vinos

Define la entidad `Wine` y su repositorio como fuente de verdad de los datos. Para empezar se modela el vino como una sola tabla; el modelo puede normalizarse después sin romper los hitos siguientes.

## Objetivo

Definir la entidad `Wine` y su repositorio como fuente de verdad de los datos.

## Teoría

- **Entidad JPA**: clase Java mapeada a una tabla relacional con `@Entity`.
- **Spring Data JPA**: repositorio que expone CRUD y consultas derivadas sin SQL manual.
- **Relational DB como source of truth**: los datos viven en la BD relacional; los embeddings son una *derivación* de esos datos.
- Campos clave del vino: `name`, `type`, `grapeVariety`, `region`, `country`, `year`, `price`, `tasteNotes`, `body`, `acidity`, `sweetness`, `foodPairings`, `description`, `label`, `stockAvailable`.

> Nota: `year` es palabra reservada en SQL; usar `vintageYear` en Java y `vintage_year` como columna en BD.

## Campos sugeridos

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `name` | `String` | Nombre comercial |
| `type` | `String` | Red, White, Rose, Sparkling... (normalizar más adelante) |
| `grapeVariety` | `String` | Malbec, Cabernet... |
| `region` | `String` | Mendoza, Rioja... |
| `country` | `String` | Argentina, España... |
| `vintageYear` | `Integer` | Año de cosecha |
| `price` | `BigDecimal` | Precio de catálogo |
| `tasteNotes` | `String` | Notas de cata en texto libre |
| `body` | `String` | light, medium, full |
| `acidity` | `String` | low, medium, high |
| `sweetness` | `String` | dry, off-dry, sweet |
| `foodPairings` | `String` | Maridajes separados por coma |
| `description` | `String` | Descripción humana |
| `label` | `String` | URL o nombre del archivo de etiqueta |
| `stockAvailable` | `boolean` | Si puede recomendarse como disponible |

## Alcance de implementación

- Dependencia `spring-boot-starter-data-jpa` y driver de BD (H2 para empezar).
- Clase `Wine` (entidad) con Lombok.
- `WineRepository extends JpaRepository<Wine, Long>`.
- Cargar datos de ejemplo (algunos vinos) con `data.sql` o un `CommandLineRunner`.

## Repositorio

```java
public interface WineRepository extends JpaRepository<Wine, Long> {
    List<Wine> findByType(String type);
    List<Wine> findByPriceLessThanEqual(BigDecimal maxPrice);
    List<Wine> findByStockAvailableTrue();
}
```

## Ejemplo de documento de vino (semilla para el Hito 7)

```text
Name: Malbec Reserva.
Type: Red wine.
Grape variety: Malbec.
Region: Mendoza, Argentina.
Vintage: 2021.
Price: 18.00.
Taste notes: plum, blackberry, soft tannins.
Body: medium. Acidity: medium. Sweetness: dry.
Food pairings: grilled meat, empanadas, hard cheese.
Description: Smooth red wine for casual dinners.
Stock available: true.
```

## Resultado esperado

La BD contiene vinos y el repositorio puede listarlos y buscarlos por id.

## Verificación manual

- Levantar la app y consultar `GET /wines` (si ya hay endpoint) o usar un `CommandLineRunner` que imprima los vinos al iniciar.
- Verificar la BD H2 en consola (`/h2-console`).