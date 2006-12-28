package jgm.glider.log;

import jgm.Sound;

public class WhisperEntry extends ChatLogEntry {
	public static final int TRIVIAL = 0;
	public static final int URGENT = 1;
	public static final int CRITICAL = 2;
	
	private int    urgency = TRIVIAL;
	private String message = null;
	
	public String from;
	
	public WhisperEntry(String raw,
						String parsed,
						String from,
						int    urgency,
						String type) {
		super(type, raw);
		
		this.message = parsed;
		this.from = from;
		this.urgency = urgency;
		
		if (isCritical()) {
			Sound.playSound(Sound.GM_WHISPER);
		} else if (isUrgent()) {
			Sound.playSound(Sound.WHISPER);
		}
	}
	
	public boolean isUrgent() {
		return urgency >= URGENT;
	}

	public boolean isCritical() {
		return urgency >= CRITICAL;
	}
	
	public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}
}
