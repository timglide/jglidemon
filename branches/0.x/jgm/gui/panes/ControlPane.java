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

import jgm.glider.*;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ControlPane extends Pane implements ActionListener, ConnectionListener {
	static Logger log = Logger.getLogger(ControlPane.class.getName());
	
	private static Conn conn;
	
	public final JButton connect;
	public final JButton start;
	public final JButton stop;

	public final JButton restore;
	public final JButton shrink;
	public final JButton hide;
	
	public ControlPane(jgm.gui.GUI gui) {
		super(gui);

		connect = new JButton("Connect");
		connect.setFocusable(false);
		connect.addActionListener(this);
		//connect.setEnabled(false);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 6;
		c.weightx = 0.0; c.fill = GridBagConstraints.HORIZONTAL;
		c.insets.bottom = jgm.gui.GUI.PADDING / 2;
		add(connect, c);

		start = new JButton("Start Glide");
		start.setFocusable(false);
		start.addActionListener(this);
		c.gridy++; c.gridwidth = 3;
		c.insets.right = c.insets.bottom;
		add(start, c);
		
		stop = new JButton("Stop Glide");
		stop.setFocusable(false);
		stop.addActionListener(this);
		c.gridx += 3;
		c.insets.right = 0;
		add(stop, c);
		
		restore = new JButton("Restore");
		restore.setToolTipText("Restore the WoW window for this connection");
		restore.setFocusable(false);
		restore.addActionListener(this);
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		c.insets.right = c.insets.bottom;
		c.insets.bottom = 0;
		add(restore, c);
		
		shrink = new JButton("Shrink");
		shrink.setToolTipText("Shrink the WoW window for this connection");
		shrink.setFocusable(false);
		shrink.addActionListener(this);
		c.gridx += 2;
		add(shrink, c);
		
		hide = new JButton("Hide");
		hide.setToolTipText("Hide the WoW window for this connection");
		hide.setFocusable(false);
		hide.addActionListener(this);
		c.gridx += 2;
		c.insets.right = 0;
		add(hide, c);
		
		setEnabled(false);
	}

	public void update(Status s) {
		//System.out.println("ControlPane.update()");
		
		setEnabled(s.attached);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = gui.sm.keysConn;
		
		Object src = e.getSource();
		String cmd = e.getActionCommand();
		Command c = null;
		
		//System.out.println("Cmd: " + cmd);
		if (src == start) {
			c = Command.getStartCommand();
		} else if (src == stop) {
			c = Command.getStopCommand();
		} else if (src == connect) {
			if (cmd.equals("Disconnect")) {
				connect.setEnabled(false);
				gui.sm.connector.disconnect(true);
			} else if (cmd.equals("Connect")) {
				connect.setEnabled(false);
				gui.sm.connector.connect(true);
			}
		} else if (src == restore) {
			gui.menu.ssRestoreActivate.doClick();
		} else if (src == shrink) {
			gui.menu.ssShrink.doClick();
		} else if (src == hide) {
			gui.menu.ssHide.doClick();
		}
		
		if (c != null) {
			log.fine("Sending: " + c);
			gui.sm.cmd.add(c);
		}
	}
	
	public void setEnabled(boolean b) {		
//		connect.setEnabled(b);
		start.setEnabled(b);
		stop.setEnabled(b);
		restore.setEnabled(b);
		shrink.setEnabled(b);
		hide.setEnabled(b);
	}
	
	public Conn getConn() {
		return null;
	}
	
	public void onConnect() {
		connect.setText("Disconnect");
		connect.setEnabled(true);
	}
	
	public void onDisconnect() {
		connect.setText("Connect");
		connect.setEnabled(true);
		setEnabled(false);
	}
	
	public void onConnecting() {
		connect.setText("Connecting...");
		connect.setEnabled(false);
	}
	
	public void onDisconnecting() {
		connect.setText("Disconnecting...");
		connect.setEnabled(false);
		setEnabled(false);
	}
	
	public void onDestroy() {}
}
