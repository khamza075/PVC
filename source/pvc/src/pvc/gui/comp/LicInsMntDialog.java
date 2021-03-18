package pvc.gui.comp;

import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.DUnits;

@SuppressWarnings("serial")
public class LicInsMntDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int TClearance = 2;
	private static final int EdtHeight = LblHeight + TClearance;
	private static final int BtnHeight = EdtHeight + TClearance*2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int BtnWidth = 90;
	
	private static final float ScrollPanelMaxFracScreenHeight = 0.4f;


	//GUI Objects
	private JButton btnOK, btnCancel;
	private ValuesTableJPanel vtPanel;

	//Data
	private AnalysisVehModelsSetup.AVehModelSetup[] vms;
	private LicIMModel licIM;
	private boolean okPressed;
	
	public LicIMModel getLicInsMntData() {return licIM;}
	public boolean okPressed() {return okPressed;}

	
	public LicInsMntDialog(LicIMModel licInsMnt, AnalysisVehModelsSetup avms) {
		super(null, "Licensing, Insurance & Maintenance Costs", Dialog.ModalityType.APPLICATION_MODAL); 

		okPressed = false;
		licIM = new LicIMModel(licInsMnt);
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
        
        String tCol1 = "Licensing ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+"/year)";
        String tCol2 = "Insurance ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+"/year)";
        String tCol3 = "Maintenance ("+DUnits.getShortName(DUnits.UnitType.MoneyUnit)+"/"+DUnits.getShortName(DUnits.UnitType.Distance)+")";
        String[] colTitles = {tCol1, tCol2, tCol3};
        int rowTitlesWidth = 180;
        int[] colWidth =  {135, 135, 135};
        int[] numDecimals =  {0, 0, 3};
        
        float[][] initialValues = new float[colTitles.length][nRows];
        for (int i=0; i<nRows; i++) {
        	initialValues[0][i] = licIM.getAnnualLicensingCost(i)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
        	initialValues[1][i] = licIM.getAnnualInsuranceCost(i)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
        	initialValues[2][i] = licIM.getMaintnenaceCostPerMile(i)
        			*DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance)/DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit);
        }
        
        int cx = WinMargin;
        int cy = WinMargin;
        
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
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private boolean screenToData() {
		try {
			float[][] tableValues = vtPanel.getTableValues();
			
	        for (int i=0; i<vtPanel.nRows(); i++) {	        	
	        	licIM.setAnnualLicensingCost(i, tableValues[0][i]*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit));
	        	licIM.setAnnualInsuranceCost(i, tableValues[1][i]*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit));
	        	licIM.setMaintnenaceCostPerMile(i, tableValues[2][i]*DUnits.convConstMPtoBCalc(DUnits.UnitType.MoneyUnit)/
	        			DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance));
	            //TODO
	        }
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
