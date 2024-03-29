/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
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
package jgm.glider;

import jgm.*;
import jgm.sound.*;

import java.util.*;
import java.util.logging.*;

public class Connector {
	static Logger log = Logger.getLogger(Connector.class.getName());
	
	public static enum State {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING, DEAD
	}
	
	public volatile State state = State.DISCONNECTED;
	
	ServerManager sm;
	Thread connect = null;
	Thread disconnect = null;
	Thread reconnector = null;
	
	volatile boolean interactiveDisconnect = false;
	
	int reconnectTries = Integer.MIN_VALUE;
	
	Vector<ConnectionListener> listeners
		= new Vector<ConnectionListener>();
	
	public Connector(ServerManager sm) {
		this.sm = sm;
	}
	
	public boolean isConnected() {
		return state == State.CONNECTED;
	}
	
	public void connect() {
		connect(false);
	}
	
	/**
	 * @param interactive True if the user pressed the connect button
	 */
	public void connect(boolean interactive) {
		connectImpl(interactive);
	}
	
	private void connectImpl(final boolean interactive) {
		if (state != State.DISCONNECTED) return;
		
		if (interactive)
			reconnectTries = Config.c.getInt("net.autoreconnecttries");
		
		state = State.CONNECTING;
		
		connect = new Thread(new Runnable() {
			public void run() {
				if (interactive) cancelReconnect();
				
				fireConnecting();
				
				boolean success = true;
				
				synchronized (listeners) {
					for (ConnectionListener c : listeners) {
						try {
							if (c.getConn() == null) continue;
							c.getConn().connect();
						} catch (java.net.UnknownHostException e) {
							log.warning("Error connecting to " + c.getConn().sm.host + ": " + e.getMessage());
							
							if (null != sm.gui)
								sm.gui.setStatusBarText("Unable to connect to " + c.getConn().sm.host + ":" + c.getConn().sm.port + " - Unknown host \"" + e.getMessage() + "\"", true, true);
							
							success = false;
							break;
						} catch (java.io.IOException e) {
							log.warning("Error connecting to " + c.getConn().sm.host + ": " + e.getMessage());
							
							if (null != sm.gui)
								sm.gui.setStatusBarText("Unable to connect to " + c.getConn().sm.host + ":" + c.getConn().sm.port + " - " + e.getMessage(), true, true);
							
							success = false;
							break;
						}
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				
				if (success) {
					reconnectTries = Config.c.getInt("net.autoreconnecttries");

					fireConnect();
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
				} else {
					fireDisconnect();
					
					if (Config.c.getBool("net.autoreconnect")) {
						createReconnector();
					}
				}
				
				connect = null;
			}
		}, sm.get("name") + ":Connector.connect");
		connect.setDaemon(true);
		
		log.fine("Attempting to connect...");
		connect.start();
	}

	public Thread disconnect() {
		return disconnect(false);
	}
	
	/**
	 * @param forced True if the user pressed the Disconnect button
	 * @return
	 */
	public Thread disconnect(boolean interactive) {		
		return disconnectImpl(interactive);
	}
	
	public void someoneDisconnected() {
		disconnect();
	}
	
	private Thread disconnectImpl(final boolean interactive) {		
		if (state != State.CONNECTED) return null;
		
		cancelReconnect();
		
		state = State.DISCONNECTING;
		
		disconnect = new Thread(new Runnable() {
			public void run() {
				fireDisconnecting();

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
				
				if (null != sm.gui) {
					sm.gui.setStatusBarText("Disconnected", false, true);
					sm.gui.hideStatusBarProgress();
				}
				
				if (success) {
					fireDisconnect();
					new Phrase(Audible.Type.STATUS, "Disconnected from server.").play();
					
					if (!interactive && Config.c.getBool("net.autoreconnect")) {
						log.finer("Creating Reconnector after disconnecting");
						createReconnector();
					}
				}
				
				interactiveDisconnect = false;
				disconnect = null;
			}
		}, sm.get("name") + ":Connector.disconnect");
		disconnect.setDaemon(true);
		
		log.fine("Attempting to disconnect...");
		disconnect.start();
		
		return disconnect;
	}
	
	public void addListener(ConnectionListener cl) {
		listeners.add(cl);
	}
	
	public void removeListener(ConnectionListener cl) {
		listeners.remove(cl);
	}
	
	private void fireConnecting() {
		synchronized (listeners) {
			log.finest("Firing connecting");
			for (ConnectionListener c : listeners) {
				c.onConnecting();
			}
		}
	}
	
	private void fireConnect() {
		synchronized (listeners) {
			log.finest("Fire connect");
			for (ConnectionListener c : listeners) {
				c.onConnect();
			}
		}
	}
	
	private void fireDisconnecting() {
		synchronized (listeners) {
			log.finest("Fire disconnecting");
			for (ConnectionListener c : listeners) {
				c.onDisconnecting();
			}
		}
	}
	
	private void fireDisconnect() {
		synchronized (listeners) {
			log.finest("Fire disconnect");
			for (ConnectionListener c : listeners) {
				c.onDisconnect();
			}
		}
	}
	
	private void createReconnector() {
		final int delay = Config.c.getInt("net.autoreconnectdelay");
		
		if (reconnectTries == Integer.MIN_VALUE) {
			reconnectTries = Config.c.getInt("net.autoreconnecttries");
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
						if (null != sm.gui)
							sm.gui.setStatusBarText("Reconnecting in " + i + "...", false, true);
						Thread.sleep(1000);
						i--;
					}
					
					Connector.this.connect();
				} catch (InterruptedException e) {
					log.fine("Cancelling auto-reconnect.");
					return;
				}
			}
		}, sm.get("name") + ":AutoReconnector");
		reconnector.setDaemon(true);
		reconnector.start();
	}
	
	public void cancelReconnect() {
		if (reconnector == null) return;
		
		reconnector.interrupt();
		reconnector = null;
	}
	
	public void destroy() {
		synchronized (state) {
			state = State.DEAD;
			
			cancelReconnect();
	
			if (connect != null) {
				connect.interrupt();
			}
			
			if (isConnected()) {
				try {
					Thread t = disconnect(true);
					t.join();
				} catch (InterruptedException e) {}
			}
			
			state = State.DEAD;
			
			synchronized (listeners) {
				log.finest("Fire destroy");
				for (ConnectionListener c : listeners) {
					c.onDestroy();
				}
			}
		}
	}
}
