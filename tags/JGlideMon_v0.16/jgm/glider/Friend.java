/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 - 2008 Tim
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
package jgm.glider;

import java.util.Date;

public class Friend implements Comparable<Friend> {
	public static enum Status {
		ADDED, REMOVED, FOLLOWING;
		
		@Override
		public String toString() {
			String s = super.toString();
			
			return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
		}
	}
	
	public Date   timestamp = new Date();
	public String name;
	public String id;
	public String race;
	public Status status = null;
	public int    encounters = 0;
	public int    followingTimes = 0;
	
	public Friend(String name) {
		this(name, null, null, Status.REMOVED);
	}
	
	public Friend(String name, String id, String race, Status status) {
		this.name = name;
		this.id   = id;
		this.race = race;
		this.status = status;
		
		switch (status) {
			case FOLLOWING:
				followingTimes++;
		}
		
		// this is necessary in case a friend is following
		// or removed before jgm sees an added message
		encounters++;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		
		if (o instanceof Friend) {
			Friend f = (Friend) o;
			return name.equals(f.name) ||
				(id != null && id.equals(f.id));
		}
		
		return false;
	}
	
	public int compareTo(Friend f) {
		return -timestamp.compareTo(f.timestamp);
	}
	
	public void update(Friend f) {
		status = f.status;
		
		if (status == Status.ADDED)
			encounters++;
		
		if (status == Status.FOLLOWING)
			followingTimes++;
		
//		if (status != Status.REMOVING)
		timestamp = f.timestamp;
		
		if (null != id && null != f.id && id.equals(f.id) && name.equals("(unknown)"))
			name = f.name;
		if (null == id && null != f.id)
			id = f.id;
		if (null == race && null != f.race)
			race = f.race;
	}
}
