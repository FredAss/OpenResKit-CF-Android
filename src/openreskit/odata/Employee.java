package openreskit.odata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;

public class Employee {

	@JsonProperty("Id")
	@DatabaseField (id = true)
    private String id;
	@JsonProperty("FirstName")
	@DatabaseField
	private String firstName;
	@JsonProperty("LastName")
	@DatabaseField
	private String lastName;	
	@JsonProperty("Number")
	@DatabaseField 
    private String number;
	@JsonProperty("Name")
	@DatabaseField
	private String name;
	@JsonProperty("Discriminator")
	@DatabaseField
	private String discriminator;
	@JsonProperty("odata.type")
	@DatabaseField
	private String odataType;
	
	public String getOdataType() {
		return odataType;
	}
	public void setOdataType(String odataType) {
		this.odataType = odataType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDiscriminator() {
		return discriminator;
	}
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}
}
