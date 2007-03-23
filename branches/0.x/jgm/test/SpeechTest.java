package jgm.test;

import jgm.util.Speech;

/**
 * Tests the text-to-speech system.
 * @author Tim
 * @since 0.3
 */
public class SpeechTest {
	public static void main(String[] args) {
		Speech.init();
		Speech.listAllVoices();
		Speech.printTime = true;
		
		Speech.speak("Hello world, this is a test.");
		Speech.speak("This is another test.");
		
		Speech.destroyWhenEmpty = true;
	}
}
