/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
package jgm.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.components.GoldPanel;
import jgm.wow.Inventory;
import jgm.wow.Item;
import jgm.wow.ItemSet;
import jgm.wow.Quality;

public abstract class AbstractLootsTab extends Tab implements ActionListener, Clearable {
	protected GoldPanel        goldLooted = new GoldPanel("Gold Looted: ");
	protected GoldPanel        lootWorth  = new GoldPanel("Loot Worth: ");
	protected GoldPanel        goldPerHour = new GoldPanel("Gold/Hour: ");
	
	protected JButton          resetBtn   = new JButton("Reset Loot");
	
	protected long lastUpdateTime = System.currentTimeMillis();
	protected long initialGoldTime = System.currentTimeMillis();
	
	// array index is the item's quality
	protected LootsPane[]      panes  = new LootsPane[5];
	protected ItemTable[]      tables = new ItemTable[5];
	protected ItemTableModel[] items  = new ItemTableModel[5];
	
	protected Timer repaintTimer;
	
	protected static final String[] headers = {
		"Poor", "Common", "Uncommon", "Rare", "Epic"
	};

	public AbstractLootsTab(GUI gui, String name) {
		super(gui, new BorderLayout(20, 0), name);
		
		FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 20, 0);
		fl.setAlignOnBaseline(true);
		JPanel goldPanel = new JPanel(fl);
		goldPanel.add(goldLooted);
		goldPanel.add(lootWorth);
		goldPanel.add(goldPerHour);
		goldPanel.add(resetBtn);
		resetBtn.addActionListener(this);
		
		add(goldPanel, BorderLayout.NORTH);
		
		goldPerHour.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				doGoldPerHour();
			}
		});
		
		JPanel jp = new JPanel(new GridLayout(1, 0, 10, 0));

		for (int i = 0; i < 5; i++) {
			items[i]  = new ItemTableModel();
			tables[i] = createItemTable(items[i]);
			panes[i]  = new LootsPane(headers[i], i, tables[i]);
			
			if (i < 3) {
				jp.add(panes[i]);
			}
			
			// to add items for testing
//			if (i == 3)
//				for (int j = 0; j < 10; j++)
//					items[i].add(ItemSet.factory(7713 + j, "Illusionary Rod", 1)); // for testing
		}

		JPanel superLoot = new JPanel(new GridLayout(0, 1, 10, 0));
		
		for (int i = 3; i < 5; i++)
			superLoot.add(panes[i]);
		
		jp.add(superLoot);
		add(jp, BorderLayout.CENTER);

		if (isRepaintTimerUsed()) {
			repaintTimer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!AbstractLootsTab.this.gui.sm.connector.isConnected())
						return;
					
					for (ItemTable it : tables)
						it.repaint();
				}
			});
			repaintTimer.start();
		}
		
		revalidate();
	}
	
	protected boolean isRepaintTimerUsed() {
		return true;
	}
	
	protected ItemTable createItemTable(ItemTableModel model) {
		return new ItemTable(model);
	}

	@Override
	public void setEnabled(boolean enabled) {
		resetBtn.setEnabled(enabled);
	}
	
	public void add(ItemSet i) {
		if (i.quantity < 1)
			return;
		
		Item item = i.getItem();
		int quality = (item.quality >= Item.EPIC)
					  ? Item.EPIC
					  : (item.quality <= Item.POOR)
					    ? Item.POOR
						: item.quality;
	
		//System.out.println("Adding [" + item.name + "]x" + i.getQuantity() + " to loot tab");
		items[quality].add(i);
		tables[quality].changeSelection(0, 0, false, false);
		tables[quality].clearSelection();
		lastUpdateTime = System.currentTimeMillis();
	}

	public void setMoney(long l) {
		goldLooted.setMoney(l);
		doGoldPerHour();
		lastUpdateTime = System.currentTimeMillis();
	}
	
	public void addMoney(long i) {
		//System.out.println("Adding " + i + "c to loot tab");
		goldLooted.addMoney(i);
		doGoldPerHour();
		lastUpdateTime = System.currentTimeMillis();
	}
	
	protected void doGoldPerHour() {
		long d = System.currentTimeMillis() - initialGoldTime;
		//System.out.print("GPH ms: " + d);
		double diff = d / 1000.0; // ms to s
		//System.out.print("; s: " + diff);
		diff /= 60.0;   // s to min
		//System.out.print("; mn: " + diff);
		diff /= 60.0;   // min to hr
		//System.out.println("; hr: " + diff);
				
		if (diff <= 0) return;
		
		//System.out.println("   GPH: " + (goldLooted.getMoney() + lootWorth.getMoney() / diff));
		
		long totalGold = goldLooted.getMoney() + lootWorth.getMoney();
		int[] totalParts = GoldPanel.cToGsc(totalGold);
		
		if (totalGold <= 0) {
			resetGPH();
		} else {
			int[] timeParts = jgm.util.Util.msToHMS(d);
			goldPerHour.setMoney((int) (totalGold / diff));
			goldPerHour.setToolTipText(
				String.format("Earned %dg %ds %dc in %dhr %dmin %dsec",
						totalParts[0], totalParts[1], totalParts[2],
						timeParts[0], timeParts[1], timeParts[2]));
		}
	}
	
	protected void resetGPH() {
		goldPerHour.setMoney(0);
		goldPerHour.setToolTipText(null);
		initialGoldTime = System.currentTimeMillis();
	}
	
	protected transient Map<Integer, ItemSet> itemSummary = null;
	
	public void update(Inventory i) {
		resetItems();
		itemSummary = i.getItemSummary(itemSummary);
		
		for (ItemSet is : itemSummary.values()) {
			add(is);
		}
	}
	
	public void update(Status s) {
		//if (isCurrentTab())
		//	doGoldPerHour();
	}
	
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public long getRunningTime() {
		return System.currentTimeMillis() - initialGoldTime;
	}
	
//	public double getRunningTimeInHours() {
//		return (double) getRunningTime() / 3600000.0;
//	}
	
	public long getGoldLooted() {
		return goldLooted.getMoney();
	}
	
	public long getLootWorth() {
		return lootWorth.getMoney();
	}
	
	public long getGoldPerHour() {
		return goldPerHour.getMoney();
	}
	
	public List<ItemSet> getItemSets(int quality) {
		quality = (quality >= Item.EPIC)
					  ? Item.EPIC
					  : (quality <= Item.POOR)
					    ? Item.POOR
						: quality;
		
		return items[quality].getItems();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetBtn) {
			reset();
		}
	}
	
	public void resetItems() {
		if (null != itemSummary) {
			for (ItemSet is : itemSummary.values()) {
				is.quantity = 0;
			}
		}
		
		for (int i = 0; i < items.length; i++) {
			items[i].empty();
		}
	}
	
	public void reset() {
		goldLooted.setMoney(0);
		lootWorth.setMoney(0);
		resetGPH();
		resetItems();
		lastUpdateTime = System.currentTimeMillis();
	}
	
	public void clear(boolean clearingAll) {
		reset();
	}
	
	protected class LootsPane extends JPanel {
		JLabel header = null;
		JScrollPane scrollPane = null;

		public LootsPane(String s, int quality, final ItemTable table) {
			setLayout(new BorderLayout(10, 10));
			//header = new JLabel(s);
			//header.setForeground(Item.getColor(quality, false));

			//add(header, BorderLayout.NORTH);
			scrollPane = new JScrollPane(table);
			
			javax.swing.border.TitledBorder ttlBorder =
				BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), s);
			ttlBorder.setTitleColor(Quality.intToQuality(quality).darkColor);
			ttlBorder.setTitleFont(Item.TITLE_FONT);
			ttlBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
			setBorder(ttlBorder);
			
			scrollPane.addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
		            int row = table.rowAtPoint(e.getPoint());
		            gui.itemTooltipPane.setItemSet(table.getItemSet(row));
				}
			});
			add(scrollPane, BorderLayout.CENTER);
		}
	}

	protected class MyIconRenderer extends DefaultTableCellRenderer {
		private Border questBorder = BorderFactory.createLineBorder(Color.YELLOW);
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			setIcon(value instanceof Icon ? (Icon) value : null);
			Item i = ((ItemTableModel) table.getModel()).getItem(row).getItem();
			setBorder(Item.BIND_QUEST == i.binds ? questBorder : i.quality_.lightBorder);
			
			return this;
		}
	}
	
	protected class ItemTable extends JTable
		implements MouseMotionListener,
				   MouseListener {
				
		public ItemTable(ItemTableModel dm) {
			super(dm);
			
			setDefaultRenderer(Icon.class, new MyIconRenderer());
			
			setRowHeight(32);

			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(32);
			cm.getColumn(0).setMaxWidth(32);
			cm.getColumn(1).setResizable(true);
			cm.getColumn(1).setMinWidth(25);
//			cm.getColumn(1).setMaxWidth(200);
			cm.getColumn(2).setResizable(false);
			cm.getColumn(2).setMinWidth(50);
			cm.getColumn(2).setMaxWidth(50);
			cm.getColumn(3).setResizable(false);
			cm.getColumn(3).setMinWidth(75);
			cm.getColumn(3).setMaxWidth(75);
			
			addMouseListener(this);
			addMouseMotionListener(this);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
				
		public ItemSet getItemSet(int row) {
			return ((ItemTableModel) dataModel).getItem(row);
		}
		
		public void mouseEntered(MouseEvent e) {
			//System.out.println("Entered: " + e);
            int row = this.rowAtPoint(e.getPoint());
            gui.itemTooltipPane.setItemSet(getItemSet(row));
		}
		
		public void mouseExited(MouseEvent e) {
			gui.itemTooltipPane.setItemSet(null);
		}
		
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() != 1) return;
			
			Point pt = e.getPoint();
			int row = this.rowAtPoint(pt);
	
			ItemTableModel tm = (ItemTableModel) this.dataModel;
			ItemSet is = tm.getItem(row);
			
			int itemId = is.getItem().id;
			
			jgm.util.Util.openURL(
				String.format(
					jgm.Config.getInstance().get("general.wowdb"),
					itemId
			));
		}
		
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		public void mouseMoved(MouseEvent e) {
			int row = this.rowAtPoint(e.getPoint());
			gui.itemTooltipPane.setItemSet(getItemSet(row));
		}
		
		public void mouseDragged(MouseEvent e) {}
	}

	protected static final java.util.Comparator<ItemSet> comp
		= ItemSet.getQuantityComparator();

	protected enum ItemCols {
		ICON(" ", Icon.class),
		NAME("Name", String.class),
		QTY("Qty", Integer.class),
		PER_HOUR("Per Hr", Double.class)
		;
		
		private ItemCols(String name, java.lang.Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}
		
		public final String name;
		public final java.lang.Class<?> clazz;
	}
	
	protected static final ItemCols[] ITEM_COLS = ItemCols.values();
	
	protected class ItemTableModel extends AbstractTableModel {
		private ArrayList<ItemSet> itemSets;

		public ItemTableModel() {
			super();
			itemSets = new ArrayList<ItemSet>();
		}

		public void empty() {
			itemSets.clear();
			this.fireTableDataChanged();
		}
		
		public void add(ItemSet i) {
			int index = itemSets.indexOf(i);

			lootWorth.addMoney(i.getItem().merchentBuyPrice * i.getQuantity());
			
			if (index < 0) {
				i.getItem().getIcon(); // call to init the icon the first time
				itemSets.add(i);
				fireTableRowsInserted(itemSets.size() - 1, itemSets.size() - 1);
				
				//System.out.println("Adding first instance of [" + i.getItem().name + "] to loot table");
			} else {
				itemSets.get(index).addQuantity(i);
				
				//System.out.println("Adding " + i.getQuantity() + " additional [" + i.getItem().name + "] to loot table");
			}

			java.util.Collections.sort(itemSets, comp);

			fireTableRowsUpdated(0, itemSets.size() - 1);
		}
		
		public ItemSet getItem(int r) {
			return itemSets.get(r);
		}
		
		@SuppressWarnings("unchecked")
		public List<ItemSet> getItems() {
			return (List<ItemSet>) itemSets.clone();
		}
		
		public int getColumnCount() {
			return ITEM_COLS.length;
		}

		public int getRowCount() {
			return itemSets.size();
		}

		public String getColumnName(int col) {
			return ITEM_COLS[col].name;
		}

		public Object getValueAt(int row, int col) {
			ItemSet i = itemSets.get(row);
			Object ret = null;

			switch (ITEM_COLS[col]) {
				case ICON:
					ret = i.getItem().getIcon();
					break;

				case NAME:
					ret = i.getItem().name;
					break;

				case QTY:
					ret = i.getQuantity();
					break;
					
				case PER_HOUR:
					ret = i.getQuantityPerHour();
					break;
			}

			return ret;
		}

		public java.lang.Class<?> getColumnClass(int c) {
			return ITEM_COLS[c].clazz;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
