package htw.bui;


import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import openreskit.odata.Car;
import openreskit.odata.CarCalculation;
import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.Employee;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import openreskit.odata.ICalculation;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class NewCar extends OrmLiteBaseActivity<DatabaseHelper>  {

	DatabaseHelper helper;
	Context ctx = this;	
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<Car, UUID> carsDao;
	private Dao<Distance, UUID> distanceDao;
	String currentFp = null;
	Footprint currentFootprint;
	Spinner fuelTypeSpinner;
	Spinner distanceType;	
	String footprintCategory = "nichts";
	private boolean resumeHasRun = false;	
	EditText descriptionView;
	EditText distanceView;	
    EditText consumptionView;
	Car editedCar = null;
	FootprintPosition editedPosition = null;	
	Car car;		
	FootprintPosition footprintPosition;	
	SharedPreferences preferences;
	Boolean employeeOk = false;
	Dao<Employee, UUID> employeeDao;
	
	ArrayList<String> models = new ArrayList<String>();
	ArrayList<String> fuelType = new ArrayList<String>();
	ArrayList<String> consumption = new ArrayList<String>();
	Spinner modelsSpinner;
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
        setContentView(R.layout.newcar);
        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        
        //Auslesen der Verbrauchswerte und des Treibstofftyps aus der Excelliste, wenn ein Modell angegeben wurde
        try {
			final InputStream excelFile = getResources().getAssets().open("carmodels.xls");
           
            POIFSFileSystem myFileSystem = new POIFSFileSystem(excelFile);
             
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 
            /** We now need something to iterate through the cells.**/
            Iterator<Row> rowIter = mySheet.rowIterator();
            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<Cell> cellIter = myRow.cellIterator();
                while(cellIter.hasNext()){
                	HSSFCell myCell = (HSSFCell) cellIter.next();
                    switch (myCell.getColumnIndex()) {
                    case 0:
                    	models.add(myCell.toString());
                     break;
                    case 1:
                    	if (myCell.toString().equalsIgnoreCase("0.0"))
                    	{
                    		fuelType.add("0");
                    	}
                    	else if (myCell.toString().equalsIgnoreCase("1.0"))
                    	{
                    		fuelType.add("1");
                    	}
                    	else {
                    		fuelType.add(myCell.toString());
                    	}
                     break;
                    case 2:
                    	consumption.add(myCell.toString());
                     break;
                    default:
                     break;
                    }   
                }}
		} catch (IOException e) {
			e.printStackTrace();
		} 
        
		modelsSpinner = (Spinner) findViewById(R.id.carmodels_spinner);
		ArrayAdapter<String> modelsSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, models);
		modelsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		modelsSpinner.setAdapter(modelsSpinnerAdapter);
		
        descriptionView = (EditText) findViewById(R.id.description);
        distanceView = (EditText) findViewById(R.id.distance);
        consumptionView = (EditText) findViewById(R.id.consumption);
        distanceView.setEnabled(false);
		
        fuelTypeSpinner = (Spinner) findViewById(R.id.fueltypes_spinner);
	    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.fueltypes_array, android.R.layout.simple_spinner_item);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    fuelTypeSpinner.setAdapter(spinnerAdapter);
	     
		distanceType = (Spinner) findViewById(R.id.distancetypes_spinner);
	    ArrayAdapter<CharSequence> distanceSpinnerAdapter = ArrayAdapter.createFromResource(this,
	    R.array.distancetypes_array, android.R.layout.simple_spinner_item);
	    distanceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    distanceType.setAdapter(distanceSpinnerAdapter);
	      
  	    helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
  	    
		try {
			footprintPositionDao = helper.getFootprintPositionDao();
  	    	footprintDao = helper.getFootprintDao();
  	    	carsDao = helper.getCarDao();
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
  			for (Car car2 : carsDao)
			{
				if (car2.getFootprintPosition().getId().equals(editedPosition.getId()))
					{
						distanceView.setText((car2.getDistance())+"");
						descriptionView.setText(editedPosition.getName());
						headline.setText("Fahrt bearbeiten");
						consumptionView.setText(car2.getConsumption()+"");
						fuelTypeSpinner.setSelection(car2.getFuel());						
						try {
							editedCar = carsDao.queryForId(UUID.fromString(car2.getId()));
						} catch (SQLException e) {
							e.printStackTrace();
						}
  				}
			}
  	    }
  	    
  	    /**
  	     * Listener für die Modellauswahl, die Werte zu dem angewählten Modell
  	     * werden aus der Excelliste gezogen und in das Eingabeformular eingetragen,
  	     * so dass der Nutzer diese Werte nichtmehr eingeben muss.
  	     */
  	    
  	  modelsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1,
	                int position, long arg3) {	
	        	if (position!=0)
	        	{
	        		fuelTypeSpinner.setSelection(Integer.parseInt(fuelType.get(position)));			
	        		consumptionView.setText(consumption.get(position));
	        	}
	        }
	        @Override
	        public void onNothingSelected(AdapterView<?> arg0) {	            
	        
	        }
	    });
  	    
    	/**
    	 * Listener für die Festlegung der Eingabeart der Distanz. Es kann zwischen der
    	 * Direkteingabe einer Entfernung in km, der Ermittlung mittels GPS-Tracking sowie
    	 * durch die Markierung des Starts und Ziels in Google Maps gewählt werden.
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
	      Double consumption = null;
	      Boolean distanceOk = false;
	      Boolean consumptionOk = false;
	      EditText distanceView = (EditText) findViewById(R.id.distance);	  		  
	      try { 
				 distance = Double.parseDouble(distanceView.getText().toString());
				 consumption = Double.parseDouble(consumptionView.getText().toString());
	      }
	      catch (NumberFormatException e) {
	    	  Toast.makeText(ctx, "Bitte Distanz und Verbrauch angeben", Toast.LENGTH_LONG).show();
			  e.printStackTrace();
			  return;
	      }	  		  		   		  
  		  String carDescription = descriptionView.getText().toString();
			try {
				footprintPositionDao = helper.getFootprintPositionDao();
				carsDao = helper.getCarDao();
	  	    	footprintDao = helper.getFootprintDao();
	  	    	currentFootprint = footprintDao.queryForId(UUID.fromString(currentFp));
			} catch (SQLException e) {
				e.printStackTrace();
			}

	/**
	 * Eine neue Flugposition wird erstellt.
	 */
		if (editedCar == null)
		{
			car = new Car();
			footprintPosition = new FootprintPosition();
	  		footprintPosition.setId(UUID.randomUUID().toString());
	  		car.setFootprintPosition(footprintPosition);
	  		footprintPosition.setFootprint(currentFootprint);
	  		car.setId(UUID.randomUUID().toString());
	  		if (distance != null)
	  	      {
	  	    	car.setDistance(distance); 
	  	    	distanceOk = true;
	  	      }
			else {
				Toast.makeText(ctx, "Distanz muss angegeben werden.", Toast.LENGTH_LONG).show();
				return;
			}
			car.setFuel(fuelTypeSpinner.getSelectedItemPosition());
			if (consumption != null && consumption != 0)
			{
	    	   	car.setConsumption(consumption);
	    	   	consumptionOk = true;
			}
			else {
				Toast.makeText(ctx, "Verbrauch muss angegeben werden.", Toast.LENGTH_LONG).show();
				return;
			}
  			
			
	  		footprintPosition.setName("Fahrzeug");	  		
	  		footprintPosition.setDate((new LocalDateTime()).toString());	  			  		
	  		footprintPosition.setPositionType("OpenResKit.DomainModel.Car");
	  		footprintPosition.setServerId(93);
	  		footprintPosition.setIconId("CfCar.png");
	  		footprintPosition.setCarbonFootprintCategoryId("Autofahrten");
			if (footprintCategory != null)
			{
  				if (footprintCategory.equalsIgnoreCase("Carbon Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Carbon");
			        car.setCalculationCategory("Carbon");
  				}
  				if (footprintCategory.equalsIgnoreCase("Water Footprint"))
  				{
			        currentFootprint.setCalculationCategory("Water");
			        car.setCalculationCategory("Water");
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
					carsDao.createOrUpdate(car);
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
				car = editedCar;
				distanceOk = true;
				consumptionOk = true;
				footprintPosition = editedPosition;			
		}
		if (distanceOk && consumptionOk)
		{
  	    	if (!carDescription.equals(""))
  	    	{
  	    		footprintPosition.setDescription(carDescription);
  	    	}
  	    	else
  	    	{
  	    		footprintPosition.setDescription("ohne Bezeichnung");
  	    	}
	  					
			/**
			 * Ermittlung des Treibhauspotentials bzw. des Wasservebrauchs abhängig von
			 * der Category des Footprints.
			 */						

			ICalculation iCalculation = new  CarCalculation(car, helper, footprintPosition);			  				
	  		car.setEmission(iCalculation.getEmission());			  		
			
	  		int totalFp = currentFootprint.getCalculation();
	  		int currentFp = (int) Math.round(car.getEmission());
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
		   		if (editedCar == null)
		   		{
					footprintPositionDao.createOrUpdate(footprintPosition);
					carsDao.createOrUpdate(car);
					footprintDao.createOrUpdate(currentFootprint);
		  		}
		   		else {
		   			footprintPositionDao.update(footprintPosition);
					carsDao.update(car);
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
