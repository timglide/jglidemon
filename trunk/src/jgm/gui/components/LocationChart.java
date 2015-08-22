package jgm.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jgm.glider.Status;
import jgm.util.RingBuffer;

public class LocationChart extends JPanel {
	static final int DEFAULT_MAX_POINTS = 7200; // 2 hrs worth
	
	RingBuffer<DataPoint> data;
	float minX, maxX, minY, maxY;
	
	MainChart mainChart;
	
	public LocationChart() {
		this(DEFAULT_MAX_POINTS);
	}
	
	public LocationChart(int numPoints) {
		super(new BorderLayout());
		data = new RingBuffer<DataPoint>(numPoints);
		minX = maxX = minY = maxY = Float.NaN;
		mainChart = new MainChart();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(true);
//		panel.setBackground(Color.BLACK);
		panel.add(mainChart, BorderLayout.CENTER);
		
		JScrollPane jsp = new JScrollPane(panel);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		jsp.setBackground(Color.BLACK);
//		jsp.getViewport().setBackground(Color.BLACK);
		add(jsp, BorderLayout.CENTER);
	}
	
	public void reset() {
		minX = maxX = minY = maxY = Float.NaN;
		data.clear();
		repaint();
	}
	
	public void update(Status s) {
		DataPoint newPoint = null;
		DataPoint prevPoint = null;
		boolean addPoint = true;
		
		try {
			newPoint = new DataPoint(s);
		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
			return;
		}
		
		if (!data.isEmpty()) {
			prevPoint = data.get(data.size() - 1);
			
			if (prevPoint.mapId != newPoint.mapId) {
				reset();
			} else if (prevPoint.isCloseTo(newPoint)) {
				addPoint = false;
				prevPoint.hitCount++;
			}
		}
		
		if (addPoint) {
			data.add(newPoint);
			
			if (Float.isNaN(minX) || newPoint.x < minX)
				minX = newPoint.x;
			if (Float.isNaN(maxX) || newPoint.x > maxX)
				maxX = newPoint.x;
			if (Float.isNaN(minY) || newPoint.y < minY)
				minY = newPoint.y;
			if (Float.isNaN(maxY) || newPoint.y > maxY)
				maxY = newPoint.y;
		}
		
		revalidate();
		repaint();
	}
	
	static final Pattern locationSplitPattern = Pattern.compile("[.,]\\s+");
	
	static class DataPoint implements Comparable<DataPoint> {
		Date timestamp;
		float x, y, z;
		int mapId;
		int hitCount;
		
		public DataPoint(Status s) {
			String[] parts = locationSplitPattern.split(s.location);
			
			if (parts.length != 3)
				throw new IllegalArgumentException("invalid location: \"" + s.location + "\"");
			
			try {
				x = Float.parseFloat(parts[0]);
				y = Float.parseFloat(parts[1]);
				z = Float.parseFloat(parts[2]);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("invalid location", nfe);
			}
			
			this.mapId = s.mapId;
			this.hitCount = 1;
		}
		
		public boolean isCloseTo(DataPoint o) {
			if (mapId != o.mapId)
				return false;
			float distSq = x * o.x + y * o.y + z * o.z;
			return distSq <= 5f * 5f;
		}
		
		@Override
		public int compareTo(DataPoint o) {
			return timestamp.compareTo(o.timestamp);
		}
	}


	static final float GRID_SCALE = 2f;
	static final float DOT_SCALE = 3f;
	
	class MainChart extends JComponent {
		Ellipse2D.Float ellipse = new Ellipse2D.Float();
		
		@Override
		public int getWidth() {
			float width = (maxX - minX) * GRID_SCALE;
			return (int) width;
		}
		
		@Override
		public int getHeight() {
			float height = (maxY - minY) * GRID_SCALE;
			return (int) height;
		}
		
		public float convertAbsX(float absX) {
			return (absX - minX) * GRID_SCALE;
		}
		
		public float convertAbsY(float absY) {
			return (absY - minY) * GRID_SCALE;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight();
			
			Graphics2D g2 = (Graphics2D) g;
			
			g2.setBackground(Color.BLACK);
			g2.clearRect(0, 0, w, h);
			g2.setColor(Color.RED);
			
			int totalPoints = data.size();
			DataPoint p;
			float x1, y1, dotSize;
//			System.out.println();
			for (int i = 0; i < totalPoints; i++) {
				p = data.get(i);
				x1 = convertAbsX(p.x);
				y1 = convertAbsY(p.y);
				dotSize = p.hitCount * DOT_SCALE;
				ellipse.setFrame(x1, y1, dotSize, dotSize);
//				System.out.printf("[%s] w/h, x/y: %s/%s, %s/%s -> %s/%s%n", i, w, h, p.x, p.y, x1, y1);
				g2.fill(ellipse);
			}
		}
	}
}
