package webSQL;

public class Driver {

	/**
	 * @param args
	 */
	public static void main( String [] args ) throws Exception
	{
		WebSQL wql = new WebSQL();
		
		wql.Query("SELECT d1.url,d1.title FROM document d1 SUCH THAT http://www.mst.edu -> d1;");

	}
	
	void setupDB() throws Exception
	{
/*		m_conn = DriverManager.getConnection("jdbc:sqlite:websql.db");
		Statement statement_ = m_conn.createStatement();
		
		statement_.executeUpdate("DROP TABLE IF EXISTS documents;");
		statement_.executeUpdate("DROP TABLE IF EXISTS anchors;");
		
		statement_.executeUpdate("CREATE TABLE documents (id, url, title, source)");
		statement_.executeUpdate("CREATE TABLE anchors (id, doc_id, href, text)");
		*/
	}
	
//	private Connection m_conn;
}
