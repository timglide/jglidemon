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

import jgm.util.RingBuffer;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

public class PlayerChart extends JPanel {
	// things that should eventually be made into options
	static final Color BG = Color.black;
	static final Color FG = Color.white;
	static final int DEFAULT_MAX_POINTS = 7200; // 2 hrs worth
	
	// other internal stuff
	static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	static final Font LEGEND_FONT = FONT.deriveFont(18.0f);
	
	static final Dimension PREF_SIZE = new Dimension(500, 100);
	static final Dimension MIN_SIZE = new Dimension(100, 50);
	
	static final double MIN_VALUE = 0;
	
	static final int X_AXIS_OFFSET = 50;
	static final int Y_AXIS_OFFSET = 50;
	static final int HEADER_HEIGHT = 25;
	
	static final int TICK_LENGTH = 5;
	
	static final int NUM_Y_TICKS = 4;
	static final int Y_TICK_LABEL_FREQ = 1;
	
	static final int X_TICK_SPACING = 120; // 30 pixels between ticks
	static final int X_TICK_VALUE = 60; // 1 minute per tick
	static final int X_POINT_VALUE = 1; // 5 seconds per data point
	static final int POINTS_PER_X_TICK = X_TICK_VALUE / X_POINT_VALUE; // each data point = 5 seconds -> 12 points per minute
	static final int X_TICK_LABEL_FREQ = 1; // put a label every # of ticks
		
	RingBuffer<DataPoint> playerHealth;
	RingBuffer<DataPoint> playerMana;
	RingBuffer<DataPoint> targetHealth;
	
	YAxis yaxis;
	MainChart chart;
	JScrollPane chartSP;
		
	public PlayerChart() {
		this(DEFAULT_MAX_POINTS);
	}
	
	public PlayerChart(int numPoints) {
		super(new BorderLayout());
		
		playerHealth = new RingBuffer<DataPoint>(numPoints);
		playerMana = new RingBuffer<DataPoint>(numPoints);
		targetHealth = new RingBuffer<DataPoint>(numPoints);
				
		this.setBorder(null);
		yaxis = new YAxis();
		yaxis.setBorder(null);
		chart = new MainChart();
		chart.setBorder(null);
		chartSP = new JScrollPane(chart, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chartSP.getHorizontalScrollBar().setUnitIncrement(X_TICK_SPACING);
		chartSP.setBorder(null);
		chartSP.setViewportBorder(null);

		add(yaxis, BorderLayout.WEST);
		add(chartSP, BorderLayout.CENTER);
	}
	
	java.util.Timer dummyDataTimer = null;
	
	public void startDummyData(final long period) {
		// insert some random points
		dummyDataTimer = new java.util.Timer();
		
		dummyDataTimer.scheduleAtFixedRate(
			new TimerTask() {
				Random r = new Random();
				double pH = 0, pM = 0, tH = 0;
				
				public void run() {
					addData(pH, pM, tH);
					
					double rnd = (r.nextDouble() - 0.5) * 15;
					
					if (pH >= 100.0) {
						pH -= Math.abs(rnd);
					} else if (pH <= 0.0) {
						pH += Math.abs(rnd);
					} else {
						pH += rnd;
					}
					
					rnd = (r.nextDouble() - 0.5) * 15;
					if (pM >= 100.0) {
						pM -= Math.abs(rnd);
					} else if (pM <= 0.0) {
						pM += Math.abs(rnd);
					} else {
						pM += rnd;
					}
					
					rnd = (r.nextDouble() - 0.5) * 15;
					if (tH >= 100.0) {
						tH -= Math.abs(rnd);
					} else if (tH <= 0.0) {
						tH += Math.abs(rnd);
					} else {
						tH += rnd;
					}
				}
			}, 0, period
		);
	}
	
	public void stopDummyData() {
		if (dummyDataTimer == null) return;
		
		dummyDataTimer.cancel();
	}
	
	public void addData(double pHealth, double pMana, double tHealth) {
		addData(pHealth, pMana, tHealth, true);
	}
	
	public void addData(double pHealth, double pMana, double tHealth, boolean autoScroll) {
//		System.out.printf("Adding data: %s, %s, %s\n", pHealth, pMana, tHealth);
		
		try {
		Date now = new Date();
		playerHealth.add(new DataPoint(now, pHealth));
		playerMana.add(new DataPoint(now, pMana));
		targetHealth.add(new DataPoint(now, tHealth));
		chart.revalidate();
		chart.repaint();
		
		if (autoScroll) {
			Dimension pref = chart.getPreferredSize();
			Rectangle rect = new Rectangle(pref.width - 2, 0, 1, 1);
			chartSP.getViewport().scrollRectToVisible(rect);
		}
		
		} catch (Throwable t) {
			System.out.println("---> Ex: " + t.getClass().getName() + ": " + t.getMessage());
		}
	}
	
	
	public Dimension getMinimumSize() {
		return MIN_SIZE;
	}
	
	double getMinValue() {
		return MIN_VALUE;
	}
	
	volatile boolean needToUpdateMax = true;
	double lastMax = 1.0;
	// returns the max value of all 3 series
	double getMaxValue() {
		return 100.0;
		
/*		if (!needToUpdateMax) {
			return lastMax;
		}
		
		needToUpdateRatio = true;
		needToUpdateMax = false;

		if (playerHealth.size() == 0) {
			return lastMax;
		}
		
		lastMax = Math.max(
			Collections.max(playerHealth).value,
			Math.max(
				Collections.max(playerMana).value,
				Collections.max(targetHealth).value
			)
		);
			
		return lastMax; */
	}
	
	int getHSBHeight() {
		return chartSP.getHorizontalScrollBar().getHeight();
	}
	
	// map a y coordinate from the logical unit to
	// an actual pixel
	int yCoordToPixel(double y) {
		Dimension size = yaxis.getSize();
		
		return yCoordToPixel(y, size, getMaxValue(), getMinValue());
	}
	
	volatile boolean needToUpdateRatio = true;
	double lastRatio = 1;
	
	int yCoordToPixel(double y, Dimension size, double maxVal, double minVal) {
		double ratio = lastRatio;
		
		//if (needToUpdateRatio) {
			needToUpdateRatio = false;
			// units per pixel
			ratio = (double) (size.height - 1 - HEADER_HEIGHT - X_AXIS_OFFSET - getHSBHeight()) / (maxVal - minVal);
		//}
		
		return size.height - 1 - X_AXIS_OFFSET - getHSBHeight() - ((int) Math.round(ratio * y));
	}
	
	static RenderingHints rh = null;
	
	static {
		rh = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		
		try {
			rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		} catch (NoSuchFieldError e) {} // java 1.5 compat
	}
	
	protected static final void doRenderingHints(Graphics2D g) {
		g.setRenderingHints(rh);
	}
	
	class DataPoint implements Comparable<DataPoint> {
		Date timestamp;
		double value;
		
		public DataPoint(double d) {
			this(new Date(), d);
		}
		
		public DataPoint(Date dt, double d) {
			timestamp = dt;
			value = d;
		}
		
		public String getFormattedTimestamp() {
			return jgm.glider.log.LogEntry.getFormattedTimestamp(timestamp);
		}
		
		public String toString() {
			return String.format("[%s] %.1f", getFormattedTimestamp(), value);
		}
		
		public int compareTo(DataPoint d) {
			if (value > d.value)
				return 1;
			
			if (value < d.value)
				return -1;
			
			return 0;
		}
	}
	
	class YAxis extends JComponent {
		Dimension PREF_SIZE = new Dimension(100, 0);
		
		public YAxis() {

		}
		
		public Dimension getPreferredSize() {
			return PREF_SIZE;
		}
		
		// this won't be redrawn very often, only when the
		// window is resized
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			doRenderingHints(g2);
			Dimension size = getSize();
			
			// fill background
			g2.setBackground(BG);
			g2.clearRect(0, 0, size.width, size.height);
			
			// draw axes
			g2.setColor(FG);
			g2.setFont(FONT);
			
			// x-axis
			int y = yCoordToPixel(0);
			g2.drawLine(0, y, size.width - 1, y);
			// y-axis
			g2.drawLine(size.width - 1, 0, size.width - 1, size.height);
			
			// draw labels
			// String str = "Time";
			// g2.drawString(str, x, y)
			
			
			// draw ticks and markers
			
			// vertical
			// int dy = (size.height - X_AXIS_OFFSET - scrollBarHeight) / NUM_Y_TICKS;
			double markerDelta = (getMaxValue() - getMinValue()) / (double) NUM_Y_TICKS;
			double curMarker = getMinValue() + markerDelta;
			FontMetrics fm = g2.getFontMetrics();
			
		//	System.out.printf("max/min/delta = %s/%s/%s\n", getMaxValue(), getMinValue(), markerDelta);
			
			//int y = size.height - X_AXIS_OFFSET - scrollBarHeight - dy;
			for (int i = 0; i < NUM_Y_TICKS; i++) {
				y = yCoordToPixel(curMarker);
		//		System.out.printf("Tick %s y=%s, curMarker=%s\n", i, y, curMarker);
				g2.drawLine(size.width - 1 - TICK_LENGTH, y, size.width - 1, y);
	
				String s = String.format("%.0f%%", curMarker);
				
				if (i % Y_TICK_LABEL_FREQ == 0) {
					Rectangle2D bounds = fm.getStringBounds(s, g2);
					// int w = fm.stringWidth(s);
					int w = (int) bounds.getWidth();
					g2.drawString(s, size.width - TICK_LENGTH - w - 5, y + (int) (bounds.getHeight() / 2.0));
				}
				
				curMarker += markerDelta;
			}
			
			
			// draw legend
			g2.setFont(LEGEND_FONT);
			fm = g2.getFontMetrics();
			
			int x = 5;
			y = yCoordToPixel(0) + TICK_LENGTH * 2;
			int h = (int) (fm.getHeight() * 0.6666666667);
			int w = h;
			
			g2.setColor(Color.green);
			g2.fillRect(x, y, w, h);
			g2.drawString("Health", x + w + 5, y + h);
			
			y += h + 5;
			
			g2.setColor(Color.blue);
			g2.fillRect(x, y, w, h);
			g2.drawString("Mana", x + w + 5, y + h);
			
			y += h + 5;
			
			g2.setColor(Color.yellow);
			g2.fillRect(x, y, w, h);
			g2.drawString("Target", x + w + 5, y + h);
		}
	}
	
	class MainChart extends JComponent {
		public MainChart() {
			setOpaque(true);
			setBackground(BG);
		}
		
		final Dimension MIN_SIZE = new Dimension(300, 300);
		public Dimension getMinimumSize() {
			return MIN_SIZE;
		}
		
		public Dimension PREF_SIZE = new Dimension(1000, 0);
		public Dimension getPreferredSize() {
			// width = total data points * width per data point
			// width per data point = X_TICK_SPACING / POINTS_PER_X_TICK
			// total data points = playerHealth.size()
			return new Dimension(playerHealth.size() * (X_TICK_SPACING / POINTS_PER_X_TICK), 0);
			// return PREF_SIZE;
		}
		
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			doRenderingHints(g2);
			
			// fill background
			Dimension size = this.getSize();
			Rectangle clip = g2.getClipBounds();
//			Rectangle visible = this.getVisibleRect();
			
//			System.out.println("Clip: " + clip);
//			System.out.println("Vis:  " + visible);
			
//			FontMetrics fm = g2.getFontMetrics();
			g2.setBackground(BG);
			g2.clearRect(clip.x, clip.y, clip.width, size.height);
			g2.setFont(FONT);
			
			// draw grid lines
			double markerDelta = (getMaxValue() - getMinValue()) / (double) NUM_Y_TICKS;
			double curMarker = getMinValue() + markerDelta;
			int y = 0;
			
			g2.setColor(Color.lightGray);
			for (int i = 0; i < NUM_Y_TICKS; i++) {
				y = yCoordToPixel(curMarker);
				g2.drawLine(clip.x, y, clip.x + clip.width - 1, y);
			
				curMarker += markerDelta;
			}
			
			
			// draw axes
			g2.setColor(FG);
			// x-axis
			y = yCoordToPixel(0);
			g2.drawLine(clip.x, y, clip.x + clip.width - 1, y);
			
			
			// doubles to prevent roundoff error
			
			// need to draw labels separately from data so that the
			// whole label will get rendered
			// this will always redraw the visible labels
			// (as opposed to using the clip to only draw new ones)
			double dxx = X_TICK_SPACING * X_TICK_LABEL_FREQ;
//			int startIndex = (int) (visible.x / dxx);
//			int endIndex = startIndex + (int) ((visible.width - 1) / dxx);
			int startIndex = 0;
			int endIndex = playerHealth.size();
//			System.out.printf("  Ticks S=%s, E=%s\n", startIndex, endIndex);
			double xx = startIndex * dxx;


			// draw x ticks
			int dx = X_TICK_SPACING;
			int x = (clip.x / dx) * X_TICK_SPACING;
			
			int diff = playerHealth.totalInserts() - playerHealth.size();
			int offset = diff % POINTS_PER_X_TICK;
			x -= X_TICK_SPACING * ((double) offset / (double) POINTS_PER_X_TICK);
			
//			for (; x < clip.x + clip.width; x += dx) {
//				g2.drawLine(x, y, x, y + TICK_LENGTH);
//			}
			
//			int yLbl = y + 2 * TICK_LENGTH + fm.getHeight();
			
			for (int i = startIndex; i < endIndex; i++) {
				if (i % POINTS_PER_X_TICK == 0) {
					g2.drawLine(x, y, x, y + TICK_LENGTH);
					
//					if (i % (POINTS_PER_X_TICK * X_TICK_LABEL_FREQ) == 0) {
//						System.out.println("   drawing label i=" + i);
//						String str = playerHealth.get(i).getFormattedTimestamp();
//						g2.drawString(str, (int) (x /*- fm.stringWidth(str) / 2.0*/), yLbl);
//						xx += dxx;
//					}
					
					x += dx;
				}
			}
			
			
			// now labels
/*			y = yCoordToPixel(0) + 2 * TICK_LENGTH + fm.getHeight();
			for (int i = startIndex;
				 i < endIndex && i < playerHealth.size(); i++) {
				// draw time label on every other tick
				if (i % (POINTS_PER_X_TICK * X_TICK_LABEL_FREQ) == 0) {
//					System.out.println("   drawing label i=" + i);
					String str = playerHealth.get(i).getFormattedTimestamp();
*/ //					g2.drawString(str, (int) (xx /*- fm.stringWidth(str) / 2.0*/), y);
	/*				xx += dxx;
				}
			}*/
			
			// need to find the closest start point before clip.x
			// and the closest point end point after clip.x + clip.width
			dxx = (double) X_TICK_SPACING / ((double) X_TICK_VALUE / (double) X_POINT_VALUE);
			startIndex = Math.max(Math.min((int) (clip.x / dxx), playerHealth.size() - 1), 0);
			endIndex = Math.max(Math.min((int) ((clip.x + clip.width - 1) / dxx), playerHealth.size() - 1), 0);
			//System.out.printf("  S=%s, E=%s\n", startIndex, endIndex);
			xx = startIndex * dxx;
			
			// can't draw one point
			if (endIndex - startIndex > 1 && playerHealth.size() > 1) {
				int lastTHY = yCoordToPixel(targetHealth.get(startIndex).value);
				int curTHY = 0;
				int lastPMY = yCoordToPixel(playerMana.get(startIndex).value);
				int curPMY = 0;
				int lastPHY = yCoordToPixel(playerHealth.get(startIndex).value);
				int curPHY = 0;
				
				for (int i = startIndex + 1; i < endIndex && i < playerHealth.size(); i++) {
					g2.setColor(Color.yellow);
					curTHY = yCoordToPixel(targetHealth.get(i).value);
					g2.drawLine((int) xx, lastTHY, (int) (xx + dxx), curTHY);
					lastTHY = curTHY;
					
					g2.setColor(Color.blue);
					curPMY = yCoordToPixel(playerMana.get(i).value);
					g2.drawLine((int) xx, lastPMY, (int) (xx + dxx), curPMY);
					lastPMY = curPMY;
					
					g2.setColor(Color.green);
					curPHY = yCoordToPixel(playerHealth.get(i).value);
					g2.drawLine((int) xx, lastPHY, (int) (xx + dxx), curPHY);
					lastPHY = curPHY;
				       
					xx += dxx;
				}
			}
			
/*			dx = (int) (size.width / (double) values.length);
			x = 0;
			
			for (int i = 0; i < values.length - 1; i++) {
				g2.drawLine(x, yCoordToPixel(values[i]), x + dx, yCoordToPixel(values[i+1]));
				x += dx;
			}*/
		}
	}
}
