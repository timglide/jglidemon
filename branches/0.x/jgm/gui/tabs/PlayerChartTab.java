package jgm.gui.tabs;

import jgm.gui.components.PlayerChart;

import java.awt.*;

public class PlayerChartTab extends Tab {
	public PlayerChart chart;
	
	public PlayerChartTab() {
		super(new BorderLayout(), "Health/Mana Chart");
		
		chart = new PlayerChart();
		add(chart, BorderLayout.CENTER);
	}
}
