### Configurar Ollama

Incorporar la dependencia

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

La conexión con ollama se configura con propiedades prefijadas por `spring.ai.ollama`. Las principales propiedades a configurar son

| Property | Description | Default |
| --- | --- | --- |
| `spring.ai.ollama.init.pull-model-strategy` | Whether to pull models at startup-time and how. | `never` |
| `spring.ai.ollama.init.timeout` | How long to wait for a model to be pulled. | `5m` |
| `spring.ai.ollama.init.max-retries` | Maximum number of retries for the model pull operation. | `0` |
| `spring.ai.ollama.init.chat.include` | Include this type of models in the initialization task. | `true` |
| `spring.ai.ollama.init.chat.additional-models` | Additional models to initialize besides the ones configured via default properties. | `[]` |

https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html

