package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import fastsimjava.FSJOneFileVehModel;

public class AnalysisVehModelsSetup {
	private AVehModelSetup[] vehModelsSetup;
	
	public int numVehModels() {return vehModelsSetup.length;}
	public AVehModelSetup[] vehModelsSetup() {return vehModelsSetup;}
	public boolean mfgGHGModelIncluded() {
		if (vehModelsSetup == null) return false;
		if (vehModelsSetup.length < 1) return false;
		return vehModelsSetup[0].hasManufacturingGHGModel;
	}
	public boolean includesIncentives() {
		if (vehModelsSetup == null) return false;
		for (int i=0; i<vehModelsSetup.length; i++) {
			if (vehModelsSetup[i].firstYearTotEqIncentives > 0) return true;
		}
		return false;
	}
	
	public static AnalysisVehModelsSetup readAnalysisVehModelsSetup(FFStructure fs, int aID) {
		try {
			return new AnalysisVehModelsSetup(fs, aID);
		} catch (Exception e) {
			return null;
		}
	}

	public AnalysisVehModelsSetup(FFStructure fs, int aID) throws Exception {
		String fname = fs.getFilePath_vehList(aID);
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		ArrayList<AVehModelSetup> lst = new ArrayList<AVehModelSetup>();
		
		while ((readLine = fin.readLine())!=null) {
			AVehModelSetup vms = new AVehModelSetup(readLine);			
			boolean vehModelIsValid = true;
			
			String vehModelFileName = fs.getFilePath_FASTSimVehModel(aID, vms.shortName);
			String vehModelOfBaseVehicleFileName = fs.getFilePath_FASTSimVehModel(aID, vms.shortNameOfVehicleDerivedFrom);
			
			try {
				FSJOneFileVehModel ofvModel = new FSJOneFileVehModel(vehModelFileName);
				if (ofvModel.vehModelParam == null) vehModelIsValid = false;
			} catch (Exception e) {
				vehModelIsValid = false;
			}
			if (vms.isDerivedVehicle) {
				try {
					FSJOneFileVehModel ofvModel = new FSJOneFileVehModel(vehModelOfBaseVehicleFileName);
					if (ofvModel.vehModelParam == null) vehModelIsValid = false;
				} catch (Exception e) {
					vehModelIsValid = false;
				}
			}
			
			if (vehModelIsValid) lst.add(vms);
		}
		
		fin.close();
			
		vehModelsSetup = new AVehModelSetup[lst.size()];
		for (int i=0; i<vehModelsSetup.length; i++) vehModelsSetup[i] = lst.get(i);
	}
	
	public static class AVehModelSetup {
		public String shortName;	//Short name identifies the FASTSim model (in same e folder, appended .csv) file and PDF documentation (in _refData folder, appended .pdf)
		public boolean isDerivedVehicle;				//If TRUE, it means the vehicle is "virtual" and derived from another one in the list	
		public String shortNameOfVehicleDerivedFrom;	//Only for derived vehicle  (i.e. virtual vehicle that doesn't exist in the real world yet) models
		public float baseScenarioUSDollarsToBuy;		//Only for non-derived vehicles (i.e. one that we know actual price in the market)

		public float nominalAERMiles;
		
		public float firstYearTotEqIncentives;
		public boolean hasManufacturingGHGModel;
		public float mfgGHG_gCO2perKWhBattery;
		
		private AVehModelSetup(String readLine) {
			String[] sp = readLine.split(",");
			shortName = sp[0];
			isDerivedVehicle = Boolean.parseBoolean(sp[1]);
			shortNameOfVehicleDerivedFrom = sp[2];
			baseScenarioUSDollarsToBuy = Float.parseFloat(sp[3]);
			
			nominalAERMiles = Float.parseFloat(sp[4]);			
			firstYearTotEqIncentives = Float.parseFloat(sp[5]);
			
			if (sp.length == 7) hasManufacturingGHGModel = true;
			else {
				hasManufacturingGHGModel = false;
				return;
			}
			mfgGHG_gCO2perKWhBattery = Float.parseFloat(sp[6]);
		}
	}
}
