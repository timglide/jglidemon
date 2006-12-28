package jgm.gui.tabs;

import jgm.cfg;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConfigTab extends Tab implements ActionListener {
	private JButton update;
	
	private JPanel net;
	private JTextField host;
	private JTextField port;
	private JTextField password;
	
	private JPanel status;
	private JTextField statusInterval;
	
	private JPanel screenshot;
	private JTextField screenshotInterval;
	private JSlider screenshotScale;
	
	public ConfigTab() {
		super(new BorderLayout(), "Config");
		
		update = new JButton("Save Settings");
		update.addActionListener(this);
		add(update, BorderLayout.NORTH);
		
		JPanel p = new JPanel(new GridLayout(1, 0, 10, 10));
		
		// net config pane
		net = new JPanel(new GridBagLayout());
		net.setBorder(
			BorderFactory.createTitledBorder("Network"));
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		net.add(new JLabel("Host: "), c);
		
		host = new JTextField(cfg.net.host);
		c.gridx++;
		net.add(host, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Port: " ), c);
		
		port = new JTextField(Integer.toString(cfg.net.port));
		c.gridx++;
		net.add(port, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Password: " ), c);
		
		password = new JTextField(cfg.net.password);
		c.gridx++;
		net.add(password, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		net.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(net);
		
		
		// status config pane
		status = new JPanel(new GridBagLayout());
		status.setBorder(
			BorderFactory.createTitledBorder("Status"));
		
		c.gridx = 0; c.gridy = 0;
		status.add(new JLabel("Refresh (ms): "), c);
		
		statusInterval = new JTextField(Integer.toString(cfg.status.updateInterval));
		c.gridx++;
		status.add(statusInterval, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		status.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(status);
		
		
		// screenshot panel
		screenshot = new JPanel(new GridBagLayout());
		screenshot.setBorder(
			BorderFactory.createTitledBorder("Screenshot"));
		
		c.gridx = 0; c.gridy = 0;
		screenshot.add(new JLabel("Refresh (ms): "), c);
		
		screenshotInterval = new JTextField(Integer.toString(cfg.screenshot.updateInterval));
		c.gridx++;
		screenshot.add(screenshotInterval, c);
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Scale: "), c);
		
		screenshotScale =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.screenshot.scale);
		screenshotScale.setMajorTickSpacing(30);
		screenshotScale.setMinorTickSpacing(10);
		screenshotScale.setPaintTicks(true);
		screenshotScale.setPaintLabels(true);
		c.gridx++;
		screenshot.add(screenshotScale, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		screenshot.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(screenshot);
		
		add(p, BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Save Settings")) {
			cfg.net.host = host.getText();
			
			try {
				cfg.net.port = Integer.parseInt(port.getText());
			} catch (NumberFormatException x) {
				System.err.println("Invalid port: " + port.getText());
			}
			
			cfg.net.password = password.getText();
			
			try {
				cfg.status.updateInterval = Integer.parseInt(statusInterval.getText());
			} catch (NumberFormatException x) {
				System.err.println("Invalid interval: " + statusInterval.getText());
			}
			
			try {
				cfg.screenshot.updateInterval = Integer.parseInt(screenshotInterval.getText());
			} catch (NumberFormatException x) {
				System.err.println("Invalid interval: " + screenshotInterval.getText());
			}
			
			cfg.screenshot.scale = screenshotScale.getValue();
			
			cfg.writeIni();
		}
	}
}
