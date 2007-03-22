package jgm.gui.dialogs;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Config extends Dialog implements ActionListener, ChangeListener {
	static Logger log = Logger.getLogger(Config.class.getName());
	
	private JTabbedPane tabs;
	private JButton update;
	private JButton close;
	
	private JPanel general;
	private JCheckBox debug;
	private JTextField statusInterval;
	private JTextField maxLogEntries;
	private JCheckBox showTray;
	private JCheckBox minToTray;
	
	private JPanel net;
	private JTextField host;
	private JTextField port;
	private JTextField password;
	private JCheckBox netReconnect;
	private JTextField netReconnectDelay;
	private JTextField netReconnectTries;
	
	private JPanel screenshot;
	private JTextField screenshotInterval;
	private JSlider screenshotScale;
	private JSlider screenshotQuality;
	private JSpinner screenshotBuffer;
	private JTextField screenshotTimeout;	
	private JLabel ssInfo;
	
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
	
	private jgm.Config cfg;
//	private static javax.swing.border.Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		
	public Config(Frame owner) {
		super(owner, "Configuration");
		cfg = jgm.Config.getInstance();
		
		tabs = new JTabbedPane(JTabbedPane.LEFT);
		
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
		
		//JPanel p = new JPanel(new GridLayout(2, 3, 10, 10));
		
		
//		 status config pane
		general = new JPanel(new GridBagLayout());
		//GUI.setTitleBorder(status, "Status/Logging");
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 2;
		
		debug = new JCheckBox("Log Debugging Info", jgm.JGlideMon.debug);
		general.add(debug, c);
		
		c.gridy++; c.gridwidth = 1;
		general.add(new JLabel("Status Refresh Interval (ms): "), c);
		
		statusInterval = new JTextField(cfg.get("status", "updateInterval"));
		statusInterval.addActionListener(this);
		c.gridx++;
		general.add(statusInterval, c);
		
		c.gridx = 0; c.gridy++;
		general.add(new JLabel("Max Entries Per Log Tab: "), c);
		
		maxLogEntries = new JTextField(cfg.get("log", "maxentries"));
		maxLogEntries.addActionListener(this);
		c.gridx++;
		general.add(maxLogEntries, c);
		
		showTray = new JCheckBox("Show Tray Icon", cfg.getBool("general", "showtray"));
		showTray.addChangeListener(this);
		c.gridy++; c.gridx = 0; c.gridwidth = 2;
		general.add(showTray, c);
		
		minToTray = new JCheckBox("Minimize To Tray", cfg.getBool("general", "mintotray"));
		minToTray.setMargin(new Insets(0, 25, 0, 0));
		c.gridy++;
		general.add(minToTray, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 1;
		general.add(new JLabel(), c);
		c.weighty = 0.0;
		
		tabs.addTab("General", general);
		//p.add(status);
		
		
		// net config pane
		net = new JPanel(new GridBagLayout());
		//GUI.setTitleBorder(net, "Network");
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
		
		netReconnect = new JCheckBox("Auto Reconnect", cfg.getBool("net", "autoreconnect"));
		netReconnect.addChangeListener(this);
		c.gridx = 0; c.gridy++;	c.gridwidth = 2;
		net.add(netReconnect, c);

		c.gridy++; c.gridwidth = 1;
		net.add(new JLabel("    Delay Between Tries (s): "), c);
		
		netReconnectDelay = new JTextField(cfg.get("net", "autoreconnectdelay"));
		c.gridx++;
		net.add(netReconnectDelay, c);
		
		c.gridx = 0; c.gridy++;
		net.add(new JLabel("    Max Number of Tries: "), c);
		
		netReconnectTries = new JTextField(cfg.get("net", "autoreconnecttries"));
		c.gridx++;
		net.add(netReconnectTries, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 1;
		net.add(new JLabel(), c);
		c.weighty = 0.0;
		
		tabs.addTab("Network", net);
		//p.add(net);
		
		
		// screenshot panel
		screenshot = new JPanel(new GridBagLayout());
		//GUI.setTitleBorder(screenshot, "Screenshot");
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
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Buffer Size (Mb): "), c);
		
		screenshotBuffer = new JSpinner(
			new SpinnerNumberModel(1.0, 0.5, 10.0, 0.1)
		);
		c.gridx++;
		screenshot.add(screenshotBuffer, c);
		
		c.gridx = 0; c.gridy++;
		screenshot.add(new JLabel("Update Timeout (s): "), c);
		
		screenshotTimeout = new JTextField();
		c.gridx++;
		screenshot.add(screenshotTimeout, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		ssInfo = new JLabel("", JLabel.CENTER);
		screenshot.add(ssInfo, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		screenshot.add(new JLabel(), c);
		c.weighty = 0.0;
		
		tabs.addTab("Screenshot", screenshot);
		//p.add(screenshot);
		
		
		// sound config
		JPanel tmp = new JPanel(new GridLayout(1, 0, 10, 10));
		//GUI.setTitleBorder(tmp, "Sound");
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
		
		tabs.addTab("Sound/TTS", sound);
		//p.add(sound);
		
		add(tabs, BorderLayout.CENTER);
		
		// ensuring appropriate groups are enabled/disabled
		// is taken care of in onShow()
		
		makeVisible();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			log.fine("Canceling config changes");
			setVisible(false);
			return;
		}
		
		cfg.set("general", "debug", debug.isSelected());
		jgm.JGlideMon.debug = debug.isSelected();
		jgm.Log.reloadConfig();
		
		cfg.set("net", "host", host.getText());
		
		try {
			cfg.set("net", "port", Integer.parseInt(port.getText()));
			
			if (cfg.getInt("net", "port") < 1) 
				throw new NumberFormatException("Port must be positive");
		} catch (NumberFormatException x) {
			log.warning("Invalid port: " + port.getText());
		}
		
		cfg.set("net", "password", password.getText());
		cfg.set("net", "autoreconnect", netReconnect.isSelected());			
		
		try {
			cfg.set("net", "autoreconnectdelay", Integer.parseInt(netReconnectDelay.getText()));
			
			if (cfg.getInt("net", "autoreconnectdelay") < 0)
				throw new NumberFormatException("Auto reconnect delay must be > 0");
		} catch (NumberFormatException x) {
			log.warning("Invalid auto reconnect delay: " + netReconnectDelay.getText());
		}
		
		try {
			cfg.set("net", "autoreconnecttries", Integer.parseInt(netReconnectTries.getText()));
			
			if (cfg.getInt("net", "autoreconnecttries") < 1)
				throw new NumberFormatException("Auto reconnect tries must be positive");
		} catch (NumberFormatException x) {
			log.warning("Invalid auto reconnect tries: " + netReconnectTries.getText());
		}
		
		try {
			cfg.set("status", "updateinterval", Integer.parseInt(statusInterval.getText()));
		} catch (NumberFormatException x) {
			log.warning("Invalid interval: " + statusInterval.getText());
		}
		
		try {
			cfg.set("log", "maxentries", Integer.parseInt(maxLogEntries.getText()));
		} catch (NumberFormatException x) {
			log.warning("Invalid max entries: " + maxLogEntries.getText());
		}
		
		cfg.set("general", "showtray", showTray.isSelected());
		cfg.set("general", "mintotray", minToTray.isSelected());
		
		try {
			cfg.set("screenshot", "updateinterval", Integer.parseInt(screenshotInterval.getText()));
		} catch (NumberFormatException x) {
			log.warning("Invalid interval: " + screenshotInterval.getText());
		}
		
		cfg.set("screenshot", "scale", screenshotScale.getValue());
		cfg.set("screenshot", "quality", screenshotQuality.getValue());
		
		cfg.set("screenshot", "buffer", ((Double) screenshotBuffer.getValue()).doubleValue());
		
		try {
			cfg.set("screenshot", "timeout", Integer.parseInt(screenshotTimeout.getText()));
		} catch (NumberFormatException x) {
			log.warning("Invalid timeout: " + screenshotTimeout.getText());
		}
		
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
		
		jgm.Config.writeIni();
		
		setVisible(false);
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == showTray) {
			boolean state = showTray.isEnabled() && showTray.isSelected();
			
			minToTray.setEnabled(state);
			
			if (state) {
				jgm.GUI.tray.enable();
			} else {
				jgm.GUI.tray.disable();
			}
		} else if (e.getSource() == netReconnect) {
			boolean state = netReconnect.isEnabled() && netReconnect.isSelected();
			
			netReconnectDelay.setEnabled(state);
			netReconnectTries.setEnabled(state);
		} else if (e.getSource() == enableSound) {
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

		debug.setSelected(cfg.getBool("general", "debug"));
		statusInterval.setText(cfg.get("status", "updateinterval"));
		maxLogEntries.setText(cfg.get("log", "maxentries"));
		showTray.setEnabled(jgm.gui.Tray.isSupported());
		showTray.setSelected(cfg.getBool("general", "showtray"));
		minToTray.setSelected(cfg.getBool("general", "mintotray"));
		
		host.setText(cfg.get("net", "host"));
		port.setText(cfg.get("net", "port"));
		password.setText(cfg.get("net", "password"));
		netReconnect.setSelected(cfg.getBool("net", "autoreconnect"));
		netReconnectDelay.setText(cfg.get("net", "autoreconnectdelay"));
		
		screenshotInterval.setText(cfg.get("screenshot", "updateinterval"));
		screenshotScale.setValue(cfg.getInt("screenshot", "scale"));
		screenshotQuality.setValue(cfg.getInt("screenshot", "quality"));
		screenshotBuffer.setValue(cfg.getDouble("screenshot", "buffer"));
		screenshotTimeout.setText(cfg.get("screenshot", "timeout"));
		
		Icon i = jgm.GUI.instance.tabsPane.screenshotTab.ssLabel.getIcon();
		
		if (i != null) {
			int width = i.getIconWidth();
			int height = i.getIconHeight();
			int size = width * height * 3;
			float sizeMb = (float) size / 1048576; 
			
			ssInfo.setText(
				String.format(
					"<html><br>Your last screenshot was %sx%s pixels.<br>\n" +
					"Uncompressed it could be at most %.2fMb.",
					width,
					height,
					sizeMb
				)
			);
			
			this.pack();
		}
		
		enableSound.setSelected(cfg.getBool("sound", "enabled"));
		soundWhisper.setSelected(cfg.getBool("sound", "whisper"));
		soundSay.setSelected(cfg.getBool("sound", "say"));
		soundGM.setSelected(cfg.getBool("sound", "gm"));
		soundFollow.setSelected(cfg.getBool("sound", "follow"));
		soundPVP.setSelected(cfg.getBool("sound", "pvp"));
		soundStuck.setSelected(cfg.getBool("sound", "stuck"));
		
		enableTTS.setEnabled(jgm.util.Speech.isSupported());
		enableTTS.setSelected(cfg.getBool("sound.tts", "enabled"));
		ttsWhisper.setSelected(cfg.getBool("sound.tts", "whisper"));
		ttsSay.setSelected(cfg.getBool("sound.tts", "say"));
		ttsGM.setSelected(cfg.getBool("sound.tts", "gm"));
		ttsStatus.setSelected(cfg.getBool("sound.tts", "status"));
		
		// to initialize enabled/disabled states
		stateChanged(new ChangeEvent(showTray));
		stateChanged(new ChangeEvent(netReconnect));
		stateChanged(new ChangeEvent(enableSound));
		stateChanged(new ChangeEvent(enableTTS));
	}
	
	public void selectTab(int index) {
		tabs.setSelectedIndex(index);
	}
}
