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

import java.util.*;

/**
 * Represents one of the 11 WoW classes. Includes
 * a ManaType field, which would be mana/rage/energy
 * accordingly.
 * @author Tim
 * @since 0.4
 */
public enum Class {	
	UNKNOWN ("Unknown"),
	WARRIOR ("Warrior",      ManaType.RAGE),
	ROGUE   ("Rogue",        ManaType.ENERGY),
	HUNTER  ("Hunter"),
	MAGE    ("Mage"),
	WARLOCK ("Warlock"),
	PRIEST  ("Priest"),
	PALADIN ("Paladin"),
	SHAMAN  ("Shaman"),
	DRUID   ("Druid",        ManaType.DRUID),
	DEATHKNIGHT
	        ("Death Knight", ManaType.RUNE),
	MONK    ("Monk",         ManaType.DRUID);
	
	private String name;
	public final ManaType mana;
	
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
	
	// i guess druids count as melee, as far as
	// the way glider plays them anyway...
	public boolean isMelee() {
		return mana != ManaType.MANA;
	}
	
	public String toString() {
		return name;
	}
	
	private static Map<String, Class> strMap = new HashMap<String, Class>();
	
	static {
		for (Class c : Class.values())
			strMap.put(c.name().toLowerCase(), c);
	}
	
	public static Class strToClass(String s) {	
		Class c = strMap.get(s.toLowerCase());
		return null == c ? UNKNOWN : c;
	}
}
