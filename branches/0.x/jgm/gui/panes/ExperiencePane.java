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

public class ExperiencePane extends Pane {
	private JLabel ttl;
	private JLabel xp;
	private JLabel xph;

	private JProgressBar xpbar;

	public ExperiencePane() {
		super();

		c.weightx = 0.0; c.weighty = 0.0;

		c.gridx = 0; c.gridy = 0;
		add(new JLabel("Time to level: "), c);

		ttl = new JLabel("Unknown");
		c.gridx = 1; c.weightx = 1.0;
		add(ttl, c);

		c.gridx = 2; c.weightx = 0.0;
		add(new JLabel("Experience: "), c);

		xp = new JLabel("Unknown");
		c.gridx = 3; c.weightx = 1.0;
		add(xp, c);

		c.gridx = 4; c.weightx = 0.0;
		add(new JLabel("XP/Hour: "), c);

		xph = new JLabel("Unknown");
		c.gridx = 5; c.weightx = 1.0;
		add(xph, c);

		xpbar = new JProgressBar(0, 100);
		xpbar.setValue(0); c.weightx = 0.0;
		c.gridx = 0; c.gridy = 1; c.gridwidth = 6;
		add(xpbar, c);
	}

	public void update(StatusUpdater s) {		
		int xpPercent = 0;

		if (s.xpPerHour > 0) {
			int seconds = 0, minutes = 0, hours = 0;
			int xpDiff = s.nextExperience - s.experience;
			double d = (double) xpDiff / (double) s.xpPerHour;
			hours = (int) d;
			d = 60 * (d - hours);
			minutes = (int) d;
			d = 60 * (d - minutes);
			seconds = (int) d;

			ttl.setText(hours + "hr " + minutes + "min " + seconds + "sec");
		} else {
			ttl.setText("Unknown");
		}

		if (s.nextExperience > 0) {
			xpPercent = (int) (100 * ((float) s.experience / (float) s.nextExperience));

			xp.setText(
				s.experience + "/" + s.nextExperience
				+ " (" + xpPercent + "%)");
		} else {
			xp.setText("Unknown");
		}

		xpbar.setValue(xpPercent);
		xph.setText(Integer.toString(s.xpPerHour));
	}
}
