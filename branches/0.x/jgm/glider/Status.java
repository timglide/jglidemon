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

public class Status {
	public String version;
	public boolean attached;
	public String mode;
	public String profile;
	public String logMode;
	public double health;
	public double mana;
	public String manaName;
	public String name;
	public jgm.wow.Class  clazz; // class
	public int    level;
	public int    experience;
	public int    nextExperience;
	public int    xpPerHour;
	public int    xpPercent;
	public String location;
	public double heading;
	public int    kills;
	public int    loots;
	public int    deaths;
	public String targetName;
	public int    targetLevel;
	public double targetHealth;
	
	public Status() {
		resetData();
	}
	
	public void resetData() {
		version        = "";
		attached       = false;
		mode           = "Auto";
		profile        = "";
		logMode        = "None";
		health         = 0.0;
		mana           = 0.0;
		manaName       = "";
		name           = "";
		clazz          = jgm.wow.Class.UNKNOWN; // class
		level          = 0;
		experience     = 0;
		nextExperience = 0;
		xpPerHour      = 0;
		xpPercent      = 0;
		location       = "";
		heading        = -1.0;
		kills          = 0;
		loots          = 0;
		deaths         = 0;
		targetName     = "";
		targetLevel    = 0;
		targetHealth   = 0.0;
	}
	
	/**
	 * Determines if the character is at the level cap.
	 * AssumeS the level cap will be 70, 80, 90, etc.
	 * Even if at the supposed level cap, it will become
	 * visible again if xpPercent >= 2 because it can only
	 * get past 1% if you're actually leveling (i.e. when
	 * a new expansion is released).
	 * @return
	 */
	public boolean atLevelCap() {
		return level >= 70 && level % 10 == 0 && xpPercent < 2;
	}
	
	public boolean isAlertLevel() {
		return !attached || level >= jgm.Config.c.getInt("alerts.minlevel");
	}
	
	public Status clone() {
		Status ret = new Status();
		
		ret.version        = version;
		ret.attached       = attached;
		ret.mode           = mode;
		ret.profile        = profile;
		ret.logMode        = logMode;
		ret.health         = health;
		ret.mana           = mana;
		ret.manaName       = manaName;
		ret.name           = name;
		ret.clazz          = clazz; // class
		ret.level          = level;
		ret.experience     = experience;
		ret.nextExperience = nextExperience;
		ret.xpPerHour      = xpPerHour;
		ret.xpPercent      = xpPercent;
		ret.location       = location;
		ret.heading        = heading;
		ret.kills          = kills;
		ret.loots          = loots;
		ret.deaths         = deaths;
		ret.targetName     = targetName;
		ret.targetLevel    = targetLevel;
		ret.targetHealth   = targetHealth;
		
		return ret;
	}
}
