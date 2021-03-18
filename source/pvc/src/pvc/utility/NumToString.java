package pvc.utility;

public class NumToString {
	private NumToString() {}
	
	public static String floatWNumDecimals(float value, int numDecimals) {
		if (value < 0) return "-"+posFloatWNumDecimals(-value, numDecimals);
		return posFloatWNumDecimals(value, numDecimals);
	}
	public static String posFloatWNumDecimals(float value, int numDecimals) {
		if (numDecimals < 1) {
			long num = (long)(value + 0.5f);
			return ""+num;
		}
		
		long mult = 1;
		for (int i=0; i<numDecimals; i++) mult *= 10;
		
		long num = (long)(mult*value + 0.5f);
		long dec = num % mult;
		long rem = num / mult;
		
		String stDec = ""+dec;
		while (stDec.length() < numDecimals) stDec = "0" + stDec;
		return ""+rem+"."+stDec;
	}
}
