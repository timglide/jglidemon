package jgm.glider;

import jgm.cfg;
import jgm.sound.*;

import java.util.*;

public class Connector {
	public enum State {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
	}
		
	private static Connector instance;
	private static cfg cfg;
	
	private static Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public static volatile State state = State.DISCONNECTED;
	
	public Connector() {
		instance = this;
		cfg = jgm.cfg.getInstance();
	}
	
	public static boolean isConnected() {
		return state == State.CONNECTED;
	}
	
	public static void connect() {
		connect(false);
	}
	
	/**
	 * @param interactive True if the user pressed the connect button
	 */
	public static void connect(boolean interactive) {
		if (instance == null) return;
		
		instance.connectImpl(interactive);
	}
	
	private void connectImpl(final boolean interactive) {
		if (state != State.DISCONNECTED) return;
		
		if (interactive)
			reconnectTries = cfg.getInt("net", "autoreconnecttries");
		
		state = State.CONNECTING;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				if (interactive) cancelReconnect();
				
				jgm.gui.GUI.instance.ctrlPane.connecting();
				
				jgm.gui.GUI.setStatusBarText("Connecting...", false, true);
				jgm.gui.GUI.setStatusBarProgressIndeterminent();
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						if (c.getConn() == null) continue;
						c.getConn().connect();
					} catch (java.net.UnknownHostException e) {
						System.err.println("Error connecting to " + cfg.get("net", "host") + ": " + e.getMessage());
						jgm.gui.GUI.setStatusBarText("Unable to connect to " + cfg.get("net", "host") + ":" + cfg.get("net", "port") + " - Unknown host \"" + e.getMessage() + "\"", true, true);						
						success = false;
						break;
					} catch (Exception e) {
						System.err.println("Error connecting to " + cfg.getString("net", "host") + ": " + e.getMessage());
						jgm.gui.GUI.setStatusBarText("Unable to connect to " + cfg.get("net", "host") + ":" + cfg.get("net", "port") + " - " + e.getMessage(), true, true);						
						success = false;
						break;
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				jgm.gui.GUI.hideStatusBarProgress();
				
				if (success) {
					reconnectTries = cfg.getInt("net", "autoreconnecttries");
					
					jgm.gui.GUI.setStatusBarText("Connected", false, true);
					jgm.gui.GUI.setTitle(cfg.get("net", "host") + ":" + cfg.get("net", "port"));
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
					notifyConnectionEstablished();
				} else {
					jgm.gui.GUI.setTitle();
					notifyConnectionDied();
					
					if (cfg.getBool("net" , "autoReconnect")) {
						createReconnector();
					}
				}
			}
		}, "Connector.connect");
		
		System.out.println("Attempting to connect...");
		t.start();
	}

	public static Thread disconnect() {
		return disconnect(false);
	}
	
	/**
	 * @param forced True if the user pressed the Disconnect button
	 * @return
	 */
	public static Thread disconnect(boolean interactive) {
		if (instance == null) return null;
		
		return instance.disconnectImpl(interactive);
	}
	
	private Thread disconnectImpl(final boolean interactive) {
		if (state != State.CONNECTED) return null;
		
		state = State.DISCONNECTING;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				runImpl();
			}
			
			private synchronized void runImpl() {
				jgm.gui.GUI.instance.ctrlPane.disconnecting();
				
				jgm.gui.GUI.setStatusBarText("Disconnecting...", false, true);
				jgm.gui.GUI.setStatusBarProgressIndeterminent();
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						if (c.getConn() == null) continue;
						c.getConn().close();
					} catch (Exception e) {
						System.err.println("Error closing a connection: " + e.getMessage());
						//success = false;
						//break;
					}
				}
				
				state = State.DISCONNECTED;
				jgm.gui.GUI.setStatusBarText("Disconnected", false, true);
				jgm.gui.GUI.hideStatusBarProgress();
				
				if (success) {
					System.out.println("Notifying of disconnect");
					notifyConnectionDied();
					new Phrase(Audible.Type.STATUS, "Disconnected from server.").play();
					
					if (!interactive && cfg.getBool("net" , "autoReconnect")) {
						createReconnector();
					}
				}

				jgm.gui.GUI.setTitle();
				notifyAll();
			}
		}, "Connector.disconnect");
		
		System.out.println("Attempting to disconnect...");
		t.start();
		
		return t;
	}
	
	public static void addListener(ConnectionListener cl) {
		listeners.add(cl);
	}
	
	public static void removeListener(ConnectionListener cl) {
		listeners.remove(cl);
	}
	
	private void notifyConnectionEstablished() {
		for (ConnectionListener c : listeners) {
			c.connectionEstablished();
		}
	}
	
	private void notifyConnectionDied() {
		for (ConnectionListener c : listeners) {
			c.connectionDied();
		}
	}
	
	private static Thread reconnector;
	private static int reconnectTries = Integer.MIN_VALUE;
	
	private static void createReconnector() {
		final int delay = cfg.getInt("net", "autoreconnectdelay");
		
		if (reconnectTries == Integer.MIN_VALUE) {
			reconnectTries = cfg.getInt("net", "autoreconnecttries");
		}
		
		reconnectTries--;
		
		if (reconnectTries < 0) {
			reconnector = null;
			return;
		}
		
		reconnector = new Thread(new Runnable() {
			public void run() {
				int i = delay;
				try {
					System.out.println("Reconnecting in " + i);
					while (i > 0) {
						jgm.gui.GUI.setStatusBarText("Reconnecting in " + i + "...", false, true);
						Thread.sleep(1000);
						i--;
					}
				} catch (InterruptedException e) {
					System.out.println("Cancelling auto-reconnect.");
					return;
				}
				Connector.connect();
			}
		}, "AutoReconnector");
		reconnector.start();
	}
	
	public static void cancelReconnect() {
		if (reconnector == null) return;
		
		reconnector.interrupt();
		reconnector = null;
	}
}