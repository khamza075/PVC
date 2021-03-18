package pvc.gui.comp;

import pvc.datamgmt.comp.*;

public enum CurVisualizationType {
	GHGHistograms(FDefaults.ShowableSBars_GHGOnly), 
	CostBars(FDefaults.ShowableSBars_CostOnly), 
	CostVsGHG(APSliderBars.values())	//Allows showing everything
	;
	private APSliderBars[] showableSBars;
	private CurVisualizationType(APSliderBars[] sbs) {
		showableSBars = sbs;
	}
	
	public boolean ableToShow(APSliderBars sbarType) {
		for (int i=0; i<showableSBars.length; i++) {
			if (showableSBars[i] == sbarType) return true;
		}
		return false;
	}
}
