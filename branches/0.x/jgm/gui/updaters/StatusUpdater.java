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
package jgm.gui.updaters;

import jgm.Config;

import jgm.glider.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;

public class StatusUpdater extends Observable
	implements Runnable, ConnectionListener {
	static Logger log = Logger.getLogger(StatusUpdater.class.getName());
	
	public String version        = "";
	public boolean attached      = false;
	public String mode           = "Auto";
	public String profile        = "";
	public String logMode        = "None";
	public double health         = 0.0;
	public double mana           = 0.0;
	public String name           = "";
	public jgm.wow.Class  clazz  = jgm.wow.Class.UNKNOWN; // class
	public int    level          = 0;
	public int    experience     = 0;
	public int    nextExperience = 0;
	public int    xpPerHour      = 0;
	public String location       = "";
	public double heading        = -1.0;
	public int    kills          = 0;
	public int    loots          = 0;
	public int    deaths         = 0;
	public String targetName     = "";
	public int    targetLevel    = 0;
	public double targetHealth   = 0.0;

	private Conn conn;
	private Thread thread;
	private volatile boolean stop = false;

	private Config cfg;
	
	public StatusUpdater() {
		conn = new Conn();
		cfg = jgm.Config.getInstance();
	}

	public void close() {
		stop = true;
		if (thread != null) thread.interrupt();
		if (conn != null) conn.close();
	}

	public Conn getConn() {
		return conn;
	}
	
	public void connecting() {}
	
	public void connectionEstablished() {
		stop = false;
		thread = new Thread(this, "StatusUpdater");
		thread.start();
	}
	
	public void disconnecting() {}
	
	public void connectionDied() {
		attached = false;
		setChanged();
		stop = true;
		notifyObservers(this);
	}
	
	public void run() {
		while (true) {
			if (stop) return;
			
			try {
				update();
				Thread.sleep(cfg.getLong("status", "updateInterval"));
			} catch (Exception e) {
				log.fine("Stopping StatusUpdater, Ex: " + e.getMessage());
				Connector.disconnect();
				return;
			}
		}
	}

	private void update()
		throws NullPointerException, InterruptedException, IOException {
		
		String line = null;
		Map<String, String> m = new HashMap<String, String>();
		
		try {
		BufferedReader r = conn.getIn();

		conn.send("/status");

		while ((line = r.readLine()) != null) {
			if (line.equals("---")) break;

			String[] parts = line.split(":", 2);

			if (parts.length != 2) continue;

			m.put(parts[0], parts[1].trim());
//			System.out.println(parts[0] + ": " + parts[1].trim());
		}
		} catch (Exception e) {}

//		System.out.println("--><--");

		version    = m.containsKey("Version")     ? m.get("Version")     : "";
		attached   = m.containsKey("Attached")
					 && m.get("Attached").equals("True")
					 							  ? true                 : false;
		mode       = m.containsKey("Mode")        ? m.get("Mode")        : "Auto";
		profile    = m.containsKey("Profile")     ? m.get("Profile")     : "";
		logMode    = m.containsKey("Log")         ? m.get("Log")         : "None";
		name       = m.containsKey("Name")        ? m.get("Name")        : "";
		clazz      = m.containsKey("Class")       ? jgm.wow.Class.strToClass(m.get("Class")) : jgm.wow.Class.UNKNOWN;
		location   = m.containsKey("Location")    ? m.get("Location")    : "";
		targetName = m.containsKey("Target-Name") ? m.get("Target-Name") : "";
		
		int i = profile.toLowerCase().indexOf("profiles\\");

		if (i >= 0) {
			profile = profile.substring(i + 9);
		} else {
			i = profile.toLowerCase().indexOf("groups\\");

			if (i >= 0) {
				profile = profile.substring(i + 7);
			}
		}

		try {
			health = m.containsKey("Health")
					 ? Double.parseDouble(m.get("Health"))
					 : 0.0;
		} catch (NumberFormatException e) {
			health = 0.0;
		}

		health *= 100; // health was a percent

		try {
			if (!m.containsKey("Mana")) {
				mana = 0.0;
			} else {
				// "Mana: 123 (42%)"
				// "Mana: 100 (CP = 0)"
				Pattern p = clazz.mana.getRegex();
				Matcher x = p.matcher(m.get("Mana"));

				//System.out.println("Matching: " + m.get("Mana"));
				
				if (!x.matches()) throw new NumberFormatException("No Match");

				//System.out.print("Matched:");
				//for (int n = 0; n <= x.groupCount(); n++) {
				//	System.out.print(" " + n + ": " + x.group(n));
				//}
				//System.out.println();
				
				//System.out.println("  Found: " + x.group(1));
				mana = Double.parseDouble(x.group(1));
			}	
		} catch (Exception e) {
			//e.printStackTrace();
			//System.err.println("Did not match mana");
			mana = 0.0;
		}

		try {
			level = m.containsKey("Level")
					? Integer.parseInt(m.get("Level"))
					: 0;
		} catch (NumberFormatException e) {
			level = 0;
		}

		try {
			experience = m.containsKey("Experience")
						 ? Integer.parseInt(m.get("Experience"))
						 : 0;
		} catch (NumberFormatException e) {
			experience = 0;
		}

		try {
			nextExperience = m.containsKey("Next-Experience")
							 ? Integer.parseInt(m.get("Next-Experience"))
							 : 0;
		} catch (NumberFormatException e) {
			nextExperience = 0;
		}

		try {
			xpPerHour = m.containsKey("XP/Hour")
						? Integer.parseInt(m.get("XP/Hour"))
						: 0;
		} catch (NumberFormatException e) {
			xpPerHour = 0;
		}

		try {
			if (!m.containsKey("Heading")) {
				heading = -1.0;
			} else {
				heading = Double.parseDouble(m.get("Heading"));
			}
		} catch (NumberFormatException e) {
			heading = -1.0;
		}

		try {
			if (!m.containsKey("KLD")) {
				kills = loots = deaths = 0;
			} else {
				String[] parts = m.get("KLD").split("/");

				if (parts.length != 3) throw new NumberFormatException();

				kills  = Integer.parseInt(parts[0]);
				loots  = Integer.parseInt(parts[1]);
				deaths = Integer.parseInt(parts[2]);
			}
		} catch (NumberFormatException e) {
			kills = loots = deaths = 0;
		}

		try {
			targetLevel = m.containsKey("Target-Level")
						  ? Integer.parseInt(m.get("Target-Level"))
						  : 0;
		} catch (NumberFormatException e) {
			targetLevel = 0;
		}

		try {
			targetHealth = m.containsKey("Target-Health")
						   ? Double.parseDouble(m.get("Target-Health"))
						   : 0.0;
		} catch (NumberFormatException e) {
			targetHealth = 0.0;
		}

		targetHealth *= 100; // health was a percent

		setChanged();
		notifyObservers(this);
	}
}
