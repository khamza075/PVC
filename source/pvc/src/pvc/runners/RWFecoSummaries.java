package pvc.runners;

import java.io.File;

import fastsimjava.FSJOneFileVehModel;
import pvc.calc.FEcoSimsGenerator;
import pvc.calc.comp.*;
import pvc.datamgmt.*;
import pvc.gui.*;
import pvc.gui.comp.*;

public class RWFecoSummaries extends RunWrapper {
	
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private CurVisualizationType cvType;

	public RWFecoSummaries(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, CurVisualizationType curVisualization) {
		super();
		
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		cvType = curVisualization;
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
			CompactTripSummaries cTripMan = CompactTripSummaries.readFromFile(fs.getFilePath_fecoSimsTripSummariesFile(aID, i));
			if (cTripMan == null) {
				runHadError = true;
				return;
			}
			
			stWindow.println("Completed reading general trip summaries for: "+fecoMan.getTitlesArray()[i]);
			if (stWindow.abortRequested()) {
				runAborted = true;
				return;
			}		
			
			fsG.ensureSolPostProcFolderExists(i);
			
			for (int j=0; j<fsofModels.length; j++) {
			
				switch (fsofModels[j].vehModelParam.general.vehPtType) {
				case bev:
					if (fsG.numBEVRepVeh() < 1) {
						String postFile = fsG.fSolPostFileName(i, j, 0);
						if (postFile != null) {
							File f = new File(postFile);
							
							if (!f.exists()) {
								String simsFile = fsG.fSolFileName(i, j, 0);
								FEcoSummayNonPlugin npg = FEcoSummayNonPlugin.processSimFile(cTripMan.cTrips(), simsFile);
								if (npg == null) {
									runHadError = true;
									return;
								}
								npg.writeToFile(postFile);
							}
							
							if (stWindow.abortRequested()) {
								runAborted = true;
								return;
							}		
						}
					} else {
						for (int k=0; k<fsG.numCasesPerBEV(); k++) {
							String postFile = fsG.fSolPostFileName(i, j, k);
							if (postFile != null) {
								File f = new File(postFile);
								
								if (!f.exists()) {
									String simsFile = fsG.fSolFileName(i, j, k);
									FEcoSummayBEVwRep bev = FEcoSummayBEVwRep.processSimFile(cTripMan.cTrips(), simsFile);
									if (bev == null) {
										runHadError = true;
										return;
									}
									bev.writeToFile(postFile);
								}
								
								if (stWindow.abortRequested()) {
									runAborted = true;
									return;
								}		
							}
						}
					}
					break;
				case phev:
					for (int k=0; k<fsG.numCasesPerPHEV(); k++) {
						String postFile = fsG.fSolPostFileName(i, j, k);
						if (postFile != null) {
							File f = new File(postFile);
							
							if (!f.exists()) {
								String simsFile = fsG.fSolFileName(i, j, k);
								FEcoSummayPHEV phev = FEcoSummayPHEV.processSimFile(cTripMan.cTrips(), simsFile);
								if (phev == null) {
									runHadError = true;
									return;
								}
								phev.writeToFile(postFile);
							}
							
							if (stWindow.abortRequested()) {
								runAborted = true;
								return;
							}		
						}
					}
					break;
				default:
					String postFile = fsG.fSolPostFileName(i, j, 0);
					if (postFile != null) {
						File f = new File(postFile);
						
						if (!f.exists()) {
							String simsFile = fsG.fSolFileName(i, j, 0);
							FEcoSummayNonPlugin npg = FEcoSummayNonPlugin.processSimFile(cTripMan.cTrips(), simsFile);
							if (npg == null) {
								runHadError = true;
								return;
							}
							npg.writeToFile(postFile);
						}
						
						if (stWindow.abortRequested()) {
							runAborted = true;
							return;
						}		
					}
					break;				
				}				
				
				stWindow.println("Processed "+avms.vehModelsSetup()[j].shortName+" in: "+fecoMan.getTitlesArray()[i]);
			}			
		}
		
		for (int i=0; i<numSolSets; i++) {
			//Attempt to create JSON file summary for current solution set
			stWindow.println("Creating JSON summary for "+fecoMan.getTitlesArray()[i]);
			try {
				VehSummaiesJSON.createJSONOutput(fs, aID, i);
				stWindow.println("Complete");
			} catch (Exception e) {
				stWindow.println("Unsuccessful");
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
