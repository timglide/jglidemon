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
package jgm.gui.panes;

import jgm.glider.Status;
import jgm.gui.tabs.*;

import java.awt.*;
import javax.swing.*;

public class TabsPane extends Pane {
	public JTabbedPane  tabbedPane;

	public ScreenshotTab screenshotTab;
	public LogTab        statusLog;
	public PlayerChartTab chartTab;
	public MobsTab       mobsTab;
	public LootsTab      lootsTab;
	
	public ChatTab       chatLog;
	
	public UrgentTab     urgent;
	public ProfilesTab   profiles;
	public LogTab        rawChatLog;
	public LogTab        combatLog;
	public LogTab        gliderLog;
	public LogTab        rawLog;

	public TabsPane(jgm.gui.GUI gui) {
		super(gui, new BorderLayout());

		tabbedPane = new JTabbedPane();

		screenshotTab = new ScreenshotTab(gui);
		tabbedPane.addChangeListener(screenshotTab);
		tabbedPane.addKeyListener(screenshotTab);
		addTab(screenshotTab);
		
		chatLog   = new ChatTab(gui);
		urgent    = new UrgentTab(gui);
		profiles  = new ProfilesTab(gui);
		combatLog = new LogTab(gui, "Combat Log", tabbedPane);
		gliderLog = new LogTab(gui, "Glider Log", tabbedPane);
		statusLog = new LogTab(gui, "Status", tabbedPane);
		
		if (jgm.JGlideMon.debug) {
			rawChatLog = new LogTab(gui, "Raw Chat Log", tabbedPane);
			rawLog     = new LogTab(gui, "Raw Log", tabbedPane);
		}

		addTab(chatLog);
		addTab(urgent);
//		addTab(profiles); // no HB profile support yet
		
		if (rawChatLog != null) {
			addTab(rawChatLog);
		}
		
		addTab(combatLog);
		addTab(gliderLog);
		addTab(statusLog);
		
		if (rawLog != null) {
			addTab(rawLog);
		}

		chartTab = new PlayerChartTab(gui);
		addTab(chartTab);
		
		mobsTab = new MobsTab(gui);
		addTab(mobsTab);
		
		lootsTab = new LootsTab(gui);
		addTab(lootsTab);

		try {
			tabbedPane.setSelectedIndex(gui.sm.getInt("general.lasttab"));
		} catch (IndexOutOfBoundsException e) {}
		
		// save current tab in the config to restore when program is restarted
		tabbedPane.addChangeListener(
			new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					TabsPane.this.gui.sm.set("general.lasttab", tabbedPane.getSelectedIndex());
				}
			}
		);
		
		add(tabbedPane, BorderLayout.CENTER);
	}

	private void addTab(Tab t) {
		tabbedPane.addTab(t.name, t);
	}

	public void update(Status s) {
		lootsTab.update(s);
		chatLog.update(s);
	}
}
