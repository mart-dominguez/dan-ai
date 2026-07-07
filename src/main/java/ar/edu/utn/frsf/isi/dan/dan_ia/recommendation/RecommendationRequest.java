package ar.edu.utn.frsf.isi.dan.dan_ia.recommendation;

import java.math.BigDecimal;

public record RecommendationRequest(
		String message,
		BigDecimal maxPrice,
		String type,
		Boolean stockAvailable,
		String foodCategory,
		Integer topK) {
}
