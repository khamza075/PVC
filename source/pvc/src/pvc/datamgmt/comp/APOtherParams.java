package pvc.datamgmt.comp;

import java.util.ArrayList;

import pvc.datamgmt.*;

public class APOtherParams {
	private FFStructure fs;
	private int aID;
	private AnalysisVehModelsSetup avms;
	private WIITModel wiitMod;
	
	public FFStructure fs() {return fs;}
	public int aID() {return aID;}
	public AnalysisVehModelsSetup avms() {return avms;}
	public WIITModel wiitMod() {return wiitMod;}
	
	private APOtherEditableParametrs[] vEditableParams;
	public int numEditableParameters() {return vEditableParams.length;}
	public APOtherEditableParametrs getEditableParmeter(int eID) {
		if (vEditableParams == null) return null;
		if (eID < 0) return null;
		if (eID >= vEditableParams.length) return null;
		return vEditableParams[eID];
	}
	public String[] getEditableParametersCaptions() {
		if (vEditableParams == null) return null;
		String[] st = new String[vEditableParams.length];
		for (int i=0; i<st.length; i++) st[i] = new String(vEditableParams[i].caption);
		return st;
	}

	public APOtherParams(FFStructure cFS, int analysisID, AnalysisVehModelsSetup vehModelsSetup, WIITModel wiitModel) {
		fs = cFS;
		aID = analysisID;
		avms = vehModelsSetup;
		wiitMod = wiitModel;
		
		initialize();
	}
	private void initialize() {
		ArrayList<APOtherEditableParametrs> lst = new ArrayList<APOtherEditableParametrs>();
		
		lst.add(APOtherEditableParametrs.vehDepr);
		lst.add(APOtherEditableParametrs.vehLIM);
		
		if (avms.mfgGHGModelIncluded()) lst.add(APOtherEditableParametrs.nonBatMfgGHG);
		
		boolean hasPHEVs = wiitMod.hasPHEVs();
		boolean hasBEVs = wiitMod.hasBEVs();
		boolean hasBEVwRepVeh = false;
		if (hasBEVs) {
			if (wiitMod.bevRepModel.bevRepCommercial == null) hasBEVwRepVeh = true;
		}
		if (hasPHEVs || hasBEVs) {
			lst.add(APOtherEditableParametrs.chgEfficiency);
		}
		
		if (hasPHEVs || hasBEVwRepVeh) {
			lst.add(APOtherEditableParametrs.gridHourlyGHG);
			lst.add(APOtherEditableParametrs.gridHourlyCost);
			lst.add(APOtherEditableParametrs.homeChargerCost);
		}
		
		if (hasBEVwRepVeh) lst.add(APOtherEditableParametrs.bevRepCosts);
		
		vEditableParams = new APOtherEditableParametrs[lst.size()];
		for (int i=0; i<vEditableParams.length; i++) vEditableParams[i] = lst.get(i);
	}

	public enum APOtherEditableParametrs {
		vehDepr("Depreciation Model for each Vehicle"),
		vehLIM("Licensing, Insurance & Maintenance Costs for each Vehicle"),
		nonBatMfgGHG("Range of Manufacturing CO2-Equivalent (aside from Battery) for each Vehicle"),
		chgEfficiency("Vehicles Charging Efficiency for each Vehicle"),
		gridHourlyGHG("Electric Grid Hourly Profile for Marginal CO2 Emissions"),
		gridHourlyCost("Electric Grid Hourly Profile for Electricity Price"),
		homeChargerCost("Cost of Home Charger (by Charger Type)"),
		bevRepCosts("Cost Model for BEV Replacement Vehicle")
		;
		public String caption;
		
		private APOtherEditableParametrs(String s) {
			caption = s;
		}
	}
}
