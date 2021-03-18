package pvc.gui.comp;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class ColorJLabel extends JLabel {

	private Color color;
	
	public ColorJLabel(Color cColor) {
		super();
		color = cColor;
	}
	public void setColor(Color cColor) {
		color = cColor;
		repaint();
	}
	@Override
    protected void paintComponent(Graphics g) {
		//Call base function
		super.paintComponent(g);
		
		g.setColor(color);
		g.fillRect(0, 0, getSize().width, getSize().height);
	}

}
