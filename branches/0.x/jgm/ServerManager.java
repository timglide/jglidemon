package jgm;

import java.util.*;

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
	static public Config cfg = Config.c;
	static Properties DEFAULTS = new Properties();
	
	static {
		final String NEEDLE = "servers.0.";			
		for (Object key : Config.DEFAULTS.keySet()) {
			String s = key.toString();
			
			if (s.startsWith(NEEDLE)) {
				DEFAULTS.setProperty(s.substring(NEEDLE.length()), Config.DEFAULTS.getProperty(s));
			}
		}
	}
	
	public static List<ServerManager> managers =
		new Vector<ServerManager>();
	
	public static void loadServers() {
		for (int i = 0; ; i++) {
			if (cfg.hasProp("servers." + i + ".name")) {
				Properties p = new Properties(DEFAULTS);
				p.setProperty("name", cfg.get("servers." + i + ".name"));
				p.setProperty("net.host", cfg.get("servers." + i + ".net.host"));
				p.setProperty("net.port", cfg.get("servers." + i + ".net.port"));
				p.setProperty("net.password", cfg.get("servers." + i + ".net.password"));
				
				managers.add(new ServerManager(p));
			} else
				break;
		}
	}
	
	public static void saveConfig() {
		Iterator<Object> it = cfg.props.keySet().iterator();
		
		while (it.hasNext()) {
			String s = it.next().toString();
			
			if (s.startsWith("servers.")) {
				it.remove();
			}
		}
		
		for (int i = 0; i < managers.size(); i++) {
			ServerManager sm = managers.get(i);
			
			sm.props.setProperty("name", sm.name);
			sm.props.setProperty("net.host", sm.host);
			sm.props.setProperty("net.port", Integer.toString(sm.port));
			sm.props.setProperty("net.password", sm.password);
			
			for (Object key : sm.props.keySet()) {
				String s = key.toString();
				cfg.set("servers." + i + "." + s, sm.props.getProperty(s));
			}
		}
	}
	
	public static void addServer() {
		ServerManager sm = new ServerManager();
		managers.add(sm);
		sm.firstRun = true;
		sm.init();
	}
	
	public static void removeServer(ServerManager sm) {
		Thread t = sm.connector.disconnect();
		
		if (t != null)
			try {
				t.join();
			} catch (InterruptedException e) {}
			
		managers.remove(sm);
		sm.gui.frame.dispose();
	}
	
	public boolean       firstRun = false;
	
	public Conn          keysConn;
	public GUI           gui;
	public StatusUpdater status;
	public LogUpdater    logUpdater;
	public SSUpdater     ssUpdater;
	public PlayerChartUpdater chartUpdater;
	
	public Connector connector;

	public Properties props;
	public String name;
	public String host;
	public int port;
	public String password;
	
	public ServerManager() {
		this(null);
	}
	
	private ServerManager(Properties p) {		
		if (p == null) {			
			p = new Properties(DEFAULTS);
			p.setProperty("name", p.getProperty("name") + " " + managers.size());
		}
		
		this.props = p;
		this.name = p.getProperty("name");
		this.host = p.getProperty("net.host");
		this.port = Integer.parseInt(p.getProperty("net.port"));
		this.password = p.getProperty("net.password");
		
		connector = new Connector(this);
	}
	
	public boolean isCurrent() {
		return JGlideMon.getCurManager() == this;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String toFullString() {
		return name + " - " + host + ":" + port;
	}
	
	public void init() {
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
	}
}
