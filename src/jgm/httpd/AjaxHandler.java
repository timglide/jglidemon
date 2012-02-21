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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;

import jgm.JGlideMon;
import jgm.glider.Command;

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
		
		ret = new Response(HTTPD.HTTP_OK, HTTPD.MIME_HTML, out);
		ret.addHeader("Content-type", "application/json; charset=utf-8");
		
		// add no-cache headers
		ret.addHeader("Cache-Control", "no-cache, must-revalidate");
		ret.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		
		return ret;
	}

	private String createXML(String uri, String method, Properties headers, Properties params) {
		JSONObject root = new JSONObject();
		final jgm.ServerManager sm = httpd.sm;
		
		try {
			// status
			root.put("status", "success");
			
			// for an error message
			root.put("message", "");
			
			if (uri.equals("command")) {
				final String cmd = params.getProperty("command");
				
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
			} else if (uri.equals("status")) {
				// jgm info
				JSONObject jgm_ = CE(root, "app");
				jgm_.put("name", JGlideMon.app);
				jgm_.put("version", JGlideMon.version);
				
				boolean connected = httpd.sm.connector.isConnected();
				jgm_.put("connected", Boolean.toString(connected));
				
				jgm_.put("update-interval", jgm.Config.getInstance().getInt("web.updateinterval"));
				
				
				
				// glider info
				JSONObject glider = CE(root, "glider");
				
				if (connected && sm.status != null) {
					jgm.glider.Status s = sm.status.s;
					
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
			} else {
				root.put("status", "error");
				root.put("message", "Invalid uri: " + uri);
			}
			
			return root.toString();
		} catch (Throwable pce) {
			// this had better not happen
			
			// Parser with specified options can't be built
			pce.printStackTrace();
		}
		
		return null;
	}
		
	/**
	 * Creates a new json object and appends it to parent with the given
	 * key.
	 * @throws JSONException 
	 */
	private JSONObject CE(JSONObject parent, String name) throws JSONException {
		JSONObject obj = new JSONObject();
		parent.put(name, obj);
		return obj;
	}
}
