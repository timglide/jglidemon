package jgm;

import jgm.util.*;

public class cfg extends Thread {
	private boolean set = false;
	private static QuickIni ini = new QuickIni("JGlideMon.ini");
	private static int instances = 0;
	
	public cfg() {
		if (++instances > 1) {
			System.err.println("Too many cfg instances:");
			
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		start();
	}
	
	public boolean isSet() {
		return set;
	}
	
	public void run() {
		readIni();
		set = true;
	}
	
	public synchronized void readIni() {
		net.host = ini.getStringProperty("network", "host", "localhost");
		net.port = ini.getIntegerProperty("network", "port", 1234);
		net.password = ini.getStringProperty("network", "password", "");
		net.autoReconnect = ini.getBooleanProperty("network", "autoreconnect", true);
		
		status.updateInterval = ini.getIntegerProperty("status", "updateInterval", 500);
		
		screenshot.autoUpdate = ini.getBooleanProperty("screenshot", "autoupdate", true);
		screenshot.updateInterval = ini.getIntegerProperty("screenshot", "updateinterval", 5000);
		screenshot.scale = ini.getIntegerProperty("screenshot", "scale", 100);
		
		if (screenshot.scale > 100) screenshot.scale = 100; else
		if (screenshot.scale < 10)  screenshot.scale = 10;
		
		window.x = ini.getIntegerProperty("window", "x", 50);
		window.y = ini.getIntegerProperty("window", "y", 50);
		window.maximized = ini.getBooleanProperty("window", "maximized", true);
		window.width = ini.getIntegerProperty("window", "width", 1000);
		window.height = ini.getIntegerProperty("window", "height", 700);
		
		notifyAll();
	}
	
	public static void writeIni() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				ini.setStringProperty("network", "host", net.host);
				ini.setIntegerProperty("network", "port", net.port);
				ini.setStringProperty("network", "password", net.password);
				ini.setBooleanProperty("network", "autoreconnect", net.autoReconnect);
		
				ini.setIntegerProperty("status", "updateinterval", status.updateInterval);
		
				ini.setBooleanProperty("screenshot", "autoupdate", screenshot.autoUpdate);
				ini.setIntegerProperty("screenshot", "updateinterval", screenshot.updateInterval);
				ini.setIntegerProperty("screenshot", "scale", screenshot.scale);
		
				ini.setIntegerProperty("window", "x", window.x);
				ini.setIntegerProperty("window", "y", window.y);
				ini.setBooleanProperty("window", "maximized", window.maximized);
				ini.setIntegerProperty("window", "width", window.width);
				ini.setIntegerProperty("window", "height", window.height);				
				
				ini.updateFile();
			}
		});
		t.start();
	}
	
	public static class net {
		public static String host;
		public static int    port;
		public static String password;
		public static boolean autoReconnect = true;
	}
	
	public static class status {
		public static int updateInterval = 500;
	}
	
	public static class screenshot {
		public static boolean autoUpdate = true;
		public static int updateInterval = 5000;
		public static int scale = 100;
	}
	
	public static class window {
		public static int x = 50;
		public static int y = 50;
		public static boolean maximized = true;
		public static int width = 1000;
		public static int height = 700;
	}
}
