package pvc.gui;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import pvc.datamgmt.*;
import pvc.gui.comp.*;

@SuppressWarnings("serial")
public class SliderBarsEditorGUI extends JFrame implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;

	private static final float BarsPanelMaxHeightFracScreen = 0.45f;
	private static final float BarsPanelMaxWidthFracScreen = 0.8f;
	private static final int TMargin = 4;
	private static final int ChkMainCaptionWidth = 300;
	private static final int LblValueWidth = 110;
	private static final int EditButtonWidth = 70;
	private static final int ValuesFontSize = 11;
	private static final int ScrollBarAllowance = 18;
	
	//GUI Objects
	private EditSBarsPanelMaker.SBarsJPanel sbPanel;
	private JButton btnSave, btnOrder, btnUnits;


	//Data Objects
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private CurVisualizationType cvType;
	private SliderBarsManager sbarMan;
	private boolean sbarsEdited, triggerAutoScaleAxes;

	//Constructor used for Launching from Visualization modules (GHG Histograms, Cost Bars, Cost versus GHG)
	public SliderBarsEditorGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, CurVisualizationType curVisualization) {
		//Call Super
		super("Setup Scenario Parameters");
		
		//Set Variables
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		cvType = curVisualization;
		AnalysisVehModelsSetup avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
		WIITModel wiitModel = WIITModel.readWIITModel(fs, aID, avms);
		sbarMan = new SliderBarsManager(fs, aID, avms, wiitModel, new BEVCommercialModel(fs, aID, wiitModel));
		sbarsEdited = false;
		triggerAutoScaleAxes = false;

		//Rest of Launch Procedure
		completeLaunchProcedure();
	}
	//Constructor used for Launching from Bars Re-Ordering Dialog
	public SliderBarsEditorGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, CurVisualizationType curVisualization, 
			SliderBarsManager sliderBarsManager, boolean hasBeenEdited) {
		//Call Super
		super("Setup Scenario Parameters");
		
		//Set Variables
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		cvType = curVisualization;
		sbarMan = sliderBarsManager;
		sbarsEdited = hasBeenEdited;
		
		//Rest of Launch Procedure
		completeLaunchProcedure();
	}
	private void completeLaunchProcedure() {
		//Create GUI
		JPanel ct = createContentPane();
		setContentPane(ct);

		//Values to screen
		sbPanel.barsToScreen(sbarMan);
		
		//Calculate insets, set position to screen center
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

        //Set window closing operation
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	            exitProcedure();
	        }
	    });

        //Set size and Launch
        setSize(winWidth, winHeight);
        setResizable(false);
        setVisible(true);
	}
	
	private void exitProcedure() {
		if (sbarsEdited) {
			int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Scenario settings have been edited. Do you wish to save changes?", 
					"Please Check", JOptionPane.YES_NO_OPTION);
			if (dlgYesNoReturn == JOptionPane.YES_OPTION) {
				sbarMan.save();
			}
		}
		
		dispose();
		
		switch (cvType) {
		case CostBars:
			new RCostBarsGUI(fs, aID, pMP, triggerAutoScaleAxes);
			break;
		case CostVsGHG:
			new RCostVsGHGGUI(fs, aID, pMP, triggerAutoScaleAxes);
			break;
		case GHGHistograms:
			new RGHGHistogramsGUI(fs, aID, pMP, triggerAutoScaleAxes);
			break;
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == btnSave) {
			sbarMan.save();
			sbarsEdited = false;
			return;
		}
		if (source == btnUnits) {
			UnitsDialog dlg = new UnitsDialog(fs, aID);
			if (dlg.okPressed()) {
				sbPanel.barsToScreen(sbarMan);
				triggerAutoScaleAxes = true;
			}
			return;
		}
		
		if (source == btnOrder) {
			launchEditOrderWindow();
			return;
		}
		
		for (int i=0; i<sbPanel.visibleSBars.length; i++) {
			if (source == sbPanel.visibleSBars[i].chkCaption) {
				sbarMan.setSBarUserShow(i, sbPanel.visibleSBars[i].chkCaption.isSelected());
				sbarsEdited = true;
				sbPanel.barsToScreen(sbarMan);
				return;
			}
			
			if (source == sbPanel.visibleSBars[i].btnEdit) {
				SliderBarEditorDialog dlg = new SliderBarEditorDialog(sbarMan, i);
				if (dlg.okPressed()) {
					float[] uaValuesBelow = dlg.uaValuesBelow();
					float[] uaValuesAbove = dlg.uaValuesAbove();
					
					float[] sValuesBelow = new float[0];
					float[] sValuesAbove = new float[0];
					
					if (uaValuesBelow != null) {
						sValuesBelow = new float[uaValuesBelow.length];
						for (int j=0; j<sValuesBelow.length; j++) sValuesBelow[j] = sbarMan.toSliderBarValue(i, uaValuesBelow[j]);
					}					
					if (uaValuesAbove != null) {
						sValuesAbove = new float[uaValuesAbove.length];
						for (int j=0; j<sValuesAbove.length; j++) sValuesAbove[j] = sbarMan.toSliderBarValue(i, uaValuesAbove[j]);
					}
					
					sbarMan.getSBar(i).editValues(sValuesBelow, sValuesAbove);
					sbarMan.rvStatus().resetToBaseline(i);
					
					sbarsEdited = true;
					sbPanel.barsToScreen(sbarMan);
				}
				return;
			}
		}
	}
	
	private void launchEditOrderWindow() {
		dispose();
		new SliderBarsOrderGUI(fs, aID, pMP, cvType, sbarMan, sbarsEdited);
	}
	
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        int cx = WinMargin + TMargin;
        int cy = WinMargin;
        
        cx += ChkMainCaptionWidth + TMargin;
        
        JLabel lbl1a = new JLabel("Limiting");
        lbl1a.setSize(LblValueWidth, LblHeight);
        lbl1a.setLocation(cx, cy);
        lbl1a.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl1a);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl2a = new JLabel("Current");
        lbl2a.setSize(LblValueWidth, LblHeight);
        lbl2a.setLocation(cx, cy);
        lbl2a.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl2a);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl3a = new JLabel("Baseline");
        lbl3a.setSize(LblValueWidth, LblHeight);
        lbl3a.setLocation(cx, cy);
        lbl3a.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl3a);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl4a = new JLabel("Current");
        lbl4a.setSize(LblValueWidth, LblHeight);
        lbl4a.setLocation(cx, cy);
        lbl4a.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl4a);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl5a = new JLabel("Limiting");
        lbl5a.setSize(LblValueWidth, LblHeight);
        lbl5a.setLocation(cx, cy);
        lbl5a.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl5a);

        cx = WinMargin + TMargin;
        cy += LblHeight;

        JLabel lbl0a = new JLabel("Show Slider Bar");
        lbl0a.setSize(ChkMainCaptionWidth, LblHeight);
        lbl0a.setLocation(cx, cy);
        totalGUI.add(lbl0a);

        cx += ChkMainCaptionWidth + TMargin;
        
        JLabel lbl1b = new JLabel("Value");
        lbl1b.setSize(LblValueWidth, LblHeight);
        lbl1b.setLocation(cx, cy);
        lbl1b.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl1b);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl2b = new JLabel("Limit");
        lbl2b.setSize(LblValueWidth, LblHeight);
        lbl2b.setLocation(cx, cy);
        lbl2b.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl2b);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl3b = new JLabel("Value");
        lbl3b.setSize(LblValueWidth, LblHeight);
        lbl3b.setLocation(cx, cy);
        lbl3b.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl3b);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl4b = new JLabel("Limit");
        lbl4b.setSize(LblValueWidth, LblHeight);
        lbl4b.setLocation(cx, cy);
        lbl4b.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl4b);
        
        cx += LblValueWidth + TMargin;
               
        JLabel lbl5b = new JLabel("Value");
        lbl5b.setSize(LblValueWidth, LblHeight);
        lbl5b.setLocation(cx, cy);
        lbl5b.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl5b);
        
        
        cy += LblHeight + TMargin;
        cx = WinMargin;
        
        int sbarsPanelWidhtAllowance = (int)(BarsPanelMaxWidthFracScreen * Toolkit.getDefaultToolkit().getScreenSize().width) - 2;
        int sbarsPanelHeightAllowance = (int)(BarsPanelMaxHeightFracScreen * Toolkit.getDefaultToolkit().getScreenSize().height) - 2;
        
        sbPanel = EditSBarsPanelMaker.createSBarsPanel(sbarMan, this, TMargin, EdtHeight, BtnHeight, LineSpacing, ChkMainCaptionWidth, 
        		LblValueWidth, EditButtonWidth, ValuesFontSize);
        
        int sbPanelWidth = sbPanel.getPreferredSize().width;
        int sbPanelHeight = sbPanel.getPreferredSize().height;
        
        int scWidth = Math.min(sbPanelWidth+2, (int)(BarsPanelMaxWidthFracScreen * Toolkit.getDefaultToolkit().getScreenSize().width));
        if (sbPanelWidth > sbarsPanelWidhtAllowance) sbarsPanelHeightAllowance += -ScrollBarAllowance;
        if (sbPanelHeight > sbarsPanelHeightAllowance) scWidth += ScrollBarAllowance;

        int panelWidth = scWidth + WinMargin*2;

        int scHeight = Math.min(sbPanelHeight+2, (int)(BarsPanelMaxHeightFracScreen * Toolkit.getDefaultToolkit().getScreenSize().height));
        
        if ((sbPanelWidth < scWidth)&&(sbPanelHeight < scHeight)) {
        	sbPanel.setSize(scWidth, scHeight);
        	sbPanel.setLocation(cx, cy);
        	sbPanel.setSize(scWidth, scHeight);  
	        totalGUI.add(sbPanel);
        }
        else {
	        JScrollPane sc1 = new JScrollPane(sbPanel);
	        sc1.setLocation(cx, cy);
	        sc1.setSize(scWidth, scHeight);  
	        totalGUI.add(sc1);
        }

        cy += scHeight + WinMargin;

        int bbtnWidth = Math.min((scWidth - 2*TMargin)/3, (4*LblValueWidth)/3);
        
        btnSave = new JButton("Save Current Setup");
        btnSave.setSize(bbtnWidth, BigBtnHeight);
        btnSave.setLocation(cx + scWidth - TMargin*2 - bbtnWidth*3, cy);
        btnSave.addActionListener(this);
        totalGUI.add(btnSave);
        
        btnOrder = new JButton("Change Order...");
        btnOrder.setSize(bbtnWidth, BigBtnHeight);
        btnOrder.setLocation(cx + scWidth - TMargin - bbtnWidth*2, cy);
        btnOrder.addActionListener(this);
        totalGUI.add(btnOrder);
               
        btnUnits = new JButton("Change Units...");
        btnUnits.setSize(bbtnWidth, BigBtnHeight);
        btnUnits.setLocation(cx + scWidth - bbtnWidth, cy);
        btnUnits.addActionListener(this);
        totalGUI.add(btnUnits);
        
        cy += BigBtnHeight;
        int panelHeight = cy + WinMargin;
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
}
