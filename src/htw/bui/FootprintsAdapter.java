
package htw.bui;

import java.sql.SQLException;
import java.text.DecimalFormat;
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
 * eines Footprints in die einzelnen Listeneinträge des Formulars mit den Footprints.
 * Siehe:
 * http://code.google.com/p/myandroidwidgets/source/browse/trunk/Phonebook/src/com/abeanie/PhonebookAdapter.java
 */
public class FootprintsAdapter extends BaseAdapter implements OnClickListener {
	private DatabaseHelper helper;
    private Context context;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<Flight, UUID> flightsDao;
	private Dao<Car, UUID> carsDao;
	private Dao<PublicTransport, UUID> publicTransportDao;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao;
    private List<Footprint> listFootprints;
    Footprint footprint;    

    public FootprintsAdapter(Context context, List<Footprint> listFootprints) {
        this.context = context;
        this.listFootprints = listFootprints;
    }

    public int getCount() {
        return listFootprints.size();
    }

    public Object getItem(int position) {
        return listFootprints.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Methode die eine Zeile der Liste zurück liefert.
     * Je nach Category des Footprints (Water/Carbon) wird ein Icon
     * mit einem Fußabdruck dargestellt.
     * @return View element with data for one Footprint
     */
    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	
        Footprint entry = listFootprints.get(position);
    	helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.footprint_list_row, null);
        }
        	   
    	try {
    			footprintDao = helper.getFootprintDao();
    			flightsDao = helper.getFlightsDao();
    			carsDao = helper.getCarDao();
    			publicTransportDao = helper.getPublicTransportDao();
    			energyConsumptionDao = helper.getEnergyConsumptionDao();
    			footprint = footprintDao.queryForId(UUID.fromString(entry.getId()));
		} catch (SQLException e) {			
			e.printStackTrace();
		}	
		View row = convertView;
		String fpCat = "nichts";
		
//		String balanceYear = footprint.getBalanceYear();
//		
//		String[] dateOnly = balanceYear.split("T");
//		String[] dateParts = dateOnly[0].split("-"); 
//		String formattedDate = dateParts[2]+"."+dateParts[1]+"."+dateParts[0];
		
		TextView name = (TextView) row.findViewById(R.id.totalFp);
	    TextView fpTextView = (TextView) row.findViewById(R.id.fpnametxt);
        name.setText("Footprint "+footprint.getNr()/**+" vom "+formattedDate**/);
        Double totalFp = 0.0;
        for (FootprintPosition footprintPosition : footprint.getFootprintPositions())
        {
    	   for (Flight flight : flightsDao)
	       {
	    	   if (flight.getFootprintPosition().getId().toString().equals(footprintPosition.getId().toString()))
	    	   {	
	    		   if (flight.getEmission()!=null)
	    		   {
	    			   totalFp = totalFp + flight.getEmission();
	    		   }
	    	}
	    }
	    for (Car car : carsDao)
	    {
    	   if (car.getFootprintPosition().getId().toString().equals(footprintPosition.getId().toString()))
    	   {
    		   totalFp = totalFp + car.getEmission();
    	   }
	    }
	    for (PublicTransport publicTransport : publicTransportDao)
	    {
    	   if (publicTransport.getFootprintPosition().getId().toString().equals(footprintPosition.getId().toString()))
    	   {
    		   totalFp = totalFp + publicTransport.getEmission();
    	   }
	    }
	    for (EnergyConsumption energyConsumption : energyConsumptionDao)
	    {
    	   if (energyConsumption.getFootprintPosition().getId().toString().equals(footprintPosition.getId().toString()))
    	   {
    		   totalFp = totalFp + energyConsumption.getEmission();
    	   }
	    }
	    fpCat = footprint.getCalculationCategory();
        }
        ImageView newFpImage = (ImageView) row.findViewById(R.id.fp_img);  

   		totalFp = totalFp/1000;
   		DecimalFormat twoDForm = new DecimalFormat("##.##");
   		String convertedTotalFp = twoDForm.format(totalFp);
		if (fpCat.equalsIgnoreCase("Water"))
		{
			fpTextView.setText(convertedTotalFp +" l Wasser"); 
			newFpImage.setImageResource(R.drawable.footprint_water_small);
		}
		if (fpCat.equalsIgnoreCase("Carbon"))
		{
			fpTextView.setText(convertedTotalFp +" kg CO2-eq");
			newFpImage.setImageResource(R.drawable.footprint_carbon_small);
		}
  		ImageView cfparrow = (ImageView) row.findViewById(R.id.arrowcf_img);   
  		cfparrow.setImageResource(R.drawable.arrowcf);
  		
        return convertView;
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
	}
}




