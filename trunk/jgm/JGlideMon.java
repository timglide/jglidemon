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
public class JGlideMon {
	public static final String app = "JGlideMon";
	public static final String version = "0.8 beta";
	public static final boolean debug = true;
	
	public static JGlideMon instance;
	
	public  GliderConn    keysConn;
	private cfg           cfg;
	public  GUI           gui;
	private StatusUpdater status;
	private LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;

	public Connector connector;
	
	public JGlideMon() {
		instance = this;
		cfg = new cfg();		
		init();
	}
	
	private void init() {
		jgm.glider.Profile.Cache.loadProfiles();
		
	  	connector = new Connector();
		gui = new GUI();

		if (!cfg.iniFileExists() || cfg.getString("net", "host").equals("")) {
			JOptionPane.showMessageDialog(GUI.frame,
				"Please enter the server name, port, and password.\n" +
				"Next, click Save Settings, then click Connect.\n\n" +
				"Remember to click Save Settings any time you change a setting.\n" +
				"You may access the configuration screen later via the File menu.",
				"Configuration Required",
				JOptionPane.INFORMATION_MESSAGE);
			
			gui.showConfig(1);
		}
		
		gui.makeVisible();
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				Sound.init();
				Speech.init();
				keysConn   = new GliderConn();
				Connector.addListener(new ConnectionAdapter() {
					public GliderConn getConn() {
						return keysConn;
					}
				});
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
		
		// not critical to get these loaded before the gui shows
		jgm.wow.Item.Cache.loadIcons();
		jgm.wow.Item.Cache.loadItems();
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
		jgm.wow.Item.Cache.saveIcons();
		jgm.wow.Item.Cache.saveItems();
		cfg.writeIni();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
