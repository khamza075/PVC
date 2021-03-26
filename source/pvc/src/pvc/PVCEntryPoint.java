package pvc;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;
import pvc.gui.MainPanelGUI;

public class PVCEntryPoint {

	public static void main(String[] args) {
		//Current Folder of Jar file
		String cpvcRoot = System.getProperty("user.dir");
		
		//File or Folder Selection dialog
		JFileChooser dlgFileFolderChooser  = new JFileChooser();
		dlgFileFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		//Check if current folder includes the minimum necessary data for PVC, if not prompt user to select a different folder
		if (!FFStructure.rootFolderIsValid(cpvcRoot)) {
			dlgFileFolderChooser.setDialogTitle("Choose PVC Root Folder");
			
			int retVal = dlgFileFolderChooser.showOpenDialog(null);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				cpvcRoot = dlgFileFolderChooser.getSelectedFile().getPath();
			}
		}

		//Attempt to create a File Structure class, cannot proceed if it fails
		FFStructure cFS = FFStructure.createFS(cpvcRoot);
		if (cFS == null) {
			JOptionPane.showMessageDialog(null, "Unable to identify necessary files and/or folder structure.", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Check that there are available analysis folders
		if (cFS.analysisFolders().length < 1) {
			JOptionPane.showMessageDialog(null, "Analysis folders not available.", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Identify the ID of last run analysis
		int aID = Math.max(0, Math.min(cFS.getLastAnalysisID(), cFS.analysisFolders().length-1));
		cFS.setLastAnalysisID(aID);

		//Launch into Main Panel
		FDefaults.readDefaultValues(cFS.getFilePath_defaultValues());
		NoneBatteryMfgGHGDefaults.readfromFile(cFS.getFilePath_defaultNoneBatteryMfgGHG());
		LIMDefaults.readfromFile(cFS.getFilePath_defaultLicInsMaint());
		DepreciationDefaults.readfromFile(cFS.getFilePath_defaultDepreciation());
		new MainPanelGUI(cFS, aID);
	}
}
