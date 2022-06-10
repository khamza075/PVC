package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.*;
import pvc.calc.UsePhaseSSimulator;
import pvc.datamgmt.comp.*;
import pvc.utility.*;

public class SliderBarsManager {
	public static final float HrsDayTimeChgWindowEqToOvernightOnly = 12f;
	
	public static float annualMilesTravelledIncCurSolSet = FDefaults.annualVMT;			//Must be updated on loading and when user changes solution set 
	public static float daysOfTravelPerYearIncCurSolSet = FDefaults.annualDriveDays;	//Must be updated on loading and when user changes solution set
	
	private FFStructure fs;
	private int aID;
	private AnalysisVehModelsSetup avms;
	private WIITModel wiitMod;
	private BEVCommercialModel bevMoreCommVeh;
	
	private SliderBarSetup[] sbars;
	public SliderBarSetup[] sbars() {return sbars;}
	
	private int[] bIDtoOID;
	
	private ArrayList<UserShowingInfo> lstSI;
	
	private RVStatus rvStatus;
	public RVStatus rvStatus() {return rvStatus;}
	
	public float getCurValue(APSliderBars qantityType) {
		int bID = qantityType.ordinal();
		int oID = bIDtoOID[bID];
		if (oID < 0) return 0f;
		
		float cValue = rvStatus.avsbValues[oID];
		return sbars[bID].getCValue(cValue);
	}
	
	public void prepUPInputs(UsePhaseSSimulator.InputStructure upsInput, UsePhaseSSimulator upSim, float lifetimeMilesLCA,
			float[] nonBatMfgGHG_lowEnd, float[] nonBatMfgGHG_highEnd, float bevRep_dollarPerMile, float bevRep_dollarPerDay) {
		
		if (upSim.mfgGHGInputsNeeded()) {
			float sbarValue_nonBatMfg = getCurValue(APSliderBars.MfgGHG_exceptBat);
			float sbarValue_BatMfgReduction = getCurValue(APSliderBars.MfgGHG_Battery);
			AnalysisVehModelsSetup.AVehModelSetup[] avs = avms.vehModelsSetup();
			
			upsInput.setIncludeMfgGHG(rvStatus.includeMfgGHG);
			upsInput.setLifetimeMilesLCA(lifetimeMilesLCA);
			
			for (int i= 0; i<avs.length; i++) {
				upsInput.setNonBatteryMfgGHGperKgVeh(nonBatMfgGHG_lowEnd[i] + sbarValue_nonBatMfg*(nonBatMfgGHG_highEnd[i] - nonBatMfgGHG_lowEnd[i]), i);
				upsInput.setBatteryMfgGHGperKWhBattery(sbarValue_BatMfgReduction*avs[i].mfgGHG_gCO2perKWhBattery, i);
			}
		}
		
		UsePhaseSSimulator.APNeededInputs[] neededInputs = upSim.neededInputs();
		
		for (int i=0; i<neededInputs.length; i++) {
			UsePhaseSSimulator.APNeededInputs iType = neededInputs[i];
			
			switch (iType) {
			case avDaysDrivenPerYear:
				upsInput.set(iType, daysOfTravelPerYearIncCurSolSet*getCurValue(APSliderBars.Cost_AnnualDriveDays));
				break;
			case avMilesDrivenPerYear:
				upsInput.set(iType, annualMilesTravelledIncCurSolSet*getCurValue(APSliderBars.Cost_AnnualDriveDistance));
				break;
			case bevRangeAnxID:
				upsInput.set(iType, getCurValue(APSliderBars.Bhv_BEVRep_anxID));
				break;
			case bevRepCost_dollarPerDay:
				upsInput.set(iType, bevRep_dollarPerDay);
				break;
			case bevRepCost_dollarPerMile:
				upsInput.set(iType, bevRep_dollarPerMile);
				break;
			case bevRepVehID:
				upsInput.set(iType, getCurValue(APSliderBars.Bhv_BEVRep_vehID));
				break;
			case chgTimingOptID:
				upsInput.set(iType, getCurValue(APSliderBars.Bhv_ChgTimePref));
				break;
			case chgWindowID:
			{
				//Revert the charging time from hours to an ID (with 0 == overnight only and 1+ are the daytime windows)
				float chgTimeHrs = getCurValue(APSliderBars.Bhv_ChgWindow);
				float zTol = 0.001f;
				float chgID = 0f;
				
				if ((FDefaults.hrsChgAfterlastTripOfLastDay - chgTimeHrs) > zTol) {
					int daytimeChgWindowID = 0;
					
					while (daytimeChgWindowID < wiitMod.chgModels.daytimeChargingMinWindow.length) {
						if (wiitMod.chgModels.daytimeChargingMinWindow[daytimeChgWindowID] <= chgTimeHrs) break;
						daytimeChgWindowID++;
					}
					
					if (daytimeChgWindowID >= wiitMod.chgModels.daytimeChargingMinWindow.length) {
						chgID = wiitMod.chgModels.daytimeChargingMinWindow.length;
					}
					else if (daytimeChgWindowID == 0) {
						float y1 = FDefaults.hrsChgAfterlastTripOfLastDay;
						float y2 = wiitMod.chgModels.daytimeChargingMinWindow[daytimeChgWindowID];
						chgID = 1f - (chgTimeHrs - y2)/(y1 - y2);
					} else {
						float y1 = wiitMod.chgModels.daytimeChargingMinWindow[daytimeChgWindowID-1];
						float y2 = wiitMod.chgModels.daytimeChargingMinWindow[daytimeChgWindowID];
						chgID = daytimeChgWindowID + 1f - (chgTimeHrs - y2)/(y1 - y2);
					}
				}
				upsInput.set(iType, chgID);
			}
				break;
			case dcFastPricePremium:
				upsInput.set(iType, getCurValue(APSliderBars.Price_DCPremium));
				break;
			case dieselGHG_gCO2PerGal:
				upsInput.set(iType, getCurValue(APSliderBars.GHG_Diesel));
				break;
			case dieselPrice_dollarPerGal:
				upsInput.set(iType, getCurValue(APSliderBars.Price_Diesel));
				break;
			case fracNonChgPHEVs:
				upsInput.set(iType, getCurValue(APSliderBars.Bhv_FracNonChgPHEVs));
				break;
			case gasGHG_gCO2PerGal:
				upsInput.set(iType, getCurValue(APSliderBars.GHG_Gas));
				break;
			case gasPrice_dollarPerGal:
				upsInput.set(iType, getCurValue(APSliderBars.Price_Gas));
				break;
			case gridAvGHG_gCO2perKWh:
				upsInput.set(iType, getCurValue(APSliderBars.GHG_Elect));
				break;
			case gridAvPrice_dollarPerKWh:
				upsInput.set(iType, getCurValue(APSliderBars.Price_Elect));
				break;
			case h2GHG_gCO2PerKg:
				upsInput.set(iType, getCurValue(APSliderBars.GHG_H2));
				break;
			case h2Price_dollarPerKg:
				upsInput.set(iType, getCurValue(APSliderBars.Price_H2));
				break;
			case ngGHG_gCO2PerM3:
				upsInput.set(iType, getCurValue(APSliderBars.GHG_NG));
				break;
			case ngPrice_dollarPerM3:
				upsInput.set(iType, getCurValue(APSliderBars.Price_NG));
				break;
			}			
		}
	}
	
	
	public SliderBarsManager(FFStructure cFS, int analysisID, AnalysisVehModelsSetup vehModelsSetup, WIITModel wiitModel, 
			BEVCommercialModel bevMoreCommercialVeh) {
		fs = cFS;
		aID = analysisID;
		avms = vehModelsSetup;
		wiitMod = wiitModel;
		bevMoreCommVeh = bevMoreCommercialVeh;
		
		sbars = new SliderBarSetup[APSliderBars.values().length];
		
		readFromSaved();
		supplimentFromWBaseModels();
		
		lstSI = new ArrayList<UserShowingInfo>();
		readOrderFromSaved();
		checkShowingInfo();
		
		bIDtoOID = new int[sbars.length];
		update_bIDtoOID();	
		
		rvStatus = new RVStatus();
		
		save();
	}
	
	public float adjustedFracAdditionalCommercialBEVs(float nominalFracExtraVeh, int nominalRangeMiles) {
		return bevMoreCommVeh.fracExtraVehicles(nominalFracExtraVeh, nominalRangeMiles);
	}
	
	private void update_bIDtoOID() {
		for (int i=0; i<bIDtoOID.length; i++) bIDtoOID[i] = -1;
		for (int i=0; i<lstSI.size(); i++) {
			bIDtoOID[lstSI.get(i).bID] = i;
		}
	}
	
	public boolean hasMfgGHG() {
		if (sbars[APSliderBars.MfgGHG_exceptBat.ordinal()] == null) return false;
		return true;
	}
	
	public SliderBarSetup getSBar(int oID) {
		return sbars[lstSI.get(oID).bID];
	}
	public boolean getSBarUserShow(int oID) {
		return lstSI.get(oID).userSelShowBar;
	}
	public void setSBarUserShow(int oID, boolean showBar) {
		lstSI.get(oID).userSelShowBar = showBar;		
		if (!showBar) rvStatus.resetToBaseline(oID);
	}
	public String[] unitsFormattedCaptions() {
		String[] st = new String[lstSI.size()];
		for (int i=0; i<st.length; i++) st[i] = unitsFormattedCaption(i);
		return st;
	}
	public int numVisibleSBars() {
		return lstSI.size();
	}
	public void moveSBarUp(int oID) {
		UserShowingInfo curSelected = lstSI.get(oID);
		lstSI.remove(oID);
		lstSI.add(oID-1, curSelected);
		
		float tmp = rvStatus.avsbValues[oID-1];
		rvStatus.avsbValues[oID-1] = rvStatus.avsbValues[oID];
		rvStatus.avsbValues[oID] = tmp;
		
		update_bIDtoOID();		
	}
	public void moveSBarDown(int oID) {
		UserShowingInfo curSelected = lstSI.get(oID);
		lstSI.remove(oID);
		lstSI.add(oID+1, curSelected);
		
		float tmp = rvStatus.avsbValues[oID+1];
		rvStatus.avsbValues[oID+1] = rvStatus.avsbValues[oID];
		rvStatus.avsbValues[oID] = tmp;
		
		update_bIDtoOID();		
	}
	
	public String unitsFormattedCaption(int oID) {
		return apUnitsFormattedCaption(lstSI.get(oID).bID);
	}
	public String unitsFormattedValue(int oID, float sbarValue) {
		return apUnitsFormattedValue(lstSI.get(oID).bID, sbarValue);
	}
	public float unitsAdjustedValue(int oID, float sbarValue) {
		return apUnitsAdjustedValue(lstSI.get(oID).bID, sbarValue);
	}
	public float toSliderBarValue(int oID, float unitsAdjutedValue) {
		return apUnitsAdjustedValueToSBValue(lstSI.get(oID).bID, unitsAdjutedValue);
	}
	
	public class RVStatus {
		private int solSetID;
		public int solSetID() {return solSetID;}
		public void setSolSetID(int value) {solSetID = value;}
		
		private boolean contIntMode;
		public boolean contIntMode() {return contIntMode;}
		public void setContIntMode(boolean value) {contIntMode = value;}
		
		private boolean includeMfgGHG;
		public boolean includeMfgGHG() {return includeMfgGHG;}
		public void setIncludeMfgGHG(boolean value) {includeMfgGHG = value;}
		
		private float[] avsbValues;
		public float[] avsbValues() {return avsbValues;}
		public void setAVSBValue(int oID, float value) {avsbValues[oID] = value;}
		
		public void resetToBaseline(int oID) {rvStatus.avsbValues[oID] = getSBar(oID).baselineIDinArray();}
		
		public void writeSummaryInFWriter(FileWriter fout) throws Exception {
			String lsep = System.getProperty("line.separator");

			for (int i=0; i<avsbValues.length; i++) {
				fout.append(unitsFormattedCaption(i)+","+unitsFormattedValue(i, getSBar(i).getCValue(avsbValues[i]))+lsep);
			}
		}


		private RVStatus() {
			//Initialize to defaults
			solSetID = 0;
			contIntMode = false;
			includeMfgGHG = false;
			
			avsbValues = new float[numVisibleSBars()];
			for (int i=0; i<avsbValues.length; i++) {
				avsbValues[i] = getSBar(i).baselineIDinArray();
			}

			//Try loading different values from file
			attemptToRead();
		}
		private void attemptToRead() {
			String fname = fs.getFilePath_ResultsVisualizationStatus(aID);

			try {
				BufferedReader fin = new BufferedReader(new FileReader(fname));
				
				try {
					String readLine = fin.readLine();
					solSetID = Integer.parseInt(readLine);
					
					FEcoSimsC fecoSims = new FEcoSimsC(fs, aID);
					if (solSetID >= fecoSims.numCompletedSims()) solSetID = fecoSims.numCompletedSims() - 1;
					
				} catch (Exception e) {}
				try {
					String readLine = fin.readLine();
					contIntMode = Boolean.parseBoolean(readLine);
				} catch (Exception e) {}
				try {
					String readLine = fin.readLine();
					includeMfgGHG = Boolean.parseBoolean(readLine);
				} catch (Exception e) {}
				
				for (int i=0; i<avsbValues.length; i++) {
					try {
						String readLine = fin.readLine();
						avsbValues[i] = Float.parseFloat(readLine);
					} catch (Exception e) {}
				}
				
				fin .close();
			} catch (Exception e) {}
		}		
		public void save() {
			String fname = fs.getFilePath_ResultsVisualizationStatus(aID);
			
			try {
				FileWriter fout = new FileWriter(fname);
				String lsep = System.getProperty("line.separator");
				
				fout.append(""+solSetID+lsep);
				fout.append(""+contIntMode+lsep);
				fout.append(""+includeMfgGHG+lsep);
				
				for (int i=0; i<avsbValues.length; i++) {
					fout.append(""+avsbValues[i]+lsep);
				}
				
				fout.flush();
				fout.close();
			} catch (Exception e) {}
		}
	}
		
	private static class UserShowingInfo {
		private int bID;
		private boolean userSelShowBar;

		private UserShowingInfo(String readLine) {
			String[] sp  = readLine.split(",");
			bID = Integer.parseInt(sp[0]);
			userSelShowBar = Boolean.parseBoolean(sp[1]);
		}
		private UserShowingInfo(SliderBarSetup sbar) {
			bID = sbar.mDesignation().ordinal();
			if (sbar.isSlidable()) userSelShowBar = true;
			else userSelShowBar = false;
		}
		
		@Override public String toString() {
			return ""+bID+","+userSelShowBar;
		}
	}
	
	private void readOrderFromSaved() {
		String fname = fs.getFilePath_sliderbarsOrdering(aID);
		File f = new File(fname);
		if (!(f.exists() && f.isFile())) return;
		
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine;
			
			while ((readLine=fin.readLine())!=null) {
				try {
					lstSI.add(new UserShowingInfo(readLine));
				} catch (Exception e) {}
			}
			
			fin.close();
		} catch (Exception e) {}
	}
	private void checkShowingInfo() {
		boolean showingInfoIsValid = true;
		
		int numNonNull = 0;
		for (int i=0; i<sbars.length; i++) {
			if (sbars[i] != null) numNonNull++;
		}
		if (numNonNull != lstSI.size()) showingInfoIsValid = false;
		
		if (showingInfoIsValid) {
			for (int i=0; i<lstSI.size(); i++) {
				if (sbars[lstSI.get(i).bID] == null) {
					showingInfoIsValid = false;
					break;
				}
				
				if (lstSI.get(i).userSelShowBar && (!sbars[lstSI.get(i).bID].isSlidable())) {
					lstSI.get(i).userSelShowBar = false;
				}
			}
		}
		
		if (!showingInfoIsValid) {
			lstSI = new ArrayList<UserShowingInfo>();
			
			for (int i=0; i<sbars.length; i++) {
				if (sbars[i] != null) lstSI.add(new UserShowingInfo(sbars[i]));
			}
		}
	}
	
	private void readFromSaved() {
		String fname = fs.getFilePath_sliderbarsSetup(aID);
		File f = new File(fname);
		if (!(f.exists() && f.isFile())) return;
		
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine;
			
			while ((readLine=fin.readLine())!=null) {
				try {
					SliderBarSetup csbar = new SliderBarSetup(readLine, fin);
					sbars[csbar.mDesignation().ordinal()] = csbar;
				} catch (Exception e) {}
			}
			
			fin.close();
		} catch (Exception e) {}
	}
	public void save() {
		String fname = fs.getFilePath_sliderbarsSetup(aID);
		try {
			FileWriter fout = new FileWriter(fname);
			
			for (int i=0; i<sbars.length; i++) {
				try {
					if (sbars[i] != null) sbars[i].writeToFileStream(fout);
				} catch (Exception e) {}
			}
			
			fout.close();
		} catch (Exception e) {}
		
		fname = fs.getFilePath_sliderbarsOrdering(aID);
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			for (int i=0; i<lstSI.size(); i++) {
				try {
					fout.append(lstSI.get(i).toString()+lsep);
				} catch (Exception e) {}
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
		
		rvStatus.save();
	}
	private boolean dcFastChargingIncluded() {
		if (!wiitMod.chgModels.dcFastAvailable) return false;
		
		if (wiitMod.hasBEVs()) {
			if (wiitMod.bevRepModel.bevRepCommercial == null) return true;
		}
		if (wiitMod.chgModels.minNomAERForPHEVsToHaveDCFast > 0) {
			AnalysisVehModelsSetup.AVehModelSetup[] avm = avms.vehModelsSetup();
			for (int i=0; i<avm.length; i++) {
				String ofvmFileName = fs.getFilePath_FASTSimVehModel(aID, avm[i].shortName);
				FSJOneFileVehModel ofvModel = new FSJOneFileVehModel(ofvmFileName);
				switch (ofvModel.vehModelParam.general.vehPtType) {
				case phev:
					if (avm[i].nominalAERMiles >= wiitMod.chgModels.minNomAERForPHEVsToHaveDCFast) return true;
					break;
				default:
					break;				
				}
			}
		}		
		return false;
	}
	private void supplimentFromWBaseModels() {
		if ((sbars[APSliderBars.Cost_GasICE.ordinal()] == null) && (wiitMod.bmGasICE != null)) {
			sbars[APSliderBars.Cost_GasICE.ordinal()] = new SliderBarSetup(APSliderBars.Cost_GasICE, wiitMod.bmGasICE.dollarPerKWmodel());
		}
		if ((sbars[APSliderBars.Cost_DieselICE.ordinal()] == null) && (wiitMod.bmDieselICE != null)) {
			sbars[APSliderBars.Cost_DieselICE.ordinal()] = new SliderBarSetup(APSliderBars.Cost_DieselICE, wiitMod.bmDieselICE.dollarPerKWmodel());
		}
		if ((sbars[APSliderBars.Cost_NGICE.ordinal()] == null) && (wiitMod.bmCNGICE != null)) {
			sbars[APSliderBars.Cost_NGICE.ordinal()] = new SliderBarSetup(APSliderBars.Cost_NGICE, wiitMod.bmCNGICE.dollarPerKWmodel());
		}
		if ((sbars[APSliderBars.Cost_FuelCell.ordinal()] == null) && (wiitMod.bmFuelCell != null)) {
			sbars[APSliderBars.Cost_FuelCell.ordinal()] = new SliderBarSetup(APSliderBars.Cost_FuelCell, wiitMod.bmFuelCell.dollarPerKWmodel());
		}
		if ((sbars[APSliderBars.Cost_Motor.ordinal()] == null) && (wiitMod.bmMotor != null)) {
			sbars[APSliderBars.Cost_Motor.ordinal()] = new SliderBarSetup(APSliderBars.Cost_Motor, wiitMod.bmMotor.dollarPerKWmodel());
		}

		if ((sbars[APSliderBars.Cost_H2Tank.ordinal()] == null) && (wiitMod.bmH2Tank != null)) {
			sbars[APSliderBars.Cost_H2Tank.ordinal()] = new SliderBarSetup(APSliderBars.Cost_H2Tank, wiitMod.bmH2Tank.dollarPerKgH2model());
		}
		if ((sbars[APSliderBars.Cost_Battery.ordinal()] == null) && (wiitMod.bmBatteries != null)) {
			sbars[APSliderBars.Cost_Battery.ordinal()] = new SliderBarSetup(APSliderBars.Cost_Battery, wiitMod.bmBatteries.nominalDollarPerKWh());
		}
		
		if ((sbars[APSliderBars.Cost_RPE.ordinal()] == null) && (wiitMod.bmRPE != null)) {
			sbars[APSliderBars.Cost_RPE.ordinal()] = new SliderBarSetup(APSliderBars.Cost_RPE, wiitMod.bmRPE);
		}
		if ((sbars[APSliderBars.Cost_RPE_ElectSystems.ordinal()] == null) && (wiitMod.bmRPEElectrification != null)) {
			sbars[APSliderBars.Cost_RPE_ElectSystems.ordinal()] = new SliderBarSetup(APSliderBars.Cost_RPE_ElectSystems, wiitMod.bmRPEElectrification);
		}
		if ((sbars[APSliderBars.Cost_NumYears.ordinal()] == null) && (wiitMod.bmNumYears != null)) {
			sbars[APSliderBars.Cost_NumYears.ordinal()] = new SliderBarSetup(APSliderBars.Cost_NumYears, wiitMod.bmNumYears);
		}
		if (sbars[APSliderBars.Cost_AnnualDriveDistance.ordinal()] == null) {
			sbars[APSliderBars.Cost_AnnualDriveDistance.ordinal()] = new SliderBarSetup(APSliderBars.Cost_AnnualDriveDistance, new VSliderBarBaseModel(1f, 0.2f, 2f));
		}
		if (sbars[APSliderBars.Cost_AnnualDriveDays.ordinal()] == null) {
			sbars[APSliderBars.Cost_AnnualDriveDays.ordinal()] = new SliderBarSetup(APSliderBars.Cost_AnnualDriveDays, new VSliderBarBaseModel(1f, 0.1f, 1f));
		}

		if ((sbars[APSliderBars.Cost_ApprHomeCharger.ordinal()] == null) && (wiitMod.hasPHEVs()||(wiitMod.hasBEVs()&&(wiitMod.bevRepModel.bevRepCommercial == null)))) {
			sbars[APSliderBars.Cost_ApprHomeCharger.ordinal()] = new SliderBarSetup(APSliderBars.Cost_ApprHomeCharger, new VSliderBarBaseModel(1f, 0f, 1f));
		}
		
		if ((sbars[APSliderBars.Cost_Incentives.ordinal()] == null) && avms.includesIncentives()) {
			sbars[APSliderBars.Cost_Incentives.ordinal()] = new SliderBarSetup(APSliderBars.Cost_Incentives, new VSliderBarBaseModel(1f, 0f, 1f));
		}

		if ((sbars[APSliderBars.MfgGHG_exceptBat.ordinal()] == null) && avms.mfgGHGModelIncluded()) {
			sbars[APSliderBars.MfgGHG_exceptBat.ordinal()] = new SliderBarSetup(APSliderBars.MfgGHG_exceptBat, new VSliderBarBaseModel(0.5f, 0f, 1f));
		}
		if ((sbars[APSliderBars.MfgGHG_Battery.ordinal()] == null) && avms.mfgGHGModelIncluded()) {
			sbars[APSliderBars.MfgGHG_Battery.ordinal()] = new SliderBarSetup(APSliderBars.MfgGHG_Battery, new VSliderBarBaseModel(1f, 0f, 1f));
		}
		
		if ((sbars[APSliderBars.Price_Gas.ordinal()] == null) && (wiitMod.bmGasCost != null)) {
			sbars[APSliderBars.Price_Gas.ordinal()] = new SliderBarSetup(APSliderBars.Price_Gas, wiitMod.bmGasCost);
		}
		if ((sbars[APSliderBars.Price_Diesel.ordinal()] == null) && (wiitMod.bmDieselCost != null)) {
			sbars[APSliderBars.Price_Diesel.ordinal()] = new SliderBarSetup(APSliderBars.Price_Diesel, wiitMod.bmDieselCost);
		}
		if ((sbars[APSliderBars.Price_NG.ordinal()] == null) && (wiitMod.bmCNGCost != null)) {
			sbars[APSliderBars.Price_NG.ordinal()] = new SliderBarSetup(APSliderBars.Price_NG, wiitMod.bmCNGCost);
		}
		if ((sbars[APSliderBars.Price_H2.ordinal()] == null) && (wiitMod.bmH2Cost != null)) {
			sbars[APSliderBars.Price_H2.ordinal()] = new SliderBarSetup(APSliderBars.Price_H2, wiitMod.bmH2Cost);
		}
		if ((sbars[APSliderBars.Price_Elect.ordinal()] == null) && (wiitMod.bmElectCost != null)) {
			sbars[APSliderBars.Price_Elect.ordinal()] = new SliderBarSetup(APSliderBars.Price_Elect, wiitMod.bmElectCost);
		}
		if ((sbars[APSliderBars.Price_DCPremium.ordinal()] == null) && dcFastChargingIncluded() && (wiitMod.chgModels.pricePremiumForDCFast != null)) {
			sbars[APSliderBars.Price_DCPremium.ordinal()] = new SliderBarSetup(APSliderBars.Price_DCPremium, wiitMod.chgModels.pricePremiumForDCFast);
		}

		if ((sbars[APSliderBars.GHG_Gas.ordinal()] == null) && (wiitMod.bmGasGHG != null)) {
			sbars[APSliderBars.GHG_Gas.ordinal()] = new SliderBarSetup(APSliderBars.GHG_Gas, wiitMod.bmGasGHG);
		}
		if ((sbars[APSliderBars.GHG_Diesel.ordinal()] == null) && (wiitMod.bmDieselGHG != null)) {
			sbars[APSliderBars.GHG_Diesel.ordinal()] = new SliderBarSetup(APSliderBars.GHG_Diesel, wiitMod.bmDieselGHG);
		}
		if ((sbars[APSliderBars.GHG_NG.ordinal()] == null) && (wiitMod.bmCNGGHG != null)) {
			sbars[APSliderBars.GHG_NG.ordinal()] = new SliderBarSetup(APSliderBars.GHG_NG, wiitMod.bmCNGGHG);
		}
		if ((sbars[APSliderBars.GHG_H2.ordinal()] == null) && (wiitMod.bmH2GHG != null)) {
			sbars[APSliderBars.GHG_H2.ordinal()] = new SliderBarSetup(APSliderBars.GHG_H2, wiitMod.bmH2GHG);
		}
		if ((sbars[APSliderBars.GHG_Elect.ordinal()] == null) && (wiitMod.bmElectGHG != null)) {
			sbars[APSliderBars.GHG_Elect.ordinal()] = new SliderBarSetup(APSliderBars.GHG_Elect, wiitMod.bmElectGHG);
		}

		
		if ((sbars[APSliderBars.Bhv_ChgTimePref.ordinal()] == null) && (wiitMod.hasPHEVs()||(wiitMod.hasBEVs()&&(wiitMod.bevRepModel.bevRepCommercial == null)))) {
			sbars[APSliderBars.Bhv_ChgTimePref.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_ChgTimePref, new VSliderBarBaseModel(0f, 0f, 1f));
		}
		if ((sbars[APSliderBars.Bhv_FracNonChgPHEVs.ordinal()] == null) && (wiitMod.chgModels.fractionNonChargingPHEVs != null)) {
			sbars[APSliderBars.Bhv_FracNonChgPHEVs.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_FracNonChgPHEVs, wiitMod.chgModels.fractionNonChargingPHEVs);
		}
		if ((sbars[APSliderBars.Bhv_ChgWindow.ordinal()] == null) && (wiitMod.hasPHEVs()||(wiitMod.hasBEVs()&&(wiitMod.bevRepModel.bevRepCommercial == null)))) {
			sbars[APSliderBars.Bhv_ChgWindow.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_ChgWindow, new VSliderBarBaseModel(HrsDayTimeChgWindowEqToOvernightOnly, wiitMod.chgModels.minDayTimeChgWindow(), HrsDayTimeChgWindowEqToOvernightOnly));
		}

		if ((sbars[APSliderBars.Bhv_BEVRep_Commercial.ordinal()] == null) && (wiitMod.bevRepModel.bevRepCommercial != null)) {
			sbars[APSliderBars.Bhv_BEVRep_Commercial.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_BEVRep_Commercial, wiitMod.bevRepModel.bevRepCommercial.fractionMoreVehiclesNeeded);
		}
		if ((sbars[APSliderBars.Bhv_BEVRep_vehID.ordinal()] == null) && ((wiitMod.hasBEVs()&&(wiitMod.bevRepModel.bevRepCommercial == null)))) {
			sbars[APSliderBars.Bhv_BEVRep_vehID.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_BEVRep_vehID, new VSliderBarBaseModel(0f, 0f, wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName.length-1));
		}
		if ((sbars[APSliderBars.Bhv_BEVRep_anxID.ordinal()] == null) && ((wiitMod.hasBEVs()&&(wiitMod.bevRepModel.bevRepCommercial == null)))) {
			sbars[APSliderBars.Bhv_BEVRep_anxID.ordinal()] = new SliderBarSetup(APSliderBars.Bhv_BEVRep_anxID, new VSliderBarBaseModel(0f, 0f, wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length-1));
		}
	}


	private String apUnitsFormattedCaption(int bID) {
		APSliderBars apsb = APSliderBars.values()[bID];
		switch (apsb) {
		case Cost_GasICE:
		case Cost_DieselICE:
		case Cost_NGICE:
		case Cost_FuelCell:
		case Cost_Motor:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.PowerUnit) + ")";
		case Cost_H2Tank:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.H2Unit) + ")";
		case Cost_Battery:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.ElectUnit) + ")";
		case Price_Gas:
		case Price_Diesel:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.LiquidFuelUnit) + ")";
		case Price_NG:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.NGUnit) + ")";
		case Price_H2:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.H2Unit) + ")";
		case Price_Elect:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +"/" + DUnits.getShortName(DUnits.UnitType.ElectUnit) + ")";
		case GHG_Gas:
		case GHG_Diesel:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +"/" + DUnits.getShortName(DUnits.UnitType.LiquidFuelUnit) + ")";
		case GHG_NG:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +"/" + DUnits.getShortName(DUnits.UnitType.NGUnit) + ")";
		case GHG_H2:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +"/" + DUnits.getShortName(DUnits.UnitType.H2Unit) + ")";
		case GHG_Elect:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +"/" + DUnits.getShortName(DUnits.UnitType.ElectUnit) + ")";
		case Cost_AnnualDriveDistance:
		case Bhv_BEVRep_anxID:
			return apsb.caption + " (" + DUnits.getShortName(DUnits.UnitType.Distance) + ")";
		default:
			return apsb.caption;
		}
	}
	private String apUnitsFormattedValue(int bID, float sbarValue) {
		APSliderBars apsb = APSliderBars.values()[bID];
		switch (apsb) {
		case Cost_AnnualDriveDistance:
		case Cost_GasICE:
		case Cost_DieselICE:
		case Cost_NGICE:
		case Cost_FuelCell:
		case Cost_Motor:
		case Cost_H2Tank:
			return NumToString.posFloatWNumDecimals(apUnitsAdjustedValue(bID, sbarValue), 1);
		case Cost_AnnualDriveDays:
		case Cost_Battery:
			return ""+(int)(apUnitsAdjustedValue(bID, sbarValue) + 0.5f);
		case Cost_RPE:
		case Cost_RPE_ElectSystems:
			return NumToString.posFloatWNumDecimals(sbarValue, 2);
		case Cost_NumYears:
			return NumToString.posFloatWNumDecimals(sbarValue, 1);
		case Price_Gas:
		case Price_Diesel:
		case Price_NG:
		case Price_H2:
		case Price_Elect:
			return NumToString.posFloatWNumDecimals(apUnitsAdjustedValue(bID, sbarValue), 2);
		case Cost_ApprHomeCharger:
		case Cost_Incentives:
		case Price_DCPremium:
			return ""+((int)(100f*sbarValue + 0.5f)+"%");
		case GHG_Gas:
		case GHG_Diesel:
		case GHG_NG:
		case GHG_H2:
		case GHG_Elect:
			return ""+(int)(apUnitsAdjustedValue(bID, sbarValue) + 0.5f);
		case MfgGHG_exceptBat:
		{
			int percent2 = (int)(100f*sbarValue + 0.5f);
			int percent1 = 100 - percent2;
			
			if (percent2 == 0) return "Low End";
			if (percent1 == 0) return "High End";
			if (percent1 >= 50) return ""+percent1+"% Low End";
			return ""+percent2+"% High End";
		}
		case Bhv_ChgTimePref:
		{
			int percent2 = (int)(100f*sbarValue + 0.5f);
			int percent1 = 100 - percent2;
			
			if (percent2 == 0) return "Minimizing Cost";
			if (percent1 == 0) return "Minimizing GHG";
			return ""+percent1+"% Min. Cost / "+percent2+"% Min. GHG";
		}
		case Bhv_ChgWindow:
		{
			float zTol = 0.001f;
			if (Math.abs(sbarValue - FDefaults.hrsChgAfterlastTripOfLastDay) < zTol) {
				return "Overnight Only";
			}
			return NumToString.posFloatWNumDecimals(sbarValue, 2);
		}
		case MfgGHG_Battery:
		case Bhv_FracNonChgPHEVs:
		case Bhv_BEVRep_Commercial:
			return NumToString.posFloatWNumDecimals(100f*sbarValue, 1)+"%";
		case Bhv_BEVRep_vehID:
		{
			String[] bevRepShortNames = wiitMod.bevRepModel.bevRepWholeDay.repVehicleShortName;
			if (bevRepShortNames.length < 2) return "All " + bevRepShortNames[0];
			
			String bevRep1 = bevRepShortNames[0];
			String bevRep2 = bevRepShortNames[1];
			int percent2 = (int)(100f*sbarValue + 0.5f);
			int percent1 = 100 - percent2;
			
			if (percent1 == 0) return "All " + bevRep2;
			if (percent2 == 0) return "All " + bevRep1;
			return ""+percent1+"% "+bevRep1+" / "+percent2+"% "+bevRep2;
		}
		case Bhv_BEVRep_anxID:
		{
			float[] milesAnxOptions = wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles;
			float milesValue = milesAnxOptions[0];
			if (milesAnxOptions.length > 1) {
				int id1 = (int)sbarValue;
				int id2 = id1+1;
				
				if (id2 >= milesAnxOptions.length) {
					milesValue = milesAnxOptions[id1];
				} else {
					float c2 = sbarValue - id1;
					float c1 = 1f - c2;
					milesValue = c1*milesAnxOptions[id1] + c2*milesAnxOptions[id2];
				}
			}
			return NumToString.posFloatWNumDecimals(milesValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance), 1);
		}
		default:
			return ""+sbarValue;
		}
	}
	private float apUnitsAdjustedValueToSBValue(int bID, float unitsAdjutedValue) {
		APSliderBars apsb = APSliderBars.values()[bID];
		switch (apsb) {
		case Cost_AnnualDriveDays:
			return unitsAdjutedValue/daysOfTravelPerYearIncCurSolSet;
		case Cost_AnnualDriveDistance:
			return (unitsAdjutedValue/annualMilesTravelledIncCurSolSet)*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
		case Cost_GasICE:
		case Cost_DieselICE:
		case Cost_NGICE:
		case Cost_FuelCell:
		case Cost_Motor:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.PowerUnit);
		case Cost_H2Tank:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case Cost_Battery:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case Price_Gas:
		case Price_Diesel:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.LiquidFuelUnit);
		case Price_NG:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.NGUnit);
		case Price_H2:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case Price_Elect:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case GHG_Gas:
		case GHG_Diesel:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.LiquidFuelUnit);
		case GHG_NG:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.NGUnit);
		case GHG_H2:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case GHG_Elect:
			return (unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))/DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case Cost_ApprHomeCharger:
		case Cost_Incentives:
		case Price_DCPremium:
		case MfgGHG_exceptBat:
		case MfgGHG_Battery:
		case Bhv_ChgTimePref:
		case Bhv_FracNonChgPHEVs:
		case Bhv_BEVRep_Commercial:
		case Bhv_BEVRep_vehID:
			return 0.01f*unitsAdjutedValue;
		case Bhv_BEVRep_anxID:
		{
			float[] milesAnxOptions = wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles;
			if (milesAnxOptions.length < 2) return milesAnxOptions[0];
			
			float milesAnx = unitsAdjutedValue*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
			int intervalID = 0;
			for (int i=1; i<milesAnxOptions.length; i++) {
				if ((milesAnxOptions[i-1] <= milesAnx)&&(milesAnxOptions[i] > milesAnx)) {
					intervalID = i-1;
					break;
				}
			}
			float x1 = intervalID;
			float x2 = intervalID+1;
			float y1 = milesAnxOptions[intervalID];
			float y2 = milesAnxOptions[intervalID+1];
			return x1 + (x2 - x1)*(milesAnx - y1)/(y2 - y1);
		}
		default:
			return unitsAdjutedValue;
		}
	}
	private float apUnitsAdjustedValue(int bID, float sbarValue) {
		APSliderBars apsb = APSliderBars.values()[bID];
		switch (apsb) {		
		case Cost_AnnualDriveDays:
			return sbarValue*daysOfTravelPerYearIncCurSolSet;
		case Cost_AnnualDriveDistance:
			return sbarValue*annualMilesTravelledIncCurSolSet/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
		case Cost_GasICE:
		case Cost_DieselICE:
		case Cost_NGICE:
		case Cost_FuelCell:
		case Cost_Motor:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.PowerUnit);
		case Cost_H2Tank:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case Cost_Battery:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case Price_Gas:
		case Price_Diesel:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.LiquidFuelUnit);
		case Price_NG:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.NGUnit);
		case Price_H2:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case Price_Elect:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case GHG_Gas:
		case GHG_Diesel:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.LiquidFuelUnit);
		case GHG_NG:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.NGUnit);
		case GHG_H2:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.H2Unit);
		case GHG_Elect:
			return (sbarValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit))*DUnits.convConstMPtoBCalc(DUnits.UnitType.ElectUnit);
		case Cost_ApprHomeCharger:
		case Cost_Incentives:
		case Price_DCPremium:
		case MfgGHG_exceptBat:
		case MfgGHG_Battery:
		case Bhv_ChgTimePref:
		case Bhv_FracNonChgPHEVs:
		case Bhv_BEVRep_Commercial:
		case Bhv_BEVRep_vehID:
			return 100f*sbarValue;
		case Bhv_BEVRep_anxID:
		{
			float[] milesAnxOptions = wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles;
			float milesValue = milesAnxOptions[0];
			if (milesAnxOptions.length > 1) {
				int id1 = (int)sbarValue;
				int id2 = id1+1;
				
				if (id2 >= milesAnxOptions.length) {
					milesValue = milesAnxOptions[id1];
				} else {
					float c2 = sbarValue - id1;
					float c1 = 1f - c2;
					milesValue = c1*milesAnxOptions[id1] + c2*milesAnxOptions[id2];
				}
			}
			return milesValue/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
		}
		default:
			return sbarValue;
		}
	}
}
