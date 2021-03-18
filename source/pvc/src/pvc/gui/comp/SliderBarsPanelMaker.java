package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import pvc.datamgmt.SliderBarsManager;
import pvc.datamgmt.comp.SliderBarSetup;

public class SliderBarsPanelMaker {
	private static final int ContModeMaxValue = 500;
	private SliderBarsPanelMaker() {}
	
	
	@SuppressWarnings("serial")
	public static class CDSliderBarsJPanel extends JPanel {
		private boolean continuousMode;
		private SBarCollection[] sbc;
		
		private int[] oIDs;
		public int oID(int vsID) {return oIDs[vsID];}
		
		private CDSliderBarsJPanel(int pWidth ,int pHeight) {
			super();
			
			setLayout(null);
			setOpaque(true);
			setPreferredSize(new Dimension(pWidth,pHeight));
			setAutoscrolls(true);
		}
		
		public void grabSliderBarValues(SliderBarsManager sbarMan) {
			if (continuousMode) {
				for (int i=0; i<sbc.length; i++) {
					float sbarValue = (((float)sbc[i].cBar.getValue())/((float)ContModeMaxValue))*sbc[i].maxDiscreteID;
					sbarMan.rvStatus().setAVSBValue(oIDs[i], sbarValue);
				}
			} else {
				for (int i=0; i<sbc.length; i++) {
					float sbarValue = sbc[i].dBar.getValue();
					sbarMan.rvStatus().setAVSBValue(oIDs[i], sbarValue);
				}
			}
		}
		
		public void updateCaptions(SliderBarsManager sbarMan) {
			for (int i=0; i<sbc.length; i++) {
				sbc[i].lblCaption.setText(sbarMan.unitsFormattedCaption(oIDs[i]));
			}
		}
		public void updateValueLabels(SliderBarsManager sbarMan) {
			
			if (continuousMode) {
				for (int i=0; i<sbc.length; i++) {
					int barValue = sbc[i].cBar.getValue();
					float cValue = ((float)barValue*sbc[i].maxDiscreteID)/((float)ContModeMaxValue);
					
					float sbarValue = sbarMan.getSBar(oIDs[i]).getCValue(cValue);
					sbc[i].lblValue.setText(sbarMan.unitsFormattedValue(oIDs[i], sbarValue));
				}
			} else {
				for (int i=0; i<sbc.length; i++) {
					int barValue = sbc[i].dBar.getValue();

					float sbarValue = sbarMan.getSBar(oIDs[i]).getDValue(barValue);
					sbc[i].lblValue.setText(sbarMan.unitsFormattedValue(oIDs[i], sbarValue));
				}
			}
		}
		public void setContinuousMode(boolean contMode) {
			continuousMode = contMode;
			if (sbc == null) return;
			if (sbc.length < 1) return;
			
			sbc[0].ignoreChangeEvents = true;
			
			if (continuousMode) {
				for (int i=0; i<sbc.length; i++) {
					sbc[i].dBar.setVisible(false);
					sbc[i].cBar.setVisible(true);
					
					int cValue = (int)((((float)sbc[i].dBar.getValue())/((float)sbc[i].maxDiscreteID))*ContModeMaxValue + 0.5f);
					if (cValue < 0) cValue = 0;
					if (cValue > ContModeMaxValue) cValue = ContModeMaxValue;
					sbc[i].cBar.setValue(cValue);
				}
			} else {
				for (int i=0; i<sbc.length; i++) {
					sbc[i].dBar.setVisible(true);
					sbc[i].cBar.setVisible(false);
					
					int dValue = (int)(((float)sbc[i].cBar.getValue())/((float)ContModeMaxValue)*sbc[i].maxDiscreteID + 0.5f);
					if (dValue < 0) dValue = 0;
					if (dValue > sbc[i].maxDiscreteID) dValue = sbc[i].maxDiscreteID;
					sbc[i].dBar.setValue(dValue);
				}
			}
			
			sbc[0].ignoreChangeEvents = false;
		}
		public void setSbarPositions(float[] allVisibleValues) {
			if (sbc.length < 1) return;
			sbc[0].ignoreChangeEvents = true;

			for (int i=0; i<sbc.length; i++) {
				float sbarValue = allVisibleValues[oIDs[i]];
				
				if (continuousMode) {
					int cValue = (int)((sbarValue*ContModeMaxValue)/((float)sbc[i].maxDiscreteID) + 0.5f);
					if (cValue < 0) cValue = 0;
					if (cValue > ContModeMaxValue) cValue = ContModeMaxValue;
					sbc[i].cBar.setValue(cValue);
				} else {
					int dValue = (int)(sbarValue + 0.5f);
					if (dValue < 0) dValue = 0;
					if (dValue > sbc[i].maxDiscreteID) dValue = sbc[i].maxDiscreteID;
					sbc[i].dBar.setValue(dValue);
				}
			}
			
			sbc[0].ignoreChangeEvents = false;
		}
		
		public boolean isIgnoringChangeEvents() {
			if (sbc == null) return true;
			if (sbc.length < 1) return true;
			return sbc[0].ignoreChangeEvents;
		}
	}
	
	public static class SBarCollection {
		private JLabel lblCaption, lblValue;
		private JSlider dBar, cBar;
		private int maxDiscreteID;
		private boolean ignoreChangeEvents;
		
		private SBarCollection(SliderBarSetup sbarSetup, ChangeListener chgListener) {
			lblCaption = new JLabel();
			lblValue = new JLabel();
			
			maxDiscreteID = sbarSetup.numDiscreteLevels()-1;
			dBar = new JSlider(JSlider.HORIZONTAL, 0, maxDiscreteID, 0);
			dBar.addChangeListener(chgListener);
			
			cBar = new JSlider(JSlider.HORIZONTAL, 0, ContModeMaxValue, 0);
			cBar.addChangeListener(chgListener);
			
			ignoreChangeEvents = true;
		}
	}
	

	public static CDSliderBarsJPanel createPanel(SliderBarsManager sbarMan, CurVisualizationType cvType, ChangeListener chgListener,
			int lblHeight, int edtHeight, int lineSpacing, int tMargin, int valueFontSize, int barWidth, int valueLabelWidth) {
		
		ArrayList<Integer> lstStoOIDs = new ArrayList<Integer>();
		
		int numVisibleBars = sbarMan.numVisibleSBars();
		for (int i=0; i<numVisibleBars; i++) {
			if (cvType.ableToShow(sbarMan.getSBar(i).mDesignation())
					&&sbarMan.getSBarUserShow(i)) {
				lstStoOIDs.add(i);
			}
		}
		
		int numSliders = lstStoOIDs.size();
		
		int pWidth = barWidth + valueLabelWidth + tMargin*3;
		int pHeight = numSliders*lineSpacing*2 + (numSliders+1)*tMargin;
		
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
		CDSliderBarsJPanel panel = new CDSliderBarsJPanel(pWidth, pHeight);
		panel.setBorder(blackBorder);
		
		JLabel lbl = new JLabel();
        Font valueFont = new Font(lbl.getFont().getFontName(), Font.PLAIN, valueFontSize);

		panel.oIDs = new int[numSliders];
		for (int i=0; i<numSliders; i++) panel.oIDs[i] = lstStoOIDs.get(i);
		
		panel.sbc = new SBarCollection[numSliders];
		
		int cx = tMargin;
		int cy = tMargin;
		
		for (int i=0; i<numSliders; i++) {
			panel.sbc[i] = new SBarCollection(sbarMan.getSBar(panel.oIDs[i]), chgListener);
			
			panel.sbc[i].lblCaption.setSize(barWidth + valueLabelWidth + tMargin, lblHeight);
			panel.sbc[i].lblCaption.setLocation(cx, cy + (lineSpacing - lblHeight)/2);
			panel.add(panel.sbc[i].lblCaption);
			
			cy += lineSpacing;
			
			panel.sbc[i].dBar.setSize(barWidth, edtHeight);
			panel.sbc[i].dBar.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.add(panel.sbc[i].dBar);
			
			panel.sbc[i].cBar.setSize(barWidth, edtHeight);
			panel.sbc[i].cBar.setLocation(cx, cy + (lineSpacing - edtHeight)/2);
			panel.add(panel.sbc[i].cBar);
			
			panel.sbc[i].lblValue.setSize(valueLabelWidth, edtHeight);
			panel.sbc[i].lblValue.setLocation(cx + barWidth + tMargin, cy + (lineSpacing - edtHeight)/2);
			panel.sbc[i].lblValue.setFont(valueFont);
			panel.sbc[i].lblValue.setBorder(blackBorder);
			panel.sbc[i].lblValue.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(panel.sbc[i].lblValue);			
			
			cy += lineSpacing + tMargin;
		}
		
		panel.setContinuousMode(false);
		return panel;
	}
}
