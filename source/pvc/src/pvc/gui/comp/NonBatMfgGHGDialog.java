package pvc.gui.comp;

import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.DUnits;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class NonBatMfgGHGDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int TClearance = 2;
	private static final int EdtHeight = LblHeight + TClearance;
	private static final int BtnHeight = EdtHeight + TClearance*2;
	private static final int LineSpacing = BtnHeight + TClearance;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int BtnWidth = 90;
	
	private static final float ScrollPanelMaxFracScreenHeight = 0.4f;

	//GUI Objects
	private JButton btnOK, btnCancel;
	private ValuesTableJPanel vtPanel;
	private JTextField edtLCADistance;

	//Data
	private AnalysisVehModelsSetup.AVehModelSetup[] vms;
	private NoneBatteryMfgGHGModel nbMfGHG;
	private boolean okPressed;
	
	public NoneBatteryMfgGHGModel getNoneBatteryMfgGHGData() {return nbMfGHG;}
	public boolean okPressed() {return okPressed;}

	
	public NonBatMfgGHGDialog(NoneBatteryMfgGHGModel nonBatMfgGHG, AnalysisVehModelsSetup avms) {
		super(null, "Vehicle Manfucturing GHG other than Battery", Dialog.ModalityType.APPLICATION_MODAL);
		
		okPressed = false;
		nbMfGHG = new NoneBatteryMfgGHGModel(nonBatMfgGHG);
		vms = avms.vehModelsSetup();
		
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
			if (!screenToData()) {
				JOptionPane.showMessageDialog(null, "Invalid Numeric Value", 
						"Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
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
        
        int nRows = vms.length;
        String[] rowTitles = new String[nRows];
        for (int i=0; i<nRows; i++) rowTitles[i] = new String(vms[i].shortName);
        int maxScrollHeight = (int)(ScrollPanelMaxFracScreenHeight * Toolkit.getDefaultToolkit().getScreenSize().height);
        
        String tCol1 = "Low End ("+DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.VehMassUnit)+"-vehicle)";
        String tCol2 = "High End ("+DUnits.getShortName(DUnits.UnitType.GHGUnit)+"/"+DUnits.getShortName(DUnits.UnitType.VehMassUnit)+"-vehicle)";
        String[] colTitles = {tCol1, tCol2};
        int rowTitlesWidth = 180;
        int[] colWidth =  {165, 165};
        int[] numDecimals =  {0, 0};
        
        float[][] initialValues = new float[colTitles.length][nRows];
        for (int i=0; i<nRows; i++) {
        	initialValues[0][i] = nbMfGHG.gCO2perKgVehicle_lowEnd[i]
        			*DUnits.convConstMPtoBCalc(DUnits.UnitType.VehMassUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
        	initialValues[1][i] = nbMfGHG.gCO2perKgVehicle_highEnd[i]
        			*DUnits.convConstMPtoBCalc(DUnits.UnitType.VehMassUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit);
        }
        
        int cx = WinMargin;
        int cy = WinMargin*2 + LineSpacing;
        
        vtPanel = new ValuesTableJPanel(colTitles, rowTitles, initialValues, numDecimals, rowTitlesWidth, colWidth, maxScrollHeight);
        vtPanel.setLocation(cx, cy);
        totalGUI.add(vtPanel);
        
        int panelWidth = vtPanel.getWidth() + WinMargin*2;
        cy += vtPanel.getHeight() + WinMargin;
               
        int tableCenter = WinMargin + rowTitlesWidth + (panelWidth - WinMargin*2 - rowTitlesWidth)/2;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation(tableCenter - BtnWidth - TClearance/2, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(tableCenter + TClearance/2, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
        
        cy += BigBtnHeight + WinMargin;

        int panelHeight = cy;
        
        cy = WinMargin;
        
        int edtWidth = colWidth[1];
        float lcaMiles = nbMfGHG.vehicleLifetimeMiles;
        float lcaDistanceInOutputUnits = lcaMiles/DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
        
        edtLCADistance = new JTextField(NumToString.floatWNumDecimals(lcaDistanceInOutputUnits, 0));
        edtLCADistance.setSize(edtWidth, EdtHeight);
        edtLCADistance.setLocation(panelWidth - edtWidth - WinMargin, cy + (LineSpacing - EdtHeight)/2);
        edtLCADistance.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtLCADistance);

        JLabel lbl = new JLabel("Vehicle Lifetime Driving Distance ("+DUnits.getShortName(DUnits.UnitType.Distance)+")");         
        lbl.setSize(panelWidth - edtWidth - WinMargin*2 - TClearance, LblHeight);
        lbl.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl);       
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private boolean screenToData() {
		try {
	        float lcaMiles = Float.parseFloat(edtLCADistance.getText())*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
	        nbMfGHG.vehicleLifetimeMiles = lcaMiles;

			float[][] tableValues = vtPanel.getTableValues();
			
	        for (int i=0; i<vtPanel.nRows(); i++) {
	        	nbMfGHG.gCO2perKgVehicle_lowEnd[i] = tableValues[0][i]
	        			*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.VehMassUnit);
	        	nbMfGHG.gCO2perKgVehicle_highEnd[i] = tableValues[1][i]
	        			*DUnits.convConstMPtoBCalc(DUnits.UnitType.GHGUnit)/DUnits.convConstMPtoBCalc(DUnits.UnitType.VehMassUnit);
	        }
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}

