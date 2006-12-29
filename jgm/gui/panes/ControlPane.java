package jgm.gui.panes;

import jgm.JGlideMon;
import jgm.glider.GliderConn;
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
		connect.setEnabled(false);
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
		if (s.attached) {
			connect.setText("Disconnect");
			attach.setEnabled(false);
			start.setEnabled(true);
			stop.setEnabled(true);
		} else {
			attach.setEnabled(true);
			start.setEnabled(false);
			stop.setEnabled(false);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		
		String cmd = e.getActionCommand();
		String s = null;
		
		if (cmd.equals("Attach")) {
			s = "/attach";
		} else if (cmd.equals("Start Glide")) {
			s = "/startglide";
		} else if (cmd.equals("Stop Glide")) {
			s = "/stopglide";
		}
		
		if (s != null) {
			System.out.println("Sending: " + s);
			conn.send(s);
			System.out.println(conn.readLine()); // status
			conn.readLine(); // ---
		}
	}
}