/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 - 2008 Tim
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
package jgm.gui;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JWindow;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
//import javax.swing.JProgressBar;

public class Splash extends JWindow {
	JPanel cp;
	JLabel bgLabel;
	ImageIcon bg;
//	JProgressBar progress;
	JLabel status;
	
	public Splash(String statusText) {
		this.setAlwaysOnTop(true);
		setBackground(Color.WHITE);
		
		bg = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/splash.png"));
		bgLabel = new JLabel(bg);
//		progress = new JProgressBar();
//		setIndeterminante(true);
		status = new JLabel(statusText, JLabel.CENTER);
		status.setFont(status.getFont().deriveFont(Font.BOLD, 16.0f));
		status.setForeground(Color.BLACK);
		
		cp = new JPanel(new BorderLayout());
		cp.setBackground(Color.WHITE);
		setContentPane(cp);
		add(status, BorderLayout.SOUTH);
		add(bgLabel, BorderLayout.CENTER);
//		add(progress, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
//	public void setProgress(int value) {
//		progress.setValue(value);
//		setIndeterminante(false);
//	}
//	
//	public void setIndeterminante(boolean on) {
//		progress.setIndeterminate(on);
//	}
	
	public void setStatus(String s) {
		status.setText(s);
	}
}
