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

import java.util.regex.*;

/**
 * Represents one of the 3 "mana" types, either
 * mana, rage, or energy. 
 * @author Tim
 * @since 0.4
 */
public enum ManaType {
	MANA   ("Mana"), 
	RAGE   ("Rage"),
	ENERGY ("Energy"),
	DRUID  ("Mixed");

	private static final Pattern caster = 
		// find number in parantheses with a %
		// (##%)
		Pattern.compile(".*\\((\\d+)%\\).*");
	private static final Pattern melee  =
		// find the first number
		// ##
		Pattern.compile(".*?(\\d+).*");
	private static final Pattern druid =
		// find rage, energy, or mana, in that order
		// R = ##, E=##, (##%)
		Pattern.compile("(?:.*?R = (\\d+).*|.*?E=(\\d+).*|.*\\((\\d+)%\\).*)");
		
	private String type;
	
	private ManaType(String type) {
		this.type = type;
	}
	
	/**
	 * @return The appropriate pattern to match this mana type
	 */
	public Pattern getRegex() {
		switch (this) {
			case MANA:  return caster;
			case DRUID: return druid;
			default:    return melee;
		}
	}
	
	/**
	 * Needed because of druids...
	 * @return
	 * @since 0.10
	 */
	public int numRegexGroups() {
		switch (this) {
			case DRUID: return 3;
			default:    return 1;
		}
	}
	
	public String toString() {
		return type;
	}
	
	/**
	 * Return the correct word for this "mana" type
	 * based on which captured group of the regex
	 * was matched. The only reason for this is because
	 * of druids....
	 * @param regexGroup
	 * @return
	 * @since 0.10
	 */
	public String toString(int regexGroup) {
		switch (this) {
			case DRUID:
				switch (regexGroup) {
					case 1:  return "Rage";
					case 2:  return "Energy";
					default: return "Mana";
				}
			
			default: return toString();
		}
	}
}
