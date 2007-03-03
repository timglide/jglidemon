package jgm.gui.tabs;

import jgm.Config;
import jgm.glider.log.*;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LogTab extends Tab {
	private JTabbedPane   parent;
	private LogTable      logTable;
	private LogTableModel logEntries;
	
	public LogTab(String s, JTabbedPane tp) {
		super(new BorderLayout(), s);

		parent     = tp;
		logEntries = new LogTableModel();
		logTable   = new LogTable(logEntries);

		add(new JScrollPane(logTable), BorderLayout.CENTER);
	}

	public void add(LogEntry e) {
		add(e, false);
	}
	
	public void add(LogEntry e, boolean select) {
		logEntries.add(e);
		logTable.changeSelection(
			logTable.getRowCount() - 1, 1, false, false
		);
		
		if (select) {
			parent.setSelectedIndex(getIndex());
		}
	}

	private class LogTable extends JTable implements MouseListener {
		public LogTable(TableModel dm) {
			super(dm);

			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(75);
			cm.getColumn(0).setMaxWidth(75);
			cm.getColumn(1).setResizable(false);
			cm.getColumn(1).setMinWidth(100);
			cm.getColumn(1).setMaxWidth(100);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.addMouseListener(this);
		}
		
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		// upon double clicking a whisper or say entry,
		// switch to the send keys tab and fill in the 
		// fields to whisper that person
		public void mouseClicked(MouseEvent e) {
			//System.out.println(e);
			
			if (e.getClickCount() != 2) return;
			
			Point pt = e.getPoint();
			int row = this.rowAtPoint(pt);
	
			LogTableModel tm = (LogTableModel) this.dataModel;
			LogEntry entry = tm.get(row);
			
			if (entry instanceof WhisperEntry) {
				WhisperEntry wentry = (WhisperEntry) entry;
				String t = wentry.getType();
				
				if (t.contains("Whisper") || t.contains("Say")) {
					SendKeysTab skt =
						jgm.JGlideMon.instance.gui.tabsPane.sendKeys;

					skt.type.setSelectedItem("Whisper");
					skt.to.setText(wentry.from);
					skt.keys.setText("");
					parent.setSelectedIndex(skt.getIndex());
					skt.keys.requestFocus();
				}
			}
		}
	}

	private static final String[] columnNames = {"Time", "Type", "Text"};
	
	private class LogTableModel extends AbstractTableModel {
		private Vector<LogEntry> entries;

		public LogTableModel() {
			super();

			entries = new Vector<LogEntry>();
		}

		public void add(LogEntry i) {
			if (entries.size() > Config.getInstance().getInt("log", "maxentries")) {
				entries.clear();
				fireTableDataChanged();
			}
			
			entries.add(i);
			fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
		}

		public LogEntry get(int row) {
			return entries.get(row);
		}
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return entries.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			LogEntry i = entries.get(row);
			Object ret = null;

			switch (col) {
				case 0:
					ret = i.getFormattedTimestamp();
					break;

				case 1:
					ret = i.getType();
					break;
					
				case 2:
					ret = i.getText();
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
