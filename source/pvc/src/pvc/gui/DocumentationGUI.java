package pvc.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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

import fastsimjava.FSJOneFileVehModel;
import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.FFStructure;

@SuppressWarnings("serial")
public class DocumentationGUI extends JFrame implements ActionListener {
	
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = BtnHeight*2;
	
	private static final int ListWidth = 280;
	private static final int BtnWidth = 160;
	private static final int InfoLabelFontSize = 11;

	//GUI Objects
	private JButton btnLaunch;
	private JList<String> selVehModel;
	private JLabel lblVehName;

	//Data objects
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;


	public DocumentationGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel) {
		//Call super()
		super("Models Documentation");
		
		//Set data objects
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		
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
		pMP.subModuleFinished();
		dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnLaunch) {
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
			
			String pdfFileName = fs.getFilePath_FASTSimVehModelPDFDoc(aID, avm.vehModelsSetup()[vID].shortName);
			File f = new File(pdfFileName);
			
			if (!f.exists()) {
				JOptionPane.showMessageDialog(null, "<html>Documentation at:<br>"+pdfFileName+
						"<br><br>... for this Vehicle Model may not be available</html>", 
						"File Not Found", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try {
				Desktop.getDesktop().open(f);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "<html>Unable to open the file:<br>"+pdfFileName
						+"<br><br>Please try navigating to the folder and opening the file manually</html>", 
						"Unable to Launch Document", JOptionPane.OK_OPTION|JOptionPane.WARNING_MESSAGE);
			}
			
			return;
		}
	}

	private JPanel createContentPane() {
		//Create
		int lstVehiclesHeight = LineSpacing*10;
		int panelHeight = LineSpacing + lstVehiclesHeight + LineSpacing + EdtHeight + WinMargin*3 + BigBtnHeight;
		int panelWidth = ListWidth + WinMargin*2;
		
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int cx = WinMargin;
        int cy = WinMargin/2;
        
        JLabel lbl1 = new JLabel("Available Vehicle Models");
        lbl1.setSize(ListWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        Font descriptionLabelsFont = new Font(lbl1.getFont().getName(), Font.PLAIN, InfoLabelFontSize);
        
        cy += LineSpacing;
        
        selVehModel = new JList<String>();
        selVehModel.setLocation(0,0);
        selVehModel.setSize(ListWidth, lstVehiclesHeight);
        selVehModel.setBorder(blackBorder);
        
		AnalysisVehModelsSetup avm = null;
		try {
			avm = new AnalysisVehModelsSetup(fs, aID);
			String[] vehShortNames = new String[avm.numVehModels()];
			for (int i=0; i<vehShortNames.length; i++) vehShortNames[i] = new String(avm.vehModelsSetup()[i].shortName);
			selVehModel.setListData(vehShortNames);	        
		} catch (Exception e) {
			lblVehName.setText("");
			selVehModel.setListData(new String[0]);
		}
		
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

        JLabel lbl2 = new JLabel("Selected Vehicle");
        lbl2.setSize(ListWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);

        cy += LineSpacing;
        		        
		lblVehName = new JLabel();
		lblVehName.setSize(ListWidth, EdtHeight);
		lblVehName.setLocation(cx, cy);
		lblVehName.setFont(descriptionLabelsFont);
		lblVehName.setHorizontalAlignment(SwingConstants.CENTER);
		lblVehName.setBorder(blackBorder);
		totalGUI.add(lblVehName);
		
		cy += LineSpacing + WinMargin;

        btnLaunch = new JButton("Show Documentation");
        btnLaunch.setSize(BtnWidth, BigBtnHeight);
        btnLaunch.setLocation(cx + (ListWidth-BtnWidth)/2, cy);
        btnLaunch.addActionListener(this);
        totalGUI.add(btnLaunch);
        
        selVehModel.setSelectedIndex(0);
        
        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}	
	
	private void lstVehSelChanged() {
		
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
