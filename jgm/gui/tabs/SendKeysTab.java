package jgm.gui.tabs;

import jgm.JGlideMon;
import jgm.glider.GliderConn;

import java.awt.event.*;
import javax.swing.*;

public class SendKeysTab extends Tab implements ActionListener {	
	private JLabel toLbl;
	public  JComboBox type;
	public  JTextField to;
	public  JTextField keys;
	private JButton send;
	private JButton reset;
	private JButton clear;
	
	private GliderConn conn;
	
	public SendKeysTab() {
		super("Send Keys");
		
		c.weightx = 0.0;
		add(new JLabel("Type "), c);
		
		toLbl = new JLabel("To                           ");
		c.gridx++;
		add(toLbl, c);
		
		c.gridx++; c.weightx = 1.0;
		add(new JLabel("Keys"), c);
		
		String[] ss = {"Whisper", "Say", "Raw"};
		type = new JComboBox(ss);
		type.addActionListener(this);
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		add(type, c);
		
		to = new JTextField();
		c.gridx++;
		add(to, c);
		
		keys = new JTextField();
		c.gridx++; c.weightx = 1.0;
		add(keys, c);
		
		send = new JButton("Send Keys");
		send.addActionListener(this);
		c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 3;
		add(send, c);
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		c.gridy++;
		add(reset, c);
		
		clear = new JButton("Clear Queue");
		send.addActionListener(this);
		c.gridy++;
		add(clear, c);
		
		c.gridy++;
		add(new JLabel(
			"<html>Whisper and Say will both add the slash command and a carriage return.<br>" +
			"You must add everything for Raw, | = CR, #VK# = VK</html>",
			JLabel.CENTER
		), c);
		
		c.gridy++; c.weighty = 1.0;
		add(new JLabel(), c);
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
	}
	
	public boolean isEnabled() {
		return type.isEnabled();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		Object source = e.getSource();
		
		if (source instanceof JComboBox) {
			String selected =
				(String) ((JComboBox) source).getSelectedItem();
			
			boolean test = selected.equals("Whisper");
			toLbl.setVisible(test);
			to.setVisible(test);
		} else if (source instanceof JButton) {			
			JButton btn = (JButton) source;
			
			String s = btn.getText();
			
			if (s.equals("Send Keys")) {
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
				
				System.out.println("Sending: " + sb.toString());
				conn.send("/queuekeys " + sb.toString());
				conn.readLine(); // queued keys
				conn.readLine(); // ---
				
				resetFields();
				setEnabled(true);
			} else if (s.equals("Reset")) {
				resetFields();
			} else if (s.equals("Clear Queue")) {
				System.out.println("Clearing key queue");
				conn.send("/clearsay");
				conn.readLine(); // status
				conn.readLine(); // ---
				resetFields();
				setEnabled(true);
			}
		}
	}
}
