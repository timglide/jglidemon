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
package jgm.glider.log;

import java.util.*;
import java.util.regex.*;
import java.util.logging.*;
import java.text.SimpleDateFormat;

/**
 * Represents a log entry that would be received from
 * the /log command from the Glider remote client.
 * @author Tim
 * @since 0.1
 */
public class LogEntry implements Comparable<LogEntry> {
	protected static Logger log = Logger.getLogger(LogEntry.class.getName());
	
	protected Date timestamp = new Date();
	protected String type = "Unknown";
	
	protected String rawText = null;

	/**
	 * Create a new LogEntry.
	 * @param type The type of entry, Whisper, Chat, GliderLog, etc.
	 * @param rawText The raw text as received from the /log command
	 */
	public LogEntry(String type, String rawText) {
		this.type = type;
		this.rawText = rawText;
	}
	
	public final String getType() {
		return type;
	}
	
	public final String toString() {
		return String.format("[%s][%s] %s",
							 getFormattedTimestamp(),
							 getType(),
							 getText());
	}

	public final String getRawText() {
		return rawText;
	}

	public String getText() {
		return getRawText();
	}

	/**
	 * Compares this LogEntry's timestamp to the
	 * supplied LogEntry's.
	 * @param e The LogEntry to compare to
	 */
	public int compareTo(LogEntry e) {
		return this.timestamp.compareTo(e.timestamp);
	}

	public Date getTimestamp() {
		return timestamp;
	}
	
	
	private static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * Returns the timestamp in HH:mm:ss format.
	 * @return The formatted timestamp
	 */
	public static String getFormattedTimestamp(Date d) {
		return df.format(d);
	}
	
	public String getFormattedTimestamp() {
		return getFormattedTimestamp(timestamp);
	}

	private static Pattern TIMESTAMP_PATTERN =
		Pattern.compile("^\\s*\\[\\d+(?::\\d\\d)+\\s*(?:AM|PM)?\\]\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	
	private static Pattern LOGFILE_TIMESTAMP_PATTERN =
		Pattern.compile("^(\\d+):(\\d+)\\s+(AM|PM)\\s+(.*)$");
	
	private static Pattern LOGFILE_DEBUG_TIMESTAMP_PATTERN =
		Pattern.compile("^\\d+:\\d+:\\d+\\.\\d+\\s+.*$");
	
	private static Calendar cal = new GregorianCalendar();
	private static Date today = new Date();
	
	public static LogEntry factory(String s) {
		return factory(s, LogFile.None);
	}
	
	/**
	 * Create a subclass of LogEntry depending on the
	 * content of s.
	 * @param s The raw String to parse
	 * @param logFile The type of log file s is from, if applicable
	 *                (it's format slightly different than from the telnet)
	 * @return The appropriate subclass of LogEntry
	 */
	public static LogEntry factory(String s, LogFile logFile) {
		Date overrideDate = null;
		Matcher m = null;
		
		switch (logFile) {
			case Chat:
			case _NormalChat:
			case Combat:
//				System.out.println("PARSING: " + s);
				// parse the timestamp
				m = LOGFILE_TIMESTAMP_PATTERN.matcher(s);
				
				if (!m.matches()) {
					m = LOGFILE_DEBUG_TIMESTAMP_PATTERN.matcher(s);
					
					if (m.matches()) {
						log.finer("Ignoring debug line while parsing log file");
					} else {
						log.finer("Ignoring malformed line while parsing log file: " + s);
					}
					
					return null;
				}
				
//				System.out.println("  Parsed Time");
				
				cal.setTime(today);
				cal.set(Calendar.AM_PM, m.group(3).equals("AM") ? Calendar.AM : Calendar.PM);
				cal.set(Calendar.HOUR, Integer.parseInt(m.group(1)));
				cal.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
				cal.set(Calendar.SECOND, 0);
				overrideDate = cal.getTime();
				
				// remove the timestamp;
				s = m.group(4);
				
//				System.out.println("  1st: " + s);
				// add the correct type
				String prepend = "";
				
				switch (logFile) {
					case Chat: prepend = "[ChatRaw] "; break;
					case _NormalChat: prepend = "[Chat] "; break;
					case Combat: prepend = "[Combat] "; break;
				}
				
				s = prepend + s;
//				System.out.println("  final: " + s);
				break;
				
			case None:
				break;
				
			default:
				log.warning("Invalid LogFile type while parsing line: " + logFile.toString());
				return null;
		}
				
		String[] parts = s.split(" ", 2);

		if (parts.length != 2) {
			log.warning("Invalid LogEntry: " + s);
			return null;
		}

		String type    = null;
		String rawText = null;

		// i don't know why but rarely an exception is thrown here
		// added the try/catch just to be safe
		try {
			type    = parts[0].substring(1, parts[0].length() - 1);
			rawText = parts[1];
		} catch (Throwable t) {
			log.log(Level.WARNING, "Strange error parsing log entry", t);
			type = "UNKNOWN";
			rawText = t.getClass().getName() + ": " + t.getMessage();
		}
		
		// remove leading timestamp in case user has a timestamp mod
		m = TIMESTAMP_PATTERN.matcher(rawText);
		
		if (m.matches()) {
			rawText = m.group(1);
			//System.out.println("Matched timestamp, removing...");
		}
		
		//System.out.println("Found: '" + type + "'->'" + rawText + "'");
		
		LogEntry ret = null;

		if (type.equals("GliderLog")) {
			ret = new GliderLogEntry(rawText);
		} else if (type.equals("Status")) {
			ret = new StatusEntry(rawText);
		} else if (type.equals("ChatRaw")) {
			ret = new RawChatLogEntry(rawText);
		} else if (type.equals("Combat")) {
			ret = new CombatLogEntry(rawText);
		} else if (type.equals("Chat")) {
			ret = ChatLogEntry.factory(rawText);
		} else {
			ret = new LogEntry(type, rawText);
		}
		
		if (overrideDate != null) {
			ret.timestamp = overrideDate;
		}

		return ret;
	}
}
