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

import javax.swing.JOptionPane;

import jgm.glider.*;
import jgm.gui.updaters.*;
import jgm.util.*;

/**
 * The main program.
 * @author Tim
 * @since 0.1
 */
public class JGlideMon {
	public static final String app = "JGlideMon";
	public static final String version = "0.11 beta";
	public static final String _revision = "$Revision$";
	public static final String revision = _revision.substring(1, _revision.length() - 1);
	public static final String _date = "$Date$";
	public static final String date = _date.substring(1, _date.length() - 1);
	
	public static boolean debug = false;
	
	public static JGlideMon instance;
	
	public  Conn          keysConn;
	private Config        cfg;
	public  GUI           gui;
	public  StatusUpdater status;
	private LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;
	private PlayerChartUpdater chartUpdater;
	
	public Connector connector;
	
	public JGlideMon() {
		instance = this;
		cfg = new Config();		
		init();
	}
	
	private void init() {
		// initialize logger
		Log.reloadConfig();
		
		try {
			jgm.glider.Profile.Cache.loadProfiles();
		} catch (Throwable e) {} // doesn't matter here 
		
	  	connector = new Connector();
		gui = new GUI();

		// put this here so that the tts config options will
		// be enabled if tts is available and JGM has yet to
		// be configured for the first time
		Sound.init();
		Speech.init();
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				if (!jgm.Config.iniFileExists() || cfg.getString("net", "host").equals("")) {
					JOptionPane.showMessageDialog(GUI.frame,
						"Please enter the remote host, port, and password.\n" +
						"Next, click Save Settings, then click Connect.\n\n" +
						"Remember to click Save Settings any time you change a setting.\n" +
						"You may access the configuration screen later via the File menu.",
						"Configuration Required",
						JOptionPane.INFORMATION_MESSAGE);
					
					// select the network tab
					gui.showConfig(1);
				}
				
				keysConn = new Conn();
				Connector.addListener(new ConnectionAdapter() {
					public Conn getConn() {
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
				
				chartUpdater = new PlayerChartUpdater();
				
				Connector.connect();
			}
		};

		new Thread(r, "JGlideMon.Init").start();

		gui.makeVisible();
		
		// not critical to get these loaded before the gui shows
		jgm.wow.Item.Cache.loadIcons();
		jgm.wow.Item.Cache.loadItems();
		
		
		new HTTPD();
		
		if (cfg.getBool("web", "enabled")) {
			try {
				HTTPD.instance.start(cfg.getInt("web", "port"));
			} catch (java.io.IOException e) {
				JOptionPane.showMessageDialog(GUI.frame,
					"Unable to start web-server.\n" +
					"Port " + cfg.getInt("web", "port") + " is unavailible.",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
	}

	public void destroy() {
		if (Connector.isConnected()) {
			try {
				Thread t = Connector.disconnect();
				t.join();
			} catch (InterruptedException e) {}
		}
		
		Speech.destroy();
		jgm.wow.Item.Cache.saveIcons();
		jgm.wow.Item.Cache.saveItems();
		HTTPD.instance.stop(); // doesn't matter if it's actually running or not
		jgm.Config.writeIni();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
