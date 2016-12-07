package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;

/**
 * Klasse für die Daten eines Energieverbrauchs. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyConsumption implements ICalculation{
	@DatabaseField (id = true)
    private String id;
	@JsonProperty("Consumption")
	@DatabaseField
	private double consumption;
	@JsonProperty("m_EnergySource")
	@DatabaseField
	private int energySource;	
	@DatabaseField(foreign = true)
	private FootprintPosition footprintPosition;
	@DatabaseField
	String calculationCategory;
	@JsonProperty("Calculation")
	@DatabaseField 
    private Double emission;

    	public EnergyConsumption() {
        super();
    }
	/**
	 * Liefert die Kalkulationskategorie für einen Energieverbrauch.
	 * @return calculationCategory
	 */
	public String getCalculationCategory() {
		return calculationCategory;
	}
	/**
	 * Speichert die Kalkulationskategorie für einen Energieverbrauch.
	 * @param calculationCategory
	 */
	public void setCalculationCategory(String calculationCategory) {
		this.calculationCategory = calculationCategory;
	}
	/**
	 * Liefert die ID als Primärschlüssel für einen Energieverbrauch.
	 * @return id
	 */
    public String getId() {
		return id;
	}
	/**
	 * Speichert die ID als Primärschlüssel für einen Energieverbrauch.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * Liefert die FootprintPosition eines Energieverbrauchs.
	 * @return footprintPosition
	 */
	public FootprintPosition getFootprintPosition() {
		return footprintPosition;
	}
	/**
	 * Speichert die FootprintPosition eines Energieverbrauchs.
	 * @param footprintPosition
	 */
	public void setFootprintPosition(FootprintPosition footprintPosition) {
		this.footprintPosition = footprintPosition;
	}
	/**
	 * Liefert die Emissionen eines Energieverbrauchs.
	 * @return emission
	 */
	public Double getEmission() {
		return emission;
	}
	/**
	 * Speichert die Emissionen eines Energieverbrauchs.
	 * @param emission
	 */
	public void setEmission(Double emission) {
		this.emission = emission;
	}
	/**
	 * Liefert den Verbrauch von Treibstoff für einen Energieverbrauch.
	 * @return consumption
	 */
	public double getConsumption() {
		return consumption;
	}
	/**
	 * Speichert den Verbrauch von Treibstoff für einen Energieverbrauch.
	 * @param consumption
	 */
	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}
	/**
	 * Liefert die Energiequelle für einen Energieverbrauch.
	 * @return energySource
	 */
	public int getEnergySource() {
		return energySource;
	}
	/**
	 * Speichert die Energiequelle für einen Energieverbrauch.
	 * @param energySource
	 */
	public void setEnergySource(int energySource) {
		this.energySource = energySource;
	}
}
