package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.comp.*;

//Class name is short for What-Is-In-The-Model :)
public class WIITModel {
	public BaseModel_PTElement_CostFnOfPower bmGasICE, bmDieselICE, bmCNGICE, bmFuelCell, bmMotor;	
	public BaseModel_H2Tank_CostFnOfKgH2Capacity bmH2Tank;
	public BaseModel_Battery_CostFnOfKWh bmBatteries;
	public VSliderBarBaseModel bmGasCost, bmDieselCost, bmCNGCost, bmH2Cost, bmElectCost;
	public VSliderBarBaseModel bmGasGHG, bmDieselGHG, bmCNGGHG, bmH2GHG, bmElectGHG;
	public VSliderBarBaseModel bmRPE, bmRPEElectrification, bmNumYears;
	public ChargingModels chgModels;
	public BEVReplacementModel bevRepModel;
	
	private boolean[] powertrainTypeIcluded;
	public boolean isPowertrainTypeIcluded(PowertrainType ptType) {return powertrainTypeIcluded[ptType.ordinal()];}
	
	public boolean hasBEVs() {
		return isPowertrainTypeIcluded(PowertrainType.bev);
	}
	public boolean hasPHEVs() {
		return isPowertrainTypeIcluded(PowertrainType.phev_cng) || isPowertrainTypeIcluded(PowertrainType.phev_diesel) || 
				isPowertrainTypeIcluded(PowertrainType.phev_fc) || isPowertrainTypeIcluded(PowertrainType.phev_gas);
	}
	public boolean hasHEVs() {
		return isPowertrainTypeIcluded(PowertrainType.hev_cng) || isPowertrainTypeIcluded(PowertrainType.hev_diesel) || 
				isPowertrainTypeIcluded(PowertrainType.hev_fc) || isPowertrainTypeIcluded(PowertrainType.hev_gas);
	}
	public boolean hasPlugIns() {
		return hasBEVs() || hasPHEVs();
	}
	public boolean hasHydrogen() {
		return isPowertrainTypeIcluded(PowertrainType.hev_fc) || 
				isPowertrainTypeIcluded(PowertrainType.phev_fc);
	}
	public boolean hasGasoline() {
		return isPowertrainTypeIcluded(PowertrainType.cv_gas) || isPowertrainTypeIcluded(PowertrainType.hev_gas) || 
				isPowertrainTypeIcluded(PowertrainType.phev_gas);
	}
	public boolean hasDiesel() {
		return isPowertrainTypeIcluded(PowertrainType.cv_diesel) || isPowertrainTypeIcluded(PowertrainType.hev_diesel) || 
				isPowertrainTypeIcluded(PowertrainType.phev_diesel);
	}
	public boolean hasCNG() {
		return isPowertrainTypeIcluded(PowertrainType.cv_cng) || isPowertrainTypeIcluded(PowertrainType.hev_cng) || 
				isPowertrainTypeIcluded(PowertrainType.phev_cng);
	}
	
	
	public static WIITModel readWIITModel(FFStructure cFS, int aID, AnalysisVehModelsSetup avms) {
		try {
			return new WIITModel(cFS, aID, avms);
		} catch (Exception e) {
			return null;
		}
	}

	public WIITModel(FFStructure cFS, int aID, AnalysisVehModelsSetup avms) throws Exception {
		//Identify existing power train types
		powertrainTypeIcluded = new boolean[PowertrainType.values().length];
		for (int i=0; i<powertrainTypeIcluded.length; i++) powertrainTypeIcluded[i] = false;
		
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		for (int i=0; i<vms.length; i++) {
			String fnameFASTSim = cFS.getFilePath_FASTSimVehModel(aID, vms[i].shortName);
			FSJOneFileVehModel ofvModel = new FSJOneFileVehModel(fnameFASTSim);
			
			powertrainTypeIcluded[PowertrainType.decode(ofvModel.vehModelParam).ordinal()] = true;
		}
		
		//Mark-up base models that need to exist
		boolean needsGasICE = false;
		boolean needsDieselICE = false;
		boolean needsCNGICE = false;
		
		boolean needsBattery = false;
		boolean needsMotor = false;
		boolean needsFuelCell = false;
		boolean needsH2Tank = false;
		
		boolean needsChargingModel = false;
		boolean needsBEVRep = false;
		
		boolean needsCNGGHG = false;
		boolean needsGasGHG = false;
		boolean needsDieselGHG = false;
		boolean needsH2GHG = false;
		boolean needsGridGHG = false;
		
		boolean needsCNGCost = false;
		boolean needsGasCost = false;
		boolean needsDieselCost = false;
		boolean needsH2Cost = false;
		boolean needsElectCost = false;
		
		boolean needsRPE = true;
		boolean needsRPEElectrification = false;
		boolean needsNumYears = true;
		
		for (int i=0; i<powertrainTypeIcluded.length; i++) {
			if (powertrainTypeIcluded[i]) {
				switch (PowertrainType.values()[i]) {
				case bev:
					needsBattery = true;
					needsMotor = true;
					needsChargingModel = true;
					needsBEVRep = true;
					needsGridGHG = true;
					needsElectCost = true;
					needsRPEElectrification = true;
					break;
				case cv_cng:
					needsCNGICE = true;
					needsCNGGHG = true;
					needsCNGCost = true;
					break;
				case cv_diesel:
					needsDieselICE = true;
					needsDieselGHG = true;
					needsDieselCost = true;
					break;
				case cv_gas:
					needsGasICE = true;
					needsGasGHG = true;
					needsGasCost = true;
					break;
				case hev_cng:
					needsCNGICE = true;
					needsBattery = true;
					needsMotor = true;
					needsCNGGHG = true;
					needsCNGCost = true;
					needsRPEElectrification = true;
					break;
				case hev_diesel:
					needsDieselICE = true;
					needsBattery = true;
					needsMotor = true;
					needsDieselGHG = true;
					needsDieselCost = true;
					needsRPEElectrification = true;
					break;
				case hev_fc:
					needsFuelCell = true;
					needsH2Tank = true;
					needsBattery = true;
					needsMotor = true;
					needsH2GHG = true;
					needsH2Cost = true;
					needsRPEElectrification = true;
					break;
				case hev_gas:
					needsGasICE = true;
					needsBattery = true;
					needsMotor = true;
					needsGasGHG = true;
					needsGasCost = true;
					needsRPEElectrification = true;
					break;
				case phev_cng:
					needsCNGICE = true;
					needsBattery = true;
					needsMotor = true;
					needsChargingModel = true;
					needsCNGGHG = true;
					needsGridGHG = true;
					needsCNGCost = true;
					needsElectCost = true;
					needsRPEElectrification = true;
					break;
				case phev_diesel:
					needsDieselICE = true;
					needsBattery = true;
					needsMotor = true;
					needsChargingModel = true;
					needsGridGHG = true;
					needsDieselGHG = true;
					needsElectCost = true;
					needsDieselCost = true;
					needsRPEElectrification = true;
					break;
				case phev_fc:
					needsFuelCell = true;
					needsH2Tank = true;
					needsBattery = true;
					needsMotor = true;
					needsChargingModel = true;
					needsGridGHG = true;
					needsH2GHG = true;
					needsElectCost = true;
					needsH2Cost = true;
					needsRPEElectrification = true;
					break;
				case phev_gas:
					needsGasICE = true;
					needsBattery = true;
					needsMotor = true;
					needsChargingModel = true;
					needsGridGHG = true;
					needsGasGHG = true;
					needsElectCost = true;
					needsGasCost = true;
					needsRPEElectrification = true;
					break;
				}
			}
		}
		
		//Read Existing Base Models in Folder		
		readBaseModels(cFS, aID);
		
		//Check for proper availability of base models
		if (needsGasICE && (bmGasICE == null)) throw new Exception();
		if (needsCNGICE && (bmCNGICE == null)) throw new Exception();
		if (needsDieselICE && (bmDieselICE == null)) throw new Exception();
		if (needsFuelCell && (bmFuelCell == null)) throw new Exception();
		if (needsMotor && (bmMotor == null)) throw new Exception();
		
		if (needsH2Tank && (bmH2Tank == null)) throw new Exception();
		if (needsBattery && (bmBatteries == null)) throw new Exception();

		if (needsCNGGHG && (bmCNGGHG == null)) throw new Exception();
		if (needsGasGHG && (bmGasGHG == null)) throw new Exception();
		if (needsDieselGHG && (bmDieselGHG == null)) throw new Exception();
		if (needsH2GHG && (bmH2GHG == null)) throw new Exception();
		if (needsGridGHG && (bmElectGHG == null)) throw new Exception();

		if (needsCNGCost && (bmCNGCost == null)) throw new Exception();
		if (needsGasCost && (bmGasCost == null)) throw new Exception();
		if (needsDieselCost && (bmDieselCost == null)) throw new Exception();
		if (needsH2Cost && (bmH2Cost == null)) throw new Exception();
		if (needsElectCost && (bmElectCost == null)) throw new Exception();

		if (needsNumYears && (bmNumYears == null)) throw new Exception();
		if (needsRPE && (bmRPE == null)) throw new Exception();
		if (needsRPEElectrification && (bmRPEElectrification == null)) throw new Exception();
		
		if (needsChargingModel && (chgModels == null)) throw new Exception();
		if (needsBEVRep && (bevRepModel == null)) throw new Exception();
	}
	
	private void readBaseModels(FFStructure cFS, int aID) throws Exception {
		String fname = cFS.getFilePath_baseModels(aID);
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine;
		
		while ((readLine=fin.readLine())!=null) {
			BaseModelTypes type = BaseModelTypes.decodeHeaderString(readLine);
			if (type !=null) {
				switch (type) {
				case gasICE:
					bmGasICE = new BaseModel_PTElement_CostFnOfPower(fin);
					break;
				case cngICE:
					bmCNGICE = new BaseModel_PTElement_CostFnOfPower(fin);
					break;
				case dieselICE:
					bmDieselICE = new BaseModel_PTElement_CostFnOfPower(fin);
					break;
				case fuelCell:
					bmFuelCell = new BaseModel_PTElement_CostFnOfPower(fin);
					break;
				case motor:
					bmMotor = new BaseModel_PTElement_CostFnOfPower(fin);
					break;
				case h2Tank:
					bmH2Tank = new BaseModel_H2Tank_CostFnOfKgH2Capacity(fin);
					break;
				case batteries:
					bmBatteries = new BaseModel_Battery_CostFnOfKWh(fin);
					break;
				case cngCost:
					bmCNGCost = new VSliderBarBaseModel(fin.readLine());
					break;
				case dieselCost:
					bmDieselCost = new VSliderBarBaseModel(fin.readLine());
					break;
				case electCost:
					bmElectCost = new VSliderBarBaseModel(fin.readLine());
					break;
				case gasCost:
					bmGasCost = new VSliderBarBaseModel(fin.readLine());
					break;
				case h2Cost:
					bmH2Cost = new VSliderBarBaseModel(fin.readLine());
					break;
				case cngGHG:
					bmCNGGHG = new VSliderBarBaseModel(fin.readLine());
					break;
				case dieselGHG:
					bmDieselGHG = new VSliderBarBaseModel(fin.readLine());
					break;
				case electGHG:
					bmElectGHG = new VSliderBarBaseModel(fin.readLine());
					break;
				case gasGHG:
					bmGasGHG = new VSliderBarBaseModel(fin.readLine());
					break;
				case h2GHG:
					bmH2GHG = new VSliderBarBaseModel(fin.readLine());
					break;
				case rpe:
					bmRPE = new VSliderBarBaseModel(fin.readLine());
					break;
				case rpeElectrification:
					bmRPEElectrification = new VSliderBarBaseModel(fin.readLine());
					break;
				case numYears:
					bmNumYears = new VSliderBarBaseModel(fin.readLine());
					break;
				case bevRep:
					bevRepModel = new BEVReplacementModel(fin);
					break;
				case charging:
					chgModels = new ChargingModels(fin);
					break;
				}
			}
		}
		
		fin.close();
	}
	
	private enum BaseModelTypes {
		gasICE("_gasICE"),
		dieselICE("_dieselICE"),
		cngICE("_cngICE"),
		fuelCell("_fuelCell"),
		motor("_motor"),
		
		h2Tank("_H2Tank"),
		batteries("_batteries"),
		
		gasCost("_gasCost"),
		dieselCost("_dieselCost"),
		cngCost("_cngCost"),
		h2Cost("_h2Cost"),
		electCost("_electCost"),
		
		gasGHG("_gasGHG"),
		dieselGHG("_dieselGHG"),
		cngGHG("_cngGHG"),
		h2GHG("_h2GHG"),
		electGHG("_electGHG"),

		rpe("_RPE"),
		rpeElectrification("_RPE_electrification"),
		numYears("_numYearsOwnership"),

		charging("_ChargingModels"),
		bevRep("_BEReplacementModel"),
		
		;
		private String headerString;
		private BaseModelTypes(String s) {headerString = s;}
		private static BaseModelTypes decodeHeaderString(String s) {
			for (int i=0; i<values().length; i++) {
				if (values()[i].headerString.equalsIgnoreCase(s)) return values()[i];
			}
			return null;
		}
	}
	
	public static class ChargingModels {
		public VSliderBarBaseModel fractionNonChargingPHEVs, pricePremiumForDCFast;
		public float[] daytimeChargingMinWindow;	//Daytime charging window must be less than 12 hours (else slider bars might have issues)
		public float minDayTimeChgWindow() {
			float mw = daytimeChargingMinWindow[0];
			for (int i=1; i<daytimeChargingMinWindow.length; i++) {
				if (mw > daytimeChargingMinWindow[i]) mw = daytimeChargingMinWindow[i];
			}
			return mw;
		}
		public float chgDeadTime_minutesBeforeConnect, chgDeadTime_minutesAfterConnect;		
		
		public boolean bevHomesHaveL2, phevHomesHaveL2, dtChgPrioritizesDCFastIfFeasible, dcFastAvailable;
		public float l1PowerKWtoBattery, l2PowerKWtoBattery, minNomAERForPHEVsToHaveDCFast;
		public float dcfsLowSOC, dcfsHighSOC, dcfsRelSOCFastRate, dcfsRelSOCSlowRate;
		
		private ChargingModels (BufferedReader fin) throws Exception {
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			chgDeadTime_minutesBeforeConnect = Float.parseFloat(sp[0]);
			chgDeadTime_minutesAfterConnect = Float.parseFloat(sp[1]);
			
			readLine = fin.readLine();
			sp = readLine.split(",");
			
			int numDaytimeChgCases = Integer.parseInt(sp[0]);
			daytimeChargingMinWindow = new float[numDaytimeChgCases];
			for (int i=0; i<daytimeChargingMinWindow.length; i++) daytimeChargingMinWindow[i] = Float.parseFloat(sp[i+1]);
			
			if (sp.length > (numDaytimeChgCases+1)) {
				fractionNonChargingPHEVs = new VSliderBarBaseModel();
				fractionNonChargingPHEVs.baseValue = Float.parseFloat(sp[numDaytimeChgCases+1]);
				fractionNonChargingPHEVs.minValue =Float.parseFloat(sp[numDaytimeChgCases+2]);
				fractionNonChargingPHEVs.maxValue = Float.parseFloat(sp[numDaytimeChgCases+3]);
			} else {
				fractionNonChargingPHEVs = null;
			}
			
			readLine = fin.readLine();
			sp = readLine.split(",");
			
			bevHomesHaveL2 = Boolean.parseBoolean(sp[0]);
			phevHomesHaveL2 = Boolean.parseBoolean(sp[1]);
			dtChgPrioritizesDCFastIfFeasible = Boolean.parseBoolean(sp[2]);
			dcFastAvailable = Boolean.parseBoolean(sp[3]);
			l1PowerKWtoBattery = Float.parseFloat(sp[4]);
			l2PowerKWtoBattery = Float.parseFloat(sp[5]);
			minNomAERForPHEVsToHaveDCFast = Float.parseFloat(sp[6]);
			
			readLine = fin.readLine();
			pricePremiumForDCFast = new VSliderBarBaseModel(readLine);

			readLine = fin.readLine();
			sp = readLine.split(",");
			
			dcfsLowSOC = Float.parseFloat(sp[0]);
			dcfsHighSOC = Float.parseFloat(sp[1]);
			dcfsRelSOCFastRate = Float.parseFloat(sp[2]);
			dcfsRelSOCSlowRate = Float.parseFloat(sp[3]);
		}
	}
	
	
	public static class BEVReplacementModel {
		public BEVRepWholeDay bevRepWholeDay;
		public BEVRepCommercial bevRepCommercial;
		
		private BEVReplacementModel(BufferedReader fin) throws Exception {
			String readLine = fin.readLine();

			if (readLine.equalsIgnoreCase("Commercial")) {
				bevRepCommercial = new BEVRepCommercial(fin);
			} else {
				bevRepWholeDay = new BEVRepWholeDay(fin);
			}
		}
	}
	public static class BEVRepWholeDay {
		public String[] repVehicleShortName;
		
		public float[] choicesForRangeAnxAt100miles;
		private ProfileCurve rangeAnxProfile;
		public ProfileCurve rangeAnxProfile() {return rangeAnxProfile;}
		
		public float calcRangeAnx(float milesUpcoming, float rangeAnxAt100miles) {
			float curveScaling = rangeAnxAt100miles/rangeAnxProfile.calcY(100f);
			return curveScaling*rangeAnxProfile.calcY(milesUpcoming);
		}
		
		private BEVRepWholeDay(BufferedReader fin) throws Exception {
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			repVehicleShortName = new String[Math.min(2, sp.length)];
			for (int i=0; i<repVehicleShortName.length; i++) repVehicleShortName[i] = new String(sp[i]);
			
			readLine = fin.readLine();
			sp = readLine.split(",");
			
			choicesForRangeAnxAt100miles = new float[sp.length];
			for (int i=0; i<choicesForRangeAnxAt100miles.length; i++) choicesForRangeAnxAt100miles[i] = Float.parseFloat(sp[i]);
			
			String l1 = fin.readLine();
			String l2 = fin.readLine();
			rangeAnxProfile = new ProfileCurve(l1,l2);
		}
	}
	public static class BEVRepCommercial {
		public VSliderBarBaseModel fractionMoreVehiclesNeeded;
		
		private BEVRepCommercial(BufferedReader fin) throws Exception {
			fractionMoreVehiclesNeeded = new VSliderBarBaseModel(fin.readLine());
		}
	}
	
	public static class BaseModel_Battery_CostFnOfKWh {
		private VSliderBarBaseModel nominalDollarPerKWhBEV60kWh;
		public VSliderBarBaseModel nominalDollarPerKWh() {return nominalDollarPerKWhBEV60kWh;}

		private ProfileCurve dollarPerKWhCurve_BEV, dollarPerKWhCurve_PHEV, dollarPerKWhCurve_HEV;

		public float calcCost_BEV(float kWh, float sliderBarDollarPerKWhBEV) {
			float curveSaclingFactor = sliderBarDollarPerKWhBEV / nominalDollarPerKWhBEV60kWh.baseValue;
			return dollarPerKWhCurve_BEV.calcY(kWh)*curveSaclingFactor;
		}
		public float calcCost_PHEV(float kWh, float sliderBarDollarPerKWhBEV) {
			float curveSaclingFactor = sliderBarDollarPerKWhBEV / nominalDollarPerKWhBEV60kWh.baseValue;
			return dollarPerKWhCurve_PHEV.calcY(kWh)*curveSaclingFactor;
		}
		public float calcCost_HEV(float kWh, float sliderBarDollarPerKWhBEV) {
			float curveSaclingFactor = sliderBarDollarPerKWhBEV / nominalDollarPerKWhBEV60kWh.baseValue;
			return dollarPerKWhCurve_HEV.calcY(kWh)*curveSaclingFactor;
		}

		private BaseModel_Battery_CostFnOfKWh(BufferedReader fin) throws Exception {
			String l1 = fin.readLine();
			nominalDollarPerKWhBEV60kWh = new VSliderBarBaseModel(l1);
			
			l1 = fin.readLine();
			String l2 = fin.readLine();
			dollarPerKWhCurve_HEV = new ProfileCurve(l1,l2);
			
			l1 = fin.readLine();
			l2 = fin.readLine();
			dollarPerKWhCurve_PHEV = new ProfileCurve(l1,l2);
			
			l1 = fin.readLine();
			l2 = fin.readLine();
			dollarPerKWhCurve_BEV = new ProfileCurve(l1,l2);
		}
	}	
	public static class BaseModel_H2Tank_CostFnOfKgH2Capacity {
		private VSliderBarBaseModel dollarPerKgH2model;
		public VSliderBarBaseModel dollarPerKgH2model() {return dollarPerKgH2model;}
		
		private ProfileCurve dollarPerKgCurve;
		
		public float calcCost(float kgH2, float sliderBarDollarPerKgH2Tank) {
			float curveSaclingFactor = sliderBarDollarPerKgH2Tank / dollarPerKgH2model.baseValue;
			return dollarPerKgCurve.calcY(kgH2)*curveSaclingFactor;
		}
		
		private BaseModel_H2Tank_CostFnOfKgH2Capacity(BufferedReader fin) throws Exception {
			String l1 = fin.readLine();
			String l2 = fin.readLine();
			String l3 = fin.readLine();
			
			dollarPerKgH2model = new VSliderBarBaseModel(l1);
			dollarPerKgCurve = new ProfileCurve(l2,l3);
		}
	}
	public static class BaseModel_PTElement_CostFnOfPower {
		private VSliderBarBaseModel dollarPerKWmodel;
		public VSliderBarBaseModel dollarPerKWmodel() {return dollarPerKWmodel;}
		
		private ProfileCurve dollarPerKWCurve;
		
		public float calcCost(float kW, float sliderBarDollarPerKW) {
			float curveSaclingFactor = sliderBarDollarPerKW / dollarPerKWmodel.baseValue;
			return dollarPerKWCurve.calcY(kW)*curveSaclingFactor;
		}
		
		private BaseModel_PTElement_CostFnOfPower(BufferedReader fin) throws Exception {
			String l1 = fin.readLine();
			String l2 = fin.readLine();
			String l3 = fin.readLine();
			
			dollarPerKWmodel = new VSliderBarBaseModel(l1);
			dollarPerKWCurve = new ProfileCurve(l2,l3);
		}
	}
}
