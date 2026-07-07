package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class WineDocumentMapper {

	public String toDocumentText(Wine wine) {
		return """
				Name: %s.
				Type: %s wine.
				Grape variety: %s.
				Region: %s, %s.
				Year: %s.
				Price: %s.
				Taste notes: %s.
				Body: %s.
				Acidity: %s.
				Sweetness: %s.
				Food pairings: %s.
				Description: %s.
				Stock available: %s.
				""".formatted(
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
				wine.isStockAvailable());
	}

	public Map<String, Object> toMetadata(Wine wine) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("wineId", wine.getId());
		metadata.put("type", wine.getType());
		metadata.put("price", wine.getPrice());
		metadata.put("country", wine.getCountry());
		metadata.put("region", wine.getRegion());
		metadata.put("stockAvailable", wine.isStockAvailable());
		metadata.put("foodPairings", wine.getFoodPairings());
		return metadata;
	}
}
