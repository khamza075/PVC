package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileReader;

public class FDefaults {	
	public static float annualVMT = 13200f;
	public static float annualDriveDays = 220f;
	public static float tooShortTripMilesCutOff = 0.03f;
	public static int preferredNumThreads = 4;
	public static int maxNumThreads = 16;
	public static float hrsChgAfterlastTripOfLastDay = 12f;
	public static float vehicleDefaultLifetimeMiles = 150000f;
	public static float bevRepCostPerMile = 0.6f;
	public static float bevRepCostPerDay = 0f;
	public static float chargerDefaultEfficiency = 0.86f;
	public static float homeChargerL1dollars = 300f;
	public static float homeChargerL2dollars = 1800f;
	public static float annualMVInflationRate = 0.02f;
	public static float JapanYenPerDollar = 100f;

	public static final float H2KWhPerKg = 32.72f;


	private FDefaults() {}	//Prevent instantiation
	
	public static void readDefaultValues(String fname) {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(fname));
			String readLine;
			
			while ((readLine = fin.readLine())!=null) {
				String[] sp = readLine.split(",");
			
				if (sp[0].equalsIgnoreCase("annualVMT")) annualVMT = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("annualDriveDays")) annualDriveDays = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("tooShortTripMilesCutOff")) tooShortTripMilesCutOff = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("preferredNumThreads")) preferredNumThreads = Integer.parseInt(sp[1]);
				if (sp[0].equalsIgnoreCase("maxNumThreads")) maxNumThreads = Integer.parseInt(sp[1]);
				if (sp[0].equalsIgnoreCase("hrsChgAfterlastTripOfLastDay")) hrsChgAfterlastTripOfLastDay = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("vehicleDefaultLifetimeMiles")) vehicleDefaultLifetimeMiles = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("bevRepCostPerMile")) bevRepCostPerMile = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("bevRepCostPerDay")) bevRepCostPerDay = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("homeChargerL1dollars")) homeChargerL1dollars = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("homeChargerL2dollars")) homeChargerL2dollars = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("annualMVInflationRate")) annualMVInflationRate = Float.parseFloat(sp[1]);
				if (sp[0].equalsIgnoreCase("JapanYenPerDollar")) JapanYenPerDollar = Float.parseFloat(sp[1]);
			}
			
			fin.close();
		} catch (Exception e) {}
	}
	
	public static final APSliderBars[] ShowableSBars_CostOnly = {
			APSliderBars.Cost_GasICE,
			APSliderBars.Cost_DieselICE,
			APSliderBars.Cost_NGICE,
			APSliderBars.Cost_FuelCell,
			APSliderBars.Cost_H2Tank,
			APSliderBars.Cost_Motor,
			APSliderBars.Cost_Battery,

			APSliderBars.Cost_RPE,
			APSliderBars.Cost_RPE_ElectSystems,
			APSliderBars.Cost_NumYears,
			APSliderBars.Cost_AnnualDriveDistance,
			APSliderBars.Cost_AnnualDriveDays,

			APSliderBars.Cost_ApprHomeCharger,

			APSliderBars.Cost_Incentives,

			APSliderBars.Price_Gas,
			APSliderBars.Price_Diesel,
			APSliderBars.Price_NG,
			APSliderBars.Price_H2,
			APSliderBars.Price_Elect,
			APSliderBars.Price_DCPremium,

			APSliderBars.Bhv_ChgTimePref,
			APSliderBars.Bhv_FracNonChgPHEVs,
			APSliderBars.Bhv_ChgWindow,
			
			APSliderBars.Bhv_BEVRep_Commercial,
			APSliderBars.Bhv_BEVRep_vehID,
			APSliderBars.Bhv_BEVRep_anxID
	};
	public static final APSliderBars[] ShowableSBars_GHGOnly = {
			APSliderBars.GHG_Gas,
			APSliderBars.GHG_Diesel,
			APSliderBars.GHG_NG,
			APSliderBars.GHG_H2,
			APSliderBars.GHG_Elect,
			
			APSliderBars.MfgGHG_exceptBat,
			APSliderBars.MfgGHG_Battery,
			
			APSliderBars.Bhv_ChgTimePref,
			APSliderBars.Bhv_FracNonChgPHEVs,
			APSliderBars.Bhv_ChgWindow,
			
			APSliderBars.Bhv_BEVRep_vehID,
			APSliderBars.Bhv_BEVRep_anxID
	};
}
