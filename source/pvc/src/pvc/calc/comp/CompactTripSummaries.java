package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class CompactTripSummaries {
	public static final String FHeader = "hhID,vehIDinHH,hhWt,miles,dayID,secFromLastTrip";
	
	private CTripSummary[] cTrips;
	public CTripSummary[] cTrips() {return cTrips;}
	
	private int unweighedNumDriveDays;
	public int unweighedNumDriveDays() {return unweighedNumDriveDays;}
	
	private float wtNumDriveDays, unweighedTotMiles, wtMiles;
	public float wtNumDriveDays() {return wtNumDriveDays;}
	public float unweighedTotMiles() {return unweighedTotMiles;}
	public float wtMiles() {return wtMiles;}
	

	public static CompactTripSummaries readFromFile(String fname) {
		try {
			return new CompactTripSummaries(fname);
		} catch (Exception e) {
			return null;
		}
	}
	private CompactTripSummaries(String fname) throws Exception {
		ArrayList<CTripSummary> lst = new ArrayList<CTripSummary>();
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		while ((readLine = fin.readLine())!=null) {
			lst.add(new CTripSummary(readLine));
		}
		
		fin.close();
		
		cTrips = new CTripSummary[lst.size()];
		for (int i=0; i<cTrips.length; i++) cTrips[i] = lst.get(i);
		
		int curHHID = cTrips[0].hhID;
		int curVehIDinHH = cTrips[0].vehIDinHH;
		int curDayID = cTrips[0].dayID;
		
		unweighedNumDriveDays = 1;
		wtNumDriveDays = cTrips[0].hhWt;
		unweighedTotMiles = cTrips[0].miles;
		wtMiles = cTrips[0].miles * cTrips[0].hhWt;
		
		for (int i=1; i<cTrips.length; i++) {
			if ((curHHID != cTrips[i].hhID)||(curVehIDinHH != cTrips[i].vehIDinHH)||(curDayID != cTrips[i].dayID)) {
				curHHID = cTrips[i].hhID;
				curVehIDinHH = cTrips[i].vehIDinHH;
				curDayID = cTrips[i].dayID;
				
				unweighedNumDriveDays++;
				wtNumDriveDays += cTrips[i].hhWt;
				unweighedTotMiles += cTrips[i].miles;
				wtMiles += cTrips[i].miles * cTrips[i].hhWt;
			}
		}
	}
	
	public static class CTripSummary {
		public int hhID,vehIDinHH,dayID,secFromLastTrip;
		public float hhWt,miles;
		
		public CTripSummary(String readLine) {
			String[] sp = readLine.split(",");
			
			hhID = Integer.parseInt(sp[0]);
			vehIDinHH = Integer.parseInt(sp[1]);
			
			hhWt = Float.parseFloat(sp[2]);
			miles = Float.parseFloat(sp[3]);
			
			dayID = Integer.parseInt(sp[4]);
			secFromLastTrip = Integer.parseInt(sp[5]);
		}
	}
}
