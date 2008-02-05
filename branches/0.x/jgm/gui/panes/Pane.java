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
package jgm.gui.panes;

import java.awt.*;
import javax.swing.*;

import jgm.glider.Status;

/**
 * Abstract class representing one of the panels 
 * in the GUI.
 * @author Tim
 * @since 0.1
 */
public abstract class Pane extends JPanel {
	protected GridBagConstraints c = null;

	/**
	 * Create a new Pane with the specified LayoutManager.
	 * @param lm The LayoutManager to use
	 */
	public Pane(LayoutManager lm) {
		setLayout(lm);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0;
	}

	/**
	 * Create a new Pane with a GridBagLayout.
	 */
	public Pane() {
		this(new GridBagLayout());
	}

	/**
	 * This method will be called when the StatusUpdater
	 * has been updated.
	 * @param s The StatusUpdater with updated information
	 */
	public void update(Status s) {}
}
