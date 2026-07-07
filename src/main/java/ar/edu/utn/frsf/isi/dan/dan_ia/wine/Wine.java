package ar.edu.utn.frsf.isi.dan.dan_ia.wine;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wines")
public class Wine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private String grapeVariety;

	@Column(nullable = false)
	private String region;

	@Column(nullable = false)
	private String country;

	@Column(name = "vintage_year")
	private Integer year;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(length = 1000)
	private String tasteNotes;

	private String body;

	private String acidity;

	private String sweetness;

	@Column(length = 1000)
	private String foodPairings;

	@Column(length = 2000)
	private String description;

	private String label;

	@Column(nullable = false)
	private boolean stockAvailable;

	protected Wine() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getGrapeVariety() {
		return grapeVariety;
	}

	public String getRegion() {
		return region;
	}

	public String getCountry() {
		return country;
	}

	public Integer getYear() {
		return year;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getTasteNotes() {
		return tasteNotes;
	}

	public String getBody() {
		return body;
	}

	public String getAcidity() {
		return acidity;
	}

	public String getSweetness() {
		return sweetness;
	}

	public String getFoodPairings() {
		return foodPairings;
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}

	public boolean isStockAvailable() {
		return stockAvailable;
	}
}
