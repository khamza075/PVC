package pvc.datamgmt;

import java.io.FileWriter;
import java.util.ArrayList;

import pvc.calc.*;
import pvc.datamgmt.comp.*;
import pvc.utility.CGHGHistogram;

public class ReportsGenerator {
	private ReportsGenerator() {}

	public static void genCostVsGHGSummaryReport(String fname, SliderBarsManager sbarMan, FEcoSimsC fecoSims,
			UsePhaseSSimulator.OutputStructure upRes, TCOCalculator tcoCalc, float lifetimeMilesLCA) throws Exception {
		FileWriter fout = new FileWriter(fname);
		String lsep = System.getProperty("line.separator");
		
		//Summary section of solution set
		fout.append("_____Drive Cycles or Real-World Driving Trips Data-set"+lsep);
		
		int sID = sbarMan.rvStatus().solSetID();
		fout.append(fecoSims.getTitlesArray()[sID]+lsep);
		fout.append(fecoSims.getDescripton(sID)+lsep);		
		fout.append(lsep);
		
		//Summary Section of Slider Bar Positions
		fout.append("_____Parameter Values"+lsep);
		sbarMan.rvStatus().writeSummaryInFWriter(fout);
		fout.append(lsep);
		
		//Indicator if Manufacturing GHG is included
		if (sbarMan.rvStatus().includeMfgGHG()) {
			fout.append("_____Manufactring_GHG_is_Included"+lsep+lsep);			
		}
		
		//Cost versus GHG details
		fout.append(tcoCalc.getCostVsGHGSummary(upRes, sbarMan, lifetimeMilesLCA).toString());
		
		fout.flush();
		fout.close();
	}
	
	public static void genGHGReportFiles(String folderName, SliderBarsManager sbarMan, FEcoSimsC fecoSims, 
			UsePhaseSSimulator.VehGHGAnalysisResult[] vehHstRes, AnalysisVehModelsSetup.AVehModelSetup[] vms, 
			GHGDisplaySetup displaySetup) throws Exception {
		
		//Summary section of scenario parameters
		String scSummaryFileName = folderName + FFStructure.slashChar() + "_ScenarioParameters.csv";
		FileWriter fout = new FileWriter(scSummaryFileName);
		String lsep = System.getProperty("line.separator");
		
		fout.append("_____Drive Cycles or Real-World Driving Trips Data-set"+lsep);
		
		int sID = sbarMan.rvStatus().solSetID();
		fout.append(fecoSims.getTitlesArray()[sID]+lsep);
		fout.append(fecoSims.getDescripton(sID)+lsep);		
		fout.append(lsep);
		
		fout.append("_____Parameter Values"+lsep);
		sbarMan.rvStatus().writeSummaryInFWriter(fout);
		fout.append(lsep);
		
		if (sbarMan.rvStatus().includeMfgGHG()) {
			fout.append("_____Manufactring_GHG_is_Included"+lsep+lsep);			
		}
		fout.flush();
		fout.close();
		
		//GHG Histogram Files
		for (int i=0; i<vms.length; i++) {			
			if (displaySetup.getVehAtDisplayPos(i).isShown) {
				int vID = displaySetup.getVehAtDisplayPos(i).vehID();
				String fname = folderName + FFStructure.slashChar() + "hstGHG_"+vms[vID].shortName+".csv";
				if (vehHstRes[vID] != null) vehHstRes[vID].genReportFile(fname);
			}
		}
		
		//Box-Plots Summary File
		ArrayList<CGHGHistogram.BoxPlot> lstBxp = new ArrayList<CGHGHistogram.BoxPlot>();
		ArrayList<String> lstCaptions = new ArrayList<String>();
		
		for (int i=0; i<vms.length; i++) {
			if (displaySetup.getVehAtDisplayPos(i).isShown) {
				int vID = displaySetup.getVehAtDisplayPos(i).vehID();
				if (vehHstRes[vID] != null) {
					lstCaptions.add(new String(vms[vID].shortName));
					lstBxp.add(vehHstRes[vID].ghgHistogram.getBoxPlotInOutputUnits(vehHstRes[vID].averageGCO2perMile));
				}
			}
		}
		
		if (lstBxp.size() > 0) {
			String fname = folderName + FFStructure.slashChar() + "_bxpGHG_usePhaseOnly.csv";
			String[] captions = new String[lstCaptions.size()];
			
			CGHGHistogram.BoxPlot[] bxps = new CGHGHistogram.BoxPlot[captions.length];
			for (int i=0; i<bxps.length; i++) {
				captions[i] = lstCaptions.get(i);
				bxps[i] = lstBxp.get(i);
			}
			
			CGHGHistogram.writeBoxPlotsFile(fname, bxps, captions);
			
			if (sbarMan.rvStatus().includeMfgGHG()) {
				CGHGHistogram.BoxPlot[] bxpswMfg = new CGHGHistogram.BoxPlot[bxps.length];
				int cID = 0;				
				for (int i=0; i<vms.length; i++) {
					if (displaySetup.getVehAtDisplayPos(i).isShown) {
						int vID = displaySetup.getVehAtDisplayPos(i).vehID();
						if (vehHstRes[vID] != null) {
							bxpswMfg[cID] = new CGHGHistogram.BoxPlot(bxps[cID], vehHstRes[vID].mfgEqGCO2perMile);							
							cID++;
						}
					}
				}
				
				fname = folderName + FFStructure.slashChar() + "_bxpGHG_wMfgGHG.csv";
				CGHGHistogram.writeBoxPlotsFile(fname, bxpswMfg, captions);
			}
		}
	}
	
	public static void genCostBarsReport(String fname, SliderBarsManager sbarMan, FEcoSimsC fecoSims, 
			CostBarsDisplaySetup.CostBarsPlotOutput cbOut) throws Exception {
		FileWriter fout = new FileWriter(fname);
		String lsep = System.getProperty("line.separator");
		
		//Summary section of solution set
		fout.append("_____Drive Cycles or Real-World Driving Trips Data-set"+lsep);
		
		int sID = sbarMan.rvStatus().solSetID();
		fout.append(fecoSims.getTitlesArray()[sID]+lsep);
		fout.append(fecoSims.getDescripton(sID)+lsep);		
		fout.append(lsep);
		
		//Summary Section of Slider Bar Positions
		fout.append("_____Parameter Values"+lsep);
		sbarMan.rvStatus().writeSummaryInFWriter(fout);
		fout.append(lsep);

		//Cost Bars
		fout.append(cbOut.toString());
		
		fout.flush();
		fout.close();
	}
}
