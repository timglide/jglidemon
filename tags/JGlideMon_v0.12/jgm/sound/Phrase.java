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
