package jgm.glider.log;

import jgm.sound.*;

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
			new Sound(Audible.Type.GM, jgm.util.Sound.File.GM_WHISPER).play();
			new Phrase(Audible.Type.GM, parsed).play();
		} else if (isUrgent()) {
			Audible.Type t = (type.equals("Whisper")) ? Audible.Type.WHISPER : Audible.Type.SAY;
			
			new Sound(t, jgm.util.Sound.File.WHISPER).play();
			new Phrase(t, getText()).play();
		} else {
			new Phrase(Audible.Type.STATUS, raw).play();
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
