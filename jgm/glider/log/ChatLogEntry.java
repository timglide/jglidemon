package jgm.glider.log;

import java.util.regex.*;

public class ChatLogEntry extends LogEntry {
	public ChatLogEntry(String t, String s) {
		super(t, s);
	}
	
	public ChatLogEntry(String s) {
		this("Chat", s);
	}

	private static Pattern PATTERN = null;

	static {
		PATTERN = Pattern.compile(".*?(<GM>|)\\[([^]]+)\\] (whisper|say)s: (.*)");

		/* group 1: <GM>?
		 *       2: from
		 *       3: type
		 *       4: message
		 */
	}

	private static ChatLogEntry parse(String s) {
		Matcher m = PATTERN.matcher(s);

		if (!m.matches()) {
			return new ChatLogEntry(s);
		}

		WhisperEntry.Urgency urgency = WhisperEntry.Urgency.URGENT;

		boolean gm     = m.group(1).equals("<GM>");
		String from    = m.group(2);
		String type    = m.group(3);
		type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
		String message = m.group(4);

		if (gm) {
			type = "GM " + type;
			urgency = WhisperEntry.Urgency.CRITICAL;
		}

		return new WhisperEntry(
			s, message, from, urgency, type
		);
	}
	
	public static ChatLogEntry factory(String rawText) {
		return parse(rawText);
	}
}
