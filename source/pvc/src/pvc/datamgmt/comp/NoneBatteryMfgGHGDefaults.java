package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;

public class NoneBatteryMfgGHGDefaults {
	private static float[] defult_gCO2_per_kgVehicle_lowEnd = {
			4200f,
			4200f,
			4200f,
			
			4100f,
			4100f,
			4100f,
			4100f,
			
			4000f,
			4000f,
			4000f,
			4100f,

			4000f
			};
	private static float[] defult_gCO2_per_kgVehicle_highEnd = {
			7800f,
			7800f,
			7800f,
			
			7500f,
			7500f,
			7500f,
			7900f,
			
			7300f,
			7300f,
			7300f,
			7900f,
			
			7900f
			};

	private NoneBatteryMfgGHGDefaults() {}
	
	public static void readfromFile(String fname) {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			while ((readLine = fin.readLine())!=null) {
				String[] sp = readLine.split(",");
				int id = PowertrainType.decode(sp[0]).ordinal();
				defult_gCO2_per_kgVehicle_lowEnd[id] = Float.parseFloat(sp[1]);
				defult_gCO2_per_kgVehicle_highEnd[id] = Float.parseFloat(sp[2]);
			}
			
			fin.close();
		} catch (Exception e) {}
	}
	
	public static float lowEndValue(PowertrainType ptType) {
		return defult_gCO2_per_kgVehicle_lowEnd[ptType.ordinal()];
	}
	public static float highEndValue(PowertrainType ptType) {
		return defult_gCO2_per_kgVehicle_highEnd[ptType.ordinal()];
	}
}
