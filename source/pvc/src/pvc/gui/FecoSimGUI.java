package pvc.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

import pvc.datamgmt.*;
import pvc.gui.comp.*;

@SuppressWarnings("serial")
public class FecoSimGUI extends JFrame implements ActionListener {
	//Sizing constants
	private static final float FracScreenWidth = 0.8f;
	private static final float FracScreenHeight = 0.8f;
	private static final float InfoPanelMaxWidthMult = 1.5f;
	private static final float ListMaxHeightMult = 2.1f;
	private static final int WinMargin = 10;
	private static final int RWinMargin = 6;
	private static final int TWinMargin = 3;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int ScrollBarAllowance = 18;

	private static final int ListWidth = 220;
	private static final int InfoLabelFontSize = 11;
	private static final int InfoPanelLargeFontSize = 12;

	//GUI Objects
	private JButton btnAdd, btnDelete, btnUnits,  btnEdit;
	private JList<String> selFecoSim;
	private JLabel lblFESDescription;
	private PreFEcoRunInfoPanelMaker.JPanel_FEcoSimInfoPanel infoPanel;


	//Data structures
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private WIITModel wiitModel;
	private AnalysisVehModelsSetup avms;
	private FEcoSimsC fecoSims;
	
	//Constructor
	public FecoSimGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel) {
		//Call Super
		super("Fuel Economy Simulations");
		
		//Set Variables
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		avms = AnalysisVehModelsSetup.readAnalysisVehModelsSetup(fs, aID);
		wiitModel = WIITModel.readWIITModel(fs, aID, avms);
		if (wiitModel == null) {
			pMP.subModuleFinished();
			return;
		}
		fecoSims = new FEcoSimsC(fs, aID);
		
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

	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnAdd) {
			new NewFecoSimGUI(fs, aID, fecoSims, avms, wiitModel, pMP);
			dispose();
			return;
		}
		if (source == btnDelete) {
			int selID = selFecoSim.getSelectedIndex();
			if (selID < 0) return;
			
			int dlgYesNoReturn = JOptionPane.showConfirmDialog(null, "Are sure you wish to delete the selected set of Fuel Economy Simulations?", 
					"Please Check", JOptionPane.YES_NO_OPTION|JOptionPane.WARNING_MESSAGE);
			if (dlgYesNoReturn == JOptionPane.YES_OPTION) {
				int moreYesNoReturn = JOptionPane.showConfirmDialog(null, "Deleting Fuel Economy Simulations Cannot be Un-Done. Are sure you wish to proceed?", 
						"Please Check", JOptionPane.YES_NO_OPTION|JOptionPane.WARNING_MESSAGE);
				if (moreYesNoReturn == JOptionPane.YES_OPTION) {
					fecoSims.deleteFecoSim(selID);
					selFecoSim.setListData(fecoSims.getTitlesArray());		
					selFecoSim.setSelectedIndex(-1);
				}
			}
			return;
		}
		if (source == btnUnits) {
			UnitsDialog dlg = new UnitsDialog(fs, aID);
			if (dlg.okPressed()) PreFEcoRunInfoPanelMaker.updateFEcoSimInfoPanel(infoPanel, wiitModel);
			return;
		}
		if (source == btnEdit) {
			int selID = selFecoSim.getSelectedIndex();
			if (selID < 0) return;
			
			EditFEcoSimTDMDialog dlg = new EditFEcoSimTDMDialog(fecoSims.getTitlesArray()[selID], fecoSims.getDescripton(selID),
					fecoSims.getAnnualMiles(selID), fecoSims.getAnnualDrivingDays(selID));
			if (dlg.okPressed()) {
				fecoSims.editFecoSim(selID, dlg.getUITitle(), dlg.getUIDescription(), dlg.getUIAnnualVMT(), dlg.getUIAnnualDriveDays());
				selFecoSim.setListData(fecoSims.getTitlesArray());		
				selFecoSim.setSelectedIndex(-1);
				return;
			}
		}
	}
	
	private void exitProcedure() {
		dispose();
		pMP.subModuleFinished();
	}

	private JPanel createContentPane() {
		//Create
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        int infoPanelMaxWidth = (int)(InfoPanelMaxWidthMult*ListWidth);
        int infoPanelAreaWidth = Math.min(infoPanelMaxWidth, (int)(FracScreenWidth*screenWidth) - ListWidth - WinMargin*3);
        
		int infoLabelHeight = LineSpacing*2;
        int listMaxHeight = (int)(ListMaxHeightMult*(LineSpacing + infoLabelHeight + WinMargin/2 + BtnHeight));
        int listHeight = Math.min(listMaxHeight, (int)(FracScreenHeight*screenHeight) - (LineSpacing*2 + infoLabelHeight + WinMargin/2 + BtnHeight + WinMargin*2));
        
		int panelWidth = infoPanelAreaWidth + ListWidth + WinMargin*3;
		int panelHeight = listHeight + LineSpacing*2 + infoLabelHeight + WinMargin/2 + BtnHeight + WinMargin*2;
		
		int infoPanelAreaHeight = panelHeight - WinMargin*2 - LineSpacing*2;
		
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

		//Create GUI Components
        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Completed Simulation Runs");
        lbl1.setSize(ListWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);

        Font descriptionLabelsFont = new Font(lbl1.getFont().getName(), Font.PLAIN, InfoLabelFontSize);

        cy += LineSpacing;
        
        selFecoSim = new JList<String>();
        selFecoSim.setLocation(0,0);
        selFecoSim.setSize(ListWidth, listHeight);
        selFecoSim.setBorder(blackBorder);
        selFecoSim.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		lstFecoSimSelChanged();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selFecoSim);
        sc1.setLocation(cx, cy);
        sc1.setSize(ListWidth, listHeight);  
        totalGUI.add(sc1);

        selFecoSim.setListData(fecoSims.getTitlesArray());
        
        cy += listHeight;
 
        JLabel lbl2 = new JLabel("Description");
        lbl2.setSize(ListWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;
        
        lblFESDescription = new JLabel();
        lblFESDescription.setSize(ListWidth, infoLabelHeight);
        lblFESDescription.setLocation(cx, cy);
        lblFESDescription.setFont(descriptionLabelsFont);
        lblFESDescription.setVerticalAlignment(SwingConstants.TOP);
        lblFESDescription.setBorder(blackBorder);
        totalGUI.add(lblFESDescription);
        
        cy += infoLabelHeight + WinMargin/2;

        int btnWidth3 = ((ListWidth-WinMargin)/3)+1;
        
        btnAdd = new JButton("New...");
        btnAdd.setSize(btnWidth3, BtnHeight);
        btnAdd.setLocation(cx, cy);
        btnAdd.addActionListener(this);
        totalGUI.add(btnAdd);
                
        btnEdit = new JButton("Edit");
        btnEdit.setSize(ListWidth - WinMargin - btnWidth3*2, BtnHeight);
        btnEdit.setLocation(cx + btnWidth3 + WinMargin/2, cy);
        btnEdit.addActionListener(this);
        btnEdit.setEnabled(false);
        totalGUI.add(btnEdit);
        
        btnDelete = new JButton("Delete");
        btnDelete.setSize(btnWidth3, BtnHeight);
        btnDelete.setLocation(cx + ListWidth - btnWidth3, cy);
        btnDelete.addActionListener(this);
        btnDelete.setEnabled(false);
        totalGUI.add(btnDelete);
        
        cx += ListWidth + WinMargin;
        cy = WinMargin;
        
        JLabel lbl3 = new JLabel("Simulation Settings");
        lbl3.setSize(infoPanelAreaWidth, LblHeight);
        lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);
        
        cy += LineSpacing;
        
        infoPanel = PreFEcoRunInfoPanelMaker.createFEcoSimInfoPanel(wiitModel,
        		infoPanelAreaWidth-ScrollBarAllowance, RWinMargin, TWinMargin, LblHeight, EdtHeight, LineSpacing, InfoPanelLargeFontSize, InfoLabelFontSize);
		infoPanel.setBorder(blackBorder);
		
        JScrollPane sc2 = new JScrollPane(infoPanel);
        sc2.setLocation(cx, cy);
        sc2.setSize(infoPanelAreaWidth, infoPanelAreaHeight);  
        totalGUI.add(sc2);

        cy += infoPanelAreaHeight;
        
        int btnWidth = ((ListWidth-WinMargin/2)/2)+1;
        
        btnUnits = new JButton("Units...");
        btnUnits.setSize(btnWidth, BtnHeight);
        btnUnits.setLocation(cx + infoPanelAreaWidth - btnWidth, cy + (LineSpacing - BtnHeight));
        btnUnits.addActionListener(this);
        totalGUI.add(btnUnits);
        
        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void lstFecoSimSelChanged() {
		int selID = selFecoSim.getSelectedIndex();
		if (selID < 0) {
	        btnDelete.setEnabled(false);
	        btnEdit.setEnabled(false);
	        lblFESDescription.setText("");
		} else {
	        btnDelete.setEnabled(true);
	        btnEdit.setEnabled(true);
	        lblFESDescription.setText("<html>"+fecoSims.getDescripton(selID)+"</html>");
		}
	}

}
