package jgm.gui.tabs;

import jgm.cfg;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ConfigTab extends Tab implements ActionListener, ChangeListener {
	private JButton update;
	
	private JPanel net;
	private JTextField host;
	private JTextField port;
	private JTextField password;
	private JCheckBox netReconnect;
	
	private JPanel status;
	private JTextField statusInterval;
	
	private JPanel screenshot;
	private JTextField screenshotInterval;
	private JSlider screenshotScale;
	private JSlider screenshotQuality;
	
	
	private JPanel sound;
	private JCheckBox enableSound;
	private JCheckBox soundWhisper;
	private JCheckBox soundSay;
	private JCheckBox soundGM;
	
	private JCheckBox enableTTS;
	private JCheckBox ttsWhisper;
	private JCheckBox ttsSay;
	private JCheckBox ttsGM;
	private JCheckBox ttsStatus;
	
	private static javax.swing.border.Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
	
	public ConfigTab() {
		super(new BorderLayout(), "Config");
		
		update = new JButton("Save Settings");
		update.addActionListener(this);
		add(update, BorderLayout.NORTH);
		
		JPanel p = new JPanel(new GridLayout(2, 3, 10, 10));
		
		
		// net config pane
		net = new JPanel(new GridBagLayout());
		net.setBorder(
			BorderFactory.createTitledBorder(lineBorder, "Network"));
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
		net.add(new JLabel("Host: "), c);
		
		host = new JTextField(cfg.net.host);
		c.gridx++;
		net.add(host, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Port: " ), c);
		
		port = new JTextField(((cfg.net.port > 0) ? Integer.toString(cfg.net.port) : ""));
		c.gridx++;
		net.add(port, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("Password: " ), c);
		
		password = new JTextField(cfg.net.password);
		c.gridx++;
		net.add(password, c);
		
		netReconnect = new JCheckBox("Auto Reconnect", cfg.net.autoReconnect);
		netReconnect.setEnabled(false);
		c.gridx = 0; c.gridy++;	c.gridwidth = 2;
		net.add(netReconnect, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 1;
		net.add(new JLabel(), c);
		c.weighty = 0.0;
		
		p.add(net);
		
		
		// status config pane
		status = new JPanel(new GridBagLayout());
		status.setBorder(
			BorderFactory.createTitledBorder(lineBorder, "Status"));
		
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
			BorderFactory.createTitledBorder(lineBorder, "Screenshot"));
		
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
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Quality: "), c);
		
		screenshotQuality =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.screenshot.quality);
			
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
		tmp.setBorder(
				BorderFactory.createTitledBorder(lineBorder, "Sound"));
			
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0; c.weighty = 0.0;
		enableSound = new JCheckBox("Enable Sound", cfg.sound.enabled);
		enableSound.addChangeListener(this);
		sound.add(enableSound, c);
		
		c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundWhisper = new JCheckBox("On Whisper", cfg.sound.whisper);
		sound.add(soundWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundSay = new JCheckBox("On Say", cfg.sound.say);
		sound.add(soundSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundGM = new JCheckBox("On GM Event", cfg.sound.gm);
		sound.add(soundGM, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weighty = 0.0;
		enableTTS = new JCheckBox("Enable TTS", cfg.sound.tts.enabled);
		enableTTS.addChangeListener(this);
		sound.add(enableTTS, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0; c.gridwidth = 1;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsWhisper = new JCheckBox("On Whisper", cfg.sound.tts.whisper);
		sound.add(ttsWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsSay = new JCheckBox("On Say", cfg.sound.tts.say);
		sound.add(ttsSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsGM = new JCheckBox("On GM Event", cfg.sound.tts.gm);
		sound.add(ttsGM, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsStatus = new JCheckBox("On Status Event", cfg.sound.tts.status);
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
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Save Settings")) {
			cfg.net.host = host.getText();
						
			try {
				cfg.net.port = Integer.parseInt(port.getText());
				
				if (cfg.net.port < 1) 
					throw new NumberFormatException("Port must be positive");
			} catch (NumberFormatException x) {
				System.err.println("Invalid port: " + port.getText());
			}
			
			cfg.net.password = password.getText();
			cfg.net.autoReconnect = netReconnect.isSelected();			
			
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
			cfg.screenshot.quality = screenshotQuality.getValue();
			
			cfg.sound.enabled = enableSound.isSelected();
			cfg.sound.whisper = soundWhisper.isSelected();
			cfg.sound.say = soundSay.isSelected();
			cfg.sound.gm = soundGM.isSelected();
			cfg.sound.tts.enabled = enableTTS.isSelected();
			cfg.sound.tts.whisper = ttsWhisper.isSelected();
			cfg.sound.tts.say = ttsSay.isSelected();
			cfg.sound.tts.gm = ttsGM.isSelected();
			cfg.sound.tts.status = ttsStatus.isSelected();
			
			cfg.writeIni();
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == enableSound) {
			boolean state = enableSound.isEnabled() && enableSound.isSelected();
			
			soundWhisper.setEnabled(state);
			soundSay.setEnabled(state);
			soundGM.setEnabled(state);
			
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
}
