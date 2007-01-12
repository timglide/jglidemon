package jgm.gui.tabs;

import jgm.JGlideMon;
import jgm.glider.*;
import jgm.gui.updaters.SSUpdater;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;
import javax.swing.*;

public class ScreenshotTab extends Tab
	implements ChangeListener, MouseListener, KeyListener {
	private static GliderConn conn = null;
	private static SSUpdater updater = null;
	
	public JTextField keysField;
	public JLabel ssLabel;
	public ImageIcon ssIcon;

	public ScreenshotTab() {
		super(new BorderLayout(), "Screenshot");
		
		JPanel jp = new JPanel(new GridBagLayout());
		c.weightx = 0.0;
		jp.add(new JLabel("Chars typed in the textfield will be sent immediately: "), c);
		keysField = new JTextField();
		keysField.addKeyListener(this);
		keysField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				keysField.setText("");
			}
		});
		c.gridx++; c.weightx = 1.0;
		jp.add(keysField, c);
		add(jp, BorderLayout.NORTH);
		
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
			updater.thread.interrupt();
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {
		if (conn == null) conn = JGlideMon.instance.keysConn;
		if (updater == null) updater = JGlideMon.instance.ssUpdater;
		
		if (!Connector.isConnected()) return;
		
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
		System.out.println(conn.readLine()); conn.readLine();
		conn.send("/clickmouse " + btn);
		System.out.println(conn.readLine()); conn.readLine();
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
		if (conn == null) conn = JGlideMon.instance.keysConn;
		if (updater == null) updater = JGlideMon.instance.ssUpdater;

		if (!Connector.isConnected()) return;
		
		int code = e.getKeyCode();
		
		if (!isGoodKey(code)) {
			System.out.println("Bad key: " + code + ", " + KeyEvent.getKeyText(code));
			return;
		}
		
		if (e.isAltDown() && code == KeyEvent.VK_F4) {
			System.out.println("NOT sending Alt+F4!");
			return;
		}
		
		if (keysDown.containsKey(code)) {
			return;
		}
		
		keysDown.put(code, Boolean.TRUE);
		
	try {
		conn.send("/holdkey " + code);
		System.out.println(conn.readLine() + " " + KeyEvent.getKeyText(code)); conn.readLine();
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	}
	
	public void keyReleased(KeyEvent e) {
		//System.out.println(e);

		if (!Connector.isConnected()) {
			keysDown.clear();
			return;
		}
		
		int code = e.getKeyCode();
		
		keysDown.remove(code);
		
		if (!isGoodKey(code)) {
			//System.out.println("Bad key: " + code + ", " + KeyEvent.getKeyText(code));
			return;
		}

	try {
		conn.send("/releasekey " + code);
		System.out.println(conn.readLine() + " " + KeyEvent.getKeyText(code)); conn.readLine();
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	}
	
	public void keyTyped(KeyEvent e) {}
	
	private boolean isGoodKey(int code) {
		// A-Z, 0-9, F1-F12, 4 arrows
		if (KeyEvent.VK_A <= code && code <= KeyEvent.VK_Z ||
			KeyEvent.VK_0 <= code && code <= KeyEvent.VK_9 || 
			KeyEvent.VK_F1 <= code && code <= KeyEvent.VK_F12 ||
			KeyEvent.VK_LEFT <= code && code <= KeyEvent.VK_DOWN) {
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