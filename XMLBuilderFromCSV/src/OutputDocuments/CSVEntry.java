package OutputDocuments;

import java.util.ArrayList;
import java.util.List;

public class CSVEntry {
	private List<CSVAttribute> _attributes;
	
	public CSVEntry(){
		_attributes = new ArrayList<CSVAttribute>();
	}
	
	public void addAttribute(CSVAttribute attribute){
		_attributes.add(attribute);
	}
	
	public List<CSVAttribute> getAttributes(){
		return _attributes;
	}
}
