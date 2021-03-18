package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.comp.*;

public class LicIMModel {
	private float[] licDollarPerYear, insDollarPerYear, maintDollarPerMile;
	
	public float getAnnualLicensingCost(int vehID) {return licDollarPerYear[vehID];}
	public float getAnnualInsuranceCost(int vehID) {return insDollarPerYear[vehID];}
	public float getMaintnenaceCostPerMile(int vehID) {return maintDollarPerMile[vehID];}
	
	public void setAnnualLicensingCost(int vehID, float value) {licDollarPerYear[vehID] = value;}
	public void setAnnualInsuranceCost(int vehID, float value) {insDollarPerYear[vehID] = value;}
	public void setMaintnenaceCostPerMile(int vehID, float value) {maintDollarPerMile[vehID] = value;}
	
	private FFStructure fs;
	private int aID;


	public LicIMModel(FFStructure cFS, int analysisID, AnalysisVehModelsSetup avms) {
		fs = cFS;
		aID = analysisID;
		
		try {
			readFromInputFile(avms);
		} catch (Exception e) {
			initializeViaDefaults(avms);
		}
		
		save();
	}
	public LicIMModel(LicIMModel other) {
		fs = other.fs;
		aID = other.aID;
		
		licDollarPerYear = new float[other.licDollarPerYear.length];
		insDollarPerYear = new float[licDollarPerYear.length];
		maintDollarPerMile = new float[licDollarPerYear.length];
		
		for (int i=0; i<licDollarPerYear.length; i++) {
			licDollarPerYear[i] = other.licDollarPerYear[i];
			insDollarPerYear[i] = other.insDollarPerYear[i];
			maintDollarPerMile[i] = other.maintDollarPerMile[i];
		}
	}
	private void readFromInputFile(AnalysisVehModelsSetup avms) throws Exception {
		String fname = fs.getFilePath_licInsMaintCostModels(aID);

		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		ArrayList<Float> lstLic = new ArrayList<Float>();
		ArrayList<Float> lstInc = new ArrayList<Float>();
		ArrayList<Float> lstMnt = new ArrayList<Float>();

		while ((readLine = fin.readLine())!=null) {
			String[] sp = readLine.split(",");
			lstLic.add(Float.parseFloat(sp[0]));
			lstInc.add(Float.parseFloat(sp[1]));
			lstMnt.add(Float.parseFloat(sp[2]));
		}
		
		fin.close();

		licDollarPerYear = new float[lstLic.size()];
		insDollarPerYear = new float[licDollarPerYear.length];
		maintDollarPerMile = new float[licDollarPerYear.length];
		
		for (int i=0; i<licDollarPerYear.length; i++) {
			licDollarPerYear[i] = lstLic.get(i);
			insDollarPerYear[i] = lstInc.get(i);
			maintDollarPerMile[i] = lstMnt.get(i);
		}
	}
	public void save() {
		String fname = fs.getFilePath_licInsMaintCostModels(aID);
		
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("licDollarPerYear,insDollarPerYear,maintDollarPerMile"+lsep);

			for (int i=0; i<licDollarPerYear.length; i++) {
				fout.append(""+licDollarPerYear[i]+","+insDollarPerYear[i]+","+maintDollarPerMile[i]+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	private void initializeViaDefaults(AnalysisVehModelsSetup avms) {
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		
		licDollarPerYear = new float[vms.length];
		insDollarPerYear = new float[vms.length];
		maintDollarPerMile = new float[vms.length];
		
		for (int i=0; i<vms.length; i++) {
			FSJOneFileVehModel offsModel = new FSJOneFileVehModel(fs.getFilePath_FASTSimVehModel(aID, vms[i].shortName));
			
			PowertrainType ptType = PowertrainType.decode(offsModel.vehModelParam);
			licDollarPerYear[i] = LIMDefaults.licencingDollarPerYear(ptType);
			insDollarPerYear[i] = LIMDefaults.insuranceDollarPerYear(ptType);
			maintDollarPerMile[i] = LIMDefaults.maintenanceDollarPerMile(ptType);
		}
	}
}
