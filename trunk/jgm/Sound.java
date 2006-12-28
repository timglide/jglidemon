package jgm;

import java.applet.Applet;
import java.applet.AudioClip;;

public class Sound {
	public static final int WHISPER = 0;
	public static final int GM_WHISPER = 1;
	
	private static AudioClip[] clips = new AudioClip[2];
	
	private Sound() {}
	
	public static void init() {
		clips[WHISPER] = Applet.newAudioClip(
			JGlideMon.class.getResource("resources/sounds/Whisper.wav")
		);
		
		clips[GM_WHISPER] = Applet.newAudioClip(
			JGlideMon.class.getResource("resources/sounds/GMWhisper.wav")
		);
	}
		
	public final static void playSound(int sound) {
		try {
			clips[sound].play();
		} catch (Exception e) {
			System.err.println("Error playing sound #" + sound + ": " + e.getMessage());
		}
	}
}
