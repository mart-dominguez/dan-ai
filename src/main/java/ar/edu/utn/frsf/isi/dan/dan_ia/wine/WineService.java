package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WineService {

	private final WineRepository wineRepository;

	public WineService(WineRepository wineRepository) {
		this.wineRepository = wineRepository;
	}

	public List<Wine> findAll() {
		return wineRepository.findAll();
	}

	public Wine findById(Long id) {
		return wineRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Wine not found: " + id));
	}

	public List<Wine> findAllById(List<Long> ids) {
		return wineRepository.findAllById(ids);
	}
}
