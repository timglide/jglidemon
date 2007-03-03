package jgm.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Tray implements ActionListener {
	private static SystemTray tray;
	private static TrayIcon icon;
	private static PopupMenu menu;
		
	public Tray() {		
		try {
			Class.forName("java.awt.SystemTray");
			
			if (!SystemTray.isSupported()) {
				throw new Exception("System tray not supported");
			}
			
			tray = SystemTray.getSystemTray();
			
			// apparantly can't use a JPopupMenu....
			menu = new PopupMenu();
			MenuItem item = new MenuItem("Exit");
			item.addActionListener(this);
			menu.add(item);
			
			icon = new TrayIcon(jgm.GUI.frame.getIconImage(), jgm.GUI.BASE_TITLE, menu);
			icon.setImageAutoSize(true);
			
			tray.add(icon);
			
			icon.addMouseListener(new MouseAdapter() {
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
			});
		} catch (Exception e) {
			tray = null;
			icon = null;
			
			System.err.println("Unable to initialize system tray. " + e.getMessage());
			return;
		}
	}
	
	public static boolean isSupported() {
		return tray != null;
	}
	
	public static void addMouseListener(MouseListener ml) {
		if (!isSupported()) return;
		
		icon.addMouseListener(ml);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Exit")) {
			jgm.JGlideMon.instance.destroy();
		}
	}
}
