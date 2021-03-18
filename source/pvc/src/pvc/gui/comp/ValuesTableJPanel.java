package pvc.gui.comp;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class ValuesTableJPanel extends JPanel {
	//GUI Sizing constants
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int TClear = 2;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	
	private static final int ScrollBarAllawance = 20;

	private JTextField[][] edtValues;
	private int nRows, nCols;
	
	public int nRows() {return nRows;}
	
	public ValuesTableJPanel(String[] colTiltes, String[] rowTitles, float[][] initialValues, int[] numDecimals,
			int rowTitlesWidth, int[] colWidth, int panelMaxHeight) {
		super();
		setLayout(null);
		
		nCols = colTiltes.length;
		nRows = rowTitles.length;
		int subPanelHeight = nRows*EdtHeight + (nRows+1)*TClear;
		
		int pHeight = Math.min(panelMaxHeight, subPanelHeight+LineSpacing);
		int subPortionHeight = pHeight - LineSpacing;
		
		int pWidth = rowTitlesWidth + TMargin + TClear;
		for (int i=0; i<nCols; i++) pWidth += colWidth[i] + TClear;
		
		int cx = TMargin + rowTitlesWidth + TClear;
		int cy = (LineSpacing-LblHeight)/2;
		
		for (int i=0; i<nCols; i++) {
			JLabel lbl = new JLabel(colTiltes[i]);
			lbl.setSize(colWidth[i], LblHeight);
			lbl.setLocation(cx, cy);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			add(lbl);
			
			cx += colWidth[i] + TClear;
		}		
		
		cx = rowTitlesWidth + TMargin + TClear;
		cy = LineSpacing;
		if (subPortionHeight < subPanelHeight) {
			//Scroll bars will be active
			pWidth += ScrollBarAllawance;
			
			//Coordinates will be within sub-panel
			cx = rowTitlesWidth + TMargin + TClear/2;
			cy = TClear/2;
		}
		
		edtValues = new JTextField[nCols][nRows];	//First index on series (colmnID), second index on entry within series
		for (int i=0; i<nCols; i++) {
			for (int j=0; j<nRows; j++) {
				edtValues[i][j] = new JTextField(NumToString.floatWNumDecimals(initialValues[i][j], numDecimals[i]));
				edtValues[i][j].setSize(colWidth[i], EdtHeight);
				edtValues[i][j].setLocation(cx, cy + j*(EdtHeight+TClear));
				edtValues[i][j].setHorizontalAlignment(SwingConstants.RIGHT);
			}
			
			cx += colWidth[i] + TClear;
		}
		
		if (subPortionHeight < subPanelHeight) {
			//Putting components in sub-panel
			JPanel sp = new JPanel();
			sp.setLayout(null);
			sp.setPreferredSize(new Dimension(pWidth-ScrollBarAllawance-TClear, subPanelHeight));
			sp.setAutoscrolls(true);
			
			cy = TClear/2 + (EdtHeight-LblHeight)/2;
			cx = TMargin - TClear/2;
			
			for (int j=0; j<nRows; j++) {
				for (int i=0; i<nCols; i++) sp.add(edtValues[i][j]);
				
				JLabel lbl = new JLabel(rowTitles[j]);
				lbl.setSize(rowTitlesWidth, LblHeight);
				lbl.setLocation(cx, cy);
				sp.add(lbl);
				
				cy += EdtHeight + TClear;
			}
			
			JScrollPane sc1 = new JScrollPane(sp);
			sc1.setSize(pWidth - TClear, subPortionHeight);
			sc1.setLocation(TClear/2, LineSpacing);
			add(sc1);
		}
		else {
			//Putting components directly in panel
			cy = LineSpacing + (EdtHeight-LblHeight)/2;
			cx = TMargin;
			
			for (int j=0; j<nRows; j++) {
				for (int i=0; i<nCols; i++) add(edtValues[i][j]);
				
				JLabel lbl = new JLabel(rowTitles[j]);
				lbl.setSize(rowTitlesWidth, LblHeight);
				lbl.setLocation(cx, cy);
				add(lbl);
				
				cy += EdtHeight + TClear;
			}
		}
		//TODO		
		
		setSize(pWidth, pHeight);
		setOpaque(true);
	}

	
	public float[][] getTableValues() {
		float[][] arr = new float[nCols][nRows];
		try {
			for (int i=0; i<nCols; i++) {
				for (int j=0; j<nRows; j++) {
					arr[i][j] = Float.parseFloat(edtValues[i][j].getText());
				}				
			}
		} catch (Exception e) {
			return null;
		}
		
		return arr;
	}
}
