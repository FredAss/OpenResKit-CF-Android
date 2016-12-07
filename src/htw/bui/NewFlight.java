package htw.bui;


import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.Employee;
import openreskit.odata.Flight;
import openreskit.odata.FlightCalculation;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.ICalculation;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

/**
 * Activity f�r die Darstellung eines Formulars zur Eingabe der Daten eines
 * zur�ckgelegten Fluges und anschlie�enden Berechnung der Emissionen/ des
 * Wasserverbrauchs auf Basis von Emissionsfaktoren. Schlie�lich wird der
 * Flug als Footprint Position gespeichert.
 */
public class NewFlight extends OrmLiteBaseActivity<DatabaseHelper>  {
	DatabaseHelper helper;
	Context ctx = this;	
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<Flight, UUID> flightsDao;
	private Dao<Distance, UUID> distanceDao;
	Dao<Employee, UUID> employeeDao;
	String currentFp = null;
	Footprint currentFootprint;
	Spinner flightTypeSpinner;
	Spinner distanceType;	
	String footprintCategory = "nichts";
	private boolean resumeHasRun = false;	
	CheckBox rfCheck;
	EditText descriptionView;
	EditText distanceView;	
	Flight editedFlight = null;
	FootprintPosition editedPosition = null;	
	Flight flight;		
	FootprintPosition footprintPosition;	
	SharedPreferences preferences;
	Boolean employeeOk = false;
		
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
        setContentView(R.layout.newflight);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        
        rfCheck = (CheckBox) findViewById(R.id.rfcheck);
        descriptionView = (EditText) findViewById(R.id.description);
        distanceView = (EditText) findViewById(R.id.distance);
        distanceView.setEnabled(false);
		
        flightTypeSpinner = (Spinner) findViewById(R.id.flighttypes_spinner);
	    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.flighttypes_array, android.R.layout.simple_spinner_item);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    flightTypeSpinner.setAdapter(spinnerAdapter);
	     
		distanceType = (Spinner) findViewById(R.id.distancetypes_spinner);
	    ArrayAdapter<CharSequence> distanceSpinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.distancetypes_array, android.R.layout.simple_spinner_item);
	    distanceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    distanceType.setAdapter(distanceSpinnerAdapter);
	      
  	    helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
  	    
		try {
			footprintPositionDao = helper.getFootprintPositionDao();
  	    	footprintDao = helper.getFootprintDao();
  	    	flightsDao = helper.getFlightsDao();
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
		 * Wurde das Formular zum Editieren einer bestehenden Flug-Position ge�ffnet wurde,
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
  			for (Flight flight2 : flightsDao)
			{
				if (flight2.getFootprintPosition().getId().equals(editedPosition.getId()))
					{
						distanceView.setText((flight2.getDistance())+"");
						descriptionView.setText(editedPosition.getName());
						headline.setText("Flug bearbeiten");
						if (flight2.getRadiativeForcing())
						{
							rfCheck.setChecked(true);
						}
						if (flight2.getmFlighType() < 3)
						{
							flightTypeSpinner.setSelection(flight2.getmFlighType());	
						}
						try {
							editedFlight = flightsDao.queryForId(UUID.fromString(flight2.getId()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
  				}
			}
  	    }
  	    
    	/**
    	 * Listener f�r die Festlegung der Eingabeart der Distanz. Es kann zwischen der
    	 * Direkteingabe einer Entfernung in km, der Ermittlung mittels GPS-Tracking sowie
    	 * durch die Markierung des Starts und Ziels in Google Maps gew�hlt werden.
    	 */
	     distanceType.setOnItemSelectedListener(new OnItemSelectedListener() {
	         @Override
	         public void onItemSelected(AdapterView<?> arg0, View arg1,
	                 int position, long arg3) {	             
//	        	 int choosenDistanceType = distanceType.getSelectedItemPosition();    
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
		        	 mapIntent.putExtra("positionType", "flight");
		        	 startActivity(mapIntent);
	        	 }
	        	 else if (position == 3)
	        	 {
		           	 Intent gpsIntent = new Intent(ctx, GpsTracking.class);
		           	 gpsIntent.putExtra("currentFootprint", currentFp);
		           	 gpsIntent.putExtra("positionType", "flight");
		        	 startActivity(gpsIntent);
	        	 }	
	        	 else if (position == 4)
	        	 {
		           	 Intent airportIntent = new Intent(ctx, AirportDistance.class);
		           	 airportIntent.putExtra("currentFootprint", currentFp);
		           	 airportIntent.putExtra("positionType", "flight");
		        	 startActivity(airportIntent);
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
     * Listener f�r das Schlie�en des Formulars. Dabei werden die in das Formular eingetragenen
     * Werte aus den View Elementen abgefragt und in die SQLite-DB geschrieben. Im Falle, dass
     * das Formular f�r das Editieren eines bestehenden Fluges ge�ffnet wurde, werden die Daten
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
  		  String flightDescription = descriptionView.getText().toString();
			try {
				footprintPositionDao = helper.getFootprintPositionDao();
				flightsDao = helper.getFlightsDao();
	  	    	footprintDao = helper.getFootprintDao();
	  	    	currentFootprint = footprintDao.queryForId(UUID.fromString(currentFp));
			} catch (SQLException e) {
				e.printStackTrace();
			}

	/**
	 * Eine neue Flugposition wird erstellt.
	 */
		if (editedFlight == null)
		{
			flight = new Flight();
			footprintPosition = new FootprintPosition();
	  		footprintPosition.setId(UUID.randomUUID().toString());
	  		flight.setFootprintPosition(footprintPosition);
	  		footprintPosition.setFootprint(currentFootprint);
	  		flight.setId(UUID.randomUUID().toString());
	  		flight.setRadiativeForcing(false);
	  	    if (distance != null)
	  	      {
	  	    	flight.setDistance(distance); 
	  	    	distanceOk = true;
	  	      }
			flight.setmFlighType(flightTypeSpinner.getSelectedItemPosition());
			if (rfCheck.isChecked())
  			{				    	   	
	  			flight.setRadiativeForcing(true);
  			}
	  		footprintPosition.setName("Flug");	  		
	  		footprintPosition.setDate((new LocalDateTime()).toString());	  			  		
	  		footprintPosition.setPositionType("OpenResKit.DomainModel.Flight");
	  		footprintPosition.setServerId(93);
	  		footprintPosition.setIconId("CfFlight.png");
	  		footprintPosition.setCarbonFootprintCategoryId("Fl�ge");
			if (footprintCategory != null)
			{
  				if (footprintCategory.equalsIgnoreCase("Carbon Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Carbon");
			        flight.setCalculationCategory("Carbon");
  				}
  				if (footprintCategory.equalsIgnoreCase("Water Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Water");
			        flight.setCalculationCategory("Water");
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
					flightsDao.createOrUpdate(flight);
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
			else {
				Toast.makeText(ctx, "Bitte zun�chst einen Mitarbeiter in den Einstellungen festlegen.", Toast.LENGTH_LONG).show();
			}

		}
		
		/**
		 * Die bestehenden Daten werden �bernommen, wenn der Flug bereits vorhanden war
		 * und zum Editieren ge�ffnet wurde.
		 */
		else {
				flight = editedFlight;
				distanceOk = true;
				footprintPosition = editedPosition;			
		}
		if (distanceOk)
		{
			if (employeeOk) {
	  	    	if (!flightDescription.equals(""))
	  	    	{
	  	    		footprintPosition.setDescription(flightDescription);
	  	    	}
	  	    	else
	  	    	{
	  	    		footprintPosition.setDescription("ohne Bezeichnung");
	  	    	}
		  					
				/**
				 * Ermittlung des Treibhauspotentials bzw. des Wasservebrauchs abh�ngig von
				 * der Category des Footprints.
				 */						
	
				ICalculation iCalculation = new  FlightCalculation(flight, helper, footprintPosition);			  				
		  		flight.setEmission(iCalculation.getEmission());			  		
				
		  		int totalFp = currentFootprint.getCalculation();
		  		int currentFp = (int) Math.round(flight.getEmission());
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
			   		if (editedFlight == null)
			   		{
						footprintPositionDao.createOrUpdate(footprintPosition);
						flightsDao.createOrUpdate(flight);
						footprintDao.createOrUpdate(currentFootprint);
			  		}
			   		else {
			   			footprintPositionDao.update(footprintPosition);
						flightsDao.update(flight);
						footprintDao.update(currentFootprint);
			   		}
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}		  						
				finish();
			}
	  }
      else 
      {
    	  Toast.makeText(ctx, "Keine Distanz vorhanden!", Toast.LENGTH_LONG).show();
      }					   
  }};
}
