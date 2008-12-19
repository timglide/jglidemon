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
public class CommandManager implements Runnable {
	static final String SLASH_CHECK = "#13#/";
	
	static Logger log = Logger.getLogger(CommandManager.class.getName());
	
	private Conn conn;
	private Queue<Command> commands =
		new LinkedList<Command>();
	
	public volatile boolean stop = false;
	public volatile boolean idle = false;
	private Thread thread = null;
	
	public ServerManager sm;
	
	private static int instances = 0;
	
	public CommandManager(ServerManager sm) {		
		instances++;
		this.sm = sm;
		thread = new Thread(this, sm.name + ":CommandManager");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void destroy() {
		stop = true;
		thread.interrupt();
	}
	
	public void add(Command cmd) {
		commands.add(cmd);
		
		if (idle)
			thread.interrupt();
	}
	
	private void sendKeys(String keys) {
		if (null == keys)
			throw new NullPointerException("keys cannot be null in sendKeys()");
		
		if (!sm.connector.isConnected())
			commands.clear();
		
		if (conn == null) conn = sm.keysConn;
		
		try {			
			log.info("Sending keys: " + keys);
			String test = 
				Command.getStopCommand().getResult(conn);
			
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
				
				Command.getChatCommand("#13#").getResult(conn);
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}			
				
				Command.getChatCommand("/").getResult(conn);
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}			
				
				Command.getChatCommand(parts[0] + " ").getResult(conn);
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}
			}

			if (null != keys) {
				Command.getChatCommand(keys).getResult(conn);
			}
			
			// don't start if we were stopped initially
			if (!test.equals("Already stopped")) {
				Command.getStartCommand().getResult(conn);
				
				String s = Config.c.get("restarter.onrestart");
				
				if (!s.equals("nothing")) {
					try {
						Command.getSetGameWSCommand(s).getResult(conn);
					} catch (IllegalArgumentException e) {}
				}
			} else {
				log.finer("Already stopped, not starting glide");
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Error sending chat", e);
		}
	}
	
	private void sendCommand(Command c) {
		if (!sm.connector.isConnected()) {
			commands.clear();
		}
		
		if (conn == null) conn = sm.keysConn;
		
		try {
			c.getResult(conn);
		} catch (java.io.IOException e) {
			log.log(Level.WARNING, "Error sending command", e);
		}
	}
	
	public void run() {
    	while (!stop) {
    		if (commands.isEmpty()) {    			
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
    		
    		Command c = null;
    		
    		// send each item in the queue removing it
    		// if successful
    		while (!stop && null != (c = commands.peek())) {
    			if (c.slash.equals("queuekeys")) {
    				sendKeys(c.args);
    				commands.poll();
    				continue;
    			}
    			
    			sendCommand(c);
    			commands.poll();
    		}
    		
    		idle = true;
    	}
	}
}
