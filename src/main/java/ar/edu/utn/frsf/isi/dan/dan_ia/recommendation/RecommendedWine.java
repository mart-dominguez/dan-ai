package ar.edu.utn.frsf.isi.dan.dan_ia.recommendation;

import java.math.BigDecimal;

import ar.edu.utn.frsf.isi.dan.dan_ia.wine.WineResponse;

public record RecommendedWine(
		Long wineId,
		String name,
		String type,
		BigDecimal price,
		String reason,
		String pairingExplanation,
		double confidence) {

	public static RecommendedWine from(WineResponse wine, double score) {
		return new RecommendedWine(
				wine.id(),
				wine.name(),
				wine.type(),
				wine.price(),
				"Selected from the catalog because it matched the user's request.",
				wine.foodPairings(),
				Math.max(0.0, Math.min(1.0, score)));
	}
}
