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
	
	// all the old keys that need to be converted to
	// servers.0.xxx
	static final String[] OLD_KEYS = {
		"net.host", "net.port", "net.password",
		"window.", "general.lasttab",
		"screenshot.scale", "screenshot.buffer",
		"web.enabled", "web.port"
	};
	
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
	
	Properties p = new Properties(DEFAULTS);
	
	static final File propsFile = new File("JGlideMon.properties");
	
	static final File iniFile = new File("JGlideMon.ini");
	
	public static boolean fileExists() {
		return propsFile.exists();
	}
	
	public static Config getInstance() {
		return instance;
	}
	
	public static Properties getProps() {
		return instance.p;
	}
	
	public static Properties getDefaults() {
		return DEFAULTS;
	}
	
	public Config() {
		if (instance != null)
			throw new IllegalStateException("Can only have one instance of Config");
		
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
					p.setProperty(cur + "." + s, qi.getStringProperty(cur, s));
				}
			}
			
			write();
			iniFile.delete();
		}
		
		if (propsFile.exists()) {
			try {
				FileInputStream fs = new FileInputStream(propsFile);
				p.load(fs);
			} catch (Throwable e) {
				log.log(Level.WARNING, "Unable to load config properties", e);
				System.exit(-1);
			}
		}
		
		Object[] keys = p.keySet().toArray();
		for (Object o : keys) {
			String key = o.toString();
			for (String oldKey : OLD_KEYS) {
				if (key.startsWith(oldKey)) {
					log.finest("Moving " + key + " to servers.0." + key);
					set("servers.0." + key, get(key));
					p.remove(key);
				}
			}
		}
		
		validate();
	}
	
	public boolean has(String propertyName) {
		return p.has(propertyName);
	}
	
	public int getInt(String propertyName) {
		return p.getInt(propertyName);
	}
	
	public boolean getBool(String propertyName) {
		return p.getBool(propertyName);
	}
	
	public long getLong(String propertyName) {
		return p.getLong(propertyName);
	}
	
	public double getDouble(String propertyName) {
		return p.getDouble(propertyName);
	}
	
	public String getString(String propertyName) {
		return get(propertyName);
	}
	
	public String get(String propertyName) {
		return p.get(propertyName);
	}

	public void set(String propertyName, Object value) {
		p.set(propertyName, value);
	}
	
	public void validate() {
		if (getInt("log.maxentries") < 1) {
			set("log.maxentries", DEFAULTS.getProperty("log.maxentries"));
		}

//		int i = getInt("screenshot.scale");
//		if (i > 100) set("screenshot.scale", 100); else
//		if (i < 10)  set("screenshot.scale", 10);
		
		int i = getInt("screenshot.quality");
		if (i > 100) set("screenshot.quality", 100); else
		if (i < 10)  set("screenshot.quality", 10);
		
//		if (getDouble("screenshot.buffer") < 1.0)
//			set("screenshot.buffer", 1.0);
		
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
			instance.p.store(fs, "JGlideMon Settings");
		} catch (Throwable t) {
			log.log(Level.WARNING, "Error saving config properties", t);
		}
	}
}
