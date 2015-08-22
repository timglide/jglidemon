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

import java.util.Date;

import jgm.glider.log.RawChatLogEntry;

/**
 * 
 * @author Tim
 * @since 0.10
 */
public class Skill {
	// for backwards compat/not having to rename everything
	public Date timestampDate = null;
	public String timestamp = "";
	public String name = "";
	public int level = 0;
	public int initialLevel;
	public Date firstSeen;
	public Date lastSeen;
	
	public Skill(Date timestampDate, String name, int level) {
		this.timestampDate = timestampDate;
		this.timestamp = RawChatLogEntry.getFormattedTimestamp(timestampDate);
		this.name = name;
		this.level = level;
		initialLevel = level;
		firstSeen = new Date();
		lastSeen = new Date(firstSeen.getTime());
	}
	
	public void incr(Skill s) {
		this.timestampDate = s.timestampDate;
		this.timestamp = s.timestamp;
		this.level = s.level;
		lastSeen.setTime(System.currentTimeMillis());
	}
	
	public double getSkillPerHour()
	{
		if (0 == level)
			return 0;
		
		long now = System.currentTimeMillis();
		long diff = now - firstSeen.getTime();
		
		if (0L == diff)
			return 0;
		
		return (double) (level - initialLevel) / ((double) diff / 3600000L);
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Skill)) return false;
		
		return ((Skill) o).name.equals(this.name);
	}
	
	public static java.util.Comparator<Skill> getLevelComparator() {
		return getLevelComparator(-1);
	}
	
	public static java.util.Comparator<Skill> getLevelComparator(final int sort) {
		return new java.util.Comparator<Skill>() {
			public int compare(Skill i1, Skill i2) {
				int ret = 0;
				if (i1.level < i2.level) ret = -1; else
				if (i1.level > i2.level) ret = 1; 

				if (ret == 0) {
					return i1.name.compareTo(i2.name);
				}

				return sort * ret;
			}
		};
	}
}
