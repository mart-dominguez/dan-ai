# 07 - Concepto de embeddings y representación de documentos

Entender qué es un embedding y cómo representar cada vino como un documento de texto para embeberlo.

## Objetivo

Entender qué es un embedding y cómo representar cada vino como un documento de texto para embeberlo.

## Teoría

- **Embedding**: vector de números (ej. 1536 dimensiones) que captura el significado de un texto. Textos semánticamente parecidos → vectores cercanos.
- **`EmbeddingModel`**: interfaz de Spring AI que convierte texto → vector. Ollama puede proveer embeddings (`nomic-embed-text` u otro).
- **Documento**: unidad de contenido a embeber. Para un vino, se construye un texto rico combinando sus campos (nombre, tipo, uva, región, notas de cata, maridajes, descripción).
- **Calidad del documento**: más información semántica relevante → mejores recuperaciones.
- **¿Por qué no embeber la fila cruda?**: un texto natural ("Tinto Malbec de Mendoza, cuerpo medio, marida con carne asada...") produce embeddings más expresivos que una fila tabular.

## Documento sugerido por vino

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
```

El documento debe incluir los campos que ayuden a buscar de forma natural: tipo, uva, región, perfil de cata, maridajes y descripción.

## Alcance de implementación

- Método que, dado un `Wine`, arme un `String` descriptivo (el "documento").
- No se persiste todavía el embedding: solo se genera y se imprime para inspeccionar.
- Configurar el modelo de embeddings de Ollama en properties.

```properties
spring.ai.ollama.embedding.model=nomic-embed-text
```

## Modelo de embeddings vs modelo de chat

El modelo de embeddings es **distinto** del modelo de chat. El chat produce texto; el embedding produce un vector. La dimensión del vector depende del modelo de embeddings elegido.

## Resultado esperado

Para un vino de ejemplo se obtiene un vector (o al menos se valida que `EmbeddingModel` responde).

## Verificación manual

```bash
ollama pull nomic-embed-text
```

Llamar al `EmbeddingModel` con el documento de un vino e imprimir la cantidad de dimensiones del vector resultante (por ejemplo con un `CommandLineRunner` o un endpoint de inspección como `GET /api/wines/1/embedding`).