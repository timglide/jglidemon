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
package jgm.gui.dialogs;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import jgm.GUI;

public class Config extends Dialog implements ActionListener, ChangeListener {
	static Logger log = Logger.getLogger(Config.class.getName());
	
	static SpinnerNumberModel PORT_SPINNER
		= new SpinnerNumberModel(1, 1, 65536, 1);
	static SpinnerNumberModel INT_SPINNER
		= new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
	
	static JSpinner makeSpinner(SpinnerNumberModel sm) {
		return new JSpinner(
			new SpinnerNumberModel(
				(Integer) sm.getValue(), sm.getMinimum(), sm.getMaximum(), sm.getStepSize())
		);
	}
	
	JTabbedPane tabs;
	JButton update;
	JButton close;
	
	JPanel general;
	JCheckBox debug;
	JSpinner statusInterval;
	JSpinner maxLogEntries;
	JCheckBox showTray;
	JCheckBox minToTray;
	JComboBox wowDbSite;
	
	JPanel net;
	JTextField host;
	JSpinner port;
	JTextField password;
	JCheckBox netReconnect;
	JSpinner netReconnectDelay;
	JSpinner netReconnectTries;
	
	JPanel screenshot;
	JSpinner screenshotInterval;
	JCheckBox screenshotAutoScale;
	JSlider screenshotScale;
	JSlider screenshotQuality;
	JSpinner screenshotBuffer;
	JSpinner screenshotTimeout;	
	JLabel ssInfo;
	
	JPanel sound;
	JCheckBox enableSound;
	JCheckBox soundWhisper;
	JCheckBox soundSay;
	JCheckBox soundGM;
	JCheckBox soundFollow;
	JCheckBox soundPVP;
	JCheckBox soundStuck;
	JCheckBox soundStatus;
	
	JCheckBox enableTTS;
	JCheckBox ttsWhisper;
	JCheckBox ttsSay;
	JCheckBox ttsGM;
	JCheckBox ttsStatus;
	
	JPanel stuck;
	JCheckBox enableStuck;
	JSpinner stuckLimit;
	JSpinner stuckTimeout;
	
	JPanel web;
	JCheckBox enableWeb;
	JSpinner webPort;
	JSpinner webInterval;
	
	jgm.Config cfg;
//	static javax.swing.border.Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		
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
		
		c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		general.add(new JLabel("Status Refresh Interval (ms): "), c);
		
		statusInterval = makeSpinner(INT_SPINNER);
//		statusInterval.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		general.add(statusInterval, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		general.add(new JLabel("Max Entries Per Log Tab: "), c);
		
		maxLogEntries = makeSpinner(INT_SPINNER);
//		maxLogEntries.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		general.add(maxLogEntries, c);
		
		showTray = new JCheckBox("Show Tray Icon", cfg.getBool("general", "showtray"));
		showTray.addChangeListener(this);
		c.gridy++; c.gridx = 0; c.gridwidth = 2;
		general.add(showTray, c);
		
		minToTray = new JCheckBox("Minimize To Tray", cfg.getBool("general", "mintotray"));
		minToTray.setMargin(new Insets(0, 25, 0, 0));
		c.gridy++;
		general.add(minToTray, c);
		
		wowDbSite = new JComboBox(
			new String[] {
				"http://www.wowhead.com/?item=%s",
				"http://www.thottbot.com/i%s",
				"http://wow.allakhazam.com/db/item.html?witem=%s"
			}
		);
/*		wowDbSite.addActionListener(new ActionListener() {
			// make sure each item gets added to the list
			public void actionPerformed(ActionEvent e) {
				Object selected = wowDbSite.getSelectedItem();
				
				int num = wowDbSite.getItemCount();
				boolean found = true;
				
				for (int i = 0; i < num; i++) {
					Object cur = wowDbSite.getItemAt(i);
					if (cur.equals(selected)) break;
					found = false;
				}
				
				if (!found) {
					wowDbSite.addItem(selected);
				}
			}
		});*/
		wowDbSite.setEditable(true);
		wowDbSite.setSelectedItem(cfg.get("general", "wowdb"));		
		
		c.gridx = 0; c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		general.add(new JLabel("WoW DB Site: "), c);
		
		c.gridx++; c.weightx = 1.0;
		general.add(wowDbSite, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 2;
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
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		net.add(new JLabel("Host: "), c);
		
		host = new JTextField(cfg.getString("net", "host"));
		host.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(host, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("Port: " ), c);
		
		port = makeSpinner(PORT_SPINNER);
		port.setEditor(new JSpinner.NumberEditor(port, "#")); // prevent comma grouping
//		port.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(port, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("Password: " ), c);
		
		password = new JPasswordField(cfg.getString("net", "password"));
		password.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(password, c);
		
		netReconnect = new JCheckBox("Auto Reconnect", cfg.getBool("net", "autoreconnect"));
		netReconnect.addChangeListener(this);
		c.gridx = 0; c.gridy++;	c.gridwidth = 2;
		net.add(netReconnect, c);

		c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		net.add(new JLabel("    Delay Between Tries (s): "), c);
		
		netReconnectDelay = makeSpinner(INT_SPINNER);
		c.gridx++; c.weightx = 1.0;
		net.add(netReconnectDelay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("    Max Number of Tries: "), c);
		
		netReconnectTries = makeSpinner(INT_SPINNER);
		c.gridx++; c.weightx = 1.0;
		net.add(netReconnectTries, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 2;
		net.add(new JLabel(), c);
		c.weighty = 0.0;
		
		tabs.addTab("Network", net);
		//p.add(net);
		
		
		// screenshot panel
		screenshot = new JPanel(new GridBagLayout());
		//GUI.setTitleBorder(screenshot, "Screenshot");
		//screenshot.setBorder(
		//	BorderFactory.createTitledBorder(lineBorder, "Screenshot"));
		
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.weighty = 0.0;
		screenshot.add(new JLabel("Refresh (ms): "), c);
		
		screenshotInterval = makeSpinner(INT_SPINNER);
//		screenshotInterval.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotInterval, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		screenshotAutoScale = new JCheckBox("Automatically adjust scale to fit window", cfg.getBool("screenshot", "autoscale"));
		screenshotAutoScale.addChangeListener(this);
		screenshot.add(screenshotAutoScale, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		screenshot.add(new JLabel("Scale: "), c);
		
		screenshotScale =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.getInt("screenshot", "scale"));
		screenshotScale.setMajorTickSpacing(30);
		screenshotScale.setMinorTickSpacing(10);
		screenshotScale.setPaintTicks(true);
		screenshotScale.setPaintLabels(true);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotScale, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		screenshot.add(new JLabel("Quality: "), c);
		
		screenshotQuality =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.getInt("screenshot", "quality"));
			
		screenshotQuality.setMajorTickSpacing(30);
		screenshotQuality.setMinorTickSpacing(10);
		screenshotQuality.setPaintTicks(true);
		screenshotQuality.setPaintLabels(true);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotQuality, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		screenshot.add(new JLabel("Buffer Size (MB): "), c);
		
		screenshotBuffer = new JSpinner(
			new SpinnerNumberModel(1.0, 0.5, 10.0, 0.1)
		);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotBuffer, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		screenshot.add(new JLabel("Update Timeout (s): "), c);
		
		screenshotTimeout = makeSpinner(INT_SPINNER);
		c.gridx++; c.weightx = 1.0;
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
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundStatus = new JCheckBox("On Status Event", cfg.getBool("sound", "status"));
		soundStatus.setToolTipText("This includes when your character dies.");
		sound.add(soundStatus, c);
		
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
		ttsStatus.setToolTipText("This includes when JGlideMon connects/disconnects with Glider.");
		sound.add(ttsStatus, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		sound = tmp;
		
		tabs.addTab("Sound/TTS", sound);
		//p.add(sound);
		
		
		// stuck options
		stuck = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		c.weightx = 1.0; c.weighty = 0.0;		
		enableStuck = new JCheckBox("Attempt To Resume When Stuck");
		enableStuck.addChangeListener(this);
		stuck.add(enableStuck, c);
		
		JLabel tmpLbl = new JLabel("  Stuck Limit: ");
		tmpLbl.setToolTipText("Give up if stuck this many times in a row or 0 to never give up");
		
		c.gridwidth = 1; c.gridy++; c.weightx = 0.0;
		stuck.add(tmpLbl, c);
		
		stuckLimit = new JSpinner(
			new SpinnerNumberModel(5, 0, 10000, 1)
		);
		stuckLimit.addChangeListener(this);
		c.gridx++; c.weightx = 1.0;
		stuck.add(stuckLimit, c);
		
		tmpLbl = new JLabel("  Limit Timeout (s): ");
		tmpLbl.setToolTipText("Reset stuck timer after this many seconds");
		
		c.gridx = 0; c.gridy++; c.weightx = 0;
		stuck.add(tmpLbl, c);

		stuckTimeout = new JSpinner(
			new SpinnerNumberModel(300, 5, 10000, 1)
		);
		stuckTimeout.addChangeListener(this);
		c.gridx++; c.weightx = 1.0;
		stuck.add(stuckTimeout, c);
		
		c.gridx = 0; c.gridwidth = 2; c.gridy++; c.weighty = 1.0;
		stuck.add(new JLabel(), c);
		
		tabs.addTab("Stuck", stuck);
		
		
		
		// web server options
		web = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1; c.weighty = 0.0; c.weightx = 1.0;
		enableWeb = new JCheckBox("Enable Web-Server");
		enableWeb.addChangeListener(this);
		web.add(enableWeb, c);
		
		tmpLbl = new JLabel("  Port: ");
//		tmpLbl.setToolTipText("Give up if stuck this many times in a row or 0 to never give up");
		
		c.gridwidth = 1; c.gridy++; c.weightx = 0.0;
		web.add(tmpLbl, c);
		
		webPort = makeSpinner(PORT_SPINNER);
		webPort.setEditor(new JSpinner.NumberEditor(webPort, "#")); // prevent comma grouping
		webPort.addChangeListener(this);
		c.gridx++; c.weightx = 1.0;
		web.add(webPort, c);
		
		tmpLbl = new JLabel("  Update Interval (ms): ");
		c.gridwidth = 1; c.gridx = 0; c.gridy++; c.weightx = 0.0;
		web.add(tmpLbl, c);
		
		webInterval = makeSpinner(INT_SPINNER);
		c.gridx++; c.weightx = 1.0;
		web.add(webInterval, c);
		
		c.gridx = 0; c.gridwidth = 2; c.gridy++; c.weighty = 1.0;
		web.add(new JLabel(), c);
		
		tabs.addTab("Web", web);
		
		
		
		
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
		
		boolean oldDebug = cfg.getBool("general", "debug");
		
		if (oldDebug != debug.isSelected()) {
			cfg.set("general", "debug", debug.isSelected());
			jgm.JGlideMon.debug = debug.isSelected();
			
			// this deletes the current log
			jgm.Log.reloadConfig();
		}
		
		cfg.set("net", "host", host.getText());		
		cfg.set("net", "port", port.getValue());
		cfg.set("net", "password", password.getText());
		cfg.set("net", "autoreconnect", netReconnect.isSelected());			
		cfg.set("net", "autoreconnectdelay", netReconnectDelay.getValue());
		cfg.set("net", "autoreconnecttries", netReconnectTries.getValue());

		cfg.set("status", "updateinterval", statusInterval.getValue());

		cfg.set("log", "maxentries", maxLogEntries.getValue());

		
		cfg.set("general", "showtray", showTray.isSelected());
		cfg.set("general", "mintotray", minToTray.isSelected());
		cfg.set("general", "wowdb", wowDbSite.getSelectedItem());
		
		
		cfg.set("screenshot", "updateinterval", screenshotInterval.getValue());
		cfg.set("screenshot", "autoscale", screenshotAutoScale.isSelected());
		cfg.set("screenshot", "scale", screenshotScale.getValue());
		cfg.set("screenshot", "quality", screenshotQuality.getValue());
		cfg.set("screenshot", "buffer", ((Double) screenshotBuffer.getValue()).doubleValue());
		cfg.set("screenshot", "timeout", screenshotTimeout.getValue());

		
		// would be null on first-time startup
		if (null != jgm.JGlideMon.instance.ssUpdater)
			jgm.JGlideMon.instance.ssUpdater.sentSettings = false;
		
		cfg.set("sound", "enabled", enableSound.isSelected());
		cfg.set("sound", "whisper", soundWhisper.isSelected());
		cfg.set("sound", "say", soundSay.isSelected());
		cfg.set("sound", "gm", soundGM.isSelected());
		cfg.set("sound", "follow", soundFollow.isSelected());
		cfg.set("sound", "pvp", soundPVP.isSelected());
		cfg.set("sound", "stuck", soundStuck.isSelected());
		cfg.set("sound", "status", soundStatus.isSelected());
		cfg.set("sound.tts", "enabled", enableTTS.isSelected());
		cfg.set("sound.tts", "whisper", ttsWhisper.isSelected());
		cfg.set("sound.tts", "say", ttsSay.isSelected());
		cfg.set("sound.tts", "gm", ttsGM.isSelected());
		cfg.set("sound.tts", "status", ttsStatus.isSelected());
		
		cfg.set("stuck", "enabled", enableStuck.isSelected());
		cfg.set("stuck", "limit", ((Integer) stuckLimit.getValue()).intValue());
		cfg.set("stuck", "timeout", ((Integer) stuckTimeout.getValue()).intValue());
		
		boolean oldWebEnabled = cfg.getBool("web", "enabled");
		int oldWebPort = cfg.getInt("web", "port");
		
		cfg.set("web", "enabled", enableWeb.isSelected());
		cfg.set("web", "port", webPort.getValue());
		cfg.set("web", "updateinterval", webInterval.getValue());

		
		// stop httpd if needed
		if ((oldWebEnabled && oldWebPort != cfg.getInt("web", "port")) ||
			(!enableWeb.isSelected())) {
			jgm.HTTPD.instance.stop();
		}
		
		// restart on different port if needed
		if ((enableWeb.isSelected() && oldWebPort != cfg.getInt("web", "port")) ||
			(!oldWebEnabled && enableWeb.isSelected()) ||
			(enableWeb.isSelected() && jgm.HTTPD.thread == null)) {
			try {
				jgm.HTTPD.instance.start(cfg.getInt("web", "port"));
			} catch (java.io.IOException x) {
				JOptionPane.showMessageDialog(GUI.frame,
					"Unable to start web-server.\n" +
					"Port " + cfg.getInt("web", "port") + " is unavailible.",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
		
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
		} else if (e.getSource() == screenshotAutoScale) {
			boolean state = screenshotAutoScale.isSelected();
			screenshotScale.setEnabled(!state);
		} else if (e.getSource() == enableSound) {
			boolean state = enableSound.isEnabled() && enableSound.isSelected();
			
			soundWhisper.setEnabled(state);
			soundSay.setEnabled(state);
			soundGM.setEnabled(state);
			soundFollow.setEnabled(state);
			soundPVP.setEnabled(state);
			soundStuck.setEnabled(state);
			soundStatus.setEnabled(state);
			
			// make tts dependant on general sound
			//enableTTS.setEnabled(state);
			//stateChanged(new ChangeEvent(enableTTS));
		} else if (e.getSource() == enableTTS) {
			boolean state = enableTTS.isEnabled() && enableTTS.isSelected();
			
			ttsWhisper.setEnabled(state);
			ttsSay.setEnabled(state);
			ttsGM.setEnabled(state);
			ttsStatus.setEnabled(state);
		} else if (e.getSource() == enableStuck) {
			boolean state = enableStuck.isEnabled() && enableStuck.isSelected();
			
			stuckLimit.setEnabled(state);
			stuckTimeout.setEnabled(state);
		} else if (e.getSource() == enableWeb) {
			boolean state = enableWeb.isEnabled() && enableWeb.isSelected();
			
			webPort.setEnabled(state);
			webInterval.setEnabled(state);
		}
	}
	
	protected void onShow() {
		//super.onShow();

		debug.setSelected(cfg.getBool("general", "debug"));
		statusInterval.setValue(cfg.getInt("status", "updateinterval"));
		maxLogEntries.setValue(cfg.getInt("log", "maxentries"));
		showTray.setEnabled(jgm.gui.Tray.isSupported());
		showTray.setSelected(cfg.getBool("general", "showtray"));
		minToTray.setSelected(cfg.getBool("general", "mintotray"));
		
		host.setText(cfg.get("net", "host"));
		port.setValue(cfg.getInt("net", "port"));
		password.setText(cfg.get("net", "password"));
		netReconnect.setSelected(cfg.getBool("net", "autoreconnect"));
		netReconnectDelay.setValue(cfg.getInt("net", "autoreconnectdelay"));
		netReconnectTries.setValue(cfg.getInt("net", "autoreconnecttries"));
		
		screenshotInterval.setValue(cfg.getInt("screenshot", "updateinterval"));
		screenshotAutoScale.setSelected(cfg.getBool("screenshot", "autoscale"));
		screenshotScale.setValue(cfg.getInt("screenshot", "scale"));
		screenshotQuality.setValue(cfg.getInt("screenshot", "quality"));
		screenshotBuffer.setValue(cfg.getDouble("screenshot", "buffer"));
		screenshotTimeout.setValue(cfg.getInt("screenshot", "timeout"));
		
		Icon i = jgm.GUI.instance.tabsPane.screenshotTab.ssLabel.getIcon();
		
		if (i != null) {
			int width = i.getIconWidth();
			int height = i.getIconHeight();
			int size = width * height * 3;
			float sizeMb = (float) size / 1048576; 
			
			ssInfo.setText(
				String.format(
					"<html><br>Your last screenshot was %sx%s pixels.<br>\n" +
					"Uncompressed it could be at most %.2fMB.",
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
		soundStatus.setSelected(cfg.getBool("sound", "status"));
		
		enableTTS.setEnabled(jgm.util.Speech.isSupported());
		enableTTS.setSelected(cfg.getBool("sound.tts", "enabled"));
		ttsWhisper.setSelected(cfg.getBool("sound.tts", "whisper"));
		ttsSay.setSelected(cfg.getBool("sound.tts", "say"));
		ttsGM.setSelected(cfg.getBool("sound.tts", "gm"));
		ttsStatus.setSelected(cfg.getBool("sound.tts", "status"));
		
		enableStuck.setSelected(cfg.getBool("stuck", "enabled"));
		stuckLimit.setValue(cfg.getInt("stuck", "limit"));
		stuckTimeout.setValue(cfg.getInt("stuck", "timeout"));
		
		enableWeb.setSelected(cfg.getBool("web", "enabled"));
		webPort.setValue(cfg.getInt("web", "port"));
		webInterval.setValue(cfg.getInt("web", "updateinterval"));
		
		// to initialize enabled/disabled states
		stateChanged(new ChangeEvent(showTray));
		stateChanged(new ChangeEvent(netReconnect));
		stateChanged(new ChangeEvent(screenshotAutoScale));
		stateChanged(new ChangeEvent(enableSound));
		stateChanged(new ChangeEvent(enableTTS));
		stateChanged(new ChangeEvent(enableStuck));
		stateChanged(new ChangeEvent(enableWeb));
	}
	
	public void selectTab(int index) {
		tabs.setSelectedIndex(index);
	}
}
