package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.utility.AxesAutoScaling;

public class GHGAxesSetup {
	private static final int DefaultNumBinsPerAxisDiv = 10;
	
	public String ghgAxisTitleWithoutUnits;
	public int axesCaptionsFontSize, numbersFontSize, legendFontSize, ghgAxisNumDecimals;
	public boolean axesCaptionsBold, numbersTextBold, legendTextBold;
	
	private float limGHGperDistInDisplayUnits, limGCO2perMile;
	private int numAxisDiv, numBinsPerAxisDiv, numBins;
	
	public float limGCO2perMile() {return limGCO2perMile;}
	public float limGHGperDistInDisplayUnits() {return limGHGperDistInDisplayUnits;}
	public int numBins() {return numBins;}
	public int numAxisDiv() {return numAxisDiv;}
	public int numBinsPerAxisDiv() {return numBinsPerAxisDiv;}
			
	private GHGAxesSetup() {}

	public GHGAxesSetup(GHGAxesSetup other) {
		ghgAxisTitleWithoutUnits = new String(other.ghgAxisTitleWithoutUnits);
		
		axesCaptionsFontSize = other.axesCaptionsFontSize;
		numbersFontSize = other.numbersFontSize;
		legendFontSize = other.legendFontSize;
		ghgAxisNumDecimals = other.ghgAxisNumDecimals;
		
		axesCaptionsBold = other.axesCaptionsBold;
		numbersTextBold = other.numbersTextBold;
		legendTextBold = other.legendTextBold;
		
		setLimGHGperDistInDisplayUnits(other.limGHGperDistInDisplayUnits);
		setNumDiv(other.numAxisDiv, other.numBinsPerAxisDiv);		
	}
	public void setLimGHGperDistInDisplayUnits(float ghgPerDistanceValue) {
		limGHGperDistInDisplayUnits = ghgPerDistanceValue;
		limGCO2perMile = limGHGperDistInDisplayUnits * DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit) / DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
	}
	public void setNumDiv(int numMainDiv, int numSubDiv) {
		numAxisDiv = numMainDiv;
		numBinsPerAxisDiv = numSubDiv;
		numBins = numAxisDiv*numBinsPerAxisDiv;
	}
	
	public static GHGAxesSetup createViaReadingFile(String fname) {
		try {
			GHGAxesSetup axesSetup = new GHGAxesSetup();
			
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			axesSetup.ghgAxisTitleWithoutUnits = fin.readLine();
			
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			axesSetup.axesCaptionsFontSize = Integer.parseInt(sp[0]);
			axesSetup.axesCaptionsBold = Boolean.parseBoolean(sp[1]);
			axesSetup.numbersFontSize = Integer.parseInt(sp[2]);
			axesSetup.numbersTextBold = Boolean.parseBoolean(sp[3]);
			axesSetup.legendFontSize = Integer.parseInt(sp[4]);
			axesSetup.legendTextBold = Boolean.parseBoolean(sp[5]);
			axesSetup.ghgAxisNumDecimals = Integer.parseInt(sp[6]);
			
			readLine = fin.readLine();
			sp = readLine.split(",");
			
			axesSetup.limGCO2perMile = Float.parseFloat(sp[0]);
			axesSetup.numAxisDiv = Integer.parseInt(sp[1]);
			axesSetup.numBinsPerAxisDiv = Integer.parseInt(sp[2]);
			
			float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);	
			axesSetup.limGHGperDistInDisplayUnits = axesSetup.limGCO2perMile * gCO2perMileToDisplayUnits;
			axesSetup.numBins = axesSetup.numAxisDiv * axesSetup.numBinsPerAxisDiv;
			
			fin.close();			
			return axesSetup;
		} catch (Exception e) {
			return null;
		}
	}
	public void save(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");

			fout.append(ghgAxisTitleWithoutUnits+lsep);
			fout.append(""+axesCaptionsFontSize+","+axesCaptionsBold+","+numbersFontSize+","+numbersTextBold+","
					+legendFontSize+","+legendTextBold+","+ghgAxisNumDecimals+lsep);
			fout.append(""+limGCO2perMile+","+numAxisDiv+","+numBinsPerAxisDiv+lsep);
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	public static GHGAxesSetup createViaAutoScale(float maxGCO2perMile, int maxNumAxesDiv) {
		//Create
		GHGAxesSetup axesSetup = new GHGAxesSetup();
		
		//Defaults
		axesSetup.ghgAxisTitleWithoutUnits = "GHG Intensity";
		axesSetup.axesCaptionsFontSize = 12;
		axesSetup.axesCaptionsBold = true;
		axesSetup.numbersFontSize = 11;
		axesSetup.numbersTextBold = false;
		axesSetup.legendFontSize = 11;
		axesSetup.legendTextBold = true;
		
		//Scale to max GHG
		int tMaxNumDiv = maxNumAxesDiv - 1;
		tMaxNumDiv = Math.max(tMaxNumDiv, 1);
		
		float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);	
		
		float maxGHGperDistInDiplayUnits = gCO2perMileToDisplayUnits*maxGCO2perMile;
		AxesAutoScaling.NumericAxisSetup tAxis = AxesAutoScaling.autoScaleNumericAxis(0, maxGHGperDistInDiplayUnits, tMaxNumDiv);
		axesSetup.ghgAxisNumDecimals = tAxis.numDecimals();
		
		float tStep = tAxis.stepSize();
		axesSetup.numAxisDiv = tAxis.numTics() - 1;
		
		while ((axesSetup.numAxisDiv*tStep) < maxGHGperDistInDiplayUnits) {
			axesSetup.numAxisDiv++;
		}
		
		axesSetup.limGHGperDistInDisplayUnits = axesSetup.numAxisDiv*tStep;
		axesSetup.limGCO2perMile = axesSetup.limGHGperDistInDisplayUnits / gCO2perMileToDisplayUnits;
		axesSetup.numBinsPerAxisDiv = DefaultNumBinsPerAxisDiv;
		axesSetup.numBins = axesSetup.numAxisDiv * axesSetup.numBinsPerAxisDiv;
		
		return axesSetup;
	}
}
