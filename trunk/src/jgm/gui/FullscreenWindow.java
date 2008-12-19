package jgm.gui;

import java.awt.*;
import javax.swing.*;

public class FullscreenWindow extends JFrame {
	final GraphicsEnvironment env;
	final GraphicsDevice device;
	
	public FullscreenWindow() {
		this(null);
	}
	
	public FullscreenWindow(Color bg) {
		env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = env.getDefaultScreenDevice();
		
		setUndecorated(true);
		// can still alt-tab without this
//		setAlwaysOnTop(true);
		setResizable(false);
		setFocusable(true);
		
		if (bg != null) {
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setBackground(bg);
			setContentPane(panel);
			setRootPaneCheckingEnabled(true);
		}
	}
	
	public void goFullscreen() {
		if (!isShowing())
			throw new IllegalStateException("Cannot go fullscreen without window showing");
		
//		GraphicsConfiguration conf = device.getDefaultConfiguration();
//		Rectangle bounds = env.getMaximumWindowBounds();
//		Rectangle bounds = conf.getBounds();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// don't realy want exclusive mode, this does it
		// similarly to how firefox's fullscreen works
//		if (false && device.isFullScreenSupported()) {
//			System.out.println("Trying to go fullscreen");
//			try {
//				DisplayMode mode = device.getDisplayMode();
//				device.setFullScreenWindow(this);
//				setSize(mode.getWidth(), mode.getHeight());
//				setLocation(0, 0);
//			} catch (Exception e) {
//				e.printStackTrace();
//				device.setFullScreenWindow(null);
//			}
//		} else {
//			System.out.println("True fullscreen not supported");
			
			setLocation(0, 0);
			setSize(screenSize);
//		}
	}
	
	public void goNormal() {
		device.setFullScreenWindow(null);
	}
	
	protected void finalize() {
		goNormal();
	}
}
