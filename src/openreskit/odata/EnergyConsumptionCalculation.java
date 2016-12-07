package openreskit.odata;

import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

/**
 * Funktion für die Berechnung der Emissionen/des Verbrauchs abhängig von der
 * Category des erstellten Footprints (Water/Carbon).
 */
public class EnergyConsumptionCalculation extends ADecorator {
	Double calculatedValue;
    public EnergyConsumptionCalculation(ICalculation iCalculation, DatabaseHelper helper, FootprintPosition footprintPosition) {
        super(iCalculation);
        Dao<EnergyConsumption, UUID> energyConsumptionDao = null;		
        try {
			energyConsumptionDao = helper.getEnergyConsumptionDao();
		} catch (SQLException e) {				
			e.printStackTrace();
		}
		for (EnergyConsumption energyConsumption : energyConsumptionDao)
		{
			if (energyConsumption.getFootprintPosition().getId().equalsIgnoreCase(footprintPosition.getId()))
			{					
		    String calculationCategory = energyConsumption.getCalculationCategory();
	        if (calculationCategory.equals("Carbon"))
				{
			       switch (energyConsumption.getEnergySource()) {
				       case (0): 
				    	   	//Heizöl
							calculatedValue = energyConsumption.getConsumption() * 897;
				       		break;
				       case (1): 
				    	   	//Erdgas
				    	   	calculatedValue = energyConsumption.getConsumption() * 428;
				       		break;
				       case (2): 
				    	   	//Steinkohle
				    	   	calculatedValue = energyConsumption.getConsumption() * 949;
				       		break;
				       		//Braunkohle
				       case (3): 
				    	   	calculatedValue = energyConsumption.getConsumption() * 1153;
				       		break;
				       		//Strommix
				       case (4): 
				    	   	calculatedValue = energyConsumption.getConsumption() * 600;
				       		break;
				       		//Ökostrom
				       case (5): 
				    	   	calculatedValue = energyConsumption.getConsumption() * 40;
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


