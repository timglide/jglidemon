package jgm.gui.tabs;

import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

/**
 * Abstract class representing one of the main tabs.
 * @author Tim
 * @since 0.1
 */
public abstract class Tab extends jgm.gui.panes.Pane {
	public String name;

	/**
	 * Create a Tab with the specified name.
	 * @param s The name of the Tab
	 */
	public Tab(String s) {
		super();
		name = s;
	}

	/**
	 * Create a Tab with the specified name and LayoutManager.
	 * @param lm The LayoutManager to use
	 * @param s The name of the Tab
	 */
	public Tab(LayoutManager lm, String s) {
		super(lm);
		name = s;
	}

	public final boolean isFocusable() {
		return true;
	}
	
	/**
	 * Returns the index of this Tab.
	 * @return The index of this Tab
	 */
	public final int getIndex() {
		return ((JTabbedPane) this.getParent()).indexOfTab(name);
	}
	
	/**
	 * Determines if this Tab is currently selected.
	 * @return Whether this Tab is currently selected
	 */
	public final boolean isCurrentTab() {
		return ((JTabbedPane) this.getParent()).getSelectedIndex()
				== getIndex();
	}
	
	/**
	 * It is optional for a Tab to implement this method
	 * for when the StatusUpdater has updated.
	 */
	public void update(StatusUpdater s) {}
}
