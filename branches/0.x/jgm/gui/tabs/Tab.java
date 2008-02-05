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
package jgm.gui.tabs;


import java.awt.*;
import javax.swing.*;

import jgm.glider.Status;

/**
 * Abstract class representing one of the main tabs.
 * @author Tim
 * @since 0.1
 */
public abstract class Tab extends jgm.gui.panes.Pane {
	public String name;

	/**
	 * Create a Tab with the specified name.
	 * @param s The name of the Tab
	 */
	public Tab(String s) {
		super();
		name = s;
	}

	/**
	 * Create a Tab with the specified name and LayoutManager.
	 * @param lm The LayoutManager to use
	 * @param s The name of the Tab
	 */
	public Tab(LayoutManager lm, String s) {
		super(lm);
		name = s;
	}

	public final boolean isFocusable() {
		return true;
	}
	
	/**
	 * Returns the index of this Tab.
	 * @return The index of this Tab
	 */
	public final int getIndex() {
		return ((JTabbedPane) this.getParent()).indexOfComponent(this);
	}
	
	/**
	 * Determines if this Tab is currently selected.
	 * @return Whether this Tab is currently selected
	 */
	public final boolean isCurrentTab() {
		return ((JTabbedPane) this.getParent()).getSelectedIndex()
				== getIndex();
	}
	
	public final void select() {
		((JTabbedPane) this.getParent()).setSelectedComponent(this);
	}
	
	/**
	 * It is optional for a Tab to implement this method
	 * for when the StatusUpdater has updated.
	 */
	public void update(Status s) {}
}
