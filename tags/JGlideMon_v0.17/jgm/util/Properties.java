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
		return null != getProperty(key);
	}
	
	public int getInt(String propertyName) {
		int defaultValue = 0;
		
		try {
			defaultValue = 
				Integer.parseInt(get(propertyName));
		} catch (NumberFormatException e) {}
		
		return defaultValue;
	}
	
	public boolean getBool(String propertyName) {
		boolean defaultValue = false;
		
		defaultValue =
			Boolean.parseBoolean(get(propertyName));
		
		return defaultValue;
	}
	
	public long getLong(String propertyName) {
		long defaultValue = 0L;
		
		try {
			defaultValue =
				Long.parseLong(get(propertyName));
		} catch (NumberFormatException e) {}
		
		return defaultValue;
	}
	
	public double getDouble(String propertyName) {
		double defaultValue = 0.0;
		
		try {
			defaultValue =
				Double.parseDouble(get(propertyName));
		} catch (NumberFormatException e) {}
		
		return defaultValue;
	}
	
	public String get(String propertyName) {
		propertyName = propertyName.toLowerCase();
		
		if (!has(propertyName))
			throw new NullPointerException("Key not found: " + propertyName);
		
		return getProperty(propertyName);
	}

	public void set(String propertyName, Object value) {
		setProperty(propertyName, value.toString());
	}
}
