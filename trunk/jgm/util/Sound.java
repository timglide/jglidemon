package jgm.util;

import java.applet.Applet;
import java.applet.AudioClip;

/**
 * Handles the available sound files that can be played.
 * @author Tim
 */
public class Sound {
	/**
	 * An available sound file.
	 * @author Tim
	 */
	public enum File {
		WHISPER    ("Whisper.wav",   1309),
		GM_WHISPER ("GMWhisper.wav", 3400);
		
		private final AudioClip clip;
		private final int length;
		
		/**
		 * Construct a sound file.
		 * @param name The filename of the sound file
		 * @param len The duration of the sound in ms
		 */
		private File(String name, int len) {
			length = len;
			
			clip = Applet.newAudioClip(
				jgm.JGlideMon.class.getResource("resources/sounds/" + name)
			);
		}
		
		/**
		 * Play the sound.
		 */
		public void play() {
			play(waitForCompletion);
		}
		
		/**
		 * Play the sound
		 * @param wait Whether to wait for the sounnd to finish playing before returning
		 */
		public void play(boolean wait) {
			clip.play();
			
			if (wait) {
				try {
					Thread.sleep(length);
				} catch (InterruptedException e) {}
			}
		}
	};
		
	public static boolean waitForCompletion = true;
	
	private Sound() {}
	
	public static void init() {}
	
	public final static void play(File sound) {		
		long t1, t2;
		
		try {
			t1 = System.currentTimeMillis();
			sound.play(waitForCompletion);
			
			t2 = System.currentTimeMillis();
			
			System.out.println("Time to play sound " + sound + ": " + (t2 - t1) + "ms");
		} catch (Exception e) {
			System.err.println("Error playing sound #" + sound + ": " + e.getMessage());
		}
	}
}
