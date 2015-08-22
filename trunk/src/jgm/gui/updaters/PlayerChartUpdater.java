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
package jgm.gui.updaters;

import java.util.Timer;
import java.util.TimerTask;

import jgm.gui.tabs.ChartsTab;

public class PlayerChartUpdater {
	// updates every second
	static final int UPDATE_INTERVAL = 1000;
	
	ChartsTab charts;
	jgm.glider.Status status;
	Timer timer;
	
	jgm.ServerManager sm;
	
	public PlayerChartUpdater(jgm.ServerManager sm) {
		this.sm = sm;
		status = sm.status.s;
		charts = sm.gui.tabsPane.chartsTab;
		
		timer = new Timer(sm.name + ":PlayerChartUpdater", true);
		timer.scheduleAtFixedRate(
			new TimerTask() {
				final jgm.ServerManager sm = PlayerChartUpdater.this.sm;
				public void run() {
					// don't add points when we're dced or not attached
					if (!sm.connector.isConnected() || !status.attached) return;
					
					charts.playerChartTab.chart.addData(
						status.health,
						status.mana,
						status.targetName.isEmpty()
						? -1 : status.targetHealth);
					
					if (null != charts.locationChartTab)
						charts.locationChartTab.update(status);
				}
			}, 0, UPDATE_INTERVAL
		);
	}
	
	public void destroy() {
		timer.cancel();
	}
}
