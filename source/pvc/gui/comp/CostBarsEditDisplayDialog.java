package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
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

import pvc.datamgmt.comp.CostBarsDisplaySetup;

@SuppressWarnings("serial")
public class CostBarsEditDisplayDialog extends JDialog implements ActionListener {
	//Spin button value limits
	private static final int GridAndAxesLineWidthMin = 1;
	private static final int GridAndAxesLineWidthMax = 5;
	private static final int MinFontSize = 8;
	private static final int MaxFontSize = 16;
	
	//Sizing
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	private static final int ScrollBarAllowance = 20;
	
	private static final int ScrollHeightNumLineSpacing = 15;

	private static final int ChkShowGridWidth = 90;
	private static final int ChkFontBoldWidth = 54;
	private static final int SpnWidth = 50;
	private static final int LblWidth1 = 100;
	private static final int LblWidth2 = 40;
	private static final int BtnColorWidth = 100;
	private static final int BtnReOrderWidth = 142;
	private static final int BtnOKWidth = 90;

	
	//GUI Objects
	private CostBarsEDPanelMaker.CBEDPanel chkPanel;
	private CostBarsEDPanelMaker.CBVehPanel vehPanel;
	private ColorJLabel gridColorLabel;
	private JButton btnOK, btnCancel, btnReOrder, btnGridColor;
	private JCheckBox chkShowGrid, chkLegendFontBold;
	private JSpinner spnGridLineWidth, spnAxesLineWidth, spnLegendFont;
	
	//Data Objects
	private CostBarsDisplaySetup dSetup;
	private boolean okPressed;
	private boolean reOrderInvoked;

	public boolean okPressed() {return okPressed;}
	public boolean reOrderInvoked() {return reOrderInvoked;}
	public CostBarsDisplaySetup getDisplaySetup() {return new CostBarsDisplaySetup(dSetup);}
	
	public CostBarsEditDisplayDialog(CostBarsDisplaySetup displaySetup) {
		//Super
		super(null, "Display Options", Dialog.ModalityType.APPLICATION_MODAL);
		
		//Set data
		okPressed = false;
		reOrderInvoked = false;
		
		dSetup = new CostBarsDisplaySetup(displaySetup);	//Using the Copy Constructor		

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
			ColorEditorDialog dlg = new ColorEditorDialog(dSetup.gridColorRed, dSetup.gridColorGreen, dSetup.gridColorBlue);
			if (dlg.okPressed()) {
				dSetup.gridColorRed = dlg.colorRed();
				dSetup.gridColorGreen = dlg.colorGreen();
				dSetup.gridColorBlue = dlg.colorBlue();
				gridColorLabel.setColor(new Color(dSetup.gridColorRed, dSetup.gridColorGreen, dSetup.gridColorBlue));
			}
			return;
		}
		if (source == btnReOrder) {
			screenToData();
			CostBarsEDVehOrderDialog dlg = new CostBarsEDVehOrderDialog(dSetup);
			
			if (dlg.reOrderInvoked()) {
				dSetup = new CostBarsDisplaySetup(dlg.getDisplaySetup());
				reOrderInvoked = true;
				dispose();	
			}

			return;
		}
	}
	private void screenToData() {		
		dSetup.axesLineWidth = Integer.parseInt(spnAxesLineWidth.getValue().toString());
		dSetup.gridLineWidth = Integer.parseInt(spnGridLineWidth.getValue().toString());
		dSetup.legendFontSize = Integer.parseInt(spnLegendFont.getValue().toString());
		
		dSetup.showGrid = chkShowGrid.isSelected();
		dSetup.legendFontBold = chkLegendFontBold.isSelected();
	}
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        chkPanel = CostBarsEDPanelMaker.createCostBarsCheckBoxesPanel(dSetup);
        chkPanel.setBorder(blackBorder);
        
        int chkPanelPrefWidth = chkPanel.getPreferredSize().width;
        int chkPanelPrefHeight = chkPanel.getPreferredSize().height;
        
        int hscChkWidth = chkPanelPrefWidth;
        int hscChkHeight = ScrollHeightNumLineSpacing * LineSpacing;
        if (hscChkHeight < chkPanelPrefHeight) hscChkWidth += ScrollBarAllowance;
           
        vehPanel = CostBarsEDPanelMaker.createVehiclesCheckBoxesPanel(dSetup);
        vehPanel.setBorder(blackBorder);
        
        int vehPanelPreferredWidth = vehPanel.getPreferredSize().width;
        int vehPanelPreferredHeight = vehPanel.getPreferredSize().height;
        
        int hscVehWidth = vehPanelPreferredWidth;
        if (hscChkHeight < vehPanelPreferredHeight) hscVehWidth += ScrollBarAllowance;

        int panelWidth = hscVehWidth + hscChkWidth + WinMargin*3;
        int panelHeight = WinMargin + LineSpacing + WinMargin + LineSpacing + hscChkHeight + WinMargin + BigBtnHeight + WinMargin;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        SpinnerNumberModel spmGrid = new SpinnerNumberModel(dSetup.gridLineWidth, GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        SpinnerNumberModel spmAxes = new SpinnerNumberModel(dSetup.axesLineWidth, GridAndAxesLineWidthMin, GridAndAxesLineWidthMax, 1);
        SpinnerNumberModel spmLegend = new SpinnerNumberModel(dSetup.legendFontSize, MinFontSize, MaxFontSize, 1);

        chkShowGrid = new JCheckBox("Show Grid");
        chkShowGrid.setSize(ChkShowGridWidth, EdtHeight);
        chkShowGrid.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
        chkShowGrid.setSelected(dSetup.showGrid);
		totalGUI.add(chkShowGrid);
		
		cx += ChkShowGridWidth + WinMargin;
		
		JLabel lbl01 = new JLabel("Grid Lines Width");
		lbl01.setSize(LblWidth1, LblHeight);
		lbl01.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl01.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl01);
		
		cx += LblWidth1 + TMargin;

		spnGridLineWidth = new JSpinner(spmGrid);
		spnGridLineWidth.setSize(SpnWidth, EdtHeight);
		spnGridLineWidth.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		spnGridLineWidth.setBorder(blackBorder);
		totalGUI.add(spnGridLineWidth);
		
		cx += SpnWidth + WinMargin;

		JLabel lbl02 = new JLabel("Color");
		lbl02.setSize(LblWidth2, LblHeight);
		lbl02.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl02.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl02);
		
		cx += LblWidth2 + TMargin;

		gridColorLabel = new ColorJLabel(new Color(dSetup.gridColorRed, dSetup.gridColorGreen, dSetup.gridColorBlue));
		gridColorLabel.setSize(SpnWidth, EdtHeight);
		gridColorLabel.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		gridColorLabel.setBorder(blackBorder);
		totalGUI.add(gridColorLabel);

		cx += SpnWidth + TMargin;
		
		btnGridColor = new JButton("Edit Color...");
		btnGridColor.setSize(BtnColorWidth, BtnHeight);
		btnGridColor.setLocation(cx, cy + (LineSpacing-BtnHeight)/2);
		btnGridColor.addActionListener(this);
		totalGUI.add(btnGridColor);

		cx = panelWidth - (WinMargin + LblWidth1 + TMargin + SpnWidth);
		
		JLabel lbl3 = new JLabel("Axes Lines Width");
		lbl3.setSize(LblWidth1, LblHeight);
		lbl3.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl3.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl3);
		
		cx += LblWidth1 + TMargin;
		
		spnAxesLineWidth = new JSpinner(spmAxes);
		spnAxesLineWidth.setSize(SpnWidth, EdtHeight);
		spnAxesLineWidth.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		spnAxesLineWidth.setBorder(blackBorder);
		totalGUI.add(spnAxesLineWidth);

		cx = panelWidth - (WinMargin + LblWidth1 + TMargin*2 + SpnWidth + ChkFontBoldWidth);
		cy += LineSpacing + WinMargin;
		
		JLabel lbl4 = new JLabel("Legend Font Size");
		lbl4.setSize(LblWidth1, LblHeight);
		lbl4.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
		lbl4.setHorizontalAlignment(SwingConstants.RIGHT);
		totalGUI.add(lbl4);
		
		cx += LblWidth1 + TMargin;
		
		spnLegendFont = new JSpinner(spmLegend);
		spnLegendFont.setSize(SpnWidth, EdtHeight);
		spnLegendFont.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		spnLegendFont.setBorder(blackBorder);
		totalGUI.add(spnLegendFont);
		
		cx += SpnWidth + TMargin;
		
		chkLegendFontBold = new JCheckBox("Bold");
		chkLegendFontBold.setSize(ChkFontBoldWidth, EdtHeight);
		chkLegendFontBold.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
		chkLegendFontBold.setSelected(dSetup.legendFontBold);
		totalGUI.add(chkLegendFontBold);
		        
        cx = WinMargin;
        cy = WinMargin*2 + LineSpacing;
        
        JLabel lbl2 = new JLabel("Select Vehicle Models to Plot");
        lbl2.setSize(hscVehWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;
        
        if (hscChkHeight < vehPanelPreferredHeight) {
        	vehPanel.setAutoscrolls(true);
        	
        	JScrollPane sc = new JScrollPane(vehPanel);
        	sc.setLocation(cx, cy);
        	sc.setSize(hscVehWidth, hscChkHeight);
			totalGUI.add(sc);        	
        } else {
        	vehPanel.setSize(vehPanelPreferredWidth, hscChkHeight);
        	vehPanel.setLocation(cx, cy);
        	totalGUI.add(vehPanel);
        }        
        
        cx = WinMargin*2 + hscVehWidth;
        cy = WinMargin*2 + LineSpacing;
        
        JLabel lbl1 = new JLabel("Select Cost Elements to Plot");
        lbl1.setSize(hscChkWidth/2, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl1);
        
        cy += LineSpacing;
        
        if (hscChkHeight < chkPanelPrefHeight) {
        	chkPanel.setAutoscrolls(true);
        	
        	JScrollPane sc = new JScrollPane(chkPanel);
        	sc.setLocation(cx, cy);
        	sc.setSize(hscChkWidth, hscChkHeight);
			totalGUI.add(sc);        	
        } else {
        	chkPanel.setSize(chkPanelPrefWidth, hscChkHeight);
        	chkPanel.setLocation(cx, cy);
        	totalGUI.add(chkPanel);
        }
        
        cy += hscChkHeight + WinMargin;
        cx = WinMargin;
        
        btnReOrder = new JButton("Change Order...");
        btnReOrder.setSize(BtnReOrderWidth, BigBtnHeight);
        btnReOrder.setLocation(cx, cy);
        btnReOrder.addActionListener(this);
        totalGUI.add(btnReOrder);
        
        cx = panelWidth - BtnOKWidth*2 - WinMargin - TMargin;
        
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
	
}
