package htw.bui;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.Employee;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.ICalculation;
import openreskit.odata.PublicTransport;
import openreskit.odata.PublicTransportCalculation;

import org.joda.time.LocalDateTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class NewPublicTransport extends OrmLiteBaseActivity<DatabaseHelper> {
	DatabaseHelper helper;
	Context ctx = this;	
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<PublicTransport, UUID> publicTransportDao;
	private Dao<Distance, UUID> distanceDao;
	String currentFp = null;
	Footprint currentFootprint;
	Spinner transportTypeSpinner;
	Spinner distanceType;	
	String footprintCategory = "nichts";
	private boolean resumeHasRun = false;	
	EditText descriptionView;
	EditText distanceView;	
	SharedPreferences preferences;
	Boolean employeeOk = false;
	Dao<Employee, UUID> employeeDao;
	
    PublicTransport editedTransport = null;
	FootprintPosition editedPosition = null;	
	PublicTransport publicTransport;		
	FootprintPosition footprintPosition;	
		
	/**
	 * Funktion zur Darstellung der View Elemente.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        currentFp = extras.getString("currentFootprint");
        String editedPos = extras.getString("editedPosition");
        footprintCategory = extras.getString("footprintCategory");
        setContentView(R.layout.newpublictransport);
        
        descriptionView = (EditText) findViewById(R.id.description);
        distanceView = (EditText) findViewById(R.id.distance);

        distanceView.setEnabled(false);
		
        transportTypeSpinner = (Spinner) findViewById(R.id.transporttypes_spinner);
	    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.transporttypes_array, android.R.layout.simple_spinner_item);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    transportTypeSpinner.setAdapter(spinnerAdapter);
	     
		distanceType = (Spinner) findViewById(R.id.distancetypes_spinner);
	    ArrayAdapter<CharSequence> distanceSpinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.distancetypes_array, android.R.layout.simple_spinner_item);
	    distanceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    distanceType.setAdapter(distanceSpinnerAdapter);
	      
        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
  	    helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
  	    
		try {
			footprintPositionDao = helper.getFootprintPositionDao();
  	    	footprintDao = helper.getFootprintDao();
  	    	publicTransportDao = helper.getPublicTransportDao();
  			currentFootprint = footprintDao.queryForId(UUID.fromString(currentFp));
  			employeeDao = helper.getEmployeeDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/**
		 * Category des Footprints (Water/Carbon) wird aus einer bereits exisitieren Position 
		 * ausgelesen. Normalerweise wird die Category beim Erstellen eines Footprints gesetzt.
		 * Falls es sich um die Erstellung einer zweiten Position zu einem Footprint handelt,
		 * muss daher die Category erneut ausgelesen werden.
		 */
  	    if (footprintCategory == null)
		{
			if (currentFootprint.getCalculationCategory().equalsIgnoreCase("Carbon"))
				{
					footprintCategory = "Carbon Footprint";
				}
			if (currentFootprint.getCalculationCategory().equalsIgnoreCase("Water"))
				{
					footprintCategory = "Water Footprint";
				}
		}
  	    
		/**
		 * Wurde das Formular zum Editieren einer bestehenden Fahrt-Position geöffnet wurde,
		 * werden die Werte aus dem exisiterden Datenobjekt ausgelesen und in das Formular 
		 * eingetragen.
		 */
  	    TextView headline = (TextView) findViewById(R.id.headline1);
  	    if (editedPos != null)
  	    {  	    	  	 
  	    	try {
  	    		editedPosition = footprintPositionDao.queryForId(UUID.fromString(editedPos));
			} catch (SQLException e1) {				
				e1.printStackTrace();
			}
  			for (PublicTransport publicTransport2 : publicTransportDao)
			{
				if (publicTransport2.getFootprintPosition().getId().equals(editedPosition.getId()))
					{
						distanceView.setText((publicTransport2.getDistance())+"");
						descriptionView.setText(editedPosition.getName());
						headline.setText("Fahrt bearbeiten");						
						transportTypeSpinner.setSelection(publicTransport2.getTransportType());						
						try {
							editedTransport = publicTransportDao.queryForId(UUID.fromString(publicTransport2.getId()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
  				}
			}
  	    }
  	    
    	/**
    	 * Listener für die Festlegung der Eingabeart der Distanz. Es kann zwischen der
    	 * Direkteingabe einer Entfernung in km, der Ermittlung mittels GPS-Tracking sowie
    	 * durch die Markierung des Starts und Ziels in Google Maps gewählt werden.
    	 */
	     distanceType.setOnItemSelectedListener(new OnItemSelectedListener() {
	         @Override
	         public void onItemSelected(AdapterView<?> arg0, View arg1,
	                 int position, long arg3) {	             
//		        	 int choosenDistanceType = distanceType.getSelectedItemPosition();    
	        	 if (position == 1)
	        	 {
	        		 distanceView.setFocusable(true);
	        		 distanceView.setEnabled(true);
	        		 distanceView.setHint("in km");
	        		 distanceView.requestFocus();
	        	 }
	        	 else if (position == 2)
	        	 {
		           	 Intent mapIntent = new Intent(ctx, MapDistance.class);
		        	 mapIntent.putExtra("currentFootprint", currentFp);
		        	 mapIntent.putExtra("positionType", "car");
		        	 startActivity(mapIntent);
	        	 }
	        	 else if (position == 3)
	        	 {
		           	 Intent gpsIntent = new Intent(ctx, GpsTracking.class);
		           	 gpsIntent.putExtra("currentFootprint", currentFp);
		           	 gpsIntent.putExtra("positionType", "car");
		        	 startActivity(gpsIntent);
	        	 }	
	        	 else if (position == 4)
	        	 {
	        		 Toast.makeText(ctx, "Diese Option ist nur für Flüge verfügbar.", Toast.LENGTH_LONG).show();
	        	 }	
	         }
	         @Override
	         public void onNothingSelected(AdapterView<?> arg0) {	            
	         
	         }
	     });	
	     Button acceptButton = (Button) findViewById(R.id.okbutton);
	     acceptButton.setOnClickListener(acceptListener);
    }
    
    /**
     * Wenn ein Formular zur Ermittlung der Distanz (Maps/Tracking) geschlossen wurde, wird
     * die ermittelte Entfernung aus der SQLite-DB abegefragt und in das Formular eingetragen.
     */
    public void onResume() {
		 super.onResume();
		   if (!resumeHasRun) {
		        resumeHasRun = true;
		        return;
		    }
	  	  try {
	  			distanceDao = helper.getDistanceDao();
	  		} catch (SQLException e2) {
	  			e2.printStackTrace();
	  		} 
		   	for (Distance distance : distanceDao)
		   	{
		   		if (distance.getId().equals(currentFp))
		   		{			   			
	    	   		Double distanceValue = distance.getDistance();
	    	   		DecimalFormat twoDForm = new DecimalFormat("#######.");
	    	   		String convertedDistance = twoDForm.format(distanceValue);
	    	   		String[] convertedDistance2 = convertedDistance.split(",");    	    	   		
			   		distanceView.setText(convertedDistance2[0]);
		   		}
		   	}			   	
    }
    
    /**
     * Listener für das Schließen des Formulars. Dabei werden die in das Formular eingetragenen
     * Werte aus den View Elementen abgefragt und in die SQLite-DB geschrieben. Im Falle, dass
     * das Formular für das Editieren einer bestehenden Fahrt geöffnet wurde, werden die Daten
     * aktualisiert.
     */
    View.OnClickListener acceptListener = new View.OnClickListener() {
	  	  public void onClick(View v) {	          
	  	  ArrayList<FootprintPosition> footprintPositionList = new ArrayList<FootprintPosition>();
	      Double distance = null;	
	      Boolean distanceOk = false;
	      EditText distanceView = (EditText) findViewById(R.id.distance);	  		  
	      try { 
				 distance = Double.parseDouble(distanceView.getText().toString());
			  }
	      catch (NumberFormatException e) {
			  e.printStackTrace();
	      }	  		  		   		  
  		  String publicTransportDescription = descriptionView.getText().toString();
			try {
				footprintPositionDao = helper.getFootprintPositionDao();
				publicTransportDao = helper.getPublicTransportDao();
	  	    	footprintDao = helper.getFootprintDao();
	  	    	currentFootprint = footprintDao.queryForId(UUID.fromString(currentFp));
			} catch (SQLException e) {
				e.printStackTrace();
			}

	/**
	 * Eine neue Flugposition wird erstellt.
	 */
		if (editedTransport == null)
		{
			publicTransport = new PublicTransport();
			footprintPosition = new FootprintPosition();
	  		footprintPosition.setId(UUID.randomUUID().toString());
	  		publicTransport.setFootprintPosition(footprintPosition);
	  		footprintPosition.setFootprint(currentFootprint);
	  		publicTransport.setId(UUID.randomUUID().toString());
	  		if (distance != null)
	  	      {
	  	    	publicTransport.setDistance(distance); 
	  	    	distanceOk = true;
	  	      }
			publicTransport.setTransportType(transportTypeSpinner.getSelectedItemPosition());
  			
	  		footprintPosition.setName("Öffentlicher Verkehr");	  		
	  		footprintPosition.setDate((new LocalDateTime()).toString());	  			  		
	  		footprintPosition.setPositionType("OpenResKit.DomainModel.PublicTransport");
	  		footprintPosition.setServerId(93);
	  		footprintPosition.setIconId("CfPublicTransport.png");
	  		footprintPosition.setCarbonFootprintCategoryId("Öffentliche Verkehrsmittel");
			if (footprintCategory != null)
			{
  				if (footprintCategory.equalsIgnoreCase("Carbon Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Carbon");
			        publicTransport.setCalculationCategory("Carbon");
  				}
  				if (footprintCategory.equalsIgnoreCase("Water Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Water");
			        publicTransport.setCalculationCategory("Water");
  				}
			}
			String currentEmployee = preferences.getString("employee", null);
  	  		for (Employee employee : employeeDao) {
  	  			if (employee.getId().equals(currentEmployee)) {
  	  				footprintPosition.setResponsibleSubject(employee);
  	  				employeeOk = true;
  	  			}
  	  		}	 
			if (footprintPosition.getResponsibleSubject() != null)
			{
			   	try 
				{			   		
					footprintPositionDao.createOrUpdate(footprintPosition);
					publicTransportDao.createOrUpdate(publicTransport);
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
			else {
				Toast.makeText(ctx, "Bitte zunächst einen Mitarbeiter in den Einstellungen festlegen.", Toast.LENGTH_LONG).show();
			}
		}
		
		/**
		 * Die bestehenden Daten werden übernommen, wenn der Flug bereits vorhanden war
		 * und zum Editieren geöffnet wurde.
		 */
		else {
				publicTransport = editedTransport;
				distanceOk = true;
				footprintPosition = editedPosition;			
		}
		if (distanceOk)
		{
  	    	if (!publicTransportDescription.equals(""))
  	    	{
  	    		footprintPosition.setDescription(publicTransportDescription);
  	    	}
  	    	else
  	    	{
  	    		footprintPosition.setDescription("ohne Bezeichnung");
  	    	}
	  					
			/**
			 * Ermittlung des Treibhauspotentials bzw. des Wasservebrauchs abhängig von
			 * der Category des Footprints.
			 */						

			ICalculation iCalculation = new  PublicTransportCalculation(publicTransport, helper, footprintPosition);			  				
	  		publicTransport.setEmission(iCalculation.getEmission());			  		
			
	  		int totalFp = currentFootprint.getCalculation();
	  		int currentFp = (int) Math.round(publicTransport.getEmission());
	  		currentFootprint.setCalculation(totalFp+(currentFp/1000));
	  		currentFootprint.setFootprintPositions(footprintPositionList);
			
	  		footprintPositionList.add(footprintPosition);
	  		for (FootprintPosition fPos : currentFootprint.getFootprintPositions())
	  		{
	  			footprintPositionList.add(fPos);
	  		}
	  		/**
	  		 * Persistierung der Daten in der SQLite-DB.
	  		 */
		   	try 
			{	
		   		if (editedTransport == null)
		   		{
					footprintPositionDao.createOrUpdate(footprintPosition);
					publicTransportDao.createOrUpdate(publicTransport);
					footprintDao.createOrUpdate(currentFootprint);
		  		}
		   		else {
		   			footprintPositionDao.update(footprintPosition);
					publicTransportDao.update(publicTransport);
					footprintDao.update(currentFootprint);
		   		}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}		  						
			finish();
      }
      else 
      {
    	  Toast.makeText(ctx, "Distanz oder Treibstoffverbrauch nicht vorhanden!", Toast.LENGTH_LONG).show();
      }					   
  }};
}
