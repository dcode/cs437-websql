package webSQL;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.*;

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
	
	public void Query1() throws SQLException, ClassNotFoundException, IOException
	{ // select d1.url,d1.title from document d1 such that http://www.umr.edu -> d1

		String url_ = "http://www.mst.edu";
		Document doc_ = new Document();
		doc_.SetURL(url_);
		
		// Retrieve webpage
		doc_.SetSource(_fetch( doc_.GetURL() ));
		doc_.Save();
		
		// Parse page
		_parse(doc_);
		
		Vector< Document > neighbors = doc_.GetNeighbors();
		Iterator<Document> i = neighbors.iterator();
		
		System.out.println(
				"URL                       |              Title              ");
		System.out.println(
				"------------------------------------------------------------");
		while( i.hasNext() )
		{
			doc_ = i.next();
			System.out.println( doc_.GetURL() + "\t|\t" + doc_.GetTitle());
		}
		
	}
	
	public void Query2() throws SQLException, ClassNotFoundException, IOException, JSONException
	{ //select d1.url,d1.title from document d1 such that d1 mentions mst;
		String referrer = "http://www.mst.edu";
		String ipaddr = "131.151.1.7";
		
		String query_ = "mst";
		JSONObject resultset_ = _query(query_, referrer, ipaddr);
		JSONArray results = resultset_.getJSONObject("responseData").getJSONArray("results");
		
		Document doc_ = null;
		Vector< Document > docs = new Vector< Document > ();
		int num_ = results.length();
		for(int i=0; i<num_; i++ )
		{	
			JSONObject obj_ = results.getJSONObject(i);
			doc_ = new Document();
			doc_.SetURL(obj_.getString("url"));
			doc_.SetTitle(obj_.getString("title"));
			docs.add(doc_);
		}
		
		Iterator<Document> i = docs.iterator();
		
		System.out.println(
		"URL                       |              Title              ");
		System.out.println(
				"------------------------------------------------------------");
		while( i.hasNext() )
		{
			doc_ = i.next();
			System.out.println( doc_.GetURL() + "\t|\t" + doc_.GetTitle());
		}
	}
	
	public void Query3()
	{//select d2.base, d2.label from document d1, anchor d2 such that d1 mentions xml where d1.length>100;
		
	}
	
	public JSONObject Query( String p_query ) throws JSONException
	{
		JSONObject ret = null;
		JSONObject resultSetA, resultSetB;
		JSONArray resultsA = null;
		
		String keywordA = "Derek Ditch MST";
		
		resultSetA = _query(keywordA , "http://www.mst.edu", "127.0.0.1");
		
		try {
			resultsA = resultSetA.getJSONObject("responseData").getJSONArray("results");			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Query: " + keywordA);
		System.out.println("Results: " + resultsA.length());
		
		
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
		 * here about it if the connection fails.
		 */
		if( null == m_conn )
			InitializeDB();
		
		return m_conn;
	}
	
	public Document _parse( Document p_doc ) throws SQLException, ClassNotFoundException
	{
		// Parse Title
		String title_ = "";
		String regex = "<title((.|\\n)*?)>((.|\\n)*?)</title>";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(p_doc.GetSource());
		
		while(m.find()) 
		{
			title_ = m.group(3);
		}
		
		// Set Title
		p_doc.SetTitle(title_);
		
		// Loop and add all outgoing anchors
		Anchor a_ = null;
		
		regex = "<a[^>]*href=\"" // Find any anchor tag and ignore non-href attributes
			+ "(http[^\"]+)"	// Capture the href attribute
			+ "\"[^>]*>"	// Ignore any other <a> attributes
			+ "([^<]+)"	// Capture everything up to end tag
			+ "</a>";	// End tag

		p = Pattern.compile(regex, 
			   Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE );
		
		m = p.matcher(p_doc.GetSource());

		while(m.find())
		{
			a_ = new Anchor( p_doc );
			a_.SetHref(m.group(1));	// First capture, the href
			a_.SetText(m.group(2)); // Second capture, the text
			a_.Save();
		}
		
		p_doc.Save();
		
		return p_doc;
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
		
		referrer = p_referrer;
		ipaddr = p_ipaddr;

		try {
			query = URLEncoder.encode( p_query, "utf-8" );
		
			uri = new URL(
					queryURL + 
					"v=" + apiVer + 
					"&q=" + query + 
					"&key=" + m_key + 
					"&rsz=large" + // Gets 8 results
					"&userip" + ipaddr );

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
	private String _fetch( String p_href ) throws IOException
	{
		URL u = new URL(p_href);
		URLConnection conn = u.openConnection();
		String encoding = conn.getContentEncoding();
		
		if(null == encoding )
		{
			encoding = "ISO-8859-1";
		}
		
		BufferedReader br = new BufferedReader( new 
				InputStreamReader(conn.getInputStream(), encoding));
		StringBuilder sb = new StringBuilder(16384);
		try {
	        String line;
	        while ((line = br.readLine()) != null )
	        {
	        	sb.append(line);
	        	sb.append('\n');
	        }
	    } finally {
	    	br.close();
		}

	    return sb.toString();
	}
}
