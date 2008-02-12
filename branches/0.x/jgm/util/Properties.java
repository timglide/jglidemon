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
package jgm.util;

public class Properties extends java.util.Properties {
	public Properties() {
		super();
	}
	
	public Properties(Properties defaults) {
		super(defaults);
	}
	
	public boolean has(String key) {
		return containsKey(key);
	}
	
	public int getInt(String propertyName) {
		int defaultValue = 0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue = 
				Integer.parseInt(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public boolean getBool(String propertyName) {
		boolean defaultValue = false;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Boolean.parseBoolean(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public long getLong(String propertyName) {
		long defaultValue = 0L;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Long.parseLong(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public double getDouble(String propertyName) {
		double defaultValue = 0.0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Double.parseDouble(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public String getString(String propertyName) {
		propertyName = propertyName.toLowerCase();
		
		return getProperty(propertyName);
	}
	
	public String get(String propertyName) {
		return getString(propertyName.toLowerCase());
	}

	public void set(String propertyName, Object value) {
		setProperty(propertyName, value.toString());
	}
}
