package pvc.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.*;

@SuppressWarnings("serial")
public class AnalysisSelectorGUI extends JFrame implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = BtnHeight*2;
	
	private static final int ListWidth = 220;
	private static final int InfoLabelFontSize = 11;

	//GUI Objects
	private JButton btnLaunch;
	private JList<String> selAnalysis, selVehModel;
	private JLabel lblAnalysisDescription, lblADTitle, lblVehName, lblVehNameTitle;


	//Data structures
	private FFStructure fs;
	private AnalysisTitleDescription[] availableAnalysis;

	public AnalysisSelectorGUI(FFStructure cFS) {
		//Call super()
		super("Analysis Selector");
		
		//Set data objects
		fs = cFS;
		availableAnalysis = new AnalysisTitleDescription[fs.analysisFolders().length];
		try {
			for (int i=0; i<availableAnalysis.length; i++) availableAnalysis[i] = new AnalysisTitleDescription(fs, i);
		} catch (Exception e) {
			System.exit(0);
			return;
		}
		
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
		
		if (source == btnLaunch) {
			int aID = selAnalysis.getSelectedIndex();
			new MainPanelGUI(fs, aID);
			dispose();
			return;
		}
	}

	private JPanel createContentPane() {
		//Create
		int infoLabelHeight = LineSpacing*2;
		int lstAvAnalysisHeight = LineSpacing*6;
		int lstVehiclesHeight = lstAvAnalysisHeight + LineSpacing + infoLabelHeight + WinMargin + BigBtnHeight - EdtHeight - LineSpacing;
		int panelHeight = LineSpacing + lstVehiclesHeight + LineSpacing + EdtHeight + WinMargin*2;
		int panelWidth = ListWidth*2 + WinMargin*3;
		
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Available Analysis");
        lbl1.setSize(ListWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        Font descriptionLabelsFont = new Font(lbl1.getFont().getName(), Font.PLAIN, InfoLabelFontSize);
        
        cy += LineSpacing;
        
        selAnalysis = new JList<String>();
        selAnalysis.setLocation(0,0);
        selAnalysis.setSize(ListWidth, lstAvAnalysisHeight);
        selAnalysis.setBorder(blackBorder);
        selAnalysis.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		lstAnalysisSelChanged();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selAnalysis);
        sc1.setLocation(cx, cy);
        sc1.setSize(ListWidth, lstAvAnalysisHeight);  
        totalGUI.add(sc1);
        
        String[] stAnalysisTitles = new String[availableAnalysis.length];
        for (int i=0; i<stAnalysisTitles.length; i++) stAnalysisTitles[i] = new String(availableAnalysis[i].title());
        selAnalysis.setListData(stAnalysisTitles);
        
        cy += lstAvAnalysisHeight;
        
        lblADTitle = new JLabel("Description");
        lblADTitle.setSize(ListWidth, LblHeight);
        lblADTitle.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lblADTitle);

        cy += LineSpacing;
              
        lblAnalysisDescription = new JLabel();
        lblAnalysisDescription.setSize(ListWidth, infoLabelHeight);
        lblAnalysisDescription.setLocation(cx, cy);
        lblAnalysisDescription.setFont(descriptionLabelsFont);
        lblAnalysisDescription.setVerticalAlignment(SwingConstants.TOP);
        lblAnalysisDescription.setBorder(blackBorder);
        totalGUI.add(lblAnalysisDescription);
        
        cy += infoLabelHeight + WinMargin;
        
        btnLaunch = new JButton("Launch...");
        btnLaunch.setSize(ListWidth, BigBtnHeight);
        btnLaunch.setLocation(cx, cy);
        btnLaunch.addActionListener(this);
        btnLaunch.setEnabled(false);
        totalGUI.add(btnLaunch);
        
        cy = WinMargin;
        cx += ListWidth + WinMargin;
        
        JLabel lbl2 = new JLabel("Vehicle Models in Analysis");
        lbl2.setSize(ListWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);

        cy += LineSpacing;
        
        selVehModel = new JList<String>();
        selVehModel.setLocation(0,0);
        selVehModel.setSize(ListWidth, lstVehiclesHeight);
        selVehModel.setBorder(blackBorder);
        selVehModel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		lstVehSelChanged();
        	}
        });
        JScrollPane sc2 = new JScrollPane(selVehModel);
        sc2.setLocation(cx, cy);
        sc2.setSize(ListWidth, lstVehiclesHeight);  
        totalGUI.add(sc2);
        
        cy += lstVehiclesHeight;
        
        lblVehNameTitle = new JLabel("Highlighted Vehicle");
        lblVehNameTitle.setSize(ListWidth, LblHeight);
        lblVehNameTitle.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lblVehNameTitle);

        cy += LineSpacing;
              
        lblVehName = new JLabel();
        lblVehName.setSize(ListWidth, EdtHeight);
        lblVehName.setLocation(cx, cy);
        lblVehName.setFont(descriptionLabelsFont);
        lblVehName.setBorder(blackBorder);
        totalGUI.add(lblVehName);

        
        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void lstAnalysisSelChanged() {
		int aID = selAnalysis.getSelectedIndex();
		if (aID < 0) {
			lblAnalysisDescription.setText("");
	        btnLaunch.setEnabled(false);
			lblVehName.setText("");
			selVehModel.setListData(new String[0]);
			return;
		}
		
        btnLaunch.setEnabled(true);
		lblAnalysisDescription.setText("<html>"+availableAnalysis[aID].description()+"</html>");
		
		AnalysisVehModelsSetup avm = null;
		try {
			avm = new AnalysisVehModelsSetup(fs, aID);
		} catch (Exception e) {
			lblVehName.setText("");
			selVehModel.setListData(new String[0]);
			return;
		}
		
		String[] vehShortNames = new String[avm.numVehModels()];
		for (int i=0; i<vehShortNames.length; i++) vehShortNames[i] = new String(avm.vehModelsSetup()[i].shortName);
		selVehModel.setListData(vehShortNames);
	}
	private void lstVehSelChanged() {
		int aID = selAnalysis.getSelectedIndex();
		if (aID < 0) {
			lblVehName.setText("");
			return;
		}
		
		AnalysisVehModelsSetup avm = null;
		try {
			avm = new AnalysisVehModelsSetup(fs, aID);
		} catch (Exception e) {
			lblVehName.setText("");
			return;
		}
		
		int vID = selVehModel.getSelectedIndex();
		if (vID < 0) {
			lblVehName.setText("");
			return;
		}
		
		String vehModelFileName = fs.getFilePath_FASTSimVehModel(aID, avm.vehModelsSetup()[vID].shortName);
		FSJOneFileVehModel ofvModel = new FSJOneFileVehModel(vehModelFileName);
		lblVehName.setText(ofvModel.vehModelParam.general.name);
	}
}
