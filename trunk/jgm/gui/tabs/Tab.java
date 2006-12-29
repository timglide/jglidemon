package jgm.gui.tabs;

import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

public abstract class Tab extends jgm.gui.panes.Pane {
	public String name;

	public Tab(String s) {
		super();
		name = s;
	}

	public Tab(LayoutManager lm, String s) {
		super(lm);
		name = s;
	}

	public boolean isFocusable() {
		return true;
	}
	
	public int getIndex() {
		return ((JTabbedPane) this.getParent()).indexOfTab(name);
	}
	
	public boolean isCurrentTab() {
		return ((JTabbedPane) this.getParent()).getSelectedIndex()
				== getIndex();
	}
	
	public void update(StatusUpdater s) {}
}
