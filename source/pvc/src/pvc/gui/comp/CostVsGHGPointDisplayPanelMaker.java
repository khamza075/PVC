package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.comp.CostVsGHGDisplaySetup;

@SuppressWarnings("serial")
public class CostVsGHGPointDisplayPanelMaker {
	private static final int ShapeSizeMin = 3;
	private static final int ShapeSizeMax = 24;
	private static final int ShapeLineWidthMin = 1;
	private static final int ShapeLineWidthMax = 10;
	private static final int TextFontSizeMin = 7;
	private static final int TextFontSizeMax = 24;
	private static final int TextOffsetMin = 0;
	private static final int TextOffsetMax = 18;

	private static final int FontSizeB12 = 12;
	private static final int FontSizeP11 = 11;

	private static final int TMargin = 4;
	private static final int TClear = 2;	
	private static final int LblHeightB12 = 16;	
	private static final int LblHeightP11 = 14;
	private static final int EdtHeightP11 = LblHeightP11 + TClear;
	private static final int BtnHeightP11 = EdtHeightP11 + TClear*2;
	private static final int LineSpacingP11 = BtnHeightP11 + TClear;
	private static final int DrawBoxWH = LineSpacingP11 * 5;

	private static final int SpnBtnWidth = 40;
	private static final int SpnLblWidth = 50;
	private static final int LblWidth = 60;
	private static final int OptShapeWidth = 85;	
	private static final int LeftSectionWidth = Math.max((SpnLblWidth+SpnBtnWidth+TClear)*2 + LblWidth + TMargin*2, OptShapeWidth*3 + TMargin*3);

	private static final int OptTextPosWidth = 105;
	private static final int ChkShowTextWidth = 80;
	private static final int EdtTextWidth = 180;
	private static final int ChkBoldWidth = 60;
	private static final int RightSectionWidth = Math.max(TMargin*5 + OptTextPosWidth*4, ChkShowTextWidth + EdtTextWidth + ChkBoldWidth + TMargin*2);

	private static final int SubPanelWidth = LeftSectionWidth + DrawBoxWH + RightSectionWidth + TMargin * 6;
	private static final int PanelWidth = SubPanelWidth + TClear*2;
	public static final int SubPanelHeight = DrawBoxWH + TMargin * 2;
	
	private CostVsGHGPointDisplayPanelMaker() {}
	
	public static CostVsGHGEditDisplayPanel createCostVsGHGEditDisplayPanel(AnalysisVehModelsSetup.AVehModelSetup[] vms, CostVsGHGDisplaySetup displaySetup) {
		return new CostVsGHGEditDisplayPanel(vms, displaySetup);
	}
	
	public static class CostVsGHGEditDisplayPanel extends JPanel {
		public CostVsGHGPointSubPanel[] subPanels;
		
		private CostVsGHGEditDisplayPanel(AnalysisVehModelsSetup.AVehModelSetup[] vms, CostVsGHGDisplaySetup displaySetup) {
			super();
			setLayout(null);
			setOpaque(true);

			int numVehModels = vms.length;
			subPanels = new CostVsGHGPointSubPanel[numVehModels];
			
			int cx = TClear;
			int cy = TClear;
			
			for (int i=0; i<numVehModels; i++) {
				CostVsGHGDisplaySetup.ParetoPointDisplayData curPPtDData = displaySetup.getCopyOfVehDrawData(i);
				subPanels[i] = new CostVsGHGPointSubPanel(vms[curPPtDData.vehID].shortName, curPPtDData);
				subPanels[i].setLocation(cx, cy);
				add(subPanels[i]);
				
				cy += SubPanelHeight + TClear;
			}
			
			setPreferredSize(new Dimension(PanelWidth, cy));
		}
	}
	
	public static class CostVsGHGPointSubPanel extends JPanel implements ActionListener, ChangeListener {
		//GUI
		private PPointGLabel gLabel;
		private JCheckBox chkShow, chkShowText, chkTextBold;
		private JRadioButton[] optButtonsSymbol, optTextPos;
		private JSpinner spnShapeSize, spnShapeLineWidth, spnTextFontSize, spnTextOffset;
		private JTextField edtText;
		private JButton btnEditColor;
		
		//Data
		private String shortName;
		private CostVsGHGDisplaySetup.ParetoPointDisplayData ptData;
		public CostVsGHGDisplaySetup.ParetoPointDisplayData ptData() {return ptData;}
		
		private CostVsGHGPointSubPanel(String shortVehName, CostVsGHGDisplaySetup.ParetoPointDisplayData ppointData) {
			super();
			setLayout(null);
			setSize(SubPanelWidth, SubPanelHeight);
			setOpaque(true);
			
			shortName = new String(shortVehName);
			ptData = new CostVsGHGDisplaySetup.ParetoPointDisplayData(ppointData);
			createContent();
		}
		private void createContent() {
	        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
	        
	        int cx = LeftSectionWidth + TMargin*3;
	        int cy = TMargin;
	        
	        gLabel = new PPointGLabel();
	        gLabel.setSize(DrawBoxWH, DrawBoxWH);
	        gLabel.setLocation(cx, cy);
	        gLabel.setBorder(blackBorder);
	        add(gLabel);
	        
	        cx = TMargin;
	        
	        JLabel lbl1 = new JLabel(shortName);
	        lbl1.setSize(LblWidth+SpnBtnWidth, LblHeightB12);
	        lbl1.setLocation(cx, cy);
	        add(lbl1);
	        
	        Font font12B = new Font(lbl1.getFont().getName(), Font.BOLD, FontSizeB12);
	        Font font11P = new Font(lbl1.getFont().getName(), Font.PLAIN, FontSizeP11);
	        Font font11B = new Font(lbl1.getFont().getName(), Font.BOLD, FontSizeP11);
	        
	        lbl1.setFont(font12B);
	        
	        chkShow = new JCheckBox("Hide in Plot");
	        chkShow.setSize(LblWidth+SpnBtnWidth, EdtHeightP11);
	        chkShow.setLocation(cx + LblWidth+SpnBtnWidth + TMargin, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        chkShow.setFont(font11P);
	        chkShow.setSelected(!ptData.showOnPlot);
	        chkShow.addActionListener(this);
	        add(chkShow);
	        
	        cy += LineSpacingP11;

	        JLabel lbl2 = new JLabel("Shape");
	        lbl2.setSize(LblWidth, LblHeightP11);
	        lbl2.setFont(font11B);
	        lbl2.setLocation(cx, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl2);
	        
	        cx = LblWidth+SpnBtnWidth + TMargin*2 - SpnLblWidth;
	        
	        JLabel lbl3 = new JLabel("Size");
	        lbl3.setSize(SpnLblWidth, LblHeightP11);
	        lbl3.setFont(font11P);
	        lbl3.setHorizontalAlignment(SwingConstants.RIGHT);
	        lbl3.setLocation(cx, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl3);
	        
	        SpinnerNumberModel spmShapeSize = new SpinnerNumberModel(ptData.shapeRadius, ShapeSizeMin, ShapeSizeMax, 1);
	        SpinnerNumberModel spmShapeLineWidth = new SpinnerNumberModel(ptData.shapeLineWidth, ShapeLineWidthMin, ShapeLineWidthMax, 1);

	        spnShapeSize = new JSpinner(spmShapeSize);
	        spnShapeSize.setSize(SpnBtnWidth, EdtHeightP11);
	        spnShapeSize.setFont(font11P);
	        spnShapeSize.setLocation(cx + SpnLblWidth + TMargin, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        spnShapeSize.setBorder(blackBorder);
	        spnShapeSize.addChangeListener(this);
	        add(spnShapeSize);
	        
	        int remSpace = LeftSectionWidth - (cx + SpnLblWidth + SpnBtnWidth + TMargin*2);
	        int nextLblWidth = remSpace - SpnBtnWidth - TMargin;
	        cx = TMargin + LeftSectionWidth - remSpace;

	        JLabel lbl4 = new JLabel("Line Width");
	        lbl4.setSize(nextLblWidth, LblHeightP11);
	        lbl4.setFont(font11P);
	        lbl4.setHorizontalAlignment(SwingConstants.RIGHT);
	        lbl4.setLocation(cx, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl4);

	        spnShapeLineWidth = new JSpinner(spmShapeLineWidth);
	        spnShapeLineWidth.setSize(SpnBtnWidth, EdtHeightP11);
	        spnShapeLineWidth.setFont(font11P);
	        spnShapeLineWidth.setLocation(cx + nextLblWidth + TMargin, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        spnShapeLineWidth.setBorder(blackBorder);
	        spnShapeLineWidth.addChangeListener(this);
	        add(spnShapeLineWidth);

	        cx = TMargin;
	        cy += LineSpacingP11;
	        
	        CostVsGHGDisplaySetup.ParetoPointDisplayShapeType[] allShapes = CostVsGHGDisplaySetup.ParetoPointDisplayShapeType.values();
	        optButtonsSymbol = new JRadioButton[allShapes.length];
			ButtonGroup obg1 = new ButtonGroup();
	        
	        for (int i=0; i<optButtonsSymbol.length; i++) {
	        	optButtonsSymbol[i] = new JRadioButton(allShapes[i].caption);	        	
	        	optButtonsSymbol[i].setSize(OptShapeWidth, EdtHeightP11);
	        	optButtonsSymbol[i].setFont(font11P);
	        	optButtonsSymbol[i].addActionListener(this);

	        	int x = cx + TMargin + (i % 3)*(OptShapeWidth + TMargin);
	        	int y = cy + (LineSpacingP11 - LblHeightP11)/2 + (i / 3)*LineSpacingP11;
	        	optButtonsSymbol[i].setLocation(x, y);
	        	
	        	obg1.add(optButtonsSymbol[i]);
	        	add(optButtonsSymbol[i]);
	        }
	        
	        optButtonsSymbol[ptData.shapeType.ordinal()].setSelected(true);
	        			
	        cy += LineSpacingP11*2;
	        
	        btnEditColor = new JButton("Edit Color...");
	        btnEditColor.setSize(LblWidth+SpnBtnWidth, BtnHeightP11);
	        btnEditColor.setLocation(cx + LeftSectionWidth - (LblWidth+SpnBtnWidth), cy + (LineSpacingP11-BtnHeightP11));
	        btnEditColor.setFont(font11P);
	        btnEditColor.addActionListener(this);
	        add(btnEditColor);
	        
	        cx = LeftSectionWidth + DrawBoxWH + TMargin*4;
	        cy = TMargin;
	        
	        chkShowText = new JCheckBox("Show Text");
	        chkShowText.setSize(ChkShowTextWidth, EdtHeightP11);
	        chkShowText.setLocation(cx, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        chkShowText.setFont(font11P);
	        chkShowText.setSelected(ptData.showNameOnPlot);
	        chkShowText.addActionListener(this);
	        add(chkShowText);
	        
	        chkTextBold = new JCheckBox("Bold");
	        chkTextBold.setSize(ChkBoldWidth, EdtHeightP11);
	        chkTextBold.setLocation(cx + RightSectionWidth - ChkBoldWidth, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        chkTextBold.setFont(font11P);
	        chkTextBold.setSelected(ptData.boldText);
	        chkTextBold.addActionListener(this);
	        add(chkTextBold);

	        edtText = new JTextField(ptData.nameTextToDisplay);
	        edtText.setSize(RightSectionWidth - ChkBoldWidth - ChkShowTextWidth - TMargin*2, EdtHeightP11);
	        edtText.setLocation(cx + ChkShowTextWidth + TMargin, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        edtText.setFont(font11P);
	        edtText.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
				@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
				@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
				private void processChange() {
					String nText = new String(edtText.getText());
					if (nText.length() > 0) {
						ptData.nameTextToDisplay = nText;				
						gLabel.repaint();
					}
				}
			});
	        add(edtText);
	        
	        cy += LineSpacingP11;

	        SpinnerNumberModel spmTextFontSize = new SpinnerNumberModel(ptData.nameFontSize, TextFontSizeMin, TextFontSizeMax, 1);
	        SpinnerNumberModel spmTextOffset = new SpinnerNumberModel(ptData.nameRadiusOffset, TextOffsetMin, TextOffsetMax, 1);

	        JLabel lbl5 = new JLabel("Font Size");
	        lbl5.setSize(LblWidth, LblHeightP11);
	        lbl5.setFont(font11P);
	        lbl5.setHorizontalAlignment(SwingConstants.RIGHT);
	        lbl5.setLocation(cx + RightSectionWidth - LblWidth - SpnBtnWidth - TMargin, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl5);

	        spnTextFontSize = new JSpinner(spmTextFontSize);
	        spnTextFontSize.setSize(SpnBtnWidth, EdtHeightP11);
	        spnTextFontSize.setFont(font11P);
	        spnTextFontSize.setLocation(cx + RightSectionWidth - SpnBtnWidth, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        spnTextFontSize.setBorder(blackBorder);
	        spnTextFontSize.addChangeListener(this);
	        add(spnTextFontSize);

	        JLabel lbl6 = new JLabel("Position");
	        lbl6.setSize(LblWidth, LblHeightP11);
	        lbl6.setFont(font11B);
	        lbl6.setLocation(cx, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl6);

	        JLabel lbl7 = new JLabel("Offset");
	        lbl7.setSize(LblWidth, LblHeightP11);
	        lbl7.setFont(font11P);
	        lbl7.setHorizontalAlignment(SwingConstants.RIGHT);
	        lbl7.setLocation(cx + LblWidth + TMargin, cy + (LineSpacingP11 - LblHeightP11)/2);
	        add(lbl7);

	        spnTextOffset = new JSpinner(spmTextOffset);
	        spnTextOffset.setSize(SpnBtnWidth, EdtHeightP11);
	        spnTextOffset.setFont(font11P);
	        spnTextOffset.setLocation(cx + LblWidth*2 + TMargin*2, cy + (LineSpacingP11 - EdtHeightP11)/2);
	        spnTextOffset.setBorder(blackBorder);
	        spnTextOffset.addChangeListener(this);
	        add(spnTextOffset);

	        cy += LineSpacingP11;
	        
	        CostVsGHGDisplaySetup.ParetoPointNameTextPosition[] allPos = CostVsGHGDisplaySetup.ParetoPointNameTextPosition.values();
	        optTextPos = new JRadioButton[allPos.length];
			ButtonGroup obg2 = new ButtonGroup();
	        
	        for (int i=0; i<optTextPos.length; i++) {
	        	optTextPos[i] = new JRadioButton(allPos[i].caption);	        	
	        	optTextPos[i].setSize(OptTextPosWidth, EdtHeightP11);
	        	optTextPos[i].setFont(font11P);
	        	optTextPos[i].addActionListener(this);

	        	int x = cx + TMargin + (i / 3)*(OptTextPosWidth + TMargin);
	        	int y = cy + (LineSpacingP11 - LblHeightP11)/2 + (i % 3)*LineSpacingP11;
	        	optTextPos[i].setLocation(x, y);
	        	
	        	obg2.add(optTextPos[i]);
	        	add(optTextPos[i]);
	        }
	        
	        optTextPos[ptData.textPosition.ordinal()].setSelected(true);
	        
			boolean isSelected = chkShow.isSelected();
			if (isSelected) {
				disableComp();
			} else {
				enableComp();
			}
			gLabel.repaint();
		}
		private void disableComp() {
			spnShapeSize.setEnabled(false);
			spnShapeLineWidth.setEnabled(false);
			for (int i=0; i<optButtonsSymbol.length; i++) optButtonsSymbol[i].setEnabled(false);
			
			chkShowText.setEnabled(false);
			chkTextBold.setEnabled(false);
			edtText.setEnabled(false);
			spnTextFontSize.setEnabled(false);
			spnTextOffset.setEnabled(false);
			for (int i=0; i<optTextPos.length; i++) optTextPos[i].setEnabled(false);
			
			btnEditColor.setEnabled(false);
		}
		private void enableComp() {
			spnShapeSize.setEnabled(true);
			spnShapeLineWidth.setEnabled(true);
			for (int i=0; i<optButtonsSymbol.length; i++) optButtonsSymbol[i].setEnabled(true);
			
			chkShowText.setEnabled(true);
			chkTextBold.setEnabled(true);
			edtText.setEnabled(true);
			spnTextFontSize.setEnabled(true);
			spnTextOffset.setEnabled(true);
			for (int i=0; i<optTextPos.length; i++) optTextPos[i].setEnabled(true);
			
			btnEditColor.setEnabled(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			
			if (source == chkShow) {
				boolean isSelected = chkShow.isSelected();
				if (isSelected) {
					disableComp();
				} else {
					enableComp();
				}
				ptData.showOnPlot = !isSelected;				
				gLabel.repaint();
				return;
			}
			for (int i=0; i<optButtonsSymbol.length; i++) {
				if (source == optButtonsSymbol[i]) {
					ptData.shapeType = CostVsGHGDisplaySetup.ParetoPointDisplayShapeType.values()[i];
					gLabel.repaint();
					return;
				}
			}
			if (source == chkShowText) {
				ptData.showNameOnPlot = chkShowText.isSelected();
				gLabel.repaint();
				return;
			}
			if (source == chkTextBold) {
				ptData.boldText = chkTextBold.isSelected();
				gLabel.repaint();
				return;
			}
			for (int i=0; i<optTextPos.length; i++) {
				if (source == optTextPos[i]) {
					ptData.textPosition = CostVsGHGDisplaySetup.ParetoPointNameTextPosition.values()[i];
					gLabel.repaint();
					return;
				}
			}
			if (source == btnEditColor) {
				ColorEditorDialog dlg = new ColorEditorDialog(ptData.shapeColorRed, ptData.shapeColorGreen, ptData.shapeColorBlue);
				if (dlg.okPressed()) {
					ptData.shapeColorRed = dlg.colorRed();
					ptData.shapeColorGreen = dlg.colorGreen();
					ptData.shapeColorBlue = dlg.colorBlue();
					gLabel.repaint();
					return;
				}
			}
		}
		@Override 
		public void stateChanged(ChangeEvent event) {
			Object source = event.getSource();

			if (source == spnShapeSize) {
				ptData.shapeRadius = Integer.parseInt(spnShapeSize.getValue().toString());
				gLabel.repaint();
				return;
			}
			if (source == spnShapeLineWidth) {
				ptData.shapeLineWidth = Integer.parseInt(spnShapeLineWidth.getValue().toString());
				gLabel.repaint();
				return;
			}
			if (source == spnTextFontSize) {
				ptData.nameFontSize = Integer.parseInt(spnTextFontSize.getValue().toString());
				gLabel.repaint();
				return;
			}
			if (source == spnTextOffset) {
				ptData.nameRadiusOffset = Integer.parseInt(spnTextOffset.getValue().toString());
				gLabel.repaint();
				return;
			}
		}
		
		private class PPointGLabel extends JLabel {
			private PPointGLabel() {super();}
			@Override
		    protected void paintComponent(Graphics g) {
				//Call base function
				super.paintComponent(g);
				
				//Cast and Draw
				Graphics2D g2 = (Graphics2D) g;
				CostVsGHGDisplaySetup.ParetoPointDisplayGraphics ppGaphics = new CostVsGHGDisplaySetup.ParetoPointDisplayGraphics(ptData);
				ppGaphics.drawInGraphics(g2, DrawBoxWH/2, DrawBoxWH/2);
			}
		}
	}
	
}
