package jgm;

import jgm.glider.*;
import jgm.gui.Tray;
import jgm.gui.panes.*;
import jgm.gui.updaters.*;
import jgm.gui.components.JStatusBar;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class GUI 
	implements java.util.Observer, ActionListener, ContainerListener, jgm.locale.LocaleListener {
	
	public static GUI instance;
	public static JFrame frame;
	public static Tray tray;

	private JPanel mainPane;
	
	public CharInfoPane   charInfo;
	public MobInfoPane    mobInfo;
	public ControlPane    ctrlPane;
	public ExperiencePane xpPane;
	public TabsPane       tabsPane;

	private JMenuBar      menuBar;
	private JMenu         fileMenu, helpMenu;
	private JMenuItem     saveCache, loadCache, configItem, exitItem, debugInfoItem, aboutItem;

	private static JStatusBar statusBar;

	private jgm.gui.dialogs.About aboutFrame;
	private jgm.gui.dialogs.Config configDialog;
	
	private Config cfg;

	public static final String BASE_TITLE = "JGlideMon " + JGlideMon.version;

	public GUI() {
		instance = this;
		cfg = jgm.Config.getInstance();
		
		frame = new JFrame(BASE_TITLE);

		ImageIcon img = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/icon.png"));
		
		frame.setIconImage(img.getImage());
		
		frame.setSize(cfg.getInt("window", "width"), cfg.getInt("window", "height"));
		frame.setLocation(cfg.getInt("window", "x"), cfg.getInt("window", "y"));

		tray = new Tray();
		
		if (cfg.getBool("window", "maximized")) {
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
					cfg.setBool("window", "maximized", true);
				} else {
					//System.out.println("Window not maximized");
					cfg.setBool("window", "maximized", false);
				}
				
				if (JFrame.ICONIFIED ==
					(frame.getExtendedState() & JFrame.ICONIFIED)) {
					
					// minimize to tray
					if (Tray.isSupported() &&
						cfg.getBool("general", "showtray") &&
						cfg.getBool("general", "mintotray")) {
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
					cfg.setInt("window", "width", s.width);					
					cfg.setInt("window", "height", s.height);
				}
			}
			
			public void componentMoved(ComponentEvent e) {
				Point p = frame.getLocation();
				//System.out.println("Window moved: " + p);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					cfg.setInt("window", "x", p.x);
					cfg.setInt("window", "y", p.y);
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
		mainPane.add(charInfo, c);

		mobInfo = new MobInfoPane();
		c.gridx = 1; c.gridy = 0; c.weightx = 0.75;
		mainPane.add(mobInfo, c);

		ctrlPane = new ControlPane();
		Connector.addListener(ctrlPane);
		c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
		mainPane.add(ctrlPane, c);

		xpPane = new ExperiencePane();
		c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
		mainPane.add(xpPane, c);

		tabsPane = new TabsPane();
		JPanel tabsPanel = new JPanel(new BorderLayout());
		tabsPanel.add(tabsPane, BorderLayout.CENTER);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
		mainPane.add(tabsPanel, c);

		addKeyAndContainerListenerRecursively(tabsPane.screenshotTab, this, frame);

		frame.setLayout(new BorderLayout());
		frame.add(mainPane, BorderLayout.CENTER);

		// set up menu
		menuBar  = new JMenuBar();
		fileMenu = new JMenu();
		menuBar.add(fileMenu);

		if (jgm.JGlideMon.debug) {
			saveCache = new JMenuItem();
			saveCache.addActionListener(this);
			fileMenu.add(saveCache);
		
			loadCache = new JMenuItem();
			loadCache.addActionListener(this);
			fileMenu.add(loadCache);
		}
		
		configItem = new JMenuItem();
		configItem.addActionListener(this);
		fileMenu.add(configItem);
		
		fileMenu.addSeparator();
		
		exitItem = new JMenuItem();
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);
		
		helpMenu = new JMenu();
		menuBar.add(helpMenu);
		
		/* TODO: localize */
		debugInfoItem = new JMenuItem("Generate Debug Info", KeyEvent.VK_D);
		debugInfoItem.addActionListener(this);
		helpMenu.add(debugInfoItem);
		helpMenu.addSeparator();

		aboutItem = new JMenuItem();
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		
		// set up status bar
		statusBar = new JStatusBar();
		statusBar.setText(Locale._("status.disconnected"));

		JProgressBar tmp = statusBar.getProgressBar();
		tmp.setStringPainted(false);
		tmp.setMaximum(100);
		tmp.setMinimum(0);
		
		frame.add(statusBar, BorderLayout.SOUTH);

		frame.setJMenuBar(menuBar);

		localeChanged();
		Locale.addListener(this);
		
		// ensure the system L&F
	    SwingUtilities.updateComponentTreeUI(frame);
	    
	    Connector.addListener(new ConnectionAdapter() {
	    	public void connecting() {
				setStatusBarText(Locale._("status.disconnecting"), false, true);
				setStatusBarProgressIndeterminent();
	    	}
	    	
	    	public void connectionEstablished() {
				setStatusBarText(Locale._("status.connecting"), false, true);
				setTitle(cfg.get("net", "host") + ":" + cfg.get("net", "port"));
				hideStatusBarProgress();
	    	}
	    	
	    	public void disconnecting() {
				setStatusBarText(Locale._("status.disconnecting"), false, true);
				setStatusBarProgressIndeterminent();
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
	
	public void localeChanged() {
		Locale.setBase("MainWindow");
		
		Locale._(fileMenu, "menu.file");
		Locale._(saveCache, "menu.file.savecache");
		Locale._(loadCache, "menu.file.loadcache");
		Locale._(configItem, "menu.file.config");
		Locale._(exitItem, "menu.file.exit");

		Locale._(helpMenu, "menu.help");
		Locale._(aboutItem, "menu.help.about");
		
		tray.localeChanged();
	}

	public void update(java.util.Observable obs, Object o) {
		Locale.clearBase();
		
//		System.out.println("GUI.update() called");
		StatusUpdater s = (StatusUpdater) o;

		charInfo.update(s);
		mobInfo.update(s);
		ctrlPane.update(s);
		xpPane.update(s);
		tabsPane.update(s);

		String version = "";
		
		if (!s.version.equals("")) {
			version = Locale._("status.connectedto") + " " + s.version + " - ";
		}
		
		if (!Connector.isConnected()) {
			String st;
			
			switch (Connector.state) {
				case CONNECTING: st = Locale._("status.connecting"); break;
				case DISCONNECTING: st = Locale._("status.disconnecting"); break;
				default: st = Locale._("status.disconnected"); break;
			}
			
			setStatusBarText(st);
		} else if (s.attached) {
			setStatusBarText(version + Locale._("status.attached") + ": " + s.profile);
		} else {
			setStatusBarText(version + Locale._("status.notattached"));
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src == configItem) {
			showConfig();
		} else if (src == exitItem) {
			JGlideMon.instance.destroy();
		} else if (src == aboutItem) {
			showAbout();
		} else if (src == debugInfoItem) {
			/* TODO: localize */
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
		} else if (src == saveCache) {
			jgm.wow.Item.Cache.saveIcons();
			jgm.wow.Item.Cache.saveItems();
		} else if (src == loadCache) {
			jgm.wow.Item.Cache.loadIcons();
			jgm.wow.Item.Cache.loadItems();
		}
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
}