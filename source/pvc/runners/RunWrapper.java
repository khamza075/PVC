package pvc.runners;

import pvc.gui.comp.RunStatusWindow;

public abstract class RunWrapper {
	
	//Implementation of this function should uphold...
	// 1- Posting progress updates to the status window 
	// 2- Keeping an eye on 'runAborted' and/or stWindow.abortRequested()
	protected abstract void actualRun(RunStatusWindow stWindow) throws Exception;
	
	//This should return the control to owner JFrame or JDialog
	public abstract void returnToOwnerWindow();
	
	//Derived classes ought to ensure super() is called, they should also 
	//	set data objects (likely including a handle to the owner JFrame or JDialog), 
	//	but NOT automatically start the run, rather stWindow.startRun() should be used
	protected RunWrapper() {
		runCompleted = false;
		runAborted = false;
		runHadError = false;
		errString = "";
	}	
	
	protected boolean runCompleted, runAborted, runHadError;
	public boolean runCompleted() {return runCompleted;}
	public boolean runAborted() {return runAborted;}
	public boolean runHadError() {return runHadError;}
	
	protected String errString;
	public String errString() {return errString;}

	
	public void abortCurrentRun() {runAborted = true;}
	public void doRun(RunStatusWindow stWindow) {
		try {
			actualRun(stWindow);
			
			if (!runAborted) runCompleted = true;					
			runHadError = false;
			errString = "";
			
		} catch (Exception e) {
			runCompleted = false;
			runAborted = false;
			runHadError = true;
			errString = e.toString();
			
			stWindow.runHadError();
		}
	}
}
