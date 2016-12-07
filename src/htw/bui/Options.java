package htw.bui;

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

import openreskit.odata.DatabaseHelper;
import openreskit.odata.Employee;
import openreskit.odata.Synchronization;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Formular zum Editieren der App-Einstellungen
 * Es kann die IP des OpenResKit-Servers geändert werden.
 * 
 */
public class Options extends Activity {
	Context ctx;
	ProgressDialog pdia;
	ObjectMapper mapper = new ObjectMapper(); 	 
	private DatabaseHelper helper;
	SharedPreferences prefs;
	String serverIp = null;
	Boolean inetTest = false;
	String jsonText = null;
	Synchronization sync = new Synchronization();
	private Dao<Employee, UUID> employeeDao;
	JSONArray employeeData = null;
	Activity act;
	Employee currentEmployee;
	ListView listview;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
        ctx = this;
	     Button acceptButton = (Button) findViewById(R.id.accepptbutton);
	     acceptButton.setOnClickListener(acceptListener); 
	     listview = (ListView) findViewById(R.id.employeesList);
	  	 prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	     getEmployeeData();
    }
    
	public void getEmployeeData() {
		

  	    if (!prefs.getString("ip", "").equalsIgnoreCase(""))
  	    {
  	    	serverIp = prefs.getString("ip", "");
  	    }
  	    else {
  	    	serverIp = "141.45.165.252";
  	    }
		if (isOnline() ) 
		{
			new GetEmployees().execute((Void[])null);	
		}
		else
		{
			Toast.makeText(ctx, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
		}
		
	}
		

		
		private class GetEmployees extends AsyncTask<Void, Void, ArrayList<String>>  {
			   protected ArrayList<String> doInBackground(Void...params) { 
				   ArrayList<String> employees = new ArrayList<String>();
			        try {		        	
			        	HttpParams httpParams = new BasicHttpParams();
			        	HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			        	HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
			        	httpParams.setBooleanParameter("http.protocol.expect-continue", false);		
			        	HttpGet request = new HttpGet("http://"+serverIp+":7000/OpenResKitHub/ResponsibleSubjects?$format=json");
			            request.setHeader("Accept", "application/json");
			            request.setHeader("Content-type", "application/json");		     
			            HttpClient httpClient = new DefaultHttpClient(httpParams);

			            if (isReachable())
			            {
				            HttpResponse response = httpClient.execute(request);
				            response = httpClient.execute(request);
			                JSONObject serverEmployees = null;
			                if(response.getStatusLine().getStatusCode() == 200){
			                    HttpEntity entity = response.getEntity();
			                    if (entity != null) {
			                        InputStream instream = entity.getContent();
			                        jsonText = convertStreamToString(instream);
			                        instream.close();
			                    }
			                }			                
			                serverEmployees = new JSONObject(jsonText);
			                employeeData = serverEmployees.getJSONArray("value");
		            	    for(int i = 0; i < employeeData.length(); i++){
		            	        JSONObject oneEmployeeObject = employeeData.getJSONObject(i);
		            	        String serverId = oneEmployeeObject.getString("Id");
		            	        Boolean exists = false;
		            	        for (Employee existing : employeeDao)
		            	        {
		            	        	if ((existing.getId() == serverId)) {
		            	        		exists = true;
		            	        	}
		            	        }
		            	        if (!exists)
		            	        {
		                	        String oneEmployeeString = oneEmployeeObject.toString();                	        
		                   	        Employee employee = mapper.readValue(oneEmployeeString, Employee.class);
		                   	        if (employee.getOdataType().equalsIgnoreCase("OpenResKit.DomainModel.Employee")) {
		  	                	        employeeDao.createOrUpdate(employee);
		                   	        }
		        	        	} 
		            	    }
			            }	           
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			       return employees;
			   }
			   
			   protected void onPreExecute() 
			   {
				   super.onPreExecute();
				   pdia = new ProgressDialog(ctx);
				   pdia.setMessage("Aktualisiere Daten");
				   pdia.show();
				   helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
		       		try {
		       			employeeDao = helper.getEmployeeDao();
					} catch (SQLException e) {					
						e.printStackTrace();
					}
			   }		   
			   protected void onPostExecute(ArrayList<String> employees) 
			   {    
				   final ArrayList<Employee> employeeList = new ArrayList<Employee>();
				   pdia.dismiss();		 
				   
				   for (Employee employee : employeeDao) {
		     	       employees.add(employee.getLastName()+", "+employee.getFirstName());
		     	       employeeList.add(employee);
				   }

				   ListAdapter adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, employees);
				   listview.setAdapter(adapter);
				   listview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						currentEmployee = employeeList.get(position);		
			        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
				        SharedPreferences.Editor prefEditor = prefs.edit();  
				        prefEditor.putString("employee", (currentEmployee.getId())+"");
			        	prefEditor.commit();
			        	finish();
					}
		        });

			   }
			}


		public boolean isOnline() {
		    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}	
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
		public String convertStreamToString(InputStream is) {
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
    
    View.OnClickListener acceptListener = new View.OnClickListener() {
	  	  public void onClick(View v) {	
	  		
	  		  EditText ipText = (EditText) findViewById(R.id.serveraddress);
	  		  String ipString = ipText.getText().toString();
//	          SharedPreferences ipSetting = getSharedPreferences("CurrentIp", MODE_PRIVATE);

	          SharedPreferences.Editor prefEditor = prefs.edit();  
	          prefEditor.putString("ip", ipString);  
	          prefEditor.commit();  
	          Toast.makeText(ctx, "IP gespeichert", Toast.LENGTH_LONG).show();
//	          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//	      		String serverIp = prefs.getString("CurrentIp", "");
	          finish();
	}};
	
}