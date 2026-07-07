package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SemanticWineSearchService {

	private final WineVectorStore wineVectorStore;
	private final WineService wineService;

	public SemanticWineSearchService(WineVectorStore wineVectorStore, WineService wineService) {
		this.wineVectorStore = wineVectorStore;
		this.wineService = wineService;
	}

	public List<WineSearchResult> search(String query, WineSearchCriteria criteria) {
		List<WineVectorStore.IndexedWineDocument> matches = wineVectorStore.search(query, criteria);
		List<Long> ids = matches.stream().map(WineVectorStore.IndexedWineDocument::wineId).toList();
		Map<Long, Wine> winesById = wineService.findAllById(ids).stream()
				.collect(Collectors.toMap(Wine::getId, Function.identity()));

		return matches.stream()
				.map(match -> toSearchResult(match, winesById.get(match.wineId())))
				.filter(result -> result.wine() != null)
				.sorted(Comparator.comparing(WineSearchResult::score).reversed())
				.toList();
	}

	private WineSearchResult toSearchResult(WineVectorStore.IndexedWineDocument match, Wine wine) {
		if (wine == null) {
			return new WineSearchResult(null, match.score(), match.text());
		}
		return new WineSearchResult(WineResponse.from(wine), match.score(), match.text());
	}
}
