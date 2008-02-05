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
package jgm;

import jgm.glider.*;
import jgm.gui.Tray;
import jgm.gui.panes.*;
import jgm.gui.components.JStatusBar;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class GUI 
	implements java.util.Observer, ActionListener, ContainerListener {
	public static final int PADDING = 10;
	
	public static GUI instance;
	public static JFrame frame;
	public static Tray tray;

	private JPanel mainPane;
	
	public CharInfoPane   charInfo;
	public MobInfoPane    mobInfo;
	public ControlPane    ctrlPane;
	public ExperiencePane xpPane;
	public TabsPane       tabsPane;

	private static JStatusBar statusBar;

	private jgm.gui.dialogs.About aboutFrame;
	private jgm.gui.dialogs.Config configDialog;
	private jgm.gui.dialogs.ParseLogFile parseLogDialog;
	
	private Config cfg;

	public static final String BASE_TITLE = "JGlideMon " + JGlideMon.version;

	
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
		
		JMenu     help;
		JMenuItem debug;
		JMenuItem about;
	}
	
	public GUI() {
		instance = this;
		cfg = jgm.Config.getInstance();
		
		frame = new JFrame(BASE_TITLE);

		ImageIcon img = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/icon.png"));
		
		frame.setIconImage(img.getImage());
		
		frame.setSize(cfg.getInt("window.width"), cfg.getInt("window.height"));
		frame.setLocation(cfg.getInt("window.x"), cfg.getInt("window.y"));

		tray = new Tray();
		
		if (cfg.getBool("window.maximized")) {
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
		
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					JGlideMon.instance.destroy();
				}
			}
		);
		
		frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if (JFrame.MAXIMIZED_BOTH ==
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					//System.out.println("Window is maximized");
					cfg.set("window.maximized", true);
				} else {
					//System.out.println("Window not maximized");
					cfg.set("window.maximized", false);
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
			public void componentResized(ComponentEvent e) {
				Dimension s = frame.getSize();
				//System.out.println("Window resized: " + s);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					cfg.set("window.width", s.width);					
					cfg.set("window.height", s.height);
				}
				
				// request to update the screenshot's scale
				try {
					JGlideMon.instance.ssUpdater.redoScale = true;
				} catch (Exception x) {}
			}
			
			public void componentMoved(ComponentEvent e) {
				Point p = frame.getLocation();
				//System.out.println("Window moved: " + p);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					cfg.set("window.x", p.x);
					cfg.set("window.y", p.y);
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

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0; c.weighty = 0.0;

		mainPane = new JPanel(new GridBagLayout());

		charInfo = new CharInfoPane();
		c.gridx = 0; c.gridy = 0; c.weightx = 0.25;
		c.insets.top = PADDING;
		c.insets.left = PADDING;
		mainPane.add(charInfo, c);

		mobInfo = new MobInfoPane();
		c.gridx = 1; c.gridy = 0; c.weightx = 0.75;
		mainPane.add(mobInfo, c);

		ctrlPane = new ControlPane();
		Connector.addListener(ctrlPane);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		c.insets.right = PADDING;
		mainPane.add(ctrlPane, c);

		xpPane = new ExperiencePane();
		c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
		c.insets.left = 0;
		c.insets.right = 0;
		mainPane.add(xpPane, c);

		tabsPane = new TabsPane();
		JPanel tabsPanel = new JPanel(new BorderLayout());
		tabsPanel.add(tabsPane, BorderLayout.CENTER);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
		mainPane.add(tabsPanel, c);

		addKeyAndContainerListenerRecursively(tabsPane.screenshotTab, this, frame);
		
		frame.setLayout(new BorderLayout());
		frame.add(mainPane, BorderLayout.CENTER);

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
		
		menu.refreshSS = doMenuItem("Refresh Immediately", KeyEvent.VK_R, menu.screenshot, tabsPane.screenshotTab);
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
		
		// help
		menu.help = new JMenu("Help");
		menu.help.setMnemonic(KeyEvent.VK_H);
		menu.bar.add(menu.help);
		
		menu.debug = doMenuItem("Generate Debug Info", KeyEvent.VK_D, menu.help, this);
		menu.help.addSeparator();
		menu.about = doMenuItem("About", KeyEvent.VK_A, menu.help, this);

		
		// set up status bar
		statusBar = new JStatusBar();
		statusBar.setText("Disconnected");

		JProgressBar tmp = statusBar.getProgressBar();
		tmp.setStringPainted(false);
		tmp.setMaximum(100);
		tmp.setMinimum(0);
		
		frame.add(statusBar, BorderLayout.SOUTH);

		frame.setJMenuBar(menu.bar);

		// ensure the system L&F
	    SwingUtilities.updateComponentTreeUI(frame);
	    
	    Connector.addListener(new ConnectionAdapter() {
	    	public void connecting() {
				setStatusBarText("Connecting...", false, true);
				setStatusBarProgressIndeterminent();
	    	}
	    	
	    	public void connectionEstablished() {
				setStatusBarText("Connected", false, true);
				setTitle(cfg.get("net.host") + ":" + cfg.get("net.port"));
				hideStatusBarProgress();
				
				menu.sendKeys.setEnabled(true);
				menu.refreshSS.setEnabled(true);
	    	}
	    	
	    	public void disconnecting() {
				setStatusBarText("Disconnecting...", false, true);
				setStatusBarProgressIndeterminent();
				
				menu.sendKeys.setEnabled(false);
				menu.refreshSS.setEnabled(false);
	    	}
	    	
	    	public void connectionDied() {
				hideStatusBarProgress();
				setTitle();
	    	}
	    });
	}
	
	public void makeVisible() {
		frame.validate();
		frame.setVisible(true);
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
		
		if (!Connector.isConnected()) {
			String st;
			
			switch (Connector.state) {
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
		} else if (source == menu.config) {
			showConfig();
		} else if (source == menu.exit) {
			JGlideMon.instance.destroy();
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
	
	public void showParse() {
		if (this.parseLogDialog == null)
			parseLogDialog = new jgm.gui.dialogs.ParseLogFile(frame);
		parseLogDialog.setVisible(true);
	}
	
	public void showConfig() {
		showConfig(-1);
	}
	
	public void showConfig(int selectTab) {
		if (configDialog == null) configDialog = new jgm.gui.dialogs.Config(frame);
		if (selectTab >= 0) configDialog.selectTab(selectTab);
		configDialog.setVisible(true);
	}
	
	public void showAbout() {
		if (aboutFrame == null) aboutFrame = new jgm.gui.dialogs.About(frame);
		aboutFrame.setVisible(true);
	}
	
	//////////////////////////////
	// Implement ContainerListener
	public void componentAdded(ContainerEvent e) {
		addKeyAndContainerListenerRecursively(tabsPane.screenshotTab, this, e.getChild());
	}

	public void componentRemoved(ContainerEvent e) {
		removeKeyAndContainerListenerRecursively(tabsPane.screenshotTab, this, e.getChild());
	}
	
	/////////////////
	// static methods
	
	private static volatile boolean lockStatusText = false;
	
	public static void unlockStatusBarText() {
		lockStatusText = false;
	}
	
	/**
	 * Set the status bar's text.
	 * @param s The String to set the text to
	 */
	public static void setStatusBarText(String s) {
		setStatusBarText(s, false, false);
	}
	
	/**
	 * Set the status bar's text and possibly lock it
	 * afterward.
	 * @param s The String to set the text to
	 * @param lock Whether to lock the text after setting it
	 * @param force Whether to ignore if the text is locked
	 */
	public static void setStatusBarText(String s, boolean lock, boolean force) {
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
	
	private static String lastStatusText = "";
	private static String currentStatusText = "";
	
	public static void revertStatusBarText() {
		if (statusBar == null) return;
		
		statusBar.setText(lastStatusText);
	}
	
	public static void setStatusBarProgressIndeterminent() {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(true);
		statusBar.getProgressBar().setVisible(true);
	}
	
	public static void setStatusBarProgress(int i) {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(false);
		statusBar.getProgressBar().setValue(i);
		statusBar.getProgressBar().setVisible(true);
	}
	
	public static void hideStatusBarProgress() {
		if (statusBar == null) return;
		
		statusBar.getProgressBar().setIndeterminate(false);
		statusBar.getProgressBar().setValue(0);
		statusBar.getProgressBar().setVisible(false);
	}
	
	public static void setTitle() {
		setTitle(null);
	}

	public static void setTitle(String s) {
		if (frame == null) return;

		frame.setTitle((s != null && !s.equals("") ? s + " - " : "") + BASE_TITLE);
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
    
    public static JMenuItem doMenuItem(String text, JMenu parent, ActionListener listener) {
    	return doMenuItem(text, -1, parent, listener);
    }
    
    public static JMenuItem doMenuItem(String text, int mnemonic, JMenu parent, ActionListener listener) {
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
