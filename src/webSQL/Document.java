package webSQL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//This class will provide a object-to-relational map to the accompanying database table.
public class Document {
/* Schema
 * CREATE TABLE documents (
 * 		documentid INTEGER PRIMARY KEY, 
 * 		url TEXT, 
 * 		title TEXT, 
 * 		source TEXT
 * )
 */
	
	private int m_documentid = -1;
	private String m_url = null;
	private String m_title = null;
	private String m_source = null;
	
	private Connection m_conn = null;
	
	
	public Document() throws SQLException, ClassNotFoundException
	{
		m_conn = WebSQL.GetConnection();
		
		LoadOrCreate();

	}
	
	public Document(int p_key ) throws SQLException, ClassNotFoundException
	{
		m_conn = WebSQL.GetConnection();
		m_documentid = p_key;
		
		LoadOrCreate();
	}
	
	public void Save() throws SQLException
	{
		SaveOrUpdate();
	}
	
	protected int GetID()
	{
		return m_documentid;
	}
	
	public String GetURL() throws SQLException
	{
		String ret_ = m_url;
		
		if( null == ret_ )
		{
			/* Pull from DB */
			PreparedStatement stmt_ = 
				m_conn.prepareStatement("SELECT * FROM documents WHERE documentid = ?");
			stmt_.setInt(1, m_documentid);
		    ResultSet rs_ = stmt_.executeQuery();
		    		    
		    ret_ = m_url = rs_.getString("url");
		}
		
		return ret_;
	}
	
	public void SetURL( String p_url ) throws SQLException
	{
		m_url = p_url;
		PreparedStatement stmt_ = 
			m_conn.prepareStatement("UPDATE documents SET url = ? WHERE documentid = ?");
		
		/* The batch processing is overkill, but it makes it a bit clearer */
		stmt_.setString(1, m_url);
		stmt_.setInt(2, m_documentid);
		stmt_.addBatch();
		
	    m_conn.setAutoCommit(false);
	    stmt_.executeBatch();
	    m_conn.setAutoCommit(true);
	}
	
	public String GetTitle() throws SQLException
	{
		String ret_ = m_title;
		if( null == ret_)
		{
			/* Pull from DB */
			PreparedStatement stmt_ = m_conn.prepareStatement("SELECT * FROM documents WHERE documentid = ?;");
			stmt_.setInt(1, m_documentid);
		    ResultSet rs_ = stmt_.executeQuery();
		    
		    ret_ = m_title = rs_.getString("title");
		}		
		return ret_;
	}
	
	public void SetTitle( String p_title ) throws SQLException
	{
		m_title = p_title;
		PreparedStatement stmt_ = 
			m_conn.prepareStatement("UPDATE documents SET title = ? WHERE documentid = ?");
		
		/* The batch processing is overkill, but it makes it a bit clearer */
		stmt_.setString(1, m_title);
		stmt_.setInt(2, m_documentid);
		stmt_.addBatch();
		
	    m_conn.setAutoCommit(false);
	    stmt_.executeBatch();
	    m_conn.setAutoCommit(true);
	}
	
	public String GetSource() throws SQLException
	{
		String source_ = m_source;
		
		if( null == source_ )
		{
			/* Pull from DB */
			PreparedStatement stmt_ = m_conn.prepareStatement("SELECT * FROM documents WHERE documentid = ?");
			stmt_.setInt(1, m_documentid);
		    ResultSet rs_ = stmt_.executeQuery();
		    
		    source_ = m_source = rs_.getString("source");
		}
		
		return source_;
	}
	
	public void SetSource( String p_source ) throws SQLException
	{
		m_source = p_source;
		Save();
	}
	
	public Vector<Document> GetNeighbors() throws SQLException, ClassNotFoundException, IOException
	{	// Returns neighboring documents according to anchors
		Vector< Document > neighbors_ = new Vector< Document >();
		Document doc_ = null;
		String url_ = null;
		int count_ = 0;
		
		PreparedStatement stmt_ = 
			m_conn.prepareStatement("SELECT * FROM anchors WHERE doc_id = ?;");		
		
		stmt_.setInt(1, m_documentid);
		ResultSet rs_ = stmt_.executeQuery();
		
		// Limit to first 25 neighbors
		while( rs_.next() && count_ < 25)
		{
			doc_ = new Document();
			url_ = rs_.getString("href");
			doc_.SetURL(url_);
			doc_.Save();
			doc_.FetchAndParse();
			
			neighbors_.add(doc_);
			count_++;
		}
		
		return neighbors_;
	}
	
	public void FetchAndParse() 
		throws ClassNotFoundException, SQLException, IOException
	{	
		// Retrieve webpage
		_fetch();
		_parse();
		Save();
	}
	
	private void _fetch() throws IOException, SQLException
	{
		URL u = new URL(m_url);
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

	    m_source = sb.toString();
	    
	    Save();
	}
	
	private void _parse() throws ClassNotFoundException, SQLException
	{
		// Parse Title
		String title_ = "";
		String regex = "<title((.|\\n)*?)>((.|\\n)*?)</title>";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher( m_source );
		
	
		while(m.find()) 
		{
			title_ = m.group(3);
		}
		
		// Set Title
		SetTitle( title_ );
		Save();
		
		// Loop and add all outgoing anchors
		Anchor a_ = null;
		
		// Find any anchor tag and ignore non-href attributes
		regex = "<a[^>]*href=\"" 
			+ "(http[^\"]+)"	// Capture the href attribute, only http
			+ "\"[^>]*>"	// Ignore any other <a> attributes
			+ "([^<]+)"	// Capture everything up to end tag
			+ "</a>";	// End tag

		p = Pattern.compile(regex, 
			   Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE );
		
		m = p.matcher(GetSource());

		while(m.find())
		{
			a_ = new Anchor( this );
			a_.SetHref(m.group(1));	// First capture, the href
			a_.SetText(m.group(2)); // Second capture, the text
			a_.Save();
		}
		
		Save();
	}
	
	private void LoadOrCreate( ) throws SQLException
	{
		int count_;
		Statement stmt_ = m_conn.createStatement();
		ResultSet rs_ = 
			stmt_.executeQuery("SELECT COUNT(*) FROM documents WHERE documentid = " + m_documentid + ";" );
		
		count_ = rs_.getInt(1);
		
		if( 0 < count_ )
		{
			/* Document already exists, retrieve it */
			rs_ = stmt_.executeQuery("SELECT * FROM documents WHERE documentid = " + m_documentid + ";");
			
			m_url = rs_.getString("url");
			m_title = rs_.getString("title");
			m_source = rs_.getString("source");	
		}
		else
		{
			/* Create a new row */
			stmt_.execute("INSERT INTO documents VALUES (NULL, '', '', '');" );
			m_documentid = stmt_.getGeneratedKeys().getInt(1);
		}
		
	}
	
	private void SaveOrUpdate() throws SQLException
	{
		int count_;
		Statement stmt_ = m_conn.createStatement();
		PreparedStatement pstmt_ = null;
		ResultSet rs_ = 
			stmt_.executeQuery("SELECT COUNT(*) FROM documents WHERE documentid = " + m_documentid + ";" );
		
		count_ = rs_.getInt(1);
		
		if( 0 < count_ )
		{
			/* Document already exists, update it */
			pstmt_ = m_conn.prepareStatement("UPDATE documents SET url = ?, title = ?, source = ? WHERE documentid = ?;");
			
			pstmt_.setString(1, m_url);
			pstmt_.setString(2, m_title);
			pstmt_.setString(3, m_source);
			pstmt_.setInt(4, m_documentid);
			
			pstmt_.addBatch();
			m_conn.setAutoCommit(false);
			pstmt_.executeBatch();
			m_conn.setAutoCommit(true);

		}
		else
		{
			/* Create a new row */
			pstmt_ = m_conn.prepareStatement("INSERT INTO documents VALUES ( NULL, ?, ?, ? );");
			
			pstmt_.setString(1, m_url);
			pstmt_.setString(2, m_title);
			pstmt_.setString(3, m_source);
			
			pstmt_.addBatch();
			m_conn.setAutoCommit(false);
			pstmt_.executeBatch();
			m_conn.setAutoCommit(true);
			
			m_documentid = stmt_.getGeneratedKeys().getInt(1);
		}

	}
	
}
