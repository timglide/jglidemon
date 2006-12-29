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
		
		//System.out.println(btn + " click @ " + x + "," + y + " (" + xp + "," + yp + ") [" + s.width + "x" + s.height + "]");
		
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
		
		int code = e.getKeyCode();
		
		boolean goodKey = false;
		
		switch (code) {
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
				goodKey = true;
		}
		
		// A-Z, 0-9, F1-F12, 4 arrows
		if (KeyEvent.VK_A <= code && code <= KeyEvent.VK_Z ||
			KeyEvent.VK_0 <= code && code <= KeyEvent.VK_9 || 
			KeyEvent.VK_F1 <= code && code <= KeyEvent.VK_F12 ||
			KeyEvent.VK_LEFT <= code && code <= KeyEvent.VK_DOWN) {
			goodKey = true;
		}
		
		 if (!goodKey) {
			System.out.println("Bad key: " + code + ", " + KeyEvent.getKeyText(code));
			return;
		}
		
		if (e.isAltDown() && code == KeyEvent.VK_F4) {
			System.out.println("NOT sending Alt+F4!");
			return;
		}
			
		if (e.isControlDown()) {
			conn.send("/holdkey " + KeyEvent.VK_CONTROL);
			System.out.println(conn.readLine()); conn.readLine();
		}
			
		if (e.isAltDown()) {
			conn.send("/holdkey " + KeyEvent.VK_ALT);
			System.out.println(conn.readLine()); conn.readLine();
		}
			
		if (e.isShiftDown()) {
			conn.send("/holdkey " + KeyEvent.VK_SHIFT);
			System.out.println(conn.readLine()); conn.readLine();
		}
			
		conn.send("/forcekeys #" + code + "#");
		System.out.println(conn.readLine()); conn.readLine();
			
		if (e.isShiftDown()) {
			conn.send("/releasekey " + KeyEvent.VK_SHIFT);
			System.out.println(conn.readLine()); conn.readLine();
		}
			
		if (e.isAltDown()) {
			conn.send("/releasekey " + KeyEvent.VK_ALT);
			System.out.println(conn.readLine()); conn.readLine();
		}
			
		if (e.isControlDown()) {
			conn.send("/releasekey " + KeyEvent.VK_CONTROL);
			System.out.println(conn.readLine()); conn.readLine();
		}
	}
	
	public void keyTyped(KeyEvent e) {}
}
