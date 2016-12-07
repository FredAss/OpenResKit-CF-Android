package htw.bui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.DistanceCalculation;
import openreskit.odata.Footprint;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
public class GpsTracking extends Activity {
	private TextView startLatLng;
	private TextView endLatLng;
    private TextView startAddress;
    private TextView endAddress;
    TextView distanceTxt;
    private Button startButton;
    private Button endButton;
    private Button calculateButton;
    private LocationManager mLocationManager;
    private Handler mHandler;
    private boolean mGeocoderAvailable;
    ProgressDialog mProgressDialog;
    Context ctx = this;
	String currentFpString;
	String currentPositionType;
	Footprint currentFootprint;	
	Dao<Distance, UUID> distanceDao;
	Distance distance = new Distance();
	double distanceValue = 0.0;
	DatabaseHelper helper;

    Location startLocation = new Location("start");
    Location endLocation = new Location("end");
	float latStart = 0;
	float lngStart = 0;
	float latEnd = 0;
	float lngEnd = 0;

    private static final int UPDATE_START_ADDRESS = 1;
    private static final int UPDATE_START_LATLNG = 2;
    private static final int UPDATE_END_ADDRESS = 3;
    private static final int UPDATE_END_LATLNG = 4;

    /**
     * Activity zur Darstellung eines Formulars zur Ermittlung einer
     * Entfernung mittels GPS Sensor des Smartphones. Zunächst werden
     * die Buttons zur Ermittlung des Start- und Zielpunktes dargestellt.
     * Ein Handler übernimmt die Darstellung der ermittelten Koordinaten und
     * Addressen in Textfeldern.
     * Siehe:
     * http://developer.android.com/training/basics/location/index.html
     */
    @SuppressLint({ "NewApi", "HandlerLeak" })
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracking);
        Bundle extras = getIntent().getExtras();
		currentFpString = extras.getString("currentFootprint");
		currentPositionType = extras.getString("positionType");

        startLatLng = (TextView) findViewById(R.id.startlatlng);
        startAddress = (TextView) findViewById(R.id.startaddress);
        endLatLng = (TextView) findViewById(R.id.endlatlng);
        endAddress = (TextView) findViewById(R.id.endaddress);
        distanceTxt = (TextView) findViewById(R.id.distance);
    
        startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(setListener);

        endButton = (Button) findViewById(R.id.end);
        endButton.setOnClickListener(setListener);
        
        calculateButton = (Button) findViewById(R.id.calculate);
        calculateButton.setOnClickListener(setListener);
       
        mGeocoderAvailable = true;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_START_ADDRESS:
                        startAddress.setText((String) msg.obj);
                        break;
                    case UPDATE_END_ADDRESS:
                    	endAddress.setText((String) msg.obj);
                        break;
                    case UPDATE_START_LATLNG:
                        startLatLng.setText((String) msg.obj);
                        break;
                    case UPDATE_END_LATLNG:
                    	endLatLng.setText((String) msg.obj);
                        break;
                }
            }
        };
    }
    /**
     * Listener für das Anklicken aller Buttons.
     * Abhängig vom gewählten Button wird ein asynchroner Task geöffnet,
     * in dem die Koordinaten abgefragt werden bzw. in dem die Entfernung zwischen
     * zwei Koordinaten abhängig vom Verkehrsmittel ermittelt wird.
     */
    View.OnClickListener setListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Location gpsLocation = null;
	        Location networkLocation = null;
	        mLocationManager.removeUpdates(listener);
	        gpsLocation = requestUpdatesFromProvider(
                    LocationManager.GPS_PROVIDER, R.string.not_support_gps);
            networkLocation = requestUpdatesFromProvider(
                    LocationManager.NETWORK_PROVIDER, R.string.not_support_network);                
            if(startButton.getId() == ((Button)v).getId() ){                	         	
	            if (gpsLocation != null && networkLocation != null) {
	                updateStartLocation(getBetterLocation(gpsLocation, networkLocation));
	            } else if (gpsLocation != null) {
	                updateStartLocation(gpsLocation);
	            } else if (networkLocation != null) {
	                updateStartLocation(networkLocation);
	            }
            }
            else if(endButton.getId() == ((Button)v).getId() ){                 	
	            if (gpsLocation != null && networkLocation != null) {
	                updateEndLocation(getBetterLocation(gpsLocation, networkLocation));
	            } else if (gpsLocation != null) {
	                updateEndLocation(gpsLocation);
	            } else if (networkLocation != null) {
	                updateEndLocation(networkLocation);
	            }
            }	    
            else if(calculateButton.getId() == ((Button)v).getId() ){           	
            	if (latStart != 0 | latEnd != 0)
 				{
 		  			new GetDistanceTask().execute();	 				
 				}
 				else
 				{
 					Toast.makeText(ctx, "Bitte zuerst Start- und Zielpunkt festlegen!", Toast.LENGTH_LONG).show();
 				}
            }	
        }
    };
   /**
    * Asynchroner Task für Entfernungsermittlung zwischen zwei Koordinaten.
    * Abhängig vom Verkehrsmittel wird die entsprechende Berechnungsfunktion (DistanceCalculator)
    * für die Strecke aufgerufen.
    * Das Ergebnis wird in den entsprechenden Datensatz der Footprint Position in der Datenbank geschrieben.
    * @return Value for distance between two coordinates	          
    */     
  class GetDistanceTask extends AsyncTask<Location, Void, Double> {
      @Override
      protected Double doInBackground(Location... params) {
       		DistanceCalculation distanceCalculator = new DistanceCalculation();
    		GeoPoint start = new GeoPoint((int) latStart, (int) lngStart);
    		GeoPoint end =  new GeoPoint((int) latEnd, (int) lngEnd);
    		helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);  
  			try {		                
                distanceDao = helper.getDistanceDao();
	  		} catch (SQLException e) {					
				e.printStackTrace();
			}

	  			if (currentPositionType.equalsIgnoreCase("flight"))
	  			{
	  				distanceValue = distanceCalculator.getFlightDistance(start, end);
	  			}
	  			else if (currentPositionType.equalsIgnoreCase("car"))
	  			{
	  				distanceValue = distanceCalculator.getRoadDistance(latStart, lngStart, latEnd, lngEnd, ctx);
	  			} 
    	  return distanceValue;
      }
		@Override
		  protected void onPreExecute() {	         
		      mProgressDialog = new ProgressDialog(ctx);
		      mProgressDialog.setMessage("Ermittle Entfernung");
		      mProgressDialog.show();
		  }		        
	      protected void onPostExecute(Double result) 
		 {				        	  
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
				mProgressDialog.dismiss();
				finish();
		 }
	 }
      
	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	
	    }

	    @Override
	    protected void onStart() {
	        super.onStart();
	
	        LocationManager locationManager =
	                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	
	        if (!gpsEnabled) {
	
	        }
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        mLocationManager.removeUpdates(listener);
	    }

    /**
     * Funktion zum Aktualisieren der Position nach einer 10 Metern Bewegung oder 
     * 10 Sekunden Zeit.
     * @param provider
     * @param errorResId
     * @return updated location
     */
    private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
        Location location = null;
        if (mLocationManager.isProviderEnabled(provider)) {
            mLocationManager.requestLocationUpdates(provider, 10000, 10, listener);
            location = mLocationManager.getLastKnownLocation(provider);
        } else {
            Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
        }
        return location;
    }
	    
    /**
     * Funktion zum Setzen der Position zum Beginn einer Strecke. Das
     * Ergebnis wird an den Handler zum Eintragen in das Formular übergeben.
     * @param location
     */
    private void updateStartLocation(Location location) {
    	startLocation.setLatitude(location.getLatitude());
    	startLocation.setLongitude(location.getLongitude());
    	latStart = (float) location.getLatitude();
    	lngStart = (float) location.getLongitude();
    	 Message.obtain(mHandler,
                 UPDATE_START_LATLNG,
                 location.getLatitude() + ", " + location.getLongitude()).sendToTarget();	    	
        if (mGeocoderAvailable) {
        	new ReverseGeocodingStartTask(this).execute(new Location[] {location});
        	}        
    }
    /**
     * Funktion zum Setzen der Position zum Beenden einer Strecke. Das
     * Ergebnis wird an den Handler zum Eintragen in das Formular übergeben.
     * @param location
     */
    private void updateEndLocation(Location location) {
    	endLocation.setLatitude(location.getLatitude());
    	endLocation.setLongitude(location.getLongitude());
       	latEnd = (float) location.getLatitude();
    	lngEnd = (float) location.getLongitude();
    	 Message.obtain(mHandler,
                 UPDATE_END_LATLNG,
                 location.getLatitude() + ", " + location.getLongitude()).sendToTarget();	
        if (mGeocoderAvailable) {
        	new ReverseGeocodingEndTask(this).execute(new Location[] {location});
        }
    }

    /**
     * Standard Methoden für den Location Listener.
     */
    private final LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * Funktion zur Ermittlung der exakteren Location vom Netzwerk-Provider sowie
     * über GPS.
     * @param newLocation
     * @param currentBestLocation
     * @return currentBestLocation
     */
    protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {	           
            return newLocation;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > (1000 * 60 * 2);
        boolean isSignificantlyOlder = timeDelta < -(1000 * 60 * 2);
        boolean isNewer = timeDelta > 0;
       
        if (isSignificantlyNewer) {
            return newLocation;	       
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }
        
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());
      
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }
    /**
     * Funktion zum Vergleichen der Positions-Provider.
     * @param provider1
     * @param provider2
     * @return checkedProviders
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
    /**
     * Asynchroner Task für die Ermittlung des Startpunktes der Strecke. Dieser wird durch einen 
     * Location Provider entweder durch GPS-Navigation oder vom Nerzwerk-Provider geliefert.
     * Das Ergebnis wird an einen Handler übergeben um Koordinaten und Addresse in der Formular
     * einzutragen.
     */
    private class ReverseGeocodingStartTask extends AsyncTask<Location, Void, Boolean> {
        Context mContext;
        public ReverseGeocodingStartTask(Context context) {
            super();
            mContext = context;
        }
        @Override
        protected Boolean doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            Location loc = params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                Message.obtain(mHandler, UPDATE_START_ADDRESS, e.toString()).sendToTarget();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
                Message.obtain(mHandler, UPDATE_START_ADDRESS, addressText).sendToTarget();
            }
            return null;
        }	 
        @Override
        protected void onPreExecute() {	         
            mProgressDialog = new ProgressDialog(ctx);
            mProgressDialog.setMessage("Ermittle Startposition");
            mProgressDialog.show();
        }
        protected void onPostExecute(Boolean result) 
		 {				
			 mProgressDialog.dismiss();
		 }
    }

    /**
     * Asynchroner Task für die Ermittlung des Endpunktes. Dieser wird durch einen 
     * Location Provider entweder durch GPS-Navigation oder vom Nerzwerk-Provider geliefert.
     * Das Ergebnis wird an einen Handler übergeben um Koordinaten und Addresse in der Formular
     * einzutragen.
     */    
    private class ReverseGeocodingEndTask extends AsyncTask<Location, Void, Boolean> {
        Context mContext;
        public ReverseGeocodingEndTask(Context context) {
            super();
            mContext = context;
        }
        @Override
        protected Boolean doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            Location loc = params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                Message.obtain(mHandler, UPDATE_END_ADDRESS, e.toString()).sendToTarget();
                }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());	             
                Message.obtain(mHandler, UPDATE_END_ADDRESS, addressText).sendToTarget();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {	         
            mProgressDialog = new ProgressDialog(ctx);
            mProgressDialog.setMessage("Ermittle Endposition");
            mProgressDialog.show();
        }
        @Override
        protected void onPostExecute(Boolean result) 
		 {				
			 mProgressDialog.dismiss();
		 }
    }

}

