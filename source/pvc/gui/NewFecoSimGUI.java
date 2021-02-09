package pvc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import pvc.calc.FEcoSimsGenerator;
import pvc.calc.comp.VehicleSampleMA;
import pvc.datamgmt.*;
import pvc.datamgmt.comp.DUnits;
import pvc.datamgmt.comp.FDefaults;
import pvc.gui.comp.RunStatusWindow;
import pvc.runners.*;

@SuppressWarnings("serial")
public class NewFecoSimGUI extends JFrame implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int ScrollBarAllowance = 18;

	private static final int VehSamplesFilePathLabelWidth = 400;
	private static final int BtnLoadWidth = 100;
	private static final int ShortLabelWidth = 70;

	
	//GUI objects
	private JButton btnLoadVehSamples, btnExaminVehSamples, btnRun;
	private JLabel lblVehSamplesPath, lblNumVehSamples, lblNumTrips, lblNumCases;
	private JTextField edtAnnualDrvDistance, edtAnnualDriveDays, edtTitle, edtDescription;
	private JSpinner spnNumThreads;
	private JFileChooser dlgVehSamplesFileChooser;

	
	//Data objects
	private FFStructure fs;
	private int aID;
	private FEcoSimsC fecoSims;
	private MainPanelGUI pMP;
	private VehicleSampleMA[] vSamples;
	private RWReadVehiclesDrivingData vehSampleReader;
	private FEcoSimsGenerator fecoGen;
	private RWFEcoSims fecoSimsRunner;
	
	//Constructor
	public NewFecoSimGUI(FFStructure cFS, int analysisID, FEcoSimsC fuelEconomySims, AnalysisVehModelsSetup aVehModelsSetup, WIITModel wiitModel, MainPanelGUI pMainPanel) {
		//Call Super
		super("New Fuel Economy Simulation");

		//Set variables
		fs = cFS;
		aID = analysisID;
		fecoSims = fuelEconomySims;
		pMP = pMainPanel;
		vSamples = null;
		fecoGen = FEcoSimsGenerator.createFEcoSimsGenerator(fs, aID, aVehModelsSetup, wiitModel);
		if (fecoGen == null) {
			exitProcedure();
			return;
		}
		
		//Dialog for choosing files
		dlgVehSamplesFileChooser = new JFileChooser();
		dlgVehSamplesFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dlgVehSamplesFileChooser.setDialogTitle("Choose Vehicle Samples Real-World Driving or Drive Cycles File");
		dlgVehSamplesFileChooser.setCurrentDirectory(new File(fs.getFolderPath_defaultRealWorldDriving()));
		dlgVehSamplesFileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
		
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
		
		//Set size
        setSize(winWidth, winHeight);
		
        //Set window closing operation
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	            exitProcedure();
	        }
	    });
		
		//Show
        setResizable(false);
        setVisible(true);
	}
	private void exitProcedure() {
		dispose();
		new FecoSimGUI(fs, aID, pMP);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == btnLoadVehSamples) {
			int retValue = dlgVehSamplesFileChooser.showOpenDialog(this);
			if (retValue == JFileChooser.APPROVE_OPTION) {
				String fname = dlgVehSamplesFileChooser.getSelectedFile().getPath();
				lblVehSamplesPath.setText("<html>"+breakupFilePathForLbl(fname)+"</html>");
				runReadVehicleSamples(fname);
			}
		}
		if (source == btnRun) {
			float annualVMT = -1;
			try {
				annualVMT = Float.parseFloat(edtAnnualDrvDistance.getText()) * DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
			} catch (Exception e) {}
			if (annualVMT <= 0) {
				JOptionPane.showMessageDialog(null, "Invalid Entry for Average Annual Driving Distance", 
						"Please Check Input", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			float annualDriveDays = -1;
			try {
				annualDriveDays = Float.parseFloat(edtAnnualDriveDays.getText());
			} catch (Exception e) {}
			if (annualDriveDays <= 0) {
				JOptionPane.showMessageDialog(null, "Invalid Entry for Annual Number of Driving Days", 
						"Please Check Input", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			int numThreads = Integer.parseInt(spnNumThreads.getValue().toString());
			if (numThreads < 1) numThreads = 1;
			if (numThreads > FDefaults.maxNumThreads) numThreads = FDefaults.maxNumThreads;
			
			runFecoSims(edtTitle.getText(), edtDescription.getText(), annualVMT, annualDriveDays, fecoSims.numCompletedSims(), numThreads); 
		}
		
		if (source == btnExaminVehSamples) {
			//TODO
		}
	}
	
	private JPanel createContentPane() {
		//Create
		int pathLabelHeight = Math.max(BtnHeight*2 + (LineSpacing - BtnHeight) - ScrollBarAllowance, EdtHeight + ScrollBarAllowance);
        int btnsHeight = Math.max(BtnHeight, (pathLabelHeight + ScrollBarAllowance - (LineSpacing - BtnHeight))/2);
		
		int panelWidth = VehSamplesFilePathLabelWidth + ScrollBarAllowance + WinMargin/2 + BtnLoadWidth + WinMargin*2;
		int panelHeight = WinMargin*2 + LineSpacing + pathLabelHeight+ScrollBarAllowance 
				+ WinMargin + LineSpacing*4 + WinMargin + btnsHeight + WinMargin/2 + LineSpacing*2;
		
		
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Path for Sample Vehicle Real-World Trips (or Drive Cycles)");
        lbl1.setSize(VehSamplesFilePathLabelWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        cy += LineSpacing;
        
        lblVehSamplesPath = new JLabel("");   
        lblVehSamplesPath.setBorder(blackBorder);
        lblVehSamplesPath.setVerticalAlignment(SwingConstants.TOP);
        lblVehSamplesPath.setPreferredSize(new Dimension(VehSamplesFilePathLabelWidth, pathLabelHeight));
        
        JScrollPane sc2 = new JScrollPane(lblVehSamplesPath);
        sc2.setLocation(cx, cy);
        sc2.setSize(VehSamplesFilePathLabelWidth + ScrollBarAllowance, pathLabelHeight + ScrollBarAllowance);
        totalGUI.add(sc2);
        
        btnLoadVehSamples = new JButton("Load...");
        btnLoadVehSamples.setSize(BtnLoadWidth, btnsHeight);
        btnLoadVehSamples.setLocation(cx + VehSamplesFilePathLabelWidth + ScrollBarAllowance + WinMargin/2, cy);
        btnLoadVehSamples.addActionListener(this);
        totalGUI.add(btnLoadVehSamples);

        btnExaminVehSamples = new JButton("Examine...");
        btnExaminVehSamples.setSize(BtnLoadWidth, btnsHeight);
        btnExaminVehSamples.setLocation(cx + VehSamplesFilePathLabelWidth + ScrollBarAllowance + WinMargin/2, cy + pathLabelHeight + ScrollBarAllowance - btnsHeight);
        btnExaminVehSamples.addActionListener(this);
        btnExaminVehSamples.setEnabled(false);
        totalGUI.add(btnExaminVehSamples);
        
        cy += pathLabelHeight + ScrollBarAllowance + WinMargin/2;
        
        int wWidth = VehSamplesFilePathLabelWidth + ScrollBarAllowance + WinMargin/2 + BtnLoadWidth;
        int lbls100Width = wWidth/2 - BtnLoadWidth - 2 - WinMargin;
        
        JLabel lbl101 = new JLabel("Num. of Vehicle Samples");
        lbl101.setSize(lbls100Width,LblHeight);
        lbl101.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl101);
        
        JLabel lbl102 = new JLabel("Total Number of Trips");
        lbl102.setSize(lbls100Width,LblHeight);
        lbl102.setLocation(cx + wWidth/2 + WinMargin/2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl102);
        
        lblNumVehSamples = new JLabel();
        lblNumVehSamples.setSize(BtnLoadWidth, EdtHeight);
        lblNumVehSamples.setLocation(cx + wWidth/2 - BtnLoadWidth - WinMargin/2, cy + (LineSpacing - EdtHeight)/2);
        lblNumVehSamples.setBorder(blackBorder);
        lblNumVehSamples.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lblNumVehSamples);
        
        lblNumTrips = new JLabel();
        lblNumTrips.setSize(BtnLoadWidth, EdtHeight);
        lblNumTrips.setLocation(cx + wWidth - BtnLoadWidth, cy + (LineSpacing - EdtHeight)/2);
        lblNumTrips.setBorder(blackBorder);
        lblNumTrips.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lblNumTrips);
        
        cy += LineSpacing;       
        
        JLabel lbl103 = new JLabel("Num. of Simulation Cases");
        lbl103.setSize(lbls100Width,LblHeight);
        lbl103.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl103);
        
        JLabel lbl104 = new JLabel("Number of Threads");
        lbl104.setSize(lbls100Width,LblHeight);
        lbl104.setLocation(cx + wWidth/2 + WinMargin/2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl104);
        
        lblNumCases = new JLabel();
        lblNumCases.setSize(BtnLoadWidth, EdtHeight);
        lblNumCases.setLocation(cx + wWidth/2 - BtnLoadWidth - WinMargin/2, cy + (LineSpacing - EdtHeight)/2);
        lblNumCases.setBorder(blackBorder);
        lblNumCases.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(lblNumCases);

        SpinnerNumberModel spinModel = new SpinnerNumberModel(FDefaults.preferredNumThreads, 1, FDefaults.maxNumThreads, 1);
        spnNumThreads = new JSpinner(spinModel);
        spnNumThreads.setLocation(cx + wWidth - BtnLoadWidth, cy + (LineSpacing - EdtHeight)/2);
        spnNumThreads.setSize(BtnLoadWidth, EdtHeight);
        spnNumThreads.setBorder(blackBorder);
        totalGUI.add(spnNumThreads);
        
        cy += LineSpacing + WinMargin;
        
        JLabel lbl2 = new JLabel("Information about New Simulation to be done...");
        lbl2.setSize(VehSamplesFilePathLabelWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;
        
        int lblVMTCaptionWidht = wWidth/2 - WinMargin - BtnLoadWidth;
        String lblVMTCaption = "Av. Annual Driving (" + DUnits.getShortName(DUnits.UnitType.Distance) + ")";
        String lblDriveDaysCaption = "Av. Annual Driving Days";
        
        int annualDirveDistance = (int)(FDefaults.annualVMT / DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance));       
        int annualDirveDays = (int)(FDefaults.annualDriveDays);       
       
        JLabel lbl3 = new JLabel(lblVMTCaption);
        lbl3.setSize(lblVMTCaptionWidht, LblHeight);
        lbl3.setLocation(cx + wWidth/2 + WinMargin/2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);
        
        edtAnnualDrvDistance = new JTextField(""+annualDirveDistance);
        edtAnnualDrvDistance.setSize(BtnLoadWidth, EdtHeight);
        edtAnnualDrvDistance.setLocation(cx + wWidth/2 + lblVMTCaptionWidht + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        edtAnnualDrvDistance.setHorizontalAlignment(SwingConstants.RIGHT);
        edtAnnualDrvDistance.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
			private void processChange() {
				updateButtonsActivity();	
			}
		});
        totalGUI.add(edtAnnualDrvDistance);
        
        int edtTitleWidht = wWidth/2 - ShortLabelWidth - WinMargin/2;
        edtTitle = new JTextField("Title for new simulation");
        edtTitle.setSize(edtTitleWidht, EdtHeight);
        edtTitle.setLocation(cx + ShortLabelWidth, cy + (LineSpacing - EdtHeight)/2);
        edtTitle.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
			private void processChange() {
				updateButtonsActivity();	
			}
		});
        totalGUI.add(edtTitle);

        JLabel lbl4 = new JLabel("Title");
        lbl4.setSize(ShortLabelWidth-2, LblHeight);
        lbl4.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl4);
        
        cy += LineSpacing;
        
        JLabel lbl3b = new JLabel(lblDriveDaysCaption);
        lbl3b.setSize(lblVMTCaptionWidht, LblHeight);
        lbl3b.setLocation(cx + wWidth/2 + WinMargin/2, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3b);
        
        edtAnnualDriveDays = new JTextField(""+annualDirveDays);
        edtAnnualDriveDays.setSize(BtnLoadWidth, EdtHeight);
        edtAnnualDriveDays.setLocation(cx + wWidth/2 + lblVMTCaptionWidht + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        edtAnnualDriveDays.setHorizontalAlignment(SwingConstants.RIGHT);
        edtAnnualDriveDays.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
			private void processChange() {
				updateButtonsActivity();	
			}
		});
        totalGUI.add(edtAnnualDriveDays);
                
        cy += LineSpacing;

        JLabel lbl5 = new JLabel("Description");
        lbl5.setSize(ShortLabelWidth-2, LblHeight);
        lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl5);

        edtDescription = new JTextField("Some detail (e.g. what kind of trips) about the new simulation to be done");
        edtDescription.setSize(wWidth - ShortLabelWidth, EdtHeight);
        edtDescription.setLocation(cx + ShortLabelWidth, cy + (LineSpacing - EdtHeight)/2);
        edtDescription.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
			private void processChange() {
				updateButtonsActivity();	
			}
		});
        totalGUI.add(edtDescription);
        
        cy += LineSpacing + WinMargin;
        
        btnRun = new JButton("Run Simulations");
        btnRun.setSize(BtnLoadWidth*2, btnsHeight);
        btnRun.setLocation(cx + wWidth/2 - BtnLoadWidth, cy);
        btnRun.addActionListener(this);
        btnRun.setEnabled(false);
        totalGUI.add(btnRun);
        
        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	public void vehSamplesReadingFinished() {
		setVisible(true);
		vSamples = vehSampleReader.vSamples();
		if (vSamples!=null) if (VehicleSampleMA.totNumTrips(vSamples) < 1) vSamples = null;
		updateButtonsActivity();
	}
	private void updateButtonsActivity() {
		if (vSamples == null) {
	        btnExaminVehSamples.setEnabled(false);
			btnRun.setEnabled(false);
			
			lblNumVehSamples.setText("");
			lblNumTrips.setText("");
			lblNumCases.setText("");
			return;
		}
		
		lblNumVehSamples.setText(""+vSamples.length);
		lblNumTrips.setText(""+VehicleSampleMA.totNumTrips(vSamples));
		lblNumCases.setText(""+fecoGen.numCases());	
        btnExaminVehSamples.setEnabled(true);
        
        if (edtDescription.getText().length() < 1) {
			btnRun.setEnabled(false);
			return;
        }
        
        if (edtTitle.getText().length() < 1) {
			btnRun.setEnabled(false);
			return;
        }
        
        float annualDistance  = -1f;
        try {
        	annualDistance = Float.parseFloat(edtAnnualDrvDistance.getText());
        } catch (Exception e) {}        
        if (annualDistance <= 0) {
			btnRun.setEnabled(false);
			return;
        }
        
        float annualDriveDays  = -1f;
        try {
        	annualDriveDays = Float.parseFloat(edtAnnualDriveDays.getText());
        } catch (Exception e) {}        
        if (annualDriveDays <= 0) {
			btnRun.setEnabled(false);
			return;
        }
		
		btnRun.setEnabled(true);
	}
	
	private void runReadVehicleSamples(String fname) {
		vehSampleReader = new RWReadVehiclesDrivingData(this, fname);
		RunStatusWindow stWindow = new RunStatusWindow(vehSampleReader, "Reading Vehicle Samples Driving Data");
		
		setVisible(false);
		stWindow.startRun();
	}
	private void runFecoSims(String title, String description, float annualVMT, float annualDriveDays, int newSimID, int numThreads) {
		fecoSimsRunner = new RWFEcoSims(pMP, fecoGen, newSimID, vSamples, title, description, annualVMT, annualDriveDays, numThreads);
		RunStatusWindow stWindow = new RunStatusWindow(fecoSimsRunner, "Running Fuel Economy Simulations");
		
		setVisible(false);
		stWindow.startRun();
	}
	private static String breakupFilePathForLbl(String fname) {
		String sChar = FFStructure.slashChar();
		ArrayList<String> lst = new ArrayList<String>();
		
		String remSt = new String(fname);
		while (remSt.contains(sChar)) {
			int firstSCharID = remSt.indexOf(sChar);
			lst.add(new String (remSt.substring(0, firstSCharID+1)));
			remSt = remSt.substring(firstSCharID+1);
		}
		
		String st = lst.get(0);
		for (int i=1; i<lst.size(); i++) {
			st = st + " " + lst.get(i);
		}
		st = st + " " + remSt;
		
		return st;
	}
}
