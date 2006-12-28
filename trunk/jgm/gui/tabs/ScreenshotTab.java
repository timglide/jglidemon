package jgm.gui.tabs;

import jgm.JGlideMon;
import jgm.glider.GliderConn;
import jgm.gui.updaters.SSUpdater;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;
import javax.swing.*;

public class ScreenshotTab extends Tab
	implements ChangeListener, MouseListener, KeyListener {
	private static GliderConn conn = null;
	private static SSUpdater updater = null;
	
	public JLabel ssLabel;
	public ImageIcon ssIcon;

	public ScreenshotTab() {
		super(new BorderLayout(), "Screenshot");
		
		ssLabel = new JLabel();
		ssLabel.setHorizontalAlignment(JLabel.CENTER);
		ssLabel.addMouseListener(this);
		ssLabel.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel p = new JPanel();
		p.add(ssLabel);
		
		add(p, BorderLayout.CENTER);
	}
	
	public void stateChanged(ChangeEvent e) {
		// if user switches to this tab, force an update
		// since we stopped updating when the tab wasn't selected
		if (updater != null && this.isCurrentTab() && updater.idle) {
			updater.interrupt();
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		if (updater == null) updater = JGlideMon.instance.ssUpdater;
		
		Dimension s = ssLabel.getSize();
		int x = e.getX(); int y = e.getY();
		
		float xp = x / (float) s.width;
		float yp = y / (float) s.height;
		
		String btn = 
			e.getButton() == MouseEvent.BUTTON1
			? "left" : "right";
		
		System.out.println(btn + " click @ " + x + "," + y + " (" + xp + "," + yp + ") [" + s.width + "x" + s.height + "]");
		
		conn.send("/setmouse " + xp + "/" + yp);
		System.out.println(conn.readLine()); conn.readLine();
		conn.send("/clickmouse " + btn);
		System.out.println(conn.readLine()); conn.readLine();
		
		//if (updater != null && updater.idle) {
			updater.interrupt();
		//}
	}
	
	public void keyPressed(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		if (updater == null) updater = JGlideMon.instance.ssUpdater;
		
		System.out.println(e);
		//displayInfo(e, "Released: ");
	}
	
	public void keyTyped(KeyEvent e) {}
	
	protected void displayInfo(KeyEvent e, String s){
	        //You should only rely on the key char if the event
	        //is a key typed event.
		String keyString = null;
		String modString = null;
		String tmpString = null;
		String actionString = null;
		String locationString = null;
	        int id = e.getID();
	        if (id == KeyEvent.KEY_TYPED) {
	            char c = e.getKeyChar();
	            keyString = "key character = '" + c + "'";
	        } else {
	            int keyCode = e.getKeyCode();
	            keyString = "key code = " + keyCode
	                        + " ("
	                        + KeyEvent.getKeyText(keyCode)
	                        + ")";
	        }

	        int modifiers = e.getModifiersEx();
	        modString = "modifiers = " + modifiers;
	        tmpString = KeyEvent.getModifiersExText(modifiers);
	        if (tmpString.length() > 0) {
	            modString += " (" + tmpString + ")";
	        } else {
	            modString += " (no modifiers)";
	        }

	        actionString = "action key? ";
	        if (e.isActionKey()) {
	            actionString += "YES";
	        } else {
	            actionString += "NO";
	        }

	        locationString = "key location: ";
	        int location = e.getKeyLocation();
	        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
	            locationString += "standard";
	        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
	            locationString += "left";
	        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
	            locationString += "right";
	        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
	            locationString += "numpad";
	        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
	            locationString += "unknown";
	        }
	        
	        System.out.println(keyString + "\n" + modString + "\n" +
	        tmpString + "\n" + actionString + "\n" + locationString);
	    }
}
