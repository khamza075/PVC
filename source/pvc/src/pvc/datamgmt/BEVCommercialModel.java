package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.comp.PowertrainType;

public class BEVCommercialModel {

	//Links
	private FFStructure fs;
	private int aID;
	private WIITModel wiitMod;

	//Data objects -- currently a simple look-up array
	private int[] nominalBEVRangeMiles;
	private float[] fractExtraVehiclesScaling;	
	
	//Constructor
	public BEVCommercialModel(FFStructure cFS, int analysisID, WIITModel wiitModel) {
		//Set links
		fs = cFS;
		aID = analysisID;
		wiitMod = wiitModel;

		//Nothing further to do if no commercial vehicles exist
		if (!bevsCommercialExist()) return;
		
		//Attempt to read from file
		try {
			readFromFile();
		} catch (Exception e) {
			//If unsuccessful reading, initialize via defaults
			initDefault();
		}
		
		//Save to file
		save();
	}
	
	private void readFromFile() throws Exception {
		String fname = saveFileLocation();
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		ArrayList<Integer> lstInt = new ArrayList<Integer>();
		ArrayList<Float> lstFlt = new ArrayList<Float>();
		
		while ((readLine = fin.readLine())!=null) {
			String[] sp = readLine.split(",");
			lstInt.add(Integer.parseInt(sp[0]));
			lstFlt.add(Float.parseFloat(sp[1]));
		}
				
		fin.close();
		
		nominalBEVRangeMiles = new int[lstInt.size()];
		fractExtraVehiclesScaling = new float[nominalBEVRangeMiles.length];
		
		for (int i=0; i<nominalBEVRangeMiles.length; i++) {
			nominalBEVRangeMiles[i] = lstInt.get(i);
			fractExtraVehiclesScaling[i] = lstFlt.get(i);
		}
	}
	
	private void initDefault() {
		try {
			AnalysisVehModelsSetup avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
			AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
			
			ArrayList<Integer> lstInt = new ArrayList<Integer>();
			
			for (int i=0; i<vms.length; i++) {
				int nomAER = (int)(vms[i].nominalAERMiles + 0.5f);
				if (nomAER > 0) {
					String fsVehModelFile = fs.getFilePath_FASTSimVehModel(aID, vms[i].shortName);
					FSJOneFileVehModel fsModel = new FSJOneFileVehModel(fsVehModelFile);
					
					if (PowertrainType.decode(fsModel.vehModelParam) == PowertrainType.bev) {
						lstInt.add(nomAER);
					}
				}
			}
			
			nominalBEVRangeMiles = new int[lstInt.size()];
			fractExtraVehiclesScaling = new float[nominalBEVRangeMiles.length];
			
			for (int i=0; i<nominalBEVRangeMiles.length; i++) {
				nominalBEVRangeMiles[i] = lstInt.get(i);
				fractExtraVehiclesScaling[i] = 1f;
			}
			
		} catch (Exception e) {
			nominalBEVRangeMiles = null;
			fractExtraVehiclesScaling = null;
		}
	}
	
	//Function for calculating fraction of extra vehicles
	public float fracExtraVehicles(float nominalFracExtraVeh, int nominalRangeMiles) {
		try {
			for (int i=0; i<nominalBEVRangeMiles.length; i++) {
				if (nominalBEVRangeMiles[i] == nominalRangeMiles) return fractExtraVehiclesScaling[i]*nominalFracExtraVeh;
			}
		} catch (Exception e) {}
		return nominalFracExtraVeh;
	}
	
	//Function for saving to file
	public void save() {
		if (!bevsCommercialExist()) return;
		
		try {
			String fname = saveFileLocation();
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("nominalBEVRangeMiles,fractExtraVehiclesScaling"+lsep);
			for (int i=0; i<nominalBEVRangeMiles.length; i++) {
				fout.append(""+nominalBEVRangeMiles[i]+","+fractExtraVehiclesScaling[i]+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	//Internal use functions
	private String saveFileLocation() {
		return fs.getFilePath_bevMoreCommercialVeh(aID);
	}	
	private boolean bevsCommercialExist() {
		if (!wiitMod.hasBEVs()) return false;
		if (wiitMod.bevRepModel.bevRepCommercial == null) return false;
		return true;
	}
}
