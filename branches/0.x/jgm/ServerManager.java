package jgm;

import javax.swing.JOptionPane;

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
	public  Conn          keysConn;
	public  GUI           mainGui;
	public  StatusUpdater status;
	public  LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;
	public  PlayerChartUpdater chartUpdater;
	
	public Connector connector;
	
	public Config cfg = Config.c;
	
	int serverNum;
	public String host;
	public int port;
	public String password;
	
	/**
	 * 
	 * @param serverNum The server index, will be used when getting config settings
	 */
	public ServerManager(int serverNum) {
		this.serverNum = serverNum;
		
//		if (!(cfg.hasProp("net.servers." + serverNum + ".host") &&
//				cfg.hasProp("net.servers." + serverNum + ".port") &&
//				cfg.hasProp("net.servers." + serverNum + ".password")
//			)) {
//			throw new IllegalArgumentException("No such server: " + serverNum);
//		}
			
		host = cfg.get("net"/*.servers." + serverNum */+ ".host");
		port = cfg.getInt("net"/*.servers." + serverNum */+ ".port");
		password = cfg.get("net"/*.servers." + serverNum */+ ".password");
		
		connector = new Connector(this);
	}
	
	public void init(GUI gui) {
		this.mainGui = gui;
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				if (!jgm.Config.fileExists() || cfg.getString("net.host").equals("")) {
					JOptionPane.showMessageDialog(GUI.frame,
						"Please enter the remote host, port, and password.\n" +
						"Next, click Save Settings, then click Connect.\n\n" +
						"Remember to click Save Settings any time you change a setting.\n" +
						"You may access the configuration screen later via the File menu.",
						"Configuration Required",
						JOptionPane.INFORMATION_MESSAGE);
					
					// select the network tab
					mainGui.showConfig(1);
				}
				
				keysConn = new Conn(ServerManager.this);
				connector.addListener(new ConnectionAdapter() {
					public Conn getConn() {
						return keysConn;
					}
				});
				logUpdater = new LogUpdater(ServerManager.this, mainGui.tabsPane);
				connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(ServerManager.this, mainGui.tabsPane.screenshotTab);
				connector.addListener(ssUpdater);
				status     = new StatusUpdater(ServerManager.this);
				connector.addListener(status);
				status.addObserver(mainGui);
				status.addObserver(ssUpdater);
				
				chartUpdater = new PlayerChartUpdater();
				
				connector.connect();
			}
		};

		Thread t = new Thread(r, "ServerManager.init");
		t.start();
	}
}
