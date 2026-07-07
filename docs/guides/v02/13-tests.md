# 13 - Tests automatizados

Agrega tests que validen cada capa: repositorio, servicio, búsqueda semántica y endpoint RAG.

## Objetivo

Agregar tests que validen cada capa: repositorio, servicio, búsqueda semántica y endpoint RAG.

## Teoría

- **Test de capas**: unitarios para servicios, de integración para repositorio y vector store.
- **`@SpringBootTest`, `@DataJpaTest`, `MockMvc`**: herramientas de test de Spring Boot.
- **Mock del LLM**: en tests de unitarios no depender de Ollama; mockear `ChatClient` y `EmbeddingModel`.
- **Tests de integración con Ollama**: opcionales, marcarlos para correr solo cuando Ollama esté disponible.
- **Determinismo**: los LLM no son deterministas; los tests deben validar formato/esquema o usar respuestas simuladas.

## Alcance de implementación

- Tests de `WineRepository`.
- Tests de `WineService` con `@MockBean` del LLM.
- Test del endpoint con `MockMvc` mockeando `ChatClient`.
- (Opcional) test de integración con Ollama real, etiquetado para ejecución manual.

## Test de repositorio

```java
@DataJpaTest
class WineRepositoryTest {
    @Autowired WineRepository repository;

    @Test
    void findByPriceLessThanEqualReturnsOnlyCheapWines() {
        // arrange + act + assert
    }
}
```

## Test de controlador con MockMvc y mocks

```java
@WebMvcTest(WineController.class)
class WineControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean WineService wineService;

    @Test
    void getWinesReturnsJson() throws Exception {
        mockMvc.perform(get("/api/wines"))
               .andExpect(status().isOk());
    }
}
```

Se mockean `ChatClient` y `EmbeddingModel` con `@MockBean` para no invocar Ollama.

## Estrategia

- Validar **comportamiento y estructura**, no el wording exacto del LLM.
- Para respuestas estructuradas, validar que el JSON cumple el esquema y que `wineId` existe.
- Los tests de integración con Ollama real se marcan con una anotación/perfil para ejecución manual.

## Resultado esperado

`./mvnw test` pasa y cubre las capas principales sin requerir Ollama en CI.

## Verificación manual

```bash
./mvnw test
```

Revisar el reporte generado y confirmar que:

- Repositorio: pasan tests de filtrado y carga de datos.
- Servicio: pasan tests con mocks del LLM.
- Controller: pasan tests de `MockMvc` sin levantar Ollama.
- Integración con Ollama: opcional, no corre por defecto.