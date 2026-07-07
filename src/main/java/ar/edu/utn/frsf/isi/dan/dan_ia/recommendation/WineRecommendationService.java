package ar.edu.utn.frsf.isi.dan.dan_ia.recommendation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.isi.dan.dan_ia.wine.SemanticWineSearchService;
import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineDocumentMapper;
import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineResponse;
import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineSearchCriteria;
import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineSearchResult;
import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineService;

@Service
public class WineRecommendationService {

	private final ChatClient chatClient;
	private final WineService wineService;
	private final WineDocumentMapper wineDocumentMapper;
	private final SemanticWineSearchService semanticWineSearchService;

	public WineRecommendationService(
			ChatClient.Builder chatClientBuilder,
			WineService wineService,
			WineDocumentMapper wineDocumentMapper,
			SemanticWineSearchService semanticWineSearchService) {
		this.chatClient = chatClientBuilder
				.defaultSystem("""
						You are a concise sommelier assistant for a wine catalog.
						Recommend only wines that appear in the provided catalog context.
						If the context is empty or insufficient, say that no catalog wine can be verified.
						""")
				.defaultOptions(OllamaChatOptions.builder()
						.temperature(0.2)
						.numPredict(600)
						.numCtx(8192)
						.disableThinking())
				.build();
		this.wineService = wineService;
		this.wineDocumentMapper = wineDocumentMapper;
		this.semanticWineSearchService = semanticWineSearchService;
	}

	public RecommendationResponse recommendWithoutRag(RecommendationRequest request) {
		List<WineResponse> wines = wineService.findAll().stream()
				.map(WineResponse::from)
				.toList();
		String context = wineService.findAll().stream()
				.map(wineDocumentMapper::toDocumentText)
				.reduce("", (left, right) -> left + "\n---\n" + right);
		String answer = askModel(request.message(), context);
		return new RecommendationResponse(request.message(), answer,
				wines.stream().map(wine -> RecommendedWine.from(wine, 0.0)).toList());
	}

	public RecommendationResponse recommendWithRag(RecommendationRequest request) {
		WineSearchCriteria criteria = new WineSearchCriteria(
				request.maxPrice(),
				request.type(),
				request.stockAvailable(),
				request.foodCategory(),
				request.topK());
		List<WineSearchResult> matches = semanticWineSearchService.search(request.message(), criteria);
		String context = matches.stream()
				.map(WineSearchResult::matchedDocument)
				.reduce("", (left, right) -> left + "\n---\n" + right);
		String answer = askModel(request.message(), context);
		List<RecommendedWine> structuredWines = matches.stream()
				.map(match -> RecommendedWine.from(match.wine(), match.score()))
				.toList();
		return new RecommendationResponse(request.message(), answer, structuredWines);
	}

	private String askModel(String question, String context) {
		return chatClient.prompt()
				.user(userPrompt(question, context))
				.call()
				.content();
	}

	private String userPrompt(String question, String context) {
		return """
				Catalog context:
				%s

				User question:
				%s

				Answer with:
				- the recommended wine name
				- the reason
				- the food pairing logic
				- a short note if a hard constraint cannot be verified
				""".formatted(context.isBlank() ? "No catalog context was retrieved." : context, question);
	}
}
