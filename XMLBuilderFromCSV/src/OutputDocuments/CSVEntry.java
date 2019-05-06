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
	
	public CSVAttribute getAttribute(String searchColumn){
		for(int i = 0; i < _attributes.size(); i++){
			if(_attributes.get(i).getColumn().compareTo(searchColumn)==0){
				return _attributes.get(i);
			}
		}
		return null;
	}
}
