package pvc.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class MCFloatValuesFileWHeader {
	private MCFloatValuesFileWHeader() {}

	public static float[][] readFileValues(String fname) {
		try {
			ArrayList<String> lst = new ArrayList<String>();
			
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			int numColumns = sp.length;
			
			while ((readLine = fin.readLine())!=null) {
				lst.add(readLine);
			}
			
			fin.close();
			
			int numLines = lst.size();
			float[][] arr = new float[numColumns][numLines];
			
			for (int i=0; i<numLines; i++) {
				sp = lst.get(i).split(",");
				for (int j=0; j<numColumns; j++) arr[j][i] = Float.parseFloat(sp[j]);
			}			
			
			return arr;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void writeToFile(String fname, String[] colHeadings, float[][] values) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			String st = colHeadings[0];
			for (int i=1; i<colHeadings.length; i++) st = st + "," + colHeadings[i];
			fout.append(st+lsep);
			
			int nCols = values.length;
			int nRows = values[0].length;
			
			for (int i=0; i<nRows; i++) {
				st = ""+values[0][i];
				for (int j=1; j<nCols; j++) st = st + "," + values[j][i];
				fout.append(st+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
}
