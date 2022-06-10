package pvc.runners;

import java.io.FileWriter;
import java.util.ArrayList;

import fastsimjava.*;
import fastsimjava.components.FSJSimConstants;
import pvc.calc.*;
import pvc.calc.comp.*;
import pvc.calc.TCOCalculator.APTCOvsGHGSummaryOutputs;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.APSliderBars;
import pvc.datamgmt.comp.PowertrainType;
import pvc.utility.JSONData;

public class VehSummaiesJSON {
	//Prevent instantiation
	private VehSummaiesJSON() {}

	//Function for creating a JSON summaries file (given file structure, analysis ID and solution set ID)
	public static void createJSONOutput(FFStructure fs, int aID, int sID) throws Exception {
		
		//Read data from files
		FEcoSimsC fecoSims = new FEcoSimsC(fs, aID);
		AnalysisVehModelsSetup avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
		WIITModel wiitModel = WIITModel.readWIITModel(fs, aID, avms);
		BEVCommercialModel bevMoreCommVeh = new BEVCommercialModel(fs, aID, wiitModel);
		SliderBarsManager sbarMan = new SliderBarsManager(fs, aID, avms, wiitModel, bevMoreCommVeh);
		NoneBatteryMfgGHGModel nonBatMfgGHGModel = new NoneBatteryMfgGHGModel(fs, aID, avms);
		BEVRepCosts bevRepCosts = new BEVRepCosts(fs, aID, wiitModel);
		LicIMModel licimModel = new LicIMModel(fs, aID, avms);
		VehDeprModels deprModels = new VehDeprModels(fs, aID, avms);
		HomeChargerCosts homeChgCosts = new HomeChargerCosts(fs, aID);
		FEcoSimsGenerator fsG = FEcoSimsGenerator.createFEcoSimsGenerator(fs, aID, avms, wiitModel);
		ChargingEffManager chEffMan = ChargingEffManager.readFromFile(avms.vehModelsSetup(), fs.getFilePath_vehChgEfficiencies(aID));
		ChgCasesLookup chgLoo = ChgCasesLookup.read_summariesOnly(fecoSims, fsG);
		FEcoCasesLookup fecoLoo = FEcoCasesLookup.read_summariesOnly(fecoSims, fsG);
		UsePhaseSSimulator upSim = new UsePhaseSSimulator(fecoLoo, chgLoo, fsG, chEffMan);
		UsePhaseSSimulator.InputStructure upsInput = upSim.createInputStructure();
		TCOCalculator tcoCalc = new TCOCalculator(fsG, deprModels, licimModel, homeChgCosts);
		
		//Get a solution for current state of the vehicles
		sbarMan.prepUPInputs(upsInput, upSim, nonBatMfgGHGModel.vehicleLifetimeMiles,
				nonBatMfgGHGModel.gCO2perKgVehicle_lowEnd, nonBatMfgGHGModel.gCO2perKgVehicle_highEnd, 
				bevRepCosts.dollarsPerMile, bevRepCosts.dollarsPerDay);
		UsePhaseSSimulator.OutputStructure upRes = upSim.calculateAverages(sbarMan.rvStatus().solSetID(), upsInput);
		TCOCalculator.TCOvsGHGSummaryOutputStructure tcoRes = tcoCalc.getCostVsGHGSummary(upRes, sbarMan, nonBatMfgGHGModel.vehicleLifetimeMiles);
		
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		
		//Prepare some common quantity values
		float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
		float annualMiles = sbarMan.getCurValue(APSliderBars.Cost_AnnualDriveDistance) * SliderBarsManager.annualMilesTravelledIncCurSolSet;
		float gCO2perKgVehicle_lowEnd = DEFAULT_MFG_GHG_LOW;
		float gCO2perKgVehicle_highEnd = DEFAULT_MFG_GHG_HIGH;
		int numDayTimeChgWindows = wiitModel.chgModels.daytimeChargingMinWindow.length;
		float dcCostPremium = 0f;
		try {
			dcCostPremium = wiitModel.chgModels.pricePremiumForDCFast.baseValue;
		} catch (Exception e) {}
		
		//Create Array of JSON Data for vehicles and populate it
		ArrayList<JSONData> lst = new ArrayList<JSONData>();
		
		for (int i=0; i<vms.length; i++) {
			JSONData curVehJSONData = null;
			float vehProdCostAllElse = 1000f * tcoRes.getValue(i, APTCOvsGHGSummaryOutputs.cost_allElseInVeh) / rpe;
			
			if (nonBatMfgGHGModel.gCO2perKgVehicle_lowEnd != null) gCO2perKgVehicle_lowEnd = nonBatMfgGHGModel.gCO2perKgVehicle_lowEnd[i];
			if (nonBatMfgGHGModel.gCO2perKgVehicle_highEnd != null) gCO2perKgVehicle_highEnd = nonBatMfgGHGModel.gCO2perKgVehicle_highEnd[i];

			switch (PowertrainType.decode(fsofModels[i].vehModelParam)) {
			case cv_cng:
			case cv_diesel:
			case cv_gas:
				curVehJSONData = vehDataToJSONData_cv(vms[i], fsofModels[i], vehProdCostAllElse, 
						deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
						gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
						licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
						fecoLoo.getSummary_NPG(sID, i));
				break;
			case hev_cng:
			case hev_diesel:
			case hev_gas:
				curVehJSONData = vehDataToJSONData_hev(vms[i], fsofModels[i], vehProdCostAllElse, 
						deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
						gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
						licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
						fecoLoo.getSummary_NPG(sID, i));
				break;
			case phev_cng:
			case phev_diesel:
			case phev_gas:
				curVehJSONData = vehDataToJSONData_phev(vms[i], fsofModels[i], vehProdCostAllElse, 
						deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
						gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
						licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
						fecoLoo, sID, i, chEffMan.vehChEff(i).chEffL1, numDayTimeChgWindows, chgLoo, dcCostPremium);
				break;
			case hev_fc:
				curVehJSONData = vehDataToJSONData_fcev(vms[i], fsofModels[i], vehProdCostAllElse, 
						deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
						gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
						licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
						fecoLoo.getSummary_NPG(sID, i));
				break;
			case phev_fc:
				curVehJSONData = vehDataToJSONData_fcphev(vms[i], fsofModels[i], vehProdCostAllElse, 
						deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
						gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
						licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
						fecoLoo, sID, i, chEffMan.vehChEff(i).chEffL1, numDayTimeChgWindows, chgLoo, dcCostPremium);
				break;
			case bev:
				if (wiitModel.bevRepModel.bevRepCommercial != null) {
					curVehJSONData = vehDataToJSONData_bevCommercial(vms[i], fsofModels[i], vehProdCostAllElse, 
							deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
							gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
							licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
							fecoLoo.getSummary_NPG(sID, i), chEffMan.vehChEff(i).chEffL2);
				} else {
					int numRepVeh = fsG.bevRepVehModelID().length;
					int repVehID1 = fsG.bevRepVehModelID()[0];
					int repVehID2 = fsG.bevRepVehModelID()[numRepVeh-1];
					int numAnx = wiitModel.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length;
					
					curVehJSONData = vehDataToJSONData_bev(vms, fsofModels, vehProdCostAllElse, 
							deprModels.residualValue(i, REF_NUM_YEARS_FOR_RESIDUAL, REF_NUM_YEARS_FOR_RESIDUAL*annualMiles),
							gCO2perKgVehicle_lowEnd, gCO2perKgVehicle_highEnd,
							licimModel.getAnnualLicensingCost(i), licimModel.getAnnualInsuranceCost(i), licimModel.getMaintnenaceCostPerMile(i),
							fecoLoo, sID, i, chEffMan.vehChEff(i).chEffL1, numDayTimeChgWindows, numAnx, chgLoo, dcCostPremium,
							repVehID1, repVehID2);
				}
				break;
			}
			
			if (curVehJSONData != null) {
				lst.add(curVehJSONData);
			}
		}		
		
		JSONData vehicles = new JSONData("vehicles");
		for (int i=0; i<lst.size(); i++) vehicles.add(lst.get(i));
		
		//Write Output to JSON File
		String jsFileName = fs.getFilePath_vehJSONSummary(aID, sID);
		FileWriter fout = new FileWriter(jsFileName);
		
		String jsonText = vehicles.toString();
		fout.append(jsonText);
		
		fout.flush();
		fout.close();
	}
	
	private static final float REF_NUM_YEARS_FOR_RESIDUAL = 5.0f;
	private static final float DEFAULT_MFG_GHG_LOW = 4000f;
	private static final float DEFAULT_MFG_GHG_HIGH = 7500f;

	private static final int DEFAULT_PLOT_SIZE = 5;
	private static final String DEFAULT_PLOT_COLOR_CV = "#FF0000";
	private static final String DEFAULT_PLOT_COLOR_HEV = "#EE7D31";
	private static final String DEFAULT_PLOT_COLOR_PHEV = "#00B0F0";
	private static final String DEFAULT_PLOT_COLOR_FCEV = "#7030A0";
	private static final String DEFAULT_PLOT_COLOR_FCPHEV = "#FF00FF";
	private static final String DEFAULT_PLOT_COLOR_BEV = "#00B050";
	
	private static final String ST_PTTYPE_CV = "ICE";
	private static final String ST_PTTYPE_HEV = "HEV";
	private static final String ST_PTTYPE_PHEV = "PHEV";
	private static final String ST_PTTYPE_FCEV = "FCEV";
	private static final String ST_PTTYPE_FCPHEV = "FCPHEV";
	private static final String ST_PTTYPE_BEV = "BEV";
	private static final String ST_PTTYPE_COMMERCIAL_BEV = "COMMERCIAL_BEV";
	
	
	private static final String ST_FUELTYPE_GAS = "Gasoline";
	private static final String ST_FUELTYPE_DZ = "Diesel";
	private static final String ST_FUELTYPE_NG = "Natural Gas";
	private static final String ST_FUELTYPE_H2 = "Hydrogen";

	
	private static JSONData vehDataToJSONData_phev(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoCasesLookup fecoLoo, int sID, int vehID, float avChargingEff,
			int numDayTimeChgWindows, ChgCasesLookup chgLoo, float dcCostPremium) throws Exception {
				
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_PHEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_PHEV);
		JSONData fuelType = null;
		switch (PowertrainType.decode(fsofModel.vehModelParam)) {
		case phev_cng:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_NG);
			break;
		case phev_diesel:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_DZ);
			break;
		case phev_gas:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_GAS);
			break;
		default:
			break;		
		}
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData enigneKW = new JSONData("enigneKW", fsofModel.vehModelParam.fuelConv.maxFuelConvKw);
		JSONData motorKW = new JSONData("motorKW", fsofModel.vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel.vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg 
				- fsofModel.vehModelParam.massProp.batteryKg/fsofModel.vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms.mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float fuelPerMile = 0f;
		FEcoSummayPHEV nonChgFecoSummary = fecoLoo.getSummary_PHEVwoChg(sID, vehID);
		if (nonChgFecoSummary != null) fuelPerMile = nonChgFecoSummary.allMilesAvFuelperMile();
		JSONData baselineFuelPerMile = new JSONData("baselineFuelPerMile", fuelPerMile);
		
		JSONData chgEff = new JSONData("chgEff", avChargingEff);
		
		JSONData bhvRes = new JSONData ("bhvRes");		
		JSONData overnightOnly = new JSONData ("overnightOnly");
		
		FEcoSummayPHEV chgPHEVsFecoSummary = fecoLoo.getSummary_PHEVwChg(sID, vehID, 0);		
		overnightOnly.add(new JSONData("fuelPerMile", chgPHEVsFecoSummary.allMilesAvFuelperMile()));
		overnightOnly.add(new JSONData("kWhPerMile", chgPHEVsFecoSummary.allMilesAvKWHperMile()));
		
		JSONData optCostTiming = new JSONData ("optCostTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minCost = chgLoo.getChgSummay_forPHEV(sID, vehID, 0, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		JSONData optGHGTiming = new JSONData ("optGHGTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minGHG = chgLoo.getChgSummay_forPHEV(sID, vehID, 0, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		overnightOnly.add(optCostTiming);
		overnightOnly.add(optGHGTiming);
		bhvRes.add(overnightOnly);
		
		JSONData maxDayTime = new JSONData ("maxDayTime");
		
		chgPHEVsFecoSummary = fecoLoo.getSummary_PHEVwChg(sID, vehID, numDayTimeChgWindows);		
		maxDayTime.add(new JSONData("fuelPerMile", chgPHEVsFecoSummary.allMilesAvFuelperMile()));
		maxDayTime.add(new JSONData("kWhPerMile", chgPHEVsFecoSummary.allMilesAvKWHperMile()));

		optCostTiming = new JSONData ("optCostTiming");
		chgSummary_minCost = chgLoo.getChgSummay_forPHEV(sID, vehID, numDayTimeChgWindows, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		optGHGTiming = new JSONData ("optGHGTiming");
		chgSummary_minGHG = chgLoo.getChgSummay_forPHEV(sID, vehID, numDayTimeChgWindows, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		maxDayTime.add(optCostTiming);
		maxDayTime.add(optGHGTiming);
		bhvRes.add(maxDayTime);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(enigneKW);
		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineFuelPerMile);
		jsdVehicle.add(chgEff);
		
		jsdVehicle.add(bhvRes);
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_hev(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoSummayNonPlugin fecoSummary) throws Exception {
		
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_HEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_HEV);
		JSONData fuelType = null;
		switch (PowertrainType.decode(fsofModel.vehModelParam)) {
		case hev_cng:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_NG);
			break;
		case hev_diesel:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_DZ);
			break;
		case hev_gas:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_GAS);
			break;
		default:
			break;		
		}
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData enigneKW = new JSONData("enigneKW", fsofModel.vehModelParam.fuelConv.maxFuelConvKw);
		JSONData motorKW = new JSONData("motorKW", fsofModel.vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel.vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg 
				- fsofModel.vehModelParam.massProp.batteryKg/fsofModel.vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms.mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float fuelPerMile = fecoSummary.avFuelOrKWhperMile();
		JSONData baselineFuelPerMile = new JSONData("baselineFuelPerMile", fuelPerMile);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(enigneKW);
		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineFuelPerMile);
		
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_cv(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoSummayNonPlugin fecoSummary) throws Exception {
		
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_CV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_CV);
		JSONData fuelType = null;
		switch (PowertrainType.decode(fsofModel.vehModelParam)) {
		case cv_cng:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_NG);
			break;
		case cv_diesel:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_DZ);
			break;
		case cv_gas:
			fuelType = new JSONData("fuelType", ST_FUELTYPE_GAS);
			break;
		default:
			break;		
		}
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData enigneKW = new JSONData("enigneKW", fsofModel.vehModelParam.fuelConv.maxFuelConvKw);
		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg;
		float nonBatteryVehWt = vehWt;
		
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float fuelPerMile = fecoSummary.avFuelOrKWhperMile();
		JSONData baselineFuelPerMile = new JSONData("baselineFuelPerMile", fuelPerMile);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(enigneKW);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineFuelPerMile);
		
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_fcev(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoSummayNonPlugin fecoSummary) throws Exception {
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_FCEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_FCEV);
		JSONData fuelType = new JSONData("fuelType", ST_FUELTYPE_H2);
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData fuelCellKW = new JSONData("fuelCellKW", fsofModel.vehModelParam.fuelConv.maxFuelConvKw);
		FSJSimConstants fsmc = new FSJSimConstants();
		float tankKgHydrogen = fsofModel.vehModelParam.fuelStore.fuelStorKwh / fsmc.h2KWhPerKg;
		JSONData h2TankKg = new JSONData("h2TankKg", tankKgHydrogen);
		JSONData motorKW = new JSONData("motorKW", fsofModel.vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel.vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg 
				- fsofModel.vehModelParam.massProp.batteryKg/fsofModel.vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms.mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float fuelPerMile = fecoSummary.avFuelOrKWhperMile();
		JSONData baselineFuelPerMile = new JSONData("baselineFuelPerMile", fuelPerMile);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(fuelCellKW);
		jsdVehicle.add(h2TankKg);
		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineFuelPerMile);
		
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_fcphev(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoCasesLookup fecoLoo, int sID, int vehID, float avChargingEff,
			int numDayTimeChgWindows, ChgCasesLookup chgLoo, float dcCostPremium) throws Exception {
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_FCPHEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_FCPHEV);
		JSONData fuelType = new JSONData("fuelType", ST_FUELTYPE_H2);
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData fuelCellKW = new JSONData("fuelCellKW", fsofModel.vehModelParam.fuelConv.maxFuelConvKw);
		FSJSimConstants fsmc = new FSJSimConstants();
		float tankKgHydrogen = fsofModel.vehModelParam.fuelStore.fuelStorKwh / fsmc.h2KWhPerKg;
		JSONData h2TankKg = new JSONData("h2TankKg", tankKgHydrogen);
		JSONData motorKW = new JSONData("motorKW", fsofModel.vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel.vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg 
				- fsofModel.vehModelParam.massProp.batteryKg/fsofModel.vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms.mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float fuelPerMile = 0f;
		FEcoSummayPHEV nonChgFecoSummary = fecoLoo.getSummary_PHEVwoChg(sID, vehID);
		if (nonChgFecoSummary != null) fuelPerMile = nonChgFecoSummary.allMilesAvFuelperMile();
		JSONData baselineFuelPerMile = new JSONData("baselineFuelPerMile", fuelPerMile);
		
		JSONData chgEff = new JSONData("chgEff", avChargingEff);
		
		JSONData bhvRes = new JSONData ("bhvRes");		
		JSONData overnightOnly = new JSONData ("overnightOnly");
		
		FEcoSummayPHEV chgPHEVsFecoSummary = fecoLoo.getSummary_PHEVwChg(sID, vehID, 0);		
		overnightOnly.add(new JSONData("fuelPerMile", chgPHEVsFecoSummary.allMilesAvFuelperMile()));
		overnightOnly.add(new JSONData("kWhPerMile", chgPHEVsFecoSummary.allMilesAvKWHperMile()));
		
		JSONData optCostTiming = new JSONData ("optCostTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minCost = chgLoo.getChgSummay_forPHEV(sID, vehID, 0, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		JSONData optGHGTiming = new JSONData ("optGHGTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minGHG = chgLoo.getChgSummay_forPHEV(sID, vehID, 0, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		overnightOnly.add(optCostTiming);
		overnightOnly.add(optGHGTiming);
		bhvRes.add(overnightOnly);
		
		JSONData maxDayTime = new JSONData ("maxDayTime");
		
		chgPHEVsFecoSummary = fecoLoo.getSummary_PHEVwChg(sID, vehID, numDayTimeChgWindows);		
		maxDayTime.add(new JSONData("fuelPerMile", chgPHEVsFecoSummary.allMilesAvFuelperMile()));
		maxDayTime.add(new JSONData("kWhPerMile", chgPHEVsFecoSummary.allMilesAvKWHperMile()));

		optCostTiming = new JSONData ("optCostTiming");
		chgSummary_minCost = chgLoo.getChgSummay_forPHEV(sID, vehID, numDayTimeChgWindows, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		optGHGTiming = new JSONData ("optGHGTiming");
		chgSummary_minGHG = chgLoo.getChgSummay_forPHEV(sID, vehID, numDayTimeChgWindows, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		maxDayTime.add(optCostTiming);
		maxDayTime.add(optGHGTiming);
		bhvRes.add(maxDayTime);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(fuelCellKW);
		jsdVehicle.add(h2TankKg);
		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineFuelPerMile);
		jsdVehicle.add(chgEff);
		
		jsdVehicle.add(bhvRes);
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_bevCommercial(AnalysisVehModelsSetup.AVehModelSetup vms, FSJOneFileVehModel fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoSummayNonPlugin fecoSummary, float avChgEff) throws Exception {
		
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms.shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_BEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_COMMERCIAL_BEV);
		
		float incentives = vms.firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData motorKW = new JSONData("motorKW", fsofModel.vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel.vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel.vehModelParam.massProp.totalKg - fsofModel.vehModelParam.massProp.cargoKg 
				- fsofModel.vehModelParam.massProp.batteryKg/fsofModel.vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms.mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);

		float kWhPerMile = fecoSummary.avFuelOrKWhperMile();
		JSONData baselineKWhPerMile = new JSONData("baselineKWhPerMile", kWhPerMile);
		JSONData chgEff = new JSONData("chgEff", avChgEff);

		//Output
		JSONData jsdVehicle = new JSONData(vms.shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(baselineKWhPerMile);
		jsdVehicle.add(chgEff);
		
		return jsdVehicle;
	}
	private static JSONData vehDataToJSONData_bev(AnalysisVehModelsSetup.AVehModelSetup[] vms, FSJOneFileVehModel[] fsofModel,
			float vehProdCostAllElse, float fiveYearResidual, 
			float gCO2perKgVehicle_lowEnd, float gCO2perKgVehicle_highEnd,
			float licDollarPerYear, float insDollarPerYear, float maintDollarPerMile,
			FEcoCasesLookup fecoLoo, int sID, int vehID, float avChargingEff,
			int numDayTimeChgWindows, int numAnxLevels, ChgCasesLookup chgLoo, float dcCostPremium,
			int repVehID1, int repVehID2) throws Exception {
		
		//Extract values and create sub-objects
		JSONData caption = new JSONData("caption", vms[vehID].shortName);
		JSONData plotColor = new JSONData("plotColor", DEFAULT_PLOT_COLOR_BEV);
		JSONData plotSize = new JSONData("plotSize", DEFAULT_PLOT_SIZE);
		
		JSONData powerTrainType = new JSONData("powerTrainType", ST_PTTYPE_BEV);
		
		JSONData fuelTypeRepVeh1 = null;
		switch (PowertrainType.decode(fsofModel[repVehID1].vehModelParam)) {
		case hev_cng:
		case cv_cng:
			fuelTypeRepVeh1 = new JSONData("fuelTypeRepVeh1", ST_FUELTYPE_NG);
			break;
		case hev_diesel:
		case cv_diesel:
			fuelTypeRepVeh1 = new JSONData("fuelTypeRepVeh1", ST_FUELTYPE_DZ);
			break;
		case hev_gas:
		case cv_gas:
			fuelTypeRepVeh1 = new JSONData("fuelTypeRepVeh1", ST_FUELTYPE_GAS);
			break;
		case hev_fc:
			fuelTypeRepVeh1 = new JSONData("fuelTypeRepVeh1", ST_FUELTYPE_H2);
			break;
		default:
			break;		
		}
		
		JSONData fuelTypeRepVeh2 = null;
		switch (PowertrainType.decode(fsofModel[repVehID2].vehModelParam)) {
		case hev_cng:
		case cv_cng:
			fuelTypeRepVeh2 = new JSONData("fuelTypeRepVeh2", ST_FUELTYPE_NG);
			break;
		case hev_diesel:
		case cv_diesel:
			fuelTypeRepVeh2 = new JSONData("fuelTypeRepVeh2", ST_FUELTYPE_DZ);
			break;
		case hev_gas:
		case cv_gas:
			fuelTypeRepVeh2 = new JSONData("fuelTypeRepVeh2", ST_FUELTYPE_GAS);
			break;
		case hev_fc:
			fuelTypeRepVeh2 = new JSONData("fuelTypeRepVeh2", ST_FUELTYPE_H2);
			break;
		default:
			break;		
		}

		float incentives = vms[vehID].firstYearTotEqIncentives;
		JSONData dollarsIncentive = new JSONData("dollarsIncentive", incentives);
		JSONData dollarsBaseVehicle = new JSONData("dollarsBaseVehicle", vehProdCostAllElse);
		
		JSONData motorKW = new JSONData("motorKW", fsofModel[vehID].vehModelParam.motor.maxMotorKw);
		JSONData batteryKWh = new JSONData("batteryKWh", fsofModel[vehID].vehModelParam.battery.maxEssKwh);

		JSONData relResidualValueAtFiveYears = new JSONData("relResidualValueAtFiveYears", fiveYearResidual);

		float vehWt = fsofModel[vehID].vehModelParam.massProp.totalKg - fsofModel[vehID].vehModelParam.massProp.cargoKg 
				- fsofModel[vehID].vehModelParam.massProp.batteryKg/fsofModel[vehID].vehModelParam.compMassMultiplier;
		float nonBatteryVehWt = vehWt;
		
		JSONData batteryMfg_gCO2perKWh = new JSONData("batteryMfg_gCO2perKWh", vms[vehID].mfgGHG_gCO2perKWhBattery);
		JSONData vehWeightExceptBattery = new JSONData("vehWeightExceptBattery", nonBatteryVehWt);
		JSONData unitMfgGHG_lowEnd = new JSONData("unitMfgGHG_lowEnd", gCO2perKgVehicle_lowEnd);
		JSONData unitMfgGHG_highEnd = new JSONData("unitMfgGHG_highEnd", gCO2perKgVehicle_highEnd);
		
		JSONData licensingDollarsPerYear = new JSONData("licensingDollarsPerYear", licDollarPerYear);
		JSONData insuranceDollarsPerYear = new JSONData("insuranceDollarsPerYear", insDollarPerYear);
		JSONData maintenanceDollarPerMile = new JSONData("maintenanceDollarPerMile", maintDollarPerMile);
		
		JSONData chgEff = new JSONData("chgEff", avChargingEff);

		int locRepID1 = 0;
		int locRepID2 = 1;
		if (repVehID1 == repVehID2) locRepID2 = locRepID1;
		
		JSONData bhvRes = new JSONData ("bhvRes");		
		JSONData overnightOnly = new JSONData ("overnightOnly");
		JSONData minAnx = new JSONData ("minAnx");
		
		FEcoSummayBEVwRep fecoSummary = fecoLoo.getSummary_BEVwRep(sID, vehID, 0, 0);	//Overnight-only charging + minimum range anxiety	
		minAnx.add(new JSONData("fracMilesOnBEV", fecoSummary.fracMilesOnBEV()));
		minAnx.add(new JSONData("fracDaysOnBEV", fecoSummary.fracDaysBEV()));
		minAnx.add(new JSONData("avKWhperMile", fecoSummary.avKWhpm()));

		minAnx.add(new JSONData("repVeh1_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID1]));
		minAnx.add(new JSONData("repVeh2_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID2]));
		
		JSONData optCostTiming = new JSONData ("optCostTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minCost = chgLoo.getChgSummay_forBEVwRep(sID, vehID, 0, 0, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		JSONData optGHGTiming = new JSONData ("optGHGTiming");
		ChgEventsCaseSummary.CEHistogram chgSummary_minGHG = chgLoo.getChgSummay_forBEVwRep(sID, vehID, 0, 0, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		minAnx.add(optCostTiming);
		minAnx.add(optGHGTiming);

		JSONData maxAnx = new JSONData ("maxAnx");
		fecoSummary = fecoLoo.getSummary_BEVwRep(sID, vehID, 0, numAnxLevels-1);	//Overnight-only charging + maximum range anxiety	
		maxAnx.add(new JSONData("fracMilesOnBEV", fecoSummary.fracMilesOnBEV()));
		maxAnx.add(new JSONData("fracDaysOnBEV", fecoSummary.fracDaysBEV()));
		maxAnx.add(new JSONData("avKWhperMile", fecoSummary.avKWhpm()));

		maxAnx.add(new JSONData("repVeh1_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID1]));
		maxAnx.add(new JSONData("repVeh2_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID2]));

		optCostTiming = new JSONData ("optCostTiming");
		chgSummary_minCost = chgLoo.getChgSummay_forBEVwRep(sID, vehID, 0, numAnxLevels-1, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		optGHGTiming = new JSONData ("optGHGTiming");
		chgSummary_minGHG = chgLoo.getChgSummay_forBEVwRep(sID, vehID, 0, numAnxLevels-1, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		maxAnx.add(optCostTiming);
		maxAnx.add(optGHGTiming);
		
		overnightOnly.add(minAnx);
		overnightOnly.add(maxAnx);
		bhvRes.add(overnightOnly);
		
		JSONData maxDayTime = new JSONData ("maxDayTime");
		minAnx = new JSONData ("minAnx");
		
		fecoSummary = fecoLoo.getSummary_BEVwRep(sID, vehID, numDayTimeChgWindows, 0);	//Maximum daytime charging charging + minimum range anxiety	
		minAnx.add(new JSONData("fracMilesOnBEV", fecoSummary.fracMilesOnBEV()));
		minAnx.add(new JSONData("fracDaysOnBEV", fecoSummary.fracDaysBEV()));
		minAnx.add(new JSONData("avKWhperMile", fecoSummary.avKWhpm()));

		minAnx.add(new JSONData("repVeh1_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID1]));
		minAnx.add(new JSONData("repVeh2_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID2]));
		
		optCostTiming = new JSONData ("optCostTiming");
		chgSummary_minCost = chgLoo.getChgSummay_forBEVwRep(sID, vehID, numDayTimeChgWindows, 0, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		optGHGTiming = new JSONData ("optGHGTiming");
		chgSummary_minGHG = chgLoo.getChgSummay_forBEVwRep(sID, vehID, numDayTimeChgWindows, 0, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		minAnx.add(optCostTiming);
		minAnx.add(optGHGTiming);
		
		maxAnx = new JSONData ("maxAnx");
		fecoSummary = fecoLoo.getSummary_BEVwRep(sID, vehID, numDayTimeChgWindows, numAnxLevels-1);	//Maximum daytime charging + maximum range anxiety	
		maxAnx.add(new JSONData("fracMilesOnBEV", fecoSummary.fracMilesOnBEV()));
		maxAnx.add(new JSONData("fracDaysOnBEV", fecoSummary.fracDaysBEV()));
		maxAnx.add(new JSONData("avKWhperMile", fecoSummary.avKWhpm()));

		maxAnx.add(new JSONData("repVeh1_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID1]));
		maxAnx.add(new JSONData("repVeh2_fuelPerMile", fecoSummary.avRepVehFuelPerMile()[locRepID2]));

		optCostTiming = new JSONData ("optCostTiming");
		chgSummary_minCost = chgLoo.getChgSummay_forBEVwRep(sID, vehID, numDayTimeChgWindows, numAnxLevels-1, 0f);
		optCostTiming.add(new JSONData("electCostMod", chgSummary_minCost.avApproxCostMod(dcCostPremium)));
		optCostTiming.add(new JSONData("electGHGMod", chgSummary_minCost.avApproxGHGMod()));
		
		optGHGTiming = new JSONData ("optGHGTiming");
		chgSummary_minGHG = chgLoo.getChgSummay_forBEVwRep(sID, vehID, numDayTimeChgWindows, numAnxLevels-1, 1f);
		optGHGTiming.add(new JSONData("electCostMod", chgSummary_minGHG.avApproxCostMod(dcCostPremium)));
		optGHGTiming.add(new JSONData("electGHGMod", chgSummary_minGHG.avApproxGHGMod()));
		
		maxAnx.add(optCostTiming);
		maxAnx.add(optGHGTiming);
		
		maxDayTime.add(minAnx);
		maxDayTime.add(maxAnx);
		bhvRes.add(maxDayTime);

		//Output
		JSONData jsdVehicle = new JSONData(vms[vehID].shortName);
		
		jsdVehicle.add(caption);
		jsdVehicle.add(plotColor);
		jsdVehicle.add(plotSize);
		
		jsdVehicle.add(powerTrainType);
		jsdVehicle.add(fuelTypeRepVeh1);
		jsdVehicle.add(fuelTypeRepVeh2);

		jsdVehicle.add(dollarsIncentive);
		jsdVehicle.add(dollarsBaseVehicle);

		jsdVehicle.add(motorKW);
		jsdVehicle.add(batteryKWh);
		jsdVehicle.add(relResidualValueAtFiveYears);

		jsdVehicle.add(batteryMfg_gCO2perKWh);
		jsdVehicle.add(vehWeightExceptBattery);
		jsdVehicle.add(unitMfgGHG_lowEnd);
		jsdVehicle.add(unitMfgGHG_highEnd);

		jsdVehicle.add(licensingDollarsPerYear);
		jsdVehicle.add(insuranceDollarsPerYear);
		jsdVehicle.add(maintenanceDollarPerMile);

		jsdVehicle.add(chgEff);
		
		jsdVehicle.add(bhvRes);
		return jsdVehicle;
	}
}
