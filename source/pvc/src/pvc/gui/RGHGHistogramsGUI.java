package pvc.gui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.calc.*;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;
import pvc.gui.comp.*;
import pvc.runners.*;

@SuppressWarnings("serial")
public class RGHGHistogramsGUI extends JFrame implements ActionListener, ChangeListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int TClearance = 2;
	private static final int EdtHeight = LblHeight + TClearance;
	private static final int BtnHeight = EdtHeight + TClearance*2;
	private static final int LineSpacing = BtnHeight + TClearance;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	
	private static final int SliderTextBoxWidth = 120;
	private static final int SliderBarWidth = 280;
	private static final int ValuesFontSize = 11;
	private static final int ScrollBarAllowance = 18;
	private static final int SolSetListNumLinespacingHeight = 3;
	
	
	private static final float GPanelMaxHeightFracScreen = 0.8f;
	private static final float GPanelMaxWidthToHeight = 1.25f;
	private static final float PanelMaxWidthFracScreen = 0.9f;
	
	private static final int DefaultMaxNumAxisDiv = 12;


	//GUI Objects
    private SliderBarsPanelMaker.CDSliderBarsJPanel sbPanel;
	private JButton btnSave, btnUnits, btnEditSliderBars, btnEditOtherParam, btnEditDsiplay, btnEditAxes, btnSaveScenario, btnManageScenarios;
	private JCheckBox chkContMode, chkMfgGHG;
	private JList<String> selSolSet;
	private JFileChooser dlgSaveFolderChooser;
	private GHGHistogramsPanelMaker.GHGHistogramsJPanel gPanel;

	
	//Data Objects
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private AnalysisVehModelsSetup avms;
	private WIITModel wiitModel;
	private SliderBarsManager sbarMan;
	private FEcoSimsC fecoSims;
	private FEcoSimsGenerator fsG;
	private UsePhaseSSimulator upSim;
	private UsePhaseSSimulator.InputStructure upsInput;
	private NoneBatteryMfgGHGModel nonBatMfgGHGModel;
	private BEVRepCosts bevRepCosts;
	private BEVCommercialModel bevMoreCommVeh;
	private ChargingEffManager chEffMan;
	
	private boolean reScaleAxes;
	private GHGAxesSetup axesSetup;
	private GHGDisplaySetup displaySetup;
	
	private SavedScenariosManager ssMan;

	
	//Constructor
	public RGHGHistogramsGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, boolean resetAxes) {
		//Call Super
		super("Green House Gas Emissions Analysis");
		
		//Set Data Objects
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		
		reScaleAxes = resetAxes;
		axesSetup = GHGAxesSetup.createViaReadingFile(fs.getFilePath_ghgAxes(aID));
		
		fecoSims = new FEcoSimsC(fs, aID);
		
		try {
			avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
			wiitModel = WIITModel.readWIITModel(fs, aID, avms);
			bevMoreCommVeh = new BEVCommercialModel(fs, aID, wiitModel);
			sbarMan = new SliderBarsManager(fs, aID, avms, wiitModel, bevMoreCommVeh);
		} catch (Exception e) {
			failedLaunch();
			return;
		}
		
		nonBatMfgGHGModel = new NoneBatteryMfgGHGModel(fs, aID, avms);
		bevRepCosts = new BEVRepCosts(fs, aID, wiitModel);

		fsG = FEcoSimsGenerator.createFEcoSimsGenerator(fs, aID, avms, wiitModel);
		if (fsG == null) {
			failedLaunch();
			return;
		}
		
		chEffMan = ChargingEffManager.readFromFile(avms.vehModelsSetup(), fs.getFilePath_vehChgEfficiencies(aID));
		if (chEffMan == null) {
			chEffMan = new ChargingEffManager(fsG);
			chEffMan.save(fs.getFilePath_vehChgEfficiencies(aID));
		}
				
		ChgCasesLookup chgLoo = ChgCasesLookup.read_wHistograms(fecoSims, fsG);
		if (chgLoo == null) {
			HourlyProfileCurve costProfile = new HourlyProfileCurve();
			HourlyProfileCurve ghgProfile = new HourlyProfileCurve();
			
			costProfile.readFromFile(fs.getFilePath_gridCost(aID));
			ghgProfile.readFromFile(fs.getFilePath_gridGHG(aID));
			
			RWChargingSummaries runner = new RWChargingSummaries(fs, aID, pMP, CurVisualizationType.GHGHistograms, costProfile, ghgProfile);
			RunStatusWindow stWindow = new RunStatusWindow(runner, "Post-Processing Charging Events");
			stWindow.startRun();
			return;
		}
		
		FEcoCasesLookup fecoLoo = FEcoCasesLookup.read_wHistograms(fecoSims, fsG);
		if (fecoLoo == null) {
			
			RWFecoSummaries runner = new RWFecoSummaries(fs, aID, pMP, CurVisualizationType.GHGHistograms);
			RunStatusWindow stWindow = new RunStatusWindow(runner, "Post-Processing Fuel Economy Simulations");
			stWindow.startRun();
			return;
		}
		
		upSim = new UsePhaseSSimulator(fecoLoo, chgLoo, fsG, chEffMan);
		upsInput = upSim.createInputStructure();
		
		displaySetup = GHGDisplaySetup.readExisting(fs, aID);
		if (displaySetup == null) {
			displaySetup = new GHGDisplaySetup(avms.vehModelsSetup(), fsG.fsofModels());
			displaySetup.save(fs.getFilePath_ghgDisplay(aID));
		}
		
		ssMan = new SavedScenariosManager(fs, aID);
				
		finalizeAndShow();
	}
	private void finalizeAndShow() {
		//Dialog for choosing folder
		dlgSaveFolderChooser = new JFileChooser();
		dlgSaveFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dlgSaveFolderChooser.setDialogTitle("Choose Output Folder");
		dlgSaveFolderChooser.setCurrentDirectory(new File(fs.getFolderPath_defaultResults(aID)));
		
		//Create GUI
		JPanel ct = createContentPane();
		setContentPane(ct);

		//Apply status of slider bars and other display options
		selSolSet.setSelectedIndex(sbarMan.rvStatus().solSetID());
		SliderBarsManager.annualMilesTravelledIncCurSolSet = fecoSims.getAnnualMiles(sbarMan.rvStatus().solSetID());

		boolean contMode = sbarMan.rvStatus().contIntMode();
		chkContMode.setSelected(contMode);
		if (chkMfgGHG != null) chkMfgGHG.setSelected(sbarMan.rvStatus().includeMfgGHG());
		
		sbPanel.setContinuousMode(contMode);
		sbPanel.setSbarPositions(sbarMan.rvStatus().avsbValues());
		
		sbPanel.updateCaptions(sbarMan);
		sbPanel.updateValueLabels(sbarMan);
		barsToGraph();
		
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
        
        //MfgGHG reminder
        MfgGHGReminder.resetReminder();
       if (chkMfgGHG!=null) if (chkMfgGHG.isSelected()) MfgGHGReminder.issueReminder();
	}
	
	
	private void exitProcedure() {
		dispose();
		pMP.subModuleFinished();
	}
	private void failedLaunch() {
		JOptionPane.showMessageDialog(null, "Data Files Error Prevented Launching of this Module.", 
				"Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
		exitProcedure();
	}
	
	
	//Processing action events from buttons and check boxes
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnSaveScenario) {
			SaveCurScenarioDialog dlg = new SaveCurScenarioDialog();
			
			if (dlg.okPressed()) {
				String stShort = dlg.getSaveScenario_shortDescription();
				String stLong = dlg.getSaveScenario_longDescription();
				ssMan.saveCurrentSateAsNewScenario(stShort, stLong);
			}
		}
		if (source == btnManageScenarios) {
			ManageScenariosDialog dlg = new ManageScenariosDialog(ssMan);
			if (!dlg.okPressed()) return;
			
			int ssID = Math.max(0,  dlg.selectedScenarioID());
			
			ssMan.loadScenarioFiles(ssID);
			dispose();			
			new RGHGHistogramsGUI(fs, aID, pMP, false);
		}

		if (source == btnEditSliderBars) {
			dispose();
			new SliderBarsEditorGUI(fs, aID, pMP, CurVisualizationType.GHGHistograms);
			return;
		}
		if (source == btnUnits) {
			UnitsDialog dlg = new UnitsDialog(fs, aID);
			if (dlg.okPressed()) {
				sbPanel.updateCaptions(sbarMan);
				sbPanel.updateValueLabels(sbarMan);
				reScaleAxes = true;
				barsToGraph();
			}
			return;
		}
		if (source == chkMfgGHG) {
	        if (chkMfgGHG.isSelected()) MfgGHGReminder.issueReminder();
			sbarMan.rvStatus().setIncludeMfgGHG(chkMfgGHG.isSelected());
			sbarMan.rvStatus().save();
			barsToGraph();
			return;
		}
		if (source == chkContMode) {
			sbarMan.rvStatus().setContIntMode(chkContMode.isSelected());
			sbarMan.rvStatus().save();
			
			sbPanel.setContinuousMode(sbarMan.rvStatus().contIntMode());
			sbPanel.updateValueLabels(sbarMan);
			barsToGraph();
			return;
		}
		if (source == btnEditOtherParam) {
			EditOtherParamsDialog dlg = new EditOtherParamsDialog(new APOtherParams(fs, aID, avms, wiitModel));
			
			if (dlg.okPressed()) {
				//Check if hourly curves have been modified, and if so, force re-generation of hourly charging profiles
				if (dlg.hourlyCurvesEdited()) {
					fs.deleteAllChgTimingOptResults(aID);
				}
			
				//Re-Launch self
				setVisible(false);
				dispose();
				new RGHGHistogramsGUI(fs, aID, pMP, false);
			}
			return;
		}
		if (source == btnSave) {
			int dlgReturnValue = dlgSaveFolderChooser.showSaveDialog(this);
			
			if (dlgReturnValue == JFileChooser.APPROVE_OPTION) {
				String outputFolder = dlgSaveFolderChooser.getSelectedFile().getPath();
				File f= new File(outputFolder);
				if (!f.exists()) {
					JOptionPane.showMessageDialog(null, "Selected Folder Does Not Exist", 
							"Output Failed", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//Attempt to generate output files
				try {
					sbarMan.prepUPInputs(upsInput, upSim, nonBatMfgGHGModel.vehicleLifetimeMiles,
							nonBatMfgGHGModel.gCO2perKgVehicle_lowEnd, nonBatMfgGHGModel.gCO2perKgVehicle_highEnd, 
							bevRepCosts.dollarsPerMile, bevRepCosts.dollarsPerDay);
	
					if (reScaleAxes||(axesSetup == null)) autoScaleAxes(upsInput);
					
					UsePhaseSSimulator.VehGHGAnalysisResult[] vehHstRes = upSim.analyzeGHG(sbarMan.rvStatus().solSetID(), upsInput, 
							axesSetup.limGCO2perMile(), axesSetup.numBins());
					
					ReportsGenerator.genGHGReportFiles(outputFolder, sbarMan, fecoSims, vehHstRes, avms.vehModelsSetup(), displaySetup);					
					JOptionPane.showMessageDialog(null, "Output Files Generation Complete", 
							"Output Successful", JOptionPane.OK_OPTION|JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "An Error Occurred while Generating or Saving Output Files", 
							"Output Failed", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				}				
			}
			return;
		}
		if (source == btnEditDsiplay) {
			GHGDisplaySetup tmpDisplaySetup = new GHGDisplaySetup(displaySetup);
			
			while (true) {
				GHGEditDisplayDialog dlg = new GHGEditDisplayDialog(avms, tmpDisplaySetup);
				
				if (dlg.okPressed()) {
					//Save new settings
					displaySetup = new GHGDisplaySetup(dlg.displaySetup());
					displaySetup.save(fs.getFilePath_ghgDisplay(aID));
					
					//Re-Launch self
					setVisible(false);
					dispose();
					new RGHGHistogramsGUI(fs, aID, pMP, false);
					return;
				} else if (dlg.reOrderInvoked()) {
					tmpDisplaySetup = new GHGDisplaySetup(dlg.displaySetup());
				}
				else {
					return;
				}
			}
		}

		if (source == btnEditAxes) {
			GHGAxesDialog dlg = new GHGAxesDialog(axesSetup);
			if (dlg.okPressed()) {
				axesSetup = new GHGAxesSetup(dlg.getAxesSetup());
				axesSetup.save(fs.getFilePath_ghgAxes(aID));
				gPanel.setAxes(axesSetup);
				barsToGraph();
			}
			return;
		}
	}
	//Processing actions from slider bars
	@Override
	public void stateChanged(ChangeEvent e) {
		if (sbPanel.isIgnoringChangeEvents()) return;
				
		sbPanel.grabSliderBarValues(sbarMan);
		sbarMan.rvStatus().save();
		sbPanel.updateValueLabels(sbarMan);
		barsToGraph();
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int gPanelHscHeight = (int)(GPanelMaxHeightFracScreen*Toolkit.getDefaultToolkit().getScreenSize().height);
        int sPanelPrefWidth = SliderTextBoxWidth + SliderBarWidth + TMargin*3;
        
        int gPanelMaxWidth = (int)(PanelMaxWidthFracScreen*Toolkit.getDefaultToolkit().getScreenSize().width) 
        		- (sPanelPrefWidth + ScrollBarAllowance + WinMargin*3);
        int gPanelWidth = Math.min(gPanelMaxWidth, (int)(GPanelMaxWidthToHeight*gPanelHscHeight));
        
        gPanel = GHGHistogramsPanelMaker.createGHGHistogramsPanel(gPanelWidth, displaySetup);
        if (axesSetup != null) gPanel.setAxes(axesSetup);
        int gPanelPrefHeight = gPanel.getPreferredSize().height;
        
        int lstSolSetHeight = LineSpacing*SolSetListNumLinespacingHeight;       
        int topPartHeight = LineSpacing*2 + lstSolSetHeight + WinMargin;
        int bottomPartHeight = BigBtnHeight*3 + TMargin*2;
        int hscHeight = gPanelHscHeight - (topPartHeight + bottomPartHeight + WinMargin*2);
        int hscWidth = sPanelPrefWidth;
        
        sbPanel = SliderBarsPanelMaker.createPanel(sbarMan, CurVisualizationType.GHGHistograms, this, 
        		LblHeight, EdtHeight, LineSpacing, TMargin, ValuesFontSize, SliderBarWidth, SliderTextBoxWidth);
        if ((sbPanel.getPreferredSize().height + TClearance) > hscHeight) hscWidth += ScrollBarAllowance;
        
    	gPanel.setBorder(blackBorder);
    	
        int gPanelHscWidth = gPanelWidth;        
        if (gPanelPrefHeight < gPanelHscHeight) {
        	gPanel.setSize(gPanelWidth, gPanelHscHeight);
	        gPanel.setLocation(hscWidth+WinMargin*3, WinMargin);
	        totalGUI.add(gPanel);
        } else {
        	gPanelHscWidth += ScrollBarAllowance;
        	
        	gPanel.setAutoscrolls(true);
        	JScrollPane hsc = new JScrollPane(gPanel);
        	hsc.setSize(gPanelHscWidth, gPanelHscHeight);
        	hsc.setLocation(hscWidth+WinMargin*3, WinMargin);
        	totalGUI.add(hsc);
        }
        
        int panelWidth = hscWidth + gPanelHscWidth + WinMargin*4;
        int panelHeight = gPanelHscHeight + WinMargin*2;
        
        int buttonsWidth = (hscWidth - TMargin)/2;        
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        chkContMode = new JCheckBox("Continuous Interpolation Mode");
        chkContMode.setSize(buttonsWidth, EdtHeight);
        chkContMode.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        chkContMode.addActionListener(this);
        totalGUI.add(chkContMode);
        
        btnEditAxes = new JButton("Edit Axes...");
        btnEditAxes.setSize(buttonsWidth, BtnHeight);
        btnEditAxes.setLocation(cx + hscWidth - buttonsWidth, cy + (LineSpacing - BtnHeight)/2);
        btnEditAxes.addActionListener(this);
        totalGUI.add(btnEditAxes);
        
        cy += LineSpacing;
        
        if (sbarMan.hasMfgGHG()) {
        	chkMfgGHG = new JCheckBox("Include Manufacturing GHG");
        	chkMfgGHG.setSize(buttonsWidth, EdtHeight);
        	chkMfgGHG.setLocation(cx, cy + (LineSpacing - EdtHeight)/2);
        	chkMfgGHG.addActionListener(this);
            totalGUI.add(chkMfgGHG);
        }
        
        btnEditDsiplay = new JButton("Edit Display...");
        btnEditDsiplay.setSize(buttonsWidth, BtnHeight);
        btnEditDsiplay.setLocation(cx + hscWidth - buttonsWidth, cy + (LineSpacing - BtnHeight)/2);
        btnEditDsiplay.addActionListener(this);
        totalGUI.add(btnEditDsiplay);
        
        cy += LineSpacing + WinMargin;
        
        JLabel lblSolSet = new JLabel("Current Simulation Run");
        lblSolSet.setSize(buttonsWidth, LblHeight);
        lblSolSet.setLocation(cx, cy - TClearance);
        lblSolSet.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lblSolSet);

        selSolSet = new JList<String>();
        selSolSet.setLocation(0,0);
        selSolSet.setSize(buttonsWidth, lstSolSetHeight);
        selSolSet.setBorder(blackBorder);
        
        selSolSet.setListData(fecoSims.getTitlesArray());
        
        selSolSet.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		solSetChanged();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selSolSet);
        sc1.setLocation(cx + hscWidth - buttonsWidth, cy);
        sc1.setSize(buttonsWidth, lstSolSetHeight);  
        totalGUI.add(sc1);
        
        cy += lstSolSetHeight + WinMargin;
        
        if (sbPanel.getPreferredSize().height < hscHeight) {
        	sbPanel.setLocation(cx, cy);
        	sbPanel.setSize(hscWidth, hscHeight);
			totalGUI.add(sbPanel);
        } else {
	        JScrollPane sc2 = new JScrollPane(sbPanel);
			sc2.setLocation(cx, cy);
			sc2.setSize(hscWidth, hscHeight);
			totalGUI.add(sc2);
        }
		
        
        cy += hscHeight + WinMargin;
        
        btnSaveScenario = new JButton("Save Current Scenario...");
        btnSaveScenario.setSize(buttonsWidth, BigBtnHeight);
        btnSaveScenario.setLocation(cx, cy);
        btnSaveScenario.addActionListener(this);
        totalGUI.add(btnSaveScenario);
        
        btnManageScenarios = new JButton("Load/Manage Scenarios...");
        btnManageScenarios.setSize(buttonsWidth, BigBtnHeight);
        btnManageScenarios.setLocation(cx + hscWidth - buttonsWidth, cy);
        btnManageScenarios.addActionListener(this);
        totalGUI.add(btnManageScenarios);
        
        cy += BigBtnHeight + TMargin;
        
        btnEditSliderBars = new JButton("Edit Scenario Parameters...");
        btnEditSliderBars.setSize(buttonsWidth, BigBtnHeight);
        btnEditSliderBars.setLocation(cx, cy);
        btnEditSliderBars.addActionListener(this);
        totalGUI.add(btnEditSliderBars);
        
        btnEditOtherParam = new JButton("Edit Other Parameters...");
        btnEditOtherParam.setSize(buttonsWidth, BigBtnHeight);
        btnEditOtherParam.setLocation(cx + hscWidth - buttonsWidth, cy);
        btnEditOtherParam.addActionListener(this);
        totalGUI.add(btnEditOtherParam);
        
        cy += BigBtnHeight + TMargin;
        
        btnSave = new JButton("Save Current Result...");
        btnSave.setSize(buttonsWidth, BigBtnHeight);
        btnSave.setLocation(cx, cy);
        btnSave.addActionListener(this);
        totalGUI.add(btnSave);
        
        btnUnits = new JButton("Change Units...");
        btnUnits.setSize(buttonsWidth, BigBtnHeight);
        btnUnits.setLocation(cx + hscWidth - buttonsWidth, cy);
        btnUnits.addActionListener(this);
        totalGUI.add(btnUnits);
               
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void solSetChanged() {
		int curSel = selSolSet.getSelectedIndex();
		if (curSel < 0) return;
		
		sbarMan.rvStatus().setSolSetID(curSel);
		sbarMan.rvStatus().save();
		SliderBarsManager.annualMilesTravelledIncCurSolSet = fecoSims.getAnnualMiles(sbarMan.rvStatus().solSetID());
		SliderBarsManager.daysOfTravelPerYearIncCurSolSet = fecoSims.getAnnualDrivingDays(sbarMan.rvStatus().solSetID());
		sbPanel.updateValueLabels(sbarMan);
		barsToGraph();
	}
	private void barsToGraph() {
		sbarMan.prepUPInputs(upsInput, upSim, nonBatMfgGHGModel.vehicleLifetimeMiles,
				nonBatMfgGHGModel.gCO2perKgVehicle_lowEnd, nonBatMfgGHGModel.gCO2perKgVehicle_highEnd, 
				bevRepCosts.dollarsPerMile, bevRepCosts.dollarsPerDay);

		if (reScaleAxes||(axesSetup == null)) autoScaleAxes(upsInput);
		
		UsePhaseSSimulator.VehGHGAnalysisResult[] vehHstRes = upSim.analyzeGHG(sbarMan.rvStatus().solSetID(), upsInput, 
				axesSetup.limGCO2perMile(), axesSetup.numBins());		
		gPanel.setPlot(vehHstRes);
	}
	private void autoScaleAxes(UsePhaseSSimulator.InputStructure upsIn) {
		//Identify maximum GHG value then re-scale axes via default settings
		float maxGCO2perMile = upSim.calcMaxGramsCO2perMile(sbarMan.rvStatus().solSetID(), upsIn);
		
		axesSetup = GHGAxesSetup.createViaAutoScale(maxGCO2perMile, DefaultMaxNumAxisDiv);
		axesSetup.save(fs.getFilePath_ghgAxes(aID));
		reScaleAxes = false;
		
		gPanel.setAxes(axesSetup);
	}
}
