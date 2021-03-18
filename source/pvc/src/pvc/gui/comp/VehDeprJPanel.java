package pvc.gui.comp;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import pvc.calc.comp.VehDepreciation;
import pvc.datamgmt.comp.DUnits;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class VehDeprJPanel extends JPanel implements ChangeListener {
	//Constants
	private static final int NumMatrixFields = 3;
	private static final float FracToPercentMult = 100f;
	
	private static final int MaxNumDeprPoints = 8;
	private static final int MaxVisNumDeprPoints = 3;

	private static final int TitleFontSize = 14;
	private static final int RegFontSize = 11;
	
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int TClear = 2;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	
	private static final int EdtYearsWidth = 85;
	private static final int EdtMileageWidth = 135;
	private static final int EdtResidualWidth = 105;
	private static final int LblPercentWidth = 10;
	
	private static final int ScrollAllowanceWidth = 20;
	private static final int PrefSubPanelWidth = TMargin*4 + EdtYearsWidth + EdtMileageWidth + EdtResidualWidth + TClear + LblPercentWidth;
	private static final int ScrollWidth = PrefSubPanelWidth + ScrollAllowanceWidth;
	
	private static final int EdtMilesValueAdjustWidth = 42;
	private static final int EdtMilesValueAdjustCaptionWidth1 = 145;
	private static final int EdtMilesValueAdjustCaptionWidth2 = 24;
	private static final int EdtMilesValueAdjustCaptionWidth3 = ScrollWidth - 
			(EdtMilesValueAdjustWidth*2 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustCaptionWidth2 + TClear*4);
	
	private static final int PanelWidth = ScrollWidth + TMargin*4;

	private static final int PrefSubPanelHeight = (EdtHeight+TMargin)*MaxNumDeprPoints + TMargin;
	private static final int ScrollHeight = (EdtHeight+TMargin)*MaxVisNumDeprPoints + TMargin;
	
	
	//Data and GUI Objects
	private int numVisibleCurvePoints;

	private JSpinner spnNumVisPoints;
	private JTextField[][] edtCurvePointsMatrix;
	private JLabel[] lblPercent;
	private JTextField edtLowMileageAdj, edtLowMileageFrac, edtHighMileageAdj, edtHighMileageFrac;
	
	private VehDepreciationDialog pDlg;

	public VehDeprJPanel(VehDepreciation vehDep, String vehShortName, ChangeListener chgListener, VehDepreciationDialog parentDialog) {
		super();
		setLayout(null);
		setOpaque(true);
		
		pDlg = parentDialog;
		
		int cx = TMargin;
		int cy = 0;
		
		JLabel lbl1 = new JLabel(vehShortName);
		lbl1.setSize(PanelWidth-TMargin*2, EdtHeight);
		lbl1.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		add(lbl1);
		
        Font titleFont = new Font(lbl1.getFont().getName(), Font.BOLD, TitleFontSize);
        Font lblFont = new Font(lbl1.getFont().getName(), Font.BOLD, RegFontSize);
        Font edtFont = new Font(lbl1.getFont().getName(), Font.PLAIN, RegFontSize);
        lbl1.setFont(titleFont);
        
        cx += TMargin;
        cy += LineSpacing - TMargin;
        
		JLabel lbl2 = new JLabel("Residual Value Curve");
		lbl2.setSize(PanelWidth-TMargin*3, LblHeight);
		lbl2.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		add(lbl2);
        
        cx += TMargin;
        cy += EdtHeight + TMargin;
        
        int spnWidth = EdtYearsWidth;
        int lbl3Width = lbl2.getWidth() - TMargin - spnWidth - TClear;
        
		JLabel lbl3 = new JLabel("Number of Curve Data Points");
		lbl3.setSize(lbl3Width, LblHeight);
		lbl3.setLocation(cx, cy + (EdtHeight-LblHeight)/2);
		lbl3.setFont(lblFont);
		add(lbl3);

		float[][] curveDataMatrix = vehDep.getCurveDataAsMatrix();
		numVisibleCurvePoints = curveDataMatrix[0].length;
		
        SpinnerNumberModel spmNumVisPoints = new SpinnerNumberModel(numVisibleCurvePoints, 1, MaxNumDeprPoints, 1);
        
        spnNumVisPoints = new JSpinner(spmNumVisPoints);
        spnNumVisPoints.setSize(spnWidth, EdtHeight);
        spnNumVisPoints.setLocation(cx + lbl3Width + TClear, cy);
        spnNumVisPoints.setFont(edtFont);
        spnNumVisPoints.addChangeListener(chgListener);
        spnNumVisPoints.addChangeListener(this);
		add(spnNumVisPoints);

        cy += EdtHeight + TMargin;

		JLabel lbl4 = new JLabel("Years");
		lbl4.setSize(EdtYearsWidth, LblHeight);
		lbl4.setLocation(cx + TMargin, cy + (EdtHeight-LblHeight)/2);
		lbl4.setHorizontalAlignment(SwingConstants.CENTER);
		lbl4.setFont(lblFont);
		add(lbl4);
		
		String mileageCaption = "Typical Mileage (" + DUnits.getShortName(DUnits.UnitType.Distance)+")";
		JLabel lbl5 = new JLabel(mileageCaption);
		lbl5.setSize(EdtMileageWidth, LblHeight);
		lbl5.setLocation(cx + TMargin*2 + EdtYearsWidth, cy + (EdtHeight-LblHeight)/2);
		lbl5.setHorizontalAlignment(SwingConstants.CENTER);
		lbl5.setFont(lblFont);
		add(lbl5);
		
		JLabel lbl6 = new JLabel("Residual Value");
		lbl6.setSize(EdtResidualWidth, LblHeight);
		lbl6.setLocation(cx + TMargin*3 + EdtYearsWidth + EdtMileageWidth, cy + (EdtHeight-LblHeight)/2);
		lbl6.setHorizontalAlignment(SwingConstants.CENTER);
		lbl6.setFont(lblFont);
		add(lbl6);
		
        cy += EdtHeight + TMargin/2;
        
        JPanel sp = new JPanel();
        sp.setLayout(null);
        sp.setPreferredSize(new Dimension(PrefSubPanelWidth, PrefSubPanelHeight));
        
        edtCurvePointsMatrix = new JTextField[NumMatrixFields][MaxNumDeprPoints];
        lblPercent = new JLabel[MaxNumDeprPoints];
        
        for (int i=0; i<MaxNumDeprPoints; i++) {
        	edtCurvePointsMatrix[0][i] = new JTextField();
        	edtCurvePointsMatrix[0][i].setSize(EdtYearsWidth, EdtHeight);
        	edtCurvePointsMatrix[0][i].setLocation(TMargin, TMargin + (EdtHeight+TMargin)*i);
        	edtCurvePointsMatrix[0][i].setHorizontalAlignment(SwingConstants.RIGHT);
        	edtCurvePointsMatrix[0][i].setFont(edtFont);
        	edtCurvePointsMatrix[0][i].setVisible(false);
    		sp.add(edtCurvePointsMatrix[0][i]);

        	edtCurvePointsMatrix[1][i] = new JTextField();
        	edtCurvePointsMatrix[1][i].setSize(EdtMileageWidth, EdtHeight);
        	edtCurvePointsMatrix[1][i].setLocation(TMargin*2 + EdtYearsWidth, TMargin + (EdtHeight+TMargin)*i);
        	edtCurvePointsMatrix[1][i].setHorizontalAlignment(SwingConstants.RIGHT);
        	edtCurvePointsMatrix[1][i].setFont(edtFont);
        	edtCurvePointsMatrix[1][i].setVisible(false);
    		sp.add(edtCurvePointsMatrix[1][i]);

        	edtCurvePointsMatrix[2][i] = new JTextField();
        	edtCurvePointsMatrix[2][i].setSize(EdtResidualWidth, EdtHeight);
        	edtCurvePointsMatrix[2][i].setLocation(TMargin*3 + EdtYearsWidth + EdtMileageWidth, TMargin + (EdtHeight+TMargin)*i);
        	edtCurvePointsMatrix[2][i].setHorizontalAlignment(SwingConstants.RIGHT);
        	edtCurvePointsMatrix[2][i].setFont(edtFont);
        	edtCurvePointsMatrix[2][i].setVisible(false);
    		sp.add(edtCurvePointsMatrix[2][i]);

    		lblPercent[i] = new JLabel("%");
    		lblPercent[i].setSize(LblPercentWidth, LblHeight);
    		lblPercent[i].setLocation(TMargin*3 + EdtYearsWidth + EdtMileageWidth + EdtResidualWidth + TClear, 
    				TMargin + (EdtHeight+TMargin)*i + (EdtHeight-LblHeight)/2);
    		lblPercent[i].setFont(lblFont);
    		lblPercent[i].setVisible(false);
    		sp.add(lblPercent[i]);
        	
        	if (i < numVisibleCurvePoints) {
            	edtCurvePointsMatrix[0][i].setText(NumToString.floatWNumDecimals(curveDataMatrix[0][i], 1));
            	edtCurvePointsMatrix[0][i].setVisible(true);

            	edtCurvePointsMatrix[1][i].setText(NumToString.floatWNumDecimals(curveDataMatrix[1][i]/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance), 0));
            	edtCurvePointsMatrix[1][i].setVisible(true);

            	edtCurvePointsMatrix[2][i].setText(NumToString.floatWNumDecimals(curveDataMatrix[2][i]*FracToPercentMult, 1));
            	edtCurvePointsMatrix[2][i].setVisible(true);
        		
        		lblPercent[i].setVisible(true);
        	}
        	
        	for (int j=0; j<NumMatrixFields; j++) {
        		edtCurvePointsMatrix[j][i].getDocument().addDocumentListener(new DocumentListener() {
        			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
        			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
        			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
        			private void processChange() {
        				edtChanged();
        			}
        		});
        	}
        }
        
        JScrollPane sc1 = new JScrollPane(sp);
        sc1.setSize(ScrollWidth, ScrollHeight);
        sc1.setLocation(cx, cy);
        add(sc1);
        
        cx += -TMargin;
        cy += ScrollHeight + TMargin*2;
        
		JLabel lbl7 = new JLabel("Low Mileage");
		lbl7.setSize(PanelWidth-TMargin*3, LblHeight);
		lbl7.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		add(lbl7);
        
        cx += TMargin;
        cy += EdtHeight + TMargin;
        
		JLabel lbl10 = new JLabel("Improves Resale by up to");
		lbl10.setSize(EdtMilesValueAdjustCaptionWidth1, LblHeight);
		lbl10.setLocation(cx, cy + (EdtHeight-LblHeight)/2);
		lbl10.setFont(lblFont);
		add(lbl10);

		edtLowMileageAdj = new JTextField(NumToString.floatWNumDecimals(vehDep.lowMileageReducesDeprByUpTo()*FracToPercentMult, 1));
		edtLowMileageAdj.setSize(EdtMilesValueAdjustWidth, EdtHeight);
		edtLowMileageAdj.setLocation(cx + TClear + EdtMilesValueAdjustCaptionWidth1, cy);
		edtLowMileageAdj.setHorizontalAlignment(SwingConstants.RIGHT);
		edtLowMileageAdj.setFont(edtFont);
		add(edtLowMileageAdj);
        
		JLabel lbl11 = new JLabel("% at");
		lbl11.setSize(EdtMilesValueAdjustCaptionWidth2, LblHeight);
		lbl11.setLocation(cx + TClear*2 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth, cy + (EdtHeight-LblHeight)/2);
		lbl11.setFont(lblFont);
		add(lbl11);

		edtLowMileageFrac = new JTextField(NumToString.floatWNumDecimals(vehDep.lowMileageAchievedAtFracExpectedMilesOf()*FracToPercentMult, 1));
		edtLowMileageFrac.setSize(EdtMilesValueAdjustWidth, EdtHeight);
		edtLowMileageFrac.setLocation(
				cx + TClear*3 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth + EdtMilesValueAdjustCaptionWidth2, cy);
		edtLowMileageFrac.setHorizontalAlignment(SwingConstants.RIGHT);
		edtLowMileageFrac.setFont(edtFont);
		add(edtLowMileageFrac);
       
		JLabel lbl12 = new JLabel("% of typical mileage");
		lbl12.setSize(EdtMilesValueAdjustCaptionWidth3, LblHeight);
		lbl12.setLocation(
				cx + TClear*4 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth*2 + EdtMilesValueAdjustCaptionWidth2, cy + (EdtHeight-LblHeight)/2);
		lbl12.setFont(lblFont);
		add(lbl12);
				
        cx += -TMargin;
        cy += EdtHeight + TMargin*2;
        
		JLabel lbl8 = new JLabel("High Mileage");
		lbl8.setSize(PanelWidth-TMargin*3, LblHeight);
		lbl8.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		add(lbl8);
        
        cx += TMargin;
        cy += EdtHeight + TMargin;
        
		JLabel lbl20 = new JLabel("Reduces Resale by up to");
		lbl20.setSize(EdtMilesValueAdjustCaptionWidth1, LblHeight);
		lbl20.setLocation(cx, cy + (EdtHeight-LblHeight)/2);
		lbl20.setFont(lblFont);
		add(lbl20);

		edtHighMileageAdj = new JTextField(NumToString.floatWNumDecimals(vehDep.highMileageIncreasesDeprByUpTo()*FracToPercentMult, 1));
		edtHighMileageAdj.setSize(EdtMilesValueAdjustWidth, EdtHeight);
		edtHighMileageAdj.setLocation(cx + TClear + EdtMilesValueAdjustCaptionWidth1, cy);
		edtHighMileageAdj.setHorizontalAlignment(SwingConstants.RIGHT);
		edtHighMileageAdj.setFont(edtFont);
		add(edtHighMileageAdj);
        
		JLabel lbl21 = new JLabel("% at");
		lbl21.setSize(EdtMilesValueAdjustCaptionWidth2, LblHeight);
		lbl21.setLocation(cx + TClear*2 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth, cy + (EdtHeight-LblHeight)/2);
		lbl21.setFont(lblFont);
		add(lbl21);

		edtHighMileageFrac = new JTextField(NumToString.floatWNumDecimals(vehDep.highMileageAchievedAtFracExpectedMilesOf()*FracToPercentMult, 1));
		edtHighMileageFrac.setSize(EdtMilesValueAdjustWidth, EdtHeight);
		edtHighMileageFrac.setLocation(
				cx + TClear*3 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth + EdtMilesValueAdjustCaptionWidth2, cy);
		edtHighMileageFrac.setHorizontalAlignment(SwingConstants.RIGHT);
		edtHighMileageFrac.setFont(edtFont);
		add(edtHighMileageFrac);
       
		JLabel lbl22 = new JLabel("% of typical mileage");
		lbl22.setSize(EdtMilesValueAdjustCaptionWidth3, LblHeight);
		lbl22.setLocation(
				cx + TClear*4 + EdtMilesValueAdjustCaptionWidth1 + EdtMilesValueAdjustWidth*2 + EdtMilesValueAdjustCaptionWidth2, cy + (EdtHeight-LblHeight)/2);
		lbl22.setFont(lblFont);
		add(lbl22);

		cy += EdtHeight + TMargin;
		
		setSize(PanelWidth, cy);
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if (source == spnNumVisPoints) {
			numVisibleCurvePoints = Integer.parseInt(spnNumVisPoints.getValue().toString());
			
			for (int i=0; i<numVisibleCurvePoints; i++) {
				for (int j=0; j<NumMatrixFields; j++) edtCurvePointsMatrix[j][i].setVisible(true);
				lblPercent[i].setVisible(true);
			}
			for (int i=numVisibleCurvePoints; i<MaxNumDeprPoints; i++) {
				for (int j=0; j<NumMatrixFields; j++) {
					edtCurvePointsMatrix[j][i].setVisible(false);
					edtCurvePointsMatrix[j][i].setText("");
				}
				lblPercent[i].setVisible(false);
			}
		}
	}
	private void edtChanged() {
		pDlg.edtChanged();
	}
	
	public static class DataOutputs {
		public float[][] deprCurveDataMatrix;	//Three Columns: year, mileage, residualValue
		public float lowMiResaleImpr, lowMiAt;
		public float highMiResaleRedc, highMiAt;
		
		private DataOutputs() {}
	}
	
	public DataOutputs getDeprData() {
		try {
			DataOutputs out = new DataOutputs();
			
			out.lowMiResaleImpr = Float.parseFloat(edtLowMileageAdj.getText())/FracToPercentMult;
			out.lowMiAt = Float.parseFloat(edtLowMileageFrac.getText())/FracToPercentMult;
			
			out.highMiResaleRedc = Float.parseFloat(edtHighMileageAdj.getText())/FracToPercentMult;
			out.highMiAt = Float.parseFloat(edtHighMileageFrac.getText())/FracToPercentMult;
			
			out.deprCurveDataMatrix = new float[NumMatrixFields][numVisibleCurvePoints];
			
			for (int i=0; i<numVisibleCurvePoints; i++) {
				out.deprCurveDataMatrix[0][i] = Float.parseFloat(edtCurvePointsMatrix[0][i].getText());
				out.deprCurveDataMatrix[1][i] = Float.parseFloat(edtCurvePointsMatrix[1][i].getText())*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
				out.deprCurveDataMatrix[2][i] = Float.parseFloat(edtCurvePointsMatrix[2][i].getText())/FracToPercentMult;
			}		
			
			float[] years = out.deprCurveDataMatrix[0];
			for (int i=1; i<years.length; i++) {
				if (years[i] <= years[i-1]) return null;	//Years must be monotone increasing
			}
			
			float[] miles = out.deprCurveDataMatrix[1];
			for (int i=1; i<miles.length; i++) {
				if (miles[i] <= miles[i-1]) return null;	//miles must be monotone increasing
			}
			
			float[] resValue = out.deprCurveDataMatrix[2];
			for (int i=1; i<resValue.length; i++) {
				if (resValue[i] >= resValue[i-1]) return null;	//resValue must be monotone decreasing
			}
			
			return out;
		} catch (Exception e) {
			return null;
		}
	}
}
