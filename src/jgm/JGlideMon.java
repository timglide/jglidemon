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
	public static final String version = "0.18";
	public static final String _revision = "$Revision$";
	public static final String revision = _revision.substring(1, _revision.length() - 1);
	public static final int revisionNum = initRevisionNum();
	
	private static int initRevisionNum() {
		try {
			return Integer.parseInt(revision.replaceAll("[^\\d]+", ""));
		} catch (NumberFormatException e) {}
		
		return 0;
	}
	
	
	public static final String _date = "$Date$";
	public static final String date = _date.substring(1, _date.length() - 1);
	
	public static boolean debug = true;
	
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

				
		// put this here so that the tts config options will
		// be enabled if tts is available and JGM has yet to
		// be configured for the first time
		splash.setStatus("Initializing Text-to-Speech Resources...");
		jgm.util.Speech.init();
		
		splash.setStatus("Initializing Server Managers...");
		
		ServerManager.loadServers();
				
		boolean atLeastOneRunning = false;
		
		// resume all the servers
		synchronized (ServerManager.managers) {
			for (ServerManager sm : ServerManager.managers) {
				if (sm.getBool("enabled")) {
					splash.setStatus("Initializing Server Manager \"" + sm.name + "\"...");
					
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
			
			splash.setStatus("Initializing Server Manager \"" + sm.name + "\"...");
			
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
		}, "JGlideMon.LoadCache");
		
		t.start();
		
		try {
			Thread.sleep(1000); // yeah yeah, but it's reassuring to see that it was successful...
		} catch (InterruptedException e) {}

		splash.setVisible(false);
		splash.dispose();
		splash = null;
	}

	public Thread destroy() {
		return destroy(false);
	}
	
	public Thread destroy(final boolean fromHook) {
		if (!fromHook)
			splash = new Splash("Shutting Down JGlideMon...");
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				// so it won't try to say disconnected
				jgm.util.Speech.destroy();
				
				synchronized (ServerManager.managers) {
					for (ServerManager sm : ServerManager.managers) {
						if (sm.state == ServerManager.State.ACTIVE) {
							if (null != splash)
								splash.setStatus("Shutting Down Server Manager \"" + sm.name + "\"...");
							sm.destroy(fromHook);
						}
					}
				}
				
				if (null != splash)
					splash.setStatus("Caching Icons and Items...");
				jgm.wow.Item.Cache.saveIcons();
				jgm.wow.Item.Cache.saveItems();
				
				if (null != splash)
					splash.setStatus("Saving Settings...");
				ServerManager.saveConfig();
				jgm.Config.write();
				
				if (null != splash)
					splash.dispose();
				
				instance = null;
				
				if (!fromHook)
					System.exit(0);
			}
		}, "JGlideMon.destroy");
		
		t.start();
		
		return t;
	}
	
	@Override
	protected void finalize() {
		// pretend it's like the shutdown hook
		try {
			destroy(true).join();
		} catch (InterruptedException e) {}
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
				
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					public void run() {
						if (null != JGlideMon.instance)
							try {
								JGlideMon.instance.destroy(true).join();
							} catch (InterruptedException e) {}
					}
				}, "JGlideMon.ShutdownHook"));
			}
		}, "JGlideMon.main").start();
	}
}
