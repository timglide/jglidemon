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

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.UIManager;

import jgm.logging.Log;
import jgm.gui.Splash;

// this line edited so the svn revision gets updated

/**
 * The main program.
 * @author Tim
 * @since 0.1
 */
public class JGlideMon {
	public static final String app = "JGlideMon";
	public static final String version = "0.15 beta";
	public static final String _revision = "$Revision$";
	public static final String revision = _revision.substring(1, _revision.length() - 1);
	public static final String _date = "$Date$";
	public static final String date = _date.substring(1, _date.length() - 1);
	
	public static boolean debug = false;
	
	public static JGlideMon instance;
	public static Config        cfg;
	
	public JGlideMon() {
		instance = this;
		init();
	}
	
	private void init() {
		splash.setStatus("Loading Settings...");
		
		// initialize logger
		Log.reloadConfig();
		cfg = new Config();
		
		// yeah... so that if it converts JGlideMon.ini
		// the log line for it looks nice but this
		// needs to be called again in case the config
		// has log.debug=true
		Log.reloadConfig();
		
		try {
			splash.setStatus("Loading Profiles...");
			jgm.glider.Profile.Cache.loadProfiles();
		} catch (Throwable e) {} // doesn't matter here 

		
		splash.setStatus("Initializing Sound Resources...");
		
		// put this here so that the tts config options will
		// be enabled if tts is available and JGM has yet to
		// be configured for the first time
		jgm.util.Sound.init();
		jgm.util.Speech.init();
		
		splash.setStatus("Initializing Server Managers...");
		
		ServerManager.loadServers();
				
		boolean atLeastOneRunning = false;
		
		// resume all the servers
		synchronized (ServerManager.managers) {
			for (ServerManager sm : ServerManager.managers) {
				if (sm.getBool("enabled")) {
					splash.setStatus("Initializing \"" + sm.name + "\"...");
					
					atLeastOneRunning = true;
					ServerManager.resumeServer(sm);
				}
			}
		}
		
		// add a server if there are none
		if (ServerManager.managers.size() == 0) {
			ServerManager.addServer();
		}
		
		// if none are running force the first to resume
		if (!atLeastOneRunning) {
			ServerManager sm = ServerManager.managers.iterator().next();
			
			splash.setStatus("Initializing \"" + sm.name + "\"...");
			
			sm.set("enabled", true);
			ServerManager.resumeServer(sm);
		}
		
		splash.setStatus("JGlideMon Loaded Successfully!");
		
		// not critical to get these loaded before the gui shows
		Thread t = new Thread(new Runnable() {
			public void run() {
				jgm.wow.Item.Cache.loadIcons();
				jgm.wow.Item.Cache.loadItems();	
			}
		});
		
		t.start();
		
		splash.setVisible(false);
		splash.dispose();
		splash = null;
		
		try {
			Thread.sleep(3000); // yeah yeah...
		} catch (InterruptedException e) {}
	}

	public void destroy() {
		splash = new Splash("Shutting Down JGlideMon...");
		
		new Thread(new Runnable() {
			public void run() {
				// so it won't try to say disconnected
				jgm.util.Speech.destroy();
				
				synchronized (ServerManager.managers) {
					for (ServerManager sm : ServerManager.managers) {
						if (sm.state == ServerManager.State.ACTIVE) {
							splash.setStatus("Shutting Down \"" + sm.name + "\"...");
							sm.destroy();
						}
					}
				}
				
				splash.setStatus("Caching Icons and Items...");
				jgm.wow.Item.Cache.saveIcons();
				jgm.wow.Item.Cache.saveItems();
				
				splash.setStatus("Saving Settings...");
				ServerManager.saveConfig();
				jgm.Config.write();
				
				splash.dispose();
				
				System.exit(0);
			}
		}, "JGlideMon.destroy").start();
	}
	
	static final Logger log = Logger.getLogger(JGlideMon.class.getName());
	public static Splash splash;
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable thrown) {
				log.log(Level.WARNING, "Exception in thread \"" + thread.getName() + "\"", thrown);
			}
		});
		
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Coultn'd set L&F: " + e.getMessage());
		}
		
		splash = new Splash("Loading JGlideMon...");
		
		new Thread(new Runnable() {
			public void run() {
				new JGlideMon();
			}
		}, "JGlideMon.main").start();
	}
}
