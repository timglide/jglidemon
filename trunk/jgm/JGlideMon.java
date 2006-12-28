package jgm;

import jgm.glider.GliderConn;
import jgm.gui.GUI;
import jgm.gui.updaters.*;

public class JGlideMon {
	public static final String version = "0.1 dev";
	
	public static JGlideMon instance;
	
	public  GliderConn    keysConn;
	private cfg           c;
	public  GUI           gui;
	private StatusUpdater status;
	private LogUpdater    logUpdater;
	public  SSUpdater     ssUpdater;

	public JGlideMon() {
		instance = this;
		c = new cfg();
		init();
	}
	
	private void init() {
	  synchronized (c) {
		if (!c.isSet()) {
			try {
				c.wait();
			} catch (InterruptedException e) {}
		}
	  }
		
		gui = new GUI(this);

		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				Sound.init();
				keysConn   = new GliderConn();
				logUpdater = new LogUpdater(gui.tabsPane);
				ssUpdater  = new SSUpdater(gui.tabsPane.screenshotTab);
				status     = new StatusUpdater();
				status.addObserver(gui);
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
		
		while (t1.isAlive() || t2.isAlive() || 
			   t3.isAlive() || t4.isAlive()) {}
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new JGlideMon();
	}
}
