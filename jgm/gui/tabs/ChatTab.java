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
package jgm.gui.tabs;

import jgm.glider.log.*;

import java.awt.*;
import javax.swing.*;

public class ChatTab extends Tab {
	private JTabbedPane tabs;
	public LogTab all;
	public LogTab pub;
	public LogTab whisper;
	public LogTab guild;
	
	public ChatTab() {
		super(new BorderLayout(), "Chat");
		
		tabs = new JTabbedPane();
		all = new LogTab("All Chat", tabs);
		pub = new LogTab("Public Chat", tabs);
		whisper = new LogTab("Whisper/Say", tabs);
		guild = new LogTab("Guild", tabs);
		
		addTab(all);
		addTab(pub);
		addTab(whisper);
		addTab(guild);
		
		add(tabs, BorderLayout.CENTER);
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t.name, t);
	}
	
	public void add(ChatLogEntry e) {
		all.add(e);
		
		String channel = e.getChannel();
		if (channel == null) return;
		
		if (channel.equals("Whisper") || channel.equals("Say")) {
			whisper.add(e);
		} else if (channel.equals("Guild") || channel.equals("Officer")) {
			guild.add(e);
		} else if (e.getType().equals("Public Chat")) {
			pub.add(e);
		}
	}
	
	public void clear() {
		all.clear();
		pub.clear();
		whisper.clear();
		guild.clear();
	}
}
