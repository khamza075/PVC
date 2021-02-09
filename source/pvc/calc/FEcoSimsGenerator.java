package pvc.calc;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.*;
import pvc.calc.comp.*;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.FDefaults;
import pvc.gui.comp.RunStatusWindow;

public class FEcoSimsGenerator {
	
	private static final float ZeroChgKWh = 0.0001f;
	private static final float ZeroChgHrs = 0.001f;
	
	private FFStructure fs; 
	public FFStructure fs() {return fs;}
	private int aID; 
	public int aID() {return aID;}
	
	private AnalysisVehModelsSetup avms;
	private WIITModel wiitMod;
	
	private FSJOneFileVehModel[] fsofModels;
	private int numDaytimeCharging, numCasesPerPHEV, numBEVRepVeh, numCasesPerBEV, numCases;
	private boolean considerNonChgPHEVs, commercialVehModelForBEVs;
	private int[] bevRepVehModelID;
	
	//Quick Access Functions -- but use at own risk (do not use to modify values in this class)
	public AnalysisVehModelsSetup avms() {return avms;}
	public WIITModel wiitMod() {return wiitMod;}
	public FSJOneFileVehModel[] fsofModels() {return fsofModels;}
	public int[] bevRepVehModelID() {return bevRepVehModelID;}

	public int numCases() {return numCases;}
	public int numCasesPerPHEV() {return numCasesPerPHEV;}
	public int numCasesPerBEV() {return numCasesPerBEV;}
	public int numBEVRepVeh() {return numBEVRepVeh;}


	//Static function for instantiation
	public static FEcoSimsGenerator createFEcoSimsGenerator(FFStructure cFS, int analysisID, AnalysisVehModelsSetup avms, WIITModel wiitModel) {
		try {
			return new FEcoSimsGenerator(cFS, analysisID, avms, wiitModel);
		} catch (Exception e) {
			return null;
		}
	}
	private FEcoSimsGenerator(FFStructure cFS, int analysisID, AnalysisVehModelsSetup aVehModelsSetup, WIITModel wiitModel) throws Exception {
		fs = cFS;
		aID = analysisID;
		avms = aVehModelsSetup;
		wiitMod = wiitModel;
		
		if (fs == null) throw new Exception();
		if (avms == null) throw new Exception();
		if (wiitMod == null) throw new Exception();
		
		fsofModels = new FSJOneFileVehModel[avms.vehModelsSetup().length];
		for (int i=0; i<fsofModels.length; i++) {
			String fsofFile = fs.getFilePath_FASTSimVehModel(aID, avms.vehModelsSetup()[i].shortName);
			fsofModels[i] = new FSJOneFileVehModel(fsofFile);
		}
		
		numDaytimeCharging = 0;
		numCasesPerPHEV = 0;
		numBEVRepVeh = 0;
		numCasesPerBEV = 0;
		bevRepVehModelID = null;
		
		if (wiitMod.hasPlugIns()) {
			numDaytimeCharging = wiitMod.chgModels.daytimeChargingMinWindow.length;
			
			if (wiitMod.chgModels.fractionNonChargingPHEVs != null) considerNonChgPHEVs = true;
			else considerNonChgPHEVs = false;
			
			numCasesPerPHEV = numDaytimeCharging + 1;
			if (considerNonChgPHEVs) numCasesPerPHEV++;
			
			if (wiitMod.bevRepModel.bevRepCommercial != null) commercialVehModelForBEVs = true;
			else {
				commercialVehModelForBEVs = false;
				
				numBEVRepVeh = wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName.length;
				bevRepVehModelID = new int[numBEVRepVeh];
				
				for (int i=0; i<bevRepVehModelID.length ;i++) {
					for (int j=0; j<avms.vehModelsSetup().length; j++) {
						if (avms.vehModelsSetup()[j].shortName.equalsIgnoreCase(wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName[i])) {
							bevRepVehModelID[i] = j;
							break;
						}
					}
				}
			}
			
			if (commercialVehModelForBEVs) numCasesPerBEV = 1;
			else numCasesPerBEV = (numDaytimeCharging + 1) * wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;			
		}
		
		numCases = 0;
		for (int i=0; i<fsofModels.length; i++) {
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
				numCases += numCasesPerBEV;
				break;
			case cv:
			case hev:
				numCases++;
				break;
			case phev:
				numCases += numCasesPerPHEV;
				break;
			}
		}
	}
	
	//Access identification function for PHEVs
	public int phevLocalCaseID(boolean nonChargingCase, boolean overnightOnly, int daytimeChgWindowID) {
		if (nonChargingCase) return 0;
		if (considerNonChgPHEVs) {
			if (overnightOnly) return 1;
			return daytimeChgWindowID + 2;
		} else {
			if (overnightOnly) return 0;
			return daytimeChgWindowID + 1;
		}
	}
	//Access identification function for BEVs
	public int bevLocalCaseID(int dtChgID, int anxID) {
		if (commercialVehModelForBEVs) return 0;

		return dtChgID*wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length + anxID;
	}
	
	//File path location functions
	public String fSolFileName(int sID, int vehModelID, int localCaseID) {
		if (vehModelID < 0) return null;
		if (vehModelID >= fsofModels.length) return null;
		
		String fname = "";
		switch (fsofModels[vehModelID].vehModelParam.general.vehPtType) {
		case cv:
		case hev:
			fname = solFileName_nonPlugin(avms.vehModelsSetup()[vehModelID].shortName);
			break;
		case bev:
			fname = solFileName_BEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		case phev:
			fname = solFileName_PHEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		}
		return fs.getFolderPath_fecoSims(aID, sID) + FFStructure.slashChar() + fname;
	}
	public String fSolPostFileName(int sID, int vehModelID, int localCaseID) {
		if (vehModelID < 0) return null;
		if (vehModelID >= fsofModels.length) return null;
		
		String fname = "";
		switch (fsofModels[vehModelID].vehModelParam.general.vehPtType) {
		case cv:
		case hev:
			fname = solFileName_nonPlugin(avms.vehModelsSetup()[vehModelID].shortName);
			break;
		case bev:
			fname = solFileName_BEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		case phev:
			fname = solFileName_PHEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		}
		return fs.getFolderPath_fecoSimsPost(aID, sID) + FFStructure.slashChar() + fname;
	}
	public String fChgFileName(int sID, int vehModelID, int localCaseID) {
		if (vehModelID < 0) return null;
		if (vehModelID >= fsofModels.length) return null;
		
		String fname = "";
		switch (fsofModels[vehModelID].vehModelParam.general.vehPtType) {
		case cv:
		case hev:
			return null;
		case bev:
			fname = chgFileName_BEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		case phev:
			fname = chgFileName_PHEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		}
		
		if (!fname.contains(".csv")) return null;
		return fs.getFolderPath_fecoSims(aID, sID) + FFStructure.slashChar() + fname;
	}
	public String fChgSummaryFileName(int sID, int vehModelID, int localCaseID) {
		if (vehModelID < 0) return null;
		if (vehModelID >= fsofModels.length) return null;
		
		String fname = "";
		switch (fsofModels[vehModelID].vehModelParam.general.vehPtType) {
		case cv:
		case hev:
			return null;
		case bev:
			fname = chgFileName_BEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		case phev:
			fname = chgFileName_PHEV(avms.vehModelsSetup()[vehModelID].shortName, localCaseID);
			break;
		}
		
		if (!fname.contains(".csv")) return null;
		return fs.getFolderPath_chgEventsSummaries(aID, sID) + FFStructure.slashChar() + fname;
	}
	
	private String fileNameWOExt_PHEV(String shortName, int localPHEVCaseID) {
		if (considerNonChgPHEVs) {
			if (localPHEVCaseID == 0) return shortName+"_nonChg";
			if (localPHEVCaseID == 1) return shortName+"_overnight";
			return shortName+"_dtChg"+(localPHEVCaseID-2);
		} else {
			if (localPHEVCaseID == 0) return shortName+"_overnight";
			return shortName+"_dtChg"+(localPHEVCaseID-1);
		}
	}
	private String solFileName_PHEV(String shortName, int localPHEVCaseID) {
		return fileNameWOExt_PHEV(shortName, localPHEVCaseID) + ".csv";
	}
	private String chgFileName_PHEV(String shortName, int localPHEVCaseID) {
		if (considerNonChgPHEVs && (localPHEVCaseID == 0)) return "";
		return fileNameWOExt_PHEV(shortName, localPHEVCaseID) + "_chg.csv";
	}
	private String fileNameWOExt_BEV(String shortName, int localBEVCaseID) {
		if (commercialVehModelForBEVs) return shortName+"_infBat";
		
		int numAnxLevels = wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;
		
		int dtChgID = localBEVCaseID / numAnxLevels;
		int anxID = localBEVCaseID % numAnxLevels;
		
		String fname = new String(shortName);
		if (dtChgID == 0) fname = fname + "_overnight";
		else fname = fname + "_dtChg" + (dtChgID-1);
		
		fname = fname + "_anx" + anxID;
		return fname;
	}
	private String solFileName_BEV(String shortName, int localBEVCaseID) {
		return fileNameWOExt_BEV(shortName, localBEVCaseID) + ".csv";
	}
	private String chgFileName_BEV(String shortName, int localBEVCaseID) {
		if (commercialVehModelForBEVs) return "";
		return fileNameWOExt_BEV(shortName, localBEVCaseID) + "_chg.csv";
	}
	private String solFileName_nonPlugin(String shortName) {
		return shortName + ".csv";
	}
	private static class LocalIDPair {
		private int vehModelID, localCaseID;
		private LocalIDPair(int vID, int cID) {
			vehModelID = vID;
			localCaseID = cID;
		}
	}
	private LocalIDPair globalCaseIDtoLocal(int gID) {
		int vID = 0;
		int prevGlobID = 0;
		int gIDNextVehStart = prevGlobID;
		
		switch (fsofModels[vID].vehModelParam.general.vehPtType) {
		case bev:
			gIDNextVehStart += numCasesPerBEV;
			break;
		case cv:
		case hev:
			gIDNextVehStart++;
			break;
		case phev:
			gIDNextVehStart += numCasesPerPHEV;
			break;
		}
		
		while (gID >= gIDNextVehStart) {
			prevGlobID = gIDNextVehStart;
			vID++;
			
			switch (fsofModels[vID].vehModelParam.general.vehPtType) {
			case bev:
				gIDNextVehStart += numCasesPerBEV;
				break;
			case cv:
			case hev:
				gIDNextVehStart++;
				break;
			case phev:
				gIDNextVehStart += numCasesPerPHEV;
				break;
			}
		}
		
		return new LocalIDPair(vID, gID-prevGlobID);
	}

	
	private boolean phevLocalCaseID_to_isNonCharging(int lcID) {
		if (!considerNonChgPHEVs) return false;
		if (lcID == 0) return true;
		return false;
	}
	private boolean phevLocalCaseID_to_isOvernightOnlyCharging(int lcID) {
		if (considerNonChgPHEVs) {
			if (lcID == 1) return true;
			return false;
		}
		if (lcID == 0) return true;
		return false;
	}
	private int phevLocalCaseID_to_dayTimeWinowID(int lcID) {
		if (considerNonChgPHEVs) return lcID - 2;
		return lcID - 1;
	}
	private boolean bevLocalCaseID_to_isOvernightOnlyCharging(int lcID) {
		int dtChgID = lcID / wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;
		if (dtChgID == 0) return true;
		return false;
	}
	private int bevLocalCaseID_to_dayTimeWinowID(int lcID) {
		int dtChgID = lcID / wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;
		return dtChgID - 1;
	}
	private int bevLocalCaseID_to_anxID(int lcID) {
		return lcID % wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;
	}
	
	//Function to ensure folder structure for new solution (Fuel Economy Simulations) exists so that files could be saved
	public void ensureSolFolderExists(int sID) {
		String folderPath = fs.getFolderPath_fecoSims(aID, sID);
		File f = new File(folderPath);
		f.mkdirs();
	}
	//Function to ensure folder structure for new post-processed solution exists so that files could be saved
	public void ensureSolPostProcFolderExists(int sID) {
		String folderPath = fs.getFolderPath_fecoSimsPost(aID, sID);
		File f = new File(folderPath);
		f.mkdirs();
	}
	//Function to ensure folder structure for charging events summary exists so that files could be saved
	public void ensureChgSummariesFolderExists(int sID) {
		String folderPath = fs.getFolderPath_chgEventsSummaries(aID, sID);
		File f = new File(folderPath);
		f.mkdirs();
	}
	
	
	//Function for writing simulation summary
	public void genFecoSimSummaryFile(int sID, String title, String description, float annualVMT, float annualDriveDays) {
		String fname = fs.getFilePath_fecoSimsDescriptionFile(aID, sID);
		
		FEcoSimsC.FEcoSimC fss = new FEcoSimsC.FEcoSimC();
		fss.title = title;
		fss.description = description;
		fss.averageAnnualMiles = annualVMT;
		fss.averageAnnualDrivingDays = annualDriveDays;
		
		fss.saveToFile(fname);
	}
	
	//Function for writing compact trip summaries (by simulation ID)
	public void genCompactTripSummaries(int sID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {		
		String fname = fs.getFilePath_fecoSimsTripSummariesFile(aID, sID);
		FileWriter fout = new FileWriter(fname);
		String lsep = System.getProperty("line.separator");
		
		fout.append(CompactTripSummaries.FHeader+lsep);
		for (int i=0; i<vs.length; i++) {
			int hhID = vs[i].vehSampleInfo().hhID;
			int vehIDinHH = vs[i].vehSampleInfo().vehIDinHH;
			float hhWt = vs[i].vehSampleInfo().hhWt;
			
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			for (int j=0; j<trips.length; j++) {
				VehicleSampleMA.TripIDs tIDs = trips[j].tripIDs();
				String st = ""+hhID+","+vehIDinHH+","+hhWt+","+tIDs.miles+","+tIDs.dayID+","+tIDs.secsFromLastTrip+lsep;
				fout.append(st);
			}
			
			if (stWin.abortRequested()) {
				fout.close();
				return;
			}			
		}
		
		fout.close();
		stWin.println("Writing Compact Trip Summaries Completed");
	}
	
	//Main function for running fuel economy simulations (by simulation ID and case ID)
	public void genFuelEcoSimCase(int sID, int cID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		LocalIDPair lpID = globalCaseIDtoLocal(cID);
		
		switch (fsofModels[lpID.vehModelID].vehModelParam.general.vehPtType) {
		case cv:
		case hev:
			runCase_ICE_HEV(sID, lpID.vehModelID, vs, stWin);
			break;
		case bev:
			if (commercialVehModelForBEVs) {
				runCase_BEV_infBattery(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin);
			} else {
				if (bevLocalCaseID_to_isOvernightOnlyCharging(lpID.localCaseID)) {
					runCase_BEV_onlyOvernightCharging(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin);
				}
				else {
					runCase_BEV_daytimeCharging(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin);
				}
			}
			break;
		case phev:
			if (phevLocalCaseID_to_isNonCharging(lpID.localCaseID)) {
				runCase_PHEV_nonCharging(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin);
			}
			else if (phevLocalCaseID_to_isOvernightOnlyCharging(lpID.localCaseID)) {
				runCase_PHEV_onlyOvernightCharging(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin); 
			} else {
				runCase_PHEV_daytimeCharging(sID, lpID.vehModelID, lpID.localCaseID, vs, stWin); 
			}
			break;
		}
	}
	
	private void runCase_BEV_daytimeCharging(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_BEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		String chgFileName = fChgFileName(sID, vehModelID, lcID);
		
		String solFileHeader = "kWh";
		String[] repVehicleShortName = wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName;
		for (int i=0; i<repVehicleShortName.length; i++) solFileHeader = solFileHeader + ",fuelAmount_" + repVehicleShortName[i];
		
		FileWriter fout = new FileWriter(solFileName);
		FileWriter foutC = new FileWriter(chgFileName);
		String lsep = System.getProperty("line.separator");
		
		fout.append(solFileHeader+lsep);
		foutC.append(ChargingEvents.FHeader+lsep);
		
		int[] repVehIDs = new int[repVehicleShortName.length];
		for (int i=0; i<repVehIDs.length; i++) {
			for (int j=0; j<avms.numVehModels(); j++) {
				if (repVehicleShortName[i].equalsIgnoreCase(avms.vehModelsSetup()[j].shortName)) {
					repVehIDs[i] = j;
					break;
				}
			}
		}
		
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);

		FASTSimJ3c[] fsjRepVehicles = new FASTSimJ3c[repVehIDs.length];
		for (int i=0; i<fsjRepVehicles.length; i++) {
			fsjRepVehicles[i] = new FASTSimJ3c();
			fsjRepVehicles[i].setVehModel(fsofModels[repVehIDs[i]].vehModelParam, fsofModels[repVehIDs[i]].curveMan,
					fsofModels[repVehIDs[i]].addMassKg, fsofModels[repVehIDs[i]].addAuxKW, fsofModels[repVehIDs[i]].adjDEMult);
		}		

		
		float relSoC = 1f;
		float batterySwingKWh = fsofModels[vehModelID].vehModelParam.batterySwingKWh();
		float nomAER = avms.vehModelsSetup()[vehModelID].nominalAERMiles;
		int rangeAnxID = bevLocalCaseID_to_anxID(lcID);
		float minDaytimeChgWindowHrs = wiitMod.chgModels.daytimeChargingMinWindow[bevLocalCaseID_to_dayTimeWinowID(lcID)];
		
		ChargingEvents.ChargingEventType homeChgType = ChargingEvents.ChargingEventType.overNight_L1;
		if (wiitMod.chgModels.bevHomesHaveL2) homeChgType = ChargingEvents.ChargingEventType.overNight_L2;		

		for (int i=0; i<vs.length; i++) {
			float hhWt = vs[i].vehSampleInfo().hhWt;
			
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			TripDaysMarker dMarker = new TripDaysMarker(trips);
			
			for (int j=0; j<dMarker.daysMarker.length; j++) {	//Loop on days
				//Form daytime charging plan
				DayTimeChargingPlanner.DTChargingPlan dtChgPlan = DayTimeChargingPlanner.bev_daytimeChgPlan(trips, dMarker.daysMarker[j].idFirstTripOfDay,
						dMarker.daysMarker[j].idLastTripOfDay, relSoC, batterySwingKWh, nomAER, minDaytimeChgWindowHrs, rangeAnxID, wiitMod);

				if (dtChgPlan!=null) {
					//BEV will be used for the day
					boolean ranOutOfBattery = false;
					
					for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
						
						//Day-time charging event if any
						ChargingEvents.ChargingEventType chgEventType = dtChgPlan.chgEventBeforeTrip(k);

						if (chgEventType != null) {
							float chgEventWindowSeconds = trips[k].tripIDs().secsFromLastTrip;
							ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(chgEventType, wiitMod, 
									chgEventWindowSeconds, relSoC, batterySwingKWh);
							
							//Set battery SoC to the value from charging event
							relSoC = chgRes.finalRelSoC;
							
							//Write data of charging event to file
							if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
								float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, chgEventWindowSeconds);
								float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
								if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
								
								String cSt = ""+hhWt+","+chgEventType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
										trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
										trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
										trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
										+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
										","+chgRes.minDurationHrs+","+hrsSlack;
								foutC.append(cSt+lsep);
								foutC.flush();
							}
						}
						
						//FASTSim simulations for trips of the day
						float[] mph = trips[k].speedMPH();
						float[] fltGrade = trips[k].fltGrade();
						float[] oAuxKw = trips[k].recAuxKW();
						float[] payloadKg = trips[k].payloadKg();
						
						fsj.setRelSoC(relSoC);
						fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
						
						float electKWhUse = fsj.lastTripSummary().batteryUse;						
						relSoC = fsj.lastTripSummary().finalRelSoC;
						if (relSoC < 0) ranOutOfBattery = true;
						
						String sSt = "";
						if (ranOutOfBattery) {
							sSt = "-1";
							for (int l=0; l<repVehIDs.length; l++) sSt = sSt + ",-1";	//Note only a fraction of a days' trips might get flagged that way
						} else {
							sSt = sSt + electKWhUse;
							for (int l=0; l<repVehIDs.length; l++) sSt = sSt + ",-1";
						}
						fout.append(sSt+lsep);
						fout.flush();
						
						if (stWin.abortRequested()) {
							fout.close();
							foutC.close();
							return;
						}
					}
					
					//Charging event at the end of the day 
					float overnightChgEventWindowSeconds = FDefaults.hrsChgAfterlastTripOfLastDay * ChargingEvents.SecondsPerHour;
					if (j < (dMarker.daysMarker.length - 1)) {
						overnightChgEventWindowSeconds = trips[dMarker.daysMarker[j+1].idFirstTripOfDay].tripIDs().secsFromLastTrip;
					}
					
					ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(homeChgType, wiitMod, 
							overnightChgEventWindowSeconds, relSoC, batterySwingKWh);
					
					//Set battery SoC to the value from charging event
					relSoC = chgRes.finalRelSoC;
					
					//Write data of charging event to file
					if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
						float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, overnightChgEventWindowSeconds);
						float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
						if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
						
						String cSt = ""+hhWt+","+homeChgType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
								+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
								","+chgRes.minDurationHrs+","+hrsSlack;
						foutC.append(cSt+lsep);
						foutC.flush();
					}				
				}
				else {
					//Replacement vehicles will be used for the day
					for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
						String sSt = "-1";

						float[] mph = trips[k].speedMPH();
						float[] fltGrade = trips[k].fltGrade();
						float[] oAuxKw = trips[k].recAuxKW();
						float[] payloadKg = trips[k].payloadKg();
						
						for (int l=0; l<fsjRepVehicles.length; l++) {
							fsjRepVehicles[l].runC(null, mph, fltGrade, oAuxKw, payloadKg);
							float fuelUse = fsjRepVehicles[l].lastTripSummary().fuelUse;
							sSt = sSt + "," + fuelUse;
						}
						
						fout.append(sSt+lsep);
						fout.flush();
						
						if (stWin.abortRequested()) {
							fout.close();
							foutC.close();
							return;
						}
					}
					
					//Replacement vehicles used for current day... if BEV wasn't at full, top it off with a charging event that can happen any time
					if (relSoC < 1f) {
						float kWhToFull = (1f - relSoC)*batterySwingKWh;
						float chgPowerKW = wiitMod.chgModels.l1PowerKWtoBattery;
						if (homeChgType == ChargingEvents.ChargingEventType.overNight_L2) chgPowerKW = wiitMod.chgModels.l2PowerKWtoBattery;
						float chgHrs = kWhToFull / chgPowerKW;
						
						String cSt = ""+hhWt+","+homeChgType.toString()+","+kWhToFull+","+0f+","+chgHrs+","+24f;
						foutC.append(cSt+lsep);
						foutC.flush();
						
						relSoC = 1f;
					}
				}
			}
		}

		fout.close();
		foutC.close();
		stWin.println("Completed Simulations for "+caseName);
	}
	private void runCase_BEV_onlyOvernightCharging(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_BEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		String chgFileName = fChgFileName(sID, vehModelID, lcID);
		
		String solFileHeader = "kWh";
		String[] repVehicleShortName = wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName;
		for (int i=0; i<repVehicleShortName.length; i++) solFileHeader = solFileHeader + ",fuelAmount_" + repVehicleShortName[i];
		
		FileWriter fout = new FileWriter(solFileName);
		FileWriter foutC = new FileWriter(chgFileName);
		String lsep = System.getProperty("line.separator");
		
		fout.append(solFileHeader+lsep);
		foutC.append(ChargingEvents.FHeader+lsep);
		
		int[] repVehIDs = new int[repVehicleShortName.length];
		for (int i=0; i<repVehIDs.length; i++) {
			for (int j=0; j<avms.numVehModels(); j++) {
				if (repVehicleShortName[i].equalsIgnoreCase(avms.vehModelsSetup()[j].shortName)) {
					repVehIDs[i] = j;
					break;
				}
			}
		}
		
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);

		FASTSimJ3c[] fsjRepVehicles = new FASTSimJ3c[repVehIDs.length];
		for (int i=0; i<fsjRepVehicles.length; i++) {
			fsjRepVehicles[i] = new FASTSimJ3c();
			fsjRepVehicles[i].setVehModel(fsofModels[repVehIDs[i]].vehModelParam, fsofModels[repVehIDs[i]].curveMan,
					fsofModels[repVehIDs[i]].addMassKg, fsofModels[repVehIDs[i]].addAuxKW, fsofModels[repVehIDs[i]].adjDEMult);
		}		
		
		float relSoC = 1f;
		float batterySwingKWh = fsofModels[vehModelID].vehModelParam.batterySwingKWh();
		float nomAER = avms.vehModelsSetup()[vehModelID].nominalAERMiles;
		int rangeAnxID = bevLocalCaseID_to_anxID(lcID);
		
		ChargingEvents.ChargingEventType homeChgType = ChargingEvents.ChargingEventType.overNight_L1;
		if (wiitMod.chgModels.bevHomesHaveL2) homeChgType = ChargingEvents.ChargingEventType.overNight_L2;		

		for (int i=0; i<vs.length; i++) {
			float hhWt = vs[i].vehSampleInfo().hhWt;
			
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			TripDaysMarker dMarker = new TripDaysMarker(trips);
			
			for (int j=0; j<dMarker.daysMarker.length; j++) {	//Loop on days
				float remMilesKinematic = relSoC * nomAER;
				float anxMiles = wiitMod.bevRepModel.bevRepWholeDay.calcRangeAnx(remMilesKinematic,
						wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles[rangeAnxID]);
				float drivableMilesOnBEV = remMilesKinematic - anxMiles;
				float sumDayMiles = 0f;
				for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) sumDayMiles += trips[k].tripIDs().miles;
				
				if (drivableMilesOnBEV >= sumDayMiles) {
					//BEV will be driven for the day's trips
					boolean ranOutOfBattery = false;
					
					for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
						
						//FASTSim simulations for trips of the day
						float[] mph = trips[k].speedMPH();
						float[] fltGrade = trips[k].fltGrade();
						float[] oAuxKw = trips[k].recAuxKW();
						float[] payloadKg = trips[k].payloadKg();
						
						fsj.setRelSoC(relSoC);
						fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
						
						float electKWhUse = fsj.lastTripSummary().batteryUse;						
						relSoC = fsj.lastTripSummary().finalRelSoC;
						if (relSoC < 0) ranOutOfBattery = true;
						
						String sSt = "";
						if (ranOutOfBattery) {
							sSt = "-1";
							for (int l=0; l<repVehIDs.length; l++) sSt = sSt + ",-1";
						} else {
							sSt = sSt + electKWhUse;
							for (int l=0; l<repVehIDs.length; l++) sSt = sSt + ",-1";
						}
						fout.append(sSt+lsep);
						fout.flush();
						
						if (stWin.abortRequested()) {
							fout.close();
							foutC.close();
							return;
						}
					}
					
					//Charging event at the end of the day 
					float overnightChgEventWindowSeconds = FDefaults.hrsChgAfterlastTripOfLastDay * ChargingEvents.SecondsPerHour;
					if (j < (dMarker.daysMarker.length - 1)) {
						overnightChgEventWindowSeconds = trips[dMarker.daysMarker[j+1].idFirstTripOfDay].tripIDs().secsFromLastTrip;
					}
					
					ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(homeChgType, wiitMod, 
							overnightChgEventWindowSeconds, relSoC, batterySwingKWh);
					
					//Set battery SoC to the value from charging event
					relSoC = chgRes.finalRelSoC;
					
					//Write data of charging event to file
					if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
						float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, overnightChgEventWindowSeconds);
						float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
						if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
						
						String cSt = ""+hhWt+","+homeChgType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
								trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
								+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
								","+chgRes.minDurationHrs+","+hrsSlack;
						foutC.append(cSt+lsep);
						foutC.flush();
					}				
				} else {
					//Replacement Vehicles will be driven for the day's trips
					for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
						String sSt = "-1";

						float[] mph = trips[k].speedMPH();
						float[] fltGrade = trips[k].fltGrade();
						float[] oAuxKw = trips[k].recAuxKW();
						float[] payloadKg = trips[k].payloadKg();
						
						for (int l=0; l<fsjRepVehicles.length; l++) {
							fsjRepVehicles[l].runC(null, mph, fltGrade, oAuxKw, payloadKg);
							float fuelUse = fsjRepVehicles[l].lastTripSummary().fuelUse;
							sSt = sSt + "," + fuelUse;
						}
						
						fout.append(sSt+lsep);
						fout.flush();
						
						if (stWin.abortRequested()) {
							fout.close();
							foutC.close();
							return;
						}
					}
					
					//Replacement vehicles used for current day... if BEV wasn't at full, top it off with a charging event that can happen any time
					if (relSoC < 1f) {
						float kWhToFull = (1f - relSoC)*batterySwingKWh;
						float chgPowerKW = wiitMod.chgModels.l1PowerKWtoBattery;
						if (homeChgType == ChargingEvents.ChargingEventType.overNight_L2) chgPowerKW = wiitMod.chgModels.l2PowerKWtoBattery;
						float chgHrs = kWhToFull / chgPowerKW;
						
						String cSt = ""+hhWt+","+homeChgType.toString()+","+kWhToFull+","+0f+","+chgHrs+","+24f;
						foutC.append(cSt+lsep);
						foutC.flush();
						
						relSoC = 1f;
					}
				}
			}
		}

		fout.close();
		foutC.close();
		stWin.println("Completed Simulations for "+caseName);
	}
	private void runCase_BEV_infBattery(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_BEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		
		FileWriter fout = new FileWriter(solFileName);
		String lsep = System.getProperty("line.separator");		
		fout.append("kWh"+lsep);
		
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);
		
		for (int i=0; i<vs.length; i++) {
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			for (int j=0; j<trips.length; j++) {
				float[] mph = trips[j].speedMPH();
				float[] fltGrade = trips[j].fltGrade();
				float[] oAuxKw = trips[j].recAuxKW();
				float[] payloadKg = trips[j].payloadKg();
				
				fsj.setRelSoC(1f);
				fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
				float kWh = fsj.lastTripSummary().batteryUse;
				
				fout.append(""+kWh+lsep);
				fout.flush();
				
				if (stWin.abortRequested()) {
					fout.close();
					return;
				}
			}
		}

		fout.close();
		stWin.println("Completed Simulations for "+caseName);
	}	
	private void runCase_PHEV_daytimeCharging(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_PHEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		String chgFileName = fChgFileName(sID, vehModelID, lcID);
		
		FileWriter fout = new FileWriter(solFileName);
		FileWriter foutC = new FileWriter(chgFileName);
		String lsep = System.getProperty("line.separator");	
		
		fout.append("kWh,fuelAmount"+lsep);
		foutC.append(ChargingEvents.FHeader+lsep);
				
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);
		
		float relSoC = 1f;
		float batterySwingKWh = fsofModels[vehModelID].vehModelParam.batterySwingKWh();
		int dtChgWindowID = phevLocalCaseID_to_dayTimeWinowID(lcID);
		float minHrsForDTChgEvent = wiitMod.chgModels.daytimeChargingMinWindow[dtChgWindowID];
		float nomAER = avms.vehModelsSetup()[vehModelID].nominalAERMiles;
		
		ChargingEvents.ChargingEventType homeChgType = ChargingEvents.ChargingEventType.overNight_L1;
		if (wiitMod.chgModels.phevHomesHaveL2) homeChgType = ChargingEvents.ChargingEventType.overNight_L2;		
		
		for (int i=0; i<vs.length; i++) {
			float hhWt = vs[i].vehSampleInfo().hhWt;
			
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			TripDaysMarker dMarker = new TripDaysMarker(trips);
			
			for (int j=0; j<dMarker.daysMarker.length; j++) {	//Loop on days
				//Create charging plan for the day
				DayTimeChargingPlanner.DTChargingPlan dtChgPlan = DayTimeChargingPlanner.phev_daytimeChgPlan(trips, dMarker.daysMarker[j].idFirstTripOfDay,
						dMarker.daysMarker[j].idLastTripOfDay, relSoC, batterySwingKWh, nomAER, minHrsForDTChgEvent, wiitMod);
				
				for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
					//Check if day-time charging event happens
					ChargingEvents.ChargingEventType chgEventType = dtChgPlan.chgEventBeforeTrip(k);

					if (chgEventType != null) {
						float chgEventWindowSeconds = trips[k].tripIDs().secsFromLastTrip;
						ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(chgEventType, wiitMod, 
								chgEventWindowSeconds, relSoC, batterySwingKWh);
						
						//Set battery SoC to the value from charging event
						relSoC = chgRes.finalRelSoC;
						
						//Write data of charging event to file
						if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
							float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, chgEventWindowSeconds);
							float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
							if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
							
							String cSt = ""+hhWt+","+chgEventType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
									trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
									trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
									trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
									+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
									","+chgRes.minDurationHrs+","+hrsSlack;
							foutC.append(cSt+lsep);
							foutC.flush();
						}
					}
					
					//FASTSim simulations for trips of the day
					float[] mph = trips[k].speedMPH();
					float[] fltGrade = trips[k].fltGrade();
					float[] oAuxKw = trips[k].recAuxKW();
					float[] payloadKg = trips[k].payloadKg();
					
					fsj.setRelSoC(relSoC);
					fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
					
					float fueluse = fsj.lastTripSummary().fuelUse;
					if (fueluse < 0) fueluse = 0f;
					
					float electKWhUse = fsj.lastTripSummary().batteryUse;
					if (electKWhUse < 0) electKWhUse = 0f;
					
					relSoC = fsj.lastTripSummary().finalRelSoC;
					
					fout.append(""+electKWhUse+","+fueluse+lsep);
					fout.flush();
					
					if (stWin.abortRequested()) {
						fout.close();
						foutC.close();
						return;
					}
				}
				
				//Overnight Charging event
				float overnightChgEventWindowSeconds = FDefaults.hrsChgAfterlastTripOfLastDay * ChargingEvents.SecondsPerHour;
				if (j < (dMarker.daysMarker.length - 1)) {
					overnightChgEventWindowSeconds = trips[dMarker.daysMarker[j+1].idFirstTripOfDay].tripIDs().secsFromLastTrip;
				}
				
				ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(homeChgType, wiitMod, 
						overnightChgEventWindowSeconds, relSoC, batterySwingKWh);
				
				//Set battery SoC to the value from charging event
				relSoC = chgRes.finalRelSoC;
				
				//Write data of charging event to file
				if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
					float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, overnightChgEventWindowSeconds);
					float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
					if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
					
					String cSt = ""+hhWt+","+homeChgType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
							+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
							","+chgRes.minDurationHrs+","+hrsSlack;
					foutC.append(cSt+lsep);
					foutC.flush();
				}
			}
		}

		fout.close();
		foutC.close();
		stWin.println("Completed Simulations for "+caseName);
	}
	private void runCase_PHEV_onlyOvernightCharging(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_PHEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		String chgFileName = fChgFileName(sID, vehModelID, lcID);
		
		FileWriter fout = new FileWriter(solFileName);
		FileWriter foutC = new FileWriter(chgFileName);
		String lsep = System.getProperty("line.separator");	
		
		fout.append("kWh,fuelAmount"+lsep);
		foutC.append(ChargingEvents.FHeader+lsep);
				
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);
		
		float relSoC = 1f;
		float batterySwingKWh = fsofModels[vehModelID].vehModelParam.batterySwingKWh();
		
		ChargingEvents.ChargingEventType chgEventType = ChargingEvents.ChargingEventType.overNight_L1;
		if (wiitMod.chgModels.phevHomesHaveL2) chgEventType = ChargingEvents.ChargingEventType.overNight_L2;
		
		for (int i=0; i<vs.length; i++) {
			float hhWt = vs[i].vehSampleInfo().hhWt;
			
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			TripDaysMarker dMarker = new TripDaysMarker(trips);
			
			for (int j=0; j<dMarker.daysMarker.length; j++) {	//Loop on days
				for (int k=dMarker.daysMarker[j].idFirstTripOfDay; k<=dMarker.daysMarker[j].idLastTripOfDay; k++) { //loop on trips of day
					//FASTSim simulations for trips of the day
					float[] mph = trips[k].speedMPH();
					float[] fltGrade = trips[k].fltGrade();
					float[] oAuxKw = trips[k].recAuxKW();
					float[] payloadKg = trips[k].payloadKg();
					
					fsj.setRelSoC(relSoC);
					fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
					
					float fueluse = fsj.lastTripSummary().fuelUse;
					if (fueluse < 0) fueluse = 0f;
					
					float electKWhUse = fsj.lastTripSummary().batteryUse;
					if (electKWhUse < 0) electKWhUse = 0f;
					
					relSoC = fsj.lastTripSummary().finalRelSoC;
					
					fout.append(""+electKWhUse+","+fueluse+lsep);
					fout.flush();
					
					if (stWin.abortRequested()) {
						fout.close();
						foutC.close();
						return;
					}
				}
				
				//Charging event
				float overnightChgEventWindowSeconds = FDefaults.hrsChgAfterlastTripOfLastDay * ChargingEvents.SecondsPerHour;
				if (j < (dMarker.daysMarker.length - 1)) {
					overnightChgEventWindowSeconds = trips[dMarker.daysMarker[j+1].idFirstTripOfDay].tripIDs().secsFromLastTrip;
				}
				
				ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(chgEventType, wiitMod, 
						overnightChgEventWindowSeconds, relSoC, batterySwingKWh);
				
				//Set battery SoC to the value from charging event
				relSoC = chgRes.finalRelSoC;
				
				if (chgRes.kWhAmountToBattery > ZeroChgKWh) {
					float availableChgHrs = ChargingEvents.availableChgHrs(wiitMod, overnightChgEventWindowSeconds);
					float hrsSlack = availableChgHrs - chgRes.minDurationHrs;
					if (hrsSlack < ZeroChgHrs) hrsSlack = 0f;
					
					String cSt = ""+hhWt+","+chgEventType.toString()+","+chgRes.kWhAmountToBattery+","+ChargingEvents.regHr24_from_h24_mm_ss(
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().hr24, 
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().min, 
							trips[dMarker.daysMarker[j].idLastTripOfDay].tripIDs().sec 
							+ (int)(wiitMod.chgModels.chgDeadTime_minutesBeforeConnect * ChargingEvents.SecondsPerMinute+ 0.5f))+
							","+chgRes.minDurationHrs+","+hrsSlack;
					foutC.append(cSt+lsep);
					foutC.flush();
				}
			}
		}

		fout.close();
		foutC.close();
		stWin.println("Completed Simulations for "+caseName);
	}
	private void runCase_PHEV_nonCharging(int sID, int vehModelID, int lcID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String caseName = fileNameWOExt_PHEV(avms.vehModelsSetup()[vehModelID].shortName, lcID);
		String solFileName = fSolFileName(sID, vehModelID, lcID);
		
		FileWriter fout = new FileWriter(solFileName);
		String lsep = System.getProperty("line.separator");		
		fout.append("fuelAmount"+lsep);		
		
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);
		
		float relSoC = 0.001f;
		
		for (int i=0; i<vs.length; i++) {
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			for (int j=0; j<trips.length; j++) {
				float[] mph = trips[j].speedMPH();
				float[] fltGrade = trips[j].fltGrade();
				float[] oAuxKw = trips[j].recAuxKW();
				float[] payloadKg = trips[j].payloadKg();
				
				fsj.setRelSoC(relSoC);
				fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
				
				float fueluse = fsj.lastTripSummary().fuelUse;
				relSoC = fsj.lastTripSummary().finalRelSoC;
				
				fout.append(""+fueluse+lsep);
				fout.flush();
				
				if (stWin.abortRequested()) {
					fout.close();
					return;
				}
			}
		}

		fout.close();
		stWin.println("Completed Simulations for "+caseName);
	}
	private void runCase_ICE_HEV(int sID, int vehModelID, VehicleSampleMA[] vs, RunStatusWindow stWin) throws Exception {
		String solFileName = fSolFileName(sID, vehModelID, 0);
		
		FileWriter fout = new FileWriter(solFileName);
		String lsep = System.getProperty("line.separator");		
		fout.append("fuelAmount"+lsep);
		
		FASTSimJ3c fsj = new FASTSimJ3c();
		fsj.setVehModel(fsofModels[vehModelID].vehModelParam, fsofModels[vehModelID].curveMan,
				fsofModels[vehModelID].addMassKg, fsofModels[vehModelID].addAuxKW, fsofModels[vehModelID].adjDEMult);
		
		for (int i=0; i<vs.length; i++) {
			VehicleSampleMA.Trip[] trips = vs[i].trips();
			for (int j=0; j<trips.length; j++) {
				float[] mph = trips[j].speedMPH();
				float[] fltGrade = trips[j].fltGrade();
				float[] oAuxKw = trips[j].recAuxKW();
				float[] payloadKg = trips[j].payloadKg();
				
				fsj.runC(null, mph, fltGrade, oAuxKw, payloadKg);
				float fueluse = fsj.lastTripSummary().fuelUse;
				
				fout.append(""+fueluse+lsep);
				fout.flush();
				
				if (stWin.abortRequested()) {
					fout.close();
					return;
				}
			}
		}

		fout.close();
		stWin.println("Completed Simulations for "+avms.vehModelsSetup()[vehModelID].shortName);
	}	
	
	private static class TripDaysMarker {
		private StartEndMarkerForTripsArray[] daysMarker;
		
		private TripDaysMarker(VehicleSampleMA.Trip[] trips) {
			ArrayList<StartEndMarkerForTripsArray> lst = new ArrayList<StartEndMarkerForTripsArray>();
			
			int curDayID = -1;
			for (int i=0; i<trips.length; i++) {
				int dayID = trips[i].tripIDs().dayID;
				
				if (dayID > curDayID) {
					if (curDayID >= 0) lst.get(curDayID).idLastTripOfDay = i-1;
					
					curDayID++;
					lst.add(new StartEndMarkerForTripsArray());
					lst.get(curDayID).idFirstTripOfDay = i;
				}
			}
			
			lst.get(lst.size() - 1).idLastTripOfDay = trips.length-1;
			
			daysMarker = new StartEndMarkerForTripsArray[lst.size()];
			for (int i=0; i<daysMarker.length; i++) {
				daysMarker[i] = lst.get(i);
			}
		}
		
		private static class StartEndMarkerForTripsArray {
			private int idFirstTripOfDay, idLastTripOfDay;
			private StartEndMarkerForTripsArray() {}
		}
	}
}
