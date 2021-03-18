package pvc.runners;

import pvc.calc.comp.VehicleSampleMA;
import pvc.gui.NewFecoSimGUI;
import pvc.gui.comp.RunStatusWindow;

public class RWReadVehiclesDrivingData extends RunWrapper {
	
	private String vehSamplesFullFilePath;
	private NewFecoSimGUI pOW;
	
	private VehicleSampleMA[] vSamples;
	public VehicleSampleMA[] vSamples() {return vSamples;}

	
	public RWReadVehiclesDrivingData(NewFecoSimGUI pOwnerWindow, String vehSamplesFile) {
		super();
		
		pOW = pOwnerWindow;
		vehSamplesFullFilePath = new String(vehSamplesFile);
		vSamples = null;
	}

	@Override
	protected void actualRun(RunStatusWindow stWindow) throws Exception {
		vSamples = VehicleSampleMA.readArrayFromFile(vehSamplesFullFilePath, stWindow);
	}

	@Override
	public void returnToOwnerWindow() {
		if (runAborted||runHadError) vSamples = null;
		pOW.vehSamplesReadingFinished();
	}

}
