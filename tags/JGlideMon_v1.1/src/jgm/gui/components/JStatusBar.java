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
import javax.swing.*;

public class JStatusBar extends jgm.gui.panes.Pane {
	private JLabel text = new JLabel();
	private JProgressBar progress = new JProgressBar();
	//private JLabel extra = new JLabel("        ");
	
	public JStatusBar(jgm.gui.GUI gui) {
		super(gui);
		
		Insets insets = new Insets(0, 2, 0, 2);
		javax.swing.border.Border b =
			BorderFactory.createLoweredBevelBorder();
		
		c.insets = insets; c.weightx = 1.0; c.weighty = 0.0;
		text.setBorder(b);
		add(text, c);
		
		c.gridx++; c.weightx = 0.001;
		progress.setVisible(false);
		progress.setBorder(b);
		progress.setBorderPainted(true);
		add(progress, c);
		
		//c.gridx++; c.weightx = 0.0;
		//extra.setBorder(b);
		//add(extra, c);
	}
	
	public void setText(String str) {
		text.setText("  " + str);
	}
	
	public JProgressBar getProgressBar() {
		return progress;
	}
	
	//public void setExtra(String str) {
	//	extra.setText("    " + str + "    ");
	//}
}
