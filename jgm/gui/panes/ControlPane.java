package jgm.gui.panes;

import jgm.JGlideMon;
import jgm.glider.*;
import jgm.gui.updaters.StatusUpdater;

import java.util.logging.*;
import java.awt.event.*;
import javax.swing.*;

public class ControlPane extends Pane implements ActionListener, ConnectionListener {
	static Logger log = Logger.getLogger(ControlPane.class.getName());
	
	private static Conn conn;
	
	public JButton connect;
	private JButton attach;
	private JButton start;
	private JButton stop;

	public ControlPane() {
		super();

		connect = new JButton("Connect");
		connect.setFocusable(false);
		connect.addActionListener(this);
		//connect.setEnabled(false);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		add(connect, c);
		
		attach = new JButton("Attach");
		attach.setFocusable(false);
		attach.addActionListener(this);
		attach.setEnabled(false);
		c.gridy++;
		add(attach, c);

		start = new JButton("Start Glide");
		start.setFocusable(false);
		start.addActionListener(this);
		start.setEnabled(false);
		c.gridy++; c.gridwidth = 1;
		add(start, c);
		
		stop = new JButton("Stop Glide");
		stop.setFocusable(false);
		stop.addActionListener(this);
		stop.setEnabled(false);
		c.gridx++;
		add(stop, c);
	}

	public void update(StatusUpdater s) {
		//System.out.println("ControlPane.update()");
		
		if (s.attached) {
			attach.setEnabled(false);
			start.setEnabled(true);
			stop.setEnabled(true);
		} else {
			if (Connector.isConnected())
				attach.setEnabled(true);
			start.setEnabled(false);
			stop.setEnabled(false);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		
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
			Connector.disconnect(true);
		} else if (cmd.equals("Connect")) {
			connect.setEnabled(false);
			Connector.connect(true);
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
	
	public void connectionEstablished() {
		connect.setText("Disconnect");
		connect.setEnabled(true);
	}
	
	public void connectionDied() {
		connect.setText("Connect");
		connect.setEnabled(true);
		attach.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
	}
	
	public void connecting() {
		connect.setText("Connecting...");
		connect.setEnabled(false);
	}
	
	public void disconnecting() {
		connect.setText("Disconnecting...");
		connect.setEnabled(false);
		attach.setEnabled(false);
		start.setEnabled(false);
		stop.setEnabled(false);
	}
}
