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
		// find the number in parantheses with a % or the first number
		// (##%) or ##
		Pattern.compile("(?:.*\\((\\d+)%\\).*|.*?(\\d+).*)");
		
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
	
	public String toString() {
		return type;
	}
}
