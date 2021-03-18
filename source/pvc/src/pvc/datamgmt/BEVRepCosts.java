package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.datamgmt.comp.FDefaults;

public class BEVRepCosts {
	
	public float dollarsPerDay, dollarsPerMile;
	
	private FFStructure fs;
	private int aID;
	private WIITModel wiitMod;

	public BEVRepCosts(FFStructure cFS, int analysisID, WIITModel wiitModel) {
		fs = cFS;
		aID = analysisID;
		wiitMod = wiitModel;
		
		dollarsPerDay = FDefaults.bevRepCostPerDay;
		dollarsPerMile = FDefaults.bevRepCostPerMile;
		
		if (bevsWRepVehExist()) {
			readFromFile();
			save();
		}
	}
	
	private void readFromFile() {
		try {
			String fname = saveFileLocation();
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			dollarsPerDay = Float.parseFloat(readLine);
			readLine = fin.readLine();
			dollarsPerMile = Float.parseFloat(readLine);
			fin.close();
		} catch (Exception e) {}
	}
	public void save() {
		if (!bevsWRepVehExist()) return;
		
		try {
			String fname = saveFileLocation();
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			fout.append(""+dollarsPerDay+lsep);
			fout.append(""+dollarsPerMile+lsep);
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private String saveFileLocation() {
		return fs.getFilePath_bevRepCosts(aID);
	}
	
	private boolean bevsWRepVehExist() {
		if (!wiitMod.hasBEVs()) return false;
		if (wiitMod.bevRepModel.bevRepCommercial == null) return true;
		return false;
	}
}
