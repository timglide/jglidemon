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
package jgm.glider.log;

import jgm.sound.*;

import java.util.*;
import java.util.regex.*;

public class ChatLogEntry extends RawChatLogEntry {	
	static Map<String, String> COLOR_MAP = new HashMap<String, String>();
	
	static {
		COLOR_MAP.put("Whisper", "#FF80FF");
		COLOR_MAP.put("Yell", "#FF4040");
		COLOR_MAP.put("Guild", "#40FF40");
		COLOR_MAP.put("Public Chat", "#FFC0C0");
	}
	
	public static enum Urgency {
		TRIVIAL, URGENT, CRITICAL
	};
		
	private String  channel = null;
	
	// if you sent a whisper this is actually the reciever
	private String  sender  = null;
	
	// this is only relevant for whisper/say/yell
	public boolean fromPlayer = true;
	
	private Urgency urgency = Urgency.TRIVIAL;
	private String  message = null;
	
	public ChatLogEntry(String t, String s) {
		super(t, s);
	}
	
	public ChatLogEntry(String s) {
		this("Chat", s);
	}

	public boolean isUrgent() {
		switch (urgency) {
			case URGENT:
			case CRITICAL: return true;
		}
		
		return false;
	}

	public boolean isCritical() {
		switch (urgency) {
			case CRITICAL: return true;
		}
		
		return false;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public String getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public boolean supportsHtmlText() {
		return true;
	}
	
	@Override
	public String getHtmlPreColor() {
		String ret = COLOR_MAP.get(this.type);
		
		return null == ret ? super.getHtmlPreColor() : ret;
	}
	
	private static Pattern PATTERN1 =
		Pattern.compile(".*?(<GM>|<Away>|<Busy>|)(\\[?)([^]]+)\\]? (whisper|say|yell)s: (.*)");
		/* group 1: <GM>?
		 *       2: [, to determine if player or npc
		 *       3: sender
		 *       4: type
		 *       5: message
		 */

	private static Pattern PATTERN2 =
		Pattern.compile(".*?\\[(\\d+\\.?\\s*?|)(Guild|Officer|[^]]+)\\]\\s*(<GM>|<Away>|<Busy>|)\\[([^]]+)\\]: (.*)");
		/* group 1: number => public chat channel
		 *       2: channel name (Guild|Office|public channel name)
		 *       3: <GM>?, doubt it
		 *       4: sender
		 *       5: message
		 */
	
	// for when you send a whisper to someone else
	private static Pattern PATTERN3 =
		Pattern.compile(".*?To \\[([^]]+)\\]: (.*)");
	
	private static ChatLogEntry parse(String s) {
		ChatLogEntry ret = new ChatLogEntry(s);
		s = ret.getText();
		Matcher m = PATTERN1.matcher(s);
//		System.out.println("Matching: " + s);
		
		if (m.matches()) {
//			System.out.println("matched pattern1: " + s);
			
			boolean gm     = m.group(1).equals("<GM>");
			ret.fromPlayer = m.group(2).equals("[");
			ret.sender  = m.group(3);
			ret.type    = m.group(4);
			ret.type = Character.toUpperCase(ret.type.charAt(0)) + ret.type.substring(1);
			ret.message = m.group(5);

			ret.channel = ret.type;
			ret.urgency =
				!ret.fromPlayer || ret.type.equals("Yell") 
				? Urgency.TRIVIAL
				: gm
				  ? Urgency.CRITICAL
				  : Urgency.URGENT;
			
			if (gm) {
				ret.type = "GM " + ret.type;
			}

		} else {
		
			m = PATTERN2.matcher(s);
			
			if (m.matches()) {
//				System.out.println("matched pattern2: " + s);
				
				ret.channel = m.group(2);
				ret.sender  = m.group(4);
				ret.message = m.group(5);
				
				if (!m.group(1).equals("")) {
					ret.type = "Public Chat";
				} else {
					ret.type = ret.channel;
				}
			} else {
				
				m = PATTERN3.matcher(s);
				
				if (m.matches()) {
					ret.type = "Whisper";
					ret.channel = "Whisper";
					ret.sender = m.group(1);
					ret.message = m.group(2);
				}
			}
		}
		
		if (ret.isCritical()) {
			new Sound(Audible.Type.GM, jgm.util.Sound.File.GM_WHISPER).play(true);
			new Phrase(Audible.Type.GM, ret.getRawText()).play();
		} else if (ret.isUrgent()) {
			Audible.Type t = (ret.type.equals("Whisper")) ? Audible.Type.WHISPER : Audible.Type.SAY;
			new Sound(t, jgm.util.Sound.File.WHISPER).play(true);
			new Phrase(t, ret.getRawText()).play();
		}
		
		return ret;
	}
	

	
	public static ChatLogEntry factory(String rawText) {
		return parse(rawText);
	}
}
