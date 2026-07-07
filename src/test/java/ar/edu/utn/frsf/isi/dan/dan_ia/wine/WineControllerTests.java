package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WineControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsWines() throws Exception {
		mockMvc.perform(get("/api/wines"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Malbec Reserva"));
	}

	@Test
	void findsWineById() throws Exception {
		mockMvc.perform(get("/api/wines/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Malbec Reserva"))
				.andExpect(jsonPath("$.stockAvailable").value(true));
	}
}
