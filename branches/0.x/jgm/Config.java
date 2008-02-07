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

import java.util.Properties;
import java.util.logging.*;
import java.io.*;

/**
 * Contains all global configuration values and handles
 * reading and writing to the ini file.
 * @author Tim
 * @since 0.1
 */
public class Config {
	static Logger log = Logger.getLogger(Config.class.getName());
	
	static Properties DEFAULTS = new Properties();
	
	static {
		try {
			DEFAULTS.load(
				jgm.JGlideMon.class.getResourceAsStream("properties/JGlideMon.defaults.properties"));
		} catch (Throwable e) {
			log.log(Level.WARNING, "Unable to load default config properties", e);
			System.exit(-1);
		}
	}
	
	public static Config instance;
	public static Config c;
	
	Properties props = new Properties(DEFAULTS);
	
	static final File propsFile = new File("JGlideMon.properties");
	
	static final File iniFile = new File("JGlideMon.ini");
	
	public static boolean fileExists() {
		return propsFile.exists();
	}
	
	public static Config getInstance() {
		return instance;
	}
	
	public static Properties getProps() {
		return instance.props;
	}
	
	public static Properties getDefaults() {
		return DEFAULTS;
	}
	
	public Config() {
		instance = this;
		c = this;
		
		// convert to new format
		if (iniFile.exists() && !propsFile.exists()) {
			log.info("Converting JGlideMon.ini to new format");
			
			QuickIni qi = new QuickIni(iniFile.getName());
			
			java.util.Iterator<String> i = qi.getAllSectionNames();
				
			while (i.hasNext()) {
				String cur = i.next();
				
				for (String s : qi.getAllPropertyNames(cur)) {
					props.setProperty(cur + "." + s, qi.getStringProperty(cur, s));
				}
			}
			
			write();
			iniFile.delete();
		}
		
		if (propsFile.exists()) {
			try {
				FileInputStream fs = new FileInputStream(propsFile);
				props.load(fs);
			} catch (Throwable e) {
				log.log(Level.WARNING, "Unable to load config properties", e);
				System.exit(-1);
			}
		}
		
		if (hasProp("net.host")) {
			log.info("Adding old server info to new format");
			set("servers.0.name", "Default");
			set("servers.0.net.host", get("net.host"));
			set("servers.0.net.port", get("net.port"));
			set("servers.0.net.password", get("net.password"));
			
			props.remove("net.host");
			props.remove("net.port");
			props.remove("net.password");
		}
		
		validate();
	}
	
	public boolean hasProp(String propertyName) {
		return props.containsKey(propertyName);
	}
	
	public int getInt(String propertyName) {
		int defaultValue = 0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue = 
				Integer.parseInt(props.getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public boolean getBool(String propertyName) {
		boolean defaultValue = false;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Boolean.parseBoolean(props.getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public long getLong(String propertyName) {
		long defaultValue = 0L;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Long.parseLong(props.getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public double getDouble(String propertyName) {
		double defaultValue = 0.0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Double.parseDouble(props.getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public String getString(String propertyName) {
		propertyName = propertyName.toLowerCase();
		
		return props.getProperty(propertyName);
	}
	
	public String get(String propertyName) {
		return getString(propertyName);
	}

	public void set(String propertyName, Object value) {
		props.setProperty(propertyName, value.toString());
	}
	
	public void validate() {
		if (getInt("log.maxentries") < 1) {
			set("log.maxentries", DEFAULTS.getProperty("log.maxentries"));
		}

		int i = getInt("screenshot.scale");
		if (i > 100) set("screenshot.scale", 100); else
		if (i < 10)  set("screenshot.scale", 10);
		
		i = getInt("screenshot.quality");
		if (i > 100) set("screenshot.quality", 100); else
		if (i < 10)  set("screenshot.quality", 10);
		
		if (getDouble("screenshot.buffer") < 1.0)
			set("screenshot.buffer", 1.0);
		
		if (getInt("screenshot.timeout") < 5)
			set("screenshot.timeout", 5);
		
		if (getInt("stuck.limit") < 0)
			set("stuck.limit", 0);
		
		if (getInt("stuck.timeout") < 5)
			set("stuck.timeout", 5);
	}
	
	public static void write() {
		log.fine("Saving configuration to " + propsFile.getName());
		
		try {
			FileOutputStream fs = new FileOutputStream(propsFile);
			instance.props.store(fs, "JGlideMon Settings");
		} catch (Throwable t) {
			log.log(Level.WARNING, "Error saving config properties", t);
		}
	}
}
