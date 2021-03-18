package pvc.datamgmt.comp;

public class VSliderBarBaseModel {
	public float baseValue, minValue, maxValue;
	
	public VSliderBarBaseModel() {}
	public VSliderBarBaseModel(String readLine) {
		String[] sp = readLine.split(",");
		baseValue = Float.parseFloat(sp[0]);
		minValue = Float.parseFloat(sp[1]);
		maxValue = Float.parseFloat(sp[2]);
	}
	public VSliderBarBaseModel(VSliderBarBaseModel other) {
		baseValue = other.baseValue;
		minValue = other.minValue;
		maxValue = other.maxValue;
	}
	public VSliderBarBaseModel(float base, float min, float max) {
		baseValue = base;
		minValue = min;
		maxValue = max;
	}
	
	@Override public String toString() {
		return ""+baseValue+","+minValue+","+maxValue;
	}
	
	public boolean isSlidable() {
		float zTol = Math.max(Math.max(0.0001f*Math.abs(baseValue), 0.000001f*(maxValue-minValue)), 0.00001f);
		if ((baseValue - minValue) > zTol) return true;
		if ((maxValue - baseValue) > zTol) return true;
		return false;
	}
	public boolean canHaveHigherValuesThanBaseline() {
		float zTol = Math.max(Math.max(0.0001f*Math.abs(baseValue), 0.000001f*(maxValue-minValue)), 0.00001f);
		if ((maxValue - baseValue) > zTol) return true;
		return false;
	}
	public boolean canHaveLowerValuesThanBaseline() {
		float zTol = Math.max(Math.max(0.0001f*Math.abs(baseValue), 0.000001f*(maxValue-minValue)), 0.00001f);
		if ((baseValue - minValue) > zTol) return true;
		return false;
	}
}