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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;

@SuppressWarnings("serial")
public class GHGEditDisplayDialog extends JDialog implements ActionListener {
	private static final int GridAndAxesLineWidthMin = 1;
	private static final int GridAndAxesLineWidthMax = 5;
	private static final int VehTitleFontSizeMin = 10;
	private static final int VehTitleFontSizeMax = 24;
	
	//Sizing
	private static final int TClear = 2;
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int ScrollBarAllowance = 20;
	
	private static final float NumVehicleSettingsVisible = 3.6f;

	private static final int ChkShowGridsWidth = 160;
	private static final int SpnWidth = 50;
	private static final int LblWidth1 = 50;
	private static final int LblWidth2 = 50;
	private static final int BtnColorWidth = 100;
	private static final int BtnOKWidth = 90;
	
	private static final int LeftSideWidth = Math.max(ChkShowGridsWidth+TClear+SpnWidth, WinMargin+LblWidth2+TClear+SpnWidth+TMargin+BtnColorWidth);
	private static final int RightSideWidth = WinMargin + LblWidth1 + TClear + SpnWidth + TMargin + BtnColorWidth;
	
	//GUI Objects
	private JButton btnOK, btnCancel, btnReOrder, btnMainGridColor, btnSubGridColor, btnL1Color, btnL2Color, btnDCColor;
	private JSpinner spnTitleFont, spnAxesLineWidth, spnBxpLineWidth, spnMainGrid, spnSubGrid;
	private JCheckBox chkMainGrid, chkSubGrid;
	private ColorJLabel clblMainGrid, clblSubGrid, clblL1, clblL2, clblDC;
	private GHGVehDisplayPanelMaker.VehDisplayOptionsPanel vehOptionsPanel;
	
	
	//Data Objects
	private AnalysisVehModelsSetup avms;
	private GHGDisplaySetup dSetup;
	
	private boolean okPressed;
	private boolean reOrderInvoked;

	public boolean okPressed() {return okPressed;}
	public boolean reOrderInvoked() {return reOrderInvoked;}
	public GHGDisplaySetup displaySetup() {return dSetup;}

	
	public GHGEditDisplayDialog(AnalysisVehModelsSetup avModelsSetup, GHGDisplaySetup displaySetup) {
		//Super
		super(null, "Display Options", Dialog.ModalityType.APPLICATION_MODAL);
		
		//Set data
		okPressed = false;
		reOrderInvoked = false;
		
		avms = avModelsSetup;
		dSetup = new GHGDisplaySetup(displaySetup);	//Using the Copy Constructor		
		
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
			screenToData();
			okPressed = true;
			dispose();	
			return;
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
			return;
		}
		if (source == btnMainGridColor) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getGridColorRed(), dSetup.getGridColorGreen(), dSetup.getGridColorBlue());
			
			if (dlg.okPressed()) {
				dSetup.setGridColor(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				clblMainGrid.setColor(dSetup.getGridColor());
			}
			
			return;
		}
		if (source == btnSubGridColor) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getMinorGridColorRed(), dSetup.getMinorGridColorGreen(), dSetup.getMinorGridColorBlue());
			
			if (dlg.okPressed()) {
				dSetup.setMinorGridColor(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				clblSubGrid.setColor(dSetup.getMinorGridColor());
			}
			
			return;
		}
		if (source == btnL1Color) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getL1ColorRed(), dSetup.getL1ColorGreen(), dSetup.getL1ColorBlue());
			
			if (dlg.okPressed()) {
				dSetup.setL1Color(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				clblL1.setColor(dSetup.getL1Color());
			}
			
			return;
		}
		if (source == btnL2Color) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getL2ColorRed(), dSetup.getL2ColorGreen(), dSetup.getL2ColorBlue());
			
			if (dlg.okPressed()) {
				dSetup.setL2Color(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				clblL2.setColor(dSetup.getL2Color());
			}
			
			return;
		}
		if (source == btnDCColor) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getDCColorRed(), dSetup.getDCColorGreen(), dSetup.getDCColorBlue());
			
			if (dlg.okPressed()) {
				dSetup.setDCColor(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				clblDC.setColor(dSetup.getDCColor());
			}
			
			return;
		}
		if (source == btnReOrder) {
			screenToData();
			GHGHistVehOrderDialog dlg = new GHGHistVehOrderDialog(avms, dSetup);
			
			if (dlg.reOrderInvoked()) {
				dSetup = new GHGDisplaySetup(dlg.getDisplaySetup());
				okPressed = false;
				reOrderInvoked = true;
				dispose();	
			}
			return;
		}
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int panelWidth = LeftSideWidth + RightSideWidth + WinMargin*4;
        int vehOptionsBH = LineSpacing*4;
        int hscHeight = (int)(vehOptionsBH * NumVehicleSettingsVisible);
        int topPortionHeight = LineSpacing*7;
        int panelHeight = topPortionHeight + WinMargin*4 + hscHeight + BigBtnHeight;
        
        int hscWidth = LeftSideWidth + RightSideWidth + WinMargin*2;
        int vPanelPrefWidth = hscWidth;
        int vPanelPrefHeight = GHGVehDisplayPanelMaker.preferredPanelHeight(dSetup);
        if (vPanelPrefHeight > hscHeight) vPanelPrefWidth += -ScrollBarAllowance;
        
        vehOptionsPanel = GHGVehDisplayPanelMaker.createPanel(avms.vehModelsSetup(), dSetup, vPanelPrefWidth);
        vehOptionsPanel.setBorder(blackBorder);
        
        int cx = WinMargin;
        int cy = WinMargin*2 + topPortionHeight;
        
        if (vPanelPrefHeight > hscHeight) {
        	vehOptionsPanel.setAutoscrolls(true);
        	JScrollPane hsc = new JScrollPane(vehOptionsPanel);
        	hsc.setSize(hscWidth, hscHeight);
        	hsc.setLocation(cx, cy);
        	totalGUI.add(hsc);
        } else {
        	vehOptionsPanel.setSize(hscWidth, hscHeight);
        	vehOptionsPanel.setLocation(cx, cy);
        	totalGUI.add(vehOptionsPanel);
        }
        
        cy += hscHeight + WinMargin;
        
        btnReOrder = new JButton("Change Order...");
        btnReOrder.setSize((3*BtnOKWidth)/2, BigBtnHeight);
        btnReOrder.setLocation(cx, cy);
        btnReOrder.addActionListener(this);
    	totalGUI.add(btnReOrder);
        
    	btnOK = new JButton("OK");
    	btnOK.setSize(BtnOKWidth, BigBtnHeight);
    	btnOK.setLocation(cx + hscWidth - BtnOKWidth*2 - TMargin, cy);
    	btnOK.addActionListener(this);
    	totalGUI.add(btnOK);
        
    	btnCancel = new JButton("Cancel");
    	btnCancel.setSize(BtnOKWidth, BigBtnHeight);
    	btnCancel.setLocation(cx + hscWidth - BtnOKWidth, cy);
    	btnCancel.addActionListener(this);
    	totalGUI.add(btnCancel);
        
        cy = WinMargin;
        
        int chkWidth = LeftSideWidth - (TClear+SpnWidth);
        
        JLabel lbl1 = new JLabel("Vehicle Title Font Size"); 
        lbl1.setSize(chkWidth - TMargin, LblHeight);
        lbl1.setLocation(cx + TMargin, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl1);
    	
        SpinnerNumberModel spmTitleFont = new SpinnerNumberModel(dSetup.titleFontSize(), VehTitleFontSizeMin, VehTitleFontSizeMax, 1);
        spnTitleFont = new JSpinner(spmTitleFont);
        spnTitleFont.setSize(SpnWidth, EdtHeight);
        spnTitleFont.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
        spnTitleFont.setBorder(blackBorder);
    	totalGUI.add(spnTitleFont);
    	
    	cy += LineSpacing;
        
        JLabel lbl2 = new JLabel("Axes Line Width"); 
        lbl2.setSize(chkWidth - TMargin, LblHeight);
        lbl2.setLocation(cx + TMargin, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl2);
    	
        SpinnerNumberModel spmAxesLW = new SpinnerNumberModel(dSetup.axesLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        spnAxesLineWidth = new JSpinner(spmAxesLW);
        spnAxesLineWidth.setSize(SpnWidth, EdtHeight);
        spnAxesLineWidth.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
        spnAxesLineWidth.setBorder(blackBorder);
    	totalGUI.add(spnAxesLineWidth);
    	
    	cy += LineSpacing;
        
        JLabel lbl3 = new JLabel("Box Plots Line Width"); 
        lbl3.setSize(chkWidth - TMargin, LblHeight);
        lbl3.setLocation(cx + TMargin, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl3);
    	
        SpinnerNumberModel spmBxpLW = new SpinnerNumberModel(dSetup.boxPlotLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        spnBxpLineWidth = new JSpinner(spmBxpLW);
        spnBxpLineWidth.setSize(SpnWidth, EdtHeight);
        spnBxpLineWidth.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
        spnBxpLineWidth.setBorder(blackBorder);
    	totalGUI.add(spnBxpLineWidth);
    	
    	cy += LineSpacing;

    	chkMainGrid = new JCheckBox("Main Grid, Line Width");
    	chkMainGrid.setSize(chkWidth, EdtHeight);
    	chkMainGrid.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
    	chkMainGrid.setSelected(dSetup.showGrid());
    	totalGUI.add(chkMainGrid);
    	
        SpinnerNumberModel spmMainGrid = new SpinnerNumberModel(dSetup.gridLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        spnMainGrid = new JSpinner(spmMainGrid);
        spnMainGrid.setSize(SpnWidth, EdtHeight);
        spnMainGrid.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
        spnMainGrid.setBorder(blackBorder);
    	totalGUI.add(spnMainGrid);
    	
    	cy += LineSpacing;

    	clblMainGrid = new ColorJLabel(dSetup.getGridColor());
    	clblMainGrid.setSize(SpnWidth, EdtHeight);
    	clblMainGrid.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
    	clblMainGrid.setBorder(blackBorder);
    	totalGUI.add(clblMainGrid);
    	
    	btnMainGridColor = new JButton("Edit Color...");
    	btnMainGridColor.setSize(BtnColorWidth, BtnHeight);
    	btnMainGridColor.setLocation(cx + chkWidth + TClear - TMargin - BtnColorWidth, cy + (LineSpacing - BtnHeight)/2);
    	btnMainGridColor.addActionListener(this);
    	totalGUI.add(btnMainGridColor);
    	
    	cy += LineSpacing;

    	chkSubGrid = new JCheckBox("Minor Grid, Line Width");
    	chkSubGrid.setSize(chkWidth, EdtHeight);
    	chkSubGrid.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
    	chkSubGrid.setSelected(dSetup.showMinorGrid());
    	totalGUI.add(chkSubGrid);
    	
        SpinnerNumberModel spmSubGrid = new SpinnerNumberModel(dSetup.minorGridLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        spnSubGrid = new JSpinner(spmSubGrid);
        spnSubGrid.setSize(SpnWidth, EdtHeight);
        spnSubGrid.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
        spnSubGrid.setBorder(blackBorder);
    	totalGUI.add(spnSubGrid);
    	
    	cy += LineSpacing;

    	clblSubGrid = new ColorJLabel(dSetup.getMinorGridColor());
    	clblSubGrid.setSize(SpnWidth, EdtHeight);
    	clblSubGrid.setLocation(cx + chkWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
    	clblSubGrid.setBorder(blackBorder);
    	totalGUI.add(clblSubGrid);
    	
    	btnSubGridColor = new JButton("Edit Color...");
    	btnSubGridColor.setSize(BtnColorWidth, BtnHeight);
    	btnSubGridColor.setLocation(cx + chkWidth + TClear - TMargin - BtnColorWidth, cy + (LineSpacing - BtnHeight)/2);
    	btnSubGridColor.addActionListener(this);
    	totalGUI.add(btnSubGridColor);
    	
    	cx += LeftSideWidth + WinMargin*2;
    	cy = WinMargin;
    	
        JLabel lbl4 = new JLabel("Charging Event Colors"); 
        lbl4.setSize(RightSideWidth, LblHeight);
        lbl4.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl4);
    	
    	cy += LineSpacing;
    	cx += WinMargin;
    	
        JLabel lbl5 = new JLabel(ChargerTypes.L1.shortName); 
        lbl5.setSize(LblWidth1, LblHeight);
        lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl5);

    	clblL1 = new ColorJLabel(dSetup.getL1Color());
    	clblL1.setSize(SpnWidth, EdtHeight);
    	clblL1.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing - EdtHeight)/2);
    	clblL1.setBorder(blackBorder);
    	totalGUI.add(clblL1);

    	btnL1Color = new JButton("Edit Color...");
    	btnL1Color.setSize(BtnColorWidth, BtnHeight);
    	btnL1Color.setLocation(cx + LblWidth1 + TClear + SpnWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
    	btnL1Color.addActionListener(this);
    	totalGUI.add(btnL1Color);

    	cy += LineSpacing;
    	
        JLabel lbl6 = new JLabel(ChargerTypes.L2.shortName); 
        lbl6.setSize(LblWidth1, LblHeight);
        lbl6.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl6);

    	clblL2 = new ColorJLabel(dSetup.getL2Color());
    	clblL2.setSize(SpnWidth, EdtHeight);
    	clblL2.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing - EdtHeight)/2);
    	clblL2.setBorder(blackBorder);
    	totalGUI.add(clblL2);

    	btnL2Color = new JButton("Edit Color...");
    	btnL2Color.setSize(BtnColorWidth, BtnHeight);
    	btnL2Color.setLocation(cx + LblWidth1 + TClear + SpnWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
    	btnL2Color.addActionListener(this);
    	totalGUI.add(btnL2Color);

    	cy += LineSpacing;
  	
        JLabel lbl7 = new JLabel(ChargerTypes.DC.shortName); 
        lbl7.setSize(LblWidth1, LblHeight);
        lbl7.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
    	totalGUI.add(lbl7);

    	clblDC = new ColorJLabel(dSetup.getDCColor());
    	clblDC.setSize(SpnWidth, EdtHeight);
    	clblDC.setLocation(cx + LblWidth1 + TClear, cy + (LineSpacing - EdtHeight)/2);
    	clblDC.setBorder(blackBorder);
    	totalGUI.add(clblDC);

    	btnDCColor = new JButton("Edit Color...");
    	btnDCColor.setSize(BtnColorWidth, BtnHeight);
    	btnDCColor.setLocation(cx + LblWidth1 + TClear + SpnWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
    	btnDCColor.addActionListener(this);
    	totalGUI.add(btnDCColor);
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void screenToData() {
		dSetup.setTitleFontSize(Integer.parseInt(spnTitleFont.getValue().toString()));
		dSetup.setAxesLineWidth(Integer.parseInt(spnAxesLineWidth.getValue().toString()));
		dSetup.setBoxPlotLineWidth(Integer.parseInt(spnBxpLineWidth.getValue().toString()));
		dSetup.setGridLineWidth(Integer.parseInt(spnMainGrid.getValue().toString()));
		dSetup.setMinorGridLineWidth(Integer.parseInt(spnSubGrid.getValue().toString()));
		
		dSetup.setShowGrid(chkMainGrid.isSelected());
		dSetup.setShowMinorGrid(chkSubGrid.isSelected());
		
		int numVehicles = dSetup.numVehicles();
		for (int i=0; i<numVehicles; i++) {
			String vTitle = new String(vehOptionsPanel.edtTitles[i].getText());
			if (vTitle.length() < 1) {
				int vID = dSetup.getVehAtDisplayPos(i).vehID();
				vTitle = new String(avms.vehModelsSetup()[vID].shortName);
			}
			
			boolean bShown = vehOptionsPanel.chkShowVeh[i].isSelected();
			int pdmLW = Integer.parseInt(vehOptionsPanel.spnPDM[i].getValue().toString());
			int cdfLW = Integer.parseInt(vehOptionsPanel.spnCDF[i].getValue().toString());
			
			dSetup.getVehAtDisplayPos(i).displayedTitle = vTitle;
			dSetup.getVehAtDisplayPos(i).isShown = bShown;
			dSetup.getVehAtDisplayPos(i).pdmLineWidth = pdmLW;
			dSetup.getVehAtDisplayPos(i).cdfLineWidth = cdfLW;
			
			dSetup.getVehAtDisplayPos(i).pdmRed = vehOptionsPanel.pdmRed[i];
			dSetup.getVehAtDisplayPos(i).pdmGreen = vehOptionsPanel.pdmGreen[i];
			dSetup.getVehAtDisplayPos(i).pdmBlue = vehOptionsPanel.pdmBlue[i];
			
			dSetup.getVehAtDisplayPos(i).cdfRed = vehOptionsPanel.cdfRed[i];
			dSetup.getVehAtDisplayPos(i).cdfGreen = vehOptionsPanel.cdfGreen[i];
			dSetup.getVehAtDisplayPos(i).cdfBlue = vehOptionsPanel.cdfBlue[i];
			
			dSetup.getVehAtDisplayPos(i).bxfRed = vehOptionsPanel.bxpRed[i];
			dSetup.getVehAtDisplayPos(i).bxfGreen = vehOptionsPanel.bxpGreen[i];
			dSetup.getVehAtDisplayPos(i).bxfBlue = vehOptionsPanel.bxpBlue[i];
		}
	}
}
