/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 - 2008 Tim
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
import java.util.logging.*;
import javax.swing.*;

import jgm.glider.*;
import jgm.gui.GUI;
import jgm.gui.updaters.*;
import jgm.httpd.HTTPD;
import jgm.util.Properties;

/**
 * This class manages everything having to do
 * with a particular Glider that jgm connects to.
 * It manages the gui stuff for that server as 
 * well as maintaining the connection.
 * 
 * @author Tim
 * @since 0.15
 */
public final class ServerManager implements Comparable<ServerManager> {	
	static Logger log = Logger.getLogger(ServerManager.class.getName());
	
	static public Config cfg = Config.c;
	static Properties DEFAULTS = new Properties();
	
	static {
		log.finest("Loading default server settings");
		
		try {
			DEFAULTS.load(JGlideMon.class.getResourceAsStream("properties/JGlideMon.defaults.properties"));
		} catch (java.io.IOException e) {
			log.log(Level.WARNING, "Unable to load default server settings", e);
			System.exit(-1);
		}
		
		final String NEEDLE = "servers.0.";			
		for (Object key : DEFAULTS.keySet().toArray()) {
			String s = key.toString();
			
			if (s.startsWith(NEEDLE)) {
				String newKey = s.substring(NEEDLE.length());
				log.finest(String.format("  %s=%s", newKey, DEFAULTS.get(s)));
				DEFAULTS.setProperty(newKey, DEFAULTS.get(s));
			}
			
			DEFAULTS.remove(key);
		}
	}
	
	// TreeSet to keep them ordered by name
	public static List<ServerManager> managers =
		new Vector<ServerManager>();
	
	public static void loadServers() {
		for (int i = 0; ; i++) {
			String needle = "servers." + i + ".";
			
			if (cfg.has(needle + "name")) {
				Properties p = new Properties(DEFAULTS);
				
				for (Object o : cfg.p.keySet().toArray()) {
					String key = o.toString();
					if (key.startsWith(needle)) {
						p.set(key.substring(needle.length()), cfg.get(key));
					}
				}
				
				ServerManager sm = new ServerManager(p);
				
				if (managers.contains(sm)) {
					log.warning("A server named \"" + sm.name + "\" already exists!");
				} else {
					synchronized (managers) {
						managers.add(new ServerManager(p));
						Collections.sort(managers);
					}
				}
			} else
				break;
		}
	}
	
	public static void saveConfig() {	
		cfg.clearKeys("servers.");
		
		synchronized (managers) {
			int i = 0;
			for (ServerManager sm : managers) {			
				log.finest("Saving config for \"" + sm.name + "\"");
				
				sm.set("name", sm.name);
				sm.set("net.host", sm.host);
				sm.set("net.port", Integer.toString(sm.port));
				sm.set("net.password", sm.password);
				
				for (Object o : sm.p.keySet().toArray()) {
					String key = o.toString();
					String newKey = "servers." + i + "." + key;
					
					log.finest(String.format("  %s=%s", newKey, sm.get(key)));
					
					cfg.set(newKey, sm.get(key));
				}
				
				i++;
			}
		}
	}
	
	public static void addServer() {
		ServerManager sm = new ServerManager();
		
		synchronized (managers) {
			managers.add(sm);
			Collections.sort(managers);
		}
		
		sm.firstRun = true;
		sm.init();
		
		fireServerAdded(sm);
	}
	
	public void suspend() {
		suspendServer(this);
	}
	
	public static void suspendServer(ServerManager sm) {
//		if (!sm.getBool("enabled")) return; // already suspended
		
		sm.set("enabled", false);
		sm.destroy();
		
		ServerManager.saveConfig();
		jgm.Config.write();
		
		fireServerSuspended(sm);
	}
	
	public void resume() {
		resumeServer(this);
	}
	
	public static void resumeServer(ServerManager sm) {
//		if (sm.getBool("enabled")) return; // already running
		
		sm.set("enabled", true);
		sm.init();
		sm.toFront();
		
		ServerManager.saveConfig();
		jgm.Config.write();
		
		fireServerResumed(sm);
	}
	
	public static void removeServer(ServerManager sm) {
		log.finer("Removing server: " + sm.name);
		
		synchronized (managers) {
			managers.remove(sm);
			Collections.sort(managers);
		}
		
		sm.destroy();
		
		ServerManager.saveConfig();
		jgm.Config.write();
		
		fireServerRemoved(sm);
		
		System.gc();
	}
	
	public static int getActiveCount() {
		int ret = 0;
		
		synchronized (managers) {
			for (ServerManager sm : managers) {
				if (sm.state == State.ACTIVE)
					ret++;
			}
		}		
		return ret;
	}
	
	public static boolean contains(String name) {
		synchronized (managers) {
			for (ServerManager sm : managers) {
				if (sm.name.equalsIgnoreCase(name))
					return true;
			}
		}		
		return false;
	}
	
	public static void connectAll() {
		synchronized (managers) {
			for (ServerManager sm : managers) {
				if (sm.state == State.ACTIVE)
					sm.connector.connect(true);
			}
		}		
	}
	
	public static void disconnectAll() {
		synchronized (managers) {
			for (ServerManager sm : managers) {
				if (sm.state == State.ACTIVE)
					sm.connector.disconnect(true);
			}
		}		
	}
	
	public enum State {
		UNKNOWN, ACTIVE, SUSPENDED
	}
	
	public boolean       firstRun = false;
	
	public State         state = State.UNKNOWN;
	public Conn          keysConn;
	public GUI           gui;
	public CommandManager cmd;
	public HTTPD         httpd = null;
	public StatusUpdater status;
	public LogUpdater    logUpdater;
	public SSUpdater     ssUpdater;
	public PlayerChartUpdater chartUpdater;
	
	public Connector connector;

	ConnectionListener myConnectionListener = null;
	
	private jgm.util.Properties p;
	public String name;
	public String host;
	public int port;
	public String password;
	
	private ServerManager() {
		this(null);
	}
	
	private ServerManager(Properties p) {		
		if (p == null) {			
			p = new Properties(DEFAULTS);
			p.setProperty("name", p.getProperty("name") + (managers.size() > 0 ? " " + managers.size() : ""));
		}
		
		this.p = p;
		this.name = p.getProperty("name");
		this.host = p.getProperty("net.host");
		this.port = Integer.parseInt(p.getProperty("net.port"));
		this.password = p.getProperty("net.password");
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String toFullString() {
		return name + " - " + host + ":" + port;
	}
	
	public void set(String propertyName, Object value) {
		p.set(propertyName, value);
		
		if (propertyName.equals("name")) {
			name = value.toString();
		} else if (propertyName.equals("net.host")) {
			host = value.toString();
		} else if (propertyName.equals("net.port")) {
			port = Integer.parseInt(value.toString());
		} else if (propertyName.equals("net.password")) {
			password = value.toString();
		} else if (propertyName.equals("icon")) {
			gui.setIcon();
		}
		
		fireServerPropChanged(this, propertyName, value);
	}
	
	public String get(String propertyName) {
		return p.get(propertyName);
	}
	
	public int getInt(String propertyName) {		
		return p.getInt(propertyName);
	}
	
	public boolean getBool(String propertyName) {		
		return p.getBool(propertyName);
	}
	
	public long getLong(String propertyName) {		
		return p.getLong(propertyName);
	}
	
	public double getDouble(String propertyName) {		
		return p.getDouble(propertyName);
	}
	
	public void toFront() {
		gui.frame.setVisible(true);
		gui.frame.setExtendedState(gui.frame.getExtendedState() & ~JFrame.ICONIFIED);
		gui.frame.requestFocus();
		gui.frame.toFront();
	}
	
	public void init() {
		if (state == State.ACTIVE)
			throw new IllegalStateException("Cannot init if already active");
		
		connector  = new Connector(this);
		cmd        = new CommandManager(this);
		gui        = new GUI(this);
		
		keysConn   = new Conn(ServerManager.this, "KeysConn");
		
		myConnectionListener = new ConnectionAdapter() {
			final Conn myConn = keysConn;
			
			public Conn getConn() {
				return myConn;
			}
		};
		
		connector.addListener(myConnectionListener);
		logUpdater = new LogUpdater(ServerManager.this, gui.tabsPane);
		connector.addListener(logUpdater);
		ssUpdater  = new SSUpdater(ServerManager.this, gui.tabsPane.screenshotTab);
		connector.addListener(ssUpdater);
		status     = new StatusUpdater(ServerManager.this);
		connector.addListener(status);
		status.addObserver(gui);
		status.addObserver(ssUpdater);
		
		chartUpdater = new PlayerChartUpdater(ServerManager.this);
		
		gui.makeVisible();
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				if (firstRun || !jgm.Config.fileExists()) {					
					JOptionPane.showMessageDialog(gui.frame,
						"Please enter the remote host, port, and password.\n" +
						"Next, click Save Settings, then click Connect.\n\n" +
						"Remember to click Save Settings any time you change a setting.\n" +
						"You may access the configuration screen later via the File menu.",
						"Configuration Required",
						JOptionPane.INFORMATION_MESSAGE);
					
					// select the network tab
					gui.showConfig(1);
				}
				
				connector.connect();				
				firstRun = false;
			}
		};

		Thread t = new Thread(r, this.name + ":ServerManager.init");
		t.start();
		
		if (p.getBool("web.enabled")) {
			try {
				startHttpd();
			} catch (java.io.IOException e) {
				JOptionPane.showMessageDialog(gui.frame,
					"Unable to start web-server.\n" +
					"Port " + p.get("web.port") + " is unavailible.",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
		
		state = State.ACTIVE;
	}
	
	public void startHttpd() throws java.io.IOException {
		if (httpd != null) return; // already started
		
		httpd = new HTTPD(this);
		
		try {
			httpd.start();
		} catch (java.io.IOException e) {
			httpd = null;
			throw e;
		}
	}
	
	public void stopHttpd() {
		if (httpd == null) return; // already stopped
		
		httpd.stop();
		httpd = null;
		
		System.gc();
	}
	
	public void destroy() {
		destroy(false);
	}
	
	public void destroy(boolean fromHook) {
		if (state != State.ACTIVE)
			throw new IllegalStateException("Cannot destroy without active state");
		
		stopHttpd();
		
		cmd.destroy();
		cmd = null;
		
		chartUpdater.destroy();		
		connector.destroy();
		
		// TODO
		// i would think this should be closed by
		// connector.destroy() but it's not
		keysConn.close();
		
		connector.removeListener(myConnectionListener);
		connector.removeListener(logUpdater);
		connector.removeListener(ssUpdater);
		connector.removeListener(status);
		
		// seems to hang the shutdown hook
		if (!fromHook)
			gui.destroy();
		gui = null;
		
		state = State.SUSPENDED;
		
		System.gc();
	}
	
	/////////////
	// Comparable
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		
		if (obj instanceof String)
			return ((String) obj).equalsIgnoreCase(name);
		
		if (obj instanceof ServerManager) {
			ServerManager sm = (ServerManager) obj;
			return
				sm.name.equalsIgnoreCase(this.name);
		}
		
		return false;
	}
	
	public int compareTo(ServerManager sm) {
		return this.name.compareToIgnoreCase(sm.name);
	}
	
	///////////
	// Listener
	
	public interface Listener extends EventListener {
		public void serverAdded(ServerManager sm);
		public void serverRemoved(ServerManager sm);
		public void serverSuspended(ServerManager sm);
		public void serverResumed(ServerManager sm);
		public void serverPropChanged(ServerManager sm, String prop, Object value);
	}
	
	public class Adapter implements Listener {
		public void serverAdded(ServerManager sm) {}
		public void serverRemoved(ServerManager sm) {}
		public void serverSuspended(ServerManager sm) {}
		public void serverResumed(ServerManager sm) {}
		public void serverPropChanged(ServerManager sm, String prop, Object value) {}
	}
	
	static javax.swing.event.EventListenerList listeners = 
		new javax.swing.event.EventListenerList();
	
	public static void addListener(Listener l) {
		listeners.add(Listener.class, l);
	}
	
	public static void removeListener(Listener l) {
		listeners.remove(Listener.class, l);
	}
	
	private static void fireServerAdded(ServerManager sm) {
		Object[] ls = listeners.getListenerList();
		
		for (int i = ls.length - 2; i>=0; i-=2) {
			if (ls[i] == Listener.class) {
				((Listener) ls[i + 1]).serverAdded(sm);
			}
		}
	}
	
	private static void fireServerRemoved(ServerManager sm) {
		Object[] ls = listeners.getListenerList();
		
		for (int i = ls.length - 2; i>=0; i-=2) {
			if (ls[i] == Listener.class) {
				((Listener) ls[i + 1]).serverRemoved(sm);
			}
		}
	}
	
	private static void fireServerSuspended(ServerManager sm) {
		Object[] ls = listeners.getListenerList();
		
		for (int i = ls.length - 2; i>=0; i-=2) {
			if (ls[i] == Listener.class) {
				((Listener) ls[i + 1]).serverSuspended(sm);
			}
		}
	}
	
	private static void fireServerResumed(ServerManager sm) {
		Object[] ls = listeners.getListenerList();
		
		for (int i = ls.length - 2; i>=0; i-=2) {
			if (ls[i] == Listener.class) {
				((Listener) ls[i + 1]).serverResumed(sm);
			}
		}
	}
	
	private static void fireServerPropChanged(ServerManager sm, String prop, Object value) {
		Object[] ls = listeners.getListenerList();
		
		for (int i = ls.length - 2; i>=0; i-=2) {
			if (ls[i] == Listener.class) {
				((Listener) ls[i + 1]).serverPropChanged(sm, prop, value);
			}
		}
	}
}
