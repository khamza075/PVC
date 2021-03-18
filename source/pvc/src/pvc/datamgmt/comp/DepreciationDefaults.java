package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;

import pvc.calc.comp.VehDepreciation;

public class DepreciationDefaults {
	
	public static VehDepreciation[] defaultsByPtType = {
			new VehDepreciation(),new VehDepreciation(),new VehDepreciation(),new VehDepreciation(),
			new VehDepreciation(),new VehDepreciation(),new VehDepreciation(),new VehDepreciation(),
			new VehDepreciation(),new VehDepreciation(),new VehDepreciation(),new VehDepreciation()
	};

	
	private DepreciationDefaults() {}
	
	public static void readfromFile(String fname) {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine;
			
			while ((readLine = fin.readLine())!=null) {
				int id = PowertrainType.decode(readLine).ordinal();				
				defaultsByPtType[id] = new VehDepreciation(fin);
			}
			
			fin.close();
		} catch (Exception e) {}
	}

	public static VehDepreciation getDepreciationModel(PowertrainType ptType) {
		return defaultsByPtType[ptType.ordinal()];
	}
}
