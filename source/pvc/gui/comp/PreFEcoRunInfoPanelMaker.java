package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.datamgmt.WIITModel;
import pvc.datamgmt.comp.DUnits;
import pvc.datamgmt.comp.PowertrainType;

public class PreFEcoRunInfoPanelMaker {
	private PreFEcoRunInfoPanelMaker () {}//Prevent Instantiation

	//Creating an info panel for summary settings of fuel economy simulations
	public static JPanel_FEcoSimInfoPanel createFEcoSimInfoPanel(WIITModel wiitModel,
			int presetWidth, int rMargin, int tMargin, int lblHeight, int edtHeight, int lineSpacing, int largerFontSize, int smallerFontSize) {
		
		JPanel_FEcoSimInfoPanel panel = new JPanel_FEcoSimInfoPanel();
		panel.setLayout(null);
		
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
		
		int headingLblWidth = 130;
		int boxLblWidth = 36;
		int twoPts = 2;
		int mainTextLineMinWidth = 250;
		int prefWidth = Math.max(mainTextLineMinWidth + rMargin*2, presetWidth);
		
		int mainLineLabelWidth = prefWidth - 2*rMargin;
		
		
		int cx = rMargin;
		int cy = 0;
		
		JLabel lblH1 = new JLabel("Powertrain Types Included");
        Font largeBoldFont = new Font(lblH1.getFont().getName(), Font.BOLD, largerFontSize);
        Font smallFont = new Font(lblH1.getFont().getName(), Font.PLAIN, smallerFontSize);
        Font smallBoldFont = new Font(lblH1.getFont().getName(), Font.BOLD, smallerFontSize);

		lblH1.setSize(mainLineLabelWidth, lblHeight);
		lblH1.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
		lblH1.setFont(largeBoldFont);
		panel.add(lblH1);
		
		cy += edtHeight;
		
		PowertrainType[] allPTTypes = PowertrainType.values();
		for (int i=0; i<allPTTypes.length; i++) {
			if (wiitModel.isPowertrainTypeIcluded(allPTTypes[i])) {
				JLabel lbl = new JLabel(allPTTypes[i].description);
				lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lbl.setFont(smallFont);
				panel.add(lbl);

				cy += edtHeight;
			}
		}
		
		cy += rMargin;
		
		if (wiitModel.hasBEVs() && (wiitModel.bevRepModel != null)) {
			if (wiitModel.bevRepModel.bevRepCommercial != null) {
				JLabel lblH2 = new JLabel("BEV Unfulfilled Trips: Commercial Vehicles");
				lblH2.setSize(mainLineLabelWidth, lblHeight);
				lblH2.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
				lblH2.setFont(largeBoldFont);
				panel.add(lblH2);

				cy += edtHeight;
				
				JLabel lbl = new JLabel("(Same type of BEV, more vehicles needed in the fleet)");
				lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lbl.setFont(smallFont);
				panel.add(lbl);

				cy += edtHeight;
			} else {
				JLabel lblH2 = new JLabel("BEV Unfulfilled Trips: Whole-Day Replacement");
				lblH2.setSize(mainLineLabelWidth, lblHeight);
				lblH2.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
				lblH2.setFont(largeBoldFont);
				panel.add(lblH2);

				cy += edtHeight;
				
				JLabel lbl = new JLabel("(Another non-range-limited vehicles does trips of the day)");
				lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lbl.setFont(smallFont);
				panel.add(lbl);

				cy += edtHeight;
				
				if (wiitModel.bevRepModel.bevRepWholeDay.repVehicleShortName.length > 1) {
					JLabel lbl1 = new JLabel("Choice #1: "+wiitModel.bevRepModel.bevRepWholeDay.repVehicleShortName[0]);
					lbl1.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl1.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl1.setFont(smallBoldFont);
					panel.add(lbl1);
	
					cy += edtHeight;
					
					JLabel lbl2 = new JLabel("Choice #2: "+wiitModel.bevRepModel.bevRepWholeDay.repVehicleShortName[1]);
					lbl2.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl2.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl2.setFont(smallBoldFont);
					panel.add(lbl2);
	
					cy += edtHeight;
				} else {
					JLabel lbl2 = new JLabel("Replacement Vehicle: "+wiitModel.bevRepModel.bevRepWholeDay.repVehicleShortName[0]);
					lbl2.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl2.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl2.setFont(smallBoldFont);
					panel.add(lbl2);
	
					cy += edtHeight;
				}
				
				JLabel lblsh1 = new JLabel("Range Anxiety Model");
				lblsh1.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lblsh1.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lblsh1.setFont(smallBoldFont);
				panel.add(lblsh1);

				cy += edtHeight;
								
				float[] rangeAnxAt100miles = wiitModel.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles;
				float[] xTripDistanceCurve = wiitModel.bevRepModel.bevRepWholeDay.rangeAnxProfile().x;
				
				int curMaxX = cx + rMargin + tMargin + headingLblWidth + (twoPts + boxLblWidth)*xTripDistanceCurve.length + tMargin;
				if (prefWidth < curMaxX) prefWidth = curMaxX;
				
				panel.bevrepTripDistHeader = new JLabel();
				panel.bevrepTripDistHeader.setSize(headingLblWidth, lblHeight);
				panel.bevrepTripDistHeader.setLocation(cx + rMargin + tMargin, cy + (edtHeight - lblHeight)/2);
				panel.bevrepTripDistHeader.setFont(smallFont);
				panel.add(panel.bevrepTripDistHeader);
				
				panel.bevrepTripDistances = new JLabel[xTripDistanceCurve.length];
				panel.bevrepAnxLevelHeader = new JLabel[rangeAnxAt100miles.length];
				panel.bevrepAnxValues = new JLabel[rangeAnxAt100miles.length][xTripDistanceCurve.length];
				
				for (int i=0; i<xTripDistanceCurve.length; i++) {
					panel.bevrepTripDistances[i] = new JLabel();
					panel.bevrepTripDistances[i].setSize(boxLblWidth, lblHeight);
					panel.bevrepTripDistances[i].setLocation(cx + rMargin + tMargin + headingLblWidth + twoPts + (boxLblWidth+twoPts)*i, cy);
					panel.bevrepTripDistances[i].setBorder(blackBorder);
					panel.bevrepTripDistances[i].setFont(smallBoldFont);
					panel.bevrepTripDistances[i].setHorizontalAlignment(SwingConstants.RIGHT);
					panel.add(panel.bevrepTripDistances[i]);
				}
				
				cy += edtHeight + twoPts;
				
				for (int j=0; j<rangeAnxAt100miles.length; j++) {
					panel.bevrepAnxLevelHeader[j]  =new JLabel();
					panel.bevrepAnxLevelHeader[j].setSize(headingLblWidth, lblHeight);
					panel.bevrepAnxLevelHeader[j].setLocation(cx + rMargin + tMargin, cy + (edtHeight - lblHeight)/2);
					panel.bevrepAnxLevelHeader[j].setFont(smallFont);
					panel.add(panel.bevrepAnxLevelHeader[j]);
					
					for (int i=0; i<xTripDistanceCurve.length; i++) {
						panel.bevrepAnxValues[j][i] = new JLabel();
						panel.bevrepAnxValues[j][i].setSize(boxLblWidth, lblHeight);
						panel.bevrepAnxValues[j][i].setLocation(cx + rMargin + tMargin + headingLblWidth + twoPts + (boxLblWidth+twoPts)*i, cy);
						panel.bevrepAnxValues[j][i].setBorder(blackBorder);
						panel.bevrepAnxValues[j][i].setFont(smallFont);
						panel.bevrepAnxValues[j][i].setHorizontalAlignment(SwingConstants.RIGHT);
						panel.add(panel.bevrepAnxValues[j][i]);
					}
					
					cy += edtHeight + twoPts;
				}
			}
			
			cy += rMargin;
		}
		
		if (wiitModel.hasPlugIns() && (wiitModel.chgModels != null)) {
			JLabel lblH2 = new JLabel("Day-time Charging Model");
			lblH2.setSize(mainLineLabelWidth, lblHeight);
			lblH2.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
			lblH2.setFont(largeBoldFont);
			panel.add(lblH2);
			
			cy += edtHeight + twoPts;
			
			float[] minChgWindowHr = wiitModel.chgModels.daytimeChargingMinWindow;

			int curMaxX = cx + rMargin + tMargin + headingLblWidth + (twoPts + boxLblWidth)*minChgWindowHr.length + tMargin;
			if (prefWidth < curMaxX) prefWidth = curMaxX;
			
			JLabel lblsh = new JLabel("Min. Time Window (hr:min)");
			lblsh.setSize(headingLblWidth, lblHeight);
			lblsh.setLocation(cx + rMargin + tMargin, cy + (edtHeight - lblHeight)/2);
			lblsh.setFont(smallFont);
			panel.add(lblsh);
			
			for (int i=0; i<minChgWindowHr.length; i++) {
				JLabel lbl = new JLabel(formatHrMin(minChgWindowHr[i]));
				lbl.setSize(boxLblWidth, lblHeight);
				lbl.setLocation(cx + rMargin + tMargin + headingLblWidth + twoPts + (boxLblWidth+twoPts)*i, cy);
				lbl.setBorder(blackBorder);
				lbl.setFont(smallFont);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				panel.add(lbl);
			}
			
			cy += edtHeight + twoPts;
			cy += rMargin;

			JLabel lblH3 = new JLabel("Charging Model - Miscellaneous");
			lblH3.setSize(mainLineLabelWidth, lblHeight);
			lblH3.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
			lblH3.setFont(largeBoldFont);
			panel.add(lblH3);
			
			cy += edtHeight + twoPts;
			
			if (wiitModel.hasBEVs()) {
				if (wiitModel.chgModels.bevHomesHaveL2) {
					JLabel lbl = new JLabel("BEV Homes HAVE Level-2 Charger");
					lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl.setFont(smallFont);
					panel.add(lbl);
				} else {
					JLabel lbl = new JLabel("BEV Homes DO NOT Have Level-2 Charger");
					lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl.setFont(smallFont);
					panel.add(lbl);
				}
				cy += edtHeight;
			}
			if (wiitModel.hasPHEVs()) {
				if (wiitModel.chgModels.phevHomesHaveL2) {
					JLabel lbl = new JLabel("PHEV Homes HAVE Level-2 Charger");
					lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl.setFont(smallFont);
					panel.add(lbl);
				} else {
					JLabel lbl = new JLabel("PHEV Homes DO NOT Have Level-2 Charger");
					lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl.setFont(smallFont);
					panel.add(lbl);
				}
				cy += edtHeight;
				
				if (wiitModel.chgModels.fractionNonChargingPHEVs != null) {
					JLabel lbl = new JLabel("Consider Fraction of PHEVs that don't Charge");
					lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl.setFont(smallFont);
					panel.add(lbl);
					
					cy += edtHeight;
				}
			}
			
			if (wiitModel.chgModels.dcFastAvailable) {
				JLabel lbl = new JLabel("DC-Fast Charging is Available for Day-time Charging");
				lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lbl.setFont(smallFont);
				panel.add(lbl);
				
				cy += edtHeight;
				
				if (wiitModel.chgModels.dtChgPrioritizesDCFastIfFeasible) {
					JLabel lbl2 = new JLabel("Daytime Charging Prioritizes DC-Fast (if Vehicle is Capable)");
					lbl2.setSize(mainLineLabelWidth-rMargin, lblHeight);
					lbl2.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					lbl2.setFont(smallFont);
					panel.add(lbl2);
					
					cy += edtHeight;
				}
				if (wiitModel.chgModels.minNomAERForPHEVsToHaveDCFast > 0) {
					panel.lblPHEVsMinAERforDCFast = new JLabel();
					panel.lblPHEVsMinAERforDCFast.setSize(mainLineLabelWidth-rMargin, lblHeight);
					panel.lblPHEVsMinAERforDCFast.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
					panel.lblPHEVsMinAERforDCFast.setFont(smallFont);
					panel.add(panel.lblPHEVsMinAERforDCFast);
					
					cy += edtHeight;
				}
			} else {
				JLabel lbl = new JLabel("No DC-Fast Charging Available");
				lbl.setSize(mainLineLabelWidth-rMargin, lblHeight);
				lbl.setLocation(cx + rMargin, cy + (edtHeight - lblHeight)/2);
				lbl.setFont(smallFont);
				panel.add(lbl);
				
				cy += edtHeight;
			}			
			
			cy += rMargin;
		}		
		
		int prefHeight = cy;
		
		panel.setPreferredSize(new Dimension(prefWidth, prefHeight));
		panel.setAutoscrolls(true);
		
		updateFEcoSimInfoPanel(panel, wiitModel);
		
		return panel;
	}
	@SuppressWarnings("serial")
	public static class JPanel_FEcoSimInfoPanel extends JPanel {
		private JPanel_FEcoSimInfoPanel() {super();}
		
		private JLabel bevrepTripDistHeader;
		private JLabel[] bevrepTripDistances;
		private JLabel[] bevrepAnxLevelHeader;
		private JLabel[][] bevrepAnxValues;
		private JLabel lblPHEVsMinAERforDCFast;
	}
	//Updating the text displayed within panel
	public static void updateFEcoSimInfoPanel(JPanel_FEcoSimInfoPanel panel, WIITModel wiitModel) {
		
		if (wiitModel.hasBEVs() && (wiitModel.bevRepModel != null)) {
			if (wiitModel.bevRepModel.bevRepCommercial == null) {
				float[] rangeAnxAt100miles = wiitModel.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles;
				float[] xTripDistanceCurve = wiitModel.bevRepModel.bevRepWholeDay.rangeAnxProfile().x;
				
				panel.bevrepTripDistHeader.setText("Trip Distance ("+DUnits.getShortName(DUnits.UnitType.Distance)+")");
				for (int i=0; i<rangeAnxAt100miles.length; i++) panel.bevrepAnxLevelHeader[i].setText("Case "+(i+1)+" Range Anx. ("+DUnits.getShortName(DUnits.UnitType.Distance)+")");
				
				for (int j=0; j<xTripDistanceCurve.length; j++) {
					float tripDistMiles = xTripDistanceCurve[j];
					panel.bevrepTripDistances[j].setText(""+roundFloat(tripDistMiles/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)));
					for (int i=0; i<rangeAnxAt100miles.length; i++) {
						float nomAnxAt100Mi = rangeAnxAt100miles[i];
						float actualAnxMi = wiitModel.bevRepModel.bevRepWholeDay.calcRangeAnx(tripDistMiles, nomAnxAt100Mi);
						
						panel.bevrepAnxValues[i][j].setText(""+roundFloat(actualAnxMi/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)));
					}
				}
			}
			
			//PHEVs w/ DC Fast Capability
			if (wiitModel.chgModels.minNomAERForPHEVsToHaveDCFast > 0) {
				panel.lblPHEVsMinAERforDCFast.setText("PHEVs w/ Range > "+roundFloat(wiitModel.chgModels.minNomAERForPHEVsToHaveDCFast / DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance))+
						" "+DUnits.getShortName(DUnits.UnitType.Distance)+" are DC-Fast Capable");
			}
		}		
	}
	
	
	public static int roundFloat(float v) {return (int)(v+0.5f);}
	public static String formatHrMin(float hrDecimals) {
		int totMinutes = (int)(60f*hrDecimals + 0.5f);
		int hr = totMinutes / 60;
		int min = totMinutes % 60;
		String sMin = ""+min;
		if (min <10) sMin = "0"+sMin;
		return ""+hr+":"+sMin;
	}
}
