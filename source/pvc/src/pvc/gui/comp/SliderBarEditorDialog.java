package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pvc.datamgmt.SliderBarsManager;
import pvc.datamgmt.comp.SliderBarSetup;

@SuppressWarnings("serial")
public class SliderBarEditorDialog extends JDialog implements ActionListener {
	//Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	
	private static final int TitleLabelWidth = 320;
	private static final int ValueLabelWidth = 120;
	private static final int AddButtonWidth = 85;
	private static final int ListsNumLineSpacingHeight = 4;
	private static final int ValuesFontSize = 11;


	//GUI Objects
	private JButton btnOK, btnCancel, btnAddAbove, btnDeleteAbove, btnAddBelow, btnDeleteBelow;
	private JTextField edtAbove, edtBelow;
	private JList<String> selAbove, selBelow;
	private boolean watchEdt, watchLst;
	

	//Data objects
	private SliderBarsManager sbarMan;
	private int oID;
	private boolean canDecrease, canIncrease;
	private float uaBaseValue, uaMinValue, uaMaxValue;
	private String fsBaseValue, fsMinValue, fsMaxValue;
	
	private ArrayList <Float> uaValuesBelow, uaValuesAbove;
	public float[] uaValuesBelow() {
		if (uaValuesBelow == null) return null;
		float[] arr = new float[uaValuesBelow.size()];
		for (int i=0; i<arr.length; i++) arr[i] = uaValuesBelow.get(i);
		return arr;
	}
	public float[] uaValuesAbove() {
		if (uaValuesAbove == null) return null;
		float[] arr = new float[uaValuesAbove.size()];
		for (int i=0; i<arr.length; i++) arr[i] = uaValuesAbove.get(i);
		return arr;
	}
	
	private boolean okPressed;	
	
	public SliderBarEditorDialog(SliderBarsManager sliderBarsManager, int curSelectedSliderBarID) {
		//Call Super
		super(null, "Edit Scenario Parameter", Dialog.ModalityType.APPLICATION_MODAL);

		//Set Data Objects
		okPressed = false;
		sbarMan = sliderBarsManager;
		oID = curSelectedSliderBarID;
		
		SliderBarSetup sbar = sbarMan.getSBar(oID);
		
		canDecrease = sbar.canDecrease();
		canIncrease = sbar.canIncrease();
		
		float baseValue = sbar.baseValue();
		float minValue = sbar.minLimit();
		float maxValue = sbar.maxLimit();
		int numDiscereteLevels = sbar.numDiscreteLevels();
		int baseLineID = sbar.baselineIDinArray();
		
		uaBaseValue = sbarMan.unitsAdjustedValue(oID, baseValue);
		uaMinValue = sbarMan.unitsAdjustedValue(oID, minValue);
		uaMaxValue = sbarMan.unitsAdjustedValue(oID, maxValue);
		
		fsBaseValue = sbarMan.unitsFormattedValue(oID, baseValue);
		fsMinValue = sbarMan.unitsFormattedValue(oID, minValue);
		fsMaxValue = sbarMan.unitsFormattedValue(oID, maxValue);
		
		if (canDecrease) {
			int num = baseLineID;
			uaValuesBelow = new ArrayList <Float>();
			for (int i=0; i<num; i++) uaValuesBelow.add(sbarMan.unitsAdjustedValue(oID, sbar.getDValue(i)));
		} else {
			uaValuesBelow = null;
		}	
		if (canIncrease) {
			int num = numDiscereteLevels - baseLineID - 1;
			uaValuesAbove = new ArrayList <Float>();
			for (int i=0; i<num; i++) uaValuesAbove.add(sbarMan.unitsAdjustedValue(oID, sbar.getDValue(baseLineID+1+i)));
		} else {
			uaValuesAbove = null;
		}	
		
		sortList(uaValuesBelow);
		sortList(uaValuesAbove);
		if ((uaMinValue > uaBaseValue)||(uaMaxValue < uaBaseValue)) {
			reverseList(uaValuesBelow);
			reverseList(uaValuesAbove);
		}
		
		//Create and show GUI
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
	public boolean okPressed() {return okPressed;}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			okPressed = true;
			
			sortList(uaValuesBelow);
			sortList(uaValuesAbove);
			if ((uaMinValue > uaBaseValue)||(uaMaxValue < uaBaseValue)) {
				reverseList(uaValuesBelow);
				reverseList(uaValuesAbove);
			}
			
			dispose();	
			return;
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
			return;
		}
		
		if (source == btnAddBelow) {
			sortList(uaValuesBelow);
			if ((uaMinValue > uaBaseValue)||(uaMaxValue < uaBaseValue)) reverseList(uaValuesBelow);
			
			if (uaValuesBelow.size() > 0) {
				float nValue = 0.5f*(uaBaseValue + uaValuesBelow.get(uaValuesBelow.size()-1));
				uaValuesBelow.add(nValue);
			} else {
				uaValuesBelow.add(uaMinValue);
			}
			
	        String[] listValues = new String[uaValuesBelow.size()];
	        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesBelow.get(i);
	        selBelow.setListData(listValues);
	        selBelow.setSelectedIndex(listValues.length-1);
	        return;
		}		
		if (source == btnAddAbove) {
			sortList(uaValuesAbove);
			if ((uaMinValue > uaBaseValue)||(uaMaxValue < uaBaseValue)) reverseList(uaValuesAbove);
			
			if (uaValuesAbove.size() > 0) {
				float nValue = 0.5f*(uaBaseValue + uaValuesAbove.get(0));
				uaValuesAbove.add(0, nValue);
			} else {
				uaValuesAbove.add(uaMaxValue);
			}
			
	        String[] listValues = new String[uaValuesAbove.size()];
	        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesAbove.get(i);
	        selAbove.setListData(listValues);
	        selAbove.setSelectedIndex(0);
	        return;
		}
		if (source == btnDeleteBelow) {
			int id = selBelow.getSelectedIndex();
			if (id < 0) return;
			
			uaValuesBelow.remove(id);
			
	        String[] listValues = new String[uaValuesBelow.size()];
	        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesBelow.get(i);
	        selBelow.setListData(listValues);
			return;
		}
		if (source == btnDeleteAbove) {
			int id = selAbove.getSelectedIndex();
			if (id < 0) return;
			
			uaValuesAbove.remove(id);

	        String[] listValues = new String[uaValuesAbove.size()];
	        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesAbove.get(i);
	        selAbove.setListData(listValues);
			return;
		}
	}

	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int panelWidth = TitleLabelWidth + WinMargin*2;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl = new JLabel(sbarMan.unitsFormattedCaption(oID));
        lbl.setSize(TitleLabelWidth, LblHeight);
        lbl.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl);
        
        Font valuesFont = new Font(lbl.getFont().getName(), Font.PLAIN, ValuesFontSize);

        cx += WinMargin;
        cy += LineSpacing + WinMargin;
        
        int lblsAreaWidth = TitleLabelWidth - WinMargin - ValueLabelWidth;
        int listsHeight = ListsNumLineSpacingHeight * LineSpacing;
        
        if (canDecrease) {
            JLabel lbl1 = new JLabel("Limiting Value");
            lbl1.setSize(lblsAreaWidth-2, LblHeight);
            lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            totalGUI.add(lbl1);

            JLabel lbl2 = new JLabel(fsMinValue);
            lbl2.setSize(ValueLabelWidth, EdtHeight);
            lbl2.setLocation(cx + lblsAreaWidth, cy + (LineSpacing - EdtHeight)/2);
            lbl2.setBorder(blackBorder);
            lbl2.setHorizontalAlignment(SwingConstants.CENTER);
            lbl2.setFont(valuesFont);
            totalGUI.add(lbl2);

            cy += LineSpacing + WinMargin/2;
            
            JLabel lbl3 = new JLabel("Values between");
            lbl3.setSize(lblsAreaWidth-2, LblHeight);
            lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl3.setHorizontalAlignment(SwingConstants.CENTER);
            totalGUI.add(lbl3);
            
            selBelow = new JList<String>();
            selBelow.setLocation(0,0);
            selBelow.setSize(ValueLabelWidth, listsHeight);
            selBelow.setBorder(blackBorder);
            selBelow.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            	public void valueChanged(ListSelectionEvent e) {
            		selBelowChanged();
            	}
            });
            JScrollPane sc1 = new JScrollPane(selBelow);
            sc1.setLocation(cx + lblsAreaWidth, cy);
            sc1.setSize(ValueLabelWidth, listsHeight);  
            totalGUI.add(sc1);
            
            selBelow.setFont(valuesFont);
            
            String[] listValues = new String[uaValuesBelow.size()];
            for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesBelow.get(i);
            selBelow.setListData(listValues);
            
                        
            cy += LblHeight;
            
            JLabel lbl4 = new JLabel("Limit & Baseline");
            lbl4.setSize(lblsAreaWidth-2, LblHeight);
            lbl4.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl4.setHorizontalAlignment(SwingConstants.CENTER);
            totalGUI.add(lbl4);

            cy += LineSpacing;
            
            btnAddBelow = new JButton("Add");
            btnAddBelow.setSize(AddButtonWidth, BtnHeight);
            btnAddBelow.setLocation(cx + lblsAreaWidth/2 - AddButtonWidth/2, cy + (LineSpacing - BtnHeight)/2);
            btnAddBelow.addActionListener(this);
            totalGUI.add(btnAddBelow);

            cy += LineSpacing;
            
            btnDeleteBelow = new JButton("Delete");
            btnDeleteBelow.setSize(AddButtonWidth, BtnHeight);
            btnDeleteBelow.setLocation(cx + lblsAreaWidth/2 - AddButtonWidth/2, cy + (LineSpacing - BtnHeight)/2);
            btnDeleteBelow.addActionListener(this);
            btnDeleteBelow.setEnabled(false);
            totalGUI.add(btnDeleteBelow);
            
            cy += listsHeight - LineSpacing*2 - LblHeight;
            
            JLabel lbl5 = new JLabel("Edit Selected Value");
            lbl5.setSize(lblsAreaWidth-WinMargin/2, LblHeight);
            lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl5.setHorizontalAlignment(SwingConstants.RIGHT);
            totalGUI.add(lbl5);
             
            edtBelow = new JTextField();
            edtBelow.setSize(ValueLabelWidth, EdtHeight);
            edtBelow.setLocation(cx + lblsAreaWidth, cy + (LineSpacing - EdtHeight)/2);
            edtBelow.setHorizontalAlignment(SwingConstants.RIGHT);
            edtBelow.setFont(valuesFont);
            edtBelow.getDocument().addDocumentListener(new DocumentListener() {
    			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
    			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
    			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
    			private void processChange() {
    				editBelowChanged();	
    			}
    		});
            totalGUI.add(edtBelow);
            
            cy += LineSpacing + WinMargin;
        }
        
        JLabel lbl1b = new JLabel("Baseline Value");
        lbl1b.setSize(lblsAreaWidth-2, LblHeight);
        lbl1b.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1b);

        JLabel lbl2b = new JLabel(fsBaseValue);
        lbl2b.setSize(ValueLabelWidth, EdtHeight);
        lbl2b.setLocation(cx + lblsAreaWidth, cy + (LineSpacing - EdtHeight)/2);
        lbl2b.setBorder(blackBorder);
        lbl2b.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2b.setFont(valuesFont);
        totalGUI.add(lbl2b);

        cy += LineSpacing + WinMargin;
        
        if (canIncrease) {
            JLabel lbl3 = new JLabel("Values between");
            lbl3.setSize(lblsAreaWidth-2, LblHeight);
            lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl3.setHorizontalAlignment(SwingConstants.CENTER);
            totalGUI.add(lbl3);
            
            selAbove = new JList<String>();
            selAbove.setLocation(0,0);
            selAbove.setSize(ValueLabelWidth, listsHeight);
            selAbove.setBorder(blackBorder);
            selAbove.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            	public void valueChanged(ListSelectionEvent e) {
            		selAboveChanged();
            	}
            });
            JScrollPane sc1 = new JScrollPane(selAbove);
            sc1.setLocation(cx + lblsAreaWidth, cy);
            sc1.setSize(ValueLabelWidth, listsHeight);  
            totalGUI.add(sc1);
            
            selAbove.setFont(valuesFont);
            
            String[] listValues = new String[uaValuesAbove.size()];
            for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesAbove.get(i);
            selAbove.setListData(listValues);
            
                        
            cy += LblHeight;
            
            JLabel lbl4 = new JLabel("Limit & Baseline");
            lbl4.setSize(lblsAreaWidth-2, LblHeight);
            lbl4.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl4.setHorizontalAlignment(SwingConstants.CENTER);
            totalGUI.add(lbl4);

            cy += LineSpacing;
            
            btnAddAbove = new JButton("Add");
            btnAddAbove.setSize(AddButtonWidth, BtnHeight);
            btnAddAbove.setLocation(cx + lblsAreaWidth/2 - AddButtonWidth/2, cy + (LineSpacing - BtnHeight)/2);
            btnAddAbove.addActionListener(this);
            totalGUI.add(btnAddAbove);

            cy += LineSpacing;
            
            btnDeleteAbove = new JButton("Delete");
            btnDeleteAbove.setSize(AddButtonWidth, BtnHeight);
            btnDeleteAbove.setLocation(cx + lblsAreaWidth/2 - AddButtonWidth/2, cy + (LineSpacing - BtnHeight)/2);
            btnDeleteAbove.addActionListener(this);
            btnDeleteAbove.setEnabled(false);
            totalGUI.add(btnDeleteAbove);
            
            cy += listsHeight - LineSpacing*2 - LblHeight;
            
            JLabel lbl5 = new JLabel("Edit Selected Value");
            lbl5.setSize(lblsAreaWidth-WinMargin/2, LblHeight);
            lbl5.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            lbl5.setHorizontalAlignment(SwingConstants.RIGHT);
            totalGUI.add(lbl5);
             
            edtAbove = new JTextField();
            edtAbove.setSize(ValueLabelWidth, EdtHeight);
            edtAbove.setLocation(cx + lblsAreaWidth, cy + (LineSpacing - EdtHeight)/2);
            edtAbove.setHorizontalAlignment(SwingConstants.RIGHT);
            edtAbove.setFont(valuesFont);
            edtAbove.getDocument().addDocumentListener(new DocumentListener() {
    			@Override public void changedUpdate(DocumentEvent arg0) {processChange();}
    			@Override public void insertUpdate(DocumentEvent arg0) {processChange();}
    			@Override public void removeUpdate(DocumentEvent arg0) {processChange();}
    			private void processChange() {
    				editAboveChanged();	
    			}
    		});
            totalGUI.add(edtAbove);

            cy += LineSpacing + WinMargin/2;
        	
            JLabel lbl1 = new JLabel("Limiting Value");
            lbl1.setSize(lblsAreaWidth-2, LblHeight);
            lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
            totalGUI.add(lbl1);

            JLabel lbl2 = new JLabel(fsMaxValue);
            lbl2.setSize(ValueLabelWidth, EdtHeight);
            lbl2.setLocation(cx + lblsAreaWidth, cy + (LineSpacing - EdtHeight)/2);
            lbl2.setBorder(blackBorder);
            lbl2.setHorizontalAlignment(SwingConstants.CENTER);
            lbl2.setFont(valuesFont);
            totalGUI.add(lbl2);
            
            cy += LineSpacing + WinMargin;
        }

        btnOK = new JButton("OK");
        btnOK.setSize(AddButtonWidth, BigBtnHeight);
        btnOK.setLocation(WinMargin + TitleLabelWidth/2 - WinMargin/4 - AddButtonWidth, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);

        btnCancel = new JButton("Cancel");
        btnCancel.setSize(AddButtonWidth, BigBtnHeight);
        btnCancel.setLocation(WinMargin + TitleLabelWidth/2 + WinMargin/4, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
        
        cy += BigBtnHeight + WinMargin;

        int panelHeight = cy;       
        watchEdt = true;
        watchLst = true;

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private void selBelowChanged() {
		if (!watchLst) return;
		
		int curSelID = selBelow.getSelectedIndex();
		if (curSelID < 0) {
			edtBelow.setText("");
			btnDeleteBelow.setEnabled(false);
			return;
		}
		btnDeleteBelow.setEnabled(true);

		watchEdt = false;
		edtBelow.setText(""+uaValuesBelow.get(curSelID));
		watchEdt = true;
	}
	private void editBelowChanged() {
		if (!watchEdt) return;
		
		int curSelID = selBelow.getSelectedIndex();
		if (curSelID < 0) return;
		
		float nValue = Float.MAX_VALUE;
		try {
			nValue = Float.parseFloat(edtBelow.getText());
		} catch (Exception e) {
			return;
		}
		
		if (!isBetween(nValue, uaBaseValue, uaMinValue)) return;
		if (isDuplicate(nValue, uaValuesBelow, curSelID)) return;
		
		uaValuesBelow.set(curSelID, nValue);
		
		watchLst = false;
        String[] listValues = new String[uaValuesBelow.size()];
        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesBelow.get(i);
        selBelow.setListData(listValues);
        selBelow.setSelectedIndex(curSelID);
		watchLst = true;
	}
	
	private void selAboveChanged() {
		if (!watchLst) return;
		
		int curSelID = selAbove.getSelectedIndex();
		if (curSelID < 0) {
			edtAbove.setText("");
			btnDeleteAbove.setEnabled(false);
			return;
		}
		btnDeleteAbove.setEnabled(true);
		
		watchEdt = false;
		edtAbove.setText(""+uaValuesAbove.get(curSelID));
		watchEdt = true;
	}
	private void editAboveChanged() {
		if (!watchEdt) return;
		
		int curSelID = selAbove.getSelectedIndex();
		if (curSelID < 0) return;
		
		float nValue = Float.MAX_VALUE;
		try {
			nValue = Float.parseFloat(edtAbove.getText());
		} catch (Exception e) {
			return;
		}
		
		if (!isBetween(nValue, uaBaseValue, uaMaxValue)) return;
		if (isDuplicate(nValue, uaValuesAbove, curSelID)) return;
		
		uaValuesAbove.set(curSelID, nValue);
		
		watchLst = false;
        String[] listValues = new String[uaValuesAbove.size()];
        for (int i=0; i<listValues.length; i++) listValues[i] = ""+uaValuesAbove.get(i);
        selAbove.setListData(listValues);
        selAbove.setSelectedIndex(curSelID);
		watchLst = true;
	}
	
	private boolean isBetween(float value, float baselineValue, float limitingValue) {
		if (limitingValue < baselineValue) {
			if (value < limitingValue) return false;
			if (value >= baselineValue) return false;
			return true;
		} else {
			if (value > limitingValue) return false;
			if (value <= baselineValue) return false;
			return true;
		}
	}
	private boolean isDuplicate(float value, ArrayList<Float> lst, int exceptID) {
		float zeroTol = Math.min(0.00001f, Math.min(0.00001f*Math.abs(uaBaseValue), 0.000001f*Math.abs(uaMaxValue - uaMinValue)));

		for (int i=0; i<exceptID; i++) {
			if (Math.abs(value - lst.get(i)) < zeroTol) return true;
		}
		for (int i=exceptID=1; i<lst.size(); i++) {
			if (Math.abs(value - lst.get(i)) < zeroTol) return true;
		}

		return false;
	}
	private void sortList(ArrayList<Float> lst) {
		if (lst == null) return;
		if (lst.size() < 2) return;
		
		int nSorted = 1;
		while (nSorted < lst.size()) {
			float src = lst.get(nSorted);
			lst.remove(nSorted);
			
			int insertPos = 0;
			while (insertPos < nSorted) {
				if (src < lst.get(insertPos)) break;
				insertPos++;
			}
			
			lst.add(insertPos, src);			
			nSorted++;
		}
	}
	private void reverseList(ArrayList<Float> lst) {
		if (lst == null) return;
		if (lst.size() < 2) return;
		
		ArrayList<Float> tmpList = new ArrayList<Float>();
		while (lst.size() > 0) {
			tmpList.add(lst.get(lst.size()-1));
			lst.remove(lst.size()-1);
		}
		
		for (int i=0; i<tmpList.size(); i++) {
			lst.add(tmpList.get(i));
		}
	}
}
