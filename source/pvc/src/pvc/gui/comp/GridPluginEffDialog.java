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

@SuppressWarnings("serial")
public class GridPluginEffDialog extends JDialog implements ActionListener {
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
	private ChargingEffManager chEffMan;
	private boolean okPressed;
	
	public ChargingEffManager getChgEffData() {return chEffMan;}
	public boolean okPressed() {return okPressed;}

	
	public GridPluginEffDialog(ChargingEffManager chgEffMan, AnalysisVehModelsSetup avms) {
		super(null, "Plug-in Vehicles Charging Efficiency", Dialog.ModalityType.APPLICATION_MODAL);
		
		okPressed = false;
		chEffMan = new ChargingEffManager(chgEffMan);
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
        
        int maxScrollHeight = (int)(ScrollPanelMaxFracScreenHeight * Toolkit.getDefaultToolkit().getScreenSize().height);
        int nVehicles = vms.length;
        
        int nRows = 0;
        for (int i=0; i<nVehicles; i++) {
        	if (chEffMan.vehChEff(i)!=null) nRows++;
        }
                
        String tCol1 = "Efficiency (L1)";
        String tCol2 = "Efficiency (L2)";
        String tCol3 = "Efficiency (DC-Fast)";
        String[] colTitles = {tCol1, tCol2, tCol3};
        int rowTitlesWidth = 180;
        int[] colWidth =  {130, 130, 140};
        int[] numDecimals =  {3, 3, 3};
        
        String[] rowTitles = new String[nRows];
        float[][] initialValues = new float[colTitles.length][nRows];
        
        int pCount = 0;
        for (int i=0; i<nVehicles; i++) {
        	
        	ChargingEffManager.VehChargingEfficiencies curVehChEff = chEffMan.vehChEff(i);
        	if (curVehChEff!=null) {
        		rowTitles[pCount] = new String(curVehChEff.vehShortName());
        		initialValues[0][pCount] = curVehChEff.chEffL1;
        		initialValues[1][pCount] = curVehChEff.chEffL2;
        		initialValues[2][pCount] = curVehChEff.chEffDCFast;
        		
        		pCount++;
        	}
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
	        int nVehicles = vms.length;
	        
	        int pCount = 0;
	        for (int i=0; i<nVehicles; i++) {
	        	
	        	ChargingEffManager.VehChargingEfficiencies curVehChEff = chEffMan.vehChEff(i);
	        	if (curVehChEff!=null) {
	        		curVehChEff.chEffL1 = tableValues[0][pCount];
	        		curVehChEff.chEffL2 = tableValues[1][pCount];
	        		curVehChEff.chEffDCFast = tableValues[2][pCount];
	        		pCount++;
	        	}
	        }
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
