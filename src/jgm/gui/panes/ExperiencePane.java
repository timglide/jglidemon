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


import java.text.NumberFormat;

import javax.swing.*;

import jgm.glider.Status;

public class ExperiencePane extends Pane {
	private JLabel ttl;
	private JLabel xp;
	private JLabel xph;

	private JProgressBar xpbar;

	public ExperiencePane(jgm.gui.GUI gui) {
		super(gui);

		c.weightx = 1.0; c.weighty = 0.0;

		ttl = new JLabel("Time to level: Unknown", JLabel.CENTER);
		add(ttl, c);

		xp = new JLabel("Experience: Unknown", JLabel.CENTER);
		c.gridx++;
		add(xp, c);

		xph = new JLabel("XP/Hour: Unknown", JLabel.CENTER);
		c.gridx++;
		add(xph, c);

		xpbar = new JProgressBar(0, 100);
		xpbar.setValue(0);
		c.gridwidth = c.gridx + 1; c.gridx = 0; c.gridy++;
		add(xpbar, c);
	}

	public void update(Status s) {		
		if (s.attached) {
			if (s.nextExperience > 0) {
				xp.setText(
					String.format(
						"Experience: %,d/%,d (%s%%)",
						s.experience,
						s.nextExperience,
						s.xpPercent
				));
			} else {
				xp.setText("Experience: Unknown");
			}
		}
		
		// hide this pane if we're at the level cap but only if we're connected and the level is valid
		// so we don't keep toggling visibility just because we disconnect and reconnect right away
		if (gui.sm.connector.isConnected() && s.level > 0) {
			this.setVisible(!s.atLevelCap());
		}
		
		if (s.xpPerHour > 0 && s.attached) {
			int seconds = 0, minutes = 0, hours = 0;
			int xpDiff = s.nextExperience - s.experience;
			double d = (double) xpDiff / (double) s.xpPerHour;
			hours = (int) d;
			d = 60 * (d - hours);
			minutes = (int) d;
			d = 60 * (d - minutes);
			seconds = (int) d;

			ttl.setText("Time to level: " + hours + "hr " + minutes + "min " + seconds + "sec");
		} else {
			ttl.setText("Time to level: Unknown");
		}

		xpbar.setValue(s.xpPercent);
		// redundant
//		xpbar.setToolTipText(xpPercent + "%");
		xph.setText(String.format("XP/Hour: %,d", s.xpPerHour));
	}
}
