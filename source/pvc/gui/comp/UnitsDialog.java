package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import pvc.datamgmt.FFStructure;
import pvc.datamgmt.comp.DUnits;

@SuppressWarnings("serial")
public class UnitsDialog extends JDialog implements ActionListener {
	//Sizing constants
	private static final float FracScreenMaxHeight = 0.55f;
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int ScrollBarWidthAllowance = 18;
	private static final int BtnWidth = 80;
	private static final int OptBtnWidth = 220;
	private static final int OptBtnFontSize = 11;

	//GUI Objects
	private JButton btnOK, btnCancel;
	private ArrayOfJRadioButton[] optButtonArrays;
	
	
	//Data structures
	private FFStructure fs;
	private int aID;
	private boolean okPressed;
	
	
	public UnitsDialog(FFStructure cFS, int analysisID) {
		super(null, "Select Display Units", Dialog.ModalityType.APPLICATION_MODAL);
		
		okPressed = false;
		fs = cFS;
		aID = analysisID;
		
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
        unitsToScreen();
		setVisible(true);		
	}
	public boolean okPressed() {return okPressed;}

	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			screenToUnits();
			saveUnits();
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
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        JLabel lbl = new JLabel();
        Font optBtnFont = new Font(lbl.getFont().getName(), Font.PLAIN, OptBtnFontSize);

		JPanel sPanel = new JPanel();
		sPanel.setLayout(null);
		sPanel.setBorder(blackBorder);
		
		int cx = WinMargin/2;
		int cy = 0;

		String[] quantityTitles = DUnits.quantities();
		optButtonArrays = new ArrayOfJRadioButton[quantityTitles.length];
		
		for (int i=0; i<quantityTitles.length; i++) {
			JLabel clbl = new JLabel(quantityTitles[i]);
			clbl.setSize(OptBtnWidth+WinMargin/2, LblHeight);
			clbl.setLocation(cx, cy+(LineSpacing-LblHeight)/2);
			sPanel.add(clbl);
			
			cy += LineSpacing;
			
			ButtonGroup optButtonGroup = new ButtonGroup();
			String[] lNames = DUnits.quantityOptionsLongNames(i);
			JRadioButton[] optButtons = new JRadioButton[lNames.length];
			
			for (int j=0; j<lNames.length; j++) {
				optButtons[j] = new JRadioButton(lNames[j]);
				optButtons[j].setSize(OptBtnWidth, LblHeight);
				optButtons[j].setLocation(cx+WinMargin/2, cy + (EdtHeight-LblHeight)/2);
				optButtons[j].setFont(optBtnFont);
				
				sPanel.add(optButtons[j]);
				optButtonGroup.add(optButtons[j]);
				
				cy += EdtHeight;
			}
			
			optButtonArrays[i] = new ArrayOfJRadioButton(optButtons);
			cy += WinMargin/2;
		}
		
		int prefWidth = WinMargin/2 + OptBtnWidth + WinMargin;
		int prefHeight = cy;
		
		int blockWidth = prefWidth;
		int blockHeight = prefHeight;
		int maxSubPanelHeight = (int)(FracScreenMaxHeight*Toolkit.getDefaultToolkit().getScreenSize().height);

		if (blockHeight > maxSubPanelHeight) {
			blockWidth += ScrollBarWidthAllowance;
			blockHeight = maxSubPanelHeight;
			
			sPanel.setPreferredSize(new Dimension(prefWidth, prefHeight));
			sPanel.setAutoscrolls(true);
			
			JScrollPane sc2 = new JScrollPane(sPanel);
			sc2.setLocation(WinMargin, WinMargin);
			sc2.setSize(blockWidth, blockHeight);
			totalGUI.add(sc2);
		} else {
			sPanel.setLocation(WinMargin, WinMargin);
			sPanel.setSize(blockWidth, blockHeight);
			totalGUI.add(sPanel);
		}
		
        int panelWidth = blockWidth + WinMargin*2;
        int panelHeight = blockHeight + BtnHeight + WinMargin*3;
        
        cx = panelWidth/2;
        cy = WinMargin*2 + blockHeight;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BtnHeight);
        btnOK.setLocation(cx - BtnWidth - WinMargin/4, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BtnHeight);
        btnCancel.setLocation(cx + WinMargin/4, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	private void screenToUnits() {
		for (int i=0; i<optButtonArrays.length; i++) {
			DUnits.setUserUnitSelection(i, optButtonArrays[i].getSelectedID());
		}
	}
	private void unitsToScreen() {
		for (int i=0; i<optButtonArrays.length; i++) {
			optButtonArrays[i].optButtons[DUnits.getUserUnitSelection(i)].setSelected(true);
		}
	}
	private void saveUnits() {
		DUnits.writeToFile(fs, aID);
	}
	
	private class ArrayOfJRadioButton {
		private JRadioButton[] optButtons;
		private ArrayOfJRadioButton(JRadioButton[] oButtons) {
			optButtons = oButtons;
		}
		private int getSelectedID() {
			if (optButtons == null) return -1;
			for (int i=0; i<optButtons.length; i++) {
				if (optButtons[i].isSelected()) return i;
			}
			return -1;
		}
	}
}
