package jgm.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.components.GoldPanel;
import jgm.wow.Bag;
import jgm.wow.Inventory;
import jgm.wow.Item;
import jgm.wow.ItemSet;

public class InventoryTab extends Tab implements ActionListener, ChangeListener {
	private JLabel slots = new JLabel();
	private GoldPanel gold = new GoldPanel("Gold: ");
	private JButton refreshBtn = new JButton("Refresh Inventory");
	
	private BagTableModel[] models = new BagTableModel[5];
	private BagTable[] tables = new BagTable[5];
	private TableWrapper[] tableWrappers = new TableWrapper[5];
	
	public InventoryTab(GUI gui) {
		super(gui, new BorderLayout(), "Inventory");
		
		refreshBtn.setEnabled(false);
		refreshBtn.addActionListener(this);
		
		JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		header.add(slots);
		header.add(gold);
		header.add(refreshBtn);
		add(header, BorderLayout.NORTH);
		
		FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 20, 20);
		fl.setAlignOnBaseline(true);
		JPanel tablesPanel = new JPanel(fl);
		
		// reverse order to match in game with backpack on right
		for (int i = models.length - 1; i >= 0; i--) {
			models[i] = new BagTableModel();
			tables[i] = new BagTable(models[i]);
			tableWrappers[i] = new TableWrapper(tables[i]);
			tablesPanel.add(tableWrappers[i]);
		}
		
		add(new JScrollPane(tablesPanel), BorderLayout.CENTER);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		refreshBtn.setEnabled(enabled);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Object source = e.getSource();
		
		if (source == refreshBtn) {
			updateInventory();
		}
	}
	
	public void updateInventory() {
		gui.sm.inventoryUpdater.updateAsync();
	}

	@Override
	public void update(Status s) {
		if (s.attached) {
			gold.setMoney(s.copper);
		}
	}

	public void update(Inventory i) {
		slots.setText(String.format(
				"Free Slots/Total: %d/%d",
				i.getTotalFreeBagSlots(), i.getTotalBagSlots()));
		
		models[0].setBag(i.backpack);
		models[1].setBag(i.bag1);
		models[2].setBag(i.bag2);
		models[3].setBag(i.bag3);
		models[4].setBag(i.bag4);
	}
	
	private class TableWrapper extends JPanel {
		JLabel label;
		BagTable table;
		
		public TableWrapper(final BagTable table) {
			super(new GridBagLayout());
			this.label = new JLabel();
			label.setHorizontalAlignment(JLabel.CENTER);
			this.table = table;
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH;
			c.weightx = 0; c.weighty = 0;
			c.gridwidth = 3;
			c.insets.bottom = 5;
			add(this.label, c);
			
			c.gridy++; c.gridwidth = 1;
			c.weightx = 0.5; c.insets.bottom = 0;
			add(Box.createGlue(), c);
			
			c.gridx++; c.weightx = 0;
			add(this.table, c);
			
			c.gridx++; c.weightx = 0.5;
			add(Box.createGlue(), c);
			
			table.getModel().addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					Bag b = ((BagTableModel) table.getModel()).bag;
					if (null != b) {
						label.setText(null != b.bagItem ? b.bagItem.name : "Backpack");
					} else {
						label.setText("(no bag equipped)");
					}
				}
			});
		}
		
		@Override
		public int getBaseline(int width, int height) {
			return 1;
		}
	}
	
	private class BagCellRenderer extends DefaultTableCellRenderer {
		private Border questBorder = BorderFactory.createLineBorder(Color.YELLOW);
		
		public BagCellRenderer() {
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, false, false,
					row, column);
			
			Icon icon = null;
			String text = "";
			setBackground(Color.WHITE);
			setBorder(null);
			
			
			if (value instanceof ItemSet) {
				ItemSet is = (ItemSet) value;
				icon = is.getItem().getIcon();
				
				if (is.quantity > 1)
					text = is.quantity >= 100 ? "*" : String.valueOf(is.quantity);
				
				if (Item.BIND_QUEST == is.getItem().binds) {
					setBorder(questBorder);
				} else {
					setBorder(is.getItem().quality_.lightBorder);
				}
			} else if (((BagTableModel) table.getModel()).getLinearIndex(row, column) < 0) {
				setBackground(Color.BLACK);
			}
			
			setFont(getFont().deriveFont(Font.BOLD));
			setHorizontalTextPosition(JLabel.CENTER);
			setVerticalTextPosition(JLabel.CENTER);
			setForeground(Color.WHITE);
			setIcon(icon);
			setText(text);
			
			return this;
		}
	}
	
	private class BagTable extends JTable implements MouseListener, MouseMotionListener {
		public BagTable(BagTableModel dm) {
			super(dm);
			
			setDefaultRenderer(ItemSet.class, new BagCellRenderer());
			setTableHeader(null);
			setRowHeight(32);
			
			TableColumnModel cm = getColumnModel();
			TableColumn col;
			for (int i = 0; i < cm.getColumnCount(); i++) {
				col = cm.getColumn(i);
				col.setResizable(false);
				col.setMinWidth(32);
				col.setMaxWidth(32);
			}
			setShowGrid(true);
			setGridColor(Color.BLACK);
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			
			setMaximumSize(new Dimension(32 * cm.getColumnCount(), Integer.MAX_VALUE));
			
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		@Override
		public int getBaseline(int width, int height) {
			return 1;
		}

		private void setItemTooltip(MouseEvent e) {
			Point p = e.getPoint();
			int row = rowAtPoint(p);
			int col = columnAtPoint(p);
			ItemSet i = (ItemSet) getValueAt(row, col);
			gui.itemTooltipPane.setItemSet(i);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			setItemTooltip(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() != 1) return;
			
			Point pt = e.getPoint();
			int row = rowAtPoint(pt);
			int col = columnAtPoint(pt);
			ItemSet i = (ItemSet) getValueAt(row, col);
			
			if (null != i) {
				int itemId = i.getItem().id;
				
				jgm.util.Util.openURL(
					String.format(
						jgm.Config.getInstance().get("general.wowdb"),
						itemId
				));
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setItemTooltip(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			gui.itemTooltipPane.setItemSet(null);
		}
	}
	
	private class BagTableModel extends AbstractTableModel {
		private Bag bag;
		
		public void setBag(Bag bag) {
			this.bag = bag;
			fireTableDataChanged();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return ItemSet.class;
		}
		
		@Override
		public String getColumnName(int column) {
			return "";
		}
		
		@Override
		public int getRowCount() {
			if (null == bag)
				return 0;
			return (int) Math.ceil((double) bag.getSlots() / (double) getColumnCount());
		}

		@Override
		public int getColumnCount() {
			return 4;
		}
		
		public int getLinearIndex(int rowIndex, int columnIndex) {
			if (null == bag)
				return -1;
			int index = rowIndex * getColumnCount() + columnIndex;
			index -= bag.getSlots() % getColumnCount();
			return index;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			int index = getLinearIndex(rowIndex, columnIndex);
			return index < 0 ? null : bag.items[index];
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (isCurrentTab()) {
			updateInventory();
		}
	}
}
