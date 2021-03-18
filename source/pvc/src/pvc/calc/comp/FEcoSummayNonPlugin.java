package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import pvc.utility.CGHGHistogram;
import pvc.utility.MCFloatValuesFileWHeader;

public class FEcoSummayNonPlugin {
	private static final int NumDivHist = 200;
	private static final int MaxNumTripsWTripMode = 100;
	
	private static final float CDF_lowCutOff = 0.02f;
	private static final float CDF_highCutOff = 0.98f;
	private static final float CDF_cutOffTolRelToRange = 0.0001f;
	
	private static final String TripsModeHeader = "_tripsMode";

	private float avFuelOrKWhperMile;
	public float avFuelOrKWhperMile() {return avFuelOrKWhperMile;}
	
	private float totWtMiles;
	public float totWtMiles() {return totWtMiles;}
	
	private boolean tripsMode;
	private CTrip[] rTrips;
	private TBins rBins;
	
	private FEcoSummayNonPlugin() {}
	
	public FEcoSummayNonPlugin copySummary() {
		FEcoSummayNonPlugin fes = new FEcoSummayNonPlugin();
		
		fes.avFuelOrKWhperMile = avFuelOrKWhperMile;
		fes.totWtMiles = totWtMiles;
		
		return fes;
	}
	
	public static FEcoSummayNonPlugin processSimFile(CompactTripSummaries.CTripSummary[] cTrips, String fname) {
		try {
			FEcoSummayNonPlugin fes = new FEcoSummayNonPlugin();
			
			float[][] matArray = MCFloatValuesFileWHeader.readFileValues(fname);
			float[] fuelAmts = matArray[0];
			if (fuelAmts.length != cTrips.length) return null;
			
			if (fuelAmts.length <= MaxNumTripsWTripMode) {
				fes.tripsMode = true;
				fes.rTrips = new CTrip[fuelAmts.length];
				
				float totWtFuel = 0f;
				fes.totWtMiles = 0f;

				for (int i=0; i<fuelAmts.length; i++) {
					float miles = cTrips[i].miles;
					float fuelAmt = fuelAmts[i];
					float hhWt = cTrips[i].hhWt;
					
					fes.rTrips[i] = new CTrip();
					fes.rTrips[i].fuelOrKWhPerMile = fuelAmt/miles;
					fes.rTrips[i].wtMiles = hhWt*miles;
					
					fes.totWtMiles += hhWt*miles;
					totWtFuel += hhWt*fuelAmt;
				}
				
				fes.avFuelOrKWhperMile = totWtFuel/fes.totWtMiles;
				return fes;
			}
			
			fes.tripsMode = false;
			CTrip[] tmpTrips = new CTrip[fuelAmts.length];
			for (int i=0; i<fuelAmts.length; i++) {
				float miles = cTrips[i].miles;
				float fuelAmt = fuelAmts[i];
				float hhWt = cTrips[i].hhWt;
				
				tmpTrips[i] = new CTrip();
				tmpTrips[i].fuelOrKWhPerMile = fuelAmt/miles;
				tmpTrips[i].wtMiles = hhWt*miles;
			}
			
			fes.rBins = new TBins(tmpTrips);			
			
			float totWtFuel = 0f;
			fes.totWtMiles = 0f;

			for (int i=0; i<fuelAmts.length; i++) {
				float miles = cTrips[i].miles;
				float fuelAmt = fuelAmts[i];
				float hhWt = cTrips[i].hhWt;
				
				float fuelAmtPerMile = fuelAmt/miles;
				if ((fuelAmtPerMile >= fes.rBins.minFuelOrKWhperMile)&&(fuelAmtPerMile <= fes.rBins.maxFuelOrKWhperMile)) {
					fes.totWtMiles += hhWt*miles;
					totWtFuel += hhWt*fuelAmt;
				}
			}
			
			fes.avFuelOrKWhperMile = totWtFuel/fes.totWtMiles;

			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	public static FEcoSummayNonPlugin readSummaryOnly(String fname) {
		try {
			FEcoSummayNonPlugin fes = new FEcoSummayNonPlugin();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.avFuelOrKWhperMile = Float.parseFloat(sp[0]);
			fes.totWtMiles = Float.parseFloat(sp[1]);
			
			fin.close();
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	public static FEcoSummayNonPlugin readFromFile(String fname) {
		try {
			FEcoSummayNonPlugin fes = new FEcoSummayNonPlugin();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.avFuelOrKWhperMile = Float.parseFloat(sp[0]);
			fes.totWtMiles = Float.parseFloat(sp[1]);
			
			readLine = fin.readLine();
			if (readLine.equalsIgnoreCase(TripsModeHeader)) {
				fes.tripsMode = true;
				ArrayList<CTrip> lst = new ArrayList<CTrip>();
				
				while ((readLine = fin.readLine())!=null) {
					lst.add(new CTrip(readLine));
				}
				
				fes.rTrips = new CTrip[lst.size()];
				for (int i=0; i<fes.rTrips.length; i++) fes.rTrips[i] = lst.get(i);
				
			} else {
				fes.tripsMode = false;
				fes.rBins = new TBins(fin);
			}
			
			fin.close();
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	
	public float maxGHGperMile(float ghgPerUnitFuelOrEnergy) {
		if (tripsMode) {
			float maxFuelOrKWhPerMile = 0f;
			for (int i=0; i<rTrips.length; i++) {
				if (maxFuelOrKWhPerMile < rTrips[i].fuelOrKWhPerMile) maxFuelOrKWhPerMile = rTrips[i].fuelOrKWhPerMile;
			}
			return ghgPerUnitFuelOrEnergy*maxFuelOrKWhPerMile;
		}
		
		int numBins = rBins.wtMiles.length;
		float binStep = (rBins.maxFuelOrKWhperMile - rBins.minFuelOrKWhperMile)/(float)numBins;
		float fuelOrKWhPerMileAtCenterOfLastBin = rBins.minFuelOrKWhperMile + (numBins - 1)*binStep + 0.5f*binStep; 
		return ghgPerUnitFuelOrEnergy * fuelOrKWhPerMileAtCenterOfLastBin;
	}
	
	public void populateGHGHistogram(CGHGHistogram hst, float ghgPerUnitFuelOrEnergy) {
		if (tripsMode) {
			for (int i=0; i<rTrips.length; i++) {
				float gCO2perMile = rTrips[i].fuelOrKWhPerMile * ghgPerUnitFuelOrEnergy;
				hst.addTrip(gCO2perMile, rTrips[i].wtMiles);
			}
			return;
		}
		
		int numBins = rBins.wtMiles.length;
		float binStep = (rBins.maxFuelOrKWhperMile - rBins.minFuelOrKWhperMile)/(float)numBins;
		for (int i=0; i<numBins; i++) {
			float fuelOrKWhPerMile = rBins.minFuelOrKWhperMile + i*binStep + 0.5f*binStep;
			float gCO2perMile = fuelOrKWhPerMile * ghgPerUnitFuelOrEnergy;
			hst.addTrip(gCO2perMile, rBins.wtMiles[i]);
		}
	}
	
	public void writeToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("avFuelOrKWhperMile,totWtMiles"+lsep);
			fout.append(""+avFuelOrKWhperMile+","+totWtMiles+lsep);
			
			try {
				if (tripsMode) {
					fout.append(TripsModeHeader+lsep);

					for (int i=0; i<rTrips.length; i++) {
						fout.append(rTrips[i].toString()+lsep);
					}
	
				} else {
					fout.append("_histogramMode"+lsep);
					fout.append(""+rBins.minFuelOrKWhperMile+","+rBins.maxFuelOrKWhperMile+lsep);
					for (int i=0; i<NumDivHist; i++) {
						fout.append(""+rBins.wtMiles[i]+lsep);
					}
				}
			} catch (Exception e) {}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private static class CTrip {
		private float fuelOrKWhPerMile,wtMiles;
		private CTrip() {}
		private CTrip(String readLine) {
			String[] sp = readLine.split(",");
			fuelOrKWhPerMile = Float.parseFloat(sp[0]);
			wtMiles = Float.parseFloat(sp[1]);
		}
		@Override public String toString() {return ""+fuelOrKWhPerMile+","+wtMiles;}
	}
	
	private static class TBins {
		private float minFuelOrKWhperMile, maxFuelOrKWhperMile;
		private float[] wtMiles;
		
		private TBins(BufferedReader fin) throws Exception {
			wtMiles = new float[NumDivHist];
			
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			minFuelOrKWhperMile = Float.parseFloat(sp[0]);
			maxFuelOrKWhperMile = Float.parseFloat(sp[1]);
			
			for (int i=0; i<wtMiles.length; i++) {
				readLine = fin.readLine();
				wtMiles[i] = Float.parseFloat(readLine);
			}
		}
		
		private TBins(CTrip[] allTrips) {
			wtMiles = new float[NumDivHist];
			
			final float fracOffset = 0.001f;
			final float zTol = 0.0001f;
			
			//Identify min, max & total sum of values
			minFuelOrKWhperMile = allTrips[0].fuelOrKWhPerMile;
			maxFuelOrKWhperMile = minFuelOrKWhperMile;
			float totWtMiles = 0f;
			
			for (int i=1; i<allTrips.length; i++) {
				float cValue = allTrips[i].fuelOrKWhPerMile;
				float cWtMiles = allTrips[i].wtMiles;
				
				if (minFuelOrKWhperMile > cValue) minFuelOrKWhperMile = cValue;
				if (maxFuelOrKWhperMile < cValue) maxFuelOrKWhperMile = cValue;
				totWtMiles += cWtMiles;
			}			
			if (maxFuelOrKWhperMile == minFuelOrKWhperMile) {
				float offset = fracOffset*maxFuelOrKWhperMile;
				maxFuelOrKWhperMile += offset;
			}
			if (maxFuelOrKWhperMile == minFuelOrKWhperMile) maxFuelOrKWhperMile += zTol;
			
			//If there aren't any entries, then there's nothing more to do
			if (totWtMiles < zTol) return;
			
			//Search for upper/lower bounds for CDF cut-off
			float deltaStep = (maxFuelOrKWhperMile - minFuelOrKWhperMile)/(float)NumDivHist;
			float minStepSize = (maxFuelOrKWhperMile - minFuelOrKWhperMile)*CDF_cutOffTolRelToRange;
			
			float lb = minFuelOrKWhperMile;
			float ub = lb + deltaStep;
			float cdfAboveUB = calcCDFAboveValue(allTrips, ub);
			float targetCDFAbove = 1f - CDF_lowCutOff;
			
				//Line search lower lower limit
			while (cdfAboveUB > targetCDFAbove) {
				ub += deltaStep;
				cdfAboveUB = calcCDFAboveValue(allTrips, ub);
			}
				//Bisection lower limit
			while ((ub - lb) > minStepSize) {
				float tPos  = 0.5f*(ub + lb);
				float cdfAboveTPos = calcCDFAboveValue(allTrips, tPos);
				
				if (Math.abs(cdfAboveTPos - targetCDFAbove) <= zTol) {
					lb = tPos;
					break;
				}
				
				if (cdfAboveTPos > targetCDFAbove) {
					lb = tPos;
				} else {
					ub = tPos;
				}
			}
				//Set lower limit
			minFuelOrKWhperMile = lb;
				//Line search upper limit
			ub = maxFuelOrKWhperMile;
			lb = ub - deltaStep;
			float cdfAboveLB = calcCDFAboveValue(allTrips, lb);
			targetCDFAbove = 1f - CDF_highCutOff;
			while (cdfAboveLB < targetCDFAbove) {
				lb += -deltaStep;
				cdfAboveLB = calcCDFAboveValue(allTrips, lb);
			}
				//Bisection upper limit
			while ((ub - lb) > minStepSize) {
				float tPos  = 0.5f*(ub + lb);
				float cdfAboveTPos = calcCDFAboveValue(allTrips, tPos);
				
				if (Math.abs(cdfAboveTPos - targetCDFAbove) <= zTol) {
					ub = tPos;
					break;
				}
				
				if (cdfAboveTPos > targetCDFAbove) {
					lb = tPos;
				} else {
					ub = tPos;
				}
			}
				//Set upper limit
			maxFuelOrKWhperMile = ub;

			//Put to bins
			float fullRange = maxFuelOrKWhperMile - minFuelOrKWhperMile;
			deltaStep = fullRange/(float)NumDivHist;
			
			for (int i=0; i<allTrips.length; i++) {
				float cValue = allTrips[i].fuelOrKWhPerMile;
				int binID = (int)(((cValue - minFuelOrKWhperMile)/fullRange)*NumDivHist);
				
				if ((binID >= 0)&&(binID < NumDivHist)) {
					float cWt = allTrips[i].wtMiles;
					wtMiles[binID] += cWt;
				}
			}
		}
	}
	private static float calcCDFAboveValue(CTrip[] allTrips, float value) {
		float sumWt = 0f;
		float sumWtAbove = 0f;
		for (int i=0; i<allTrips.length; i++) {
			float cWt = allTrips[i].wtMiles;
			sumWt += cWt;
			if (allTrips[i].fuelOrKWhPerMile >= value) sumWtAbove += cWt;
		}
		return sumWtAbove/sumWt;
	}
}
