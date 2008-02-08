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
	public static Config        cfg;
	
	public static List<ServerManager> managers = null;
	
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
		
		ServerManager.loadServers();
		managers = ServerManager.managers;
				
		boolean atLeastOneRunning = false;
		
		// resume all the servers
		for (ServerManager sm : managers) {
			if (sm.p.getBool("enabled")) {
				atLeastOneRunning = true;
				ServerManager.resumeServer(sm);
			}
		}
		
		// add a server if there are none
		if (managers.size() == 0) {
			ServerManager.addServer();
		}
		
		// if none are running force the first to resume
		if (!atLeastOneRunning) {
			ServerManager sm = managers.get(0);
			sm.p.set("enabled", true);
			ServerManager.resumeServer(sm);
		}
		
		// not critical to get these loaded before the gui shows
		jgm.wow.Item.Cache.loadIcons();
		jgm.wow.Item.Cache.loadItems();
	}

	public void destroy() {
		for (ServerManager sm : managers) {
			sm.destroy();
		}
		
		Speech.destroy();
		jgm.wow.Item.Cache.saveIcons();
		jgm.wow.Item.Cache.saveItems();
		
		ServerManager.saveConfig();
		jgm.Config.write();
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
