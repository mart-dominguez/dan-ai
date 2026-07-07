# 14 - Documentación final y mejoras futuras

Consolida la documentación del proyecto y enumera mejoras posibles para seguir aprendiendo.

## Objetivo

Consolidar la documentación del proyecto y enumerar mejoras posibles para seguir aprendiendo.

## Teoría

- **Buenas prácticas de RAG en producción**: reindexación, versionado de embeddings, evaluación de calidad.
- **Mejoras avanzadas**: re-ranking, hybrid search (semántica + keyword), multi-tenancy, streaming de respuestas, conversación con memoria.
- **Observabilidad**: trazas de prompts, latencia y costos con Spring Boot Actuator / Micrometer.

## Flujo completo logrado

1. Datos del catálogo en una BD relacional.
2. Endpoints REST para vinos.
3. Chat con Ollama a través de Spring AI.
4. Generación de documentos de vino.
5. Embeddings con Spring AI.
6. Búsqueda semántica (vector store).
7. Endpoint de recomendación con RAG.
8. Filtros estructurados.
9. Salida estructurada JSON.
10. Tests automatizados.

## Alcance de implementación

- Revisar y enlazar toda la doc `01`..`13`.
- Escribir un `README.md` con cómo levantar el proyecto (Ollama, BD, endpoints).
- Listar mejoras futuras priorizadas.

## Mejoras futuras

- Reemplazar el vector store en memoria por PostgreSQL + pgvector.
- Reindexar automáticamente cuando cambian los vinos.
- Parsear salida estructurada del LLM en su totalidad (no solo envoltorio).
- Re-ranking después de la recuperación vectorial.
- Hybrid search (semántica + keyword/SQL).
- Streaming de respuestas con `ChatClient` reactivo.
- Memoria de conversación (historial multi-turno).
- Test de evaluación de calidad de recomendaciones.
- Endpoints admin para gestión del catálogo.
- Observabilidad con Micrometer.

## Verificación manual end-to-end

```bash
ollama pull llama3
ollama pull nomic-embed-text
./mvnw spring-boot:run
curl -X POST http://localhost:8080/api/wines/index
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "message":"Recomendá un vino para pasta con salsa de tomate por menos de 20 dólares.",
    "maxPrice":20,
    "stockAvailable":true
  }'
```

## Resultado esperado

Un proyecto documentado de punta a punta, reproducible por otra persona.

## Verificación manual final

Seguir el `README.md` desde cero en un entorno limpio y verificar que se obtiene una recomendación RAG fundamentada en el catálogo.