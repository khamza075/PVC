package pvc.datamgmt.comp;

public enum ChargerTypes {
	L1("L1","AC Level-1 (110/120V)", 128, 128, 128),
	L2("L2","AC Level-2 (240V)", 0, 0, 255),
	DC("DC-Fast","DC Fast Charger", 255, 128, 0)
	;
	public String shortName, description;
	public int defaultColorRed, defaultColorGreen, defaultColorBlue;
	
	private ChargerTypes(String sName, String lName, int r, int g, int b) {
		shortName = sName;
		description = lName;
		defaultColorRed = r;
		defaultColorGreen = g;
		defaultColorBlue = b;
	}
}
