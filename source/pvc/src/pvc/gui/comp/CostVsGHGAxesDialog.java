package pvc.gui.comp;

import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import pvc.datamgmt.comp.*;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class CostVsGHGAxesDialog extends JDialog implements ActionListener {
	//Constants
	private static final int AxesMaxNumDiv = 30;	//Must be sure to keep this <= (CostVsGHGJPanel.HardLimitNumAxesTics - 1) 
	private static final int MinNumDecimals = 0;
	private static final int MaxNumDecimals = 4;
	private static final int MinFontSize = 8;
	private static final int MaxFontSize = 16;

	private static final int WinMargin = 10;
	private static final int TClearance = 2;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (BtnHeight*3)/2;

	private static final int NumEdtWidth = 70;
	private static final int NumLblWidth = 60;
	private static final int UnitsLblWidth = 70;
	private static final int SpinBtnWidth = 40;
	private static final int ChkWidth = 60;
	private static final int BtnWidth = 80;
	
	//Input Error Trapping Messages
	private static final String SExceptionMessage_GHG_MaxMinRange = "Horizontal Axis, Maximum Value Must be Greater than Minimum Value";
	private static final String SExceptionMessage_GHG_StepSize_tooSmall = "Horizontal Axis, Step Size Too Small";
	private static final String SExceptionMessage_GHG_StepSize_tooLarge = "Horizontal Axis, Step Size Too Large";
	
	private static final String SExceptionMessage_Cost_MaxMinRange = "Vertical Axis, Maximum Value Must be Greater than Minimum Value";
	private static final String SExceptionMessage_Cost_StepSize_tooSmall = "Vertical Axis, Step Size Too Small";
	private static final String SExceptionMessage_Cost_StepSize_tooLarge = "Vertical Axis, Step Size Too Large";
	private static final String[] SExceptionMessages = {
			SExceptionMessage_GHG_MaxMinRange, SExceptionMessage_GHG_StepSize_tooSmall, SExceptionMessage_GHG_StepSize_tooLarge,
			SExceptionMessage_Cost_MaxMinRange, SExceptionMessage_Cost_StepSize_tooSmall, SExceptionMessage_Cost_StepSize_tooLarge
	};
		
	//GUI Objects
	private JButton btnOK, btnCancel;
	private JSpinner spnATFontSize, spnNumFontSize, spnGHGDecimals, spnCostDecimals;
	private JCheckBox chkATBold, chkNumBold;
	private JTextField edtGHGTitle, edtCostTitle, edtGHGMin, edtGHGMax, edtGHGStep, edtCostMin, edtCostMax, edtCostStep;

	//Data
	private boolean okPressed;	
	private boolean atBold, numBold;
	private int atFontSize, numFontSize, ghgNumDecimals, costNumDecimals;
	private float ghgMin, ghgMax, ghgStep, costMin, costMax, costStep;
	private String ghgTitle, costTitle;
	
	private float gCO2perMileToDisplayUnits, dollarPerMileToDisplayUnits;
	
	
	//Data access functions
	public boolean okPressed() {return okPressed;}
	
	public boolean atBold() {return atBold;}
	public boolean numBold() {return numBold;}

	public int atFontSize() {return atFontSize;}
	public int numFontSize() {return numFontSize;}
	public int ghgNumDecimals() {return ghgNumDecimals;}
	public int costNumDecimals() {return costNumDecimals;}

	public float ghgMin() {return ghgMin;}
	public float ghgMax() {return ghgMax;}
	public float ghgStep() {return ghgStep;}
	public float costMin() {return costMin;}
	public float costMax() {return costMax;}
	public float costStep() {return costStep;}

	public String ghgTitle() {return new String(ghgTitle);}
	public String costTitle() {return new String(costTitle);}

	
	//Constructor
	public CostVsGHGAxesDialog(int axisTitlesFontSize, boolean axisTitlesBold, int numLabelsFontSize, boolean numLabelsBold, 
			String ghgAxisTitleWOUnits, String costAxisTitleWOUnits, 
			float ghgAxisMinGCO2perMile, float ghgAxisMaxGCO2perMile, float ghgAxisGCO2perMileStep, int ghgAxisNumDecimals,
			float costAxisMinDollarPerMile, float costAxisMaxDollarPerMile, float costAxisDollarPerMileStep, int costAxisNumDecimals) {
		//Call Super
		super(null, "Edit Axes", Dialog.ModalityType.APPLICATION_MODAL);

		//Set Data
		okPressed = false;
		
		atBold = axisTitlesBold;
		numBold = numLabelsBold;
		
		atFontSize = axisTitlesFontSize;
		if (atFontSize < MinFontSize) atFontSize = MinFontSize;
		if (atFontSize > MaxFontSize) atFontSize = MaxFontSize;
		numFontSize = numLabelsFontSize;
		if (numFontSize < MinFontSize) numFontSize = MinFontSize;
		if (numFontSize > MaxFontSize) numFontSize = MaxFontSize;
		
		ghgNumDecimals = ghgAxisNumDecimals;
		if (ghgNumDecimals < MinNumDecimals) ghgNumDecimals = MinNumDecimals;
		if (ghgNumDecimals > MaxNumDecimals) ghgNumDecimals = MaxNumDecimals;
		costNumDecimals = costAxisNumDecimals;
		if (costNumDecimals < MinNumDecimals) costNumDecimals = MinNumDecimals;
		if (costNumDecimals > MaxNumDecimals) costNumDecimals = MaxNumDecimals;
		
		ghgMin = ghgAxisMinGCO2perMile;
		ghgMax = ghgAxisMaxGCO2perMile;
		ghgStep = ghgAxisGCO2perMileStep;
		costMin = costAxisMinDollarPerMile;
		costMax = costAxisMaxDollarPerMile;
		costStep = costAxisDollarPerMileStep;
		
		ghgTitle = new String(ghgAxisTitleWOUnits);
		costTitle = new String(costAxisTitleWOUnits);
		
		gCO2perMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
		dollarPerMileToDisplayUnits = DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);

		//Create Graphics	
		JPanel ct = createContentPane();
		setContentPane(ct);
		
		JFrame frame = new JFrame();
		frame.pack();
		Insets insets = frame.getInsets();
        int addedWidth = insets.left + insets.right;
        int addedHeight = insets.top + insets.bottom;
     
        int winWidth = ct.getWidth() + addedWidth;
        int winHeight = ct.getHeight() + addedHeight;
         
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((screenWidth-winWidth)/2, (screenHeight-winHeight)/2);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(winWidth, winHeight);
        setResizable(false);
		setVisible(true);		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			try {
				screenToData();
			} catch (Exception e) {
				for (int i=0; i<SExceptionMessages.length; i++) {
					if (e.toString().contains(SExceptionMessages[i])) {
						JOptionPane.showMessageDialog(null, SExceptionMessages[i], "Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				JOptionPane.showMessageDialog(null, "Invalid Numeric Value", "Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			okPressed = true;
			dispose();	
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
		}
	}

	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        int panelWidth = (NumLblWidth + TClearance + NumEdtWidth)*4 + WinMargin*6;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        int tlblWidth = (panelWidth - 3*WinMargin)/2;
        
        JLabel lbl1 = new JLabel("Axes Titles");
        lbl1.setSize(tlblWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        JLabel lbl2 = new JLabel("Numeric Values on Axes");
        lbl2.setSize(tlblWidth, LblHeight);
        lbl2.setLocation(cx + tlblWidth + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;
        
        JLabel lbl3 = new JLabel("Font Size");
        lbl3.setSize(NumLblWidth, LblHeight);
        lbl3.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);
        
        SpinnerNumberModel spnm1 = new SpinnerNumberModel(atFontSize, MinFontSize, MaxFontSize, 1);
        spnATFontSize = new JSpinner(spnm1);
        spnATFontSize.setSize(SpinBtnWidth, EdtHeight);
        spnATFontSize.setLocation(cx + WinMargin + NumLblWidth + TClearance, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnATFontSize);
        
        chkATBold = new JCheckBox("Bold");
        chkATBold.setSize(ChkWidth, EdtHeight);
        chkATBold.setLocation(cx + WinMargin*2 + NumLblWidth + TClearance + SpinBtnWidth, cy + (LineSpacing - EdtHeight)/2);
        chkATBold.setSelected(atBold);
        totalGUI.add(chkATBold);
        
        JLabel lbl4 = new JLabel("Font Size");
        lbl4.setSize(NumLblWidth, LblHeight);
        lbl4.setLocation(cx + WinMargin + tlblWidth + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl4);
        
        SpinnerNumberModel spnm2 = new SpinnerNumberModel(numFontSize, MinFontSize, MaxFontSize, 1);
        spnNumFontSize = new JSpinner(spnm2);
        spnNumFontSize.setSize(SpinBtnWidth, EdtHeight);
        spnNumFontSize.setLocation(cx + WinMargin + NumLblWidth + TClearance + tlblWidth + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnNumFontSize);
        
        chkNumBold = new JCheckBox("Bold");
        chkNumBold.setSize(ChkWidth, EdtHeight);
        chkNumBold.setLocation(cx + WinMargin*2 + NumLblWidth + TClearance + SpinBtnWidth + tlblWidth + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        chkNumBold.setSelected(numBold);
        totalGUI.add(chkNumBold);
        
        cy += LineSpacing + WinMargin*2;
        
        int t2lblWidth = NumLblWidth + TClearance*2 + SpinBtnWidth + ChkWidth + WinMargin;
        
        JLabel lbl5 = new JLabel("Horizontal Axis Title w/o Units");
        lbl5.setSize(t2lblWidth, LblHeight);
        lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl5);
        
        edtGHGTitle = new JTextField(ghgTitle);
        edtGHGTitle.setSize(t2lblWidth, EdtHeight);
        edtGHGTitle.setLocation(cx + t2lblWidth + TClearance, cy +(LineSpacing - EdtHeight)/2);
        edtGHGTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtGHGTitle);

        JLabel lbl6 = new JLabel("("+DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")");
        lbl6.setSize(UnitsLblWidth, LblHeight);
        lbl6.setLocation(cx + t2lblWidth + TClearance*2 + t2lblWidth, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl6);

        cy += LineSpacing;
        
        JLabel lbl7 = new JLabel("Minimum");
        lbl7.setSize(NumLblWidth, LblHeight);
        lbl7.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl7);
        
        String edtText = NumToString.floatWNumDecimals(ghgMin*gCO2perMileToDisplayUnits, ghgNumDecimals);
        edtGHGMin = new JTextField(edtText);
        edtGHGMin.setSize(NumEdtWidth, EdtHeight);
        edtGHGMin.setLocation(cx + WinMargin + NumLblWidth + TClearance, cy + (LineSpacing - EdtHeight)/2);
        edtGHGMin.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtGHGMin);

        JLabel lbl8 = new JLabel("Maximum");
        lbl8.setSize(NumLblWidth, LblHeight);
        lbl8.setLocation(cx + WinMargin*2 + NumLblWidth + NumEdtWidth + TClearance, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl8);
        
        edtText = NumToString.floatWNumDecimals(ghgMax*gCO2perMileToDisplayUnits, ghgNumDecimals);
        edtGHGMax = new JTextField(edtText);
        edtGHGMax.setSize(NumEdtWidth, EdtHeight);
        edtGHGMax.setLocation(cx + WinMargin*2 + NumLblWidth*2 + NumEdtWidth + TClearance*2, cy + (LineSpacing - EdtHeight)/2);
        edtGHGMax.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtGHGMax);

        JLabel lbl9 = new JLabel("Step size");
        lbl9.setSize(NumLblWidth, LblHeight);
        lbl9.setLocation(cx + WinMargin*3 + NumLblWidth*2 + NumEdtWidth*2 + TClearance*2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl9);
        
        edtText = NumToString.posFloatWNumDecimals(ghgStep*gCO2perMileToDisplayUnits, ghgNumDecimals);
        edtGHGStep = new JTextField(edtText);
        edtGHGStep.setSize(NumEdtWidth, EdtHeight);
        edtGHGStep.setLocation(cx + WinMargin*3 + NumLblWidth*3 + NumEdtWidth*2 + TClearance*3, cy + (LineSpacing - EdtHeight)/2);
        edtGHGStep.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtGHGStep);
        
        int t3lblWidth = NumLblWidth + NumEdtWidth - SpinBtnWidth;

        JLabel lbl10 = new JLabel("Num. Decimals");
        lbl10.setSize(t3lblWidth, LblHeight);
        lbl10.setLocation(cx + WinMargin*4 + NumLblWidth*3 + NumEdtWidth*3 + TClearance*3, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl10); 
        
        SpinnerNumberModel spnm3 = new SpinnerNumberModel(ghgNumDecimals, MinNumDecimals, MaxNumDecimals, 1);
        spnGHGDecimals = new JSpinner(spnm3);
        spnGHGDecimals.setSize(SpinBtnWidth, EdtHeight);
        spnGHGDecimals.setLocation(cx + WinMargin*4 + NumLblWidth*3 + NumEdtWidth*3 + TClearance*4 + t3lblWidth, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnGHGDecimals);

        cy += LineSpacing + WinMargin;
        
        JLabel lbl11 = new JLabel("Vertical Axis Title w/o Units");
        lbl11.setSize(t2lblWidth, LblHeight);
        lbl11.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl11);
        
        edtCostTitle = new JTextField(costTitle);
        edtCostTitle.setSize(t2lblWidth, EdtHeight);
        edtCostTitle.setLocation(cx + t2lblWidth + TClearance, cy +(LineSpacing - EdtHeight)/2);
        edtCostTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostTitle);

        JLabel lbl12 = new JLabel("("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")");
        lbl12.setSize(UnitsLblWidth, LblHeight);
        lbl12.setLocation(cx + t2lblWidth + TClearance*2 + t2lblWidth, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl12);

        cy += LineSpacing;

        JLabel lbl13 = new JLabel("Minimum");
        lbl13.setSize(NumLblWidth, LblHeight);
        lbl13.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl13);
        
        edtText = NumToString.floatWNumDecimals(costMin*dollarPerMileToDisplayUnits, costNumDecimals);
        edtCostMin = new JTextField(edtText);
        edtCostMin.setSize(NumEdtWidth, EdtHeight);
        edtCostMin.setLocation(cx + WinMargin + NumLblWidth + TClearance, cy + (LineSpacing - EdtHeight)/2);
        edtCostMin.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostMin);

        JLabel lbl14 = new JLabel("Maximum");
        lbl14.setSize(NumLblWidth, LblHeight);
        lbl14.setLocation(cx + WinMargin*2 + NumLblWidth + NumEdtWidth + TClearance, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl14);
        
        edtText = NumToString.floatWNumDecimals(costMax*dollarPerMileToDisplayUnits, costNumDecimals);
        edtCostMax = new JTextField(edtText);
        edtCostMax.setSize(NumEdtWidth, EdtHeight);
        edtCostMax.setLocation(cx + WinMargin*2 + NumLblWidth*2 + NumEdtWidth + TClearance*2, cy + (LineSpacing - EdtHeight)/2);
        edtCostMax.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostMax);

        JLabel lbl15 = new JLabel("Step size");
        lbl15.setSize(NumLblWidth, LblHeight);
        lbl15.setLocation(cx + WinMargin*3 + NumLblWidth*2 + NumEdtWidth*2 + TClearance*2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl15);
        
        edtText = NumToString.posFloatWNumDecimals(costStep*dollarPerMileToDisplayUnits, costNumDecimals);
        edtCostStep = new JTextField(edtText);
        edtCostStep.setSize(NumEdtWidth, EdtHeight);
        edtCostStep.setLocation(cx + WinMargin*3 + NumLblWidth*3 + NumEdtWidth*2 + TClearance*3, cy + (LineSpacing - EdtHeight)/2);
        edtCostStep.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostStep);
        
        JLabel lbl16 = new JLabel("Num. Decimals");
        lbl16.setSize(t3lblWidth, LblHeight);
        lbl16.setLocation(cx + WinMargin*4 + NumLblWidth*3 + NumEdtWidth*3 + TClearance*3, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl16); 
        
        SpinnerNumberModel spnm4 = new SpinnerNumberModel(costNumDecimals, MinNumDecimals, MaxNumDecimals, 1);
        spnCostDecimals = new JSpinner(spnm4);
        spnCostDecimals.setSize(SpinBtnWidth, EdtHeight);
        spnCostDecimals.setLocation(cx + WinMargin*4 + NumLblWidth*3 + NumEdtWidth*3 + TClearance*4 + t3lblWidth, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnCostDecimals);

        cy += LineSpacing + WinMargin*2;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation((panelWidth-WinMargin/2)/2 - BtnWidth, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(panelWidth/2 + WinMargin/2, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
        
        int panelHeight = cy + BigBtnHeight + WinMargin;
        totalGUI.setSize(panelWidth, panelHeight);
        
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	
	private void screenToData() throws Exception {
		atFontSize = Integer.parseInt(spnATFontSize.getValue().toString());
		numFontSize = Integer.parseInt(spnNumFontSize.getValue().toString());
		
		atBold = chkATBold.isSelected();
		numBold = chkNumBold.isSelected();
		
		ghgTitle = edtGHGTitle.getText();
		ghgMin = Float.parseFloat(edtGHGMin.getText());
		ghgMax = Float.parseFloat(edtGHGMax.getText());
		ghgStep = Float.parseFloat(edtGHGStep.getText());
		ghgNumDecimals = Integer.parseInt(spnGHGDecimals.getValue().toString());
		
		costTitle = edtCostTitle.getText();
		costMin = Float.parseFloat(edtCostMin.getText());
		costMax = Float.parseFloat(edtCostMax.getText());
		costStep = Float.parseFloat(edtCostStep.getText());
		costNumDecimals = Integer.parseInt(spnCostDecimals.getValue().toString());
		
		if (ghgMax <= ghgMin) throw new ExceptionGHG_MaxMinRange();
		if (costMax <= costMin) throw new ExceptionCost_MaxMinRange();
		
		if (ghgStep <= 0) throw new ExceptionGHG_StepSize_tooSmall();
		if (costStep <= 0) throw new ExceptionCost_StepSize_tooSmall();
		
		int numDiv = 0;
		if ((ghgMax * ghgMin) < 0) {
			//Opposite signs
			numDiv = (int)(-ghgMin/ghgStep) + (int)(ghgMax/ghgStep);
		} else {
			numDiv = (int)((ghgMax - ghgMin)/ghgStep);
		}
		if (numDiv < 1) throw new ExceptionGHG_StepSize_tooLarge();
		if (numDiv > AxesMaxNumDiv) throw new ExceptionGHG_StepSize_tooSmall();
		
		numDiv = 0;
		if ((costMax * costMin) < 0) {
			//Opposite signs
			numDiv = (int)(-costMin/costStep) + (int)(costMax/costStep);
		} else {
			numDiv = (int)((costMax - costMin)/costStep);
		}
		if (numDiv < 1) throw new ExceptionCost_StepSize_tooLarge();
		if (numDiv > AxesMaxNumDiv) throw new ExceptionCost_StepSize_tooSmall();
	}
	
	private class ExceptionGHG_MaxMinRange extends Exception {private ExceptionGHG_MaxMinRange() {super(SExceptionMessage_GHG_MaxMinRange);};}
	private class ExceptionGHG_StepSize_tooSmall extends Exception {private ExceptionGHG_StepSize_tooSmall() {super(SExceptionMessage_GHG_StepSize_tooSmall);};}
	private class ExceptionGHG_StepSize_tooLarge extends Exception {private ExceptionGHG_StepSize_tooLarge() {super(SExceptionMessage_GHG_StepSize_tooLarge);};}
	
	private class ExceptionCost_MaxMinRange extends Exception {private ExceptionCost_MaxMinRange() {super(SExceptionMessage_Cost_MaxMinRange);};}
	private class ExceptionCost_StepSize_tooSmall extends Exception {private ExceptionCost_StepSize_tooSmall() {super(SExceptionMessage_Cost_StepSize_tooSmall);};}
	private class ExceptionCost_StepSize_tooLarge extends Exception {private ExceptionCost_StepSize_tooLarge() {super(SExceptionMessage_Cost_StepSize_tooLarge);};}	
}
