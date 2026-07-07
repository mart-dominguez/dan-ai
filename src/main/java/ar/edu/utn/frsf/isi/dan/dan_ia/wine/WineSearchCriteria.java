package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.math.BigDecimal;

public record WineSearchCriteria(
		BigDecimal maxPrice,
		String type,
		Boolean stockAvailable,
		String foodCategory,
		Integer topK) {

	public int resolvedTopK() {
		if (topK == null || topK < 1) {
			return 4;
		}
		return Math.min(topK, 10);
	}
}
