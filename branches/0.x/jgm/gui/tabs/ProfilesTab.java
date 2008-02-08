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

import jgm.gui.components.*;
import jgm.glider.*;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ProfilesTab extends Tab
	implements ActionListener, TreeSelectionListener {
	static Logger log = Logger.getLogger(ProfilesTab.class.getName());
	
	private ProfileTree profiles;
	private JButton loadProfile;
	private JButton refreshProfiles;
	private JButton manualLoad;
	
	private Conn conn;
	
	private volatile boolean connected = false;
	
	public ProfilesTab(jgm.GUI gui) {
		super(gui, new BorderLayout(), "Profiles");
						
		JPanel prosPanel = new JPanel(new BorderLayout(10, 10));
		
		profiles = new ProfileTree();
		profiles.addTreeSelectionListener(this);

		c = new GridBagConstraints();
		prosPanel.add(profiles, BorderLayout.CENTER);
		
		JPanel btns = new JPanel(new GridLayout(0, 1));
		
		loadProfile = new JButton("Load Selected Profile");
		loadProfile.setEnabled(false);
		loadProfile.addActionListener(this);
		btns.add(loadProfile);
		
		manualLoad = new JButton("Manually Enter Profile To Load");
		manualLoad.addActionListener(this);
		btns.add(manualLoad);
		
		refreshProfiles = new JButton("Reload Profile List from " + Profile.Cache.profileFile.getName());
		refreshProfiles.addActionListener(this);
		btns.add(refreshProfiles);
		
		prosPanel.add(btns, BorderLayout.SOUTH);
		
		add(prosPanel, BorderLayout.CENTER);
		
		setEnabled(false);
		
		validate();
		
		gui.sm.connector.addListener(new ConnectionAdapter() {
			public void onConnect() {
				connected = true;
				setEnabled(true);
			}
			
			public void onDisconnecting() {
				connected = false;
				setEnabled(false);
			}
		});
	}
	
	public void resetFields() {

	}
	
	public void setEnabled(boolean b) {		
		// should only be enabled upon selection of
		// a leaf node
		Profile p = profiles.getSelected();
		
		if (p != null) {
			loadProfile.setEnabled(b && p.isLeaf());
		} else {
			loadProfile.setEnabled(false);
		}
		
		manualLoad.setEnabled(b);
	}
	
	public boolean isEnabled() {
		return loadProfile.isEnabled();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = gui.sm.keysConn;
		Object source = e.getSource();
		
		if (source instanceof JButton) {			
			if (source == loadProfile) {				
				Profile p = profiles.getSelectedProfile();
				loadProfile(p.toFullString());
			} else if (source == manualLoad) {
				String path =
					JOptionPane.showInputDialog(
						gui.frame,
						"Enter full profile path to load:",
						"Load Profile",
						JOptionPane.QUESTION_MESSAGE
					);
				
				if (path == null) return;
				
				loadProfile(path);
			} else if (source == refreshProfiles) {
				try {
					profiles.reloadProfiles();
				} catch (Throwable x) {
					String extra = "";
					
					if (x instanceof java.io.InvalidClassException) {
						extra = "\n\nYou may have to run Profiler.jar and copy " + Profile.Cache.profileFile.getName() + " again.";
					} else if (x instanceof java.io.FileNotFoundException) {
						extra = "\n\nEnsure you've copied " + Profile.Cache.profileFile.getName() + " to the directory\n" +
								"where JGlideMon.jar is located, which appears to be\n" +
								(new java.io.File("")).getAbsolutePath();
					}
					
					JOptionPane.showMessageDialog(
						gui.frame, 
						"There was an error loading the profiles:\n\n" +
						x.getClass().getName() + "\n" +
						x.getMessage() + extra,
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		Profile p = (Profile) e.getPath().getLastPathComponent();
		loadProfile.setEnabled(p.isLeaf() && connected);
	}
	
	public void loadProfile(String path) {
		if (path.trim().equals("")) {
			JOptionPane.showMessageDialog(
				gui.frame,
				"You must enter a profile to load.",
				"Error",
				JOptionPane.ERROR_MESSAGE
			);
			
			return;
		}
		
		int groupIndex = path.indexOf("groups\\");
		int profileIndex = path.indexOf("profiles\\");
		
		// glider seems to load the profile relative to
		// the profiles folder instead of the folder where
		// glider.exe is when given a relative path
		if (groupIndex == 0) {
			path = "..\\" + path;
		} else if (profileIndex == 0) {
			path = path.substring(9);
		}
		
		try {
			log.fine("Loading profile: " + path);
			conn.send("/loadprofile " + path);
			String ret = conn.readLine(); // queued keys
			log.fine(ret);
			conn.readLine(); // ---
			
			if (ret.toLowerCase().contains("failed")) {
				JOptionPane.showMessageDialog(
					gui.frame,
					ret,
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			} else if (ret.contains("ok")) {
				JOptionPane.showMessageDialog(
					gui.frame,
					ret + ".\nDon't forget to start gliding.",
					"Profile Loaded",
					JOptionPane.INFORMATION_MESSAGE
				);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
