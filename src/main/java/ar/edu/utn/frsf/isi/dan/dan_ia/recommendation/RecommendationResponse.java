package ar.edu.utn.frsf.isi.dan.dan_ia.recommendation;

import java.util.List;

public record RecommendationResponse(
		String question,
		String answer,
		List<RecommendedWine> recommendedWines) {
}
