package pvc.utility;

public class AxesAutoScaling {
	private static final float ZTolMult = 0.0005f;
	private static final float LimOffsetIfEqual = 0.2f;
	
	private AxesAutoScaling() {}
	
	public static NumericAxisSetup autoScaleNumericAxis(float minValue, float maxValue, int maxNumScreenDiv) {
		NumericAxisSetup axis = new NumericAxisSetup();
		
		float min = minValue;
		float max = maxValue;
		if (min > max) {
			float t = max;
			max = min;
			min = t;
		}
		
		if (max == min) {
			//Attempt to offset limits
			if (max < 0) max *= (1f - LimOffsetIfEqual);
			else max *= (1f + LimOffsetIfEqual);
		}
		if (max == min) {
			//shouldn't be here unless both are zero
			max = 1f;
		}
		
		float zTol = ZTolMult*(max - min);
		float maxStepSize = snapToLowerPreferredStepSize(max - min);
		if ((min < -zTol) && (max > zTol)) {
			maxStepSize = Math.max(snapToLowerPreferredStepSize(-min), snapToLowerPreferredStepSize(max));
		} else if ((min < -zTol) && (Math.abs(max) <= zTol)) {
			maxStepSize = snapToLowerPreferredStepSize(-min);
		} else if ((max > zTol) && (Math.abs(min) <= zTol)) {
			maxStepSize = snapToLowerPreferredStepSize(max);
		}
				
		
		//Find the smallest step size that keeps number of divisions on screen within limit
		float curStepSize = maxStepSize;
		while (true) {
			float testStepSize = snapToLowerPreferredStepSize(0.8f*curStepSize);
			axis.reScale(min, max, testStepSize, 0);
			
			if ((axis.numTics-1) <= maxNumScreenDiv) {
				curStepSize = testStepSize;
			} else {
				break;
			}
		}
		
		//Adjust number of decimals
		float l10 = (float)Math.log10(curStepSize);
		int l10Exponent = (int)l10;
		if ((l10 < 0) && (Math.abs(l10Exponent-l10) > ZTolMult)) l10Exponent--;
		if (((curStepSize/Math.pow(10f, l10Exponent))- 1f) > ZTolMult) l10Exponent++;

		int numDecimals = 0;
		if (l10 < 2) {
			numDecimals = 2 - l10Exponent;
		}

		axis.reScale(min, max, curStepSize, numDecimals);		
		return axis;
	}
	private static float snapToLowerPreferredStepSize(float stepSize) {
		float l10 = (float)Math.log10(stepSize);
		int l10Exponent = (int)l10;
		if ((l10 < 0) && (Math.abs(l10Exponent-l10) > ZTolMult)) l10Exponent--;
		int firstDigit = (int)(stepSize/Math.pow(10f, l10Exponent));
		
		if (firstDigit >= 5) return 5f * (float)Math.pow(10f, l10Exponent);
		if (firstDigit >= 2) return 2f * (float)Math.pow(10f, l10Exponent);
		return (float)Math.pow(10f, l10Exponent);
	}
	
	
	
	//Class for axis setup
	public static class NumericAxisSetup {
		private float minValue;
		private float maxValue;
		private float ticStep;
		private int numDecimalDigits;
		
		private float firstTicValue;
		private int numTics;
		
		public float minValue() {return minValue;}
		public float maxValue() {return maxValue;}
		public float stepSize() {return ticStep;}
		public int numDecimals() {return numDecimalDigits;}
		
		public int valueToPixPos(int numScreenPix, float value) {
			float scPos = (value - minValue)/(maxValue - minValue);
			return (int)(scPos * (numScreenPix-1) + 0.5f);
		}
		public int numTics() {return numTics;}
		public float valueAtTic(int ticID) {
			return firstTicValue + ticID*ticStep;
		}
		
		//Note: TO AVOID ISSUES... before calling this function, must ensure that:
		// 1) maxVal > minVal
		// 2) stepSize > 0
		// 3.a) stepSize <= (maxVal - minVal) IF both minVal & maxVal have the same sign
		// 3.b) stepSize <= max(|minVal|, |maxVal|) IF minVal & maxVal have different signs
		public void reScale(float minVal, float maxVal, float stepSize, int numDecimals) {
			numDecimalDigits = numDecimals;
			minValue = minVal;
			maxValue = maxVal;
			ticStep = stepSize;
			
			float zTol = ZTolMult * (maxValue - minValue);
			
			if ((minValue < -zTol) && (maxValue > zTol)) {
				//Different signs... Set first tick so that zero value is at a tick
				int nDivNeg = (int)(-(minValue-zTol)/ticStep);
				int nDivPos = (int)((maxValue+zTol)/ticStep);
				
				firstTicValue = -nDivNeg * ticStep;
				numTics = nDivNeg + nDivPos + 1;
			}
			else if ((minValue < -zTol) && (Math.abs(maxValue) <= zTol)) {
				//All on negative size, going to zero
				int nDivNeg = (int)(-(minValue-zTol)/ticStep);
				
				firstTicValue = -nDivNeg * ticStep;
				numTics = nDivNeg + 1;
			} else if ((maxValue > zTol) && (Math.abs(minValue) <= zTol)) {
				//All on positive size, starting from zero
				int nDivPos = (int)((maxValue+zTol)/ticStep);
				
				firstTicValue = 0f;
				numTics = nDivPos + 1;
			} else {
				//All on one side, start at minimum
				firstTicValue = minValue;
				int nDiv = (int)((maxValue + zTol - minValue)/ticStep);
				numTics = nDiv + 1;
			}
		}
				
		private NumericAxisSetup() {}
		public NumericAxisSetup(NumericAxisSetup other) {
			minValue = other.minValue;
			maxValue = other.maxValue;
			ticStep = other.ticStep;
			numDecimalDigits = other.numDecimalDigits;
			
			firstTicValue = other.firstTicValue;
			numTics = other.numTics;
		}
		

		public NumericAxisSetup(String readLine) throws Exception {
			String[] sp = readLine.split(",");
			float minVal = Float.parseFloat(sp[0]);
			float maxVal = Float.parseFloat(sp[1]);
			float stepSize = Float.parseFloat(sp[2]);
			int numDecimals = Integer.parseInt(sp[3]);
			
			reScale(minVal, maxVal, stepSize, numDecimals);
		}
		@Override public String toString() {return ""+minValue+","+maxValue+","+ticStep+","+numDecimalDigits;}
	}

}
