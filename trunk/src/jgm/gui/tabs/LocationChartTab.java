package jgm.gui.tabs;

import java.awt.BorderLayout;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.components.LocationChart;

public class LocationChartTab extends Tab implements Clearable {
	public LocationChart chart;
	
	public LocationChartTab(GUI gui) {
		super(gui, new BorderLayout(), "Location");
		chart = new LocationChart();
		chart.setBorder(null);
		add(chart, BorderLayout.CENTER);
	}
	
	@Override
	public void clear(boolean clearingAll) {
		chart.reset();
	}
	
	@Override
	public void update(Status s) {
		chart.update(s);
	}
}
