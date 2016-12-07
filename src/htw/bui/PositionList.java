package htw.bui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import openreskit.odata.Car;
import openreskit.odata.DatabaseHelper;
import openreskit.odata.Flight;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;

import org.joda.time.LocalDateTime;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Formular zur Darstellung aller zu einem Footprint gehörenden Positionen
 * (Flüge, Autofahrten, Energieverbräuche, Zugfahrten). Über der Liste 
 * befindet sich ein Auswahlfeld für die Erstellung einer neuen Position.
 * Die Darstellung der einzlenen View Elemente zu einer Position in der Liste wird 
 * dein Listen Adapter eingesetzt.
 */
public class PositionList extends Activity implements OnItemSelectedListener {
	protected static LocalActivityManager mLocalActivityManager;
	private DatabaseHelper helper;
	private Dao<Footprint, UUID> footprintDao = null;
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	Dao<Flight, UUID> flightsDao;
	Dao<Car, UUID> carsDao;
	LinearLayout entryLayout;
	String choice = "nichts";
	String footprintCategory = "nichts";
	Context ctx = this;
  	FootprintPosition clickedFootprintPosition = null;
	private UUID currentFootprint;
	String posString;
	private boolean resumeHasRun = false;
	int lastFootprint = 0;	
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> listAdapter;
	Boolean waterFp = true;

	/**
	 * Funktion die nach dem Starten des Formulars die einzlenen View
	 * Elemente darstellt.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String currentFp = extras.getString("currentFootprint");
        footprintCategory = extras.getString("footprintCategory");
        setContentView(R.layout.position_list);
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
    		
        Spinner spinner = (Spinner) findViewById(R.id.positiontypes_spinner);
 	     ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
 	    		 	R.array.positiontypes_array, android.R.layout.simple_spinner_item);
	     
 	     spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     spinner.setAdapter(spinnerAdapter);
	     spinner.setOnItemSelectedListener(this);
	     
	     ImageButton addPositionButton = (ImageButton) findViewById(R.id.addpos);
	     addPositionButton.setOnClickListener(addPositionListener);
	     
 		  try {
	  	    	footprintDao = helper.getFootprintDao();	  	    	
	  	    	footprintPositionDao = helper.getFootprintPositionDao();
	  	    		  				  				  	    
	  		} catch (SQLException e2) {
	  			e2.printStackTrace();
	  		} 
	  		/**
	  		 * Wenn dieses Formular für das Erstellen eines neuen Footprints geöffnet wurde,
	  		 * wird dieser neue Footprint erstellt und in die Datenbank geschrieben.
	  		 * Dies geschieht nicht erst, wenn eine Position angelegt wird, sondern vorher, da
	  		 * für das folgende Formular zur Erstellung einer Position immer ein bestehender
	  		 * Footprint übergeben werden muss. Durch die relativ lose Kopplung von Formularen
	  		 * kann nicht anders verfahren werden.
	  		 * Entsprechend wird durch die Else Alternative ein bestehender Footprint geladen, 
	  		 * wenn dieses Formular zum Hinzufügen von Positionen zu bereits vorher erstellten
	  		 * Footprints geöffnet wurde.
	  		 */
	  		if (currentFp.equals("new"))
	    	{	
		  		for (Footprint footprint : footprintDao)
		  		{
		  			if (footprint.getNr() > lastFootprint)
		  			{
		  				lastFootprint = footprint.getNr();
		  			}
		  		}		  		
		  		lastFootprint = lastFootprint + 1;
				Footprint footprint = new Footprint();
				currentFootprint = UUID.randomUUID();
				footprint.setId(currentFootprint.toString());
				footprint.setServerId(13);
				footprint.setName("Footprint "+lastFootprint);
				footprint.setDescription("Description of Footprint "+lastFootprint);
				footprint.setSiteLocation("Werk 3");
				footprint.setEmployees(100);
				footprint.setCalculation(0);
				footprint.setNr(lastFootprint);
				footprint.setBalanceYear((new LocalDateTime()).toString());
			  	try 
				{
					footprintDao.createOrUpdate(footprint);
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
	    	}	 	  		    
	    	else
	    	{	    		
	    		for(Footprint footprint : footprintDao)
	    		{
	    			if (footprint.getId().equalsIgnoreCase(currentFp))
	    			{	    				
	    				updatePositionsList(footprint);	    
	    				if (footprint.getCalculationCategory().equalsIgnoreCase("Carbon"))
	    				{
	    					waterFp = false;
	    				}
	    			}
	    		}
	    	}
			if (footprintCategory != null && footprintCategory.equalsIgnoreCase("Carbon Footprint"))
            {
            	waterFp = false;
            }
    }
    
    /**
     * Methode zum Aktualisieren der Liste mit Positionen eines Footprints.
     * Die Daten zu dem Footprint werden aus der DB geladen und an einen 
     * Listen Adapter übergeben, der diese in die View Elemente einer
     * Listen-Zeile einträgt.
     * @param footprint
     */
	void updatePositionsList(Footprint footprint) {
		listItems.clear();
        final List<FootprintPosition> listOfPositions = new ArrayList<FootprintPosition>();        
        currentFootprint = UUID.fromString(footprint.getId());
        
		for (FootprintPosition footprintPosition : footprint.getFootprintPositions())
		{	         			        
	         listOfPositions.add(footprintPosition);
	         listItems.add(footprintPosition.getDescription());
		}
		   
		ListView positionsListView = (ListView) findViewById(R.id.positionlist);
     
	     if (positionsListView != null)
	     {
	         positionsListView.setClickable(true);
	         PositionsAdapter adapter = new PositionsAdapter(this, listOfPositions);
	         positionsListView.setAdapter(adapter);
	         
	  	  	 /**
	  	  	  * Listener durch den ein Formular mit bestehenden Daten geöffnet wird, 
	  	  	  * wenn eine Position zum Editieren angeklickt wird.
	  	  	  */
		     positionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		  	  	 @Override
			  	 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			  	  	 Footprint footprint = null;  
			  	  	 ArrayList<String> footprintPositionList = new ArrayList<String>();  				  	  	 
					 try {
						 	footprintDao = helper.getFootprintDao();
						 	flightsDao = helper.getFlightsDao();
						 	carsDao = helper.getCarDao();
							footprint = footprintDao.queryForId(currentFootprint);
						} catch (SQLException e) {
							e.printStackTrace();
						}

			  	    	for (FootprintPosition footprintPosition : footprint.getFootprintPositions())
			  	    	{
			  	    		footprintPositionList.add(footprintPosition.getId());
			  	    	}  	    	
			  	    	String footprintPositionId = footprintPositionList.get(position);
						try {
							clickedFootprintPosition = footprintPositionDao.queryForId(UUID.fromString(footprintPositionId));
						} catch (SQLException e) {			
							e.printStackTrace();
						}
						if (clickedFootprintPosition.getName().equalsIgnoreCase("Fahrzeug") || clickedFootprintPosition.getName().equalsIgnoreCase("Herstellerfahrzeug"))
			  	    	{
					  		Intent carIntent = new Intent(arg1.getContext(), NewCar.class);
					  		carIntent.putExtra("currentFootprint", currentFootprint.toString());
					  		carIntent.putExtra("editedPosition", footprintPositionId);
					  		startActivity(carIntent);					
			  	    	}
			
			  	    	else if (clickedFootprintPosition.getName().equalsIgnoreCase("Flug") || clickedFootprintPosition.getName().equalsIgnoreCase("Linienflug"))
			  	    	{ 
					  		Intent flightIntent = new Intent(arg1.getContext(), NewFlight.class);
					  		flightIntent.putExtra("currentFootprint", currentFootprint.toString());
					  		flightIntent.putExtra("editedPosition", footprintPositionId);
					  		startActivity(flightIntent);
			  	    	}
			  	    	else if (clickedFootprintPosition.getName().equalsIgnoreCase("Öffentlicher Verkehr"))
			  	    	{ 
					  		Intent publicTransportIntent = new Intent(arg1.getContext(), NewPublicTransport.class);
					  		publicTransportIntent.putExtra("currentFootprint", currentFootprint.toString());
					  		publicTransportIntent.putExtra("editedPosition", footprintPositionId);
					  		startActivity(publicTransportIntent);
			  	    	}
			  	    	else if (clickedFootprintPosition.getName().equalsIgnoreCase("Energieverbrauch"))
			  	    	{ 
					  		Intent energyConsumptionIntent = new Intent(arg1.getContext(), NewEnergyConsumption.class);
					  		energyConsumptionIntent.putExtra("currentFootprint", currentFootprint.toString());
					  		energyConsumptionIntent.putExtra("editedPosition", footprintPositionId);
					  		startActivity(energyConsumptionIntent);
			  	    	}
			  	    	else {
			  	    		Toast.makeText(ctx, "Eintrag kann nicht bearbeitet werden.", Toast.LENGTH_LONG).show();
			  	    	}
			  	  	  }
		  	  	});
	     }    
	}
    
	/**
	 * Nach Erstellung eines neuen Footprints wird die Liste der Positionen erneuert
	 */
    protected void onResume() {
		 super.onResume();
		   if (!resumeHasRun) {
		        resumeHasRun = true;
		        return;
		    }		   
		   if (currentFootprint != null)
		   {
			   Footprint footprint = null;
				 try {
					 	footprintDao = helper.getFootprintDao();
						footprint = footprintDao.queryForId(currentFootprint);
					} catch (SQLException e) {				
						e.printStackTrace();
					}
					updatePositionsList(footprint);
		   }
		   else { finish();}
 	 }
    
    /**
     * Listener für das Ersteller einer neuen Position. Abhängig von der Auswahl
     * des Positionstyps (Flug, Fahrt, etc.) wird eines enstprechendes Formular
     * zur Eingabe der Positionsdaten geöffnet.
     */
    View.OnClickListener addPositionListener = new View.OnClickListener() {
  	  public void onClick(View v) {
  		 if (choice != null)
  		 { 
	  		try {
		  	    	footprintDao = helper.getFootprintDao();
		  			footprintPositionDao = helper.getFootprintPositionDao();
		  		  							  	    
		  		} catch (SQLException e2) {
		  			e2.printStackTrace();
		  		} 

		  		 if (choice.equalsIgnoreCase("Flug"))
		  		 {
	  			 		Intent flightIntent = new Intent(v.getContext(), NewFlight.class);
	  			  		flightIntent.putExtra("currentFootprint", currentFootprint.toString());
	  			  		flightIntent.putExtra("footprintCategory", footprintCategory);
				  		startActivity(flightIntent);
				  		
		  		 }
		  		 if (choice.equalsIgnoreCase("Autofahrt"))
  				 {
			  			Intent carIntent = new Intent(v.getContext(), NewCar.class);
	  			  		carIntent.putExtra("currentFootprint", currentFootprint.toString());
	  			  		carIntent.putExtra("footprintCategory", footprintCategory);
				  		startActivity(carIntent);
		  		 }
		  		 if (choice.equalsIgnoreCase("Öffentlicher Verkehr"))
  				 {
		  			 if (!waterFp)
		  			 {
			  			Intent publicTransportIntent = new Intent(v.getContext(), NewPublicTransport.class);
	  			  		publicTransportIntent.putExtra("currentFootprint", currentFootprint.toString());
	  			  		publicTransportIntent.putExtra("footprintCategory", footprintCategory);
				  		startActivity(publicTransportIntent);
		  			 }
		  			 else {
		  				 Toast.makeText(ctx, "Für die ausgewählte Position kann kein Water Footprint berechnet werden.", Toast.LENGTH_LONG).show();
		  			 }
		  		 }
		  		 if (choice.equalsIgnoreCase("Energieverbrauch"))
  				 {
		  			 if (!waterFp)
		  			 {
			  			Intent energyConsumptionIntent = new Intent(v.getContext(), NewEnergyConsumption.class);
	  			  		energyConsumptionIntent.putExtra("currentFootprint", currentFootprint.toString());
	  			  		energyConsumptionIntent.putExtra("footprintCategory", footprintCategory);
				  		startActivity(energyConsumptionIntent);
		  			 }
		  			 else {
		  				 Toast.makeText(ctx, "Für die ausgewählte Position kann kein Water Footprint berechnet werden.", Toast.LENGTH_LONG).show();
		  			 }
		  		 }
	  		}
  		 }
  	};

  	/**
  	 * Übergeben der Auswahl, wenn ein Listeneintrag angeklickt wurde.
  	 */
     public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        
        choice = (String) parent.getItemAtPosition(pos);        
     }
     public void onNothingSelected(AdapterView<?> parent) {       
     }
    
}

