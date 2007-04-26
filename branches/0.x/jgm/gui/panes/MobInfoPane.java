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

import jgm.gui.updaters.StatusUpdater;

import javax.swing.*;

public class MobInfoPane extends Pane {
	private JLabel name;
	private JLabel level;

	private JProgressBar health;

	private JLabel kills;
	private JLabel loots;
	private JLabel deaths;

	public MobInfoPane() {
		super();

		name = new JLabel("Target Name: ");
		c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
		add(name, c);

		level = new JLabel("Target Level: ");
		c.gridx = 0; c.gridy = 1;
		add(level, c);

		health = new JProgressBar(0, 100);
		health.setString("Target's Health");
		health.setStringPainted(true);
		health.setValue(0);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.weightx = 1.0;
		add(health, c);

		kills = new JLabel("Kills: 0");
		c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
		add(kills, c);

		loots = new JLabel("Loots: 0");
		c.gridx = 1;
		add(loots, c);

		deaths = new JLabel("Deaths: 0");
		c.gridx = 2;
		add(deaths, c);
	}

	public void update(StatusUpdater s) {
		name.setText("Target Name: " + ((s.targetName.equals("")) ? "No Target" : s.targetName));
		level.setText("Target Level: " + ((s.targetLevel > 0) ? s.targetLevel : ""));
		health.setValue((int) s.targetHealth);

		kills.setText("Kills: " + s.kills);
		loots.setText("Loots: " + s.loots);
		deaths.setText("Deaths: " +s.deaths);
	}
}
