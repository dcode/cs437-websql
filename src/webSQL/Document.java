package webSQL;

import java.sql.*;

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
	
	public String GetURL() throws SQLException
	{
		String ret_ = m_url;
		
		if( null == ret_ )
		{
			/* Pull from DB */
			PreparedStatement stmt_ = m_conn.prepareStatement("SELECT * FROM documents WHERE documentid = ?");
			stmt_.setInt(1, m_documentid);
		    ResultSet rs_ = stmt_.executeQuery();
		    		    
		    ret_ = m_url = rs_.getString("url");
		}
		
		return ret_;
	}
	
	public void SetURL( String p_url ) throws SQLException
	{
		PreparedStatement stmt_ = 
			m_conn.prepareStatement("UPDATE documents SET url = ? WHERE documentid = ?");
		
		/* The batch processing is overkill, but it makes it a bit clearer */
		stmt_.setString(1, p_url);
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
			PreparedStatement stmt_ = m_conn.prepareStatement("SELECT * FROM documents WHERE documentid = ?");
			stmt_.setInt(1, m_documentid);
		    ResultSet rs_ = stmt_.executeQuery();
		    
		    ret_ = m_title = rs_.getString("title");
		}		
		return ret_;
	}
	
	public void SetTitle( String p_title ) throws SQLException
	{
		PreparedStatement stmt_ = 
			m_conn.prepareStatement("UPDATE documents SET title = ? WHERE documentid = ?");
		
		/* The batch processing is overkill, but it makes it a bit clearer */
		stmt_.setString(1, p_title);
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
