package pvc.gui.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import pvc.calc.TCOCalculator;
import pvc.datamgmt.comp.CostVsGHGAxesSetup;
import pvc.datamgmt.comp.CostVsGHGDisplaySetup;
import pvc.datamgmt.comp.DUnits;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class CostVsGHGJPanel extends JPanel {
	
	//Constants
	private static final int AxesNumbersLabelHeight = 16;
	private static final int AxesTitleLabelHeight = 20;
	private static final int TicSize = 4;
	private static final int TClearance = 2;
	private static final int VAxesNumbersLabelWidth = 30;

	private static final int HardLimitNumAxesTics = 51;

	
	//GUI
	private int pWidth, pHeight, yAxisNumLblsX, gLabelX, gLabelWidth, xAxisNumPix, xAxisTitleLabelY, xAxisNumLblsY, gLabelHeight, yAxisNumPix;
	private JLabel lblYAxisTitle, lblXAxisTitle;
	private JLabel[] lblXNums, lblYNums;
	private CostVsGHGJLabel gLabel;
	
	//Data
	private CostVsGHGAxesSetup axes;
	private TCOCalculator.CParetoPoint[] costVsGHGRes;
	private CostVsGHGDisplaySetup dispSetup;

	
	public CostVsGHGJPanel(int panelWidth, int panelHeight) {
		super();
		
		//Sizing & positioning
		pWidth = panelWidth;
		pHeight = panelHeight;
		
		yAxisNumLblsX = AxesTitleLabelHeight + TClearance;
		gLabelX = yAxisNumLblsX + VAxesNumbersLabelWidth + TClearance;
		gLabelWidth = pWidth - gLabelX - TClearance;
		xAxisNumPix = gLabelWidth - TicSize;
		
		xAxisTitleLabelY = pHeight - AxesTitleLabelHeight;
		xAxisNumLblsY = xAxisTitleLabelY - TClearance - AxesNumbersLabelHeight;
		gLabelHeight = xAxisNumLblsY - TClearance*2;
		yAxisNumPix = gLabelHeight - TicSize;
		
		
		//Labels for Axes titles
		lblYAxisTitle = new JLabel();
		lblYAxisTitle.setSize(AxesTitleLabelHeight, gLabelHeight);
		lblYAxisTitle.setLocation(0, TClearance);
		lblYAxisTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblYAxisTitle.setVerticalAlignment(SwingConstants.CENTER);
		add(lblYAxisTitle);
		 
		lblXAxisTitle = new JLabel();
		lblXAxisTitle.setSize(gLabelWidth, AxesTitleLabelHeight);
		lblXAxisTitle.setLocation(gLabelX, xAxisTitleLabelY);
		lblXAxisTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblXAxisTitle.setVerticalAlignment(SwingConstants.CENTER);
		add(lblXAxisTitle);

		lblXNums = new JLabel[HardLimitNumAxesTics];
		lblYNums = new JLabel[HardLimitNumAxesTics];
		
		for (int i=0; i<HardLimitNumAxesTics; i++) {
			lblXNums[i] = new JLabel();
			lblXNums[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
			add(lblXNums[i]);
			
			lblYNums[i] = new JLabel();
			lblYNums[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
			lblYNums[i].setHorizontalAlignment(SwingConstants.RIGHT);
			add(lblYNums[i]);
		}

		//Graphics label
		gLabel = new CostVsGHGJLabel();
		gLabel.setSize(gLabelWidth, gLabelHeight);
		gLabel.setLocation(gLabelX, TClearance);
		add(gLabel);
		
		axes = null;
		costVsGHGRes = null;
		 
		setLayout(null);
		setSize(pWidth, pHeight);
		setOpaque(true);
	}
	
	public void setAxes(CostVsGHGAxesSetup axesSetup) {
		axes = axesSetup;
		
		//Fonts
		JLabel lbl = new JLabel();
        Font xTitleFont;
        if (axes.axesCaptionsBold) xTitleFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.axesCaptionsFontSize);
        else xTitleFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.axesCaptionsFontSize);
       
        Font axesNumbersFont;
        if (axes.numbersTextBold) axesNumbersFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.numbersFontSize);
        else axesNumbersFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.numbersFontSize);
		
        AffineTransform aft = new AffineTransform();
		aft.rotate(-0.5*Math.PI);

		Font yTitleFont;
		if (axes.axesCaptionsBold) yTitleFont = new Font(xTitleFont.getName(), Font.BOLD, axes.axesCaptionsFontSize).deriveFont(aft);
		else yTitleFont = new Font(xTitleFont.getName(), Font.PLAIN, axes.axesCaptionsFontSize).deriveFont(aft);
		 
		
		String xTitle = axes.ghgAxisTilteWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance) + ")";
		String yTitle = axes.costAxisTilteWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance) + ")";
		
		lblXAxisTitle.setFont(xTitleFont);
		lblXAxisTitle.setText(xTitle);
		
		lblYAxisTitle.setFont(yTitleFont);
		lblYAxisTitle.setText(yTitle);
		
		int numDecimalsX = axes.ghgAxis.numDecimals();
		int numTicsX = axes.ghgAxis.numTics();
		
		for (int i=0; i<numTicsX; i++) {
			if (i >= lblXNums.length) break;
			
			float value = axes.ghgAxis.valueAtTic(i);
			int pixPos = axes.ghgAxis.valueToPixPos(xAxisNumPix, value);
			int cx = gLabelX + TicSize + pixPos - VAxesNumbersLabelWidth/2;
			
			String stValue = NumToString.floatWNumDecimals(value, numDecimalsX);
			
			lblXNums[i].setText(stValue);
			lblXNums[i].setLocation(cx, xAxisNumLblsY);
			lblXNums[i].setHorizontalAlignment(SwingConstants.CENTER);
			if ((cx + VAxesNumbersLabelWidth) > (pWidth - TClearance)) {
				lblXNums[i].setLocation(pWidth - VAxesNumbersLabelWidth - TClearance, xAxisNumLblsY);
				lblXNums[i].setHorizontalAlignment(SwingConstants.RIGHT);
			}
			lblXNums[i].setFont(axesNumbersFont);
			lblXNums[i].setVisible(true);
		}		
		for (int i=numTicsX; i<lblXNums.length; i++) {
			lblXNums[i].setVisible(false);
		}
		
		int numDecimalsY = axes.costAxis.numDecimals();
		int numTicsY = axes.costAxis.numTics();
		
		for (int i=0; i<numTicsY; i++) {
			if (i >= lblYNums.length) break;
			
			float value = axes.costAxis.valueAtTic(i);
			int pixPos = axes.costAxis.valueToPixPos(yAxisNumPix, value);
			int cy = xAxisNumLblsY -TicSize - pixPos - AxesNumbersLabelHeight/2 - TClearance;
			
			String stValue = NumToString.floatWNumDecimals(value, numDecimalsY);
			
			lblYNums[i].setText(stValue);
			lblYNums[i].setLocation(yAxisNumLblsX, cy);
			if (cy < TClearance) {
				lblYNums[i].setLocation(yAxisNumLblsX, TClearance);
			}
			lblYNums[i].setFont(axesNumbersFont);
			lblYNums[i].setVisible(true);
		}		
		for (int i=numTicsY; i<lblYNums.length; i++) {
			lblYNums[i].setVisible(false);
		}
	}
	
	public void setPlot(TCOCalculator.CParetoPoint[] costVsGHGResult, CostVsGHGDisplaySetup displaySetup) {
		costVsGHGRes = costVsGHGResult;
		dispSetup = displaySetup;
	
		gLabel.repaint();
	}
		
	private class CostVsGHGJLabel extends JLabel {
		
		private CostVsGHGJLabel() {
			super();
		}
		
		@Override
	    protected void paintComponent(Graphics g) {
			//Call base function
			super.paintComponent(g);
						
			//Quick strike
			if (axes == null) return;
			Graphics2D g2 = (Graphics2D) g;
			
			int ox = TicSize-1;
			int oy = gLabelHeight-TicSize;
			int by = gLabelHeight;
			
			//Draw grid if requested
			if (dispSetup.showGrid()) {
				g2.setStroke(new BasicStroke(dispSetup.gridLineWidth()));
				g2.setColor(dispSetup.getGridColor());
				
				int numTics = axes.ghgAxis.numTics();
				for (int i=0; i<numTics; i++) {
					int x = ox + axes.ghgAxis.valueToPixPos(xAxisNumPix, axes.ghgAxis.valueAtTic(i));
					g2.drawLine(x, oy, x, oy-yAxisNumPix);
				}
				
				numTics = axes.costAxis.numTics();
				for (int i=0; i<numTics; i++) {
					int y = oy - axes.costAxis.valueToPixPos(yAxisNumPix, axes.costAxis.valueAtTic(i));
					g2.drawLine(ox, y, ox+xAxisNumPix, y);
				}
			}
			
			//Draw Axes
			g2.setStroke(new BasicStroke(dispSetup.axesLineWidth()));		
			
			g2.setColor(Color.BLACK);			
			g2.drawLine(ox, oy, ox+xAxisNumPix, oy);			
			g2.drawLine(ox, oy, ox, oy-yAxisNumPix);
			
			int numTics = axes.ghgAxis.numTics();
			for (int i=0; i<numTics; i++) {
				int x = ox + axes.ghgAxis.valueToPixPos(xAxisNumPix, axes.ghgAxis.valueAtTic(i));
				g2.drawLine(x, oy, x, by);
			}
			
			numTics = axes.costAxis.numTics();
			for (int i=0; i<numTics; i++) {
				int y = oy - axes.costAxis.valueToPixPos(yAxisNumPix, axes.costAxis.valueAtTic(i));
				g2.drawLine(ox, y, 0, y);
			}
			
			
			//Another quick strike
			if (costVsGHGRes == null) return;
			
			//Draw Symbols
			float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			float dollarPerMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			
			for (int i=0; i<costVsGHGRes.length; i++) {
				int vehID = dispSetup.getVehIDtoDraw(i);
				float ghgPerDistance = gCO2perMileToDisplayUnits * costVsGHGRes[vehID].gCO2perMile;
				float costPerDistance = dollarPerMileToDisplayUnits * costVsGHGRes[vehID].dollarPerMile;
				
				int x = ox + axes.ghgAxis.valueToPixPos(xAxisNumPix, ghgPerDistance);
				int y = oy - axes.costAxis.valueToPixPos(yAxisNumPix, costPerDistance);
				
				dispSetup.drawInGraphics(i, g2, x, y);
			}
		}
	}
}
