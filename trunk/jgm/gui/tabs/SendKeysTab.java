package jgm.gui.tabs;

import jgm.JGlideMon;
import jgm.gui.components.*;
import jgm.glider.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class SendKeysTab extends Tab
	implements ActionListener, TreeSelectionListener {	
	private JLabel toLbl;
	public  JComboBox type;
	public  JTextField to;
	public  JTextField keys;
	private JButton send;
	private JButton reset;
	private JButton clear;
	
	private ProfileTree profiles;
	private JButton loadProfile;
	private JButton refreshProfiles;
	private JButton manualLoad;
	
	private Conn conn;
	
	private volatile boolean connected = false;
	
	public SendKeysTab() {
		super(new BorderLayout(), "Keys/Profiles");

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
		
		send = new JButton("Send Keys");
		send.addActionListener(this);
		c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 3;
		keysPanel.add(send, c);
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		c.gridy++;
		keysPanel.add(reset, c);
		
		clear = new JButton("Clear Queue");
		send.addActionListener(this);
		c.gridy++;
		keysPanel.add(clear, c);
		
		c.gridy++;
		keysPanel.add(new JLabel(
			"<html>Whisper and Say will both add the slash command and a carriage return.<br>" +
			"You must add everything for Raw, | = CR, #VK# = VK</html>",
			JLabel.CENTER
		), c);
		
		jgm.GUI.setTitleBorder(keysPanel, "Send Keys");
						
		JPanel prosPanel = new JPanel(new BorderLayout(10, 10));
		
		profiles = new ProfileTree();
		profiles.addTreeSelectionListener(this);

		c = new GridBagConstraints();
		prosPanel.add(profiles, BorderLayout.CENTER);
		
		JPanel btns = new JPanel(new GridLayout(0, 1));
		
		loadProfile = new JButton("Load Selected Profile");
		loadProfile.setEnabled(false);
		loadProfile.addActionListener(this);
		btns.add(loadProfile);
		
		manualLoad = new JButton("Manually Enter Profile To Load");
		manualLoad.addActionListener(this);
		btns.add(manualLoad);
		
		refreshProfiles = new JButton("Reload Profile List from " + Profile.Cache.profileFile.getName());
		refreshProfiles.addActionListener(this);
		btns.add(refreshProfiles);
		
		prosPanel.add(btns, BorderLayout.SOUTH);
		
		jgm.GUI.setTitleBorder(prosPanel, "Load Profile");

		add(keysPanel, BorderLayout.NORTH);
		add(prosPanel, BorderLayout.CENTER);
		
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
		
		// should only be enabled upon selection of
		// a leaf node
		Profile p = profiles.getSelected();
		
		if (p != null) {
			loadProfile.setEnabled(b && p.isLeaf());
		} else {
			loadProfile.setEnabled(false);
		}
		
		manualLoad.setEnabled(b);
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
					System.out.println("Sending: " + sb.toString());
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
					System.out.println("Clearing key queue");
					conn.send("/clearsay");
					conn.readLine(); // status
					conn.readLine(); // ---
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				resetFields();
				setEnabled(true);
			} else if (source == loadProfile) {				
				Profile p = profiles.getSelectedProfile();
				loadProfile(p.toFullString());
			} else if (source == manualLoad) {
				String path =
					JOptionPane.showInputDialog(
						jgm.GUI.frame,
						"Enter full profile path to load:",
						"Load Profile",
						JOptionPane.QUESTION_MESSAGE
					);
				
				if (path == null) return;
				
				loadProfile(path);
			} else if (source == refreshProfiles) {
				try {
					profiles.reloadProfiles();
				} catch (Throwable x) {
					String extra = "";
					
					if (x instanceof java.io.InvalidClassException) {
						extra = "\n\nYou may have to run Profiler.jar and copy " + Profile.Cache.profileFile.getName() + " again.";
					} else if (x instanceof java.io.FileNotFoundException) {
						extra = "\n\nEnsure you've copied " + Profile.Cache.profileFile.getName() + " to the directory\n" +
								"where JGlideMon.jar is located, which appears to be\n" +
								(new java.io.File("")).getAbsolutePath();
					}
					
					JOptionPane.showMessageDialog(
						jgm.GUI.frame, 
						"There was an error loading the profiles:\n\n" +
						x.getClass().getName() + "\n" +
						x.getMessage() + extra,
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		Profile p = (Profile) e.getPath().getLastPathComponent();
		loadProfile.setEnabled(p.isLeaf() && connected);
	}
	
	public void loadProfile(String path) {
		if (path.trim().equals("")) {
			JOptionPane.showMessageDialog(
				jgm.GUI.frame,
				"You must enter a profile to load.",
				"Error",
				JOptionPane.ERROR_MESSAGE
			);
			
			return;
		}
		
		int groupIndex = path.indexOf("groups\\");
		int profileIndex = path.indexOf("profiles\\");
		
		// glider seems to load the profile relative to
		// the profiles folder instead of the folder where
		// glider.exe is when given a relative path
		if (groupIndex == 0) {
			path = "..\\" + path;
		} else if (profileIndex == 0) {
			path = path.substring(9);
		}
		
		try {
			System.out.println("Loading profile: " + path);
			conn.send("/loadprofile " + path);
			String ret = conn.readLine(); // queued keys
			System.out.println(ret);
			conn.readLine(); // ---
			
			if (ret.toLowerCase().contains("failed")) {
				JOptionPane.showMessageDialog(
					jgm.GUI.frame,
					ret,
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			} else if (ret.contains("ok")) {
				JOptionPane.showMessageDialog(
					jgm.GUI.frame,
					ret + ".\nDon't forget to start gliding.",
					"Profile Loaded",
					JOptionPane.INFORMATION_MESSAGE
				);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
