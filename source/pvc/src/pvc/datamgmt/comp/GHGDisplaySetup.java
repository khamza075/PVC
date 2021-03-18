package pvc.datamgmt.comp;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.FFStructure;

public class GHGDisplaySetup {
	private int titleFontSize;
	public void setTitleFontSize(int fontSize) {titleFontSize = fontSize;}
	public int titleFontSize() {return titleFontSize;}
	
	private int axesLineWidth;
	public void setAxesLineWidth(int lineWidth) {axesLineWidth = lineWidth;}
	public int axesLineWidth() {return axesLineWidth;}
	
	private int boxPlotLineWidth;
	public void setBoxPlotLineWidth(int lineWidth) {boxPlotLineWidth = lineWidth;}
	public int boxPlotLineWidth() {return boxPlotLineWidth;}
	
	private boolean showGrid;
	public void setShowGrid(boolean flag) {showGrid = flag;}
	public boolean showGrid() {return showGrid;}
	
	private boolean showMinorGrid;
	public void setShowMinorGrid(boolean flag) {showMinorGrid = flag;}
	public boolean showMinorGrid() {return showMinorGrid;}
	
	private int gridLineWidth;
	public void setGridLineWidth(int lineWidth) {gridLineWidth = lineWidth;}
	public int gridLineWidth() {return gridLineWidth;}
	
	private int minorGridLineWidth;
	public void setMinorGridLineWidth(int lineWidth) {minorGridLineWidth = lineWidth;}
	public int minorGridLineWidth() {return minorGridLineWidth;}
	
	private int gridColorRed, gridColorGreen, gridColorBlue;
	private Color gridColor;
	public void setGridColor(int r, int g, int b) {
		gridColorRed = r;
		gridColorGreen = g;
		gridColorBlue = b;
		gridColor = new Color(r,g,b);
	}
	public int getGridColorRed() {return gridColorRed;}
	public int getGridColorGreen() {return gridColorGreen;}
	public int getGridColorBlue() {return gridColorBlue;}
	public Color getGridColor() {return gridColor;}
	
	private int minorGridColorRed, minorGridColorGreen, minorGridColorBlue;
	private Color minorGridColor;
	public void setMinorGridColor(int r, int g, int b) {
		minorGridColorRed = r;
		minorGridColorGreen = g;
		minorGridColorBlue = b;
		minorGridColor = new Color(r,g,b);
	}
	public int getMinorGridColorRed() {return minorGridColorRed;}
	public int getMinorGridColorGreen() {return minorGridColorGreen;}
	public int getMinorGridColorBlue() {return minorGridColorBlue;}
	public Color getMinorGridColor() {return minorGridColor;}
	
	private int l1ColorRed, l1ColorGreen, l1ColorBlue;
	private Color l1Color;
	public void setL1Color(int r, int g, int b) {
		l1ColorRed = r;
		l1ColorGreen = g;
		l1ColorBlue = b;
		l1Color = new Color(r,g,b);
	}
	public int getL1ColorRed() {return l1ColorRed;}
	public int getL1ColorGreen() {return l1ColorGreen;}
	public int getL1ColorBlue() {return l1ColorBlue;}
	public Color getL1Color() {return l1Color;}
	
	private int l2ColorRed, l2ColorGreen, l2ColorBlue;
	private Color l2Color;
	public void setL2Color(int r, int g, int b) {
		l2ColorRed = r;
		l2ColorGreen = g;
		l2ColorBlue = b;
		l2Color = new Color(r,g,b);
	}
	public int getL2ColorRed() {return l2ColorRed;}
	public int getL2ColorGreen() {return l2ColorGreen;}
	public int getL2ColorBlue() {return l2ColorBlue;}
	public Color getL2Color() {return l2Color;}
	
	private int dcColorRed, dcColorGreen, dcColorBlue;
	private Color dcColor;
	public void setDCColor(int r, int g, int b) {
		dcColorRed = r;
		dcColorGreen = g;
		dcColorBlue = b;
		dcColor = new Color(r,g,b);
	}
	public int getDCColorRed() {return dcColorRed;}
	public int getDCColorGreen() {return dcColorGreen;}
	public int getDCColorBlue() {return dcColorBlue;}
	public Color getDCColor() {return dcColor;}
	
	
	private ArrayList<VehGHGDisplayOpions> lst;
	public int numVehicles() {return lst.size();}
	public int numDisplayedVehicles() {
		int num = 0;
		for (int i=0; i<lst.size(); i++) {
			if (lst.get(i).isShown) num++;
		}
		return num;
	}
	public VehGHGDisplayOpions getVehAtDisplayPos(int id) {return lst.get(id);}
	
	public void moveVehUp(int id) {
		if (id <=0) return;
		VehGHGDisplayOpions tmp = lst.get(id);
		lst.remove(id);
		lst.add(id-1, tmp);
	}
	public void moveVehDown(int id) {
		if (id >= (lst.size()-1)) return;
		VehGHGDisplayOpions tmp = lst.get(id);
		lst.remove(id);
		lst.add(id+1, tmp);
	}
	
	
	public static GHGDisplaySetup readExisting(FFStructure fs, int aID) {
		try {
			return new GHGDisplaySetup(fs.getFilePath_ghgDisplay(aID));
		} catch (Exception e) {
			return null;
		}
	}
	
	private GHGDisplaySetup(String fname) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		
		String readLine = fin.readLine();
		String[] sp = readLine.split(",");
		
		titleFontSize = Integer.parseInt(sp[0]);
		axesLineWidth = Integer.parseInt(sp[1]);
		boxPlotLineWidth = Integer.parseInt(sp[2]);
		showGrid = Boolean.parseBoolean(sp[3]);
		showMinorGrid = Boolean.parseBoolean(sp[4]);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		gridLineWidth = Integer.parseInt(sp[0]);
		int r = Integer.parseInt(sp[1]);
		int g = Integer.parseInt(sp[2]);
		int b = Integer.parseInt(sp[3]);
		setGridColor(r, g, b);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		minorGridLineWidth = Integer.parseInt(sp[0]);
		r = Integer.parseInt(sp[1]);
		g = Integer.parseInt(sp[2]);
		b = Integer.parseInt(sp[3]);
		setMinorGridColor(r, g, b);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		r = Integer.parseInt(sp[0]);
		g = Integer.parseInt(sp[1]);
		b = Integer.parseInt(sp[2]);
		setL1Color(r, g, b);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		r = Integer.parseInt(sp[0]);
		g = Integer.parseInt(sp[1]);
		b = Integer.parseInt(sp[2]);
		setL2Color(r, g, b);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		r = Integer.parseInt(sp[0]);
		g = Integer.parseInt(sp[1]);
		b = Integer.parseInt(sp[2]);
		setDCColor(r, g, b);
		
		lst = new ArrayList<VehGHGDisplayOpions>();
		
		while ((readLine = fin.readLine())!=null) {
			String readLine2 = fin.readLine();
			VehGHGDisplayOpions vehOptions = new VehGHGDisplayOpions(readLine, readLine2);
			lst.add(vehOptions);
		}
		
		fin.close();
	}
	public void save(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append(""+titleFontSize+","+axesLineWidth+","+boxPlotLineWidth+","+showGrid+","+showMinorGrid+lsep);
			fout.append(""+gridLineWidth+","+gridColorRed+","+gridColorGreen+","+gridColorBlue+lsep);
			fout.append(""+minorGridLineWidth+","+minorGridColorRed+","+minorGridColorGreen+","+minorGridColorBlue+lsep);
			
			fout.append(""+l1ColorRed+","+l1ColorGreen+","+l1ColorBlue+lsep);
			fout.append(""+l2ColorRed+","+l2ColorGreen+","+l2ColorBlue+lsep);
			fout.append(""+dcColorRed+","+dcColorGreen+","+dcColorBlue+lsep);
			
			for (int i=0; i<lst.size(); i++) {
				fout.append(lst.get(i).toString());
			}

			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	public GHGDisplaySetup(AnalysisVehModelsSetup.AVehModelSetup[] vms, FSJOneFileVehModel[] fsofModels) {
		titleFontSize = 14;
		axesLineWidth = 1;
		boxPlotLineWidth = 2;
		showGrid = true;
		showMinorGrid = true;
		
		gridLineWidth = 1;
		setGridColor(180, 180, 180);
		
		minorGridLineWidth = 1;
		setMinorGridColor(210, 210, 210);
		
		setL1Color(ChargerTypes.L1.defaultColorRed, ChargerTypes.L1.defaultColorGreen, ChargerTypes.L1.defaultColorBlue);
		setL2Color(ChargerTypes.L2.defaultColorRed, ChargerTypes.L2.defaultColorGreen, ChargerTypes.L2.defaultColorBlue);
		setDCColor(ChargerTypes.DC.defaultColorRed, ChargerTypes.DC.defaultColorGreen, ChargerTypes.DC.defaultColorBlue);
		
		lst = new ArrayList<VehGHGDisplayOpions>();
		for (int i=0; i<vms.length; i++) {
			lst.add(new VehGHGDisplayOpions(vms, i, PowertrainType.decode(fsofModels[i].vehModelParam)));
		}
	}
	
	public GHGDisplaySetup(GHGDisplaySetup other) {
		titleFontSize = other.titleFontSize;
		axesLineWidth = other.axesLineWidth;
		boxPlotLineWidth = 2;
		showGrid = other.showGrid;
		showMinorGrid = other.showMinorGrid;
		
		gridLineWidth = other.gridLineWidth;
		setGridColor(other.gridColorRed, other.gridColorGreen, other.gridColorBlue);
		
		minorGridLineWidth = other.minorGridLineWidth;
		setMinorGridColor(other.minorGridColorRed, other.minorGridColorGreen, other.minorGridColorBlue);
		
		setL1Color(other.l1ColorRed, other.l1ColorGreen, other.l1ColorBlue);
		setL2Color(other.l2ColorRed, other.l2ColorGreen, other.l2ColorBlue);
		setDCColor(other.dcColorRed, other.dcColorGreen, other.dcColorBlue);
		
		lst = new ArrayList<VehGHGDisplayOpions>();
		for (int i=0; i<other.lst.size(); i++) {
			lst.add(new VehGHGDisplayOpions(other.lst.get(i)));
		}
	}
	
	public static class VehGHGDisplayOpions {
		private int vehID;
		public int vehID() {return vehID;}
		
		public String displayedTitle;
		public boolean isShown;
		public int pdmRed, pdmGreen, pdmBlue, pdmLineWidth;
		public int cdfRed, cdfGreen, cdfBlue, cdfLineWidth;
		public int bxfRed, bxfGreen, bxfBlue;
		
		private VehGHGDisplayOpions(VehGHGDisplayOpions other) {
			vehID = other.vehID;
			displayedTitle = new String(other.displayedTitle);
			isShown = other.isShown;
			
			pdmLineWidth = other.pdmLineWidth;
			pdmRed = other.pdmRed;
			pdmGreen = other.pdmGreen;
			pdmBlue = other.pdmBlue;
			
			cdfLineWidth = other.cdfLineWidth;
			cdfRed = other.cdfRed;
			cdfGreen = other.cdfGreen;
			cdfBlue = other.cdfBlue;
			
			bxfRed = other.bxfRed;
			bxfGreen = other.bxfGreen;
			bxfBlue = other.bxfBlue;
		}
		private VehGHGDisplayOpions(AnalysisVehModelsSetup.AVehModelSetup[] vms, int vID, PowertrainType ptType) {
			vehID = vID;
			displayedTitle = new String(vms[vID].shortName);
			isShown = true;
			
			pdmLineWidth = 3;
			pdmRed = ptType.defaultColorRed;
			pdmGreen = ptType.defaultColorGreen;
			pdmBlue = ptType.defaultColorBlue;
			
			cdfLineWidth = 2;
			cdfRed = 0;
			cdfGreen = 0;
			cdfBlue = 0;
			
			bxfRed = colorToTwoThirdsWhiteVersion(pdmRed);
			bxfGreen = colorToTwoThirdsWhiteVersion(pdmGreen);
			bxfBlue = colorToTwoThirdsWhiteVersion(pdmBlue);
		}
		private VehGHGDisplayOpions(String readLine1, String readLine2) throws Exception {
			displayedTitle = new String(readLine1);
			String[] sp = readLine2.split(",");
			
			vehID = Integer.parseInt(sp[0]);
			isShown = Boolean.parseBoolean(sp[1]);
			
			pdmRed = Integer.parseInt(sp[2]);
			pdmGreen = Integer.parseInt(sp[3]);
			pdmBlue = Integer.parseInt(sp[4]);
			pdmLineWidth = Integer.parseInt(sp[5]);
			
			cdfRed = Integer.parseInt(sp[6]);
			cdfGreen = Integer.parseInt(sp[7]);
			cdfBlue = Integer.parseInt(sp[8]);
			cdfLineWidth = Integer.parseInt(sp[9]);
			
			bxfRed = Integer.parseInt(sp[10]);
			bxfGreen = Integer.parseInt(sp[11]);
			bxfBlue = Integer.parseInt(sp[12]);
		}
		@Override public String toString() {
			String lsep = System.getProperty("line.separator");
			String st = displayedTitle + lsep + vehID + "," + isShown + "," + pdmRed + "," + pdmGreen + "," + pdmBlue + "," + pdmLineWidth +
					"," + cdfRed + "," + cdfGreen + "," + cdfBlue + "," + cdfLineWidth + "," + bxfRed + "," + bxfGreen + "," + bxfBlue + lsep;
			return st;
		}
	}
	private static int colorToTwoThirdsWhiteVersion(int cValue) {
		return Math.min(((cValue + 510)/3 + 1), 255);
	}
}
