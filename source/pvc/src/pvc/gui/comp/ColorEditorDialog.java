package pvc.gui.comp;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ColorEditorDialog  extends JDialog implements ActionListener, ChangeListener {
	//Sizing Constants
	private static final int WinMargin = 10;
	private static final int TMargin = 4;
	private static final int LblHeight = 16;
	private static final int TClearance = 2;
	private static final int EdtHeight = LblHeight + TClearance;
	private static final int BtnHeight = EdtHeight + TClearance*2;
	private static final int LineSpacing = BtnHeight + TClearance;
	private static final int BigBtnHeight = (3*BtnHeight)/2;
	
	private static final int SpnWidth = 60;
	private static final int LblWidth = 100;

	private int cRed, cGreen, cBlue;
	public int colorRed() {return cRed;}
	public int colorGreen() {return cGreen;}
	public int colorBlue() {return cBlue;}
	
	private boolean okPressed;
	public boolean okPressed() {return okPressed;}
	
	private JSpinner spnRed, spnGreen, spnBlue;
	private JButton btnOK, btnCancel;
	private ColorLabel cLabel;

	
	public ColorEditorDialog(int red, int green, int blue) {
		//Super
		super(null, "Edit Color", Dialog.ModalityType.APPLICATION_MODAL);
		
		//Set data
		okPressed = false;
		cRed = red;
		cGreen = green;
		cBlue = blue;
		
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
	
	private JPanel createContentPane() {
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        
        int cLabelWidth = LblWidth + SpnWidth + TMargin;
        int cLabelHeight = (cLabelWidth*2)/3;
        
        int panelWidth = cLabelWidth + WinMargin*2;
        int panelHeight = cLabelHeight + LineSpacing*3 + BigBtnHeight + WinMargin*4;
        
        int cx = WinMargin;
        int cy = WinMargin;
        
        cLabel = new ColorLabel();
        cLabel.setSize(cLabelWidth, cLabelHeight);
        cLabel.setBorder(blackBorder);
        cLabel.setLocation(cx, cy);
        totalGUI.add(cLabel);
        
        cy += cLabelHeight + WinMargin;
        
        JLabel lbl1 = new JLabel("Red");
        lbl1.setSize(LblWidth, LblHeight);
        lbl1.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl1);
        
        SpinnerNumberModel spmRed = new SpinnerNumberModel(cRed, 0, 255, 1);
        SpinnerNumberModel spmGreen = new SpinnerNumberModel(cGreen, 0, 255, 1);
        SpinnerNumberModel spmBlue = new SpinnerNumberModel(cBlue, 0, 255, 1);

        spnRed = new JSpinner(spmRed);
        spnRed.setSize(SpnWidth, EdtHeight);
        spnRed.setLocation(cx + LblWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        spnRed.setBorder(blackBorder);
        spnRed.addChangeListener(this);
        totalGUI.add(spnRed);

        cy += LineSpacing;
               
        JLabel lbl2 = new JLabel("Green");
        lbl2.setSize(LblWidth, LblHeight);
        lbl2.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl2);

        spnGreen = new JSpinner(spmGreen);
        spnGreen.setSize(SpnWidth, EdtHeight);
        spnGreen.setLocation(cx + LblWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        spnGreen.setBorder(blackBorder);
        spnGreen.addChangeListener(this);
        totalGUI.add(spnGreen);

        cy += LineSpacing;
               
        JLabel lbl3 = new JLabel("Blue");
        lbl3.setSize(LblWidth, LblHeight);
        lbl3.setLocation(cx, cy + (LineSpacing - LblHeight)/2);
        totalGUI.add(lbl3);

        spnBlue = new JSpinner(spmBlue);
        spnBlue.setSize(SpnWidth, EdtHeight);
        spnBlue.setLocation(cx + LblWidth + TMargin, cy + (LineSpacing - EdtHeight)/2);
        spnBlue.setBorder(blackBorder);
        spnBlue.addChangeListener(this);
        totalGUI.add(spnBlue);
        

        cy += LineSpacing + WinMargin;
        
        btnOK = new JButton("OK");
        btnOK.setSize(cLabelWidth/2 - TMargin, BigBtnHeight);
        btnOK.setLocation(cx, cy);
        btnOK.addActionListener(this);
        totalGUI.add(btnOK);
        
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(cLabelWidth/2 - TMargin, BigBtnHeight);
        btnCancel.setLocation(cx + cLabelWidth/2 + TMargin/2, cy);
        btnCancel.addActionListener(this);
        totalGUI.add(btnCancel);

        totalGUI.setSize(panelWidth, panelHeight);
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	
	@Override 
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		
		if (source == spnRed) {
			cRed = Integer.parseInt(spnRed.getValue().toString());
	        cLabel.repaint();
			return;
		}
		if (source == spnGreen) {
			cGreen = Integer.parseInt(spnGreen.getValue().toString());
	        cLabel.repaint();
			return;
		}
		if (source == spnBlue) {
			cBlue = Integer.parseInt(spnBlue.getValue().toString());
	        cLabel.repaint();
			return;
		}
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == btnOK) {
			okPressed = true;
			dispose();	
		}
		if (source == btnCancel) {
			okPressed = false;
			dispose();	
		}
	}
	
	private class ColorLabel extends JLabel {
		private ColorLabel() {
			super();
		}
		@Override
	    protected void paintComponent(Graphics g) {
			//Call base function
			super.paintComponent(g);
			
			g.setColor(new Color(cRed, cGreen, cBlue));
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
	}
}
