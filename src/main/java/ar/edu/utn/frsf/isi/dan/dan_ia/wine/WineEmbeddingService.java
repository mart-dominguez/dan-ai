package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class WineEmbeddingService {

	private final EmbeddingModel embeddingModel;
	private final WineService wineService;
	private final WineDocumentMapper wineDocumentMapper;
	private final WineVectorStore wineVectorStore;

	public WineEmbeddingService(
			EmbeddingModel embeddingModel,
			WineService wineService,
			WineDocumentMapper wineDocumentMapper,
			WineVectorStore wineVectorStore) {
		this.embeddingModel = embeddingModel;
		this.wineService = wineService;
		this.wineDocumentMapper = wineDocumentMapper;
		this.wineVectorStore = wineVectorStore;
	}

	public EmbeddingPreview previewEmbedding(Long wineId) {
		Wine wine = wineService.findById(wineId);
		String document = wineDocumentMapper.toDocumentText(wine);
		float[] embedding = embeddingModel.embed(document);
		return new EmbeddingPreview(wine.getId(), document, embedding.length);
	}

	public IndexingResult reindexAll() {
		int indexedDocuments = wineVectorStore.index(wineService.findAll());
		return new IndexingResult(indexedDocuments);
	}

	public record EmbeddingPreview(Long wineId, String document, int dimensions) {
	}

	public record IndexingResult(int indexedDocuments) {
	}
}
