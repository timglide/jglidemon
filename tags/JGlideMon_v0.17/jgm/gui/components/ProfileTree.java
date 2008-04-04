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

import jgm.glider.Profile;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class ProfileTree extends JPanel {
	private JTree tree;
	
	public ProfileTree() {
		tree = new JTree(Profile.root);
		tree.setDoubleBuffered(true);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setLargeModel(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(tree);
		add(sp, BorderLayout.CENTER);
	}
	
	public void reloadProfiles() throws Throwable {
		Profile.Cache.loadProfiles();
		tree.setModel(Profile.root);
	}
	
	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		tree.addTreeSelectionListener(tsl);
	}
	
	public Profile getSelectedProfile() {
		return (Profile) tree.getSelectionPath().getLastPathComponent();
	}
	
	// fake a selection change
	public Profile getSelected() {
		if (tree.getSelectionCount() < 1) return null;
		return (Profile) tree.getSelectionPath().getLastPathComponent();
	}
}
