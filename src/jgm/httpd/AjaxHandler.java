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
package jgm.httpd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import jgm.JGlideMon;
import jgm.glider.Command;
import jgm.glider.log.LogEntry;
import jgm.gui.panes.TabsPane;
import jgm.gui.tabs.LogTab;
import jgm.gui.tabs.LootsTab;
import jgm.gui.tabs.MobsTab;
import jgm.wow.Item;
import jgm.wow.ItemSet;
import jgm.wow.Mob;
import jgm.wow.Quality;
import jgm.wow.Rep;
import jgm.wow.Skill;

public class AjaxHandler extends Handler {
	public static final String DEF_ERROR_JSON =
		"{status: \"failure\", message: \"\"}";
	
	public AjaxHandler(HTTPD httpd) {
		super(httpd);
	}
	
	@Override
	public Response handle(String uri, String method, Properties headers, Properties params) {
		String out = createXML(uri, method, headers, params);
		if (out == null) out = DEF_ERROR_JSON;
		
		Response ret = null;
		
		ret = new Response(HTTPD.HTTP_OK, "application/json; charset=utf-8", out);

		// add no-cache headers
		ret.addHeader("Cache-Control", "no-cache, must-revalidate");
		ret.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		
		return ret;
	}

	private String createXML(String uri, String method, Properties headers, Properties params) {
		JSONObject root = new JSONObject();
		String since = params.getProperty("since");
		
		try {
			// status
			root.put("status", "success");
			
			if (uri.equals("command")) {
				command(root, params);
			} else if (uri.equals("status")) {
				status(root);
			} else if (uri.equals("chat")) {
				String type  = params.getProperty("type");
				String count = params.getProperty("count");
				
				chat(root, type, count, since);
			} else if (uri.equals("mobs")) {
				mobs(root, since);
			} else if (uri.equals("loot")) {
				loot(root, since);
			} else {
				root.put("status", "error");
				root.put("message", "Invalid uri: " + uri);
			}
			
			return root.toString();
		} catch (Throwable t) {
			try {
				root.put("status", "error");
				root.put("message", t.toString());
				return root.toString();
			} catch (JSONException je) {
				// shouldn't happen
			}
		}
		
		return null;
	}
	
	private void command(JSONObject root, Properties params) throws JSONException {
		final String cmd = params.getProperty("command");
		final jgm.ServerManager sm = httpd.sm;
		
		if (cmd != null) {
			if (cmd.equals("start")) {
				sm.cmd.add(Command.getStartCommand());
			} else if (cmd.equals("stop")) {
				sm.cmd.add(Command.getStopCommand());
			} else if (cmd.equals("chat")) {
				String keys = params.getProperty("keys");
				
				if (keys != null && !"".equals(keys = keys.trim())) {
					sm.cmd.add(Command.getChatCommand(keys));
				} else {
					root.put("status", "error");
					root.put("message", "No keys provided for sending");
				}
			} else {
				root.put("status", "error");
				root.put("message", "Invalid command: " + cmd);
			}
		} else {
			root.put("status", "error");
			root.put("message", "No command given");
		}
	}
	
	private void status(JSONObject root) throws JSONException {
		// jgm info
		JSONObject jgm_ = CE(root, "app");
		jgm_.put("name", JGlideMon.app);
		jgm_.put("version", JGlideMon.version);
		
		boolean connected = httpd.sm.connector.isConnected();
		jgm_.put("connected", Boolean.toString(connected));
		
		jgm_.put("update-interval", jgm.Config.getInstance().getInt("web.updateinterval"));
		
		
		
		// glider info
		JSONObject glider = CE(root, "glider");
		
		if (connected && httpd.sm.status != null) {
			jgm.glider.Status s = httpd.sm.status.s;
			
			glider.put("version", s.version);
			glider.put("attached", Boolean.toString(s.attached));
			
			if (s.attached) {
				glider.put("lcclass", s.clazz.toString().toLowerCase().replace(' ', '_'));
				glider.put("class", s.clazz.toString());
				glider.put("name", s.name);
				glider.put("level", s.level);
				glider.put("health", (int) s.health);
				glider.put("mana", (int) s.mana);
				glider.put("mana-name", s.manaName.toLowerCase());
				glider.put("xp", s.experience);
				glider.put("next-xp", s.nextExperience);
				glider.put("xp-percent", (int) (100.0 * ((double) s.experience) / s.nextExperience));
				glider.put("xp-per-hour", s.xpPerHour);
				
				if (s.xpPerHour > 0) {
					int seconds = 0, minutes = 0, hours = 0;
					int xpDiff = s.nextExperience - s.experience;
					double d = (double) xpDiff / (double) s.xpPerHour;
					hours = (int) d;
					d = 60 * (d - hours);
					minutes = (int) d;
					d = 60 * (d - minutes);
					seconds = (int) d;
					
					glider.put("ttl", String.format("%d:%02d:%02d", hours, minutes, seconds));
				} else {
					glider.put("ttl", "Unknown");
				}
				
				glider.put("mode", s.mode);
				glider.put("full-profile", s.profile);
				
				String[] parts = s.profile.split("\\\\");
				String str = parts.length > 0 ? parts[parts.length - 1] : "";
				glider.put("profile", str);
				
				glider.put("kills", s.kills);
				glider.put("loots", s.loots);
				glider.put("deaths", s.deaths);
				
				glider.put("location", s.location);
				glider.put("heading", s.heading);
				
				glider.put("target-name", s.targetName);
				glider.put("target-level", s.targetLevel);
				glider.put("target-health", (int) s.targetHealth);
			}
		}
	}
	
	private enum LogType {
		ALL, PUBLIC, WHISPER, GUILD, URGENT, COMBAT, GLIDER, STATUS;
		
		public LogTab getTab(TabsPane tabsPane) {
			switch (this) {
				case ALL:
					return tabsPane.chatLog.all;
				case PUBLIC:
					return tabsPane.chatLog.pub;
				case WHISPER:
					return tabsPane.chatLog.whisper;
				case GUILD:
					return tabsPane.chatLog.guild;
				case URGENT:
					return tabsPane.urgent.logs;
				case COMBAT:
					return tabsPane.combatLog;
				case GLIDER:
					return tabsPane.gliderLog;
				case STATUS:
					return tabsPane.statusLog;
			}
			
			return null;
		}
	}
	
	private void chat(JSONObject root, String type, String count, String since) throws JSONException {
		if (null == type) {
			throw new IllegalArgumentException("must specify type");
		}
		
		int iCount = -1;
		
		try {
			iCount = Integer.parseInt(count);
		} catch (NumberFormatException nfe) {}
		
		long iSince = -1L;
		
		try {
			iSince = Long.parseLong(since);
		} catch (NumberFormatException nfe) {}
		
		if (iCount < 1 && iSince == -1L) {
			throw new IllegalArgumentException("must provide count or since");
		}
		
		Date dateSince = null;
		
		if (-1L != iSince) {
			dateSince = new Date(iSince);
		}
		
		LogTab logTab = null;
		
		try {
			logTab = LogType.valueOf(
				type.toUpperCase()).getTab(httpd.sm.gui.tabsPane);
		} catch (IllegalArgumentException e) {}
		
		if (null == logTab) {
			throw new IllegalArgumentException("invalid type: " + type);
		}
		
		List<LogEntry> entries = logTab.getEntries(iCount, dateSince);
		
		JSONArray entriesArray = new JSONArray();
		
		for (LogEntry e : entries) {
			JSONObject entryObj = new JSONObject();
			entryObj.put("timestamp", e.timestamp.getTime());
			entryObj.put("text", e.supportsHtmlText() ? e.getHtml5Text() : e.getText());
			entriesArray.put(entryObj);
		}
		
		root.put("entries", entriesArray);
	}
	
	private void mobs(JSONObject root, String since) throws JSONException {
		MobsTab mobsTab = httpd.sm.gui.tabsPane.mobsTab;
		
		if (null != since) {
			long iSince = 0L;
			
			try {
				iSince = Long.parseLong(since);
				
				if (mobsTab.getLastUpdateTime() < iSince) {
					// the last update was before the specified time
					// meaning nothing new has been added
					return;
				}
			} catch (NumberFormatException e) {}
		}
		
		JSONArray mobs = CA(root, "mobs");
		
		for (Mob m : mobsTab.getMobs()) {
			AP(mobs)
				.put("name", m.name)
				.put("qty", m.number)
				.put("avgXp", m.xp);
		}
		
		
		JSONArray reps = CA(root, "rep");
		
		for (Rep r : mobsTab.getReps()) {
			AP(reps)
				.put("faction", r.faction)
				.put("time", r.timestampDate.getTime())
				.put("gained", r.amount);
		}
		
		
		JSONObject skills = CE(root, "skills");
		
		for (Skill s : mobsTab.getSkills()) {
			CE(skills, s.name)
				.put("skill", s.name)
				.put("level", s.level)
				.put("time", s.timestampDate);
		}
	}
	
	private void loot(JSONObject root, String since) throws JSONException {
		LootsTab lootsTab = httpd.sm.gui.tabsPane.lootsTab;
		
		if (null != since) {
			long iSince = 0L;
			
			try {
				iSince = Long.parseLong(since);
				
				if (lootsTab.getLastUpdateTime() < iSince) {
					// the last update was before the specified time
					// meaning nothing new has been added
					return;
				}
			} catch (NumberFormatException e) {}
		}
		
		root
			.put("goldLooted", lootsTab.getGoldLooted())
			.put("lootWorth", lootsTab.getLootWorth())
			.put("goldPerHour", lootsTab.getGoldPerHour())
			.put("runtime", lootsTab.getRunningTime());
		
		JSONArray loot = CA(root, "loot");
		
		for (int quality = Item.POOR; quality <= Item.EPIC; quality++) {
			List<ItemSet> itemSets = lootsTab.getItemSets(quality);
			
			for (ItemSet is : itemSets) {
				Item item = is.getItem();
				AP(loot)
					.put("id", item.id)
					.put("quality", item.quality)
					.put("qualityName", item.quality_.name().toLowerCase())
					.put("name", item.name)
					.put("icon", item.iconPath)
					.put("qty", is.getQuantity());
			}
		}
	}
	
	/**
	 * Creates a new json object and appends it to parent with the given
	 * key. Originally named as an abbreviation for Create Element when
	 * this all used to be XML instead of JSON.
	 * @throws JSONException 
	 */
	private JSONObject CE(JSONObject parent, String name) throws JSONException {
		JSONObject obj = new JSONObject();
		parent.put(name, obj);
		return obj;
	}
	
	private JSONArray CA(JSONObject parent, String name) throws JSONException {
		JSONArray arr = new JSONArray();
		parent.put(name, arr);
		return arr;
	}
	
	/**
	 * Creates a new JSONObject, appends it to parent, and returns the new object
	 * @param parent
	 * @return
	 */
	private JSONObject AP(JSONArray parent) {
		JSONObject obj = new JSONObject();
		parent.put(obj);
		return obj;
	}
}
