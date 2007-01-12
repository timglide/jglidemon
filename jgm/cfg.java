package jgm;

import jgm.util.*;

import java.io.File;

/**
 * Contains all global configuration values and handles
 * reading and writing to the ini file.
 * @author Tim
 * @since 0.1
 */
public class cfg extends Thread {
	private volatile boolean set = false;
	private static final File iniFile = new File("JGlideMon.ini");
	private static QuickIni ini = new QuickIni(iniFile.getName());
	private static int instances = 0;
	
	public static boolean iniFileExists() {
		return iniFile.exists();
	}
	
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
		net.host = ini.getStringProperty("network", "host", "");
		net.port = ini.getIntegerProperty("network", "port", 0);
		net.password = ini.getStringProperty("network", "password", "");
		net.autoReconnect = ini.getBooleanProperty("network", "autoreconnect", true);
		
		status.updateInterval = ini.getIntegerProperty("status", "updateInterval", 500);
		
		screenshot.autoUpdate = ini.getBooleanProperty("screenshot", "autoupdate", true);
		screenshot.updateInterval = ini.getIntegerProperty("screenshot", "updateinterval", 5000);
		screenshot.scale = ini.getIntegerProperty("screenshot", "scale", 100);
		screenshot.quality = ini.getIntegerProperty("screenshot", "quality", 50);
		
		if (screenshot.scale > 100) screenshot.scale = 100; else
		if (screenshot.scale < 10)  screenshot.scale = 10;
		
		if (screenshot.quality > 100) screenshot.quality = 100; else
		if (screenshot.quality < 10)  screenshot.quality = 10;
		
		window.x = ini.getIntegerProperty("window", "x", 50);
		window.y = ini.getIntegerProperty("window", "y", 50);
		window.maximized = ini.getBooleanProperty("window", "maximized", true);
		window.width = ini.getIntegerProperty("window", "width", 1000);
		window.height = ini.getIntegerProperty("window", "height", 700);
		
		sound.enabled = ini.getBooleanProperty("sound", "enabled", true);
		sound.whisper = ini.getBooleanProperty("sound", "whisper", true);
		sound.say = ini.getBooleanProperty("sound", "say", true);
		sound.gm = ini.getBooleanProperty("sound", "gm", true);
		sound.tts.enabled = ini.getBooleanProperty("sound.tts", "enabled", true);
		sound.tts.whisper = ini.getBooleanProperty("sound.tts", "whisper", true);
		sound.tts.say = ini.getBooleanProperty("sound.tts", "say", false);
		sound.tts.gm = ini.getBooleanProperty("sound.tts", "gm", true);
		sound.tts.status = ini.getBooleanProperty("sound.tts", "status", true);
				
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
				ini.setIntegerProperty("screenshot", "quality", screenshot.quality);
		
				ini.setIntegerProperty("window", "x", window.x);
				ini.setIntegerProperty("window", "y", window.y);
				ini.setBooleanProperty("window", "maximized", window.maximized);
				ini.setIntegerProperty("window", "width", window.width);
				ini.setIntegerProperty("window", "height", window.height);				
				
				ini.setBooleanProperty("sound", "enabled", sound.enabled);
				ini.setBooleanProperty("sound", "whisper", sound.whisper);
				ini.setBooleanProperty("sound", "say", sound.say);
				ini.setBooleanProperty("sound", "gm", sound.gm);
				ini.setBooleanProperty("sound.tts", "enabled", sound.tts.enabled);
				ini.setBooleanProperty("sound.tts", "whisper", sound.tts.whisper);
				ini.setBooleanProperty("sound.tts", "say", sound.tts.say);
				ini.setBooleanProperty("sound.tts", "gm", sound.tts.gm);
				ini.setBooleanProperty("sound.tts", "status", sound.tts.status);
				
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
		public static int quality = 100;
	}
	
	public static class window {
		public static int x = 50;
		public static int y = 50;
		public static boolean maximized = true;
		public static int width = 1000;
		public static int height = 700;
	}
	
	public static class sound {
		public static boolean enabled = true;
		public static boolean whisper = true;
		public static boolean say = true;
		public static boolean gm = true;
		
		public static class tts {
			public static boolean enabled = true;
			public static boolean whisper = true;
			public static boolean say = true;
			public static boolean gm = true;
			public static boolean status = true;
		}
	}
}
