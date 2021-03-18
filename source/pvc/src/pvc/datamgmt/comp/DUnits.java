package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.datamgmt.FFStructure;

public class DUnits {	
	//Preset-constants
	private static final float MetersPerMile = 1609f;
	private static final float LitersPerGallon = 3.78541f;
	private static final float PoundsPerKilogram = 2.20462f;
	
	private static final String Kanji_man = "\u4e07";
	private static final String Kanji_nichi = "\u65e5";
	private static final String Kanji_hon = "\u672c";
	private static final String Kanji_en = "\u5186";

	//Preset arrays
	private static final String[] Distance_sNames = {"mi", "km"};
	private static final String[] Distance_lNames = {"Mile", "Kilometer"};
	private static final float[] Distance_convV = {1f, 1000f/MetersPerMile};
	
	private static final String[] LiquidFuelUnit_sNames = {"gal", "ltr"};
	private static final String[] LiquidFuelUnit_lNames = {"Gallon", "Liter"};
	private static final float[] LiquidFuelUnit_convV = {1f, 1f/LitersPerGallon};
	
	private static final String[] NGUnit_sNames = {"m3"};
	private static final String[] NGUnit_lNames = {"Cubic Meter"};
	private static final float[] NGUnit_convV = {1f};
	
	private static final String[] H2Unit_sNames = {"kg-H2"};
	private static final String[] H2Unit_lNames = {"Kilogram of Hydrogen"};
	private static final float[] H2Unit_convV = {1f};
	
	private static final String[] ElectUnit_sNames = {"kWh"};
	private static final String[] ElectUnit_lNames = {"KiloWatt-Hour"};
	private static final float[] ElectUnit_convV = {1f};
	
	private static final String[] PowerUnit_sNames = {"kW"};
	private static final String[] PowerUnit_lNames = {"KiloWatt"};
	private static final float[] PowerUnit_convV = {1f};
	
	private static final String[] GHGUnit_sNames = {"g-CO2"};
	private static final String[] GHGUnit_lNames = {"Grams of Carbon Dioxide"};
	private static final float[] GHGUnit_convV = {1f};
	
	private static final String[] VehMassUnit_sNames = {"kg", "lb"};
	private static final String[] VehMassUnit_lNames = {"Kilogram", "Pound"};
	private static final float[] VehMassUnit_convV = {1f, 1f/PoundsPerKilogram};
	
	private static final String[] MoneyUnit_sNames = {"$", Kanji_en};
	private static final String[] MoneyUnit_lNames = {"US Dollar", Kanji_nichi + Kanji_hon + Kanji_en};
	private static final float[] MoneyUnit_convV = {1f, 1f/FDefaults.JapanYenPerDollar};
	
	private static final String[] LargeMoneyUnit_sNames = {"$1000", Kanji_man+Kanji_en};
	private static final String[] LargeMoneyUnit_lNames = {"1000 US Dollars", Kanji_nichi+Kanji_hon + " "+Kanji_man+Kanji_en};
	private static final float[] LargeMoneyUnit_convV = {1f, 10f/FDefaults.JapanYenPerDollar};

	
	
	
	//Types of units that user may select among
	public enum UnitType {
		Distance("Travel Distance", Distance_sNames, Distance_lNames, Distance_convV),
		
		LiquidFuelUnit("Liquid Fuels Unit", LiquidFuelUnit_sNames, LiquidFuelUnit_lNames, LiquidFuelUnit_convV),
		NGUnit("Natural Gas Unit", NGUnit_sNames, NGUnit_lNames, NGUnit_convV),
		H2Unit("Hydrogen Fuel Unit", H2Unit_sNames, H2Unit_lNames, H2Unit_convV),
		ElectUnit("Electric Energy Unit", ElectUnit_sNames, ElectUnit_lNames, ElectUnit_convV),

		PowerUnit("Power Unit", PowerUnit_sNames, PowerUnit_lNames, PowerUnit_convV),

		GHGUnit("Green-House Gas Emissions", GHGUnit_sNames, GHGUnit_lNames, GHGUnit_convV),
		VehMassUnit("Vehicle Mass", VehMassUnit_sNames, VehMassUnit_lNames, VehMassUnit_convV),
		
		MoneyUnit("Money Amount", MoneyUnit_sNames, MoneyUnit_lNames, MoneyUnit_convV),
		LargeMoneyUnit("Large Money Amount", LargeMoneyUnit_sNames, LargeMoneyUnit_lNames, LargeMoneyUnit_convV),
		;
		private String unitTypeLongCaption;
		private String[] choicesShortNames, choicesLongNames;
		private float[] multiplyToConvertToCalc;
		
		private UnitType(String caption, String[] sNames, String[] lNames, float[] convValues) {
			unitTypeLongCaption = caption;
			choicesShortNames = sNames;
			choicesLongNames = lNames;
			multiplyToConvertToCalc = convValues;			
		}
	}
	
	public static String getShortName(int uOrdinal) {return UnitType.values()[uOrdinal].choicesShortNames[userChoices[uOrdinal]];}
	public static String getShortName(UnitType unitType) {return getShortName(unitType.ordinal());}
	
	public static String getLongName(int uOrdinal) {return UnitType.values()[uOrdinal].choicesLongNames[userChoices[uOrdinal]];}
	public static String getLongName(UnitType unitType) {return getLongName(unitType.ordinal());}
	
	public static float convConstMPtoBCalc(int uOrdinal) {return UnitType.values()[uOrdinal].multiplyToConvertToCalc[userChoices[uOrdinal]];}
	public static float convConstMPtoBCalc(UnitType unitType) {return convConstMPtoBCalc(unitType.ordinal());}
		
	public static String[] quantities() {
		String[] st = new String[UnitType.values().length];
		for (int i=0; i<st.length; i++) st[i] = new String(UnitType.values()[i].unitTypeLongCaption);
		return st;
	}
	public static String[] quantityOptionsLongNames(int uOrdinal) {
		UnitType unitType = UnitType.values()[uOrdinal];
		String[] st = new String[unitType.choicesLongNames.length];
		for (int i=0; i<st.length; i++) st[i] = new String(unitType.choicesLongNames[i]);
		return st;
	}
	public static void setUserUnitSelection(int uOrdinal, int selectionID) {
		userChoices[uOrdinal] = selectionID;
	}
	public static int getUserUnitSelection(int uOrdinal) {return userChoices[uOrdinal];}
	
	//Currently set choices
	private static int[] userChoices = new int[UnitType.values().length];
	
	//Prevent instantiation
	private DUnits() {}
	
	//Function for reading the constants from file
	public static void readFromFile(FFStructure fs, int aID) {
		try {
			String fname = fs.getFilePath_DUnits(aID);
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			
			for (int i=0; i<userChoices.length; i++) {
				String readLine = fin.readLine();
				userChoices[i] = Integer.parseInt(readLine);
			}
			
			fin.close();
		} catch (Exception e) {}
	}
	
	//Function for writing the constants to file
	public static void writeToFile(FFStructure fs, int aID) {
		try {
			String fname = fs.getFilePath_DUnits(aID);
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			for (int i=0; i<userChoices.length; i++) {
				fout.append(""+userChoices[i]+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}	
}
