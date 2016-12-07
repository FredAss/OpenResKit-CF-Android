package openreskit.odata;

import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

/**
 * Funktion für die Berechnung der Emissionen/des Verbrauchs abhängig von der
 * Category des erstellten Footprints (Water/Carbon).
 */
public class FlightCalculation extends ADecorator {
	Double calculatedValue;    
    public FlightCalculation(ICalculation iCalculation, DatabaseHelper helper, FootprintPosition footprintPosition) {
        super(iCalculation);
        Dao<Flight, UUID> flightDao = null;		
        try {
			flightDao = helper.getFlightsDao();
		} catch (SQLException e) {				
			e.printStackTrace();
		}
		for (Flight flight : flightDao)
		{
			if (flight.getFootprintPosition().getId().equalsIgnoreCase(footprintPosition.getId()))
			{					
		    String calculationCategory = flight.getCalculationCategory();
	        if (calculationCategory.equals("Carbon"))
				{
			       switch (flight.getmFlighType()) {
				       case (0): 
				    	   	//Langstrecke
							calculatedValue = flight.getDistance() * 131.43;
				       		break;
				       case (1): 
				    	   	//Mittelstrecke
				    	   	calculatedValue = flight.getDistance() * 114.86;
				       		break;
				       case (2): 
				    	   	//Kurzstrecke
				    	   	calculatedValue = flight.getDistance() * 201.24;
				       		break;
			       }
			       if (flight.getRadiativeForcing())
		  			  {
			    	   	calculatedValue *= 1.9;
			  			flight.setRadiativeForcing(true);
		  			  }
				}	        		       
		       else if (calculationCategory.equals("Water"))
		       {
			       switch (flight.getmFlighType()) {
				       case (0): 
				    	    //Langstrecke
							calculatedValue = flight.getDistance() * 217.36;				    	   	
				       		break;
				       case (1): 
				    	   	//Mittelstrecke
				    	   	calculatedValue = flight.getDistance() * 236.6;
				       		break;
				       case (2): 
				    	   	//Kurzstrecke
							calculatedValue = flight.getDistance() * 374.92;
				       		break;
			       }
		       }
			}
		}
    }

	@Override
	public Double getEmission() {
		return calculatedValue;
	}

}
