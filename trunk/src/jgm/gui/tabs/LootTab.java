package jgm.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.panes.TabsPaneBase;
import jgm.wow.Inventory;

public class LootTab extends Tab implements Clearable, ChangeListener {
	private TabsPaneBase tabs;
	
	public LootsTab lootsTab;
	public InventoryLootsTab inventoryLootsTab;
	public InventoryTab inventoryTab;
	
	public LootTab(GUI gui) {
		super(gui, new BorderLayout(), "Loot/Inventory");
		tabs = new TabsPaneBase(gui);
		add(tabs, BorderLayout.CENTER);
		
		lootsTab = new LootsTab(gui);
		tabs.addTab(lootsTab);
		
		inventoryLootsTab = new InventoryLootsTab(gui);
		tabs.addTab(inventoryLootsTab);
		
		inventoryTab = new InventoryTab(gui);
		tabs.addTab(inventoryTab);
		
		tabs.tabbedPane.addChangeListener(inventoryLootsTab);
		tabs.tabbedPane.addChangeListener(inventoryTab);
	}
	
	@Override
	public void update(Status s) {
		tabs.update(s);
	}

	@Override
	public void clear(boolean clearingAll) {
		tabs.clear(clearingAll);
	}

	public void update(Inventory i) {
		inventoryLootsTab.update(i);
		inventoryTab.update(i);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (isCurrentTab()) {
			inventoryLootsTab.stateChanged(e);
			inventoryTab.stateChanged(e);
		}
	}
}
