/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
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
		WHISPER        ("Whisper.wav",      1309),
		GM_WHISPER     ("GMWhisper.wav",    3400),
		BEING_ATTACK   ("PlayerAttack.wav", 295),
		BEING_FOLLOWED ("PlayerNear.wav",   484),
		STOP           ("GlideStop.wav",    1040);
		
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
