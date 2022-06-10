package pvc.datamgmt.comp;

import javax.swing.JOptionPane;

public class MfgGHGReminder {
	private static boolean reminderIssued = false;

	private MfgGHGReminder() {}	//Prevent instantiation
	
	public static void issueReminder() {
		if (reminderIssued) return;
		
		JOptionPane.showMessageDialog(null, 
				 "These manufacturing GHG estimates are based on GREET 2020, and are based\n"
				+"on GREET 2020 (https://greet.es.anl.gov/) the specific vehicle’s characteristics,\n"
				+"such as battery size and chemistry, vehicle class and weight, etc. However,\n" 
				+"the authors have not validated, nor suggest, that this estimate is correct for\n"+
				"any specific vehicle or manufacturer.", 
				"Attention", JOptionPane.OK_OPTION|JOptionPane.WARNING_MESSAGE);
		reminderIssued = true;
	}
	public static void resetReminder() {
		reminderIssued = false;
	}
}
