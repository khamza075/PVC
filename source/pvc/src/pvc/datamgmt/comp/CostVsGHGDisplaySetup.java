package pvc.datamgmt.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JLabel;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.*;

public class CostVsGHGDisplaySetup {
	private int axesLineWidth;
	public void setAxesLineWidth(int lineWidth) {axesLineWidth = lineWidth;}
	public int axesLineWidth() {return axesLineWidth;}
	
	private boolean showGrid;
	public void setShowGrid(boolean flag) {showGrid = flag;}
	public boolean showGrid() {return showGrid;}
	
	private int gridLineWidth;
	public void setGridLineWidth(int lineWidth) {gridLineWidth = lineWidth;}
	public int gridLineWidth() {return gridLineWidth;}
	
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
	
	
	private boolean ghgOnYAxis, ghgAxesShowTotal, costAxesShowTotal;
	public boolean ghgOnYAxis() {return ghgOnYAxis;}
	public boolean ghgAxesShowTotal() {return ghgAxesShowTotal;}
	public boolean costAxesShowTotal() {return costAxesShowTotal;}
	public void setGHGOnYAxis(boolean flag) {ghgOnYAxis = flag;}
	public void setGHGAxisToShowTotal(boolean flag) {ghgAxesShowTotal = flag;}
	public void setCostAxisToShowTotal(boolean flag) {costAxesShowTotal = flag;}

	
	private ArrayList<ParetoPointDisplayGraphics> lstPoints;
	public int getVehIDtoDraw(int drawOrderID) {return lstPoints.get(drawOrderID).dData.vehID;}
	public void drawInGraphics(int drawOrderID, Graphics2D g2, int cx, int cy) {lstPoints.get(drawOrderID).drawInGraphics(g2, cx, cy);}
	public ParetoPointDisplayData getCopyOfVehDrawData(int drawOrderID) {return new ParetoPointDisplayData(lstPoints.get(drawOrderID).dData);}
	public void setVehDrawData(int drawOrderID, ParetoPointDisplayData vehDrawData) {lstPoints.set(drawOrderID, new ParetoPointDisplayGraphics(vehDrawData));}
	public void moveVehOrderUp(int drawOrderID) {
		if (drawOrderID < 1) return;
		if (drawOrderID >= lstPoints.size()) return;
		
		ParetoPointDisplayGraphics cur = lstPoints.get(drawOrderID);
		lstPoints.remove(drawOrderID);
		lstPoints.add(drawOrderID-1, cur);
	}
	public void moveVehOrderDown(int drawOrderID) {
		if (drawOrderID < 0) return;
		if (drawOrderID >= (lstPoints.size()-1)) return;
		
		ParetoPointDisplayGraphics cur = lstPoints.get(drawOrderID);
		lstPoints.remove(drawOrderID);
		lstPoints.add(drawOrderID+1, cur);
	}
	
	public static CostVsGHGDisplaySetup readExisting(FFStructure fs, int aID) {
		try {			
			return new CostVsGHGDisplaySetup(fs.getFilePath_costVsGHGDisplay(aID));
		} catch (Exception e) {
			return null;
		}
	}
	private CostVsGHGDisplaySetup(String fname) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		
		String readLine = fin.readLine();
		readLine = fin.readLine();
		String[] sp = readLine.split(",");
		
		axesLineWidth = Integer.parseInt(sp[0]);
		showGrid = Boolean.parseBoolean(sp[1]);
		gridLineWidth = Integer.parseInt(sp[2]);
		int gr = Integer.parseInt(sp[3]);
		int gg = Integer.parseInt(sp[4]);
		int gb = Integer.parseInt(sp[5]);
		setGridColor(gr,gg,gb);
		
		ghgOnYAxis = Boolean.parseBoolean(sp[6]);
		ghgAxesShowTotal = Boolean.parseBoolean(sp[7]);
		costAxesShowTotal = Boolean.parseBoolean(sp[8]);
		
		readLine = fin.readLine();
		lstPoints = new ArrayList<ParetoPointDisplayGraphics>();
		
		while ((readLine = fin.readLine())!=null) {
			try {
				String readLine2 = fin.readLine();
				lstPoints.add(new ParetoPointDisplayGraphics(new ParetoPointDisplayData(readLine, readLine2)));
			} catch (Exception e) {
				fin.close();
				throw new Exception();
			}
		}
		
		fin.close();
	}
	public CostVsGHGDisplaySetup(AnalysisVehModelsSetup.AVehModelSetup[] vms, FSJOneFileVehModel[] fsofModels) {
		axesLineWidth = 1;
		showGrid = true;
		gridLineWidth = 1;
		setGridColor(180,180,180);
		
		ghgOnYAxis = false;
		ghgAxesShowTotal = false;
		costAxesShowTotal = false;
		
		lstPoints = new ArrayList<ParetoPointDisplayGraphics>();

		for (int i=0; i<vms.length; i++) {
			lstPoints.add(new ParetoPointDisplayGraphics(new ParetoPointDisplayData(PowertrainType.decode(fsofModels[i].vehModelParam), vms[i].shortName, i)));
		}
	}
	public CostVsGHGDisplaySetup(CostVsGHGDisplaySetup other) {
		axesLineWidth = other.axesLineWidth;
		gridLineWidth = other.gridLineWidth;
		showGrid = other.showGrid;
		setGridColor(other.gridColorRed, other.gridColorGreen, other.gridColorBlue);
		
		ghgOnYAxis = other.ghgOnYAxis;
		ghgAxesShowTotal = other.ghgAxesShowTotal;
		costAxesShowTotal = other.costAxesShowTotal;
		
		lstPoints = new ArrayList<ParetoPointDisplayGraphics>();
		for (int i=0; i<other.lstPoints.size(); i++) {
			lstPoints.add(new ParetoPointDisplayGraphics(new ParetoPointDisplayData(other.lstPoints.get(i).dData)));
		}
	}
	public void save(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("__generalSettings"+lsep);
			String st = ""+axesLineWidth+","+showGrid+","+gridLineWidth+","+gridColorRed+","+gridColorGreen+","+gridColorBlue
					+","+ghgOnYAxis+","+ghgAxesShowTotal+","+costAxesShowTotal;
			fout.append(st+lsep);
			
			fout.append("__pointsPlottingSetting"+lsep);
			for (int i=0; i<lstPoints.size(); i++) {
				fout.append(lstPoints.get(i).dData.toString()+lsep);
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	public enum ParetoPointDisplayShapeType {
		Circle("Circle"), 
		Square("Square"), 
		Triangle("Triangle"), 
		InvertedTriangle("Inv. Triangle"),
		Diamond("Diamond"), 
		CrossHair("Cross-Hair"), 
		XShape("X Symbol")
		;
		public String caption;
		private ParetoPointDisplayShapeType(String s) {caption = s;}
		public static ParetoPointDisplayShapeType decode(String s) {
			for (int i=0; i<values().length; i++) {
				if (s.equalsIgnoreCase(values()[i].toString())) return values()[i];
			}
			return ParetoPointDisplayShapeType.Circle;
		}
	}
	public enum ParetoPointNameTextPosition {
		Above("Above"), 
		AboveCentered("Above (Center)"), 
		AboveLeft("Above (Left)"), 
		
		UpperRight("Upper-Right"), 
		Right("Right"), 
		LowerRight("Lower-Right"), 
		
		Bottom("Bottom"), 
		BottomCentered("Bottom (Center)"), 
		BottomLeft("Bottom (Left)"),
		
		LowerLeft("Lower-Left"), 
		Left("Left"), 
		UpperLeft("Upper-Left"), 
		;
		public String caption;
		private ParetoPointNameTextPosition(String s) {caption = s;}
		public static ParetoPointNameTextPosition decode(String s) {
			for (int i=0; i<values().length; i++) {
				if (s.equalsIgnoreCase(values()[i].toString())) return values()[i];
			}
			return ParetoPointNameTextPosition.UpperRight;
		}
	}
	
	public static class ParetoPointDisplayData {
		public String nameTextToDisplay;
		public int vehID;
		public boolean showOnPlot;
		public ParetoPointDisplayShapeType shapeType;
		public int shapeRadius;
		public int shapeLineWidth;
		public int shapeColorRed, shapeColorGreen, shapeColorBlue;
		public boolean showNameOnPlot;
		public boolean boldText;
		public ParetoPointNameTextPosition textPosition;
		public int nameFontSize;
		public int nameRadiusOffset;		
		
		@Override public String toString() {
			String lsep = System.getProperty("line.separator");
			return nameTextToDisplay + lsep + vehID + "," + showOnPlot + "," + shapeType + "," + shapeRadius + "," + shapeLineWidth + "," + 
					shapeColorRed + "," + shapeColorGreen + "," + shapeColorBlue + "," + showNameOnPlot + "," + boldText + "," + textPosition + "," +
					nameFontSize + "," + nameRadiusOffset;
		}
		private ParetoPointDisplayData(String readLine1, String readLine2) throws Exception {
			String[] sp = readLine2.split(",");
			
			nameTextToDisplay = new String(readLine1);			
			vehID = Integer.parseInt(sp[0]);
			showOnPlot = Boolean.parseBoolean(sp[1]);
			
			shapeType = ParetoPointDisplayShapeType.decode(sp[2]);			
			shapeRadius = Integer.parseInt(sp[3]);
			shapeLineWidth = Integer.parseInt(sp[4]);
			shapeColorRed = Integer.parseInt(sp[5]);
			shapeColorGreen = Integer.parseInt(sp[6]);
			shapeColorBlue = Integer.parseInt(sp[7]);

			showNameOnPlot = Boolean.parseBoolean(sp[8]);
			boldText = Boolean.parseBoolean(sp[9]);
			textPosition = ParetoPointNameTextPosition.decode(sp[10]);
			nameFontSize = Integer.parseInt(sp[11]);
			nameRadiusOffset = Integer.parseInt(sp[12]);
		}
		public ParetoPointDisplayData(ParetoPointDisplayData other) {
			nameTextToDisplay = new String(other.nameTextToDisplay);
			vehID = other.vehID;
			showOnPlot = other.showOnPlot;
			shapeType = other.shapeType;
			shapeRadius = other.shapeRadius;
			shapeLineWidth = other.shapeLineWidth;
			shapeColorRed = other.shapeColorRed;
			shapeColorGreen = other.shapeColorGreen;
			shapeColorBlue = other.shapeColorBlue;
			showNameOnPlot = other.showNameOnPlot;
			boldText = other.boldText;
			textPosition = other.textPosition;
			nameFontSize = other.nameFontSize;
			nameRadiusOffset = other.nameRadiusOffset;
		}
		public ParetoPointDisplayData(PowertrainType ptType, String vehText, int vehicleID) {
			nameTextToDisplay = new String(vehText);
			vehID = vehicleID;
			showOnPlot = true;
			shapeType = ParetoPointDisplayShapeType.Circle;
			shapeRadius = 8;
			shapeLineWidth = 3;
			shapeColorRed = ptType.defaultColorRed;
			shapeColorGreen = ptType.defaultColorGreen;
			shapeColorBlue = ptType.defaultColorBlue;
			showNameOnPlot = true;
			boldText = false;
			textPosition = ParetoPointNameTextPosition.UpperRight;
			nameFontSize = 11;
			nameRadiusOffset = 3;
		}
	}
	
	public static class ParetoPointDisplayGraphics {
		private ParetoPointDisplayData dData;
		
		private Color drawColor;
		private Font nameTextFont;
		
		
		public ParetoPointDisplayGraphics(ParetoPointDisplayData data) {
			dData = new ParetoPointDisplayData(data);
			
			JLabel lbl = new JLabel();
			drawColor = new Color(dData.shapeColorRed, dData.shapeColorGreen, dData.shapeColorBlue);
			if (dData.boldText) nameTextFont = new Font(lbl.getFont().getName(), Font.BOLD, dData.nameFontSize);
			else nameTextFont = new Font(lbl.getFont().getName(), Font.PLAIN, dData.nameFontSize);
		}
		
		public void drawInGraphics(Graphics2D g2, int cx, int cy) {
			if (!dData.showOnPlot) return;
			
			g2.setStroke(new BasicStroke(dData.shapeLineWidth));
			g2.setColor(drawColor);
			
			switch(dData.shapeType) {
			case Circle:
				g2.drawArc(cx-dData.shapeRadius, cy-dData.shapeRadius, dData.shapeRadius*2, dData.shapeRadius*2, 0, 360);
				break;
			case CrossHair:
				g2.drawLine(cx-dData.shapeRadius, cy, cx+dData.shapeRadius, cy);
				g2.drawLine(cx, cy-dData.shapeRadius, cx, cy+dData.shapeRadius);
				break;
			case Diamond:
				g2.drawLine(cx, cy+dData.shapeRadius, cx+dData.shapeRadius, cy);
				g2.drawLine(cx+dData.shapeRadius, cy, cx, cy-dData.shapeRadius);
				g2.drawLine(cx, cy-dData.shapeRadius, cx-dData.shapeRadius, cy);
				g2.drawLine(cx-dData.shapeRadius, cy, cx, cy+dData.shapeRadius);
				break;
			case Square:
			{
				int sqSideHalfLength = (int)(0.707f*dData.shapeRadius + 0.5f);
				g2.drawLine(cx-sqSideHalfLength, cy+sqSideHalfLength, cx+sqSideHalfLength, cy+sqSideHalfLength);
				g2.drawLine(cx+sqSideHalfLength, cy+sqSideHalfLength, cx+sqSideHalfLength, cy-sqSideHalfLength);
				g2.drawLine(cx+sqSideHalfLength, cy-sqSideHalfLength, cx-sqSideHalfLength, cy-sqSideHalfLength);
				g2.drawLine(cx-sqSideHalfLength, cy-sqSideHalfLength, cx-sqSideHalfLength, cy+sqSideHalfLength);
			}
				break;
			case Triangle:
			{
				float sideLength = 1.71f * dData.shapeRadius;
				int thirdSideLength = (int)(0.3333f*sideLength + 0.5f);
				int halfSideLength = (int)(0.5f*sideLength + 0.5f);
				int twoThirdsSideLength = (int)(0.6667f*sideLength + 0.5f);
				
				g2.drawLine(cx-halfSideLength, cy+thirdSideLength, cx+halfSideLength, cy+thirdSideLength);
				g2.drawLine(cx+halfSideLength, cy+thirdSideLength, cx, cy-twoThirdsSideLength);
				g2.drawLine(cx, cy-twoThirdsSideLength, cx-halfSideLength, cy+thirdSideLength);
			}
				break;
			case InvertedTriangle:
			{
				float sideLength = 1.71f * dData.shapeRadius;
				int thirdSideLength = (int)(0.3333f*sideLength + 0.5f);
				int halfSideLength = (int)(0.5f*sideLength + 0.5f);
				int twoThirdsSideLength = (int)(0.6667f*sideLength + 0.5f);
				
				g2.drawLine(cx-halfSideLength, cy-thirdSideLength, cx+halfSideLength, cy-thirdSideLength);
				g2.drawLine(cx+halfSideLength, cy-thirdSideLength, cx, cy+twoThirdsSideLength);
				g2.drawLine(cx, cy+twoThirdsSideLength, cx-halfSideLength, cy-thirdSideLength);
			}
				break;
			case XShape:
			{
				int sqSideHalfLength = (int)(0.707f*dData.shapeRadius + 0.5f);
				g2.drawLine(cx-sqSideHalfLength, cy+sqSideHalfLength, cx+sqSideHalfLength, cy-sqSideHalfLength);
				g2.drawLine(cx-sqSideHalfLength, cy-sqSideHalfLength, cx+sqSideHalfLength, cy+sqSideHalfLength);
			}
				break;
			}

			
			if (!dData.showNameOnPlot) return;
			
			g2.setColor(Color.BLACK);
			g2.setFont(nameTextFont);
			Rectangle2D strRect = g2.getFontMetrics(nameTextFont).getStringBounds(dData.nameTextToDisplay, g2);
			float textHeight = (float)strRect.getHeight();
			float textWidth = (float)strRect.getWidth();
			float posRadius = dData.shapeRadius + dData.nameRadiusOffset;
			
			switch (dData.textPosition) {
			case Above:
				g2.drawString(dData.nameTextToDisplay, (int)(cx + 0.5f), (int)(cy - posRadius));
				break;
			case AboveCentered:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - 0.5f*textWidth + 0.5f), (int)(cy - posRadius));
				break;
			case AboveLeft:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - textWidth), (int)(cy - posRadius));
				break;
			case Bottom:
				g2.drawString(dData.nameTextToDisplay, (int)(cx + 0.5f), (int)(cy + posRadius + textHeight));
				break;
			case BottomCentered:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - 0.5f*textWidth + 0.5f), (int)(cy + posRadius + textHeight));
				break;
			case BottomLeft:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - textWidth), (int)(cy + posRadius + textHeight));
				break;
			case Left:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - posRadius - textWidth), (int)(cy + 0.5f*textHeight));
				break;
			case LowerLeft:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - textWidth - posRadius*0.707f), (int)(cy + 0.5f*textHeight + posRadius*0.707f));
				break;
			case LowerRight:
				g2.drawString(dData.nameTextToDisplay, (int)(cx + posRadius*0.707f), (int)(cy + 0.5f*textHeight + posRadius*0.707f));
				break;
			case Right:
				g2.drawString(dData.nameTextToDisplay, (int)(cx + posRadius), (int)(cy + 0.5f*textHeight));
				break;
			case UpperLeft:
				g2.drawString(dData.nameTextToDisplay, (int)(cx - textWidth - posRadius*0.707f), (int)(cy - posRadius*0.707f));
				break;
			case UpperRight:
				g2.drawString(dData.nameTextToDisplay, (int)(cx + posRadius*0.707f), (int)(cy - posRadius*0.707f));
				break;
			}
		}
	}
}
