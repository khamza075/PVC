package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import pvc.utility.CGHGHistogram;
import pvc.utility.MCFloatValuesFileWHeader;

public class FEcoSummayPHEV {
	private static final int NumDivUni = 200;
	private static final int NumDivBi = 50;
	private static final int MaxNumTripsWTripMode = 100;

	private static final int MinHistTripsToChopOutliers = 100;
	private static final float CDF_lowCutOffUni = 0.02f;
	private static final float CDF_highCutOffUni = 0.98f;
	private static final float CDF_highCutOffBi = 0.98f;
	private static final float CDF_cutOffTolRelToRange = 0.0001f;
	private static final float ZTol = 0.0001f;
	
	private static final String TripsModeHeader = "_tripsMode";

	private float allMilesAvFuelperMile;
	public float allMilesAvFuelperMile() {return allMilesAvFuelperMile;}
	
	private float allMilesAvKWHperMile;
	public float allMilesAvKWHperMile() {return allMilesAvKWHperMile;}
	
	private float totWtMiles;
	public float totWtMiles() {return totWtMiles;}
	
	private boolean tripsMode;
	private CTrip[] rTrips;
	private TBins rBins;

	private FEcoSummayPHEV() {}
	
	public FEcoSummayPHEV copySummary() {
		FEcoSummayPHEV fes = new FEcoSummayPHEV();
		
		fes.allMilesAvFuelperMile = allMilesAvFuelperMile;
		fes.allMilesAvKWHperMile = allMilesAvKWHperMile;
		fes.totWtMiles = totWtMiles;
		
		return fes;
	}
	
	public void populateGHGHistogram(CGHGHistogram hst, float eqGridGHG, float ghgPerUnitFuel, float adjWt) {
		if (tripsMode) {
			for (int i=0; i<rTrips.length; i++) {
				float gCO2perMile = rTrips[i].kWhPerMile*eqGridGHG + rTrips[i].fuelPerMile*ghgPerUnitFuel;
				hst.addTrip(gCO2perMile, rTrips[i].wtMiles * adjWt);
			}
			return;
		}

		if (rBins.zeroFuelBins_wtMiles != null) {
			float deltaKWhpm = (rBins.zeroFuelBins_maxKWhpm - rBins.zeroFuelBins_minKWhpm)/(float)NumDivUni;
			
			for (int i=0; i<NumDivUni; i++) {
				float kWhpmAtCurBin = rBins.zeroFuelBins_minKWhpm + deltaKWhpm*i + 0.5f*deltaKWhpm;
				float gCO2perMile = kWhpmAtCurBin*eqGridGHG;
				hst.addTrip(gCO2perMile, rBins.zeroFuelBins_wtMiles[i] * adjWt);
			}
		}
		if (rBins.zeroElectBins_wtMiles != null) {
			float deltaFuelPerMile = (rBins.zeroElectBins_maxFuelperMile - rBins.zeroElectBins_minFuelperMile)/(float)NumDivUni;
			for (int i=0; i<NumDivUni; i++) {
				float fuelPerMileAtCurBin = rBins.zeroElectBins_minFuelperMile + deltaFuelPerMile*i + 0.5f*deltaFuelPerMile;			
				float gCO2perMile = fuelPerMileAtCurBin*ghgPerUnitFuel;
				hst.addTrip(gCO2perMile, rBins.zeroElectBins_wtMiles[i] * adjWt);
			}
		}
		if (rBins.mixedBins_wtMiles != null) {
			float deltaKWhpm = rBins.mixedBins_maxKWhpm/(float)NumDivBi;
			float deltaFuelPerMile = rBins.mixedBins_maxFuelPerMile/(float)NumDivBi;
			
			for (int i=0; i<NumDivBi; i++) {
				float kWhpmAtCurBin = deltaKWhpm*i + 0.5f*deltaKWhpm;
				
				for (int j=0; j<NumDivBi; j++) {
					float fuelPerMileAtCurBin = deltaFuelPerMile*j + 0.5f*deltaFuelPerMile;
					float gCO2perMile = kWhpmAtCurBin*eqGridGHG + fuelPerMileAtCurBin*ghgPerUnitFuel;
					hst.addTrip(gCO2perMile, rBins.mixedBins_wtMiles[j][i] * adjWt);	//First index is on fuel per mile
				}
			}
		}		
	}
	
	public float maxGHGperMile(float eqGridGHG, float ghgPerUnitFuel) {
		float maxGHGperMile = 0f;
		
		if (tripsMode) {
			for (int i=0; i<rTrips.length; i++) {
				float curTripGHGperMile = rTrips[i].kWhPerMile*eqGridGHG + rTrips[i].fuelPerMile*ghgPerUnitFuel;
				if (maxGHGperMile < curTripGHGperMile) maxGHGperMile = curTripGHGperMile;
			}
			return maxGHGperMile;
		}
		
		if (rBins.zeroFuelBins_wtMiles != null) {
			float deltaKWhpm = (rBins.zeroFuelBins_maxKWhpm - rBins.zeroFuelBins_minKWhpm)/(float)NumDivUni;
			float kWhpmAtMaxBin = rBins.zeroFuelBins_minKWhpm + deltaKWhpm*(NumDivUni-1) + 0.5f*deltaKWhpm;
			
			float curGHGperMile = kWhpmAtMaxBin*eqGridGHG;
			if (maxGHGperMile < curGHGperMile) maxGHGperMile = curGHGperMile;
		}
		if (rBins.zeroElectBins_wtMiles != null) {
			float deltaFuelPerMile = (rBins.zeroElectBins_maxFuelperMile - rBins.zeroElectBins_minFuelperMile)/(float)NumDivUni;
			float fuelPerMileAtMaxBin = rBins.zeroElectBins_minFuelperMile + deltaFuelPerMile*(NumDivUni-1) + 0.5f*deltaFuelPerMile;
			
			float curGHGperMile = fuelPerMileAtMaxBin*ghgPerUnitFuel;
			if (maxGHGperMile < curGHGperMile) maxGHGperMile = curGHGperMile;
		}
		if (rBins.mixedBins_wtMiles != null) {
			float deltaKWhpm = rBins.mixedBins_maxKWhpm/(float)NumDivBi;
			float deltaFuelPerMile = rBins.mixedBins_maxFuelPerMile/(float)NumDivBi;
			float kWhpmAtMaxBin = deltaKWhpm*(NumDivBi-1) + 0.5f*deltaKWhpm;
			float fuelPerMileAtMaxBin = deltaFuelPerMile*(NumDivBi-1) + 0.5f*deltaFuelPerMile;

			float curGHGperMile = kWhpmAtMaxBin*eqGridGHG + fuelPerMileAtMaxBin*ghgPerUnitFuel;
			if (maxGHGperMile < curGHGperMile) maxGHGperMile = curGHGperMile;
		}		
		
		return maxGHGperMile;
	}
	
	public static FEcoSummayPHEV weighedSummary(FEcoSummayPHEV vCase1, float wt1, FEcoSummayPHEV vCase2, float wt2) {
		FEcoSummayPHEV fes = new FEcoSummayPHEV();
		
		float totFuel = vCase1.allMilesAvFuelperMile * vCase1.totWtMiles * wt1 + vCase2.allMilesAvFuelperMile * vCase2.totWtMiles * wt2;
		float totKWh = vCase1.allMilesAvKWHperMile * vCase1.totWtMiles * wt1 + vCase2.allMilesAvKWHperMile * vCase2.totWtMiles * wt2;
		
		fes.totWtMiles = vCase1.totWtMiles * wt1 + vCase2.totWtMiles * wt2;
		fes.allMilesAvFuelperMile = totFuel / fes.totWtMiles;
		fes.allMilesAvKWHperMile = totKWh / fes.totWtMiles;

		return fes;
	}
	
	public static FEcoSummayPHEV processSimFile(CompactTripSummaries.CTripSummary[] cTrips, String fname) {
		try {
			FEcoSummayPHEV fes = new FEcoSummayPHEV();
			
			float[][] matArray = MCFloatValuesFileWHeader.readFileValues(fname);
			float[] fuelAmounts, kWhAmounts;
			if (matArray.length < 2) {
				fuelAmounts = matArray[0];
				kWhAmounts = null;
			} else {
				kWhAmounts = matArray[0];
				fuelAmounts = matArray[1];
			}			
			if (fuelAmounts.length != cTrips.length) return null;

			CTrip[] allTrips = new CTrip[fuelAmounts.length];
			
			float totWtFuel = 0f;
			float totWtKWh = 0f;
			float totWtMi = 0f;
			
			for (int i=0; i<allTrips.length; i++) {
				float miles = cTrips[i].miles;
				float fuelAmount = fuelAmounts[i];
				float kWhAmount = 0f;
				if (kWhAmounts!= null) kWhAmount = kWhAmounts[i];
				float hhWt = cTrips[i].hhWt;
				
				totWtFuel += fuelAmount*hhWt;
				totWtKWh += kWhAmount*hhWt;
				totWtMi += miles*hhWt;

				allTrips[i] = new CTrip();
				allTrips[i].fuelPerMile = fuelAmount/miles;
				allTrips[i].kWhPerMile = kWhAmount/miles;
				allTrips[i].wtMiles = hhWt*miles;
			}
			
			if (allTrips.length <= MaxNumTripsWTripMode) {
				fes.tripsMode = true;
				fes.rTrips = allTrips;
				
				fes.allMilesAvFuelperMile = totWtFuel/totWtMi;
				fes.allMilesAvKWHperMile = totWtKWh/totWtMi;
				fes.totWtMiles = totWtMi;
				
				return fes;
			}
			
			fes.tripsMode = false;
			fes.rBins = new TBins(allTrips);
			
			totWtFuel = 0f;
			totWtKWh = 0f;
			totWtMi = 0f;
			
			for (int i=0; i<allTrips.length; i++) {
				float miles = cTrips[i].miles;
				float fuelAmount = fuelAmounts[i];
				float kWhAmount = 0f;
				if (kWhAmounts!= null) kWhAmount = kWhAmounts[i];
				float hhWt = cTrips[i].hhWt;
				
				float kWhpm = kWhAmount/miles;
				float fuelPerMi = fuelAmount/miles;

				boolean isExcluded = false;
				
				if ((fes.rBins.zeroElectBins_wtMiles != null)&&(kWhpm < ZTol)) {
					if (fuelPerMi < fes.rBins.zeroElectBins_minFuelperMile) isExcluded = true;
					if (fuelPerMi > fes.rBins.zeroElectBins_maxFuelperMile) isExcluded = true;
				}
				if ((fes.rBins.zeroFuelBins_wtMiles != null)&&(fuelPerMi < ZTol)) {
					if (kWhpm < fes.rBins.zeroFuelBins_minKWhpm) isExcluded = true;
					if (kWhpm > fes.rBins.zeroFuelBins_maxKWhpm) isExcluded = true;
				}
				if (fes.rBins.mixedBins_wtMiles != null) {
					if (fuelPerMi > fes.rBins.mixedBins_maxFuelPerMile) isExcluded = true;
					if (kWhpm > fes.rBins.mixedBins_maxKWhpm) isExcluded = true;
				}
				
				if (!isExcluded) {
					totWtFuel += fuelAmount*hhWt;
					totWtKWh += kWhAmount*hhWt;
					totWtMi += miles*hhWt;
				}
			}
			
			fes.allMilesAvFuelperMile = totWtFuel/totWtMi;
			fes.allMilesAvKWHperMile = totWtKWh/totWtMi;
			fes.totWtMiles = totWtMi;

			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static FEcoSummayPHEV readSummaryOnly(String fname) {
		try {
			FEcoSummayPHEV fes = new FEcoSummayPHEV();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.allMilesAvFuelperMile = Float.parseFloat(sp[0]);
			fes.allMilesAvKWHperMile = Float.parseFloat(sp[1]);
			fes.totWtMiles = Float.parseFloat(sp[2]);
			
			fin.close();
			
			return fes;
		} catch (Exception e) {
			return null;
		}
	}
	public static FEcoSummayPHEV readFromFile(String fname) {
		try {
			FEcoSummayPHEV fes = new FEcoSummayPHEV();

			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine = fin.readLine();
			
			readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			fes.allMilesAvFuelperMile = Float.parseFloat(sp[0]);
			fes.allMilesAvKWHperMile = Float.parseFloat(sp[1]);
			fes.totWtMiles = Float.parseFloat(sp[2]);
			
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
	
	public void writeToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("allMilesAvFuelperMile,allMilesAvKWHperMile,totWtMiles"+lsep);
			fout.append(""+allMilesAvFuelperMile+","+allMilesAvKWHperMile+","+totWtMiles+lsep);

			try {
				if (tripsMode) {
					fout.append(TripsModeHeader+lsep);

					for (int i=0; i<rTrips.length; i++) {
						fout.append(rTrips[i].toString()+lsep);
					}
	
				} else {
					fout.append("_histogramMode"+lsep);					
					fout.append(""+(rBins.zeroFuelBins_wtMiles != null)+","+(rBins.zeroElectBins_wtMiles != null)+","+(rBins.mixedBins_wtMiles != null)+lsep);
					
					if (rBins.zeroFuelBins_wtMiles != null) {
						fout.append("__zeroFuelBins_wtMiles"+lsep);					
						fout.append(""+rBins.zeroFuelBins_minKWhpm+","+rBins.zeroFuelBins_maxKWhpm+lsep);					

						for (int i=0; i<NumDivUni; i++) {
							fout.append(""+rBins.zeroFuelBins_wtMiles[i]+lsep);
						}
					}
					if (rBins.zeroElectBins_wtMiles != null) {
						fout.append("__zeroElectBins_wtMiles"+lsep);					
						fout.append(""+rBins.zeroElectBins_minFuelperMile+","+rBins.zeroElectBins_maxFuelperMile+lsep);					

						for (int i=0; i<NumDivUni; i++) {
							fout.append(""+rBins.zeroElectBins_wtMiles[i]+lsep);
						}
					}
					if (rBins.mixedBins_wtMiles != null) {
						fout.append("__mixedBins_wtMiles"+lsep);					
						fout.append(""+rBins.mixedBins_maxFuelPerMile+","+rBins.mixedBins_maxKWhpm+lsep);					

						for (int i=0; i<NumDivBi; i++) {
							String st = ""+rBins.mixedBins_wtMiles[i][0];
							for (int j=1; j<NumDivBi; j++) st = st + "," + rBins.mixedBins_wtMiles[i][j];
							fout.append(st+lsep);					
						}
					}
				}
			} catch (Exception e) {}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private static class CTrip {
		private float fuelPerMile,kWhPerMile,wtMiles;
		private CTrip() {}
		private CTrip(String readLine) {
			String[] sp = readLine.split(",");
			fuelPerMile = Float.parseFloat(sp[0]);
			kWhPerMile = Float.parseFloat(sp[1]);
			wtMiles = Float.parseFloat(sp[2]);
		}
		@Override public String toString() {return ""+fuelPerMile+","+kWhPerMile+","+wtMiles;}
	}

	private static class TBins {
		private float zeroFuelBins_minKWhpm, zeroFuelBins_maxKWhpm, zeroElectBins_minFuelperMile, zeroElectBins_maxFuelperMile;
		private float mixedBins_maxFuelPerMile, mixedBins_maxKWhpm;
		private float[] zeroFuelBins_wtMiles, zeroElectBins_wtMiles;
		private float[][] mixedBins_wtMiles;	//First index on fuel per mile, second index on kWh/mile
		
		private TBins(BufferedReader fin) throws Exception {
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			boolean zeroFuelHstExists = Boolean.parseBoolean(sp[0]);
			boolean zeroElectHstExists = Boolean.parseBoolean(sp[1]);
			boolean mixedHstExists = Boolean.parseBoolean(sp[2]);
			
			if (zeroFuelHstExists) {
				readLine = fin.readLine();
				readLine = fin.readLine();
				sp = readLine.split(",");
				
				zeroFuelBins_minKWhpm = Float.parseFloat(sp[0]);
				zeroFuelBins_maxKWhpm = Float.parseFloat(sp[1]);
				zeroFuelBins_wtMiles = new float[NumDivUni];
				
				for (int i=0; i<NumDivUni; i++) {
					readLine = fin.readLine();
					zeroFuelBins_wtMiles[i] = Float.parseFloat(readLine);
				}
			}
			if (zeroElectHstExists) {
				readLine = fin.readLine();
				readLine = fin.readLine();
				sp = readLine.split(",");
				
				zeroElectBins_minFuelperMile = Float.parseFloat(sp[0]);
				zeroElectBins_maxFuelperMile = Float.parseFloat(sp[1]);
				zeroElectBins_wtMiles = new float[NumDivUni];
				
				for (int i=0; i<NumDivUni; i++) {
					readLine = fin.readLine();
					zeroElectBins_wtMiles[i] = Float.parseFloat(readLine);
				}
			}
			
			if (mixedHstExists) {
				readLine = fin.readLine();
				readLine = fin.readLine();
				sp = readLine.split(",");
				
				mixedBins_maxFuelPerMile = Float.parseFloat(sp[0]);
				mixedBins_maxKWhpm = Float.parseFloat(sp[1]);
				mixedBins_wtMiles = new float[NumDivBi][NumDivBi];
				
				for (int i=0; i<NumDivBi; i++) {
					readLine = fin.readLine();
					sp = readLine.split(",");

					for (int j=0; j<NumDivBi; j++) {
						mixedBins_wtMiles[i][j] = Float.parseFloat(sp[j]);
					}
				}
			}
		}
		private TBins (CTrip[] allTrips) {
			//Quick-scan
			int numZeroFuel = 0;
			int numZeroElect = 0;
			
			zeroFuelBins_minKWhpm = Float.MAX_VALUE;
			zeroFuelBins_maxKWhpm = 0f;
			zeroElectBins_minFuelperMile = Float.MAX_VALUE;
			zeroElectBins_maxFuelperMile = 0f;
			mixedBins_maxFuelPerMile = 0f;
			mixedBins_maxKWhpm = 0f;

			for (int i=0; i<allTrips.length; i++) {
				float fuelPerMi = allTrips[i].fuelPerMile;
				float kWhpm = allTrips[i].kWhPerMile;
				
				if (fuelPerMi < ZTol) {
					numZeroFuel++;
					if (zeroFuelBins_minKWhpm > kWhpm) zeroFuelBins_minKWhpm = kWhpm;
					if (zeroFuelBins_maxKWhpm < kWhpm) zeroFuelBins_maxKWhpm = kWhpm;
				}
				else if (kWhpm < ZTol) {
					numZeroElect++;
					if (zeroElectBins_minFuelperMile > fuelPerMi) zeroElectBins_minFuelperMile = fuelPerMi;
					if (zeroElectBins_maxFuelperMile < fuelPerMi) zeroElectBins_maxFuelperMile = fuelPerMi;
				} else {
					if (mixedBins_maxFuelPerMile < fuelPerMi) mixedBins_maxFuelPerMile = fuelPerMi;
					if (mixedBins_maxKWhpm < kWhpm) mixedBins_maxKWhpm = kWhpm;
				}
			}
			
			zeroFuelBins_minKWhpm = Math.min(zeroFuelBins_minKWhpm, zeroFuelBins_maxKWhpm);
			zeroElectBins_minFuelperMile = Math.min(zeroElectBins_minFuelperMile, zeroElectBins_maxFuelperMile);
			
			int numMixed = allTrips.length - numZeroFuel - numZeroElect;
			
			if (numZeroFuel > 0) zeroFuelBins_wtMiles = new float[NumDivUni];
			if (numZeroElect > 0) zeroElectBins_wtMiles = new float[NumDivUni];
			if (numMixed > 0) mixedBins_wtMiles = new float[NumDivBi][NumDivBi];
			
			if (numZeroFuel > MinHistTripsToChopOutliers) {
				//Re-adjust limits to chop outliers
					//Ensure non-overlapping limits
				if (zeroFuelBins_maxKWhpm == zeroFuelBins_minKWhpm) {
					zeroFuelBins_maxKWhpm *= (1f + 10f*ZTol);
				}
				if (zeroFuelBins_maxKWhpm == zeroFuelBins_minKWhpm) zeroFuelBins_maxKWhpm += 2f*ZTol;
				
				//Line search lower end
				float deltaBin = (zeroFuelBins_maxKWhpm - zeroFuelBins_minKWhpm)/(float)NumDivUni;
				float deltaTol = (zeroFuelBins_maxKWhpm - zeroFuelBins_minKWhpm)*CDF_cutOffTolRelToRange;
				float lb = zeroFuelBins_minKWhpm;
				float ub = lb + deltaBin;
				float targetCDFAbove = 1f - CDF_lowCutOffUni;
				float cdfAboveUB = cdfAboveKWh_forZeroFuel(allTrips, ub);
				
				while (cdfAboveUB > targetCDFAbove) {
					ub += deltaBin;
					cdfAboveUB = cdfAboveKWh_forZeroFuel(allTrips, ub);
				}
				
					//Bisection lower end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveKWh_forZeroFuel(allTrips, tPos);

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
				
				zeroFuelBins_minKWhpm = lb;
				
					//Line search higher end
				ub = zeroFuelBins_maxKWhpm;
				lb = ub - deltaBin;
				targetCDFAbove = 1f - CDF_highCutOffUni;
				float cdfAboveLB = cdfAboveKWh_forZeroFuel(allTrips, lb);
				
				while (cdfAboveLB < targetCDFAbove) {
					lb += -deltaBin;
					cdfAboveLB = cdfAboveKWh_forZeroFuel(allTrips, lb);
				}
				
					//Bisection higher end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveKWh_forZeroFuel(allTrips, tPos);

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
				
				zeroFuelBins_maxKWhpm = ub;
			}
			if (numZeroElect > MinHistTripsToChopOutliers) {
				//Re-adjust limits to chop outliers
					//Ensure non-overlapping limits
				if (zeroElectBins_maxFuelperMile == zeroElectBins_minFuelperMile) {
					zeroElectBins_maxFuelperMile *= (1f + 10f*ZTol);
				}
				if (zeroElectBins_maxFuelperMile == zeroElectBins_minFuelperMile) zeroElectBins_maxFuelperMile += 2f*ZTol;
				
				//Line search lower end
				float deltaBin = (zeroElectBins_maxFuelperMile - zeroElectBins_minFuelperMile)/(float)NumDivUni;
				float deltaTol = (zeroElectBins_maxFuelperMile - zeroElectBins_minFuelperMile)*CDF_cutOffTolRelToRange;
				float lb = zeroElectBins_minFuelperMile;
				float ub = lb + deltaBin;
				float targetCDFAbove = 1f - CDF_lowCutOffUni;
				float cdfAboveUB = cdfAboveFuelPerMile_forZeroElect(allTrips, ub);
				
				while (cdfAboveUB > targetCDFAbove) {
					ub += deltaBin;
					cdfAboveUB = cdfAboveFuelPerMile_forZeroElect(allTrips, ub);
				}
				
					//Bisection lower end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveFuelPerMile_forZeroElect(allTrips, tPos);
	
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
				
				zeroElectBins_minFuelperMile = lb;
				
					//Line search higher end
				ub = zeroElectBins_maxFuelperMile;
				lb = ub - deltaBin;
				targetCDFAbove = 1f - CDF_highCutOffUni;
				float cdfAboveLB = cdfAboveFuelPerMile_forZeroElect(allTrips, lb);
				
				while (cdfAboveLB < targetCDFAbove) {
					lb += -deltaBin;
					cdfAboveLB = cdfAboveFuelPerMile_forZeroElect(allTrips, lb);
				}
				
					//Bisection higher end
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveFuelPerMile_forZeroElect(allTrips, tPos);
	
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
				
				zeroElectBins_maxFuelperMile = ub;
			}
			if (numMixed > MinHistTripsToChopOutliers) {
				//Re-adjust limits to chop outliers
				//Line-search fuel
				float deltaBin = mixedBins_maxFuelPerMile/(float)NumDivBi;
				float deltaTol = mixedBins_maxFuelPerMile*CDF_cutOffTolRelToRange;
				float ub = mixedBins_maxFuelPerMile;
				float lb = ub - deltaBin;
				float targetCDFAbove = 1f - CDF_highCutOffBi;
				float cdfAboveLB = cdfAboveFuelPerMile_forMixedMode(allTrips, lb);
				
				while (cdfAboveLB < targetCDFAbove) {
					lb += -deltaBin;
					cdfAboveLB = cdfAboveFuelPerMile_forMixedMode(allTrips, lb);
				}
				
				//Bisection fuel
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveFuelPerMile_forMixedMode(allTrips, tPos);
	
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
				
				mixedBins_maxFuelPerMile = ub;
				
				//Line-search electricity
				deltaBin = mixedBins_maxKWhpm/(float)NumDivBi;
				deltaTol = mixedBins_maxKWhpm*CDF_cutOffTolRelToRange;
				ub = mixedBins_maxKWhpm;
				lb = ub - deltaBin;
				targetCDFAbove = 1f - CDF_highCutOffBi;
				cdfAboveLB = cdfAboveKWh_forMixedMode(allTrips, lb);
				
				while (cdfAboveLB < targetCDFAbove) {
					lb += -deltaBin;
					cdfAboveLB = cdfAboveKWh_forMixedMode(allTrips, lb);
				}

				//Bisection Electricity
				while ((ub - lb) > deltaTol) {
					float tPos  = 0.5f*(ub + lb);
					float cdfAboveTPos = cdfAboveKWh_forMixedMode(allTrips, tPos);
	
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
				
				mixedBins_maxKWhpm = ub;
			}
			
			//Place trip weighed miles in respective bins			
			for (int i=0; i<allTrips.length; i++) {
				float fuelPerMi = allTrips[i].fuelPerMile;
				float kWhpm = allTrips[i].kWhPerMile;
				float wtMiles = allTrips[i].wtMiles;
				
				if (fuelPerMi < ZTol) {
					if ((zeroFuelBins_maxKWhpm - zeroFuelBins_minKWhpm) < ZTol) {
						zeroFuelBins_wtMiles[0] += wtMiles;
					} else {
						int binID = (int)(((kWhpm - zeroFuelBins_minKWhpm)/(zeroFuelBins_maxKWhpm - zeroFuelBins_minKWhpm))*NumDivUni);
						if ((binID >= 0)&&(binID<NumDivUni)) zeroFuelBins_wtMiles[binID] += wtMiles;
					}
				} else if (kWhpm < ZTol) {
					if ((zeroElectBins_maxFuelperMile - zeroElectBins_minFuelperMile) < ZTol) {
						zeroElectBins_wtMiles[0] += wtMiles;
					} else {
						int binID = (int)(((fuelPerMi - zeroElectBins_minFuelperMile)/(zeroElectBins_maxFuelperMile - zeroElectBins_minFuelperMile))*NumDivUni);
						if ((binID >= 0)&&(binID<NumDivUni)) zeroElectBins_wtMiles[binID] += wtMiles;
					}
				} else {
					int iBinID = (int)((fuelPerMi/mixedBins_maxFuelPerMile)*NumDivBi);
					int jBinID = (int)((kWhpm/mixedBins_maxKWhpm)*NumDivBi);
					
					if ((iBinID < NumDivBi)&&(jBinID < NumDivBi)) mixedBins_wtMiles[iBinID][jBinID] += wtMiles;
				}
			}
		}
	}
	
	private static float cdfAboveKWh_forZeroFuel(CTrip[] allTrips, float kWhValue) {
		float wtMilesZeroFuel = 0f;
		float wtMilesZeroFuelAboveKWhValue = 0f;
		
		for (int i=0; i<allTrips.length; i++) {
			float fuelPerMi = allTrips[i].fuelPerMile;
			float kWhpm = allTrips[i].kWhPerMile;
			float wtMiles = allTrips[i].wtMiles;
			
			if (fuelPerMi < ZTol) {
				wtMilesZeroFuel += wtMiles;
				if (kWhpm >= kWhValue) wtMilesZeroFuelAboveKWhValue += wtMiles;
			}
		}
		
		return wtMilesZeroFuelAboveKWhValue/wtMilesZeroFuel;
	}
	private static float cdfAboveFuelPerMile_forZeroElect(CTrip[] allTrips, float fuelPerMile) {
		float wtMilesZeroElect = 0f;
		float wtMilesAboveValue = 0f;
		
		for (int i=0; i<allTrips.length; i++) {
			float fuelPerMi = allTrips[i].fuelPerMile;
			float kWhpm = allTrips[i].kWhPerMile;
			float wtMiles = allTrips[i].wtMiles;
			
			if (kWhpm < ZTol) {
				wtMilesZeroElect += wtMiles;
				if (fuelPerMi >= fuelPerMile) wtMilesAboveValue += wtMiles;
			}
		}
		
		return wtMilesAboveValue/wtMilesZeroElect;
	}
	private static float cdfAboveKWh_forMixedMode(CTrip[] allTrips, float kWhValue) {
		float wtMilesMixedMode = 0f;
		float wtMilesAboveValue = 0f;
		
		for (int i=0; i<allTrips.length; i++) {
			float fuelPerMi = allTrips[i].fuelPerMile;
			float kWhpm = allTrips[i].kWhPerMile;
			float wtMiles = allTrips[i].wtMiles;
			
			if ((kWhpm >= ZTol)&&(fuelPerMi >= ZTol)) {
				wtMilesMixedMode += wtMiles;
				if (kWhpm >= kWhValue) wtMilesAboveValue += wtMiles;
			}
		}
		
		return wtMilesAboveValue/wtMilesMixedMode;
	}
	private static float cdfAboveFuelPerMile_forMixedMode(CTrip[] allTrips, float fuelPerMile) {
		float wtMilesMixedMode = 0f;
		float wtMilesAboveValue = 0f;
		
		for (int i=0; i<allTrips.length; i++) {
			float fuelPerMi = allTrips[i].fuelPerMile;
			float kWhpm = allTrips[i].kWhPerMile;
			float wtMiles = allTrips[i].wtMiles;
			
			if ((kWhpm >= ZTol)&&(fuelPerMi >= ZTol)) {
				wtMilesMixedMode += wtMiles;
				if (fuelPerMi >= fuelPerMile) wtMilesAboveValue += wtMiles;
			}
		}
		
		return wtMilesAboveValue/wtMilesMixedMode;
	}
}
