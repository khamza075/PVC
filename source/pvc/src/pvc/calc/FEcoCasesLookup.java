package pvc.calc;

import java.io.File;

import pvc.calc.comp.*;
import pvc.datamgmt.FEcoSimsC;
import pvc.utility.CGHGHistogram;

public class FEcoCasesLookup {
	public static final float ZTolFracNonChg = 0.001f;
	
	private FEcoCase[][] fecoCases;	//First index on solution sets, Second index on vehicle models
	private FEcoSimsGenerator fsG;
	
	
	public float getMaxGHG_NPG(int sID, int vehID, float ghgPerUnitFuelOrEnergy) {
		return fecoCases[sID][vehID].npg.maxGHGperMile(ghgPerUnitFuelOrEnergy);
	}	
	public FEcoSummayNonPlugin getSummary_NPG(int sID, int vehID) {
		return fecoCases[sID][vehID].npg.copySummary();
	}
	public void populateGHGHistogram_NPG(CGHGHistogram hst, int sID, int vehID, float ghgPerUnitFuelOrEnergy) {
		fecoCases[sID][vehID].npg.populateGHGHistogram(hst, ghgPerUnitFuelOrEnergy);
	}
	
	public float getMaxGHG_PHEVwoChg(int sID, int vehID, float eqGridGHG, float ghgPerUnitFuel) {
		return fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
	}
	public FEcoSummayPHEV getSummary_PHEVwoChg(int sID, int vehID) {
		if (fsG.wiitMod().chgModels.fractionNonChargingPHEVs == null) return null;
		return fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].copySummary();
	}	
	public float getMaxGHG_PHEVwChg(int sID, int vehID, float chgWindowCombID, float eqGridGHG, float ghgPerUnitFuel) {
		int cID1 = (int)chgWindowCombID;
		int cID2 = cID1 + 1;

		if (cID1 == 0) {
			int loc1 = fsG.phevLocalCaseID(false, true, 0);
			int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
			float maxGHG1 = fecoCases[sID][vehID].phevAtLocID[loc1].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
			float maxGHG2 = fecoCases[sID][vehID].phevAtLocID[loc2].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
			return Math.max(maxGHG1, maxGHG2);
		}
		if (cID1 >= fsG.wiitMod().chgModels.daytimeChargingMinWindow.length) {
			return fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(false, false, cID1-1)].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
		}
		
		int loc1 = fsG.phevLocalCaseID(false, false, cID1-1);
		int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
		float maxGHG1 = fecoCases[sID][vehID].phevAtLocID[loc1].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
		float maxGHG2 = fecoCases[sID][vehID].phevAtLocID[loc2].maxGHGperMile(eqGridGHG, ghgPerUnitFuel);
		return Math.max(maxGHG1, maxGHG2);
	}	
	public FEcoSummayPHEV getSummary_PHEVwChg(int sID, int vehID, float chgWindowCombID) {
		int cID1 = (int)chgWindowCombID;
		int cID2 = cID1 + 1;
		float wt2 = chgWindowCombID - cID1;
		float wt1 = 1f - wt2;
		
		if (cID1 == 0) {
			int loc1 = fsG.phevLocalCaseID(false, true, 0);
			int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
			return FEcoSummayPHEV.weighedSummary(fecoCases[sID][vehID].phevAtLocID[loc1], wt1, fecoCases[sID][vehID].phevAtLocID[loc2], wt2);
		}
		if (cID1 >= fsG.wiitMod().chgModels.daytimeChargingMinWindow.length) {
			return fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(false, false, cID1-1)].copySummary();
		}
		int loc1 = fsG.phevLocalCaseID(false, false, cID1-1);
		int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
		return FEcoSummayPHEV.weighedSummary(fecoCases[sID][vehID].phevAtLocID[loc1], wt1, fecoCases[sID][vehID].phevAtLocID[loc2], wt2);
	}	
	public FEcoSummayPHEV getSummary_PHEV(int sID, int vehID, float chgWindowCombID, float fracNonChgPHEVs) {
		if (fracNonChgPHEVs >= ZTolFracNonChg) {
			float wtNonChg = Math.min(fracNonChgPHEVs, 1f);
			float wtChg = 1f - wtNonChg;
			
			if (wtChg >= ZTolFracNonChg) {
				return FEcoSummayPHEV.weighedSummary(
						getSummary_PHEVwChg(sID, vehID, chgWindowCombID), wtChg,
						getSummary_PHEVwoChg(sID, vehID), wtNonChg
						);
			}
			
			return getSummary_PHEVwoChg(sID, vehID);
		}
		
		return getSummary_PHEVwChg(sID, vehID, chgWindowCombID);
	}
	
	public void populateGHGHistogram_PHEV(CGHGHistogram hst, int sID, int vehID, float chgWindowCombID, float fracNonChgPHEVs, 
			float eqGridGHG, float ghgPerUnitFuel) {
		
		if (fracNonChgPHEVs < ZTolFracNonChg) {
			//All are charging
			int cID1 = (int)chgWindowCombID;
			int cID2 = cID1 + 1;
			float wt2 = chgWindowCombID - cID1;
			float wt1 = 1f - wt2;
			
			if (cID1 == 0) {
				int loc1 = fsG.phevLocalCaseID(false, true, 0);
				int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);

				fecoCases[sID][vehID].phevAtLocID[loc1].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt1);
				fecoCases[sID][vehID].phevAtLocID[loc2].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt2);
				return;
			}
			if (cID1 >= fsG.wiitMod().chgModels.daytimeChargingMinWindow.length) {
				fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(false, false, cID1-1)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, 1f);
				return;
			}
			int loc1 = fsG.phevLocalCaseID(false, false, cID1-1);
			int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
			fecoCases[sID][vehID].phevAtLocID[loc1].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt1);
			fecoCases[sID][vehID].phevAtLocID[loc2].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt2);
		} else {
			float fracChgPHEVs = 1f - fracNonChgPHEVs;
			
			if (fracChgPHEVs < ZTolFracNonChg) {
				//All Are non-charging
				fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, 1f);
			} else {
				//Some are charging and some aren't
				int cID1 = (int)chgWindowCombID;
				int cID2 = cID1 + 1;
				float wt2 = chgWindowCombID - cID1;
				float wt1 = 1f - wt2;

				wt1 *= fracChgPHEVs;
				wt2 *= fracChgPHEVs;
				
				if (cID1 == 0) {
					int loc1 = fsG.phevLocalCaseID(false, true, 0);
					int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);

					fecoCases[sID][vehID].phevAtLocID[loc1].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt1);
					fecoCases[sID][vehID].phevAtLocID[loc2].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt2);
					fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, fracNonChgPHEVs);
					return;
				}
				if (cID1 >= fsG.wiitMod().chgModels.daytimeChargingMinWindow.length) {
					fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(false, false, cID1-1)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, fracChgPHEVs);
					fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, fracNonChgPHEVs);
					return;
				}
				int loc1 = fsG.phevLocalCaseID(false, false, cID1-1);
				int loc2 = fsG.phevLocalCaseID(false, false, cID2-1);
				fecoCases[sID][vehID].phevAtLocID[loc1].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt1);
				fecoCases[sID][vehID].phevAtLocID[loc2].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, wt2);
				fecoCases[sID][vehID].phevAtLocID[fsG.phevLocalCaseID(true, false, 0)].populateGHGHistogram(hst, eqGridGHG, ghgPerUnitFuel, fracNonChgPHEVs);
			}
		}
	}
	
	public float getMaxGHG_BEVwRep(int sID, int vehID, float chgWindowCombID, float rangeAnxID, float eqGridGHG, float[] repVehGHGperUnitFuel) {
		float maxGHGperMile = 0f;
		
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

		float wt11 = wtWin1*wtAnx1;	//corresponds to chgWinID1, anxID1)
		float wt12 = wtWin1*wtAnx2;	//corresponds to chgWinID1, anxID2)
		float wt22 = wtWin2*wtAnx2;	//corresponds to chgWinID2, anxID2)
		float wt21 = wtWin2*wtAnx1;	//corresponds to chgWinID2, anxID1)
		
		if (wt11 > 0) {
			float curWorstGHG = fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID1)].maxGHGperMile(eqGridGHG, repVehGHGperUnitFuel);
			maxGHGperMile = Math.max(maxGHGperMile, curWorstGHG);					
		}
		if (wt12 > 0) {
			float curWorstGHG = fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID2)].maxGHGperMile(eqGridGHG, repVehGHGperUnitFuel);
			maxGHGperMile = Math.max(maxGHGperMile, curWorstGHG);					
		}
		if (wt22 > 0) {
			float curWorstGHG = fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID2)].maxGHGperMile(eqGridGHG, repVehGHGperUnitFuel);
			maxGHGperMile = Math.max(maxGHGperMile, curWorstGHG);					
		}
		if (wt21 > 0) {
			float curWorstGHG = fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID1)].maxGHGperMile(eqGridGHG, repVehGHGperUnitFuel);
			maxGHGperMile = Math.max(maxGHGperMile, curWorstGHG);					
		}
		
		return maxGHGperMile;
	}
	public FEcoSummayBEVwRep getSummary_BEVwRep(int sID, int vehID, float chgWindowCombID, float rangeAnxID) {
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
		int anxID2 = anxID1 +1;
		float wtAnx2 = rangeAnxID - anxID1;
		float wtAnx1 = 1f - wtAnx2;
		
		if (anxID2 >= fsG.wiitMod().bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles.length) {
			anxID2 = anxID1;
			wtAnx1 = 1f;
			wtAnx2 = 0f;
		}

		return FEcoSummayBEVwRep.weighedSummary(fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID1)], wtWin1*wtAnx1, 
				fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID2)], wtWin1*wtAnx2, 
				fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID2)], wtWin2*wtAnx2, 
				fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID1)], wtWin2*wtAnx1);
	}
	public void populateGHGHistogram_BEV(CGHGHistogram hst, int sID, int vehID, float chgWindowCombID, float rangeAnxID, 
			float eqGridGHG, float[] repVehGHGperUnitFuel, float fracRepVehID) {
		
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

		float wt11 = wtWin1*wtAnx1;	//corresponds to chgWinID1, anxID1)
		float wt12 = wtWin1*wtAnx2;	//corresponds to chgWinID1, anxID2)
		float wt22 = wtWin2*wtAnx2;	//corresponds to chgWinID2, anxID2)
		float wt21 = wtWin2*wtAnx1;	//corresponds to chgWinID2, anxID1)

		if (wt11 > 0) {
			fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID1)].populateGHGHistogram(hst, eqGridGHG, repVehGHGperUnitFuel, fracRepVehID, wt11);
		}
		if (wt12 > 0) {
			fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID1, anxID2)].populateGHGHistogram(hst, eqGridGHG, repVehGHGperUnitFuel, fracRepVehID, wt12);
		}
		if (wt22 > 0) {
			fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID2)].populateGHGHistogram(hst, eqGridGHG, repVehGHGperUnitFuel, fracRepVehID, wt22);
		}
		if (wt21 > 0) {
			fecoCases[sID][vehID].bevAtLocID[fsG.bevLocalCaseID(chgWinID2, anxID1)].populateGHGHistogram(hst, eqGridGHG, repVehGHGperUnitFuel, fracRepVehID, wt21);
		}
	}
	
	public static FEcoCasesLookup read_wHistograms(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen) {
		try {
			return new FEcoCasesLookup(fsMan, fecoSimsGen, true);
		} catch (Exception e) {
			return null;
		}
	}
	public static FEcoCasesLookup read_summariesOnly(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen) {
		try {
			return new FEcoCasesLookup(fsMan, fecoSimsGen, false);
		} catch (Exception e) {
			return null;
		}
	}

	private FEcoCasesLookup(FEcoSimsC fsMan, FEcoSimsGenerator fecoSimsGen, boolean readHistograms) throws Exception {
		fsG = fecoSimsGen;
		
		int numSolSets = fsMan.numCompletedSims();
		if (numSolSets < 1) throw new Exception();
		
		int numVehicles = fsG.fsofModels().length;
		if (numVehicles < 1) throw new Exception();

		fecoCases = new FEcoCase[numSolSets][numVehicles];
		
		for (int j=0; j<numVehicles; j++) {
			for (int i=0; i<numSolSets; i++) {
				
				fecoCases[i][j] = new FEcoCase();
				
				switch (fsG.fsofModels()[j].vehModelParam.general.vehPtType) {
				case bev:
					if (fsG.numBEVRepVeh() < 1) {
						String solPostFileName = fsG.fSolPostFileName(i,j,0);
						if (solPostFileName!=null) {
							File f = new File(solPostFileName);
							if (!f.exists()) throw new Exception();

							if (readHistograms) fecoCases[i][j].npg = FEcoSummayNonPlugin.readFromFile(solPostFileName);
							else fecoCases[i][j].npg = FEcoSummayNonPlugin.readSummaryOnly(solPostFileName);
						}
					} else {
						int numCasesPerBEV = fsG.numCasesPerBEV();
						fecoCases[i][j].bevAtLocID = new FEcoSummayBEVwRep[numCasesPerBEV];
						
						for (int k=0; k<numCasesPerBEV; k++) {
							String solPostFileName = fsG.fSolPostFileName(i,j,k);
							if (solPostFileName!=null) {
								File f = new File(solPostFileName);
								if (!f.exists()) throw new Exception();

								if (readHistograms) fecoCases[i][j].bevAtLocID[k] = FEcoSummayBEVwRep.readFromFile(solPostFileName);
								else fecoCases[i][j].bevAtLocID[k] = FEcoSummayBEVwRep.readSummaryOnly(solPostFileName);
							}
						}
					}
					break;
				case phev:
					int numCasesPerPHEV = fsG.numCasesPerPHEV();
					fecoCases[i][j].phevAtLocID = new FEcoSummayPHEV[numCasesPerPHEV];
					
					for (int k=0; k<numCasesPerPHEV; k++) {
						String solPostFileName = fsG.fSolPostFileName(i,j,k);
						if (solPostFileName!=null) {
							File f = new File(solPostFileName);
							if (!f.exists()) throw new Exception();

							if (readHistograms) fecoCases[i][j].phevAtLocID[k] = FEcoSummayPHEV.readFromFile(solPostFileName);
							else fecoCases[i][j].phevAtLocID[k] = FEcoSummayPHEV.readSummaryOnly(solPostFileName);
						}
					}
					break;
				default:
					String solPostFileName = fsG.fSolPostFileName(i,j,0);
					if (solPostFileName!=null) {
						File f = new File(solPostFileName);
						if (!f.exists()) throw new Exception();

						if (readHistograms) fecoCases[i][j].npg = FEcoSummayNonPlugin.readFromFile(solPostFileName);
						else fecoCases[i][j].npg = FEcoSummayNonPlugin.readSummaryOnly(solPostFileName);
					}
					break;				
				}
			}
		}
	}
	
	
	private class FEcoCase {
		private FEcoSummayNonPlugin npg;
		private FEcoSummayPHEV[] phevAtLocID;
		private FEcoSummayBEVwRep[] bevAtLocID;
		
		private FEcoCase() {}
	}
}
