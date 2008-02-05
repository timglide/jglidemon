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

import jgm.Config;

/**
 * Represents something that can be heard.
 * @author Tim
 * @since 0.3
 */
public abstract class Audible {
	/**
	 * Master control for making sounds. Will
	 * be used for parsing log files to not
	 * make the usual noises.
	 */
	public static boolean ENABLE_SOUNDS = true;
	
	/**
	 * Represents the type of an audible indication of
	 * some event.
	 * @author Tim
	 * @since 0.3
	 */
	public enum Type {
		STATUS, WHISPER, SAY, GM, FOLLOW, PVP, STUCK
	}
	
	protected Type type;
	protected static Config cfg;
	
	/**
	 * Construct an Audible object
	 * @param type The type of audible indication
	 */
	public Audible(Type type) {
		this.type = type;
		if (cfg == null) cfg = jgm.Config.getInstance();
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
		if (!(ENABLE_SOUNDS && isAudible())) return;
		
		createSound(wait);
	}
}
