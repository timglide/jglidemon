package jgm.gui.panes;

import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

public abstract class Pane extends JPanel {
	protected GridBagConstraints c = null;

	public Pane(LayoutManager lm) {
		setLayout(lm);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0;
	}

	public Pane() {
		this(new GridBagLayout());
	}

	public abstract void update(StatusUpdater s);
}
