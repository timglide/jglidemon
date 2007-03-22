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
