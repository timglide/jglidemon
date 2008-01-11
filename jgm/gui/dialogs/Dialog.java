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
package jgm.gui.dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JDialog;

public abstract class Dialog extends JDialog {
	protected String title;
	protected GridBagConstraints c = new GridBagConstraints();
	
	public Dialog(Frame owner, String title) {
		super(owner, title, true);
		this.title = title;
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());
		
		this.addWindowListener(new WindowAdapter() {			
			public void windowActivated(WindowEvent e) {
				onShow();
			}
		});
	}
	
	protected final void makeVisible() {
		validate();
		pack();
		setLocationRelativeTo(null); // center
	}

	protected void onShow() {
		if (jgm.JGlideMon.debug)
			java.util.logging.Logger.getLogger("jgm").fine("Showing dialog " + getClass().getName());
	}
}
