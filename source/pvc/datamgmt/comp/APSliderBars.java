package pvc.datamgmt.comp;

public enum APSliderBars {
	Cost_GasICE("Gasoline ICE System Cost"),
	Cost_DieselICE("Diesel ICE System Cost"),
	Cost_NGICE("Natural Gas ICE System Cost"),
	Cost_FuelCell("Fuel Cell System Cost"),
	Cost_H2Tank("Hydrogen Tank Cost"),
	Cost_Motor("Motor System Cost"),
	Cost_Battery("Batteries System Cost"),
	
	Cost_RPE("Average Retail Price Equivalent"),
	Cost_RPE_ElectSystems("Elect. Systems Retail Price Equivalent"),
	Cost_NumYears("Number of Years of Ownership"),
	Cost_AnnualDriveDistance("Annual Driving Distance"),		//auto-set to between 20% to 200% of the value from the driving data-set
	Cost_AnnualDriveDays("Number of Driving Days per Year"),	//auto-set to between 10% to 100% of the value from the driving data-set
	
	Cost_ApprHomeCharger("Fraction Cost of Home Charger (%)"),

	Cost_Incentives("Incentives Level Relative to Baseline (%)"),

	
	Price_Gas("Average Gasoline Price"),	
	Price_Diesel("Average Diesel Price"),	
	Price_NG("Average Natural Gas Price"),	
	Price_H2("Average Hydrogen Price"),		
	Price_Elect("Average Electricity Price"),
	Price_DCPremium("Additional Premium for DC-Fast Charging (%)"),
	
	
	GHG_Gas("Average Gasoline GHG"),	
	GHG_Diesel("Average Diesel GHG"),	
	GHG_NG("Average Natural Gas GHG"),	
	GHG_H2("Average Hydrogen GHG"),		
	GHG_Elect("Average Electricity GHG"),
	
	MfgGHG_exceptBat("Manufacturing GHG Except Battery"),			//This is a scale between 0 to 1 (between min/max values for "less" to "more" g-CO2/kg-Vehicle)
	MfgGHG_Battery("Battery Mfg. GHG Relative to Baseline (%)"),	//This is a scale between 0 to 1
	
	
	Bhv_ChgTimePref("Timing within Charging Events"),		//Slides between 0 = minimum cost (or start immediately) to 1 = minimize grid GHG (via hourly profile)
	Bhv_FracNonChgPHEVs("Fraction of PHEV Owners Not Charging (%)"),	
	Bhv_ChgWindow("Minimum Time-Window for Charging (hr)"),	//Auto-set minimum value of 12-hrs == overnight only

	Bhv_BEVRep_Commercial("Fraction Extra Vehicles in BEV Fleet (%)"),
	Bhv_BEVRep_vehID("BEV Replcement Vehicle"),
	Bhv_BEVRep_anxID("BEV Range Anxiety"),	//Units and value changes with selected units
	
	;
	public String caption;

	private APSliderBars(String s) {
		caption = s;
	}
	public static APSliderBars decode(String s) {
		for (int i=0; i<values().length; i++) {
			if (s.equalsIgnoreCase(values()[i].toString())) return values()[i];
		}
		return null;
	}
}
