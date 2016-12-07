package htw.bui;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.DistanceCalculation;
import openreskit.odata.Footprint;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class AirportDistance extends Activity {
    TextView distanceTxt;
    Context ctx = this;
	String currentFpString;
	String currentPositionType;
	Footprint currentFootprint;	
	Dao<Distance, UUID> distanceDao;

	double distanceValue = 0.0;
	DatabaseHelper helper;
	Spinner startAirportsSpinner;
	Spinner endAirportsSpinner;
	ArrayList<String> airports = new ArrayList<String>();
	ArrayList<String> countries = new ArrayList<String>();
	ArrayList<String> latitudes = new ArrayList<String>();
	ArrayList<String> longitudes = new ArrayList<String>();
	GeoPoint start;
	GeoPoint end;

    /**
     * Activity zur Darstellung eines Formulars zur Ermittlung einer
     * Entfernung mittels GPS Sensor des Smartphones. Zunächst werden
     * die Buttons zur Ermittlung des Start- und Zielpunktes dargestellt.
     * Ein Handler übernimmt die Darstellung der ermittelten Koordinaten und
     * Addressen in Textfeldern.
     * Siehe:
     * http://developer.android.com/training/basics/location/index.html
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airportdistance);
        Bundle extras = getIntent().getExtras();
		currentFpString = extras.getString("currentFootprint");
		currentPositionType = extras.getString("positionType");

		try {
			final InputStream excelFile = getResources().getAssets().open("airports.xls");

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(excelFile);
 
            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
 
            // Get the first sheet from workbook
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
                    	airports.add(myCell.toString());
                     break;
                    case 1:
                    	countries.add(myCell.toString());
                     break;
                    case 2:
                    	latitudes.add(myCell.toString());
                     break;
                    case 3:
                    	longitudes.add(myCell.toString());
                     break;
                    default:
                     break;
                    }
                    	
                    
                }}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		startAirportsSpinner = (Spinner) findViewById(R.id.startairport);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, airports);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		startAirportsSpinner.setAdapter(spinnerArrayAdapter);
		endAirportsSpinner = (Spinner) findViewById(R.id.endairport);
		ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, airports);
		spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		endAirportsSpinner.setAdapter(spinnerArrayAdapter2);
 
	    startAirportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1,
	                int position, long arg3) {	
	        	if (position!=0)
	        	{
		       	 	TextView startCountry = (TextView) findViewById(R.id.startcountry);
		       	 	TextView startLatText = (TextView) findViewById(R.id.startlat);
		       	 	TextView startLngText = (TextView) findViewById(R.id.startlng);
		       	 	startCountry.setText(countries.get(position));
		       	 	startLatText.setText(latitudes.get(position));
		       	 	startLngText.setText(longitudes.get(position));
		       	 	Double latitude = Double.parseDouble(latitudes.get(position));
		       	 	Double longitude = Double.parseDouble(longitudes.get(position));
		       	 	start = new GeoPoint((int)(latitude * 1e6),
                            (int)(longitude * 1e6));
	        	}
	        }
	        @Override
	        public void onNothingSelected(AdapterView<?> arg0) {	            
	        
	        }
	    });
	    endAirportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1,
	                int position, long arg3) {	
	        	if (position!=0)
	        	{
		       	 	TextView endCountry = (TextView) findViewById(R.id.endcountry);
		       	 	TextView endLatText = (TextView) findViewById(R.id.endlat);
		       	 	TextView endLngText = (TextView) findViewById(R.id.endlng);
		       	 	endCountry.setText(countries.get(position));
		       	 	endLatText.setText(latitudes.get(position));
		       	 	endLngText.setText(longitudes.get(position));
		       	 	Double latitude = Double.parseDouble(latitudes.get(position));
		       	 	Double longitude = Double.parseDouble(longitudes.get(position));
		       	 	end = new GeoPoint((int)(latitude * 1e6),
                            (int)(longitude * 1e6));
	        	}
	        }
	        @Override
	        public void onNothingSelected(AdapterView<?> arg0) {	            
	        
	        }
	    });
	     Button acceptButton = (Button) findViewById(R.id.calculate);
	     acceptButton.setOnClickListener(acceptListener);
	     
    }
    View.OnClickListener acceptListener = new View.OnClickListener() {
	  	  public void onClick(View v) {	          
          	if (start != null | end != null)
				{
        		helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);  
      			try {		                
                    distanceDao = helper.getDistanceDao();
    	  		} catch (SQLException e) {					
    				e.printStackTrace();
    			}
    	  		DistanceCalculation distanceCalculator = new DistanceCalculation();

        	    distanceValue = distanceCalculator.getFlightDistance(start, end);
    	  		Distance distance = new Distance();
		    	distance.setId(currentFpString);
		    	distance.setDistance(distanceValue);
			  	try 
				{
					distanceDao.createOrUpdate(distance);
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				finish();
				}
				else
				{
					Toast.makeText(ctx, "Bitte zuerst Start- und Zielpunkt festlegen!", Toast.LENGTH_LONG).show();
				}
	  	  }
    };
}
