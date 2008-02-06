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

import jgm.JGlideMon;
import jgm.gui.components.PlayerChart;

import java.util.*;

public class PlayerChartUpdater {
	// updates every second
	static final int UPDATE_INTERVAL = 1000;
	
	PlayerChart chart;
	jgm.glider.Status status;
	Timer timer;
	
	public PlayerChartUpdater() {
		status = JGlideMon.getCurManager().status.s;
		chart = JGlideMon.instance.gui.tabsPane.chartTab.chart;
		
		timer = new Timer();
		timer.scheduleAtFixedRate(
			new TimerTask() {
				public void run() {
					// don't add points when we're dced or not attached
					if (!JGlideMon.getCurManager().connector.isConnected() || !status.attached) return;
					
					chart.addData(status.health, status.mana, status.targetHealth);
				}
			}, 0, UPDATE_INTERVAL
		);
	}
}
