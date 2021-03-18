package pvc.utility;

import java.io.FileWriter;
import java.util.ArrayList;

import pvc.datamgmt.comp.DUnits;

public class CGHGHistogram {
	private float[] pdm, cdf;
	private float x05, x25, x50, x75, x95, maxGCO2perMile, deltaBin;	
	

	public CGHGHistogram(float limGCO2perMile, int numBins) {
		reset(limGCO2perMile, numBins);
	}
	public void reset(float limGCO2perMile, int numBins) {
		x05 = 0f;
		x25 = 0f;
		x50 = 0f;
		x75 = 0f;
		x95 = 0f;
		
		maxGCO2perMile = limGCO2perMile;
		deltaBin = maxGCO2perMile/(float)numBins;
		pdm = new float[numBins];
		cdf = null;
	}
	
	//Do not use if finished adding trips unless reset() first
	public void addTrip(float gCO2perMile, float wt) {
		int binID = (int)(gCO2perMile/deltaBin);
		binID = Math.max(binID, 0);
		binID = Math.min(binID, pdm.length-1);
		pdm[binID] += wt;
	}
	//Must invoke in order to extract statistics, do add trips after that
	public void finishAddingTrips() {
		float sumWt = 0f;
		for (int i=0; i<pdm.length; i++) sumWt += pdm[i];
		
		cdf = new float[pdm.length+1];
		
		if (sumWt <= 0) {
			cdf[pdm.length] = 1f;
			x05 = (pdm.length-1)*deltaBin + 0.05f*deltaBin;
			x25 = (pdm.length-1)*deltaBin + 0.25f*deltaBin;
			x50 = (pdm.length-1)*deltaBin + 0.5f*deltaBin;
			x75 = (pdm.length-1)*deltaBin + 0.75f*deltaBin;
			x95 = (pdm.length-1)*deltaBin + 0.95f*deltaBin;
			return;
		}
		
		for (int i=0; i<pdm.length; i++) {
			float cPDM = pdm[i]/sumWt;
			pdm[i] = cPDM;
			cdf[i+1] = cdf[i] + cPDM;
		}
		
		int cPos = 0;
		float cdfSeekValue = 0.05f;		
		for (int i=cPos; i<pdm.length; i++) {
			if (cdfSeekValue <= cdf[i+1]) {
				float x1 = i*deltaBin;
				float x2 = (i+1)*deltaBin;
				float curPDM = pdm[i];
				if (curPDM <= 0) {
					x05 = 0.5f*(x1+x2);
					break;
				}
				float c2 = (cdfSeekValue - cdf[i])/curPDM;
				float c1 = 1f - c2;
				x05 = x1*c1 + x2*c2;
				break;
			}
			cPos++;
		}	
		
		cdfSeekValue = 0.25f;		
		for (int i=cPos; i<pdm.length; i++) {
			if (cdfSeekValue <= cdf[i+1]) {
				float x1 = i*deltaBin;
				float x2 = (i+1)*deltaBin;
				float curPDM = pdm[i];
				if (curPDM <= 0) {
					x25 = 0.5f*(x1+x2);
					break;
				}
				float c2 = (cdfSeekValue - cdf[i])/curPDM;
				float c1 = 1f - c2;
				x25 = x1*c1 + x2*c2;
				break;
			}
			cPos++;
		}
		
		cdfSeekValue = 0.5f;		
		for (int i=cPos; i<pdm.length; i++) {
			if (cdfSeekValue <= cdf[i+1]) {
				float x1 = i*deltaBin;
				float x2 = (i+1)*deltaBin;
				float curPDM = pdm[i];
				if (curPDM <= 0) {
					x50 = 0.5f*(x1+x2);
					break;
				}
				float c2 = (cdfSeekValue - cdf[i])/curPDM;
				float c1 = 1f - c2;
				x50 = x1*c1 + x2*c2;
				break;
			}
			cPos++;
		}
		
		cdfSeekValue = 0.75f;		
		for (int i=cPos; i<pdm.length; i++) {
			if (cdfSeekValue <= cdf[i+1]) {
				float x1 = i*deltaBin;
				float x2 = (i+1)*deltaBin;
				float curPDM = pdm[i];
				if (curPDM <= 0) {
					x75 = 0.5f*(x1+x2);
					break;
				}
				float c2 = (cdfSeekValue - cdf[i])/curPDM;
				float c1 = 1f - c2;
				x75 = x1*c1 + x2*c2;
				break;
			}
			cPos++;
		}
		
		cdfSeekValue = 0.95f;		
		for (int i=cPos; i<pdm.length; i++) {
			if (cdfSeekValue <= cdf[i+1]) {
				float x1 = i*deltaBin;
				float x2 = (i+1)*deltaBin;
				float curPDM = pdm[i];
				if (curPDM <= 0) {
					x95 = 0.5f*(x1+x2);
					break;
				}
				float c2 = (cdfSeekValue - cdf[i])/curPDM;
				float c1 = 1f - c2;
				x95 = x1*c1 + x2*c2;
				break;
			}
			cPos++;
		}
		
		if (cPos == pdm.length) x95 = (pdm.length-1)*deltaBin + 0.95f*deltaBin;
	}

	public static class BoxPlot {
		public float x05, x25, x50, x75, x95, average;
		private BoxPlot() {}
		
		public BoxPlot(BoxPlot other, float offsetValue) {
			x05 = other.x05 + offsetValue;
			x25 = other.x25 + offsetValue;
			x50 = other.x50 + offsetValue;
			x75 = other.x75 + offsetValue;
			x95 = other.x95 + offsetValue;
			average = other.average + offsetValue;
		}
		
		public void writeInCSVFile(FileWriter fout) throws Exception {
			String valueCaption = "Value ("+DUnits.getShortName(DUnits.UnitType.GHGUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance)+")";
			String lsep = System.getProperty("line.separator");
			
			fout.append("Quantity,"+valueCaption+lsep);
			fout.append("5th_percentile,"+x05+lsep);
			fout.append("25th_percentile,"+x25+lsep);
			fout.append("Median,"+x50+lsep);
			fout.append("Average,"+average+lsep);
			fout.append("75th_percentile,"+x75+lsep);
			fout.append("95th_percentile,"+x95+lsep);
		}
	}
	public BoxPlot getBoxPlotInOutputUnits(float averageGCO2perMile) {
		BoxPlot bxp = new BoxPlot();
		
		float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);

		bxp.x05 = x05*gCO2perMileToDisplayUnits;
		bxp.x25 = x25*gCO2perMileToDisplayUnits;
		bxp.x50 = x50*gCO2perMileToDisplayUnits;
		bxp.x75 = x75*gCO2perMileToDisplayUnits;
		bxp.x95 = x95*gCO2perMileToDisplayUnits;
		bxp.average = averageGCO2perMile*gCO2perMileToDisplayUnits;
		
		return bxp;
	}
	public void writeBoxPlotDataInCSVFile(FileWriter fout, float averageGCO2perMile) throws Exception {
		getBoxPlotInOutputUnits(averageGCO2perMile).writeInCSVFile(fout);
	}
	public static void writeBoxPlotsFile(String fname, BoxPlot[] boxPlotsData, String[] captions) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			String stOut = "quantity";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + captions[i];
			fout.append(stOut+lsep);
			
			stOut = "5th Percentile";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].x05;
			fout.append(stOut+lsep);
			
			stOut = "25th Percentile";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].x25;
			fout.append(stOut+lsep);
			
			stOut = "median";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].x50;
			fout.append(stOut+lsep);
			
			stOut = "average";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].average;
			fout.append(stOut+lsep);
			
			stOut = "75th Percentile";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].x75;
			fout.append(stOut+lsep);
			
			stOut = "95th Percentile";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + boxPlotsData[i].x95;
			fout.append(stOut+lsep);
			
			stOut = "_errBarLow";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + (boxPlotsData[i].x25-boxPlotsData[i].x05);
			fout.append(stOut+lsep);
			
			stOut = "_bar1_hide";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + (boxPlotsData[i].x25);
			fout.append(stOut+lsep);
			
			stOut = "_bar2_show";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + (boxPlotsData[i].x50 - boxPlotsData[i].x25);
			fout.append(stOut+lsep);
			
			stOut = "_bar3_show";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + (boxPlotsData[i].x75 - boxPlotsData[i].x50);
			fout.append(stOut+lsep);
			
			stOut = "_errBarHight";
			for (int i=0; i<boxPlotsData.length; i++) stOut = stOut + "," + (boxPlotsData[i].x95-boxPlotsData[i].x75);
			fout.append(stOut+lsep);
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	public static class LSPoint {
		public float ghgPerDistance, pdmValue, cdfValue;
		private LSPoint(float x, float pdmVal, float cdfVal) {
			ghgPerDistance = x;
			pdmValue = pdmVal;
			cdfValue = cdfVal;
		}
		@Override public String toString() {
			return ""+ghgPerDistance+","+pdmValue+","+cdfValue;
		}
	}
	
	public LSPoint[] genLadderStepInOutputUnits() {
		float gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
		ArrayList<LSPoint> lst = new ArrayList<LSPoint>();
		
		float prevPDM = 0f;
		for (int i=0; i<pdm.length; i++) {
			float x = i*deltaBin*gCO2perMileToDisplayUnits;
			float curPDM = pdm[i];
			float curCDF = cdf[i];
			
			lst.add(new LSPoint(x, prevPDM, curCDF));
			lst.add(new LSPoint(x, curPDM, curCDF));
			
			prevPDM = curPDM;
		}
		
		lst.add(new LSPoint(maxGCO2perMile*gCO2perMileToDisplayUnits, prevPDM, 1f));
		lst.add(new LSPoint(maxGCO2perMile*gCO2perMileToDisplayUnits, 0, 1f));
		
		LSPoint[] arr = new LSPoint[lst.size()];
		for (int i=0; i<arr.length; i++) arr[i] = lst.get(i);
		return arr;
	}
	
	public void writeHstLadderStepInCSVFile(FileWriter fout) throws Exception {
		String valueCaption = "CO2 Intensity ("+DUnits.getShortName(DUnits.UnitType.GHGUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance)+"),PDM,CDF";
		String lsep = System.getProperty("line.separator");
		
		fout.append(valueCaption+lsep);
		
		LSPoint[] lsPoints = genLadderStepInOutputUnits();
		for (int i=0; i<lsPoints.length; i++) {
			fout.append(lsPoints[i].toString()+lsep);
		}
	}
}
