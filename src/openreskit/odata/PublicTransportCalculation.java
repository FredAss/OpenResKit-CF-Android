package openreskit.odata;

import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

/**
 * Funktion für die Berechnung der Emissionen/des Verbrauchs abhängig von der
 * Category des erstellten Footprints (Water/Carbon).
 */
public class PublicTransportCalculation extends ADecorator {
	Double calculatedValue;    
    public PublicTransportCalculation(ICalculation iCalculation, DatabaseHelper helper, FootprintPosition footprintPosition) {
        super(iCalculation);
        Dao<PublicTransport, UUID> publicTransportDao = null;		
        try {
			publicTransportDao = helper.getPublicTransportDao();
		} catch (SQLException e) {				
			e.printStackTrace();
		}
		for (PublicTransport publicTransport : publicTransportDao)
		{
			if (publicTransport.getFootprintPosition().getId().equalsIgnoreCase(footprintPosition.getId()))
			{					
		    String calculationCategory = publicTransport.getCalculationCategory();
	        if (calculationCategory.equals("Carbon"))
				{
			       switch (publicTransport.getTransportType()) {
				       case (0): 
				    	   	//Fernzug
							calculatedValue = publicTransport.getDistance() * 17;
				       		break;
				       case (1): 
				    	   	//Regionalzug
				    	   	calculatedValue = publicTransport.getDistance() * 67;
				       		break;
				       case (2): 
				    	   	//Metro
				    	   	calculatedValue = publicTransport.getDistance() * 82;
				       		break;
				       		//Linienbus
				       case (3): 
				    	   	calculatedValue = publicTransport.getDistance() * 35;
				       		break;
				       		//Reisebus
				       case (4): 
				    	   	calculatedValue = publicTransport.getDistance() * 136;
				       		break;
				       		//Straßenbahn
				       case (5): 
				    	   	calculatedValue = publicTransport.getDistance() * 77;
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

