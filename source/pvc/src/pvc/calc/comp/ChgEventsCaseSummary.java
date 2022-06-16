package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import pvc.datamgmt.comp.ChargerTypes;
import pvc.datamgmt.comp.HourlyProfileCurve;

public class ChgEventsCaseSummary {
	public static final int HoursPerDay = 24;
	private static final float ZTol = 0.001f;
	
	private CEHistogram hstMinCost, hstMinGHG;
	public CEHistogram getTimingInterpolatedHistogram(float fracMinGHG) {
		return interpolateBetween(hstMinCost, hstMinGHG, 1f-fracMinGHG, fracMinGHG);
	}
	public static CEHistogram interpolateBetween(CEHistogram h1, CEHistogram h2, float wt1, float wt2) {
		return new CEHistogram(h1, h2, wt1, wt2);
	}
		
	private ChgEventsCaseSummary() {
		hstMinCost = new CEHistogram();
		hstMinGHG = new CEHistogram();
	}
	
	public static ChgEventsCaseSummary processChargingData(ChargingEvents.ChargingEvent[] chgEvents, HourlyProfileCurve costProfile, HourlyProfileCurve ghgProfile) {
		try {
			ChgEventsCaseSummary chgSummary = new ChgEventsCaseSummary();
			chgSummary.hstMinCost.chgL1.hourlyTotKWh = new float[HoursPerDay];
			chgSummary.hstMinCost.chgL2.hourlyTotKWh = new float[HoursPerDay];
			chgSummary.hstMinCost.chgDCFast.hourlyTotKWh = new float[HoursPerDay];
			chgSummary.hstMinGHG.chgL1.hourlyTotKWh = new float[HoursPerDay];
			chgSummary.hstMinGHG.chgL2.hourlyTotKWh = new float[HoursPerDay];
			chgSummary.hstMinGHG.chgDCFast.hourlyTotKWh = new float[HoursPerDay];

			for (int i=0; i<chgEvents.length; i++) {
				float h24Start = chgEvents[i].h24MinStart;
				float hDuration = chgEvents[i].hrsDuration;
				float hMaxDelay = chgEvents[i].hrsSlack;
				float kWhAmount = chgEvents[i].hhWt * chgEvents[i].kWhAmountToBattery;
				
				if ((hDuration >= ZTol) && (kWhAmount >= ZTol)) {
					HourlyProfileCurve.OptDelayResult minCost_optDelayRes = costProfile.optimizeDelayForMinAvProfileValue(h24Start, hDuration, hMaxDelay);
					float ghgChgEventProfile_for_minCost = ghgProfile.calcEventProfileAverage(h24Start + minCost_optDelayRes.hoursDelay, hDuration);
					
					HourlyProfileCurve.OptDelayResult minGHG_optDelayRes = ghgProfile.optimizeDelayForMinAvProfileValue(h24Start, hDuration, hMaxDelay);
					float costChgEventProfile_for_minGHG = costProfile.calcEventProfileAverage(h24Start + minGHG_optDelayRes.hoursDelay, hDuration);
					
					float adjTotKWh, wtOld, wtNew;					
					switch (chgEvents[i].chgEventType) {
					case dayTime_DCFast:
						addChgEventToHistogram(chgSummary.hstMinCost.chgDCFast.hourlyTotKWh, kWhAmount, h24Start + minCost_optDelayRes.hoursDelay, hDuration);
						addChgEventToHistogram(chgSummary.hstMinGHG.chgDCFast.hourlyTotKWh, kWhAmount, h24Start + minGHG_optDelayRes.hoursDelay, hDuration);
						
						adjTotKWh = chgSummary.hstMinCost.chgDCFast.totKWh + kWhAmount;
						wtOld = chgSummary.hstMinCost.chgDCFast.totKWh/adjTotKWh;
						wtNew = kWhAmount/adjTotKWh;
						
						chgSummary.hstMinCost.chgDCFast.totKWh = adjTotKWh;
						chgSummary.hstMinGHG.chgDCFast.totKWh = adjTotKWh;
						
						chgSummary.hstMinCost.chgDCFast.priceMod = wtOld * chgSummary.hstMinCost.chgDCFast.priceMod + wtNew * minCost_optDelayRes.eventMinAvProfile;
						chgSummary.hstMinCost.chgDCFast.ghgMod = wtOld * chgSummary.hstMinCost.chgDCFast.ghgMod + wtNew * ghgChgEventProfile_for_minCost;

						chgSummary.hstMinGHG.chgDCFast.priceMod = wtOld * chgSummary.hstMinGHG.chgDCFast.priceMod + wtNew * costChgEventProfile_for_minGHG;
						chgSummary.hstMinGHG.chgDCFast.ghgMod = wtOld * chgSummary.hstMinGHG.chgDCFast.ghgMod + wtNew * minGHG_optDelayRes.eventMinAvProfile;
						break;
					case overNight_L2:
					case dayTime_L2:
						addChgEventToHistogram(chgSummary.hstMinCost.chgL2.hourlyTotKWh, kWhAmount, h24Start + minCost_optDelayRes.hoursDelay, hDuration);
						addChgEventToHistogram(chgSummary.hstMinGHG.chgL2.hourlyTotKWh, kWhAmount, h24Start + minGHG_optDelayRes.hoursDelay, hDuration);
						
						adjTotKWh = chgSummary.hstMinCost.chgL2.totKWh + kWhAmount;
						wtOld = chgSummary.hstMinCost.chgL2.totKWh/adjTotKWh;
						wtNew = kWhAmount/adjTotKWh;
						
						chgSummary.hstMinCost.chgL2.totKWh = adjTotKWh;
						chgSummary.hstMinGHG.chgL2.totKWh = adjTotKWh;
						
						chgSummary.hstMinCost.chgL2.priceMod = wtOld * chgSummary.hstMinCost.chgL2.priceMod + wtNew * minCost_optDelayRes.eventMinAvProfile;
						chgSummary.hstMinCost.chgL2.ghgMod = wtOld * chgSummary.hstMinCost.chgL2.ghgMod + wtNew * ghgChgEventProfile_for_minCost;

						chgSummary.hstMinGHG.chgL2.priceMod = wtOld * chgSummary.hstMinGHG.chgL2.priceMod + wtNew * costChgEventProfile_for_minGHG;
						chgSummary.hstMinGHG.chgL2.ghgMod = wtOld * chgSummary.hstMinGHG.chgL2.ghgMod + wtNew * minGHG_optDelayRes.eventMinAvProfile;
						break;
					case overNight_L1:
						addChgEventToHistogram(chgSummary.hstMinCost.chgL1.hourlyTotKWh, kWhAmount, h24Start + minCost_optDelayRes.hoursDelay, hDuration);
						addChgEventToHistogram(chgSummary.hstMinGHG.chgL1.hourlyTotKWh, kWhAmount, h24Start + minGHG_optDelayRes.hoursDelay, hDuration);
						
						adjTotKWh = chgSummary.hstMinCost.chgL1.totKWh + kWhAmount;
						wtOld = chgSummary.hstMinCost.chgL1.totKWh/adjTotKWh;
						wtNew = kWhAmount/adjTotKWh;
						
						chgSummary.hstMinCost.chgL1.totKWh = adjTotKWh;
						chgSummary.hstMinGHG.chgL1.totKWh = adjTotKWh;
						
						chgSummary.hstMinCost.chgL1.priceMod = wtOld * chgSummary.hstMinCost.chgL1.priceMod + wtNew * minCost_optDelayRes.eventMinAvProfile;
						chgSummary.hstMinCost.chgL1.ghgMod = wtOld * chgSummary.hstMinCost.chgL1.ghgMod + wtNew * ghgChgEventProfile_for_minCost;

						chgSummary.hstMinGHG.chgL1.priceMod = wtOld * chgSummary.hstMinGHG.chgL1.priceMod + wtNew * costChgEventProfile_for_minGHG;
						chgSummary.hstMinGHG.chgL1.ghgMod = wtOld * chgSummary.hstMinGHG.chgL1.ghgMod + wtNew * minGHG_optDelayRes.eventMinAvProfile;
						break;
					}
				}
			}

			return chgSummary;
		} catch (Exception e) {
			return null;
		}
	}
	private static void addChgEventToHistogram(float[] hourlyAmts, float kWhAmount, float h24Start, float hDuration) {
		float amountPerHour = kWhAmount/hDuration;
		float remDuration = hDuration;
		
		float hStart = h24Start;
		while (hStart > HoursPerDay) {
			hStart += -HoursPerDay;
		}
		
		while (remDuration > HoursPerDay) {
			for (int i=0; i<HoursPerDay; i++) hourlyAmts[i] += amountPerHour;
			remDuration += -HoursPerDay;
		}
		
		int prevH = (int)hStart;
		int nextH = prevH + 1;
		float remTimeInCurHour = nextH - hStart;
		
		if (remDuration <= remTimeInCurHour) {
			hourlyAmts[prevH] += remDuration*amountPerHour;
			return;
		}
		
		hourlyAmts[prevH] += remTimeInCurHour*amountPerHour;
		remDuration += -remTimeInCurHour;
		
		while (remDuration > 1) {
			if (nextH >= HoursPerDay) nextH = 0;
			prevH = nextH;
			nextH++;
			
			hourlyAmts[prevH] += amountPerHour;
			remDuration += -1;
		}
		
		if (nextH >= HoursPerDay) nextH = 0;
		hourlyAmts[nextH] += remDuration*amountPerHour;
	}
	
	public static ChgEventsCaseSummary readSummaryOnly(String fname) {
		ChgEventsCaseSummary chgSummary = new ChgEventsCaseSummary();
		
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));

			try {
				String readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				String[] sp = readLine.split(",");
				chgSummary.hstMinCost.chgL1.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgL1.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgL1.ghgMod = Float.parseFloat(sp[3]);

				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinCost.chgL2.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgL2.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgL2.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinCost.chgDCFast.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgDCFast.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgDCFast.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgL1.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgL1.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgL1.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgL2.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgL2.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgL2.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgDCFast.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgDCFast.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgDCFast.ghgMod = Float.parseFloat(sp[3]);
				
			} catch (Exception e) {
				fin.close();
				return chgSummary;
			}
			
			fin.close();
		} catch (Exception e) {}
		
		return chgSummary;
	}	
	public static ChgEventsCaseSummary readFromFile(String fname) {
		ChgEventsCaseSummary chgSummary = new ChgEventsCaseSummary();
		
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));

			try {
				String readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				String[] sp = readLine.split(",");
				chgSummary.hstMinCost.chgL1.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgL1.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgL1.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinCost.chgL2.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgL2.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgL2.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinCost.chgDCFast.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinCost.chgDCFast.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinCost.chgDCFast.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgL1.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgL1.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgL1.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgL2.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgL2.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgL2.ghgMod = Float.parseFloat(sp[3]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				chgSummary.hstMinGHG.chgDCFast.totKWh = Float.parseFloat(sp[1]);
				chgSummary.hstMinGHG.chgDCFast.priceMod = Float.parseFloat(sp[2]);
				chgSummary.hstMinGHG.chgDCFast.ghgMod = Float.parseFloat(sp[3]);
				
				chgSummary.hstMinCost.chgL1.hourlyTotKWh = new float[HoursPerDay];
				chgSummary.hstMinCost.chgL2.hourlyTotKWh = new float[HoursPerDay];
				chgSummary.hstMinCost.chgDCFast.hourlyTotKWh = new float[HoursPerDay];
				chgSummary.hstMinGHG.chgL1.hourlyTotKWh = new float[HoursPerDay];
				chgSummary.hstMinGHG.chgL2.hourlyTotKWh = new float[HoursPerDay];
				chgSummary.hstMinGHG.chgDCFast.hourlyTotKWh = new float[HoursPerDay];
								
				readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinCost.chgL1.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinCost.chgL2.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinCost.chgDCFast.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);

				readLine = fin.readLine();
				readLine = fin.readLine();
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinGHG.chgL1.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinGHG.chgL2.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);
				
				readLine = fin.readLine();
				sp = readLine.split(",");
				for (int i=0; i<HoursPerDay; i++) chgSummary.hstMinGHG.chgDCFast.hourlyTotKWh[i] = Float.parseFloat(sp[i+1]);

			} catch (Exception e) {
				fin.close();
				
				chgSummary.hstMinCost.chgL1.hourlyTotKWh = null;
				chgSummary.hstMinCost.chgL2.hourlyTotKWh = null;
				chgSummary.hstMinCost.chgDCFast.hourlyTotKWh = null;
				chgSummary.hstMinGHG.chgL1.hourlyTotKWh = null;
				chgSummary.hstMinGHG.chgL2.hourlyTotKWh = null;
				chgSummary.hstMinGHG.chgDCFast.hourlyTotKWh = null;
				
				return chgSummary;
			}
			
			fin.close();
		} catch (Exception e) {}
		
		return chgSummary;
	}	
	public void writeToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");

			fout.append("_minCost_Summary"+lsep);
			fout.append(",totKWh,priceMod,ghgMod"+lsep);		
			fout.append("chgL1,"+hstMinCost.chgL1.totKWh+","+hstMinCost.chgL1.priceMod+","+hstMinCost.chgL1.ghgMod+lsep);
			fout.append("chgL2,"+hstMinCost.chgL2.totKWh+","+hstMinCost.chgL2.priceMod+","+hstMinCost.chgL2.ghgMod+lsep);
			fout.append("chgDCFast,"+hstMinCost.chgDCFast.totKWh+","+hstMinCost.chgDCFast.priceMod+","+hstMinCost.chgDCFast.ghgMod+lsep);

			fout.append("_minGHG_Summary"+lsep);
			fout.append(",totKWh,priceMod,ghgMod"+lsep);		
			fout.append("chgL1,"+hstMinGHG.chgL1.totKWh+","+hstMinGHG.chgL1.priceMod+","+hstMinGHG.chgL1.ghgMod+lsep);
			fout.append("chgL2,"+hstMinGHG.chgL2.totKWh+","+hstMinGHG.chgL2.priceMod+","+hstMinGHG.chgL2.ghgMod+lsep);
			fout.append("chgDCFast,"+hstMinGHG.chgDCFast.totKWh+","+hstMinGHG.chgDCFast.priceMod+","+hstMinGHG.chgDCFast.ghgMod+lsep);

			if (hstMinCost.chgL1.hourlyTotKWh != null) {
				try {
					fout.append("_minCost_Hourly"+lsep);
					
					String st = "hourInterval";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + i;
					fout.append(st+lsep);
					
					st = "chgAmtKWh_L1";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinCost.chgL1.hourlyTotKWh[i];
					fout.append(st+lsep);
					
					st = "chgAmtKWh_L2";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinCost.chgL2.hourlyTotKWh[i];
					fout.append(st+lsep);
					
					st = "chgAmtKWh_DCFast";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinCost.chgDCFast.hourlyTotKWh[i];
					fout.append(st+lsep);

					fout.append("_minGHG_Hourly"+lsep);
					
					st = "hourInterval";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + i;
					fout.append(st+lsep);
					
					st = "chgAmtKWh_L1";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinGHG.chgL1.hourlyTotKWh[i];
					fout.append(st+lsep);
					
					st = "chgAmtKWh_L2";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinGHG.chgL2.hourlyTotKWh[i];
					fout.append(st+lsep);
					
					st = "chgAmtKWh_DCFast";
					for (int i=0; i<HoursPerDay; i++) st = st + "," + hstMinGHG.chgDCFast.hourlyTotKWh[i];
					fout.append(st+lsep);
				} catch (Exception e) {}
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	public static class CEHistogram {
		private CEHistogramForChgType chgL1, chgL2, chgDCFast;
		
		public float grandTotKWh() {
			return chgL1.totKWh + chgL2.totKWh + chgDCFast.totKWh;
		}
		public float fracKWhDCFast() {
			float grandTotKWh = grandTotKWh();			
			if (grandTotKWh < ZTol) return 0f;			
			return chgDCFast.totKWh/grandTotKWh;
		}
		
		public float avApproxCostMod(float dcFastPricePremium) {
			float kWhL1 = chgL1.totKWh;
			float kWhL2 = chgL2.totKWh;
			float kWhDC = chgDCFast.totKWh;
			
			return (kWhL1*chgL1.priceMod + kWhL2*chgL2.priceMod + kWhDC * chgDCFast.priceMod * (1f+dcFastPricePremium))/(kWhL1+kWhL2+kWhDC);
		}
		public float avApproxGHGMod() {
			float kWhL1 = chgL1.totKWh;
			float kWhL2 = chgL2.totKWh;
			float kWhDC = chgDCFast.totKWh;
			
			return (kWhL1*chgL1.ghgMod + kWhL2*chgL2.ghgMod + kWhDC * chgDCFast.ghgMod)/(kWhL1+kWhL2+kWhDC);
		}
		
		public float eqGHG_perKWh(float avGridGHG_perKWh, float chgEffL1, float chgEffL2, float chgEffDCFast) {
			float grandTotKWh = grandTotKWh();
			if (grandTotKWh < ZTol) return avGridGHG_perKWh;
			
			float ghgL1 = (avGridGHG_perKWh*chgL1.ghgMod/chgEffL1)*chgL1.totKWh;
			float ghgL2 = (avGridGHG_perKWh*chgL2.ghgMod/chgEffL2)*chgL2.totKWh;
			float ghgDCFast = (avGridGHG_perKWh*chgDCFast.ghgMod/chgEffDCFast)*chgDCFast.totKWh;
			
			return (ghgL1 + ghgL2 + ghgDCFast)/grandTotKWh;
		}	
		public float eqCost_perKWh(float avGridCost_perKWh, float chgEffL1, float chgEffL2, float chgEffDCFast, float dcFastPricePremium) {
			float grandTotKWh = grandTotKWh();
			if (grandTotKWh < ZTol) return avGridCost_perKWh;

			float costL1 = (avGridCost_perKWh*chgL1.priceMod/chgEffL1)*chgL1.totKWh;
			float costL2 = (avGridCost_perKWh*chgL2.priceMod/chgEffL2)*chgL2.totKWh;
			float costDCFast = (avGridCost_perKWh*chgDCFast.priceMod/chgEffDCFast)*chgDCFast.totKWh*(1f+dcFastPricePremium);
			
			return (costL1 + costL2 + costDCFast)/grandTotKWh;
		}	
		
		public float[][] pdmsChgType() {
			if (chgL1.hourlyTotKWh == null) return null;
			if (chgL2.hourlyTotKWh == null) return null;
			if (chgDCFast.hourlyTotKWh == null) return null;
			
			float[][] pdms = new float[ChargerTypes.values().length][HoursPerDay];
			
			float grandTotKWh = 0f;
			for (int i=0; i<HoursPerDay; i++) {
				grandTotKWh += chgL1.hourlyTotKWh[i] + chgL2.hourlyTotKWh[i] + chgDCFast.hourlyTotKWh[i];
			}
			if (grandTotKWh < ZTol) return pdms;
			
			for (int i=0; i<HoursPerDay; i++) {
				pdms[ChargerTypes.L1.ordinal()][i] = chgL1.hourlyTotKWh[i]/grandTotKWh;
				pdms[ChargerTypes.L2.ordinal()][i] = chgL2.hourlyTotKWh[i]/grandTotKWh;
				pdms[ChargerTypes.DC.ordinal()][i] = chgDCFast.hourlyTotKWh[i]/grandTotKWh;
			}
			
			return pdms;
		}
		
		private CEHistogram() {
			chgL1 = new CEHistogramForChgType();
			chgL2 = new CEHistogramForChgType();
			chgDCFast = new CEHistogramForChgType();
		}	
		private CEHistogram(CEHistogram h1, CEHistogram h2, float wt1, float wt2) {
			chgL1 = new CEHistogramForChgType();
			chgL2 = new CEHistogramForChgType();
			chgDCFast = new CEHistogramForChgType();

			//Re-adjust weights to account for effect of different total kWh
			float gt1 = h1.grandTotKWh();
			float gt2 = h2.grandTotKWh();
			float superWt = gt1*wt1 + gt2*wt2;			
			if (superWt < ZTol) return;
			
			float wWt1 = gt1*wt1/superWt;
			float wWt2 = gt2*wt2/superWt;
			
			chgL1.totKWh = wWt1 * h1.chgL1.totKWh + wWt2 * h2.chgL1.totKWh;
			chgL2.totKWh = wWt1 * h1.chgL2.totKWh + wWt2 * h2.chgL2.totKWh;
			chgDCFast.totKWh = wWt1 * h1.chgDCFast.totKWh + wWt2 * h2.chgDCFast.totKWh;
			
			chgL1.priceMod = wWt1 * h1.chgL1.priceMod + wWt2 * h2.chgL1.priceMod;
			chgL2.priceMod = wWt1 * h1.chgL2.priceMod + wWt2 * h2.chgL2.priceMod;
			chgDCFast.priceMod = wWt1 * h1.chgDCFast.priceMod + wWt2 * h2.chgDCFast.priceMod;
			
			chgL1.ghgMod = wWt1 * h1.chgL1.ghgMod + wWt2 * h2.chgL1.ghgMod;
			chgL2.ghgMod = wWt1 * h1.chgL2.ghgMod + wWt2 * h2.chgL2.ghgMod;
			chgDCFast.ghgMod = wWt1 * h1.chgDCFast.ghgMod + wWt2 * h2.chgDCFast.ghgMod;
			
			if (h1.chgL1.hourlyTotKWh == null) return;
			if (h1.chgL2.hourlyTotKWh == null) return;
			if (h1.chgDCFast.hourlyTotKWh == null) return;
			
			if (h2.chgL1.hourlyTotKWh == null) return;
			if (h2.chgL2.hourlyTotKWh == null) return;
			if (h2.chgDCFast.hourlyTotKWh == null) return;

			chgL1.hourlyTotKWh = new float[HoursPerDay];
			chgL2.hourlyTotKWh = new float[HoursPerDay];
			chgDCFast.hourlyTotKWh = new float[HoursPerDay];
			
			for (int i=0; i<HoursPerDay; i++) {
				chgL1.hourlyTotKWh[i] = wWt1 * h1.chgL1.hourlyTotKWh[i] + wWt2 * h2.chgL1.hourlyTotKWh[i];
				chgL2.hourlyTotKWh[i] = wWt1 * h1.chgL2.hourlyTotKWh[i] + wWt2 * h2.chgL2.hourlyTotKWh[i];
				chgDCFast.hourlyTotKWh[i] = wWt1 * h1.chgDCFast.hourlyTotKWh[i] + wWt2 * h2.chgDCFast.hourlyTotKWh[i];
			}
		}
	}
	private static class CEHistogramForChgType {
		private float totKWh, priceMod, ghgMod;
		private float[] hourlyTotKWh;
		
		private CEHistogramForChgType() {
			totKWh = 0f;
			priceMod = 1f;
			ghgMod = 1f;
			hourlyTotKWh = null;
		}
	}
}
