package openreskit.odata;

import htw.bui.FootprintList;
import htw.bui.Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
/**
 * Funktion zur Durchführung der Datensynchronisation mit einem OpenResKit-Hub.
 * Dies beinhaltet eine Upload- und eine Download-Funktion.
 *
 */
public class Synchronization {

	Context ctx;
	private DatabaseHelper helper;
	private Dao<Footprint, UUID> footprintDao;
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Flight, UUID> flightDao;
	private Dao<Car, UUID> carDao;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao;
	private Dao<PublicTransport, UUID> publicTransportDao;	
	Dao<Employee, UUID> employeeDao;
	ArrayList<FootprintPosition> globalFootprintPositions;
	ObjectMapper mapper = new ObjectMapper(); 	 
	FootprintList act;
	private ProgressDialog pdia;
	String jsonText = null;
	String jsonEmployeeText = null;
	JSONArray footprintData = null;
	Boolean uploadOk = false;
	SharedPreferences prefs;
	String serverIp = null;
	Boolean inetTest = false;
	
	
	public Synchronization() {}
	
	/**
	 * Methode zum Aufrufen des asynchronen Tasks zum Herunterladen der Daten von einem Server.
	 * @param context
	 * @param activity
	 */
	public void getData(Context context, FootprintList activity) {
		
		ctx = context;
		act = activity;
  	    helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
  	    prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
  	    if (!prefs.getString("ip", "").equalsIgnoreCase(""))
  	    {
  	    	serverIp = prefs.getString("ip", "");
  	    }
  	    else {
  	    	serverIp = "141.45.165.252";
  	    }
		if (isOnline() ) 
		{
			new GetFootprints().execute((Void[])null);	
		}
		else
		{
			Toast.makeText(ctx, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
		}
		
	}
	/**
	 * Methode zum Aufrufen des asynchronen Tasks zum Hochladen von Daten zu einem Server.
	 * @param context
	 * @param databaseHelper
	 */
	public void writeData(Context context, DatabaseHelper databaseHelper) 
	{
		ctx = context;
		helper = databaseHelper;
  	    prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
  	    if (!prefs.getString("ip", "").equalsIgnoreCase(""))
  	    {
  	    	serverIp = prefs.getString("ip", "");
  	    }
  	    else {
  	    	serverIp = "141.45.165.252";
  	    }
		if (isOnline() ) 
		{
			new WriteData().execute((Void[])null);	
		}
		else
		{
			Toast.makeText(ctx, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * Methode zur Prüfung, ob das Smartphone über eine aktive Online-Verbindung verfügt.
	 */
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}	
	/**
	 * Methode zur Prüfung, ob der eingegebene Server erreichbar ist.
	 */
	//ToDo: UrlConnection liefert -1 als Antwort
	public boolean isReachable() {
		URL url;
		try {
			url = new URL("http://"+serverIp+":7000/OpenResKitHub/");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept-Encoding", "identity");
			urlConnection.connect();
		    if (urlConnection.getResponseCode() == 200) {
		    	inetTest = true;
		        return true;
		    }
		    else {
		    	return false;
		    }
		} catch (IOException e2) {
            Log.e("Error", e2.getMessage());
			e2.printStackTrace();
			return false;
		}
	}	
		
	/**
	 * Methode zum Upload aller auf dem Gerät in der SQLite DB gespeicherten Datensätze auf einen
	 * OpenResKit-Hub. Dies wird durch einen asynchronen Task bewerkstelligt. Vor dem Upload 
	 * wird die lokale DB abgefragt und eine Fortschrittsanzeige geöffnet. Der Upload wird mittelt
	 * HTTPPost bewerkstelligt. Wurden alle Daten hochgeladen, wird der Fortschrittsdialog geschlossen.
	 */
	private class WriteData extends AsyncTask<Void, Void, Boolean> 
	{
		@Override
		protected Boolean doInBackground(Void... params) {			
			try {

				HttpResponse response;	
	        	HttpParams httpParams = new BasicHttpParams();
	        	HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
	        	HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
	        	httpParams.setBooleanParameter("http.protocol.expect-continue", false);
	        	int ID = 1;
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
				//ToDo: Ids aus Footprints auslesen und in URL übergeben


		         if (isReachable())
		         {
					 for (Footprint footprint : footprintDao)
					 {
						 JSONObject uploadedFootprint = JsonConverter.convertFootprintToJson(footprint, ctx);				 
						 StringEntity stringEntity = new StringEntity(uploadedFootprint.toString(),HTTP.UTF_8);
						 stringEntity.setContentType("application/json");
						 
						 HttpPost request = null;

//						 if (footprint.getServerId() == 0) {
							 request = new HttpPost("http://"+serverIp+":7000/OpenResKitHub/CarbonFootprints");
							 request.setHeader("X-HTTP-Method-Override", "PUT");
//						 }
//						 else if (footprint.getServerId() > 0) {
//							 request = new HttpPost("http://"+serverIp+":7000/OpenResKitHub/CarbonFootprints("+footprint.getServerId()+")");
//							 request.setHeader("X-HTTP-Method", "MERGE");
//						 }
						 request.setHeader("Accept", "application/json");
						 request.setHeader("Content-type", "application/json");	
						 request.setEntity(stringEntity);
							 
						 response = httpClient.execute(request);
						 HttpEntity responseEntity = response.getEntity();
						 char[] buffer = new char[(int)responseEntity.getContentLength()];
				         InputStream stream = responseEntity.getContent();
				         InputStreamReader reader = new InputStreamReader(stream);
				         reader.read(buffer);
				         stream.close();
						 uploadOk = true;					 
						 JSONObject answer = new JSONObject(new String(buffer));
						 System.out.print(answer);
					 }
		         }
			 }
			 catch (Exception e) {
				 e.printStackTrace();
				 Toast.makeText(ctx, "Upload fehlgeschlagen!", Toast.LENGTH_LONG).show();
			 }
			return true;
		}
		
		 protected void onPreExecute() 
		 {
			   super.onPreExecute();
			   pdia = new ProgressDialog(ctx);
			   pdia.setMessage("Schreibe Daten");
			   pdia.show();
			   
	       		try {
					footprintPositionDao = helper.getFootprintPositionDao();
		    	   	footprintDao = helper.getFootprintDao();
		    	   	flightDao = helper.getFlightsDao();
		    	   	carDao = helper.getCarDao();
		    	   	energyConsumptionDao = helper.getEnergyConsumptionDao();
		    	   	publicTransportDao = helper.getPublicTransportDao();
		    	   	employeeDao = helper.getEmployeeDao();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
		 }
		 
		 @Override
		 protected void onPostExecute(Boolean result) 
		 {
			   pdia.dismiss();
			   if (uploadOk && inetTest)
//			if (uploadOk )
			   {
				   Toast.makeText(ctx, "Upload erfolgreich!", Toast.LENGTH_LONG).show();
			   }
			   else {
				   Toast.makeText(ctx, "Upload fehlgeschlagen! Bitte überprüfen sie die Serveradresse.", Toast.LENGTH_LONG).show();
			   }
		 }
	}
	/**
	 * Methode zum Download aller Datensätze auf einem OpenResKit-Hub und Speicherung in der
	 * auf dem Gerät befindlichen SQLite DB. Dies wird durch einen asynchronen Task bewerkstelligt. 
	 * Vor dem Download wird die lokale DB abgefragt und eine Fortschrittsanzeige geöffnet. 
	 * Der Download wird mittelt HTTPGet bewerkstelligt. Wurden alle Daten heruntergeladen, 
	 * werden sie lokal gespeicher (falls nicht schon vorhanden) und der Fortschrittsdialog geschlossen.
	 * @return footprints
	 */
	private class GetFootprints extends AsyncTask<Void, Void, ArrayList<Footprint>>  {
		   protected ArrayList<Footprint> doInBackground(Void...params) { 
			   ArrayList<Footprint> footprints = new ArrayList<Footprint>();
		        try {		        	
		        	HttpParams httpParams = new BasicHttpParams();
		        	HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		        	HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
		        	httpParams.setBooleanParameter("http.protocol.expect-continue", false);		
		        	HttpGet request = new HttpGet("http://"+serverIp+":7000/OpenResKitHub/CarbonFootprints?$format=json&$expand=Positions/ResponsibleSubject");
		            request.setHeader("Accept", "application/json");
		            request.setHeader("Content-type", "application/json");		     
		            HttpClient httpClient = new DefaultHttpClient(httpParams);

		            if (isReachable())
		            {
			            HttpResponse response = httpClient.execute(request);
			            response = httpClient.execute(request);
		                JSONObject serverFootprints = null;
		                if(response.getStatusLine().getStatusCode() == 200){
		                    HttpEntity entity = response.getEntity();
		                    if (entity != null) {
		                        InputStream instream = entity.getContent();
		                        jsonText = convertStreamToString(instream);
		                        instream.close();
		                    }
		                }			                
		                serverFootprints = new JSONObject(jsonText);
		                footprintData = serverFootprints.getJSONArray("value");
	                	int lastFootprint = 0;
	            	    for(int i = 0; i < footprintData.length(); i++){
	            	        JSONObject oneFootprintObject = footprintData.getJSONObject(i);
	            	        String serverId = oneFootprintObject.getString("Id");
	            	        Boolean exists = false;
	            	        for (Footprint footprintCheck : footprintDao)
	            	        {
	            	        	if ((footprintCheck.getServerId() == Integer.parseInt(serverId))) {
	            	        		exists = true;
	            	        	}
	            	        }
	            	        if (!exists)
	            	        {
	                	        String oneFootprintString = oneFootprintObject.toString();                	        
	                   	        Footprint footprint = mapper.readValue(oneFootprintString, Footprint.class);
	                   	        footprint.setId(UUID.randomUUID().toString());
	               	        	footprint.setCalculationCategory("Carbon");		
	            		  		for (Footprint footprintForNr : footprintDao)
							  		{
							  			if (footprintForNr.getNr() > lastFootprint)
							  			{
							  				lastFootprint = footprintForNr.getNr();
							  			}
							  		}						  	
							  	lastFootprint = lastFootprint + 1;
								footprint.setNr(lastFootprint);
								ArrayList<FootprintPosition> footprintPositionList = new ArrayList<FootprintPosition>();
	                	        JSONArray positionsArray = oneFootprintObject.getJSONArray("Positions");
	                	        for(int j = 0; j < positionsArray.length(); j++){
	                	        	JSONObject onePositionObject = positionsArray.getJSONObject(j);
	                	        	String onePositionString = onePositionObject.toString();
	                	        	
	                	        	FootprintPosition footprintPosition = mapper.readValue(onePositionString, FootprintPosition.class);
	                	        	footprintPosition.setId(UUID.randomUUID().toString());
	                	        	footprintPosition.setFootprint(footprint);       
	                	        	footprintPositionList.add(footprintPosition);	
	                	        	

	                	        	if (employeeDao.countOf() == 0) { 
	                	        		JSONArray employeeData = null;
	            			        	HttpGet employeeRequest = new HttpGet("http://"+serverIp+":7000/OpenResKitHub/ResponsibleSubjects/OpenResKit.DomainModel.Employee?$format=json");
	            			        	employeeRequest.setHeader("Accept", "application/json");
	            			        	employeeRequest.setHeader("Content-type", "application/json");		     

            				            HttpResponse employeeResponse = httpClient.execute(employeeRequest);
            				            employeeResponse = httpClient.execute(employeeRequest);
            			                JSONObject serverEmployees = null;
            			                if(employeeResponse.getStatusLine().getStatusCode() == 200){
            			                    HttpEntity entity = employeeResponse.getEntity();
            			                    if (entity != null) {
            			                        InputStream instream = entity.getContent();
            			                        jsonEmployeeText = convertStreamToString(instream);
            			                        instream.close();
            			                    }
            			                }			                
            			                serverEmployees = new JSONObject(jsonEmployeeText);
            			                employeeData = serverEmployees.getJSONArray("value");
            		            	    for(int k = 0; k < employeeData.length(); k++){
            		            	        JSONObject oneEmployeeObject = employeeData.getJSONObject(k);
            		            	     
        		                	        String oneEmployeeString = oneEmployeeObject.toString();                	        
        		                   	        Employee employee = mapper.readValue(oneEmployeeString, Employee.class);        		                   	        
        		  	                	    employeeDao.createOrUpdate(employee);
        		                   	        
            		            	    }	            			                      
	                	        	}
	                	        	
	                	        	JSONObject responsibleSubjectObject = onePositionObject.getJSONObject("ResponsibleSubject");
	                	        	String responsibleSubjectString = responsibleSubjectObject.toString();
	                	        	Employee employee = mapper.readValue(responsibleSubjectString, Employee.class);
//	                	        	String responsibleSubjectId = responsibleSubjectObject.getString("Id");
	                	        	footprintPosition.setResponsibleSubject(employee);
	                	        	               	        	
	                	        	String positionType = onePositionObject.getString("odata.type");
	                	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Flight") || positionType.equalsIgnoreCase("OpenResKit.DomainModel.AirportBasedFlight"))
	                	        	{	          	
	                	        		Flight flight = mapper.readValue(onePositionString, Flight.class);
	                	        		flight.setId(UUID.randomUUID().toString());
	                	        		flight.setFootprintPosition(footprintPosition);	 
	                	        		flight.setCalculationCategory("Carbon");

	                		       		flightDao.createOrUpdate(flight);
		                	        	footprintPositionDao.createOrUpdate(footprintPosition);
	                	        	}
	                	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Car") || positionType.equalsIgnoreCase("OpenResKit.DomainModel.FullyQualifiedCar"))
	                	        	{	                	       
	                	        		Car car = mapper.readValue(onePositionString, Car.class);
	                	        		car.setId(UUID.randomUUID().toString());
	                	        		car.setFootprintPosition(footprintPosition);
	                	        		car.setCalculationCategory("Carbon");
	                	        		carDao.createOrUpdate(car);	
		                	        	footprintPositionDao.createOrUpdate(footprintPosition);
	                	        	}
	                	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.EnergyConsumption"))
	                	        	{	 		                	        	
	                	        		EnergyConsumption energyConsumption = mapper.readValue(onePositionString, EnergyConsumption.class);
	                	        		energyConsumption.setId(UUID.randomUUID().toString());
	                	        		energyConsumption.setFootprintPosition(footprintPosition);
	                	        		energyConsumption.setCalculationCategory("Carbon");
	                	        		energyConsumptionDao.createOrUpdate(energyConsumption);	          
	                	        	}
	                	        	if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.PublicTransport"))
	                	        	{		             
	                	        		PublicTransport publicTransport = mapper.readValue(onePositionString, PublicTransport.class);
	                	        		publicTransport.setId(UUID.randomUUID().toString());
	                	        		publicTransport.setFootprintPosition(footprintPosition);
	                	        		publicTransport.setCalculationCategory("Carbon");
	                	        		publicTransportDao.createOrUpdate(publicTransport);	   
		                	        	footprintPositionDao.createOrUpdate(footprintPosition);
	                	        	}
	                	        }
	                	        if (footprintPositionList.size() > 0) {
		                	        footprint.setFootprintPositions(footprintPositionList);
		                	        footprintDao.createOrUpdate(footprint);
		                		    footprints.add(footprint);
	                	        }
	        	        	} 
	            	    }
		            }	           
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		       return footprints;
		   }
		   
		   protected void onPreExecute() 
		   {
			   super.onPreExecute();
			   pdia = new ProgressDialog(ctx);
			   pdia.setMessage("Aktualisiere Daten");
			   pdia.show();
	       		try {
					footprintPositionDao = helper.getFootprintPositionDao();
		    	   	footprintDao = helper.getFootprintDao();
		    	   	flightDao = helper.getFlightsDao();
		    	   	carDao = helper.getCarDao();
		    	   	energyConsumptionDao = helper.getEnergyConsumptionDao();
		    	   	publicTransportDao = helper.getPublicTransportDao();
		    	   	employeeDao = helper.getEmployeeDao();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
		   }		   
		   protected void onPostExecute(ArrayList<Footprint> footprints) 
		   {    
//	           	FragmentTransaction ft = getFragmentManager().beginTransaction();
//	           	DialogFragment newFragment = new DateDialogFragment(EntryFragment.this);
//	           	newFragment.show(ft, "dialog");
			   if (act != null)
			   {
//				    FragmentTransaction fragmentTransaction = act.getFragmentManager().beginTransaction();
//			        ListOverview listFragment = new ListOverview();
//	
//			        fragmentTransaction.replace(R.id.placeholder, listFragment);
//	//		        fragmentTransaction.addToBackStack(null);
//			        fragmentTransaction.commit();
			        
			        try {
						footprintDao = helper.getFootprintDao();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			        if (footprintDao != null ) {
//						   for (Footprint footprint : footprintDao)
//						   {
//							   	listFragment.updateList();
//						   }
			        }
			        act.updateList();
			   }
			   pdia.dismiss();		  
       	       if (!inetTest) {
	            	Toast.makeText(ctx, "Download fehlgeschlagen. Server nicht erreichbar!", Toast.LENGTH_LONG).show();
       	       }	
		   }
		}
    /**
     * Methode zum Konvertieren eines Input Streams in einen JSON String.
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     * Siehe
     * http://stackoverflow.com/questions/4480363/android-java-utf-8-httpclient-problem
     */
	private static String convertStreamToString(InputStream is) {
	    BufferedReader reader = null;
	    try {
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    } catch (UnsupportedEncodingException e1) {
	        e1.printStackTrace();
	    }
	    StringBuilder sb = new StringBuilder();

	    String line;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}

}
