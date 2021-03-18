package pvc.gui.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.datamgmt.comp.*;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class CostBarsJPanel extends JPanel {
	//Constants
	private static final int TClear = 2;
	private static final int TMargin = 4;
	private static final int LMargin = 7;
	private static final int WinMargin = 10;
	
	private static final int LegLblHeight = 30;
	private static final int LegLblWidth = 180;
	private static final int LegBoxHeight = 24;
	private static final int LegBoxWidth = 32;

	private static final int VehNamesLblWidth = 100;
	private static final int VehNamesLblHeight = 30;
	private static final int AxesNumbersLabelHeight = 16;
	private static final int AxesTitleLabelHeight = 20;
	private static final int TicSize = 4;

	private static final int MinHalfBarWidth = 4;
	private static final int MaxHalfBarWidth = 16;

	private static final int MinBarGapWidth = 4;
	private static final int MaxBarGapWidth = 48;
	private static final float TargetBarGapWidthRatio = 0.5f;


	private static final int VAxesNumbersLabelWidth = 30;
	private static final int HardLimitNumAxesTics = 51;

	
	//GUIs & GUI data
	private LegendJPanel legPanel;
	private CostBarsJLabel cbLabel;
	private JLabel[] lblVehNames, lblAxisNum;
	private JLabel xAxisTtile;
	private int pWidth, pHeight, gLabelTop, gLabelLeft, vehNamesLblsX, axisNumLblsY, gLabelHeight, gLabelWidth, 
		curNumVehShown, halfBarWidth, gapWidth, yBarsOffset, numPixY;

	//Data Objects
	private CostBarsDisplaySetup dSetup;
	private CostBarsAxesSetup axes;
	private CostBarsDisplaySetup.CostBarsPlotOutput plotData;
	
	public CostBarsJPanel(int panelWidth, int panelHeight, CostBarsDisplaySetup cbdSetup) {
		//Initialize Panel
		super();
		setLayout(null);
		pWidth = panelWidth;
		pHeight = panelHeight;
		setSize(pWidth, pHeight);
		setOpaque(true);
		
		//Link Data
		dSetup = cbdSetup;
		axes = null;
		plotData = null;
		
		//Create & size GUI Objects, position some of them
		legPanel = new LegendJPanel(pWidth - TClear*2);
		legPanel.setLocation(TClear, TClear);
		add(legPanel);
		
		gLabelTop = TClear*2 + legPanel.spHeight;
		vehNamesLblsX = TClear;
		gLabelLeft = vehNamesLblsX + VehNamesLblWidth + TClear;
		axisNumLblsY = pHeight - TClear - AxesTitleLabelHeight - TClear - AxesNumbersLabelHeight;
		
		gLabelHeight = axisNumLblsY - gLabelTop - TClear;
		gLabelWidth = pWidth - TClear - gLabelLeft;
		
		cbLabel = new CostBarsJLabel();
		cbLabel.setSize(gLabelWidth, gLabelHeight);
		cbLabel.setLocation(gLabelLeft, gLabelTop);
		add(cbLabel);
        
        lblAxisNum = new JLabel[HardLimitNumAxesTics];
        for (int i=0; i<lblAxisNum.length; i++) {
        	lblAxisNum[i] = new JLabel();
        	lblAxisNum[i].setSize(VAxesNumbersLabelWidth, AxesNumbersLabelHeight);
        	lblAxisNum[i].setVisible(false);
        	add(lblAxisNum[i]);
        }
        
        lblVehNames = new JLabel[dSetup.numVehicles()];
        for (int i=0; i<lblVehNames.length; i++) {
        	lblVehNames[i] = new JLabel();
        	lblVehNames[i].setSize(VehNamesLblWidth, VehNamesLblHeight);
        	lblVehNames[i].setHorizontalAlignment(SwingConstants.RIGHT);
        	lblVehNames[i].setVerticalAlignment(SwingConstants.CENTER);
        	lblVehNames[i].setVisible(false);
        	add(lblVehNames[i]);
        }
        
        xAxisTtile = new JLabel();
        xAxisTtile.setSize(gLabelWidth, AxesTitleLabelHeight);
        xAxisTtile.setLocation(gLabelLeft, axisNumLblsY + AxesNumbersLabelHeight + TClear);
        xAxisTtile.setHorizontalAlignment(SwingConstants.CENTER);
    	add(xAxisTtile);
	}
	
	public void setAxisData(CostBarsAxesSetup axesData) {
		axes = axesData;
		
		JLabel lbl = new JLabel();
		
        Font titleFont;
        if (axes.axesCaptionBold) titleFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.axesCaptionFontSize);
        else titleFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.axesCaptionFontSize);
        
        xAxisTtile.setFont(titleFont);
        xAxisTtile.setText(axes.costAxisTilteWOUnits + " ("+DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit)+")");
       
        Font axesNumbersFont;
        if (axes.numbersTextBold) axesNumbersFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.numbersFontSize);
        else axesNumbersFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.numbersFontSize);
        
        int numTics = axes.costAxis.numTics();
        int numDecimals = axes.costAxis.numDecimals();
        
        for (int i=0; i<numTics; i++) {
        	float axisValue = axes.costAxis.valueAtTic(i);
        	int pixX = axes.costAxis.valueToPixPos(gLabelWidth - TClear*2, axisValue);
        	int lblX = gLabelLeft + TClear + pixX - VAxesNumbersLabelWidth/2;
        	
        	if ((lblX + VAxesNumbersLabelWidth) > (gLabelLeft + gLabelWidth)) {
        		lblX = gLabelLeft + gLabelWidth - VAxesNumbersLabelWidth;
        		lblAxisNum[i].setHorizontalAlignment(SwingConstants.RIGHT);
        	} else {
        		lblAxisNum[i].setHorizontalAlignment(SwingConstants.CENTER);
        	}
        	
        	lblAxisNum[i].setLocation(lblX, axisNumLblsY);
        	lblAxisNum[i].setFont(axesNumbersFont);
        	lblAxisNum[i].setText(NumToString.floatWNumDecimals(axisValue, numDecimals));      
        	lblAxisNum[i].setVisible(true);
        }
        for (int i=numTics; i<lblAxisNum.length; i++) {
        	lblAxisNum[i].setVisible(false);
        }
	}
	
	public void setPlotData(CostBarsDisplaySetup.CostBarsPlotOutput plotOutput, CostBarsDisplaySetup cbdSetup) {
		//Set data
		dSetup = cbdSetup;
		plotData = plotOutput;
		
		//Update legends
		legPanel.updateLegends();
		
		//Vehicles Axis
        Font vehLabelsFont;
        JLabel lbl = new JLabel();
        if (axes.vehicleNamesBold) vehLabelsFont = new Font(lbl.getFont().getName(), Font.BOLD, axes.vehicleNameFontSize);
        else vehLabelsFont = new Font(lbl.getFont().getName(), Font.PLAIN, axes.vehicleNameFontSize);
		
		curNumVehShown = dSetup.numVehiclesShown();		
		float gAlpha = TargetBarGapWidthRatio;
		numPixY = gLabelHeight - TicSize - TClear;
		halfBarWidth = (int)(((0.5f*numPixY)/(curNumVehShown + gAlpha*(curNumVehShown+1))) + 0.5f);
		
		if (halfBarWidth > MaxHalfBarWidth) {
			halfBarWidth = MaxHalfBarWidth;
			
			gapWidth = (int)(((float)(numPixY - halfBarWidth*2*curNumVehShown)/(1f + curNumVehShown)) + 0.5);
			if (gapWidth > MaxBarGapWidth) gapWidth = MaxBarGapWidth;
			
			int blockSize = halfBarWidth*2*curNumVehShown + (curNumVehShown-1)*gapWidth;
			yBarsOffset = ((numPixY - blockSize)/2) + halfBarWidth;
			
		} else if (halfBarWidth < MinHalfBarWidth) {
			halfBarWidth = MinHalfBarWidth;
			
			gapWidth = (int)(((float)(numPixY - halfBarWidth*2*curNumVehShown)/(1f + curNumVehShown)) + 0.5);
			if (gapWidth < MinBarGapWidth) gapWidth = MinBarGapWidth;
			
			yBarsOffset = gapWidth + halfBarWidth;
		} else {
			gapWidth = (int)(gAlpha * (halfBarWidth*2) + 0.5f);
			yBarsOffset = gapWidth + halfBarWidth;
		}
				
		int numVehicles = dSetup.numVehicles();
		int cbarID = 0;
		
		for (int i=0; i<numVehicles; i++) {
			if (dSetup.isVehShown(i)) {
				lblVehNames[cbarID].setLocation(vehNamesLblsX, gLabelTop + yPixPosAtCenterOfCostBar(cbarID) - VehNamesLblHeight/2);
				lblVehNames[cbarID].setFont(vehLabelsFont);
				lblVehNames[cbarID].setText(dSetup.vehModelShortName(i));
				lblVehNames[cbarID].setVisible(true);
				
				cbarID++;
			}
		}
		for (int i=curNumVehShown; i<numVehicles; i++) {
			lblVehNames[i].setVisible(false);
		}
		
		//Draw Cost Bars
		cbLabel.repaint();
	}
	private int yPixPosAtCenterOfCostBar(int barID) {
		return TClear + numPixY - (yBarsOffset + barID*(gapWidth + halfBarWidth*2));
	}
	private int xPixPosAtCostValue(float costValue) {
		return TClear + axes.costAxis.valueToPixPos(gLabelWidth - TClear*2, costValue);
	}
	
	private class CostBarsJLabel extends JLabel {
		
		private CostBarsJLabel() {
			super();
		}
		
		@Override
	    protected void paintComponent(Graphics g) {
			//Call base function
			super.paintComponent(g);

			//Quick Strike
			if ((axes == null)||(plotData == null)) return;
			Graphics2D g2 = (Graphics2D) g;
			
			//Draw Grid
			if (dSetup.showGrid) {
				g2.setStroke(new BasicStroke(dSetup.gridLineWidth));
				g2.setColor(new Color(dSetup.gridColorRed, dSetup.gridColorGreen, dSetup.gridColorBlue));
				
				int numTics = axes.costAxis.numTics();
				for (int i=0; i<numTics; i++) {
					int x = xPixPosAtCostValue(axes.costAxis.valueAtTic(i));					
					g2.drawLine(x, numPixY + TClear, x, TClear);
				}
			}
			
			//Draw Axis
			g2.setStroke(new BasicStroke(dSetup.axesLineWidth));
			g2.setColor(Color.BLACK);
			
			g2.drawLine(TClear, TClear + numPixY, gLabelWidth - TClear, TClear + numPixY);

			int x = xPixPosAtCostValue(0f);
			g2.drawLine(x, TClear + numPixY, x, TClear);

			int numTics = axes.costAxis.numTics();
			for (int i=0; i<numTics; i++) {
				x = xPixPosAtCostValue(axes.costAxis.valueAtTic(i));					
				g2.drawLine(x, numPixY + TClear, x, numPixY + TClear + TicSize);
			}
			
			//Prepare arrays of colors for the quantities in cost bars
			CostBarsDisplaySetup.CostBarsPlotOutput negBarsData = plotData.getNegativeBars();
			CostBarsDisplaySetup.CostBarsPlotOutput posBarsData = plotData.getPositiveBars();

			int numNeg = negBarsData.numCBars();
			int numPos = posBarsData.numCBars();
			if ((numNeg+numPos) < 1) return;
			
			Color[] negColors = new Color[numNeg];
			Color[] posColors = new Color[numPos];
			
			for (int i=0; i<negColors.length; i++) {
				CostBarsDisplaySetup.CostBarType qtType = negBarsData.getCBarType(i);
				negColors[i] = dSetup.getChkData(qtType).getColor();
			}
			for (int i=0; i<posColors.length; i++) {
				CostBarsDisplaySetup.CostBarType qtType = posBarsData.getCBarType(i);
				posColors[i] = dSetup.getChkData(qtType).getColor();
			}
			
			//Draw Bars
			g2.setStroke(new BasicStroke(1));
			
			int numVehicles = dSetup.numVehicles();
			int cpos = 0;
			
			for (int i=0; i<numVehicles; i++) {
				if (dSetup.isVehShown(i)) {
					int cy = yPixPosAtCenterOfCostBar(cpos);
					
					if ((numNeg > 0)&&(numPos > 0)) {
						int y1 = cy;
						int y2 = cy + halfBarWidth;
						
						float curValue = 0f;
						int x1 = xPixPosAtCostValue(curValue);
						
						for (int j=0; j<numNeg; j++) {
							curValue += -negBarsData.getValueInOutputLargeMoneyUnits(i, j);
							int x2 = xPixPosAtCostValue(curValue);
							
							safeDrawFramedBox(g2, negColors[j], x1, y1, x2, y2);
							x1 = x2;
						}
						
						y1 = cy - halfBarWidth;
						y2 = cy;
						
						for (int j=0; j<numPos; j++) {
							curValue += posBarsData.getValueInOutputLargeMoneyUnits(i, j);
							int x2 = xPixPosAtCostValue(curValue);
							
							safeDrawFramedBox(g2, posColors[j], x1, y1, x2, y2);
							x1 = x2;
						}
						
						
					} else {
						int y1 = cy - halfBarWidth;
						int y2 = cy + halfBarWidth;
						
						float curValue = 0f;
						int x1 = xPixPosAtCostValue(curValue);
						for (int j=0; j<numNeg; j++) {
							curValue += -negBarsData.getValueInOutputLargeMoneyUnits(i, j);
							int x2 = xPixPosAtCostValue(curValue);
							
							safeDrawFramedBox(g2, negColors[j], x1, y1, x2, y2);
							x1 = x2;
						}
						for (int j=0; j<numPos; j++) {
							curValue += posBarsData.getValueInOutputLargeMoneyUnits(i, j);
							int x2 = xPixPosAtCostValue(curValue);
							
							safeDrawFramedBox(g2, posColors[j], x1, y1, x2, y2);
							x1 = x2;
						}
					}					
					
					cpos++;
				}
			}
		}
		
		private void safeDrawFramedBox(Graphics2D g2, Color color, int x1, int y1, int x2, int y2) {
			int xx1 = Math.min(x1, x2);
			int xx2 = Math.max(x1, x2);
			int yy1 = Math.min(y1, y2);
			int yy2 = Math.max(y1, y2);
			int w = xx2 - xx1;
			int h = yy2 - yy1;
			
			g2.setColor(color);
			g2.fillRect(xx1, yy1, w, h);
			g2.setColor(Color.BLACK);
			g2.drawRect(xx1, yy1, w, h);
		}
	}
	
	private class LegendJPanel extends JPanel {
		private int spWidth, spHeight, maxNumLegendsPerRow;
		private JLabel[] lbls;
		private ColorJLabel[] boxes;
		
		private LegendJPanel(int allowedWidth) {
			super();
			setLayout(null);
			
	        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
			
			spWidth = allowedWidth;
			maxNumLegendsPerRow = (spWidth - TClear*2 + WinMargin)/(LegLblWidth + LegBoxWidth + TMargin + WinMargin);
			
			int maxNumLegends = dSetup.maxNumBarsOnPlot();
			int maxNumRows = maxNumLegends / maxNumLegendsPerRow;
			if ((maxNumRows * maxNumLegendsPerRow) < maxNumLegends) maxNumRows++;
			
			spHeight = maxNumRows * LegLblHeight + (maxNumRows - 1)*LMargin + TClear*2;
			
			setSize(spWidth, spHeight);
			setOpaque(true);
			
			lbls = new JLabel[maxNumLegends];
			boxes = new ColorJLabel[maxNumLegends];
			
			for (int i=0; i<lbls.length; i++) {
				lbls[i] = new JLabel();
				lbls[i].setSize(LegLblWidth, LegLblHeight);
				lbls[i].setVerticalAlignment(SwingConstants.CENTER);
				lbls[i].setVisible(false);
				add(lbls[i]);
				
				boxes[i] = new ColorJLabel(Color.BLACK);
				boxes[i].setSize(LegBoxWidth, LegBoxHeight);
				boxes[i].setBorder(blackBorder);
				boxes[i].setVisible(false);
				add(boxes[i]);
			}
		}
		private void updateLegends() {
			if (plotData == null) return;
			
	        Font legendFont;
	        if (dSetup.legendFontBold) legendFont = new Font(lbls[0].getFont().getName(), Font.BOLD, dSetup.legendFontSize);
	        else legendFont = new Font(lbls[0].getFont().getName(), Font.PLAIN, dSetup.legendFontSize);

	        String[] legendsText = plotData.costBarTitles();
	        
	        for (int i=0; i<legendsText.length; i++) {
	        	lbls[i].setFont(legendFont);
	        	lbls[i].setText("<html>"+legendsText[i]+"</html>");
	        	lbls[i].setVisible(true);
	        	
	        	boxes[i].setColor(dSetup.getChkData(plotData.getCBarType(i)).getColor());
				boxes[i].setVisible(true);
	        }
	        
	        if ((legendsText.length > 0)&&(legendsText.length < (maxNumLegendsPerRow - 1))) {
	        	int ldiff = spWidth - TClear*2 - legendsText.length * (LegLblWidth + LegBoxWidth + TMargin) - (legendsText.length-1)*WinMargin;
	        	int cx = TClear + ldiff/2;
	        	int cy = TClear;
	        	
	        	for (int i=0; i<legendsText.length; i++) {
	        		lbls[i].setLocation(cx + i*(LegLblWidth + LegBoxWidth + TMargin + WinMargin) + LegBoxWidth + TMargin, cy);
	        		boxes[i].setLocation(cx + i*(LegLblWidth + LegBoxWidth + TMargin + WinMargin), cy + (LegLblHeight - LegBoxHeight)/2);
	        	}
	        } else {
	        	int ldiff = spWidth - TClear*2 - maxNumLegendsPerRow*(LegLblWidth + LegBoxWidth + TMargin) + (maxNumLegendsPerRow-1)*WinMargin;
	        	int cx = TClear + ldiff/2;
	        	int cy = TClear;
	        	int numRows = legendsText.length / maxNumLegendsPerRow;
	        	if ((numRows * maxNumLegendsPerRow) < legendsText.length) numRows++;
	        	
	        	for (int i=0; i<legendsText.length; i++) {
	        		int x = cx + (i / numRows)*(LegLblWidth + LegBoxWidth + TMargin + WinMargin);
	        		int y = cy + (i % numRows)*(LegLblHeight + LMargin);
	        		
	        		lbls[i].setLocation(x + LegBoxWidth + TMargin, y);
	        		boxes[i].setLocation(x, y + (LegLblHeight - LegBoxHeight)/2);
	        	}
	        }
	        
	        for (int i=legendsText.length; i<lbls.length; i++) {
	        	lbls[i].setVisible(false);
	        	boxes[i].setVisible(false);
	        }
		}
	}
}
