package jgm.gui.tabs;

import jgm.gui.components.*;
import jgm.wow.Item;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LootsTab extends Tab implements ActionListener {
	private GoldPanel        goldLooted = new GoldPanel("Gold Looted: ");
	private GoldPanel        lootWorth  = new GoldPanel("Loot Worth: ");
	
	private JButton          resetBtn   = new JButton("Reset Loot");
	
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

	public LootsTab() {
		super(new BorderLayout(20, 20), "Loot");
		
		JPanel goldPanel = new JPanel(new GridLayout(1, 0));
		goldPanel.add(goldLooted);
		goldPanel.add(lootWorth);
		goldPanel.add(resetBtn);
		resetBtn.addActionListener(this);
		
		add(goldPanel, BorderLayout.NORTH);
		
		JPanel jp = new JPanel(new GridLayout(1, 0, 10, 0));

		for (int i = 0; i < 5; i++) {
			items[i]  = new ItemTableModel();
			tables[i] = new ItemTable(items[i]);
			panes[i]  = new LootsPane(headers[i], i, tables[i]);
			
			if (i < 3) {
				jp.add(panes[i]);
			}
			
//			if (i == 3)
//				for (int j = 0; j < 10; j++)
//					items[i].add(Item.factory(7713 + j, "Illusionary Rod")); // for testing
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

	public void add(Item i) {
		int quality = (i.quality > Item.EPIC)
					  ? Item.EPIC
					  : (i.quality < Item.POOR)
					    ? Item.POOR
						: i.quality;
	
		items[quality].add(i);
		tables[quality].changeSelection(0, 1, false, false);
	}

	public void addMoney(int i) {
		goldLooted.addMoney(i);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Reset Loot")) {
			goldLooted.setMoney(0);
			lootWorth.setMoney(0);
			
			for (int i = 0; i < items.length; i++) {
				items[i].empty();
			}
		}
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
		            Item i = table.getItem(row);
		            itemTooltip.setItem(i);
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
			
			setRowHeight(64);

			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(64);
			cm.getColumn(0).setMaxWidth(64);
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
				
		public Item getItem(int row) {
			return ((ItemTableModel) dataModel).getItem(row);
		}
		
		public void mouseEntered(MouseEvent e) {
			//System.out.println("Entered: " + e);
            int row = this.rowAtPoint(e.getPoint());
            Item i = getItem(row);
            itemTooltip.setItem(i);
			itemTooltip.setVisible(true);
			itemTooltip.revalidate();
		}
		
		public void mouseExited(MouseEvent e) {
			//System.out.println("Exited: " + e);
			itemTooltip.setVisible(false);
			itemTooltip.revalidate();
		}
		
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		public void mouseMoved(MouseEvent e) {
		  try {
            int row = this.rowAtPoint(e.getPoint());
            Item i = getItem(row);
            itemTooltip.setItem(i);
            
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

	private static final java.util.Comparator<Item> comp
		= Item.getQuantityComparator();

	private static final String[] columnNames = {"Icon", "Name", "Qty"};
	
	private class ItemTableModel extends AbstractTableModel {
		private Vector<Item> items;

		public ItemTableModel() {
			super();

			items = new Vector<Item>();
		}

		public void empty() {
			items.clear();
			this.fireTableDataChanged();
		}
		
		public void add(Item i) {
			int index = items.indexOf(i);

			lootWorth.addMoney(i.merchentBuyPrice * i.quantity);
			
			if (index < 0) {
				i.getIcon(); // call to init the icon the first time
				items.add(i);
				fireTableRowsInserted(items.size() - 1, items.size() - 1);
			} else {
				items.get(index).addQuantity(i);
			}

			java.util.Collections.sort(items, comp);

			fireTableRowsUpdated(0, items.size() - 1);
		}
		
		public Item getItem(int r) {
			return items.get(r);
		}
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return items.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Item i = items.get(row);
			Object ret = null;

			switch (col) {
				case 0:
					ret = i.getIcon();
					break;

				case 1:
					ret = i.name;
					break;

				case 2:
					ret = i.quantity;
					break;
			}

			return ret;
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
