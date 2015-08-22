package jgm.gui.tabs;

import java.awt.BorderLayout;

import jgm.glider.Status;
import jgm.gui.GUI;
import jgm.gui.panes.TabsPaneBase;

public class ChartsTab extends Tab implements Clearable {
	private TabsPaneBase tabs;
	public PlayerChartTab playerChartTab;
	public LocationChartTab locationChartTab;
	
	public ChartsTab(GUI gui) {
		super(gui, new BorderLayout(), "Charts");
		tabs = new TabsPaneBase(gui);
		
		playerChartTab = new PlayerChartTab(gui);
		addTab(playerChartTab);
		
//		locationChartTab = new LocationChartTab(gui);
//		addTab(locationChartTab);
		
		add(tabs, BorderLayout.CENTER);
	}
	
	@Override
	public void clear(boolean clearingAll) {
		tabs.clear(clearingAll);
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t);
	}
	
	@Override
	public void update(Status s) {
		tabs.update(s);
	}

}
