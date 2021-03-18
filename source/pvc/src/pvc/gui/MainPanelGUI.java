package pvc.gui;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;

@SuppressWarnings("serial")
public class MainPanelGUI extends JFrame implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;

	private static final int BtnWidth = 250;
		
	//GUI objects
	private JButton btnChangeAnalysis, btnDocumentation, btnFecoSims, btnGHGHistograms, btnCostAnalysis, btnCostVsGHG;

	
	//Data structures
	private FFStructure fs;
	private int aID;

	public MainPanelGUI(FFStructure cFS, int analysisID) {
		//Call super
		super();
		
		//Set variables
		fs = cFS;
		aID = analysisID;
		
		//Ensure customization folder exists
		fs.ensureCustomizationFolderExists(aID);
		
		//Update unit constants from file system
		DUnits.readFromFile(fs, aID);
		
		//Set title
		AnalysisTitleDescription atd = null;
		try {
			atd = new AnalysisTitleDescription(fs, aID);
		} catch (Exception e) {
			System.exit(0);
			return;
		}
		setTitle("Main Panel: "+atd.title());
		
		//Save current analysis as last accessed
		fs.setLastAnalysisID(aID);
		
		//Create GUI
		JPanel ct = createContentPane();
		setContentPane(ct);
		
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set size and Launch
        setSize(winWidth, winHeight);
        setResizable(false);
        setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == btnChangeAnalysis) {
			new AnalysisSelectorGUI(fs);
			dispose();
			return;
		}
		if (source == btnDocumentation) {
			setVisible(false);
			new DocumentationGUI(fs, aID, this);
			return;
		}
		if (source == btnFecoSims) {
			setVisible(false);
			new FecoSimGUI(fs, aID, this);
			return;
		}
		if (source == btnGHGHistograms) {
			setVisible(false);
			new RGHGHistogramsGUI(fs, aID, this, false);
			return;
		}
		if (source == btnCostAnalysis) {
			setVisible(false);
			new RCostBarsGUI(fs, aID, this, false);
			return;
		}
		if (source == btnCostVsGHG) {
			setVisible(false);
			new RCostVsGHGGUI(fs, aID, this, false);
			return;
		}	
	}
	
	private JPanel createContentPane() {
		//Create
		int numButtons = 6;
		int panelHeight = numButtons*LineSpacing + WinMargin*3;
		int panelWidth = BtnWidth + WinMargin*2;
		
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);

        //Add GUI objects
        int cx = WinMargin;
        int cy = WinMargin;
        
        btnChangeAnalysis = new JButton("Change Analysis...");
        btnChangeAnalysis.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnChangeAnalysis.setSize(BtnWidth, BtnHeight);
        btnChangeAnalysis.addActionListener(this);
        totalGUI.add(btnChangeAnalysis);
        
        cy += LineSpacing + WinMargin;
        
        btnDocumentation = new JButton("Vehicle Models Documentation...");
        btnDocumentation.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnDocumentation.setSize(BtnWidth, BtnHeight);
        btnDocumentation.addActionListener(this);
        totalGUI.add(btnDocumentation);
        
        cy += LineSpacing;
        
        btnFecoSims = new JButton("Fuel Economy Simulations...");
        btnFecoSims.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnFecoSims.setSize(BtnWidth, BtnHeight);
        btnFecoSims.addActionListener(this);
        totalGUI.add(btnFecoSims);
        
        cy += LineSpacing;
        
        btnGHGHistograms = new JButton("GHG Analysis...");
        btnGHGHistograms.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnGHGHistograms.setSize(BtnWidth, BtnHeight);
        btnGHGHistograms.addActionListener(this);
        totalGUI.add(btnGHGHistograms);
        
        cy += LineSpacing;
        
        btnCostAnalysis = new JButton("Cost Analysis...");
        btnCostAnalysis.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnCostAnalysis.setSize(BtnWidth, BtnHeight);
        btnCostAnalysis.addActionListener(this);
        totalGUI.add(btnCostAnalysis);
        
        cy += LineSpacing;
        
        btnCostVsGHG = new JButton("Cost vs GHG Analysis...");
        btnCostVsGHG.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnCostVsGHG.setSize(BtnWidth, BtnHeight);
        btnCostVsGHG.addActionListener(this);
        totalGUI.add(btnCostVsGHG);
        
        //Adjust Buttons Activity
        adjustButtonsActivity();
		
        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void adjustButtonsActivity() {
		FEcoSimsC fecoSims = new FEcoSimsC(fs, aID);
		if (fecoSims.numCompletedSims() > 0) {
			btnGHGHistograms.setEnabled(true);
			btnCostAnalysis.setEnabled(true);
			btnCostVsGHG.setEnabled(true);
		} else {
			btnGHGHistograms.setEnabled(false);
			btnCostAnalysis.setEnabled(false);
			btnCostVsGHG.setEnabled(false);
		}
	}
	
	public void subModuleFinished() {
		adjustButtonsActivity();
		setVisible(true);
	}
}
