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
package jgm.util;

import java.io.*;
import java.util.logging.*;

import jgm.Config;
import jgm.JGlideMon;

public class Log {
	public static void reloadConfig() {
		try {
			String props =
				(Config.getInstance() != null && Config.getInstance().getBool("general.debug")
						 ? "logging.debug.properties" : "logging.properties");
			
//			System.out.println("Reading: " + props);
			
			InputStream in =
				JGlideMon.class.getResourceAsStream("properties/" + props);
			LogManager.getLogManager().readConfiguration(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setLevel(Level l) {
		Logger.getLogger("jgm").setLevel(l);
	}
}
