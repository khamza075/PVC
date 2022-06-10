package pvc.datamgmt.comp;

import javax.swing.JOptionPane;

public class MfgGHGReminder {
	private static boolean reminderIssued = false;

	private MfgGHGReminder() {}	//Prevent instantiation
	
	public static void issueReminder() {
		if (reminderIssued) return;
		
		JOptionPane.showMessageDialog(null, 
				 "All estimates of manufacturing GHG are approximate estimates based on\n"
				+"generic vehicle and battery models from GREET 2020 that are NOT specific\n"
				+"to any particular vehicle or battery manufacturer.", 
				"Attention", JOptionPane.OK_OPTION|JOptionPane.WARNING_MESSAGE);
		reminderIssued = true;
	}
	public static void resetReminder() {
		reminderIssued = false;
	}
}
