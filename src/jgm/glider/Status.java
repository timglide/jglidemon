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
	

	public String goalText;
	public String statusText;
	public long copper;
	public double timeToLevel; // in seconds
	public boolean targetIsPlayer;
	public int honorGained;
	public int honorPerHour;
	public int bgsWon;
	public int bgsLost;
	public int bgsCompleted;
	public int bgsWonPerHour;
	public int bgsLostPerHour;
	public int bgsPerHour;
	public int killsPerHour;
	public int lootsPerHour;
	public int deathsPerHour;
	public int nodes;
	public int nodesPerHour;
	public int solves;
	public int solvesPerHour;
	public int fish;
	public int fishPerHour;
	
	public boolean running;
	public String accountName;
	public String realm;
	public String map;
	public int mapId;
	public String internalMapName;
	public String zone;
	public int zoneId;
	public String realZone;
	public String subZone;
	
	public Status() {
		resetData();
	}
	
	public void resetData() {
		version        = "";
		attached       = false;
		mode           = "Unknown";
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
		

		goalText = "";
		statusText = "";
		copper = 0L;
		timeToLevel = Double.POSITIVE_INFINITY; // in seconds
		targetIsPlayer = false;
		honorGained = 0;
		honorPerHour = 0;
		bgsWon = 0;
		bgsLost = 0;
		bgsCompleted = 0;
		bgsWonPerHour = 0;
		bgsLostPerHour = 0;
		bgsPerHour = 0;
		killsPerHour = 0;
		lootsPerHour = 0;
		deathsPerHour = 0;
		nodes = 0;
		nodesPerHour = 0;
		solves = 0;
		solvesPerHour = 0;
		fish = 0;
		fishPerHour = 0;
		
		running = false;
		accountName = "";
		realm = "";
		map = "";
		mapId = -1;
		internalMapName = "";
		zone = "";
		zoneId = -1;
		realZone = "";
		subZone = "";
	}
	
	/**
	 * Determines if the character is at the level cap.
	 * Assumes the level cap will be 60, 70, 80, 85, 90, 95, ...
	 * Even if at the supposed level cap, it will become
	 * visible again if xpPercent >= 2 because it can only
	 * get past 1% if you're actually leveling (i.e. when
	 * a new expansion is released).
	 * @return
	 */
	public boolean atLevelCap() {
		return
		((level >= 85 && level % 5 == 0) ||
		 (level >= 60 && level % 10 == 0)) &&
		xpPercent < 2;
	}
	
	public boolean isAlertLevel() {
		return !attached || level >= jgm.Config.c.getInt("alerts.minlevel");
	}
	
	public Status clone() {
		Status ret = null;
		
		try {
			ret = (Status) super.clone();
		} catch (CloneNotSupportedException e) {
			throw (InternalError) new InternalError().initCause(e);
		}
		
		return ret;
	}
}
