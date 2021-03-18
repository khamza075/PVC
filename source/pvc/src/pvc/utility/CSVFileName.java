package pvc.utility;

public class CSVFileName {
	private CSVFileName() {}

	//Checks if a file path is for a CSV file, if yes returns copy of original, otherwise tacks .csv to it
	public static String getCSVFileName(String fname) {
		boolean isAlreadyCSVFile = true;
		
		int lastDot = fname.lastIndexOf(".");
		if (lastDot < 0) {
			isAlreadyCSVFile = false;
		} else {
			String fileExtension = fname.substring(lastDot);
			if (!fileExtension.equalsIgnoreCase(".CSV")) isAlreadyCSVFile = false;
		}		
		
		if (isAlreadyCSVFile) return new String(fname);
		return fname + ".csv";
	}
}
