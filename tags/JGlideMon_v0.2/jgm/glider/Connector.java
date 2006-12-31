package jgm.glider;

import jgm.cfg;

import java.util.*;

public class Connector extends Thread {
	public boolean stop = false;
	
	public static Connector instance;
	
	private static Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public Connector() {
		super("Connector");
		instance = this;
	}
	
	public static boolean isConnected() {
		if (listeners.size() > 0) {
			return listeners.get(0).getConn().isConnected();
		}
		
		return false;
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
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						c.getConn().connect();
					} catch (Exception e) {
						e.printStackTrace();						
						success = false;
						break;
					}
				}
				
				if (success) {
					start(); // start auto-reconnector
					notifyConnectionEstablished();
				}
			}
		}, "Connector.connect");
		
		t.start();
	}
	
	public void disconnect() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				boolean success = true;
				
				for (ConnectionListener c : listeners) {
					try {
						c.getConn().close();
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
						break;
					}
				}
				
				if (success) {
					//notifyConnectionDied();
				}
			}
		}, "Connector.disconnect");
		
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
}
