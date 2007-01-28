package jgm.gui.panes;

import jgm.JGlideMon;
import jgm.glider.*;
import jgm.gui.updaters.StatusUpdater;

import java.awt.event.*;
import javax.swing.*;

public class ControlPane extends Pane implements ActionListener {
	private static GliderConn conn;
	
	private JButton connect;
	private JButton attach;
	private JButton start;
	private JButton stop;

	public ControlPane() {
		super();

		connect = new JButton("Connect");
		connect.addActionListener(this);
		//connect.setEnabled(false);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		add(connect, c);
		
		attach = new JButton("Attach");
		attach.addActionListener(this);
		attach.setEnabled(false);
		c.gridy++;
		add(attach, c);

		start = new JButton("Start Glide");
		start.addActionListener(this);
		start.setEnabled(false);
		c.gridy++; c.gridwidth = 1;
		add(start, c);
		
		stop = new JButton("Stop Glide");
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
			attach.setEnabled(true);
			start.setEnabled(false);
			stop.setEnabled(false);
		}
		
		//System.out.println(Connector.state);
		switch (Connector.state) {
			case CONNECTED:
			case DISCONNECTED:
				connect.setEnabled(true);
				break;
				
			default:
				connect.setEnabled(false);
				break;
		}
		
		if (Connector.isConnected()) {
			connect.setText("Disconnect");
		} else {
			connect.setText("Connect");
			attach.setEnabled(false);
			start.setEnabled(false);
			stop.setEnabled(false);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		
		String cmd = e.getActionCommand();
		String s = null;
		System.out.println("Cmd: " + cmd);
		if (cmd.equals("Attach")) {
			s = "/attach";
		} else if (cmd.equals("Start Glide")) {
			s = "/startglide";
		} else if (cmd.equals("Stop Glide")) {
			s = "/stopglide";
		} else if (cmd.equals("Disconnect")) {
			connect.setEnabled(false);
			Connector.disconnect();
		} else if (cmd.equals("Connect")) {
			connect.setEnabled(false);
			Connector.connect();
		}
		
		if (s != null) {
			System.out.println("Sending: " + s);
			
			try {
				conn.send(s);
				System.out.println(conn.readLine()); // status
				conn.readLine(); // ---
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}
}
