package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.math.BigDecimal;

public record WineResponse(
		Long id,
		String name,
		String type,
		String grapeVariety,
		String region,
		String country,
		Integer year,
		BigDecimal price,
		String tasteNotes,
		String body,
		String acidity,
		String sweetness,
		String foodPairings,
		String description,
		String label,
		boolean stockAvailable) {

	public static WineResponse from(Wine wine) {
		return new WineResponse(
				wine.getId(),
				wine.getName(),
				wine.getType(),
				wine.getGrapeVariety(),
				wine.getRegion(),
				wine.getCountry(),
				wine.getYear(),
				wine.getPrice(),
				wine.getTasteNotes(),
				wine.getBody(),
				wine.getAcidity(),
				wine.getSweetness(),
				wine.getFoodPairings(),
				wine.getDescription(),
				wine.getLabel(),
				wine.isStockAvailable());
	}
}
