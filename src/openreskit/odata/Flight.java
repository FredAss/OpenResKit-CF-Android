package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
/**
 * Klasse für die Daten eines Energieverbrauchs. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight implements ICalculation {
	@DatabaseField (id = true)
	public String id;
	@JsonProperty("Kilometrage")
	@DatabaseField 
	public double distance;
	@JsonProperty("Calculation")
	@DatabaseField 
    public Double emission;
	@DatabaseField(foreign = true)
    public FootprintPosition footprintPosition;
	@JsonProperty("RadiativeForcing")
	@DatabaseField 
	public Boolean radiativeForcing;
	@DatabaseField 
	public String calculationCategory;
	@JsonProperty("m_FlightType")
	@DatabaseField 
	public int mFlighType;

	public Flight() {
        super();
    }
	/**
	 * Liefert die Distanz für einen Flug.
	 * @return distance
	 */
   public double getDistance() {
	      return distance;
	   }
	/**
	 * Speichert die Distanz für einen Flug.
	 * @param distance
	 */
   public void setDistance(double distance) {
	      this.distance = distance;
	   }
	/**
	 * Liefert die Emissionen eines Flugs.
	 * @return emission
	 */
	public Double getEmission() {
		return emission;
	}
	/**
	 * Speichert die Emissionen eines Flugs.
	 * @param emission
	 */
	public void setEmission(Double emission) {
		this.emission = emission;
	}
	/**
	 * Liefert die Footprint Position eines Flugs.
	 * @return footprintPosition
	 */
	public FootprintPosition getFootprintPosition() {
		return footprintPosition;
	}
	/**
	 * Speichert die Footprint Position eines Flugs.
	 * @param footprintPosition
	 */
	public void setFootprintPosition(FootprintPosition footprintPosition) {
		this.footprintPosition = footprintPosition;
	}
	/**
	 * Liefert ob ein Flug unter Einfluß von radiative Forcing steht.
	 * @return radiativeForcing
	 */
	public Boolean getRadiativeForcing() {
		return radiativeForcing;
	}
	/**
	 * Speichert ob ein Flug unter Einfluß von radiative Forcing steht.
	 * @param radiativeForcing
	 */
	public void setRadiativeForcing(Boolean radiativeForcing) {
		this.radiativeForcing = radiativeForcing;
	}
	/**
	 * Liefert die Kalkulationskategorie für einen Flug.
	 * @return calculationCategory
	 */
	public String getCalculationCategory() {
		return calculationCategory;
	}
	/**
	 * Speichert die Kalkulationskategorie für einen Flug.
	 * @param calculationCategory
	 */
	public void setCalculationCategory(String calculationCategory) {
		this.calculationCategory = calculationCategory;
	}
	/**
	 * Liefert die ID als Primärschlüssel für einen Flug.
	 * @return id
	 */
    public String getId() {
		return id;
	}
	/**
	 * Speichert die ID als Primärschlüssel für einen Flug.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * Liefert den Typ der Strecke eines Flugs.
	 * @return mFlighType
	 */
	public int getmFlighType() {
		return mFlighType;
	}
	/**
	 * Speichert den Typ der Strecke eines Flugs.
	 * @param mFlighType
	 */
	public void setmFlighType(int mFlighType) {
		this.mFlighType = mFlighType;
	}
}
