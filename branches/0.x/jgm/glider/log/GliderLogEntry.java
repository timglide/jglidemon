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

import jgm.glider.Friend;
import jgm.sound.Audible;
import jgm.sound.Sound;

import java.util.regex.*;

// nothing special
public class GliderLogEntry extends LogEntry {
	public static enum Type {
		NORMAL, BEING_ATTACKED, BEING_FOLLOWED,
		LOGGING_OUT, STUCK, DIED, EXCEPTION,
		INVENTORY_NOT_GOING_UP,
		NEW_FRIEND, REMOVING_FRIEND
	}
	
	public  Type    type    = Type.NORMAL;
	private boolean isAlert = false;
	public  Friend  friend  = null;
	
	static Pattern BEING_FOLLOWED_PATTERN =
		//               !! Being followed for 60 seconds by: Thesleeper (a97633)
		Pattern.compile(".*Being followed for (\\d+) seconds by: (.*?) \\((.*?)\\)");
	static Pattern NEW_FRIEND_PATTERN =
		Pattern.compile(".*New friend: ([^:]+): (.+)$");
	static Pattern REMOVING_FRIEND_PATTERN = 
		Pattern.compile(".*Removing friend: (.+)$");
	
	public GliderLogEntry(String s) {
		super("GliderLog", s);
		
		isAlert = s.startsWith("!") || s.startsWith("*");
		
		if (isAlert) {
			if (s.contains("Being attacked")) {
				type = Type.BEING_ATTACKED;
				new Sound(Audible.Type.PVP, jgm.util.Sound.File.BEING_ATTACK).play(true);
			} else if (s.contains("Being followed")) {
				if (s.contains("logging out now")) {
					type = Type.LOGGING_OUT;
					new Sound(Audible.Type.STATUS, jgm.util.Sound.File.STOP).play(true);
				} else {
					Matcher m = BEING_FOLLOWED_PATTERN.matcher(s);
					
					if (m.matches()) { // it should
						friend = new Friend(m.group(2), m.group(3), null, Friend.Status.FOLLOWING);
					} else {
						log.finer("Didn't match BEING_FOLLOWED but should have");
						log.finer("Line: " + s);
					}
					
					type = Type.BEING_FOLLOWED;
					new Sound(Audible.Type.FOLLOW, jgm.util.Sound.File.BEING_FOLLOWED).play(true);
				}
			}
		} else { // some things we know won't start with ! or *
			Matcher m = NEW_FRIEND_PATTERN.matcher(s);
			
			if (m.matches()) {
				type = Type.NEW_FRIEND;
				friend = new Friend(m.group(1), null, m.group(2), Friend.Status.ADDING);
			} else {
				m = REMOVING_FRIEND_PATTERN.matcher(s);
				
				if (m.matches()) {
					type = Type.REMOVING_FRIEND;
					friend = new Friend(m.group(1));
				}
			}
		}
		
		if (s.contains("Died while gliding")) {
			isAlert = true;
			type = Type.DIED;
			new Sound(Audible.Type.STATUS, jgm.util.Sound.File.STOP).play(true);
		} else if (s.contains("Exception")) {
			type = Type.EXCEPTION;
			
			if (!s.contains("being used by another process")) { // so annoying...
				isAlert = false; // not as helpful to make it an alert as i thought
//				new Sound(Audible.Type.STATUS, jgm.util.Sound.File.STOP).play(true);
			}
		} else if (s.contains("Stuck too many times") && !s.contains("MoveToMonster")) {
			isAlert = true;
			type = Type.STUCK;
			new Sound(Audible.Type.STUCK, jgm.util.Sound.File.STOP).play(true);
		} else if (s.contains("Inventory doesn't seem to be going up")) {
			type = Type.INVENTORY_NOT_GOING_UP;
		}
	}
	
	public boolean isAlert() {
		return isAlert;
	}
}
