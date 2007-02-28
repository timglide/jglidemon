package jgm.glider.log;

import jgm.sound.Audible;
import jgm.sound.Sound;

// nothing special
public class GliderLogEntry extends LogEntry {
	public enum Type {
		NORMAL, BEING_ATTACKED, BEING_FOLLOWED, STUCK
	}
	
	private Type type = Type.NORMAL;
	private boolean isAlert = false;
	
	public GliderLogEntry(String s) {
		super("GliderLog", s);
		
		isAlert = s.startsWith("!");
		
		if (isAlert) {
			if (s.contains("Being attacked")) {
				type = Type.BEING_ATTACKED;
				new Sound(Audible.Type.PVP, jgm.util.Sound.File.BEING_ATTACK).play(true);
			} else if (s.contains("Being followed")) {
				type = Type.BEING_FOLLOWED;
				new Sound(Audible.Type.FOLLOW, jgm.util.Sound.File.BEING_FOLLOWED).play(true);
			}
		} else {
			if (s.contains("Stuck too many times")) {
				isAlert = true;
				type = Type.STUCK;
				new Sound(Audible.Type.STUCK, jgm.util.Sound.File.STOP).play(true);
			}
		}
	}
	
	public boolean isAlert() {
		return isAlert;
	}
}
