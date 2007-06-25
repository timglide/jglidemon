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

/**
 * Represents a sound file to be played.
 * @author Tim
 */
public class Sound extends Audible {
	private jgm.util.Sound.File sound;
	
	/**
	 * Construct a sound.
	 * @param type The type of sound indication
	 * @param sound The sound file to play
	 */
	public Sound(Type type, jgm.util.Sound.File sound) {
		super(type);
		this.sound = sound;
	}
	
	protected void createSound(boolean wait) {
		sound.play(wait);
	}

	public boolean isAudible() {
		if (!cfg.getBool("sound", "enabled")) return false;
		return cfg.getBool("sound", type.toString().toLowerCase());
	}

}
