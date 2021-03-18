package pvc.datamgmt.comp;

public class ProfileCurve {
	public float[] x,y;
	public float calcY(float xValue) {
		if (x == null) return 0f;
		if (x.length < 1) return 0f;
		if (x.length == 1) return y[0];
		
		if (xValue <= x[0]) return y[0];
		if (xValue >= x[x.length-1]) return y[x.length-1];
		
		float x1 = x[0];
		float y1 = y[0];
		float x2 = x[1];
		float y2 = y[1];
		int iid = 0;
		while (xValue > x2) {
			iid++;
			x1 = x2;
			y1 = y2;
			x2 = x[iid+1];
			y2 = y[iid+1];
		}
		float c2 = (xValue - x1)/(x2 - x1);
		float c1 = 1f - c2;
		return c1*y1 + c2*y2;
	}
	
	public ProfileCurve(float[] cx, float[] cy) {
		x = new float[cx.length];
		y = new float[x.length];
		for (int i=0; i<x.length; i++) {
			x[i] = cx[i];
			y[i] = cy[i];
		}
	}
	public ProfileCurve(ProfileCurve other) {
		x = new float[other.x.length];
		y = new float[x.length];
		for (int i=0; i<x.length; i++) {
			x[i] = other.x[i];
			y[i] = other.y[i];
		}
	}
	public ProfileCurve(String line1, String line2) {
		String[] sp1 = line1.split(",");
		String[] sp2 = line2.split(",");
		
		x = new float[sp1.length];
		y = new float[x.length];
		for (int i=0; i<x.length; i++) {
			x[i] = Float.parseFloat(sp1[i]);
			y[i] = Float.parseFloat(sp2[i]);
		}
	}
}