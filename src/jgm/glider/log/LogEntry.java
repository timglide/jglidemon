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

import jgm.gui.updaters.LogUpdater;

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
	
	static Map<String, String> COLOR_MAP = new HashMap<String, String>();
	
	static {
		COLOR_MAP.put("loot", "#00AA00");
		COLOR_MAP.put("rep",  "#5555FF");
		COLOR_MAP.put("skill", COLOR_MAP.get("rep"));
	}
	
	public    Date timestamp = new Date();
	protected String type = "Unknown";
	
	protected final String rawText;
	protected String text = null;
	private String htmlText = null;
	private String html5Text = null;

	private Map<Integer, ChatLink> chatLinks;
	
	/**
	 * Create a new LogEntry.
	 * @param type The type of entry, Whisper, Chat, GliderLog, etc.
	 * @param rawText The raw text as received from the /log command
	 */
	public LogEntry(String type, String rawText) {
		this.type = type;
		this.rawText = rawText;
		this.text = rawText;
		parseLinks();
	}
	
	public boolean hasChatLinks() {
		return !chatLinks.isEmpty();
	}
	
	public Map<Integer, ChatLink> getChatLinks() {
		return chatLinks;
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

	public String getPlainText() {
		return rawText;
	}
	
	public boolean supportsHtmlText() {
		return true;
	}
	
	public String getHtmlPreColor() {
		return null;
	}
	
	public String getHtml5Text() {
		return getHtml5Text(getHtmlPreColor());
	}
	
	/**
	 * Returns a string suitable for displaying within a web page that
	 * will contain links useful for generating Wowhead tooltips.
	 * @param preColor
	 * @return
	 */
	public String getHtml5Text(String preColor) {
		if (null != html5Text) return html5Text;
		
		if (!supportsHtmlText())
			throw new UnsupportedOperationException("HTML not available");
		
		String rawText = this.rawText
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
		Matcher m = FORMATTING_REGEX.matcher(convertLinks(rawText));
	    
		html5Text = 
		(preColor != null ? "<span style=\"color: #" + preColor + ";\">" : "") +
		m.replaceAll("<span style=\"color: #$2;\">")
			.replace("|r", "</span>") +
		(preColor != null ? "</span>" : "");
		
		return html5Text;
	}
	
	public String getHtmlText() {
		return getHtmlText(getHtmlPreColor());
	}
	
	/**
	 * Returns a string suitable for placing in a Swing label for display
	 * within the program.
	 * @param preColor
	 * @return
	 */
	public String getHtmlText(String preColor) {
		if (null != htmlText) return htmlText;
		
		if (!supportsHtmlText())
			throw new UnsupportedOperationException("HTML not available");
		
		String rawText = this.rawText
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
		Matcher m = FORMATTING_REGEX.matcher(removeLinks(rawText));
			    
		htmlText = 
		(preColor != null ? "<font color=\"" + preColor + "\">" : "") +
		m.replaceAll("<font color=\"$2\">")
			.replace("|r", "</font>") +
		(preColor != null ? "</font>" : "");
		
		return htmlText;
	}
	
	public String getText() {
		return null != text ? text : rawText;
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
	
	public static LogEntry factory(LogUpdater updater, String s) {
		return factory(updater, s, LogFile.None);
	}
	
	/**
	 * Create a subclass of LogEntry depending on the
	 * content of s.
	 * @param s The raw String to parse
	 * @param logFile The type of log file s is from, if applicable
	 *                (it's format slightly different than from the telnet)
	 * @return The appropriate subclass of LogEntry
	 */
	public static LogEntry factory(LogUpdater updater, String s, LogFile logFile) {
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
						return null;
					}
				}
				
//				System.out.println("  Parsed Time");
				
				cal.setTime(new Date());
				
				if (m.matches()) {
					cal.set(Calendar.AM_PM, m.group(3).equals("AM") ? Calendar.AM : Calendar.PM);
					cal.set(Calendar.HOUR, Integer.parseInt(m.group(1)));
					cal.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
					cal.set(Calendar.SECOND, 0);
					overrideDate = cal.getTime();
					
					// remove the timestamp;
					s = m.group(4);
				}
				
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
//			log.log(Level.WARNING, "Invalid LogEntry line", t);
//			log.warning("Raw line: " + s);
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
			ret = CombatLogEntry.factory(updater, rawText);
//			ret = new CombatLogEntry(rawText);
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
	
	protected static final Pattern CONVERT_LINK_REGEX =
		Pattern.compile("\\|H([^|]*?)\\|h(.*?)\\|h");
	
	public static String convertLinks(String str) {
		Matcher m = CONVERT_LINK_REGEX.matcher(str);
		
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;
		int matchCount = 0;
		
		while (m.find()) {
			matchCount++;
//			System.out.println("Found: " + m.group(0));
			sb.append(str.substring(lastEnd, m.start()));
			lastEnd = m.end();
			
			String
				linkType = "",
				id = "",
				enchant = "",
				gem1 = "",
				gem2 = "",
				gem3 = "",
				gem4 = "",
				suffix = "",
				unique = "",
				linkLvl = "",
				name = "";
			
			String[] parts = m.group(1).split(":");
			name = m.group(2);
			
			try {
				linkType = parts[0].toUpperCase();
				id = parts[1];
				enchant = parts[2];
				gem1 = parts[3];
				gem2 = parts[4];
				gem3 = parts[5];
				gem4 = parts[6];
				suffix = parts[7];
				unique = parts[8];
				linkLvl = parts[9];
			} catch (IndexOutOfBoundsException e) {}
			
			
			ChatLinkType type = null;
			
			try {
				type = ChatLinkType.valueOf(linkType);
			} catch (IllegalArgumentException e) {
				log.fine("Unable to parse chat link, type=" + linkType + ", " + m.group(0));
				sb.append(removeLinks(m.group(0)));
			}
			
			if (null != type) {
				sb.append(type.getHtml5Link(
					id, enchant, gem1, gem2, gem3, gem4,
					suffix, unique, linkLvl, name));
			}
		}
		
		if (0 == lastEnd) {
			return removeLinks(str);
		}
		
		if (lastEnd < str.length()) {
			sb.append(str.substring(lastEnd, str.length()));
		}
		
		return sb.toString();
	}
	
	// removing formatting, links
	protected static final Pattern LINK_REGEX =
		Pattern.compile("\\|H[^|]+(?=|h)");
	
	public static Map<Integer, ChatLink> parseLinks(String str) {
		Map<Integer, ChatLink> ret = new LinkedHashMap<Integer, ChatLink>(0);
		
		str = removeFormatting(str);
		
		ChatLinkType curType;
		ChatLink curLink, lastLink = null;
		Matcher m = CONVERT_LINK_REGEX.matcher(str);
		int rawIndex = 0;
		int index = 0;
		int discardedChars = 0;
		String[] parts;
		
		while (m.find(rawIndex)) {
			rawIndex = m.start();
			index = rawIndex - discardedChars;
			
			parts = m.group(1).split(":");
			try {
				curType = ChatLinkType.valueOf(parts[0].toUpperCase());
			} catch (IllegalArgumentException iae) {
				curType = ChatLinkType.UNKNOWN;
			}
			curLink = new ChatLink(rawIndex, index, curType, m.group(0), parts, m.group(2));
			
			discardedChars += curLink.rawLength() - curLink.length();
			
			ret.put(index, curLink);
			lastLink = curLink;
			
			rawIndex = m.end();
		}
		
		return ret;
	}
	
	protected void parseLinks() {
		chatLinks = parseLinks(rawText);
		
//		if (!chatLinks.isEmpty()) {
//			System.out.println("\nLinks found for: " + getText());
//			
//			for (ChatLink cl : chatLinks.values()) {
//				System.out.println("  " + cl);
//			}
//		}
	}
	
	public static String removeLinks(String str) {
		Matcher m = LINK_REGEX.matcher(str);
		return m.replaceAll("").replace("|h", "");
	}
	
	protected void removeLinks() {
		text = removeLinks(text);
	}
	
	// group 1 = transparancy, 2 = color (both in hex)
	protected static final Pattern FORMATTING_REGEX =
		Pattern.compile("\\|(?:[Cc]([0-9A-Fa-f]{2})([0-9A-Fa-f]{6}))");

	public static String removeFormatting(String str) {
		Matcher m = FORMATTING_REGEX.matcher(str);
		return m.replaceAll("").replace("|r", "");
	}
	
	protected void removeFormatting() {
		 // |cFFFFFFFFWhite
		text = removeFormatting(text);
	}
	
	protected void removeLinksAndFormatting() {
		removeLinks();
		removeFormatting();
	}
}
