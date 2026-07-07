package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wines")
public class WineController {

	private final WineService wineService;
	private final WineEmbeddingService wineEmbeddingService;
	private final SemanticWineSearchService semanticWineSearchService;

	public WineController(
			WineService wineService,
			WineEmbeddingService wineEmbeddingService,
			SemanticWineSearchService semanticWineSearchService) {
		this.wineService = wineService;
		this.wineEmbeddingService = wineEmbeddingService;
		this.semanticWineSearchService = semanticWineSearchService;
	}

	@GetMapping
	public List<WineResponse> findAll() {
		return wineService.findAll().stream()
				.map(WineResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<WineResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(WineResponse.from(wineService.findById(id)));
	}

	@GetMapping("/{id}/embedding")
	public WineEmbeddingService.EmbeddingPreview previewEmbedding(@PathVariable Long id) {
		return wineEmbeddingService.previewEmbedding(id);
	}

	@PostMapping("/index")
	public WineEmbeddingService.IndexingResult reindex() {
		return wineEmbeddingService.reindexAll();
	}

	@GetMapping("/search")
	public List<WineSearchResult> search(
			@RequestParam String q,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) Boolean stockAvailable,
			@RequestParam(required = false) String foodCategory,
			@RequestParam(required = false) Integer topK) {
		WineSearchCriteria criteria = new WineSearchCriteria(maxPrice, type, stockAvailable, foodCategory, topK);
		return semanticWineSearchService.search(q, criteria);
	}
}
