package jgm;

import javax.swing.JOptionPane;

import jgm.glider.*;
import jgm.gui.GUI;
import jgm.gui.updaters.*;
import jgm.util.*;

/**
 * The main program.
 * @author Tim
 * @since 0.1
 */
public class JGlideMon implements ConnectionListener {
	public static final String version = "0.3";
	
	public static JGlideMon instance;
	
	public  GliderConn    keysConn;
	private cfg           c;
	public  GUI           gui;
	private StatusUpdater status;
	private LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;

	public Connector connector;
	
	public JGlideMon() {
		instance = this;
		c = new cfg();
		init();
	}
	
	public GliderConn getConn() {
		return keysConn;
	}
	
	public void connectionEstablished() {}
	public void connectionDied() {}
	
	private void init() {
		synchronized (c) {
		if (!c.isSet()) {
			try {
				c.wait();
			} catch (InterruptedException e) {}
		}
		}
		
	  	connector = new Connector();
		gui = new GUI(this);

		if (!jgm.cfg.iniFileExists() || jgm.cfg.net.host.equals("")) {
			gui.tabsPane.tabbedPane.setSelectedComponent(gui.tabsPane.config);
			
			JOptionPane.showMessageDialog(gui.frame,
				"Please enter the server name, port, and password.\n" +
				"Next, click Save Settings, then click Connect.\n\n" +
				"Remember to click Save Settings any time you change a setting.",
				"Configuration Required",
				JOptionPane.INFORMATION_MESSAGE);
		}
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				Sound.init();
				Speech.init();
				keysConn   = new GliderConn();
				Connector.addListener(JGlideMon.this);
				logUpdater = new LogUpdater(gui.tabsPane);
				Connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(gui.tabsPane.screenshotTab);
				Connector.addListener(ssUpdater);
				status     = new StatusUpdater();
				Connector.addListener(status);
				status.addObserver(gui);
				status.addObserver(ssUpdater);
				
				Connector.connect();
			}
		};

		new Thread(r, "JGlideMon.Init").start();
	}

	public void destroy() {
		if (Connector.isConnected()) {
			try {
				Thread t = Connector.disconnect();
				
				synchronized (t) {
					t.wait();
				}
			} catch (InterruptedException e) {}
		}
		
		Speech.destroy();
		cfg.writeIni();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
