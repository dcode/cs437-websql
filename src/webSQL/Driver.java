package webSQL;


import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.io.*;

import org.json.JSONObject;
import org.json.JSONException;

public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// This example request includes an optional API key which you will need to
		// remove or replace with your own key.
		// Read more about why it's useful to have an API key.
		// The request also includes the userip parameter which provides the end
		// user's IP address. Doing so will help distinguish this legitimate
		// server-side traffic from traffic which doesn't come from an end-user.
		URL url;
		URLConnection connection;
		
		// This key is tied to the domain of this referrer.
		String key = "ABQIAAAAb5tp6ien1vDieaah85YjVBSvWF1I7e9NNVC_sWpzf5mrAoJcaRShjl967bKfhGjmgF3t1cqAxfkSOw";
		String referrer = "http://mst.edu";
		
		
		String userIP = "127.0.0.1";
		
		
		String query = "Paris%20Hilton";
		
		try {
			url = new URL(
			    "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&"
			    + "q=" + query + "&key=" + key + "&userip=" + userIP);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		try {
			connection = url.openConnection();
			connection.addRequestProperty("Referer", referrer);
		
		} catch (IOException e1)
		{
			System.err.println("Error connecting to URL: " + e1.getLocalizedMessage());
			return;
		}


		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader;
		
		try {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch ( IOException e1 )
		{
			System.err.println("Error reading web stream: " + e1.getLocalizedMessage());
		}
		
		JSONObject json;
		try {
			json = new JSONObject(builder.toString());
			
			System.out.println("JSON Parsing success!");
			System.out.println(json.toString(4));

		} catch ( JSONException e1 )
		{
			System.err.println("Error parsing JSON string: " + e1.getLocalizedMessage());
		}
		// now have some fun with the results...
		
	}

}
