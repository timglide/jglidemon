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
import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import jgm.glider.Friend;
import jgm.glider.log.ChatLogEntry;
import jgm.glider.log.GliderLogEntry;
import jgm.glider.log.LogEntry;
import jgm.gui.panes.TabsPaneBase;
//import javax.swing.table.TableColumnModel;

public class UrgentTab extends Tab implements Clearable {
//	static Logger log = Logger.getLogger(ChatTab.class.getName());
	
	private TabsPaneBase tabs;
	public LogTab logs;
	public FollowersTab followers;
	
	public UrgentTab(jgm.gui.GUI gui) {
		super(gui, new BorderLayout(), "Urgent");
		
		tabs = new TabsPaneBase(gui);
		logs = new LogTab(gui, "Logs");
		followers = new FollowersTab(gui);
		
		addTab(logs);
		addTab(followers);	
		
		add(tabs, BorderLayout.CENTER);
		
		validate();
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t);
	}
	
	public void add(LogEntry e, boolean select) {
		if (select) 
			this.select();
		
		if (e instanceof ChatLogEntry) {
			ChatLogEntry ce = (ChatLogEntry) e;
			
			if (ce.fromPlayer && "Whisper".equalsIgnoreCase(ce.getChannel())) {
				gui.tabsPane.overviewTab.incrementWhispers();
			}
		}
		
		logs.add(e, select);
	}
	
	public void clear(boolean clearingAll) {
		tabs.clear(clearingAll);
	}
	
	public class FollowersTab extends Tab implements Clearable {
		FollowersTable      table;
		FollowersTableModel entries;
		
		public FollowersTab(jgm.gui.GUI gui) {
			super(gui, new BorderLayout(), "Followers");
			entries = new FollowersTableModel();
			table = new FollowersTable(entries);
			add(new JScrollPane(table), BorderLayout.CENTER);
		}
		
		public void add(GliderLogEntry e) {
			entries.add(e);
		}
		
		public void clear(boolean b) {
			entries.empty();
		}
		
		private class FollowersTable extends JTable {
			public FollowersTable(TableModel dm) {
				super(dm);

				FontMetrics fm = this.getFontMetrics(this.getFont());
				this.setRowHeight(fm.getHeight());
				
				this.showVerticalLines = false;
				
//				TableColumnModel cm = getColumnModel();
//				cm.getColumn(0).setResizable(false);
//				cm.getColumn(0).setMinWidth(75);
//				cm.getColumn(0).setMaxWidth(75);
//				cm.getColumn(1).setResizable(false);
//				cm.getColumn(1).setMinWidth(75);
//				cm.getColumn(1).setMaxWidth(75);
				
				this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
		}

		private class FollowersTableModel extends AbstractTableModel {
			private final String[] columnNames =
				{"Last Seen", "Last Event", "# Encounters", "# Follow Alerts", "Name", "ID", "Race/Class"};
			
			private Vector<Friend> entries;

			public FollowersTableModel() {
				super();

				entries = new Vector<Friend>();
			}

			public void add(GliderLogEntry i) {
//				if (entries.size() > Config.getInstance().getInt("log", "maxentries")) {
//					entries.clear();
//					fireTableDataChanged();
//				}
				
				Friend f = i.friend;
				
				if (null == f) return;
				
				int index = entries.indexOf(f);
				
				if (index >= 0) {
					entries.get(index).update(f);
				} else {
					entries.add(f);
					fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
				}
				
				java.util.Collections.sort(entries);
				fireTableRowsUpdated(0, entries.size() - 1);
			}

			public Friend get(int row) {
				return entries.get(row);
			}
			
			public void empty() {
				entries.clear();
				this.fireTableDataChanged();
			}
			
			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				return entries.size();
			}

			public String getColumnName(int col) {
				return columnNames[col];
			}

			public Object getValueAt(int row, int col) {
				Friend i = entries.get(row);
				Object ret = null;
				
//				{"Last Seen", "Last Status", "# Encounters", "# Follow Alerts", "Name", "ID", "Race/Class"};
				switch (col) {
					case 0:
						ret = LogEntry.getFormattedTimestamp(i.timestamp);
						break;
					case 1: ret = i.status; break;
					case 2: ret = i.encounters; break;
					case 3: ret = i.followingTimes; break;
					case 4: ret = i.name; break;
					case 5:	ret = i.id; break;
					case 6: ret = i.race; break;
					
				}

				return null != ret ? ret : "";
			}

			public java.lang.Class<?> getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}
		}
	}
}
