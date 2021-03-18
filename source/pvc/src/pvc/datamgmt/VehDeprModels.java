package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import fastsimjava.FSJOneFileVehModel;
import pvc.calc.comp.VehDepreciation;
import pvc.datamgmt.comp.DepreciationDefaults;
import pvc.datamgmt.comp.PowertrainType;

public class VehDeprModels {
	private VehDepreciation[] vehDrpModels;
	public float residualValue(int vehID, float numYears, float mileageAtSale) {return vehDrpModels[vehID].residualValue(numYears, mileageAtSale);}
	
	public int numVehicles() {
		return vehDrpModels.length;
	}
	public VehDepreciation getVehDeprModel(int id) {
		return vehDrpModels[id];
	}
	public String getVehShortName(int id) {
		return avms.vehModelsSetup()[id].shortName;
	}
	
	private FFStructure fs;
	private int aID;
	private AnalysisVehModelsSetup avms;

	public VehDeprModels(FFStructure cFS, int analysisID,  AnalysisVehModelsSetup avmSetup) {
		fs = cFS;
		aID = analysisID;
		avms = avmSetup;
		
		try {
			readFromInputFile();
		} catch (Exception e) {
			initializeViaDefaults();
		}
		
		save();
	}
	
	public VehDeprModels(VehDeprModels other) {
		fs = other.fs;
		aID = other.aID;
		avms = other.avms;
		
		vehDrpModels = new VehDepreciation[other.vehDrpModels.length];
		for (int i=0; i<vehDrpModels.length; i++) vehDrpModels[i] = new VehDepreciation(other.vehDrpModels[i]);
	}

	private void readFromInputFile() throws Exception {
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();	
		vehDrpModels = new VehDepreciation[vms.length];
		
		String fname = fs.getFilePath_dereciationModels(aID);
		BufferedReader fin = new BufferedReader(new FileReader(fname));

		for (int i=0; i<vehDrpModels.length; i++) {
			fin.readLine();
			vehDrpModels[i] = new VehDepreciation(fin);
		}
		
		fin.close();
	}
	public void save() {
		String fname = fs.getFilePath_dereciationModels(aID);
		
		try {
			AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			for (int i=0; i<vehDrpModels.length; i++) {
				fout.append(vms[i].shortName+lsep);
				fout.append(vehDrpModels[i].toString()+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	private void initializeViaDefaults() {
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		
		vehDrpModels = new VehDepreciation[vms.length];
		for (int i=0; i<vehDrpModels.length; i++) {
			FSJOneFileVehModel offsModel = new FSJOneFileVehModel(fs.getFilePath_FASTSimVehModel(aID, vms[i].shortName));
			
			PowertrainType ptType = PowertrainType.decode(offsModel.vehModelParam);
			vehDrpModels[i] = DepreciationDefaults.getDepreciationModel(ptType);
		}
	}
}
