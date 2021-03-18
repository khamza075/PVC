package pvc.calc.comp;

import java.util.ArrayList;

import pvc.datamgmt.WIITModel;

public class DayTimeChargingPlanner {
	private static final float ZTol_phevGasMiles = 0.01f;
	private static final int MaxCandidateChgEventsForExS2 = 12;
	private static final int MaxCandidateChgEventsForExS3 = 8;
	
	
	private DayTimeChargingPlanner() {}

	public static class DTChargingPlan {
		private int idFirstTripOfCurrentDay;
		private ChargingEvents.ChargingEventType[] chgTypeBeforeTripIDinCurDay;	//null value implies no charging event		
		private DTChargingPlan(int idFirstTrip, int numTripsInCurDay) {
			idFirstTripOfCurrentDay = idFirstTrip;
			chgTypeBeforeTripIDinCurDay = new ChargingEvents.ChargingEventType[numTripsInCurDay];
		}		
		public ChargingEvents.ChargingEventType chgEventBeforeTrip(int tripIDinAllTripsArray) {
			return chgTypeBeforeTripIDinCurDay[tripIDinAllTripsArray - idFirstTripOfCurrentDay];
		}
	}
	
	public static DTChargingPlan bev_daytimeChgPlan(VehicleSampleMA.Trip[] allVSTrips, int idFirstTripOfDay, int idLastTripOfDay,
			float relSoCDayStart, float batterySwingKWh, float nomAER, float minDTWindowHrs, int bevAnxID, WIITModel wiitModel) {
		
		DTChargingPlan res = new DTChargingPlan(idFirstTripOfDay, idLastTripOfDay - idFirstTripOfDay + 1);	//Automatically initialized as no charging events

		//Check if possible to get through the day without any charging events
		float dayMiles = 0;
		for (int i=idFirstTripOfDay; i<=idLastTripOfDay; i++) dayMiles += allVSTrips[i].tripIDs().miles;
		
		float remMiles = relSoCDayStart*nomAER;
		float remMilesAfterAnx = remMiles - wiitModel.bevRepModel.bevRepWholeDay.calcRangeAnx(remMiles, 
				wiitModel.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles[bevAnxID]);

		if (dayMiles <= remMilesAfterAnx) return res;
		
		//Check if there are feasible charging time windows
		ArrayList<Integer> lstPossibleChgBeforeTripID = new ArrayList<Integer>();
		ArrayList<Float> lstPossibleChgWindowHr = new ArrayList<Float>();
		
		for (int i=idFirstTripOfDay+1; i<=idLastTripOfDay; i++) {
			float hrsWindow = ((float)allVSTrips[i].tripIDs().secsFromLastTrip)/ChargingEvents.SecondsPerHour;
			
			if (hrsWindow >= minDTWindowHrs) {
				lstPossibleChgBeforeTripID.add(i);
				lstPossibleChgWindowHr.add(hrsWindow);
			}
		}	
		if (lstPossibleChgBeforeTripID.size() < 1) {
			//Unable to do the day on the BEV (since the day cannot be done with Overnight-only, and there are no feasible time windows for day-time charging
			return null;
		}
		
		//Setup kinematic analysis charging plan optimization model
		KinematicOptChgPlanModel dModel = new KinematicOptChgPlanModel(wiitModel, relSoCDayStart, batterySwingKWh, 
				nomAER, bevAnxID, allVSTrips, idFirstTripOfDay, idLastTripOfDay, lstPossibleChgBeforeTripID, lstPossibleChgWindowHr);

		ChargingEvents.ChargingEventType[] chgPlan = new ChargingEvents.ChargingEventType[dModel.chgCandidateTripIDinDay.length];	

		//Check whether it is feasible to do the day with maximum daytime charging
		if (wiitModel.chgModels.dcFastAvailable) {
			for (int i=0; i<chgPlan.length; i++) {
				chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
			}
		} else {
			for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
		}
		
		if (!checkIfBEVCanDoDay(dModel, chgPlan)) {
			return null;	//Unable to do the day on the BEV
		}

		//Run Optimization
		if (wiitModel.chgModels.dcFastAvailable) {
			//DC-Fast is available
			if (wiitModel.chgModels.dtChgPrioritizesDCFastIfFeasible) {
				//Prioritizing DC-Fast, optimize vi DC-Fast only
				if (chgPlan.length <= MaxCandidateChgEventsForExS2) bevOptimizeChgPlan_DCFast_exhaustive(chgPlan, dModel);
				else bevOptimizeChgPlan_DCFast_greedy(chgPlan, dModel);
			} else {
				//Favoring L2 over DC-Fast... check if possible to do the day with only L2
				for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
				if (checkIfBEVCanDoDay(dModel, chgPlan)) {
					//Can do the day with L2-only
					if (chgPlan.length <= MaxCandidateChgEventsForExS2) bevOptimizeChgPlan_L2_exhaustive(chgPlan, dModel);
					else bevOptimizeChgPlan_L2_greedy(chgPlan, dModel);
				} else {
					//Run a mix of DC-Fast and L2
					for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
					
					if (chgPlan.length <= MaxCandidateChgEventsForExS3) bevOptimizeChgPlan_L2orDCFast_exhaustive(chgPlan, dModel);
					else bevOptimizeChgPlan_L2orDCFast_greedy(chgPlan, dModel);
				}
			}
			
		} else {
			//DC-Fast is NOT available, optimize via L2 only
			if (chgPlan.length <= MaxCandidateChgEventsForExS2) bevOptimizeChgPlan_L2_exhaustive(chgPlan, dModel);
			else bevOptimizeChgPlan_L2_greedy(chgPlan, dModel);
		}		
		
		for (int i=0; i<chgPlan.length; i++) res.chgTypeBeforeTripIDinCurDay[lstPossibleChgBeforeTripID.get(i)-idFirstTripOfDay] = chgPlan[i];
		return res;
	}
	
	public static DTChargingPlan phev_daytimeChgPlan(VehicleSampleMA.Trip[] allVSTrips, int idFirstTripOfDay, int idLastTripOfDay,
			float relSoCDayStart, float batterySwingKWh, float nomAER, float minDTWindowHrs, WIITModel wiitModel) {
		
		DTChargingPlan res = new DTChargingPlan(idFirstTripOfDay, idLastTripOfDay - idFirstTripOfDay + 1);	//Automatically initialized as no charging events
		
		//Check if possible to get through the day without any charging events
		float dayMiles = 0;
		for (int i=idFirstTripOfDay; i<=idLastTripOfDay; i++) dayMiles += allVSTrips[i].tripIDs().miles;
		
		float remElectMiles = relSoCDayStart*nomAER;
		if (dayMiles <= remElectMiles) return res;
		
		//Check if there are feasible charging time windows
		ArrayList<Integer> lstPossibleChgBeforeTripID = new ArrayList<Integer>();
		ArrayList<Float> lstPossibleChgWindowHr = new ArrayList<Float>();
		
		for (int i=idFirstTripOfDay+1; i<=idLastTripOfDay; i++) {
			float hrsWindow = ((float)allVSTrips[i].tripIDs().secsFromLastTrip)/ChargingEvents.SecondsPerHour;
			
			if (hrsWindow >= minDTWindowHrs) {
				lstPossibleChgBeforeTripID.add(i);
				lstPossibleChgWindowHr.add(hrsWindow);
			}
		}
		
		if (lstPossibleChgBeforeTripID.size() < 1) return res;
		
		//Setup kinematic analysis charging plan optimization model
		KinematicOptChgPlanModel dModel = new KinematicOptChgPlanModel(wiitModel, relSoCDayStart, batterySwingKWh, 
				nomAER, -1, allVSTrips, idFirstTripOfDay, idLastTripOfDay, lstPossibleChgBeforeTripID, lstPossibleChgWindowHr);

		ChargingEvents.ChargingEventType[] chgPlan;
		if (wiitModel.chgModels.dcFastAvailable && (wiitModel.chgModels.minNomAERForPHEVsToHaveDCFast > 0)) {
			//Consider possibility of DC-Fast
			
			if (nomAER >= wiitModel.chgModels.minNomAERForPHEVsToHaveDCFast) {
				//Current PHEV is DC-Fast capable
				
				if (wiitModel.chgModels.dtChgPrioritizesDCFastIfFeasible) {
					//Prioritizing DC-Fast
					chgPlan = phevOptimizeChgPlan_DCFast(dModel);
				} else {
					//UseDC-Fast only if it minimizes gas miles
					chgPlan = phevOptimizeChgPlan_L2orDCFast(dModel);
				}
				
			} else {
				//Current PHEV is not DC-Fast capable
				chgPlan = phevOptimizeChgPlan_L2(dModel);
			}
		} else {
			//DC Fast is not in consideration
			chgPlan = phevOptimizeChgPlan_L2(dModel);
		}
		
		for (int i=0; i<chgPlan.length; i++) res.chgTypeBeforeTripIDinCurDay[lstPossibleChgBeforeTripID.get(i)-idFirstTripOfDay] = chgPlan[i];
		return res;
	}
		
	private static void bevOptimizeChgPlan_L2orDCFast_greedy(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumDCFast = numDCFastChgEvents(chgPlan);
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);

		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;
		
		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					//One neighbor (above)
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDCFast) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
								curMinNumDCFast = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}
						}
					}
				} else if (canChgPlan[i] == ChargingEvents.ChargingEventType.dayTime_DCFast) {
					//One neighbor (below)
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDCFast) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
								curMinNumDCFast = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}
						}
					}
				} else {
					//One (below) out of Two Neighbors
					canChgPlan[i] = null;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDCFast) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
								curMinNumDCFast = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}
						}
					}
					
					//Two (above) out of Two Neighbors
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
					curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDCFast) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
								curMinNumDCFast = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}
						}
					}
				}
			}
		}
	}
	private static void bevOptimizeChgPlan_DCFast_greedy(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumDCFast = numDCFastChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;
		
		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				} else {
					canChgPlan[i] = null;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDCFast) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumDCFast = curNumDC;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				}
			}
		}
	}
	private static void bevOptimizeChgPlan_L2_greedy(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;
		
		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					
					if (curNumL2 < curMinNumL2) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumL2 = curNumL2;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				} else {
					canChgPlan[i] = null;
					int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					
					if (curNumL2 < curMinNumL2) {
						if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
							curMinNumL2 = curNumL2;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				}
			}
		}
	}
	private static void bevOptimizeChgPlan_L2orDCFast_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumDCFast = numDCFastChgEvents(chgPlan);
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		int nEX = intPow(3, chgPlan.length);
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		
		for (int i=1; i<nEX; i++) {
			genExInstance3(canChgPlan, i);
			int curNumDCFast = numDCFastChgEvents(canChgPlan);
			
			if (curNumDCFast < curMinNumDCFast) {
				if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
					curMinNumDCFast = curNumDCFast;
					curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			} else if (curNumDCFast == curMinNumDCFast) {
				int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
				
				if (curNumL2 < curMinNumL2) {
					if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
						curMinNumDCFast = curNumDCFast;
						curMinNumL2 = curNumL2;
						for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
					}
				}
			}
		}
	}
	private static void bevOptimizeChgPlan_DCFast_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumDCFast = numDCFastChgEvents(chgPlan);
		
		int nEX = intPow(2, chgPlan.length);
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		
		for (int i=1; i<nEX; i++) {
			genExInstanceDC(canChgPlan, i);
			int curNumDCFast = numDCFastChgEvents(canChgPlan);
			
			if (curNumDCFast < curMinNumDCFast) {
				if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
					curMinNumDCFast = curNumDCFast;
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			}
		}
	}
	private static void bevOptimizeChgPlan_L2_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, KinematicOptChgPlanModel dModel) {
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		int nEX = intPow(2, chgPlan.length);
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		
		for (int i=1; i<nEX; i++) {
			genExInstanceL2(canChgPlan, i);
			int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
			
			if (curNumL2 < curMinNumL2) {
				if (checkIfBEVCanDoDay(dModel, canChgPlan)) {
					curMinNumL2 = curNumL2;
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			}
		}
	}
	
	private static ChargingEvents.ChargingEventType[] phevOptimizeChgPlan_L2(KinematicOptChgPlanModel dModel) {
		ChargingEvents.ChargingEventType[] chgPlan = new ChargingEvents.ChargingEventType[dModel.chgCandidateTripIDinDay.length];
		
		//Gas mile with L2 in every charging opportunity
		for (int i=0; i<chgPlan.length; i++) {
			chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
		}
		float gasMiles_wAllL2 = estimatePHEVDaytimeGasMiles(dModel, chgPlan);

		//Optimize via L2 (2-parameter levels)
		if (chgPlan.length <= MaxCandidateChgEventsForExS2) {
			phevOptimizeChgPlan_L2_exhaustive(chgPlan, gasMiles_wAllL2, dModel);
		}
		else {
			phevOptimizeChgPlan_L2_greedy(chgPlan, gasMiles_wAllL2, dModel);
		}
		
		return chgPlan;
	}
	private static ChargingEvents.ChargingEventType[] phevOptimizeChgPlan_DCFast(KinematicOptChgPlanModel dModel) {
		ChargingEvents.ChargingEventType[] chgPlan = new ChargingEvents.ChargingEventType[dModel.chgCandidateTripIDinDay.length];
		
		//Gas mile with DC-Fast in every charging opportunity
		for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
		float gasMiles_wAllDCFast = estimatePHEVDaytimeGasMiles(dModel, chgPlan);
		
		//Optimize via DC-Fast (2-parameter levels)
		if (chgPlan.length <= MaxCandidateChgEventsForExS3) phevOptimizeChgPlan_DCFast_exhaustive(chgPlan, gasMiles_wAllDCFast, dModel);
		else phevOptimizeChgPlan_DCFast_greedy(chgPlan, gasMiles_wAllDCFast, dModel);

		return chgPlan;
	}
	private static ChargingEvents.ChargingEventType[] phevOptimizeChgPlan_L2orDCFast(KinematicOptChgPlanModel dModel) {
		ChargingEvents.ChargingEventType[] chgPlan = new ChargingEvents.ChargingEventType[dModel.chgCandidateTripIDinDay.length];
		
		//Gas mile with DC-Fast in every charging opportunity
		for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
		float gasMiles_wAllDCFast = estimatePHEVDaytimeGasMiles(dModel, chgPlan);
		
		//Gas mile with L2 in every charging opportunity
		for (int i=0; i<chgPlan.length; i++) chgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
		float gasMiles_wAllL2 = estimatePHEVDaytimeGasMiles(dModel, chgPlan);
		
		//Optimize with L2-only if not different than with DC-Fast
		if (Math.abs(gasMiles_wAllDCFast - gasMiles_wAllL2) <= ZTol_phevGasMiles) {
			return phevOptimizeChgPlan_L2(dModel);
		}
		
		//Optimize via a Mix of DC-Fast and L2 (3-parameter levels)
		if (chgPlan.length <= MaxCandidateChgEventsForExS3) phevOptimizeChgPlan_L2orDCFast_exhaustive(chgPlan, gasMiles_wAllDCFast, dModel);
		else phevOptimizeChgPlan_L2orDCFast_greedy(chgPlan, gasMiles_wAllDCFast, dModel);
		
		return chgPlan;
	}
	private static void phevOptimizeChgPlan_L2orDCFast_greedy(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllDCFast,
			KinematicOptChgPlanModel dModel) {
		int curMinNumDC = numDCFastChgEvents(chgPlan);
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;

		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					//One direction (up)
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					
					int curNumDC = numDCFastChgEvents(canChgPlan);
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDC) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
							
							if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
								curMinNumDC = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}							
						}
					}
				} else if (canChgPlan[i] == ChargingEvents.ChargingEventType.dayTime_DCFast) {
					//One direction (down)
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					
					int curNumDC = numDCFastChgEvents(canChgPlan);
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDC) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
							
							if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
								curMinNumDC = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}							
						}
					}
				} else {
					//one of Two Directions (down)
					canChgPlan[i] = null;
										
					int curNumDC = numDCFastChgEvents(canChgPlan);
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDC) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
							
							if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
								curMinNumDC = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}							
						}
					}
					
					//Two of Two Directions (up)
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
									
					curNumDC = numDCFastChgEvents(canChgPlan);
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					} else if (curNumDC == curMinNumDC) {
						int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
						
						if (curNumL2 < curMinNumL2) {
							float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
							
							if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
								curMinNumDC = curNumDC;
								curMinNumL2 = curNumL2;
								for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
								betterNeighborFound = true;
							}							
						}
					}
				}
			}
		}
	}
	private static void phevOptimizeChgPlan_L2_greedy(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllL2,
			KinematicOptChgPlanModel dModel) {
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;
		
		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_L2;
					int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					
					if (curNumL2 < curMinNumL2) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						if (Math.abs(gasMiles_wAllL2 - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumL2 = curNumL2;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				} else {
					canChgPlan[i] = null;
					int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					
					if (curNumL2 < curMinNumL2) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						if (Math.abs(gasMiles_wAllL2 - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumL2 = curNumL2;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				}
			}
		}
	}
	private static void phevOptimizeChgPlan_DCFast_greedy(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllDCFast,
			KinematicOptChgPlanModel dModel) {
		int curMinNumDC = numDCFastChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		boolean betterNeighborFound = true;
		
		while (betterNeighborFound) {
			//Set flag to finish if no better solution is found
			betterNeighborFound = false;
			
			//Loop on all candidate events (one at a time)
			for (int i=0; i<chgPlan.length; i++) {
				//Copy current best solution to candidate
				for (int j=0; j<chgPlan.length; j++) canChgPlan[j] = chgPlan[j];
			
				//Neighbor on current candidate event
				if (canChgPlan[i] == null) {
					canChgPlan[i] = ChargingEvents.ChargingEventType.dayTime_DCFast;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				} else {
					canChgPlan[i] = null;
					int curNumDC = numDCFastChgEvents(canChgPlan);
					
					if (curNumDC < curMinNumDC) {
						float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
						if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
							curMinNumDC = curNumDC;
							for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
							betterNeighborFound = true;
						}
					}
				}
			}
		}
	}
	private static void phevOptimizeChgPlan_L2orDCFast_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllDCFast,
			KinematicOptChgPlanModel dModel) {
		int curMinNumDC = numDCFastChgEvents(chgPlan);
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		int nEX = intPow(3, chgPlan.length);
		
		for (int i=1; i<nEX; i++) {
			genExInstance3(canChgPlan, i);
			int curNumDC = numDCFastChgEvents(canChgPlan);
			
			if (curNumDC < curMinNumDC) {
				float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
				if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
					curMinNumDC = curNumDC;
					curMinNumL2 = numDaytimeL2ChgEvents(canChgPlan);
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			} else if (curNumDC == curMinNumDC) {
				int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
				
				if (curNumL2 < curMinNumL2) {
					float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
					if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
						curMinNumDC = curNumDC;
						curMinNumL2 = curNumL2;
						for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
					}
				}
			}
		}
	}
	private static void phevOptimizeChgPlan_L2_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllL2,
			KinematicOptChgPlanModel dModel) {
		int curMinNumL2 = numDaytimeL2ChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		int nEX = intPow(2, chgPlan.length);
		
		for (int i=1; i<nEX; i++) {
			genExInstanceL2(canChgPlan, i);
			int curNumL2 = numDaytimeL2ChgEvents(canChgPlan);
			
			if (curNumL2 < curMinNumL2) {
				float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
				if (Math.abs(gasMiles_wAllL2 - curGasMiles) <= ZTol_phevGasMiles) {
					curMinNumL2 = curNumL2;
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			}
		}
	}
	private static void phevOptimizeChgPlan_DCFast_exhaustive(ChargingEvents.ChargingEventType[] chgPlan, float gasMiles_wAllDCFast,
			KinematicOptChgPlanModel dModel) {
		int curMinNumDC = numDCFastChgEvents(chgPlan);
		
		ChargingEvents.ChargingEventType[] canChgPlan = new ChargingEvents.ChargingEventType[chgPlan.length];
		int nEX = intPow(2, chgPlan.length);
		
		for (int i=1; i<nEX; i++) {
			genExInstanceDC(canChgPlan, i);
			int curNumDC = numDCFastChgEvents(canChgPlan);
			
			if (curNumDC < curMinNumDC) {
				float curGasMiles = estimatePHEVDaytimeGasMiles(dModel, canChgPlan);
				if (Math.abs(gasMiles_wAllDCFast - curGasMiles) <= ZTol_phevGasMiles) {
					curMinNumDC = curNumDC;
					for (int j=0; j<chgPlan.length; j++) chgPlan[j] = canChgPlan[j];
				}
			}
		}
	}
	
	private static void genExInstanceL2(ChargingEvents.ChargingEventType[] canChgPlan, int exID) {
		int bitID = 0;
		int remValue = exID;
		int bitBase = intPow(2, canChgPlan.length-1);
		
		while (bitID < canChgPlan.length) {
			int bitValue = remValue/bitBase;
			remValue = remValue % bitBase;
			bitBase = bitBase / 2;
			
			//Interpret bitValue
			switch (bitValue) {
			case 1:
				canChgPlan[bitID] = ChargingEvents.ChargingEventType.dayTime_L2;
				break;
			default:
				canChgPlan[bitID] = null;
				break;
			}
			bitID++;
		}
	}
	private static void genExInstanceDC(ChargingEvents.ChargingEventType[] canChgPlan, int exID) {
		int bitID = 0;
		int remValue = exID;
		int bitBase = intPow(2, canChgPlan.length-1);
		
		while (bitID < canChgPlan.length) {
			int bitValue = remValue/bitBase;
			remValue = remValue % bitBase;
			bitBase = bitBase / 2;
			
			//Interpret bitValue
			switch (bitValue) {
			case 1:
				canChgPlan[bitID] = ChargingEvents.ChargingEventType.dayTime_DCFast;
				break;
			default:
				canChgPlan[bitID] = null;
				break;
			}
			bitID++;
		}
	}
	private static void genExInstance3(ChargingEvents.ChargingEventType[] canChgPlan, int exID) {
		int bitID = 0;
		int remValue = exID;
		int bitBase = intPow(3, canChgPlan.length-1);
		
		while (bitID < canChgPlan.length) {
			int bitValue = remValue/bitBase;
			remValue = remValue % bitBase;
			bitBase = bitBase / 3;
			
			//Interpret bitValue
			switch (bitValue) {
			case 1:
				canChgPlan[bitID] = ChargingEvents.ChargingEventType.dayTime_L2;
				break;
			case 2:
				canChgPlan[bitID] = ChargingEvents.ChargingEventType.dayTime_DCFast;
				break;
			default:
				canChgPlan[bitID] = null;
				break;
			}
			bitID++;
		}
	}
	private static int intPow(int base, int pow) {
		if (pow == 0) return 1;
		int res = base;
		for (int i=0; i<pow; i++) res *= base;
		return res;
	}
	
	private static class KinematicOptChgPlanModel {
		private float[] dayTripMiles;
		private int[] chgCandidateTripIDinDay;
		private float[] chgCandidateHrsWindow;
		private float relSoCatDayStart, batSwingKWh, nominalAER;
		private int anxID;
		private WIITModel wiitMod;
		
		private KinematicOptChgPlanModel(WIITModel wiitModel, float relSoCDayStart, float batterySwingKWh, float nomAER, int bevAnxID,
				VehicleSampleMA.Trip[] allVSTrips, int idFirstTripOfDay, int idLastTripOfDay, 
				ArrayList<Integer> lstPossibleChgBeforeTripID, ArrayList<Float> lstPossibleChgWindowHr) {
			wiitMod = wiitModel;
			relSoCatDayStart = relSoCDayStart;
			batSwingKWh = batterySwingKWh;
			nominalAER = nomAER;
			anxID = bevAnxID;
			
			dayTripMiles = new float[idLastTripOfDay - idFirstTripOfDay + 1];
			for (int i=0; i<dayTripMiles.length; i++) dayTripMiles[i] = allVSTrips[idFirstTripOfDay+i].tripIDs().miles;
			
			chgCandidateTripIDinDay = new int[lstPossibleChgBeforeTripID.size()];
			chgCandidateHrsWindow = new float[chgCandidateTripIDinDay.length];
			for (int i=0; i<chgCandidateTripIDinDay.length; i++) {
				chgCandidateTripIDinDay[i] = lstPossibleChgBeforeTripID.get(i) - idFirstTripOfDay;
				chgCandidateHrsWindow[i] = lstPossibleChgWindowHr.get(i);
			}
		}
	}
	private static float estimatePHEVDaytimeGasMiles(KinematicOptChgPlanModel dModel, ChargingEvents.ChargingEventType[] chgPlan) {
		float gasMiles = 0f;
		
		float kWhpm = dModel.batSwingKWh / dModel.nominalAER;
		float curRelSoC = dModel.relSoCatDayStart;
		float curRemBatKWh = curRelSoC*dModel.batSwingKWh;
		
		int curTripID = 0;
		int curCandidateChgEventID = 0;
		int nextCandidateChgEventBeforeTripID = dModel.chgCandidateTripIDinDay[curCandidateChgEventID];
		
		while (curTripID < dModel.dayTripMiles.length) {			
			if (curTripID == nextCandidateChgEventBeforeTripID) {
				if (chgPlan[curCandidateChgEventID] != null) {
					ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(chgPlan[curCandidateChgEventID], dModel.wiitMod, 
							dModel.chgCandidateHrsWindow[curCandidateChgEventID]*ChargingEvents.SecondsPerHour, curRelSoC, dModel.batSwingKWh);
					curRelSoC = chgRes.finalRelSoC;
					curRemBatKWh = curRelSoC*dModel.batSwingKWh;
				}				
				
				curCandidateChgEventID++;
				if (curCandidateChgEventID < dModel.chgCandidateTripIDinDay.length) {
					nextCandidateChgEventBeforeTripID = dModel.chgCandidateTripIDinDay[curCandidateChgEventID];
				}
			}
			
			float tripMiles = dModel.dayTripMiles[curTripID];
			float remElectricMiles = curRemBatKWh / kWhpm;
			
			if (tripMiles > remElectricMiles) {
				float tripElectricMiles = remElectricMiles;
				gasMiles += tripMiles - tripElectricMiles;
				
				curRemBatKWh = 0f;
				curRelSoC = 0f;
			} else {
				float tripKWhUsed = tripMiles * kWhpm;
				curRemBatKWh += -tripKWhUsed;
				curRelSoC = curRemBatKWh/dModel.batSwingKWh;
			}
			
			curTripID++;
		}
		
		return gasMiles;
	}
	private static boolean checkIfBEVCanDoDay(KinematicOptChgPlanModel dModel, ChargingEvents.ChargingEventType[] chgPlan) {
		float curRelSoC = dModel.relSoCatDayStart;
		float curRemBatKWh = curRelSoC*dModel.batSwingKWh;
		
		int curTripID = 0;
		float curRemainingMiles = curRelSoC * dModel.nominalAER;
		float curAnxMiles = dModel.wiitMod.bevRepModel.bevRepWholeDay.calcRangeAnx(curRemainingMiles, 
				dModel.wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles[dModel.anxID]);
		float curSafeDrivingMiles = curRemainingMiles - curAnxMiles;
		float kWhpmAtAnxRate = dModel.batSwingKWh / curSafeDrivingMiles;
				
		//Check miles driven until charging event
		for (int i=0; i<chgPlan.length; i++) {
			int nextCandidateChgEventBeforeTripID = dModel.chgCandidateTripIDinDay[i];
			
			float milesUntilNextChgEvent = 0f;
			for (int j=curTripID; j<nextCandidateChgEventBeforeTripID; j++) milesUntilNextChgEvent += dModel.dayTripMiles[j];
			
			if (milesUntilNextChgEvent > curSafeDrivingMiles) {
				return false;
			}
			
			curRemBatKWh += -milesUntilNextChgEvent*kWhpmAtAnxRate;
			curRelSoC = curRemBatKWh/dModel.batSwingKWh;

			//Charging event adjusts SoC
			ChargingEvents.ChargingWindowEventResult chgRes = ChargingEvents.chgResult(chgPlan[i], dModel.wiitMod, 
					dModel.chgCandidateHrsWindow[i]*ChargingEvents.SecondsPerHour, curRelSoC, dModel.batSwingKWh);
			curRelSoC = chgRes.finalRelSoC;
			curRemBatKWh = curRelSoC*dModel.batSwingKWh;

			//Re-calculate safe driving range after the charging event 
			curRemainingMiles = curRelSoC * dModel.nominalAER;
			curAnxMiles = dModel.wiitMod.bevRepModel.bevRepWholeDay.calcRangeAnx(curRemainingMiles, 
					dModel.wiitMod.bevRepModel.bevRepWholeDay.choicesForRangeAnxAt100miles[dModel.anxID]);
			curSafeDrivingMiles = curRemainingMiles - curAnxMiles;
			curTripID = nextCandidateChgEventBeforeTripID;
		}
		
		//Check miles driven between last day-time charging event and last trip of the day
		float milesUntilDayEnds = 0f;
		for (int j=curTripID; j<dModel.dayTripMiles.length; j++) milesUntilDayEnds += dModel.dayTripMiles[j];
		
		if (milesUntilDayEnds > curSafeDrivingMiles) {
			return false;
		}
		
		return true;
	}
	
	private static int numDCFastChgEvents(ChargingEvents.ChargingEventType[] chgPlan) {
		if (chgPlan == null) return 0;
		
		int numEvents = 0;
		for (int i=0; i<chgPlan.length; i++) {
			if (chgPlan[i] != null) {
				switch (chgPlan[i]) {
				case dayTime_DCFast:
					numEvents++;
					break;
				default:
					break;					
				}
			}
		}
		return numEvents;
	}
	private static int numDaytimeL2ChgEvents(ChargingEvents.ChargingEventType[] chgPlan) {
		if (chgPlan == null) return 0;
		
		int numEvents = 0;
		for (int i=0; i<chgPlan.length; i++) {
			if (chgPlan[i] != null) {
				switch (chgPlan[i]) {
				case dayTime_L2:
					numEvents++;
					break;
				default:
					break;					
				}
			}
		}
		return numEvents;
	}
}
