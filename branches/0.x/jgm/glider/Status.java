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
