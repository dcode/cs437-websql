package webSQL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.*;

import java.sql.*;

public class WebSQL {
	private String m_key;
	static private Connection m_conn;

	public static final String queryURL = "http://ajax.googleapis.com/ajax/services/search/web?";
	public static final String apiVer = "1.0";
	
	
	
	public WebSQL() 
	{
		m_key = "ABQIAAAAb5tp6ien1vDieaah85YjVBSvWF1I7e9NNVC_sWpzf5mrAoJcaRShjl967bKfhGjmgF3t1cqAxfkSOw";
		m_conn = null;

	}
	
	public WebSQL( String p_key )
	{
		m_key = p_key;
		m_conn = null;
	}
	
	public JSONObject Query( String p_query ) throws JSONException
	{
		JSONObject ret = null;
		JSONObject resultSetA, resultSetB;
		JSONArray resultsA = null, resultsB = null;
		String keywordA = "Derek Ditch MST", keywordB = "Derek Snyder MST";
		
		resultSetA = _query(keywordA , "", "");
		resultSetB = _query(keywordB, "", "");
		
		try {
			resultsA = resultSetA.getJSONObject("responseData").getJSONArray("results");
			resultsB = resultSetB.getJSONObject("responseData").getJSONArray("results");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Query: " + keywordA);
		System.out.println("Results: " + resultsA.length());
		
		System.out.println("Query: " + keywordB);
		System.out.println("Results: " + resultsB.length());
		
		System.out.println("Search complete.");
		return ret;
	}
	
	protected static void InitializeDB() throws ClassNotFoundException, SQLException
	{
		try {
			Class.forName("org.sqlite.JDBC");
			m_conn = DriverManager.getConnection("jdbc:sqlite:websql.db");
		}  catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Unable to load JDBC driver for sqlite database. Check CLASSPATH.");
			throw e;
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Unable to open JDBC connection for SQLite database file 'websql.db'.");
			throw e;
		}
	}
	
	protected static Connection GetConnection() throws ClassNotFoundException, SQLException
	{
		/* I don't check the return of InitializeDB since there's not much I can do
		 * here about it if the connection fails. The user of this method wil
		 */
		if( null == m_conn )
			InitializeDB();
		
		return m_conn;
	}
	
	private JSONObject _query( String p_query, String p_referrer, String p_ipaddr )
	{
		String query, referrer, ipaddr;
		JSONObject ret = null;
		URL uri;
		URLConnection conn;
		String line;
		StringBuilder builder;
		BufferedReader reader;
		
		/* Ease development by giving pre-configured values */
		if( "" == p_referrer )
		{
			referrer = "http://www.mst.edu";
		}
		else
		{
			referrer = p_referrer;
		}
		
		if( "" == p_ipaddr )
		{
			ipaddr = "127.0.0.1";
		}
		else
		{
			ipaddr = p_ipaddr;
		}
		
		try {
			query = URLEncoder.encode( p_query, "utf-8" );
		
			uri = new URL(queryURL + "v=" + apiVer + "&q=" + query + "&key=" + m_key + "&userip" + ipaddr );

			conn = uri.openConnection();
			conn.addRequestProperty("Referer", referrer);

			builder = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			} 
			
			ret = new JSONObject(builder.toString());
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
}
