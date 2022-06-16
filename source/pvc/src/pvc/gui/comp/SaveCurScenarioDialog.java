package pvc.gui.comp;

import java.awt.Dialog;
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


@SuppressWarnings("serial")
public class SaveCurScenarioDialog extends JDialog implements ActionListener {
	//Constants
	private static final int WinMargin = 10;
	private static final int TClear = 2;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (BtnHeight*3)/2;

	private static final int LblWidth = 120;
	private static final int EdtShortWidth = 240;
	private static final int BtnWidth = 80;

	//GUI Objects
	private JButton btnOK, btnCancel;
	private JTextField edtShort, edtLong;

	//Data
	private boolean okPressed;
	private String stShort, stLong;

	//Data access functions
	public boolean okPressed() {return okPressed;}
	public String getSaveScenario_shortDescription() {return stShort;}
	public String getSaveScenario_longDescription() {return stLong;}

	//Constructor
	public SaveCurScenarioDialog() {
		//Call Super
		super(null, "Save Settings & Display as a Scenario", Dialog.ModalityType.APPLICATION_MODAL);

		//Set Data
		okPressed = false;
		
		//Create Graphics	
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
			//Read data from GUI
			stShort = new String(edtShort.getText());
			stLong = new String(edtLong.getText());
			
			//Error Trapping
			if (stShort.length() < 1) {
				JOptionPane.showMessageDialog(null, "Short Description Cannot be Blank", 
						"Please Modify", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (stShort.contains(",")) {
				JOptionPane.showMessageDialog(null, "Short Description Cannot Contain Comma(s)", 
						"Please Modify", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (stLong.length() < 1) {
				JOptionPane.showMessageDialog(null, "Detailed Description Cannot be Blank", 
						"Please Modify", JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//Return
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
        
        int edtLongWidth = TClear + LblWidth + EdtShortWidth;
        int panelWidth = WinMargin*2 + edtLongWidth;
        int panelHeight = WinMargin*3 + WinMargin/2 + LineSpacing*3 + BigBtnHeight;

        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Short Description");
        lbl1.setSize(LblWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl1);
        
        edtShort = new JTextField();
        edtShort.setSize(EdtShortWidth, EdtHeight);
        edtShort.setLocation(cx + LblWidth + TClear, cy + (LineSpacing-EdtHeight)/2);
        totalGUI.add(edtShort);
        
        cy += LineSpacing + WinMargin/2;
                
        JLabel lbl2 = new JLabel("Detailed Description");
        lbl2.setSize(edtLongWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing-LblHeight)/2);
        totalGUI.add(lbl2);

        cy += LineSpacing;
        
        edtLong = new JTextField();
        edtLong.setSize(edtLongWidth, EdtHeight);
        edtLong.setLocation(cx, cy + (LineSpacing-EdtHeight)/2);
        totalGUI.add(edtLong);

        cy += LineSpacing + WinMargin;
        cx = panelWidth/2 - WinMargin/2 - BtnWidth;
        
        btnOK = new JButton("OK");
        btnOK.setSize(BtnWidth, BigBtnHeight);
        btnOK.setLocation(cx, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        cx += BtnWidth + WinMargin;
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(BtnWidth, BigBtnHeight);
        btnCancel.setLocation(cx, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
        
        totalGUI.setSize(panelWidth, panelHeight);        
        totalGUI.setOpaque(true);
		return totalGUI;

	}
}
