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
package jgm.gui.tabs;

import jgm.*;
import jgm.glider.*;
import jgm.gui.updaters.SSUpdater;

import java.util.*;
import java.util.logging.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;
import javax.swing.*;

public class ScreenshotTab extends Tab
	implements ActionListener, ChangeListener, MouseListener,
				KeyListener {
	static Logger log = Logger.getLogger(ScreenshotTab.class.getName());
	
	private static Conn conn = null;
	private static SSUpdater updater = null;
	
	public JCheckBox keysEnabled;
	public JScrollPane jsp;
	public JLabel ssLabel;
	public ImageIcon ssIcon;

	private JButton refresh;
	
	public ScreenshotTab() {
		super(new BorderLayout(), "Screenshot");
		
		JPanel jp = new JPanel(new GridBagLayout());
		c.weightx = 0.0;
		keysEnabled = new JCheckBox("Enable Sending Keystrokes", false);
		keysEnabled.setFocusable(false);
		jp.add(keysEnabled, c);
		add(jp, BorderLayout.NORTH);
		
		ssLabel = new JLabel();
		ssLabel.setHorizontalAlignment(JLabel.CENTER);
		ssLabel.addMouseListener(this);
		ssLabel.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel p = new JPanel();
		p.add(ssLabel);
		
		jsp = new JScrollPane(p);
		// jsp.setBorder(null);
		// jsp.setBorder(BorderFactory.createLineBorder(Color.red, 10));
		add(jsp, BorderLayout.CENTER);
		
		refresh = new JButton("Refresh Screenshot Immediately");
		refresh.setFocusable(false);
		refresh.addActionListener(this);
		add(refresh, BorderLayout.SOUTH);
		
		checkNulls();
		
		setEnabled(false);
		
		Connector.addListener(new ConnectionAdapter() {
			public void connectionEstablished() {
				setEnabled(true);
			}
			
			public void disconnecting() {
				setEnabled(false);
			}
		});
	}
	
	private void checkNulls() {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		if (updater == null) updater = JGlideMon.instance.ssUpdater;
	}
	
	public void setEnabled(boolean b) {
		//super.setEnabled(b);
		refresh.setEnabled(b);
	}
	
	public void actionPerformed(ActionEvent e) {
		checkNulls();
		
		if (e.getSource() == refresh) {
			log.finer("Want to update SS");
			
			if (updater != null && updater.idle) {
				log.finer("Updating SS");
				updater.thread.interrupt();
			}	
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		checkNulls();
		
		// if user switches to this tab, force an update
		// since we stopped updating when the tab wasn't selected
		if (this.isCurrentTab()) {
			log.finer("Want to update SS");
			
			if (updater != null && updater.thread != null && updater.idle) {
				log.finer("Updating SS");
				updater.thread.interrupt();
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {	
		checkNulls();
		if (!Connector.isConnected() || !keysEnabled.isSelected()) return;
		
		Dimension s = ssLabel.getSize();
		int x = e.getX(); int y = e.getY();
		
		float xp = x / (float) s.width;
		float yp = y / (float) s.height;
		
		String btn = 
			e.getButton() == MouseEvent.BUTTON1
			? "left" : "right";
		
		//System.out.println(btn + " click @ " + x + "," + y + " (" + xp + "," + yp + ") [" + s.width + "x" + s.height + "]");
		try {
			conn.send("/setmouse " + xp + "/" + yp);
			log.fine(conn.readLine()); conn.readLine();
			conn.send("/clickmouse " + btn);
			log.fine(conn.readLine()); conn.readLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//if (updater != null && updater.idle) {
			updater.thread.interrupt();
		//}
	}
	
	private static Map<Integer, Boolean> keysDown
		= new HashMap<Integer, Boolean>();
	
	public void keyPressed(KeyEvent e) {
		if (!this.isCurrentTab() || !keysEnabled.isSelected()) return;
		
		checkNulls();
		if (!Connector.isConnected()) return;
		
		int code = e.getKeyCode();
		
		if (!isGoodKey(code)) {
			log.fine("Bad key: " + code + ", " + KeyEvent.getKeyText(code));
			return;
		}
		
		if (e.isAltDown() && code == KeyEvent.VK_F4) {
			log.fine("NOT sending Alt+F4!");
			return;
		}
		
		if (keysDown.containsKey(code)) {
			return;
		}
		
		keysDown.put(code, Boolean.TRUE);
		
		try {
			conn.send("/holdkey " + code);
			log.fine(conn.readLine() + " " + KeyEvent.getKeyText(code)); conn.readLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void keyReleased(KeyEvent e) {
		if (!this.isCurrentTab() || !keysEnabled.isSelected()) return;
		
		checkNulls();
		//System.out.println(e);

		if (!Connector.isConnected()) {
			keysDown.clear();
			return;
		}
		
		int code = e.getKeyCode();
		
		if (null == keysDown.remove(code)) return;
		
		if (!isGoodKey(code)) {
			//System.out.println("Bad key: " + code + ", " + KeyEvent.getKeyText(code));
			return;
		}

		try {
			conn.send("/releasekey " + code);
			log.fine(conn.readLine() + " " + KeyEvent.getKeyText(code)); conn.readLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void keyTyped(KeyEvent e) {}
	
	private boolean isGoodKey(int code) {
		// A-Z, 0-9, F1-F12, NOT arrows because left/right will
		// end up switching tabs
		if (KeyEvent.VK_A <= code && code <= KeyEvent.VK_Z ||
			KeyEvent.VK_0 <= code && code <= KeyEvent.VK_9 || 
			KeyEvent.VK_F1 <= code && code <= KeyEvent.VK_F12 /*||
			KeyEvent.VK_LEFT <= code && code <= KeyEvent.VK_DOWN*/) {
			return true;
		}
		
		switch (code) {
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_PERIOD:
			case KeyEvent.VK_COMMA:
			case KeyEvent.VK_SLASH:
			case KeyEvent.VK_SEMICOLON:
			case KeyEvent.VK_QUOTE:
			case KeyEvent.VK_OPEN_BRACKET:
			case KeyEvent.VK_CLOSE_BRACKET:
			case KeyEvent.VK_BACK_SLASH:
			case KeyEvent.VK_BACK_SPACE:
			case KeyEvent.VK_BACK_QUOTE:
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_EQUALS:
				return true;
		}
				
		return false;
	}
}