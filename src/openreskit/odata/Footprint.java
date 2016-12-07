package openreskit.odata;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
/**
 * Klasse für die Daten eines Footprints. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Footprint {
	@DatabaseField (id = true)
	String id;
	@DatabaseField 
	@JsonProperty("Employees")
	int employees;
	@DatabaseField 
	@JsonProperty("Name")
	String name;
	@DatabaseField 
	@JsonProperty("BalanceYear")
	String balanceYear;
	@DatabaseField 
	@JsonProperty("Description")
	String description;	
	@DatabaseField 
	@JsonProperty("Id")
	int serverId;
	@DatabaseField 
	@JsonProperty("SiteLocation")
	String siteLocation;
	@DatabaseField 
	@JsonProperty("Calculation")
	int calculation;	
	@ForeignCollectionField(eager = false)
	@JsonProperty("Positions")
	Collection<FootprintPosition> footprintPositions;
	@DatabaseField
    private int nr;
	@DatabaseField
	public String calculationCategory;

	public Footprint() {
        super();
    }

    public Footprint(String id) {
        this();
        this.id = id;
    }
    /**
     * Liefert die Anzahl der Mitarbeiter in einem Werk für einen Footprint
     * @return employees
     */
	public int getEmployees() {
		return employees;
	}
    /**
     * Speichert die Anzahl der Mitarbeiter in einem Werk für einen Footprint
     * @param employees
     */
	public void setEmployees(int employees) {
		this.employees = employees;
	}
    /**
     * Liefert den Namen für einen Footprint.
     * @return name
     */
	public String getName() {
		return name;
	}
    /**
     * Speichert den Namen für einen Footprint.
     * @param name
     */
	public void setName(String name) {
		this.name = name;
	}
    /**
     * Liefert das Bilanzjahr für einen Footprint.
     * @return balanceYear
     */
	public String getBalanceYear() {
		return balanceYear;
	}
	  /**
     * Speichert das Bilanzjahr für einen Footprint.
     * @param balanceYear
     */
	public void setBalanceYear(String balanceYear) {
		this.balanceYear = balanceYear;
	}
    /**
     * Liefert die Beschreibung für einen Footprint.
     * @return description
     */
	public String getDescription() {
		return description;
	}
	  /**
     * Speichert die Beschreibung für einen Footprint.
     * @param description
     */
	public void setDescription(String description) {
		this.description = description;
	}
    /**
     * Liefert die Id als Primärschlüssel für einen Footprint.
     * @return id
     */
	public String getId() {
		return id;
	}
    /**
     * Speichert die Id als Primärschlüssel für einen Footprint.
     * @param id
     */
	public void setId(String id) {
		this.id = id;
	}
    /**
     * Liefert die Id auf dem Server für einen Footprint.
     * @return serverId
     */
	public int getServerId() {
		return serverId;
	}
    /**
     * Speichert die Id auf dem Server für einen Footprint.
     * @param serverId
     */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
    /**
     * Liefert Footprint Positionen für einen Footprint.
     * @return footprintPositions
     */
	public Collection<FootprintPosition> getFootprintPositions() {
		return footprintPositions;
	}
    /**
     * Speichert Footprint Positionen für einen Footprint.
     * @param footprintPositions
     */
	public void setFootprintPositions(
			Collection<FootprintPosition> footprintPositions) {
		this.footprintPositions = footprintPositions;
	}
    /**
     * Liefert die Location eines Standortes für einen Footprint.
     * @return siteLocation
     */
	public String getSiteLocation() {
		return siteLocation;
	}
    /**
     * Speichert die Location eines Standortes für einen Footprint.
     * @param siteLocation
     */
	public void setSiteLocation(String siteLocation) {
		this.siteLocation = siteLocation;
	}
    /**
     * Liefert die gesamten Emissionen für einen Footprint.
     * @return calculation
     */
	public int getCalculation() {
		return calculation;
	}
    /**
     * Speichert die gesamten Emissionen für einen Footprint.
     * @param calculation
     */
	public void setCalculation(int calculation) {
		this.calculation = calculation;
	}
    /**
     * Speichert Nummer eines Footprints für die Listensortierung.
     * @return nr
     */
	public int getNr() {
		return nr;
	}
    /**
     * Speichert Nummer eines Footprints für die Listensortierung.
     * @param nr
     */
	public void setNr(int nr) {
		this.nr = nr;
	}	
    /**
     * Liefert Berechnungskategorie eines Footprints.
     * @return calculationCategory
     */
	public String getCalculationCategory() {
		return calculationCategory;
	}
    /**
     * Speichert Berechnungskategorie eines Footprints.
     * @param calculationCategory
     */
	public void setCalculationCategory(String calculationCategory) {
		this.calculationCategory = calculationCategory;
	}

}
	   
