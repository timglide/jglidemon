package jgm;

import jgm.glider.*;
import jgm.gui.GUI;
import jgm.gui.updaters.*;

public class JGlideMon implements ConnectionListener {
	public static final String version = "0.2";
	
	public static JGlideMon instance;
	
	public  GliderConn    keysConn;
	private cfg           c;
	public  GUI           gui;
	private StatusUpdater status;
	private LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;

	public Connector connector;
	
	public JGlideMon() {
		instance = this;
		c = new cfg();
		init();
	}
	
	public GliderConn getConn() {
		return keysConn;
	}
	
	public void connectionEstablished() {}
	
	private void init() {
		synchronized (c) {
		if (!c.isSet()) {
			try {
				c.wait();
			} catch (InterruptedException e) {}
		}
		}
		
	  	connector = new Connector();
		gui = new GUI(this);

		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				Sound.init();
				keysConn   = new GliderConn();
				Connector.addListener(JGlideMon.this);
				logUpdater = new LogUpdater(gui.tabsPane);
				Connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(gui.tabsPane.screenshotTab);
				Connector.addListener(ssUpdater);
				status     = new StatusUpdater();
				Connector.addListener(status);
				status.addObserver(gui);
				
				connector.connect();
			}
		};

		new Thread(r, "JGlideMon.Init").start();
	}

	public void destroy() {
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				logUpdater.close();
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				status.close();
			}
		});
		
		Thread t3 = new Thread(new Runnable() {
			public void run() {
				ssUpdater.close();
			}
		});
		
		Thread t4 = new Thread(new Runnable() {
			public void run() {
				keysConn.close();
			}
		});
			
		t1.start(); t2.start(); t3.start(); t4.start();
		
		cfg.writeIni();
		connector.stop = true;
		connector.interrupt();
		
		while (t1.isAlive() || t2.isAlive() || 
			   t3.isAlive() || t4.isAlive()) {}
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
