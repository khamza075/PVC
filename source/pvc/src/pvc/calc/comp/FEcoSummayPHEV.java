package pvc.calc.comp;

import java.io.BufferedReader;

public class VehDepreciation {
	//Residual Values Curve
	private ResidualValuePoint[] resValueCurvePoints;
	
	//Tuning for how a low-mileage vehicle can have slightly better re-sale value
	private float lowMileageReducesDeprByUpTo, lowMileageAchievedAtFracExpectedMilesOf;
	//Tuning for how a high-mileage vehicle can have slightly worse re-sale value
	private float highMileageIncreasesDeprByUpTo, highMileageAchievedAtFracExpectedMilesOf;
	
	public float lowMileageReducesDeprByUpTo() {return lowMileageReducesDeprByUpTo;}
	public float lowMileageAchievedAtFracExpectedMilesOf() {return lowMileageAchievedAtFracExpectedMilesOf;}
	public float highMileageIncreasesDeprByUpTo() {return highMileageIncreasesDeprByUpTo;}
	public float highMileageAchievedAtFracExpectedMilesOf() {return highMileageAchievedAtFracExpectedMilesOf;}
	
	public float[][] getCurveDataAsMatrix() {
		float[][] arr = new float[3][resValueCurvePoints.length];
		for (int i=0; i<resValueCurvePoints.length; i++) {
			arr[0][i] = resValueCurvePoints[i].numYears;
			arr[1][i] = resValueCurvePoints[i].expectedMiles;
			arr[2][i] = resValueCurvePoints[i].residualValue;
		}
		return arr;
	}
	public void setData(float[][] curveDataMatrix, float lowMiResaleImpr, float lowMiAt, float highMiResaleRedc, float highMiAt) {
		lowMileageReducesDeprByUpTo = lowMiResaleImpr;
		lowMileageAchievedAtFracExpectedMilesOf = lowMiAt;
		
		highMileageIncreasesDeprByUpTo = highMiResaleRedc;
		highMileageAchievedAtFracExpectedMilesOf = highMiAt;
		
		resValueCurvePoints = new ResidualValuePoint[curveDataMatrix[0].length];
		for (int i=0; i<resValueCurvePoints.length; i++) {
			resValueCurvePoints[i] = new ResidualValuePoint();
			resValueCurvePoints[i].numYears = curveDataMatrix[0][i];
			resValueCurvePoints[i].expectedMiles = curveDataMatrix[1][i];
			resValueCurvePoints[i].residualValue = curveDataMatrix[2][i];
		}
	}
	
	//Function to calculate residual value
	public float residualValue(float numYears, float mileageAtSale) {
		if ((resValueCurvePoints.length < 2) || (numYears <= resValueCurvePoints[0].numYears)) {
			//One point only or Multiple Points but before first point
			float annualDeprRate = (float)Math.exp(Math.log(resValueCurvePoints[0].residualValue)/resValueCurvePoints[0].numYears);
			float baseResidual = (float)Math.pow(annualDeprRate, numYears);
			
			float expectedMilesPerYear = resValueCurvePoints[0].expectedMiles / resValueCurvePoints[0].numYears;
			float expectedMiles = numYears * expectedMilesPerYear;
			float fracExpMiles = mileageAtSale/expectedMiles;
			return mileageAdjustedResidual(baseResidual, fracExpMiles);
		}
		
		float lastNumYears = resValueCurvePoints[resValueCurvePoints.length-1].numYears;
		if (numYears >= lastNumYears) {
			//Multiple Points, beyond last point, and there are more than one point
			float beforeLastResidualValue = resValueCurvePoints[resValueCurvePoints.length-2].residualValue;
			float lastResidualValue = resValueCurvePoints[resValueCurvePoints.length-1].residualValue;
			float beforeLastNumYears = resValueCurvePoints[resValueCurvePoints.length-2].numYears;
			float lastAnnualDeprRate = (float)Math.exp((Math.log(lastResidualValue) - Math.log(beforeLastResidualValue))/(lastNumYears - beforeLastNumYears));
			
			float baseResidual = lastResidualValue*(float)Math.pow(lastAnnualDeprRate, numYears-lastNumYears);
			
			float lastMiles = resValueCurvePoints[resValueCurvePoints.length-1].expectedMiles;
			float beforeLastMiles = resValueCurvePoints[resValueCurvePoints.length-2].expectedMiles;
			float lastMilesPerYear = (lastMiles - beforeLastMiles)/(lastNumYears - beforeLastNumYears);
			float expectedMiles = lastMiles + (numYears - lastNumYears)*lastMilesPerYear;

			float fracExpMiles = mileageAtSale/expectedMiles;
			return mileageAdjustedResidual(baseResidual, fracExpMiles);
		}		
		
		//Multiple Points internal point
		int pointAheadInYears = 1;
		while (pointAheadInYears < resValueCurvePoints.length) {
			if (resValueCurvePoints[pointAheadInYears].numYears >= numYears) break;			
			pointAheadInYears++;
		}
		if (pointAheadInYears >= resValueCurvePoints.length) pointAheadInYears = resValueCurvePoints.length - 1;
		
		float pointAheadYears = resValueCurvePoints[pointAheadInYears].numYears;
		float pointBeforeYears = resValueCurvePoints[pointAheadInYears-1].numYears;
		float aheadResidual = resValueCurvePoints[pointAheadInYears].residualValue;
		float prevResidual = resValueCurvePoints[pointAheadInYears-1].residualValue;
		float curSegmentDeprRate = (float)Math.exp((Math.log(aheadResidual) - Math.log(prevResidual))/(pointAheadYears - pointBeforeYears));
		
		float baseResidual = prevResidual*(float)Math.pow(curSegmentDeprRate, numYears-pointBeforeYears);

		float pointAheadMiles = resValueCurvePoints[pointAheadInYears].expectedMiles;
		float pointBeforeMiles = resValueCurvePoints[pointAheadInYears-1].expectedMiles;
		float curSegmentMilesPerYear = (pointAheadMiles - pointBeforeMiles)/(pointAheadYears - pointBeforeYears);
		float expectedMiles = pointBeforeMiles + (numYears - pointBeforeYears)*curSegmentMilesPerYear;

		float fracExpMiles = mileageAtSale/expectedMiles;
		return mileageAdjustedResidual(baseResidual, fracExpMiles);
	}
	private float mileageAdjustedResidual(float baseResidual, float fracExpMiles) {		
		if (fracExpMiles < 1f) {
			float deprReduction = lowMileageReducesDeprByUpTo;
			if (fracExpMiles > lowMileageAchievedAtFracExpectedMilesOf) {
				float x = (fracExpMiles - lowMileageAchievedAtFracExpectedMilesOf)/(1f - lowMileageAchievedAtFracExpectedMilesOf);
				deprReduction *= (1f - x);
			}
			
			float baseDepr = 1f - baseResidual;
			float adjustedDepr = baseDepr * (1f - deprReduction);
			return Math.min(baseResidual*(1f+deprReduction), 1f - adjustedDepr);
		}
		if (fracExpMiles > 1f) {
			float deprIncrease = highMileageIncreasesDeprByUpTo;
			if (fracExpMiles < highMileageAchievedAtFracExpectedMilesOf) {
				float x = (fracExpMiles - 1f)/(highMileageAchievedAtFracExpectedMilesOf - 1f);
				deprIncrease *= x;
			}
			
			return (1f - deprIncrease)*baseResidual;
		}
		
		return baseResidual;
	}
	
	public VehDepreciation() {
		lowMileageReducesDeprByUpTo = 0.2f;
		lowMileageAchievedAtFracExpectedMilesOf = 0.5f;
		
		highMileageIncreasesDeprByUpTo = 0.2f;
		highMileageAchievedAtFracExpectedMilesOf = 1.5f;
		
		resValueCurvePoints = new ResidualValuePoint[2];
		
		resValueCurvePoints[0] = new ResidualValuePoint();
		resValueCurvePoints[0].numYears = 3f;
		resValueCurvePoints[0].residualValue = 0.65f;
		resValueCurvePoints[0].expectedMiles = 41400f;
		
		resValueCurvePoints[1] = new ResidualValuePoint();
		resValueCurvePoints[1].numYears = 5f;
		resValueCurvePoints[1].residualValue = 0.5f;
		resValueCurvePoints[1].expectedMiles = 66000f;
	}
	
	public VehDepreciation(VehDepreciation other) {
		lowMileageReducesDeprByUpTo = other.lowMileageReducesDeprByUpTo;
		lowMileageAchievedAtFracExpectedMilesOf = other.lowMileageAchievedAtFracExpectedMilesOf;
		
		highMileageIncreasesDeprByUpTo = other.highMileageIncreasesDeprByUpTo;
		highMileageAchievedAtFracExpectedMilesOf = other.highMileageAchievedAtFracExpectedMilesOf;
		
		resValueCurvePoints = new ResidualValuePoint[other.resValueCurvePoints.length];
		for (int i=0; i<resValueCurvePoints.length; i++) resValueCurvePoints[i] = new ResidualValuePoint(other.resValueCurvePoints[i]);
	}
	
	public VehDepreciation(BufferedReader fin) throws Exception {
		String readLine = fin.readLine();
		String [] sp = readLine.split(",");
		
		int nPoints = Integer.parseInt(sp[0]);
		lowMileageReducesDeprByUpTo = Float.parseFloat(sp[1]);
		lowMileageAchievedAtFracExpectedMilesOf = Float.parseFloat(sp[2]);
		highMileageIncreasesDeprByUpTo = Float.parseFloat(sp[3]);
		highMileageAchievedAtFracExpectedMilesOf = Float.parseFloat(sp[4]);
		
		resValueCurvePoints = new ResidualValuePoint[nPoints];
		for (int i=0; i<nPoints; i++) {
			readLine = fin.readLine();
			resValueCurvePoints[i] = new ResidualValuePoint(readLine);
		}
	}
	
	public static class ResidualValuePoint {
		private float numYears,residualValue,expectedMiles;
		private ResidualValuePoint() {}
		private ResidualValuePoint(ResidualValuePoint other) {
			numYears = other.numYears;
			residualValue =other.residualValue;
			expectedMiles = other.expectedMiles;
		}
		private ResidualValuePoint(String readLine) {
			String[] sp = readLine.split(",");
			numYears = Float.parseFloat(sp[0]);
			residualValue = Float.parseFloat(sp[1]);
			expectedMiles = Float.parseFloat(sp[2]);
		}
		@Override public String toString() {return ""+numYears+","+residualValue+","+expectedMiles;}
	}

	@Override public String toString() {
		String lsep = System.getProperty("line.separator");
		String st = ""+resValueCurvePoints.length+","+lowMileageReducesDeprByUpTo+","+lowMileageAchievedAtFracExpectedMilesOf+
				","+highMileageIncreasesDeprByUpTo+","+highMileageAchievedAtFracExpectedMilesOf;
		
		for (int i=0; i<resValueCurvePoints.length; i++) {
			st = st + lsep + resValueCurvePoints[i];
		}
		
		return st;
	}
}
