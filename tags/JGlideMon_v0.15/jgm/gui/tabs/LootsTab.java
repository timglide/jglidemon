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

import jgm.glider.Status;
import jgm.gui.components.*;
import jgm.wow.*;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LootsTab extends Tab implements ActionListener, Clearable {
	private GoldPanel        goldLooted = new GoldPanel("Gold Looted: ");
	private GoldPanel        lootWorth  = new GoldPanel("Loot Worth: ");
	private GoldPanel        goldPerHour = new GoldPanel("Gold/Hour: ");
	
	public  JButton          resetBtn   = new JButton("Reset Loot");
	
	private long initialGoldTime = System.currentTimeMillis();
	
	// array index is the item's quality
	private LootsPane[]      panes  = new LootsPane[5];
	private ItemTable[]      tables = new ItemTable[5];
	private ItemTableModel[] items  = new ItemTableModel[5];

	private JLayeredPane     layeredPane = null;
	private ItemTooltip      itemTooltip = null;
	//private JToolTip         toolTip     = new JToolTip();
	
	private static final String[] headers = {
		"Poor", "Common", "Uncommon", "Rare", "Epic"
	};

	public LootsTab(jgm.gui.GUI gui) {
		super(gui, new BorderLayout(20, 20), "Loot");
		
		JPanel goldPanel = new JPanel(new GridLayout(1, 0));
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
			tables[i] = new ItemTable(items[i]);
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
		
		layeredPane = new JLayeredPane();
		
		itemTooltip = new ItemTooltip("Test Item");
		//toolTip.add(itemTooltip);
		layeredPane.add(itemTooltip, 0);
		
		layeredPane.setLayout(new BorderLayout());
		layeredPane.add(jp, BorderLayout.CENTER, 1);
				
		add(layeredPane, BorderLayout.CENTER);

		revalidate();
	}

	public void add(ItemSet i) {
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
		doGoldPerHour();
	}

	public void addMoney(int i) {
		//System.out.println("Adding " + i + "c to loot tab");
		goldLooted.addMoney(i);
		doGoldPerHour();
	}
	
	private void doGoldPerHour() {
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
		
		int totalGold = goldLooted.getMoney() + lootWorth.getMoney();
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
	
	private void resetGPH() {
		goldPerHour.setMoney(0);
		goldPerHour.setToolTipText(null);
		initialGoldTime = System.currentTimeMillis();
	}
	
	public void update(Status s) {
		//if (isCurrentTab())
		//	doGoldPerHour();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetBtn) {
			goldLooted.setMoney(0);
			lootWorth.setMoney(0);
			resetGPH();
			
			for (int i = 0; i < items.length; i++) {
				items[i].empty();
			}
		}
	}
	
	public void clear(boolean clearingAll) {
		resetBtn.doClick();
	}
	
	private class LootsPane extends JPanel {
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
			ttlBorder.setTitleColor(Item.getColor(quality, false));
			ttlBorder.setTitleFont(Item.TITLE_FONT);
			ttlBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
			setBorder(ttlBorder);
			
			scrollPane.addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					//System.out.println(e);
					scrollPane.repaint();
					table.repaint();
		            int row = table.rowAtPoint(e.getPoint());
		            itemTooltip.setItemSet(table.getItemSet(row));
					itemTooltip.repaint();
				}
			});
			add(scrollPane, BorderLayout.CENTER);
		}
	}

	
	private class ItemTable extends JTable
		implements MouseMotionListener,
				   MouseListener {
				
		public ItemTable(ItemTableModel dm) {
			super(dm);
			
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
            itemTooltip.setItemSet(getItemSet(row));
			itemTooltip.setVisible(true);
			itemTooltip.revalidate();
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		
		public void mouseExited(MouseEvent e) {
			//System.out.println("Exited: " + e);
			itemTooltip.setVisible(false);
			itemTooltip.revalidate();
			
			setCursor(Cursor.getDefaultCursor());
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
		  try {
            int row = this.rowAtPoint(e.getPoint());
            itemTooltip.setItemSet(getItemSet(row));
            
			Point p = layeredPane.getMousePosition();
			Dimension panelSize = layeredPane.getSize();
			Dimension ttSize    = itemTooltip.getSize();
			
			// ensure tooltip is drawn within the window
			if (p.x + ttSize.width  >= panelSize.width)
				p.x = panelSize.width - ttSize.width;
			if (p.y + ttSize.height >= panelSize.height)
				p.y = panelSize.height - ttSize.height;
			
			itemTooltip.setLocation(p.x + 10, p.y);
		  } catch (NullPointerException x) {} // meh
		}
		
		public void mouseDragged(MouseEvent e) {}
	}

	private static final java.util.Comparator<ItemSet> comp
		= ItemSet.getQuantityComparator();

	private static final String[] columnNames = {" ", "Name", "Qty"};
	
	private class ItemTableModel extends AbstractTableModel {
		private Vector<ItemSet> itemSets;

		public ItemTableModel() {
			super();

			itemSets = new Vector<ItemSet>();
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
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return itemSets.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			ItemSet i = itemSets.get(row);
			Object ret = null;

			switch (col) {
				case 0:
					ret = i.getItem().getIcon();
					break;

				case 1:
					ret = i.getItem().name;
					break;

				case 2:
					ret = i.getQuantity();
					break;
			}

			return ret;
		}

		public java.lang.Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
