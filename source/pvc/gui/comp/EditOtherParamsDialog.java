package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.calc.FEcoSimsGenerator;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;

@SuppressWarnings("serial")
public class EditOtherParamsDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int TClearance = 2;
	private static final int EdtHeight = LblHeight + TClearance;
	private static final int BtnHeight = EdtHeight + TClearance*2;
	private static final int LineSpacing = BtnHeight + TClearance;
	private static final int BigBtnHeight = (3*BtnHeight)/2;

	private static final int LstHeightNumLineSpacing = 7;
	private static final int LstWidth = 480;
	private static final int Btn1Width = 150;
	private static final int Btn2Width = 80;
	
	//GUI Objects
	private JButton btnOK, btnCancel, btnEdit;
	private JList<String> selParameters;

	//Data
	private APOtherParams vEditableParams;
	private VehDeprModels deprModels;
	private LicIMModel licimModel;
	private NoneBatteryMfgGHGModel nonBatMfgGHGModel;
	private ChargingEffManager chEffMan;
	private HourlyProfileCurve ghgProfile;
	private HourlyProfileCurve costProfile;
	private HomeChargerCosts homeChgCosts;
	private BEVRepCosts bevRepCosts;
	
	private boolean okPressed, hourlyCurvesEdited, editsOccurred;
	
	public EditOtherParamsDialog(APOtherParams vEditableParameters) {
		super(null, "Other Editable Parameters", Dialog.ModalityType.APPLICATION_MODAL);

		okPressed = false;
		hourlyCurvesEdited = false; 
		editsOccurred = false;

		vEditableParams = vEditableParameters;
		readAllFromFiles();
		
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
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	        	exitCheck();
	        }
	    });
		
        setSize(winWidth, winHeight);
        setResizable(false);
		setVisible(true);		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			saveAlltoFiles();
			okPressed = true;
			dispose();	
		}
		if (source == btnCancel) {
			if (editsOccurred) {
				int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Parameter settings have been edited. Do you wish to Discard changes?", 
						"Please Check", JOptionPane.YES_NO_OPTION);
				if (dlgYesNoReturn == JOptionPane.NO_OPTION) return;
			}
			okPressed = false;
			dispose();	
		}
		if (source == btnEdit) {
			int selID = selParameters.getSelectedIndex();
			if (selID >= 0) launchEditorSubDialog(selID);
		}
	}
	private void exitCheck() {
		if (editsOccurred) {
			int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Parameter settings have been edited. Do you wish to Discard changes?", 
					"Please Check", JOptionPane.YES_NO_OPTION);
			if (dlgYesNoReturn == JOptionPane.NO_OPTION) return;
		}
		okPressed = false;
		dispose();
	}
	
	public boolean okPressed() {return okPressed;}
	public boolean hourlyCurvesEdited() {return hourlyCurvesEdited;}

	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int lstHeight = LstHeightNumLineSpacing * LineSpacing;
        int panelWidth  = LstWidth + WinMargin*2;
        int panelHeight = lstHeight + BigBtnHeight + WinMargin*3;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        selParameters = new JList<String>();
        selParameters.setLocation(0,0);
        selParameters.setSize(LstWidth, lstHeight);
        selParameters.setBorder(blackBorder);
        selParameters.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		updateButtonsActivity();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selParameters);
        sc1.setLocation(cx, cy);
        sc1.setSize(LstWidth, lstHeight);  
        totalGUI.add(sc1);

        selParameters.setListData(vEditableParams.getEditableParametersCaptions());
        
        cy += lstHeight + WinMargin;
        
        btnEdit = new JButton("Launch Editor...");
        btnEdit.setSize(Btn1Width, BigBtnHeight);
        btnEdit.setLocation(cx, cy);
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);
        totalGUI.add(btnEdit);
        
        cx += LstWidth - Btn2Width*2 - TClearance;
        
        btnOK = new JButton("OK");
        btnOK.setSize(Btn2Width, BigBtnHeight);
        btnOK.setLocation(cx, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        cx += Btn2Width + TClearance;
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(Btn2Width, BigBtnHeight);
        btnCancel.setLocation(cx, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	private void updateButtonsActivity() {
		int curSel = selParameters.getSelectedIndex();
		if (curSel < 0) btnEdit.setEnabled(false);
		else btnEdit.setEnabled(true);
	}
	
	private void readAllFromFiles() {
		FFStructure fs = vEditableParams.fs();
		int aID = vEditableParams.aID();
		AnalysisVehModelsSetup avms = vEditableParams.avms();
		WIITModel wiitModel = vEditableParams.wiitMod();
		
		deprModels = new VehDeprModels(fs, aID, avms);
		licimModel = new LicIMModel(fs, aID, avms);
		nonBatMfgGHGModel = new NoneBatteryMfgGHGModel(fs, aID, avms);

		chEffMan = ChargingEffManager.readFromFile(avms.vehModelsSetup(), fs.getFilePath_vehChgEfficiencies(aID));
		if (chEffMan == null) {
			FEcoSimsGenerator fsG = FEcoSimsGenerator.createFEcoSimsGenerator(fs, aID, avms, wiitModel);
			chEffMan = new ChargingEffManager(fsG);
		}
		
		ghgProfile = new HourlyProfileCurve();
		ghgProfile.readFromFile(fs.getFilePath_gridGHG(aID));
		
		costProfile = new HourlyProfileCurve();
		costProfile.readFromFile(fs.getFilePath_gridCost(aID));

		homeChgCosts = new HomeChargerCosts(fs, aID);
		bevRepCosts = new BEVRepCosts(fs, aID, wiitModel);
	}
	
	private void saveAlltoFiles() {
		FFStructure fs = vEditableParams.fs();
		int aID = vEditableParams.aID();

		deprModels.save();
		licimModel.save();
		nonBatMfgGHGModel.save();
		chEffMan.save(fs.getFilePath_vehChgEfficiencies(aID));
		
		ghgProfile.writeFile(fs.getFilePath_gridGHG(aID));
		costProfile.writeFile(fs.getFilePath_gridCost(aID));
		
		homeChgCosts.save();
		bevRepCosts.save();
	}
	
	private void launchEditorSubDialog(int selID) {
		switch (vEditableParams.getEditableParmeter(selID)) {
		case vehDepr:
		{
			VehDepreciationDialog dlg = new VehDepreciationDialog(deprModels);
			if (dlg.okPressed()) {
				deprModels = new VehDeprModels(dlg.dpModels());
				editsOccurred = true;
			}
		}
			break;
		case chgEfficiency:
		{
			GridPluginEffDialog dlg = new GridPluginEffDialog(chEffMan, vEditableParams.avms());
			if (dlg.okPressed()) {
				chEffMan = new ChargingEffManager(dlg.getChgEffData());
				editsOccurred = true;
			}
		}
			break;
		case nonBatMfgGHG:
		{
			NonBatMfgGHGDialog dlg = new NonBatMfgGHGDialog(nonBatMfgGHGModel, vEditableParams.avms());
			if (dlg.okPressed()) {
				nonBatMfgGHGModel = new NoneBatteryMfgGHGModel(dlg.getNoneBatteryMfgGHGData());
				editsOccurred = true;
			}
		}
			break;
		case vehLIM:
		{
			LicInsMntDialog dlg = new LicInsMntDialog(licimModel, vEditableParams.avms());
			if (dlg.okPressed()) {
				licimModel = new LicIMModel(dlg.getLicInsMntData());
				editsOccurred = true;
			}
		}
			break;
		case gridHourlyCost:
		{
			GridHourlyProfileDialog dlg = new GridHourlyProfileDialog(costProfile, "Electricity Cost Hourly Profile");
			if (dlg.okPressed()) {
				costProfile = new HourlyProfileCurve(dlg.getCurve());
				hourlyCurvesEdited = true;
				editsOccurred = true;
			}
		}
			break;
		case gridHourlyGHG:
		{
			GridHourlyProfileDialog dlg = new GridHourlyProfileDialog(ghgProfile, "Grid GHG Hourly Profile");
			if (dlg.okPressed()) {
				ghgProfile = new HourlyProfileCurve(dlg.getCurve());
				hourlyCurvesEdited = true;
				editsOccurred = true;
			}
		}
			break;
		case homeChargerCost:
		{
			String dlgTitle = "Home Charger Costs";
			float value1 = homeChgCosts.homeChargerCostL1/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			float value2 = homeChgCosts.homeChargerCostL2/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			int numDec1 = 2;
			int numDec2 = 2; 
			String caption1 = "Level-1 Home Charger* ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+")";
			String caption2 = "Level-2 Home Charger* ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+")";
			String footNote = "* Including Charger and Installation, plus Electric Re-Wiring (if any)";
			int captionsWidth = 165;
			int edtWidth = 100;
			int fnFontSize = 11;
			
			TwoValueDialog dlg = new TwoValueDialog(dlgTitle, value1, value2, numDec1, numDec2, caption1, caption2, footNote, captionsWidth, edtWidth, fnFontSize);
			if (dlg.okPressed()) {
				homeChgCosts.homeChargerCostL1 = dlg.v1()*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
				homeChgCosts.homeChargerCostL2 = dlg.v2()*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
				editsOccurred = true;
			}
		}
			break;
		case bevRepCosts:
		{
			String dlgTitle = "BEV Replacement Vehicle Cost";
			float value1 = bevRepCosts.dollarsPerDay/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			float value2 = bevRepCosts.dollarsPerMile*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
			int numDec1 = 2;
			int numDec2 = 2; 
			String caption1 = "Cost per Day* ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+")";
			String caption2 = "Cost per Distance* ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")";
			String footNote = "* One of those values is typically set to Zero (Cost allocated by Replaced Distance or Days, not both)";
			int captionsWidth = 170;
			int edtWidth = 100;
			int fnFontSize = 11;
			
			TwoValueDialog dlg = new TwoValueDialog(dlgTitle, value1, value2, numDec1, numDec2, caption1, caption2, footNote, captionsWidth, edtWidth, fnFontSize);
			if (dlg.okPressed()) {
				bevRepCosts.dollarsPerDay = dlg.v1()*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
				bevRepCosts.dollarsPerMile = dlg.v2()*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
				editsOccurred = true;
			}
		}
			break;
		}
	}
}
