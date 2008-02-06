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
import java.util.*;

import jgm.util.*;

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
	
	public  GUI           gui;
	public  Config        cfg;
	
	List<ServerManager> managers = new Vector<ServerManager>();
	
	public static ServerManager getCurManager() {
		return instance.managers.get(0);
	}
	
	public JGlideMon() {
		instance = this;
		init();
	}
	
	private void init() {
		// initialize logger
		Log.reloadConfig();
		cfg = new Config();
		
		// yeah... so that if it converts JGlideMon.ini
		// the log line for it looks nice but this
		// needs to be called again in case the config
		// has log.debug=true
		Log.reloadConfig();
		
		try {
			jgm.glider.Profile.Cache.loadProfiles();
		} catch (Throwable e) {} // doesn't matter here 

		// put this here so that the tts config options will
		// be enabled if tts is available and JGM has yet to
		// be configured for the first time
		Sound.init();
		Speech.init();
		
		managers.add(new ServerManager(0));

		gui = new GUI();
		
		for (ServerManager sm : managers) {
			sm.init(gui);
		}
		
		gui.managerChanged(managers.get(0));
		
		gui.makeVisible();
		
		// not critical to get these loaded before the gui shows
		jgm.wow.Item.Cache.loadIcons();
		jgm.wow.Item.Cache.loadItems();
		
		
		new HTTPD();
		
		if (cfg.getBool("web.enabled")) {
			try {
				HTTPD.instance.start(cfg.getInt("web.port"));
			} catch (java.io.IOException e) {
				JOptionPane.showMessageDialog(GUI.frame,
					"Unable to start web-server.\n" +
					"Port " + cfg.getInt("web.port") + " is unavailible.",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
	}

	public void destroy() {
		for (ServerManager sm : managers) {
			if (sm.connector.isConnected()) {
				try {
					Thread t = sm.connector.disconnect();
					t.join();
				} catch (InterruptedException e) {}
			}
		}
		
		Speech.destroy();
		jgm.wow.Item.Cache.saveIcons();
		jgm.wow.Item.Cache.saveItems();
		HTTPD.instance.stop(); // doesn't matter if it's actually running or not
		jgm.Config.write();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
