package pvc.gui.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.calc.UsePhaseSSimulator;
import pvc.calc.comp.ChgEventsCaseSummary;
import pvc.datamgmt.comp.*;
import pvc.utility.CGHGHistogram;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class GHGHistogramsPanelMaker {
	//Sizing constants
	private static final int TClear = 2;
	private static final int TMargin = 4;
	private static final int WinMargin = 10;
	
	private static final int AxesNumbersLabelHeight = 16;
	private static final int AxesTitleLabelHeight = 20;
	private static final int VAxesNumbersLabelWidth = 30;

	private static final float GHGHstStDrawWidth = 2.5f;
	private static final float GHGHstStDrawHeight = 1.0f;
	private static final float HourlyStDrawWidth = 1.0f;

	private static final int HardLimitNumAxesTics = 51;
	private static final int TicSize = 4;
	private static final int BoxPlotGap = (int)(TicSize*3.5f + 0.5f);
	private static final int BoxPlotHalfBarWidth = BoxPlotGap;
	
	private static final float[] YAxisHstNumValues = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
	private static final int[] XAxisHrNumValues = {0, 6, 12, 18};
	private static final int YAxisHstNumSubDiv = 5;
	private static final int XAxisHrMainNumDiv = 8;

	
	private static int titleFontSize = 14;
	
	public static GHGHistogramsJPanel createGHGHistogramsPanel(int sWidth, GHGDisplaySetup displaySetup) {
		return new GHGHistogramsJPanel(sWidth, displaySetup);
	}
	
	public static class GHGHistogramsJPanel extends JPanel {
		private GHGDisplaySetup dSetup;
		private VehSummaryPanel[] subPanels;
		
		private GHGHistogramsJPanel(int sWidth, GHGDisplaySetup displaySetup) {
			super();
			
			setLayout(null);
			setOpaque(true);
			
			dSetup = displaySetup;
			
			int subPanelsWidth = sWidth - TClear*2;
			int subPanelsHeight = calcSubPanelHeight(subPanelsWidth);
			
			int prefHeight = 0;
			int numSubPanels = dSetup.numDisplayedVehicles();
			if (numSubPanels > 0) {
				prefHeight = TClear*2 + numSubPanels*subPanelsHeight + (numSubPanels-1)*WinMargin;
			}	
			
			setPreferredSize(new Dimension(subPanelsWidth, prefHeight));			
			subPanels = new VehSummaryPanel[numSubPanels];
			
			int cID = 0;
			int numVeh = dSetup.numVehicles();
			
			int axesLW = dSetup.axesLineWidth();
			int bxpLW = dSetup.boxPlotLineWidth();
			int gridLW = dSetup.gridLineWidth();
			int mGridLW = dSetup.minorGridLineWidth();
			if (!dSetup.showGrid()) {
				gridLW = -1;
				mGridLW = -1;
			}
			if (!dSetup.showMinorGrid()) {
				mGridLW = -1;
			}
			
			titleFontSize = dSetup.titleFontSize();
			
			int cx = TClear;
			int cy = TClear;
			for (int i=0; i<numVeh; i++) {
				GHGDisplaySetup.VehGHGDisplayOpions curVehOptions = dSetup.getVehAtDisplayPos(i);
				
				if (curVehOptions.isShown) {
					subPanels[cID] = new VehSummaryPanel(subPanelsWidth, subPanelsHeight, axesLW, bxpLW, gridLW, mGridLW,
							dSetup.getGridColor(), dSetup.getMinorGridColor(), dSetup.getL1Color(), dSetup.getL2Color(), dSetup.getDCColor(),
							curVehOptions.pdmLineWidth, curVehOptions.cdfLineWidth,
							new Color(curVehOptions.pdmRed, curVehOptions.pdmGreen, curVehOptions.pdmBlue),
							new Color(curVehOptions.cdfRed, curVehOptions.cdfGreen, curVehOptions.cdfBlue),
							new Color(curVehOptions.bxfRed, curVehOptions.bxfGreen, curVehOptions.bxfBlue));
					
					subPanels[cID].setSize(subPanelsWidth, subPanelsHeight);
					subPanels[cID].setLocation(cx, cy);
					add(subPanels[cID]);
					
					cy += subPanelsHeight + WinMargin;
					cID++;
				}
			}
			
			cy = TClear + subPanelsHeight + WinMargin/2 - TClear/2;
			for (int i=1; i<numSubPanels; i++) {
				ColorJLabel clblPartition = new ColorJLabel(Color.BLACK);
				clblPartition.setSize(sWidth, TClear/2);
				clblPartition.setLocation(0, cy);
				add(clblPartition);
				
				cy += subPanelsHeight + WinMargin;
			}
		}		
		public void setAxes(GHGAxesSetup axes) {
			for (int i=0; i<subPanels.length; i++) subPanels[i].setAxes(axes);
		}
		public void setPlot(UsePhaseSSimulator.VehGHGAnalysisResult[] vehHstRes) {
			int cID = 0;
			int numVeh = dSetup.numVehicles();
			
			for (int i=0; i<numVeh; i++) {
				GHGDisplaySetup.VehGHGDisplayOpions curVehOptions = dSetup.getVehAtDisplayPos(i);
				
				if (curVehOptions.isShown) {
					int vehID = curVehOptions.vehID();
					subPanels[cID].setPlot(vehHstRes[vehID], curVehOptions.displayedTitle);
					cID++;
				}
			}
		}
	}
	private static int calcSubPanelHeight(int subPanelWidth) {
		int sDrawWidth = subPanelWidth - (TMargin + TClear*3 + WinMargin + AxesTitleLabelHeight + VAxesNumbersLabelWidth);
		int sDrawHeight = (int)((sDrawWidth*GHGHstStDrawHeight/(GHGHstStDrawWidth+HourlyStDrawWidth)) + 0.5f);	//Includes the tick-size inside JLabel
		int bxpAreaHeight = BoxPlotGap*2 + BoxPlotHalfBarWidth*2 + 1;
		int subPanelHeight = TClear*4 + AxesTitleLabelHeight + AxesNumbersLabelHeight + sDrawHeight + bxpAreaHeight;
		return subPanelHeight;
	}
	private static int calcGHGHistLabelWidth(int subPanelWidth) {
		int sDrawWidth = subPanelWidth - (TMargin + TClear*3 + WinMargin + AxesTitleLabelHeight + VAxesNumbersLabelWidth);
		int ghgHstWidth = (int)((sDrawWidth*GHGHstStDrawWidth/(GHGHstStDrawWidth+HourlyStDrawWidth)) + 0.5f);	//Includes the tick-size inside JLabel
		return ghgHstWidth;
	}
	private static int calcGHGHistLabelHeight(int subPanelHeight) {
		return subPanelHeight - (TClear*4 + AxesTitleLabelHeight + AxesNumbersLabelHeight);
	}
	private static int calcHourlyLabelWidth(int subPanelWidth) {
		int sDrawWidth = subPanelWidth - (TMargin + TClear*3 + WinMargin + AxesTitleLabelHeight + VAxesNumbersLabelWidth);
		int ghgHstWidth = (int)((sDrawWidth*GHGHstStDrawWidth/(GHGHstStDrawWidth+HourlyStDrawWidth)) + 0.5f);	//Includes the tick-size inside JLabel
		return sDrawWidth - ghgHstWidth;
	}
	private static int calcHourlyLabelHeight(int subPanelHeight) {
		int sHeight = subPanelHeight - (TClear*4 + AxesTitleLabelHeight + AxesNumbersLabelHeight);
		int topSectionHeight = AxesNumbersLabelHeight*5 + TMargin*5;
		return sHeight - topSectionHeight;
	}

	private static class VehSummaryPanel extends JPanel {
		private int pWidth, pHeight;
		private int axesLineWidth, boxPlotLineWidth, gridLineWidth, minorGridLineWidth;
		private Color gridColor, minorGridColor;
		private Color[] chgTypeColors;
		
		private int pdmLineWidth, cdfLineWidth;
		private Color pdmColor, cdfColor, bxpColor;
		
		private int hstLblX, hstLblY, hstLblW, hstLblH, hstXAxisNumLabelsY, hstXAxisTitleLabelsY;
		private int hrLblX, hrLblY, hrLblW, hrLblH;
		private HistogramJLabel hstLabel;
		private HourlyJLabel hrLabel;
		
		private JLabel[] hstYAxisNumLabels, hstXAxisNumLabels, chgTypeTextLbls, hrNumLabels;
		private JLabel lblTitle, lblGridAvGHG, lblBEVFracMiles, lblBEVFracDays, lblBEVFracDaysFailed;
		private JLabel hstXTitle, hstYTitle, hrXTitle;
		private ColorJLabel[] chgTypeColorLbls; 
		
		private int yPixBxpCenterInLabel, yPixBxpAreaTopInLabel, yPixBxpAreaBottomInLabel, yPixVerticalAxisZeroInLabel, yPixRangeHst;
		private int yPixHstInLabel(float pScaledValue) {
			return yPixVerticalAxisZeroInLabel - (int)(pScaledValue*yPixRangeHst + 0.5f);
		}
		private int yPixHstOutsideLabel(float pScaledValue) {
			return hstLblY + yPixHstInLabel(pScaledValue);
		}
		
		private int xPixZeroGHGInLabel, xPixRangeHst, numXAxisMainDiv, numXAxisSubDiv;
		private float maxGHGinDisplayUnits;
		private int xPixHstInLabel(float ghgInDisplayUnits) {
			return xPixZeroGHGInLabel + (int)((ghgInDisplayUnits/maxGHGinDisplayUnits)*xPixRangeHst + 0.5f);
		}
		private int xPixHstOutsideLabel(float ghgInDisplayUnits) {
			return hstLblX + xPixHstInLabel(ghgInDisplayUnits);
		}
		private int xPixZeroHrInLabel, xPixRangeHr, yPixZeroPDMHr, yPixRangeHr;
		private float maxStackedPDM;
		private int xPixHrInLabel(float hour) {
			return xPixZeroHrInLabel + (int)((hour/ChgEventsCaseSummary.HoursPerDay)*xPixRangeHr+ 0.5f);
		}
		private int xPixHrOutsideLabel(float hour) {
			return hrLblX + xPixHrInLabel(hour);
		}
		private int yPixHrInLabel(float stackedPDM) {
			return yPixZeroPDMHr - (int)((stackedPDM/maxStackedPDM)*yPixRangeHr + 0.5f);
		}
		
		private float[][] hourlyStackedPDM;
		private CGHGHistogram.BoxPlot ghgBoxPlot;
		private CGHGHistogram.LSPoint[] lsPoints;

		private VehSummaryPanel(int sWidth, int sHeight, int axesLW, int bxpLW, int gridLW, int mGridLW, Color gColor, Color mgColor, 
				Color chgL1Color, Color chgL2Color, Color chgDCColor, int pdmLW, int cdfLW, Color pdmC, Color cdfC, Color bxpC) {
			//Initialize
			super();
			setLayout(null);
			setOpaque(true);
			
			pWidth = sWidth;
			pHeight = sHeight;
			setSize(pWidth, pHeight);
			
			axesLineWidth = axesLW;
			boxPlotLineWidth = bxpLW;
			gridLineWidth = gridLW;
			minorGridLineWidth = mGridLW;
			
			gridColor = gColor;
			minorGridColor = mgColor;
			
			chgTypeColors = new Color[ChargerTypes.values().length];
			chgTypeColors[ChargerTypes.L1.ordinal()] = chgL1Color;
			chgTypeColors[ChargerTypes.L2.ordinal()] = chgL2Color;
			chgTypeColors[ChargerTypes.DC.ordinal()] = chgDCColor;
			
			pdmLineWidth = pdmLW;
			cdfLineWidth = cdfLW;
			pdmColor = pdmC;
			cdfColor = cdfC;
			bxpColor = bxpC;
			
			//Create components
	        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

	        hstLblW = calcGHGHistLabelWidth(pWidth);
	        hstLblH = calcGHGHistLabelHeight(pHeight);
	        hrLblW = calcHourlyLabelWidth(pWidth);
	        hrLblH = calcHourlyLabelHeight(pHeight);
	        	        
	        hstLblX = TMargin + hrLblW + WinMargin + AxesTitleLabelHeight + TClear*2 + VAxesNumbersLabelWidth;
	        hstLblY = TClear;
	        hrLblX = TMargin;
	        hrLblY = pHeight - (TClear*3 + AxesTitleLabelHeight + AxesNumbersLabelHeight + hrLblH);
	        
	        hstXAxisNumLabelsY = pHeight - (TClear*2 + AxesTitleLabelHeight + AxesNumbersLabelHeight);
	        hstXAxisTitleLabelsY = pHeight - (TClear + AxesTitleLabelHeight);
	        
	        xPixZeroHrInLabel = TClear;
	        xPixRangeHr = hrLblW - 2*TClear;
	        yPixZeroPDMHr = hrLblH - TicSize;
	        yPixRangeHr = hrLblH - TicSize - TClear;
	        	        
	        hstLabel = new HistogramJLabel();
	        hstLabel.setSize(hstLblW, hstLblH);
	        hstLabel.setLocation(hstLblX, hstLblY);
	        add(hstLabel);
	        
	        hrLabel = new HourlyJLabel();
	        hrLabel.setSize(hrLblW, hrLblH);
	        hrLabel.setLocation(hrLblX, hrLblY);
	        add(hrLabel);
	        
	        yPixBxpCenterInLabel = (BoxPlotGap*2 - TClear)/2 + BoxPlotHalfBarWidth + 1;
	        yPixBxpAreaTopInLabel = TClear;
	        yPixBxpAreaBottomInLabel = BoxPlotGap*2 + BoxPlotHalfBarWidth*2 + 1;
	        yPixVerticalAxisZeroInLabel = hstLblH - TicSize;
	        yPixRangeHst = yPixVerticalAxisZeroInLabel - yPixBxpAreaBottomInLabel;
	        
	        xPixZeroGHGInLabel = TicSize;
	        xPixRangeHst = hstLblW - xPixZeroGHGInLabel - TClear;
	        
	        hstXTitle = new JLabel();
	        hstXTitle.setSize(hstLblW, AxesTitleLabelHeight);
	        hstXTitle.setLocation(hstLblX, hstXAxisTitleLabelsY);
	        hstXTitle.setHorizontalAlignment(SwingConstants.CENTER);
	        add(hstXTitle);
	        
	        int yTitleSizeOffset = (yPixVerticalAxisZeroInLabel - yPixBxpAreaBottomInLabel)/3;
	        
	        hstYTitle = new JLabel();
	        hstYTitle.setSize(AxesTitleLabelHeight, yPixVerticalAxisZeroInLabel - yPixBxpAreaBottomInLabel - yTitleSizeOffset);
	        hstYTitle.setLocation(hrLblX + hrLblW + WinMargin, hstLblY + yPixBxpAreaBottomInLabel);
	        hstYTitle.setHorizontalAlignment(SwingConstants.CENTER);
	        hstYTitle.setVerticalAlignment(SwingConstants.BOTTOM);
	        add(hstYTitle);
	        
	        hrNumLabels = new JLabel[XAxisHrNumValues.length];
	        for (int i=0; i<hrNumLabels.length; i++) {
	        	hrNumLabels[i] = new JLabel(""+XAxisHrNumValues[i]+":00");
	        	hrNumLabels[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
	        	
	        	int x = xPixHrOutsideLabel(XAxisHrNumValues[i]) - VAxesNumbersLabelWidth/2;
	        	if (x < TClear) {
	        		x = TClear;
	        		hrNumLabels[i].setLocation(x, hstXAxisNumLabelsY);
	        		hrNumLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
	        	} else {
	        		hrNumLabels[i].setLocation(x, hstXAxisNumLabelsY);
	        		hrNumLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
	        	}
	        	
	        	hrNumLabels[i].setVisible(false);
	        	add(hrNumLabels[i]);
	        }
	        
	        hrXTitle = new JLabel("Hour of the Day");
	        hrXTitle.setSize(hrLblW, AxesTitleLabelHeight);
	        hrXTitle.setLocation(hrLblX, hstXAxisTitleLabelsY);
	        hrXTitle.setHorizontalAlignment(SwingConstants.CENTER);
	        hrXTitle.setVisible(false);
	        add(hrXTitle);
	        	        
	        hstYAxisNumLabels = new JLabel[YAxisHstNumValues.length];
	        for (int i=0; i<hstYAxisNumLabels.length; i++) {
	        	hstYAxisNumLabels[i] = new JLabel();
	        	hstYAxisNumLabels[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
	        	hstYAxisNumLabels[i].setLocation(hstLblX - VAxesNumbersLabelWidth - TClear, 
	        			yPixHstOutsideLabel(YAxisHstNumValues[i]) - (2*AxesNumbersLabelHeight)/3);
	        	hstYAxisNumLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
	        	add(hstYAxisNumLabels[i]);
	        }
	        
	        hstXAxisNumLabels = new JLabel[HardLimitNumAxesTics];
	        for (int i=0; i<hstXAxisNumLabels.length; i++) {
	        	hstXAxisNumLabels[i] = new JLabel();
	        	hstXAxisNumLabels[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
	        	hstXAxisNumLabels[i].setVisible(false);
	        	add(hstXAxisNumLabels[i]);
	        }
	        
	        int titleLblW = hrLblW/2;
	        int bevLblsW = hrLblW - titleLblW - TClear*2;
	        int cy = TClear;
	        int cx = TMargin+TClear;
	        
	        lblTitle = new JLabel();
	        Font titleFont = new Font(lblTitle.getFont().getName(), Font.BOLD, titleFontSize);
	        lblTitle.setSize(titleLblW, AxesNumbersLabelHeight*3 + TMargin*2);
	        lblTitle.setLocation(cx, cy);
	        lblTitle.setFont(titleFont);
	        lblTitle.setVerticalAlignment(SwingConstants.TOP);
	        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);
	        add(lblTitle);
	        
	        cx += titleLblW + TClear;
	        
	        lblBEVFracMiles = new JLabel();
	        lblBEVFracMiles.setSize(bevLblsW, AxesNumbersLabelHeight);
	        lblBEVFracMiles.setLocation(cx, cy);
	        lblBEVFracMiles.setHorizontalAlignment(SwingConstants.RIGHT);
	        lblBEVFracMiles.setVisible(false);
	        add(lblBEVFracMiles);
	        
	        cy += AxesNumbersLabelHeight+TMargin;
	        
	        lblBEVFracDays = new JLabel();
	        lblBEVFracDays.setSize(bevLblsW, AxesNumbersLabelHeight);
	        lblBEVFracDays.setLocation(cx, cy);
	        lblBEVFracDays.setHorizontalAlignment(SwingConstants.RIGHT);
	        lblBEVFracDays.setVisible(false);
	        add(lblBEVFracDays);
	        
	        cy += AxesNumbersLabelHeight+TMargin;
	        
	        lblBEVFracDaysFailed = new JLabel();
	        lblBEVFracDaysFailed.setSize(bevLblsW, AxesNumbersLabelHeight);
	        lblBEVFracDaysFailed.setLocation(cx, cy);
	        lblBEVFracDaysFailed.setHorizontalAlignment(SwingConstants.RIGHT);
	        lblBEVFracDaysFailed.setVisible(false);
	        add(lblBEVFracDaysFailed);
	        
	        cx = TMargin+TClear;
	        cy += AxesNumbersLabelHeight+TMargin;

	        lblGridAvGHG = new JLabel();
	        lblGridAvGHG.setSize(hrLblW, AxesNumbersLabelHeight);
	        lblGridAvGHG.setLocation(cx, cy);
	        lblGridAvGHG.setHorizontalAlignment(SwingConstants.LEFT);
	        lblGridAvGHG.setVisible(false);
	        add(lblGridAvGHG);
	        
	        cy += AxesNumbersLabelHeight+TMargin;
	        
	        int chgTypeLabelXUnit = (hrLblW - TClear*2)/3;
	        int colorLabelW = (chgTypeLabelXUnit - TMargin)/3;
	        int colorLabelTextW = chgTypeLabelXUnit - TMargin - colorLabelW;
	        
	        chgTypeColorLbls = new ColorJLabel[chgTypeColors.length];
	        chgTypeTextLbls = new JLabel[chgTypeColorLbls.length];
	        
	        for (int i=0; i<chgTypeColorLbls.length; i++) {
	        	chgTypeColorLbls[i] = new ColorJLabel(chgTypeColors[i]);
	        	chgTypeColorLbls[i].setSize(colorLabelW, AxesNumbersLabelHeight+TClear);
	        	chgTypeColorLbls[i].setLocation(cx, cy-TClear/2);
	        	chgTypeColorLbls[i].setBorder(blackBorder);
	        	chgTypeColorLbls[i].setVisible(false);
	        	add(chgTypeColorLbls[i]);
	        	
	        	cx += colorLabelW + TMargin;
	        	
	        	chgTypeTextLbls[i] = new JLabel(ChargerTypes.values()[i].shortName);
	        	chgTypeTextLbls[i].setSize(colorLabelTextW, AxesNumbersLabelHeight);
	        	chgTypeTextLbls[i].setLocation(cx, cy);
	        	chgTypeTextLbls[i].setVisible(false);
	        	add(chgTypeTextLbls[i]);
	        	
	        	cx += colorLabelTextW + TClear;
	        }
	        
	        hourlyStackedPDM = null;
	        ghgBoxPlot = null;
	        lsPoints = null;
		}
		
		private void setAxes(GHGAxesSetup axes) {
			//Fonts
			JLabel lbl = new JLabel();
	        Font xTitleFont;
	        if (axes.axesCaptionsBold) xTitleFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.axesCaptionsFontSize);
	        else xTitleFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.axesCaptionsFontSize);
		       
	        Font axesNumbersFont;
	        if (axes.numbersTextBold) axesNumbersFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.numbersFontSize);
	        else axesNumbersFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.numbersFontSize);
		       
	        Font legendsFont;
	        if (axes.legendTextBold) legendsFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.legendFontSize);
	        else legendsFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.legendFontSize);
			
	        AffineTransform aft = new AffineTransform();
			aft.rotate(-0.5*Math.PI);

			Font yTitleFont;
			if (axes.axesCaptionsBold) yTitleFont = new Font(xTitleFont.getName(), Font.BOLD, axes.axesCaptionsFontSize).deriveFont(aft);
			else yTitleFont = new Font(xTitleFont.getName(), Font.PLAIN, axes.axesCaptionsFontSize).deriveFont(aft);

			//Histogram Vertical Axis Labels
			for (int i=0; i<hstYAxisNumLabels.length; i++) {
				hstYAxisNumLabels[i].setFont(axesNumbersFont);
				hstYAxisNumLabels[i].setText(NumToString.floatWNumDecimals(100f*YAxisHstNumValues[i], 0)+"%");
			}
			
			//Additional Info Labels
			lblGridAvGHG.setFont(legendsFont);
			lblBEVFracMiles.setFont(legendsFont);
			lblBEVFracDays.setFont(legendsFont);
			lblBEVFracDaysFailed.setFont(legendsFont);
			
			//Charger type labels 
			for (int i=0; i<chgTypeTextLbls.length; i++) {
				chgTypeTextLbls[i].setFont(legendsFont);
			}
			
			//Histogram x-axis
			maxGHGinDisplayUnits = axes.limGHGperDistInDisplayUnits();
			numXAxisMainDiv = axes.numAxisDiv();
			numXAxisSubDiv = axes.numBinsPerAxisDiv();
			
			for (int i=0; i<=numXAxisMainDiv; i++) {
				float ghgValue = (maxGHGinDisplayUnits*i)/(float)numXAxisMainDiv;
				String st = NumToString.posFloatWNumDecimals(ghgValue, axes.ghgAxisNumDecimals);
				
				int x = xPixHstOutsideLabel(ghgValue) - VAxesNumbersLabelWidth/2;
				if ((x + VAxesNumbersLabelWidth) > (pWidth-TClear)) {
					x = pWidth-TClear-VAxesNumbersLabelWidth;
					hstXAxisNumLabels[i].setLocation(x, hstXAxisNumLabelsY);
					hstXAxisNumLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
				} else {
					hstXAxisNumLabels[i].setLocation(x, hstXAxisNumLabelsY);
					hstXAxisNumLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
				}
				
				hstXAxisNumLabels[i].setFont(axesNumbersFont);
				hstXAxisNumLabels[i].setText(st);
				hstXAxisNumLabels[i].setVisible(true);
			}
			for (int i=numXAxisMainDiv+1; i<hstXAxisNumLabels.length; i++) {
				hstXAxisNumLabels[i].setVisible(false);
			}
			
			hstYTitle.setFont(yTitleFont);
			hstYTitle.setText("CDF or Scaled PDM");
			
			String ghgUnits = " ("+DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")";
			hstXTitle.setFont(xTitleFont);
			hstXTitle.setText(axes.ghgAxisTitleWithoutUnits+ghgUnits);
			
			for (int i=0; i<hrNumLabels.length; i++) {
				hrNumLabels[i].setFont(axesNumbersFont);
			}
			
			hrXTitle.setFont(xTitleFont);
		}
		private void setPlot(UsePhaseSSimulator.VehGHGAnalysisResult vehHstRes, String vehTitle) {
			lblTitle.setText("<html>"+vehTitle+"</html>");
			
			if (vehHstRes.wellToBattery_gCO2perKWh >= 0) {			
				float gridGHGinDisplayUnits = vehHstRes.wellToBattery_gCO2perKWh 
						* DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
				String dUnits = " ("+DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.ElectUnit)+")";
				String st = "Well-To-Battery GHG"+dUnits+": "+NumToString.floatWNumDecimals(gridGHGinDisplayUnits, 1);
				
				lblGridAvGHG.setText(st);
				lblGridAvGHG.setVisible(true);
			} else {
				lblGridAvGHG.setVisible(false);
			}			
			if (vehHstRes.fracMilesOnBEV >= 0) {
				String st = "BEV Dist. Travel "+NumToString.floatWNumDecimals(100f*vehHstRes.fracMilesOnBEV, 1)+"%";
				lblBEVFracMiles.setText(st);
				lblBEVFracMiles.setVisible(true);
			} else {
				lblBEVFracMiles.setVisible(false);
			}			
			if (vehHstRes.fracDaysOnBEV >= 0) {
				String st = "BEV Days Travel "+NumToString.floatWNumDecimals(100f*vehHstRes.fracDaysOnBEV, 1)+"%";
				lblBEVFracDays.setText(st);
				lblBEVFracDays.setVisible(true);
			} else {
				lblBEVFracDays.setVisible(false);
			}
			if (vehHstRes.fracDaysAlteredPlans >= 0) {
				String st = "Days Plan Altered "+NumToString.floatWNumDecimals(100f*vehHstRes.fracDaysAlteredPlans, 1)+"%";
				lblBEVFracDaysFailed.setText(st);
				lblBEVFracDaysFailed.setVisible(true);
			} else {
				lblBEVFracDaysFailed.setVisible(false);
			}
			
			if (vehHstRes.stackedHourlyChargingPDM != null) {
				for (int i=0; i<chgTypeTextLbls.length; i++) {
					chgTypeColorLbls[i].setVisible(true);
					chgTypeTextLbls[i].setVisible(true);
				}
				
				hourlyStackedPDM = vehHstRes.stackedHourlyChargingPDM;
				maxStackedPDM = 0.02f;
				for (int i=0; i<ChgEventsCaseSummary.HoursPerDay; i++) {
					float curStackedPDM = hourlyStackedPDM[0][i] + hourlyStackedPDM[1][i] + hourlyStackedPDM[2][i];
					maxStackedPDM = Math.max(maxStackedPDM, curStackedPDM);
				}
				
				for (int i=0; i<hrNumLabels.length; i++) {
					hrNumLabels[i].setVisible(true);
				}
				hrXTitle.setVisible(true);
				
				hrLabel.setVisible(true);
				hrLabel.repaint();
			} else {
				for (int i=0; i<chgTypeTextLbls.length; i++) {
					chgTypeColorLbls[i].setVisible(false);
					chgTypeTextLbls[i].setVisible(false);
				}
				
				for (int i=0; i<hrNumLabels.length; i++) {
					hrNumLabels[i].setVisible(false);
				}
				hrXTitle.setVisible(false);

				hourlyStackedPDM = null;
				hrLabel.setVisible(false);
			}
			
	        lsPoints = vehHstRes.ghgHistogram.genLadderStepInOutputUnits();
	        if (vehHstRes.mfgEqGCO2perMile >= 0) {
	        	ghgBoxPlot = new CGHGHistogram.BoxPlot(vehHstRes.ghgHistogram.getBoxPlotInOutputUnits(vehHstRes.averageGCO2perMile), vehHstRes.mfgEqGCO2perMile);
	        } else {
	        	ghgBoxPlot = vehHstRes.ghgHistogram.getBoxPlotInOutputUnits(vehHstRes.averageGCO2perMile);
	        }
			hstLabel.repaint();
		}
		
		private class HistogramJLabel extends JLabel {
			private HistogramJLabel() {
				super();
			}
			
			@Override
		    protected void paintComponent(Graphics g) {
				//Call base function
				super.paintComponent(g);

				//Quick-strike
				if (lsPoints == null) return;
				
				//Cast
				Graphics2D g2 = (Graphics2D) g;

				//Draw Minor grid lines if requested
				if (minorGridLineWidth > 0) {
					g2.setStroke(new BasicStroke(minorGridLineWidth));
					g2.setColor(minorGridColor);
					
					float deltaGHGMain = maxGHGinDisplayUnits/(float)numXAxisMainDiv;
					float deltaGHGSub = deltaGHGMain/(float)numXAxisSubDiv;
					
					for (int i=0; i<numXAxisMainDiv; i++) {
						for (int j=1; j<numXAxisSubDiv; j++) {
							float ghgValueInDisplayUnits = i*deltaGHGMain + j*deltaGHGSub;
							int x = xPixHstInLabel(ghgValueInDisplayUnits);
							g2.drawLine(x, yPixBxpAreaTopInLabel, x, yPixVerticalAxisZeroInLabel); 
						}
					}
					
					float deltaYMain = 1f/(float)(YAxisHstNumValues.length-1);
					float deltaYSub = deltaYMain/(float)YAxisHstNumSubDiv;
					
					for (int i=0; i<(YAxisHstNumValues.length-1); i++) {
						for (int j=1; j<YAxisHstNumSubDiv; j++) {
							float scValue = deltaYMain*i + deltaYSub*j;
							int y = yPixHstInLabel(scValue);
							g2.drawLine(xPixZeroGHGInLabel, y, xPixZeroGHGInLabel+xPixRangeHst, y);
						}
					}
				}
				
				//Draw Major grid lines if requested
				if (gridLineWidth > 0) {
					g2.setStroke(new BasicStroke(gridLineWidth));
					g2.setColor(gridColor);

					float deltaGHGMain = maxGHGinDisplayUnits/(float)numXAxisMainDiv;
					for (int i=1; i<numXAxisMainDiv; i++) {
						float ghgValueInDisplayUnits = i*deltaGHGMain;
						int x = xPixHstInLabel(ghgValueInDisplayUnits);
						g2.drawLine(x, yPixBxpAreaTopInLabel, x, yPixVerticalAxisZeroInLabel); 
					}
					
					float deltaYMain = 1f/(float)(YAxisHstNumValues.length-1);
					for (int i=1; i<(YAxisHstNumValues.length-1); i++) {
						float scValue = deltaYMain*i;
						int y = yPixHstInLabel(scValue);
						g2.drawLine(xPixZeroGHGInLabel, y, xPixZeroGHGInLabel+xPixRangeHst, y);
					}
				}
				
				//Draw Axes, frames & ticks
				g2.setStroke(new BasicStroke(axesLineWidth));
				g2.setColor(Color.BLACK);
				
				g2.drawLine(xPixZeroGHGInLabel, yPixBxpAreaTopInLabel, xPixZeroGHGInLabel, yPixVerticalAxisZeroInLabel);
				g2.drawLine(xPixZeroGHGInLabel+xPixRangeHst, yPixBxpAreaTopInLabel, xPixZeroGHGInLabel+xPixRangeHst, yPixVerticalAxisZeroInLabel);

				g2.drawLine(xPixZeroGHGInLabel, yPixBxpAreaTopInLabel, xPixZeroGHGInLabel+xPixRangeHst, yPixBxpAreaTopInLabel);
				g2.drawLine(xPixZeroGHGInLabel, yPixBxpAreaBottomInLabel, xPixZeroGHGInLabel+xPixRangeHst, yPixBxpAreaBottomInLabel);
				g2.drawLine(xPixZeroGHGInLabel, yPixVerticalAxisZeroInLabel, xPixZeroGHGInLabel+xPixRangeHst, yPixVerticalAxisZeroInLabel);
				
				float deltaGHGMain = maxGHGinDisplayUnits/(float)numXAxisMainDiv;
				for (int i=0; i<=numXAxisMainDiv; i++) {
					float ghgValueInDisplayUnits = i*deltaGHGMain;
					int x = xPixHstInLabel(ghgValueInDisplayUnits);
					
					g2.drawLine(x, yPixVerticalAxisZeroInLabel, x, yPixVerticalAxisZeroInLabel+TicSize);
					g2.drawLine(x, yPixBxpAreaTopInLabel, x, yPixBxpAreaTopInLabel+TicSize);
					g2.drawLine(x, yPixBxpAreaBottomInLabel, x, yPixBxpAreaBottomInLabel-TicSize);
				}
				
				for (int i=0; i<YAxisHstNumValues.length; i++) {
					int y = yPixHstInLabel(YAxisHstNumValues[i]);
					g2.drawLine(0, y, xPixZeroGHGInLabel, y);
				}
				
				//Draw PDM
				g2.setStroke(new BasicStroke(pdmLineWidth));
				g2.setColor(pdmColor);
				
				float maxPDM = 1f/(float)lsPoints.length;
				for (int i=0; i<lsPoints.length; i++) {
					maxPDM = Math.max(maxPDM, lsPoints[i].pdmValue);
				}
				
				int px = xPixHstInLabel(lsPoints[0].ghgPerDistance);
				int py = yPixHstInLabel(lsPoints[0].pdmValue/maxPDM);
				
				for (int i=1; i<lsPoints.length; i++) {
					int cx = xPixHstInLabel(lsPoints[i].ghgPerDistance);
					int cy = yPixHstInLabel(lsPoints[i].pdmValue/maxPDM);
					
					g2.drawLine(px, py, cx, cy);
					
					px = cx;
					py = cy;
				}
				
				//Draw CDF
				g2.setStroke(new BasicStroke(cdfLineWidth));
				g2.setColor(cdfColor);
				
				px = xPixHstInLabel(lsPoints[0].ghgPerDistance);
				py = yPixHstInLabel(lsPoints[0].cdfValue);
				
				for (int i=1; i<lsPoints.length; i++) {
					int cx = xPixHstInLabel(lsPoints[i].ghgPerDistance);
					int cy = yPixHstInLabel(lsPoints[i].cdfValue);
					
					g2.drawLine(px, py, cx, cy);
					
					px = cx;
					py = cy;
				}
				
				//Draw Box-plot
				g2.setStroke(new BasicStroke(boxPlotLineWidth));
				g2.setColor(Color.BLACK);

				int yCenter = yPixBxpCenterInLabel;
				int yBoxTop = yCenter - BoxPlotHalfBarWidth;
				int yBoxBottom = yCenter + BoxPlotHalfBarWidth;
				int yErrBarTop = yCenter - BoxPlotHalfBarWidth/2;
				int yErrBarBottom = yCenter + BoxPlotHalfBarWidth/2;
				int x5 = xPixHstInLabel(ghgBoxPlot.x05);
				int x25 = xPixHstInLabel(ghgBoxPlot.x25);
				int x50 = xPixHstInLabel(ghgBoxPlot.x50);
				int x75 = xPixHstInLabel(ghgBoxPlot.x75);
				int x95 = xPixHstInLabel(ghgBoxPlot.x95);
				int xAv = xPixHstInLabel(ghgBoxPlot.average);
				
				g2.drawLine(x5, yCenter, x95, yCenter);
				g2.drawLine(x5, yErrBarTop, x5, yErrBarBottom);
				g2.drawLine(x95, yErrBarTop, x95, yErrBarBottom);
				
				g2.setColor(bxpColor);
				safeFillRect(g2, x25, yBoxTop, x75, yBoxBottom);
				
				g2.setColor(Color.BLACK);
				safeDrawRect(g2, x25, yBoxTop, x50, yBoxBottom);
				safeDrawRect(g2, x50, yBoxTop, x75, yBoxBottom);
				
				g2.drawLine(xAv, yBoxTop, xAv-BoxPlotHalfBarWidth, yCenter);
				g2.drawLine(xAv-BoxPlotHalfBarWidth, yCenter, xAv, yBoxBottom);
				g2.drawLine(xAv, yBoxBottom, xAv+BoxPlotHalfBarWidth, yCenter);
				g2.drawLine(xAv+BoxPlotHalfBarWidth, yCenter, xAv, yBoxTop);
			}
		}		
		private class HourlyJLabel extends JLabel {
			private HourlyJLabel() {
				super();
			}
			
			@Override
		    protected void paintComponent(Graphics g) {
				//Call base function
				super.paintComponent(g);

				//Quick-strike
				if (hourlyStackedPDM == null) return;
				
				//Cast
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(axesLineWidth));

				//Draw Hourly PDMs
				for (int i=0; i<chgTypeColors.length; i++) {
					g2.setColor(chgTypeColors[i]);
					
					int px = xPixHrInLabel(0);
					
					for (int j=0; j<ChgEventsCaseSummary.HoursPerDay; j++) {
						float prevStackValue = 0f;
						for (int k=0; k<i; k++) {
							prevStackValue += hourlyStackedPDM[k][j];
						}
						
						float curStackValue = prevStackValue + hourlyStackedPDM[i][j];
						
						int cx = xPixHrInLabel(j+1);
						int py = yPixHrInLabel(prevStackValue);
						int cy = yPixHrInLabel(curStackValue);
						
						safeFillRect(g2, px, py, cx, cy);						
						px = cx;
					}
				}
				
				//Draw Axis, frames and ticks
				int xMin = xPixHrInLabel(0);
				int xMax = xPixHrInLabel(24);
				int yMin = yPixHrInLabel(0);
				int yMax = yPixHrInLabel(maxStackedPDM);
				
				g2.setColor(Color.BLACK);
				
				g2.drawLine(xMin, yMin, xMax, yMin);
				g2.drawLine(xMin, yMax, xMax, yMax);
				g2.drawLine(xMin, yMin, xMin, yMax);
				g2.drawLine(xMax, yMin, xMax, yMax);
				
				int deltaHr = ChgEventsCaseSummary.HoursPerDay/XAxisHrMainNumDiv;
				for (int i=0; i<=XAxisHrMainNumDiv; i++) {
					int x = xPixHrInLabel(i*deltaHr);
					g2.drawLine(x, yMin, x, yMin+TicSize);
				}
			}
		}
		
		private static void safeDrawRect(Graphics2D g2, int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int w = Math.max(x1,  x2) - x;
			int y = Math.min(y1, y2);
			int h = Math.max(y1,  y2) - y;
			g2.drawRect(x, y, w, h);
		}		
		private static void safeFillRect(Graphics2D g2, int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int w = Math.max(x1,  x2) - x;
			int y = Math.min(y1, y2);
			int h = Math.max(y1,  y2) - y;
			g2.fillRect(x, y, w, h);
		}
	}
}
