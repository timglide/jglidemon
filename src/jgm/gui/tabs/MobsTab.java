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

import jgm.glider.log.*;
import jgm.wow.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

public class MobsTab extends Tab implements ActionListener, Clearable {
	public  JButton       resetBtn   = new JButton("Reset All");
	
	private long lastUpdateTime = System.currentTimeMillis();
	
	private MobTable      mobTable;
	private MobTableModel mobEntries;
	
	private RepTable repTable;
	private RepTableModel repEntries;
	
	private SkillTable skillTable;
	private SkillTableModel skillEntries;
	
	private Timer repaintTimer;
	
	public MobsTab(jgm.gui.GUI gui) {
		super(gui, new BorderLayout(20, 20), "Mobs/Rep/Skills");
		
		JPanel infoPanel = new JPanel(new GridLayout(1, 0));
		resetBtn.addActionListener(this);
		infoPanel.add(resetBtn);
		add(infoPanel, BorderLayout.NORTH);
		
		
		JPanel jp = new JPanel(new GridLayout(1, 0, 10, 0));
		
		mobEntries = new MobTableModel();
		mobTable   = new MobTable(mobEntries);
		MyPane myp = new MyPane("Mobs", mobTable);
		jp.add(myp);
		
		repEntries = new RepTableModel();
		repTable   = new RepTable(repEntries);
		myp        = new MyPane("Rep", repTable);
		jp.add(myp);
		
		skillEntries = new SkillTableModel();
		skillTable   = new SkillTable(skillEntries);
		myp          = new MyPane("Skills", skillTable);
		jp.add(myp);
		
		add(jp, BorderLayout.CENTER);
		
		repaintTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!MobsTab.this.gui.sm.connector.isConnected())
					return;
				
				mobTable.repaint();
				repTable.repaint();
				skillTable.repaint();
			}
		});
		repaintTimer.start();
	}
	
	public void add(LogEntry e) {
		JTable table = null;
		
		if (e instanceof CombatLogEntry) {
			mobEntries.add((CombatLogEntry) e);
			table = mobTable;
		} else {
			RawChatLogEntry e2 = (RawChatLogEntry) e;
			
			if (e2.hasRep()) {
				repEntries.add(e2);
				table = repTable;
			} else {
				skillEntries.add(e2);
				table = skillTable;
			}
		}
		
		// scroll to top and clear selection
		table.changeSelection(
			0, 0, false, false
		);
		table.clearSelection();
		
		lastUpdateTime = System.currentTimeMillis();
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public List<Mob> getMobs() {
		return mobEntries.getEntries();
	}
	
	public List<Rep> getReps() {
		return repEntries.getEntries();
	}
	
	public List<Skill> getSkills() {
		return skillEntries.getEntries();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetBtn) {
			mobEntries.empty();
			repEntries.empty();
			skillEntries.empty();
			
			lastUpdateTime = System.currentTimeMillis();
		}
	}
	
	public void clear(boolean clearingAll) {
		resetBtn.doClick();
	}
	
	private class MyPane extends JPanel {
		JLabel header = null;
		JScrollPane scrollPane = null;

		public MyPane(String s, final JTable table) {
			setLayout(new BorderLayout(10, 10));
			scrollPane = new JScrollPane(table);
			
			javax.swing.border.TitledBorder ttlBorder =
				BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), s);
			ttlBorder.setTitleFont(Item.TITLE_FONT);
			ttlBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
			setBorder(ttlBorder);
			
			add(scrollPane, BorderLayout.CENTER);
		}
	}
	
	// mob table
	private class MobTable extends JTable {
		public MobTable(TableModel dm) {
			super(dm);

			FontMetrics fm = this.getFontMetrics(this.getFont());
			this.setRowHeight(fm.getHeight());
			
			this.showVerticalLines = false;
			
			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(75);
			cm.getColumn(0).setMaxWidth(75);
			cm.getColumn(1).setResizable(false);
			cm.getColumn(1).setMinWidth(75);
			cm.getColumn(1).setMaxWidth(75);
			cm.getColumn(2).setResizable(false);
			cm.getColumn(2).setMinWidth(75);
			cm.getColumn(2).setMaxWidth(75);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
	
	private enum MobCols {
		COUNT("Count", Integer.class),
		PER_HOUR("Per Hour", Integer.class),
		AVG_XP("Avg XP", Integer.class),
		NAME("Name", String.class)
		;
		
		private MobCols(String name, java.lang.Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}
		
		public final String name;
		public final java.lang.Class<?> clazz;
	}
	
	private static final MobCols[] MOB_COLS = MobCols.values();
	
	private class MobTableModel extends AbstractTableModel {
		private final java.util.Comparator<Mob> comp = Mob.getQuantityComparator();
		
		private ArrayList<Mob> entries;
		private Mob totalMob = null;
		
		public MobTableModel() {
			super();

			entries = new ArrayList<Mob>();
		}

		public void add(CombatLogEntry i) {
//			if (entries.size() > Config.getInstance().getInt("log", "maxentries")) {
//				entries.clear();
//				fireTableDataChanged();
//			}
			
			Mob m = new Mob(i.getFormattedTimestamp(), i.getMobName(), 1, i.getMobXP());
			int index = entries.indexOf(m);
			
			if (index >= 0) {
				entries.get(index).incr(m);
			} else {
				entries.add(m);
				fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
			}
			
			java.util.Collections.sort(entries, comp);
			fireTableRowsUpdated(0, entries.size() - 1);
			increaseTotal(m);
		}
		
		private void increaseTotal(Mob m) {
			if (null == totalMob) {
				totalMob = new Mob("", "Total", m.number, m.xp);
				entries.add(totalMob);
				fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
			} else {
				totalMob.incr(m);
			}
			
			java.util.Collections.sort(entries, comp);
			fireTableRowsUpdated(0, entries.size() - 1);
		}

		public Mob get(int row) {
			return entries.get(row);
		}
		
		@SuppressWarnings("unchecked")
		public java.util.List<Mob> getEntries() {
			return (java.util.List<Mob>) entries.clone();
		}
		
		public void empty() {
			entries.clear();
			totalMob = null;
			this.fireTableDataChanged();
		}
		
		public int getColumnCount() {
			return MOB_COLS.length;
		}

		public int getRowCount() {
			return entries.size();
		}

		public String getColumnName(int col) {
			return MOB_COLS[col].name;
		}

		public Object getValueAt(int row, int col) {
			Mob i = entries.get(row);
			Object ret = null;

			switch (MOB_COLS[col]) {
				case COUNT:
					ret = i.number;
					break;

				case PER_HOUR:
					ret = (int) i.getNumberPerHour();
					break;
					
				case AVG_XP:
					ret = i.xp;
					break;
					
				case NAME:
					ret = i.name;
					break;
			}

			return ret;
		}

		public java.lang.Class<?> getColumnClass(int c) {
			return MOB_COLS[c].clazz;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
	
	
	// rep table
	private class RepTable extends JTable {
		public RepTable(TableModel dm) {
			super(dm);

			FontMetrics fm = this.getFontMetrics(this.getFont());
			this.setRowHeight(fm.getHeight());
			
			this.showVerticalLines = false;
			
			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(75);
			cm.getColumn(0).setMaxWidth(75);
			cm.getColumn(1).setResizable(false);
			cm.getColumn(1).setMinWidth(75);
			cm.getColumn(1).setMaxWidth(75);
			cm.getColumn(2).setResizable(false);
			cm.getColumn(2).setMinWidth(75);
			cm.getColumn(2).setMaxWidth(75);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
	
	private enum RepCols {
		TIME("Time", String.class),
		GAINED("Gained", Integer.class),
		PER_HOUR("Per Hour", Integer.class),
		FACTION("Faction", String.class)
		;
		
		private RepCols(String name, java.lang.Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}
		
		public final String name;
		public final java.lang.Class<?> clazz;
	}
	
	private static final RepCols[] REP_COLS = RepCols.values();
	
	private class RepTableModel extends AbstractTableModel {
		private final java.util.Comparator<Rep> comp = Rep.getAmountComparator();
		
		private ArrayList<Rep> entries;

		public RepTableModel() {
			super();

			entries = new ArrayList<Rep>();
		}

		public void add(RawChatLogEntry i) {
//			if (entries.size() > Config.getInstance().getInt("log", "maxentries")) {
//				entries.clear();
//				fireTableDataChanged();
//			}
			
			Rep r = new Rep(i.getTimestamp(), i.getRepFaction(), i.getRepAmount());
			int index = entries.indexOf(r);
			
			if (index >= 0) {
				entries.get(index).incr(r);
			} else {			
				entries.add(r);
				fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
			}
			
			java.util.Collections.sort(entries, comp);
			fireTableRowsUpdated(0, entries.size() - 1);
		}

		public Rep get(int row) {
			return entries.get(row);
		}
		
		@SuppressWarnings("unchecked")
		public java.util.List<Rep> getEntries() {
			return (java.util.List<Rep>) entries.clone();
		}
		
		public void empty() {
			entries.clear();
			this.fireTableDataChanged();
		}
		
		public int getColumnCount() {
			return REP_COLS.length;
		}

		public int getRowCount() {
			return entries.size();
		}

		public String getColumnName(int col) {
			return REP_COLS[col].name;
		}

		public Object getValueAt(int row, int col) {
			Rep i = entries.get(row);
			Object ret = null;

			switch (REP_COLS[col]) {
				case TIME:
					ret = i.timestamp;
					break;

				case GAINED:
					ret = i.amount;
					break;
					
				case PER_HOUR:
					ret = (int) i.getAmountPerHour();
					break;
					
				case FACTION:
					ret = i.faction;
					break;
			}

			return ret;
		}

		public java.lang.Class<?> getColumnClass(int c) {
			return REP_COLS[c].clazz;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
	
	
	// skill table
	private class SkillTable extends JTable {
		public SkillTable(TableModel dm) {
			super(dm);

			FontMetrics fm = this.getFontMetrics(this.getFont());
			this.setRowHeight(fm.getHeight());
			
			this.showVerticalLines = false;
			
			TableColumnModel cm = getColumnModel();
			cm.getColumn(0).setResizable(false);
			cm.getColumn(0).setMinWidth(75);
			cm.getColumn(0).setMaxWidth(75);
			cm.getColumn(1).setResizable(false);
			cm.getColumn(1).setMinWidth(75);
			cm.getColumn(1).setMaxWidth(75);
			cm.getColumn(2).setResizable(false);
			cm.getColumn(2).setMinWidth(75);
			cm.getColumn(2).setMaxWidth(75);
			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}

	private enum SkillCols {
		TIME("Time", String.class),
		LEVEL("Level", Integer.class),
		PER_HOUR("Per Hour", Integer.class),
		Skill("Faction", String.class)
		;
		
		private SkillCols(String name, java.lang.Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}
		
		public final String name;
		public final java.lang.Class<?> clazz;
	}
	
	private static final SkillCols[] SKILL_COLS = SkillCols.values();
	
	private class SkillTableModel extends AbstractTableModel {
		private final java.util.Comparator<Skill> comp = Skill.getLevelComparator();
		
		private ArrayList<Skill> entries;

		public SkillTableModel() {
			super();

			entries = new ArrayList<Skill>();
		}

		public void add(RawChatLogEntry i) {
//			if (entries.size() > Config.getInstance().getInt("log", "maxentries")) {
//				entries.clear();
//				fireTableDataChanged();
//			}
			
			Skill s = new Skill(i.getTimestamp(), i.getSkillName(), i.getSkillLevel());
			int index = entries.indexOf(s);
			
			if (index >= 0) {
				entries.get(index).incr(s);
			} else {			
				entries.add(s);
				fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
			}
			
			java.util.Collections.sort(entries, comp);
			fireTableRowsUpdated(0, entries.size() - 1);
		}

		public Skill get(int row) {
			return entries.get(row);
		}
		
		@SuppressWarnings("unchecked")
		public List<Skill> getEntries() {
			return (List<Skill>) entries.clone();
		}
		
		public void empty() {
			entries.clear();
			this.fireTableDataChanged();
		}
		
		public int getColumnCount() {
			return SKILL_COLS.length;
		}

		public int getRowCount() {
			return entries.size();
		}

		public String getColumnName(int col) {
			return SKILL_COLS[col].name;
		}

		public Object getValueAt(int row, int col) {
			Skill i = entries.get(row);
			Object ret = null;

			switch (SKILL_COLS[col]) {
				case TIME:
					ret = i.timestamp;
					break;

				case LEVEL:
					ret = i.level;
					break;
					
				case PER_HOUR:
					ret = (int) i.getSkillPerHour();
					break;
					
				case Skill:
					ret = i.name;
					break;
			}

			return ret;
		}

		public java.lang.Class<?> getColumnClass(int c) {
			return SKILL_COLS[c].clazz;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
