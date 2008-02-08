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
import java.awt.event.*;
import javax.swing.*;

public class ControlPane extends Pane implements ActionListener, ConnectionListener {
	static Logger log = Logger.getLogger(ControlPane.class.getName());
	
	private static Conn conn;
	
	public final JButton connect;
//	public final JButton attach;
	public final JButton start;
	public final JButton stop;

	public ControlPane(jgm.GUI gui) {
		super(gui);

		connect = new JButton("Connect");
		connect.setFocusable(false);
		connect.addActionListener(this);
		//connect.setEnabled(false);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		c.insets.bottom = jgm.GUI.PADDING / 2;
		add(connect, c);
		
		// attach is done automatically now
//		attach = new JButton("Attach");
//		attach.setFocusable(false);
//		attach.addActionListener(this);
//		attach.setEnabled(false);
//		c.gridy++;
//		add(attach, c);

		start = new JButton("Start Glide");
		start.setFocusable(false);
		start.addActionListener(this);
		start.setEnabled(false);
		c.gridy++; c.gridwidth = 1;
		c.insets.right = c.insets.bottom;
		c.insets.bottom = 0;
		add(start, c);
		
		stop = new JButton("Stop Glide");
		stop.setFocusable(false);
		stop.addActionListener(this);
		stop.setEnabled(false);
		c.gridx++;
		c.insets.right = 0;
		add(stop, c);
	}

	public void update(Status s) {
		//System.out.println("ControlPane.update()");
		
		if (s.attached) {
//			attach.setEnabled(false);
			start.setEnabled(true);
			stop.setEnabled(true);
		} else {
//			if (Connector.isConnected())
//				attach.setEnabled(true);
			start.setEnabled(false);
			stop.setEnabled(false);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = gui.sm.keysConn;
		
		String cmd = e.getActionCommand();
		String s = null;
		//System.out.println("Cmd: " + cmd);
		if (cmd.equals("Attach")) {
			s = "/attach";
		} else if (cmd.equals("Start Glide")) {
			s = "/startglide";
		} else if (cmd.equals("Stop Glide")) {
			s = "/stopglide";
		} else if (cmd.equals("Disconnect")) {
			connect.setEnabled(false);
			gui.sm.connector.disconnect(true);
		} else if (cmd.equals("Connect")) {
			connect.setEnabled(false);
			gui.sm.connector.connect(true);
		}
		
		if (s != null) {
			log.fine("Sending: " + s);
			
			try {
				conn.send(s);
				log.fine(conn.readLine()); // status
				conn.readLine(); // ---
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
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
//		attach.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
	}
	
	public void onConnecting() {
		connect.setText("Connecting...");
		connect.setEnabled(false);
	}
	
	public void onDisconnecting() {
		connect.setText("Disconnecting...");
		connect.setEnabled(false);
//		attach.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
	}
}
