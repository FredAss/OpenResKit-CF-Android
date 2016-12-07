package openreskit.odata;

import com.j256.ormlite.field.DatabaseField;
/**
 * Klasse für die Daten einer Distanz. 
 */
public class Distance {
	@DatabaseField (id = true)
    private String id;
	@DatabaseField 
    private Double distance;
   /**
    * Liefert die Id einer Distanz.
    * @return id
    */
	public String getId() {
		return id;
	}
   /**
    * Speichert die Id einer Distanz.
    * @param id
    */
	public void setId(String id) {
		this.id = id;
	}
   /**
    * Liefert eine Distanz.
    * @return distance
    */
	public Double getDistance() {
		return distance;
	}
   /**
    * Speichert eine Distanz.
    * @param distance
    */
	public void setDistance(Double distance) {
		this.distance = distance;
	}
}
