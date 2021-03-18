package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;

public class LIMDefaults {
	private static float[] licensing_dollarPerYear = {285,285,285, 285,285,285,285, 285,285,285,285, 285};
	private static float[] insurance_dollarPerYear = {880,880,880, 880,880,880,880, 880,880,880,880, 880};
	private static float[] maintenance_dollarPerMile = {0.090f,0.090f,0.090f, 0.075f,0.075f,0.075f,0.075f, 0.045f,0.045f,0.045f,0.045f, 0.045f};

	
	private LIMDefaults() {}
	
	public static void readfromFile(String fname) {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			while ((readLine = fin.readLine())!=null) {
				String[] sp = readLine.split(",");
				int id = PowertrainType.decode(sp[0]).ordinal();
				licensing_dollarPerYear[id] = Float.parseFloat(sp[1]);
				insurance_dollarPerYear[id] = Float.parseFloat(sp[2]);
				maintenance_dollarPerMile[id] = Float.parseFloat(sp[3]);
			}
			
			fin.close();
		} catch (Exception e) {}
	}

	public static float licencingDollarPerYear(PowertrainType ptType) {
		return licensing_dollarPerYear[ptType.ordinal()];
	}
	public static float insuranceDollarPerYear(PowertrainType ptType) {
		return insurance_dollarPerYear[ptType.ordinal()];
	}
	public static float maintenanceDollarPerMile(PowertrainType ptType) {
		return maintenance_dollarPerMile[ptType.ordinal()];
	}

}
