package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.datamgmt.*;
import pvc.datamgmt.comp.*;

public class EditSBarsPanelMaker {
	private EditSBarsPanelMaker() {}
	
	@SuppressWarnings("serial")
	public static class SBarsJPanel extends JPanel {
		public SBarDisplayGUICollection[] visibleSBars;
		
		private SBarsJPanel() {
			super();
			setLayout(null);
			setOpaque(true);
		}
		
		public void barsToScreen(SliderBarsManager sbarMan) {
			String[] chkCaptions = sbarMan.unitsFormattedCaptions();

			for (int i=0; i<visibleSBars.length; i++) {
				boolean userShow = sbarMan.getSBarUserShow(i);
				SliderBarSetup sbar = sbarMan.getSBar(i);
				
				String sBaselineValue = sbarMan.unitsFormattedValue(i, sbar.baseValue());
				visibleSBars[i].lblBaselineValue.setText(sBaselineValue);
				
				if (!sbar.isEditable()) {
					visibleSBars[i].chkCaption.setSelected(false);
					visibleSBars[i].chkCaption.setEnabled(false);
					
					visibleSBars[i].btnEdit.setEnabled(false);

					visibleSBars[i].lblMinValue.setText("");
					visibleSBars[i].lblCurLowLimit.setText("");
					visibleSBars[i].lblCurHighLimit.setText("");
					visibleSBars[i].lblMaxValue.setText("");
				} else {
					visibleSBars[i].btnEdit.setEnabled(true);

					if (!sbar.isSlidable()) {
						visibleSBars[i].chkCaption.setSelected(false);
						visibleSBars[i].chkCaption.setEnabled(false);
					} else {
						visibleSBars[i].chkCaption.setSelected(userShow);
						visibleSBars[i].chkCaption.setEnabled(true);
					}
					
					if (sbar.canDecrease()) {
						visibleSBars[i].lblMinValue.setText(sbarMan.unitsFormattedValue(i, sbar.minLimit()));
						visibleSBars[i].lblCurLowLimit.setText(sbarMan.unitsFormattedValue(i, sbar.getDValue(0)));
					} else {
						visibleSBars[i].lblMinValue.setText("");
						visibleSBars[i].lblCurLowLimit.setText("");
					}
					
					if (sbar.canIncrease()) {
						visibleSBars[i].lblMaxValue.setText(sbarMan.unitsFormattedValue(i, sbar.maxLimit()));
						visibleSBars[i].lblCurHighLimit.setText(sbarMan.unitsFormattedValue(i, sbar.getDValue(sbar.numDiscreteLevels()-1)));
					} else {
						visibleSBars[i].lblMaxValue.setText("");
						visibleSBars[i].lblCurHighLimit.setText("");
					}
				}
				
				if (!visibleSBars[i].chkCaption.isSelected()) {
					visibleSBars[i].lblMinValue.setText("");
					visibleSBars[i].lblCurLowLimit.setText("");
					visibleSBars[i].lblCurHighLimit.setText("");
					visibleSBars[i].lblMaxValue.setText("");
				}
				
				visibleSBars[i].chkCaption.setText(chkCaptions[i]);
			}
		}
	}
	public static class SBarDisplayGUICollection {
		public JCheckBox chkCaption;
		public JLabel lblMinValue, lblCurLowLimit, lblBaselineValue, lblCurHighLimit, lblMaxValue;
		public JButton btnEdit;
		
		private SBarDisplayGUICollection(String caption) {
			chkCaption = new JCheckBox(caption);
			
			lblMinValue = new JLabel();
			lblCurLowLimit = new JLabel();
			lblBaselineValue = new JLabel();
			lblCurHighLimit = new JLabel();
			lblMaxValue = new JLabel();
			
			btnEdit = new JButton("Edit...");
		}
	}


	public static SBarsJPanel createSBarsPanel(SliderBarsManager sbarMan, ActionListener listener, 
			int tMargin, int edtHeight, int btnHeight, int lineSpacing, int chkWidth, int edtWidth, int btnWidth, int valuesFontSize) {
		        
		String[] chkCaptions = sbarMan.unitsFormattedCaptions();

		JLabel lbl = new JLabel();
        Font valuesFont = new Font(lbl.getFont().getName(), Font.PLAIN, valuesFontSize);
		Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
		SBarsJPanel panel = new SBarsJPanel();
		
		panel.setBorder(blackBorder);
		panel.setAutoscrolls(true);
		panel.setPreferredSize(new Dimension(chkWidth+5*edtWidth+btnWidth+tMargin*8, lineSpacing*chkCaptions.length + tMargin*2));

		panel.visibleSBars = new SBarDisplayGUICollection[chkCaptions.length];
		
		int cy = tMargin;
		
		for (int i=0; i<chkCaptions.length; i++) {
			panel.visibleSBars[i] = new SBarDisplayGUICollection(chkCaptions[i]);
			
			int cx = tMargin;

			panel.visibleSBars[i].chkCaption.setSize(chkWidth, edtHeight);
			panel.visibleSBars[i].chkCaption.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].chkCaption.addActionListener(listener);
			panel.add(panel.visibleSBars[i].chkCaption);
			
			cx += chkWidth + tMargin;
			
			panel.visibleSBars[i].lblMinValue.setSize(edtWidth, edtHeight);
			panel.visibleSBars[i].lblMinValue.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].lblMinValue.setBorder(blackBorder);
			panel.visibleSBars[i].lblMinValue.setFont(valuesFont);
			panel.visibleSBars[i].lblMinValue.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.visibleSBars[i].lblMinValue);
			
			cx += edtWidth + tMargin;
			
			panel.visibleSBars[i].lblCurLowLimit.setSize(edtWidth, edtHeight);
			panel.visibleSBars[i].lblCurLowLimit.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].lblCurLowLimit.setBorder(blackBorder);
			panel.visibleSBars[i].lblCurLowLimit.setFont(valuesFont);
			panel.visibleSBars[i].lblCurLowLimit.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.visibleSBars[i].lblCurLowLimit);
			
			cx += edtWidth + tMargin;
			
			panel.visibleSBars[i].lblBaselineValue.setSize(edtWidth, edtHeight);
			panel.visibleSBars[i].lblBaselineValue.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].lblBaselineValue.setBorder(blackBorder);
			panel.visibleSBars[i].lblBaselineValue.setFont(valuesFont);
			panel.visibleSBars[i].lblBaselineValue.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.visibleSBars[i].lblBaselineValue);
			
			cx += edtWidth + tMargin;
			
			panel.visibleSBars[i].lblCurHighLimit.setSize(edtWidth, edtHeight);
			panel.visibleSBars[i].lblCurHighLimit.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].lblCurHighLimit.setBorder(blackBorder);
			panel.visibleSBars[i].lblCurHighLimit.setFont(valuesFont);
			panel.visibleSBars[i].lblCurHighLimit.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.visibleSBars[i].lblCurHighLimit);
			
			cx += edtWidth + tMargin;
			
			panel.visibleSBars[i].lblMaxValue.setSize(edtWidth, edtHeight);
			panel.visibleSBars[i].lblMaxValue.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.visibleSBars[i].lblMaxValue.setBorder(blackBorder);
			panel.visibleSBars[i].lblMaxValue.setFont(valuesFont);
			panel.visibleSBars[i].lblMaxValue.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.visibleSBars[i].lblMaxValue);
			
			cx += edtWidth + tMargin;
			
			panel.visibleSBars[i].btnEdit.setSize(btnWidth, btnHeight);
			panel.visibleSBars[i].btnEdit.setLocation(cx, cy + (lineSpacing - btnHeight)/2);
			panel.visibleSBars[i].btnEdit.addActionListener(listener);
			panel.add(panel.visibleSBars[i].btnEdit);
			
			cy += lineSpacing;
		}
		
		return panel;
	}
}
