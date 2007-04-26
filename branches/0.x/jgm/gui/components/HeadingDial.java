/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
package jgm.gui.components;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Draws an arrow within a circle pointing toward
 * a particular heading.
 * @author Tim
 * @since 0.1
 */
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
			System.err.println("Unable to load HeadingDial resources: " + e.getMessage());
		}
	}
	
	private double heading = -1.0;

	public HeadingDial() {
		setMinimumSize(size);
		setMaximumSize(size);
		setHeading(-1.0);
	}

	public void setHeading(double newHeading) {
		heading = newHeading;
		repaint();
		
		if (heading >= 0.0) {
			setToolTipText(String.format("Heading: %01.2f", heading));
		} else {
			setToolTipText(null);
		}
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
		
		// don't draw arrow if it's negative
		if (heading >= 0.0) {
			// it would be Math.PI / 2.0 + heading but
			// the arrow points up unrotated
			g2.rotate(-heading, 45 / 2, 45 / 2);
		
			g.drawImage(arrow, 25 / 2, 25 / 2, null);
		}
	}
}
