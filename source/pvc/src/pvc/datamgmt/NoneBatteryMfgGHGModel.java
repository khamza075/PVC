package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.comp.*;

public class NoneBatteryMfgGHGModel {
	private FFStructure fs;
	private int aID;
	
	public float vehicleLifetimeMiles;
	public float[] gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd;

	public NoneBatteryMfgGHGModel(FFStructure cFS, int analysisID, AnalysisVehModelsSetup avms) {
		fs = cFS;
		aID = analysisID;
		
		try {
			readFromInputFile(avms);
		} catch (Exception e) {
			initializeViaDefaults(avms);
		}
		
		save();
	}
	public NoneBatteryMfgGHGModel(NoneBatteryMfgGHGModel other) {
		fs = other.fs;
		aID = other.aID;
		
		vehicleLifetimeMiles = other.vehicleLifetimeMiles;
		
		gCO2perKgVehicle_lowEnd = new float[other.gCO2perKgVehicle_lowEnd.length];
		gCO2perKgVehicle_highEnd = new float[gCO2perKgVehicle_lowEnd.length];
		
		for (int i=0; i<gCO2perKgVehicle_lowEnd.length; i++) {
			gCO2perKgVehicle_lowEnd[i] = other.gCO2perKgVehicle_lowEnd[i];
			gCO2perKgVehicle_highEnd[i] = other.gCO2perKgVehicle_highEnd[i];			
		}
	}
	
	private void readFromInputFile(AnalysisVehModelsSetup avms) throws Exception {
		vehicleLifetimeMiles = FDefaults.vehicleDefaultLifetimeMiles;
		
		if (!avms.mfgGHGModelIncluded()) {
			gCO2perKgVehicle_lowEnd = null;
			gCO2perKgVehicle_highEnd = null;
			return;
		}

		String fname = fs.getFilePath_nonBatteryMfgGHG(aID);

		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();		
		readLine = fin.readLine();
		vehicleLifetimeMiles = Float.parseFloat(readLine);
		
		readLine = fin.readLine();
		ArrayList<Float> lstLow = new ArrayList<Float>();
		ArrayList<Float> lstHigh = new ArrayList<Float>();
		
		while ((readLine = fin.readLine())!=null) {
			String[] sp = readLine.split(",");
			lstLow.add(Float.parseFloat(sp[0]));
			lstHigh.add(Float.parseFloat(sp[1]));
		}
		
		fin.close();
		
		gCO2perKgVehicle_lowEnd = new float[lstLow.size()];
		gCO2perKgVehicle_highEnd = new float[gCO2perKgVehicle_lowEnd.length];
		
		for (int i=0; i<gCO2perKgVehicle_lowEnd.length; i++) {
			gCO2perKgVehicle_lowEnd[i] = lstLow.get(i);
			gCO2perKgVehicle_highEnd[i] = lstHigh.get(i);
		}
	}
	private void initializeViaDefaults(AnalysisVehModelsSetup avms) {
		vehicleLifetimeMiles = FDefaults.vehicleDefaultLifetimeMiles;
		
		if (!avms.mfgGHGModelIncluded()) {
			gCO2perKgVehicle_lowEnd = null;
			gCO2perKgVehicle_highEnd = null;
			return;
		}
		
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		
		gCO2perKgVehicle_lowEnd = new float[vms.length];
		gCO2perKgVehicle_highEnd = new float[vms.length];
		
		for (int i=0; i<vms.length; i++) {
			FSJOneFileVehModel offsModel = new FSJOneFileVehModel(fs.getFilePath_FASTSimVehModel(aID, vms[i].shortName));
			
			PowertrainType ptType = PowertrainType.decode(offsModel.vehModelParam);
			gCO2perKgVehicle_lowEnd[i] = NoneBatteryMfgGHGDefaults.lowEndValue(ptType);
			gCO2perKgVehicle_highEnd[i] = NoneBatteryMfgGHGDefaults.highEndValue(ptType);
		}
	}
	public void save() {
		if (gCO2perKgVehicle_lowEnd == null) return;
		
		String fname = fs.getFilePath_nonBatteryMfgGHG(aID);
		
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("__vehicleLifetimeMiles"+lsep);
			fout.append(""+vehicleLifetimeMiles+lsep);
			fout.append("gCO2perKgVehicle_lowEnd,gCO2perKgVehicle_highEnd"+lsep);

			for (int i=0; i<gCO2perKgVehicle_lowEnd.length; i++) {
				fout.append(""+gCO2perKgVehicle_lowEnd[i]+","+gCO2perKgVehicle_highEnd[i]+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
}
