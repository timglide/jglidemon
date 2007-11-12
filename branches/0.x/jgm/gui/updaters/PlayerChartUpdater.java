package jgm.gui.updaters;

import jgm.JGlideMon;
import jgm.gui.components.PlayerChart;

import java.util.*;

public class PlayerChartUpdater {
	// updates every 5 seconds
	static final int UPDATE_INTERVAL = 1000;
	
	PlayerChart chart;
	StatusUpdater status;
	Timer timer;
	
	public PlayerChartUpdater() {
		status = JGlideMon.instance.status;
		chart = JGlideMon.instance.gui.tabsPane.chartTab.chart;
		
		timer = new Timer();
		timer.scheduleAtFixedRate(
			new TimerTask() {
				public void run() {
					chart.addData(status.health, status.mana, status.targetHealth);
				}
			}, 0, UPDATE_INTERVAL
		);
	}
}
