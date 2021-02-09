package pvc.gui.comp;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pvc.utility.NumToString;

@SuppressWarnings("serial")
public class TwoValueDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int TClear = 2;
	private static final int EdtHeight = LblHeight + TClear;
	private static final int BtnHeight = EdtHeight + TClear*2;
	private static final int LineSpacing = BtnHeight + TClear;
	private static final int BigBtnHeight = (3*BtnHeight)/2;

	private static final int BtnWidth = 90;

	//GUI & Data Objects
	private JButton btnOK, btnCancel;
	private JTextField edt1, edt2;
	
	private boolean okPressed;
	public boolean okPressed() {return okPressed;}
	
	private float v1, v2;
	public float v1() {return v1;}
	public float v2() {return v2;}
	
	//Constructor
	public TwoValueDialog(String dlgTitle, float value1, float value2, int numDec1, int numDec2, 
			String caption1, String caption2, String footNote, int captionsWidth, int edtWidth, int footnoteFontSize) {
		
		super(null, dlgTitle, Dialog.ModalityType.APPLICATION_MODAL);

		okPressed = false;
		v1 = value1;
		v2 = value2;
		
		JPanel ct = createContentPane(numDec1, numDec2, caption1, caption2, footNote, captionsWidth, edtWidth, footnoteFontSize);
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
			if (!screenToData()) {
				JOptionPane.showMessageDialog(null, "Invalid Numeric Value", 
						"Error", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			okPressed = true;
			dispose();	
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
		}
	}
	
	private JPanel createContentPane(int numDec1, int numDec2, String caption1, String caption2, String footNote, int captionsWidth, int edtWidth, int fnFontSize) {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        int panelWidth = WinMargin*2 + TClear + Math.max(BtnWidth*2, captionsWidth + edtWidth);
        int panelHeight = LineSpacing*2 + BigBtnHeight + WinMargin*5;
        if (footNote!=null) panelHeight += LblHeight*2 + WinMargin;

        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel(caption1);
        lbl1.setSize(captionsWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        edt1 = new JTextField(NumToString.floatWNumDecimals(v1, numDec1));
        edt1.setSize(edtWidth, EdtHeight);
        edt1.setLocation(panelWidth - WinMargin - edtWidth, cy + (LineSpacing - EdtHeight)/2);
        edt1.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edt1);
        
        cy += LineSpacing + WinMargin;
                  
        JLabel lbl2 = new JLabel(caption2);
        lbl2.setSize(captionsWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
         
        edt2 = new JTextField(NumToString.floatWNumDecimals(v2, numDec2));
        edt2.setSize(edtWidth, EdtHeight);
        edt2.setLocation(panelWidth - WinMargin - edtWidth, cy + (LineSpacing - EdtHeight)/2);
        edt2.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edt2);
         
        cy += LineSpacing + WinMargin;
         
        if (footNote!=null) {
            Font fontNote = new Font(lbl1.getFont().getName(), Font.PLAIN, fnFontSize);

            JLabel lbl3 = new JLabel("<html>"+footNote+"</html>");
            lbl3.setSize(captionsWidth+TClear+edtWidth, LblHeight*2);
            lbl3.setLocation(cx, cy);
            lbl3.setFont(fontNote);
            totalGUI.add(lbl3);

            cy += WinMargin + LblHeight*2;
        }
        
        cy += WinMargin;
        
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

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	private boolean screenToData() {
		try {
			v1 = Float.parseFloat(edt1.getText());
			v2 = Float.parseFloat(edt2.getText());
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
}
