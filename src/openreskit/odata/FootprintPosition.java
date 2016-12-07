package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
/**
 * Klasse für die Daten einer Footprint Position. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class FootprintPosition {
	@DatabaseField
	@JsonProperty("Description")
	public String description;
	@DatabaseField (id = true)
    public String id;
	@DatabaseField
	@JsonProperty("Start")
	public String date;
	@DatabaseField
	@JsonProperty("Name")
	public String name;
	@DatabaseField
	@JsonProperty("Id")
	public int serverId;
	@DatabaseField(foreign = true)
	public Footprint footprint;
	@DatabaseField
	@JsonProperty("CarbonFootprintCategoryId")
	public String carbonFootprintCategoryId;
	@DatabaseField
	@JsonProperty("odata.type")
	public String positionType;
	@DatabaseField
	@JsonProperty("IconId")
	public String iconId;
	@DatabaseField(foreign = true)
	public Employee responsibleSubject;
	
	public FootprintPosition() {
	        super();
	    }
	   
	    public FootprintPosition(String id) {
	        this();
	        this.id = id;
	    }
		/**
		 * Liefert die Beschreibung für eine Footprint Position.
		 * @return description
		 */
	    public String getDescription() {
			return description;
		}
		/**
		 * Speichert die Beschreibung für eine Footprint Position.
		 * @param description
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		/**
		 * Liefert die ID als Primärschlüssel für eine Footprint Position.
		 * @return id
		 */
		public String getId() {
			return id;
		}
		/**
		 * Speichert die ID als Primärschlüssel für eine Footprint Position.
		 * @param id
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * Liefert das Datum für eine Footprint Position.
		 * @return date
		 */
		public String getDate() {
			return date;
		}
		/**
		 * Speichert das Datum für eine Footprint Position.
		 * @param date
		 */
		public void setDate(String date) {
			this.date = date;
		}
		/**
		 * Liefert den Namen für eine Footprint Position.
		 * @return name
		 */
		public String getName() {
			return name;
		}
		/**
		 * Speichert den Namen für eine Footprint Position.
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * Liefert die Id auf dem Server für eine Footprint Position.
		 * @return serverId
		 */
		public int getServerId() {
			return serverId;
		}
		/**
		 * Speichert die Id auf dem Server für eine Footprint Position.
		 * @param serverId
		 */
		public void setServerId(int serverId) {
			this.serverId = serverId;
		}	
		/**
		 * Liefert den Footprint zu einer Footprint Position.
		 * @return footprint
		 */
		public Footprint getFootprint() {
			return footprint;
		}
		/**
		 * Speichert den Footprint zu einer  Footprint Position.
		 * @param footprint
		 */
		public void setFootprint(Footprint footprint) {
			this.footprint = footprint;
		}

		/**
		 * Liefert Id für die Carbon Footprint Kategorie auf dem Server für eine Footprint Position.
		 * @return carbonFootprintCategoryId
		 */
		public String getCarbonFootprintCategoryId() {
			return carbonFootprintCategoryId;
		}
		/**
		 * Speichert Id für die Carbon Footprint Kategorie auf dem Server für eine Footprint Position.
		 * @param carbonFootprintCategoryId
		 */
		public void setCarbonFootprintCategoryId(String carbonFootprintCategoryId) {
			this.carbonFootprintCategoryId = carbonFootprintCategoryId;
		}		
		/**
		 * Liefert den Typ der Position für eine Footprint Position.
		 * @return positionType
		 */
		public String getPositionType() {
			return positionType;
		}
		/**
		 * Speichert den Typ der Position für eine Footprint Position.
		 * @param positionType
		 */
		public void setPositionType(String positionType) {
			this.positionType = positionType;
		}
		/**
		 * Liefert das Icon für eine Footprint Position.
		 * @return iconId
		 */
		public String getIconId() {
			return iconId;
		}
		/**
		 * Speichert das Icon für eine Footprint Position.
		 * @param iconId
		 */
		public void setIconId(String iconId) {
			this.iconId = iconId;
		}
		
		public Employee getResponsibleSubject() {
			return responsibleSubject;
		}

		public void setResponsibleSubject(Employee responsibleSubject) {
			this.responsibleSubject = responsibleSubject;
		}

}
