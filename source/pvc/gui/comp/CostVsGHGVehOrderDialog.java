package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.comp.CostVsGHGDisplaySetup;

@SuppressWarnings("serial")
public class CostVsGHGVehOrderDialog extends JDialog implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;

	private static final int LstHeightNumLineSpacing = 8;
	private static final int LstWidth = 240;
	
	//GUI Objects
	private JButton btnUp, btnDown;
	private JList<String> selVehModels;
	
	//Data Objects & Access
	private AnalysisVehModelsSetup.AVehModelSetup[] vms;
	private CostVsGHGDisplaySetup dSetup;	
	
	private boolean reOrderInvoked;
	public boolean reOrderInvoked() {return reOrderInvoked;}
	public CostVsGHGDisplaySetup displaySetup() {return dSetup;}


	public CostVsGHGVehOrderDialog(AnalysisVehModelsSetup.AVehModelSetup[] vmSetup, CostVsGHGDisplaySetup displaySetup) {
		//Super
		super(null, "Vehicle Models", Dialog.ModalityType.APPLICATION_MODAL);
		
		//Set data
		reOrderInvoked = false;		
		vms = vmSetup;
		dSetup = new CostVsGHGDisplaySetup(displaySetup);	//Using the Copy Constructor		

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
		
		if (source == btnUp) {
			int curSel = selVehModels.getSelectedIndex();
			if (curSel < 0) return;

			dSetup.moveVehOrderUp(curSel);
			rePopulateList();
			
			reOrderInvoked = true;
			selVehModels.setSelectedIndex(curSel-1);
		}
		if (source == btnDown) {
			int curSel = selVehModels.getSelectedIndex();
			if (curSel < 0) return;

			dSetup.moveVehOrderDown(curSel);
			rePopulateList();
			
			reOrderInvoked = true;
			selVehModels.setSelectedIndex(curSel+1);
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
        
        JLabel lbl = new JLabel("Drawing Order of Vehicle Models");
        lbl.setSize(LstWidth, LblHeight);
        lbl.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl);
        
        cy += LineSpacing;
        
        selVehModels = new JList<String>();
        selVehModels.setLocation(0,0);
        selVehModels.setSize(LstWidth, lstHeight);
        selVehModels.setBorder(blackBorder);
        selVehModels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		updateButtonsActivity();
        	}
        });
        JScrollPane sc1 = new JScrollPane(selVehModels);
        sc1.setLocation(cx, cy);
        sc1.setSize(LstWidth, lstHeight);  
        totalGUI.add(sc1);
        
        rePopulateList();

        cy += lstHeight + WinMargin/2;

        int btnWidth = (LstWidth-TMargin)/2;
        
        btnUp = new JButton("Move Up");
        btnUp.setSize(btnWidth, BtnHeight);
        btnUp.setLocation(cx, cy);
        btnUp.addActionListener(this);
        btnUp.setEnabled(false);
        totalGUI.add(btnUp);

        btnDown = new JButton("Move Down");
        btnDown.setSize(btnWidth, BtnHeight);
        btnDown.setLocation(cx + LstWidth - btnWidth, cy);
        btnDown.addActionListener(this);
        btnDown.setEnabled(false);
        totalGUI.add(btnDown);
        
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	private void updateButtonsActivity() {
		int curSel = selVehModels.getSelectedIndex();
		
		if (curSel < 0) {
	        btnUp.setEnabled(false);
	        btnDown.setEnabled(false);
	        return;
		}
		
		if (curSel > 0) btnUp.setEnabled(true);
		else btnUp.setEnabled(false);
		
		if (curSel < (vms.length-1)) btnDown.setEnabled(true);
		else btnDown.setEnabled(false);
	}
	private void rePopulateList() {
		String[] st = new String[vms.length];
		for (int i=0; i<st.length; i++) st[i] = new String(vms[dSetup.getVehIDtoDraw(i)].shortName);
		selVehModels.setListData(st);
	}
}
