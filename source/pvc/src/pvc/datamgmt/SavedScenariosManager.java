package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class SavedScenariosManager {
	private FFStructure fs;
	private int aID;
	private ArrayList<SavedScenarioDescriptor> lst;
	
	//Constructor -- should not reach the point of calling this until fuel economy summaries has finished processing
	public SavedScenariosManager(FFStructure cFS, int analysisID) {
		fs = cFS;
		aID = analysisID;
		
		try {
			initFromExisting();
		} catch (Exception e) {
			initFromScratch();
		}		
	}
	
	//Initialize via reading existing file and folder structure
	private void initFromExisting() throws Exception {
		lst = new ArrayList<SavedScenarioDescriptor>();
		
		BufferedReader fin = new BufferedReader(new FileReader(fs.getFilePath_savedScenariosDescription(aID)));
		String readLine = fin.readLine();
		
		while ((readLine = fin.readLine())!=null) {
			lst.add(new SavedScenarioDescriptor(readLine));
		}
		
		fin.close();
		
		//Ensure that at least the baseline default exists
		int numScenarios = lst.size();
		if (numScenarios < 1) throw new Exception();
		
		//Ensure that there are in fact enough sub-folders to match the list size
		for (int i=0; i<numScenarios; i++) {
			File subFolder = new File(fs.getFolderPath_savedScenariosSubFolder(aID, i));
			if (!subFolder.exists()) throw new Exception();
		}
	}
	//Function for saving
	private void save() {
		fs.ensureSavedScenariosFolderExists(aID);
		
		try {
			FileWriter fout = new FileWriter(fs.getFilePath_savedScenariosDescription(aID));
			String lsep = System.getProperty("line.separator");
			
			fout.append("shortDescription,longDescription"+lsep);
			
			for (int i=0; i<lst.size(); i++) {
				fout.append(lst.get(i).toString()+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	//Initialize via creating new baseline default
	private void initFromScratch() {
		//Clean-up anything already existing
		String savedScenariosFolder = fs.getFolderPath_savedScenariosRoot(aID);
		FFStructure.deleteFolderAndContents(new File(savedScenariosFolder));
		
		//Re-create saved scenarios root folder
		fs.ensureSavedScenariosFolderExists(aID);
		
		//Adjust the list
		lst = new ArrayList<SavedScenarioDescriptor>();
		
		//Make current the baseline default
		saveCurrentSateAsNewScenario("(Baseline Default)", "Reset everything to default values for current analysis.");
				
		//Save
		save();
	}
	
	//Function to save current settings as a new scenario
	public void saveCurrentSateAsNewScenario(String stShort, String stLong) {
		//Update the list
		lst.add(new SavedScenarioDescriptor(stShort+","+stLong));
		
		//Create new sub-folder
		int ssID = lst.size()-1;
		File f = new File(fs.getFolderPath_savedScenariosSubFolder(aID, ssID));
		f.mkdirs();
		
		//Copy files
		String[] stSrc = fs.scenarioFilesInCustomFolder(aID);
		String[] stDst = fs.scenarioFilesInScenarioFolder(aID, ssID);
		
		for (int i=0; i<stSrc.length; i++) {
			File fSrc = new File(stSrc[i]);
			if (fSrc.exists()) FFStructure.copyContentsWOverrite(stSrc[i], stDst[i]);
		}
		
		//Save
		save();
	}
	
	
	//Function to load an existing scenario
	public void loadScenarioFiles(int ssID) {
		String[] stSrc = fs.scenarioFilesInScenarioFolder(aID, ssID);
		String[] stDst = fs.scenarioFilesInCustomFolder(aID);
		
		for (int i=0; i<stSrc.length; i++) {
			File fSrc = new File(stSrc[i]);
			if (fSrc.exists()) FFStructure.copyContentsWOverrite(stSrc[i], stDst[i]);
		}
	}
	
	//Function to delete an existing scenario
	public void deleteScenario(int ssID) {
		if (ssID < 1) return;
		if (ssID >= lst.size()) return;
		
		//Delete sub-folder and contents
		String folder = fs.getFolderPath_savedScenariosSubFolder(aID, ssID);
		FFStructure.deleteFolderAndContents(new File(folder));
		
		//Re-name sub-folders with indices higher than current
		int numSc = lst.size();
		for (int i=ssID+1; i<numSc; i++) {
			String orgSubFolderName = fs.getFolderPath_savedScenariosSubFolder(aID, i);
			String newSubFolderName = fs.getFolderPath_savedScenariosSubFolder(aID, i-1);
			
			File f = new File(orgSubFolderName);
			f.renameTo(new File(newSubFolderName));
		}
		
		//Remove from list
		lst.remove(ssID);
		//Save
		save();
	}
	
	//Function for switching the order of two existing scenarios in the list
	public void switchOrder(int id1, int id2) {
		//Safety checks
		if (id1 < 1) return;
		if (id1 >= lst.size()) return;
		if (id2 < 1) return;
		if (id2 >= lst.size()) return;
		if (id1 == id2) return;

		//Rename sub-folders
		int numSc = lst.size();		
		String nameS1 = fs.getFolderPath_savedScenariosSubFolder(aID, id1);
		String nameS2 = fs.getFolderPath_savedScenariosSubFolder(aID, id2);
		String nameSTmp = fs.getFolderPath_savedScenariosSubFolder(aID, numSc);
		
		//Switch #1 to Tmp name
		File f1 = new File(nameS1);
		f1.renameTo(new File(nameSTmp));
		
		//Switch #2 to #1 name
		File f2 = new File(nameS2);
		f2.renameTo(new File(nameS1));
		
		//Switch Temporary name to #2 name
		File ft = new File(nameSTmp);
		ft.renameTo(new File(nameS2));

		//Re-order the items in the list
		int lowID = Math.min(id1,  id2);
		int highID = Math.max(id1,  id2);
		
		SavedScenarioDescriptor tmpLow = lst.get(lowID);
		SavedScenarioDescriptor tmpHigh = lst.get(highID);
		
		lst.remove(highID);
		lst.remove(lowID);
		
		lst.add(lowID, tmpHigh);
		lst.add(highID, tmpLow);
		
		//Save
		save();
	}
	
	//Function to return an array of long descriptions
	public String[] getLongDescriptions() {
		String[] st = new String[lst.size()];
		for (int i=0; i<st.length; i++) st[i] = new String(lst.get(i).longDescription);
		return st;
	}
	//Function to return an array of short descriptions
	public String[] getShortDescriptions() {
		String[] st = new String[lst.size()];
		for (int i=0; i<st.length; i++) st[i] = new String(lst.get(i).shortDescription);
		return st;
	}
	public int numScenarios() {
		return lst.size();
	}
	

	private class SavedScenarioDescriptor {
		private String shortDescription;	//Commas not allowed
		private String longDescription;		//No restrictions
		
		private SavedScenarioDescriptor(String readLine) {
			String[] sp = readLine.split(",");
			
			shortDescription = new String(sp[0]);
			
			longDescription = new String(sp[1]);
			for (int i=2; i<sp.length; i++) longDescription = longDescription + "," + sp[i];
		}
		
		@Override public String toString() {
			return shortDescription + "," + longDescription;
		}
	}
}
