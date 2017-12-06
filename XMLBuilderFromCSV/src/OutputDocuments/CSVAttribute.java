package OutputDocuments;

public class CSVAttribute {

	private String _column;
	private String _value;
	
	public CSVAttribute(String column, String value) {
		this._column = column;
		this._value = value;
	}
	
	public String getColumn(){
		return _column;
	}
	
	public String getValue(){
		return _value;
	}

}
