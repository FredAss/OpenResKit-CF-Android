package htw.bui;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;

import openreskit.odata.Car;
import openreskit.odata.DatabaseHelper;
import openreskit.odata.EnergyConsumption;
import openreskit.odata.Flight;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.PublicTransport;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Adapter für das Darstellen der Argumente (Datum, Treibhauspotential, etc.) 
 * einer Footprint-Position in die einzelnen View Elemente der Listenzeilen mit den Positionen.
 * Siehe:
 * http://code.google.com/p/myandroidwidgets/source/browse/trunk/Phonebook/src/com/abeanie/PhonebookAdapter.java
 */
public class PositionsAdapter extends BaseAdapter implements OnClickListener {
	private DatabaseHelper helper;
    private Context context;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Flight, UUID> flightsDao;
	private Dao<Car, UUID> carsDao;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao;
	private Dao<PublicTransport, UUID> publicTransportDao;
    private List<FootprintPosition> listPositions;
    FootprintPosition footprintPosition;
    Footprint footprint;

    public PositionsAdapter(Context context, List<FootprintPosition> listPositions) {
        this.context = context;
        this.listPositions = listPositions;
    }

    public int getCount() {
        return listPositions.size();
    }

    public Object getItem(int position) {
        return listPositions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    /**
     * Methode die eine Zeile der Liste zurück liefert.
     * Je nach Positionstyp wird ein anderes Icon (Flugzeug, Auto, etc.) dargestellt.
     * Außerdem die Emissionen sowie der Name der Position.
     * @return View element with data for one Footprint
     */
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	
        FootprintPosition entry = listPositions.get(position);
    	helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.position_list_row, null);
        }
    View row = convertView;
    Double convertedCalculation = null;
	ImageView positionType = (ImageView) row.findViewById(R.id.position_img);   
	try {
		   footprintDao = helper.getFootprintDao();
		   footprintPositionDao = helper.getFootprintPositionDao();
		   flightsDao = helper.getFlightsDao();
		   carsDao = helper.getCarDao();
		   energyConsumptionDao = helper.getEnergyConsumptionDao();
		   publicTransportDao = helper.getPublicTransportDao();
		   footprintPosition = footprintPositionDao.queryForId(UUID.fromString(entry.getId()));
		   footprint = footprintDao.queryForId(UUID.fromString(footprintPosition.getFootprint().getId()));;
		} catch (SQLException e) {
		e.printStackTrace();
	}
       if (entry.getName().equals("Flug") || entry.getName().equals("Linienflug"))
       {
     	   for (Flight flight : flightsDao)
    	   {
    		   if (flight.getFootprintPosition().getId().equals(footprintPosition.getId()))
    		   {
	    		   try {
					flightsDao.refresh(flight);
					flightsDao.queryForId(UUID.fromString(flight.getId()));
					} catch (SQLException e) {
						e.printStackTrace();
					}					
		       		positionType.setImageResource(R.drawable.flight);
		       		if (flight.getEmission()!=null)
		       		{
		       			convertedCalculation = (flight.getEmission()/1000);
		       		}
	    	    }
    	   }
   	   }
       if (entry.getName().equals("Fahrzeug") || entry.getName().equals("Herstellerfahrzeug"))
       {
     	   for (Car car : carsDao)
    	   {
    		   if (car.getFootprintPosition().getId().equals(footprintPosition.getId()))
    		   {
	    		   try {
	    			   carsDao.refresh(car);
	    			   carsDao.queryForId(UUID.fromString(car.getId()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
		       		positionType.setImageResource(R.drawable.car);		       		
		       		convertedCalculation = (car.getEmission()/1000);
	    	    }
    	   }
   	   }
       if (entry.getName().equals("Energieverbrauch"))
       {
     	   for (EnergyConsumption energyConsumption : energyConsumptionDao)
    	   {
    		   if (energyConsumption.getFootprintPosition().getId().equals(footprintPosition.getId()))
    		   {
	    		   try {
	    			   energyConsumptionDao.refresh(energyConsumption);
	    			   energyConsumptionDao.queryForId(UUID.fromString(energyConsumption.getId()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
		       		positionType.setImageResource(R.drawable.energyconsumption);		       		
		       		convertedCalculation = (energyConsumption.getEmission()/1000);
	    	    }
    	   }
   	   }
       if (entry.getName().equalsIgnoreCase("Öffentlicher Verkehr"))
       {
     	   for (PublicTransport publicTransport : publicTransportDao)
    	   {
    		   if (publicTransport.getFootprintPosition().getId().equals(footprintPosition.getId()))
    		   {
	    		   try {
	    			   publicTransportDao.refresh(publicTransport);
	    			   publicTransportDao.queryForId(UUID.fromString(publicTransport.getId()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
		       		positionType.setImageResource(R.drawable.publictransport);		       		
		       		convertedCalculation = (publicTransport.getEmission()/1000);
	    	    }
    	   }
   	   }
	      
	   TextView calculationTextView = (TextView) row.findViewById(R.id.nametxt);
	   TextView name = (TextView) row.findViewById(R.id.calculation);
	   name.setText(footprintPosition.getName());
	   
	   String cuttedFootprintDescription;
	   String footprintDescription = footprintPosition.getDescription();
	   if (footprintDescription != null)
	   {
		   cuttedFootprintDescription = footprintDescription.substring(0, Math.min(footprintDescription.length(), 15));
		   if (footprintDescription.length() > 15)
		   {
			   cuttedFootprintDescription = cuttedFootprintDescription+"...";
		   }	
	   }
	   else {
		   cuttedFootprintDescription = " ";
	   }  
	   TextView description = (TextView) row.findViewById(R.id.descriptiontxt);
	   description.setText(": " + cuttedFootprintDescription);

       String convertedTotalCalculation;
	   convertedTotalCalculation = convertedCalculation.toString();
	   NumberFormat formatter = new DecimalFormat("#0.00");
	   convertedTotalCalculation = (formatter.format(convertedCalculation)).toString();

       if (footprint.getCalculationCategory().equalsIgnoreCase("Carbon"))
	      {
	    	   calculationTextView.setText(convertedTotalCalculation + " kg CO2-eq");
	      }
	    if (footprint.getCalculationCategory().equalsIgnoreCase("Water"))
	      {
	    	   calculationTextView.setText(convertedTotalCalculation + " l Wasser");
	      }	    
       ImageView cfparrow = (ImageView) row.findViewById(R.id.arrowcf_img);   
       cfparrow.setImageResource(R.drawable.arrowcf);
       return convertView;
    }

@Override
public void onClick(DialogInterface dialog, int which) {
	
}

}

