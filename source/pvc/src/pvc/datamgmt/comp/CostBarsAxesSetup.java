package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.utility.AxesAutoScaling;

public class CostBarsAxesSetup {
	private static final float MaxMultiplier = 1.05f;
	
	public AxesAutoScaling.NumericAxisSetup costAxis;
	public int axesCaptionFontSize, numbersFontSize, vehicleNameFontSize, numberNumDecimals;
	public boolean axesCaptionBold, numbersTextBold, vehicleNamesBold;
	public String costAxisTilteWOUnits;
	
	private CostBarsAxesSetup() {}
	public CostBarsAxesSetup(CostBarsAxesSetup other) {
		costAxis = new AxesAutoScaling.NumericAxisSetup(other.costAxis);
		
		axesCaptionFontSize = other.axesCaptionFontSize;
		numbersFontSize = other.numbersFontSize;
		vehicleNameFontSize = other.vehicleNameFontSize;
		numberNumDecimals = other.numberNumDecimals;

		axesCaptionBold = other.axesCaptionBold;
		numbersTextBold = other.numbersTextBold;
		vehicleNamesBold = other.vehicleNamesBold;
		
		costAxisTilteWOUnits = new String(other.costAxisTilteWOUnits);
	}
	
	public static CostBarsAxesSetup createViaReadingFile(String fname) {
		try {
			CostBarsAxesSetup axes = new CostBarsAxesSetup();
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			
			String readLine = fin.readLine();
			axes.costAxis = new AxesAutoScaling.NumericAxisSetup(readLine);

			readLine = fin.readLine();
			String [] sp = readLine.split(",");
			
			axes.axesCaptionFontSize = Integer.parseInt(sp[0]);
			axes.axesCaptionBold = Boolean.parseBoolean(sp[1]);
			
			axes.numbersFontSize = Integer.parseInt(sp[2]);
			axes.numbersTextBold = Boolean.parseBoolean(sp[3]);
			
			axes.vehicleNameFontSize = Integer.parseInt(sp[4]);
			axes.vehicleNamesBold = Boolean.parseBoolean(sp[5]);
			axes.numberNumDecimals = Integer.parseInt(sp[6]);

			readLine = fin.readLine();
			axes.costAxisTilteWOUnits = new String(readLine);
			
			fin.close();
			return axes;
		} catch (Exception e) {
			return null;
		}
	}
	public static CostBarsAxesSetup createViaAutoScale(CostBarsDisplaySetup.CostBarsPlotOutput cbOut, int maxNumDivCost) {
		CostBarsAxesSetup axes = new CostBarsAxesSetup();
		
		axes.axesCaptionFontSize = 12;
		axes.axesCaptionBold = true;
		
		axes.numbersFontSize = 11;
		axes.numbersTextBold = false;
		
		axes.vehicleNameFontSize = 11;
		axes.vehicleNamesBold = true;

		axes.costAxisTilteWOUnits = "Ownership Period Cost";

		axes.reScale(cbOut, maxNumDivCost);		
		return axes;
	}
	public void reScale(CostBarsDisplaySetup.CostBarsPlotOutput cbOut, int maxNumDivCost) {
		float minValue = 0f;
		float maxValue = 0f;
		
		CostBarsDisplaySetup.CostBarsPlotOutput negBars = cbOut.getNegativeBars();
		CostBarsDisplaySetup.CostBarsPlotOutput posBars = cbOut.getPositiveBars();
		
		int numNeg = negBars.numCBars();
		int numPos = posBars.numCBars();
		
		if ((numNeg + numPos) < 1) {
			maxValue = 1f;
		}
		
		int numVehModels = cbOut.numVehModels();
		for (int i=0; i<numVehModels; i++) {
			float curPosValue = 0f;
			for (int j=0; j<numPos; j++) curPosValue += posBars.getValueInOutputLargeMoneyUnits(i, j);
			for (int j=0; j<numNeg; j++) curPosValue += -negBars.getValueInOutputLargeMoneyUnits(i, j);
			if (maxValue < curPosValue) maxValue = curPosValue;
			
			float curNegValue = 0f;
			for (int j=0; j<numNeg; j++) curNegValue += negBars.getValueInOutputLargeMoneyUnits(i, j);
			if (minValue > -curNegValue) minValue = -curNegValue;
		}
				
		costAxis = AxesAutoScaling.autoScaleNumericAxis(minValue*MaxMultiplier, maxValue*MaxMultiplier, maxNumDivCost);
		numberNumDecimals = costAxis.numDecimals();
	}
	public void saveToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append(costAxis.toString()+lsep);
			
			fout.append(""+axesCaptionFontSize+","+axesCaptionBold+","+numbersFontSize+","+numbersTextBold
					+","+vehicleNameFontSize+","+vehicleNamesBold+ ","+numberNumDecimals+lsep);
			
			fout.append(costAxisTilteWOUnits+lsep);
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}

}
