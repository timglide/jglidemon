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
package jgm.glider.log;

import jgm.sound.Audible;
import jgm.sound.Sound;

// nothing special
public class GliderLogEntry extends LogEntry {
	public enum Type {
		NORMAL, BEING_ATTACKED, BEING_FOLLOWED,
		LOGGING_OUT, STUCK, DIED
	}
	
	public  Type    type    = Type.NORMAL;
	private boolean isAlert = false;
	
	public GliderLogEntry(String s) {
		super("GliderLog", s);
		
		isAlert = s.startsWith("!");
		
		if (isAlert) {
			if (s.contains("Being attacked")) {
				type = Type.BEING_ATTACKED;
				new Sound(Audible.Type.PVP, jgm.util.Sound.File.BEING_ATTACK).play(true);
			} else if (s.contains("Being followed")) {
				if (s.contains("logging out now")) {
					type = Type.LOGGING_OUT;
					new Sound(Audible.Type.STATUS, jgm.util.Sound.File.STOP).play(true);
				} else {
					type = Type.BEING_FOLLOWED;
					new Sound(Audible.Type.FOLLOW, jgm.util.Sound.File.BEING_FOLLOWED).play(true);
				}
			}
		} else {
			if (s.contains("Stuck too many times")) {
				isAlert = true;
				type = Type.STUCK;
				new Sound(Audible.Type.STUCK, jgm.util.Sound.File.STOP).play(true);
			} else if (s.contains("Died while gliding")) {
				isAlert = true;
				type = Type.DIED;
				new Sound(Audible.Type.STATUS, jgm.util.Sound.File.STOP).play(true);
			}
		}
	}
	
	public boolean isAlert() {
		return isAlert;
	}
}
