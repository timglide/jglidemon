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
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
	}
	
	public volatile State state = State.DISCONNECTED;
	
	ServerManager sm;
	Thread reconnector;
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
			reconnectTries = sm.cfg.getInt("net.autoreconnecttries");
		
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
						log.warning("Error connecting to " + c.getConn().host + ": " + e.getMessage());
						jgm.GUI.setStatusBarText("Unable to connect to " + c.getConn().host + ":" + c.getConn().port + " - Unknown host \"" + e.getMessage() + "\"", true, true);						
						success = false;
						break;
					} catch (Exception e) {
						log.warning("Error connecting to " + c.getConn().host + ": " + e.getMessage());
						jgm.GUI.setStatusBarText("Unable to connect to " + c.getConn().host + ":" + c.getConn().port + " - " + e.getMessage(), true, true);						
						success = false;
						break;
					}
				}
				
				state = (success) ? State.CONNECTED : State.DISCONNECTED;
				
				if (success) {
					reconnectTries = sm.cfg.getInt("net.autoreconnecttries");

					notifyConnectionEstablished();
					new Phrase(Audible.Type.STATUS, "Connection established.").play();
				} else {
					notifyConnectionDied();
					
					if (sm.cfg.getBool("net.autoReconnect")) {
						createReconnector();
					}
				}
			}
		}, "Connector.connect");
		
		log.fine("Attempting to connect...");
		t.start();
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
					
					if (!interactive && sm.cfg.getBool("net.autoreconnect")) {
						createReconnector();
					}
				}
			}
		}, "Connector.disconnect");
		
		log.fine("Attempting to disconnect...");
		t.start();
		
		return t;
	}
	
	public void addListener(ConnectionListener cl) {
		listeners.add(cl);
	}
	
	public void removeListener(ConnectionListener cl) {
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
	
	private void createReconnector() {
		final int delay = sm.cfg.getInt("net.autoreconnectdelay");
		
		if (reconnectTries == Integer.MIN_VALUE) {
			reconnectTries = sm.cfg.getInt("net.autoreconnecttries");
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
				Connector.this.connect();
			}
		}, "AutoReconnector");
		reconnector.start();
	}
	
	public void cancelReconnect() {
		if (reconnector == null) return;
		
		reconnector.interrupt();
		reconnector = null;
	}
}
