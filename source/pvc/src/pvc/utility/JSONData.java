package pvc.utility;

import java.util.ArrayList;

public class JSONData {
	private CType cType;
	private String cName, cText;
	private float cValue;
	private int iValue;
	private ArrayList<JSONData> lst;
	
	public JSONData(String name) {
		cName = new String(name);
		lst = new ArrayList<JSONData>();
		cType = CType.Container;
	}
	public JSONData(String name, String txtString) {
		cName = new String(name);
		cText = new String(txtString);
		cType = CType.TxtString;
	}
	public JSONData(String name, int value) {
		cName = new String(name);
		iValue = value;
		cType = CType.NumInt;
	}
	public JSONData(String name, float value) {
		cName = new String(name);
		cValue = value;
		cType = CType.NumFloat;
	}
	
	public void add(JSONData jsonData) {
		lst.add(jsonData);
	}
	
	private String toString(int curLevel) {
		String lsep = System.getProperty("line.separator");
		String tabChar = "	";
		String indent = "";
		String stOut = "";
		
		if (curLevel <= 0) {
			if (cType != CType.Container) {
				stOut = "{" + lsep + tabChar;
				switch (cType) {
				case NumFloat:
					stOut = stOut + cValue;
					break;
				case NumInt:
					stOut = stOut + iValue;
					break;
				case TxtString:
					stOut = stOut + "\"" + cText + "\"";
					break;
				default:
					break;				
				}
				stOut = stOut + "}";
				
				return stOut;
			}
			
			if (lst.size() < 1) return "{}";
			
			stOut = "{";
			for (int i=0; i<lst.size()-1; i++) {
				stOut = stOut + lsep + lst.get(i).toString(1) + ",";
			}
			stOut = stOut + lsep + lst.get(lst.size()-1).toString(1);
			stOut = stOut + lsep + "}";
			return stOut;
		}
		
		for (int i=0; i<curLevel; i++) indent = indent + tabChar;
		
		if (cType == CType.NumFloat) return indent + "\"" + cName + "\" : " + cValue;
		if (cType == CType.NumInt) return indent + "\"" + cName + "\" : " + iValue;
		if (cType == CType.TxtString) return indent + "\"" + cName + "\" : \"" + cText + "\"";
		
		if (lst.size() < 1) return "{\"" + cName + "\"}";
		
		stOut = indent + "\"" + cName + "\" : {";
		
		for (int i=0; i<lst.size()-1; i++) {
			stOut = stOut + lsep + lst.get(i).toString(curLevel+1) + ",";
		}
		stOut = stOut + lsep + lst.get(lst.size()-1).toString(curLevel+1);
		stOut = stOut + lsep + indent + "}";
		return stOut;
	}
	
	@Override public String toString() {
		return toString(0);
	}
		
	public void utl_showContentsOneLevel() {
		switch (cType) {
		case Container:
			for (int i=0; i<lst.size(); i++) {
				if (lst.get(i) == null) System.out.println("null");
				else System.out.println(lst.get(i).cName + " --> " + cType);
			}
			break;
		case NumFloat:
			System.out.println(cName + " --> " + cType + ": " + cValue);
			break;
		case NumInt:
			System.out.println(cName + " --> " + cType + ": " + iValue);
			break;
		case TxtString:
			System.out.println(cName + " --> " + cType + ": " + cText);
			break;
		}
	}
	
	
	private enum CType {
		NumFloat, NumInt, TxtString, Container
	}
}
