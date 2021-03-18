package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pvc.datamgmt.VehDeprModels;

@SuppressWarnings("serial")
public class VehDepreciationDialog extends JDialog implements ActionListener, ChangeListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int TClear = 2;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int BtnWidth = 90;
	
	private static final int ScrollAllowanceWidth = 20;
	private static final float ScrollMaxHeightFracScreen = 0.65f;

	//GUI Objects
	private JButton btnOK, btnCancel;
	private VehDeprJPanel[] dprModelDataPanels;
	
	//Data
	private VehDeprModels dpModels;
	public VehDeprModels dpModels() {return dpModels;}
	
	private boolean okPressed, editsOccurred;
	public boolean okPressed() {return okPressed;}
	
	public VehDepreciationDialog (VehDeprModels depreciationModels) {
		super(null, "Vehicle Depreciation Models", Dialog.ModalityType.APPLICATION_MODAL);

		okPressed = false;
		editsOccurred = false;
		dpModels = new VehDeprModels(depreciationModels);
		
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
	private void exitCheck() {
		if (editsOccurred) {
			int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Parameter settings have been edited. Do you wish to Discard changes?", 
					"Please Check", JOptionPane.YES_NO_OPTION);
			if (dlgYesNoReturn == JOptionPane.NO_OPTION) return;
		}
		okPressed = false;
		dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			if (!screenToData()) {
				JOptionPane.showMessageDialog(null, "Invalid Inputs Exist", 
						"Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
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
	}
	@Override
	public void stateChanged(ChangeEvent event) {
		edtChanged();
	}
	public void edtChanged() {
		editsOccurred = true;
		//if (screenToData()) {}	//TODO This is where an update of plots or other visuals can be done		
	}

	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int numVehicles = dpModels.numVehicles();
        dprModelDataPanels = new VehDeprJPanel[numVehicles];
        
        for (int i=0; i < dprModelDataPanels.length; i++) {
        	dprModelDataPanels[i] = new VehDeprJPanel(dpModels.getVehDeprModel(i), dpModels.getVehShortName(i), this, this);
        }
        
        int vPanelWidth = dprModelDataPanels[0].getWidth();
        int vPanelHeight = dprModelDataPanels[0].getHeight();
        
        int subPanelPrefWidth = vPanelWidth + TClear*2;
        int subPanelPrefHeight = vPanelHeight*numVehicles + TClear*(numVehicles+1);
        
        JPanel subPanel = new JPanel();
        subPanel.setLayout(null);
        subPanel.setBorder(blackBorder);
        
        for (int i=0; i < dprModelDataPanels.length; i++) {
        	dprModelDataPanels[i].setLocation(TClear, TClear + (TClear+vPanelHeight)*i);
        	subPanel.add(dprModelDataPanels[i]);
        }
        
        int scHeight = Math.min((int)(ScrollMaxHeightFracScreen*Toolkit.getDefaultToolkit().getScreenSize().height), subPanelPrefHeight);
        int scWidth = subPanelPrefWidth;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        if (subPanelPrefHeight > scHeight) {
        	subPanel.setPreferredSize(new Dimension(subPanelPrefWidth, subPanelPrefHeight));
        	scWidth += ScrollAllowanceWidth;       	
        	
            JScrollPane sc1 = new JScrollPane(subPanel);
            sc1.setSize(scWidth, scHeight);
            sc1.setLocation(cx, cy);
        	totalGUI.add(sc1);
        } else {
        	subPanel.setSize(subPanelPrefWidth, subPanelPrefHeight);
        	subPanel.setLocation(cx, cy);
        	totalGUI.add(subPanel);
        }
        
        int panelWidth = scWidth + WinMargin*2;
        cy += scHeight + WinMargin;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation(panelWidth/2 - BtnWidth - TClear/2, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(panelWidth/2 + TClear/2, cy);
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
			VehDeprJPanel.DataOutputs[] outputData = new VehDeprJPanel.DataOutputs[dprModelDataPanels.length];
			for (int i=0; i<dprModelDataPanels.length; i++) {
				outputData[i] = dprModelDataPanels[i].getDeprData();
				if (outputData[i] == null) return false;
			}
			
			for (int i=0; i<outputData.length; i++) {
				dpModels.getVehDeprModel(i).setData(outputData[i].deprCurveDataMatrix, 
						outputData[i].lowMiResaleImpr, outputData[i].lowMiAt, 
						outputData[i].highMiResaleRedc, outputData[i].highMiAt);
			}
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
