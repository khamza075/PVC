package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import pvc.utility.CGHGHistogram;
import pvc.utility.MCFloatValuesFileWHeader;

public class FEcoSummayBEVwRep {
	//Local constants
	private static final int NumDivHist = 200;
	private static final int MaxNumTripsWTripMode = 100;
	private static final float CDF_lowCutOff = 0.02f;
	private static final float CDF_highCutOff = 0.98f;
	private static final String TripsModeHeader = "_tripsMode";
	private static final float CDF_cutOffTolRelToRange = 0.0001f;
	private static final float ZTol = 0.0001f;

	
	//Fraction of miles traveled on the BEV in analyzed trips (excluding days where BEV is driven but ends up not being able to do the day trips)
	private float fracMilesOnBEV;
	public float fracMilesOnBEV() {return fracMilesOnBEV;}
	
	//Fraction of days traveled on the BEV in all drive-days
	private float fracDaysBEV;
	public float fracDaysBEV() {return fracDaysBEV;}
	
	//Fraction of days where BEV is driven but ends up not being able to do the day trips
	private float fracDaysFailed;
	public float fracDaysFailed() {return fracDaysFailed;}
	
	//Average kWh/mile for the BEV
	private float avKWhpm;
	public float avKWhpm() {return avKWhpm;}
	
	//Average fuel/mile for the replacement vehicle(s)
	private float[] avRepVehFuelPerMile;
	public float[] avRepVehFuelPerMile() {return avRepVehFuelPerMile;}

	//Total miles in "analyzed" trips (excluding days where BEV is driven but ends up not being able to do the day trips)
	private float totWtMiles;
	public float totWtMiles() {return totWtMiles;}

	//Flag for how summary of trips data is stored
	private boolean tripsMode;
	private CTrip[] rTrips;
	private TBins rBins;
	
	//Function for worst GHG per mile estimation
	public float maxGHGperMile(float eqGridGHG, float[] repVehGHGperUnitFuel) {
		float maxGHGperMile = 0f;
		
		if (tripsMode) {
			for (int i=0; i<rTrips.length; i++) {
				maxGHGperMile = Math.max(maxGHGperMile, rTrips[i].kWhpm * eqGridGHG);
				for (int j=0; j<avRepVehFuelPerMile.length; j++) {
					maxGHGperMile = Math.max(maxGHGperMile, rTrips[i].repVehFuelPerMile[j] * repVehGHGperUnitFuel[j]);
				}
			}			
			return maxGHGperMile;
		}
		
		float deltaBinKWhpm = (rBins.bevMaxKWhpm - rBins.bevMinKWhpm)/(float)NumDivHist;
		float maxKWhPerMileOnBEV = rBins.bevMinKWhpm + deltaBinKWhpm*(NumDivHist - 1) + 0.5f*deltaBinKWhpm;
		float maxGHGperMileOnBEV = maxKWhPerMileOnBEV * eqGridGHG;
		maxGHGperMile = Math.max(maxGHGperMile, maxGHGperMileOnBEV);
		
		for (int j=0; j<avRepVehFuelPerMile.length; j++) {
			float deltaBinFuelPerMile = (rBins.repVehMaxFuelPerMile[j] - rBins.repVehMinFuelPerMile[j])/(float)NumDivHist;
			float cRepVehMaxFuelPerMile = rBins.repVehMinFuelPerMile[j] + deltaBinFuelPerMile*(NumDivHist - 1) + 0.5f*deltaBinFuelPerMile;
			maxGHGperMile = Math.max(maxGHGperMile, cRepVehMaxFuelPerMile*repVehGHGperUnitFuel[j]);
		}
		
		return maxGHGperMile;
	}
	
	public void populateGHGHistogram(CGHGHistogram hst, float eqGridGHG, float[] repVehGHGperUnitFuel, float fracRepVehID, float adjWt) {
		if (tripsMode) {
			for (int i=0; i<rTrips.length; i++) {
				float kWhpm = rTrips[i].kWhpm;
				if (kWhpm >= ZTol) {
					hst.addTrip(kWhpm * eqGridGHG, rTrips[i].wtMiles * adjWt);
				} else if (rTrips[i].repVehFuelPerMile[0] >= ZTol) {
					for (int j=0; j<repVehGHGperUnitFuel.length; j++) {
						float wwt = adjWt * Math.abs((1f-j) - fracRepVehID);
						float gCO2perMile = rTrips[i].repVehFuelPerMile[j]*repVehGHGperUnitFuel[j];
						hst.addTrip(gCO2perMile, rTrips[i].wtMiles * wwt);
					}
				}
			}
			return;
		}
		
		float deltaBinKWhpm = (rBins.bevMaxKWhpm - rBins.bevMinKWhpm)/(float)NumDivHist;
		for (int i=0; i<NumDivHist; i++) {
			float kWhpm = rBins.bevMinKWhpm + deltaBinKWhpm*i + 0.5f*deltaBinKWhpm;
			hst.addTrip(kWhpm * eqGridGHG, rBins.bevBinsWtMiles[i] * adjWt);
		}
		
		for (int j=0; j<repVehGHGperUnitFuel.length; j++) {
			float deltaBinFuelPerMile = (rBins.repVehMaxFuelPerMile[j] - rBins.repVehMinFuelPerMile[j])/(float)NumDivHist;
			float gCO2perUnitFuel = repVehGHGperUnitFuel[j];
			float wwt = adjWt * Math.abs((1f-j) - fracRepVehID);
			
			for (int i=0; i<NumDivHist; i++) {
				float repVehFuelPerMile = rBins.repVehMinFuelPerMile[j] + deltaBinFuelPerMile*i + 0.5f*deltaBinFuelPerMile;
				float gCO2perMile = repVehFuelPerMile * gCO2perUnitFuel;
				hst.addTrip(gCO2perMile, rBins.repVehBinsWtMiles[j][i] * wwt);
			}
		}
	}
	
	//Function for weighing 4 corners
	public static FEcoSummayBEVwRep weighedSummary(FEcoSummayBEVwRep v1, float w1, FEcoSummayBEVwRep v2, float w2,
			FEcoSummayBEVwRep v3, float w3, FEcoSummayBEVwRep v4, float w4) {
		FEcoSummayBEVwRep fes = new FEcoSummayBEVwRep();

		fes.fracDaysBEV = w1*v1.fracDaysBEV + w2*v2.fracDaysBEV + w3*v3.fracDaysBEV + w4*v4.fracDaysBEV;
		fes.fracDaysFailed = w1*v1.fracDaysFailed + w2*v2.fracDaysFailed + w3*v3.fracDaysFailed + w4*v4.fracDaysFailed;
		
		fes.totWtMiles = w1*v1.totWtMiles + w2*v2.totWtMiles + w3*v3.totWtMiles + w4*v4.totWtMiles;
		
		float milesOnBEV = w1*v1.totWtMiles*v1.fracMilesOnBEV + w2*v2.totWtMiles*v2.fracMilesOnBEV + 
				w3*v3.totWtMiles*v3.fracMilesOnBEV + w4*v4.totWtMiles*v4.fracMilesOnBEV;
		fes.fracMilesOnBEV = milesOnBEV / fes.totWtMiles;
		
		float totKWh = w1*v1.totWtMiles*v1.fracMilesOnBEV*v1.avKWhpm + w2*v2.totWtMiles*v2.fracMilesOnBEV*v2.avKWhpm + 
				w3*v3.totWtMiles*v3.fracMilesOnBEV*v3.avKWhpm + w4*v4.totWtMiles*v4.fracMilesOnBEV*v4.avKWhpm;
		fes.avKWhpm = totKWh/(fes.fracMilesOnBEV*fes.totWtMiles);
		
		float[] totFuelRepVeh = new float[v1.avRepVehFuelPerMile.length];
		for (int i=0; i<totFuelRepVeh.length; i++) {
			float milesRepVeh = w1*v1.totWtMiles*(1f - v1.fracMilesOnBEV);
			float fuelRepVeh = milesRepVeh*v1.avRepVehFuelPerMile[i];
			
			milesRepVeh = w2*v2.totWtMiles*(1f - v2.fracMilesOnBEV);
			fuelRepVeh += milesRepVeh*v2.avRepVehFuelPerMile[i];
			
			milesRepVeh = w3*v3.totWtMiles*(1f - v3.fracMilesOnBEV);
			fuelRepVeh += milesRepVeh*v3.avRepVehFuelPerMile[i];
			
			milesRepVeh = w4*v4.totWtMiles*(1f - v4.fracMilesOnBEV);
			fuelRepVeh += milesRepVeh*v4.avRepVehFuelPerMile[i];
			
			totFuelRepVeh[i] = fuelRepVeh;
		}
		
		fes.avRepVehFuelPerMile = new float[totFuelRepVeh.length];
		float milesRepVeh = fes.totWtMiles * (1f - fes.fracMilesOnBEV);
		
		for (int i=0; i<fes.avRepVehFuelPerMile.length; i++) {
			if (milesRepVeh > 0) fes.avRepVehFuelPerMile[i] = totFuelRepVeh[i]/milesRepVeh;
		}
		
		return fes;
	}

	//Private constructor to prevent external instantiation
	private FEcoSummayBEVwRep() {}

	//Function for parsing simulation file
	public static FEcoSummayBEVwRep processSimFile(CompactTripSummaries.CTripSummary[] cTrips, String fname) {
		try {
			//Initialize
			FEcoSummayBEVwRep fes = new FEcoSummayBEVwRep();
			
			//Read Values from simulation file
			float[][] matArray = MCFloatValuesFileWHeader.readFileValues(fname);
			float[] kWhAmounts = matArray[0];

			if (kWhAmounts.length != cTrips.length) return null;

			int numBEVRepVeh = matArray.length - 1;
			float[][] fuelAmounts = new float[numBEVRepVeh][];
			for (int i=0; i<fuelAmounts.length; i++) fuelAmounts[i] = matArray[i+1];
			
			//Check driving days and non-analyze-able days
			float totWtDays = 0f;
			float wtDaysFailed = 0f;
			float wtDaysBEVusedSuccessfully = 0f;
			
			ArrayList<CTrip> lstATrips = new ArrayList<CTrip>();
			
			int idFirstTripOfCurDay = 0;
			while (idFirstTripOfCurDay < cTrips.length) {
				int idLastTripOfCurDay = lastTipOfCurDay(cTrips, idFirstTripOfCurDay);
				
				float hhWt = cTrips[idFirstTripOfCurDay].hhWt;
				boolean bevUsedSuccessfully = true;
				boolean dayFailed = false;
				
				for (int i=idFirstTripOfCurDay; i<=idLastTripOfCurDay; i++) {
					if (kWhAmounts[i] < 0) {
						bevUsedSuccessfully = false;
						if (fuelAmounts[0][i] < 0) dayFailed = true;
					}
				}
				
				totWtDays += hhWt;
				
				if (dayFailed) {
					wtDaysFailed += hhWt;
				} else {
					if (bevUsedSuccessfully) wtDaysBEVusedSuccessfully += hhWt;
					
					for (int i=idFirstTripOfCurDay; i<=idLastTripOfCurDay; i++) {
						CTrip curTrip = new CTrip(numBEVRepVeh);
						
						float miles = cTrips[i].miles;
						curTrip.wtMiles = miles * hhWt;
						
						if (bevUsedSuccessfully) {
							curTrip.kWhpm = kWhAmounts[i]/miles;
						} else {
							for (int j=0; j<numBEVRepVeh; j++) {
								curTrip.repVehFuelPerMile[j] = fuelAmounts[j][i]/miles;
							}
						}
						
						lstATrips.add(curTrip);
					}
				}				
				
				idFirstTripOfCurDay = idLastTripOfCurDay + 1;
			}
			
			//Record day analysis results
			fes.fracDaysFailed = wtDaysFailed/totWtDays;
			fes.fracDaysBEV = wtDaysBEVusedSuccessfully/totWtDays;
			
			//Form array of analyzable trips
			CTrip[] aTrips  = new CTrip[lstATrips.size()];
			for (int i=0; i<aTrips.length; i++) aTrips[i] = lstATrips.get(i);
			
			//Check if trips mode
			if (aTrips.length <= MaxNumTripsWTripMode) {
				fes.tripsMode = true;
				
				fes.rTrips = aTrips;
				fes.totWtMiles = 0f;
				float wtMilesOnBEV = 0f;
				float totWtKWh = 0f;
				float[] totWtFuelAmt = new float[numBEVRepVeh];
				
				for (int i=0; i<aTrips.length; i++) {
					float wtMiles = aTrips[i].wtMiles;
					float kWhpm = aTrips[i].kWhpm;
					float[] fuelPerMile = aTrips[i].repVehFuelPerMile;
					
					fes.totWtMiles += wtMiles;
					
					if (kWhpm > ZTol) {
						wtMilesOnBEV += wtMiles;
						totWtKWh += kWhpm*wtMiles;
					} else {
						for (int j=0; j<numBEVRepVeh; j++) {
							totWtFuelAmt[j] += fuelPerMile[j];
						}
					}
				}
				
				fes.fracMilesOnBEV = wtMilesOnBEV/fes.totWtMiles;
				fes.avKWhpm = totWtKWh/wtMilesOnBEV;
				
				fes.avRepVehFuelPerMile = new float[numBEVRepVeh];
				float wtMilesOnRepVeh = fes.totWtMiles - wtMilesOnBEV;
				
				if (wtMilesOnRepVeh > 0) {
					for (int j=0; j<numBEVRepVeh; j++) fes.avRepVehFuelPerMile[j] = totWtFuelAmt[j]/wtMilesOnRepVeh;
				}
				
				return fes;
			}
			
			//Histograms mode
			fes.tripsMode = false;

			//Form Histograms
			fes.rBins = new TBins(aTrips);

			//Calculate averages for non-outlier samples
			float wtMilesOnBEV = 0f;
			float totWtKWh = 0f;
			float[] totWtFuelAmt = new float[numBEVRepVeh];
			float[] wtMilesRepVeh = new float[numBEVRepVeh];

			for (int i=0; i<aTrips.length; i++) {
				float wtMiles = aTrips[i].wtMiles;
				float kWhpm = aTrips[i].kWhpm;
				
				if (kWhpm > ZTol) {
					if ((kWhpm >= fes.rBins.bevMinKWhpm) && (kWhpm <= fes.rBins.bevMaxKWhpm)) {
						wtMilesOnBEV += wtMiles;
						totWtKWh += kWhpm*wtMiles;
					}
				} else {
					for (int j=0; j<numBEVRepVeh; j++) {
						float fuelPerMile = aTrips[i].repVehFuelPerMile[j];
						
						if ((fuelPerMile >= fes.rBins.repVehMinFuelPerMile[j]) && (fuelPerMile <= fes.rBins.repVehMaxFuelPerMile[j])) {
							totWtFuelAmt[j] += fuelPerMile*wtMiles;
							wtMilesRepVeh[j] += wtMiles;
						}
					}
				}
			}
			
			float avRepVehWtMiles = 0f;
			for (int j=0; j<numBEVRepVeh; j++) avRepVehWtMiles += wtMilesRepVeh[j];
			avRepVehWtMiles = avRepVehWtMiles/(float)numBEVRepVeh;
			
			fes.totWtMiles = wtMilesOnBEV + avRepVehWtMiles;
			fes.fracMilesOnBEV = wtMilesOnBEV/fes.totWtMiles;
			
			fes.avKWhpm = totWtKWh/wtMilesOnBEV;			
			fes.avRepVehFuelPerMile = new float[numBEVRepVeh];

			for (int j=0; j<numBEVRepVeh; j++) {
				if (wtMilesRepVeh[j] > ZTol) fes.avRepVehFuelPerMile[j] = totWtFuelAmt[j]/wtMilesRepVeh[j];
			}
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static FEcoSummayBEVwRep readSummaryOnly(String fname) {
		try {
			FEcoSummayBEVwRep fes = new FEcoSummayBEVwRep();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.totWtMiles = Float.parseFloat(sp[0]);
			fes.fracMilesOnBEV = Float.parseFloat(sp[1]);
			fes.fracDaysBEV = Float.parseFloat(sp[2]);
			fes.fracDaysFailed = Float.parseFloat(sp[3]);
			fes.avKWhpm = Float.parseFloat(sp[4]);
			
			fes.avRepVehFuelPerMile = new float[sp.length - 5];
			for (int i=0; i<fes.avRepVehFuelPerMile.length; i++) fes.avRepVehFuelPerMile[i] = Float.parseFloat(sp[i+5]);
			
			fin.close();
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	public static FEcoSummayBEVwRep readFromFile(String fname) {
		try {
			FEcoSummayBEVwRep fes = new FEcoSummayBEVwRep();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.totWtMiles = Float.parseFloat(sp[0]);
			fes.fracMilesOnBEV = Float.parseFloat(sp[1]);
			fes.fracDaysBEV = Float.parseFloat(sp[2]);
			fes.fracDaysFailed = Float.parseFloat(sp[3]);
			fes.avKWhpm = Float.parseFloat(sp[4]);
			
			fes.avRepVehFuelPerMile = new float[sp.length - 5];
			for (int i=0; i<fes.avRepVehFuelPerMile.length; i++) fes.avRepVehFuelPerMile[i] = Float.parseFloat(sp[i+5]);
						
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
				fes.rBins = new TBins(fin, fes.avRepVehFuelPerMile.length);
			}
			
			fin.close();
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}

	public void writeToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			String st ="totWtMiles,fracMilesOnBEV,fracDaysBEV,fracDaysFailed,avKWhpm";
			for (int i=0; i<avRepVehFuelPerMile.length; i++) st = st + ",avFuelPerMile_bevRep" + (i+1);
			
			fout.append(st+lsep);
			
			st = "" + totWtMiles + "," + fracMilesOnBEV + "," + fracDaysBEV + "," + fracDaysFailed + "," + avKWhpm;
			for (int i=0; i<avRepVehFuelPerMile.length; i++) st = st + "," + avRepVehFuelPerMile[i];
			
			fout.append(st+lsep);
			
			try {
				if (tripsMode) {
					fout.append(TripsModeHeader+lsep);

					for (int i=0; i<rTrips.length; i++) {
						fout.append(rTrips[i].toString()+lsep);
					}
				} else {
					fout.append("_histogramMode"+lsep);					
					
					fout.append(""+rBins.bevMinKWhpm+","+rBins.bevMaxKWhpm+lsep);
					
					for (int i=0; i<rBins.repVehMinFuelPerMile.length; i++) {
						fout.append(""+rBins.repVehMinFuelPerMile[i]+","+rBins.repVehMaxFuelPerMile[i]+lsep);
					}

					for (int i=0; i<NumDivHist; i++) {
						fout.append(""+rBins.bevBinsWtMiles[i]+lsep);
					}
					
					for (int i=0; i<NumDivHist; i++) {
						st = "" + rBins.repVehBinsWtMiles[0][i];
						for (int j=1; j<rBins.repVehMinFuelPerMile.length; j++) st = st + "," + rBins.repVehBinsWtMiles[j][i];
						fout.append(st+lsep);
					}
				}
			} catch (Exception e) {}			
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
		
	private static class CTrip {
		private float wtMiles, kWhpm;
		private float[] repVehFuelPerMile;
		
		private CTrip(int numRepVeh) {
			repVehFuelPerMile = new float[numRepVeh];
		}
		private CTrip(String readLine) {
			String[] sp = readLine.split(",");
			repVehFuelPerMile = new float[sp.length-2];
			for (int i=0; i<repVehFuelPerMile.length; i++) repVehFuelPerMile[i] = Float.parseFloat(sp[i]);
			kWhpm = Float.parseFloat(sp[repVehFuelPerMile.length]);
			wtMiles = Float.parseFloat(sp[repVehFuelPerMile.length+1]);
		}
		@Override public String toString() {
			String st = ""+repVehFuelPerMile[0];
			for (int i=1; i<repVehFuelPerMile.length; i++) st = st + "," + repVehFuelPerMile[i];
			return st + "," + kWhpm + "," + wtMiles;
		}
	}
	private static class TBins {
		private float bevMinKWhpm, bevMaxKWhpm;
		private float[] bevBinsWtMiles;
		
		private float[] repVehMinFuelPerMile, repVehMaxFuelPerMile;
		private float[][] repVehBinsWtMiles;	//First index on replacement vehicle ID
		
		private TBins(BufferedReader fin, int numRepVeh) throws Exception {
			bevBinsWtMiles = new float[NumDivHist];
			repVehMinFuelPerMile = new float[numRepVeh];
			repVehMaxFuelPerMile = new float[numRepVeh];
			repVehBinsWtMiles = new float[numRepVeh][NumDivHist];
			
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			bevMinKWhpm = Float.parseFloat(sp[0]);
			bevMaxKWhpm = Float.parseFloat(sp[1]);
			
			for (int i=0; i<numRepVeh; i++) {
				readLine = fin.readLine();
				sp = readLine.split(",");
				
				repVehMinFuelPerMile[i] = Float.parseFloat(sp[0]);
				repVehMaxFuelPerMile[i] = Float.parseFloat(sp[1]);
			}
			
			for (int i=0; i<NumDivHist; i++) {
				readLine = fin.readLine();
				bevBinsWtMiles[i] = Float.parseFloat(readLine);
			}
			
			for (int i=0; i<NumDivHist; i++) {
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int j=0; j<numRepVeh; j++) {
					repVehBinsWtMiles[j][i] = Float.parseFloat(sp[j]);
				}
			}
		}
		private TBins(CTrip[] aTrips) {
			int numRepVeh = aTrips[0].repVehFuelPerMile.length;
			
			//Allocate
			bevBinsWtMiles = new float[NumDivHist];
			repVehMinFuelPerMile = new float[numRepVeh];
			repVehMaxFuelPerMile = new float[numRepVeh];
			repVehBinsWtMiles = new float[numRepVeh][NumDivHist];
			
			//Identify min-max
			bevMinKWhpm = Float.MAX_VALUE;
			bevMaxKWhpm = 0f;
			for (int i=0; i<repVehMinFuelPerMile.length; i++) repVehMinFuelPerMile[i] = Float.MAX_VALUE;
			
			for (int i=0; i<aTrips.length; i++) {
				float kWhpm = aTrips[i].kWhpm;
				float[] fuelPerMile = aTrips[i].repVehFuelPerMile;
				
				if (kWhpm > ZTol) {
					if (bevMinKWhpm > kWhpm) bevMinKWhpm = kWhpm;
					if (bevMaxKWhpm < kWhpm) bevMaxKWhpm = kWhpm;
				} else {
					for (int j=0; j<fuelPerMile.length; j++) {
						if (fuelPerMile[j] > ZTol) {
							if (repVehMinFuelPerMile[j] > fuelPerMile[j]) repVehMinFuelPerMile[j] = fuelPerMile[j];
							if (repVehMaxFuelPerMile[j] < fuelPerMile[j]) repVehMaxFuelPerMile[j] = fuelPerMile[j];
						}
					}
				}
			}
			for (int i=0; i<repVehMinFuelPerMile.length; i++) repVehMinFuelPerMile[i] = Math.min(repVehMaxFuelPerMile[i], repVehMinFuelPerMile[i]);
			
			
			//Re-adjust limits for electricity
			if (bevMaxKWhpm < ZTol) {
				//Can only happen if there are no electric trips?!
				bevMaxKWhpm = 2f*ZTol;
			} else {
				if (bevMaxKWhpm == bevMinKWhpm) {
					//The odd-chance of all trips being perfectly identical
					bevMaxKWhpm *= (1f + 10f*ZTol);	//Multiple by a number just greater than 1
				}
				if (bevMaxKWhpm == bevMinKWhpm) {
					//If still not good enough, add a small number
					bevMaxKWhpm += 2f*ZTol;
				}
				
				//Line search lower end
				float deltaBin = (bevMaxKWhpm - bevMinKWhpm)/(float)NumDivHist;
				float deltaTol = (bevMaxKWhpm - bevMinKWhpm)*CDF_cutOffTolRelToRange;
				float lb = bevMinKWhpm;
				float ub = lb + deltaBin;
				float targetCDFAbove = 1f - CDF_lowCutOff;
				float cdfAboveUB = cdfAbove_kWhpmValue(aTrips, ub);
				
				while (cdfAboveUB > targetCDFAbove) {
					ub += deltaBin;
					cdfAboveUB = cdfAbove_kWhpmValue(aTrips, ub);
				}
				
				//Bisection lower end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAbove_kWhpmValue(aTrips, tPos);

					if (Math.abs(cdfAboveTPos - targetCDFAbove) <= ZTol) {
						lb = tPos;
						break;
					}
					
					if (cdfAboveTPos > targetCDFAbove) {
						lb = tPos;
					} else {
						ub = tPos;
					}
				}
				
				bevMinKWhpm = lb;
				
				//Line search higher end
				ub = bevMaxKWhpm;
				lb = ub - deltaBin;
				targetCDFAbove = 1f - CDF_highCutOff;
				float cdfAboveLB = cdfAbove_kWhpmValue(aTrips, lb);
				
				while (cdfAboveLB < targetCDFAbove) {
					lb += -deltaBin;
					cdfAboveLB = cdfAbove_kWhpmValue(aTrips, lb);
				}

				//Bisection higher end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAbove_kWhpmValue(aTrips, tPos);
	
					if (Math.abs(cdfAboveTPos - targetCDFAbove) <= ZTol) {
						ub = tPos;
						break;
					}
					
					if (cdfAboveTPos > targetCDFAbove) {
						lb = tPos;
					} else {
						ub = tPos;
					}
				}
				
				bevMaxKWhpm = ub;
			}
			
			//Re-adjust limits for fuel
			for (int j=0; j<numRepVeh; j++) {
				if (repVehMaxFuelPerMile[j] < ZTol) {
					//Can only happen if there are no gas trips trips?!
					repVehMaxFuelPerMile[j] = 2f*ZTol;
				} else {
					if (repVehMaxFuelPerMile[j] == repVehMinFuelPerMile[j]) {
						//The odd-chance of all trips being perfectly identical
						repVehMaxFuelPerMile[j] *= (1f + 10f*ZTol);	//Multiple by a number just greater than 1
					}
					if (repVehMaxFuelPerMile[j] == repVehMinFuelPerMile[j]) {
						//If still not good enough, add a small number
						repVehMaxFuelPerMile[j] += 2f*ZTol;
					}
					
					//Line search lower end
					float deltaBin = (repVehMaxFuelPerMile[j] - repVehMinFuelPerMile[j])/(float)NumDivHist;
					float deltaTol = (repVehMaxFuelPerMile[j] - repVehMinFuelPerMile[j])*CDF_cutOffTolRelToRange;
					float lb = repVehMinFuelPerMile[j];
					float ub = lb + deltaBin;
					float targetCDFAbove = 1f - CDF_lowCutOff;
					float cdfAboveUB = cdfAbove_fuelPerMileValue(aTrips, ub, j);
					
					while (cdfAboveUB > targetCDFAbove) {
						ub += deltaBin;
						cdfAboveUB = cdfAbove_fuelPerMileValue(aTrips, ub, j);
					}
					
					//Bisection lower end
					while ((ub - lb) > deltaTol) {
						float tPos  = 0.5f*(ub + lb);
						float cdfAboveTPos = cdfAbove_fuelPerMileValue(aTrips, tPos, j);

						if (Math.abs(cdfAboveTPos - targetCDFAbove) <= ZTol) {
							lb = tPos;
							break;
						}
						
						if (cdfAboveTPos > targetCDFAbove) {
							lb = tPos;
						} else {
							ub = tPos;
						}
					}
					
					repVehMinFuelPerMile[j] = lb;
					
					//Line search higher end
					ub = repVehMaxFuelPerMile[j];
					lb = ub - deltaBin;
					targetCDFAbove = 1f - CDF_highCutOff;
					float cdfAboveLB = cdfAbove_fuelPerMileValue(aTrips, lb, j);
					
					while (cdfAboveLB < targetCDFAbove) {
						lb += -deltaBin;
						cdfAboveLB = cdfAbove_fuelPerMileValue(aTrips, lb, j);
					}

					//Bisection higher end
					while ((ub - lb) > deltaTol) {
						float tPos  = 0.5f*(ub + lb);
						float cdfAboveTPos = cdfAbove_fuelPerMileValue(aTrips, tPos, j);
		
						if (Math.abs(cdfAboveTPos - targetCDFAbove) <= ZTol) {
							ub = tPos;
							break;
						}
						
						if (cdfAboveTPos > targetCDFAbove) {
							lb = tPos;
						} else {
							ub = tPos;
						}
					}
					
					repVehMaxFuelPerMile[j] = ub;
				}
			}
			
			//Place weighed miles into bins
			for (int i=0; i<aTrips.length; i++) {
				float kWhpm = aTrips[i].kWhpm;
				
				if (kWhpm > ZTol) {
					int binID = (int)(((kWhpm - bevMinKWhpm)/(bevMaxKWhpm - bevMinKWhpm))*NumDivHist);
					if ((binID >= 0) && (binID < NumDivHist)) bevBinsWtMiles[binID] += aTrips[i].wtMiles;
				} else {
					for (int j=0; j<numRepVeh; j++) {
						float fuelPerMile = aTrips[i].repVehFuelPerMile[j];
						if (fuelPerMile > ZTol) {
							int binID = (int)(((fuelPerMile - repVehMinFuelPerMile[j])/(repVehMaxFuelPerMile[j] - repVehMinFuelPerMile[j]))*NumDivHist);
							if ((binID >= 0) && (binID < NumDivHist)) repVehBinsWtMiles[j][binID] += aTrips[i].wtMiles;
						}
					}
				}
			}
		}
	}

	private static int lastTipOfCurDay(CompactTripSummaries.CTripSummary[] allTrips, int curTripID) {
		int idLastTripOfCurDay = curTripID;
		int hhID = allTrips[idLastTripOfCurDay].hhID;
		int vehIDinHH = allTrips[idLastTripOfCurDay].vehIDinHH;
		int dayID = allTrips[idLastTripOfCurDay].dayID;
		
		while (true) {
			idLastTripOfCurDay++;
			if (idLastTripOfCurDay >= allTrips.length) return idLastTripOfCurDay-1;
			
			int curDayID = allTrips[idLastTripOfCurDay].dayID;
			if (curDayID != dayID) return idLastTripOfCurDay-1;

			int curVehIDinHH = allTrips[idLastTripOfCurDay].vehIDinHH;
			if (curVehIDinHH != vehIDinHH) return idLastTripOfCurDay-1;

			int curHouseholdID = allTrips[idLastTripOfCurDay].hhID;
			if (curHouseholdID != hhID) return idLastTripOfCurDay-1;
		}
	}	

	private static float cdfAbove_kWhpmValue(CTrip[] aTrips, float kWhpmValue) {
		float wtMilesAnalyzed = 0f;
		float wtMilesAboveValue = 0f;
		
		for (int i=0; i<aTrips.length; i++) {
			float kWhpm = aTrips[i].kWhpm;
			if (kWhpm > ZTol) {
				float wtMiles = aTrips[i].wtMiles;
				wtMilesAnalyzed += wtMiles;
				if (kWhpm >= kWhpmValue) wtMilesAboveValue += wtMiles;
			}
		}
		
		if (wtMilesAnalyzed < ZTol) return 0f;
		return wtMilesAboveValue/wtMilesAnalyzed;
	}
	private static float cdfAbove_fuelPerMileValue(CTrip[] aTrips, float fuelPerMileValue, int vehRepID) {
		float wtMilesAnalyzed = 0f;
		float wtMilesAboveValue = 0f;
		
		for (int i=0; i<aTrips.length; i++) {
			float fuelPerMile = aTrips[i].repVehFuelPerMile[vehRepID];
			
			if (fuelPerMile > ZTol) {
				float wtMiles = aTrips[i].wtMiles;
				wtMilesAnalyzed += wtMiles;
				if (fuelPerMile >= fuelPerMileValue) wtMilesAboveValue += wtMiles;
			}
		}
		
		if (wtMilesAnalyzed < ZTol) return 0f;
		return wtMilesAboveValue/wtMilesAnalyzed;
	}
}
