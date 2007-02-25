package jgm.sound;

import jgm.util.Speech;

/**
 * Represents a sound that will be generated with TTS.
 * @author Tim
 * @since 0.3
 */
public class Phrase extends Audible {
	private String text;
	
	/**
	 * Construct a phrase.
	 * @param t The type of sound
	 * @param s The text to say
	 */
	public Phrase(Type t, String s) {
		super(t);
		text = s;
	}
	
	protected void createSound(boolean wait) {		
		Speech.speak(text, wait);
	}
	
	public boolean isAudible() {
		if (!cfg.getBool("sound.tts", "enabled")) return false;
		return cfg.getBool("sound.tts", type.toString().toLowerCase());
	}
}
