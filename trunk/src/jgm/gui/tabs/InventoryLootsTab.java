package jgm.gui.tabs;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import jgm.glider.Status;
import jgm.gui.GUI;

public class InventoryLootsTab extends AbstractLootsTab implements ChangeListener {
	public InventoryLootsTab(GUI gui) {
		super(gui, "Inventory Summary");
		goldLooted.setText("Gold: ");
		goldPerHour.setVisible(false);
		resetBtn.setText("Refresh Inventory");
	}
	
	@Override
	protected boolean isRepaintTimerUsed() {
		return false;
	}
	
	@Override
	protected ItemTable createItemTable(ItemTableModel model) {
		ItemTable table = super.createItemTable(model);
		TableColumn tc = table.getColumnModel().getColumn(3);
		table.getColumnModel().removeColumn(tc);
		return table;
	}
	
	@Override
	public void reset() {
		gui.sm.inventoryUpdater.updateAsync();
	}
	
	@Override
	public void update(Status s) {
		if (s.attached) {
			goldLooted.setMoney(s.copper);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (isCurrentTab()) {
			reset();
		}
	}
}
