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

/**
 * 
 * @author Tim
 * @since 0.13
 */

public enum ChatType {
	WHISPER("/w "),
	REPLY("/r "),
	SAY("/s "),
	GUILD("/g "),
	RAW("");
	
	private String slashCmd;
	
	ChatType(String slashCmd) {
		this.slashCmd = slashCmd;
	}
	
	public String getSlashCommand() {
		return slashCmd;
	}
	
	public String toString() {
		String s = super.toString();
		s = s.substring(0, 1) + s.substring(1).toLowerCase();
		
		return s;
	}
}
