package htw.bui;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import openreskit.odata.Car;
import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.Employee;
import openreskit.odata.EnergyConsumption;
import openreskit.odata.Flight;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.PublicTransport;
import openreskit.odata.Synchronization;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Activity zur Darstellung aller Footprints in einer Liste sowie zum
 * Hinzuf�gen neuer Footprints. Da es sich um das Startmen� der App
 * handelt, beinhaltet die Activity ebenfalls das Optionsmen� durch das
 * die Daten-Synchronisation mit dem Server durchgef�hrt werden kann.
 */
public class FootprintList extends Activity implements OnItemSelectedListener {
	Context ctx = this;
	DatabaseHelper helper;
	private Dao<Footprint, UUID> footprintDao = null;
	private Dao<FootprintPosition, UUID> footprintPositionDao = null;
	private Dao<Flight, UUID> flightDao = null;
	private Dao<Car, UUID> carDao = null;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao = null;
	private Dao<PublicTransport, UUID> publicTransportDao = null;
	private Dao<Distance, UUID> distanceDao = null;
	Dao<Employee, UUID> employeeDao;
	private List<Footprint> footprintList;
	private boolean resumeHasRun = false;
	ArrayAdapter<String> arrayAdapter;
	TreeSet<String> fpIds = new TreeSet<String>();
	ArrayList<String> footprints = new ArrayList<String>();
	int currentFootprint;
	TreeSet<Integer> fpNummern = new TreeSet<Integer>();
	String choice = "nichts";
	
	    	
	FootprintsAdapter adapter;
	
	/**
	 * Methode die das Formular mit der Liste zur �bersicht der vorhandenen Footprints
	 * �ffnet und darstellt. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.footprint_list);
        
        Spinner spinner = (Spinner) findViewById(R.id.fptypes_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
	             R.array.footprinttypes_array, android.R.layout.simple_spinner_item);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(spinnerAdapter);
	    spinner.setOnItemSelectedListener(this);
	    
	    helper = OpenHelperManager.getHelper(this, DatabaseHelper.class);    	  
	    deleteEmptyFootprints();
        updateList();
        
	  	ImageButton addButton = (ImageButton) findViewById(R.id.addbtn);
	  	addButton.setOnClickListener(addButtonListener);
    }
    
    
	/**
	 * Methode die beim Klicken des "Hinzuf�gen" Buttons das Formular zur �bersicht der Footprint-
	 * Positionen �ffnet. Dabei wird die ausgew�hlte Kategorie des Footprints (Water/Carbon) �bergeben.
	 */
  	View.OnClickListener addButtonListener = new View.OnClickListener() {
	  	  public void onClick(View v) {
	  		  	Intent newFpIntent = new Intent(getBaseContext(), PositionList.class);
	  		  	newFpIntent.putExtra("currentFootprint", "new");
	  		  	newFpIntent.putExtra("footprintCategory", choice);
	  		  	startActivity(newFpIntent);
	  		 }
	  	  };
	  	  
  	/**
  	 * Wenn die Liste der Footprint-Positionen geschlossen wird, wird diese Acitivity wieder
  	 * in den Vordergrund gebracht. Dadurch werden zun�chst leere Footprints gel�scht, f�r
  	 * die keine Positionen erstellt wurden (voriges Men� wurde ohne Erstellung abgebrochen).
  	 * Anschlie�end wird die Liste der Footprints aktualisiert.
  	 */
	protected void onResume() {
		super.onResume();
	 	if (!resumeHasRun) {
	 		resumeHasRun = true;
	 		return;
	 		}
	 	deleteEmptyFootprints();
	 	updateList();
	}
	  	  
    /**
	* Liefert das Optionsmen� f�r den Start Bildschirm.
	*/
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
    }

	/**
	* Startet einen Intent f�r jedes Objekt des Optionsmen�s und
	* liefert einen Boolean-Wert f�r jedes Optionsitem.
	* Die Optionen sind Download der Daten vom Server, Upload der 
	* Daten zum Server, ein Einstellungsmen� zur �nderung der Server-IP,
	* eine Funktion zum l�schen aller Datens�tze sowie das Schlie�en der
	* Anwendung.
	* @return Boolean value for Optionsitem
	*/
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    		switch (item.getItemId()) {
    		case R.id.downloadSync:
    			
    	    	Synchronization downloadSync = new Synchronization();
    	    	downloadSync.getData(ctx, FootprintList.this);
    			return true;
    		case R.id.uploadSync:
    		    Synchronization uploadSync = new Synchronization();
    		    uploadSync.writeData(ctx, helper);
       			return true;
    		case R.id.settings:
    			Intent startPreferences = new Intent(this, Options.class);
    			this.startActivity(startPreferences);    			
    			return true;
    		case R.id.deleteAll:
    			try {
					deleteLocalData();
				} catch (SQLException e) {
					e.printStackTrace();
				}
    			return true;
    		case R.id.exit:
    			finish();
    			return true;
    		default:
    			super.onOptionsItemSelected(item);
    		}
    		return false;
    }
	/**
	 * Methode zum Nachladen der Eintr�ge der Footprints aus der Datenbank
	 * in eine Liste. Hierf�r werden die Datens�tze an einen Listenadapter (FootprintsAdapter)
	 * �bergeben, der die Darstellung der einzelnen Listenobjekten �bernimmt.
	 * Es wird f�r die Listeneintr�ge ein Clicklistener definiert. Durch Klick 
	 * �ffnet sich f�r den Footprint die Liste der angelegten Positionen.
	 */
	@SuppressWarnings("unchecked")
	public void updateList() {
		ListView cfsListView = (ListView) findViewById(R.id.overviewlist);
		final ArrayList<String> footprintIdsList = new ArrayList<String>();  	  

    	try {
    		footprintDao = helper.getFootprintDao();
    		footprintPositionDao = helper.getFootprintPositionDao();
    		flightDao = helper.getFlightsDao();
    		carDao = helper.getCarDao();
    		energyConsumptionDao = helper.getEnergyConsumptionDao();
    		publicTransportDao = helper.getPublicTransportDao();
    		distanceDao = helper.getDistanceDao();
    		footprintDao.updateBuilder();
			footprintList = footprintDao.queryForAll();
			employeeDao = helper.getEmployeeDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}    	
		for (Footprint footprint : footprintList)
		{
			footprintIdsList.add(footprint.getId());
		}
		for (Footprint footprint : footprintList) 
		{
			fpNummern.add(footprint.getNr());
			fpIds.add(footprint.getNr()+"");

    		@SuppressWarnings("rawtypes")
			TreeSet sorted = new TreeSet(fpNummern);

    		footprints.clear();
    		footprints.addAll(sorted);
    		
    		cfsListView.invalidate();
    		
    		adapter = new FootprintsAdapter(this, footprintList);
	        
    	  	adapter.notifyDataSetChanged();
    	  	cfsListView.setAdapter(adapter);		    	  	

    	  	cfsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    	  	  @Override
    	  	  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	  	    	  String footprintPositionId = footprintIdsList.get(position);

    	          for (int i : fpNummern)
    	          {
	    	          if (i == position+1)
	    	          {
	    	        	  currentFootprint = i;
	    	          }
    	          }
      		  	Intent editFpIntent = new Intent(getBaseContext(), PositionList.class);
      		  	editFpIntent.putExtra("currentFootprint", footprintPositionId);			      	      		  	
      		    startActivity(editFpIntent);
    	  	  }
    	  	});
		}
	}

 /**
  * Methode zum L�schen aller auf dem Ger�t vorhandener Eintr�ge zu Footprints 
  * und der Positionen in der SQLite Datenbank. 
  * @throws SQLException
  */
  private void deleteLocalData() throws SQLException 
    {
    	if(!footprintList.isEmpty()) 
    	{
    		footprintDao.executeRaw("Delete FROM footprint");
        	footprintPositionDao.executeRaw("Delete FROM footprintPosition");
        	flightDao.executeRaw("Delete FROM flight");
        	carDao.executeRaw("Delete From car");
        	energyConsumptionDao.executeRaw("Delete From energyConsumption");
        	publicTransportDao.executeRaw("Delete From publicTransport");
        	distanceDao.executeRaw("Delete From distance");
        	employeeDao.executeRaw("Delete From employee");
        	Toast.makeText(ctx, "Datenbank geleert", Toast.LENGTH_SHORT).show();
    		footprintList.clear();
    		adapter.notifyDataSetChanged();
    	}
    }
  /**
   * Methode zum L�schen von Footprints, zu denen keine Positionen exisitieren.
   * Dies wird n�tig, wenn ein Footprint angelegt wurde, aber die Erstellung der
   * Position(en) abgebrochen wurde. Da in diesem Fall nicht von der Absicht zur
   * Erstellung eins kompletten Footprints augegangen werden kann und die Daten-
   * konsistenz so gest�rt w�rde, ist der angelegte Footprint ohne Position unn�tig.
   */
  private void deleteEmptyFootprints() 
  {
	  try 
		{
	  	footprintDao = helper.getFootprintDao();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		for (Footprint footprint : footprintDao)
		{
			if (footprint.getCalculationCategory() == null)
			{
				try {
					footprintDao.executeRaw("Delete FROM footprint WHERE id='"+footprint.getId()+"'");
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
		}
  }
  
  	/**
  	 * Methode die die Auswahl der Optionsmen�-Auswahl in eine Variable schreibt.
  	 */
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {        
        choice = (String) parent.getItemAtPosition(pos);        
     }

    public void onNothingSelected(AdapterView<?> parent) {
       
    }
}