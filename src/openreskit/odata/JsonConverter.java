package openreskit.odata;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class JsonConverter {	
	/**
	 * Funktion zum Umwandeln der in der SQLite DB gespeicherten Objekte in JSON Arrays.
	 * Dazu werden zunächste die Footprints umgewandelt und anschließend die dazugehörenden Positionen
	 * je nach Typ ebenfalls umgewandelt und in das JSON Objekt des zugehörigen Footprints integriert.
	 * @param footprint
	 * @param helper
	 * @return footprintObject
	 */
	@SuppressWarnings("static-access")
	public static JSONObject convertFootprintToJson(Footprint footprint, Context ctx)
	{
		Dao<Flight, UUID> flightDao;
		Dao<Car, UUID> carDao;
		Dao<EnergyConsumption, UUID> energyConsumptionDao;
		Dao<PublicTransport, UUID> publicTransportDao;
		Dao<Employee, UUID> employeeDao;
		JSONArray footprintPositionData = new JSONArray();
		JSONObject footprintObject = null;
		JSONObject footprintPositionObject = null;
		LocalDateTime date = null;
		DatabaseHelper helper;
		helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
		
//		LocalDateTime localDateTimeformat = new LocalDateTime();
//		@SuppressWarnings("static-access")
//		LocalDateTime balanceYear = localDateTimeformat.parse(footprint.getBalanceYear()); 
		
		footprintObject = new JSONObject();
		 try {
			footprintObject.put("Id", footprint.getServerId());
			footprintObject.put("Name", footprint.getName());
			footprintObject.put("Description", footprint.getDescription());
			footprintObject.put("SiteLocation", footprint.getSiteLocation());
			footprintObject.put("Employees", footprint.getEmployees());
			footprintObject.put("Calculation", footprint.getCalculation());		
			
			for (FootprintPosition footprintPosition : footprint.getFootprintPositions())
			{
				footprintPositionObject = new JSONObject();
				LocalDateTime dateTimeFormat = new LocalDateTime();

				if (footprintPosition.getDate() != null) {
					date = dateTimeFormat.parse(footprintPosition.getDate());
				}
				footprintPositionObject.put("odata.type", footprintPosition.getPositionType());
				footprintPositionObject.put("Id", footprintPosition.getServerId());
				if (date != null)
				{
					footprintPositionObject.put("Start", footprintPosition.getDate());
				}
				else {
					footprintPositionObject.put("Start", new DateTime());
				}
				if (date != null)
				{
					footprintPositionObject.put("Finish", footprintPosition.getDate());
				}
				else {
					footprintPositionObject.put("Finish", new DateTime());
				}
				footprintPositionObject.put("Description", footprintPosition.getDescription());
				footprintPositionObject.put("IconId", footprintPosition.getIconId());
				footprintPositionObject.put("Name", footprintPosition.getName());
				footprintPositionObject.put("Tag", footprintPosition.getCarbonFootprintCategoryId());
				JSONObject employeeObject = new JSONObject();
				employeeDao = helper.getEmployeeDao();

				for (Employee employee : employeeDao) {
					if (employee.getId().equals(footprintPosition.getResponsibleSubject().getId())) {
						employeeObject.put("Id", employee.getId());
						employeeObject.put("odata.type", "OpenResKit.DomainModel.Employee");
						employeeObject.put("LastName", employee.getLastName());
						employeeObject.put("FirstName", employee.getFirstName());
						employeeObject.put("Number", employee.getNumber());
						employeeObject.put("Name", employee.getName());
						employeeObject.put("Discriminator", employee.getDiscriminator());
					}
				}			 
				footprintPositionObject.put("ResponsibleSubject", employeeObject);
				String positionType = footprintPosition.getPositionType();
				if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Flight") || positionType.equalsIgnoreCase("OpenResKit.DomainModel.AirportBasedFlight"))
				{
					flightDao = helper.getFlightsDao();
					for (Flight flight : flightDao)
					{
						if (flight.getFootprintPosition().getId().equals(footprintPosition.getId()))
						{
							footprintPositionObject.put("Calculation", flight.getEmission());
							footprintPositionObject.put("RadiativeForcing", flight.getRadiativeForcing());
							footprintPositionObject.put("Kilometrage", flight.getDistance());
							footprintPositionObject.put("m_FlightType", flight.getmFlighType());								
						}
					}
				}
	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Car"))
	        	{
					carDao = helper.getCarDao();
					for (Car car : carDao)
					{
						if (car.getFootprintPosition().getId().equals(footprintPosition.getId()))
						{
							footprintPositionObject.put("Calculation", car.getEmission());
							footprintPositionObject.put("Consumption", car.getConsumption());
							footprintPositionObject.put("CarbonProduction", car.getCarbonProduction());
							footprintPositionObject.put("Kilometrage", car.getDistance());
							footprintPositionObject.put("m_Fuel", car.getFuel());								
						}
					}
				if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.FullyQualifiedCar"))
					carDao = helper.getCarDao();
					for (Car car : carDao)
					{
						if (car.getFootprintPosition().getId().equals(footprintPosition.getId()))
						{
							footprintPositionObject.put("Calculation", car.getEmission());
							footprintPositionObject.put("Consumption", car.getConsumption());
							footprintPositionObject.put("CarbonProduction", car.getCarbonProduction());
							footprintPositionObject.put("Kilometrage", car.getDistance());
							footprintPositionObject.put("m_Fuel", car.getFuel());
							footprintPositionObject.put("Manufacturer", car.getManufacturer());
							footprintPositionObject.put("Model", car.getModel());
							footprintPositionObject.put("CarDescription", car.getCarDescription());
						}
					}		
	        	}
	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.PublicTransport"))
	        	{
					publicTransportDao = helper.getPublicTransportDao();
					for (PublicTransport publicTransport : publicTransportDao)
					{
						if (publicTransport.getFootprintPosition().getId().equals(footprintPosition.getId()))
						{
							footprintPositionObject.put("Calculation", publicTransport.getEmission());
							footprintPositionObject.put("Kilometrage", publicTransport.getDistance());
							footprintPositionObject.put("m_TransportType", publicTransport.getTransportType());								
						}
					}
	        	}
	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.EnergyConsumption"))
	        	{
					energyConsumptionDao = helper.getEnergyConsumptionDao();
					for (EnergyConsumption energyConsumption : energyConsumptionDao)
					{
						if (energyConsumption.getFootprintPosition().getId().equals(footprintPosition.getId()))
						{
							footprintPositionObject.put("Calculation", energyConsumption.getEmission());
							footprintPositionObject.put("Consumption", energyConsumption.getConsumption());
							footprintPositionObject.put("m_EnergySource", energyConsumption.getEnergySource());								
						}
					}
	        	}					
				footprintPositionData.put(footprintPositionObject);
			}
			footprintObject.put("Positions",(Object)footprintPositionData);
		
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}			

		return footprintObject;
	}
	public static JSONArray convertPositionToJson(List<FootprintPosition> footprintPositions)
	{
		JSONArray footprintPositionsData = new JSONArray();
		
		return footprintPositionsData;
	}
}
