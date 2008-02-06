package jgm;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import jgm.glider.Conn;
import jgm.glider.ConnectionAdapter;
import jgm.glider.Connector;
import jgm.glider.Status;
import jgm.gui.panes.CharInfoPane;
import jgm.gui.panes.ControlPane;
import jgm.gui.panes.ExperiencePane;
import jgm.gui.panes.MobInfoPane;
import jgm.gui.panes.TabsPane;
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
	public  MyGUI         myGui;
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
	
	public boolean isCurrent() {
		return JGlideMon.getCurManager() == this;
	}
	
	
	public void init(GUI gui) {
		this.mainGui = gui;
		myGui = new MyGUI();
		
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
				logUpdater = new LogUpdater(ServerManager.this, myGui.tabsPane);
				connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(ServerManager.this, myGui.tabsPane.screenshotTab);
				connector.addListener(ssUpdater);
				status     = new StatusUpdater(ServerManager.this);
				connector.addListener(status);
				status.addObserver(myGui);
				status.addObserver(ssUpdater);
				
				chartUpdater = new PlayerChartUpdater(ServerManager.this);
				
				connector.connect();
			}
		};

		Thread t = new Thread(r, "ServerManager.init");
		t.start();
	}
	
	
	public class MyGUI extends JPanel implements java.util.Observer {
		static final int PADDING = GUI.PADDING;
		
		public CharInfoPane   charInfo;
		public MobInfoPane    mobInfo;
		public ControlPane    ctrlPane;
		public ExperiencePane xpPane;
		public TabsPane       tabsPane;
		
		public MyGUI() {
			super(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.0; c.weighty = 0.0;

			charInfo = new CharInfoPane();
			c.gridx = 0; c.gridy = 0; c.weightx = 0.25;
			c.insets.top = PADDING;
			c.insets.left = PADDING;
			add(charInfo, c);

			mobInfo = new MobInfoPane();
			c.gridx = 1; c.gridy = 0; c.weightx = 0.75;
			add(mobInfo, c);

			ctrlPane = new ControlPane();
			ServerManager.this.connector.addListener(ctrlPane);
			c.gridx = 2; c.gridy = 0; c.weightx = 0.0;
			c.insets.right = PADDING;
			add(ctrlPane, c);

			xpPane = new ExperiencePane();
			c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
			c.insets.left = 0;
			c.insets.right = 0;
			add(xpPane, c);

			tabsPane = new TabsPane();
			JPanel tabsPanel = new JPanel(new BorderLayout());
			tabsPanel.add(tabsPane, BorderLayout.CENTER);
			c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0; c.weighty = 1.0;
			add(tabsPanel, c);

			GUI.addKeyAndContainerListenerRecursively(tabsPane.screenshotTab, mainGui, GUI.frame);
			
		}
		
		public void update(java.util.Observable obs, Object o) {
//			System.out.println("GUI.update() called");
			Status s = (Status) o;

			charInfo.update(s);
			mobInfo.update(s);
			ctrlPane.update(s);
			xpPane.update(s);
			tabsPane.update(s);

			tabsPane.chatLog.update(s);
			
			String version = "";
			
			if (!s.version.equals("")) {
				version = "Connected to Glider v" + s.version + " - ";
			}
			
			if (!JGlideMon.getCurManager().connector.isConnected()) {
				String st;
				
				switch (JGlideMon.getCurManager().connector.state) {
					case CONNECTING: st = "Connecting..."; break;
					case DISCONNECTING: st = "Disconnecting..."; break;
					default: st = "Disconnected"; break;
				}
				
				GUI.setStatusBarText(st);
			} else if (s.attached) {
				GUI.setStatusBarText(version + "Attached: " + s.profile);
			} else {
				GUI.setStatusBarText(version + "Not Attached");
			}
		}
	}
}
