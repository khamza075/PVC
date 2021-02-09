package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import pvc.datamgmt.comp.CostBarsDisplaySetup;

@SuppressWarnings("serial")
public class CostBarsEDPanelMaker {
	//Prevent instantiation
	private CostBarsEDPanelMaker() {}

	//Sizing constants
	private static final int TClear = 2;
	private static final int TMargin = 4;
	private static final int LblHeight = 14;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	
	private static final int SubPanelsHeight = BtnHeight + TClear*2;
	private static final int SubPanelsLayerOffset = 24;

	private static final int FontSize = 11;
	private static final int CBChkBoxesWidth = 330;
	private static final int VehChkBoxesWidth = 280;
	private static final int CombChkBoxesWidth = 70;
	private static final int ColorLabelWidth = 36;
	private static final int ColorButtonlWidth = 90;
	
	public static CBEDPanel createCostBarsCheckBoxesPanel(CostBarsDisplaySetup displaySetup) {return new CBEDPanel(displaySetup);}
	public static CBVehPanel createVehiclesCheckBoxesPanel(CostBarsDisplaySetup displaySetup) {return new CBVehPanel(displaySetup);}
	
	public static class CBVehPanel extends JPanel implements ActionListener {
		private CostBarsDisplaySetup dSetup;
		private JCheckBox[] chkVeh;

		private CBVehPanel(CostBarsDisplaySetup displaySetup) {
			//Call super and initialize panel
			super();
			setLayout(null);
			setOpaque(true);
			
			//Links to data objects
			dSetup = displaySetup;

			//Create check boxes
			int numVeh = dSetup.numVehicles();
			chkVeh = new JCheckBox[numVeh];
			
			JLabel lbl = new JLabel();
	        Font fontB = new Font(lbl.getFont().getName(), Font.BOLD, FontSize);
			int cx = TMargin;
			int cy = TClear;
			
			for (int i=0; i<chkVeh.length; i++) {
				chkVeh[i] = new JCheckBox(dSetup.vehModelShortName(i));
				chkVeh[i].setSize(VehChkBoxesWidth, EdtHeight);
				chkVeh[i].setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
				chkVeh[i].setFont(fontB);
				chkVeh[i].setSelected(dSetup.isVehShown(i));
				chkVeh[i].addActionListener(this);
				
				add(chkVeh[i]);
				cy += LineSpacing;
			}
			
			setPreferredSize(new Dimension(VehChkBoxesWidth+TMargin*2, LineSpacing*numVeh + TClear*2));
		}
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			
			for (int i=0; i<chkVeh.length; i++) {
				if (source == chkVeh[i]) {
					dSetup.setVehShown(i, chkVeh[i].isSelected());
					return;
				}
			}
		}
	}
	
	public static class CBEDPanel extends JPanel {
		private CostBarsDisplaySetup dSetup;
		private CBarUnitPanel[] subPanels;
		
		private CBEDPanel(CostBarsDisplaySetup displaySetup) {
			//Call super and initialize panel
			super();
			setLayout(null);
			setOpaque(true);
			
			//Links to data objects
			dSetup = displaySetup;

			//Create sub-panels
			int numSubPanels = dSetup.numCheckBoxes();
			subPanels = new CBarUnitPanel[numSubPanels];
			
			int cx = TMargin;
			int cy = TMargin;
			int maxX = 0;
			
			for (int i=0; i<subPanels.length; i++) {
				CostBarsDisplaySetup.CBarUnitData uData = dSetup.getChkData(i);
				subPanels[i] = new CBarUnitPanel(uData, this);
				
				int layerLevel = uData.layerLevel();
				int x = cx + layerLevel * SubPanelsLayerOffset;
				subPanels[i].setLocation(x, cy);
				
				int cXRight = x + subPanels[i].getWidth();
				if (maxX < cXRight) maxX = cXRight;
				
				cy += SubPanelsHeight + TMargin;
				add(subPanels[i]);
			}
			
			setPreferredSize(new Dimension(maxX+TMargin, cy));
		}
		
		private void subPanelSelectedAction(CostBarsDisplaySetup.CostBarType chkType, boolean flag) {
			dSetup.setSelected(chkType, flag);
			for (int i=0; i<subPanels.length; i++) subPanels[i].dataToScreen();
		}
		private void subPanelCombineAction(CostBarsDisplaySetup.CostBarType chkType, boolean flag) {
			dSetup.setCombined(chkType, flag);
			for (int i=0; i<subPanels.length; i++) subPanels[i].dataToScreen();
		}
	}
	
	private static class CBarUnitPanel extends JPanel implements ActionListener {
		private CostBarsDisplaySetup.CBarUnitData uData;
		private CBEDPanel mPanel;
		
		private JCheckBox chkSelected, chkCombined;
		private ColorJLabel colorLabel;
		private JButton btnEditColor;
		
		private CBarUnitPanel(CostBarsDisplaySetup.CBarUnitData unitData, CBEDPanel masterPanel) {
			//Call super and initialize panel
			super();
			setLayout(null);
			setOpaque(true);
			
			//Links to data objects
			uData = unitData;
			mPanel = masterPanel;
			
			//Fonts
			JLabel lbl = new JLabel();
	        Font fontB = new Font(lbl.getFont().getName(), Font.BOLD, FontSize);
	        Font fontP = new Font(lbl.getFont().getName(), Font.PLAIN, FontSize);
	        
	        int cx = TClear;
	        int cy = TClear;
	        
	        chkSelected = new JCheckBox(uData.type().getCaptionWithoutUnits());
	        chkSelected.addActionListener(this);
	        chkSelected.setFont(fontB);
	        chkSelected.setSize(CBChkBoxesWidth, EdtHeight);
	        chkSelected.setLocation(cx, cy + (BtnHeight - EdtHeight)/2);
	        add(chkSelected);
	        
	        cx += CBChkBoxesWidth + TMargin;
	        
	        chkCombined = null;
	        if (uData.hasSubLinks()) {
	        	chkCombined = new JCheckBox("Combine");
	        	chkCombined.addActionListener(this);
	        	chkCombined.setFont(fontP);
	        	chkCombined.setSize(CombChkBoxesWidth, EdtHeight);
	        	chkCombined.setLocation(cx, cy + (BtnHeight - EdtHeight)/2);
		        add(chkCombined);
		        
		        cx += CombChkBoxesWidth + TMargin;
	        }

	        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
	        colorLabel = new ColorJLabel(uData.getColor());
	        colorLabel.setSize(ColorLabelWidth, EdtHeight);
        	colorLabel.setLocation(cx, cy + (BtnHeight - EdtHeight)/2);
        	colorLabel.setBorder(blackBorder);
	        add(colorLabel);
	        
	        cx += ColorLabelWidth + TClear;
	        
	        btnEditColor = new JButton("Edit Color...");
	        btnEditColor.addActionListener(this);
	        btnEditColor.setFont(fontP);
	        btnEditColor.setSize(ColorButtonlWidth, BtnHeight);
	        btnEditColor.setLocation(cx, cy);
	        add(btnEditColor);
	        
	        cx += ColorButtonlWidth + TClear;	        
	        setSize(cx, SubPanelsHeight);
	        
	        //Put data to screen 
	        dataToScreen();
		}
		
		private void dataToScreen() {
			chkSelected.setSelected(uData.isFullyShown());
			if (chkCombined!=null) chkCombined.setSelected(uData.isCombined());
			
			if (uData.parentHasCombined()) {
				chkSelected.setEnabled(false);
				if (chkCombined!=null) chkCombined.setEnabled(false);
				
				colorLabel.setVisible(false);
				btnEditColor.setVisible(false);
			} else {
				chkSelected.setEnabled(true);
				if (chkCombined!=null) chkCombined.setEnabled(true);

				if (uData.hasSubLinks()) {
					if (uData.isCombined()) {
						colorLabel.setVisible(true);
						btnEditColor.setVisible(true);
					} else {
						colorLabel.setVisible(false);
						btnEditColor.setVisible(false);
					}
				} else {
					colorLabel.setVisible(true);
					btnEditColor.setVisible(true);
				}
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			
			if (source == btnEditColor) {
				ColorEditorDialog dlg = new ColorEditorDialog(uData.colorRed(), uData.colorGreen(), uData.colorBlue());
				if (dlg.okPressed()) {
					uData.setColor(dlg.colorRed(), dlg.colorGreen(), dlg.colorBlue());
					colorLabel.setColor(new Color(uData.colorRed(), uData.colorGreen(), uData.colorBlue()));
				}
				return;
			}
			if (source == chkSelected) {
				boolean selection = chkSelected.isSelected();
				mPanel.subPanelSelectedAction(uData.type(), selection);
				return;
			}
			if (source == chkCombined) {
				boolean selection = chkCombined.isSelected();
				mPanel.subPanelCombineAction(uData.type(), selection);
				return;
			}
		}
	}

}
