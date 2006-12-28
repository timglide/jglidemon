package jgm.glider.log;

import java.util.Date;
import java.text.SimpleDateFormat;

public class LogEntry implements Comparable<LogEntry> {
	protected Date timestamp = new Date();
	protected String type = "Unknown";
	
	protected String rawText = null;

	public LogEntry(String t, String s) {
		type = t;
		rawText = s;
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

	public int compareTo(LogEntry e) {
		return this.timestamp.compareTo(e.timestamp);
	}

	public String getFormattedTimestamp() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		return df.format(timestamp);
	}

	public static LogEntry factory(String s) {
		String[] parts = s.split(" ", 2);

		if (parts.length != 2) {
			System.err.println("Invalid LogEntry: " + s);
			return null;
		}

		String type    = parts[0].substring(1, parts[0].length() - 1);
		String rawText = parts[1];

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
