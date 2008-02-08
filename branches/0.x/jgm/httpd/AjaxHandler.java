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

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*; 

import java.io.*;
import java.util.Properties;

import jgm.JGlideMon;
import jgm.gui.updaters.StatusUpdater;

public class AjaxHandler extends Handler {
	public static final String DEF_ERROR_XML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\nn" +
		"<response>\n" + 
		"<status>failure</status>\n" +
		"<message/>\n" +
		"</response>";
	
	public AjaxHandler(HTTPD httpd) {
		super(httpd);
	}
	
	@Override
	public Response handle(String uri, String method, Properties headers, Properties params) {
		String out = createXML(uri, method, headers, params);
		if (out == null) out = DEF_ERROR_XML;
		
		Response ret = null;
		
		ret = new Response(HTTPD.HTTP_OK, HTTPD.MIME_HTML, out);
		ret.addHeader("Content-type", "text/xml");
		
		// add no-cache headers
		ret.addHeader("Cache-Control", "no-cache, must-revalidate");
		ret.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		
		return ret;
	}

	private String createXML(String uri, String method, Properties headers, Properties params) {
		Document xml = null;
		DocumentBuilderFactory factory = 
		DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xml = builder.newDocument();  // Create from whole cloth
	
			Element root = CE(xml, "response"); 
			AC(xml, root);
			
			// status
			Element tmp = CE(xml, "status");
			Text status = CTN(xml, "success");
			AC(tmp, status);
			AC(root, tmp);
			
			// for an error message
			tmp = CE(xml, "message");
			Text message = CTN(xml, "");
			AC(tmp, message);
			AC(root, tmp);
			
			
/*			Element request = CE(xml, "request");
			AC(root, request);
			
			// url
			tmp = CE(xml, "url");
			AC(tmp, CTN(xml, uri));
			AC(request, tmp);
	
			// headers
			tmp = CE(xml, "headers");
			
			for (Object key : headers.keySet()) {
				Element tmp2 = CE(xml, "header");
				tmp2.setAttribute("name", key.toString());
				tmp2.appendChild(CTN(xml, headers.get(key).toString()));
				AC(tmp, tmp2);
			}
			
			AC(request, tmp);
			*/
			
			if (uri.equals("command")) {
				final String cmd = params.getProperty("command");
				
				final jgm.ServerManager sm = httpd.sm;
				
				if (cmd != null) {
					Thread t = null;
					
					if (cmd.equals("start") || cmd.equals("stop")) {
						t = new Thread() {
							public void run() {
								javax.swing.JButton btn = 
									cmd.equals("start")
									? sm.gui.ctrlPane.start
									: sm.gui.ctrlPane.stop;
								btn.doClick();
							}
						};
					} else if (cmd.equals("chat")) {
						final String keys = params.getProperty("keys");
						
						if (keys != null && !keys.trim().equals("")) {
							t = new Thread() {
								public void run() {
									jgm.gui.tabs.ChatTab tab = 
										sm.gui.tabsPane.chatLog;
									Object last = tab.type.getSelectedItem();
									tab.type.setSelectedItem(jgm.glider.ChatType.RAW);
									tab.keys.setText(keys);
									tab.send.doClick();
									tab.reset.doClick();
									tab.type.setSelectedItem(last);
								}
							};
						} else {
							status.setTextContent("error");
							message.setTextContent("No keys provided for sending");
						}
					} else {
						status.setTextContent("error");
						message.setTextContent("Invalid command: " + cmd);
					}
					
					if (t != null) {
						t.start();
					}
				} else {
					status.setTextContent("error");
					message.setTextContent("No command given");
				}
			} else if (uri.equals("status")) {
				// jgm info
				Element jgm_ = CE(xml, "app");
				_(xml, jgm_, "name", JGlideMon.app);
				_(xml, jgm_, "version", JGlideMon.version);
				
				boolean connected = httpd.sm.connector.isConnected();
				_(xml, jgm_, "connected", Boolean.toString(connected));
				
				_(xml, jgm_, "update-interval", jgm.Config.getInstance().getInt("web.updateinterval"));
				
				AC(root, jgm_);
				
				
				// glider info
				Element glider = CE(xml, "glider");
				
				if (connected && StatusUpdater.instance != null) {
					jgm.glider.Status s = StatusUpdater.instance.s;
					
					_(xml, glider, "version", s.version);
					_(xml, glider, "attached", Boolean.toString(s.attached));
					
					if (s.attached) {
						_(xml, glider, "lcclass", s.clazz.toString().toLowerCase());
						_(xml, glider, "class", s.clazz.toString());
						_(xml, glider, "name", s.name);
						_(xml, glider, "level", s.level);
						_(xml, glider, "health", (int) s.health);
						_(xml, glider, "mana", (int) s.mana);
						_(xml, glider, "mana-name", s.manaName.toLowerCase());
						_(xml, glider, "xp", s.experience);
						_(xml, glider, "next-xp", s.nextExperience);
						_(xml, glider, "xp-percent", (int) (100.0 * ((double) s.experience) / s.nextExperience));
						_(xml, glider, "xp-per-hour", s.xpPerHour);
						
						if (s.xpPerHour > 0) {
							int seconds = 0, minutes = 0, hours = 0;
							int xpDiff = s.nextExperience - s.experience;
							double d = (double) xpDiff / (double) s.xpPerHour;
							hours = (int) d;
							d = 60 * (d - hours);
							minutes = (int) d;
							d = 60 * (d - minutes);
							seconds = (int) d;
							
							_(xml, glider, "ttl", String.format("%d:%02d:%02d", hours, minutes, seconds));
						} else {
							_(xml, glider, "ttl", "Unknown");
						}
						
						_(xml, glider, "mode", s.mode);
						_(xml, glider, "full-profile", s.profile);
						
						String[] parts = s.profile.split("\\\\");
						String str = parts.length > 0 ? parts[parts.length - 1] : "";
						_(xml, glider, "profile", str);
						
						_(xml, glider, "kills", s.kills);
						_(xml, glider, "loots", s.loots);
						_(xml, glider, "deaths", s.deaths);
						
						_(xml, glider, "location", s.location);
						_(xml, glider, "heading", s.heading);
						
						_(xml, glider, "target-name", s.targetName);
						_(xml, glider, "target-level", s.targetLevel);
						_(xml, glider, "target-health", (int) s.targetHealth);
					}
				}
				
				AC(root, glider);
			} else {
				status.setTextContent("error");
				message.setTextContent("Invalid uri: " + uri);
			}
			
			
			// prepare for serialization
			xml.setXmlStandalone(true);
			xml.getDocumentElement().normalize();
			
//			Serialisation through Tranform.
			StringWriter out = new StringWriter();
			DOMSource domSource = new DOMSource(xml);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.transform(domSource, streamResult); 
			
			return out.toString();
		} catch (Throwable pce) {
			// this had better not happen
			
			// Parser with specified options can't be built
			pce.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	// cuz i don't want to have to type this out each fucking time
	
	/**
	 * One liner to make a new tag with a text node and append
	 * it to parent when you won't need to modify it later.
	 */
	private void _(Document xml, Element parent, String tag, Object contents) {
		Element tmp = CE(xml, tag);
		AC(tmp, CTN(xml, contents.toString()));
		AC(parent, tmp);
	}
	
	/**
	 * createElement shortcut
	 */
	private Element CE(Document xml, String name) {
		return xml.createElement(name);
	}
	
	/**
	 * createTextNode shortcut
	 * @param xml
	 * @param contents
	 * @return
	 */
	private Text CTN(Document xml, String contents) {
		return xml.createTextNode(contents);
	}
	
	/**
	 * appendChild shortcut
	 * @param parent
	 * @param child
	 */
	private void AC(Document parent, Element child) {
		parent.appendChild(child);
	}
	
	/**
	 * appendChild shortcut
	 * @param parent
	 * @param child
	 */
	private void AC(Element parent, Node child) {
		parent.appendChild(child);
	}
}
