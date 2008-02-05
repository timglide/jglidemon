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
package jgm;

import jgm.util.*;

import java.lang.reflect.*;
import java.util.logging.*;
import java.io.File;

/**
 * Contains all global configuration values and handles
 * reading and writing to the ini file.
 * @author Tim
 * @since 0.1
 */
public class Config extends QuickIni {
	static Logger log = Logger.getLogger(Config.class.getName());
		
	public static Config instance;
	private static final File iniFile = new File("JGlideMon.ini");
	//private static QuickIni ini = new QuickIni(iniFile.getName());
	
	public static boolean iniFileExists() {
		return iniFile.exists();
	}
	
	public static Config getInstance() {
		return instance;
	}
	
	public Config() {
		super(iniFile.getName());
		instance = this;
		
		if (iniFileExists()) {
			validate();
		}
	}
	
	private static final String DEF = defaults.class.getName() + "$";
	
	private String getClass(String section) {
		return DEF + section.replace('.', '$');
	}
	
	public int getInt(final String sectionName,
					  String propertyName) {
		Class c; Field f; int defaultValue = 0;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = f.getInt(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return super.getIntegerProperty(sectionName, propertyName, defaultValue);
	}
	
	public boolean getBool(final String sectionName,
						   String propertyName) {
		Class c; Field f; boolean defaultValue = false;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = f.getBoolean(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return super.getBooleanProperty(sectionName, propertyName, defaultValue);
	}
	
	public long getLong(final String sectionName,
						String propertyName) {
		Class c; Field f; long defaultValue = 0;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = f.getLong(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return super.getLongProperty(sectionName, propertyName, defaultValue);
	}
	
	public double getDouble(final String sectionName,
							String propertyName) {
		Class c; Field f; double defaultValue = 0.0;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = f.getDouble(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		return super.getDoubleProperty(sectionName, propertyName, defaultValue);
	}
	
	public String getString(final String sectionName,
							String propertyName) {
		Class c; Field f; String defaultValue = null;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = (String) f.get(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return super.getStringProperty(sectionName, propertyName, defaultValue);
	}
	
	public String get(final String sectionName,
					  String propertyName) {
		Class c; Field f; Object defaultValue = null;
		propertyName = propertyName.toLowerCase();
		
		try {
			c = Class.forName(getClass(sectionName));
			f = c.getField(propertyName);
			defaultValue = f.get(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return super.getStringProperty(sectionName, propertyName, defaultValue.toString());
	}

	public boolean setBool(final String sectionName,
						   String propertyName,
						   final boolean value) {
		propertyName = propertyName.toLowerCase();
		return super.setBooleanProperty(sectionName, propertyName, value);
	}
	
	public boolean setInt(final String sectionName,
						  String propertyName,
						  final int value) {
		propertyName = propertyName.toLowerCase();
		return super.setIntegerProperty(sectionName, propertyName, value);
	}
	
	public boolean setLong(final String sectionName,
						   String propertyName,
						   final long value) {
		propertyName = propertyName.toLowerCase();
		return super.setLongProperty(sectionName, propertyName, value);
	}
	
	public boolean setDouble(final String sectionName,
							 String propertyName,
							 final double value) {
		propertyName = propertyName.toLowerCase();
		return super.setDoubleProperty(sectionName, propertyName, value);
	}
	
	public boolean setString(final String sectionName,
							 String propertyName,
							 final String value) {
		propertyName = propertyName.toLowerCase();
		return super.setStringProperty(sectionName, propertyName, value);
	}
	
	public boolean set(final String sectionName,
					   final String propertyName,
					   final Object value) {
		/*Class c; Field f; String defaultValue = null;
		
		try {
			c = Class.forName(DEF + sectionName);
			f = c.getField(propertyName);
			defaultValue = (String) f.get(null);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Class c = f.getType();
		c.getCanonicalName()*/
						   
		if (value instanceof Integer) {
			return setInt(sectionName, propertyName, (Integer) value);
		} else if (value instanceof Double) {
			return setDouble(sectionName, propertyName, (Double) value);
		} else if (value instanceof Boolean) {
			return setBool(sectionName, propertyName, (Boolean) value);
		} else if (value instanceof String) {
			return setString(sectionName, propertyName, (String) value);
		}
		
		return false;
	}
	
	public void validate() {
		if (getInt("log", "maxEntries") < 1) {
			setInt("log", "maxEntries", defaults.log.maxentries);
		}

		int i = getInt("screenshot", "scale");
		if (i > 100) setInt("screenshot", "scale", 100); else
		if (i < 10)  setInt("screenshot", "scale", 10);
		
		i = getInt("screenshot", "quality");
		if (i > 100) setInt("screenshot", "quality", 100); else
		if (i < 10)  setInt("screenshot", "quality", 10);
		
		if (getDouble("screenshot", "buffer") < 1.0)
			setDouble("screenshot", "buffer", 1.0);
		
		if (getInt("screenshot", "timeout") < 5)
			setInt("screenshot", "timeout", 5);
		
		if (getInt("stuck", "limit") < 0)
			setInt("stuck", "limit", 0);
		
		if (getInt("stuck", "timeout") < 5)
			setInt("stuck", "timeout", 5);
	}
	
	public static void writeIni() {
		log.fine("Saving configuration to " + iniFile.getName());
		instance.updateFile();
	}
	
	public static class defaults {
		public static class general {
			public static final boolean debug = false;
			public static final boolean showtray = true;
			public static final boolean mintotray = true;
			public static final String wowdb = "http://wow.allakhazam.com/db/item.html?witem=%s";
			public static final int lasttab = 0;
		}
		
		public static class log {
			public static final int maxentries = 500;
		}
		
		public static class net {
			public static final String host = "localhost";
			public static final int    port = 3200;
			public static final String password = "";
			public static final boolean autoreconnect = true;
			public static final int     autoreconnectdelay = 5;
			public static final int     autoreconnecttries = 10;
		}
		
		public static class status {
			public static final int updateinterval = 500;
		}
		
		public static class screenshot {
			public static final boolean autoupdate = true;
			public static final int updateinterval = 5000;
			public static final boolean autoscale = true;
			// seems to be some weird issue if scale is 100
			public static final int scale = 99;
			public static final int quality = 100;
			public static final double buffer = 1.5;
			public static final int timeout = 10;
		}
		
		public static class window {
			public static final int x = 50;
			public static final int y = 50;
			public static final boolean maximized = true;
			public static final int width = 900;
			public static final int height = 650;
		}
		
		public static class sound {
			public static final boolean enabled = true;
			public static final boolean whisper = true;
			public static final boolean say = true;
			public static final boolean gm = true;
			public static final boolean follow = true;
			public static final boolean pvp = true;
			public static final boolean stuck = true;
			public static final boolean status = true;
			
			public static class tts {
				public static final boolean enabled = true;
				public static final boolean whisper = true;
				public static final boolean say = false;
				public static final boolean gm = true;
				public static final boolean status = true;
			}
		}
		
		public static class stuck {
			public static final boolean enabled = false;
			public static final int limit = 5;
			public static final int timeout = 300;
		}
		
		public static class web {
			public static final boolean enabled = false;
			public static final int port = 3201;
			public static final int updateinterval = 5000;
		}
	}
}
