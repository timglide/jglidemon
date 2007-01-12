package jgm.gui;

import jgm.*;
import jgm.glider.*;
import jgm.gui.panes.*;
import jgm.gui.updaters.*;

import com.zfqjava.swing.JStatusBar;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class GUI 
	implements java.util.Observer, ActionListener {
	
	private JGlideMon     jgm;
	private JFrame        frame;

	private JPanel        mainPane;

	public CharInfoPane   charInfo;
	public MobInfoPane    mobInfo;
	public ControlPane    ctrlPane;
	public ExperiencePane xpPane;
	public TabsPane       tabsPane;

	private JMenuBar      menuBar;

	private JStatusBar    statusBar;

	public GUI(JGlideMon j) {
		jgm = j;
		
		frame = new JFrame("JGlideMon " + JGlideMon.version);

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

		frame.add(statusBar, BorderLayout.SOUTH);

		frame.setJMenuBar(menuBar);

		// ensure the system L&F
	    SwingUtilities.updateComponentTreeUI(frame);
		frame.validate();
		frame.setVisible(true);
	    frame.repaint();
	    
	    initAboutFrame();
	}
	

	private JFrame aboutFrame;
	private JLabel aboutIcon;
	private JLabel aboutText;
	
	private void initAboutFrame() {
		aboutFrame = new JFrame("About JGlideMon");
		aboutFrame.setSize(250, 200);
		aboutFrame.setResizable(false);
		aboutFrame.setLocationRelativeTo(null); // center
		aboutFrame.setLayout(new BorderLayout(20, 20));
		ImageIcon icon = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/stitch0.jpg"));
		aboutIcon = new JLabel(icon);
		aboutFrame.add(aboutIcon, BorderLayout.WEST);
		aboutText = new JLabel(
			"<html>JGlideMon " + JGlideMon.version + "<br><br>" +
			"By Tim" +
			"</html>"
		);
		aboutFrame.add(aboutText, BorderLayout.CENTER);
		
		aboutFrame.addWindowListener(
			new WindowAdapter() {
				public void windowOpened(WindowEvent e) {
					java.util.Random r = new java.util.Random();
					aboutIcon.setIcon(
						new ImageIcon(
							JGlideMon.class.getResource("resources/images/stitch/stitch" + r.nextInt(2) + ".jpg")));
				}
				
				public void windowClosing(WindowEvent e) {
					aboutFrame.setVisible(false);
				} // end WindowClosing
			}
		);		
	}

	public void update(java.util.Observable obs, Object o) {
//		System.out.println("GUI.update() called");
		StatusUpdater s = (StatusUpdater) o;

		charInfo.update(s);
		mobInfo.update(s);
		ctrlPane.update(s);
		xpPane.update(s);
//		tabsPane.update(s);

		String version = "";
		
		if (!s.version.equals("")) {
			version = "Glider v" + s.version + " - ";
		}
		
		if (!Connector.isConnected()) {
			statusBar.setText("Disconnected");
		} else if (s.attached) {
			statusBar.setText(version + "Attached: " + s.profile);
		} else {
			statusBar.setText(version + "Not Attached");
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (cmd.equals("Exit")) {
			jgm.destroy();
		} else if (cmd.equals("About")) {
			aboutFrame.setVisible(true);
		}
	}
}
