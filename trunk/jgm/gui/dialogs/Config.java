package jgm.gui.dialogs;

import jgm.cfg;
import jgm.gui.GUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/* TODO Add method to update all the fields to the current value in the config */

public class Config extends Dialog implements ActionListener, ChangeListener {
	private JButton update;
	private JButton close;
	
	private JPanel net;
	private JTextField host;
	private JTextField port;
	private JTextField password;
	private JCheckBox netReconnect;
	
	private JPanel status;
	private JTextField statusInterval;
	private JTextField maxLogEntries;
	
	private JPanel screenshot;
	private JTextField screenshotInterval;
	private JSlider screenshotScale;
	private JSlider screenshotQuality;
	
	
	private JPanel sound;
	private JCheckBox enableSound;
	private JCheckBox soundWhisper;
	private JCheckBox soundSay;
	private JCheckBox soundGM;
	private JCheckBox soundFollow;
	private JCheckBox soundPVP;
	private JCheckBox soundStuck;
	
	private JCheckBox enableTTS;
	private JCheckBox ttsWhisper;
	private JCheckBox ttsSay;
	private JCheckBox ttsGM;
	private JCheckBox ttsStatus;
	
	private cfg cfg;
//	private static javax.swing.border.Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		
	public Config(Frame owner) {
		super(owner, "Configuration");
		cfg = jgm.cfg.getInstance();
		
		JPanel btnPanel = new JPanel(new GridLayout(1, 0));
		update = new JButton("Save Settings");
		update.setMnemonic(KeyEvent.VK_S);
		update.addActionListener(this);
		btnPanel.add(update);
		
		close = new JButton("Cancel");
		close.setMnemonic(KeyEvent.VK_C);
		close.addActionListener(this);
		btnPanel.add(close);
		
		add(btnPanel, BorderLayout.SOUTH);
		
		JPanel p = new JPanel(new GridLayout(2, 3, 10, 10));
		
		
		// net config pane
		net = new JPanel(new GridBagLayout());
		GUI.setTitleBorder(net, "Network");
		//net.setBorder(
		//	BorderFactory.createTitledBorder(lineBorder, "Network"));
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		net.add(new JLabel("Host: "), c);
		
		host = new JTextField(cfg.getString("net", "host"));
		host.addActionListener(this);
		c.gridx++;
		net.add(host, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Port: " ), c);
		
		port = new JTextField(((cfg.getInt("net", "port") > 0) ? Integer.toString(cfg.getInt("net", "port")) : ""));
		port.addActionListener(this);
		c.gridx++;
		net.add(port, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Password: " ), c);
		
		password = new JPasswordField(cfg.getString("net", "password"));
		password.addActionListener(this);
		c.gridx++;
		net.add(password, c);
		
		//netReconnect = new JCheckBox("Auto Reconnect", cfg.getBool("net", "autoreconnect"));
		netReconnect = new JCheckBox("Auto Reconnect");
		netReconnect.setEnabled(false);
		c.gridx = 0; c.gridy++;	c.gridwidth = 2;
		net.add(netReconnect, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 1;
		net.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(net);
		
		
		// status config pane
		status = new JPanel(new GridBagLayout());
		GUI.setTitleBorder(status, "Status/Logging");
		
		c.gridx = 0; c.gridy = 0;
		status.add(new JLabel("Refresh (ms): "), c);
		
		statusInterval = new JTextField(cfg.get("status", "updateInterval"));
		statusInterval.addActionListener(this);
		c.gridx++;
		status.add(statusInterval, c);
		
		c.gridx = 0; c.gridy++;
		status.add(new JLabel("Max Entries Per Log Tab: "), c);
		
		maxLogEntries = new JTextField(cfg.get("log", "maxentries"));
		maxLogEntries.addActionListener(this);
		c.gridx++;
		status.add(maxLogEntries, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		status.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(status);
		
		
		// screenshot panel
		screenshot = new JPanel(new GridBagLayout());
		GUI.setTitleBorder(screenshot, "Screenshot");
		//screenshot.setBorder(
		//	BorderFactory.createTitledBorder(lineBorder, "Screenshot"));
		
		c.gridx = 0; c.gridy = 0;
		screenshot.add(new JLabel("Refresh (ms): "), c);
		
		screenshotInterval = new JTextField(Integer.toString(cfg.getInt("screenshot", "updateInterval")));
		screenshotInterval.addActionListener(this);
		c.gridx++;
		screenshot.add(screenshotInterval, c);
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Scale: "), c);
		
		screenshotScale =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.getInt("screenshot", "scale"));
		screenshotScale.setMajorTickSpacing(30);
		screenshotScale.setMinorTickSpacing(10);
		screenshotScale.setPaintTicks(true);
		screenshotScale.setPaintLabels(true);
		c.gridx++;
		screenshot.add(screenshotScale, c);
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Quality: "), c);
		
		screenshotQuality =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.getInt("screenshot", "quality"));
			
		screenshotQuality.setMajorTickSpacing(30);
		screenshotQuality.setMinorTickSpacing(10);
		screenshotQuality.setPaintTicks(true);
		screenshotQuality.setPaintLabels(true);
		c.gridx++;
		screenshot.add(screenshotQuality, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		screenshot.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(screenshot);
		
		
		// sound config
		JPanel tmp = new JPanel(new GridLayout(1, 0, 10, 10));
		GUI.setTitleBorder(tmp, "Sound");
		//tmp.setBorder(
		//		BorderFactory.createTitledBorder(lineBorder, "Sound"));
			
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0; c.weighty = 0.0;
		enableSound = new JCheckBox("Enable Sound", cfg.getBool("sound", "enabled"));
		enableSound.addChangeListener(this);
		sound.add(enableSound, c);
		
		c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundWhisper = new JCheckBox("On Whisper", cfg.getBool("sound", "whisper"));
		sound.add(soundWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundSay = new JCheckBox("On Say", cfg.getBool("sound", "say"));
		sound.add(soundSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundGM = new JCheckBox("On GM Event", cfg.getBool("sound", "gm"));
		sound.add(soundGM, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundFollow = new JCheckBox("On Follower Alert", cfg.getBool("sound", "follow"));
		sound.add(soundFollow, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundPVP = new JCheckBox("On PVP Attacker Alert", cfg.getBool("sound", "pvp"));
		sound.add(soundPVP, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundStuck = new JCheckBox("On Stuck Too Many Times", cfg.getBool("sound", "stuck"));
		sound.add(soundStuck, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weighty = 0.0;
		enableTTS = new JCheckBox("Enable TTS", cfg.getBool("sound.tts", "enabled"));
		enableTTS.addChangeListener(this);
		sound.add(enableTTS, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0; c.gridwidth = 1;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsWhisper = new JCheckBox("On Whisper", cfg.getBool("sound.tts", "whisper"));
		sound.add(ttsWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsSay = new JCheckBox("On Say", cfg.getBool("sound.tts", "say"));
		sound.add(ttsSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsGM = new JCheckBox("On GM Event", cfg.getBool("sound.tts", "gm"));
		sound.add(ttsGM, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsStatus = new JCheckBox("On Status Event", cfg.getBool("sound.tts", "status"));
		sound.add(ttsStatus, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		sound = tmp;
		
		p.add(sound);
		
		add(p, BorderLayout.CENTER);
		
		// to initialize enabled/disabled states
		stateChanged(new ChangeEvent(enableSound));
		stateChanged(new ChangeEvent(enableTTS));
		
		makeVisible();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			System.out.println("Canceling config changes");
			setVisible(false);
			return;
		}
		
		cfg.set("net", "host", host.getText());
		
		try {
			cfg.set("net", "port", Integer.parseInt(port.getText()));
			
			if (cfg.getInt("net", "port") < 1) 
				throw new NumberFormatException("Port must be positive");
		} catch (NumberFormatException x) {
			System.err.println("Invalid port: " + port.getText());
		}
		
		cfg.set("net", "password", password.getText());
		cfg.set("net", "autoreconnect", netReconnect.isSelected());			
		
		try {
			cfg.set("status", "updateinterval", Integer.parseInt(statusInterval.getText()));
		} catch (NumberFormatException x) {
			System.err.println("Invalid interval: " + statusInterval.getText());
		}
		
		try {
			cfg.set("log", "maxentries", Integer.parseInt(maxLogEntries.getText()));
		} catch (NumberFormatException x) {
			System.err.println("Invalid max entries: " + maxLogEntries.getText());
		}
		
		try {
			cfg.set("screenshot", "updateinterval", Integer.parseInt(screenshotInterval.getText()));
		} catch (NumberFormatException x) {
			System.err.println("Invalid interval: " + screenshotInterval.getText());
		}
		
		cfg.set("screenshot", "scale", screenshotScale.getValue());
		cfg.set("screenshot", "quality", screenshotQuality.getValue());
		
		cfg.set("sound", "enabled", enableSound.isSelected());
		cfg.set("sound", "whisper", soundWhisper.isSelected());
		cfg.set("sound", "say", soundSay.isSelected());
		cfg.set("sound", "gm", soundGM.isSelected());
		cfg.set("sound", "follow", soundFollow.isSelected());
		cfg.set("sound", "pvp", soundPVP.isSelected());
		cfg.set("sound", "stuck", soundStuck.isSelected());
		cfg.set("sound.tts", "enabled", enableTTS.isSelected());
		cfg.set("sound.tts", "whisper", ttsWhisper.isSelected());
		cfg.set("sound.tts", "say", ttsSay.isSelected());
		cfg.set("sound.tts", "gm", ttsGM.isSelected());
		cfg.set("sound.tts", "status", ttsStatus.isSelected());
		
		cfg.writeIni();
		
		setVisible(false);
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == enableSound) {
			boolean state = enableSound.isEnabled() && enableSound.isSelected();
			
			soundWhisper.setEnabled(state);
			soundSay.setEnabled(state);
			soundGM.setEnabled(state);
			soundFollow.setEnabled(state);
			soundPVP.setEnabled(state);
			soundStuck.setEnabled(state);
			
			// make tts dependant on general sound
			//enableTTS.setEnabled(state);
			//stateChanged(new ChangeEvent(enableTTS));
		} else if (e.getSource() == enableTTS) {
			boolean state = enableTTS.isEnabled() && enableTTS.isSelected();
			
			ttsWhisper.setEnabled(state);
			ttsSay.setEnabled(state);
			ttsGM.setEnabled(state);
			ttsStatus.setEnabled(state);
		}
	}
	
	protected void onShow() {
		//super.onShow();
		
		host.setText(cfg.get("net", "host"));
		port.setText(cfg.get("net", "port"));
		password.setText(cfg.get("net", "password"));
		//netReconnect.setSelected(cfg.getBool("net", "autoreconnect"));
		
		statusInterval.setText(cfg.get("status", "updateinterval"));
		maxLogEntries.setText(cfg.get("log", "maxentries"));
		
		screenshotInterval.setText(cfg.get("screenshot", "updateinterval"));
		screenshotScale.setValue(cfg.getInt("screenshot", "scale"));
		screenshotQuality.setValue(cfg.getInt("screenshot", "quality"));
		
		enableSound.setSelected(cfg.getBool("sound", "enabled"));
		soundWhisper.setSelected(cfg.getBool("sound", "whisper"));
		soundSay.setSelected(cfg.getBool("sound", "say"));
		soundGM.setSelected(cfg.getBool("sound", "gm"));
		soundFollow.setSelected(cfg.getBool("sound", "follow"));
		soundPVP.setSelected(cfg.getBool("sound", "pvp"));
		soundStuck.setSelected(cfg.getBool("sound", "stuck"));
		
		enableTTS.setSelected(cfg.getBool("sound.tts", "enabled"));
		ttsWhisper.setSelected(cfg.getBool("sound.tts", "whisper"));
		ttsSay.setSelected(cfg.getBool("sound.tts", "say"));
		ttsGM.setSelected(cfg.getBool("sound.tts", "gm"));
		ttsStatus.setSelected(cfg.getBool("sound.tts", "status"));
	}
}
