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
package jgm.gui;

import java.util.logging.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Tray implements ActionListener {
	static Logger log = Logger.getLogger(Tray.class.getName());
	
	// whether the icon has been added to the tray or not
	private volatile boolean enabled = false;
	
	private static SystemTray tray;
	private static TrayIcon icon;
	private static PopupMenu menu;

	public Tray() {		
		try {
			Class.forName("java.awt.SystemTray");
			
			if (!SystemTray.isSupported()) {
				throw new Throwable("System tray not supported");
			}
			
			tray = SystemTray.getSystemTray();
			
			// apparantly can't use a JPopupMenu....
			menu = new PopupMenu();
			MenuItem item = new MenuItem("Exit");
			item.addActionListener(this);
			menu.add(item);
			
			icon = new TrayIcon(jgm.GUI.frame.getIconImage(), jgm.GUI.BASE_TITLE, menu);
			icon.setImageAutoSize(true);
			icon.addMouseListener(new MyMouseListener());
			
			if (jgm.Config.getInstance().getBool("general", "showtray")) {
				enable();
			}
		} catch (Throwable e) {
			tray = null;
			icon = null;
			
			log.log(Level.WARNING, "Unable to initialize system tray", e);
			return;
		}
	}
	
	public void enable() {
		if (!isSupported() || enabled) return;
		
		try {
			tray.add(icon);
			enabled = true;
		} catch (AWTException e) {
			log.log(Level.WARNING, "Error trying to enable tray", e);
		}
	}
	
	public void disable() {
		if (!isSupported() || !enabled) return;
		tray.remove(icon);
		enabled = false;
	}
	
	public static boolean isSupported() {
		return tray != null;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Exit")) {
			jgm.JGlideMon.instance.destroy();
		}
	}
	
	/**
	 * This will display a popup message only if the
	 * program isn't the active window. It is intended
	 * for urgent log alerts. It would be redundant to
	 * display the popup when the program is active.
	 * 
	 * @param caption
	 * @param text
	 */
	public void messageIfInactive(String caption, String text) {
		if (jgm.GUI.frame.isActive())
			return;
		
		displayMessage(caption, text, MessageType.WARNING);
	}
	
	// necessary to not have it break with java 1.5
	public enum MessageType {
		ERROR, INFO, NONE, WARNING
	}
	
	// passthru for displayMessage
	public void displayMessage(String caption, String text, MessageType type) {
//		try {
//			Class.forName("java.awt.TrayIcon");
//		} catch (ClassNotFoundException e) {
//			return;
//		}
		
		if (icon == null) return;
		
		TrayIcon.MessageType realType = null;
		
		switch (type) {
			case ERROR:
				realType = TrayIcon.MessageType.ERROR;
				break;
			
			case INFO:
				realType = TrayIcon.MessageType.INFO;
				break;
				
			case WARNING:
				realType = TrayIcon.MessageType.WARNING;
				break;
			
			default:
				realType = TrayIcon.MessageType.NONE;
				break;
		}
		
		icon.displayMessage(caption, text, realType);
	}
	
	private class MyMouseListener extends MouseAdapter {
		// when the tray icon is clicked
		public void mouseClicked(MouseEvent e) {
			JFrame f = jgm.GUI.frame;
			
			// restore if we were minimized
			if (e.getButton() == MouseEvent.BUTTON1 &&
				JFrame.ICONIFIED ==
				(JFrame.ICONIFIED & f.getExtendedState())) {
				f.setVisible(true);
				f.setExtendedState(f.getExtendedState() & ~JFrame.ICONIFIED);
			}
			
			// and bring it in focus
			f.requestFocus();
			f.toFront();
		}
	} 
}
