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

import jgm.ServerManager;
import jgm.gui.GUI;
import jgm.gui.components.SortedListModel;

public class Config extends Dialog implements ActionListener, ChangeListener {
	static Logger log = Logger.getLogger(Config.class.getName());
	
	static final SpinnerNumberModel PORT_SPINNER
		= new SpinnerNumberModel(1, 1, 65536, 1);
	static final SpinnerNumberModel INT_SPINNER
		= new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
	
	static JSpinner makeSpinner(SpinnerNumberModel sm) {
		return new JSpinner(
			new SpinnerNumberModel(
				(Number) sm.getValue(), sm.getMinimum(), sm.getMaximum(), sm.getStepSize())
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
	JTextField serverName;
	JComboBox serverIcon;
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
	JButton reloadSounds;
	
	JCheckBox enableTTS;
	JCheckBox ttsWhisper;
	JCheckBox ttsSay;
	JCheckBox ttsGM;
	JCheckBox ttsStatus;
	
	JPanel alerts;
	JSpinner  minAlertLevel;
	JCheckBox enableAutoUrgent;
	JCheckBox enableAlerts;
	JCheckBox alertOnDeath;
	JCheckBox alertOnChat;
	JCheckBox alertOnFollow;
	JCheckBox alertOnPVP;
	JCheckBox alertOnStatus;
	JCheckBox alertOnLoot;
	JCheckBox alertOnOther;
	
	
	JPanel loot;
	JComboBox phatLootLevel;
	SortedListModel ahLootListData;
	JList ahLootList;
	JTextField ahLootAddField;
	JButton ahLootAddBtn;
	JButton ahLootRemoveBtn;
	JButton ahLootRestoreBtn;
	
	
	JPanel stuck;
	JCheckBox enableStuck;
	JSpinner stuckLimit;
	JSpinner stuckTimeout;

	JCheckBox restartOnException;
	JSpinner restartOnExceptionTime;
	JSpinner restartOnExceptionDelay;
	
	JCheckBox restartOnInventory;
	JSpinner restartOnInventoryTime;
	JSpinner restartOnInventoryDelay;
	
	JCheckBox restartOnFlight;
	JSpinner restartOnFlightTime;
	JSpinner restartOnFlightDelay;
	
	ButtonGroup onRestartGroup;
	JRadioButton onRestartNothing;
	JRadioButton onRestartShrink;
	JRadioButton onRestartHide;
	
	JPanel web;
	JCheckBox enableWeb;
	JSpinner webPort;
	JSpinner webInterval;
	
	jgm.Config cfg;
//	static javax.swing.border.Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		
	public Config(GUI gui) {
		super(gui, "Configuration");
		cfg = jgm.Config.getInstance();
				
		tabs = new JTabbedPane(JTabbedPane.LEFT);
		
		update = new JButton("Save Settings");
		update.setMnemonic(KeyEvent.VK_S);
		update.addActionListener(this);
		
		close = new JButton("Cancel");
		close.setMnemonic(KeyEvent.VK_C);
		close.addActionListener(this);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(new JLabel("* Indicates server specific setting.", JLabel.CENTER), BorderLayout.CENTER);
		southPanel.add(Dialog.makeNiceButtons(update, close), BorderLayout.SOUTH);
		
		add(southPanel, BorderLayout.SOUTH);
		
		
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
		
		showTray = new JCheckBox("Show Tray Icon", cfg.getBool("general.showtray"));
		showTray.addChangeListener(this);
		c.gridy++; c.gridx = 0; c.gridwidth = 2;
		general.add(showTray, c);
		
		minToTray = new JCheckBox("Minimize To Tray", cfg.getBool("general.mintotray"));
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
		wowDbSite.setPrototypeDisplayValue("X");
		wowDbSite.setEditable(true);
		wowDbSite.setSelectedItem(cfg.get("general.wowdb"));		
		
		c.gridx = 0; c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		general.add(new JLabel("WoW DB Site: "), c);
		
		c.gridx++; c.weightx = 1.0;
		general.add(wowDbSite, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 1.0;
		c.weighty = 1.0; c.gridwidth = 2;
		general.add(new JLabel(), c);
		c.weighty = 0.0;
		
		addTab("General", general);
		//p.add(status);
		
		
		// net config pane
		net = new JPanel(new GridBagLayout());
		//GUI.setTitleBorder(net, "Network");
		//net.setBorder(
		//	BorderFactory.createTitledBorder(lineBorder, "Network"));
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0;
		net.add(new JLabel("* Name: "), c);
		
		serverName = new JTextField();
		c.gridx++; c.weightx = 1.0;
		net.add(serverName, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("* Icon: "), c);
		
		serverIcon = new JComboBox(GUI.ICONS);
		c.gridx++; c.weightx = 1.0;
		net.add(serverIcon, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("* Host: "), c);
		
		host = new JTextField();
//		host.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(host, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("* Port: " ), c);
		
		port = makeSpinner(PORT_SPINNER);
		port.setEditor(new JSpinner.NumberEditor(port, "#")); // prevent comma grouping
//		port.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(port, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		net.add(new JLabel("* Password: " ), c);
		
		password = new JPasswordField();
//		password.addActionListener(this);
		c.gridx++; c.weightx = 1.0;
		net.add(password, c);
		
		netReconnect = new JCheckBox("Auto Reconnect", cfg.getBool("net.autoreconnect"));
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
		
		addTab("Server", net);
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
		screenshotAutoScale = new JCheckBox("Automatically adjust scale to fit window", cfg.getBool("screenshot.autoscale"));
		screenshotAutoScale.addChangeListener(this);
		screenshot.add(screenshotAutoScale, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		screenshot.add(new JLabel("* Scale: "), c);
		
		screenshotScale =
			new JSlider(JSlider.HORIZONTAL, 10, 100, gui.sm.getInt("screenshot.scale"));
		screenshotScale.setMajorTickSpacing(30);
		screenshotScale.setMinorTickSpacing(10);
		screenshotScale.setPaintTicks(true);
		screenshotScale.setPaintLabels(true);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotScale, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		screenshot.add(new JLabel("Quality: "), c);
		
		screenshotQuality =
			new JSlider(JSlider.HORIZONTAL, 10, 100, cfg.getInt("screenshot.quality"));
			
		screenshotQuality.setMajorTickSpacing(30);
		screenshotQuality.setMinorTickSpacing(10);
		screenshotQuality.setPaintTicks(true);
		screenshotQuality.setPaintLabels(true);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotQuality, c);
		
//		c.gridx = 0; c.gridy++; c.weightx = 0.0;
//		screenshot.add(new JLabel("* Buffer Size (MB): "), c);
//		
		screenshotBuffer = new JSpinner(
			new SpinnerNumberModel(1.0, 0.5, 10.0, 0.1)
		);
//		c.gridx++; c.weightx = 1.0;
//		screenshot.add(screenshotBuffer, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		screenshot.add(new JLabel("Update Timeout (s): "), c);
		
		screenshotTimeout = makeSpinner(INT_SPINNER);
		c.gridx++; c.weightx = 1.0;
		screenshot.add(screenshotTimeout, c);
		
//		c.gridx = 0; c.gridy++; c.gridwidth = 2;
//		ssInfo = new JLabel("", JLabel.CENTER);
//		screenshot.add(ssInfo, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0;
		screenshot.add(new JLabel(), c);
		c.weighty = 0.0;
		
		addTab("Screenshot", screenshot);
		//p.add(screenshot);
		
		
		/// sound config
		
		JPanel tmp = new JPanel(new GridLayout(1, 0, 10, 10));
			
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0; c.weighty = 0.0;
		enableSound = new JCheckBox("Enable Sound", cfg.getBool("sound.enabled"));
		enableSound.addChangeListener(this);
		sound.add(enableSound, c);
		
		c.gridy++; c.gridwidth = 1; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundWhisper = new JCheckBox("On Whisper", cfg.getBool("sound.whisper"));
		sound.add(soundWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundSay = new JCheckBox("On Say", cfg.getBool("sound.say"));
		sound.add(soundSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundGM = new JCheckBox("On GM Event", cfg.getBool("sound.gm"));
		sound.add(soundGM, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundFollow = new JCheckBox("On Follower Alert", cfg.getBool("sound.follow"));
		sound.add(soundFollow, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundPVP = new JCheckBox("On PVP Attacker Alert", cfg.getBool("sound.pvp"));
		sound.add(soundPVP, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundStuck = new JCheckBox("On Stuck Too Many Times", cfg.getBool("sound.stuck"));
		sound.add(soundStuck, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		soundStatus = new JCheckBox("On Status Event", cfg.getBool("sound.status"));
		soundStatus.setToolTipText("This includes when your character dies.");
		sound.add(soundStatus, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		sound = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weighty = 0.0;
		enableTTS = new JCheckBox("Enable TTS", cfg.getBool("sound.tts.enabled"));
		enableTTS.addChangeListener(this);
		sound.add(enableTTS, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0; c.gridwidth = 1;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsWhisper = new JCheckBox("On Whisper", cfg.getBool("sound.tts.whisper"));
		sound.add(ttsWhisper, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsSay = new JCheckBox("On Say", cfg.getBool("sound.tts.say"));
		sound.add(ttsSay, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsGM = new JCheckBox("On GM Event", cfg.getBool("sound.tts.gm"));
		sound.add(ttsGM, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		sound.add(new JLabel("    "), c);
		
		c.gridx++; c.weightx = 1.0;
		ttsStatus = new JCheckBox("On Status Event", cfg.getBool("sound.tts.status"));
		ttsStatus.setToolTipText("This includes when JGlideMon connects/disconnects with Glider.");
		sound.add(ttsStatus, c);
		
		c.gridy++; c.weighty = 1.0;
		sound.add(new JLabel(), c);
		
		tmp.add(sound);
		
		sound = new JPanel(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		c.weighty = 0.0; c.weightx = 1.0;
		sound.add(tmp, c);
		
		reloadSounds = new JButton("Reload Custom Sounds");
		reloadSounds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						jgm.util.Sound.reload();
					}
				}, "SoundReloader").start();
			}
		});
		c.gridy++;
		sound.add(makeNiceButtons(reloadSounds), c);
		
		addTab("Sound/TTS", sound);
		//p.add(sound);
		
		
		
		/// alert config
		
		alerts = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.weighty = 0.0;
		c.weightx = 0.0; c.gridwidth = 1;
		alerts.add(new JLabel("Minimum Character Level: "), c);
		
		minAlertLevel = makeSpinner(INT_SPINNER);
		minAlertLevel.setToolTipText("Only display alerts when character is at least this level");
		c.gridx++; c.weightx = 1.0; c.gridwidth = 1;
		alerts.add(minAlertLevel, c);
		
		enableAutoUrgent = new JCheckBox("Automatically switch to Urgent Chat tab");
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		alerts.add(enableAutoUrgent, c);
		
		enableAlerts = new JCheckBox("Enable Popup Alerts");
		enableAlerts.addChangeListener(this);
		enableAlerts.setToolTipText("When enabled, popup alerts will be displayed when the window is not active");
		c.gridy++;
		alerts.add(enableAlerts, c);
		
		alertOnDeath = new JCheckBox("On Death");
		alertOnChat = new JCheckBox("On Chat");
		alertOnFollow = new JCheckBox("On Follow");
		alertOnPVP = new JCheckBox("On PVP");
		alertOnStatus = new JCheckBox("On Status");
		alertOnStatus.setToolTipText("Display alert when Glider stops");
		alertOnLoot = new JCheckBox("On Phat Loot");
		alertOnOther = new JCheckBox("On Other");
		alertOnOther.setToolTipText("Display alert when a log entry starting with \"!\" is received");
		
		int oldY = c.gridy;
		
		JPanel leftCol = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.gridwidth = 1;
		leftCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; leftCol.add(alertOnDeath, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		leftCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; leftCol.add(alertOnChat, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		leftCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; leftCol.add(alertOnFollow, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		leftCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; leftCol.add(alertOnPVP, c);
		
		
		JPanel rightCol = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.gridwidth = 1;
		rightCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; rightCol.add(alertOnStatus, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		rightCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; rightCol.add(alertOnLoot, c);
		
		c.gridx = 0; c.gridy++; c.weightx = 0.0;
		rightCol.add(new JLabel("    "), c);
		c.gridx++; c.weightx = 1.0; rightCol.add(alertOnOther, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weighty = 1.0;
		rightCol.add(new JLabel(), c);
		
		c.gridx = 0; c.gridy = oldY + 1; c.weightx = 0.5; c.weighty = 0.0;
		c.gridwidth = 1;
		alerts.add(leftCol, c);
		c.gridx++;
		alerts.add(rightCol, c);
		
		c.gridx = 0; c.gridy++; c.weighty = 1.0; c.gridwidth = 2;
		alerts.add(new JLabel(), c);
		
		
		addTab("Alerts", alerts);
		
		
		loot = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.weightx = 0.0; c.weighty = 0.0;
		c.gridwidth = 1; c.insets.bottom = GUI.PADDING;
		loot.add(new JLabel("Minimum \"Phat Loot\" Quality: "), c);
		
		phatLootLevel = new JComboBox(jgm.wow.Quality.values());
		c.gridx++; c.weightx = 1.0;
		loot.add(phatLootLevel, c);
		
		c.insets.bottom = 0;
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		loot.add(new JLabel("<html>The following items will retrieve the median AH price instead of the<br>vendor price. \".*\" is a wildcard, names are case-insensitive."), c);
		
		final ActionListener ahLootAddListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = ahLootAddField.getText().trim();
				ahLootAddField.requestFocusInWindow();
				
				if ("".equals(s)) return;
				
				try {
					java.util.regex.Pattern.compile(s, java.util.regex.Pattern.CASE_INSENSITIVE);
				} catch (java.util.regex.PatternSyntaxException x) {
					JOptionPane.showMessageDialog(Config.this,
						"The pattern you have entered is invalid.\n\n" +
						x.getMessage(),
						"Invalid Pattern",
						JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				ahLootAddField.setText("");
				ahLootListData.add(s);
			}
		};
		
		final ActionListener ahLootRemoveListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] values = ahLootList.getSelectedValues();
				
				for (Object o : values)
					ahLootListData.removeElement(o);
			}
		};
		
		ahLootListData = new SortedListModel();
		ahLootList = new JList(ahLootListData);
		ahLootList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ahLootList.setVisibleRowCount(-1);
		ahLootList.setLayoutOrientation(JList.VERTICAL);
		ahLootList.setAutoscrolls(true);
		ahLootList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					ahLootRemoveListener.actionPerformed(null);
				}
			}
		});
		
		c.gridy++; c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		loot.add(new JScrollPane(ahLootList), c);
		
		ahLootAddField = new JTextField();
		c.gridy++; c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		loot.add(ahLootAddField, c);
		
		ahLootAddField.addActionListener(ahLootAddListener);
		
		ahLootAddBtn = new JButton("Add");
		ahLootAddBtn.addActionListener(ahLootAddListener);
		
		ahLootRemoveBtn = new JButton("Remove Selected");
		ahLootRemoveBtn.addActionListener(ahLootRemoveListener);
		
		ahLootRestoreBtn = new JButton("Restore Defaults");
		ahLootRestoreBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ahLootListData.clear();
				
				String cur = null;
				for (int i = 0; ; i++) {
					try {
						cur = jgm.Config.DEFAULTS.get("loot.ahlist." + i);
					} catch (NullPointerException x) {
						break;
					}
					
					ahLootListData.add(cur);
				}
			}
		});
		
		c.gridy++;
		loot.add(makeNiceButtons(ahLootAddBtn, ahLootRemoveBtn, ahLootRestoreBtn), c);
		
//		c.gridy++; c.weighty = 0.75;
//		loot.add(new JLabel(), c);
		
		
		addTab("Loot", loot);
		
		
		// stuck options
		
		//          Enabled Timeout Limit
		// Stuck       x      #      #
		//                          Delay (ms)
		//
		stuck = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		c.weightx = 1.0; c.weighty = 0.0;
		
		// headers
		stuck.add(new JLabel(), c);
		c.weightx = 0.0; c.gridx++;
		stuck.add(new JLabel("Enabled", JLabel.CENTER), c);
		c.gridx++;
		stuck.add(new JLabel("Timeout (s)", JLabel.CENTER), c);
		c.gridx++;
		stuck.add(new JLabel("Limit", JLabel.CENTER), c);
		
		enableStuck = new JCheckBox();
		enableStuck.setToolTipText("Try to restart when stuck");
		enableStuck.addChangeListener(this);
		c.gridx = 0; c.gridy++; c.insets.bottom = GUI.PADDING;
		stuck.add(new JLabel("Stuck: "), c);
		c.gridx++;
		stuck.add(enableStuck, c);
		
		stuckTimeout = new JSpinner(
				new SpinnerNumberModel(300, 5, 10000, 1)
			);
			stuckTimeout.setToolTipText("Reset stuck timer after this many seconds");
			stuckTimeout.addChangeListener(this);
			c.gridx++;
			stuck.add(stuckTimeout, c);
		
		stuckLimit = new JSpinner(
			new SpinnerNumberModel(5, 0, 10000, 1)
		);
		stuckLimit.setToolTipText("Give up if stuck this many times in a row or 0 to never give up");
		stuckLimit.addChangeListener(this);
		c.gridx++;
		stuck.add(stuckLimit, c);
		
//		c.gridx = 0; c.gridwidth = 2; c.gridy++; c.weighty = 0.0;
//		stuck.add(new JLabel(), c);
	
		// headers
		c.gridy++; c.gridx = 0; c.weightx = 1.0;
		c.insets.bottom = 0;
		stuck.add(new JLabel(), c);
		c.weightx = 0.0; c.gridx++;
		stuck.add(new JLabel(), c);
		c.gridx++;
		stuck.add(new JLabel(), c);
		c.gridx++;
		stuck.add(new JLabel("Delay (ms)", JLabel.CENTER), c);
		
		restartOnException = new JCheckBox();
		restartOnException.setToolTipText("Try to restart when Glider stops as a result of an exception");
		restartOnException.addChangeListener(this);
		c.gridx = 0; c.gridy++;
		stuck.add(new JLabel("Exception: "), c);
		c.gridx++;
		stuck.add(restartOnException, c);
				
		restartOnExceptionTime = makeSpinner(INT_SPINNER);
		restartOnExceptionTime.setToolTipText("Glider will be restarted if an exception occured within this many seconds before stopping");
		restartOnExceptionTime.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnExceptionTime, c);
		
		restartOnExceptionDelay = makeSpinner(INT_SPINNER);
		restartOnExceptionDelay.setToolTipText("Glider will be restarted after waiting this long");
		restartOnExceptionDelay.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnExceptionDelay, c);
		
		
		restartOnInventory = new JCheckBox();
		restartOnInventory.setToolTipText("Try to restart when Glider stops when it can't resupply from a vendor.");
		restartOnInventory.addChangeListener(this);
		c.gridy++; c.gridx = 0;
		stuck.add(new JLabel("Inventory: "), c);
		c.gridx++;
		stuck.add(restartOnInventory, c);
				
		restartOnInventoryTime = Config.makeSpinner(Config.INT_SPINNER);
		restartOnInventoryTime.setToolTipText("Glider will be restarted if an inventory not going up error occured within this many seconds before stopping.");
		restartOnInventoryTime.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnInventoryTime, c);
		
		restartOnInventoryDelay = makeSpinner(INT_SPINNER);
		restartOnInventoryDelay.setToolTipText("Glider will be restarted after waiting this long");
		restartOnInventoryDelay.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnInventoryDelay, c);
		
		restartOnFlight = new JCheckBox();
		restartOnFlight.setToolTipText("<html>Try to restart when Glider stops a profile group when it can't interact with the flight master.<br>This would help with auto-vendoring/repairing profile groups.");
		restartOnFlight.addChangeListener(this);
		c.gridy++; c.gridx = 0;
		c.insets.bottom = GUI.PADDING;
		stuck.add(new JLabel("Flight: "), c);
		c.gridx++;
		stuck.add(restartOnFlight, c);
		
		restartOnFlightTime = Config.makeSpinner(Config.INT_SPINNER);
		restartOnFlightTime.setToolTipText("Glider will be restarted if a flight error occured within this many seconds before stopping.");
		restartOnFlightTime.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnFlightTime, c);
		
		restartOnFlightDelay = makeSpinner(INT_SPINNER);
		restartOnFlightDelay.setToolTipText("Glider will be restarted after waiting this long");
		restartOnFlightDelay.addChangeListener(this);
		c.gridx++;
		stuck.add(restartOnFlightDelay, c);
		
		c.gridwidth = 4; c.weightx = 1.0; c.gridx = 0; c.gridy += 2;
		c.insets.bottom = 0;
		JLabel lbl = new JLabel("After restarting,");
		lbl.setToolTipText("<html>Shrink, hide, or do nothing to the wow window when restarting.<br>This also affects when you send chat.");
		stuck.add(lbl, c);
		
		onRestartGroup = new ButtonGroup();
		
		onRestartNothing = new JRadioButton("do nothing");
		onRestartGroup.add(onRestartNothing);
		c.gridy++;
		stuck.add(onRestartNothing, c);
		
		onRestartShrink = new JRadioButton("shrink the WoW window");
		onRestartGroup.add(onRestartShrink);
		c.gridy++;
		stuck.add(onRestartShrink, c);
		
		onRestartHide = new JRadioButton("hide the WoW window");
		onRestartGroup.add(onRestartHide);
		c.gridy++;
		stuck.add(onRestartHide, c);
		
		c.gridx = 0; c.gridwidth = 2; c.gridy++; c.weighty = 1.0;
		stuck.add(new JLabel(), c);
		
		addTab("Restarter", stuck);
		
		
		
		// web server options
		web = new JPanel(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1; c.weighty = 0.0; c.weightx = 1.0;
		enableWeb = new JCheckBox(" * Enable Web-Server");
		enableWeb.addChangeListener(this);
		web.add(enableWeb, c);
		
		JLabel tmpLbl = new JLabel("  * Port: ");
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
		
		addTab("Web", web);
		
		
		
		
		add(tabs, BorderLayout.CENTER);
		
		// ensuring appropriate groups are enabled/disabled
		// is taken care of in onShow()
		
		makeVisible();
	}
	
	private void addTab(String title, Component panel) {
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		tmp.add(panel, BorderLayout.CENTER);
		tabs.addTab(title, tmp);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			log.fine("Canceling config changes");
			setVisible(false);
			return;
		}
		
		boolean oldDebug = cfg.getBool("general.debug");
		
		if (oldDebug != debug.isSelected()) {
			cfg.set("general.debug", debug.isSelected());
			jgm.JGlideMon.debug = debug.isSelected();
			
			// this deletes the current log
			jgm.logging.Log.reloadConfig();
		}
		
		String newName = serverName.getText().trim();
		if (!gui.sm.name.equals(newName)) {
			if (jgm.ServerManager.contains(newName)) {
				JOptionPane.showMessageDialog(this,
					"A server named \"" + newName + "\" already exists.\n" +
					"Please choose a different name.",
					"Error", JOptionPane.ERROR_MESSAGE);
				
				tabs.setSelectedIndex(1);
				serverName.selectAll();
				serverName.requestFocusInWindow();
				
				return;
			} else {
				gui.sm.set("name", serverName.getText());
				
				// to resort it
				synchronized (ServerManager.managers) {
					java.util.Collections.sort(ServerManager.managers);
				}
			}
		}
		
		log.finest(String.format("Old server: %s:%s = %s",
			gui.sm.host, gui.sm.port, gui.sm.password));
		
		boolean reconnect = !gui.sm.host.equals(host.getText());
		gui.sm.set("net.host", host.getText());
		reconnect = reconnect || gui.sm.port != (Integer) port.getValue();
		gui.sm.set("net.port", port.getValue());
		reconnect = reconnect || !gui.sm.password.equals(password.getText());		
		gui.sm.set("net.password", password.getText());
		
		gui.sm.set("icon", serverIcon.getSelectedIndex());
		
		log.finest(String.format("New server: %s:%s = %s",
			gui.sm.host, gui.sm.port, gui.sm.password));
		
		gui.setTitle();
		
		if (!gui.sm.firstRun && reconnect) {
			log.finer("Server info changed, need to reconnect...");
			
			new Thread(new Runnable() {
				public void run() {
					Thread t = gui.sm.connector.disconnect();
					
					if (t != null) {
						try {
							t.join();
						} catch (InterruptedException e) {}
					}
					
					gui.sm.connector.connect();
				}
			}).start();
		}
		
		cfg.set("net.autoreconnect", netReconnect.isSelected());			
		cfg.set("net.autoreconnectdelay", netReconnectDelay.getValue());
		cfg.set("net.autoreconnecttries", netReconnectTries.getValue());

		cfg.set("status.updateinterval", statusInterval.getValue());

		cfg.set("log.maxentries", maxLogEntries.getValue());

		
		cfg.set("general.showtray", showTray.isSelected());
		cfg.set("general.mintotray", minToTray.isSelected());
		cfg.set("general.wowdb", wowDbSite.getSelectedItem());
		
		
		cfg.set("screenshot.updateinterval", screenshotInterval.getValue());
		cfg.set("screenshot.autoscale", screenshotAutoScale.isSelected());
		gui.sm.set("screenshot.scale", screenshotScale.getValue());
		cfg.set("screenshot.quality", screenshotQuality.getValue());
		gui.sm.set("screenshot.buffer", ((Double) screenshotBuffer.getValue()).doubleValue());
		cfg.set("screenshot.timeout", screenshotTimeout.getValue());

		
		// would be null on first-time startup
		if (null != gui.sm.ssUpdater)
			gui.sm.ssUpdater.sentSettings = false;
		
		cfg.set("sound.enabled", enableSound.isSelected());
		cfg.set("sound.whisper", soundWhisper.isSelected());
		cfg.set("sound.say", soundSay.isSelected());
		cfg.set("sound.gm", soundGM.isSelected());
		cfg.set("sound.follow", soundFollow.isSelected());
		cfg.set("sound.pvp", soundPVP.isSelected());
		cfg.set("sound.stuck", soundStuck.isSelected());
		cfg.set("sound.status", soundStatus.isSelected());
		cfg.set("sound.tts.enabled", enableTTS.isSelected());
		cfg.set("sound.tts.whisper", ttsWhisper.isSelected());
		cfg.set("sound.tts.say", ttsSay.isSelected());
		cfg.set("sound.tts.gm", ttsGM.isSelected());
		cfg.set("sound.tts.status", ttsStatus.isSelected());
		
		cfg.set("alerts.minlevel", minAlertLevel.getValue());
		cfg.set("alerts.autourgent", enableAutoUrgent.isSelected());
		cfg.set("alerts.enabled", enableAlerts.isSelected());
		cfg.set("alerts.death", alertOnDeath.isSelected());
		cfg.set("alerts.chat", alertOnChat.isSelected());
		cfg.set("alerts.follow", alertOnFollow.isSelected());
		cfg.set("alerts.pvp", alertOnPVP.isSelected());
		cfg.set("alerts.status", alertOnStatus.isSelected());
		cfg.set("alerts.loot", alertOnLoot.isSelected());
		cfg.set("alerts.other", alertOnOther.isSelected());
		
		cfg.set("loot.phatlevel", phatLootLevel.getSelectedIndex());
		cfg.setArray("loot.ahlist", ahLootListData.toArray());
		
		cfg.set("stuck.enabled", enableStuck.isSelected());
		cfg.set("stuck.limit", ((Integer) stuckLimit.getValue()).intValue());
		cfg.set("stuck.timeout", ((Integer) stuckTimeout.getValue()).intValue());
		
		cfg.set("restarter.exception.enabled", restartOnException.isSelected());
		cfg.set("restarter.exception.timeout", restartOnExceptionTime.getValue());
		cfg.set("restarter.exception.delay", restartOnExceptionDelay.getValue());
		cfg.set("restarter.inventory.enabled", restartOnInventory.isSelected());
		cfg.set("restarter.inventory.timeout", restartOnInventoryTime.getValue());
		cfg.set("restarter.inventory.delay", restartOnInventoryDelay.getValue());
		cfg.set("restarter.flight.enabled", restartOnFlight.isSelected());
		cfg.set("restarter.flight.timeout", restartOnFlightTime.getValue());
		cfg.set("restarter.flight.delay", restartOnFlightDelay.getValue());
		
		
		cfg.set("restarter.onrestart",
			onRestartShrink.isSelected()
			? "shrink"
			: onRestartHide.isSelected()
			  ? "hide"
			  : "nothing"
		);
		
		boolean oldWebEnabled = gui.sm.getBool("web.enabled");
		int oldWebPort = gui.sm.getInt("web.port");
		
		gui.sm.set("web.enabled", enableWeb.isSelected());
		gui.sm.set("web.port", webPort.getValue());
		cfg.set("web.updateinterval", webInterval.getValue());

		
		// stop httpd if needed
		if ((oldWebEnabled && oldWebPort != gui.sm.getInt("web.port")) ||
			(!enableWeb.isSelected())) {
			gui.sm.stopHttpd();
		}
		
		// restart on different port if needed
		if ((enableWeb.isSelected() && oldWebPort != gui.sm.getInt("web.port")) ||
			(!oldWebEnabled && enableWeb.isSelected()) ||
			(enableWeb.isSelected())) {
			try {
				gui.sm.startHttpd();
			} catch (java.io.IOException x) {
				JOptionPane.showMessageDialog(gui.frame,
					"Unable to start web-server.\n" +
					"Port " + gui.sm.get("web.port") + " is unavailible.",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
		
		ServerManager.saveConfig();
		jgm.Config.write();
		
		setVisible(false);
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == showTray) {
			boolean state = showTray.isEnabled() && showTray.isSelected();
			
			minToTray.setEnabled(state);
			enableAlerts.setEnabled(state);
			
			if (state) {
				gui.tray.enable();
			} else {
				gui.tray.disable();
			}
			
			// since it depends on this being enabled
			stateChanged(new ChangeEvent(enableAlerts));
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
		} else if (e.getSource() == enableAlerts) {
			boolean state =	enableAlerts.isEnabled() && enableAlerts.isSelected();
			
			alertOnDeath.setEnabled(state);
			alertOnChat.setEnabled(state);
			alertOnFollow.setEnabled(state);
			alertOnPVP.setEnabled(state);
			alertOnStatus.setEnabled(state);
			alertOnLoot.setEnabled(state);
			alertOnOther.setEnabled(state);
		} else if (e.getSource() == enableStuck) {
			boolean state = enableStuck.isEnabled() && enableStuck.isSelected();
			
			stuckLimit.setEnabled(state);
			stuckTimeout.setEnabled(state);
		} else if (e.getSource() == restartOnException) {
			boolean state = restartOnException.isEnabled() && restartOnException.isSelected();
			restartOnExceptionTime.setEnabled(state);
			restartOnExceptionDelay.setEnabled(state);
		} else if (e.getSource() == restartOnInventory) {
			boolean state = restartOnInventory.isEnabled() && restartOnInventory.isSelected();
			restartOnInventoryTime.setEnabled(state);
			restartOnInventoryDelay.setEnabled(state);
		} else if (e.getSource() == restartOnFlight) {
			boolean state = restartOnFlight.isEnabled() && restartOnFlight.isSelected();
			restartOnFlightTime.setEnabled(state);
			restartOnFlightDelay.setEnabled(state);
		} else if (e.getSource() == enableWeb) {
			boolean state = enableWeb.isEnabled() && enableWeb.isSelected();
			
			webPort.setEnabled(state);
			webInterval.setEnabled(state);
		}
	}
	
	protected void onShow() {
		//super.onShow();

		debug.setSelected(cfg.getBool("general.debug"));
		statusInterval.setValue(cfg.getInt("status.updateinterval"));
		maxLogEntries.setValue(cfg.getInt("log.maxentries"));
		showTray.setEnabled(jgm.gui.Tray.isSupported());
		showTray.setSelected(cfg.getBool("general.showtray"));
		minToTray.setSelected(cfg.getBool("general.mintotray"));
		
		serverName.setText(gui.sm.name);
		
		try {
			serverIcon.setSelectedIndex(gui.sm.getInt("icon"));
		} catch (IllegalArgumentException e) {
			serverIcon.setSelectedIndex(0);
		}
		
		host.setText(gui.sm.host);
		port.setValue(gui.sm.port);
		password.setText(gui.sm.password);
		netReconnect.setSelected(cfg.getBool("net.autoreconnect"));
		netReconnectDelay.setValue(cfg.getInt("net.autoreconnectdelay"));
		netReconnectTries.setValue(cfg.getInt("net.autoreconnecttries"));
		
		screenshotInterval.setValue(cfg.getInt("screenshot.updateinterval"));
		screenshotAutoScale.setSelected(cfg.getBool("screenshot.autoscale"));
		screenshotScale.setValue(gui.sm.getInt("screenshot.scale"));
		screenshotQuality.setValue(cfg.getInt("screenshot.quality"));
		screenshotBuffer.setValue(gui.sm.getDouble("screenshot.buffer"));
		screenshotTimeout.setValue(cfg.getInt("screenshot.timeout"));
		
		Icon i = gui.tabsPane.screenshotTab.ssLabel.getIcon();
		
//		if (i != null) {
//			int width = i.getIconWidth();
//			int height = i.getIconHeight();
//			int size = width * height * 3;
//			float sizeMb = (float) size / 1048576; 
//			
//			ssInfo.setText(
//				String.format(
//					"<html><br>Your last screenshot was %sx%s pixels.<br>\n" +
//					"Uncompressed it could be at most %.2fMB.",
//					width,
//					height,
//					sizeMb
//				)
//			);
//			
//			this.pack();
//		}
		
		enableSound.setSelected(cfg.getBool("sound.enabled"));
		soundWhisper.setSelected(cfg.getBool("sound.whisper"));
		soundSay.setSelected(cfg.getBool("sound.say"));
		soundGM.setSelected(cfg.getBool("sound.gm"));
		soundFollow.setSelected(cfg.getBool("sound.follow"));
		soundPVP.setSelected(cfg.getBool("sound.pvp"));
		soundStuck.setSelected(cfg.getBool("sound.stuck"));
		soundStatus.setSelected(cfg.getBool("sound.status"));
		
		enableTTS.setEnabled(jgm.util.Speech.isSupported());
		enableTTS.setSelected(cfg.getBool("sound.tts.enabled"));
		ttsWhisper.setSelected(cfg.getBool("sound.tts.whisper"));
		ttsSay.setSelected(cfg.getBool("sound.tts.say"));
		ttsGM.setSelected(cfg.getBool("sound.tts.gm"));
		ttsStatus.setSelected(cfg.getBool("sound.tts.status"));
		
		minAlertLevel.setValue(cfg.getInt("alerts.minlevel"));
		enableAutoUrgent.setSelected(cfg.getBool("alerts.autourgent"));
		enableAlerts.setEnabled(jgm.gui.Tray.isSupported());
		enableAlerts.setSelected(cfg.getBool("alerts.enabled"));
		alertOnDeath.setSelected(cfg.getBool("alerts.death"));
		alertOnChat.setSelected(cfg.getBool("alerts.chat"));
		alertOnFollow.setSelected(cfg.getBool("alerts.follow"));
		alertOnPVP.setSelected(cfg.getBool("alerts.pvp"));
		alertOnStatus.setSelected(cfg.getBool("alerts.status"));
		alertOnLoot.setSelected(cfg.getBool("alerts.loot"));
		alertOnOther.setSelected(cfg.getBool("alerts.other"));
		
		try {
			phatLootLevel.setSelectedIndex(cfg.getInt("loot.phatquality"));
		} catch (IllegalArgumentException e) {}
		
		ahLootListData.clear();
		for (String cur : cfg.getArray("loot.ahlist")) {			
			ahLootListData.add(cur);
		}
		
		enableStuck.setSelected(cfg.getBool("stuck.enabled"));
		stuckLimit.setValue(cfg.getInt("stuck.limit"));
		stuckTimeout.setValue(cfg.getInt("stuck.timeout"));
		
		restartOnException.setSelected(cfg.getBool("restarter.exception.enabled"));
		restartOnExceptionTime.setValue(cfg.getInt("restarter.exception.timeout"));
		restartOnExceptionDelay.setValue(cfg.getInt("restarter.exception.delay"));
		restartOnInventory.setSelected(cfg.getBool("restarter.inventory.enabled"));
		restartOnInventoryTime.setValue(cfg.getInt("restarter.inventory.timeout"));
		restartOnInventoryDelay.setValue(cfg.getInt("restarter.inventory.delay"));
		restartOnFlight.setSelected(cfg.getBool("restarter.flight.enabled"));
		restartOnFlightTime.setValue(cfg.getInt("restarter.flight.timeout"));
		restartOnFlightDelay.setValue(cfg.getInt("restarter.flight.delay"));
		
		String tmp = cfg.get("restarter.onrestart");
		
		if (tmp.equals("shrink"))
			onRestartShrink.setSelected(true);
		else if (tmp.equals("hide"))
			onRestartHide.setSelected(true);
		else
			onRestartNothing.setSelected(true);
		
		enableWeb.setSelected(gui.sm.getBool("web.enabled"));
		webPort.setValue(gui.sm.getInt("web.port"));
		webInterval.setValue(cfg.getInt("web.updateinterval"));
		
		// to initialize enabled/disabled states
		stateChanged(new ChangeEvent(showTray));
		stateChanged(new ChangeEvent(netReconnect));
		stateChanged(new ChangeEvent(screenshotAutoScale));
		stateChanged(new ChangeEvent(enableSound));
		stateChanged(new ChangeEvent(enableTTS));
		stateChanged(new ChangeEvent(enableAlerts));
		stateChanged(new ChangeEvent(enableStuck));
		stateChanged(new ChangeEvent(restartOnException));
		stateChanged(new ChangeEvent(restartOnInventory));
		stateChanged(new ChangeEvent(restartOnFlight));
		stateChanged(new ChangeEvent(enableWeb));
	}
	
	public void selectTab(int index) {
		tabs.setSelectedIndex(index);
	}
}
