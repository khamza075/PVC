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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import pvc.datamgmt.AnalysisVehModelsSetup;
import pvc.datamgmt.comp.GHGDisplaySetup;

@SuppressWarnings("serial")
public class GHGVehDisplayPanelMaker {
	private static final int CurvesLineWidthMin = 1;
	private static final int CurvesLineWidthMax = 5;
	
	//Sizing
	private static final int TClear = 2;
	private static final int TMargin = 4;
	private static final int WinMargin = 10;

	private static final int TitleLblHeight = 16;
	private static final int LblHeight = 14;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	
	private static final int TitleLblWidth = 120;
	private static final int ChkWidth = 120;
	
	private static final int SpnWidth = 40;
	private static final int SpnLableWidth = 110;
	private static final int ColorCaptionWidth = 100;
	private static final int ColorLblWidth = 40;
	private static final int EditColorBtnWidth = 90;

	
	private static final int FontSizeSmall = 11;

	
	public static int preferredPanelHeight(GHGDisplaySetup displaySetup) {
		int numVehicles = displaySetup.numVehicles();
		int unitH = LineSpacing*4;
		return TMargin*2 + unitH*numVehicles + (numVehicles-1)*WinMargin;
	}
	public static VehDisplayOptionsPanel createPanel(AnalysisVehModelsSetup.AVehModelSetup[] vms, GHGDisplaySetup displaySetup, int panelWidth) {
		return new VehDisplayOptionsPanel(vms, displaySetup, panelWidth);
	}
	

	
	public static class VehDisplayOptionsPanel extends JPanel implements ActionListener {
		public int[] pdmRed, pdmGreen, pdmBlue, cdfRed, cdfGreen, cdfBlue, bxpRed, bxpGreen, bxpBlue;
		public JTextField[] edtTitles;
		public JCheckBox[] chkShowVeh;
		public JSpinner[] spnPDM, spnCDF;
		
		private JButton[] btnPDM, btnCDF, btnBxp;
		private ColorJLabel[] clblPDM, clblCDF, clblBxp;

		private VehDisplayOptionsPanel(AnalysisVehModelsSetup.AVehModelSetup[] vms, GHGDisplaySetup displaySetup, int panelWidth) {
			super();
			setLayout(null);
			setOpaque(true);
			setPreferredSize(new Dimension(panelWidth, preferredPanelHeight(displaySetup)));
						
			//Setup arrays
			int numVehicles = displaySetup.numVehicles();

			pdmRed = new int[numVehicles];
			pdmGreen = new int[numVehicles];
			pdmBlue = new int[numVehicles];

			cdfRed = new int[numVehicles];
			cdfGreen = new int[numVehicles];
			cdfBlue = new int[numVehicles];

			bxpRed = new int[numVehicles];
			bxpGreen = new int[numVehicles];
			bxpBlue = new int[numVehicles];
			
			edtTitles = new JTextField[numVehicles];
			chkShowVeh = new JCheckBox[numVehicles];
			
			spnPDM = new JSpinner[numVehicles];
			spnCDF = new JSpinner[numVehicles];
			
			btnPDM = new JButton[numVehicles];
			btnCDF = new JButton[numVehicles];
			btnBxp = new JButton[numVehicles];

			clblPDM = new ColorJLabel[numVehicles];
			clblCDF = new ColorJLabel[numVehicles];
			clblBxp = new ColorJLabel[numVehicles];

			//Prepare smaller sized font
			JLabel lbl = new JLabel();
	        Font smallerFont = new Font(lbl.getFont().getName(), Font.PLAIN, FontSizeSmall);
	       
	        //Create the GUI objects and set values
	        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
	        int edtWidth = panelWidth - (TMargin*2 + TClear*2 + TitleLblWidth + ChkWidth);
	        
	        int cx = TMargin;
	        int cy = TMargin;
	        
	        for (int i=0; i<numVehicles; i++) {
	        	GHGDisplaySetup.VehGHGDisplayOpions cvOptions = displaySetup.getVehAtDisplayPos(i);
	        	
				JLabel lblT = new JLabel(vms[cvOptions.vehID()].shortName);
				lblT.setSize(TitleLblWidth, TitleLblHeight);
				lblT.setLocation(cx, cy + (LineSpacing - TitleLblHeight)/2);
				add(lblT);
				
				chkShowVeh[i] = new JCheckBox("Show Plot, w/ Title");
				chkShowVeh[i].setSize(ChkWidth, EdtHeight);
				chkShowVeh[i].setLocation(cx + TClear + TitleLblWidth, cy + (LineSpacing - EdtHeight)/2);
				chkShowVeh[i].setFont(smallerFont);
				chkShowVeh[i].setSelected(cvOptions.isShown);
				add(chkShowVeh[i]);

				edtTitles[i] = new JTextField(cvOptions.displayedTitle);
				edtTitles[i].setSize(edtWidth, EdtHeight);
				edtTitles[i].setLocation(cx + TClear*2 + TitleLblWidth + ChkWidth, cy + (LineSpacing - EdtHeight)/2);
				edtTitles[i].setFont(smallerFont);
				add(edtTitles[i]);
				
				cy += LineSpacing;
				
				JLabel lbl1 = new JLabel("PDM Curve Color");
				lbl1.setSize(ColorCaptionWidth, LblHeight);
				lbl1.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
				lbl1.setFont(smallerFont);
				add(lbl1);

				pdmRed[i] = cvOptions.pdmRed;
				pdmGreen[i] = cvOptions.pdmGreen;
				pdmBlue[i] = cvOptions.pdmBlue;
				
				clblPDM[i] = new ColorJLabel(new Color(pdmRed[i], pdmGreen[i], pdmBlue[i]));
				clblPDM[i].setSize(ColorLblWidth, EdtHeight);
				clblPDM[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
				clblPDM[i].setBorder(blackBorder);
				add(clblPDM[i]);
				
				btnPDM[i] = new JButton("Edit Color...");
				btnPDM[i].setSize(EditColorBtnWidth, BtnHeight);
				btnPDM[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear + ColorLblWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
				btnPDM[i].setFont(smallerFont);
				btnPDM[i].addActionListener(this);
				add(btnPDM[i]);

		        SpinnerNumberModel spmPDM = new SpinnerNumberModel(cvOptions.pdmLineWidth, CurvesLineWidthMin, CurvesLineWidthMax, 1);
		        spnPDM[i] = new JSpinner(spmPDM);
		        spnPDM[i].setSize(SpnWidth, EdtHeight);
		        spnPDM[i].setLocation(panelWidth - (TMargin + SpnWidth), cy + (LineSpacing-EdtHeight)/2);
		        spnPDM[i].setBorder(blackBorder);
		        spnPDM[i].setFont(smallerFont);
				add(spnPDM[i]);
				
				JLabel lbl2 = new JLabel("PDM Curve Line Width");
				lbl2.setSize(SpnLableWidth, LblHeight);
				lbl2.setLocation(panelWidth - (TMargin + SpnWidth + TClear + SpnLableWidth), cy + (LineSpacing - LblHeight)/2);
				lbl2.setFont(smallerFont);
				add(lbl2);
				
				cy += LineSpacing;
				
				JLabel lbl3 = new JLabel("CDF Curve Color");
				lbl3.setSize(ColorCaptionWidth, LblHeight);
				lbl3.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
				lbl3.setFont(smallerFont);
				add(lbl3);

				cdfRed[i] = cvOptions.cdfRed;
				cdfGreen[i] = cvOptions.cdfGreen;
				cdfBlue[i] = cvOptions.cdfBlue;
				
				clblCDF[i] = new ColorJLabel(new Color(cdfRed[i], cdfGreen[i], cdfBlue[i]));
				clblCDF[i].setSize(ColorLblWidth, EdtHeight);
				clblCDF[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
				clblCDF[i].setBorder(blackBorder);
				add(clblCDF[i]);
				
				btnCDF[i] = new JButton("Edit Color...");
				btnCDF[i].setSize(EditColorBtnWidth, BtnHeight);
				btnCDF[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear + ColorLblWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
				btnCDF[i].setFont(smallerFont);
				btnCDF[i].addActionListener(this);
				add(btnCDF[i]);

		        SpinnerNumberModel spmCDF = new SpinnerNumberModel(cvOptions.cdfLineWidth, CurvesLineWidthMin, CurvesLineWidthMax, 1);
		        spnCDF[i] = new JSpinner(spmCDF);
		        spnCDF[i].setSize(SpnWidth, EdtHeight);
		        spnCDF[i].setLocation(panelWidth - (TMargin + SpnWidth), cy + (LineSpacing-EdtHeight)/2);
		        spnCDF[i].setBorder(blackBorder);
		        spnCDF[i].setFont(smallerFont);
				add(spnCDF[i]);
				
				JLabel lbl4 = new JLabel("CDF Curve Line Width");
				lbl4.setSize(SpnLableWidth, LblHeight);
				lbl4.setLocation(panelWidth - (TMargin + SpnWidth + TClear + SpnLableWidth), cy + (LineSpacing - LblHeight)/2);
				lbl4.setFont(smallerFont);
				add(lbl4);
				
				cy += LineSpacing;
				
				JLabel lbl5 = new JLabel("Box Plot Color");
				lbl5.setSize(ColorCaptionWidth, LblHeight);
				lbl5.setLocation(cx + WinMargin, cy + (LineSpacing - LblHeight)/2);
				lbl5.setFont(smallerFont);
				add(lbl5);

				bxpRed[i] = cvOptions.bxfRed;
				bxpGreen[i] = cvOptions.bxfGreen;
				bxpBlue[i] = cvOptions.bxfBlue;
				
				clblBxp[i] = new ColorJLabel(new Color(bxpRed[i], bxpGreen[i], bxpBlue[i]));
				clblBxp[i].setSize(ColorLblWidth, EdtHeight);
				clblBxp[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear, cy + (LineSpacing - EdtHeight)/2);
				clblBxp[i].setBorder(blackBorder);
				add(clblBxp[i]);
				
				btnBxp[i] = new JButton("Edit Color...");
				btnBxp[i].setSize(EditColorBtnWidth, BtnHeight);
				btnBxp[i].setLocation(cx + WinMargin + ColorCaptionWidth + TClear + ColorLblWidth + TMargin, cy + (LineSpacing - BtnHeight)/2);
				btnBxp[i].setFont(smallerFont);
				btnBxp[i].addActionListener(this);
				add(btnBxp[i]);
				
				cy += LineSpacing + WinMargin;				
	        }
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			
			for (int i=0; i<btnPDM.length; i++) {
				if (source == btnPDM[i]) {
					ColorEditorDialog dlg = new ColorEditorDialog(pdmRed[i], pdmGreen[i], pdmBlue[i]);	
					
					if (dlg.okPressed()) {
						pdmRed[i] = dlg.colorRed();
						pdmGreen[i] = dlg.colorGreen();
						pdmBlue[i] = dlg.colorBlue();
						
						clblPDM[i].setColor(new Color(pdmRed[i], pdmGreen[i], pdmBlue[i]));
					}
					return;
				}
			}
			for (int i=0; i<btnPDM.length; i++) {
				if (source == btnCDF[i]) {
					ColorEditorDialog dlg = new ColorEditorDialog(cdfRed[i], cdfGreen[i], cdfBlue[i]);	
					
					if (dlg.okPressed()) {
						cdfRed[i] = dlg.colorRed();
						cdfGreen[i] = dlg.colorGreen();
						cdfBlue[i] = dlg.colorBlue();
						
						clblCDF[i].setColor(new Color(cdfRed[i], cdfGreen[i], cdfBlue[i]));
					}
					return;
				}
			}
			for (int i=0; i<btnPDM.length; i++) {
				if (source == btnBxp[i]) {
					ColorEditorDialog dlg = new ColorEditorDialog(bxpRed[i], bxpGreen[i], bxpBlue[i]);	
					
					if (dlg.okPressed()) {
						bxpRed[i] = dlg.colorRed();
						bxpGreen[i] = dlg.colorGreen();
						bxpBlue[i] = dlg.colorBlue();
						
						clblBxp[i].setColor(new Color(bxpRed[i], bxpGreen[i], bxpBlue[i]));
					}
					return;
				}
			}			
		}
	}
	
}
