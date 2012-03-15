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
package jgm.gui;

import jgm.Config;
import jgm.JGlideMon;
import jgm.ServerManager;
import jgm.glider.*;
import jgm.gui.components.JStatusBar;
import jgm.gui.panes.CharInfoPane;
import jgm.gui.panes.ControlPane;
import jgm.gui.panes.ExperiencePane;
import jgm.gui.panes.MobInfoPane;
import jgm.gui.panes.SendChatPane;
import jgm.gui.panes.TabsPane;
import jgm.util.Util;

import java.util.logging.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class GUI 
	implements ActionListener, java.util.Observer, ContainerListener {
	
	static Logger log = Logger.getLogger(GUI.class.getName());
	
	static final ImageIcon ONLINE_ICON =
		new ImageIcon(JGlideMon.class.getResource("resources/images/status/online.png"));
	static final ImageIcon OFFLINE_ICON =
		new ImageIcon(JGlideMon.class.getResource("resources/images/status/offline.png"));
	
//	public static final String[] ICON_NAMES = {
//		"Default", "Druid", "Hunter", "Mage",
//		"Paladin", "Priest", "Rogue", "Shaman",
//		"Warlock", "Warrior"
//	};
	
	public static final ImageIcon[] ICONS = new ImageIcon[11];
	
	static {
		ICONS[0] = new ImageIcon(JGlideMon.class.getResource("resources/images/stitch/icon.png"));
		ICONS[1] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/druid.png"));
		ICONS[2] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/hunter.png"));
		ICONS[3] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/mage.png"));
		ICONS[4] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/paladin.png"));
		ICONS[5] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/priest.png"));
		ICONS[6] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/rogue.png"));
		ICONS[7] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/shaman.png"));
		ICONS[8] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/warlock.png"));
		ICONS[9] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/warrior.png"));
		ICONS[10] = new ImageIcon(JGlideMon.class.getResource("resources/images/classes/death_knight.png"));
	}
	
	public static final int PADDING = 10;
	static Config cfg = Config.c;
	public static final String BASE_TITLE = "JGlideMon " + JGlideMon.version;
	
	public static enum ScreenshotState {
		NORMAL, MAXIMIZED, FULLSCREEN
	}
	
	public final ServerManager sm;
	
	public JFrame frame;
	public Tray tray;
	private SendChatPane sendChatPane;
	JStatusBar statusBar;
	
	// elements
	public CharInfoPane   charInfo;
	public MobInfoPane    mobInfo;
	public ControlPane    ctrlPane;
	public ExperiencePane xpPane;
	public TabsPane       tabsPane;
	
	// dialogs
	jgm.gui.dialogs.About aboutFrame;
	jgm.gui.dialogs.Config configDialog;
	jgm.gui.dialogs.ParseLogFile parseLogDialog;
	
	
	// for fullscreen screenshot
	public JPanel ssPanel = null;
	public JPanel mainPanel;
	private JPanel footer;
	public ScreenshotState ssState = ScreenshotState.NORMAL;
	Rectangle lastWindowBounds = null;
	int lastWindowState = 0;
	
	// menu stuff
	public final Menu menu = new Menu();
	
	// this class is just to give a menu namespace for the
	// necessary variables
	public class Menu {
		JMenuBar  bar;
		
		JMenu     file;
		JMenuItem clearItems;
		JMenuItem clearIcons;
		JMenuItem saveIcons;
		JMenuItem loadIcons;
		JMenuItem config;
		JMenuItem exit;
		
		JMenu     screenshot;
		JMenu     ssMode;
		public JRadioButtonMenuItem normalSS;
		public JRadioButtonMenuItem maxSS;
		public JRadioButtonMenuItem fullSS;
		
		public JCheckBoxMenuItem sendKeys;
		public JMenuItem refreshSS;
		
		JMenu     ssSize;
		public JMenuItem ssRestoreActivate;
		JMenuItem ssShrinkOthers;
		JMenuItem ssHideOthers;
		public JMenuItem ssShrink;
		JMenuItem ssShrinkAll;
		public JMenuItem ssHide;
		JMenuItem ssHideAll;
		
		JMenu     logs;
		JMenuItem clearCurLog;
		JMenuItem clearAllLogs;
		JMenuItem parseLogFile;
		
		JMenu     servers;
		JMenuItem addServer;
		JMenuItem removeServer;
		JMenuItem activateServers;
		JMenuItem connectServers;
		JMenuItem disconnectServers;
		JMenu     serversSub;
		
		JMenu     help;
		JMenuItem helpContents;
		JMenuItem helpOnline;
		JMenuItem debug;
		JMenuItem about;
	}
	
	ServerManager.Listener mySmListener = new ServerManager.Listener() {
    	void doit() {
    		doServersMenu();
    	}
    	
		public void serverAdded(ServerManager sm) { doit();}
		public void serverRemoved(ServerManager sm) { doit(); }
		public void serverSuspended(ServerManager sm) { doit(); } 
		public void serverResumed(ServerManager sm) { doit(); }
		public void serverPropChanged(ServerManager sm, String prop, Object value) {
			if (prop.equals("name")) {
				doit();
			}
		}
    };
    
    public WindowAdapter myWindowAdapter = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			int ret =
				// don't show if there's only one active server
				ServerManager.getActiveCount() == 1
				? JOptionPane.YES_OPTION :
				JOptionPane.showConfirmDialog(frame,
					"Yes: Close all instances of JGlideMon\n" +
					"No: Close this server's instance of JGlideMon\n" +
					"Cancel: Do nothing.",
					"Do you want to exit?",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			
			switch (ret) {
				case JOptionPane.YES_OPTION:
					JGlideMon.instance.destroy();
					break;
					
				case JOptionPane.NO_OPTION:
					sm.suspend();
					break;
			}
		}
	};
	
	public GUI(final ServerManager sm) {
		this.sm = sm;

		frame = new JFrame(BASE_TITLE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.setSize(sm.getInt("window.width"), sm.getInt("window.height"));
		frame.setLocation(sm.getInt("window.x"), sm.getInt("window.y"));

		tray = new Tray(this);
		
		setTitle();
		setIcon();
		
		if (sm.getBool("window.maximized")) {
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
		
		frame.addWindowListener(myWindowAdapter);
		
		frame.addWindowStateListener(new WindowStateListener() {
			final ServerManager sm = GUI.this.sm;
			public void windowStateChanged(WindowEvent e) {
				if (JFrame.MAXIMIZED_BOTH ==
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					//System.out.println("Window is maximized");
					sm.set("window.maximized", true);
				} else {
					//System.out.println("Window not maximized");
					sm.set("window.maximized", false);
				}
				
				if (JFrame.ICONIFIED ==
					(frame.getExtendedState() & JFrame.ICONIFIED)) {
					
					// minimize to tray
					if (Tray.isSupported() &&
						cfg.getBool("general.showtray") &&
						cfg.getBool("general.mintotray")) {
						frame.setVisible(false);
					}
				}
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			final ServerManager sm = GUI.this.sm;
			public void componentResized(ComponentEvent e) {
				Dimension s = frame.getSize();
				//System.out.println("Window resized: " + s);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					sm.set("window.width", s.width);					
					sm.set("window.height", s.height);
				}
				
				// request to update the screenshot's scale
				try {
					sm.ssUpdater.redoScale = true;
				} catch (Exception x) {}
			}
			
			public void componentMoved(ComponentEvent e) {
				Point p = frame.getLocation();
				//System.out.println("Window moved: " + p);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					sm.set("window.x", p.x);
					sm.set("window.y", p.y);
				}
			}
		});

		frame.setLayout(new BorderLayout());
				
		////////////////
		// set up panels
		mainPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0; c.weighty = 0.0;

		charInfo = new CharInfoPane(this);
		c.gridx = 0; c.gridy = 0; c.weightx = 0.25;
		c.insets.top = PADDING;
		c.insets.left = PADDING;
		mainPanel.add(charInfo, c);

		mobInfo = new MobInfoPane(this);
		c.gridx = 1; c.gridy = 0; c.weightx = 0.75;
		mainPanel.add(mobInfo, c);

		ctrlPane = new ControlPane(this);
		sm.connector.addListener(ctrlPane);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		c.insets.right = PADDING;
		mainPanel.add(ctrlPane, c);

		xpPane = new ExperiencePane(this);
		c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
		c.insets.left = 0;
		c.insets.right = 0;
		mainPanel.add(xpPane, c);

		tabsPane = new TabsPane(this);
		JPanel tabsPanel = new JPanel(new BorderLayout());
		tabsPanel.add(tabsPane, BorderLayout.CENTER);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
		mainPanel.add(tabsPanel, c);

		frame.add(mainPanel, BorderLayout.CENTER);
		
		addKeyAndContainerListenerRecursively(tabsPane.screenshotTab, this, frame);
		
		
		//////////////
		// set up menu
		menu.bar  = new JMenuBar();
		
		// file
		menu.file = new JMenu("File");
		menu.file.setMnemonic(KeyEvent.VK_F);
		menu.bar.add(menu.file);

		menu.config = doMenuItem("Configuration", KeyEvent.VK_C, menu.file, this);		
		menu.file.addSeparator();
		
		menu.clearItems = doMenuItem("Clear Item Cache", menu.file, this);
		menu.clearIcons = doMenuItem("Clear Icon Cache", menu.file, this);
		
		if (jgm.JGlideMon.debug) {
			menu.saveIcons = doMenuItem("Save Cache", menu.file, this);
			menu.loadIcons = doMenuItem("Load Cache", menu.file, this);
		}
		
		menu.file.addSeparator();
		
		menu.exit = doMenuItem("Exit", KeyEvent.VK_X, menu.file, this);
		
		// screenshot
		menu.screenshot = new JMenu("Screenshot");
		menu.screenshot.setMnemonic(KeyEvent.VK_S);
		menu.bar.add(menu.screenshot);
		
		menu.ssMode = new JMenu("Display Mode");
		menu.ssMode.setMnemonic('M');
		// added lower down
		
		menu.normalSS = new JRadioButtonMenuItem("Normal");
		menu.normalSS.setMnemonic('N');
		menu.maxSS = new JRadioButtonMenuItem("Maximized");
		menu.maxSS.setMnemonic('M');
		menu.fullSS = new JRadioButtonMenuItem("Fullscreen");
		menu.fullSS.setMnemonic('F');
		menu.fullSS.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		menu.fullSS.setToolTipText("Press F11 to toggle between fullscreen and normal");
		
		ButtonGroup grp = new ButtonGroup();
		grp.add(menu.normalSS);
		grp.add(menu.maxSS);
		grp.add(menu.fullSS);
		
		menu.normalSS.setSelected(true);
		
		ActionListener ssModeAl = new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (e.getSource() == menu.fullSS && ssState == ScreenshotState.FULLSCREEN) {
					// if you press F11 while in full screen
					// it will still be selected so we need to 
					// treat it specially
					
					menu.normalSS.doClick();
					return;
				}
				
				ScreenshotState newState =
					menu.maxSS.isSelected()
					? ScreenshotState.MAXIMIZED
					: menu.fullSS.isSelected()
					  ? ScreenshotState.FULLSCREEN
					  : ScreenshotState.NORMAL;

				// i wouldn't expect this to happen
				if (newState == ssState) {
					log.warning("Old and new ss state are the same: " + newState);
					return;
				}
				
				switch (ssState) {
				case NORMAL:
					switch (newState) {
					case MAXIMIZED:						
					case FULLSCREEN:
						doMaximizedSS(newState);
						break;
					}
					
					break;
					
				case MAXIMIZED:
					switch (newState) {
					case NORMAL:
					case FULLSCREEN:
						doMaximizedSS(newState);
						break;
					}
					
					break;
					
				case FULLSCREEN:
					switch (newState) {
					case NORMAL:
					case MAXIMIZED:
						doMaximizedSS(newState);
						break;
					}
					
					break;
				}
				
				ssState = newState;
				sm.ssUpdater.redoScale = true;
			}
		};
		
		menu.normalSS.addActionListener(ssModeAl);
		menu.maxSS.addActionListener(ssModeAl);
		menu.fullSS.addActionListener(ssModeAl);
		
		menu.ssMode.add(menu.normalSS);
		menu.ssMode.add(menu.maxSS);
		menu.ssMode.add(menu.fullSS);
		
		menu.sendKeys = new JCheckBoxMenuItem("Enable Sending Keystrokes");
		menu.sendKeys.setMnemonic(KeyEvent.VK_E);
		// alt+k
		menu.sendKeys.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK));
		menu.sendKeys.setEnabled(false);
		menu.screenshot.add(menu.sendKeys);
		
		menu.refreshSS = doMenuItem("Refresh Immediately", KeyEvent.VK_R, menu.screenshot,
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tabsPane.screenshotTab.actionPerformed(e);
				}
			}
		);
		menu.refreshSS.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		menu.refreshSS.setEnabled(false);
		
		// added here instead
		menu.screenshot.add(menu.ssMode);
		
//		menu.ssSize = new JMenu("Shrink/Restore");
//		menu.ssSize.setMnemonic('S');
//		menu.ssRestoreActivate = doMenuItem("Restore and Activate Window", menu.ssSize, this);
//		menu.ssShrinkOthers = doMenuItem("Restore and Shrink Others", menu.ssSize, this);
//		menu.ssHideOthers = doMenuItem("Restore and Hide Others", menu.ssSize, this);
//		menu.ssSize.addSeparator();
//		menu.ssShrink = doMenuItem("Shrink Window", menu.ssSize, this);
//		menu.ssShrinkAll = doMenuItem("Shrink All", menu.ssSize, this);
//		menu.ssSize.addSeparator();
//		menu.ssHide = doMenuItem("Hide Window", menu.ssSize, this);
//		menu.ssHideAll = doMenuItem("Hide All", menu.ssSize, this);
//		menu.screenshot.add(menu.ssSize);
		
		menu.logs = new JMenu("Logs");
		menu.logs.setMnemonic(KeyEvent.VK_L);
		menu.bar.add(menu.logs);

		menu.clearCurLog  = doMenuItem("Clear Current Tab", KeyEvent.VK_R, menu.logs, this);
		menu.clearAllLogs = doMenuItem("Clear All Tabs", KeyEvent.VK_A, menu.logs, this);
		menu.logs.addSeparator();
		menu.parseLogFile = doMenuItem("Parse Log File", KeyEvent.VK_P, menu.logs, this);
		
		
		// servers
		menu.servers = new JMenu("Servers");
		menu.servers.setMnemonic(KeyEvent.VK_R);
		menu.bar.add(menu.servers);
		
		menu.addServer = doMenuItem("Add New Server...", KeyEvent.VK_N, menu.servers, this);
		menu.removeServer = doMenuItem("Remove Current Server", KeyEvent.VK_R, menu.servers, this);
		menu.servers.addSeparator();
		menu.connectServers = doMenuItem("Connect All Servers", KeyEvent.VK_C, menu.servers, this);
		menu.disconnectServers = doMenuItem("Disconnect All Servers", KeyEvent.VK_D, menu.servers, this);
		menu.servers.addSeparator();
		menu.activateServers = doMenuItem("Activate Inactive Servers", KeyEvent.VK_A, menu.servers, this);
		menu.servers.addSeparator();
		menu.serversSub = new JMenu("Activate Server...");
		menu.serversSub.setMnemonic(KeyEvent.VK_V);
		menu.servers.add(menu.serversSub);
		// other items dynamically allocated
		
		
		// help
		menu.help = new JMenu("Help");
		menu.help.setMnemonic(KeyEvent.VK_H);
		menu.bar.add(menu.help);
		
		menu.debug = doMenuItem("Generate Debug Info", KeyEvent.VK_D, menu.help, this);
		menu.help.addSeparator();

		menu.helpOnline   = new JMenuItem("View Help Online", KeyEvent.VK_O);
		menu.helpOnline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menu.helpOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Util.openURL("http://dl.dropbox.com/u/65205371/jgm/docs/index.html");
			}
		});
		menu.help.add(menu.helpOnline);
		
		menu.helpContents = new JMenuItem("Help Contents", KeyEvent.VK_H);
		final java.io.File helpFile = new java.io.File("JGlideMon.chm");
		if (helpFile.exists() && System.getProperty("os.name").startsWith("Windows")) {
			menu.help.add(menu.helpContents);
			menu.helpContents.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (helpFile.exists())
						try {
							Runtime.getRuntime().exec("cmd /c \"" + helpFile.getCanonicalPath() + "\"");
						} catch (Throwable x) {
							log.log(Level.WARNING, "Error opening help file", x);
						}
				}
			});
		}
		
		menu.about = doMenuItem("About", KeyEvent.VK_A, menu.help, this);


		footer = new JPanel(new BorderLayout());
		
		sendChatPane = new SendChatPane(this);
		footer.add(sendChatPane, BorderLayout.NORTH);
		
		try {
			sendChatPane.setChatType(
				ChatType.valueOf(sm.get("window.chattype")));
		} catch (RuntimeException e) { }
		
		// set up status bar
		statusBar = new JStatusBar(this);
		statusBar.setText("Disconnected");

		JProgressBar tmp = statusBar.getProgressBar();
		tmp.setStringPainted(false);
		tmp.setMaximum(100);
		tmp.setMinimum(0);
		
		footer.add(statusBar, BorderLayout.SOUTH);
		
		frame.add(footer, BorderLayout.SOUTH);
		frame.setJMenuBar(menu.bar);

		// ensure the system L&F
	    SwingUtilities.updateComponentTreeUI(frame);
	    
	    sm.connector.addListener(new ConnectionAdapter() {
	    	public void onConnecting() {
				setStatusBarText("Connecting...", false, true);
				setStatusBarProgressIndeterminent();
	    	}
	    	
	    	public void onConnect() {
				setStatusBarText("Connected", false, true);
				hideStatusBarProgress();
				
				menu.sendKeys.setEnabled(true);
				menu.refreshSS.setEnabled(true);
	    	}
	    	
	    	public void onDisconnecting() {
				setStatusBarText("Disconnecting...", false, true);
				setStatusBarProgressIndeterminent();
				
				menu.sendKeys.setEnabled(false);
				menu.refreshSS.setEnabled(false);
	    	}
	    	
	    	public void onDisconnect() {
				hideStatusBarProgress();
	    	}
	    });
	    
	    ServerManager.addListener(mySmListener);
	}
	
	public void doServersMenu() {
		menu.serversSub.removeAll();
		
		JMenuItem item = null;
		
		synchronized (ServerManager.managers) {
			log.finer(sm.name + ":doServersMenu()");
			
			int i = KeyEvent.VK_1;
			
			for (final ServerManager s : ServerManager.managers) {
				item = new JMenuItem(s.name,
					s.getBool("enabled") ? ONLINE_ICON : OFFLINE_ICON);
				
				if (i <= KeyEvent.VK_9)
					item.setAccelerator(KeyStroke.getKeyStroke(i++, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
				
				menu.serversSub.add(item);

				item.addActionListener(new ActionListener() {
					final ServerManager mySm = s;
					public void actionPerformed(ActionEvent e) {
						log.finer("Activating " + mySm.name);
						
						if (!mySm.getBool("enabled")) {
							mySm.resume();
						}

						mySm.toFront();
					}
				});
			}
		}		
	}
	
	public void setWhisperTarget(String name) {
		sendChatPane.setWhisperTarget(name);
	}
	
	public void setIcon() {
		int i = sm.getInt("icon");
		
		if (i < 0 || i >= ICONS.length)
			i = 0;
		
		frame.setIconImage(ICONS[i].getImage());
		tray.setIcon(ICONS[i].getImage());
	}
	
	public void makeVisible() {
		frame.validate();
		frame.setVisible(true);
	}
	
	public void destroy() {
		ServerManager.removeListener(mySmListener);
		frame.dispose();
		tray.destroy();
	}
	
	
	private void doMaximizedSS(ScreenshotState state) {
		switch (state) {
		case MAXIMIZED:
		case FULLSCREEN:
			tabsPane.screenshotTab.select();
			ssPanel = tabsPane.screenshotTab.removeContent();
			frame.remove(mainPanel);
			frame.add(ssPanel, BorderLayout.CENTER);
			
			if (state == ScreenshotState.FULLSCREEN) {
				frame.dispose();
				frame.setUndecorated(true);
				frame.setJMenuBar(null);
				
				frame.remove(footer);
				frame.add(sendChatPane, BorderLayout.SOUTH);
				
				lastWindowState = frame.getExtendedState();
				lastWindowBounds = frame.getBounds();
				frame.setExtendedState(0);
				frame.setLocation(0, 0);
				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
				ssPanel.setBackground(Color.BLACK);
				ssPanel.setOpaque(true);
				
				// F11, Alt-K
				frame.getRootPane()
					.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(menu.fullSS.getAccelerator(), "closeWindow");
				frame.getRootPane()
					.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(menu.sendKeys.getAccelerator(), "sendKeys");
				frame.getRootPane()
					.getActionMap()
					.put("closeWindow", new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							menu.normalSS.doClick();
						}
					});
				frame.getRootPane()
					.getActionMap()
					.put("sendKeys", new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							menu.sendKeys.doClick();
						}
					});
				
				
				frame.setVisible(true);
			}
			break;
			
		default:
			frame.remove(ssPanel);
			frame.add(mainPanel, BorderLayout.CENTER);
			tabsPane.screenshotTab.restoreContent();
			
			if (ssState == ScreenshotState.FULLSCREEN) {
				frame.dispose();
				
				// remove F11, Alt-K
				frame.getRootPane()
					.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.remove(menu.fullSS.getAccelerator());
				frame.getRootPane()
					.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.remove(menu.sendKeys.getAccelerator());
				frame.getRootPane()
					.getActionMap()
					.remove("closeWindow");
				frame.getRootPane()
					.getActionMap()
					.remove("sendKeys");
			
				frame.setUndecorated(false);
				frame.setJMenuBar(menu.bar);
				
				frame.remove(sendChatPane);
				footer.add(sendChatPane, BorderLayout.NORTH);
				frame.add(footer, BorderLayout.SOUTH);
				
				frame.setBounds(lastWindowBounds);
				frame.setExtendedState(lastWindowState);
				ssPanel.setOpaque(false);				
				
				frame.setVisible(true);
			}
		}
		
		sm.ssUpdater.redoScale = true;
		
		frame.validate();
		frame.repaint();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == menu.ssRestoreActivate) {
			sm.cmd.add(Command.getSetGameWSCommand("normal"));
			sm.cmd.add(Command.getSelectGameCommand());
			sm.ssUpdater.redoScale = true;
		} else if (source == menu.ssShrinkOthers) {
			synchronized (ServerManager.managers) {
				for (ServerManager sm : ServerManager.managers) {
					if (!this.sm.equals(sm) && sm.getBool("enabled")) {
						sm.cmd.add(Command.getSetGameWSCommand("shrunk"));
						sm.ssUpdater.redoScale = true;
					}
				}
			}
			
			sm.cmd.add(Command.getSetGameWSCommand("normal"));
			sm.cmd.add(Command.getSelectGameCommand());
			sm.ssUpdater.redoScale = true;
		} else if (source == menu.ssHideOthers) {
			synchronized (ServerManager.managers) {
				for (ServerManager sm : ServerManager.managers) {
					if (!this.sm.equals(sm) && sm.getBool("enabled")) {
						sm.cmd.add(Command.getSetGameWSCommand("hidden"));
					}
				}
			}
			
			sm.cmd.add(Command.getSetGameWSCommand("normal"));
			sm.cmd.add(Command.getSelectGameCommand());
			sm.ssUpdater.redoScale = true;
		} else if (source == menu.ssShrink) {
			sm.cmd.add(Command.getSetGameWSCommand("shrunk"));
			sm.ssUpdater.redoScale = true;
		} else if (source == menu.ssShrinkAll) {
			synchronized (ServerManager.managers) {
				for (ServerManager sm : ServerManager.managers) {
					if (sm.getBool("enabled")) {
						sm.cmd.add(Command.getSetGameWSCommand("shrunk"));
						sm.ssUpdater.redoScale = true;
					}
				}
			}
		} else if (source == menu.ssHide) {
			sm.cmd.add(Command.getSetGameWSCommand("hidden"));
		} else if (source == menu.ssHideAll) {
			synchronized (ServerManager.managers) {
				for (ServerManager sm : ServerManager.managers) {
					if (sm.getBool("enabled")) {
						sm.cmd.add(Command.getSetGameWSCommand("hidden"));
					}
				}
			}
		} else if (source == menu.parseLogFile) {
			showParse();
		} else if (source == menu.clearCurLog) {
			if (tabsPane.tabbedPane.getSelectedComponent() instanceof jgm.gui.tabs.Clearable) {
				((jgm.gui.tabs.Clearable) tabsPane.tabbedPane.getSelectedComponent()).clear(false);
			}
		} else if (source == menu.clearAllLogs) {
			for (int i = 0; i < tabsPane.tabbedPane.getComponentCount(); i++) {
				if (tabsPane.tabbedPane.getComponentAt(i) instanceof jgm.gui.tabs.Clearable) {
					((jgm.gui.tabs.Clearable) tabsPane.tabbedPane.getComponentAt(i)).clear(true);
				}
			}
		} else if (source == menu.addServer) {
			ServerManager.addServer();
		} else if (source == menu.removeServer) {
			if (ServerManager.getActiveCount() == 1) {
				JOptionPane.showMessageDialog(frame,
					"You cannot remove the only active server.",
					"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				int ret = JOptionPane.showConfirmDialog(frame,
					"Removing this server will delete it from\n" +
					"your settings file and it will not reappear\n" +
					"when JGlideMon is restarted.\n\n" +
					
					"Alternatively, you can deactivate this server\n" +
					"by closing the window and selecting \"No\"\n" +
					"to close only this server. Its setting will\n" +
					"be saved but it will be in an inactive state\n" +
					"until you activate it by clicking its name in\n" +
					"the \"Servers\" menu.",
					"Are you sure?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
				
				if (ret == JOptionPane.YES_OPTION)
					ServerManager.removeServer(sm);
			}
		} else if (source == menu.connectServers) {
			ServerManager.connectAll();
		} else if (source == menu.disconnectServers) {
			ServerManager.disconnectAll();
		} else if (source == menu.activateServers) {
			synchronized (ServerManager.managers) {
				for (ServerManager sm : ServerManager.managers) {
					if (!sm.getBool("enabled")) {
						sm.resume();
						sm.toFront();
					}
				}
			}
		} else if (source == menu.config) {
			showConfig();
		} else if (source == menu.exit) {
			myWindowAdapter.windowClosing(null);
		} else if (source == menu.about) {
			showAbout();
		} else if (source == menu.debug) {
			Util.generateDebugInfo();
			
			String path = null;
			
			try {
				path = Util.debugInfoFile.getCanonicalPath();
			} catch (java.io.IOException x) {
				x.printStackTrace();
			}
			
			if (path != null) {
				JOptionPane.showMessageDialog(
					frame,
					"Debug info saved to\n" + path + "\n\n" +
					"Note this file may contain personal information, like folder names, you should remove.",
					"Debug Info Generated",
					JOptionPane.INFORMATION_MESSAGE
				);
			} else {
				JOptionPane.showMessageDialog(
						frame,
						"Error saving debug info.",
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
			}
		} else if (source == menu.clearItems) {
			jgm.wow.Item.Cache.clearItems();
		} else if (source == menu.clearIcons) {
			jgm.wow.Item.Cache.clearIcons();
		} else if (source == menu.saveIcons) {
			jgm.wow.Item.Cache.saveIcons();
			jgm.wow.Item.Cache.saveItems();
		} else if (source == menu.loadIcons) {
			jgm.wow.Item.Cache.loadIcons();
			jgm.wow.Item.Cache.loadItems();
		}
	}
	
	public void update(java.util.Observable obs, Object o) {
//		System.out.println("GUI.update() called");
		Status s = (Status) o;

		charInfo.update(s);
		mobInfo.update(s);
		ctrlPane.update(s);
		xpPane.update(s);
		tabsPane.update(s);
		
		sendChatPane.update(s);
		
		String version = "";
		
		if (!s.version.equals("")) {
			version = "Connected to Glider v" + s.version + " - ";
		}
		
		if (!sm.connector.isConnected()) {
			String st;
			
			switch (sm.connector.state) {
				case CONNECTING: st = "Connecting..."; break;
				case DISCONNECTING: st = "Disconnecting..."; break;
				default: st = "Disconnected"; break;
			}
			
			setStatusBarText(st);
		} else if (s.attached) {
			setStatusBarText(version + "Attached: " + (s.profile.isEmpty() ? "(unknown profile)" : s.profile));
		} else {
			setStatusBarText(version + "Not Attached");
		}
	}
	
	
	//////////////////////////////
	// Implement ContainerListener
	public void componentAdded(ContainerEvent e) {
		GUI.addKeyAndContainerListenerRecursively(this.tabsPane.screenshotTab, this, e.getChild());
	}

	public void componentRemoved(ContainerEvent e) {
		GUI.removeKeyAndContainerListenerRecursively(this.tabsPane.screenshotTab, this, e.getChild());
	}
	
	public void showParse() {
		if (this.parseLogDialog == null)
			parseLogDialog = new jgm.gui.dialogs.ParseLogFile(this);
		parseLogDialog.setVisible(true);
	}
	
	public void showConfig() {
		showConfig(-1);
	}
	
	public void showConfig(int selectTab) {
		if (configDialog == null) configDialog = new jgm.gui.dialogs.Config(this);
		if (selectTab >= 0) configDialog.selectTab(selectTab);
		configDialog.setVisible(true);
	}
	
	public void showAbout() {
		if (aboutFrame == null) aboutFrame = new jgm.gui.dialogs.About(this);
		aboutFrame.setVisible(true);
	}
	

	/////////////////
	// static methods
	
	private volatile boolean lockStatusText = false;
	
	public void unlockStatusBarText() {
		lockStatusText = false;
	}
	
	/**
	 * Set the status bar's text.
	 * @param s The String to set the text to
	 */
	public void setStatusBarText(String s) {
		setStatusBarText(s, false, false);
	}
	
	/**
	 * Set the status bar's text and possibly lock it
	 * afterward.
	 * @param s The String to set the text to
	 * @param lock Whether to lock the text after setting it
	 * @param force Whether to ignore if the text is locked
	 */
	public void setStatusBarText(String s, boolean lock, boolean force) {
		if (statusBar == null || (lockStatusText && !force)) return;

		// if locking set the lock
		if (lock)
			lockStatusText = true;
		// else not locking and forcing unset the lock
		else if (force)
			lockStatusText = false;
		
		
		lastStatusText = currentStatusText;
		currentStatusText = s;
		statusBar.setText(s);
	}
	
	private String lastStatusText = "";
	private String currentStatusText = "";
	
	public void revertStatusBarText() {
		if (statusBar == null) return;
		
		statusBar.setText(lastStatusText);
	}
	
	public void setStatusBarProgressIndeterminent() {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(true);
		statusBar.getProgressBar().setVisible(true);
	}
	
	public void setStatusBarProgress(int i) {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(false);
		statusBar.getProgressBar().setValue(i);
		statusBar.getProgressBar().setVisible(true);
	}
	
	public void hideStatusBarProgress() {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(false);
		statusBar.getProgressBar().setValue(0);
		statusBar.getProgressBar().setVisible(false);
	}
	
	public void setTitle() {
		setTitle(sm.toFullString());
	}

	public void setTitle(String s) {
		if (frame == null) return;

		frame.setTitle((s != null && !s.equals("") ? s + " - " : "") + BASE_TITLE);
		tray.setTitle(frame.getTitle());
	}

	
	/////////////////////
	// General util stuff
	
	public static void setTitleBorder(JComponent c, String text) {
		c.setBorder(
			BorderFactory.createTitledBorder(text)
		);
	}
	
    public static void addKeyAndContainerListenerRecursively(KeyListener kl, ContainerListener cl, Component c) {
    	c.addKeyListener(kl);
    	//System.out.println("Adding lstnr: " + c);
    	
		if (c instanceof Container) {
			Container cont = (Container) c;
			
			cont.addContainerListener(cl);
			
			for (Component child : cont.getComponents()){
				addKeyAndContainerListenerRecursively(kl, cl, child);
			}
		}
    }
    
    public static void removeKeyAndContainerListenerRecursively(KeyListener kl, ContainerListener cl, Component c) {    	
		c.removeKeyListener(kl);
		
		if (c instanceof Container){
			Container cont = (Container) c;
			cont.removeContainerListener(cl);
		
			for (Component child : cont.getComponents()){
				removeKeyAndContainerListenerRecursively(kl, cl, child);
			}
		}
	}
    
    public JMenuItem doMenuItem(String text, JMenu parent, ActionListener listener) {
    	return doMenuItem(text, -1, parent, listener);
    }
    
    public JMenuItem doMenuItem(String text, int mnemonic, JMenu parent, ActionListener listener) {
    	JMenuItem item =
    		(mnemonic >= 0) ? new JMenuItem(text, mnemonic) : new JMenuItem(text);
    	item.addActionListener(listener);
    	parent.add(item);
    	
    	return item;
    }
    
	public static final Cursor
		HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
		DEFAULT_CURSOR = Cursor.getDefaultCursor();
}
