package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class FFStructure {
	//Root folder
	private String rootFolder;
	
	//List of Folders with Vehicle Analysis
	private String[] analysisFolders;
	public String[] analysisFolders() {return analysisFolders;}
		

	//Creation function
	public static FFStructure createFS(String rFolder) {
		try {
			return new FFStructure(rFolder);
		} catch (Exception e) {
			return null;
		}
	}
	//Static function to check root folder validity
	public static boolean rootFolderIsValid(String rFolder) {
		String[] fMustExistList  = {
				rFolder+FFStrings.files_byPowerTrain_depreciation.s,
				rFolder+FFStrings.files_byPowerTrain_licMaint.s,
				rFolder+FFStrings.files_byPowerTrain_nbMfgGHG.s,
				rFolder+FFStrings.files_defaultSettings.s,
		};
		
		for (int i=0; i<fMustExistList.length; i++) {
			File f = new File(fMustExistList[i]);
			if (!f.exists()) return false;
		}
		
		return true;
	}	
	//Static function to check if a vehicle analysis sub-folder is valid
	private static boolean vaSubFolderIsValid(String subFolder) {
		String[] fMustExistList  = {
				subFolder + FFStrings.vmfiles_title.s,
				subFolder + FFStrings.vmfiles_vehList.s,
				subFolder + FFStrings.vmfiles_baseModels.s,						
				subFolder + FFStrings.vmfolders_refData.s,
		};
		
		for (int i=0; i<fMustExistList.length; i++) {
			File f = new File(fMustExistList[i]);
			if (!f.exists()) return false;
		}
		
		return true;
	}
	//private constructor to prevent instantiation except via static function
	private FFStructure(String rFolder) throws Exception {
		if (!rootFolderIsValid(rFolder)) throw new Exception("Invalid Root Folder");
		rootFolder = new String(rFolder);
		
		File frFolder = new File(rootFolder);
		File[] subFs = frFolder.listFiles();
		ArrayList<String> lst = new ArrayList<String>();
		
		for (int i=0; i<subFs.length; i++) {
			if (subFs[i].isDirectory()) {
				String curFolderName = subFs[i].getName();
				String firstChar = curFolderName.substring(0, 1);
				if (!firstChar.equalsIgnoreCase("_")) {
					String subFolder = rootFolder + slashChar() + curFolderName;
					if (vaSubFolderIsValid(subFolder)) {
						lst.add(curFolderName);
					}
				}
			}
		}
		
		analysisFolders = new String[lst.size()];
		for (int i=0; i<analysisFolders.length; i++) analysisFolders[i] = lst.get(i);
		
		//Check that title, description and vehicle models are correctly setup (basically removing invalid entries)
		lst = new ArrayList<String>();
		for (int i=0; i<analysisFolders.length; i++) {
			boolean isValidFolder = true;
			try {
				new AnalysisTitleDescription(this, i);
				AnalysisVehModelsSetup avms = new AnalysisVehModelsSetup(this, i);
				if (avms.numVehModels() < 1) isValidFolder = false;
				
				new WIITModel(this, i, avms);
				
			} catch (Exception e) {
				isValidFolder = false;
			}
			
			if (isValidFolder) lst.add(analysisFolders[i]);
		}
		
		//Set list to valid entries
		analysisFolders = new String[lst.size()];
		for (int i=0; i<analysisFolders.length; i++) analysisFolders[i] = lst.get(i);
	}
	
	//String constants enumeration
	public enum FFStrings {
		folders_common(slashChar()+"_common"),
		
		folders_defaults(folders_common.s+slashChar()+"_defaults"),
		files_defaultSettings(folders_defaults.s+slashChar()+"gen_defaults.csv"),
		files_byPowerTrain_depreciation(folders_defaults.s+slashChar()+"ptDepreciation.csv"),
		files_byPowerTrain_licMaint(folders_defaults.s+slashChar()+"ptLicMaint.csv"),
		files_byPowerTrain_nbMfgGHG(folders_defaults.s+slashChar()+"ptVehNonBatMfgGHG.csv"),
		
		folders_defaultRealWorldDriving(folders_common.s+slashChar()+"realWorldDriving"),

		
		files_lastAnalysis(slashChar()+"_lastAnalyzed.csv"),
		
		vmfolders_modeling(slashChar()+"_vehModels"),
		vmfolders_refData(vmfolders_modeling.s+slashChar()+"_refData"),
		vmfolders_customData(vmfolders_modeling.s+slashChar()+"_custom"),
		
		vmfiles_title(vmfolders_modeling.s+slashChar()+"_title_description.csv"),
		vmfiles_vehList(vmfolders_modeling.s+slashChar()+"_vehModelsList.csv"),
		vmfiles_baseModels(vmfolders_modeling.s+slashChar()+"_baseModels.csv"),
		
		vmfiles_dUnits(vmfolders_customData.s+slashChar()+"_units.csv"),
		vmfiles_sbarsSetup(vmfolders_customData.s+slashChar()+"_sbarsSetup.csv"),
		vmfiles_sbarsOrder(vmfolders_customData.s+slashChar()+"_sbarsOrder.csv"),
		vmfiles_rvStatus(vmfolders_customData.s+slashChar()+"_rvStatus.csv"),
		vmfiles_gridGHG(vmfolders_customData.s+slashChar()+"_gridHourlyProfile_GHG.csv"),
		vmfiles_gridCost(vmfolders_customData.s+slashChar()+"_gridHourlyProfile_Cost.csv"),
		vmfolders_nonBatteryMfgGHG(vmfolders_customData.s+slashChar()+"_nonBatterManufacturingGHG.csv"),
		vmfolders_bevRepCosts(vmfolders_customData.s+slashChar()+"_bevReplacementCosts.csv"),
		vmfolders_licInsMaint(vmfolders_customData.s+slashChar()+"_licInsMaintCosts.csv"),
		vmfolders_depreciation(vmfolders_customData.s+slashChar()+"_depreciationModels.csv"),
		vmfolders_homeChargerCosts(vmfolders_customData.s+slashChar()+"_homeChargerCosts.csv"),
		vmfolders_costVsGHGAxes(vmfolders_customData.s+slashChar()+"_costVsGHGAxes.csv"),
		vmfolders_ghgAxes(vmfolders_customData.s+slashChar()+"_ghgAxes.csv"),
		vmfolders_costVsGHGDisplay(vmfolders_customData.s+slashChar()+"_costVsGHGDisplay.csv"),
		vmfolders_ghgDisplay(vmfolders_customData.s+slashChar()+"_ghgDisplay.csv"),
		vmfolders_costBarsDisplay(vmfolders_customData.s+slashChar()+"_costBarsDisplay.csv"),
		vmfolders_costBarsAxes(vmfolders_customData.s+slashChar()+"_costBarsAxes.csv"),
		vmfolders_vehChgEfficiencies(vmfolders_customData.s+slashChar()+"_vehChgEfficiencies.csv"),
		
		folders_solutionFolderPrefix(slashChar()+"_simRuns"+slashChar()+"_s"),
		files_fecoDescription(slashChar()+"_srDescription.csv"),
		files_fecoTripSummarys(slashChar()+"_srTripSummaries.csv"),
		
		folders_solProcFolderPrefix(slashChar()+"_simPost"+slashChar()+"_s"),
		
		folders_chgEventsSummayRoot(slashChar()+"_cetOpt"),
		folders_chgEventsSummaySubFolderPrefix(slashChar()+"_s"),
		
		folder_defaultSaveResults(slashChar()+"SavedResults"),
		;
		public String s;
		private FFStrings(String v) {s = v;}
	}
	//Slash character function (varies by OS)
	public static String slashChar() {
		String sc = "/";
		String osName = System.getProperty("os.name");
		if (osName.contains("Windows")||osName.contains("windows")) sc = "\\";
		return sc;
	}
	
	
	
	//Function to return the folder location for standard drive cycles
	public String folders_driveCycles() {
		return rootFolder + FFStrings.folders_defaults.s;
	}
	//Function to return the last conducted analysis
	public int getLastAnalysisID() {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(rootFolder+FFStrings.files_lastAnalysis.s));
			String readLine = fin.readLine();
			fin.close();
			return Integer.parseInt(readLine);
		} catch (Exception e) {
			return -1;
		}
	}
	//Function to set the last conducted analysis
	public void setLastAnalysisID(int id) {
		try {
			FileWriter fout = new FileWriter(rootFolder+FFStrings.files_lastAnalysis.s);
			String lsep = System.getProperty("line.separator");
			fout.append(""+id+lsep);
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	//Function to return path for file containing some general default settings
	public String getFilePath_defaultValues() {
		return rootFolder + FFStrings.files_defaultSettings.s;
	}
	//Function to return path for file containing default settings for Non-battery manufacturing GHG
	public String getFilePath_defaultNoneBatteryMfgGHG() {
		return rootFolder + FFStrings.files_byPowerTrain_nbMfgGHG.s;
	}
	//Function to return path for file containing default settings for Licensing, Insurance & Maintenance Cost
	public String getFilePath_defaultLicInsMaint() {
		return rootFolder + FFStrings.files_byPowerTrain_licMaint.s;
	}
	//Function to return path for file containing default settings for Vehicles Depreciation
	public String getFilePath_defaultDepreciation() {
		return rootFolder + FFStrings.files_byPowerTrain_depreciation.s;
	}
	//Function to return path for folder of vehicle analysis (vehicle analysis root)
	public String getFolderPath_defaultRealWorldDriving() {
		return rootFolder + FFStrings.folders_defaultRealWorldDriving.s;
	}
	
	//Function to ensure the customization folder exists
	public void ensureCustomizationFolderExists(int aID) {
		String folderPath = getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_customData.s;
		File f = new File(folderPath);
		f.mkdirs();
	}
	

	//Function to return path for folder of vehicle analysis (vehicle analysis root)
	public String getFolderPath_vehAnalysisRoot(int aID) {
		return rootFolder + slashChar() + analysisFolders[aID];
	}
	//Function to return path for default folder for saving results
	public String getFolderPath_defaultResults(int aID) {
		return rootFolder + slashChar() + analysisFolders[aID] + FFStrings.folder_defaultSaveResults.s;
	}
	//Function to return path for folder of a completed fuel economy simulations result
	public String getFolderPath_fecoSims(int aID, int sID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.folders_solutionFolderPrefix.s + sID;
	}
	//Function to return path for folder of a post-processing of fuel economy simulations result
	public String getFolderPath_fecoSimsPost(int aID, int sID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.folders_solProcFolderPrefix.s + sID;
	}
	//Function to return path for folder of analysis root folder of charging events summary results
	public String getFolderPath_chgEventsSummariesRoot(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.folders_chgEventsSummayRoot.s;
	}
	//Function to return path for folder of a completed post-processing of charging events result
	public String getFolderPath_chgEventsSummaries(int aID, int sID) {
		return getFolderPath_chgEventsSummariesRoot(aID) + FFStrings.folders_chgEventsSummaySubFolderPrefix.s + sID;
	}
	
	
	//Function to return path for file for analysis Title and Description
	public String getFilePath_analysisTitle(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_title.s;
	}
	//Function to return path for file for list of vehicles
	public String getFilePath_vehList(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_vehList.s;
	}
	//Function to return path for file for unit settings
	public String getFilePath_DUnits(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_dUnits.s;
	}
	//Function to return path for file for base models
	public String getFilePath_baseModels(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_baseModels.s;
	}
	//Function to return path for file for slider bars setup
	public String getFilePath_sliderbarsSetup(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_sbarsSetup.s;
	}
	//Function to return path for file for slider bars ordering and showing
	public String getFilePath_sliderbarsOrdering(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_sbarsOrder.s;
	}
	//Function to return path for file for slider bars and visualization options status
	public String getFilePath_ResultsVisualizationStatus(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_rvStatus.s;
	}
	//Function to return path for file for Grid Hourly GHG Profile Curve
	public String getFilePath_gridGHG(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_gridGHG.s;
	}
	//Function to return path for file for Grid Hourly Cost Profile Curve
	public String getFilePath_gridCost(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfiles_gridCost.s;
	}
	//Function to return path for file for FASTSim One-file-vehicle-model
	public String getFilePath_FASTSimVehModel(int aID, String shortName) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_modeling.s + slashChar() + shortName + ".csv";
	}
	//Function to return path for file for Non-battery Manufacturing GHG model
	public String getFilePath_nonBatteryMfgGHG(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_nonBatteryMfgGHG.s;
	}
	//Function to return path for file for licensing, insurance and maintenance cost model
	public String getFilePath_licInsMaintCostModels(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_licInsMaint.s;
	}
	//Function to return path for file for depreciation models model
	public String getFilePath_dereciationModels(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_depreciation.s;
	}
	//Function to return path for file for BEV Replacement cost numbers
	public String getFilePath_bevRepCosts(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_bevRepCosts.s;
	}
	//Function to return path for file for home charger cost numbers
	public String getFilePath_homeChargerCosts(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_homeChargerCosts.s;
	}
	//Function to return path for file for vehicle charging efficiencies
	public String getFilePath_vehChgEfficiencies(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_vehChgEfficiencies.s;
	}
	//Function to return path for file for GHG axes
	public String getFilePath_ghgAxes(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_ghgAxes.s;
	}
	//Function to return path for file for Cost versus GHG axes
	public String getFilePath_costVsGHGAxes(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_costVsGHGAxes.s;
	}
	//Function to return path for file for Cost bars axes
	public String getFilePath_costBarsAxes(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_costBarsAxes.s;
	}
	//Function to return path for file for GHG display options
	public String getFilePath_ghgDisplay(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_ghgDisplay.s;
	}
	//Function to return path for file for Cost versus GHG display options
	public String getFilePath_costVsGHGDisplay(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_costVsGHGDisplay.s;
	}
	//Function to return path for file for Cost bars display options
	public String getFilePath_costBarsDisplay(int aID) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_costBarsDisplay.s;
	}
	//Function to return path for file for PDF file of FASTSim vehicle model documentation
	public String getFilePath_FASTSimVehModelPDFDoc(int aID, String shortName) {
		return getFolderPath_vehAnalysisRoot(aID) + FFStrings.vmfolders_refData.s + slashChar() + shortName + ".pdf";
	}
	
	public String getFilePath_fecoSimsDescriptionFile(int aID, int sID) {
		return getFolderPath_fecoSims(aID,sID) + FFStrings.files_fecoDescription.s;
	}
	public String getFilePath_fecoSimsTripSummariesFile(int aID, int sID) {
		return getFolderPath_fecoSims(aID,sID) + FFStrings.files_fecoTripSummarys.s;
	}
	
	
	//Function to delete a fuel economy simulation set of results, all its dependencies, and rename all associated folders
	public void deleteFEcoSimResult(int aID, int sID) {
		//Delete
		deleteFolderAndContents(new File(getFolderPath_fecoSims(aID, sID)));
		deleteFolderAndContents(new File(getFolderPath_chgEventsSummaries(aID, sID)));
		deleteFolderAndContents(new File(getFolderPath_fecoSimsPost(aID, sID)));
				
		//Re-Name
		int higherSolIDs = sID + 1;
		while (true) {
			File higherIDSolFolder = new File(getFolderPath_fecoSims(aID, higherSolIDs));
			
			if (higherIDSolFolder.exists()) {
				File renamedFolder = new File(getFolderPath_fecoSims(aID, higherSolIDs-1));
				higherIDSolFolder.renameTo(renamedFolder);
				
				File oldPPFolder = new File(getFolderPath_chgEventsSummaries(aID, higherSolIDs));
				File newPPFolder = new File(getFolderPath_chgEventsSummaries(aID, higherSolIDs-1));
				if (oldPPFolder.exists()) oldPPFolder.renameTo(newPPFolder);
				
				oldPPFolder = new File(getFolderPath_fecoSimsPost(aID, higherSolIDs));
				newPPFolder = new File(getFolderPath_fecoSimsPost(aID, higherSolIDs-1));
				if (oldPPFolder.exists()) oldPPFolder.renameTo(newPPFolder);
				
				higherSolIDs++;
			} else {
				break;
			}
		}
	}
	
	public void deleteAllChgTimingOptResults(int aID) {
		deleteFolderAndContents(new File(getFolderPath_chgEventsSummariesRoot(aID)));
	}	
	
	//Utility
	public static void deleteFolderAndContents(File folder) {
		try {
			File[] subFs = folder.listFiles();
			
			for (int i=0; i<subFs.length; i++) {
				if (subFs[i].isDirectory()) {
					deleteFolderAndContents(subFs[i]);
				} else {
					subFs[i].delete();
				}
			}
			
			folder.delete();
		} catch (Exception e) {}
	}
}
