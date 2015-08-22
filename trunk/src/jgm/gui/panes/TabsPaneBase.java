package jgm.gui.panes;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.tabs.Clearable;
import jgm.gui.tabs.Tab;

public class TabsPaneBase extends Pane implements Clearable {
	public JTabbedPane	tabbedPane;

	public TabsPaneBase(GUI gui) {
		super(gui, new BorderLayout());
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void addTab(Tab t) {
		tabbedPane.addTab(t.name, t);
	}

	public void clear(boolean clearingAll) {
		Component c;
		if (!clearingAll) {
			c = tabbedPane.getSelectedComponent();
			if (c instanceof Clearable)
				((Clearable) c).clear(clearingAll);
		} else {
			for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
				c = tabbedPane.getComponentAt(i);
				if (c instanceof Clearable)
					((Clearable) c).clear(clearingAll);
			}
		}
	}
	
	@Override
	public void update(Status s) {
		Component c;
		for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
			c = tabbedPane.getComponentAt(i);
			if (c instanceof Tab)
				((Tab) c).update(s);
		}
	}
}