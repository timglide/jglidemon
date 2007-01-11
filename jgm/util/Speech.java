package jgm.util;

import com.sun.speech.freetts.*;
import java.util.*;

/**
 * Handle the generation ot text-to-speech. This
 * class has a queue of phrases to be spoken and
 * a thread to ensure they are all heard in order.
 * 
 * @author Tim
 * @since 0.3
 */
public class Speech implements Runnable {
	public static boolean destroyWhenEmpty = false;
	public static boolean printTime = false;
	
	private static Voice voice;
	private static Queue<String> toSay = new LinkedList<String>();
	private static Speech instance;
	
	private Thread thread;
	
	public static volatile boolean stop = false;
	
	private Speech() {
		if (voice == null) return;
		
		thread = new Thread(this, "TTS");
		thread.start();
	}
	
	/**
	 * Determines if speech may be spoken at the current time
	 * @return Whether speech can be spoken currently
	 */
	public static boolean ready() {
		return (voice != null && instance != null && instance.thread != null);
	}
	
	/**
	 * Initialize the TTS system with the default
	 * voice (kevin16).
	 */
	public static void init() {
		init("kevin16");
	}
	
	/**
	 * Initialize the TTS system with the given voice.
	 * @param v The voice name to use
	 */
	public static void init(String v) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(v);
        
        if (voice == null) {
        	System.err.println("Unable to load TTS");
        	return;
        }

   		voice.allocate();
   		
		instance = new Speech();
	}
	
	public static void destroy() {
		if (voice == null) return;
		
		stop = true;
		
		if (!destroyWhenEmpty)
			instance.thread.interrupt();
		
		voice.deallocate();
	}
	
	/**
	 * Enqueue a phrase to be spoken.
	 * @param phrase The phrase to be spoken
	 */
	public static void speak(String phrase) {
		if (voice == null)
			return;
		
		System.out.println("Queuing \"" + phrase + "\" for TTS");
		toSay.add(phrase);
		
		if (instance.idle)
			instance.thread.interrupt();
	}
	
	/**
	 * Actually synthesize the phrase.
	 * @param phrase The phrase to synthesize
	 */
	private static void speakImpl(String phrase) {		
		long t1, t2;
		
		t1 = System.currentTimeMillis();
		voice.speak(phrase);
		t2 = System.currentTimeMillis();
		
		if (printTime) 
			System.out.println("Time to say \"" + phrase + "\": " + (t2 - t1) + "ms");
	}
	
	/**
	 * Prints a list of available voices.
	 */
    public static void listAllVoices() {
        System.out.println("All voices available:");        
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (int i = 0; i < voices.length; i++) {
            System.out.println("    " + voices[i].getName()
                               + " (" + voices[i].getDomain() + " domain)");
        }
    }
    
    private volatile boolean idle = false;
    
    public void run() {
    	while (!stop) {
    		if (toSay.isEmpty()) {
    			if (destroyWhenEmpty) {
    				destroy();
    				return;
    			}
    			
    			try {
    				idle = true;
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				idle = false;
    				Thread.interrupted();
    				continue;
    			}
    		}

			idle = false;
    		
    		String s = null;
    		
    		while (!stop && null != (s = toSay.poll())) {
    			speakImpl(s);
    		}
    		
    		idle = true;
    	}
    }
}
