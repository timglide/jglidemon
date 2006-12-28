package jgm.gui.panes;

import jgm.glider.*;
import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
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
		health.setValue(74);
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
		name.setText("Target Name: " + s.targetName);
		level.setText("Target Level: " + s.targetLevel);
		health.setValue((int) s.targetHealth);

		kills.setText("Kills: " + s.kills);
		loots.setText("Loots: " + s.loots);
		deaths.setText("Deaths: " +s.deaths);
	}
}
