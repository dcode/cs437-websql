package webSQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This class will provide a object-to-relational map to the accompanying database table.
public class Anchor {
/* Table Schema
 * CREATE TABLE anchors (
		anchorid INTEGER PRIMARY KEY, 
		doc_id INTEGER, 
		href TEXT, 
		text TEXT,
		FOREIGN KEY(doc_id) REFERENCES documents(documentid))
*/
	private int 	m_anchorid;
	private int		m_docid;
	private String 	m_href;
	private String	m_text;
	
	private Connection m_conn = null;

	
	public Anchor( Document p_parent ) throws ClassNotFoundException, SQLException
	{
		m_conn = WebSQL.GetConnection();
		m_docid = p_parent.GetID();
		
		LoadOrCreate();
	}
	
	public Anchor( int p_anchorid ) throws ClassNotFoundException, SQLException
	{
		m_conn = WebSQL.GetConnection();
		
		LoadOrCreate();
	}
	
	public void Save() throws SQLException
	{
		SaveOrUpdate();
	}
	
	public String GetHref() throws SQLException
	{
		String href_ = m_href;
		
		if( null == m_href )
		{
			LoadOrCreate();
		}
		
		return href_;
	}
	
	public void SetHref( String p_href ) throws SQLException
	{
		m_href = p_href;
		Save();
	}
	
	public String GetText() throws SQLException
	{
		String text_ = m_text;
		
		if( null == m_text )
		{
			LoadOrCreate();
		}
		
		return text_;
	}
	
	public void SetText( String p_text ) throws SQLException
	{
		m_text = p_text;
		Save();
	}
	
	public String GetBase() throws SQLException
	{
		String base_ = null;
		
		if( null == m_href )
		{
			LoadOrCreate();
		}
		
		/* @TODO Parse out base URL i.e. www.mst.edu from http://www.mst.edu/directory/foo.html */
		return base_;
	}
	
	private void LoadOrCreate( ) throws SQLException
	{
		int count_;
		Statement stmt_ = m_conn.createStatement();
		ResultSet rs_ = 
			stmt_.executeQuery("SELECT COUNT(*) FROM anchors WHERE anchorid = " + m_anchorid + ";" );
		
		count_ = rs_.getInt(1);
		
		if( 0 < count_ )
		{
			/* Document already exists, retrieve it */
			rs_ = stmt_.executeQuery("SELECT * FROM anchors WHERE anchorid = " + m_anchorid + ";");
			
			m_href = rs_.getString("href");
			m_text = rs_.getString("text");
			m_docid = rs_.getInt("doc_id");	
		}
		else
		{
			/* Create a new row */
			stmt_.execute("INSERT INTO anchors VALUES (NULL, " + m_docid + ", '', '');" );
			m_anchorid = stmt_.getGeneratedKeys().getInt(1);
		}
		
	}
	
	private void SaveOrUpdate() throws SQLException
	{
		int count_;
		Statement stmt_ = m_conn.createStatement();
		PreparedStatement pstmt_ = null;
		ResultSet rs_ = 
			stmt_.executeQuery("SELECT COUNT(*) FROM anchors WHERE anchorid = " + m_anchorid + ";" );
		
		count_ = rs_.getInt(1);
		
		if( 0 < count_ )
		{
			/* Document already exists, update it */
			pstmt_ = m_conn.prepareStatement("UPDATE anchors SET href = ?, text = ?, doc_id = ? WHERE anchorid = ?;");
			
			pstmt_.setString(1, m_href);
			pstmt_.setString(2, m_text);
			pstmt_.setInt(3, m_docid);
			pstmt_.setInt(4, m_anchorid);
			
			pstmt_.addBatch();
			m_conn.setAutoCommit(false);
			pstmt_.executeBatch();
			m_conn.setAutoCommit(true);

		}
		else
		{
			/* Create a new row */
			pstmt_ = m_conn.prepareStatement("INSERT INTO anchors VALUES ( NULL, " + m_docid + ", ?, ? );");
			
			pstmt_.setString(1, m_href);
			pstmt_.setString(2, m_text);
			
			pstmt_.addBatch();
			m_conn.setAutoCommit(false);
			pstmt_.executeBatch();
			m_conn.setAutoCommit(true);
			
			m_anchorid = stmt_.getGeneratedKeys().getInt(1);
		}

	}
}
