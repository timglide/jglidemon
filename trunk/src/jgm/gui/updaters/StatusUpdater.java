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
	
	public Status last = new Status();
	public Status s = new Status();

	private Conn conn;
	private Thread thread;
	private volatile boolean stop = false;

	Config cfg;
	final jgm.ServerManager sm;
	
	public StatusUpdater(final jgm.ServerManager sm) {
		this.sm = sm;
		conn = new Conn(sm, "StatusUpdater");
		cfg = jgm.Config.getInstance();
	}
	
	public void onDestroy() {
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
		thread = new Thread(this, sm.name + ":StatusUpdater");
		thread.setDaemon(true);
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
				sm.connector.someoneDisconnected();
				return;
			}
		}
	}
	
	private void update()
		throws NullPointerException, InterruptedException, IOException {
		
		String line = null;
		Map<String, String> m = new HashMap<String, String>();
		
		try {
		Command.getStatusCommand().send(conn);

		while ((line = conn.readLine()) != null) {
			if (line.equals("---")) break;

			String[] parts = line.split(":", 2);

			if (parts.length != 2) continue;

			m.put(parts[0], parts[1].trim());
//			System.out.println(parts[0] + ": " + parts[1].trim());
		}
		} catch (Exception e) {}

//		System.out.println("--><--");

		Status tmp = last;
		last = s;
		s = tmp;
		s.resetData();
		
		s.version    = _(m, "Version");
		s.attached   = "True".equals(_(m, "Attached"));;
		s.mode       = _(m, "Mode", "Auto");
		s.profile    = _(m, "Profile");
		s.logMode    = _(m, "Log", "None");
		s.name       = _(m, "Name");
		s.clazz      = m.containsKey("Class") ? jgm.wow.Class.strToClass(m.get("Class")) : jgm.wow.Class.UNKNOWN;
		s.location   = _(m, "Location");
		s.targetName = _(m, "Target-Name");
		
		int i = s.profile.toLowerCase().indexOf("profiles\\");

		if (i >= 0) {
			s.profile = s.profile.substring(i + 9);
		} else {
			i = s.profile.toLowerCase().indexOf("groups\\");

			if (i >= 0) {
				s.profile = s.profile.substring(i + 7);
			}
		}

		s.health = _(m, "Health", 0.0);
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
				
				int group = s.clazz.mana.getDisplayRegexGroup();
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

		s.level = _(m, "Level", 0);
		s.experience = _(m, "Experience", 0);
		s.nextExperience = _(m, "Next-Experience", 0);
		s.xpPerHour = _(m, "XP/Hour", 0);

		if (s.nextExperience > 0) {
			s.xpPercent = (int) (100 * ((float) s.experience / (float) s.nextExperience));
		} else {
			s.xpPercent = 0;
		}
		
		s.heading = _(m, "Heading", -1.0);

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

		s.targetLevel = _(m, "Target-Level", 0);
		s.targetHealth = _(m, "Target-Health", 0.0);
		s.targetHealth *= 100; // health was a percent

		
		
		s.goalText = _(m, "Goal-Text");
		s.statusText = _(m, "Status-Text");
		s.copper = _(m, "Copper", 0L);
		s.timeToLevel = _(m, "Time-To-Level", Double.POSITIVE_INFINITY); // in seconds
		s.targetIsPlayer = _(m, "Target-Is-Player", false);
		s.honorGained = _(m, "Honor-Gained", 0);
		s.honorPerHour = _(m, "Honor/Hour", 0);
		s.bgsWon = _(m, "BGs-Won", 0);
		s.bgsLost = _(m, "BGs-Lost", 0);
		s.bgsCompleted = _(m, "BGs-Completed", 0);
		s.bgsWonPerHour = _(m, "BGs-Won/Hour", 0);
		s.bgsLostPerHour = _(m, "BGs-Lost/Hour", 0);
		s.bgsPerHour = _(m, "BGs/Hour", 0);
		s.killsPerHour = _(m, "Kills/Hour", 0);
		s.lootsPerHour = _(m, "Loots/Hour", 0);
		s.deathsPerHour = _(m, "Deaths/Hour", 0);
		
		s.nodes = _(m, "Nodes", 0);
		s.nodesPerHour = _(m, "Nodes/Hour", 0);
		s.solves = _(m, "Solves", 0);
		s.solvesPerHour = _(m, "Solves/Hour", 0);
		s.fish = _(m, "Fish", 0);
		s.fishPerHour = _(m, "Fish/Hour", 0);
		
		s.running = _(m, "Running", false);
		s.accountName = _(m, "Account-Name");
		s.realm = _(m, "Realm");
		s.map = _(m, "Map");
		s.mapId = _(m, "Map-Id", -1);
		s.zone = _(m, "Zone");
		s.realZone = _(m, "Real-Zone");
		s.subZone = _(m, "Sub-Zone");
		
		
		setChanged();
		notifyObservers(s);
		
		if (s.attached && !last.attached)
			fireOnAttach();
		else if (!s.attached && last.attached)
			fireOnDetach();
	}
	
	private String _(Map<String, String> m, String key) {
		return _(m, key, "");
	}
	
	private String _(Map<String, String> m, String key, String defaultValue) {
		return m.get(key) != null ? m.get(key) : defaultValue;
	}
	
	private int _(Map<String, String> m, String key, int defaultValue) {
		try {
			return m.get(key) != null ? Integer.parseInt(m.get(key)) : defaultValue;
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	private long _(Map<String, String> m, String key, long defaultValue) {
		try {
			return m.get(key) != null ? Long.parseLong(m.get(key)) : defaultValue;
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	private double _(Map<String, String> m, String key, double defaultValue) {
		try {
			return m.get(key) != null ? Double.parseDouble(m.get(key)) : defaultValue;
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	private boolean _(Map<String, String> m, String key, boolean defaultValue) {
		return m.get(key) != null ? Boolean.parseBoolean(m.get(key)) : defaultValue;
	}
	
	List<GliderListener> gliderListeners = new Vector<GliderListener>();
	
	public void addGliderListener(GliderListener l) {
		gliderListeners.add(l);
	}
	
	public void removeGliderListener(GliderListener l) {
		gliderListeners.remove(l);
	}
	
	private void fireOnAttach() {
		for (GliderListener l : gliderListeners) {
			l.onAttach();
		}
	}
	
	private void fireOnDetach() {
		for (GliderListener l : gliderListeners) {
			l.onDetach();
		}
	}
}
