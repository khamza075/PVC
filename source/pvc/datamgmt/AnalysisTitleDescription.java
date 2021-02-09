package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;

public class AnalysisTitleDescription {
	private String title;
	private String description;
	
	public String title() {return title;}
	public String description() {return description;}

	public AnalysisTitleDescription(FFStructure fs, int aID) throws Exception {
		String fname = fs.getFilePath_analysisTitle(aID);
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		
		if ((title = fin.readLine())==null) {
			fin.close();
			throw new Exception ("Invalid Analysis Title");
		}
		
		if ((description = fin.readLine())==null) {
			fin.close();
			throw new Exception ("Invalid Analysis Description");
		}
		
		fin.close();
	}
}
