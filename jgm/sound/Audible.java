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
	 * Actually generate the sound.
	 * @param wait Whether to block the current thread until the sound finishes playing
	 */
	protected abstract void createSound(boolean wait);
	
	/**
	 * Play the sound if it should be played according
	 * to the coniguration.
	 */
	public final void play() {
		play(false);
	}
	
	/**
	 * Play the sound if it should be played according
	 * to the configuration and potentially block the current thread
	 * until the sound finishes playing.
	 * @param wait Whether to block the current thread until the sound has finished playing
	 */
	public final void play(boolean wait) {
		if (!isAudible()) return;
		
		createSound(wait);
	}
}
