package jgm.glider;

import jgm.cfg;
import jgm.sound.*;

import java.util.*;

public class Connector extends Thread {
	public enum State {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
	}
	
	public volatile boolean stop = false;
	
	private static Connector instance;
	
	private static Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public static volatile State state = State.DISCONNECTED;
	
	public Connector() {
		super("Connector");
		instance = this;
	}
	
	public static boolean isConnected() {
		return state == State.CONNECTED;
	}
	
	public void run() {
		int ticks = 0;
		
		while (!stop && cfg.net.autoReconnect) {
			System.out.println("Connector tick " + (++ticks));
			
			for (final ConnectionListener c : listeners) {
				final GliderConn conn = c.getConn();
				
				if (!conn.isConnected()) {
					System.out.println("Reconecting conn " + conn.hashCode());
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								conn.connect();
								c.connectionEstablished();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, "Reconnector" + c.hashCode());
					t.start();
				}
			}
			
			try {
				sleep(30000);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		System.out.println("Auto-reconnector terminating");
	}
	
	public static void connect() {
		if (instance == null) return;
		
		instance.connectImpl();
	}
	
	private void connectImpl() {
		if (state != State.DISCONNECTED) return;
		
		state = State.CONNECTING;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				jgm.gui.GUI.setStatusBarText("Connecting...", false, true);
				jgm.gui.GUI.setStatusBarProgressIndeterminent();
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						c.getConn().connect();
					} catch (java.net.UnknownHostException e) {
						System.err.println("Error connecting to " + cfg.net.host + ": " + e.getMessage());
						jgm.gui.GUI.setStatusBarText("Unable to connect to " + cfg.net.host + ":" + cfg.net.port + " - Unknown host \"" + e.getMessage() + "\"", true, true);						
						success = false;
						break;
					} catch (Exception e) {
						System.err.println("Error connecting to " + cfg.net.host + ": " + e.getMessage());
						jgm.gui.GUI.setStatusBarText("Unable to connect to " + cfg.net.host + ":" + cfg.net.port + " - " + e.getMessage(), true, true);						
						success = false;
						break;
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				jgm.gui.GUI.hideStatusBarProgress();
				
				if (success) {
					jgm.gui.GUI.setStatusBarText("Connected", false, true);
					jgm.gui.GUI.setTitle(cfg.net.host + ':' + cfg.net.port);
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
					
					if (cfg.net.autoReconnect)
						Connector.this.start(); // start auto-reconnector
					notifyConnectionEstablished();
				} else {
					jgm.gui.GUI.setTitle();
					notifyConnectionDied();
				}
			}
		}, "Connector.connect");
		
		System.out.println("Attempting to connect...");
		t.start();
	}
	
	public static Thread disconnect() {
		if (instance == null) return null;
		
		return instance.disconnectImpl();
	}
	
	private Thread disconnectImpl() {
		if (state != State.CONNECTED) return null;
		
		state = State.DISCONNECTING;
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				runImpl();
			}
			
			private synchronized void runImpl() {
				jgm.gui.GUI.setStatusBarText("Disconnecting...", false, true);
				jgm.gui.GUI.setStatusBarProgressIndeterminent();
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
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
}
