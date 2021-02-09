package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
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
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import pvc.datamgmt.comp.*;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class GHGAxesDialog extends JDialog implements ActionListener, ChangeListener {
	//Constants
	private static final int AxesMaxNumDiv = 20;	//Must be sure to keep this <= (CostVsGHGJPanel.HardLimitNumAxesTics - 1) 
	private static final int AxesMaxNumSubDiv = 20;
	private static final int MinNumDecimals = 0;
	private static final int MaxNumDecimals = 3;
	private static final int MinFontSize = 8;
	private static final int MaxFontSize = 16;

	private static final int TMargin = 4;
	private static final int WinMargin = 10;
	private static final int TClear = 2;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	private static final int BigBtnHeight = (BtnHeight*3)/2;
	private static final int BtnOKWidth = 100;
	
	private static final int LblFontSizeWidth = 65;
	private static final int LblAxisStepSizeWidth = 85;
	private static final int LblUnitsWidth = 50;
	private static final int SpnWidth = 45;
	private static final int ChkBoldWidth = 50;
	
	private static final int FontSectionWidth = WinMargin + LblFontSizeWidth + SpnWidth + TMargin*2 + ChkBoldWidth;
	private static final int AxisSpecSectionWidth = LblAxisStepSizeWidth + TClear + SpnWidth + WinMargin + //axis step
			LblFontSizeWidth + TClear + SpnWidth + WinMargin +	//Number of steps
			LblFontSizeWidth + TClear + SpnWidth + WinMargin + 	//Max value
			LblAxisStepSizeWidth + TClear + SpnWidth + WinMargin + 	//Number of sub-divisions value
			LblAxisStepSizeWidth + TClear + SpnWidth;				//Number of decimals
	

	//GUI Objects
	private JButton btnOK, btnCancel;
	private JTextField edtXAxisTitle, edtGHGStep;
	private JSpinner spnATFontSize, spnNumFontSize, spnLegendFontSize, spnNumAxisSteps, spnNumAxisSubSteps, spnGHGDecimals;
	private JCheckBox chkATBold, chkNumBold, chkLegendBold; 
	private JLabel lblMaxGHG;
	
	//Data Objects
	private boolean okPressed;
	private GHGAxesSetup axes;

	//Data access functions
	public boolean okPressed() {return okPressed;}
	public GHGAxesSetup getAxesSetup() {return axes;}

	//Constructor/Launcher
	public GHGAxesDialog(GHGAxesSetup ghgAxes) {
		//Call Super
		super(null, "Edit Axes", Dialog.ModalityType.APPLICATION_MODAL);

		//Set Data
		okPressed = false;
		axes = new GHGAxesSetup(ghgAxes);

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
				JOptionPane.showMessageDialog(null, "Invalid Numeric Value for GHG Axis Step", "Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
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
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();		
		if (source == spnNumAxisSteps) {
			edtGHGAxisEdited();
			return;
		}
		if (source == spnGHGDecimals) {
			edtGHGAxisEdited();
			return;
		}
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        int panelWidth = Math.max(FontSectionWidth*3 + WinMargin*6, AxisSpecSectionWidth + WinMargin*2);
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Axes Titles");
        lbl1.setSize(FontSectionWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        cy += LineSpacing;
        
        JLabel lbl2 = new JLabel("Font Size");
        lbl2.setSize(LblFontSizeWidth, LblHeight);
        lbl2.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        lbl2.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lbl2);
        
        SpinnerNumberModel spnmAT = new SpinnerNumberModel(axes.axesCaptionsFontSize, MinFontSize, MaxFontSize, 1);
        spnATFontSize = new JSpinner(spnmAT);
        spnATFontSize.setSize(SpnWidth, EdtHeight);
        spnATFontSize.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnATFontSize);

        chkATBold = new JCheckBox("Bold");
        chkATBold.setSize(ChkBoldWidth, EdtHeight);
        chkATBold.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin + SpnWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        chkATBold.setSelected(axes.axesCaptionsBold);
        totalGUI.add(chkATBold);
        
        cy = WinMargin;
        cx = WinMargin + FontSectionWidth + (panelWidth - FontSectionWidth*3 - WinMargin*2)/2;
        
        JLabel lbl3 = new JLabel("Numeric Values on Axes");
        lbl3.setSize(FontSectionWidth, LblHeight);
        lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);
        
        cy += LineSpacing;
        
        JLabel lbl4 = new JLabel("Font Size");
        lbl4.setSize(LblFontSizeWidth, LblHeight);
        lbl4.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        lbl4.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lbl4);
        
        SpinnerNumberModel spnmNum = new SpinnerNumberModel(axes.numbersFontSize, MinFontSize, MaxFontSize, 1);
        spnNumFontSize = new JSpinner(spnmNum);
        spnNumFontSize.setSize(SpnWidth, EdtHeight);
        spnNumFontSize.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnNumFontSize);

        chkNumBold = new JCheckBox("Bold");
        chkNumBold.setSize(ChkBoldWidth, EdtHeight);
        chkNumBold.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin + SpnWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        chkNumBold.setSelected(axes.numbersTextBold);
        totalGUI.add(chkNumBold);
        
        cy = WinMargin;
        cx = panelWidth - (WinMargin + FontSectionWidth);
        
        JLabel lbl5 = new JLabel("Legends & Additional Data");
        lbl5.setSize(FontSectionWidth, LblHeight);
        lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl5);
        
        cy += LineSpacing;
        
        JLabel lbl6 = new JLabel("Font Size");
        lbl6.setSize(LblFontSizeWidth, LblHeight);
        lbl6.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
        lbl6.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lbl6);
        
        SpinnerNumberModel spnmLeg = new SpinnerNumberModel(axes.legendFontSize, MinFontSize, MaxFontSize, 1);
        spnLegendFontSize = new JSpinner(spnmLeg);
        spnLegendFontSize.setSize(SpnWidth, EdtHeight);
        spnLegendFontSize.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnLegendFontSize);

        chkLegendBold = new JCheckBox("Bold");
        chkLegendBold.setSize(ChkBoldWidth, EdtHeight);
        chkLegendBold.setLocation(cx + WinMargin + LblFontSizeWidth + TMargin + SpnWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        chkLegendBold.setSelected(axes.legendTextBold);
        totalGUI.add(chkLegendBold);
        
        cx = WinMargin;
        cy += LineSpacing + WinMargin;
        
        int ctLabelWidth = FontSectionWidth - WinMargin;
        int tedtWidth = panelWidth - (WinMargin*3 + ctLabelWidth + TClear + TMargin + LblUnitsWidth);
        String ghgUnitString = "(" + DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")";
        
        JLabel lbl7 = new JLabel("GHG Axis Title without Units");
        lbl7.setSize(ctLabelWidth, LblHeight);
        lbl7.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl7);
        
        cx += ctLabelWidth + TClear;
        
        edtXAxisTitle = new JTextField(axes.ghgAxisTitleWithoutUnits);
        edtXAxisTitle.setSize(tedtWidth, EdtHeight);
        edtXAxisTitle.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        edtXAxisTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtXAxisTitle);
        
        cx += tedtWidth + TMargin;
        
        JLabel lbl8 = new JLabel(ghgUnitString);
        lbl8.setSize(ctLabelWidth, LblHeight);
        lbl8.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl8);
        
        cx = WinMargin;
        cy += LineSpacing;
              
        JLabel lbl9 = new JLabel("GHG Axis Step");
        lbl9.setSize(LblAxisStepSizeWidth, LblHeight);
        lbl9.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl9);

        cx += LblAxisStepSizeWidth + TClear;
        
        edtGHGStep = new JTextField(NumToString.floatWNumDecimals(axes.limGHGperDistInDisplayUnits()/(float)axes.numAxisDiv(), axes.ghgAxisNumDecimals));
        edtGHGStep.setSize(SpnWidth, EdtHeight);
        edtGHGStep.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        edtGHGStep.setHorizontalAlignment(SwingConstants.RIGHT);
        edtGHGStep.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
			private void processChange() {
				edtGHGAxisEdited();	
			}
		});
        totalGUI.add(edtGHGStep);
        
        cx += SpnWidth + WinMargin;
        
		JLabel lbl10 = new JLabel("Num. Steps");
		lbl10.setSize(LblFontSizeWidth, LblHeight);
		lbl10.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
		totalGUI.add(lbl10);
		
		cx += LblFontSizeWidth + TClear;
		
        SpinnerNumberModel spnmNumAxisSteps = new SpinnerNumberModel(axes.numAxisDiv(), 1, AxesMaxNumDiv, 1);
        spnNumAxisSteps = new JSpinner(spnmNumAxisSteps);
        spnNumAxisSteps.setSize(SpnWidth, EdtHeight);
        spnNumAxisSteps.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        spnNumAxisSteps.addChangeListener(this);
        totalGUI.add(spnNumAxisSteps);

        cx += SpnWidth + WinMargin;
              
		JLabel lbl11 = new JLabel("Max. Value");
		lbl11.setSize(LblFontSizeWidth, LblHeight);
		lbl11.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
		totalGUI.add(lbl11);
		
		cx += LblFontSizeWidth + TClear;
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
                
        lblMaxGHG = new JLabel(NumToString.floatWNumDecimals(axes.limGHGperDistInDisplayUnits(), axes.ghgAxisNumDecimals));
        lblMaxGHG.setSize(SpnWidth, EdtHeight);
        lblMaxGHG.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        lblMaxGHG.setBorder(blackBorder);
        lblMaxGHG.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lblMaxGHG);

        cx += SpnWidth + WinMargin;
              
		JLabel lbl12 = new JLabel("Sub-Divisions");
		lbl12.setSize(LblAxisStepSizeWidth, LblHeight);
		lbl12.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
		totalGUI.add(lbl12);
		
		cx += LblAxisStepSizeWidth + TClear;
		
        SpinnerNumberModel spnmNumSubDiv = new SpinnerNumberModel(axes.numBinsPerAxisDiv(), 1, AxesMaxNumSubDiv, 1);
        spnNumAxisSubSteps = new JSpinner(spnmNumSubDiv);
        spnNumAxisSubSteps.setSize(SpnWidth, EdtHeight);
        spnNumAxisSubSteps.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(spnNumAxisSubSteps);

        cx += SpnWidth + WinMargin;
        
		JLabel lbl13 = new JLabel("Num. Decimals");
		lbl13.setSize(LblAxisStepSizeWidth, LblHeight);
		lbl13.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
		totalGUI.add(lbl13);
		
		cx += LblAxisStepSizeWidth + TClear;

        SpinnerNumberModel spnmNumDecimals = new SpinnerNumberModel(axes.ghgAxisNumDecimals, MinNumDecimals, MaxNumDecimals, 1);
        spnGHGDecimals = new JSpinner(spnmNumDecimals);
        spnGHGDecimals.setSize(SpnWidth, EdtHeight);
        spnGHGDecimals.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        spnGHGDecimals.addChangeListener(this);
        totalGUI.add(spnGHGDecimals);
		
		cy += LineSpacing + WinMargin;
		cx = panelWidth/2 - TMargin/2 - BtnOKWidth;
		
		btnOK = new JButton("OK");
		btnOK.setSize(BtnOKWidth, BigBtnHeight);
		btnOK.setLocation(cx, cy);
		btnOK.addActionListener(this);
		totalGUI.add(btnOK);

		cx += BtnOKWidth + TMargin;
		
		btnCancel = new JButton("Cancel");
		btnCancel.setSize(BtnOKWidth, BigBtnHeight);
		btnCancel.setLocation(cx, cy);
		btnCancel.addActionListener(this);
		totalGUI.add(btnCancel);

        cy += BigBtnHeight + WinMargin;       
        int panelHeight = cy;
        
        totalGUI.setSize(panelWidth, panelHeight);        
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	
	private void screenToData() throws Exception {
		float maxGHGinDisplayUnits = Float.parseFloat(lblMaxGHG.getText());
		int numAxisSteps = Integer.parseInt(spnNumAxisSteps.getValue().toString());
		int numSubDiv = Integer.parseInt(spnNumAxisSubSteps.getValue().toString());
		int numDecimals = Integer.parseInt(spnGHGDecimals.getValue().toString());
		
		axes.setLimGHGperDistInDisplayUnits(maxGHGinDisplayUnits);
		axes.setNumDiv(numAxisSteps, numSubDiv);
		axes.ghgAxisNumDecimals = numDecimals;
		
		int axesTitlesFontSize = Integer.parseInt(spnATFontSize.getValue().toString());
		int axesNumFontSize = Integer.parseInt(spnNumFontSize.getValue().toString());
		int legendsFontSize = Integer.parseInt(spnLegendFontSize.getValue().toString());
		
		axes.ghgAxisTitleWithoutUnits = new String(edtXAxisTitle.getText());
		axes.axesCaptionsFontSize = axesTitlesFontSize;
		axes.numbersFontSize = axesNumFontSize;
		axes.legendFontSize = legendsFontSize;
		
		axes.axesCaptionsBold = chkATBold.isSelected();
		axes.numbersTextBold = chkNumBold.isSelected();
		axes.legendTextBold = chkLegendBold.isSelected();
	}
	private void edtGHGAxisEdited() {
		float axisStep = -1f;
		try {
			axisStep = Float.parseFloat(edtGHGStep.getText());
		} catch (Exception e) {}
		
		if (axisStep <= 0) return;
		
		int numAxisSteps = Integer.parseInt(spnNumAxisSteps.getValue().toString());
		int numDecimals = Integer.parseInt(spnGHGDecimals.getValue().toString());
		
		lblMaxGHG.setText(NumToString.floatWNumDecimals(numAxisSteps*axisStep, numDecimals));
	}
}
