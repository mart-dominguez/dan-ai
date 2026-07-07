package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WineRepositoryTests {

	@Autowired
	private WineRepository wineRepository;

	@Test
	void loadsSampleWineCatalog() {
		assertThat(wineRepository.findAll())
				.hasSize(6)
				.extracting(Wine::getName)
				.contains("Malbec Reserva", "Sauvignon Blanc Costa");
	}
}
