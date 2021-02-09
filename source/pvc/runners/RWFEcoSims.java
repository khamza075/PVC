package pvc.runners;

import javax.swing.JOptionPane;

import pvc.calc.FEcoSimsGenerator;
import pvc.calc.comp.VehicleSampleMA;
import pvc.gui.*;
import pvc.gui.comp.RunStatusWindow;

public class RWFEcoSims extends RunWrapper {
	
	private static final int ThreadSleepMS = 100;
	
	private MainPanelGUI pMP;
	private FEcoSimsGenerator fecoGen;
	private int sID, nThreads;
	private VehicleSampleMA[] vs;
	private String title, description;
	private float annualVMT, annualDriveDays;
	
	public RWFEcoSims(MainPanelGUI pMainWindow, FEcoSimsGenerator fecoGenerator, int newFEcoSimID, VehicleSampleMA[] vehSamples,
			String srTitle, String srDescription, float srAnnualVMT, float srAnnualDriveDays, int numThreads) {
		super();
		
		pMP = pMainWindow;
		fecoGen = fecoGenerator;
		sID = newFEcoSimID;
		vs = vehSamples;
		
		nThreads = numThreads;
		title = new String(srTitle);
		description = new String(srDescription);
		annualVMT = srAnnualVMT;
		annualDriveDays = srAnnualDriveDays;
		
		alreadyReturned = false;
	}
	
	private boolean alreadyReturned;

	@Override
	public void returnToOwnerWindow() {
		if (alreadyReturned) return;
		alreadyReturned = true;
		
		if (runHadError) {
			JOptionPane.showMessageDialog(null, "An Error Occurred during Fuel Economy Simulations", 
					"Run Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
		} else if (runAborted) {
			JOptionPane.showMessageDialog(null, "Fuel Economy Simulations was NOT completed due to User-Abort", 
					"Run Aborted", JOptionPane.OK_OPTION|JOptionPane.WARNING_MESSAGE);
		}
		else {
			fecoGen.genFecoSimSummaryFile(sID, title, description, annualVMT, annualDriveDays);
			JOptionPane.showMessageDialog(null, "Fuel Economy Simulations Complete", 
					"Run Complete", JOptionPane.OK_OPTION|JOptionPane.INFORMATION_MESSAGE);
		}
		
		new FecoSimGUI(fecoGen.fs(), fecoGen.aID(), pMP);
	}
	public void createSimSummaryFile() {
		fecoGen.genFecoSimSummaryFile(sID, title, description, annualVMT, annualDriveDays);
	}
	
	@Override
	protected void actualRun(RunStatusWindow stWindow) throws Exception {
		//Make sure folder is created
		fecoGen.ensureSolFolderExists(sID);
		
		//Generate trip compact summaries
		fecoGen.genCompactTripSummaries(sID, vs, stWindow);
		
		int numCases = fecoGen.numCases();
		int npThreads = Math.min(numCases, nThreads);
		PThreadContainer[] pt = new PThreadContainer[npThreads];
		for (int i=0; i<pt.length; i++) pt[i] = new PThreadContainer(stWindow);
		
		//Run
		int numCompleted = 0;
		int lastLaunched = -1;
		
		//Loop for running threads until last case has been launched
		while (numCompleted < numCases) {
			for (int i=0; i<pt.length; i++) {
				//Initiating new run after one has finished
				if (pt[i].taskCompleted) {
					if (pt[i].waitForRequest) {
						numCompleted++;
						
						if (lastLaunched < numCases-1) {
							lastLaunched++;
							pt[i].doSimulation(lastLaunched);
						}
					}
					else {
						//Cannot launch any more new runs because none are remaining
						pt[i].waitForRequest = false;
					}
				} else if (!pt[i].isRunning) {
					//Initiating runs on first entry
					if (pt[i].waitForRequest) {
						lastLaunched++;
						pt[i].doSimulation(lastLaunched);
					}
				}
			}
			
			if (stWindow.abortRequested()) {
				for (int i=0; i<pt.length; i++) pt[i].waitForRequest = false;
				return;
			}
			
			if (runHadError) {
				for (int i=0; i<pt.length; i++) pt[i].waitForRequest = false;
			}
			
			try {
				Thread.sleep(ThreadSleepMS);
			} catch (InterruptedException e) {}
		}
		
		//Wait until all running threads have finished
		boolean allThreadsFinished = false;
		while (!allThreadsFinished) {
			allThreadsFinished = true;
			for (int i=0; i<pt.length; i++) allThreadsFinished = allThreadsFinished && pt[i].taskCompleted;
			
			try {
				Thread.sleep(ThreadSleepMS);
			} catch (InterruptedException e) {}
		}
	}
	
	private class PThreadContainer {
		private int caseIDtoRun;
		private boolean waitForRequest, isRunning, taskCompleted;
		private RunStatusWindow stWin;
		
		private PThreadContainer(RunStatusWindow stWindow) {
			stWin = stWindow;
			
			waitForRequest = true;
			isRunning = false;
			taskCompleted = false;			

			Thread t = new LocalRunner();
			t.setDaemon(true);
			t.start();
		}
		
		private void doSimulation(int cID) {
			if (isRunning) return;
			
			caseIDtoRun = cID;
			taskCompleted = false;
			isRunning = true;
		}

		
		private class LocalRunner extends Thread {
			public LocalRunner() {}
			
			@Override public void run() {
				while (waitForRequest) {
					if (isRunning) {
						//Invoke run
						try {
							fecoGen.genFuelEcoSimCase(sID, caseIDtoRun, vs, stWin);
							
							//Wait till another assignment
							taskCompleted = true;
							isRunning = false;
						} catch (Exception e) {
							errString = e.toString();
							runHadError = true;
							stWin.runHadError();
						}
					}					
					try {
						sleep(ThreadSleepMS);
					} catch (InterruptedException e) {}
				}
			}
		}
	}
}
