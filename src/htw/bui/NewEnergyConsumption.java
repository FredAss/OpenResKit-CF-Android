package htw.bui;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.Employee;
import openreskit.odata.EnergyConsumption;
import openreskit.odata.EnergyConsumptionCalculation;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.ICalculation;

import org.joda.time.LocalDateTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class NewEnergyConsumption extends OrmLiteBaseActivity<DatabaseHelper> {
	DatabaseHelper helper;
	Context ctx = this;	
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao;
	private Dao<Distance, UUID> distanceDao;
	String currentFp = null;
	Footprint currentFootprint;
	Spinner energyTypeSpinner;
	Spinner distanceType;	
	String footprintCategory = "nichts";
	private boolean resumeHasRun = false;	
	EditText descriptionView;
	EditText consumptionView;	
	SharedPreferences preferences;
	Boolean employeeOk = false;
	Dao<Employee, UUID> employeeDao;
	
    EnergyConsumption editedEnergyConsumption = null;
	FootprintPosition editedPosition = null;	
	EnergyConsumption energyConsumption;		
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
        setContentView(R.layout.newenergyconsumption);
        
        descriptionView = (EditText) findViewById(R.id.description);
        consumptionView = (EditText) findViewById(R.id.consumption);
		
        energyTypeSpinner = (Spinner) findViewById(R.id.energytypes_spinner);
	    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.energytypes_array, android.R.layout.simple_spinner_item);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    energyTypeSpinner.setAdapter(spinnerAdapter);
	    
        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
 	    helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
  	    
		try {
			footprintPositionDao = helper.getFootprintPositionDao();
  	    	footprintDao = helper.getFootprintDao();
  	    	energyConsumptionDao = helper.getEnergyConsumptionDao();
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
  			for (EnergyConsumption energyConsumption2 : energyConsumptionDao)
			{
				if (energyConsumption2.getFootprintPosition().getId().equals(editedPosition.getId()))
					{
						consumptionView.setText(energyConsumption2.getConsumption()+"");
						descriptionView.setText(editedPosition.getName());
						headline.setText("Verbrauch bearbeiten");						
						energyTypeSpinner.setSelection(energyConsumption2.getEnergySource());						
						try {
							editedEnergyConsumption = energyConsumptionDao.queryForId(UUID.fromString(energyConsumption2.getId()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
  				}
			}
  	    }
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
	    	   		Double consumptionValue = distance.getDistance();
	    	   		DecimalFormat twoDForm = new DecimalFormat("#######.");
	    	   		String convertedConsumption = twoDForm.format(consumptionValue);
	    	   		String[] convertedConsumption2 = convertedConsumption.split(",");    	    	   		
			   		consumptionView.setText(convertedConsumption2[0]);
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
	      Double consumption = null;	
	      Boolean consumptionOk = false;
	      EditText consumptionView = (EditText) findViewById(R.id.consumption);	  		  
	      try { 
				 consumption = Double.parseDouble(consumptionView.getText().toString());
			  }
	      catch (NumberFormatException e) {
			  e.printStackTrace();
	      }	  		  		   		  
  		  String publicTransportDescription = descriptionView.getText().toString();
			try {
				footprintPositionDao = helper.getFootprintPositionDao();
				energyConsumptionDao = helper.getEnergyConsumptionDao();
	  	    	footprintDao = helper.getFootprintDao();
	  	    	currentFootprint = footprintDao.queryForId(UUID.fromString(currentFp));
			} catch (SQLException e) {
				e.printStackTrace();
			}

	/**
	 * Eine neue Flugposition wird erstellt.
	 */
		if (editedEnergyConsumption == null)
		{
			energyConsumption = new EnergyConsumption();
			footprintPosition = new FootprintPosition();
	  		footprintPosition.setId(UUID.randomUUID().toString());
	  		energyConsumption.setFootprintPosition(footprintPosition);
	  		footprintPosition.setFootprint(currentFootprint);
	  		energyConsumption.setId(UUID.randomUUID().toString());
	  		if (consumption != null)
	  	      {
	  	    	energyConsumption.setConsumption(consumption); 
	  	    	consumptionOk = true;
	  	      }
			energyConsumption.setEnergySource(energyTypeSpinner.getSelectedItemPosition());
  			
	  		footprintPosition.setName("Energieverbrauch");	  		
	  		footprintPosition.setDate((new LocalDateTime()).toString());	  			  		
	  		footprintPosition.setPositionType("OpenResKit.DomainModel.EnergyConsumption");
	  		footprintPosition.setServerId(93);
	  		footprintPosition.setIconId("CfSite.png");
	  		footprintPosition.setCarbonFootprintCategoryId("Energieverbrauch");
			if (footprintCategory != null)
			{
  				if (footprintCategory.equalsIgnoreCase("Carbon Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Carbon");
			        energyConsumption.setCalculationCategory("Carbon");
  				}
  				if (footprintCategory.equalsIgnoreCase("Water Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Water");
			        energyConsumption.setCalculationCategory("Water");
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
					energyConsumptionDao.createOrUpdate(energyConsumption);
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
				energyConsumption = editedEnergyConsumption;
				consumptionOk = true;
				footprintPosition = editedPosition;			
		}
		if (consumptionOk)
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

			ICalculation iCalculation = new  EnergyConsumptionCalculation(energyConsumption, helper, footprintPosition);			  				
	  		energyConsumption.setEmission(iCalculation.getEmission());			  		
			
	  		int totalFp = currentFootprint.getCalculation();
	  		int currentFp = (int) Math.round(energyConsumption.getEmission());
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
		   		if (editedEnergyConsumption == null)
		   		{
					footprintPositionDao.createOrUpdate(footprintPosition);
					energyConsumptionDao.createOrUpdate(energyConsumption);
					footprintDao.createOrUpdate(currentFootprint);
		  		}
		   		else {
		   			footprintPositionDao.update(footprintPosition);
					energyConsumptionDao.update(energyConsumption);
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
    	  Toast.makeText(ctx, "Kein Verbrauch vorhanden!", Toast.LENGTH_LONG).show();
      }					   
  }};
}
