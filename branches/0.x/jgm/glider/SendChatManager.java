/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 - 2008 Tim
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

import java.util.*;
import java.io.*;
import java.util.logging.*;

import jgm.*;

/**
 * This class will take care of sending chat
 * to Glider. It's main purpose is to create
 * a background thread for sending so that it
 * won't freeze the GUI.
 * 
 * @author Tim
 * @since 0.13
 */
public class SendChatManager implements Runnable {
	static final String SLASH_CHECK = "#13#/";
	
	static Logger log = Logger.getLogger(SendChatManager.class.getName());
	
	private Conn conn;
	private Queue<String> messages =
		new LinkedList<String>();
	
	public volatile boolean stop = false;
	public volatile boolean idle = false;
	private Thread thread = null;
	
	public ServerManager sm;
	
	private static int instances = 0;
	
	public SendChatManager(ServerManager sm) {		
		instances++;
		this.sm = sm;
		thread = new Thread(this);
		thread.setName("SendChatManager" + instances);
		thread.setDaemon(true);
		thread.start();
	}
	
	public void add(String keys) {
		messages.add(keys);
		
		if (idle)
			thread.interrupt();
	}
	
	private boolean sendKeys(String keys) {
		if (null == keys)
			throw new NullPointerException("keys cannot be null in sendKeys()");
		
		if (!sm.connector.isConnected()) return false;
		
		if (conn == null) conn = sm.keysConn;
		
		try {			
			log.info("Sending keys: " + keys);
			conn.send("/stopglide");
			String test = conn.readLine(); // attempting stop
			conn.readLine(); // ---
			
			// if it's a slash command, try to send the keys
			// one at a time with a delay to try to account
			// for glider's bug with not always sending
			// the keys properly
			if (keys.startsWith(SLASH_CHECK)) {
				keys = keys.substring(SLASH_CHECK.length());
				String[] parts = keys.split("\\s+", 2);
				
				if (parts.length == 2) {
					keys = parts[1];
				} else {
					keys = null;
				}
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}
				conn.send("/queuekeys #13#");
				conn.readLine(); conn.readLine();
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}			
				
				conn.send("/queuekeys /");
				conn.readLine(); conn.readLine();
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}			
				
				conn.send("/queuekeys " + parts[0] + " ");
				conn.readLine(); conn.readLine();
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}
			}

			if (null != keys) {
				conn.send("/queuekeys " + keys);
				conn.readLine(); // queued keys
				conn.readLine(); // ---
			}
			
			// don't start if we were stopped initially
			if (!test.equals("Already stopped")) {
				conn.send("/startglide");
				conn.readLine(); // attempting start
				conn.readLine(); // ---
			} else {
				log.finer("Already stopped, not starting glide");
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Error sending chat", e);
		}
		
		return true;
	}
	
	public void run() {
    	while (!stop) {
    		if (messages.isEmpty()) {    			
    			try {
    				idle = true;
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				idle = false;
    				Thread.interrupted();
    				continue;
    			}
    		}

			idle = false;
    		
    		String s = null;
    		
    		// send each item in the queue removing it
    		// if successful
    		while (!stop && null != (s = messages.peek())) {
    			if (sendKeys(s))
    				messages.poll();
    		}
    		
    		idle = true;
    	}
	}
}
