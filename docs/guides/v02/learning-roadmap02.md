# Roadmap de aprendizaje — Asistente de recomendación de vinos con IA

> Proyecto educativo para aprender **Spring Boot + Spring AI + Ollama + Embeddings + RAG** de forma gradual.
>
> Cada hito (milestone) es pequeño y autocontenido: no se debe avanzar al siguiente sin entender el actual.
>
> El código **no se implementa en este documento**. Aquí solo se define el plan y la convención de documentación.

---

## Contexto del proyecto

El sistema es un asistente que recomienda vinos a partir de un catálogo relacional. El usuario hace preguntas en lenguaje natural, por ejemplo:

- "¿Qué vino sirve con carne asada?"
- "Recomendá un vino para pasta con salsa de tomate por menos de 20 dólares."
- "Quiero un tinto suave para alguien que no suele tomar vino."
- "¿Qué vino de mi catálogo combina con mariscos?"

Cada vino del catálogo tiene campos como: `name`, `type`, `grape variety`, `region`, `country`, `year`, `price`, `taste notes`, `body`, `acidity`, `sweetness`, `food pairings`, `description`, `label` y `stock availability`.

### Arquitectura objetivo (visión general)

```
                 ┌─────────────┐    pregunta     ┌──────────────────┐
   Usuario  ───► │  REST API   │ ──────────────► │   ChatClient     │
                 └─────────────┘                 │  (Spring AI)     │
                                                  └────────┬─────────┘
                                                           │
                          ┌────────────────────────────────┼────────────────────┐
                          │                                │                    │
                          ▼                                ▼                    ▼
                 ┌─────────────────┐         ┌──────────────────┐    ┌──────────────────┐
                 │  BD relacional  │◄────────│   Vector Store   │    │   Ollama (LLM)   │
                 │  (source of     │  sync   │  (embeddings +   │    │   modelo local   │
                 │   truth)        │         │   similarity)    │    │   ej: llama3     │
                 └─────────────────┘         └──────────────────┘    └──────────────────┘
```

- **BD relacional**: fuente de verdad de los vinos.
- **Embeddings**: representación semántica de cada vino en un espacio vectorial.
- **Vector store**: índice para buscar vinos similares por significado.
- **Ollama**: proveedor de LLM local (no depende de la nube).
- **Spring AI**: capa de integración que unifica chat, embeddings y vector store.
- **RAG (Retrieval-Augmented Generation)**: recuperar vinos relevantes y entregarlos como contexto al LLM antes de responder.

---

## Convención de documentación

Cada hito genera **un archivo Markdown** en la carpeta `docs/guides/v02` con un número correlativo y un nombre en inglés (kebab-case), siguiendo el orden del roadmap:

| # | Archivo                         | Hito                                                     |
|---|---------------------------------|----------------------------------------------------------|
| 1 | `01-overview.md`                | Visión general y arquitectura                            |
| 2 | `02-wine-catalog-model.md`      | Modelo del catálogo de vinos                             |
| 3 | `03-basic-rest-endpoint.md`     | Endpoint REST básico                                     |
| 4 | `04-chat-client-ollama.md`      | Chatbot simple con `ChatClient` y Ollama                 |
| 5 | `05-system-prompts.md`          | Personalización con system prompts                       |
| 6 | `06-basic-recommendation-without-rag.md` | Recomendación sin RAG (contexto en el prompt) |
| 7 | `07-embeddings-concept.md`      | Concepto de embeddings y representación de documentos    |
| 8 | `08-vector-store.md`            | Almacenamiento de embeddings en un vector store          |
| 9 | `09-semantic-search.md`         | Búsqueda semántica sobre vinos                           |
| 10| `10-rag-recommendation.md`      | Endpoint de recomendación con RAG                        |
| 11| `11-sql-filters.md`             | Filtros SQL (precio, stock, tipo, comida)                |
| 12| `12-structured-output.md`       | Salida estructurada del LLM                              |
| 13| `13-tests.md`                   | Tests automatizados                                      |
| 14| `14-final-docs.md`              | Documentación final y mejoras futuras                    |

> **Regla**: al terminar cada hito se crea (o actualiza) el archivo correspondiente explicando qué se hizo y la teoría detrás. El archivo `learning-roadmap.md` (este) es el índice general y no se numera.

---

## Pila tecnológica (stack)

- **Java 21**
- **Spring Boot 4.1.0** (web, actuator)
- **Spring AI 2.0.0** (`spring-ai-starter-model-ollama`, vector store, embeddings)
- **Ollama** corriendo en local (modelo sugerido: `llama3` o `qwen`)
- **Lombok** para reducir boilerplate
- **Maven** como herramienta de build
- BD relacional y vector store a definir en los hitos correspondientes

---

## Hito 1 — Visión general y arquitectura

### 1. Goal
Comprender qué construiremos, por qué y cómo encajan las piezas (BD relacional, embeddings, vector store, Ollama, Spring AI, RAG) antes de escribir código.

### 2. Theory
- **LLM (Large Language Model)**: modelo que genera texto a partir de un prompt. Por sí solo no conoce nuestros vinos.
- **Ollama**: herramienta que ejecuta LLMs en local, expone una API HTTP y se integra con Spring AI sin claves de nube.
- **Spring AI**: abstracción de Spring para chat, embeddings y vector stores con interfaces uniformes (`ChatClient`, `EmbeddingModel`, `VectorStore`).
- **Embedding**: vector de números que representa el significado de un texto. Textos parecidos → vectores cercanos.
- **Vector store**: base de datos optimizada para buscar por similitud vectorial (coseno, etc.).
- **RAG (Retrieval-Augmented Generation)**: patrón que primero *recupera* datos relevantes (del vector store) y luego se los entrega al LLM como contexto para que responda con fundamento.
- **Por qué RAG**: el LLM no "sabe" del catálogo; RAG le inyecta la información correcta en el prompt.

### 3. Implementation Scope
- Crear este archivo `learning-roadmap.md`.
- Definir convención de documentación.
- No se escribe código todavía.

### 4. Expected Result
Existe un roadmap claro que guía el aprendizaje paso a paso.

### 5. How to Test
Revisar que el documento esté completo y cada hito tenga los 6 puntos (Goal, Theory, Scope, Result, Test, Doc).

### 6. Documentation to Generate
`01-overview.md` — visión general, arquitectura y glosario de términos.

---

## Hito 2 — Modelo del catálogo de vinos

### 1. Goal
Definir la entidad `Wine` y su repositorio como fuente de verdad de los datos.

### 2. Theory
- **Entidad JPA**: clase Java mapeada a una tabla relacional con `@Entity`.
- **Spring Data JPA**: repositorio que expone CRUD y consultas derivadas sin SQL manual.
- **Relational DB como source of truth**: los datos viven en la BD relacional; los embeddings son una *derivación* de esos datos.
- Campos clave del vino: `name`, `type`, `grapeVariety`, `region`, `country`, `year`, `price`, `tasteNotes`, `body`, `acidity`, `sweetness`, `foodPairings`, `description`, `label`, `stockAvailable`.

### 3. Implementation Scope
- Dependencia `spring-boot-starter-data-jpa` y driver de BD (H2 para empezar).
- Clase `Wine` (entidad) con Lombok.
- `WineRepository extends JpaRepository<Wine, Long>`.
- Cargar datos de ejemplo (algunos vinos) con `data.sql` o un `CommandLineRunner`.

### 4. Expected Result
La BD contiene vinos y el repositorio puede listarlos y buscarlos por id.

### 5. How to Test
- Levantar la app y consultar `GET /wines` (si ya hay endpoint) o usar un `CommandLineRunner` que imprima los vinos al iniciar.
- Verificar la BD H2 en consola (`/h2-console`).

### 6. Documentation to Generate
`02-wine-catalog-model.md` — modelo de datos, decisiones de campos y cómo se carga data de ejemplo.

---

## Hito 3 — Endpoint REST básico

### 1. Goal
Exponer un endpoint REST simple para listar y buscar vinos, sin IA todavía.

### 2. Theory
- **`@RestController` y `@GetMapping`**: exponer recursos HTTP en Spring Boot.
- **DTO vs entidad**: por qué conviene separar la representación de API de la entidad de persistencia.
- **`ResponseEntity`**: control del código de estado HTTP.
- **Capas**: controller → service → repository.

### 3. Implementation Scope
- `WineController` con `GET /api/wines` y `GET /api/wines/{id}`.
- `WineService` con la lógica de negocio.
- (Opcional) `WineDTO` para la respuesta.

### 4. Expected Result
`curl http://localhost:8080/api/wines` devuelve la lista de vinos en JSON.

### 5. How to Test
Usar `curl` o Bruno (ya existe carpeta `bruno/` en el proyecto) para hacer GET y validar el JSON.

### 6. Documentation to Generate
`03-basic-rest-endpoint.md` — estructura de capas y convenciones de la API REST.

---

## Hito 4 — Chatbot simple con `ChatClient` y Ollama

### 1. Goal
Integrar Ollama con Spring AI y responder a un prompt simple del usuario, sin usar aún el catálogo.

### 2. Theory
- **`ChatClient`**: interfaz principal de Spring AI para hablar con un LLM. Reemplaza al uso directo de HTTP contra Ollama.
- **Autoconfiguración de Spring AI**: `spring-ai-starter-model-ollama` configura el cliente a partir de properties.
- **Properties clave**: `spring.ai.ollama.base-url`, `spring.ai.ollama.chat.model`.
- **Prompt y respuesta**: el usuario envía texto, el LLM devuelve texto.
- **Sin estado**: el LLM no recuerda conversaciones previas salvo que lo modelemos.

### 3. Implementation Scope
- Verificar Ollama corriendo y el modelo descargado (`ollama pull llama3`).
- Configurar `application.properties` (`spring.ai.ollama.*`).
- Bean `ChatClient.Builder` (inyectado por Spring AI).
- Endpoint `POST /api/chat` que reciba `{ "message": "..." }` y devuelva la respuesta del LLM.

### 4. Expected Result
Al mandar "Hola, ¿qué sos?" el endpoint responde con texto generado por Ollama.

### 5. How to Test
`curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{"message":"Hola"}'`

### 6. Documentation to Generate
`04-chat-client-ollama.md` — configuración de Ollama, `ChatClient` y ciclo básico de prompt/respuesta. (Amplía al `01-configurar-ollama.md` ya existente.)

---

## Hito 5 — Personalización con system prompts

### 1. Goal
Darle al LLM un "rol" y reglas de comportamiento mediante un *system prompt*.

### 2. Theory
- **System prompt**: instrucciones de contexto que definen el rol, tono y reglas del asistente. Se envía antes del mensaje del usuario.
- **User prompt**: la pregunta concreta del usuario.
- **`ChatClient` con `defaultSystem(...)`**: fija un system prompt por defecto para todos los pedidos.
- **Parámetros con `{placeholder}`**: Spring AI permite plantillas con variables reemplazables.
- **Por qué importa**: un system prompt convierte un LLM genérico en un "sommelier" con personalidad y límites.

### 3. Implementation Scope
- Definir un system prompt: "Sos un sommelier experto en vinos del catálogo. Respondé en español, en breve, y si no sabés algo decílo."
- Usar `ChatClient.Builder` con `defaultSystem(...)`.
- Endpoint `POST /api/chat` reutilizado, ahora con comportamiento guiado.

### 4. Expected Result
Las respuestas adoptan el rol y tono definidos, aunque el LLM todavía no conoce el catálogo real (puede alucinar).

### 5. How to Test
Mandar "Recomendá un tinto" y verificar que la respuesta tenga el tono de sommelier y admita limitaciones.

### 6. Documentation to Generate
`05-system-prompts.md` — system vs user prompt, plantillas y cómo fijar el rol.

---

## Hito 6 — Recomendación sin RAG (contexto en el prompt)

### 1. Goal
Demostrar el problema base: recomendar vinos pasándole el catálogo entero dentro del prompt. Esto revela las limitaciones que RAG resolverá.

### 2. Theory
- **Inyección de contexto**: incluir datos del catálogo como texto en el prompt del usuario.
- **Limitaciones**: el contexto cabe hasta un límite de tokens; no escala con muchos vinos; el LLM puede ignorar datos.
- **Por qué RAG luego**: en lugar de pasar *todo*, pasar solo los vinos *relevantes* recuperados por similitud.
- **`UserMessage` dinámico**: construir el prompt concatenando los vinos recuperados de la BD relacional.

### 3. Implementation Scope
- `WineService` con método que traiga todos los vinos y los formatee como texto.
- Endpoint `POST /api/recommend` que arma un prompt con el catálogo y la pregunta del usuario, y lo envíe al `ChatClient`.

### 4. Expected Result
El asistente recomienda vinos reales del catálogo, pero el prompt crece con la cantidad de vinos (problema a resolver).

### 5. How to Test
Mandar "¿Qué tinto barato recomendás?" y comprobar que la respuesta menciona vinos del catálogo. Contar el tamaño del prompt enviado.

### 6. Documentation to Generate
`06-basic-recommendation-without-rag.md` — inyección de contexto, sus límites y motivación para RAG.

---

## Hito 7 — Concepto de embeddings y representación de documentos

### 1. Goal
Entender qué es un embedding y cómo representar cada vino como un documento de texto para embeberlo.

### 2. Theory
- **Embedding**: vector de números (ej. 1536 dimensiones) que captura el significado de un texto. Textos semánticamente parecidos → vectores cercanos.
- **`EmbeddingModel`**: interfaz de Spring AI que convierte texto → vector. Ollama puede proveer embeddings (`nomic-embed-text` u otro).
- **Documento**: unidad de contenido a embeber. Para un vino, se construye un texto rico combinando sus campos (nombre, tipo, uva, región, notas de cata, maridajes, descripción).
- **Calidad del documento**: más información semántica relevante → mejores recuperaciones.
- **¿Por qué no embeber la fila cruda?**: un texto natural ("Tinto Malbec de Mendoza, cuerpo medio, marida con carne asada...") produce embeddings más expresivos que una fila tabular.

### 3. Implementation Scope
- Método que, dado un `Wine`, arme un `String` descriptivo (el "documento").
- No se persiste todavía el embedding: solo se genera y se imprime para inspeccionar.
- Configurar el modelo de embeddings de Ollama en properties.

### 4. Expected Result
Para un vino de ejemplo se obtiene un vector (o al menos se valida que `EmbeddingModel` responde).

### 5. How to Test
Llamar al `EmbeddingModel` con el documento de un vino e imprimir la cantidad de dimensiones del vector resultante.

### 6. Documentation to Generate
`07-embeddings-concept.md` — qué es un embedding, cómo armar el documento de vino y elección del modelo de embeddings.

---

## Hito 8 — Almacenamiento de embeddings en un vector store

### 1. Goal
Persistir los embeddings de los vinos en un vector store para poder buscar por similitud.

### 2. Theory
- **Vector store**: almacena documentos junto con su embedding y metadatos. Permite búsqueda por similitud (coseno, euclidiana).
- **`VectorStore` de Spring AI**: interfaz unificada; implementaciones (SimpleInMemory, PgVector, Chroma, etc.).
- **`Document`**: objeto de Spring AI con `id`, `content`, `metadata` y `embedding`.
- **Metadatos**: guardamos el `wineId` como metadata para luego volver a la BD relacional y traer los datos completos.
- **Indexación**: proceso de generar embeddings para todos los vinos y guardarlos en el vector store.

### 3. Implementation Scope
- Elegir e implementar un vector store (empezar con uno simple y migrar si hace falta).
- Servicio `WineEmbeddingService` que recorra los vinos y guarde un `Document` por cada uno.
- Endpoint o `CommandLineRunner` que dispare la indexación.

### 4. Expected Result
El vector store contiene un documento por vino, con su embedding y `wineId` en metadata.

### 5. How to Test
Consultar el tamaño del vector store y buscar por similitud un texto de prueba ("tinto suave para carne") mostrando los ids recuperados.

### 6. Documentation to Generate
`08-vector-store.md` — elección de vector store, modelo de `Document`, metadatos y proceso de indexación.

---

## Hito 9 — Búsqueda semántica sobre vinos

### 1. Goal
Dada una consulta del usuario, recuperar los vinos más parecidos por significado.

### 2. Theory
- **Similaridad semántica**: la consulta del usuario se embebe y se comparan sus vectores contra los del store.
- **`VectorStore.similaritySearch(...)`**: método de Spring AI que devuelve los `Document` más cercanos.
- **Top-k y umbral (threshold)**: controlar cuántos resultados y qué tan parecidos deben ser.
- **Volver a la fuente de verdad**: con los `wineId` de metadata, ir al `WineRepository` y reconstruir los vinos completos.

### 3. Implementation Scope
- `SemanticSearchService` que reciba un texto, haga `similaritySearch` y devuelva los `Wine` completos.
- Endpoint `GET /api/wines/search?q=...` que devuelva los vinos similares en JSON.

### 4. Expected Result
Buscar "tinto para carne asada" devuelve vinos tintos con maridaje a carnes, ordenados por relevancia.

### 5. How to Test
`curl "http://localhost:8080/api/wines/search?q=tinto%20para%20carne%20asada"` y revisar que los resultados tengan sentido.

### 6. Documentation to Generate
`09-semantic-search.md` — similaridad, top-k/threshold y cómo volver del vector store a la BD relacional.

---

## Hito 10 — Recomendación con RAG

### 1. Goal
Combinar búsqueda semántica + LLM para responder con RAG: recuperar vinos relevantes y dárselos como contexto al `ChatClient`.

### 2. Theory
- **RAG = Retrieve + Generate**: (1) recuperar documentos relevantes del vector store, (2) armar un prompt con esos documentos como contexto, (3) dejar que el LLM genere la respuesta.
- **`RetrievalAugmentationAdvisor` / patrones de Spring AI**: Spring AI ofrece advisors que automatizan el retrieval y la inyección de contexto en el prompt.
- **Context window**: ahora solo pasamos los vinos relevantes (no todo el catálogo), escalando mejor.
- **Citación / fundamento**: la respuesta debe apoyarse en los vinos recuperados; se puede pedir al LLM que cite nombres o ids.

### 3. Implementation Scope
- Refactorizar `POST /api/recommend` para que:
  1. Recupere top-k vinos vía búsqueda semántica.
  2. Construya (o use un advisor para) el prompt con contexto.
  3. Llame al `ChatClient` y devuelva la respuesta.
- Mantener el system prompt de sommelier del Hito 5.

### 4. Expected Result
Preguntas como "¿Qué vino sirve con mariscos?" se responden citando vinos reales del catálogo, recuperados por semántica.

### 5. How to Test
Probar varias preguntas naturales y verificar que las recomendaciones correspondan a vinos existentes en la BD.

### 6. Documentation to Generate
`10-rag-recommendation.md` — flujo RAG, advisors de Spring AI y comparación con el Hito 6.

---

## Hito 11 — Filtros SQL (precio, stock, tipo, comida)

### 1. Goal
Combinar búsqueda semántica con filtros estructurados (rango de precio, stock disponible, tipo, categoría de comida) para que la recuperación respete restricciones duras.

### 2. Theory
- **Filtros estructurados vs semánticos**: la semántica encuentra "parecidos", pero el precio y el stock son restricciones exactas.
- **Metadata filter en vector store**: Spring AI permite filtrar por metadatos durante `similaritySearch` (ej. `price < 20`, `stock == true`).
- **Estrategia híbrida**: aplicar filtros por metadata en el vector store o, alternativamente, filtrar en la BD relacional después de recuperar candidatos.
- **Extracción de criterios**: parsear la consulta del usuario para inferir filtros (precio máx, tipo, etc.) antes de recuperar.

### 3. Implementation Scope
- Agregar metadata útil a cada `Document` (`price`, `type`, `stockAvailable`, `foodPairings`).
- Definir un `WineSearchCriteria` (precio máx, tipo, stock, comida).
- Aplicar el filtro en `similaritySearch` o post-recuperación.
- Actualizar `POST /api/recommend` para aceptar criterios opcionales.

### 4. Expected Result
"Recomendá un vino para pasta por menos de 20 dólares" devuelve solo vinos que cumplen precio y maridaje.

### 5. How to Test
Mandar consultas con y sin restricciones, y comprobar que los resultados respetan los filtros.

### 6. Documentation to Generate
`11-sql-filters.md` — filtros por metadata, estrategia híbrida y extracción de criterios desde la consulta.

---

## Hito 12 — Salida estructurada del LLM

### 1. Goal
Que el LLM devuelva una recomendación estructurada (JSON) en vez de texto libre, para que la API la pueda consumir un frontend.

### 2. Theory
- **Salida estructurada**: pedirle al LLM que responda con un objeto JSON que cumpla un esquema.
- **`ChatClient.entity(...)` / `BeanOutputConverter`**: Spring AI convierte la respuesta del LLM a un POJO o record.
- **Esquema del output**: definir un `WineRecommendation` con campos como `wineId`, `name`, `reason`, `confidence`.
- **Ventajas**: parseo seguro, integración con frontend, validación.

### 3. Implementation Scope
- Definir `WineRecommendation` (record).
- Usar `ChatClient` con conversión a ese tipo.
- Actualizar `POST /api/recommend` para devolver `WineRecommendation` (o una lista) en JSON.

### 4. Expected Result
La respuesta es un JSON con campos predecibles, no un texto libre.

### 5. How to Test
Hacer un `POST` y validar que el JSON devuelto parsea contra el esquema esperado (con un script o Bruno).

### 6. Documentation to Generate
`12-structured-output.md` — salida estructurada, `BeanOutputConverter` y diseño del esquema de recomendación.

---

## Hito 13 — Tests

### 1. Goal
Agregar tests que validen cada capa: repositorio, servicio, búsqueda semántica y endpoint RAG.

### 2. Theory
- **Test de capas**: unitarios para servicios, de integración para repositorio y vector store.
- **`@SpringBootTest`, `@DataJpaTest`, `MockMvc`**: herramientas de test de Spring Boot.
- **Mock del LLM**: en tests de unitarios no depender de Ollama; mockear `ChatClient` y `EmbeddingModel`.
- **Tests de integración con Ollama**: opcionales, marcarlos para correr solo cuando Ollama esté disponible.
- **Determinismo**: los LLM no son deterministas; los tests deben validar formato/esquema o usar respuestas simuladas.

### 3. Implementation Scope
- Tests de `WineRepository`.
- Tests de `WineService` con `@MockBean` del LLM.
- Test del endpoint con `MockMvc` mockeando `ChatClient`.
- (Opcional) test de integración con Ollama real, etiquetado para ejecución manual.

### 4. Expected Result
`./mvnw test` pasa y cubre las capas principales sin requerir Ollama en CI.

### 5. How to Test
Correr `./mvnw test` y revisar el reporte.

### 6. Documentation to Generate
`13-tests.md` — estrategia de testeo, mocks del LLM y manejo de no-determinismo.

---

## Hito 14 — Documentación final y mejoras futuras

### 1. Goal
Consolidar la documentación del proyecto y enumerar mejoras posibles para seguir aprendiendo.

### 2. Theory
- **Buenas prácticas de RAG en producción**: reindexación, versionado de embeddings, evaluación de calidad.
- **Mejoras avanzadas**: re-ranking, hybrid search (semántica + keyword), multi-tenancy, streaming de respuestas, conversación con memoria.
- **Observabilidad**: trazas de prompts, latencia y costos con Spring Boot Actuator / Micrometer.

### 3. Implementation Scope
- Revisar y enlazar toda la doc `01`..`13`.
- Escribir un `README.md` con cómo levantar el proyecto (Ollama, BD, endpoints).
- Listar mejoras futuras priorizadas.

### 4. Expected Result
Un proyecto documentado de punta a punta, reproducible por otra persona.

### 5. How to Test
Seguir el `README.md` desde cero en un entorno limpio y verificar que se obtiene una recomendación RAG.

### 6. Documentation to Generate
`14-final-docs.md` — resumen final, README y backlog de mejoras.

---

## Resumen del flujo de aprendizaje

```
Hito 1  → entender la arquitectura
Hito 2  → tener datos (catálogo)
Hito 3  → exponer datos por REST
Hito 4  → hablar con Ollama
Hito 5  → darle rol al LLM
Hito 6  → ver el problema de meter todo en el prompt
Hito 7  → entender embeddings
Hito 8  → guardar embeddings
Hito 9  → buscar por semántica
Hito 10 → RAG (recuperar + generar)
Hito 11 → agregar filtros duros
Hito 12 → salida estructurada
Hito 13 → tests
Hito 14 → doc final y siguientes pasos
```

> **Principio rector**: cada hito agrega **una sola idea nueva**. Si un hito parece grande, partirlo en dos antes de avanzar.
