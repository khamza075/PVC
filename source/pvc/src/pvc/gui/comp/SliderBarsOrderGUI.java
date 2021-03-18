package pvc.gui.comp;

import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.datamgmt.*;
import pvc.gui.*;

@SuppressWarnings("serial")
public class SliderBarsOrderGUI extends JFrame implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;

	private static final int LstHeightNumLineSpacing = 12;
	private static final int LstWidth = 300;
	private static final int BtnWidth = 100;
	
	//GUI Objects
	private JButton btnUp, btnDown;
	private JList<String> selParameters;


	//Data objects
	private FFStructure fs;
	private int aID;
	private MainPanelGUI pMP;
	private CurVisualizationType cvType;
	private SliderBarsManager sbarMan;
	private boolean sbarsEdited;

	public SliderBarsOrderGUI(FFStructure cFS, int analysisID, MainPanelGUI pMainPanel, CurVisualizationType curVisualization, 
			SliderBarsManager sliderBarsManager, boolean hasBeenEdited) {
		//Call Super
		super("Parameters Ordering");
		
		//Set Variables
		fs = cFS;
		aID = analysisID;
		pMP = pMainPanel;
		cvType = curVisualization;
		sbarMan = sliderBarsManager;
		sbarsEdited = hasBeenEdited;

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
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	            exitProcedure();
	        }
	    });

        //Set size and Launch
        setSize(winWidth, winHeight);
        setResizable(false);
        setVisible(true);
	}
	
	private void exitProcedure() {
		dispose();
		new SliderBarsEditorGUI(fs, aID, pMP, cvType, sbarMan, sbarsEdited);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnUp) {
			int curSel = selParameters.getSelectedIndex();
			if (curSel < 0) return;

			sbarMan.moveSBarUp(curSel);
			rePopulateList();
			
			sbarsEdited = true;
			selParameters.setSelectedIndex(curSel-1);
		}
		if (source == btnDown) {
			int curSel = selParameters.getSelectedIndex();
			if (curSel < 0) return;

			sbarMan.moveSBarDown(curSel);
			rePopulateList();
			
			sbarsEdited = true;
			selParameters.setSelectedIndex(curSel+1);
		}
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        int lstHeight = LineSpacing*LstHeightNumLineSpacing;
        int panelWidth = LstWidth + WinMargin*2;
        int panelHeight = WinMargin*2 + LineSpacing + lstHeight + WinMargin/2 + BtnHeight;
        
        int cy = WinMargin;
        int cx = WinMargin;
        
        JLabel lbl = new JLabel("Select a Parameter then Use Buttons to Re-Order");
        lbl.setSize(LstWidth, LblHeight);
        lbl.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl);
        
        cy += LineSpacing;
        
        selParameters = new JList<String>();
        selParameters.setLocation(0,0);
        selParameters.setSize(LstWidth, lstHeight);
        selParameters.setBorder(blackBorder);
        selParameters.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		updateButtonsActivity();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selParameters);
        sc1.setLocation(cx, cy);
        sc1.setSize(LstWidth, lstHeight);  
        totalGUI.add(sc1);
        
        rePopulateList();

        cy += lstHeight + WinMargin/2;

        btnUp = new JButton("Move Up");
        btnUp.setSize(BtnWidth, BtnHeight);
        btnUp.setLocation(cx + LstWidth/2 - WinMargin/4 - BtnWidth, cy);
        btnUp.addActionListener(this);
        btnUp.setEnabled(false);
        totalGUI.add(btnUp);

        btnDown = new JButton("Move Down");
        btnDown.setSize(BtnWidth, BtnHeight);
        btnDown.setLocation(cx + LstWidth/2 + WinMargin/4, cy);
        btnDown.addActionListener(this);
        btnDown.setEnabled(false);
        totalGUI.add(btnDown);
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	private void updateButtonsActivity() {
		int curSel = selParameters.getSelectedIndex();
		
		if (curSel < 0) {
	        btnUp.setEnabled(false);
	        btnDown.setEnabled(false);
	        return;
		}
		
		if (curSel > 0) btnUp.setEnabled(true);
		else btnUp.setEnabled(false);
		
		if (curSel < (sbarMan.numVisibleSBars()-1)) btnDown.setEnabled(true);
		else btnDown.setEnabled(false);
	}
	private void rePopulateList() {
		selParameters.setListData(sbarMan.unitsFormattedCaptions());
	}
}
