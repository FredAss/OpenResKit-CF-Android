package openreskit.odata;

import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

/**
 * Funktion für die Berechnung der Emissionen/des Verbrauchs abhängig von der
 * Category des erstellten Footprints (Water/Carbon).
 */
public class CarCalculation extends ADecorator {
	Double calculatedValue;    
    public CarCalculation(ICalculation iCalculation, DatabaseHelper helper, FootprintPosition footprintPosition) {
        super(iCalculation);
        Dao<Car, UUID> carDao = null;		
        try {
			carDao = helper.getCarDao();
		} catch (SQLException e) {				
			e.printStackTrace();
		}
		for (Car car : carDao)
		{
			if (car.getFootprintPosition().getId().equalsIgnoreCase(footprintPosition.getId()))
			{					
		    String calculationCategory = car.getCalculationCategory();
	        if (calculationCategory.equals("Carbon"))
				{
			       switch (car.getFuel()) {
				       case (0): 
				    	   	//Benzin
							calculatedValue = car.getDistance()/car.getConsumption() * 3142;
				       		break;
				       case (1): 
				    	   	//Diesel
				    	   	calculatedValue = car.getDistance()/car.getConsumption() * 2778;
				       		break;
			       }
				}	        		       
		       else if (calculationCategory.equals("Water"))
		       {
			       switch (car.getFuel()) {
				       case (0): 
				    	    //Benzin
							calculatedValue = car.getDistance() * 312;				    	   	
				       		break;
				       case (1): 
				    	   	//Diesel
				    	   	calculatedValue = car.getDistance() * 312;
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
