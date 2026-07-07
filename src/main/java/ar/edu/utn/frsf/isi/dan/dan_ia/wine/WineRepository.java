package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WineRepository extends JpaRepository<Wine, Long> {

	List<Wine> findByPriceLessThanEqualAndStockAvailable(BigDecimal maxPrice, boolean stockAvailable);
}
