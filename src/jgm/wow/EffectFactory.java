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

import jgm.util.SaveURL;

import java.net.*;
import java.util.regex.*;
import java.util.logging.*;

public class EffectFactory {
	static Logger log = Logger.getLogger(Item.class.getName());
	
	public static final String EFFECT_URL = 
		"http://wow.allakhazam.com/ihtml?";
	
	private static final String EFFECT_STR = 
		// Equip: <a href="/db/spell.html?wspell=18185" class="itemeffectlink">Increases defense rating by 16.</a>
		"(Chance On Hit|Equip|Use): <a.*?(\\d+).*?>(.*?)</a>.*?";
	private static Pattern EFFECT_PATTERN = null;
	
	static {
		EFFECT_PATTERN = Pattern.compile(
			".*?" + EFFECT_STR /*+ "(?:" + EFFECT_STR + "(?:" + EFFECT_STR + "))"*/
			/* group 1,4,7: type
			 *       2,5,8: id
			 *       3,6,9: text
			 */,
			 Pattern.DOTALL
		);
	}
	
	public static boolean factory(Item item) {
		String page = null;
		
		try {
			page = SaveURL.getURL(new URL(EFFECT_URL + item.id));
		} catch (Exception x) {
			log.warning("Problem retrieving effect: " + x.getMessage());
			return false;
		}
		//System.out.println(page);
		
		Matcher m = EFFECT_PATTERN.matcher(page);
		if (!m.matches()) {
			//System.err.println("No effect match");
			return false;
		}
		
		// TODO Fix regex to find up to 3 effects
		for (int i = 0; i < 1; i++) {
			Effect e = new Effect();
			String s = m.group(1);
			
			if (s != null && !s.equals("")) {
				e.type = Effect.Type.UNKNOWN;
				
				for (Effect.Type t : Effect.Type.values()) {
					if (s.equals(t.toString())) e.type = t;
				}
			} else {
				e.type = Effect.Type.UNKNOWN;				
			}
			
			s = m.group(2);
			
			if (s != null && !s.equals("")) {
				try {
					e.id = Integer.parseInt(s);
				} catch (NumberFormatException x) {}
			}
			
			s = m.group(3);
			if (s != null && !s.equals("")) {
				e.text = s;
			}
			
			item.setEffect(i, e);
			
			//System.out.println("Found " + i + ": " + e.toString());
		}
		
		return true;
	}
}
