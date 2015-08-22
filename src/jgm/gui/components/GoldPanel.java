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
package jgm.gui.components;

import java.text.NumberFormat;

import javax.swing.*;

/**
 * A JPanel with a JLabel for gold, silver, and copper.
 * Also has a gold/silver/copper gif icon next to the number
 * and an optional prefix.
 * @author Tim
 * @since 0.1
 */
public class GoldPanel extends JPanel {
	private NumberFormat numberFormat = NumberFormat.getIntegerInstance();
	private String      title;
	private long         money  = 0;
	private boolean     hideWhenZero = false;

	private        JLabel      ttlLabel = null;
	
	// 0, 1, 2 => gold, silver, copper
	private        JLabel[]    labels = null;
	private static ImageIcon[] icons  = null;
	
	/**
	 * Create a GoldPanel with the given prefix and
	 * an initial amount of 0.
	 * @param prefix
	 */
	public GoldPanel(String prefix) {
		this(0, prefix, false);
	}
	
	/**
	 * Create a GoldPanel with an initial amount of 0.
	 */
	public GoldPanel() {
		this(0, "", false);
	}
	
	/**
	 * Create a GoldPanel with the specified amount.
	 * @param gold The amount of gold
	 * @param silver The amount of silver
	 * @param copper The amount of copper
	 */
	public GoldPanel(int gold, int silver, int copper) {
		this(gscToC(gold, silver, copper), "", false);
	}
	
	/**
	 * Create a new GoldPanel.
	 * @param s A prefix to precede the gold icons with
	 * @param i The amount of copper to represent
	 * @param hide Whether to hide the panel when there is zero copper worth
	 * @param fontColor The foreground color of the labels
	 */
	public GoldPanel(String s, long i, boolean hide, java.awt.Color fontColor) {
		this(i, s, hide);
		setTextColor(fontColor);
	}
	
	public GoldPanel(long i, String s, boolean hide) {
		super();
		if (icons == null) {
			icons = new ImageIcon[3];
			icons[0] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/gold.gif")); 
			icons[1] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/silver.gif"));
			icons[2] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/copper.gif"));
		}
		
		title = s;
		hideWhenZero = hide;
		ttlLabel = new JLabel(title);
		add(ttlLabel);
		
		labels = new JLabel[3];
		for (int n = 0; n < labels.length; n++) {
			labels[n] = new JLabel("0",
				icons[n],
				JLabel.LEFT
			);
			labels[n].setHorizontalTextPosition(JLabel.LEFT);
			add(labels[n]);
		}
		
		setMoney(i);
	}
	
	@Override
	public int getBaseline(int width, int height) {
		return ttlLabel.getBaseline(width, height);
	}
	
	public void setText(String s) {
		title = s;
		ttlLabel.setText(title);
	}
	
	public void setTextColor(java.awt.Color c) {		
		ttlLabel.setForeground(c);
		
		for (int n = 0; n < labels.length; n++) {
			labels[n].setForeground(c);
		}
	}
	
	public long getMoney() {
		return money;
	}
	
	public void setMoney(long c) {
		setMoney(cToGsc(c));
	}
	
	public void setMoney(int[] i) {
		setMoney(i[0], i[1], i[2]);
	}
	
	public void setMoney(int g, int s, int c) {
		money = gscToC(g, s, c);
		
		labels[0].setText(numberFormat.format(g));
		labels[1].setText(numberFormat.format(s));
		labels[2].setText(numberFormat.format(c));
		
		if (g == 0) {
			labels[0].setVisible(false);
			
			if (s == 0) {
				labels[1].setVisible(false);
				
				if (c == 0 && this.hideWhenZero) {
					this.setVisible(false);
				} else {
					this.setVisible(true);
				}
			} else {
				labels[1].setVisible(true);
				this.setVisible(true);
			}
		} else {
			labels[0].setVisible(true);
			labels[1].setVisible(true);
			this.setVisible(true);
		}
	}
	
	public void addMoney(long c) {
		setMoney(money + c);
	}
	
	public void addMoney(int g, int s, int c) {
		addMoney(gscToC(g, s, c));
	}
	
	/**
	 * Convert seperate gold, silver, and copper values into
	 * a single copper value.
	 * @param gold The amount of gold
	 * @param silver The amount of silver
	 * @param copper The amount of copper
	 * @return The total amount of copper
	 */
	public static long gscToC(int gold, int silver, int copper) {
		return copper + silver * 100 + gold * 100 * 100;
	}
	
	/**
	 * Convert a single copper value to individual gold, silver,
	 * and copper values.
	 * @param copper The total amount of copper
	 * @return An array with 3 elements corresponding to 
	 * the amount of gold, silver, and copper.
	 */
	public static int[] cToGsc(long copper) {
		// i = 1234567 (123.45.67)
		
		int[] ret = new int[3];
		
		int i = (int) (copper / 100);       // 12345 (123.45)
		ret[0] = i / 100;           // 123
		ret[1] = i - ret[0] * 100;  // 12345 - 12300 = 45
		ret[2] = (int) (copper - i * 100);  // 1234567 - 1234500 = 67
		
		return ret;
	}
}
