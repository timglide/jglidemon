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
		PATTERN = Pattern.compile("\\[([^]]+)\\] (whisper|say)s: (.*)");

		/* group 1: from
		 *       2: type
		 *       3: message
		 */
	}

	private static ChatLogEntry parse(String s) {
		Matcher m = PATTERN.matcher(s);

		if (!m.matches()) {
			return new ChatLogEntry(s);
		}

		int urgency = 1;;

		String from    = m.group(1);
		String type    = m.group(2);
		type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
		String message = m.group(3);

		if (from.startsWith("<GM>")) urgency++;
		
		return new WhisperEntry(
			s, message, from, urgency, type
		);
	}
	
	public static ChatLogEntry factory(String rawText) {
		return parse(rawText);
	}
}
