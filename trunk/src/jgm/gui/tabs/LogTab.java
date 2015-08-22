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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import jgm.Config;
import jgm.glider.log.ChatLink;
import jgm.glider.log.ChatLinkType;
import jgm.glider.log.ChatLogEntry;
import jgm.glider.log.LogEntry;
import jgm.util.RingBuffer;
import jgm.wow.ItemSet;

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
	
	private LogTable      logTable;
	private LogTableModel logEntries;
	private JScrollPane   jsp;
	
	public LogTab(jgm.gui.GUI gui, String s) {
		super(gui, new BorderLayout(), s);

		this.setBackground(Color.BLACK);
		this.setOpaque(true);
		
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
		Rectangle r = logTable.getBounds();
		r.y = r.height - 2;
		r.height = 1;
		logTable.scrollRectToVisible(r);
		logTable.clearSelection();
		
		if (select) {
			this.select();
		}
		repaint();
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
	
	private class LogTable extends JTable implements MouseListener, MouseMotionListener {
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
			this.addMouseMotionListener(this);
		}
		
		private void updateItemTooltip(MouseEvent e) {
			Point p = e.getPoint();
			int realX = -1;
			int vRow = rowAtPoint(p);
			int vCol = columnAtPoint(p);
			int mRow = convertRowIndexToModel(vRow);
			int mCol = convertColumnIndexToModel(vCol);
			
			if (mRow < 0 || mCol != 2) {
				gui.itemTooltipPane.setItemSet(null);
				return;
			}
			
			int itemId = 0;
			LogEntry entry = ((LogTableModel) dataModel).get(mRow);
			Component c = null;
			FontMetrics fm = null;
			
			if (entry.hasChatLinks()) {
				char[] entryTextChars = null;
				
				for (ChatLink cl : entry.getChatLinks().values()) {
					if (ChatLinkType.ITEM != cl.type)
						continue;
					
					if (cl.pixelOffset < 0 || cl.pixelWidth < 0) {
						if (null == fm) {
							c = getCellRenderer(vRow, vCol).getTableCellRendererComponent(this, "ZZZ", false, false, vRow, vCol);
							fm = c.getFontMetrics(c.getFont());
						}
						
						if (null == entryTextChars) {
							entryTextChars = entry.getText().toCharArray();
						}
						
//						System.out.printf("Measuring %s%n", cl);
						cl.pixelOffset = fm.charsWidth(entryTextChars, 0, cl.offset);
						cl.pixelWidth = fm.charsWidth(entryTextChars, cl.offset, cl.length());
//						System.out.printf("  x=%d,w=%d%n", cl.pixelOffset, cl.pixelWidth);
					}
					
					if (realX < 0) {
						Rectangle rect = getCellRect(vRow, vCol, false);
						realX = p.x - rect.x;
//						System.out.printf("realX/p.x/rect = %d/%d/%s%n", realX, p.x, rect);
					}
					
					if (cl.pixelOffset <= realX && realX < cl.pixelOffset + cl.pixelWidth) {
						try {
							itemId = Integer.parseInt(cl.parts[1]);
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
						break;
					}
				}
			}
			
			if (itemId > 0) {
				if (null == gui.itemTooltipPane.getItemSet() ||
						itemId != gui.itemTooltipPane.getItemSet().getItem().id) {
					ItemSet is = ItemSet.factory(itemId, "FIXME", 1);
					gui.itemTooltipPane.setItemSet(is);
				} else {
					gui.itemTooltipPane.moveTooltip();
				}
			} else {
				gui.itemTooltipPane.setItemSet(null);
			}
		}
		
		public void mouseMoved(MouseEvent e) {
			updateItemTooltip(e);
		}
		
		public void mouseDragged(MouseEvent e) {}
		
		public void mouseEntered(MouseEvent e) {
			updateItemTooltip(e);
		}
		
		public void mouseExited(MouseEvent e) {
			gui.itemTooltipPane.setItemSet(null);
		}
		
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
				ChatLogEntry cEntry = (ChatLogEntry) entry;
				if (cEntry.getSender() == null ||
						!cEntry.fromPlayer) {
					return;
				}

				gui.setWhisperTarget(cEntry.getSender());
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
				fireTableDataChanged();
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
