package jgm.sound;

/**
 * Represents a sound file to be played.
 * @author Tim
 */
public class Sound extends Audible {
	private jgm.util.Sound.File sound;
	
	/**
	 * Construct a sound.
	 * @param type The type of sound indication
	 * @param sound The sound file to play
	 */
	public Sound(Type type, jgm.util.Sound.File sound) {
		super(type);
		this.sound = sound;
	}
	
	protected void createSound(boolean wait) {
		sound.play(wait);
	}

	public boolean isAudible() {
		if (!cfg.getBool("sound", "enabled")) return false;
		return cfg.getBool("sound", type.toString().toLowerCase());
	}

}
