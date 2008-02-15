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
package jgm;

import jgm.glider.Profile;

import java.io.*;
import javax.swing.*;

public class Profiler {
	public static final String app = "JGlideMon Profiler";
	
	public static void main(String[] args) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Coultn'd set L&F: " + e.getMessage());
		}
		
		int ret = JOptionPane.showConfirmDialog(
			null,
			"Browse to where Glider.exe is and your profile names will be\n" +
			"saved to \"profiles.dat\" in the current directory.\n\n" +
			"Copy this file to the same directory as JGlideMon.jar on\n" +
			"the remote computer you intend to run JGlideMon on.",
			app,
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE
		);
		
		if (ret != JOptionPane.OK_OPTION) {
			System.out.println("User canceled, exiting.");
			System.exit(0);
		}
		
		JFileChooser fc = new JFileChooser();
		
		String progFiles = System.getenv("ProgramFiles");
		
		if (progFiles != null && !progFiles.equals("")) {
			fc.setCurrentDirectory(new File(progFiles));
		}
		
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public String getDescription() {
				return "Glider.exe";
			}
			
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().equals("glider.exe");
			}
		});
		fc.setAcceptAllFileFilterUsed(false);
		
		ret = fc.showDialog(null, "Select Glider");
		
		switch (ret) {
			case JFileChooser.CANCEL_OPTION:
				System.out.println("User canceled, exiting.");
				System.exit(0);
				break;
				
			case JFileChooser.APPROVE_OPTION:
				System.out.println("User selected a file.");
				break;
				
			case JFileChooser.ERROR_OPTION:
			default:
				System.err.println("Error returned from file chooser!");
				System.exit(1);
				break;
		}
		
		File f = fc.getSelectedFile();
		System.out.println("Selected: " + f);
		
		if (!f.getName().toLowerCase().equals("glider.exe")) {
			JOptionPane.showMessageDialog(
				null,
				"You have selected a file other than Glider.exe.\n" +
				"Run this program again and select Glider.exe.",
				"Invalid File",
				JOptionPane.ERROR_MESSAGE
			);
			System.exit(1);
		}
		
		f = f.getParentFile();
		File groups = new File(f, "groups");
		File profiles = new File(f, "profiles");
		
		doDir(groups);
		doDir(profiles);
		
		Profile.Cache.saveProfiles();
		
		JOptionPane.showMessageDialog(
			null,
			"Profiles saved to \"" + Profile.Cache.profileFile.getName() + "\" successfully.\n\n" +
			"Copy this file to the folder where JGlideMon.jar\n" +
			"is located on the remote computer.",
			app,
			JOptionPane.INFORMATION_MESSAGE
		);
		
		/*for (Profile p : Profile.root.children) {
			System.out.println(p.toFullString());
			
			for (Profile p2 : p.children) {
				System.out.println(p2.toFullString());
			}
		}
		
		JFrame frame = new JFrame("Profiles");
		frame.setPreferredSize(new Dimension(700, 400));
		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(new jgm.gui.components.ProfileTree(), BorderLayout.CENTER);
		frame.pack();
		frame.validate();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		*/
	}
	
	private static FileFilter ff = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".xml");
		}
	};
	
	private static void doDir(File f) {
		doDir(f, Profile.root);
	}
	
	private static void doDir(File f, Profile parent) {
		if (!f.isDirectory() || !f.canRead()) return;
		
		File[] children = f.listFiles(ff);
		
		if (children.length == 0) return;
		
		parent = Profile.createNode(parent, f.getName());
		
		for (File child : children) {
			if (child.isDirectory()) {
				doDir(child, parent);
			} else {
				Profile.createLeaf(parent, child.getName());
			}
		}
	}
}
