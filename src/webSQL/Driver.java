package webSQL;

import java.sql.*;

public class Driver {

	/**
	 * @param args
	 */
	public static void main( String [] args ) throws Exception
	{
		WebSQL wql = new WebSQL();
		
		// This doesn't do anything yet.
		wql.Query("SELECT d1.url,d1.title FROM document d1 SUCH THAT http://www.mst.edu -> d1;");

		// This creates the websql.db file and populates it with the schema.
		setupDB();
		
		wql.Query1();
		
	}
	
	// This should be moved into the WebSQL package once the supporting classes exist.
	static void setupDB() throws Exception
	{
	    Class.forName("org.sqlite.JDBC");
		m_conn = DriverManager.getConnection("jdbc:sqlite:websql.db");
		Statement statement_ = m_conn.createStatement();
		
		statement_.executeUpdate("DROP TABLE IF EXISTS documents;");
		statement_.executeUpdate("DROP TABLE IF EXISTS anchors;");
		
		statement_.executeUpdate("CREATE TABLE documents (documentid INTEGER PRIMARY KEY, url TEXT, title TEXT, source TEXT)");
		statement_.executeUpdate("CREATE TABLE anchors (anchorid INTEGER PRIMARY KEY, doc_id INTEGER, href TEXT, text TEXT, FOREIGN KEY(doc_id) REFERENCES documents(documentid))");
	}
	
	static private Connection m_conn;
}
