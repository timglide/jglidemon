package jgm.sound;

/**
 * Represents something that can be heard.
 * @author Tim
 * @since 0.3
 */
public abstract class Audible {
	/**
	 * Represents the type of an audible indication of
	 * some event.
	 * @author Tim
	 * @since 0.3
	 */
	public enum Type {
		STATUS, WHISPER, SAY, GM
	}
	
	protected Type type;
	
	/**
	 * Construct an Audible object
	 * @param type The type of audible indication
	 */
	public Audible(Type type) {
		this.type = type;
	}
	
	/**
	 * Determine whether the sound should be heard when
	 * played, according to the configuration.
	 * @return Whether the sound should be heard
	 */
	public abstract boolean isAudible();
	
	/**
	 * Play the sound if it should be played according
	 * to the configuration.
	 */
	public void play() {
		if (!isAudible()) return;
		
		createSound();
	}
	
	/**
	 * Actually generate the sound.
	 */
	protected abstract void createSound();
}
