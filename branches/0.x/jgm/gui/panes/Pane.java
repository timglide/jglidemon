package jgm.gui.panes;

import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

/**
 * Abstract class representing one of the panels 
 * in the GUI.
 * @author Tim
 * @since 0.1
 */
public abstract class Pane extends JPanel {
	protected GridBagConstraints c = null;

	/**
	 * Create a new Pane with the specified LayoutManager.
	 * @param lm The LayoutManager to use
	 */
	public Pane(LayoutManager lm) {
		setLayout(lm);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0; c.gridy = 0;
	}

	/**
	 * Create a new Pane with a GridBagLayout.
	 */
	public Pane() {
		this(new GridBagLayout());
	}

	/**
	 * This method will be called when the StatusUpdater
	 * has been updated.
	 * @param s The StatusUpdater with updated information
	 */
	public void update(StatusUpdater s) {}
}
