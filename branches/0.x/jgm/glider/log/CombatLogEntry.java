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

import java.util.regex.*;

import jgm.gui.updaters.StatusUpdater;

// nothing special
public class CombatLogEntry extends LogEntry {
	public String killedMob = null;
	public int xp = 0;
	
	public CombatLogEntry(String s) {
		super("Combat", s);
		parseMob();
	}
	
	public boolean hasMob() {
		return killedMob != null;
	}
	
	public String getMobName() {
		return killedMob;
	}
	
	public int getMobXP() {
		return xp;
	}
	
	private static Pattern KILLED_MOB_PATTERN = 
		Pattern.compile("(.+) dies, you gain (\\d+) experience.(?: \\(\\+\\d+ exp Rested bonus\\))?");
	
	private static Pattern SLAIN_MOB_PATTERN =
		Pattern.compile("You have slain (.+)!");
	
	private void parseMob() {
		Matcher m = KILLED_MOB_PATTERN.matcher(rawText);
		
		if (m.matches()) {
			killedMob = m.group(1);
			
			try {
				xp = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {}
		} else {
			// only check the slain pattern if at the 
			// level cap
			
			if (StatusUpdater.instance.s.atLevelCap()) {
				m = SLAIN_MOB_PATTERN.matcher(rawText);
				
				if (m.matches()) {
					killedMob = m.group(1);
					xp = 0;
				}
			}
		}
	}
}
