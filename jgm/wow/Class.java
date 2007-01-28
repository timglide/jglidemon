package jgm.wow;

import java.util.*;

/**
 * Represents one of the 9 WoW classes. Includes
 * a ManaType field, which would be mana/rage/energy
 * accordingly.
 * @author Tim
 */
public enum Class {	
	UNKNOWN ("Unknown"),
	WARRIOR ("Warrior", ManaType.RAGE),
	ROGUE   ("Rogue",   ManaType.ENERGY),
	HUNTER  ("Hunter"),
	MAGE    ("Mage"),
	WARLOCK ("Warlock"),
	PRIEST  ("Priest"),
	PALADIN ("Paladin"),
	SHAMAN  ("Shaman"),
	DRUID   ("Druid");
	
	private String name;
	public ManaType mana;
	
	private Class(String name) {
		this(name, ManaType.MANA);
	}
	
	private Class(String name, ManaType mana) {
		this.name = name;
		this.mana = mana;
	}
	
	public boolean isCaster() {
		return mana == ManaType.MANA;
	}
	
	public boolean isMelee() {
		return mana != ManaType.MANA;
	}
	
	public String toString() {
		return name;
	}
	
	private static Map<String, Class> strMap = new HashMap<String, Class>();
	
	static {
		strMap.put("warrior", WARRIOR);
		strMap.put("rogue",   ROGUE);
		strMap.put("hunter",  HUNTER);
		strMap.put("mage",    MAGE);
		strMap.put("warlock", WARLOCK); 
		strMap.put("priest",  PRIEST);
		strMap.put("paladin", PALADIN);
		strMap.put("shaman",  SHAMAN);  
		strMap.put("druid",   DRUID);
	}
	
	public static Class strToClass(String s) {	
		return strMap.get(s.toLowerCase());
	}
}
