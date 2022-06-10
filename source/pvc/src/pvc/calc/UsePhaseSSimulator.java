package pvc.calc;

import java.io.FileWriter;

import fastsimjava.FSJOneFileVehModel;
import pvc.calc.comp.*;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;
import pvc.utility.CGHGHistogram;

public class UsePhaseSSimulator {
	
	private AnalysisVehModelsSetup avms;
	private FEcoSimsGenerator fsG;
	private ChargingEffManager chEffMan;
	private FEcoCasesLookup fecoLoo;
	private ChgCasesLookup chgLoo;
	
	private boolean mfgGHGInputsNeeded;
	public boolean mfgGHGInputsNeeded() {return mfgGHGInputsNeeded;}
	
	private APNeededInputs[] neededInputs;
	public APNeededInputs[] neededInputs() {return neededInputs;}
	
	private APAvOutputsPerVehModel[] returnedOutputs;
	public APAvOutputsPerVehModel[] returnedOutputs() {return returnedOutputs;}


	public UsePhaseSSimulator(FEcoCasesLookup fecoLookup, ChgCasesLookup chgLookup, FEcoSimsGenerator fecoSimsGen, ChargingEffManager chgEffManager) {
		fsG = fecoSimsGen;
		chEffMan = chgEffManager;
		fecoLoo = fecoLookup;
		chgLoo = chgLookup;
		
		//Look at what's in the model to decide what's currently required in input and returned in outputs
		boolean[] inputsNeed = new boolean[APNeededInputs.values().length];
		boolean[] outputsGenerated = new boolean[APAvOutputsPerVehModel.values().length];
		
		avms = fsG.avms();
		WIITModel wiitMod = fsG.wiitMod();
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();
		
		for (int i=0; i<fsofModels.length; i++) {
			//Mark-up fuel types
			switch (PowertrainType.decode(fsofModels[i].vehModelParam)) {
			case bev:
				break;
			case cv_cng:
			case hev_cng:
			case phev_cng:
				inputsNeed[APNeededInputs.ngGHG_gCO2PerM3.ordinal()] = true;
				inputsNeed[APNeededInputs.ngPrice_dollarPerM3.ordinal()] = true;
				
				outputsGenerated[APAvOutputsPerVehModel.ngAv_m3pm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHGPerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelPricePerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
				break;
			case cv_diesel:
			case hev_diesel:
			case phev_diesel:
				inputsNeed[APNeededInputs.dieselGHG_gCO2PerGal.ordinal()] = true;
				inputsNeed[APNeededInputs.dieselPrice_dollarPerGal.ordinal()] = true;
				
				outputsGenerated[APAvOutputsPerVehModel.dieselAv_galpm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHGPerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelPricePerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
				break;
			case cv_gas:
			case hev_gas:
			case phev_gas:
				inputsNeed[APNeededInputs.gasGHG_gCO2PerGal.ordinal()] = true;
				inputsNeed[APNeededInputs.gasPrice_dollarPerGal.ordinal()] = true;
				
				outputsGenerated[APAvOutputsPerVehModel.gasAv_galpm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHGPerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelPricePerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
				break;
			case hev_fc:
			case phev_fc:
				inputsNeed[APNeededInputs.h2GHG_gCO2PerKg.ordinal()] = true;
				inputsNeed[APNeededInputs.h2Price_dollarPerKg.ordinal()] = true;
				
				outputsGenerated[APAvOutputsPerVehModel.h2Av_kgpm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHGPerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelPricePerUnit.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
				break;
			}
			

			
			//Mark-up power train types & associated models
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
				inputsNeed[APNeededInputs.gridAvGHG_gCO2perKWh.ordinal()] = true;
				inputsNeed[APNeededInputs.gridAvPrice_dollarPerKWh.ordinal()] = true;
				
				outputsGenerated[APAvOutputsPerVehModel.electAv_kWhpm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBattery_gCO2perKWh.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBattery_dollarPerKWh.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile.ordinal()] = true;

				if (wiitMod.bevRepModel.bevRepCommercial == null) {
					inputsNeed[APNeededInputs.dcFastPricePremium.ordinal()] = true;
					inputsNeed[APNeededInputs.chgWindowID.ordinal()] = true;
					inputsNeed[APNeededInputs.chgTimingOptID.ordinal()] = true;
					inputsNeed[APNeededInputs.bevRangeAnxID.ordinal()] = true;
					inputsNeed[APNeededInputs.bevRepVehID.ordinal()] = true;
					inputsNeed[APNeededInputs.avMilesDrivenPerYear.ordinal()] = true;
					inputsNeed[APNeededInputs.avDaysDrivenPerYear.ordinal()] = true;
					inputsNeed[APNeededInputs.bevRepCost_dollarPerDay.ordinal()] = true;
					inputsNeed[APNeededInputs.bevRepCost_dollarPerMile.ordinal()] = true;
					
					outputsGenerated[APAvOutputsPerVehModel.bevFracMilesReplaced.ordinal()] = true;
					outputsGenerated[APAvOutputsPerVehModel.bevFracDaysReplaced.ordinal()] = true;
					outputsGenerated[APAvOutputsPerVehModel.bevFracDaysFailed.ordinal()] = true;
					outputsGenerated[APAvOutputsPerVehModel.bevRepCost_netDollarPerMile.ordinal()] = true;

					//Loop on replacement vehicles
					for (int j=0; j<fsG.bevRepVehModelID().length; j++) {
						switch (PowertrainType.decode(fsofModels[fsG.bevRepVehModelID()[j]].vehModelParam)) {
						case cv_cng:
						case hev_cng:
							inputsNeed[APNeededInputs.ngGHG_gCO2PerM3.ordinal()] = true;
							inputsNeed[APNeededInputs.ngPrice_dollarPerM3.ordinal()] = true;
							
							outputsGenerated[APAvOutputsPerVehModel.ngAv_m3pm.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
							break;
						case cv_diesel:
						case hev_diesel:
							inputsNeed[APNeededInputs.dieselGHG_gCO2PerGal.ordinal()] = true;
							inputsNeed[APNeededInputs.dieselPrice_dollarPerGal.ordinal()] = true;
							
							outputsGenerated[APAvOutputsPerVehModel.dieselAv_galpm.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
							break;
						case cv_gas:
						case hev_gas:
							inputsNeed[APNeededInputs.gasGHG_gCO2PerGal.ordinal()] = true;
							inputsNeed[APNeededInputs.gasPrice_dollarPerGal.ordinal()] = true;
							
							outputsGenerated[APAvOutputsPerVehModel.gasAv_galpm.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
							break;
						case hev_fc:
							inputsNeed[APNeededInputs.h2GHG_gCO2PerKg.ordinal()] = true;
							inputsNeed[APNeededInputs.h2Price_dollarPerKg.ordinal()] = true;
							
							outputsGenerated[APAvOutputsPerVehModel.h2Av_kgpm.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelGHG_gCO2PerMile.ordinal()] = true;
							outputsGenerated[APAvOutputsPerVehModel.fuelCost_dollarPerMile.ordinal()] = true;
							break;
						default:
							break;
						}
					}
				}
				break;
			case cv:
				break;
			case hev:
				break;
			case phev:
				inputsNeed[APNeededInputs.gridAvGHG_gCO2perKWh.ordinal()] = true;
				inputsNeed[APNeededInputs.gridAvPrice_dollarPerKWh.ordinal()] = true;
				
				inputsNeed[APNeededInputs.chgWindowID.ordinal()] = true;
				inputsNeed[APNeededInputs.chgTimingOptID.ordinal()] = true;
				
				if (wiitMod.chgModels.minNomAERForPHEVsToHaveDCFast > 0) {
					inputsNeed[APNeededInputs.dcFastPricePremium.ordinal()] = true;
				}
				if (wiitMod.chgModels.fractionNonChargingPHEVs != null) {
					inputsNeed[APNeededInputs.fracNonChgPHEVs.ordinal()] = true;
				}
				
				outputsGenerated[APAvOutputsPerVehModel.electAv_kWhpm.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBattery_gCO2perKWh.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBattery_dollarPerKWh.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile.ordinal()] = true;
				outputsGenerated[APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile.ordinal()] = true;
				break;
			}
		}
		
		if (avms.mfgGHGModelIncluded()) {
			mfgGHGInputsNeeded = true;
			outputsGenerated[APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage.ordinal()] = true;
			outputsGenerated[APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage.ordinal()] = true;
		} else {
			mfgGHGInputsNeeded = false;
		}
		outputsGenerated[APAvOutputsPerVehModel.netGHG_gCO2perMile.ordinal()] = true;
		outputsGenerated[APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile.ordinal()] = true;

		//Create marker array for inputs
		int numInputs = 0;
		for (int i=0; i<inputsNeed.length; i++) {
			if (inputsNeed[i]) numInputs++;
		}
		
		neededInputs = new APNeededInputs[numInputs];
		numInputs = 0;
		for (int i=0; i<inputsNeed.length; i++) {
			if (inputsNeed[i]) {
				neededInputs[numInputs] = APNeededInputs.values()[i];
				numInputs++;
			}
		}
		
		int numOutputs = 0;
		for (int i=0; i<outputsGenerated.length; i++) {
			if (outputsGenerated[i]) numOutputs++;
		}

		returnedOutputs = new APAvOutputsPerVehModel[numOutputs];
		numOutputs = 0;
		for (int i=0; i<outputsGenerated.length; i++) {
			if (outputsGenerated[i]) {
				returnedOutputs[numOutputs] = APAvOutputsPerVehModel.values()[i];
				numOutputs++;
			}
		}
	}
	
	public InputStructure createInputStructure() {
		return new InputStructure();
	}
	
	public enum APNeededInputs {
		gasPrice_dollarPerGal,
		dieselPrice_dollarPerGal,
		ngPrice_dollarPerM3,
		h2Price_dollarPerKg,
		
		gasGHG_gCO2PerGal,
		dieselGHG_gCO2PerGal,
		ngGHG_gCO2PerM3,
		h2GHG_gCO2PerKg,
		
		gridAvGHG_gCO2perKWh,
		gridAvPrice_dollarPerKWh,
		dcFastPricePremium,
		fracNonChgPHEVs,
		
		chgWindowID,
		chgTimingOptID,
		bevRangeAnxID,
		bevRepVehID,
				
		avDaysDrivenPerYear,
		avMilesDrivenPerYear,
		bevRepCost_dollarPerDay,
		bevRepCost_dollarPerMile
		;
	}
	public enum APAvOutputsPerVehModel {
		gasAv_galpm("Average Gasoline Usage"),
		dieselAv_galpm("Average Diesel Usage"),
		ngAv_m3pm("Average Natural Gas Usage"),
		h2Av_kgpm("Average Hydrogen Usage"),
		electAv_kWhpm("Average Electricity Usage"),	//This is re-adjusted to all miles driven
		
		fuelGHGPerUnit("Unit Fuel GHG"),
		fuelGHG_gCO2PerMile("Average Fuel GHG per Distance"),
		fuelPricePerUnit("Unit Fuel Price"),
		fuelCost_dollarPerMile("Average Fuel Cost per Distance"),
		
		electToBattery_gCO2perKWh("Electricity (to Battery) GHG"),
		electToBattery_dollarPerKWh("Electricity (to Battery) Price"),
		electToBatteryGHG_gCO2PerMile("Average Electricity GHG per Distance"),
		electToBatteryCost_dollarPerMile("Average Electricity Cost per Distance"),

		mfgGHG_nonBat_gCO2perMileUsage("Manufacturing GHG aside from Battery"),
		mfgGHG_battery_gCO2perMileUsage("Manufacturing GHG from Battery"),
		
		bevFracMilesReplaced("Fraction of Distance on Replacement Vehicle"),
		bevFracDaysReplaced("Fraction of Days on Replacement Vehicle"),
		bevFracDaysFailed("Fraction of Days Alternative Plans"),

		bevRepCost_netDollarPerMile("Replacement Vehicle Cost per Distance"),
		
		netGHG_gCO2perMile("Net GHG per Distance"),
		costOf_fuel_elect_bevRep_dollarPerMile("Simulation-based Running Cost per Distance")
		;
		public String captionWOUnits;
		private APAvOutputsPerVehModel(String s) {
			captionWOUnits = s;
		}
	}

	public class InputStructure {
		private float[] activeInputs, nonBatteryMfgGHGperKgVeh, batteryMfgGHGperKWh;
		private float lifetimeMilesLCA;
		private boolean includeMfgGHG;
		private int[] reqToArrayMap;
		
		private InputStructure() {
			activeInputs = new float[neededInputs.length];
			
			if (mfgGHGInputsNeeded()) {
				nonBatteryMfgGHGperKgVeh = new float[avms.numVehModels()];
				batteryMfgGHGperKWh = new float[avms.numVehModels()];
				includeMfgGHG = true;
			} else {
				includeMfgGHG = false;
			}
			
			lifetimeMilesLCA = FDefaults.vehicleDefaultLifetimeMiles;
			
			reqToArrayMap = new int[APNeededInputs.values().length];
			for (int i=0; i<reqToArrayMap.length; i++) reqToArrayMap[i] = -1;
			for (int i=0; i<neededInputs.length; i++) {
				reqToArrayMap[neededInputs[i].ordinal()] = i;
			}
		}
		
		public float get(APNeededInputs inputType) {
			int id = reqToArrayMap[inputType.ordinal()];
			if (id < 0) return 0f;
			return activeInputs[id];
		}
		public void set(APNeededInputs inputType, float value) {
			int id = reqToArrayMap[inputType.ordinal()];
			if (id < 0) return;
			activeInputs[id] = value;
		}
		
		public float[] nonBatteryMfgGHGperKgVeh() {return nonBatteryMfgGHGperKgVeh;}
		public float[] batteryMfgGHGperKWh() {return batteryMfgGHGperKWh;}
		
		public void setNonBatteryMfgGHGperKgVeh(float gCO2perKgVeh, int vehID) {
			nonBatteryMfgGHGperKgVeh[vehID] = gCO2perKgVeh;
		}
		public void setBatteryMfgGHGperKWhBattery(float gCO2perKWhBattery, int vehID) {
			batteryMfgGHGperKWh[vehID] = gCO2perKWhBattery;
		}
		
		public boolean includeMfgGHG() {return includeMfgGHG;}
		public void setIncludeMfgGHG(boolean flag) {
			if (!mfgGHGInputsNeeded()) return;
			includeMfgGHG = flag;
		}
		
		public float lifetimeMilesLCA() {return lifetimeMilesLCA;}
		public void setLifetimeMilesLCA(float lifetimeMiles) {
			if (!mfgGHGInputsNeeded()) return;
			lifetimeMilesLCA = lifetimeMiles;
		}
	}
	
	public class OutputStructure {
		private float[][] vStore;	//First index on vehicle ID, second index on output type
		private int[] reqToArrayMap;	
		private OutputStructure() {
			vStore = new float[avms.numVehModels()][returnedOutputs.length];
			
			reqToArrayMap = new int[APAvOutputsPerVehModel.values().length];
			for (int i=0; i<reqToArrayMap.length; i++) reqToArrayMap[i] = -1;
			for (int i=0; i<returnedOutputs.length; i++) {
				reqToArrayMap[returnedOutputs[i].ordinal()] = i;
			}
		}
		public float get(APAvOutputsPerVehModel outputType, int vehID) {
			int id = reqToArrayMap[outputType.ordinal()];
			if (id < 0) return 0;
			return vStore[vehID][id];
		}
		private void set(APAvOutputsPerVehModel outputType, float value, int vehID) {
			int id = reqToArrayMap[outputType.ordinal()];
			if (id < 0) return;
			vStore[vehID][id] = value;
		}
		
		@Override public String toString() {
			String lsep = System.getProperty("line.separator");
			
			String st = "Vehicle Model";			
			for (int i=0; i<returnedOutputs.length; i++) st = st + "," + returnedOutputs[i].captionWOUnits;
			st = st + lsep;
			
			for (int i=0; i<vStore.length; i++) {
				st = st + avms.vehModelsSetup()[i].shortName;
				for (int j=0; j<returnedOutputs.length; j++) st = st + "," + vStore[i][j];
				st = st + lsep;
			}
			
			return st;
		}
	}
	
	public OutputStructure calculateAverages(int solSetID, InputStructure inputs) {
		OutputStructure res = new OutputStructure();
		
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();
		
		for (int i=0; i<fsofModels.length; i++) {
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case cv:
			case hev:
			{
				FEcoSummayNonPlugin npg = fecoLoo.getSummary_NPG(solSetID, i);
				float fuelPerMile = npg.avFuelOrKWhperMile();
				
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				float costperUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					res.set(APAvOutputsPerVehModel.dieselAv_galpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					costperUnitFuel = inputs.get(APNeededInputs.dieselPrice_dollarPerGal);
					break;
				case gas:
					res.set(APAvOutputsPerVehModel.gasAv_galpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					costperUnitFuel = inputs.get(APNeededInputs.gasPrice_dollarPerGal);
					break;
				case h2:
					res.set(APAvOutputsPerVehModel.h2Av_kgpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					costperUnitFuel = inputs.get(APNeededInputs.h2Price_dollarPerKg);
					break;
				case ng:
					res.set(APAvOutputsPerVehModel.ngAv_m3pm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					costperUnitFuel = inputs.get(APNeededInputs.ngPrice_dollarPerM3);
					break;
				}
				
				res.set(APAvOutputsPerVehModel.fuelGHGPerUnit, ghgPerUnitFuel, i);
				res.set(APAvOutputsPerVehModel.fuelPricePerUnit, costperUnitFuel, i);

				float fuelGHG_gCO2PerMile = ghgPerUnitFuel*fuelPerMile;
				float fuelCost_dollarPerMile = costperUnitFuel*fuelPerMile;
				res.set(APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, fuelGHG_gCO2PerMile, i);
				res.set(APAvOutputsPerVehModel.fuelCost_dollarPerMile, fuelCost_dollarPerMile, i);

				float mfgGHG = 0f;
				if (inputs.includeMfgGHG) {
					float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
					float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
					float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
					float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
					float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
					float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
					
					res.set(APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage, nonBatMfgGHGperMile, i);
					
					float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
					float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

					res.set(APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage, batteryMfgPerMile, i);
					mfgGHG = nonBatMfgGHGperMile + batteryMfgPerMile;
				}
				
				float netGHG = fuelGHG_gCO2PerMile + mfgGHG;
				float colCostPerMile = fuelCost_dollarPerMile;
				res.set(APAvOutputsPerVehModel.netGHG_gCO2perMile, netGHG, i);
				res.set(APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, colCostPerMile, i);
			}
				break;
			case phev:
			{
				float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
				
				FEcoSummayPHEV chgPHEV = fecoLoo.getSummary_PHEVwChg(solSetID, i, chgWindowCombID);
				FEcoSummayPHEV nonChgPHEV = fecoLoo.getSummary_PHEVwoChg(solSetID, i);
				FEcoSummayPHEV effPHEV = chgPHEV;
				if (nonChgPHEV != null) {
					float fracNonChg = inputs.get(APNeededInputs.fracNonChgPHEVs);
					effPHEV = FEcoSummayPHEV.weighedSummary(nonChgPHEV, fracNonChg, chgPHEV, 1f-fracNonChg);
				}
				
				float fuelPerMile = effPHEV.allMilesAvFuelperMile();
				
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				float costperUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					res.set(APAvOutputsPerVehModel.dieselAv_galpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					costperUnitFuel = inputs.get(APNeededInputs.dieselPrice_dollarPerGal);
					break;
				case gas:
					res.set(APAvOutputsPerVehModel.gasAv_galpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					costperUnitFuel = inputs.get(APNeededInputs.gasPrice_dollarPerGal);
					break;
				case h2:
					res.set(APAvOutputsPerVehModel.h2Av_kgpm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					costperUnitFuel = inputs.get(APNeededInputs.h2Price_dollarPerKg);
					break;
				case ng:
					res.set(APAvOutputsPerVehModel.ngAv_m3pm, fuelPerMile, i);
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					costperUnitFuel = inputs.get(APNeededInputs.ngPrice_dollarPerM3);
					break;
				}
				
				res.set(APAvOutputsPerVehModel.fuelGHGPerUnit, ghgPerUnitFuel, i);
				res.set(APAvOutputsPerVehModel.fuelPricePerUnit, costperUnitFuel, i);

				float fuelGHG_gCO2PerMile = ghgPerUnitFuel*fuelPerMile;
				float fuelCost_dollarPerMile = costperUnitFuel*fuelPerMile;
				res.set(APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, fuelGHG_gCO2PerMile, i);
				res.set(APAvOutputsPerVehModel.fuelCost_dollarPerMile, fuelCost_dollarPerMile, i);

				float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);
				ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forPHEV(solSetID, i, chgWindowCombID, fracMinGHGChgTiming);
				
				float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
				float avGridPrice = inputs.get(APNeededInputs.gridAvPrice_dollarPerKWh);
				float dcFastPremium = inputs.get(APNeededInputs.dcFastPricePremium);
				
				ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
				float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
				float eqGridPrice = chgSummary.eqCost_perKWh(avGridPrice, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast, dcFastPremium);

				float kWhpm = effPHEV.allMilesAvKWHperMile();
				res.set(APAvOutputsPerVehModel.electAv_kWhpm, kWhpm, i);
				
				float drivingElectGHGperMile = eqGridGHG * kWhpm;
				float drivingElectDollarPerMile = eqGridPrice * kWhpm;
				
				res.set(APAvOutputsPerVehModel.electToBattery_gCO2perKWh, eqGridGHG, i);
				res.set(APAvOutputsPerVehModel.electToBattery_dollarPerKWh, eqGridPrice, i);
				res.set(APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, drivingElectGHGperMile, i);
				res.set(APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, drivingElectDollarPerMile, i);

				float mfgGHG = 0f;
				if (inputs.includeMfgGHG) {
					float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
					float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
					float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
					float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
					float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
					float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
					
					res.set(APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage, nonBatMfgGHGperMile, i);
					
					float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
					float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

					res.set(APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage, batteryMfgPerMile, i);
					mfgGHG = nonBatMfgGHGperMile + batteryMfgPerMile;
				}
				
				float netGHG = fuelGHG_gCO2PerMile + drivingElectGHGperMile + mfgGHG;
				float colCostPerMile = fuelCost_dollarPerMile + drivingElectDollarPerMile;
				res.set(APAvOutputsPerVehModel.netGHG_gCO2perMile, netGHG, i);
				res.set(APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, colCostPerMile, i);
			}
				break;
			case bev:
			{
				if (fsG.numBEVRepVeh() > 0) {
					//BEV w/ Replacement Vehicle(s)
					float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
					float rangeAnxID = inputs.get(APNeededInputs.bevRangeAnxID);
					float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);
					
					ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forBEVwRep(solSetID, i, chgWindowCombID, rangeAnxID, fracMinGHGChgTiming);

					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					float avGridPrice = inputs.get(APNeededInputs.gridAvPrice_dollarPerKWh);
					float dcFastPremium = inputs.get(APNeededInputs.dcFastPricePremium);
					
					ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
					float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
					float eqGridPrice = chgSummary.eqCost_perKWh(avGridPrice, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast,
							dcFastPremium);
					
					FEcoSummayBEVwRep effBEV = fecoLoo.getSummary_BEVwRep(solSetID, i, chgWindowCombID, rangeAnxID);
					
					float kWhpm = effBEV.avKWhpm();
					res.set(APAvOutputsPerVehModel.electAv_kWhpm, kWhpm, i);

					res.set(APAvOutputsPerVehModel.electAv_kWhpm, kWhpm, i);
					
					float drivingElectGHGperMile = eqGridGHG * kWhpm;
					float drivingElectDollarPerMile = eqGridPrice * kWhpm;
					
					res.set(APAvOutputsPerVehModel.electToBattery_gCO2perKWh, eqGridGHG, i);
					res.set(APAvOutputsPerVehModel.electToBattery_dollarPerKWh, eqGridPrice, i);
					res.set(APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, drivingElectGHGperMile, i);
					res.set(APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, drivingElectDollarPerMile, i);

					float fracDaysFailed = effBEV.fracDaysFailed();
					float fracDaysOnBEV = effBEV.fracDaysBEV();
					float fracDaysReplaced = 1f - fracDaysOnBEV;
					float fracMilesOnBEV = effBEV.fracMilesOnBEV();
					float fracMilesOnRepVeh = 1f - fracMilesOnBEV;
					
					res.set(APAvOutputsPerVehModel.bevFracDaysFailed, fracDaysFailed, i);
					res.set(APAvOutputsPerVehModel.bevFracDaysReplaced, fracDaysReplaced, i);
					res.set(APAvOutputsPerVehModel.bevFracMilesReplaced, fracMilesOnRepVeh, i);

					int[] repVehIDs = fsG.bevRepVehModelID();
					float[] repVehFuelPerMile = effBEV.avRepVehFuelPerMile();
					
					float fracRepVehID = inputs.get(APNeededInputs.bevRepVehID);
					
					float fuelGHG_gCO2PerMile = 0f;
					float fuelCost_dollarPerMile = 0f;
					float dieselGalPerMile = 0f;
					float gasGalPerMile = 0f;
					float h2KgPerMile = 0f;
					float ngM3PerMile = 0f;

					for (int j=0; j<repVehIDs.length; j++) {
						//Loop on replacement vehicle(s) fuel type
						PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[repVehIDs[j]].vehModelParam));
						
						float fracOnCurrentRepVeh = Math.abs((1f - j) - fracRepVehID);
						
						switch (fuelType) {
						case diesel:
							dieselGalPerMile += fracOnCurrentRepVeh*repVehFuelPerMile[j];
							float ghgPerGalDiesel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
							float costPerGalDiesel = inputs.get(APNeededInputs.dieselPrice_dollarPerGal);
							
							fuelGHG_gCO2PerMile += ghgPerGalDiesel*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							fuelCost_dollarPerMile += costPerGalDiesel*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							break;
						case gas:
							gasGalPerMile += fracOnCurrentRepVeh*repVehFuelPerMile[j];
							float ghgPerGalGas = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
							float costPerGalGas = inputs.get(APNeededInputs.gasPrice_dollarPerGal);
							
							fuelGHG_gCO2PerMile += ghgPerGalGas*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							fuelCost_dollarPerMile += costPerGalGas*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							break;
						case h2:
							h2KgPerMile += fracOnCurrentRepVeh*repVehFuelPerMile[j];
							float ghgPerKgH2 = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
							float costPerKgH2 = inputs.get(APNeededInputs.h2Price_dollarPerKg);
							
							fuelGHG_gCO2PerMile += ghgPerKgH2*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							fuelCost_dollarPerMile += costPerKgH2*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							break;
						case ng:
							ngM3PerMile += fracOnCurrentRepVeh*repVehFuelPerMile[j];
							float ghgPerM3NG = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
							float costPerM3NG = inputs.get(APNeededInputs.ngPrice_dollarPerM3);
							
							fuelGHG_gCO2PerMile += ghgPerM3NG*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							fuelCost_dollarPerMile += costPerM3NG*repVehFuelPerMile[j]*fracOnCurrentRepVeh;
							break;
						}
					}
					
					res.set(APAvOutputsPerVehModel.dieselAv_galpm, dieselGalPerMile, i);
					res.set(APAvOutputsPerVehModel.gasAv_galpm, gasGalPerMile, i);
					res.set(APAvOutputsPerVehModel.h2Av_kgpm, h2KgPerMile, i);
					res.set(APAvOutputsPerVehModel.ngAv_m3pm, ngM3PerMile, i);
					
					res.set(APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, fuelGHG_gCO2PerMile, i);
					res.set(APAvOutputsPerVehModel.fuelCost_dollarPerMile, fuelCost_dollarPerMile, i);
					
					float bevRepVehCostPerMile = inputs.get(APNeededInputs.bevRepCost_dollarPerMile);
					float bevRepVehCostPerDay = inputs.get(APNeededInputs.bevRepCost_dollarPerDay);
					float replacedMilesPerYear = inputs.get(APNeededInputs.avMilesDrivenPerYear) * fracMilesOnRepVeh;
					float replacedDaysPerYear = inputs.get(APNeededInputs.avDaysDrivenPerYear) * fracDaysReplaced;
					float bevRepVehAnnualCost = bevRepVehCostPerMile * replacedMilesPerYear + bevRepVehCostPerDay * replacedDaysPerYear;
					
					float bevRepCostPerMile = 0f;
					if (replacedMilesPerYear > 0) bevRepCostPerMile = bevRepVehAnnualCost/replacedMilesPerYear;
					res.set(APAvOutputsPerVehModel.bevRepCost_netDollarPerMile, bevRepCostPerMile, i);
					
					float mfgGHG = 0f;
					if (inputs.includeMfgGHG) {
						float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
						float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
						float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
						float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
						float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
						float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
						
						res.set(APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage, nonBatMfgGHGperMile, i);
						
						float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
						float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
						float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
						float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

						res.set(APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage, batteryMfgPerMile, i);
						mfgGHG = nonBatMfgGHGperMile + batteryMfgPerMile;
					}
					
					float netGHG = fuelGHG_gCO2PerMile*fracMilesOnRepVeh + drivingElectGHGperMile*fracMilesOnBEV + mfgGHG;
					float colCostPerMile = fuelCost_dollarPerMile*fracMilesOnRepVeh + drivingElectDollarPerMile*fracMilesOnBEV 
							+ bevRepCostPerMile*fracMilesOnRepVeh;
					res.set(APAvOutputsPerVehModel.netGHG_gCO2perMile, netGHG, i);
					res.set(APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, colCostPerMile, i);

				} else {
					//infBattery mode
					FEcoSummayNonPlugin npg = fecoLoo.getSummary_NPG(solSetID, i);
					
					float kWhpm = npg.avFuelOrKWhperMile();
					res.set(APAvOutputsPerVehModel.electAv_kWhpm, kWhpm, i);
					
					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					float avGridPrice = inputs.get(APNeededInputs.gridAvPrice_dollarPerKWh);
					float chargingEff = chEffMan.vehChEff(i).chEffL2;
					
					float ghgPerKWh = avGridGHG/chargingEff;
					float dollarPerKWh = avGridPrice/chargingEff;
					float drivingElectGHGperMile = ghgPerKWh * kWhpm;
					float drivingElectDollarPerMile = dollarPerKWh * kWhpm;
					
					res.set(APAvOutputsPerVehModel.electToBattery_gCO2perKWh, ghgPerKWh, i);
					res.set(APAvOutputsPerVehModel.electToBattery_dollarPerKWh, dollarPerKWh, i);
					res.set(APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, drivingElectGHGperMile, i);
					res.set(APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, drivingElectDollarPerMile, i);

					float mfgGHG = 0f;
					if (inputs.includeMfgGHG) {
						float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
						float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
						float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
						float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
						float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
						float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
						
						res.set(APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage, nonBatMfgGHGperMile, i);
						
						float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
						float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
						float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
						float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

						res.set(APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage, batteryMfgPerMile, i);
						mfgGHG = nonBatMfgGHGperMile + batteryMfgPerMile;
					}
					
					float netGHG = drivingElectGHGperMile + mfgGHG;
					float colCostPerMile = drivingElectDollarPerMile;
					res.set(APAvOutputsPerVehModel.netGHG_gCO2perMile, netGHG, i);
					res.set(APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, colCostPerMile, i);
				}
			}
				break;
			}
		}
		
		return res;
	}
	
	public float calcMaxGramsCO2perMile(int solSetID, InputStructure inputs) {
		float maxGCO2perMile = 1f;
		
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();		
		for (int i=0; i<fsofModels.length; i++) {
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case hev:
			case cv:
			{
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					break;
				case gas:
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					break;
				case h2:
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					break;
				case ng:
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					break;
				}
				
				float curVehMaxGHGperMile = fecoLoo.getMaxGHG_NPG(solSetID, i, ghgPerUnitFuel);
				maxGCO2perMile = Math.max(maxGCO2perMile, curVehMaxGHGperMile);
			}
				break;
			case phev:
			{
				float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
				float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);
				ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forPHEV(solSetID, i, chgWindowCombID, fracMinGHGChgTiming);
				
				float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
				ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
				float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
				
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					break;
				case gas:
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					break;
				case h2:
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					break;
				case ng:
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					break;
				}
				
								
				float ghgCurPHEV = fecoLoo.getMaxGHG_PHEVwChg(solSetID, i, chgWindowCombID, eqGridGHG, ghgPerUnitFuel);				
				FEcoSummayPHEV nonChgPHEV = fecoLoo.getSummary_PHEVwoChg(solSetID, i);
				
				if (nonChgPHEV != null) {
					float fracNonChg = inputs.get(APNeededInputs.fracNonChgPHEVs);
					if (fracNonChg > 0) {
						float ghgCurNonPHEV = fecoLoo.getMaxGHG_PHEVwoChg(solSetID, i, eqGridGHG, ghgPerUnitFuel);
						ghgCurPHEV = Math.max(ghgCurPHEV, ghgCurNonPHEV);
					}
				}
				
				maxGCO2perMile = Math.max(maxGCO2perMile, ghgCurPHEV);
			}
				break;
			case bev:
			{
				if (fsG.numBEVRepVeh() > 0) {
					//BEV w/ Replacement Vehicle(s)
					float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
					float rangeAnxID = inputs.get(APNeededInputs.bevRangeAnxID);
					float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);					
					ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forBEVwRep(solSetID, i, chgWindowCombID, rangeAnxID, fracMinGHGChgTiming);

					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					
					ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
					float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
					
					int[] repVehIDs = fsG.bevRepVehModelID();
					float[] repVehGHGperUnitFuel = new float[repVehIDs.length];

					for (int j=0; j<repVehIDs.length; j++) {
						//Loop on replacement vehicle(s) fuel type
						PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[repVehIDs[j]].vehModelParam));
						
						switch (fuelType) {
						case diesel:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
							break;
						case gas:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
							break;
						case h2:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
							break;
						case ng:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
							break;
						}
					}

					float curVehMaxGHGperMile = fecoLoo.getMaxGHG_BEVwRep(solSetID, i, chgWindowCombID, rangeAnxID, eqGridGHG, repVehGHGperUnitFuel);
					maxGCO2perMile = Math.max(maxGCO2perMile, curVehMaxGHGperMile);
				} else {
					//Inf-battery mode
					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
					float ghgPerUnitElect = avGridGHG * curVehChgEffs.chEffL2;
					
					float curVehMaxGHGperMile = fecoLoo.getMaxGHG_NPG(solSetID, i, ghgPerUnitElect);
					maxGCO2perMile = Math.max(maxGCO2perMile, curVehMaxGHGperMile);
				}
			}
				break;
			}
			System.out.println(fsofModels[i].vehModelParam.general.name + " --> current maxGCO2perMile = "+maxGCO2perMile);
		}
		
		return maxGCO2perMile;
	}
		
	public static class VehGHGAnalysisResult {
		public float mfgEqGCO2perMile, averageGCO2perMile, wellToBattery_gCO2perKWh, fracMilesOnBEV, fracDaysOnBEV, fracDaysAlteredPlans;
		public float[][] stackedHourlyChargingPDM;	//First index on charger type (L1, L2, DC-Fast),
													//Second index on hour of the day 0 to 23, 
													//values = fraction of total charging amount
		public CGHGHistogram ghgHistogram;
		
		private VehGHGAnalysisResult(float hstLimGCO2perMile, int hstNumBins) {
			ghgHistogram = new CGHGHistogram(hstLimGCO2perMile, hstNumBins);
			stackedHourlyChargingPDM = null;
			
			mfgEqGCO2perMile = -1;
			averageGCO2perMile = hstLimGCO2perMile - 0.5f*(hstLimGCO2perMile/(float)hstNumBins);
			
			wellToBattery_gCO2perKWh = -1;
			fracMilesOnBEV = -1;
			fracDaysOnBEV = -1;
			fracDaysAlteredPlans = -1;
		}
		
		public void genReportFile(String fname) {
			try {
				FileWriter fout = new FileWriter(fname);
				String lsep = System.getProperty("line.separator");

				fout.append("__miscInfo"+lsep);
				
				if (wellToBattery_gCO2perKWh >= 0) {
					fout.append("wellToBattery_gCO2perKWh,"+wellToBattery_gCO2perKWh+lsep);
				}
				if (fracMilesOnBEV >= 0) {
					fout.append("fracMilesOnBEV,"+fracMilesOnBEV+lsep);
				}
				if (fracDaysOnBEV >= 0) {
					fout.append("fracDaysOnBEV,"+fracDaysOnBEV+lsep);
				}
				if (fracDaysAlteredPlans >= 0) {
					fout.append("fracDaysAlteredPlans,"+fracDaysAlteredPlans+lsep);
				}

				fout.append(lsep);
				fout.flush();
				
				if (stackedHourlyChargingPDM != null) {
					fout.append("__hourlyCharging"+lsep);
					fout.append("hour,"+ChargerTypes.L1.shortName+","+ChargerTypes.L2.shortName+","+ChargerTypes.DC.shortName+lsep);
					
					for (int i=0; i<ChgEventsCaseSummary.HoursPerDay; i++) {
						String stOut = ""+i;
						stOut = stOut + "," + stackedHourlyChargingPDM[ChargerTypes.L1.ordinal()][i];
						stOut = stOut + "," + stackedHourlyChargingPDM[ChargerTypes.L2.ordinal()][i];
						stOut = stOut + "," + stackedHourlyChargingPDM[ChargerTypes.DC.ordinal()][i] + lsep;
						fout.append(stOut);
					}

					fout.append(lsep);
					fout.flush();
				}				
				
				CGHGHistogram.BoxPlot bxp = ghgHistogram.getBoxPlotInOutputUnits(averageGCO2perMile);
				fout.append("__ghgBoxPlot"+lsep);
				bxp.writeInCSVFile(fout);				
				fout.append(lsep);
				fout.flush();
				
				if (mfgEqGCO2perMile > 0) {
					CGHGHistogram.BoxPlot bxpWMfgGHG = new CGHGHistogram.BoxPlot(bxp, mfgEqGCO2perMile);
					fout.append("__ghgBoxPlot_wMfgGHG"+lsep);
					bxpWMfgGHG.writeInCSVFile(fout);
					fout.append(lsep);
					fout.flush();
				}
				
				fout.append("__ghgHistogramPlots"+lsep);
				ghgHistogram.writeHstLadderStepInCSVFile(fout);
				
				fout.flush();
				fout.close();
			} catch (Exception e) {}
		}
	}
	
	public VehGHGAnalysisResult[] analyzeGHG(int solSetID, InputStructure inputs, float limGCO2perMile, int hstNumBins) {
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();	
		VehGHGAnalysisResult[] vehiclesResult = new VehGHGAnalysisResult[fsofModels.length];

		for (int i=0; i<fsofModels.length; i++) {
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case cv:
			case hev:
			{
				FEcoSummayNonPlugin npg = fecoLoo.getSummary_NPG(solSetID, i);
				float avFuelPerMile = npg.avFuelOrKWhperMile();
				
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					break;
				case gas:
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					break;
				case h2:
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					break;
				case ng:
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					break;
				}
				
				vehiclesResult[i] = new VehGHGAnalysisResult(limGCO2perMile, hstNumBins);
				vehiclesResult[i].averageGCO2perMile = avFuelPerMile * ghgPerUnitFuel;
				fecoLoo.populateGHGHistogram_NPG(vehiclesResult[i].ghgHistogram, solSetID, i, ghgPerUnitFuel);
				vehiclesResult[i].ghgHistogram.finishAddingTrips();

				if (inputs.includeMfgGHG) {
					float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
					float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
					float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
					float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
					float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
					float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
										
					float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
					float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

					vehiclesResult[i].mfgEqGCO2perMile = nonBatMfgGHGperMile + batteryMfgPerMile;
				}
			}
				break;
			case phev:
			{
				float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
				float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);
				ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forPHEV(solSetID, i, chgWindowCombID, fracMinGHGChgTiming);
				
				float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
				ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
				float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
				
				PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[i].vehModelParam));
				float ghgPerUnitFuel = 0f;
				
				switch (fuelType) {
				case diesel:
					ghgPerUnitFuel = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
					break;
				case gas:
					ghgPerUnitFuel = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
					break;
				case h2:
					ghgPerUnitFuel = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
					break;
				case ng:
					ghgPerUnitFuel = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
					break;
				}
				
				vehiclesResult[i] = new VehGHGAnalysisResult(limGCO2perMile, hstNumBins);
												
				FEcoSummayPHEV nonChgPHEV = fecoLoo.getSummary_PHEVwoChg(solSetID, i);
				FEcoSummayPHEV chgPHEV = fecoLoo.getSummary_PHEVwChg(solSetID, i, chgWindowCombID);
				
				float fracNonChg = 0f;
				float fracCharging = 1f;
				
				if (nonChgPHEV != null) {
					fracNonChg = inputs.get(APNeededInputs.fracNonChgPHEVs);
					fracCharging = 1f - fracNonChg;
					
					if (fracCharging >= FEcoCasesLookup.ZTolFracNonChg) {
						float avGHGperMileChgPHEVs = ghgPerUnitFuel*chgPHEV.allMilesAvFuelperMile() + eqGridGHG*chgPHEV.allMilesAvKWHperMile();
						float avGHGperMileNonChgPHEVs = ghgPerUnitFuel*nonChgPHEV.allMilesAvFuelperMile();
						vehiclesResult[i].averageGCO2perMile = fracCharging*avGHGperMileChgPHEVs + fracNonChg*avGHGperMileNonChgPHEVs;

						vehiclesResult[i].stackedHourlyChargingPDM = chgSummary.pdmsChgType();
						vehiclesResult[i].wellToBattery_gCO2perKWh = eqGridGHG;
					} else {
						float avGHGperMileNonChgPHEVs = ghgPerUnitFuel*nonChgPHEV.allMilesAvFuelperMile();
						vehiclesResult[i].averageGCO2perMile = avGHGperMileNonChgPHEVs;
					}
				} else {					
					float avGHGperMileChgPHEVs = ghgPerUnitFuel*chgPHEV.allMilesAvFuelperMile() + eqGridGHG*chgPHEV.allMilesAvKWHperMile();
					vehiclesResult[i].averageGCO2perMile = avGHGperMileChgPHEVs;
					
					vehiclesResult[i].stackedHourlyChargingPDM = chgSummary.pdmsChgType();
					vehiclesResult[i].wellToBattery_gCO2perKWh = eqGridGHG;					
				}
				//Fill trips into histogram
				fecoLoo.populateGHGHistogram_PHEV(vehiclesResult[i].ghgHistogram, solSetID, i, chgWindowCombID, fracNonChg, eqGridGHG, ghgPerUnitFuel);
				vehiclesResult[i].ghgHistogram.finishAddingTrips();
				
				if (inputs.includeMfgGHG) {
					float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
					float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
					float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
					float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
					float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
					float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
										
					float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
					float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

					vehiclesResult[i].mfgEqGCO2perMile = nonBatMfgGHGperMile + batteryMfgPerMile;
				}
			}
				break;
			case bev:
			{
				vehiclesResult[i] = new VehGHGAnalysisResult(limGCO2perMile, hstNumBins);

				if (fsG.numBEVRepVeh() > 0) {
					//BEV w/ Replacement Vehicle(s)
					float chgWindowCombID = inputs.get(APNeededInputs.chgWindowID);
					float rangeAnxID = inputs.get(APNeededInputs.bevRangeAnxID);
					float fracMinGHGChgTiming = inputs.get(APNeededInputs.chgTimingOptID);					
					ChgEventsCaseSummary.CEHistogram chgSummary = chgLoo.getChgSummay_forBEVwRep(solSetID, i, chgWindowCombID, rangeAnxID, fracMinGHGChgTiming);

					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					
					ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
					float eqGridGHG = chgSummary.eqGHG_perKWh(avGridGHG, curVehChgEffs.chEffL1, curVehChgEffs.chEffL2, curVehChgEffs.chEffDCFast);
					
					vehiclesResult[i].wellToBattery_gCO2perKWh = eqGridGHG;
					vehiclesResult[i].stackedHourlyChargingPDM = chgSummary.pdmsChgType();

					int[] repVehIDs = fsG.bevRepVehModelID();
					float[] repVehGHGperUnitFuel = new float[repVehIDs.length];

					for (int j=0; j<repVehIDs.length; j++) {
						//Loop on replacement vehicle(s) fuel type
						PowertrainType.FuelType fuelType = PowertrainType.fuelType(PowertrainType.decode(fsofModels[repVehIDs[j]].vehModelParam));
						
						switch (fuelType) {
						case diesel:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.dieselGHG_gCO2PerGal);
							break;
						case gas:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.gasGHG_gCO2PerGal);
							break;
						case h2:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.h2GHG_gCO2PerKg);
							break;
						case ng:
							repVehGHGperUnitFuel[j] = inputs.get(APNeededInputs.ngGHG_gCO2PerM3);
							break;
						}
					}
					
					FEcoSummayBEVwRep effBEV = fecoLoo.getSummary_BEVwRep(solSetID, i, chgWindowCombID, rangeAnxID);
					
					vehiclesResult[i].fracDaysAlteredPlans = effBEV.fracDaysFailed();
					vehiclesResult[i].fracDaysOnBEV = effBEV.fracDaysBEV();
					
					float fracMilesOnBEV = effBEV.fracMilesOnBEV();
					float fracMilesOnRepVeh = 1f - fracMilesOnBEV;
					vehiclesResult[i].fracMilesOnBEV = fracMilesOnBEV;
					
					float avGHGperMile = fracMilesOnBEV * effBEV.avKWhpm() * eqGridGHG;
					float fracRepVehID = inputs.get(APNeededInputs.bevRepVehID);
					
					for (int j=0; j<repVehGHGperUnitFuel.length; j++) {
						float fracOnCurrentRepVeh = Math.abs((1f - j) - fracRepVehID);
						avGHGperMile += fracOnCurrentRepVeh*fracMilesOnRepVeh * effBEV.avRepVehFuelPerMile()[j] * repVehGHGperUnitFuel[j];
					}
					
					vehiclesResult[i].averageGCO2perMile = avGHGperMile;
					
					fecoLoo.populateGHGHistogram_BEV(vehiclesResult[i].ghgHistogram, solSetID, i, chgWindowCombID,
							rangeAnxID, eqGridGHG, repVehGHGperUnitFuel, fracRepVehID);
					vehiclesResult[i].ghgHistogram.finishAddingTrips();
				} else {
					//Inf-battery mode
					float avGridGHG = inputs.get(APNeededInputs.gridAvGHG_gCO2perKWh);
					ChargingEffManager.VehChargingEfficiencies curVehChgEffs = chEffMan.vehChEff(i);
					float ghgPerUnitElect = avGridGHG * curVehChgEffs.chEffL2;
					vehiclesResult[i].wellToBattery_gCO2perKWh = ghgPerUnitElect;
					
					FEcoSummayNonPlugin npg = fecoLoo.getSummary_NPG(solSetID, i);
					vehiclesResult[i].averageGCO2perMile = npg.avFuelOrKWhperMile() * ghgPerUnitElect;
					fecoLoo.populateGHGHistogram_NPG(vehiclesResult[i].ghgHistogram, solSetID, i, ghgPerUnitElect);
					vehiclesResult[i].ghgHistogram.finishAddingTrips();
				}
				
				if (inputs.includeMfgGHG) {
					float gCO2perKgVeh = inputs.nonBatteryMfgGHGperKgVeh[i];
					float vehMassWCargo = fsofModels[i].vehModelParam.massProp.totalKg;
					float cargoMass = fsofModels[i].vehModelParam.massProp.cargoKg;
					float batteryMass = fsofModels[i].vehModelParam.massProp.batteryKg / fsofModels[i].vehModelParam.compMassMultiplier;
					float nonBatMfgLifetimeGHG = gCO2perKgVeh * (vehMassWCargo - cargoMass - batteryMass);
					float nonBatMfgGHGperMile = nonBatMfgLifetimeGHG/inputs.lifetimeMilesLCA;
										
					float batteryMfg_gCO2perKWh = inputs.batteryMfgGHGperKWh[i];
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					float batteryMfgLifetimeGHG = batteryMfg_gCO2perKWh * batteryKWh;
					float batteryMfgPerMile = batteryMfgLifetimeGHG/inputs.lifetimeMilesLCA;

					vehiclesResult[i].mfgEqGCO2perMile = nonBatMfgGHGperMile + batteryMfgPerMile;
				}
			}
				break;
			}
		}		
		
		return vehiclesResult;
	}
}
