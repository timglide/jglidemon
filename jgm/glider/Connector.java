package jgm.glider;

import jgm.cfg;
import jgm.sound.*;

import java.util.*;

public class Connector extends Thread {
	public enum State {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
	}
	
	public volatile boolean stop = false;
	
	public static Connector instance;
	
	private static Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public static volatile State state = State.DISCONNECTED;
	
	public Connector() {
		super("Connector");
		instance = this;
	}
	
	public static boolean isConnected() {
		return state == State.CONNECTED;
		/*if (listeners.size() > 0) {
			return listeners.get(0).getConn().isConnected();
		}
		
		return false;*/
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
	}
	
	public void connect() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				state = State.CONNECTING;
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						c.getConn().connect();
					} catch (Exception e) {
						System.err.println("Error connecting to " + cfg.net.host + ": " + e.getMessage());						
						success = false;
						break;
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				
				if (success) {
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
					//Connector.this.start(); // start auto-reconnector
					notifyConnectionEstablished();
				} else {
					notifyConnectionDied();
				}
			}
		}, "Connector.connect");
		
		System.out.println("Attempting to connect...");
		t.start();
	}
	
	public void disconnect() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				state = State.DISCONNECTING;
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
				
				if (success) {
					new Phrase(Audible.Type.STATUS, "Disconnected from server.").play();
					System.out.println("Notifying of disconnect");
					notifyConnectionDied();
				}
			}
		}, "Connector.disconnect");
		
		System.out.println("Attempting to disconnect...");
		t.start();
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
