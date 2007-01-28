package jgm.gui;

import jgm.*;
import jgm.glider.*;
import jgm.gui.dialogs.*;
import jgm.gui.panes.*;
import jgm.gui.updaters.*;

import com.zfqjava.swing.JStatusBar;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class GUI 
	implements java.util.Observer, ActionListener {
	
	public static GUI instance;
	
	private JGlideMon     jgm;
	public static JFrame        frame;

	private JPanel        mainPane;

	public CharInfoPane   charInfo;
	public MobInfoPane    mobInfo;
	public ControlPane    ctrlPane;
	public ExperiencePane xpPane;
	public TabsPane       tabsPane;

	private JMenuBar      menuBar;

	private static JStatusBar    statusBar;

	private About aboutFrame;
	private Config configDialog;
	
	public static void setTitleBorder(JComponent c, String text) {
		c.setBorder(
			BorderFactory.createTitledBorder(text)
		);
	}
	
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
	
	public static final String BASE_TITLE = "JGlideMon " + JGlideMon.version;

	public static void setTitle() {
		setTitle(null);
	}

	public static void setTitle(String s) {
		if (frame == null) return;

		frame.setTitle((s != null && !s.equals("") ? s + " - " : "") + BASE_TITLE);
	}

	public GUI(JGlideMon j) {
		instance = this;
		jgm = j;
		
		frame = new JFrame(BASE_TITLE);

		frame.setSize(cfg.window.width, cfg.window.height);
		frame.setLocation(cfg.window.x, cfg.window.y);
		
		if (cfg.window.maximized) {
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
		
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					jgm.destroy();
				} // end WindowClosing
			}
		);
		
		frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if (JFrame.MAXIMIZED_BOTH ==
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					//System.out.println("Window is maximized");
					cfg.window.maximized = true;
				} else {
					//System.out.println("Window not maximized");
					cfg.window.maximized = false;
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
					cfg.window.width = s.width;
					cfg.window.height = s.height;
				}
			}
			
			public void componentMoved(ComponentEvent e) {
				Point p = frame.getLocation();
				//System.out.println("Window moved: " + p);
				
				// only save if not maximized
				if (JFrame.MAXIMIZED_BOTH !=
					(frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)) {
					cfg.window.x = p.x;
					cfg.window.y = p.y;
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

		mainPane.setBackground(new Color(0xAA, 0, 0));

		frame.setLayout(new BorderLayout());
		frame.add(mainPane, BorderLayout.CENTER);

		// set up menu
		         menuBar  = new JMenuBar();
		JMenu    fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem configItem = new JMenuItem("Configuration", KeyEvent.VK_C);
		configItem.addActionListener(this);
		fileMenu.add(configItem);
		
		fileMenu.addSeparator();
		
		JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);
		
		JMenu    helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);
		
		JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		
		// set up status bar
		statusBar = new JStatusBar(JStatusBar.EXPLORER);
		statusBar.putClientProperty("JStatusBar.clientBorder", "Flat");
		statusBar.setText("Disconnected");

		JProgressBar tmp = statusBar.getProgressBar();
		tmp.setStringPainted(false);
		tmp.setMaximum(100);
		tmp.setMinimum(0);
		
		frame.add(statusBar, BorderLayout.SOUTH);

		frame.setJMenuBar(menuBar);

		// ensure the system L&F
	    SwingUtilities.updateComponentTreeUI(frame);
	}
	
	public void makeVisible() {
		frame.validate();
		frame.setVisible(true);
	}
	
	public void update(java.util.Observable obs, Object o) {
//		System.out.println("GUI.update() called");
		StatusUpdater s = (StatusUpdater) o;

		charInfo.update(s);
		mobInfo.update(s);
		ctrlPane.update(s);
		xpPane.update(s);
		tabsPane.update(s);

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
		String cmd = e.getActionCommand();
		
		if (cmd.equals("Configuration")) {
			showConfig();
		} else if (cmd.equals("Exit")) {
			jgm.destroy();
		} else if (cmd.equals("About")) {
			showAbout();
		}
	}
	
	public void showConfig() {
		if (configDialog == null) configDialog = new Config(frame);
		configDialog.setVisible(true);
	}
	
	public void showAbout() {
		if (aboutFrame == null) aboutFrame = new About(frame);
		aboutFrame.setVisible(true);
	}
}