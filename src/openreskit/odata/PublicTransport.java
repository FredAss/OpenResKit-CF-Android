package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
/**
 * Klasse f�r die Daten einer Fahrt mit einem �ffentlichen Verkehrsmittel. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicTransport implements ICalculation{
	@DatabaseField (id = true)
    private String id;
	@JsonProperty("m_TransportType")
	@DatabaseField
	private int transportType;	
	@JsonProperty("Kilometrage")
	@DatabaseField 
    private double distance;
	@DatabaseField(foreign = true)
	private FootprintPosition footprintPosition;
	@DatabaseField
	String calculationCategory;
	@JsonProperty("Calculation")
	@DatabaseField 
    private Double emission;

    	public PublicTransport() {
        super();
    }

	/**
	 * Liefert die Distanz f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return distance
	 */
   public double getDistance() {
	      return distance;
	   }
	/**
	 * Speichert die Distanz f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param distance
	 */
   public void setDistance(double distance) {
	      this.distance = distance;
	   }
	/**
	 * Liefert die Berechnungskategorie f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return calculationCategory
	 */
	public String getCalculationCategory() {
		return calculationCategory;
	}
	/**
	 * Speichert die Berechnungskategorie f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param calculationCategory
	 */
	public void setCalculationCategory(String calculationCategory) {
		this.calculationCategory = calculationCategory;
	}
	/**
	 * Liefert die ID als Prim�rschl�ssel f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return id
	 */
    public String getId() {
		return id;
	}
	/**
	 * Speichert die ID als Prim�rschl�ssel f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * Liefert die Footprint Position f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return footprintPosition
	 */
	public FootprintPosition getFootprintPosition() {
		return footprintPosition;
	}
	/**
	 * Speichert die Footprint Position f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param footprintPosition
	 */
	public void setFootprintPosition(FootprintPosition footprintPosition) {
		this.footprintPosition = footprintPosition;
	}
	/**
	 * Liefert die Emissionen f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return emission
	 */
	public Double getEmission() {
		return emission;
	}
	/**
	 * Speichert die Emissionen f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param emission
	 */
	public void setEmission(Double emission) {
		this.emission = emission;
	}
	/**
	 * Liefert den Bef�rderungstyp f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @return transportType
	 */
	public int getTransportType() {
		return transportType;
	}
	/**
	 * Speichert den Bef�rderungstyp f�r die Fahrt mit einem �ffentlichen Verkehrsmittel.
	 * @param transportType
	 */
	public void setTransportType(int transportType) {
		this.transportType = transportType;
	}
}
