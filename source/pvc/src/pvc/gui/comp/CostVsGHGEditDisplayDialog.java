package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.comp.CostVsGHGDisplaySetup;

@SuppressWarnings("serial")
public class CostVsGHGEditDisplayDialog extends JDialog implements ActionListener {
	private static final int GridAndAxesLineWidthMin = 1;
	private static final int GridAndAxesLineWidthMax = 5;
	
	//Sizing
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int ScrollBarAllowance = 20;

	private static final float NumSubPanelsVisible = 3.5f;

	private static final int ChkShowGridWidth = 90;
	private static final int SpnWidth = 50;
	private static final int LblWidth1 = 100;
	private static final int LblWidth2 = 40;
	private static final int BtnColorWidth = 100;
	private static final int BtnOKWidth = 90;

	
	//GUI Objects
	private CostVsGHGPointDisplayPanelMaker.CostVsGHGEditDisplayPanel mpPanel;
	private JButton btnOK, btnCancel, btnReOrder, btnGridColor;
	private JCheckBox chkShowGrid;
	private JSpinner spnGrid, spnAxes;
	private ColorJLabel gLabel;

	
	//Data Objects
	private AnalysisVehModelsSetup avms;
	private CostVsGHGDisplaySetup dSetup;
	
	private boolean okPressed;
	private boolean reOrderInvoked;

	public boolean okPressed() {return okPressed;}
	public boolean reOrderInvoked() {return reOrderInvoked;}
	public CostVsGHGDisplaySetup displaySetup() {return dSetup;}

	
	public CostVsGHGEditDisplayDialog(AnalysisVehModelsSetup avModelsSetup, CostVsGHGDisplaySetup displaySetup) {
		//Super
		super(null, "Display Options", Dialog.ModalityType.APPLICATION_MODAL);
		
		//Set data
		okPressed = false;
		reOrderInvoked = false;
		
		avms = avModelsSetup;
		dSetup = new CostVsGHGDisplaySetup(displaySetup);	//Using the Copy Constructor		
		
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
			screenToData();
			okPressed = true;
			dispose();	
			return;
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
			return;
		}
		if (source == btnGridColor) {
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.getGridColorRed(), dSetup.getGridColorGreen(), dSetup.getGridColorBlue());
			if (dlg.okPressed()) {
				dSetup.setGridColor(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
				gLabel.repaint();
			}
			return;
		}
		if (source == btnReOrder) {
			screenToData();
			CostVsGHGVehOrderDialog dlg = new CostVsGHGVehOrderDialog(avms.vehModelsSetup(), dSetup);
			if (dlg.reOrderInvoked()) {
				dSetup = new CostVsGHGDisplaySetup(dlg.displaySetup());
				reOrderInvoked = true;
				dispose();	
			}
			return;
		}
	}
	private void screenToData() {
		//Grid color does need to be grabbed (already set if edited)
		
		//Grab Grid Flag & Line Width
		dSetup.setShowGrid(chkShowGrid.isSelected());
		dSetup.setGridLineWidth(Integer.parseInt(spnGrid.getValue().toString()));
		
		//Axes Line Width
		dSetup.setAxesLineWidth(Integer.parseInt(spnAxes.getValue().toString()));

		//Grab Point Plotting Options
		for (int i=0; i<mpPanel.subPanels.length; i++) {
			dSetup.setVehDrawData(i, mpPanel.subPanels[i].ptData());
		}
	}

	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int scAreaHeight = (int)(NumSubPanelsVisible * CostVsGHGPointDisplayPanelMaker.SubPanelHeight);
        mpPanel = CostVsGHGPointDisplayPanelMaker.createCostVsGHGEditDisplayPanel(avms.vehModelsSetup(), dSetup);
    	mpPanel.setBorder(blackBorder);
        
        int ppPanelPrefHeight = mpPanel.getPreferredSize().height;
        int scAreaWidth = mpPanel.getPreferredSize().width;
        if (ppPanelPrefHeight > scAreaHeight) scAreaWidth += ScrollBarAllowance;
        
        int panelWidth = scAreaWidth + WinMargin*2;
        int panelHeight = scAreaHeight + LineSpacing + BigBtnHeight + WinMargin*4;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        chkShowGrid = new JCheckBox("Show Grid");
        chkShowGrid.setSize(ChkShowGridWidth, EdtHeight);
        chkShowGrid.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
        chkShowGrid.setSelected(dSetup.showGrid());
		totalGUI.add(chkShowGrid);
		
		cx += ChkShowGridWidth + WinMargin;
		
		JLabel lbl1 = new JLabel("Grid Lines Width");
		lbl1.setSize(LblWidth1, LblHeight);
		lbl1.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl1.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl1);
		
		cx += LblWidth1 + TMargin;
				
        SpinnerNumberModel spmGrid = new SpinnerNumberModel(dSetup.gridLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        SpinnerNumberModel spmAxes = new SpinnerNumberModel(dSetup.axesLineWidth(), GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);

        spnGrid = new JSpinner(spmGrid);
        spnGrid.setSize(SpnWidth, EdtHeight);
        spnGrid.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
        spnGrid.setBorder(blackBorder);
		totalGUI.add(spnGrid);
		
		cx += SpnWidth + WinMargin;

		JLabel lbl2 = new JLabel("Color");
		lbl2.setSize(LblWidth2, LblHeight);
		lbl2.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl2.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl2);
		
		cx += LblWidth2 + TMargin;

		gLabel = new ColorJLabel();
		gLabel.setSize(SpnWidth, EdtHeight);
		gLabel.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		gLabel.setBorder(blackBorder);
		totalGUI.add(gLabel);

		cx += SpnWidth + TMargin;
		
		btnGridColor = new JButton("Edit Color...");
		btnGridColor.setSize(BtnColorWidth, BtnHeight);
		btnGridColor.setLocation(cx, cy + (LineSpacing-BtnHeight)/2);
		btnGridColor.addActionListener(this);
		totalGUI.add(btnGridColor);

		cx = WinMargin + scAreaWidth - (LblWidth1 + TMargin + SpnWidth);
		
		JLabel lbl3 = new JLabel("Axes Lines Width");
		lbl3.setSize(LblWidth1, LblHeight);
		lbl3.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl3.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl3);
		
		cx += LblWidth1 + TMargin;
		
		spnAxes = new JSpinner(spmAxes);
		spnAxes.setSize(SpnWidth, EdtHeight);
		spnAxes.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		spnAxes.setBorder(blackBorder);
		totalGUI.add(spnAxes);

		cx = WinMargin;
        cy += LineSpacing + WinMargin;
        
        if (ppPanelPrefHeight > scAreaHeight) {
        	mpPanel.setAutoscrolls(true);
        	
			JScrollPane sc1 = new JScrollPane(mpPanel);
			sc1.setLocation(cx, cy);
			sc1.setSize(scAreaWidth, scAreaHeight);
			totalGUI.add(sc1);
        } else {
        	mpPanel.setSize(scAreaWidth, scAreaHeight);
        	mpPanel.setLocation(cx, cy);
			totalGUI.add(mpPanel);
        }
        
        cy += scAreaHeight + WinMargin;
        
        int btnReOrderWidth = BtnOKWidth*2 + TMargin;
        btnReOrder = new JButton("Change Drawing Order...");
        btnReOrder.setSize(btnReOrderWidth, BigBtnHeight);
        btnReOrder.setLocation(cx, cy);
        btnReOrder.addActionListener(this);
		totalGUI.add(btnReOrder);
		
		cx += scAreaWidth - btnReOrderWidth;
		
		btnOK = new JButton("OK");
		btnOK.setSize(BtnOKWidth, BigBtnHeight);
		btnOK.setLocation(cx, cy);
		btnOK.addActionListener(this);
		totalGUI.add(btnOK);

		cx += BtnOKWidth + TMargin;
		
		btnCancel = new JButton("Cancel");
		btnCancel.setSize(BtnOKWidth, BigBtnHeight);
		btnCancel.setLocation(cx, cy);
		btnCancel.addActionListener(this);
		totalGUI.add(btnCancel);
                
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private class ColorJLabel extends JLabel {
		private ColorJLabel() {super();}
		@Override
	    protected void paintComponent(Graphics g) {
			//Call base function
			super.paintComponent(g);
			
			//White fill
			g.setColor(dSetup.getGridColor());
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
	}
}
