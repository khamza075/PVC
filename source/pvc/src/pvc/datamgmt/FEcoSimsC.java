package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

//Class name is short for Fuel-Economy-Simulations-Completed -- it's mainly a list of headers
public class FEcoSimsC {
	private ArrayList<FEcoSimC> lst;
	private FFStructure fs;
	private int aID;
	
	
	public int numCompletedSims() {return lst.size();}
	public String[] getTitlesArray() {
		String[] arr = new String[lst.size()];
		for (int i=0; i<arr.length; i++) arr[i] = new String(lst.get(i).title);
		return arr;
	}
	public String getDescripton(int sID) {
		if (sID < 0) return "";
		if (sID >= lst.size()) return "";
		return new String(lst.get(sID).description);
	}
	public float getAnnualMiles(int sID) {
		if (sID < 0) return -1;
		if (sID >= lst.size()) return -1;
		return lst.get(sID).averageAnnualMiles;
	}
	public float getAnnualDrivingDays(int sID) {
		if (sID < 0) return -1;
		if (sID >= lst.size()) return -1;
		return lst.get(sID).averageAnnualDrivingDays;
	}
	
	
	public FEcoSimsC(FFStructure cFS, int analysisID) {
		lst = new ArrayList<FEcoSimC>();
		fs = cFS;
		aID = analysisID;
		
		int sID = 0;
		while (true) {
			String fecoSimSubfolder = fs.getFolderPath_fecoSims(aID, sID);
			
			File fFolder = new File(fecoSimSubfolder);
			if (fFolder.exists()) {
				String fname = fs.getFilePath_fecoSimsDescriptionFile(aID, sID);
				try {
					lst.add(new FEcoSimC(fname));
					sID++;				
				} catch (Exception e) {
					fs.deleteFEcoSimResult(aID, sID);
				}
			} else {
				break;
			}			
		}		
	}
	
	public static class FEcoSimC {
		public String title;
		public String description;
		public float averageAnnualMiles;
		public float averageAnnualDrivingDays;
		
		public FEcoSimC() {}
		private FEcoSimC(String fname) throws Exception {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			title = new String(fin.readLine());
			description = new String(fin.readLine());
			averageAnnualMiles = Float.parseFloat(fin.readLine());
			averageAnnualDrivingDays = Float.parseFloat(fin.readLine());
			fin.close();
		}
		public void saveToFile(String fname) {
			try {
				FileWriter fout = new FileWriter(fname);
				String lsep = System.getProperty("line.separator");
				
				fout.append(title+lsep);
				fout.append(description+lsep);
				fout.append(averageAnnualMiles+lsep);
				fout.append(averageAnnualDrivingDays+lsep);

				fout.flush();
				fout.close();
			} catch (Exception e) {}
		}
	}
	
	public void deleteFecoSim(int fecoSimID) {
		fs.deleteFEcoSimResult(aID, fecoSimID);
		lst.remove(fecoSimID);
	}
	public void editFecoSim(int fecoSimID, String title, String description, float averageAnnualMiles, float averageAnnualDrivingDays) {
		if (fecoSimID < 0) return;
		if (fecoSimID >= lst.size()) return;
		
		lst.get(fecoSimID).title = new String(title);
		lst.get(fecoSimID).description = new String(description);
		lst.get(fecoSimID).averageAnnualMiles = averageAnnualMiles;
		lst.get(fecoSimID).averageAnnualDrivingDays = averageAnnualDrivingDays;
		
		String fname = fs.getFilePath_fecoSimsDescriptionFile(aID, fecoSimID);
		lst.get(fecoSimID).saveToFile(fname);
	}
}
