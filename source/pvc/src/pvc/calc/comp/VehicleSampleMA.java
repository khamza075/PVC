package pvc.calc.comp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import fastsimjava.utility.VehicleSampleMA.AdditionalPayload;
import pvc.datamgmt.comp.FDefaults;
import pvc.gui.comp.RunStatusWindow;

public class VehicleSampleMA {
	private static final int NumDaysPerYear = 365;
	private static final int NumDaysPerYearLY = 366;
	private static final int[] NumDaysPerMonth = {31,28,31, 30,31,30, 31,31,30, 31,30,31};
	private static final int[] NumDaysPerMonthLY = {31,29,31, 30,31,30, 31,31,30, 31,30,31};
	private static final int NumHoursPerDay = 24;
	private static final int NumMinutesPerHour = 60;
	private static final int NumSecondsPerMinute = 60;
	private static final int NumSecondsPerHour = NumMinutesPerHour*NumSecondsPerMinute;
	private static final int NumSecondsPerDay = NumSecondsPerHour*NumHoursPerDay;
	private static final int daysInYearUntilEndOfPrevMonth(int month, int[] daysPerMonthForYear) {
		int days = 0;
		for (int i=0; i<(month-1); i++) days += daysPerMonthForYear[i];
		return days;
	}

	//Vehicle Sample Identification Info
	private SampleID vehSampleInfo;
	public SampleID vehSampleInfo() {return vehSampleInfo;}
	
	//Array of Trips
	private Trip[] trips;
	public Trip[] trips() {return trips;}
	
	//Utility function to return the number of drive days
	public int numDriveDays() {return trips[trips.length-1].tripIDs.dayID + 1;}
	//Utility function to extract an array of trips on a given day
	public Trip[] getDayTrips(int dayID) {
		ArrayList<Trip> lst = new ArrayList<Trip>();
		for (int i=0; i<trips.length; i++) {
			if (trips[i].tripIDs.dayID == dayID) lst.add(trips[i]);
		}
		Trip[] arr = new Trip[lst.size()];
		for (int i=0; i<arr.length; i++) arr[i] = lst.get(i);
		return arr;
	}
	
	
	//Function for reading array from file
	public static VehicleSampleMA[] readArrayFromFile(String fname, RunStatusWindow stWin) throws Exception {
		ArrayList<VehicleSampleMA> lst = new ArrayList<VehicleSampleMA>();
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));

		int updatePeriod = 50;
		int samplesCount = 0;

		while (true) {
			try {
				VehicleSampleMA vs = new VehicleSampleMA(fin);
				if (vs.trips.length >= 1) lst.add(vs);
			} catch (Exception e2) {
				break;
			}
			
			samplesCount++;
			if (samplesCount%updatePeriod == 0) stWin.println(""+samplesCount+" samples read...");
			if (stWin.abortRequested()) {
				fin.close();
				return null;
			}
		}
		
		fin.close();
		
		VehicleSampleMA[] arr = new VehicleSampleMA[lst.size()];
		for (int i=0; i<arr.length; i++) arr[i] = lst.get(i);
		return arr;
	}
	
	//Utility function for counting total number of trips in an array of vehicle samples
	public static int totNumTrips(VehicleSampleMA[] vs) {
		int numTrips = 0;
		for (int i=0; i<vs.length; i++) {
			numTrips += vs[i].trips.length;
		}		
		return numTrips;
	}
		
	//Private constructor via reading from file
	private VehicleSampleMA(BufferedReader fin) throws Exception {
		String readLine = fin.readLine();
		readLine = fin.readLine();
		
		readLine = fin.readLine();
		vehSampleInfo = new SampleID(readLine);
		
		trips = new Trip[vehSampleInfo.nTrips];
		for (int i=0; i<trips.length; i++) trips[i] = new Trip(fin);
				
		//Do a pass to remove trips that are "way-too-short" (i.e. broken fragments)
		ArrayList<Trip> lst = new ArrayList<Trip>();
		int gID = 0;
		int refDayID = -1;
		int dayID = -1;
		int idInDay = 0;
		
		for (int i=0; i<trips.length; i++) {
			if (trips[i].tripIDs.miles >= FDefaults.tooShortTripMilesCutOff) {
				int curTripDayID = trips[i].tripIDs.dayID;
				if (curTripDayID > refDayID) {
					refDayID = curTripDayID;
					dayID++;
					idInDay = 0;
				}
				
				trips[i].tripIDs.gID = gID;
				trips[i].tripIDs.dayID = dayID;
				trips[i].tripIDs.idInDay = idInDay;
				
				lst.add(trips[i]);
				
				idInDay++;
				gID++;
			}
		}
		
		vehSampleInfo.nTrips = lst.size();
		trips = new Trip[vehSampleInfo.nTrips]; 
		for (int i=0; i<trips.length; i++) trips[i] = lst.get(i);
	}	
	
	public static class SampleID {
		//Household ID, Vehicle ID within Household and number of Trips
		public int hhID, vehIDinHH, nTrips;
		//Household Weight
		public float hhWt;

		//Constructor from reading a line from CSV file
		private SampleID(String readLine) {
			String[] sp = readLine.split(",");
			hhID = Integer.parseInt(sp[0]);
			vehIDinHH = Integer.parseInt(sp[1]);
			hhWt = Float.parseFloat(sp[2]);
			nTrips = Integer.parseInt(sp[3]);
		}	
		//Forming a line String
		@Override public String toString() {
			return ""+hhID+","+vehIDinHH+","+hhWt+","+nTrips;
		}
	}
	
	public static class TripIDs {
		//Trip ID in Vehicle Sample, Day ID, Trip ID within given Day
		public int gID,dayID,idInDay;
		//Duration (sec) from end of last trip, number of recorded time steps
		public int secsFromLastTrip,numPayloadAdjust,numRecSteps;
		//Trip distance (miles)
		public float miles;
		//Date and Local Time for Trip Start
		public int year,month,day,hr24,min,sec;

		//Constructor from reading a line from CSV file
		private TripIDs(String readLine) {
			String[] sp = readLine.split(",");
			
			gID = Integer.parseInt(sp[0]);
			dayID = Integer.parseInt(sp[1]);
			idInDay = Integer.parseInt(sp[2]);
			
			secsFromLastTrip = Integer.parseInt(sp[3]);
			numRecSteps = Integer.parseInt(sp[4]);
			miles = Float.parseFloat(sp[5]);
			
			year = Integer.parseInt(sp[6]);
			month = Integer.parseInt(sp[7]);
			day = Integer.parseInt(sp[8]);
			
			hr24 = Integer.parseInt(sp[9]);
			min = Integer.parseInt(sp[10]);
			sec = Integer.parseInt(sp[11]);
			numPayloadAdjust = Integer.parseInt(sp[12]);
		}
		
		//Forming a line String
		@Override public String toString() {
			return ""+gID+","+dayID+","+idInDay+","+secsFromLastTrip+","+numRecSteps+","+miles+","+year+","+month+","+day+","+hr24+","+min+","+sec+","+numPayloadAdjust;
		}		
		
		public int secFrom2k() {
			int daysUpToCurYear = 0;
			for (int i=2000; i<year; i++) {
				if (i%4 == 0) daysUpToCurYear += NumDaysPerYearLY;
				else daysUpToCurYear += NumDaysPerYear;
			}
			
			int numDays = daysUpToCurYear;
			if (year % 4 == 0) numDays += daysInYearUntilEndOfPrevMonth(month, NumDaysPerMonthLY);
			else numDays += daysInYearUntilEndOfPrevMonth(month, NumDaysPerMonth);
			
			numDays += (day-1);
			return numDays*NumSecondsPerDay + hr24*NumSecondsPerHour + min*NumSecondsPerMinute + sec;
		}
		public static int deltaSecStartTime_twoMinusOne(TripIDs t2, TripIDs t1) {
			return t2.secFrom2k() - t1.secFrom2k();
		}
	}
	
	//Class for Trip
	public static class Trip {
		//Trip Identifiers
		private TripIDs tripIDs;
		public TripIDs tripIDs() {return tripIDs;}
		
		//Pay load Adjustments
		private AdditionalPayload[] additionalPayLoadKg;
		public AdditionalPayload[] additionalPayLoadKg() {return additionalPayLoadKg;}
		
		//Speed in MPH
		private float[] speedMPH;
		public float[] speedMPH() {return speedMPH;}
		
		//Filtered road grade
		private float[] fltGrade;
		public float[] fltGrade() {return fltGrade;}
		
		//Recorded Auxiliary Power in kW
		private float[] recAuxKW;
		public float[] recAuxKW() {return recAuxKW;}
		
		//Function to form a time series pay load
		public float[] payloadKg() {return payload1HzTimeSeries(additionalPayLoadKg, speedMPH.length);}
		
		//Constructor via reading a chunk from a file
		private Trip (BufferedReader fin) throws Exception {
			String readLine = fin.readLine();		//Skip header of Trip	
			
			readLine = fin.readLine();	//Skip header of Trip IDs
			readLine = fin.readLine();	//Data of Trip IDs
			
			tripIDs = new TripIDs(readLine);
			
			additionalPayLoadKg = new AdditionalPayload[tripIDs.numPayloadAdjust];
			speedMPH = new float[tripIDs.numRecSteps];
			fltGrade = new float[tripIDs.numRecSteps];
			recAuxKW = new float[tripIDs.numRecSteps];
						
			readLine = fin.readLine();	//Skip header of payload adjustment
			for (int i=0; i<additionalPayLoadKg.length; i++) {
				readLine = fin.readLine();
				additionalPayLoadKg[i] = new AdditionalPayload(readLine);
			}
			
			readLine = fin.readLine();	//Skip header of sec-by-sec			
			for (int i=0; i<speedMPH.length; i++) {
				readLine = fin.readLine();
				String[] sp = readLine.split(",");
				speedMPH[i] = Float.parseFloat(sp[0]);
				fltGrade[i] = Float.parseFloat(sp[1]);
				recAuxKW[i] = Float.parseFloat(sp[2]);
			}
		}
	}
	private static float[] payload1HzTimeSeries(AdditionalPayload[] payloadsInfo, int arrLength) {
		if (payloadsInfo == null) return null;
		
		float[] payloadKg = new float[arrLength];
		if (payloadsInfo.length < 1) return payloadKg;
		
		int curPayloadEndID = payloadKg.length - 1;
		int curPayloadID = payloadsInfo.length-1;
		float curPayload = payloadsInfo[curPayloadID].payloadKg;
		int curPayloadStartID = payloadsInfo[curPayloadID].tStepID;
		
		for (int i=curPayloadEndID; i>=curPayloadStartID; i--) payloadKg[i] = curPayload;
		
		while (curPayloadID > 0) {
			curPayloadID--;
			curPayloadEndID = curPayloadStartID - 1;
			curPayload = payloadsInfo[curPayloadID].payloadKg;
			curPayloadStartID = payloadsInfo[curPayloadID].tStepID;
			for (int i=curPayloadEndID; i>=curPayloadStartID; i--) payloadKg[i] = curPayload;
		}			
		
		return payloadKg;
	}
}
