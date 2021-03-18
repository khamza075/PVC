package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.datamgmt.comp.FDefaults;

public class HomeChargerCosts {
	
	public float homeChargerCostL1, homeChargerCostL2;
	
	private FFStructure fs;
	private int aID;

	public HomeChargerCosts(FFStructure cFS, int analysisID) {
		fs = cFS;
		aID = analysisID;

		homeChargerCostL1 = FDefaults.homeChargerL1dollars;
		homeChargerCostL2 = FDefaults.homeChargerL2dollars;
		
		readFromFile();
		save();
	}
	private void readFromFile() {
		try {
			String fname = saveFileLocation();
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			homeChargerCostL1 = Float.parseFloat(readLine);
			readLine = fin.readLine();
			homeChargerCostL2 = Float.parseFloat(readLine);
			fin.close();
		} catch (Exception e) {}
	}
	public void save() {
		try {
			String fname = saveFileLocation();
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			fout.append(""+homeChargerCostL1+lsep);
			fout.append(""+homeChargerCostL2+lsep);
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private String saveFileLocation() {
		return fs.getFilePath_homeChargerCosts(aID);
	}
}
