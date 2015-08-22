package jgm.gui.tabs;

import jgm.gui.GUI;
import jgm.wow.ItemSet;

public class LootsTab extends AbstractLootsTab {
	public LootsTab(GUI gui) {
		super(gui, "Loot from Chat Log");
	}
	
	@Override
	public void add(ItemSet i) {
		super.add(i);
		doGoldPerHour();
//		gui.tabsPane.lootTab.inventoryTab.updateInventory();
	}
}
