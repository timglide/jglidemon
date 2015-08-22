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
package jgm.wow;

import java.awt.Color;
import java.util.Date;

/**
 * 
 * @author Tim
 * @since 0.10
 */
public class Mob {
	public String timestamp;
	public int number = 0;
	public int xp = 0;
	public String name = "UNKNOWN";
	public Date firstSeen;
	public Date lastSeen;
	
	public Mob(String name) {
		this("", name, 0, 0);
	}
	
	public Mob(String timestamp, String name, int number, int xp) {
		this.timestamp = timestamp;
		this.name = name;
		this.number = number;
		this.xp = xp;
		firstSeen = new Date();
		lastSeen = new Date(firstSeen.getTime());
	}
	
	public void incr(Mob mob) {
		this.xp += mob.xp;
		this.xp /= 2;
		this.number++;
		lastSeen.setTime(System.currentTimeMillis());
	}
	
	public double getNumberPerHour()
	{
		if (0 == number)
			return 0;
		
		long now = System.currentTimeMillis();
		long diff = now - firstSeen.getTime();
		
		if (0L == diff)
			return 0;
		
		return (double) number / ((double) diff / 3600000L);
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Mob)) return false;
		
		return ((Mob) o).name.equals(this.name);
	}
	
	public static java.util.Comparator<Mob> getQuantityComparator() {
		return getNumberComparator(-1);
	}
	
	public static java.util.Comparator<Mob> getNumberComparator(final int sort) {
		return new java.util.Comparator<Mob>() {
			public int compare(Mob i1, Mob i2) {
				int ret = 0;
				if (i1.number < i2.number) ret = -1; else
				if (i1.number > i2.number) ret = 1; 

				if (ret == 0) {
					if ("Total".equals(i1.name)) { 
						return -1;
					}
					if ("Total".equals(i2.name)) {
						return 1;
					}
					
					return i1.name.compareTo(i2.name);
				}

				return sort * ret;
			}
		};
	}
	
	
	/**
	 * Returns the appropriate color for a mob depending on
	 * a player's level.
	 * @param charLevel
	 * @param mobLevel
	 * @return
	 */
	public static Color getMobColor(int charLevel, int mobLevel) {
		// see http://www.wowwiki.com/Formulas:Mob_XP
		
		int diff = mobLevel - charLevel;
		int grayLevel = 0;
		
		if (6 <= charLevel && charLevel <= 39) {
			grayLevel = charLevel - 5 - (charLevel / 10);
		} else if (40 <= charLevel && charLevel <= 59) {
			grayLevel = charLevel - 1 - (charLevel / 5);
		} else if (60 <= charLevel /*&& charLevel <= 70*/) {
			// charLevel <= 70 is commented in the exceedingly
			// remote chance the level cap is raised, of course
			// by that time they may change the formula.......
			grayLevel = charLevel - 9;
		}
		
		if (diff >= 5) // 5+ higher
			return Color.RED;
		if (diff >= 3) // 3-4 higher
			return Color.ORANGE;
		if (diff >= -2) // 2 lower to 2 higher
			return Color.YELLOW;
		
		if (mobLevel > grayLevel)
			return Color.GREEN;
		
		return Color.DARK_GRAY;
	}
}
