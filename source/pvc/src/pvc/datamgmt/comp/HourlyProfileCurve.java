package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class HourlyProfileCurve {
	private static final int NumHoursPerDay  = 24;
	private static final float OptStepHours  = 0.05f;	//3 minutes
	
	private float[] profileValueAtHourTop;
	private float fullDayProfileAv;
	public float fullDayProfileAv() {return fullDayProfileAv;}

	public HourlyProfileCurve() {
		profileValueAtHourTop = new float[NumHoursPerDay];
		for (int i=0; i<profileValueAtHourTop.length; i++) profileValueAtHourTop[i] = 1f;
		fullDayProfileAv = 1f;
	}
	public HourlyProfileCurve(HourlyProfileCurve other) {
		profileValueAtHourTop = new float[NumHoursPerDay];
		for (int i=0; i<profileValueAtHourTop.length; i++) profileValueAtHourTop[i] = other.profileValueAtHourTop[i];
		calcFullDayProfileAverage();
	}
	
	public float[] getProfileValues() {
		float[] arr = new float[profileValueAtHourTop.length];
		for (int i=0; i<arr.length; i++) arr[i] = profileValueAtHourTop[i];
		return arr;
	}
	public void setValue(int hrID, float value) {
		if (hrID < 0) return;
		if (hrID >= NumHoursPerDay) return;
		
		profileValueAtHourTop[hrID] = value;
		calcFullDayProfileAverage();
	}
	
	public void readFromFile(String fname) {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			for (int i=0; i<profileValueAtHourTop.length; i++) {
				try {
					String readLine = fin.readLine();
					String[] sp = readLine.split(",");
					profileValueAtHourTop[i] = Float.parseFloat(sp[1]);
				} catch (Exception e) {}
			}			
			fin.close();
		} catch (Exception e) {}
		
		calcFullDayProfileAverage();
	}
	public void writeFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			for (int i=0; i<profileValueAtHourTop.length; i++) {
				fout.append(""+i+","+profileValueAtHourTop[i]+lsep);
			}			
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private void calcFullDayProfileAverage() {
		float sum = 0f;
		for (int i=0; i<profileValueAtHourTop.length; i++) sum += profileValueAtHourTop[i];

		fullDayProfileAv = sum/(float)NumHoursPerDay;
	}
	
	public float getProfileValue(float hr24) {	//hr24 can be any positive value less than the limit for long
		long h = (long)hr24;
		float hFrac = hr24 - h;
		int hID = (int)(h % NumHoursPerDay);
		
		if (hID == (NumHoursPerDay-1)) {			
			float c1 = 1f - hFrac;
			return c1*profileValueAtHourTop[hID] + hFrac*profileValueAtHourTop[0]; 
		}
		
		float c1 = 1f - hFrac;
		return c1*profileValueAtHourTop[hID] + hFrac*profileValueAtHourTop[hID+1]; 
	}
	public float calcEventProfileAverage(float h24StartEvent, float hDuration) {
		float h24Start = h24StartEvent;
		while (h24Start > NumHoursPerDay) h24Start += -NumHoursPerDay;
		
		float zTol = 0.001f;
		if (hDuration < zTol) return getProfileValue(h24Start);
		
		float area = 0f;
		float remDuration = hDuration;
		
		while (remDuration > NumHoursPerDay) {
			area += fullDayProfileAv*NumHoursPerDay;
			remDuration += -NumHoursPerDay;
		}
		
		int prevH = (int)h24Start;
		int nextH = prevH + 1;
		
		if ((prevH + remDuration) <= nextH) {
			float y1 = getProfileValue(h24Start);
			float y2 = getProfileValue(h24Start+remDuration);
			area += 0.5f*(y1+y2)*remDuration;
			
			return area/hDuration;
		}
		
		float y1 = getProfileValue(h24Start);
		float y2;
		if (nextH < NumHoursPerDay) {
			y2 = profileValueAtHourTop[nextH];
			
			area += 0.5f*(y1+y2)*(nextH - h24Start);
			remDuration += -(nextH - h24Start);
		} else {
			y2 = profileValueAtHourTop[0];
			
			area += 0.5f*(y1+y2)*(nextH - h24Start);
			remDuration += -(nextH - h24Start);
			
			nextH = 0;
		}
		
		if ((nextH+remDuration) > NumHoursPerDay) {
			for (int i=nextH+1; i<NumHoursPerDay; i++) {
				area += 0.5f*(profileValueAtHourTop[i-1] + profileValueAtHourTop[i]);
				remDuration += -1;
			}
			
			nextH = 0;
		}
		
		prevH = nextH;
		nextH++;
		
		while ((prevH+remDuration) > nextH) {
			area += 0.5f*(profileValueAtHourTop[prevH] + profileValueAtHourTop[nextH]);
			remDuration += -1;

			prevH = nextH;
			nextH++;
		}
		
		y1 = profileValueAtHourTop[prevH];
		y2 = getProfileValue(prevH+remDuration);
		area += 0.5f*(y1+y2)*remDuration;
		
		return area/hDuration;
	}
	
	public static class OptDelayResult {
		public float hoursDelay, eventMinAvProfile;
		private OptDelayResult() {}
	}
	
	public OptDelayResult optimizeDelayForMinAvProfileValue(float h24Start, float hDuration, float hMaxDelay) {
		OptDelayResult res = new OptDelayResult();
		
		float cDelay = 0f;
		float cAvProfile = calcEventProfileAverage(h24Start+cDelay, hDuration);
		
		res.hoursDelay = cDelay;
		res.eventMinAvProfile = cAvProfile;
		
		float zTol = 0.0001f;
		if (hMaxDelay < zTol) return res;
		
		int numTries = (int)(hMaxDelay/OptStepHours);
		for (int i=0; i<numTries; i++) {
			cDelay += OptStepHours;
			cAvProfile = calcEventProfileAverage(h24Start+cDelay, hDuration);
			
			if (cAvProfile < (res.eventMinAvProfile - zTol)) {
				res.hoursDelay = cDelay;
				res.eventMinAvProfile = cAvProfile;
			}
		}
		
		return res;
	}
}
