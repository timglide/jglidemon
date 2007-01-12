package jgm;

import javax.swing.JOptionPane;

import jgm.glider.*;
import jgm.gui.GUI;
import jgm.gui.updaters.*;
import jgm.util.*;

/**
 * The main program.
 * @author Tim
 * @since 0.1
 */
public class JGlideMon implements ConnectionListener {
	public static final String version = "0.2 dev";
	
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
	public void connectionDied() {}
	
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

		if (!jgm.cfg.iniFileExists() || jgm.cfg.net.host.equals("")) {
			gui.tabsPane.tabbedPane.setSelectedComponent(gui.tabsPane.config);
			
			JOptionPane.showMessageDialog(gui.frame,
				"Please enter the server name, port, and password.\n" +
				"Next, click Save Settings, then click Connect.\n\n" +
				"Remember to click Save Settings any time you change a setting.",
				"Configuration Required",
				JOptionPane.INFORMATION_MESSAGE);
		}
		
		// create a seperate thread to connect in case it
		// takes a while to connect it won't slow the gui
		Runnable r = new Runnable() {
			public void run() {
				Sound.init();
				Speech.init();
				keysConn   = new GliderConn();
				Connector.addListener(JGlideMon.this);
				logUpdater = new LogUpdater(gui.tabsPane);
				Connector.addListener(logUpdater);
				ssUpdater  = new SSUpdater(gui.tabsPane.screenshotTab);
				Connector.addListener(ssUpdater);
				status     = new StatusUpdater();
				Connector.addListener(status);
				status.addObserver(gui);
				status.addObserver(ssUpdater);
				
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
		Speech.destroy();
		
		cfg.writeIni();
		connector.stop = true;
		connector.interrupt();
		
		while (t1.isAlive() || t2.isAlive() || 
			   t3.isAlive() || t4.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
		JGlideMon jgm = new JGlideMon();
		
		while (!Speech.ready()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		jgm.gui.tabsPane.urgentChatLog.add(new jgm.glider.log.WhisperEntry("[Test] whispers: Test whisper", "[Test] whispers: Test whisper", "Test", 1, "Whisper"));
	}
}
