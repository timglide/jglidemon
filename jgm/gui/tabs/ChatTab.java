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

import jgm.glider.*;
import jgm.glider.log.*;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;

import javax.swing.*;

public class ChatTab extends Tab implements ActionListener, Clearable {
	static Logger log = Logger.getLogger(ChatTab.class.getName());
	
	private JTabbedPane tabs;
	public LogTab all;
	public LogTab pub;
	public LogTab whisper;
	public LogTab guild;
	
	public  JComboBox type;
	public  JTextField to;
	public  JTextField keys;
	public  JButton send;
	public  JButton reset;
	
	public ChatTab(jgm.gui.GUI gui) {
		super(gui, new BorderLayout(), "Chat");
		
		tabs = new JTabbedPane();
		all = new LogTab(gui, "All Chat", tabs);
		pub = new LogTab(gui, "Public Chat", tabs);
		whisper = new LogTab(gui, "Whisper/Say/Yell", tabs);
		guild = new LogTab(gui, "Guild", tabs);
		
		addTab(all);
		addTab(pub);
		addTab(whisper);
		addTab(guild);
		
		
		// set up send keys
		JPanel keysPanel = new JPanel(new GridBagLayout());
		
		type = new JComboBox(ChatType.values());
		type.addActionListener(this);
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		keysPanel.add(type, c);
		
		to = new JTextField();
		to.setToolTipText("The person to send a whisper to");
		c.gridx++; c.weightx = 0.15;
		keysPanel.add(to, c);
		
		keys = new JTextField();
		keys.setToolTipText("<html>Slash command and a carriage return will be added except for Raw.<br>" +
		                    "You must add everything for Raw, | = CR, #VK# = VK");
		keys.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		keysPanel.add(keys, c);
				
		JPanel btns = new JPanel(new GridLayout(1, 0));
		
		send = new JButton("Send");
		send.addActionListener(this);
		btns.add(send);
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		btns.add(reset);
		
		c.gridx++; c.weightx = 0.0;
		keysPanel.add(btns, c);
		
/*		c.gridy++;
		keysPanel.add(new JLabel(
			"<html>Whisper and Say will both add the slash command and a carriage return.<br>" +
			"You must add everything for Raw, | = CR, #VK# = VK</html>",
			JLabel.CENTER
		), c);
*/
		
		setEnabled(false);
		
		gui.sm.connector.addListener(new ConnectionAdapter() {
			public void onConnect() {
				setEnabled(true);
			}
			
			public void onDisconnecting() {
				setEnabled(false);
			}
		});
		
		
		add(tabs, BorderLayout.CENTER);
		add(keysPanel, BorderLayout.SOUTH);
		
		validate();
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t.name, t);
	}
	
	public void add(ChatLogEntry e) {
		all.add(e);
		
		String channel = e.getChannel();
		if (channel == null) return;
		
		if (channel.equals("Whisper") || channel.equals("Say") || channel.equals("Yell")) {
			whisper.add(e);
		} else if (channel.equals("Guild") || channel.equals("Officer")) {
			guild.add(e);
		} else if (e.getType().equals("Public Chat")) {
			pub.add(e);
		}
	}
	
	public void clear(boolean clearingAll) {
		if (!clearingAll) {
			((Clearable) tabs.getSelectedComponent()).clear(false);
		} else {
			for (int i = 0; i < tabs.getComponentCount(); i++) {
				((Clearable) tabs.getComponentAt(i)).clear(true);
			}
		}
	}
	
	
	// send keys related stuff
	
	public void resetFields() {
//		to.setText("");
		keys.setText("");
	}
	
	public void setEnabled(boolean b) {
		type.setEnabled(b);
		to.setEnabled(b);
		keys.setEnabled(b);
		send.setEnabled(b);
		reset.setEnabled(b);
	}
	
	public boolean isEnabled() {
		return type.isEnabled();
	}
	
	public void update(Status s) {
		if (s.attached != isEnabled()) {
			setEnabled(s.attached);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == type) {
			ChatType selected =
				(ChatType) type.getSelectedItem();
			
			boolean test = selected.equals(ChatType.WHISPER);
			to.setVisible(test);
			this.revalidate();
		} else if (source == send || source == keys) {
			if (!isEnabled()) return;
				
			setEnabled(false);
			StringBuilder sb = new StringBuilder();
			
			ChatType t = (ChatType) type.getSelectedItem();
			
			switch (t) {
				case RAW:
					break;
				
				default:
					sb.append("#13#"); // press enter first
			}
			
			sb.append(t.getSlashCommand());
			
			if (t == ChatType.WHISPER) {
				if (to.getText().trim().equals("")) {
					setEnabled(true);
					return;
				}
				
				sb.append(to.getText());
				sb.append(' ');
			}
			
			if (keys.getText().trim().equals("")) {
				setEnabled(true);
				return;
			}
			
			sb.append(keys.getText());
			
			switch (t) {
				case RAW: break;
				default:
					sb.append("#13#");
			}

			String keys = sb.toString();
			log.fine("Queuing keys: " + keys);
			gui.sm.cmd.add(Command.getChatCommand(keys));
			
			resetFields();
			setEnabled(true);
		} else if (source == reset) {
			resetFields();
		}
	}
}
