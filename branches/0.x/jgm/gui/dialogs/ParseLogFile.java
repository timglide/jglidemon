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
package jgm.gui.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

import java.util.logging.*;

import jgm.glider.log.LogFile;

public class ParseLogFile extends Dialog implements ActionListener {
	JPanel mainPanel;
	JPanel waitPanel;
	
	JComboBox type;
	JTextField fileText;
	JButton browse;
	JButton parse;
	JButton close;
	
	java.io.File selectedFile = null;
	
	JFileChooser fc = new JFileChooser();
	
	public ParseLogFile(jgm.GUI gui) {
		super(gui, "Parse Log File");
		
		mainPanel = new JPanel(new BorderLayout());
		JLabel lbl = new JLabel("Please select the log type, browse to the log file, and click Parse.", JLabel.CENTER);
		mainPanel.add(lbl, BorderLayout.NORTH);
		
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(PADDING, 0, PADDING, 0);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.gridwidth = 1;
		jp.add(new JLabel("Log Type"), c);
		
		type = new JComboBox(new Object[]{LogFile.Chat, LogFile.Combat});
		c.gridx++; c.weightx = 1.0; c.gridwidth = 2;
		c.insets.left = PADDING;
		jp.add(type, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0; c.gridwidth = 1;
		c.insets.top = 0; c.insets.left = 0;
		jp.add(new JLabel("Log File"), c);
		
		fileText = new JTextField();
		fileText.setEnabled(false);
		c.gridx++; c.weightx = 1.0;
		c.insets.bottom = 0; c.insets.left = PADDING;
		jp.add(fileText, c);
		
		browse = new JButton("...");
		browse.setToolTipText("Brose for log file");
		browse.addActionListener(this);
		c.gridx++; c.weightx = 0.0;
		jp.add(browse, c);
		
		mainPanel.add(jp, BorderLayout.CENTER);
		
		
		parse = new JButton("Parse");
		parse.setMnemonic(KeyEvent.VK_P);
		parse.addActionListener(this);
		
		close = new JButton("Cancel");
		close.setMnemonic(KeyEvent.VK_C);
		close.addActionListener(this);
		
		mainPanel.add(Dialog.makeNiceButtons(parse, close), BorderLayout.SOUTH);
		
		
		waitPanel = new JPanel(new BorderLayout());
		lbl = new JLabel("Please wait...");
		lbl.setHorizontalAlignment(JLabel.CENTER);
		lbl.setVerticalAlignment(JLabel.CENTER);
		waitPanel.add(lbl, BorderLayout.CENTER);
		
		
		
		makeVisible();
	}
	
	protected void onShow() {
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		makeVisible();
	}
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src == close) {
			setVisible(false);
		} else if (src == browse) {
			if (selectedFile == null) {
				String progFiles = System.getenv("ProgramFiles");
				
				if (progFiles != null && !progFiles.equals("")) {
					fc.setCurrentDirectory(new File(progFiles));
				}
			}
			// else the fc should stay in the last visited directory
			
			fc.setMultiSelectionEnabled(false);
			fc.setAcceptAllFileFilterUsed(true);
			fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public String getDescription() {
					return "Log File";
				}
				
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase().endsWith(".log");
				}
			});
			
			int ret = fc.showOpenDialog(this);
			
			switch (ret) {
				case JFileChooser.CANCEL_OPTION:
					return;
					
				case JFileChooser.APPROVE_OPTION:
					selectedFile = fc.getSelectedFile();
					
					try {
						fileText.setText(selectedFile.getCanonicalPath());
					} catch (java.io.IOException ex) {}
					
					// try to help the user out
					String str = selectedFile.getName().toLowerCase();
					if (str.contains("chat")) {
						type.setSelectedItem(LogFile.Chat);
					} else if (str.contains("combat")) {
						type.setSelectedItem(LogFile.Combat);
					}
					break;
					
				case JFileChooser.ERROR_OPTION:
				default:
					JOptionPane.showMessageDialog(this,
						"An internal error has occured.",
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
			}
		} else if (src == parse) {
			if (selectedFile == null) {
				JOptionPane.showMessageDialog(this,
					"You must select a log file to parse first.",
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			new Thread(new Runnable() {
				public void run() {
					ParseLogFile.this.getContentPane().add(waitPanel, BorderLayout.CENTER);
					ParseLogFile.this.validate();
					
					try {
						jgm.JGlideMon.getCurManager().logUpdater.parseFile(
							selectedFile,
							(jgm.glider.log.LogFile) type.getSelectedItem());
					} catch (Throwable t) {
						Logger.getLogger(getClass().getName())
							.log(Level.WARNING, "Error parsing log file", t);
						
						JOptionPane.showMessageDialog(ParseLogFile.this,
							"There was an error parsing the log file.\n\n" +
							t.getClass().getName() + ": " + t.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					}
					
					ParseLogFile.this.setVisible(false);
				}
			}).start();
		}
	}
}
