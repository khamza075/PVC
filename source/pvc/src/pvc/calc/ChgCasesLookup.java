package pvc.calc;

import java.io.File;

import pvc.calc.comp.ChgEventsCaseSummary;
import pvc.datamgmt.FEcoSimsC;

public class ChgCasesLookup {
	private ChgEventsSummariesByPluginVehLocID[][] bySolSet_then_byVehID;
	private FEcoSimsGenerator fsG;
	
	public ChgEventsCaseSummary.CEHistogram getChgSummay_forBEVwRep(int sID, int vehID, float chgWindowCombID, float rangeAnxID, float fracMinGHGChgTiming) {
		int chgWinID1 = (int)chgWindowCombID;
		int chgWinID2 = chgWinID1 + 1;
		float wtWin2 = chgWindowCombID - chgWinID1;
		float wtWin1 = 1f - wtWin2;
		
		if (chgWinID2 > fsG.wiitMod().chgModels.daytimeChargingMinWindow.length) {
			chgWinID2 = chgWinID1;
			wtWin1 = 1f;
			wtWin2 = 0f;
		}
		
		int anxID1 = (int)rangeAnxID;
		int anxID2 = anxID1  +1;
		float wtAnx2 = rangeAnxID - anxID1;
		float wtAnx1 = 1f - wtAnx2;
		
		if (anxID2 >= fsG.wiitMod().bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length) {
			anxID2 = anxID1;
			wtAnx1 = 1f;
			wtAnx2 = 0f;
		}
		
		ChgEventsCaseSummary.CEHistogram chgVehWin1anx1 = 
				bySolSet_then_byVehID[sID][vehID].locChgEventCases[fsG.bevLocalCaseID(chgWinID1, anxID1)].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		ChgEventsCaseSummary.CEHistogram chgVehWin1anx2 = 
				bySolSet_then_byVehID[sID][vehID].locChgEventCases[fsG.bevLocalCaseID(chgWinID1, anxID2)].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		ChgEventsCaseSummary.CEHistogram chgVehWin2anx2 = 
				bySolSet_then_byVehID[sID][vehID].locChgEventCases[fsG.bevLocalCaseID(chgWinID2, anxID2)].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		ChgEventsCaseSummary.CEHistogram chgVehWin2anx1 = 
				bySolSet_then_byVehID[sID][vehID].locChgEventCases[fsG.bevLocalCaseID(chgWinID2, anxID1)].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		
		ChgEventsCaseSummary.CEHistogram chgVehWin1 = ChgEventsCaseSummary.interpolateBetween(chgVehWin1anx1, chgVehWin1anx2, wtAnx1, wtAnx2);
		ChgEventsCaseSummary.CEHistogram chgVehWin2 = ChgEventsCaseSummary.interpolateBetween(chgVehWin2anx1, chgVehWin2anx2, wtAnx1, wtAnx2);
		return ChgEventsCaseSummary.interpolateBetween(chgVehWin1, chgVehWin2, wtWin1, wtWin2);
	}
	public ChgEventsCaseSummary.CEHistogram getChgSummay_forPHEV(int sID, int vehID, float chgWindowCombID, float fracMinGHGChgTiming) {
		int cID1 = (int)chgWindowCombID;
		int cID2 = cID1 + 1;
		float wt2 = chgWindowCombID - cID1;
		float wt1 = 1f - wt2;
		
		if (cID1 == 0) {
			int locID1 = fsG.phevLocalCaseID(false, true, 0);
			int locID2 = fsG.phevLocalCaseID(false, false, 0);
			
			ChgEventsCaseSummary.CEHistogram chgV1 = bySolSet_then_byVehID[sID][vehID].locChgEventCases[locID1].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
			ChgEventsCaseSummary.CEHistogram chgV2 = bySolSet_then_byVehID[sID][vehID].locChgEventCases[locID2].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
			return ChgEventsCaseSummary.interpolateBetween(chgV1, chgV2, wt1, wt2);
		}
		
		int locID1 = fsG.phevLocalCaseID(false, false, cID1-1);
		int locID2 = fsG.phevLocalCaseID(false, false, cID2-1);
		if (locID2 >= bySolSet_then_byVehID[sID][vehID].locChgEventCases.length) {
			return bySolSet_then_byVehID[sID][vehID].locChgEventCases[locID1].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		}
		
		ChgEventsCaseSummary.CEHistogram chgV1 = bySolSet_then_byVehID[sID][vehID].locChgEventCases[locID1].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		ChgEventsCaseSummary.CEHistogram chgV2 = bySolSet_then_byVehID[sID][vehID].locChgEventCases[locID2].getTimingInterpolatedHistogram(fracMinGHGChgTiming);
		return ChgEventsCaseSummary.interpolateBetween(chgV1, chgV2, wt1, wt2);
	}
	
	public static ChgCasesLookup read_wHistograms(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen) {
		try {
			return new ChgCasesLookup(fsMan, fecoSimsGen, true);
		} catch (Exception e) {
			return null;
		}
	}
	public static ChgCasesLookup read_summariesOnly(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen) {
		try {
			return new ChgCasesLookup(fsMan, fecoSimsGen, false);
		} catch (Exception e) {
			return null;
		}
	}
	
	private ChgCasesLookup(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen, boolean readHistograms) throws Exception {
		fsG = fecoSimsGen;
		
		int numSolSets = fsMan.numCompletedSims();
		if (numSolSets < 1) throw new Exception();
		
		int numVehicles = fsG.fsofModels().length;
		if (numVehicles < 1) throw new Exception();

		bySolSet_then_byVehID = new ChgEventsSummariesByPluginVehLocID[numSolSets][numVehicles];
		
		for (int j=0; j<numVehicles; j++) {
			for (int i=0; i<numSolSets; i++) {
				switch (fsG.fsofModels()[j].vehModelParam.general.vehPtType) {
				case bev:
					int numCasesPerBEV = fsG.numCasesPerBEV();
					bySolSet_then_byVehID[i][j] = new ChgEventsSummariesByPluginVehLocID(numCasesPerBEV);
					
					for (int k=0; k<numCasesPerBEV; k++) {
						String chgFileName = fsG.fChgSummaryFileName(i, j, k);
						if (chgFileName != null) {
							File f = new File(chgFileName);
							if (!f.exists()) throw new Exception();
							
							ChgEventsCaseSummary chgCaseSummary = null;
							if (readHistograms) chgCaseSummary = ChgEventsCaseSummary.readFromFile(chgFileName);
							else chgCaseSummary = ChgEventsCaseSummary.readSummaryOnly(chgFileName);
							if (chgCaseSummary == null) throw new Exception();
							
							bySolSet_then_byVehID[i][j].locChgEventCases[k] = chgCaseSummary;
						}
					}
					break;
				case phev:
					int numCasesPerPHEV = fsG.numCasesPerPHEV();
					bySolSet_then_byVehID[i][j] = new ChgEventsSummariesByPluginVehLocID(numCasesPerPHEV);

					for (int k=0; k<numCasesPerPHEV; k++) {
						String chgFileName = fsG.fChgSummaryFileName(i, j, k);
						if (chgFileName != null) {
							File f = new File(chgFileName);
							if (!f.exists()) throw new Exception();
							
							ChgEventsCaseSummary chgCaseSummary = null;
							if (readHistograms) chgCaseSummary = ChgEventsCaseSummary.readFromFile(chgFileName);
							else chgCaseSummary = ChgEventsCaseSummary.readSummaryOnly(chgFileName);
							if (chgCaseSummary == null) throw new Exception();

							bySolSet_then_byVehID[i][j].locChgEventCases[k] = chgCaseSummary;
						}
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	private class ChgEventsSummariesByPluginVehLocID {
		private ChgEventsCaseSummary[] locChgEventCases;
		
		private ChgEventsSummariesByPluginVehLocID(int numCases) {
			locChgEventCases = new ChgEventsCaseSummary[numCases];
		}
	}
}
