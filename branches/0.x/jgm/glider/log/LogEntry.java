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

import java.util.Date;
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
	
	/**
	 * Returns the timestamp in HH:mm:ss format.
	 * @return The formatted timestamp
	 */
	public String getFormattedTimestamp() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		return df.format(timestamp);
	}

	private static Pattern TIMESTAMP_PATTERN =
		Pattern.compile("^\\s*\\[\\d+(?::\\d\\d)+\\s*(?:AM|PM)?\\]\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Create a subclass of LogEntry depending on the
	 * content of s.
	 * @param s The raw String to parse
	 * @return The appropriate subclass of LogEntry
	 */
	public static LogEntry factory(String s) {
		String[] parts = s.split(" ", 2);

		if (parts.length != 2) {
			log.warning("Invalid LogEntry: " + s);
			return null;
		}

		String type    = parts[0].substring(1, parts[0].length() - 1);
		String rawText = parts[1];

		// remove leading timestamp in case user has a timestamp mod
		Matcher m = TIMESTAMP_PATTERN.matcher(rawText);
		
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

		return ret;
	}
}
