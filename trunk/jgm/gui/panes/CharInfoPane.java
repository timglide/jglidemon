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
import jgm.gui.components.HeadingDial;

import java.awt.*;
import javax.swing.*;

public class CharInfoPane extends Pane {
	private JLabel name;
	private JLabel clazz; // player's class
	private JLabel iconLabel;
	private JLabel level;
	private HeadingDial heading;

	private JProgressBar health;
	private JLabel       manaLbl;
	private JProgressBar mana;

	public CharInfoPane(jgm.gui.GUI gui) {
		super(gui);

		setLayout(new BorderLayout());

		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createEmptyBorder(0, jgm.gui.GUI.PADDING / 2, 0, 0));
		
		c.gridx = 0; c.gridy = 0;
		p.add(new JLabel("Name: "), c);

		name = new JLabel();
		c.gridx = 1; c.gridy = 0;
		p.add(name, c);

		c.gridx = 0; c.gridy = 1;
		p.add(new JLabel("Class: "),  c);

		clazz = new JLabel();
		c.gridx = 1; c.gridy = 1;
		p.add(clazz, c);

		c.gridx = 0; c.gridy = 2;
		p.add(new JLabel("Level: "), c);

		level = new JLabel();
		c.gridx = 1; c.gridy = 2;
		p.add(level, c);

		c.gridx = 0; c.gridy = 3;
		p.add(new JLabel("Health: "), c);

		health = new JProgressBar(0, 100);
		health.setValue(0);
		c.gridx = 1; c.gridy = 3; c.gridwidth = 2; c.weightx = 1.0;
		p.add(health, c);

		c.gridx = 0; c.gridy = 4; c.gridwidth = 1; c.weightx = 0.0;
		manaLbl = new JLabel("Mana: ");
		p.add(manaLbl, c);

		mana = new JProgressBar(0, 100);
		mana.setValue(0);
		c.gridx = 1; c.gridy = 4; c.gridwidth = 2; c.weightx = 1.0;
		p.add(mana, c);

		java.net.URL iconURL = jgm.JGlideMon.class.getResource("resources/images/classes/unknown.png");
		iconLabel = new JLabel(new ImageIcon(iconURL));

		heading = new HeadingDial();

		JPanel iconsPane = new JPanel();
		iconsPane.setLayout(new BoxLayout(iconsPane, BoxLayout.Y_AXIS));
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconsPane.add(iconLabel);
		iconsPane.add(Box.createRigidArea(new Dimension(0, jgm.gui.GUI.PADDING / 2)));
		heading.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconsPane.add(heading);

		add(iconsPane, BorderLayout.WEST);
		add(p, BorderLayout.CENTER);
	}

	public void update(Status s) {
		name.setText(s.name);
		level.setText(((s.level > 0) ? Integer.toString(s.level) : ""));
		health.setValue((int) s.health);
		health.setToolTipText(Integer.toString((int) s.health) + "%");
		mana.setValue((int) s.mana);
		mana.setToolTipText(Integer.toString((int) s.mana) + "%");

		if (!clazz.getText().equals(s.clazz.toString())) {
			try {
				clazz.setText(s.clazz.toString());
				java.net.URL iconURL = jgm.JGlideMon.class.getResource("resources/images/classes/" + s.clazz.toString().toLowerCase() + ".png");
				iconLabel.setIcon(new ImageIcon(iconURL));
			} catch (NullPointerException e) {
				System.err.println("Null when setting class icon");
				e.printStackTrace();
			}
		}
		
		// need to update it each time to account for druids
		manaLbl.setText(s.manaName + ": ");

		heading.setHeading(s.heading);
	}
}
