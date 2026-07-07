# 01 - Visión general y arquitectura

El proyecto es un asistente de recomendación de vinos construido con **Spring Boot + Spring AI + Ollama + Embeddings + RAG**.

La fuente de verdad es el catálogo relacional de vinos. Las capacidades de IA se agregan alrededor de ese catálogo, no en reemplazo de él.

## Objetivo

Comprender qué construiremos, por qué y cómo encajan las piezas (BD relacional, embeddings, vector store, Ollama, Spring AI, RAG) antes de escribir código.

## Arquitectura

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

## Idea clave

RAG significa *recuperar primero, luego generar*. El asistente debe recomendar vinos a partir del contexto recuperado del catálogo en lugar de inventar vinos desde la memoria del modelo.

## Pila tecnológica

- **Java 21**
- **Spring Boot 4.1.0** (web, actuator)
- **Spring AI 2.0.0** (`spring-ai-starter-model-ollama`, vector store, embeddings)
- **Ollama** en local (modelo sugerido: `llama3` o `qwen`)
- **Lombok** para reducir boilerplate
- **Maven** como herramienta de build
- BD relacional y vector store a definir en los hitos correspondientes

## Glosario

- **LLM (Large Language Model)**: modelo que genera texto a partir de un prompt. Por sí solo no conoce nuestros vinos.
- **Ollama**: herramienta que ejecuta LLMs en local, expone una API HTTP y se integra con Spring AI sin claves de nube.
- **Spring AI**: abstracción de Spring para chat, embeddings y vector stores con interfaces uniformes (`ChatClient`, `EmbeddingModel`, `VectorStore`).
- **Embedding**: vector de números que representa el significado de un texto. Textos parecidos → vectores cercanos.
- **Vector store**: base de datos optimizada para buscar por similitud vectorial (coseno, etc.).
- **RAG**: patrón que primero *recupera* datos relevantes (del vector store) y luego se los entrega al LLM como contexto.

## Resultado esperado

Existe un roadmap claro que guía el aprendizaje paso a paso (`learning-roadmap02.md`).

## Verificación manual

Revisar `docs/guides/v02/learning-roadmap02.md` y confirmar que cada hito agrega **una sola idea nueva** y contiene los 6 puntos (Goal, Theory, Scope, Result, Test, Doc).