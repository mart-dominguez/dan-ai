package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class WineVectorStore {

	private final EmbeddingModel embeddingModel;
	private final WineDocumentMapper wineDocumentMapper;
	private final Map<Long, IndexedWineDocument> documents = new LinkedHashMap<>();

	public WineVectorStore(EmbeddingModel embeddingModel, WineDocumentMapper wineDocumentMapper) {
		this.embeddingModel = embeddingModel;
		this.wineDocumentMapper = wineDocumentMapper;
	}

	public synchronized int index(List<Wine> wines) {
		documents.clear();
		for (Wine wine : wines) {
			String documentText = wineDocumentMapper.toDocumentText(wine);
			float[] embedding = embeddingModel.embed(documentText);
			documents.put(wine.getId(), new IndexedWineDocument(wine.getId(), documentText,
					wineDocumentMapper.toMetadata(wine), embedding));
		}
		return documents.size();
	}

	public int size() {
		return documents.size();
	}

	public List<IndexedWineDocument> search(String query, WineSearchCriteria criteria) {
		if (documents.isEmpty()) {
			return List.of();
		}

		float[] queryEmbedding = embeddingModel.embed(query);
		return documents.values().stream()
				.filter(document -> matchesCriteria(document, criteria))
				.map(document -> document.withScore(cosineSimilarity(queryEmbedding, document.embedding())))
				.sorted(Comparator.comparing(IndexedWineDocument::score).reversed())
				.limit(criteria.resolvedTopK())
				.toList();
	}

	private boolean matchesCriteria(IndexedWineDocument document, WineSearchCriteria criteria) {
		if (criteria == null) {
			return true;
		}

		if (criteria.maxPrice() != null) {
			BigDecimal price = (BigDecimal) document.metadata().get("price");
			if (price.compareTo(criteria.maxPrice()) > 0) {
				return false;
			}
		}

		if (hasText(criteria.type())) {
			String type = String.valueOf(document.metadata().get("type"));
			if (!type.equalsIgnoreCase(criteria.type())) {
				return false;
			}
		}

		if (criteria.stockAvailable() != null) {
			boolean stockAvailable = (boolean) document.metadata().get("stockAvailable");
			if (stockAvailable != criteria.stockAvailable()) {
				return false;
			}
		}

		if (hasText(criteria.foodCategory())) {
			String pairings = String.valueOf(document.metadata().get("foodPairings")).toLowerCase(Locale.ROOT);
			if (!pairings.contains(criteria.foodCategory().toLowerCase(Locale.ROOT))) {
				return false;
			}
		}

		return true;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private double cosineSimilarity(float[] left, float[] right) {
		double dot = 0.0;
		double leftNorm = 0.0;
		double rightNorm = 0.0;
		int size = Math.min(left.length, right.length);
		for (int i = 0; i < size; i++) {
			dot += left[i] * right[i];
			leftNorm += left[i] * left[i];
			rightNorm += right[i] * right[i];
		}
		if (leftNorm == 0.0 || rightNorm == 0.0) {
			return 0.0;
		}
		return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
	}

	public record IndexedWineDocument(
			Long wineId,
			String text,
			Map<String, Object> metadata,
			float[] embedding,
			double score) {

		public IndexedWineDocument(Long wineId, String text, Map<String, Object> metadata, float[] embedding) {
			this(wineId, text, metadata, embedding, 0.0);
		}

		public IndexedWineDocument withScore(double score) {
			return new IndexedWineDocument(wineId, text, metadata, embedding, score);
		}
	}
}
