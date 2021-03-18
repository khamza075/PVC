package pvc.runners;

import java.io.File;

import fastsimjava.FSJOneFileVehModel;
import pvc.calc.*;
import pvc.calc.comp.*;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;
import pvc.gui.*;
import pvc.gui.comp.*;

public class RWChargingSummaries extends RunWrapper {
	
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private CurVisualizationType cvType;
	private HourlyProfileCurve costProfile, ghgProfile;
	
	public RWChargingSummaries(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, CurVisualizationType curVisualization, 
			HourlyProfileCurve costProf, HourlyProfileCurve ghgProf) {
		super();
		
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		cvType = curVisualization;
		costProfile = costProf;
		ghgProfile = ghgProf;
	}
	
	@Override
	protected void actualRun(RunStatusWindow stWindow) throws Exception {
		
		AnalysisVehModelsSetup avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
		if (avms == null) {
			runHadError = true;
			return;
		}
		
		WIITModel wiitModel = WIITModel.readWIITModel(fs, aID, avms);
		if (wiitModel == null) {
			runHadError = true;
			return;
		}
		
		FEcoSimsC fecoMan = new FEcoSimsC(fs, aID);
		int numSolSets = fecoMan.numCompletedSims();
		if (numSolSets < 1) return;
		
		FEcoSimsGenerator fsG = FEcoSimsGenerator.createFEcoSimsGenerator(fs, aID, avms, wiitModel);
		if (fsG == null) {
			runHadError = true;
			return;
		}
		
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();

		for (int i=0; i<numSolSets; i++) {
			fsG.ensureChgSummariesFolderExists(i);
			
			for (int j=0; j<fsofModels.length; j++) {
				int numCases = 0;				
				switch (fsofModels[j].vehModelParam.general.vehPtType) {
				case bev:
					numCases = fsG.numCasesPerBEV();
					break;
				case phev:
					numCases = fsG.numCasesPerPHEV();
					break;
				default:
					break;
				}
				
				switch (fsofModels[j].vehModelParam.general.vehPtType) {
				case bev:
				case phev:
					for (int k=0; k<numCases; k++) {
						String summaryFile = fsG.fChgSummaryFileName(i, j, k);
						if (summaryFile != null) {
							File f = new File(summaryFile);
							
							if (!f.exists()) {
								String chgFile = fsG.fChgFileName(i, j, k);
								ChargingEvents chgEvents = ChargingEvents.readFromFile(chgFile);
								if (chgEvents == null) {
									runHadError = true;
									return;
								}
								
								if (stWindow.abortRequested()) {
									runAborted = true;
									return;
								}
								
								ChgEventsCaseSummary cghSummary = ChgEventsCaseSummary.processChargingData(chgEvents.chgEvents(), costProfile, ghgProfile);
								if (cghSummary == null) {
									runHadError = true;
									return;
								}
								
								if (stWindow.abortRequested()) {
									runAborted = true;
									return;
								}
								
								cghSummary.writeToFile(summaryFile);
							}
						}
					}
					
					stWindow.println("Processed "+avms.vehModelsSetup()[j].shortName+" in: "+fecoMan.getTitlesArray()[i]);
					break;
				default:
					break;
				}
			}
		}
	}
	
	@Override
	public void returnToOwnerWindow() {
		if (runHadError) {
			pMP.subModuleFinished();
			return;
		} else if (runAborted) {
			pMP.subModuleFinished();
			return;
		}
		
		switch (cvType) {
		case CostBars:
			new RCostBarsGUI(fs, aID, pMP, true);
			break;
		case CostVsGHG:
			new RCostVsGHGGUI(fs, aID, pMP, true);
			break;
		case GHGHistograms:
			new RGHGHistogramsGUI(fs, aID, pMP, true);
			break;
		}
	}
}
