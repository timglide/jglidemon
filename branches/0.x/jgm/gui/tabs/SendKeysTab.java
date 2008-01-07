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

import jgm.JGlideMon;
import jgm.glider.*;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class SendKeysTab extends Tab
	implements ActionListener {
	static Logger log = Logger.getLogger(SendKeysTab.class.getName());
	
	private JLabel toLbl;
	public  JComboBox type;
	public  JTextField to;
	public  JTextField keys;
	public JButton send;
	public JButton reset;
	private JButton clear;
	
	private Conn conn;
	
	private volatile boolean connected = false;
	
	public SendKeysTab() {
		super(new BorderLayout(), "Keys");

		JPanel keysPanel = new JPanel(new GridBagLayout());
		
		c.weightx = 0.0; c.weighty = 0.0;
		keysPanel.add(new JLabel("Type "), c);
		
		toLbl = new JLabel("To                           ");
		c.gridx++;
		keysPanel.add(toLbl, c);
		
		c.gridx++; c.weightx = 1.0;
		keysPanel.add(new JLabel("Keys"), c);
		
		String[] ss = {"Whisper", "Say", "Raw"};
		type = new JComboBox(ss);
		type.addActionListener(this);
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		keysPanel.add(type, c);
		
		to = new JTextField();
		c.gridx++;
		keysPanel.add(to, c);
		
		keys = new JTextField();
		c.gridx++; c.weightx = 1.0;
		keysPanel.add(keys, c);
				
		JPanel btns = new JPanel(new GridLayout(1, 0));
		
		send = new JButton("Send Keys");
		send.addActionListener(this);
		btns.add(send);
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		btns.add(reset);
		
		clear = new JButton("Clear Queue");
		send.addActionListener(this);
		btns.add(clear);
		
		c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 3;
		keysPanel.add(btns, c);
		
		c.gridy++;
		keysPanel.add(new JLabel(
			"<html>Whisper and Say will both add the slash command and a carriage return.<br>" +
			"You must add everything for Raw, | = CR, #VK# = VK</html>",
			JLabel.CENTER
		), c);
		
		add(keysPanel, BorderLayout.NORTH);

		setEnabled(false);
		
		validate();
		
		Connector.addListener(new ConnectionAdapter() {
			public void connectionEstablished() {
				connected = true;
				setEnabled(true);
			}
			
			public void disconnecting() {
				connected = false;
				setEnabled(false);
			}
		});
	}
	
	public void resetFields() {
		to.setText("");
		keys.setText("");
	}
	
	public void setEnabled(boolean b) {
		type.setEnabled(b);
		to.setEnabled(b);
		keys.setEnabled(b);
		send.setEnabled(b);
		reset.setEnabled(b);
		clear.setEnabled(b);
	}
	
	public boolean isEnabled() {
		return type.isEnabled();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		Object source = e.getSource();
		
		if (source == type) {
			String selected =
				(String) type.getSelectedItem();
			
			boolean test = selected.equals("Whisper");
			toLbl.setVisible(test);
			to.setVisible(test);
		} else if (source instanceof JButton) {			
			if (source == send) {
				if (!isEnabled()) return;
				
				setEnabled(false);
				StringBuffer sb = new StringBuffer();
				
				String t = (String) type.getSelectedItem();
				
				if (t.equals("Whisper")) {
					if (to.getText().trim().equals("")) {
						setEnabled(true);
						return;
					}
					
					sb.append("/w " + to.getText() + " ");
				} else if (t.equals("Say")) {
					sb.append("/s ");
				}
				
				if (keys.getText().trim().equals("")) {
					setEnabled(true);
					return;
				}
				
				sb.append(keys.getText());
				
				if (t.equals("Whisper") || t.equals("Say")) {
					sb.append('|');
				}
				
				try {
					log.fine("Sending: " + sb.toString());
					conn.send("/queuekeys " + sb.toString());
					conn.readLine(); // queued keys
					conn.readLine(); // ---
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				resetFields();
				setEnabled(true);
			} else if (source == reset) {
				resetFields();
			} else if (source == clear) {
				try {
					log.fine("Clearing key queue");
					conn.send("/clearsay");
					conn.readLine(); // status
					conn.readLine(); // ---
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				resetFields();
				setEnabled(true);
			}
		}
	}
}
