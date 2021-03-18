package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import pvc.datamgmt.comp.HourlyProfileCurve;
import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class GridHourlyProfileDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 15;
	private static final int TClear = 2;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	private static final int BigBtnHeight = (3*BtnHeight)/2;

	private static final int ProfileLabelWidth = 120;
	private static final int HrLabelWidth = 90;
	private static final int BtnWidth = 90;
	
	private static final int NumDecimals = 5;
	private static final int HrFontSize = 12;
	private static final int NoteFontSize = 11;
	private static final int NoteNumLines = 4;
	
	//GUI Objects
	private JButton btnOK, btnCancel;
	private JTextField[] edtValues;
	
	//Data
	private HourlyProfileCurve hCurve;
	private boolean okPressed;
	
	public HourlyProfileCurve getCurve() {return hCurve;}
	
	
	public GridHourlyProfileDialog(HourlyProfileCurve hourlyCurve, String dlgCaption) {
		super(null, dlgCaption, Dialog.ModalityType.APPLICATION_MODAL);

		okPressed = false;
		hCurve = new HourlyProfileCurve(hourlyCurve);
		
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
			if (screenToData()) {
				okPressed = true;
				dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Invalid Numeric Value", 
						"Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
			}
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
		}
	}
	public boolean okPressed() {return okPressed;}

	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int panelWidth = ProfileLabelWidth + HrLabelWidth + WinMargin/2 + WinMargin*2;
        int cx = WinMargin;
        int cy = WinMargin;
        
        float[] pValues = hCurve.getProfileValues();
        edtValues = new JTextField[pValues.length];
        for (int i=0; i<edtValues.length; i++) edtValues[i] = new JTextField(NumToString.floatWNumDecimals(pValues[i], NumDecimals));
        
        JLabel lbl1 = new JLabel("Hour");
        lbl1.setSize(HrLabelWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        lbl1.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl1);
        
        JLabel lbl2 = new JLabel("Profile Value*");
        lbl2.setSize(ProfileLabelWidth, LblHeight);
        lbl2.setLocation(cx + HrLabelWidth + WinMargin/2, cy + (LineSpacing - LblHeight)/2);
        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        totalGUI.add(lbl2);
        
        Font fontHr = new Font(lbl1.getFont().getName(), Font.PLAIN, HrFontSize);
        Font fontNote = new Font(lbl1.getFont().getName(), Font.PLAIN, NoteFontSize);

        cy += LineSpacing;

        for (int i=0; i<edtValues.length; i++) {
        	JLabel lbl = new JLabel(hrString(i)); 
            lbl.setSize(HrLabelWidth, EdtHeight);
            lbl.setLocation(cx, cy + TClear/2);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(fontHr);
            lbl.setBorder(blackBorder);
            totalGUI.add(lbl);
            
            edtValues[i].setSize(ProfileLabelWidth, EdtHeight);
            edtValues[i].setLocation(cx + HrLabelWidth + WinMargin/2, cy + TClear/2);
            edtValues[i].setHorizontalAlignment(SwingConstants.RIGHT);
            totalGUI.add(edtValues[i]);           
            
            cy += EdtHeight + TClear;
        }

        String footNote = "<html>"
        		+ "* Values more than 1.0 imply (proportionally) More than the Grid-Average, while values less than 1.0 imply Less than the Grid-Average"
        		+"</html>";
        int footNoteHeight = NoteNumLines * LblHeight;
        
        JLabel lbl3 = new JLabel(footNote);
        lbl3.setSize(HrLabelWidth+ProfileLabelWidth+WinMargin/2, footNoteHeight);
        lbl3.setLocation(cx, cy);
        lbl3.setVerticalAlignment(SwingConstants.TOP);
        lbl3.setFont(fontNote);
        totalGUI.add(lbl3);
        
        cy += WinMargin + footNoteHeight;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation(panelWidth/2 - BtnWidth - TClear/2, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(panelWidth/2 + TClear/2, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);        
        
        cy += WinMargin + BigBtnHeight;

        int panelHeight = cy;

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private static String hrString(int hID) {
		String st = ""+hID;
		if (hID < 10) st = "0"+hID;
		st = st + ":00";
		return st;
	}
	
	private boolean screenToData() {
		try {
			for (int i=0; i<edtValues.length; i++) {
				float value = Float.parseFloat(edtValues[i].getText());
				hCurve.setValue(i, value);
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
}
