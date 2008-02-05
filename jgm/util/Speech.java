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

import com.sun.speech.freetts.*;
import java.util.*;
import java.util.logging.*;

/**
 * Handle the generation ot text-to-speech. This
 * class has a queue of phrases to be spoken and
 * a thread to ensure they are all heard in order.
 * 
 * @author Tim
 * @since 0.3
 */
public class Speech implements Runnable {
	static Logger log = Logger.getLogger(Speech.class.getName());
	
	public static boolean destroyWhenEmpty = false;
	public static boolean printTime = false;

	private static Speech instance = null;
	private static Voice voice = null;
	private static Queue<String> toSay = new LinkedList<String>();
		
	public static volatile boolean stop = false;

	private Thread thread = null;
	
	private Speech() {
		if (voice == null) return;
		
		thread = new Thread(this, "TTS");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * Determines if speech may be spoken at the current time
	 * @return Whether speech can be spoken currently
	 */
	public static boolean isSupported() {
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
		VoiceManager voiceManager = null;
		
		// allow the TTS libraries to be optional
		try {
			Class.forName("com.sun.speech.freetts.VoiceManager");
			voiceManager = VoiceManager.getInstance();
		} catch (Throwable e) {
			voice = null;
			instance = null;
			
			log.warning("Unable to load TTS, libraries not available");
			return;
		}
		
        voice = voiceManager.getVoice(v);
        
        if (voice == null) {
			voice = null;
			instance = null;
			
        	log.warning("Unable to load TTS");
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
		speak(phrase, false);
	}
	
	/**
	 * Enqueue a phrase to be spoken.
	 * @param phrase The phrase to be spoken
	 * @param wait Whether to block the current thread until the phrase is spoken
	 */
	public static void speak(String phrase, boolean wait) {
		if (voice == null)
			return;
		
		log.fine("Queuing \"" + phrase + "\" for TTS");
		toSay.add(phrase);
		
		if (instance.idle)
			instance.thread.interrupt();
		
		if (wait) {
			while (toSay.contains(phrase)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	/**
	 * Actually synthesize the phrase.
	 * @param phrase The phrase to synthesize
	 */
	private static void speakImpl(String phrase) {		
		long t1, t2;
		
		try {
			t1 = System.currentTimeMillis();
			voice.speak(phrase);
			t2 = System.currentTimeMillis();
		
			if (printTime) 
				log.fine("Time to say \"" + phrase + "\": " + (t2 - t1) + "ms");
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Error during TTS", e);
		}
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
    		
    		// speak each item in the queue removing it
    		// once it has been spoken
    		while (!stop && null != (s = toSay.peek())) {
    			speakImpl(s);
    			toSay.poll();
    		}
    		
    		idle = true;
    	}
    }
}
