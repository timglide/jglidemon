package jgm.glider.log;

import jgm.sound.*;

public class WhisperEntry extends ChatLogEntry {
	public static enum Urgency {
		TRIVIAL, URGENT, CRITICAL
	};
		
	private Urgency urgency = Urgency.TRIVIAL;
	private String message = null;
	
	public String from;
	
	public WhisperEntry(String raw,
						String parsed,
						String from,
						Urgency urgency,
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
	
	public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}
}
