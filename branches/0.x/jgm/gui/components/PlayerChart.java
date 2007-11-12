package jgm.gui.components;

import jgm.util.RingBuffer;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

public class PlayerChart extends JPanel {
	// things that should eventually be made into options
	static final Color BG = Color.black;
	static final Color FG = Color.white;
	static final int MAX_POINTS = 1440; // 2 hrs worth
	
	// other internal stuff
	static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	
	static final Dimension PREF_SIZE = new Dimension(500, 100);
	static final Dimension MIN_SIZE = new Dimension(100, 50);
	
	static final double MIN_VALUE = 0;
	
	static final int X_AXIS_OFFSET = 75;
	static final int Y_AXIS_OFFSET = 50;
	
	static final int TICK_LENGTH = 5;
	
	static final int NUM_Y_TICKS = 9;
	static final int Y_TICK_LABEL_FREQ = 2;
	
	static final int X_TICK_SPACING = 120; // 30 pixels between ticks
	static final int X_TICK_VALUE = 60; // 1 minute per tick
	static final int X_POINT_VALUE = 1; // 5 seconds per data point
	static final int POINTS_PER_X_TICK = X_TICK_VALUE / X_POINT_VALUE; // each data point = 5 seconds -> 12 points per minute
	static final int X_TICK_LABEL_FREQ = 5; // put a label every # of ticks
		
	RingBuffer<DataPoint> playerHealth;
	RingBuffer<DataPoint> playerMana;
	RingBuffer<DataPoint> targetHealth;
	
	YAxis yaxis;
	MainChart chart;
	JScrollPane chartSP;
	
	public PlayerChart() {
		super(new BorderLayout());
		
		playerHealth = new RingBuffer<DataPoint>(MAX_POINTS);
		playerMana = new RingBuffer<DataPoint>(MAX_POINTS);
		targetHealth = new RingBuffer<DataPoint>(MAX_POINTS);
		
/*		// insert some random points
		Random r = new Random();
		long date = System.currentTimeMillis();
		double pH = 0, pM = 0, tH = 0;
		
		for (int i = 0; i < playerHealth.capacity(); i++) {
			Date d = new Date(date);
			date += 5000;
			playerHealth.add(new DataPoint(d, pH));
			playerMana.add(new DataPoint(d, pM));
			targetHealth.add(new DataPoint(d, tH));
			
			pH += 5 * (r.nextDouble() - 0.5);
			pM += 5 * (r.nextDouble() - 0.5);
			tH += 5 * (r.nextDouble() - 0.5);
		}
		*/
		
		yaxis = new YAxis();
		chart = new MainChart();
		chartSP = new JScrollPane(chart, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chart.setAutoscrolls(true);
		chartSP.getHorizontalScrollBar().setUnitIncrement(X_TICK_SPACING);
		chartSP.setBorder(null);
		
		add(yaxis, BorderLayout.WEST);
		add(chartSP, BorderLayout.CENTER);
	}
	
	public void addData(double pHealth, double pMana, double tHealth) {
		System.out.printf("Adding data: %s, %s, %s\n", pHealth, pMana, tHealth);
		Date now = new Date();
		playerHealth.add(new DataPoint(now, pHealth));
		playerMana.add(new DataPoint(now, pMana));
		targetHealth.add(new DataPoint(now, tHealth));
		this.repaint();
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
			ratio = (double) (size.height - 1 - X_AXIS_OFFSET - getHSBHeight()) / (maxVal - minVal);
		//}
		
		return size.height - 1 - X_AXIS_OFFSET - getHSBHeight() - ((int) Math.round(ratio * y));
	}
	
	static RenderingHints rh = null;
	
	static {
		rh = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
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
		
		public void paint(Graphics g) {
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
	
				// TODO move to main grid component
//				g2.setColor(Color.lightGray);
//				g2.drawLine(Y_AXIS_OFFSET, y, size.width, y);
				
//				g2.setColor(FG);
				String s = String.format("%.1f", curMarker);
				
				if (i % Y_TICK_LABEL_FREQ == 0) {
					Rectangle2D bounds = fm.getStringBounds(s, g2);
					// int w = fm.stringWidth(s);
					int w = (int) bounds.getWidth();
					g2.drawString(s, size.width - TICK_LENGTH - w - 5, y + (int) (bounds.getHeight() / 2.0));
				}
				
				curMarker += markerDelta;
			}
		}
	}
	
	class MainChart extends JComponent {
		public MainChart() {
			setOpaque(true);
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
		
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			doRenderingHints(g2);
			
			// fill background
			Dimension size = this.getSize();
			FontMetrics fm = g2.getFontMetrics();
			g2.setBackground(BG);
			g2.clearRect(0, 0, size.width, size.height);
			g2.setFont(FONT);
			
			// draw axes
			g2.setColor(FG);
			// x-axis
			int y = yCoordToPixel(0);
			g2.drawLine(0, y, size.width - 1, y);

			
			// draw x ticks
			int dx = X_TICK_SPACING;
			// int dx = size.width / playerHealth.size() * 4;
			int x = dx;
			
			for (; x < size.width; x += dx) {
				g2.drawLine(x, y, x, y + TICK_LENGTH);
			}
			
			// draw data
			g2.setColor(Color.green);
			
			// doubles to prevent roundoff error
			double xx = 0.0;
			// dx = 30 / (60 / 20);
			// 30 pixels per minute (i.e. per tick),
			// 12 points per minute @ 5 seconds each
			// -> each data point = 2.5 pixels
			double dxx = (double) X_TICK_SPACING / ((double) X_TICK_VALUE / (double) X_POINT_VALUE);
			// dx = size.wisdth / playerHealth.size();
			
			for (int i = 0; i < playerHealth.size() - 2; i++) {
				// draw time label on every other tick
				if (i % (POINTS_PER_X_TICK * X_TICK_LABEL_FREQ) == 0) {
					AffineTransform savedAT = g2.getTransform();
					g2.setColor(FG);
					g2.translate(xx, yCoordToPixel(0) + TICK_LENGTH * 2);
					g2.rotate(Math.PI / 6.0);
					g2.drawString(playerHealth.get(i).getFormattedTimestamp(), 0, fm.getHeight());
					g2.setTransform(savedAT);
				}
				
				g2.setColor(Color.green);
				g2.drawLine((int) xx, yCoordToPixel(playerHealth.get(i).value), (int) (xx + dxx), yCoordToPixel(playerHealth.get(i + 1).value));
				g2.setColor(Color.blue);
				g2.drawLine((int) xx, yCoordToPixel(playerMana.get(i).value), (int) (xx + dxx), yCoordToPixel(playerMana.get(i + 1).value));
				g2.setColor(Color.yellow);
				g2.drawLine((int) xx, yCoordToPixel(targetHealth.get(i).value), (int) (xx + dxx), yCoordToPixel(targetHealth.get(i + 1).value));
				
				xx += dxx;
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
