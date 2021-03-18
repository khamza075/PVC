package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.calc.TCOCalculator;
import pvc.utility.AxesAutoScaling;

public class CostVsGHGAxesSetup {
	private static final float MaxMultiplier = 1.05f;
	
	public AxesAutoScaling.NumericAxisSetup ghgAxis, costAxis;
	public int axesCaptionsFontSize, numbersFontSize;
	public boolean axesCaptionsBold, numbersTextBold;
	public String ghgAxisTilteWOUnits, costAxisTilteWOUnits;
	
	private CostVsGHGAxesSetup() {}
	
	public static CostVsGHGAxesSetup createViaReadingFile(String fname) {
		try {
			CostVsGHGAxesSetup axes = new CostVsGHGAxesSetup();
			
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			
			String readLine = fin.readLine();			
			axes.ghgAxis = new AxesAutoScaling.NumericAxisSetup(readLine);
			
			readLine = fin.readLine();			
			axes.costAxis = new AxesAutoScaling.NumericAxisSetup(readLine);
			
			readLine = fin.readLine();
			axes.ghgAxisTilteWOUnits = new String(readLine);	
				
			readLine = fin.readLine();	
			String[] sp = readLine.split(",");	
			axes.axesCaptionsBold = Boolean.parseBoolean(sp[0]);
			axes.axesCaptionsFontSize = Integer.parseInt(sp[1]);
				
			readLine = fin.readLine();
			axes.costAxisTilteWOUnits = new String(readLine);

			readLine = fin.readLine();	
			sp = readLine.split(",");	
			axes.numbersTextBold = Boolean.parseBoolean(sp[0]);
			axes.numbersFontSize = Integer.parseInt(sp[1]);
			
			fin.close();
			return axes;
		} catch (Exception e) {
			return null;
		}
	}
	public static CostVsGHGAxesSetup createViaAutoScale(TCOCalculator.CParetoPoint[] costVsGHG, int maxNumDivGHG, int maxNumDivCost) {
		float minGHG = 0f;
		float minCost = 0f;
		float maxGHG = 0f;
		float maxCost = 0f;
		
		//Identify max GHG and Cost in units of the output display
		float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
		float dollarPerMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
		
		for (int i=0; i<costVsGHG.length; i++) {
			float curGHG = gCO2perMileToDisplayUnits * costVsGHG[i].gCO2perMile;
			float curCost = dollarPerMileToDisplayUnits * costVsGHG[i].dollarPerMile;
			
			if (maxGHG < curGHG) maxGHG = curGHG;
			if (maxCost < curCost) maxCost = curCost;
		}		
		
		CostVsGHGAxesSetup axes = new CostVsGHGAxesSetup();
		axes.ghgAxis = AxesAutoScaling.autoScaleNumericAxis(minGHG, maxGHG*MaxMultiplier, maxNumDivGHG);
		axes.costAxis = AxesAutoScaling.autoScaleNumericAxis(minCost, maxCost*MaxMultiplier, maxNumDivCost);
		
		axes.ghgAxisTilteWOUnits = "GHG Emissions";
		axes.axesCaptionsBold = true;
		axes.axesCaptionsFontSize = 12;

		axes.costAxisTilteWOUnits = "Cost";
		axes.numbersTextBold = false;
		axes.numbersFontSize = 11;

		return axes;
	}
	public void reScale(TCOCalculator.CParetoPoint[] costVsGHG, int maxNumDivGHG, int maxNumDivCost) {
		float minGHG = 0f;
		float minCost = 0f;
		float maxGHG = 0f;
		float maxCost = 0f;
		
		//Identify max GHG and Cost in units of the output display
		float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
		float dollarPerMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
		
		for (int i=0; i<costVsGHG.length; i++) {
			float curGHG = gCO2perMileToDisplayUnits * costVsGHG[i].gCO2perMile;
			float curCost = dollarPerMileToDisplayUnits * costVsGHG[i].dollarPerMile;
			
			if (maxGHG < curGHG) maxGHG = curGHG;
			if (maxCost < curCost) maxCost = curCost;
		}		
		
		ghgAxis = AxesAutoScaling.autoScaleNumericAxis(minGHG, maxGHG*MaxMultiplier, maxNumDivGHG);
		costAxis = AxesAutoScaling.autoScaleNumericAxis(minCost, maxCost*MaxMultiplier, maxNumDivCost);
	}

	public void saveToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append(ghgAxis.toString()+lsep);
			fout.append(costAxis.toString()+lsep);
			
			fout.append(ghgAxisTilteWOUnits+lsep);
			fout.append(""+axesCaptionsBold+","+axesCaptionsFontSize+lsep);
			
			fout.append(costAxisTilteWOUnits+lsep);
			fout.append(""+numbersTextBold+","+numbersFontSize+lsep);
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
}
