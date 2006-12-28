package jgm.gui.panes;

import jgm.gui.components.HeadingDial;
import jgm.gui.updaters.StatusUpdater;

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

	public CharInfoPane() {
		super();

		setLayout(new BorderLayout());

		JPanel p = new JPanel(new GridBagLayout());

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
		heading.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconsPane.add(heading);

		add(iconsPane, BorderLayout.WEST);
		add(p, BorderLayout.CENTER);
	}

	public void update(StatusUpdater s) {
		name.setText(s.name);
		level.setText(Integer.toString(s.level));
		health.setValue((int) s.health);
		mana.setValue((int) s.mana);

		if (!clazz.getText().equals(s.clazz)) {
			clazz.setText(s.clazz);
			java.net.URL iconURL = jgm.JGlideMon.class.getResource("resources/images/classes/" + s.clazz.toLowerCase() + ".png");
			iconLabel.setIcon(new ImageIcon(iconURL));
			
			if (s.clazz.equals("Warrior")) {
				manaLbl.setText("Rage: ");
			} else if (s.clazz.equals("Rogue")) {
				manaLbl.setText("Energy: ");
			} else {
				manaLbl.setText("Mana: ");
			}
		}

		heading.setHeading(s.heading);
	}
}
