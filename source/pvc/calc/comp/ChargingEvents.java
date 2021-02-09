package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import pvc.datamgmt.WIITModel;


public class ChargingEvents {
	public static final String FHeader = "hhWt,chgEventType,kWhAmountToBattery,h24MinStart,hrsDuration,hrsSlack";
	public static final float MinutesPerHour = 60.0f;
	public static final float SecondsPerMinute = 60.0f;
	public static final float SecondsPerHour = 3600.0f;
	
	
	private ChargingEvent[] chgEvents;
	public ChargingEvent[] chgEvents() {return chgEvents;}
	
	private float wtNumChgEvents;
	public float wtNumChgEvents() {return wtNumChgEvents;}
	
	
	public static ChargingEvents readFromFile(String fname) {
		try {
			return new ChargingEvents(fname);
		} catch (Exception e) {
			return null;
		}
	}
	private ChargingEvents(String fname) throws Exception {
		ArrayList<ChargingEvent> lst = new ArrayList<ChargingEvent>();
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		while ((readLine = fin.readLine())!=null) {
			lst.add(new ChargingEvent(readLine));
		}
		
		fin.close();
		
		wtNumChgEvents = 0f;
		chgEvents = new ChargingEvent[lst.size()];
		for (int i=0; i<chgEvents.length; i++) {
			chgEvents[i] = lst.get(i);
			wtNumChgEvents += chgEvents[i].hhWt;
		}
	}
	
	public enum ChargingEventType {
		overNight_L1, overNight_L2, dayTime_L2, dayTime_DCFast
		;
		private ChargingEventType() {}
		public static ChargingEventType decode(String s) {
			for (int i=0; i<values().length; i++) {
				if (s.equalsIgnoreCase(values()[i].toString())) return values()[i];
			}
			return ChargingEventType.dayTime_L2;
		}
	}
	
	public static class ChargingEvent {
		public float hhWt,kWhAmountToBattery,h24MinStart,hrsDuration,hrsSlack;
		public ChargingEventType chgEventType;
		
		private ChargingEvent(String readLine) {
			String[] sp = readLine.split(",");
			
			hhWt = Float.parseFloat(sp[0]);
			chgEventType = ChargingEventType.decode(sp[1]);
			kWhAmountToBattery = Float.parseFloat(sp[2]);
			h24MinStart = Float.parseFloat(sp[3]);
			hrsDuration = Float.parseFloat(sp[4]);
			hrsSlack = Float.parseFloat(sp[5]);
		}
	}
	
	public static class ChargingWindowEventResult {
		public float finalRelSoC, minDurationHrs, kWhAmountToBattery;		
		private ChargingWindowEventResult() {}
	}
	public static float availableChgHrs(WIITModel wiitModel, float timeWindowSeconds) {
		return (timeWindowSeconds / SecondsPerHour) - 
				(wiitModel.chgModels.chgDeadTime_minutesBeforeConnect + wiitModel.chgModels.chgDeadTime_minutesAfterConnect)/MinutesPerHour;
	}
	public static ChargingWindowEventResult chgResult(ChargingEventType chgType, WIITModel wiitModel, float timeWindowSeconds,
			float batteryRelSoC, float batterySwingKWh) {
		
		ChargingWindowEventResult res = new ChargingWindowEventResult();
		
		float availableChgHrs = availableChgHrs(wiitModel, timeWindowSeconds);
		if (availableChgHrs < 0) {
			res.finalRelSoC = batteryRelSoC;
			res.kWhAmountToBattery = 0f;
			res.minDurationHrs = 0f;
			return res;
		}
		
		if (chgType != null) {
			switch (chgType) {
			case dayTime_DCFast:
			{
				float remHrs = availableChgHrs;
				float curSoC = batteryRelSoC;
				
				if (curSoC < wiitModel.chgModels.dcfsLowSOC) {
					float hrsToReachNextPhase = (wiitModel.chgModels.dcfsLowSOC - curSoC) / wiitModel.chgModels.dcfsRelSOCSlowRate;
					
					if (remHrs >= hrsToReachNextPhase) {
						remHrs += -hrsToReachNextPhase;
						curSoC = wiitModel.chgModels.dcfsLowSOC;
					} else {
						curSoC += wiitModel.chgModels.dcfsRelSOCSlowRate*remHrs;
						remHrs = 0f;
						
						res.finalRelSoC = curSoC;
						res.kWhAmountToBattery = (res.finalRelSoC - batteryRelSoC)*batterySwingKWh;
						res.minDurationHrs = availableChgHrs - remHrs;
						return res;
					}
				}
				if (curSoC < wiitModel.chgModels.dcfsHighSOC) {
					float hrsToReachNextPhase = (wiitModel.chgModels.dcfsHighSOC - curSoC) / wiitModel.chgModels.dcfsRelSOCFastRate;

					if (remHrs >= hrsToReachNextPhase) {
						remHrs += -hrsToReachNextPhase;
						curSoC = wiitModel.chgModels.dcfsHighSOC;
					} else {
						curSoC += wiitModel.chgModels.dcfsRelSOCFastRate*remHrs;
						remHrs = 0f;
						
						res.finalRelSoC = curSoC;
						res.kWhAmountToBattery = (res.finalRelSoC - batteryRelSoC)*batterySwingKWh;
						res.minDurationHrs = availableChgHrs - remHrs;
						return res;
					}
				}
				
				float hrsToFull = (1f - curSoC) / wiitModel.chgModels.dcfsRelSOCSlowRate;
				
				if (remHrs >= hrsToFull) {
					remHrs += -hrsToFull;
					curSoC = 1f;
				} else {
					curSoC += wiitModel.chgModels.dcfsRelSOCSlowRate*remHrs;
					remHrs = 0f;
				}
				
				res.finalRelSoC = curSoC;
				res.kWhAmountToBattery = (res.finalRelSoC - batteryRelSoC)*batterySwingKWh;
				res.minDurationHrs = availableChgHrs - remHrs;
				return res;
			}
			case overNight_L2:
			case dayTime_L2:
			{
				float kWhBatteryToFull = (1f - batteryRelSoC)*batterySwingKWh;
				float chargerPowerKW = wiitModel.chgModels.l2PowerKWtoBattery;
				float minHoursToFull = kWhBatteryToFull / chargerPowerKW;

				if (availableChgHrs >= minHoursToFull) {
					res.finalRelSoC = 1f;
					res.minDurationHrs = minHoursToFull;
					res.kWhAmountToBattery = kWhBatteryToFull;
				} else {
					res.kWhAmountToBattery = chargerPowerKW * availableChgHrs;
					res.minDurationHrs = availableChgHrs;
					res.finalRelSoC = batteryRelSoC + (res.kWhAmountToBattery/batterySwingKWh);
				}
				return res;
			}
			case overNight_L1:
			{
				float kWhBatteryToFull = (1f - batteryRelSoC)*batterySwingKWh;
				float chargerPowerKW = wiitModel.chgModels.l1PowerKWtoBattery;
				float minHoursToFull = kWhBatteryToFull / chargerPowerKW;

				if (availableChgHrs >= minHoursToFull) {
					res.finalRelSoC = 1f;
					res.minDurationHrs = minHoursToFull;
					res.kWhAmountToBattery = kWhBatteryToFull;
				} else {
					res.kWhAmountToBattery = chargerPowerKW * availableChgHrs;
					res.minDurationHrs = availableChgHrs;
					res.finalRelSoC = batteryRelSoC + (res.kWhAmountToBattery/batterySwingKWh);
				}
				return res;
			}
			}	
		}
		
		res.finalRelSoC = batteryRelSoC;
		res.kWhAmountToBattery = 0f;
		res.minDurationHrs = 0f;
		return res;
	}
	

	public static float regHr24_from_h24_mm_ss(int hr24, int min, int sec) {
		float h24 = hr24 + ((float)min)/MinutesPerHour + ((float)sec)/SecondsPerHour;
		
		while (h24 < 0) h24 += 24f;
		while (h24 >= 24f) h24 += -24f;
		if (Math.abs(h24) < 0.0001f) h24 = 0f;
		
		return h24;
	}
}
