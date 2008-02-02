package jgm.glider;

import java.util.*;
import java.io.*;
import java.util.logging.*;

import jgm.JGlideMon;

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
	static Logger log = Logger.getLogger(SendChatManager.class.getName());
	
	private Conn conn;
	private Queue<String> messages =
		new LinkedList<String>();
	
	public volatile boolean stop = false;
	public volatile boolean idle = false;
	private Thread thread = null;
	
	private static int instances = 0;
	
	public SendChatManager() {		
		instances++;
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
		if (!Connector.isConnected()) return false;
		
		if (conn == null) conn = JGlideMon.instance.keysConn;
		
		try {
			log.info("Sending keys: " + keys);
			conn.send("/stopglide");
			String test = conn.readLine(); // attempting stop
			conn.readLine(); // ---
			conn.send("/forcekeys " + keys);
			conn.readLine(); // queued keys
			conn.readLine(); // ---
			
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
