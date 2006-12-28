package jgm.gui.components;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class HeadingDial extends JComponent {
	private static BufferedImage bg    = null;
	private static BufferedImage arrow = null;
	
	static {
		try {
			bg = javax.imageio.ImageIO.read(
					jgm.JGlideMon.class.getResource("resources/images/blank.png")
				);
			arrow = javax.imageio.ImageIO.read(
					jgm.JGlideMon.class.getResource("resources/images/arrow.png")
			); 
		} catch (Exception e){
			System.err.println("Unable to load resources: " + e.getMessage());
		}
	}
	
	private double heading = 0.0;

	public HeadingDial() {
		setMinimumSize(size);
		setMaximumSize(size);
	}

	public void setHeading(double newHeading) {
		heading = newHeading;
		repaint();
	}

	private static final Dimension size = new Dimension(45, 45);

	public Dimension getPreferredSize() {
		return size;
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		RenderingHints rh = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(rh);
			
		g.drawImage(bg, 0, 0, null);
		
		// it would be Math.PI / 2.0 + heading but
		// the arrow points up unrotated
		g2.rotate(-heading, 45 / 2, 45 / 2);
		
		g.drawImage(arrow, 25 / 2, 25 / 2, null);
	}
}
