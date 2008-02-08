package jgm;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;

import jgm.glider.Conn;
import jgm.glider.ConnectionAdapter;
import jgm.glider.Connector;
import jgm.gui.updaters.LogUpdater;
import jgm.gui.updaters.PlayerChartUpdater;
import jgm.gui.updaters.SSUpdater;
import jgm.gui.updaters.StatusUpdater;

/**
 * This class manages everything having to do
 * with a particular Glider that jgm connects to.
 * It manages the gui stuff for that server as 
 * well as maintaining the connection.
 * 
 * @author Tim
 * @since 0.15
 */
public class ServerManager {	
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
								
				managers.add(new ServerManager(p));
			} else
				break;
		}
	}
	
	public static void saveConfig() {
		Iterator<Object> it = cfg.p.keySet().iterator();
		
		while (it.hasNext()) {
			String s = it.next().toString();
			
			if (s.startsWith("servers.")) {
				it.remove();
			}
		}
		
		for (int i = 0; i < managers.size(); i++) {
			ServerManager sm = managers.get(i);
			
			log.finest("Saving config for \"" + sm.name + "\"");
			
			sm.p.set("name", sm.name);
			sm.p.set("net.host", sm.host);
			sm.p.set("net.port", Integer.toString(sm.port));
			sm.p.set("net.password", sm.password);
			
			for (Object o : sm.p.keySet().toArray()) {
				String key = o.toString();
				String newKey = "servers." + i + "." + key;
				
				log.finest(String.format("  %s=%s", newKey, sm.p.get(key)));
				
				cfg.set(newKey, sm.p.get(key));
			}
		}
	}
	
	public static void addServer() {
		ServerManager sm = new ServerManager();
		managers.add(sm);
		sm.firstRun = true;
		sm.init();
		
		fireServerAdded(sm);
	}
	
	public void suspend() {
		suspendServer(this);
	}
	
	public static void suspendServer(ServerManager sm) {
//		if (!sm.p.getBool("enabled")) return; // already suspended
		
		sm.p.set("enabled", false);
		sm.destroy();
		
		fireServerSuspended(sm);
	}
	
	public void resume() {
		resumeServer(this);
	}
	
	public static void resumeServer(ServerManager sm) {
//		if (sm.p.getBool("enabled")) return; // already running
		
		sm.p.set("enabled", true);
		sm.init();
		
		fireServerResumed(sm);
	}
	
	public static void removeServer(ServerManager sm) {			
		managers.remove(sm);
		sm.destroy();
		
		fireServerRemoved(sm);
		
		System.gc();
	}
	
	public static int getActiveCount() {
		int ret = 0;
		
		for (ServerManager sm : managers) {
			if (sm.state == State.ACTIVE)
				ret++;
		}
		
		return ret;
	}
	
	public enum State {
		UNKNOWN, ACTIVE, SUSPENDED
	}
	
	public boolean       firstRun = false;
	
	public State         state = State.UNKNOWN;
	public Conn          keysConn;
	public GUI           gui;
	public HTTPD         httpd = null;
	public StatusUpdater status;
	public LogUpdater    logUpdater;
	public SSUpdater     ssUpdater;
	public PlayerChartUpdater chartUpdater;
	
	public Connector connector;

	public jgm.Properties p;
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
		
		connector = new Connector(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String toFullString() {
		return name + " - " + host + ":" + port;
	}
	
	public void init() {
		if (state == State.ACTIVE)
			throw new IllegalStateException("Cannot init if already active");
		
		gui = new GUI(this);

		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
//		Runnable r = new Runnable() {
//			public void run() {
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
				
				keysConn = new Conn(ServerManager.this);
				connector.addListener(new ConnectionAdapter() {
					public Conn getConn() {
						return keysConn;
					}
				});
				logUpdater = new LogUpdater(ServerManager.this, gui.tabsPane);
				connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(ServerManager.this, gui.tabsPane.screenshotTab);
				connector.addListener(ssUpdater);
				status     = new StatusUpdater(ServerManager.this);
				connector.addListener(status);
				status.addObserver(gui);
				status.addObserver(ssUpdater);
				
				chartUpdater = new PlayerChartUpdater(ServerManager.this);
				
				connector.connect();
				gui.makeVisible();
				
				firstRun = false;
//			}
//		};
//
//		Thread t = new Thread(r, "ServerManager.init");
//		t.start();
		
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
		if (state != State.ACTIVE)
			throw new IllegalStateException("Cannot destroy without active state");
		
		if (connector.isConnected()) {
			try {
				Thread t = connector.disconnect(true);
				t.join();
			} catch (InterruptedException e) {}
		}
		
		stopHttpd();
		gui.destroy();
		gui = null;
		
		state = State.SUSPENDED;
		
		System.gc();
	}
	
	
	
	///////////
	// Listener
	
	public interface Listener extends EventListener {
		public void serverAdded(ServerManager sm);
		public void serverRemoved(ServerManager sm);
		public void serverSuspended(ServerManager sm);
		public void serverResumed(ServerManager sm);
	}
	
	public class Adaptor implements Listener {
		public void serverAdded(ServerManager sm) {}
		public void serverRemoved(ServerManager sm) {}
		public void serverSuspended(ServerManager sm) {}
		public void serverResumed(ServerManager sm) {}
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
}
