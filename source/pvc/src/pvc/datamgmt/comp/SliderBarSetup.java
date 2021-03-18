package pvc.datamgmt.comp;

import java.io.BufferedReader;
import java.io.FileWriter;

public class SliderBarSetup {
	private APSliderBars mDesignation;
	
	public APSliderBars mDesignation() {return mDesignation;}
	public String captionTitle() {return mDesignation.caption;}
	
	
	private VSliderBarBaseModel baseModel;
	
	public float baseValue() {return baseModel.baseValue;}
	public boolean isEditable() {return baseModel.isSlidable();}
	public boolean isSlidable() {
		if (oneArrayValues.length > 1) return true;
		return false;
	}
	public boolean canIncrease() {return baseModel.canHaveHigherValuesThanBaseline();}
	public boolean canDecrease() {return baseModel.canHaveLowerValuesThanBaseline();}
	public float minLimit() {return baseModel.minValue;}
	public float maxLimit() {return baseModel.maxValue;}

	private float[] valuesBelow, valuesAbove, oneArrayValues;
	private int oneArrayBaselineID;
	
	public int baselineIDinArray() {return oneArrayBaselineID;}
	public int numDiscreteLevels() {return oneArrayValues.length;}
	
	public SliderBarSetup(APSliderBars designation, VSliderBarBaseModel bModel) {
		mDesignation = designation;
		baseModel = new VSliderBarBaseModel(bModel);
		
		if (canDecrease()) {
			int numValuesToAdd = 2;
			float delta = (baseModel.baseValue - baseModel.minValue)/((float)numValuesToAdd);
			valuesBelow = new float[numValuesToAdd];
			for (int i=0; i<valuesBelow.length; i++) valuesBelow[i] = baseModel.baseValue - (numValuesToAdd-i)*delta;
		} else {
			valuesBelow = new float[0];
		}
		
		if (canIncrease()) {
			int numValuesToAdd = 2;
			float delta = (baseModel.maxValue - baseModel.baseValue)/((float)numValuesToAdd);
			valuesAbove = new float[numValuesToAdd];
			for (int i=0; i<valuesAbove.length; i++) valuesAbove[i] = baseModel.baseValue + (i+1)*delta;
		} else {
			valuesAbove = new float[0];
		}
		
		formOneArray();
	}
	public SliderBarSetup(String readLineOfDesignation, BufferedReader fin) throws Exception {
		mDesignation = APSliderBars.decode(readLineOfDesignation);
		if (mDesignation == null) throw new Exception("Invalid Designation for Slider Bar Model");
		
		String readLine = fin.readLine();
		baseModel = new VSliderBarBaseModel(readLine);
		
		readLine = fin.readLine();
		String[] sp = readLine.split(",");
		
		int numBelow = Integer.parseInt(sp[0]);
		valuesBelow = new float[numBelow];
		for (int i=0; i<valuesBelow.length; i++) valuesBelow[i] = Float.parseFloat(sp[i+1]);
		
		readLine = fin.readLine();
		sp = readLine.split(",");
		int numAbove = Integer.parseInt(sp[0]);
		valuesAbove = new float[numAbove];
		for (int i=0; i<valuesAbove.length; i++) valuesAbove[i] = Float.parseFloat(sp[i+1]);

		formOneArray();
	}
	public void writeToFileStream(FileWriter fout) throws Exception {
		String lsep = System.getProperty("line.separator");
		
		fout.append(mDesignation.toString()+lsep);
		fout.append(baseModel.toString()+lsep);
		
		fout.append(""+valuesBelow.length);
		for (int i=0; i<valuesBelow.length; i++) fout.append(","+valuesBelow[i]);
		fout.append(lsep);
		
		fout.append(""+valuesAbove.length);
		for (int i=0; i<valuesAbove.length; i++) fout.append(","+valuesAbove[i]);
		fout.append(lsep);
	}	
	
	public void editValues(float[] vBelow, float[] vAbove) {
		if (canDecrease()) {
			valuesBelow = new float[vBelow.length];
			for (int i=0; i<valuesBelow.length; i++) valuesBelow[i] = vBelow[i];
		} else {
			valuesBelow = new float[0];
		}
		if (canIncrease()) {
			valuesAbove = new float[vAbove.length];
			for (int i=0; i<valuesAbove.length; i++) valuesAbove[i] = vAbove[i];
		} else {
			valuesAbove = new float[0];
		}
		
		formOneArray();
	}
	
	private void formOneArray() {
		//From the valuesBelow & valuesAbove
		oneArrayValues = new float[valuesBelow.length + 1 + valuesAbove.length];
		oneArrayBaselineID = valuesBelow.length;
		for (int i=0; i<valuesBelow.length; i++) oneArrayValues[i] = valuesBelow[i];
		oneArrayValues[oneArrayBaselineID] = baseModel.baseValue;
		for (int i=0; i<valuesAbove.length; i++) oneArrayValues[oneArrayBaselineID+1+i] = valuesAbove[i];
	}
	
	public float getDValue(int dID) {
		if (oneArrayValues.length < 2) return oneArrayValues[0];
		if (dID < 0) return oneArrayValues[0];
		if (dID >= oneArrayValues.length) return oneArrayValues[oneArrayValues.length-1];
		return oneArrayValues[dID];
	}
	public float getCValue(float cID) {
		if (oneArrayValues.length < 2) return oneArrayValues[0];
		if (cID < 0) return oneArrayValues[0];
		if (cID >= oneArrayValues.length) return oneArrayValues[oneArrayValues.length-1];

		final float snapToTol = 0.01f;
		int snapToID = (int)(cID + 0.5f);
		if (Math.abs(snapToID - cID) <= snapToTol) return oneArrayValues[snapToID];
		
		int id1 = (int)cID;
		int id2 = id1+1;
		float c2 = cID - id1;
		float c1 = 1f - c2;
		
		return c1*oneArrayValues[id1] + c2*oneArrayValues[id2];
	}
}
