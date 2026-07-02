package ar.edu.utn.frsf.isi.dan.dan_ia.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatClient chatClient;

	public ChatController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder
				.defaultSystem("You are a helpful assistant. Answer in simple words and keep the response concise.")
				.defaultOptions(OllamaChatOptions.builder()
						.temperature(0.3)
						.numPredict(512)
						.numCtx(8192)
						.disableThinking())
				.build();
	}

	@PostMapping
	public ChatResponse chat(@RequestBody ChatRequest request) {
		String answer = this.chatClient.prompt()
				.user(request.message())
				.call()
				.content();

		return new ChatResponse(answer);
	}

	@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> chatStream(@RequestBody ChatRequest request) {
		return this.chatClient.prompt()
				.user(request.message())
				.stream()
				.content();
	}

	public record ChatRequest(String message) {
	}

	public record ChatResponse(String answer) {
	}
}