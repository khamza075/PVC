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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pvc.datamgmt.comp.DUnits;

@SuppressWarnings("serial")
public class EditFEcoSimTDMDialog extends JDialog implements ActionListener {
	//GUI Sizing constants
	private static final int WinMargin = 10;
	private static final int LblHeight = 16;
	private static final int EdtHeight = LblHeight + 2;
	private static final int BtnHeight = EdtHeight + 4;
	private static final int LineSpacing = BtnHeight + 2;
	private static final int BigBtnHeight = (3*BtnHeight)/2;

	private static final int SZControlWidth = 500;
	private static final int MilesEdtWidth = 90;
	private static final int ShortLabelWidth = 70;

	//Data & GUI Objects
	private JButton btnOK, btnCancel;
	private JTextField edtAnnualDrvDistance, edtAnuualDriveDays, edtTitle, edtDescription;
	private boolean okPressed;
	private String title, description;
	private float annualVMT, annualDriveDays;
	
	public EditFEcoSimTDMDialog(String fecosTitle, String fecosDescription, float averageAnnualMiles, float averageAnnualDriveDays) {
		super(null, "Edit Description of Fuel Economy Simulations Run", Dialog.ModalityType.APPLICATION_MODAL);
		
		okPressed = false;
		title = new String(fecosTitle);
		description = new String(fecosDescription);
		annualVMT = averageAnnualMiles;
		annualDriveDays = averageAnnualDriveDays;

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
			screenToVaraibles();
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
        
        int panelWidth = SZControlWidth + MilesEdtWidth + WinMargin*3;
        int panelHeight = LineSpacing*3 + BigBtnHeight + WinMargin*3;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        JLabel lbl1 = new JLabel("Title");
        lbl1.setSize(ShortLabelWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        int edtTitleWidth = SZControlWidth/2 - ShortLabelWidth -1;
        
        edtTitle = new JTextField(title);
        edtTitle.setSize(edtTitleWidth, EdtHeight);
        edtTitle.setLocation(cx + SZControlWidth/2 - edtTitleWidth, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(edtTitle);
        
        float annualDriveDistance = annualVMT / DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
        
        edtAnnualDrvDistance = new JTextField(""+annualDriveDistance);
        edtAnnualDrvDistance.setSize(MilesEdtWidth, EdtHeight);
        edtAnnualDrvDistance.setLocation(cx + SZControlWidth + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        edtAnnualDrvDistance.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtAnnualDrvDistance);
        
        String distanceCaption = "Average Annual Driving Distance ("+DUnits.getShortName(DUnits.UnitType.Distance)+")";

        JLabel lbl2 = new JLabel(distanceCaption);
        lbl2.setSize(SZControlWidth/2 -1, LblHeight);
        lbl2.setLocation(cx + SZControlWidth/2 + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);
        
        cy += LineSpacing;
        
        edtAnuualDriveDays = new JTextField(""+(int)annualDriveDays);
        edtAnuualDriveDays.setSize(MilesEdtWidth, EdtHeight);
        edtAnuualDriveDays.setLocation(cx + SZControlWidth + WinMargin, cy + (LineSpacing - EdtHeight)/2);
        edtAnuualDriveDays.setHorizontalAlignment(SwingConstants.RIGHT);
        totalGUI.add(edtAnuualDriveDays);
        
        String daysCaption = "Average Annual Number of Driving Days";

        JLabel lbl2b = new JLabel(daysCaption);
        lbl2b.setSize(SZControlWidth/2 -1, LblHeight);
        lbl2b.setLocation(cx + SZControlWidth/2 + WinMargin, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2b);
                
        cy += LineSpacing;
        
        JLabel lbl3 = new JLabel("Description");
        lbl3.setSize(ShortLabelWidth, LblHeight);
        lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);
        
        edtDescription = new JTextField(description);
        edtDescription.setSize(SZControlWidth + WinMargin + MilesEdtWidth - ShortLabelWidth -1, EdtHeight);
        edtDescription.setLocation(cx + ShortLabelWidth +1, cy + (LineSpacing - EdtHeight)/2);
        totalGUI.add(edtDescription);
        
        cy += LineSpacing + WinMargin;
        
        btnOK = new JButton("OK");
        btnOK.setSize(MilesEdtWidth, BigBtnHeight);
        btnOK.setLocation((panelWidth - WinMargin/2)/2 - MilesEdtWidth, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(MilesEdtWidth, BigBtnHeight);
        btnCancel.setLocation(panelWidth/2 + WinMargin/4, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);
                
        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	private void screenToVaraibles() {
		float txtNum = -1f;
		try {
			txtNum = Float.parseFloat(edtAnnualDrvDistance.getText()) * DUnits.convConstMPtoBCalc(DUnits.UnitType.Distance);
		} catch (Exception e) {}
		if (txtNum > 0) annualVMT = txtNum;
		
		txtNum = -1f;
		try {
			txtNum = Float.parseFloat(edtAnuualDriveDays.getText());
		} catch (Exception e) {}
		if (txtNum > 0) annualDriveDays = txtNum;

		title = edtTitle.getText();
		description = edtDescription.getText();
	}
	
	public String getUITitle() {return title;}
	public String getUIDescription() {return description;}
	public float getUIAnnualVMT() {return annualVMT;}
	public float getUIAnnualDriveDays() {return annualDriveDays;}
}
