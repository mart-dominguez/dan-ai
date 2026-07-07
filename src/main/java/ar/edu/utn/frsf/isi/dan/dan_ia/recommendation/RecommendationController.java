package ar.edu.utn.frsf.isi.dan.dan_ia.recommendation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

	private final WineRecommendationService wineRecommendationService;

	public RecommendationController(WineRecommendationService wineRecommendationService) {
		this.wineRecommendationService = wineRecommendationService;
	}

	@PostMapping("/basic")
	public RecommendationResponse recommendWithoutRag(@RequestBody RecommendationRequest request) {
		return wineRecommendationService.recommendWithoutRag(request);
	}

	@PostMapping
	public RecommendationResponse recommendWithRag(@RequestBody RecommendationRequest request) {
		return wineRecommendationService.recommendWithRag(request);
	}
}
