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
	
	public static StatusUpdater instance = null;
	
	public Status last = new Status();
	public Status s = new Status();

	private Conn conn;
	private Thread thread;
	private volatile boolean stop = false;

	Config cfg;
	jgm.ServerManager sm;
	
	public StatusUpdater(jgm.ServerManager sm) {
		this.sm = sm;
		instance = this;
		conn = new Conn(sm);
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
	
	public void onConnecting() {}
	
	public void onConnect() {
		stop = false;
		thread = new Thread(this, "StatusUpdater");
		thread.start();
	}
	
	public void onDisconnecting() {}
	
	public void onDisconnect() {
		s.resetData();
		s.attached = false;
		setChanged();
		stop = true;
		notifyObservers(s);
	}
	
	public void run() {
		while (true) {
			if (stop) return;
			
			try {
				update();
				Thread.sleep(cfg.getLong("status.updateInterval"));
			} catch (Exception e) {
				log.fine("Stopping StatusUpdater, Ex: " + e.getMessage());
				sm.connector.disconnect();
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

		last = s.clone();
		
		s.version    = m.containsKey("Version")     ? m.get("Version")     : "";
		s.attached   = m.containsKey("Attached")
					 && m.get("Attached").equals("True")
					 							  ? true                 : false;
		s.mode       = m.containsKey("Mode")        ? m.get("Mode")        : "Auto";
		s.profile    = m.containsKey("Profile")     ? m.get("Profile")     : "";
		s.logMode    = m.containsKey("Log")         ? m.get("Log")         : "None";
		s.name       = m.containsKey("Name")        ? m.get("Name")        : "";
		s.clazz      = m.containsKey("Class")       ? jgm.wow.Class.strToClass(m.get("Class")) : jgm.wow.Class.UNKNOWN;
		s.location   = m.containsKey("Location")    ? m.get("Location")    : "";
		s.targetName = m.containsKey("Target-Name") ? m.get("Target-Name") : "";
		
		int i = s.profile.toLowerCase().indexOf("profiles\\");

		if (i >= 0) {
			s.profile = s.profile.substring(i + 9);
		} else {
			i = s.profile.toLowerCase().indexOf("groups\\");

			if (i >= 0) {
				s.profile = s.profile.substring(i + 7);
			}
		}

		try {
			s.health = m.containsKey("Health")
					 ? Double.parseDouble(m.get("Health"))
					 : 0.0;
		} catch (NumberFormatException e) {
			s.health = 0.0;
		}

		s.health *= 100; // health was a percent

		try {
			if (!m.containsKey("Mana")) {
				s.mana = 0.0;
				s.manaName = "Mana";
			} else {
				// "Mana: 123 (42%)"
				// "Mana: 100 (CP = 0)"
				Pattern p = s.clazz.mana.getRegex();
				Matcher x = p.matcher(m.get("Mana"));

//				System.out.println("Matching: " + m.get("Mana"));
				
				if (!x.matches()) throw new NumberFormatException("No Match");

//				System.out.print("Matched:");
//				for (int n = 0; n <= x.groupCount(); n++) {
//					System.out.print(" " + n + ": " + x.group(n));
//				}
//				System.out.println();
				
				int group = 1;
				
				// only check if the groups are null if there's more than 1
				// don't check the last one because we must assume that the
				// last one won't be null if all the others are
				for (int j = 1; j < s.clazz.mana.numRegexGroups(); j++) {
					if (x.group(group) == null) group++;
					else break;
				}
				
				s.mana = Double.parseDouble(x.group(group));
				s.manaName = s.clazz.mana.toString(group);
				
//				System.out.println("  Found: " + x.group(group) + "; group: " + group);
			}	
		} catch (Exception e) {
			//e.printStackTrace();
//			System.err.println("Did not match mana");
			s.mana = 0.0;
			s.manaName = "Mana";
		}

		try {
			s.level = m.containsKey("Level")
					? Integer.parseInt(m.get("Level"))
					: 0;
		} catch (NumberFormatException e) {
			s.level = 0;
		}

		try {
			s.experience = m.containsKey("Experience")
						 ? Integer.parseInt(m.get("Experience"))
						 : 0;
		} catch (NumberFormatException e) {
			s.experience = 0;
		}

		try {
			s.nextExperience = m.containsKey("Next-Experience")
							 ? Integer.parseInt(m.get("Next-Experience"))
							 : 0;
		} catch (NumberFormatException e) {
			s.nextExperience = 0;
		}

		try {
			s.xpPerHour = m.containsKey("XP/Hour")
						? Integer.parseInt(m.get("XP/Hour"))
						: 0;
		} catch (NumberFormatException e) {
			s.xpPerHour = 0;
		}

		if (s.nextExperience > 0) {
			s.xpPercent = (int) (100 * ((float) s.experience / (float) s.nextExperience));
		} else {
			s.xpPercent = 0;
		}
		
		try {
			if (!m.containsKey("Heading")) {
				s.heading = -1.0;
			} else {
				s.heading = Double.parseDouble(m.get("Heading"));
			}
		} catch (NumberFormatException e) {
			s.heading = -1.0;
		}

		try {
			if (!m.containsKey("KLD")) {
				s.kills = s.loots = s.deaths = 0;
			} else {
				String[] parts = m.get("KLD").split("/");

				if (parts.length != 3) throw new NumberFormatException();

				s.kills  = Integer.parseInt(parts[0]);
				s.loots  = Integer.parseInt(parts[1]);
				s.deaths = Integer.parseInt(parts[2]);
			}
		} catch (NumberFormatException e) {
			s.kills = s.loots = s.deaths = 0;
		}

		try {
			s.targetLevel = m.containsKey("Target-Level")
						  ? Integer.parseInt(m.get("Target-Level"))
						  : 0;
		} catch (NumberFormatException e) {
			s.targetLevel = 0;
		}

		try {
			s.targetHealth = m.containsKey("Target-Health")
						   ? Double.parseDouble(m.get("Target-Health"))
						   : 0.0;
		} catch (NumberFormatException e) {
			s.targetHealth = 0.0;
		}

		s.targetHealth *= 100; // health was a percent

		setChanged();
		notifyObservers(s);
	}
}
