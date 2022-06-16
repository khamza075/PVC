package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.datamgmt.SavedScenariosManager;

@SuppressWarnings("serial")
public class ManageScenariosDialog extends JDialog implements ActionListener {
	//Constants
	private static final int WinMargin = 10;
	private static final int TClear = 2;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (BtnHeight*3)/2;

	private static final int ListWidth = 280;
	private static final int LstHeightNumLineSpacing = 8;
	private static final int DscLblHeightNumLineSpacing = 2;
	private static final int DscLblFontSize = 11;
	

	//GUI Objects
	private JButton btnUp, btnDown, btnLoad, btnDelete;
	private JList<String> selScenarios;
	private JLabel lblDescription;

	//Data
	private boolean okPressed;
	private int selectedScenarioID;
	private SavedScenariosManager ssMan;
	
	//Data access functions
	public boolean okPressed() {return okPressed;}
	public int selectedScenarioID() {return selectedScenarioID;}

	public ManageScenariosDialog(SavedScenariosManager savedScenarioManager) {
		//Super
		super(null, "Manage Saved Scenarios", Dialog.ModalityType.APPLICATION_MODAL);

		//Data
		ssMan = savedScenarioManager;
		selectedScenarioID = 0;
		okPressed = false;
		
		//Create content and show
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
		
		if (source == btnLoad) {
			int selID = selScenarios.getSelectedIndex();
			if (selID < 0) return;
			
			selectedScenarioID = selID;
			okPressed = true;
			dispose();
		}
		
		if (source == btnDelete) {
			int selID = selScenarios.getSelectedIndex();
			if (selID < 1) return;
			
			int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this previously saved scenario?"+
					"\n\nThis action cannot be undone.", 
					"Please Confirm", JOptionPane.YES_NO_OPTION|JOptionPane.WARNING_MESSAGE);
			if (dlgYesNoReturn == JOptionPane.YES_OPTION) {
				ssMan.deleteScenario(selID);
				
				selectedScenarioID = Math.min(selID, ssMan.numScenarios()-1);
		        selScenarios.setListData(ssMan.getShortDescriptions());
		        selScenarios.setSelectedIndex(selectedScenarioID);
		        listSelectionChanged();
			}
		}
		
		if (source == btnUp) {
			int selID = selScenarios.getSelectedIndex();
			if (selID < 2) return;

			ssMan.switchOrder(selID-1, selID);
			selScenarios.setListData(ssMan.getShortDescriptions());
			selScenarios.setSelectedIndex(selID-1);
	        listSelectionChanged();
		}
		
		if (source == btnDown) {
			int selID = selScenarios.getSelectedIndex();
			if (selID < 1) return;
			if (selID >= (ssMan.numScenarios()-1)) return;

			ssMan.switchOrder(selID, selID+1);
			selScenarios.setListData(ssMan.getShortDescriptions());
			selScenarios.setSelectedIndex(selID+1);
	        listSelectionChanged();
		}
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int lstHeight = LineSpacing*LstHeightNumLineSpacing;
        int dscLabelHeight = LineSpacing * DscLblHeightNumLineSpacing;
        int panelWidth = ListWidth + WinMargin*2;
        int panelHeight = WinMargin*3 + WinMargin/2 + TClear + LineSpacing*2 + lstHeight + dscLabelHeight + BtnHeight + BigBtnHeight;
        
        int cx = WinMargin;
        int cy = WinMargin;
               
        JLabel lbl1 = new JLabel("Saved Scenarios");
        lbl1.setSize(ListWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        cy += LineSpacing;
        
        selScenarios = new JList<String>();
        selScenarios.setLocation(0,0);
        selScenarios.setSize(ListWidth, lstHeight);
        selScenarios.setBorder(blackBorder);
        
        selScenarios.setListData(ssMan.getShortDescriptions());
        selScenarios.setSelectedIndex(selectedScenarioID);
        
        selScenarios.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		listSelectionChanged();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selScenarios);
        sc1.setLocation(cx, cy);
        sc1.setSize(ListWidth, lstHeight);  
        totalGUI.add(sc1);
        
        cy += lstHeight + TClear;
        
        int bntWidth = (ListWidth - TClear)/2;
        
        btnUp = new JButton("Move Up");
        btnUp.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnUp.setSize(bntWidth, BtnHeight);
        btnUp.addActionListener(this);
        totalGUI.add(btnUp);
        
        btnDown = new JButton("Move Down");
        btnDown.setLocation(cx + ListWidth - bntWidth, cy + (LineSpacing - BtnHeight)/2);
        btnDown.setSize(bntWidth, BtnHeight);
        btnDown.addActionListener(this);
        totalGUI.add(btnDown);
        
        cy += LineSpacing + WinMargin/2;

        JLabel lbl2 = new JLabel("Scenario Description");
        lbl2.setSize(ListWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;

        Font smallerFont = new Font(lbl1.getFont().getName(), Font.PLAIN, DscLblFontSize);
        lblDescription = new JLabel();
        lblDescription.setSize(ListWidth, dscLabelHeight);
        lblDescription.setLocation(cx, cy);
        lblDescription.setFont(smallerFont);
        lblDescription.setBorder(blackBorder);
        lblDescription.setVerticalAlignment(SwingConstants.TOP);
        totalGUI.add(lblDescription);
        
        cy += dscLabelHeight + WinMargin/2;
        
        btnLoad = new JButton("Load Scenario");
        btnLoad.setLocation(cx, cy + (LineSpacing - BtnHeight)/2);
        btnLoad.setSize(bntWidth, BigBtnHeight);
        btnLoad.addActionListener(this);
        totalGUI.add(btnLoad);
        
        btnDelete = new JButton("Delete Scenario");
        btnDelete.setLocation(cx + ListWidth - bntWidth, cy + (LineSpacing - BtnHeight)/2);
        btnDelete.setSize(bntWidth, BigBtnHeight);
        btnDelete.addActionListener(this);
        totalGUI.add(btnDelete);

        listSelectionChanged();
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void listSelectionChanged() {
		int selID = selScenarios.getSelectedIndex();
		updateButtonsActivity(selID);
		
		if (selID < 0) {
			lblDescription.setText("");
			return;
		}
		
		String st = "<HTML>" + ssMan.getLongDescriptions()[selID] + "</HTML>";
		lblDescription.setText(st);		
	}
	private void updateButtonsActivity(int selID) {
		if (selID < 0) {
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
			btnLoad.setEnabled(false);
			btnDelete.setEnabled(false);
			return;
		}

		btnLoad.setEnabled(true);

		if (selID == 0) btnDelete.setEnabled(false);
		else btnDelete.setEnabled(true);
		
		if (selID > 1) btnUp.setEnabled(true);
		else btnUp.setEnabled(false);
		
		if ((selID > 0)&&(selID < (ssMan.numScenarios()-1))) btnDown.setEnabled(true);
		else btnDown.setEnabled(false);
	}
}
