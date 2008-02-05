package jgm.glider.log;

import jgm.Locale;
import jgm.sound.*;

import java.util.regex.*;

public class ChatLogEntry extends LogEntry {
	static String GM_INDICATOR = null;
	static String WHISPERS_VERB = null;
	static String SAYS_VERB = null;
	static String WHISPER_NOUN = null;
	static String SAY_NOUN = null;
	
	static {
		localeChanged();
		
		Locale.addListener(new jgm.locale.LocaleListener() {
			public void localeChanged() {
				ChatLogEntry.localeChanged();
			}
		});
	}
	
	public static void localeChanged() {
		Locale.setBase("regex");
		
		try {
			GM_INDICATOR = Locale._("chat.gmindicato");
			WHISPERS_VERB = Locale._("chat.whispers");
			SAY_NOUN = Locale._("chat.says");
			WHISPER_NOUN = Locale._("chat.whisper");
			SAY_NOUN = Locale._("chat.say");
			PATTERN1 = Pattern.compile(Locale._("chat.whispersay"));
			PATTERN2 = Pattern.compile(Locale._("chat.normalchat"));
		} catch (Throwable e) {
			e.printStackTrace();
			
			Locale.setLocale(Locale.LOCALE_ENGLISH);
		}
	}
	
	public static enum Urgency {
		TRIVIAL, URGENT, CRITICAL
	};
		
	private String  channel = null;
	private String  sender  = null;
	private Urgency urgency = Urgency.TRIVIAL;
	private String  message = null;
	
	public ChatLogEntry(String t, String s) {
		super(t, s);
	}
	
	public ChatLogEntry(String s) {
		this("Chat", s);
	}

	public boolean isUrgent() {
		switch (urgency) {
			case URGENT:
			case CRITICAL: return true;
		}
		
		return false;
	}

	public boolean isCritical() {
		switch (urgency) {
			case CRITICAL: return true;
		}
		
		return false;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public String getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}
	
	private static Pattern PATTERN1 = null;
//		Pattern.compile(".*?(<GM>|)\\[([^]]+)\\] (whisper|say)s: (.*)");
		/* group 1: <GM>?
		 *       2: sender
		 *       3: type
		 *       4: message
		 */

	private static Pattern PATTERN2 = null;
//		Pattern.compile(".*?\\[(\\d+\\s*?|)(Guild|Officer|[^]]+)\\] \\[(<GM>|)([^]]+)\\]: (.*)");
		/* group 1: number => public chat channel
		 *       2: channel name (Guild|Office|public channel name)
		 *       3: <GM>?
		 *       4: sender
		 *       5: message
		 */
	
	private static ChatLogEntry parse(String s) {
		ChatLogEntry ret = new ChatLogEntry(s);
		Matcher m = PATTERN1.matcher(s);
		
		if (m.matches()) {	
			//System.out.println("matched pattern1: " + s);
			
			boolean gm     = m.group(1).equals(GM_INDICATOR);
			String sender  = m.group(2);
			String type    = m.group(3);
			//type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
			
			if (type.equals(WHISPERS_VERB)) {
				type = WHISPER_NOUN;
			} else if (type.equals(SAYS_VERB)) {
				type = SAY_NOUN;
			}
			
			String message = m.group(4);
			
			ret.channel = type;
			ret.sender = sender;
			ret.message = message;
			ret.urgency = (gm) ? Urgency.CRITICAL : Urgency.URGENT;
			
			if (gm) {
				ret.type = "GM " + type;
			} else {
				ret.type = type;
			}

		} else {
		
			m = PATTERN2.matcher(s);
			
			if (m.matches()) {
				//System.out.println("matched pattern2: " + s);
							
				ret.channel = m.group(2);
				ret.sender  = m.group(4);
				ret.message = m.group(5);
				
				if (!m.group(1).equals("")) {
					ret.type = "Public Chat";
				} else {
					ret.type = ret.channel;
				}
			}
		}
		
		if (ret.isCritical()) {
			new Sound(Audible.Type.GM, jgm.util.Sound.File.GM_WHISPER).play(true);
			new Phrase(Audible.Type.GM, ret.getText()).play();
		} else if (ret.isUrgent()) {
			Audible.Type t = (ret.type.equals(WHISPER_NOUN)) ? Audible.Type.WHISPER : Audible.Type.SAY;
			new Sound(t, jgm.util.Sound.File.WHISPER).play(true);
			new Phrase(t, ret.getText()).play();
		}
		
		return ret;
	}
	
	public static ChatLogEntry factory(String rawText) {
		return parse(rawText);
	}
}
