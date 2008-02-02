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

import jgm.Config;
import jgm.glider.log.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LogTab extends Tab {
	protected JTabbedPane   parent;
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
			this.select();
		}
	}

	public void clear() {
		logEntries.entries.clear();
		logEntries.fireTableDataChanged();
	}
	
	static HashMap<String, Color> TYPE_COLORS = new HashMap<String, Color>();
	
	static {
		TYPE_COLORS.put("Chat", new Color(0x00AA00));
		TYPE_COLORS.put("Public Chat", new Color(0xC84646));
		TYPE_COLORS.put("Whisper", Color.MAGENTA);
		TYPE_COLORS.put("Guild", Color.GREEN);
		TYPE_COLORS.put("Say", Color.DARK_GRAY);
		TYPE_COLORS.put("Combat", new Color(0x800000));
		TYPE_COLORS.put("GliderLog", new Color(0xF06514));
		TYPE_COLORS.put("Status", TYPE_COLORS.get("GliderLog"));
	}
	
	private class ColorLabelRenderer extends DefaultTableCellRenderer {
		
		public Component getTableCellRendererComponent(
			JTable table, Object value,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
			
			super.getTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column);

			Color color = TYPE_COLORS.get(value);
			
			if (color != null)
				this.setForeground(color);
		
			return this;
		}
	};
	
	private class LogTable extends JTable implements MouseListener {
		ColorLabelRenderer colorLabelRenderer = new ColorLabelRenderer();
		
		public LogTable(TableModel dm) {
			super(dm);

//			System.out.println("LogTable Orig Font: " + this.getFont().toString());		
//			this.setFont(this.getFont().deriveFont(Font.PLAIN, 22.0f));
			FontMetrics fm = this.getFontMetrics(this.getFont());
			this.setRowHeight(fm.getHeight());
			
			this.showVerticalLines = false;
			
			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(75);
			cm.getColumn(0).setMaxWidth(75);
			cm.getColumn(1).setResizable(false);
			cm.getColumn(1).setMinWidth(100);
			cm.getColumn(1).setMaxWidth(100);
			cm.getColumn(1).setCellRenderer(colorLabelRenderer);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.addMouseListener(this);
		}
		
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		// upon double clicking a chat entry
		// switch to the send keys tab and fill in the 
		// fields to whisper that person
		public void mouseClicked(MouseEvent e) {
			//System.out.println(e);
			
			if (e.getClickCount() != 2) return;
			
			Point pt = e.getPoint();
			int row = this.rowAtPoint(pt);
	
			LogTableModel tm = (LogTableModel) this.dataModel;
			LogEntry entry = tm.get(row);
			
			if (entry instanceof ChatLogEntry) {
				ChatLogEntry centry = (ChatLogEntry) entry;
				if (centry.getSender() == null) return;

				ChatTab ct =
					jgm.JGlideMon.instance.gui.tabsPane.chatLog;
	
				ct.type.setSelectedItem(jgm.glider.ChatType.WHISPER);
				ct.to.setText(centry.getSender());
				ct.keys.setText("");
				ct.select();
				ct.keys.requestFocusInWindow();
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
			fireTableRowsInserted(entries.size(), entries.size());
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
