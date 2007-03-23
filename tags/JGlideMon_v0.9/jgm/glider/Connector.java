package jgm.glider;

import jgm.*;
import jgm.sound.*;

import java.util.*;
import java.util.logging.*;

public class Connector {
	static Logger log = Logger.getLogger(Connector.class.getName());
	
	public enum State {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
	}
		
	private static Connector instance;
	private static Config cfg;
	
	private static Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public static volatile State state = State.DISCONNECTED;
	
	public Connector() {
		instance = this;
		cfg = jgm.Config.getInstance();
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
				
				notifyConnecting();
				
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						if (c.getConn() == null) continue;
						c.getConn().connect();
					} catch (java.net.UnknownHostException e) {
						log.warning("Error connecting to " + cfg.get("net", "host") + ": " + e.getMessage());
						jgm.GUI.setStatusBarText("Unable to connect to " + cfg.get("net", "host") + ":" + cfg.get("net", "port") + " - Unknown host \"" + e.getMessage() + "\"", true, true);						
						success = false;
						break;
					} catch (Exception e) {
						log.warning("Error connecting to " + cfg.getString("net", "host") + ": " + e.getMessage());
						jgm.GUI.setStatusBarText("Unable to connect to " + cfg.get("net", "host") + ":" + cfg.get("net", "port") + " - " + e.getMessage(), true, true);						
						success = false;
						break;
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				
				if (success) {
					reconnectTries = cfg.getInt("net", "autoreconnecttries");

					notifyConnectionEstablished();
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
				} else {
					notifyConnectionDied();
					
					if (cfg.getBool("net" , "autoReconnect")) {
						createReconnector();
					}
				}
			}
		}, "Connector.connect");
		
		log.fine("Attempting to connect...");
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
				notifyDisconnecting();

				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						if (c.getConn() == null) continue;
						c.getConn().close();
					} catch (Throwable e) {
						log.log(Level.WARNING, "Error closing a connection", e);
						//System.err.println("Error closing a connection: " + e.getClass().getName() + ":" + e.getMessage());
						//success = false;
						//break;
					}
				}
				
				state = State.DISCONNECTED;
				jgm.GUI.setStatusBarText("Disconnected", false, true);
				jgm.GUI.hideStatusBarProgress();
				
				if (success) {
					notifyConnectionDied();
					new Phrase(Audible.Type.STATUS, "Disconnected from server.").play();
					
					if (!interactive && cfg.getBool("net" , "autoReconnect")) {
						createReconnector();
					}
				}
			}
		}, "Connector.disconnect");
		
		log.fine("Attempting to disconnect...");
		t.start();
		
		return t;
	}
	
	public static void addListener(ConnectionListener cl) {
		listeners.add(cl);
	}
	
	public static void removeListener(ConnectionListener cl) {
		listeners.remove(cl);
	}
	
	private void notifyConnecting() {
		log.finer("Notifying of connecting");
		for (ConnectionListener c : listeners) {
			c.connecting();
		}
	}
	
	private void notifyConnectionEstablished() {
		log.finer("Notifying of connection established");
		for (ConnectionListener c : listeners) {
			c.connectionEstablished();
		}
	}
	
	private void notifyDisconnecting() {
		log.finer("Notifying of disconnecting");
		for (ConnectionListener c : listeners) {
			c.disconnecting();
		}
	}
	
	private void notifyConnectionDied() {
		log.finer("Notifying of connection died");
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
					log.fine("Reconnecting in " + i);
					while (i > 0) {
						jgm.GUI.setStatusBarText("Reconnecting in " + i + "...", false, true);
						Thread.sleep(1000);
						i--;
					}
				} catch (InterruptedException e) {
					log.fine("Cancelling auto-reconnect.");
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
