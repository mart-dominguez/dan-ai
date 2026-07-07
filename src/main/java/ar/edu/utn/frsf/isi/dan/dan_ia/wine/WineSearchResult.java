package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

public record WineSearchResult(
		WineResponse wine,
		double score,
		String matchedDocument) {
}
