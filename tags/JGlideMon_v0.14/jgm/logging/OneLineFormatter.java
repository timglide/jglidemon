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
package jgm.logging;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class OneLineFormatter extends Formatter {	
	static final SimpleDateFormat df =
		new SimpleDateFormat("HH:mm:ss");
	static final String fmt =
		"[%s][%-7s] %s: %s" + System.getProperty("line.separator");
	
	Date dt = new Date();
	
	public String format(LogRecord r) {
		StringBuffer sb = new StringBuffer();
		dt.setTime(r.getMillis());
		
		sb.append(String.format(
			fmt,
			getDateFormat().format(dt),
			r.getLevel().toString(),
			r.getLoggerName(),
			r.getMessage()
		));	
		
		if (r.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				r.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {}
		}
				
		return sb.toString();
	}
	
	protected SimpleDateFormat getDateFormat() {
		return df;
	}
}
