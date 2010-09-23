package webSQL;

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
	
	public Anchor( Document p_parent )
	{
		
	}
	
	public String GetHref()
	{
		String href_ = null;
		
		return href_;
	}
	
	public void SetHref( String p_href )
	{
		m_href = p_href;
	}
	
	public String GetText()
	{
		String text_ = null;
		
		return text_;
	}
	
	public String GetBase()
	{
		String base_ = null;
		
		return base_;
	}
}
