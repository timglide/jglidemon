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
import jgm.gui.panes.TabsPane;
import jgm.util.Util;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class GUI 
	implements ActionListener, java.util.Observer, ContainerListener {
	
	static final ImageIcon ONLINE_ICON =
		new ImageIcon(JGlideMon.class.getResource("resources/images/status/online.png"));
	static final ImageIcon OFFLINE_ICON =
		new ImageIcon(JGlideMon.class.getResource("resources/images/status/offline.png"));
	
//	public static final String[] ICON_NAMES = {
//		"Default", "Druid", "Hunter", "Mage",
//		"Paladin", "Priest", "Rogue", "Shaman",
//		"Warlock", "Warrior"
//	};
	
	public static final ImageIcon[] ICONS = new ImageIcon[10];
	
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
	}
	
	public static final int PADDING = 10;
	static Config cfg = Config.c;
	public static final String BASE_TITLE = "JGlideMon " + JGlideMon.version;
	
	public final ServerManager sm;
	
	public JFrame frame;
	public Tray tray;
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
	
	
	// menu stuff
	public final Menu menu = new Menu();
	
	// this class is just to give a menu namespace for the
	// necessary variables
	public class Menu {
		JMenuBar  bar;
		
		JMenu     file;
		JMenuItem saveIcons;
		JMenuItem loadIcons;
		JMenuItem config;
		JMenuItem exit;
		
		JMenu     screenshot;
		public JCheckBoxMenuItem sendKeys;
		public JMenuItem refreshSS;
		
		JMenu     logs;
		JMenuItem clearCurLog;
		JMenuItem clearAllLogs;
		JMenuItem parseLogFile;
		
		JMenu     servers;
		JMenuItem addServer;
		JMenuItem removeServer;
		JMenuItem activateServers;
		java.util.List<JMenuItem> serverItems =
			new Vector<JMenuItem>();
		
		JMenu     help;
		JMenuItem debug;
		JMenuItem about;
	}
	
	ServerManager.Listener mySmListener = new ServerManager.Listener() {
    	void doit() {
    		doServersMenu();
    	}
    	
		public void serverAdded(ServerManager sm) {doit();}
		public void serverRemoved(ServerManager sm) {doit();}
		public void serverSuspended(ServerManager sm) {doit();}
		public void serverResumed(ServerManager sm) {doit();}
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
		
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Coultn'd set L&F: " + e.getMessage());
		}

		frame.setLayout(new BorderLayout());
		
		////////////////
		// set up panels
		JPanel mainPanel = new JPanel(new GridBagLayout());
		
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

		if (jgm.JGlideMon.debug) {
			menu.saveIcons = doMenuItem("Save Cache", menu.file, this);
			menu.loadIcons = doMenuItem("Load Cache", menu.file, this);
			menu.file.addSeparator();
		}
		
		menu.config = doMenuItem("Configuration", KeyEvent.VK_C, menu.file, this);		
		menu.file.addSeparator();
		menu.exit = doMenuItem("Exit", KeyEvent.VK_X, menu.file, this);
		
		// screenshot
		menu.screenshot = new JMenu("Screenshot");
		menu.screenshot.setMnemonic(KeyEvent.VK_S);
		menu.bar.add(menu.screenshot);
		
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
		menu.activateServers = doMenuItem("Activate Inactive Servers", KeyEvent.VK_A, menu.servers, this);
		// other items dynamically allocated
		
		
		// help
		menu.help = new JMenu("Help");
		menu.help.setMnemonic(KeyEvent.VK_H);
		menu.bar.add(menu.help);
		
		menu.debug = doMenuItem("Generate Debug Info", KeyEvent.VK_D, menu.help, this);
		menu.help.addSeparator();
		menu.about = doMenuItem("About", KeyEvent.VK_A, menu.help, this);

		
		// set up status bar
		statusBar = new JStatusBar(this);
		statusBar.setText("Disconnected");

		JProgressBar tmp = statusBar.getProgressBar();
		tmp.setStringPainted(false);
		tmp.setMaximum(100);
		tmp.setMinimum(0);
		
		frame.add(statusBar, BorderLayout.SOUTH);

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
		menu.servers.removeAll();
		menu.serverItems.clear();
		
		menu.servers.add(menu.addServer);
		menu.servers.add(menu.removeServer);
		menu.servers.add(menu.activateServers);
		menu.servers.addSeparator();
		
		JMenuItem item = null;
		
		for (final ServerManager sm : ServerManager.managers) {
			item = new JMenuItem(sm.name, sm.getBool("enabled") ? ONLINE_ICON : OFFLINE_ICON);
			menu.serverItems.add(item);
			menu.servers.add(item);
			
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!sm.getBool("enabled")) {
						sm.resume();
					}
					
					sm.gui.frame.setVisible(true);
					sm.gui.frame.setExtendedState(sm.gui.frame.getExtendedState() & ~JFrame.ICONIFIED);
					sm.gui.frame.requestFocus();
					sm.gui.frame.toFront();
				}
			});
		}
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
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == menu.parseLogFile) {
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
			if (ServerManager.managers.size() == 1) {
				JOptionPane.showMessageDialog(frame,
					"You cannot remove the only server.",
					"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				ServerManager.removeServer(sm);
			}
		} else if (source == menu.activateServers) {
			for (ServerManager sm : ServerManager.managers.toArray(new ServerManager[] {})) {
				if (!sm.getBool("enabled")) {
					sm.resume();
					
					sm.gui.frame.setVisible(true);
					sm.gui.frame.setExtendedState(sm.gui.frame.getExtendedState() & ~JFrame.ICONIFIED);
					sm.gui.frame.requestFocus();
					sm.gui.frame.toFront();
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
					"Debug info saved to\n" + path,
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

		tabsPane.chatLog.update(s);
		
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
			setStatusBarText(version + "Attached: " + s.profile);
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
