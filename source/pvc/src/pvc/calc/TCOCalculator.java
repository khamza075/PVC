package pvc.calc;

import java.util.ArrayList;

import fastsimjava.*;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.APSliderBars;
import pvc.datamgmt.comp.DUnits;
import pvc.datamgmt.comp.FDefaults;
import pvc.datamgmt.comp.PowertrainType;

public class TCOCalculator {

	private FEcoSimsGenerator fsG;
	private AnalysisVehModelsSetup avms;
	private WIITModel wiitMod;
	private FSJOneFileVehModel[] fsofModels;
	private VehDeprModels deprMod;
	private LicIMModel licimMod;
	private HomeChargerCosts homeChgCosts;
	
	private float[] allElseInVehCost;


	//Note... this class needs to be re-instantiated if either VehDeprModels or LicIMModel were modified
	public TCOCalculator(FEcoSimsGenerator fsGen, VehDeprModels deprModels, LicIMModel licimModel, HomeChargerCosts homeChargerCosts) {
		fsG = fsGen;
		avms = fsG.avms();
		wiitMod = fsG.wiitMod();
		fsofModels = fsG.fsofModels();
		
		deprMod = deprModels;
		licimMod = licimModel;
		homeChgCosts = homeChargerCosts;
		
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		allElseInVehCost = new float[vms.length];
		int[] idOfVehDerivedFrom = new int[vms.length];
		
		for (int i=0; i<vms.length; i++) {
			idOfVehDerivedFrom[i] = -1;
			if (vms[i].isDerivedVehicle) {
				String shortNameOfVehDerivedFrom = vms[i].shortNameOfVehicleDerivedFrom;
				
				for (int j=0; j<vms.length; j++) {
					if (vms[j].shortName.equalsIgnoreCase(shortNameOfVehDerivedFrom)) {
						idOfVehDerivedFrom[i] = j;
						break;
					}
				}	
			} else {
				switch (PowertrainType.decode(fsofModels[i].vehModelParam)) {
				case bev:
				{
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_BEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpeElect*(batteryCost + motorCost);
				}
					break;
				case cv_cng:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmCNGICE.calcCost(engineKW, wiitMod.bmCNGICE.dollarPerKWmodel().baseValue);

					allElseInVehCost[i] = vehPrice - rpe*engineCost;
				}
					break;
				case cv_diesel:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmDieselICE.calcCost(engineKW, wiitMod.bmDieselICE.dollarPerKWmodel().baseValue);

					allElseInVehCost[i] = vehPrice - rpe*engineCost;
				}
					break;
				case cv_gas:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmGasICE.calcCost(engineKW, wiitMod.bmGasICE.dollarPerKWmodel().baseValue);

					allElseInVehCost[i] = vehPrice - rpe*engineCost;
				}
					break;
				case phev_cng:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmCNGICE.calcCost(engineKW, wiitMod.bmCNGICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
				case hev_cng:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmCNGICE.calcCost(engineKW, wiitMod.bmCNGICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_HEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
					break;
				case phev_diesel:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmDieselICE.calcCost(engineKW, wiitMod.bmDieselICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
				case hev_diesel:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmDieselICE.calcCost(engineKW, wiitMod.bmDieselICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_HEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
					break;
				case phev_gas:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmGasICE.calcCost(engineKW, wiitMod.bmGasICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
				case hev_gas:
				{
					float rpe = wiitMod.bmRPE.baseValue;
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float engineCost = wiitMod.bmGasICE.calcCost(engineKW, wiitMod.bmGasICE.dollarPerKWmodel().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_HEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpe*engineCost - rpeElect*(batteryCost + motorCost);
				}
					break;
				case phev_fc:
				{
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float fuelCellCost = wiitMod.bmFuelCell.calcCost(fuelCellKW, wiitMod.bmFuelCell.dollarPerKWmodel().baseValue);
					
					float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
					float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
					float tankCost = wiitMod.bmH2Tank.calcCost(tankKgH2, wiitMod.bmH2Tank.dollarPerKgH2model().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpeElect*(batteryCost + motorCost + fuelCellCost + tankCost);
				}
				case hev_fc:
				{
					float rpeElect = wiitMod.bmRPEElectrification.baseValue;
					float vehPrice = vms[i].baseScenarioUSDollarsToBuy;
					
					float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
					float fuelCellCost = wiitMod.bmFuelCell.calcCost(fuelCellKW, wiitMod.bmFuelCell.dollarPerKWmodel().baseValue);
					
					float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
					float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
					float tankCost = wiitMod.bmH2Tank.calcCost(tankKgH2, wiitMod.bmH2Tank.dollarPerKgH2model().baseValue);
					
					float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
					float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
					
					float motorCost = wiitMod.bmMotor.calcCost(motorKW, wiitMod.bmMotor.dollarPerKWmodel().baseValue);
					float batteryCost = wiitMod.bmBatteries.calcCost_HEV(batteryKWh, wiitMod.bmBatteries.nominalDollarPerKWh().baseValue);
					
					allElseInVehCost[i] = vehPrice - rpeElect*(batteryCost + motorCost + fuelCellCost + tankCost);
				}
					break;
				}
			}
		}
		
		for (int i=0; i<idOfVehDerivedFrom.length; i++) {
			if (idOfVehDerivedFrom[i] >= 0) allElseInVehCost[i] = allElseInVehCost[idOfVehDerivedFrom[i]];
		}
	}	
	
	public class CParetoPoint {
		public float gCO2perMile, dollarPerMile;
		private CParetoPoint() {}
		@Override public String toString() {return ""+gCO2perMile+","+dollarPerMile;}
	}
	
	public CParetoPoint[] getCostVsGHG(UsePhaseSSimulator.OutputStructure upResults, SliderBarsManager sbarMan) {
		CParetoPoint[] res = new CParetoPoint[avms.numVehModels()];
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();

		
		for (int i=0; i<res.length; i++) {
			//Initialize for output
			res[i] = new CParetoPoint();
			
			//Calculate Price Elements
			float vehAllElse = allElseInVehCost[i];
			
			float engineCost = 0f;
			float fuelCellCost = 0f;
			float motorCost = 0f;
			float batteryCost = 0f;
			float h2TankCost = 0f;
			
			switch (PowertrainType.decode(fsofModels[i].vehModelParam)) {
			case bev:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;

				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_BEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case cv_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case cv_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case cv_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case phev_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_fc:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float fuelCellDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_FuelCell);
				float h2TankDollarPerKg = sbarMan.getCurValue(APSliderBars.Cost_H2Tank);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
				float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				fuelCellCost = rpeElect * wiitMod.bmFuelCell.calcCost(fuelCellKW, fuelCellDollarPerKW);
				h2TankCost = rpeElect * wiitMod.bmH2Tank.calcCost(tankKgH2, h2TankDollarPerKg);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_fc:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float fuelCellDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_FuelCell);
				float h2TankDollarPerKg = sbarMan.getCurValue(APSliderBars.Cost_H2Tank);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
				float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				fuelCellCost = rpeElect * wiitMod.bmFuelCell.calcCost(fuelCellKW, fuelCellDollarPerKW);
				h2TankCost = rpeElect * wiitMod.bmH2Tank.calcCost(tankKgH2, h2TankDollarPerKg);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			default:
			}
			
			//Vehicle Price
			float vehPrice = vehAllElse + engineCost + fuelCellCost + motorCost + batteryCost + h2TankCost;
			
			//Incentives
			float incentives = 0f;
			if (avms.includesIncentives()) {
				float incentivesLevel = sbarMan.getCurValue(APSliderBars.Cost_Incentives);
				incentives = incentivesLevel * vms[i].firstYearTotEqIncentives;
			}
			
			//Number of years and total miles
			float numYears = sbarMan.getCurValue(APSliderBars.Cost_NumYears);
			float annualMiles = sbarMan.getCurValue(APSliderBars.Cost_AnnualDriveDistance) * SliderBarsManager.annualMilesTravelledIncCurSolSet;
			float milesTotal = numYears * annualMiles;
			
			//Re-Sale
			float residualValue = deprMod.residualValue(i, numYears, milesTotal);
			float resalePrice = (vehPrice - incentives)*residualValue;
			float presentValueOfResale = resalePrice / (float)Math.pow(1f+FDefaults.annualMVInflationRate, numYears);
			
			//Home Charger
			float homeChargerCost = 0f;
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
			{
				float fracofHomeChargerCostToInclude = sbarMan.getCurValue(APSliderBars.Cost_ApprHomeCharger);
				if (wiitMod.chgModels.bevHomesHaveL2) homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL2;
				else homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL1;
			}
				break;
			case phev:
			{
				float fracofHomeChargerCostToInclude = sbarMan.getCurValue(APSliderBars.Cost_ApprHomeCharger);
				if (wiitMod.chgModels.phevHomesHaveL2) homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL2;
				else homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL1;
			}
				break;
			default:
				break;			
			}
			
			//Total Acquisition Cost
			float totAcquisitionCost = vehPrice + homeChargerCost - incentives - presentValueOfResale;
			
			//Additional vehicles in case of Commercial BEVs
			float additionalVehiclesCost = 0f;
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
				if (wiitMod.bevRepModel.bevRepCommercial != null) {
					float fracAdditionalVehiclesBaseLine = sbarMan.getCurValue(APSliderBars.Bhv_BEVRep_Commercial);
					int nomAER = (int)(vms[i].nominalAERMiles + 0.5f);
					float fracAdditionalVehicles = sbarMan.adjustedFracAdditionalCommercialBEVs(fracAdditionalVehiclesBaseLine, nomAER);
					additionalVehiclesCost = fracAdditionalVehicles * totAcquisitionCost;
				}
				break;
			default:
				break;
			}
			
			//Licensing, Insurance & Maintenance
			float annualLicensingCost = licimMod.getAnnualLicensingCost(i);
			float annualInsuranceCost = licimMod.getAnnualInsuranceCost(i);
			float maintenanceCostPerMile = licimMod.getMaintnenaceCostPerMile(i);
			
			float totLicensingCost = annualLicensingCost * numYears;
			float totInsuranceCost = annualInsuranceCost * numYears;
			float totMaintenaceCost = maintenanceCostPerMile * milesTotal;
			
			//Fuel, Electricity & BEV-Replacement Vehicles if any
			float simBasedRunningCost = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, i) * milesTotal;
			
			//Total cost of ownership
			float tco = totAcquisitionCost + additionalVehiclesCost + totLicensingCost + totInsuranceCost + totMaintenaceCost + simBasedRunningCost;
			
			//Set outputs
			res[i].gCO2perMile = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.netGHG_gCO2perMile, i);
			res[i].dollarPerMile = tco/milesTotal;
		}
		
		return res;
	}
	public TCOvsGHGSummaryOutputStructure getCostVsGHGSummary(UsePhaseSSimulator.OutputStructure upResults, SliderBarsManager sbarMan, float lifetimeMilesLCA) {
		TCOvsGHGSummaryOutputStructure res = new TCOvsGHGSummaryOutputStructure();
		AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
		
		for (int i=0; i<vms.length; i++) {
			//Calculate Price Elements
			float vehAllElse = allElseInVehCost[i];
			
			float engineCost = 0f;
			float fuelCellCost = 0f;
			float motorCost = 0f;
			float batteryCost = 0f;
			float h2TankCost = 0f;
			
			switch (PowertrainType.decode(fsofModels[i].vehModelParam)) {
			case bev:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;

				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_BEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case cv_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case cv_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case cv_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
			}
				break;
			case phev_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_cng:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_NGICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmCNGICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_diesel:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_DieselICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmDieselICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_gas:
			{
				float rpe = sbarMan.getCurValue(APSliderBars.Cost_RPE);
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float engineDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_GasICE);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float engineKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				engineCost = rpe * wiitMod.bmGasICE.calcCost(engineKW, engineDollarPerKW);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case phev_fc:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float fuelCellDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_FuelCell);
				float h2TankDollarPerKg = sbarMan.getCurValue(APSliderBars.Cost_H2Tank);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
				float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				fuelCellCost = rpeElect * wiitMod.bmFuelCell.calcCost(fuelCellKW, fuelCellDollarPerKW);
				h2TankCost = rpeElect * wiitMod.bmH2Tank.calcCost(tankKgH2, h2TankDollarPerKg);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_PHEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			case hev_fc:
			{
				float rpeElect = sbarMan.getCurValue(APSliderBars.Cost_RPE_ElectSystems);
				float fuelCellDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_FuelCell);
				float h2TankDollarPerKg = sbarMan.getCurValue(APSliderBars.Cost_H2Tank);
				float batteryDollarPerKWh = sbarMan.getCurValue(APSliderBars.Cost_Battery);
				float motorDollarPerKW = sbarMan.getCurValue(APSliderBars.Cost_Motor);
				
				float fuelCellKW = fsofModels[i].vehModelParam.fuelConv.maxFuelConvKw;
				float tankKWh = fsofModels[i].vehModelParam.fuelStore.fuelStorKwh;
				float tankKgH2 = tankKWh/FDefaults.H2KWhPerKg;
				float motorKW = fsofModels[i].vehModelParam.motor.maxMotorKw;
				float batteryKWh = fsofModels[i].vehModelParam.battery.maxEssKwh;
				
				fuelCellCost = rpeElect * wiitMod.bmFuelCell.calcCost(fuelCellKW, fuelCellDollarPerKW);
				h2TankCost = rpeElect * wiitMod.bmH2Tank.calcCost(tankKgH2, h2TankDollarPerKg);
				motorCost = rpeElect * wiitMod.bmMotor.calcCost(motorKW, motorDollarPerKW);
				batteryCost = rpeElect * wiitMod.bmBatteries.calcCost_HEV(batteryKWh, batteryDollarPerKWh);
			}
				break;
			default:
			}
			
			//Vehicle Price
			float vehPrice = vehAllElse + engineCost + fuelCellCost + motorCost + batteryCost + h2TankCost;
			
			//Incentives
			float incentives = 0f;
			if (avms.includesIncentives()) {
				float incentivesLevel = sbarMan.getCurValue(APSliderBars.Cost_Incentives);
				incentives = incentivesLevel * vms[i].firstYearTotEqIncentives;
			}
			
			//Number of years and total miles
			float numYears = sbarMan.getCurValue(APSliderBars.Cost_NumYears);
			float annualMiles = sbarMan.getCurValue(APSliderBars.Cost_AnnualDriveDistance) * SliderBarsManager.annualMilesTravelledIncCurSolSet;
			float milesTotal = numYears * annualMiles;
			
			//Re-Sale
			float residualValue = deprMod.residualValue(i, numYears, milesTotal);
			float resalePrice = (vehPrice - incentives)*residualValue;
			float presentValueOfResale = resalePrice / (float)Math.pow(1f+FDefaults.annualMVInflationRate, numYears);
			
			//Home Charger
			float homeChargerCost = 0f;
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
			{
				float fracofHomeChargerCostToInclude = sbarMan.getCurValue(APSliderBars.Cost_ApprHomeCharger);
				if (wiitMod.chgModels.bevHomesHaveL2) homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL2;
				else homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL1;
			}
				break;
			case phev:
			{
				float fracofHomeChargerCostToInclude = sbarMan.getCurValue(APSliderBars.Cost_ApprHomeCharger);
				if (wiitMod.chgModels.phevHomesHaveL2) homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL2;
				else homeChargerCost = fracofHomeChargerCostToInclude * homeChgCosts.homeChargerCostL1;
			}
				break;
			default:
				break;			
			}
			
			//Total Acquisition Cost
			float totAcquisitionCost = vehPrice + homeChargerCost - incentives - presentValueOfResale;
			
			//Additional vehicles in case of Commercial BEVs
			float additionalVehiclesCost = 0f;
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
				if (wiitMod.bevRepModel.bevRepCommercial != null) {
					float fracAdditionalVehiclesBaseLine = sbarMan.getCurValue(APSliderBars.Bhv_BEVRep_Commercial);
					int nomAER = (int)(vms[i].nominalAERMiles + 0.5f);
					float fracAdditionalVehicles = sbarMan.adjustedFracAdditionalCommercialBEVs(fracAdditionalVehiclesBaseLine, nomAER);
					additionalVehiclesCost = fracAdditionalVehicles * totAcquisitionCost;
				}
				break;
			default:
				break;
			}
			
			//Licensing, Insurance & Maintenance
			float annualLicensingCost = licimMod.getAnnualLicensingCost(i);
			float annualInsuranceCost = licimMod.getAnnualInsuranceCost(i);
			float maintenanceCostPerMile = licimMod.getMaintnenaceCostPerMile(i);
			float annualMaintenanceCost = maintenanceCostPerMile * annualMiles;
			
			//Simulation-based Use-Phase Values
			float annualGHG_fuel = 0f;
			float annualGHG_elect = 0f;
			
			float annualCost_fuel = 0f;
			float annualCost_elect = 0f;
			float annualCost_bevRepVeh = 0f;

			float bevWRepVeh_fracMilesOnBEV = 0f;
			float bevWRepVeh_fracDaysOnBEV = 0f;
			float bevWRepVeh_fracDaysAltered = 0f;

			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
			{
				if (wiitMod.bevRepModel.bevRepCommercial != null) {
					annualGHG_elect = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, i) * annualMiles;
					annualCost_elect = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, i) 
							* annualMiles;
				} else {
					float fracMilesReplaced = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.bevFracMilesReplaced, i);
					float fracDaysReplaced = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.bevFracDaysReplaced, i);
					float ghgPerMileElectric = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, i);
					float ghgPerMileRepVeh = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, i);
					
					bevWRepVeh_fracDaysAltered = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.bevFracDaysFailed, i);
					bevWRepVeh_fracDaysOnBEV = 1f - (fracDaysReplaced + bevWRepVeh_fracDaysAltered);
					bevWRepVeh_fracMilesOnBEV = 1f - fracMilesReplaced;
					
					annualGHG_fuel = ghgPerMileRepVeh * annualMiles * fracMilesReplaced;
					annualGHG_elect = ghgPerMileElectric * annualMiles * bevWRepVeh_fracMilesOnBEV;
					
					annualCost_fuel = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelCost_dollarPerMile, i) * annualMiles * fracMilesReplaced;
					annualCost_elect = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, i) 
							* annualMiles * bevWRepVeh_fracMilesOnBEV;
					annualCost_bevRepVeh = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.bevRepCost_netDollarPerMile, i) 
							* annualMiles * fracMilesReplaced;
				}
			}
				break;
			case cv:
			case hev:
				annualGHG_fuel = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, i) * annualMiles;
				annualCost_fuel = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelCost_dollarPerMile, i) * annualMiles;
				break;
			case phev:
				annualGHG_fuel = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelGHG_gCO2PerMile, i) * annualMiles;
				annualCost_fuel = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.fuelCost_dollarPerMile, i) * annualMiles;
				annualGHG_elect = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryGHG_gCO2PerMile, i) * annualMiles;
				annualCost_elect = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.electToBatteryCost_dollarPerMile, i) * annualMiles;
				break;
			}
			
			float lifetimeMfgGHG_nonBattery = 0f;
			float lifetimeMfgGHG_battery = 0f;
			if (avms.mfgGHGModelIncluded()) {
				lifetimeMfgGHG_nonBattery = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.mfgGHG_nonBat_gCO2perMileUsage, i) * lifetimeMilesLCA;
				lifetimeMfgGHG_battery = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.mfgGHG_battery_gCO2perMileUsage, i) * lifetimeMilesLCA;
			}

			//float simBasedRunningCost = upResults.get(UsePhaseSSimulator.APAvOutputsPerVehModel.costOf_fuel_elect_bevRep_dollarPerMile, i) * milesTotal;
			
			//Total annual running cost
			float totAnnualRunningCost = annualLicensingCost + annualInsuranceCost + annualMaintenanceCost + 
					annualCost_fuel + annualCost_elect + annualCost_bevRepVeh;
						
			//Total cost of ownership
			float tco = totAcquisitionCost + additionalVehiclesCost + totAnnualRunningCost * numYears;
			
			//Per-Mile numbers
			float tcoPerMile = tco / milesTotal;
			float ghgPerMile = ((annualGHG_fuel + annualGHG_elect)*numYears)/milesTotal + (lifetimeMfgGHG_nonBattery + lifetimeMfgGHG_battery)/lifetimeMilesLCA;
			
			
			//Put values in output structure
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_ghg_fuel, annualGHG_fuel);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_ghg_elect, annualGHG_elect);
			
			res.setValue(i, APTCOvsGHGSummaryOutputs.mfgGHG_nonBattery, lifetimeMfgGHG_nonBattery);
			res.setValue(i, APTCOvsGHGSummaryOutputs.mfgGHG_battery, lifetimeMfgGHG_battery);
			
			res.setValue(i, APTCOvsGHGSummaryOutputs.netGHG_perDistance, ghgPerMile);
			
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_fuel, annualCost_fuel);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_elect, annualCost_elect);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_lic, annualLicensingCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_ins, annualInsuranceCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_mnt, annualMaintenanceCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.bevRep_fracMilesOnBEV, bevWRepVeh_fracMilesOnBEV);
			res.setValue(i, APTCOvsGHGSummaryOutputs.bevRep_fracDaysOnBEV, bevWRepVeh_fracDaysOnBEV);
			res.setValue(i, APTCOvsGHGSummaryOutputs.bevRep_fracDaysAltered, bevWRepVeh_fracDaysAltered);
			res.setValue(i, APTCOvsGHGSummaryOutputs.annual_cost_bevRep, annualCost_bevRepVeh);
			res.setValue(i, APTCOvsGHGSummaryOutputs.totAnnual_runningCost, totAnnualRunningCost);

			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_allElseInVeh, 0.001f * vehAllElse);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_ICE, 0.001f * engineCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_fuelCell, 0.001f * fuelCellCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_h2Tank, 0.001f * h2TankCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_motor, 0.001f * motorCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_battery, 0.001f * batteryCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.totPucahseCost, 0.001f * vehPrice);
			
			res.setValue(i, APTCOvsGHGSummaryOutputs.credit_Incentives, 0.001f * incentives);
			res.setValue(i, APTCOvsGHGSummaryOutputs.credit_pvResale, 0.001f * presentValueOfResale);
			res.setValue(i, APTCOvsGHGSummaryOutputs.cost_homeCharger, 0.001f * homeChargerCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.netAcquistionCost, 0.001f * totAcquisitionCost);
			res.setValue(i, APTCOvsGHGSummaryOutputs.addtionalVehiclesInFleetCost, 0.001f * additionalVehiclesCost);

			res.setValue(i, APTCOvsGHGSummaryOutputs.tco, 0.001f * tco);
			res.setValue(i, APTCOvsGHGSummaryOutputs.netTCO_perDistance, tcoPerMile);
		}

		return res;
	}	
	
	public enum APTCOvsGHGSummaryOutputs {
		annual_ghg_fuel("Annual Fuel GHG", 0,0,0),
		annual_ghg_elect("Annual Electricity GHG", 0,0,0),
		
		mfgGHG_nonBattery("Lifetime Vehicle Manufacturing GHG Except Battery", 0,0,0),
		mfgGHG_battery("Lifetime Vehicle Manufacturing GHG from Battery", 0,0,0),
		
		netGHG_perDistance("Net GHG per Distance", 0,0,0),		
		
		annual_cost_fuel("Annual Fuel Cost", 0,0,0),
		annual_cost_elect("Annual Electricity Cost", 255,255,0),
		
		annual_cost_lic("Annual Licensing Cost", 255,178,232),
		annual_cost_ins("Annual Insurance Cost", 193,117,255),
		annual_cost_mnt("Annual Maintenance Cost", 255,35,127),
		
		bevRep_fracMilesOnBEV("Fraction of Miles on the BEV", 0,0,0),
		bevRep_fracDaysOnBEV("Fraction of Days on the BEV", 0,0,0),
		bevRep_fracDaysAltered("Fraction of Days Plans Altered", 0,0,0),		
		annual_cost_bevRep("Annual BEV Replacement Vehicle", 255,0,0),

		totAnnual_runningCost("Total Annual Running Cost", 120,175,205),
		
		
		cost_allElseInVeh("Equivalent Retail Cost for Everything in Vehicle Except Modelled Poweretrain Systems", 160,160,160),
		cost_ICE("Equivalent Retail Cost for Engine System", 130,56,0),
		cost_fuelCell("Equivalent Retail Cost for Fuel Cell System", 60,60,125),
		cost_h2Tank("Equivalent Retail Cost for Hydrogen Tank System", 35,130,0),
		cost_motor("Equivalent Retail Cost for Motor System", 255,205,0),
		cost_battery("Equivalent Retail Cost for Battery System", 0,176,80),
		
		totPucahseCost("Total Purchase Cost", 78,16,128),

		credit_Incentives("First-Year Equivalent Incentives", 255,255,213),
		credit_pvResale("First-Year Equivalent Re-Sale Value", 186,224,184),
		
		cost_homeCharger("Appropreated Cost of Home Charger", 255,150,120),

		netAcquistionCost("Net Aquisition Cost", 255,0,155),
		addtionalVehiclesInFleetCost("Additional Vehicles in Fleet Cost", 17,209,114),
		
		tco("Total Cost of Ownership", 17,17,114),
		netTCO_perDistance("Net Total Cost of Ownership per Distance", 0,0,0),
		;
		private String captionWOUnits;
		public int dRed, dGreen, dBlue;
		private APTCOvsGHGSummaryOutputs(String s, int r, int g, int b) {
			captionWOUnits = s;
			dRed = r;
			dGreen = g;
			dBlue = b;
		}
		
		public static String getCaptionWUnits(APTCOvsGHGSummaryOutputs outputType) {
			switch (outputType) {
			case annual_cost_bevRep:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_cost_elect:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_cost_fuel:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_cost_ins:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_cost_lic:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_cost_mnt:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case annual_ghg_elect:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +")";
			case annual_ghg_fuel:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +")";
			case cost_ICE:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_allElseInVeh:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_battery:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_fuelCell:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_h2Tank:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_homeCharger:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case cost_motor:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case credit_Incentives:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case credit_pvResale:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case mfgGHG_battery:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +")";
			case mfgGHG_nonBattery:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) +")";
			case netAcquistionCost:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case netGHG_perDistance:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.GHGUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance) +")";
			case netTCO_perDistance:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) + "/" + DUnits.getShortName(DUnits.UnitType.Distance) +")";
			case addtionalVehiclesInFleetCost:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case tco:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			case totAnnual_runningCost:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.MoneyUnit) +")";
			case totPucahseCost:
				return outputType.captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) +")";
			default:
				return outputType.captionWOUnits;
			}
		}
		public static float convertToOutputUnits(APTCOvsGHGSummaryOutputs outputType, float value) {
			switch (outputType) {
			case annual_cost_bevRep:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_cost_elect:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_cost_fuel:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_cost_ins:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_cost_lic:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_cost_mnt:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case annual_ghg_elect:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			case annual_ghg_fuel:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			case cost_ICE:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_allElseInVeh:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_battery:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_fuelCell:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_h2Tank:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_homeCharger:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case cost_motor:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case credit_Incentives:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case credit_pvResale:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case mfgGHG_battery:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			case mfgGHG_nonBattery:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			case netAcquistionCost:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case netGHG_perDistance:
				return value*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
			case netTCO_perDistance:
				return value*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case addtionalVehiclesInFleetCost:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case tco:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			case totAnnual_runningCost:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			case totPucahseCost:
				return value/DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			default:
				return value;
			}
		}
	}
	
	public class TCOvsGHGSummaryOutputStructure {
		private int[] oIDs;
		private APTCOvsGHGSummaryOutputs[] vOutputs;
		private float[][] vStore;	//First Index on Vehicle ID, second index on type of output (ID by order in current set of outputs)
		
		public float getValue(int vehID, APTCOvsGHGSummaryOutputs outputType) {
			int oID = oIDs[outputType.ordinal()];
			if (oID < 0) return 0f;
			if (oID >= vOutputs.length) return 0f;
			return vStore[vehID][oID];
		}
		public void setValue(int vehID, APTCOvsGHGSummaryOutputs outputType, float value) {
			int oID = oIDs[outputType.ordinal()];
			if (oID < 0) return;
			if (oID >= vOutputs.length) return;
			vStore[vehID][oID] = value;
		}
		
		private TCOvsGHGSummaryOutputStructure() {
			//Identify outputs included
			boolean hasBEVwRep = false;
			if (wiitMod.hasBEVs()) {
				if (wiitMod.bevRepModel.bevRepCommercial == null) hasBEVwRep = true;
			}			
			boolean hasBEVwRepOrPHEV = hasBEVwRep || wiitMod.hasPHEVs();
			boolean hasPlugIns = wiitMod.hasPlugIns();
			boolean hasElectrified = hasPlugIns || wiitMod.hasHEVs();
			
			boolean hasHydrogen = wiitMod.hasHydrogen();
			boolean hasGasoline = wiitMod.hasGasoline();
			boolean hasDiesel = wiitMod.hasDiesel();
			boolean hasCNG = wiitMod.hasCNG();
			boolean hasFuel = hasHydrogen || hasGasoline || hasDiesel || hasCNG || hasBEVwRep;
			
			ArrayList<APTCOvsGHGSummaryOutputs> lst = new ArrayList<APTCOvsGHGSummaryOutputs>();
			
			if (hasFuel) lst.add(APTCOvsGHGSummaryOutputs.annual_ghg_fuel);
			if (hasPlugIns) lst.add(APTCOvsGHGSummaryOutputs.annual_ghg_elect);
			
			if (avms.mfgGHGModelIncluded()) {
				lst.add(APTCOvsGHGSummaryOutputs.mfgGHG_nonBattery);
				lst.add(APTCOvsGHGSummaryOutputs.mfgGHG_battery);
			}
			
			lst.add(APTCOvsGHGSummaryOutputs.netGHG_perDistance);

			if (hasFuel) lst.add(APTCOvsGHGSummaryOutputs.annual_cost_fuel);
			if (hasPlugIns) lst.add(APTCOvsGHGSummaryOutputs.annual_cost_elect);

			lst.add(APTCOvsGHGSummaryOutputs.annual_cost_lic);
			lst.add(APTCOvsGHGSummaryOutputs.annual_cost_ins);
			lst.add(APTCOvsGHGSummaryOutputs.annual_cost_mnt);
			
			if (hasBEVwRep) {
				lst.add(APTCOvsGHGSummaryOutputs.bevRep_fracMilesOnBEV);
				lst.add(APTCOvsGHGSummaryOutputs.bevRep_fracDaysOnBEV);
				lst.add(APTCOvsGHGSummaryOutputs.bevRep_fracDaysAltered);
				lst.add(APTCOvsGHGSummaryOutputs.annual_cost_bevRep);
			}

			lst.add(APTCOvsGHGSummaryOutputs.totAnnual_runningCost);

			lst.add(APTCOvsGHGSummaryOutputs.cost_allElseInVeh);
			if (hasGasoline||hasDiesel||hasCNG) lst.add(APTCOvsGHGSummaryOutputs.cost_ICE);
			if (hasHydrogen) {
				lst.add(APTCOvsGHGSummaryOutputs.cost_fuelCell);
				lst.add(APTCOvsGHGSummaryOutputs.cost_h2Tank);
			}
			if (hasElectrified) {
				lst.add(APTCOvsGHGSummaryOutputs.cost_motor);
				lst.add(APTCOvsGHGSummaryOutputs.cost_battery);
			}
			
			lst.add(APTCOvsGHGSummaryOutputs.totPucahseCost);
			lst.add(APTCOvsGHGSummaryOutputs.credit_Incentives);
			lst.add(APTCOvsGHGSummaryOutputs.credit_pvResale);

			if (hasBEVwRepOrPHEV) lst.add(APTCOvsGHGSummaryOutputs.cost_homeCharger);
			
			lst.add(APTCOvsGHGSummaryOutputs.netAcquistionCost);
			if (wiitMod.hasBEVs()) {
				if (wiitMod.bevRepModel.bevRepCommercial != null) lst.add(APTCOvsGHGSummaryOutputs.addtionalVehiclesInFleetCost);
			}
			lst.add(APTCOvsGHGSummaryOutputs.tco);
			lst.add(APTCOvsGHGSummaryOutputs.netTCO_perDistance);
			
			//Form lookup arrays
			vOutputs = new APTCOvsGHGSummaryOutputs[lst.size()];
			for (int i=0; i<vOutputs.length; i++) vOutputs[i] = lst.get(i);
			
			oIDs = new int[APTCOvsGHGSummaryOutputs.values().length];
			for (int i=0; i<oIDs.length; i++) oIDs[i] = -1;
			for (int i=0; i<vOutputs.length; i++) oIDs[vOutputs[i].ordinal()] = i;
			
			//Allocate space for output values
			vStore = new float[avms.numVehModels()][vOutputs.length];
		}
		
		@Override public String toString() {
			AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();			
			String lsep = System.getProperty("line.separator");
			
			String st = "vehicleModel";
			for (int j=0; j<vOutputs.length; j++) st = st + "," + APTCOvsGHGSummaryOutputs.getCaptionWUnits(vOutputs[j]);
			st = st + lsep;
			
			for (int i=0; i<vms.length; i++) {
				st = st + vms[i].shortName;
				for (int j=0; j<vOutputs.length; j++) st = st + "," + APTCOvsGHGSummaryOutputs.convertToOutputUnits(vOutputs[j], getValue(i, vOutputs[j]));
				st = st + lsep;
			}
			
			return st;
		}
	}
}
