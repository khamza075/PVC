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

import pvc.datamgmt.comp.CostBarsAxesSetup;
import pvc.datamgmt.comp.DUnits;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class CostBarsAxesDialog extends JDialog implements ActionListener {
	//Constants
	private static final int AxesMaxNumDiv = 30;	//Must be sure to keep this <= (CostVsGHGJPanel.HardLimitNumAxesTics - 1) 
	private static final int MinNumDecimals = 0;
	private static final int MaxNumDecimals = 4;
	private static final int MinFontSize = 8;
	private static final int MaxFontSize = 16;

	private static final int WinMargin = 10;
	private static final int TClear = 2;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (BtnHeight*3)/2;

	private static final int LblWidth1 = 120;
	private static final int SpnFontWidth = 45;
	private static final int ChkBoldWidth = 30;
	private static final int LeftSectionWidth = WinMargin + LblWidth1 + SpnFontWidth + ChkBoldWidth + TClear*2;

	private static final int LblWidth2 = 100;
	private static final int EdtNumWidth = 70;
	private static final int RightSectionWidth = WinMargin + LblWidth2 + EdtNumWidth + TClear;
	private static final int UnitsLblWidth = 50;
	private static final int BtnWidth = 80;

	//Input Error Trapping Messages
	private static final String SExceptionMessage_MaxMinRange = "Maximum Axis Value Must be Greater than Minimum Value";
	private static final String SExceptionMessage_StepSize_tooSmall = "Axis Step Size Too Small";
	private static final String SExceptionMessage_StepSize_tooLarge = "Axis Step Size Too Large";
	
	private static final String[] SExceptionMessages = {
			SExceptionMessage_MaxMinRange, SExceptionMessage_StepSize_tooSmall, SExceptionMessage_StepSize_tooLarge
	};


	//GUI Objects
	private JButton btnOK, btnCancel;
	private JTextField edtCostTitle, edtCostMin, edtCostMax, edtCostStep;
	private JSpinner spnATFontSize, spnNumFontSize, spnVehFontSize, spnCostDecimals;
	private JCheckBox chkATBold, chkNumBold, chkVehBold;

	
	//Data
	private boolean okPressed;
	private CostBarsAxesSetup axes;

	//Data access functions
	public boolean okPressed() {return okPressed;}
	public CostBarsAxesSetup getAxesSetup() {return axes;}

	
	public CostBarsAxesDialog(CostBarsAxesSetup costBarsAxesData) {
		//Call Super
		super(null, "Edit Axes", Dialog.ModalityType.APPLICATION_MODAL);

		//Set Data
		okPressed = false;
		axes = new CostBarsAxesSetup(costBarsAxesData);

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

        int panelWidth = LeftSectionWidth + RightSectionWidth +WinMargin*5;
        int panelHeight = LineSpacing*6 + WinMargin*4 + BigBtnHeight;
        
        int cx = panelWidth - WinMargin - UnitsLblWidth;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("("+DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit)+")");
        lbl1.setSize(UnitsLblWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl1);
        
        int titleEdtWidth = cx - WinMargin - LblWidth2 - TClear*2;
        cx = WinMargin;
        
        JLabel lbl2 = new JLabel("Cost Axis Title");
        lbl2.setSize(LblWidth2, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl2);
        
        cx += LblWidth2 + TClear;
        
        edtCostTitle = new JTextField(axes.costAxisTilteWOUnits);
        edtCostTitle.setSize(titleEdtWidth, EdtHeight);
        edtCostTitle.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
        edtCostTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostTitle);

        cx = WinMargin;
        cy += LineSpacing + WinMargin;
        
        JLabel lbl3 = new JLabel("Text Customization");
        lbl3.setSize(LblWidth1, LblHeight);
        lbl3.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl3);
        
        cx += LblWidth1 + TClear;

        JLabel lbl4 = new JLabel("Font Size");
        lbl4.setSize(SpnFontWidth + WinMargin, LblHeight);
        lbl4.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        lbl4.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lbl4);
        
        cx += SpnFontWidth + WinMargin + TClear;

        JLabel lbl5 = new JLabel("Bold");
        lbl5.setSize(ChkBoldWidth, LblHeight);
        lbl5.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        lbl5.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl5);
        
        cx = WinMargin*2;
        cy += LineSpacing;
        
        SpinnerNumberModel spnmAT = new SpinnerNumberModel(axes.axesCaptionFontSize, MinFontSize, MaxFontSize, 1);
        SpinnerNumberModel spnmNum = new SpinnerNumberModel(axes.numbersFontSize, MinFontSize, MaxFontSize, 1);
        SpinnerNumberModel spnmVeh = new SpinnerNumberModel(axes.vehicleNameFontSize, MinFontSize, MaxFontSize, 1);
        
        JLabel lbl6 = new JLabel("Cost Axes Title");
        lbl6.setSize(LblWidth1, LblHeight);
        lbl6.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl6);

        spnATFontSize = new JSpinner(spnmAT);
        spnATFontSize.setSize(SpnFontWidth, EdtHeight);
        spnATFontSize.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing-EdtHeight)/2);
        totalGUI.add(spnATFontSize);
        
        chkATBold = new JCheckBox();
        chkATBold.setSize(ChkBoldWidth, EdtHeight);
        chkATBold.setLocation(cx + LblWidth1 + SpnFontWidth + TClear*2, cy + (LineSpacing-EdtHeight)/2);
        chkATBold.setHorizontalAlignment(SwingConstants.CENTER);
        chkATBold.setSelected(axes.axesCaptionBold);
        totalGUI.add(chkATBold);
        
        cy += LineSpacing;

        JLabel lbl7 = new JLabel("Numeric Values");
        lbl7.setSize(LblWidth1, LblHeight);
        lbl7.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl7);

        spnNumFontSize = new JSpinner(spnmNum);
        spnNumFontSize.setSize(SpnFontWidth, EdtHeight);
        spnNumFontSize.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing-EdtHeight)/2);
        totalGUI.add(spnNumFontSize);
        
        chkNumBold = new JCheckBox();
        chkNumBold.setSize(ChkBoldWidth, EdtHeight);
        chkNumBold.setLocation(cx + LblWidth1 + SpnFontWidth + TClear*2, cy + (LineSpacing-EdtHeight)/2);
        chkNumBold.setHorizontalAlignment(SwingConstants.CENTER);
        chkNumBold.setSelected(axes.numbersTextBold);
        totalGUI.add(chkNumBold);
        
        cy += LineSpacing;

        JLabel lbl8 = new JLabel("Vehicle Names");
        lbl8.setSize(LblWidth1, LblHeight);
        lbl8.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl8);

        spnVehFontSize = new JSpinner(spnmVeh);
        spnVehFontSize.setSize(SpnFontWidth, EdtHeight);
        spnVehFontSize.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing-EdtHeight)/2);
        totalGUI.add(spnVehFontSize);
        
        chkVehBold = new JCheckBox();
        chkVehBold.setSize(ChkBoldWidth, EdtHeight);
        chkVehBold.setLocation(cx + LblWidth1 + SpnFontWidth + TClear*2, cy + (LineSpacing-EdtHeight)/2);
        chkVehBold.setHorizontalAlignment(SwingConstants.CENTER);
        chkVehBold.setSelected(axes.vehicleNamesBold);
        totalGUI.add(chkVehBold);
        
        cx = LeftSectionWidth + WinMargin*4;
        cy = LineSpacing + WinMargin*2;
        
        JLabel lbl9 = new JLabel("Cost Axis Limits");
        lbl9.setSize(RightSectionWidth, LblHeight);
        lbl9.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl9);
        
        cy += LineSpacing;
        cx += WinMargin;
        
        int numDecimals = axes.costAxis.numDecimals();
        SpinnerNumberModel spnmDec = new SpinnerNumberModel(numDecimals, MinNumDecimals, MaxNumDecimals, 1);

        JLabel lbl10 = new JLabel("Minimum Value");
        lbl10.setSize(LblWidth2, LblHeight);
        lbl10.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl10);

        edtCostMin = new JTextField(NumToString.floatWNumDecimals(axes.costAxis.minValue(), numDecimals));
        edtCostMin.setSize(EdtNumWidth, EdtHeight);
        edtCostMin.setLocation(cx + LblWidth2  +TClear, cy + (LineSpacing-EdtHeight)/2);
        edtCostMin.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostMin);

        cy += LineSpacing;

        JLabel lbl11 = new JLabel("Maximum Value");
        lbl11.setSize(LblWidth2, LblHeight);
        lbl11.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl11);

        edtCostMax = new JTextField(NumToString.floatWNumDecimals(axes.costAxis.maxValue(), numDecimals));
        edtCostMax.setSize(EdtNumWidth, EdtHeight);
        edtCostMax.setLocation(cx + LblWidth2  +TClear, cy + (LineSpacing-EdtHeight)/2);
        edtCostMax.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostMax);

        cy += LineSpacing;

        JLabel lbl12 = new JLabel("Step Size");
        lbl12.setSize(LblWidth2, LblHeight);
        lbl12.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl12);

        edtCostStep = new JTextField(NumToString.floatWNumDecimals(axes.costAxis.stepSize(), numDecimals));
        edtCostStep.setSize(EdtNumWidth, EdtHeight);
        edtCostStep.setLocation(cx + LblWidth2  +TClear, cy + (LineSpacing-EdtHeight)/2);
        edtCostStep.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtCostStep);

        cy += LineSpacing;
        
        int splblWidth = LblWidth2 + EdtNumWidth - SpnFontWidth;

        JLabel lbl13 = new JLabel("Number of Decimals");
        lbl13.setSize(splblWidth, LblHeight);
        lbl13.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl13);
        
        spnCostDecimals = new JSpinner(spnmDec);
        spnCostDecimals.setSize(SpnFontWidth, EdtHeight);
        spnCostDecimals.setLocation(cx + splblWidth + TClear, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(spnCostDecimals);
        
        cy += LineSpacing + WinMargin;
        cx = panelWidth/2;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation(cx - BtnWidth - WinMargin/2, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(cx + WinMargin/2, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
        
        totalGUI.setSize(panelWidth, panelHeight);        
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	
	private void screenToData() throws Exception {
		axes.axesCaptionFontSize = Integer.parseInt(spnATFontSize.getValue().toString());
		axes.numbersFontSize = Integer.parseInt(spnNumFontSize.getValue().toString());
		axes.vehicleNameFontSize = Integer.parseInt(spnVehFontSize.getValue().toString());
		axes.numberNumDecimals = Integer.parseInt(spnCostDecimals.getValue().toString());
		
		axes.axesCaptionBold = chkATBold.isSelected();
		axes.numbersTextBold = chkNumBold.isSelected();
		axes.vehicleNamesBold = chkVehBold.isSelected();
		
		axes.costAxisTilteWOUnits = new String(edtCostTitle.getText());
		
		float costMin = Float.parseFloat(edtCostMin.getText());
		float costMax = Float.parseFloat(edtCostMax.getText());
		float costStep = Float.parseFloat(edtCostStep.getText());
		
		if (costMax <= costMin) throw new Exception(SExceptionMessage_MaxMinRange);
		if (costStep <= 0) throw new Exception(SExceptionMessage_StepSize_tooSmall);
		
		int numDiv = 0;
		if ((costMin*costMax) > 0) {
			numDiv = (int)((costMax-costMin)/costStep);
		} else {
			int numNeg = (int)((-costMin)/costStep);
			int numPos = (int)(costMax/costStep);
			numDiv = numNeg + numPos;
		}
		
		if (numDiv < 1) throw new Exception(SExceptionMessage_StepSize_tooLarge);
		if (numDiv > AxesMaxNumDiv) throw new Exception(SExceptionMessage_StepSize_tooSmall);
		
		axes.costAxis.reScale(costMin, costMax, costStep, axes.numberNumDecimals);
	}
}
