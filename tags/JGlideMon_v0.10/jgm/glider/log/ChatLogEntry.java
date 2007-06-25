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

import java.util.regex.*;

public class ChatLogEntry extends LogEntry {	
	public static enum Urgency {
		TRIVIAL, URGENT, CRITICAL
	};
		
	private String  channel = null;
	private String  sender  = null;
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
	
	private static Pattern PATTERN1 =
		Pattern.compile(".*?(<GM>|)\\[([^]]+)\\] (whisper|say)s: (.*)");
		/* group 1: <GM>?
		 *       2: sender
		 *       3: type
		 *       4: message
		 */

	private static Pattern PATTERN2 =
		Pattern.compile(".*?\\[(\\d+\\s*?|)(Guild|Officer|[^]]+)\\] \\[(<GM>|)([^]]+)\\]: (.*)");
		/* group 1: number => public chat channel
		 *       2: channel name (Guild|Office|public channel name)
		 *       3: <GM>?
		 *       4: sender
		 *       5: message
		 */
	
	private static ChatLogEntry parse(String s) {
		ChatLogEntry ret = new ChatLogEntry(s);
		Matcher m = PATTERN1.matcher(s);
		
		if (m.matches()) {	
			//System.out.println("matched pattern1: " + s);
			
			boolean gm     = m.group(1).equals("<GM>");
			String sender  = m.group(2);
			String type    = m.group(3);
			type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
			String message = m.group(4);
			
			ret.channel = type;
			ret.sender = sender;
			ret.message = message;
			ret.urgency = (gm) ? Urgency.CRITICAL : Urgency.URGENT;
			
			if (gm) {
				ret.type = "GM " + type;
			} else {
				ret.type = type;
			}

		} else {
		
			m = PATTERN2.matcher(s);
			
			if (m.matches()) {
				//System.out.println("matched pattern2: " + s);
							
				ret.channel = m.group(2);
				ret.sender  = m.group(4);
				ret.message = m.group(5);
				
				if (!m.group(1).equals("")) {
					ret.type = "Public Chat";
				} else {
					ret.type = ret.channel;
				}
			}
		}
		
		if (ret.isCritical()) {
			new Sound(Audible.Type.GM, jgm.util.Sound.File.GM_WHISPER).play(true);
			new Phrase(Audible.Type.GM, ret.getText()).play();
		} else if (ret.isUrgent()) {
			Audible.Type t = (ret.type.equals("Whisper")) ? Audible.Type.WHISPER : Audible.Type.SAY;
			new Sound(t, jgm.util.Sound.File.WHISPER).play(true);
			new Phrase(t, ret.getText()).play();
		}
		
		return ret;
	}
	
	public static ChatLogEntry factory(String rawText) {
		return parse(rawText);
	}
}
