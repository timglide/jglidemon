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
import jgm.gui.panes.TabsPaneBase;

import java.awt.*;
import javax.swing.*;

public class ChatTab extends Tab implements Clearable {
	private TabsPaneBase tabs;
	public LogTab all;
	public LogTab pub;
	public LogTab whisper;
	public LogTab guild;
	
	public ChatTab(jgm.gui.GUI gui) {
		super(gui, new BorderLayout(), "Chat");
		
		tabs = new TabsPaneBase(gui);
		all = new LogTab(gui, "All Chat");
		pub = new LogTab(gui, "Public Chat");
		whisper = new LogTab(gui, "Whisper/Say/Yell");
		guild = new LogTab(gui, "Guild");
		
		addTab(all);
		addTab(pub);
		addTab(whisper);
		addTab(guild);
		
		add(tabs, BorderLayout.CENTER);
		
		validate();
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t);
	}
	
	public void add(ChatLogEntry e) {
		all.add(e);
		
		String channel = e.getChannel();
		if (channel == null) return;
		
		if (channel.equals("Whisper") || channel.equals("Say") || channel.equals("Yell")) {
			whisper.add(e);
		} else if (channel.equals("Guild") || channel.equals("Officer")) {
			guild.add(e);
		} else if (e.getType().equals("Public Chat")) {
			pub.add(e);
		}
	}
	
	public void clear(boolean clearingAll) {
		tabs.clear(clearingAll);
	}
}
