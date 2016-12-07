package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;

/**
 * Klasse für die Daten einer Fahrt mit dem Auto. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Car implements ICalculation{
	@DatabaseField (id = true)
    private String id;
	@JsonProperty("Consumption")
	@DatabaseField
	private double consumption;
	@JsonProperty("m_Fuel")
	@DatabaseField
	private int fuel;	
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
	@JsonProperty("CarbonProduction")
	@DatabaseField 
	private String carbonProduction;
	@JsonProperty("Manufacturer")
	@DatabaseField 
	private String manufacturer;	
	@JsonProperty("Model")
	@DatabaseField 
	private String model;	
	@JsonProperty("CarDescription")
	@DatabaseField 
	private String carDescription;

    	public Car() {
        super();
    }
   /**
    * Liefert die Distanz einer Autofahrt.
    * @return distance
    */
   public double getDistance() {
	      return distance;
	   }
   /**
    * Speichert die Distanz einer Autofahrt.
    * @param distance
    */
   public void setDistance(double distance) {
	      this.distance = distance;
	   }
   /**
    * Liefert die Calculation Category der Footprint Position.
    * Notwendig für Decorator Pattern.
    * @return calculationCategory
    */
	public String getCalculationCategory() {
		return calculationCategory;
	}
   /**
    * Speichert die Calculation Category der Footprint Position.
    * Notwendig für Decorator Pattern.
    * @param calculationCategory
    */
	public void setCalculationCategory(String calculationCategory) {
		this.calculationCategory = calculationCategory;
	}
   /**
    * Liefert die ID als Primärschlüssel der Fahrt.   
    * @return id
    */
    public String getId() {
		return id;
	}
    /**
     * Speichert die ID als Primärschlüssel der Fahrt. 
     * @param id
     */
	public void setId(String id) {
		this.id = id;
	}
   /**
    * Liefert die FootprintPosition der Autofahrt.   
    * @return footprintPosition
    */
	public FootprintPosition getFootprintPosition() {
		return footprintPosition;
	}
   /**
    * Speichert die FootprintPosition der Autofahrt.   
    * @param footprintPosition
    */
	public void setFootprintPosition(FootprintPosition footprintPosition) {
		this.footprintPosition = footprintPosition;
	}
   /**
    * Liefert die Emissionen/Wasserverbrauch als Berechnungsergebnis für
    * die Autofahrt.   
    * @return emission
    */
	public Double getEmission() {
		return emission;
	}
   /**
    * Speichert die Emissionen/Wasserverbrauch als Berechnungsergebnis für
    * die Autofahrt.   
    * @param emission
    */
	public void setEmission(Double emission) {
		this.emission = emission;
	}
   /**
    * Liefert den Verbrauch des Fahrzeugs pro 100 km.
    * @return consumption
    */
	public double getConsumption() {
		return consumption;
	}
   /**
    * Speichert den Verbrauch des Fahrzeugs pro 100 km.
    * @param consumption
    */
	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}
   /**
    * Liefert den Kraftstofftyp des Fahrzeugs.
    * @return fuel
    */
	public int getFuel() {
		return fuel;
	}
   /**
    * Speichert den Kraftstofftyp des Fahrzeugs.
    * @param fuel
    */
	public void setFuel(int fuel) {
		this.fuel = fuel;
	}
   /**
    * Liefert die Treibhausbildung die vom Typ des Fahrzeugs abhängen kann.
    * @return carbonProduction
    */
    public String getCarbonProduction() {
		return carbonProduction;
	}
    /**
     * Speichert die Treibhausbildung die vom Typ des Fahrzeugs abhängen kann.
     * @param carbonProduction
     */
	public void setCarbonProduction(String carbonProduction) {
		this.carbonProduction = carbonProduction;
	}
   /**
    * Liefert den Herstellertyp des Fahrzeugs.
    * @return manufacturer
    */
	public String getManufacturer() {
		return manufacturer;
	}
   /**
    * Speichert den Herstellertyp des Fahrzeugs.
    * @param manufacturer
    */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
   /**
    * Liefert das Modell des Fahrzeugs.
    * @return model
    */
	public String getModel() {
		return model;
	}
   /**
    * Speichert das Modell des Fahrzeugs.
    * @param model
    */
	public void setModel(String model) {
		this.model = model;
	}
   /**
    * Liefert die Beschreibung für den Typ des Fahrzeugs.
    * @return carDescription
    */
	public String getCarDescription() {
		return carDescription;
	}
   /**
    * Speichert die Beschreibung für den Typ des Fahrzeugs.
    * @param carDescription
    */
	public void setCarDescription(String carDescription) {
		this.carDescription = carDescription;
	}

}
