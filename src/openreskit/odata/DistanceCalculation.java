package openreskit.odata;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class DistanceCalculation {	ProgressDialog mProgressDialog;
	Context context;
	Double resultDistance = 0.0;

	/**
	 * Liefert die Straßenentfernung zwischen zwei Geopunkten basierend auf Google Maps.
	 * Hierfür wird die Entfernung vom Google Server abgefragt. Die Antwort ist eine XML Datei 
	 * aus der die Entfernung geparsed wird, welche die Funktion als Rückgabewert liefert.
	 * Siehe:
	 * http://androidpallikoodam.wordpress.com/2012/04/23/getting-the-distance-between-locations-using-google-maps-api/
	 * http://www.vogella.com/articles/AndroidXML/article.html	
	 * @param latStart
	 * @param lngStart
	 * @param latEnd
	 * @param lngEnd
	 * @param ctx
	 * @return road distance from google maps between two coordinates
	 */
	public Double getRoadDistance(Float latStart, Float lngStart, Float latEnd, Float lngEnd, Context ctx) {
		context = ctx;    	
    	String url = "http://maps.google.com/maps/api/directions/xml?origin=" + latStart + "," + lngStart + "&destination=" + latEnd + "," + lngEnd + "&sensor=false&units=metric";
    	HttpResponse response = null;
    	try {
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpContext localContext = new BasicHttpContext();
    	HttpPost httpPost = new HttpPost(url);
    	response = httpClient.execute(httpPost, localContext);
    	InputStream is = response.getEntity().getContent();

         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
         factory.setNamespaceAware(true);
         XmlPullParser xpp = factory.newPullParser();
         InputStreamReader reader = new InputStreamReader(is);
         xpp.setInput(reader);
         int eventType = xpp.getEventType();
         while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("status"))
        	{
        		if (xpp.nextText().equalsIgnoreCase("OK"))
        		{
        			while (eventType != XmlPullParser.END_DOCUMENT) {
            			if(eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("distance")) {
          	     	         xpp.nextTag();
          	     	         Double nextDistance = Double.parseDouble(xpp.nextText())/1000;
          	     	         if (resultDistance < nextDistance)
          	     	         {
          	     	        	 resultDistance = nextDistance;  
          	     	         }  
            			}
            		eventType = xpp.next();              
        			}
        		}
        		else {
        			Toast.makeText(ctx, "Keine Straße zwischen Start und Ziel ermittelbar.", Toast.LENGTH_LONG).show();
        			break;
        		}
        	}	          
          eventType = xpp.next();
         }
    	} catch (Exception e) {
    	   e.printStackTrace();
    	}
		return resultDistance;
	}
	/**
	 * Liefert den Kreisbogen als Flugentfernung zwischen zwei Geopunkten.
	 * Hierfür werden die Koordinaten entsprechend des Erdradius zur Berechnung eines
	 * Kreisbogens verwendet. Das Ergebnis ist ein WGS84 Ellipsoid, welches die Funktion
	 * als Rückgabewert liefert.
	 * Siehe:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * http://stackoverflow.com/questions/8725283/distance-between-geopoints
	 * @param start
	 * @param end
	 * @return Value for flight Distance between two coordinates.
	 */
		@SuppressLint("UseValueOf")
		public Double getFlightDistance (GeoPoint start, GeoPoint end) 
		{
		    double earthRadius = 6371;
		    double dLat = Math.toRadians((end.getLatitudeE6()/1E6) - (start.getLatitudeE6()/1E6));
		    double dLng = Math.toRadians((end.getLongitudeE6()/1E6) - (start.getLongitudeE6()/1E6));
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(Math.toRadians(start.getLatitudeE6()/1E6)) * Math.cos(Math.toRadians(end.getLatitudeE6()/1E6)) *
		    Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double dist = earthRadius * c;
		    
		    Location startLocation = new Location(""); 
		    startLocation.setLatitude(start.getLatitudeE6() / 1E6);
		    startLocation.setLongitude(start.getLongitudeE6() / 1E6);

		    Location endLocation = new Location("");
		    endLocation.setLatitude(end.getLatitudeE6() / 1E6);
		    endLocation.setLongitude(end.getLongitudeE6() / 1E6);
		    
//		    float distance = startLocation.distanceTo(endLocation);
//		    Double length = (double) Math.round(distance/1000);
//		    int meterConversion = 1609;
		    Double length = (double) (Math.round(dist));
		    return length;		
//		    return length;
	     }	 
		
}
