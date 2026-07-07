package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WineDocumentMapperTests {

	@Autowired
	private WineRepository wineRepository;

	@Autowired
	private WineDocumentMapper wineDocumentMapper;

	@Test
	void mapsWineToSemanticDocument() {
		Wine wine = wineRepository.findAll().getFirst();

		String document = wineDocumentMapper.toDocumentText(wine);

		assertThat(document)
				.contains("Name: Malbec Reserva.")
				.contains("Grape variety: Malbec.")
				.contains("Food pairings: grilled meat, barbecue, hard cheese.");
	}
}
