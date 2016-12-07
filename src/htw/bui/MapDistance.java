package htw.bui;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Distance;
import openreskit.odata.DistanceCalculation;
import openreskit.odata.Flight;
import openreskit.odata.Footprint;
import openreskit.odata.FootprintPosition;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


/**
 * Activity zur Darstellung einer Google Karte. Auf dieser kann ein Start-
 * und ein Endpunkt definiert werden, zwischen denen anschließend eine Linie
 * gezogen wird und die Entfernung schließlich abhängig vom gewählten Verkehrs-
 * mittel berechnet wird.
 * Siehe
 * http://stackoverflow.com/questions/2023669/j2me-android-blackberry-driving-directions-route-between-two-locations/2023685#2023685
 * http://stackoverflow.com/questions/9804001/get-path-and-distance-from-my-current-location-to-user-input-location
 */
public class MapDistance extends MapActivity implements LocationListener {
	 Context context = this;
	 private MapView mapView;
	 MapController mc;
	 Boolean start = true;
	 private DatabaseHelper helper;
	 String currentFpString;
	 String currentPositionType;
	 Footprint currentFootprint;
	 private Dao<Distance, UUID> distanceDao;
	 Distance distance = new Distance();
	 double distanceValue = 0.0;
	 ProgressDialog mProgressDialog;
	 Context ctx = this;

	 FootprintPosition footprintPosition;
	 Flight currentFlight;
	 float latStart = 0;
	 float lngStart = 0;
	 float latEnd = 0;
	 float lngEnd = 0;
	 GeoPoint from;
	 GeoPoint to;
	 
	 Button endButton;
	 Button acceptButton;
	 
	 /**
	  * Methode zur Erstellung des Formulars mit einem Start-, einem
	  * Endbutton sowie einem Speicherbuttons mit den entsprechenden
	  * Listenern die Geopunkte festlegen, die Strecke einzeichnen
	  * und die Funktion zur Berechnung der Entfernung startet. 
	  */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 Bundle extras = getIntent().getExtras();
		 currentFpString = extras.getString("currentFootprint");
		 currentPositionType = extras.getString("positionType");
		 
		 setContentView(R.layout.mapdistance);
		 		 
		 mapView = (MapView) findViewById(R.id.mapv);
		 mc = mapView.getController();
		 mapView.setBuiltInZoomControls(true);
		 		 
		 Button startButton = (Button) findViewById(R.id.strbutton);
		 startButton.setOnClickListener(startListener);
		 
		 endButton = (Button) findViewById(R.id.endbutton);
		 endButton.setOnClickListener(endListener);
		 
		 acceptButton = (Button) findViewById(R.id.acceptdistance);
		 acceptButton.setOnClickListener(acceptListener);
		 
		 Toast.makeText(ctx, "Bitte \"Start festlegen\" klicken und anschließend Startpunkt auf der Karte anwählen", Toast.LENGTH_LONG).show();
		 
	  }
	  View.OnClickListener startListener = new View.OnClickListener() {
	  	  public void onClick(View v) {
	  		 start = true;
	 		 StartOverlay mapOverlay = new StartOverlay();
	         List<Overlay> listOfOverlays = mapView.getOverlays();
	         listOfOverlays.clear();
	         listOfOverlays.add(mapOverlay);
	         mapView.invalidate();			    
	  	  }
	  };
	  View.OnClickListener endListener = new View.OnClickListener() {
	  	  public void onClick(View v) {
	  		 start= false;
	 		 StartOverlay mapOverlay = new StartOverlay();
	         List<Overlay> listOfOverlays = mapView.getOverlays();
	         listOfOverlays.clear();
	         listOfOverlays.add(mapOverlay);   
	         mapView.invalidate();
		   }
	  };
	  View.OnClickListener acceptListener = new View.OnClickListener() {
	  	  public void onClick(View v) {
	  		 if (latStart != 0 | latEnd != 0)
			 {
	  		 	 new GetDistanceTask().execute();	 				
			 }
			 else
			 {
				 Toast.makeText(context, "Bitte zuerst Start- und Zielpunkt festlegen!", Toast.LENGTH_SHORT).show();
			 }
	  	  }
	  };
	  
	   /** 
	    * Async Task für die die Ermittlung der Entfernung abhängig vom Verkehrsmittel     
	    * mit dem sie zurückgelegt wurde.
	    * @return distance between two geopoints
	    */
     class GetDistanceTask extends AsyncTask<Location, Void, Double> {
         @Override
         protected Double doInBackground(Location... params) {
          		DistanceCalculation distanceCalculator = new DistanceCalculation();
       		helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);  
 	  			try {		                
 	                distanceDao = helper.getDistanceDao();
 		  		} catch (SQLException e) {					
 					e.printStackTrace();
 				}
	
		  			if (currentPositionType.equalsIgnoreCase("flight"))
		  			{
		  				distanceValue = distanceCalculator.getFlightDistance(from, to);
		  			}
		  			else if (currentPositionType.equalsIgnoreCase("car"))
		  			{
		  				distanceValue = distanceCalculator.getRoadDistance(latStart, lngStart, latEnd, lngEnd, context);
		  			} 
       	  return distanceValue;
         }
     	@Override
         protected void onPreExecute() {	         
             mProgressDialog = new ProgressDialog(context);
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
     
    /**
     * Funktion die auf der Google Karte eine Linie, die etwa der zurückgelegten
     * Strecke zwischen dem definierten Start- und Endpunkt entspricht.
     * http://stackoverflow.com/questions/7975862/returning-a-geopoint-on-user-touch-mapview-android
     * http://stackoverflow.com/questions/4903004/connect-points-on-map-with-lines/4903756#4903756
     */
    class StartOverlay extends Overlay
    {
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {           	
       	 GeoPoint p;
           
            if (event.getAction() == 1) {                
                p = mapView.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY());
           	 
                if (start){
                	TextView startTv = (TextView) findViewById(R.id.strtxt);
	                startTv.setText(p.getLatitudeE6() / 1E6 + "," + 
	                        p.getLongitudeE6() /1E6);
	                latStart = (float) (p.getLatitudeE6() / 1E6);
	                lngStart = (float) (p.getLongitudeE6() /1E6);		
	                from = p;
	                endButton.setVisibility(View.VISIBLE);
	   	         	Toast.makeText(ctx, "Startpunkt ok. \"Ende festlegen\" und anschließend Zielpunkt auf der Karte wählen.", Toast.LENGTH_LONG).show();
                }
                else {
                	TextView startTv = (TextView) findViewById(R.id.endtxt);
	                startTv.setText(p.getLatitudeE6() / 1E6 + "," + 
	                        p.getLongitudeE6() /1E6);
	                
	                latEnd = (float) (p.getLatitudeE6() / 1E6);
	                lngEnd = (float) (p.getLongitudeE6() /1E6);		
	                to = p;
			        MapOverlay mapOvlay = new MapOverlay(from, to);
			        mapView.getOverlays().add(mapOvlay);
			        acceptButton.setVisibility(View.VISIBLE);
			        Toast.makeText(ctx, "Route festgelegt. \"Bestätigen\" um die Distanz zu berechnen.", Toast.LENGTH_SHORT).show();
                }


            }                            
            return false;
        }  
    }
	    @Override
	    protected boolean isRouteDisplayed() {
	        return false;
	    }
		@Override
		public void onLocationChanged(Location arg0) {
			
		}
		@Override
		public void onProviderDisabled(String arg0) {
			
		}
		@Override
		public void onProviderEnabled(String arg0) {
			
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			
		}
		 public class MapOverlay extends com.google.android.maps.Overlay {		 
	        protected MapOverlay(GeoPoint gp1, GeoPoint gp2 ) {
	           from = gp1;
	           to = gp2;           
        }
        /**
         * Zeichnen der Linie
         * @return true if paint was succesfull
         */
        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
              long when) {
           super.draw(canvas, mapView, shadow);
           if (from != null | to != null)
           {
           Paint paint;
           paint = new Paint();
           paint.setColor(Color.RED);
           paint.setAntiAlias(true);
           paint.setStyle(Style.STROKE);
           paint.setStrokeWidth(2);
           Point pt1 = new Point();
           Point pt2 = new Point();
           Projection projection = mapView.getProjection();
           projection.toPixels(from, pt1);
           projection.toPixels(to, pt2);
           canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);          
           }
           return true;
        }
	 }
}