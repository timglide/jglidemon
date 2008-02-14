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

import jgm.glider.*;
import jgm.glider.log.*;
import jgm.gui.panes.TabsPane;
import jgm.gui.tabs.*;

import java.util.regex.*;
import java.util.logging.*;
import java.io.*;

public class LogUpdater implements Runnable, ConnectionListener {
	static Logger log = Logger.getLogger(LogUpdater.class.getName());
	
	private volatile boolean stop = false;

	private Conn conn;

	private LogTab statusLog;
	private LogTab rawLog;
	private LogTab gliderLog;
	private LogTab rawChatLog;
	private ChatTab chatLog;
	private UrgentTab urgent;
	private LogTab combatLog;
	private MobsTab mobsTab;
	private LootsTab lootsTab;
 
	private Long stuckTimer = null;
	private int stuckCount = 0;
	
	private Long lastGliderException = null;
	private Long lastInventoryNotGoingUp = null;
	private Long lastFlightError = null;
	
	private Thread thread;
	
	public final jgm.ServerManager sm;
	
	public LogUpdater(jgm.ServerManager sm, TabsPane t) {
		this.sm = sm;
		
		statusLog  = t.statusLog;
		rawLog     = t.rawLog;
		gliderLog  = t.gliderLog;
		rawChatLog = t.rawChatLog;
		chatLog    = t.chatLog;
		urgent     = t.urgent;
		combatLog  = t.combatLog;
		mobsTab    = t.mobsTab;
		lootsTab   = t.lootsTab;
		
		conn = new Conn(sm, "LogUpdater");
 	}

	public Conn getConn() {
		return conn;
	}
	
	public void onConnecting() {}
	
	public void onConnect() {
		stop = false;
		thread = new Thread(this, sm.name + ":LogUpdater");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void onDisconnecting() {}
	
	public void onDisconnect() {
		stop = true;
	}
	
	public void onDestroy() {
		stop = true;
		if (thread != null) thread.interrupt();
		if (conn != null) conn.close();
	}
	
	public void run() {
		try {
			Command.getEscapeHiCommand("on").getResult(conn);
			Command.getLogCommand("all").getResult(conn);
			Command.getNoLogCommand("chat").getResult(conn);
		} catch (IOException e) {
			log.fine("Stopping LogUpdater, IOE: " + e.getMessage());
			sm.connector.someoneDisconnected();
			return; // connection died
		}

		String   line = null;

		while (true) {
			if (stop) return;
			
			try {
				line = conn.readLine();
			} catch (Throwable x) {
				log.fine("Stopping LogUpdater, Ex: " + x.getMessage());
				sm.connector.someoneDisconnected();
				return;
			}
			
			if (null == line || stop) return;

			handleLine(line);
		}
	}
	
	public void parseFile(String filename, LogFile logFile) throws IOException {
		parseFile(new File(filename), logFile);
	}
	
	public void parseFile(File f, LogFile logFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		
		String line = null;
		
		while (null != (line = in.readLine())) {
			handleLine(line, logFile);
			
/// !!! No longer necessary since we manually create non-raw
///     entries in order to have color 
			// since the chat log file is tecnically
			// raw, we have to create a non-raw line
			// to get whispers and stuff
//			if (logFile.equals(LogFile.Chat)) {
//				line = RawChatLogEntry.removeFormatting(line);
//				line = RawChatLogEntry.removeLinks(line);
//				handleLine(line, LogFile._NormalChat);
//			}
		}
	}
	
	private Pattern ESCPAE_PATTERN =
//		Pattern.compile("&(&#\\d+;)"); // see below...
		Pattern.compile("&&#(\\d+);");
	
	private void handleLine(String line) {
		handleLine(line, LogFile.None);
	}
	
	/**
	 * Parse the line and add it to the appropriate tab(s)
	 * @param line
	 * @param logFile The type of log file we're parsing the
	 *                line from, if applicable
	 */
	private LogEntry handleLine(String line, LogFile logFile) {
		boolean fromLog = !logFile.equals(LogFile.None);
//		System.out.println("Line: " + line);
		
		if (fromLog) {
			jgm.sound.Audible.ENABLE_SOUNDS = false;
		}
		
		// fix escaped characters
		Matcher m = ESCPAE_PATTERN.matcher(line);
		StringBuilder sb = new StringBuilder();
		int i = 0;
		
		// if this were php, i would use preg_replace
		// with the e modifier, but java doesn't have
		// eval obviously
		while (m.find()) {
			sb.append(line.substring(i, m.start()));
			sb.append((char) Integer.parseInt(m.group(1)));
			i = m.end();
		}
		sb.append(line.substring(i));
		
		line = sb.toString();
		
		// Not going to include Apache Commons just for
		// one thing.
//		line = m.replaceAll("$1");
//		line = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(line);
		
		LogEntry e = LogEntry.factory(this, line, logFile);

		if (e == null) {
			if (!logFile.equals(LogFile.None)) {
				jgm.sound.Audible.ENABLE_SOUNDS = true;
			}
			
			return null;
		}
		
		if (jgm.JGlideMon.debug && rawLog != null) {
			rawLog.add(e);
		}

		if (e instanceof GliderLogEntry) {
			gliderLog.add(e);
			
			GliderLogEntry e2 = (GliderLogEntry) e;
			
			if (null != e2.friend) {
				urgent.followers.add(e2);
			}
			
			if (e2.isAlert()) {
				urgent.add(e, true);
				
				if (!fromLog && null != sm.gui)
					sm.gui.tray
						.warnIfInactive("Glider", e2.getText());
			}
			
			if (!fromLog) {
				if (e2.type == GliderLogEntry.Type.STUCK &&
					jgm.Config.getInstance().getBool("stuck.enabled")) {
					long now = System.currentTimeMillis();
					long timeout = 1000 * jgm.Config.getInstance().getInt("stuck.timeout");
					int limit = jgm.Config.getInstance().getInt("stuck.limit");
					
					if (stuckTimer == null) {
						stuckTimer = now;
					} else if (now - stuckTimer >= timeout) {
						// we're stuck but it's been long enough
						// since the last time we were stuck
						stuckTimer = now;
						stuckCount = 0;
					}
					
					log.fine("Stuck " + stuckCount + " times. Limit = " + limit);
					
					if (limit == 0 || stuckCount < limit) {
						stuckCount++;
						// simulate pressing the Start button
						sm.gui.ctrlPane.start.doClick();
						
						String tmp = jgm.Config.c.get("restarter.onrestart");
						if (tmp.equals("shrink"))
							sm.gui.ctrlPane.shrink.doClick();
						else if (tmp.equals("hide"))
							sm.gui.ctrlPane.hide.doClick();
						
						log.info("Restarting glide after being stuck");
					} else {
						stuckTimer = null;
						stuckCount = 0;
						log.warning("Stuck too many times in a row, giving up");
					}
				} else if (e2.type == GliderLogEntry.Type.EXCEPTION &&
							jgm.Config.getInstance().getBool("restarter.exception.enabled")) {
					log.finer("Exception GliderLogEntry: " + e2.getText());
					lastGliderException = System.currentTimeMillis();
				} else if (e2.type == GliderLogEntry.Type.INVENTORY_NOT_GOING_UP &&
							jgm.Config.c.getBool("restarter.inventory.enabled")) {
					log.finer("Inventory GliderLogEntry: " + e2.getText());
					lastInventoryNotGoingUp = System.currentTimeMillis();
				}
			}
		} else if (e instanceof StatusEntry) {
			statusLog.add(e);
			
			if (!fromLog && e.getText().equals("Stopping glide")) {
				if (null != sm.gui)
					sm.gui.tray
						.warnIfInactive("Glider", e.getText());
				
				if (lastGliderException != null)
					log.finer("Restarter exception check: " + (System.currentTimeMillis() - lastGliderException) + " <=? " + (1000 * jgm.Config.c.getInt("restarter.exception.timeout")));
				
				if (lastInventoryNotGoingUp != null)
					log.finer("Restarter inventory check: " + (System.currentTimeMillis() - lastInventoryNotGoingUp) + " <=? " + (1000 * jgm.Config.c.getInt("restarter.inventory.timeout")));
				
				if (lastFlightError != null)					
					log.finer("Restarter flight check: " + (System.currentTimeMillis() - lastFlightError) + " <=? " + (1000 * jgm.Config.c.getInt("restarter.flight.timeout")));
				
				if (lastGliderException != null
					&& jgm.Config.getInstance().getBool("restarter.exception.enabled")
					&& System.currentTimeMillis() - lastGliderException <= 1000 * jgm.Config.getInstance().getInt("restarter.exception.timeout")) {

					java.util.Timer timer = new java.util.Timer(sm.name + ":ExceptionRestarter");
					timer.schedule(new java.util.TimerTask() {			
						public void run() {
							log.info("Restarting glide after an exception");
							sm.gui.ctrlPane.start.doClick();

							String tmp = jgm.Config.c.get("restarter.onrestart");
							if (tmp.equals("shrink"))
								sm.gui.ctrlPane.shrink.doClick();
							else if (tmp.equals("hide"))
								sm.gui.ctrlPane.hide.doClick();
							
							this.cancel();
						}
					}, 5000);
					
					lastGliderException = null;
				}
				
				if (lastInventoryNotGoingUp != null
					&& jgm.Config.getInstance().getBool("restarter.inventory.enabled")
					&& System.currentTimeMillis() - lastFlightError <= 1000 * jgm.Config.c.getInt("restarter.inventory.timeout")) {

					java.util.Timer timer = new java.util.Timer(sm.name + ":InventoryRestarter");
					timer.schedule(new java.util.TimerTask() {			
						public void run() {
							log.info("Restarting glide after an inventory message");
							sm.gui.ctrlPane.start.doClick();

							String tmp = jgm.Config.c.get("restarter.onrestart");
							if (tmp.equals("shrink"))
								sm.gui.ctrlPane.shrink.doClick();
							else if (tmp.equals("hide"))
								sm.gui.ctrlPane.hide.doClick();
								
							this.cancel();
						}
					}, 15000); // 15 seconds, if they're auto selling, it could lag
					
					lastInventoryNotGoingUp = null;
				}
				
				if (lastFlightError != null
					&& jgm.Config.getInstance().getBool("restarter.flight.enabled")
					&& System.currentTimeMillis() - lastFlightError <= 1000 * jgm.Config.c.getInt("restarter.flight.timeout")) {

					java.util.Timer timer = new java.util.Timer(sm.name + ":FlightRestarter");
					timer.schedule(new java.util.TimerTask() {			
						public void run() {
							log.info("Restarting glide after a flight error message");
							sm.gui.ctrlPane.start.doClick();

							String tmp = jgm.Config.c.get("restarter.onrestart");
							if (tmp.equals("shrink"))
								sm.gui.ctrlPane.shrink.doClick();
							else if (tmp.equals("hide"))
								sm.gui.ctrlPane.hide.doClick();
								
							this.cancel();
						}
					}, 15000); // 15 seconds, again, if they're auto selling, it could lag
					
					lastFlightError = null;
				}
			}
		} else if (e instanceof ChatLogEntry) {
			ChatLogEntry e2 = (ChatLogEntry) e;
			
			if (e2.isUrgent()) {
				urgent.add(e, true);
				
				if (!fromLog && null != sm.gui) 
					sm.gui.tray
						.warnIfInactive("Chat", e2.getText());
			}
			
			chatLog.add(e2);
		} else if (e instanceof RawChatLogEntry) {
			if (jgm.JGlideMon.debug && rawChatLog != null) {
				rawChatLog.add(e);
			}

			RawChatLogEntry e2 = (RawChatLogEntry) e;

			if (e2.hasItemSet())  {
//				System.out.println("Adding item: " + e2.getItem().toString());
				lootsTab.add(e2.getItemSet());
				
				if (!fromLog && e2.getItemSet().getItem().quality >= jgm.wow.Item.RARE)
					if (null != sm.gui)
						sm.gui.tray
							.informIfInactive("Phat Loot", "[" + e2.getItemSet().getItem().name + "]");
			}
			
			if (e2.hasMoney()) {
				lootsTab.addMoney(e2.getMoney());
			}
			
			if (e2.hasRep() || e2.hasSkill()) {
				mobsTab.add(e);
			}
			
			// create the non-raw chat entry
			if (fromLog) {
				LogEntry newEntry = 
					handleLine(e2.getHtmlText(), LogFile._NormalChat);
				newEntry.timestamp = e.timestamp;
			} else {
				handleLine("[Chat] " + e2.getHtmlText(), LogFile.None);
			}
		} else if (e instanceof CombatLogEntry) {
			CombatLogEntry e2 = (CombatLogEntry) e;
			combatLog.add(e2);
			
			if (e2.hasMob()) {
				mobsTab.add(e2);
			}
		}
		
		if (fromLog) {
			jgm.sound.Audible.ENABLE_SOUNDS = true;
		}
		
		return e;
	}
}
