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
	
	public void reloadProfiles() {
		Profile.Cache.loadProfiles();
		tree.setModel(Profile.root);
	}
	
	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		tree.addTreeSelectionListener(tsl);
	}
	
	public Profile getSelectedProfile() {
		return (Profile) tree.getSelectionPath().getLastPathComponent();
	}
}
