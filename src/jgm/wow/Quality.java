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
package jgm.wow;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public enum Quality {
	POOR     (0, Color.DARK_GRAY,        new Color(0x9d9d9d)),
	COMMON   (1, Color.BLACK,            Color.WHITE),
	UNCOMMON (2, Color.GREEN,            new Color(0x1EFF00)),
	RARE     (3, Color.BLUE,             new Color(0x0070DD)),
	EPIC     (4, new Color(128, 0, 128), new Color(0xa335ee)),
	LEGENDARY(5, Color.ORANGE,           new Color(0xff8000)),
	ARTIFACT (6, Color.RED,              new Color(0xFF0000)),
	HEIRLOOM (7, new Color(0xe6cc80),    new Color(0xe6cc80));
	
	public final int value;
	public final Color darkColor;
	public final Color lightColor;
	public final Border darkBorder;
	public final Border lightBorder;
	
	private Quality(int value, Color darkColor, Color lightColor) {
		this.value = value;
		this.darkColor = darkColor;
		this.lightColor = lightColor;
		darkBorder = BorderFactory.createLineBorder(darkColor);
		lightBorder = BorderFactory.createLineBorder(lightColor);
	}
	
	public String toString() {
		String s = super.toString();
		return s.charAt(0) + s.substring(1).toLowerCase();
	}
	
	public static Quality intToQuality(int i) {
		if (i < 0) i = 0;
		else if (i > 7) i = 7;
		
		return values()[i];
	}
}
