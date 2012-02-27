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
import jgm.util.RingBuffer;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LogTab extends Tab implements Clearable {
	static Font BASE_FONT = null;
	static Font FONT = null;
	
	static {
		try {
//			BASE_FONT = Font.createFont(Font.TRUETYPE_FONT, jgm.JGlideMon.class.getResourceAsStream("resources/fonts/arialn.ttf"));
//			BASE_FONT = new Font("Arial", 0, 16);
//			FONT = BASE_FONT.deriveFont(18.0f);
		} catch (Throwable e) {}
	}
	
	static HashMap<String, Color> COLOR_MAP = new HashMap<String, Color>();
	
	static {
		COLOR_MAP.put("Chat", new Color(0x00AA00));
//		COLOR_MAP.put("Public Chat", new Color(0xC84646)); // darker version
		COLOR_MAP.put("Public Chat", new Color(0xFFC0C0));
		COLOR_MAP.put("Whisper", new Color(0xFF80FF));
		COLOR_MAP.put("Guild", new Color(0x40FF40));
		COLOR_MAP.put("Say", Color.WHITE);
		COLOR_MAP.put("Yell", new Color(0xFF4040));
		COLOR_MAP.put("Combat", new Color(0xFF0000));
		COLOR_MAP.put("GliderLog", new Color(0xF06514));
		COLOR_MAP.put("Status", COLOR_MAP.get("GliderLog"));
	}
	
	protected JTabbedPane   parent;
	private LogTable      logTable;
	private LogTableModel logEntries;
	private JScrollPane   jsp;
	
	public LogTab(jgm.gui.GUI gui, String s, JTabbedPane tp) {
		super(gui, new BorderLayout(), s);

		this.setBackground(Color.BLACK);
		this.setOpaque(true);
		
		parent     = tp;
		logEntries = new LogTableModel();
		logTable   = new LogTable(logEntries);

		jsp = new JScrollPane(logTable);
		jsp.getViewport().setBackground(Color.BLACK);
		jsp.getViewport().setOpaque(true);
		add(jsp, BorderLayout.CENTER);
	}

	public void add(LogEntry e) {
		add(e, false);
	}
	
	public void add(LogEntry e, boolean select) {
		logEntries.add(e);
		
		// to scroll the added entry into view but not keep it selected
		logTable.changeSelection(
			logTable.getRowCount() - 1, 1, false, false
		);
		logTable.clearSelection();
		
		if (select) {
			this.select();
		}
	}

	public void clear(boolean clearingAll) {
		logEntries.entries.clear();
		logEntries.fireTableDataChanged();
	}
	
	public List<LogEntry> getEntries(int count, Date since) {
		return logEntries.getEntries(count, since);
	}
	
	private class ColorLabelRenderer extends DefaultTableCellRenderer {
		
		public Component getTableCellRendererComponent(
			JTable table, Object value,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
			
			super.getTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column);

			Color color = COLOR_MAP.get(value);
			
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
			if (FONT != null) {
				this.setFont(FONT);
			}
			FontMetrics fm = this.getFontMetrics(this.getFont());
			this.setRowHeight(fm.getHeight());
			
			this.showHorizontalLines = false;
			this.showVerticalLines = false;
			this.setForeground(Color.WHITE);
			this.setBackground(Color.BLACK);
			
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
				if (centry.getSender() == null || !centry.fromPlayer) return;

				ChatTab ct =
					gui.tabsPane.chatLog;
	
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
		private RingBuffer<LogEntry> entries;

		public LogTableModel() {
			super();

			entries = new RingBuffer<LogEntry>(Config.getInstance().getInt("log.maxentries"));
		}

		public void add(LogEntry i) {			
			entries.add(i);
			
			if (entries.size() < entries.capacity())
				fireTableRowsInserted(entries.size(), entries.size());
			else
				fireTableRowsUpdated(0, entries.size());
		}

		public LogEntry get(int row) {
			return entries.get(row);
		}
		
		public List<LogEntry> getEntries(int count, Date since) {
			List<LogEntry> source = null;
			List<LogEntry> copy = new ArrayList<LogEntry>();
			
			if (entries.isEmpty()) {
				return copy;
			}
			
			if (count > 0) {
				source = entries.subList(
					Math.max(0, entries.size() - count), entries.size());
			} else {
				source = entries;
			}
			
			for (LogEntry e : source) {
				if (null == since || since.compareTo(e.timestamp) <= 0) {
					copy.add(e);
				}
			}
			
			return copy;
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
					ret =
						i.supportsHtmlText()
						? "<html>" + i.getHtmlText() : i.getText();
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
