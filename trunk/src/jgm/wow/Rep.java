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
public class Rep {
	// for backwards compat/not having to rename everything
	public Date timestampDate = null;
	public String timestamp = "";
	public String faction = "";
	public int amount = 0;
	
	public Rep(Date timestampDate, String faction, int amount) {
		this.timestampDate = timestampDate;
		this.timestamp = RawChatLogEntry.getFormattedTimestamp(timestampDate);
		this.faction = faction;
		this.amount = amount;
	}
	
	public void incr(Rep rep) {
		this.timestampDate = rep.timestampDate;
		this.timestamp = rep.timestamp;
		this.amount += rep.amount;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Rep)) return false;
		
		return ((Rep) o).faction.equals(this.faction);
	}
	
	public static java.util.Comparator<Rep> getAmountComparator() {
		return getAmountComparator(-1);
	}
	
	public static java.util.Comparator<Rep> getAmountComparator(final int sort) {
		return new java.util.Comparator<Rep>() {
			public int compare(Rep i1, Rep i2) {
				int ret = 0;
				if (i1.amount < i2.amount) ret = -1; else
				if (i1.amount > i2.amount) ret = 1; 

				if (ret == 0) {
					return i1.faction.compareTo(i2.faction);
				}

				return sort * ret;
			}
		};
	}
}
