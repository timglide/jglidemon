package jgm.gui.updaters;

import jgm.cfg;

import jgm.glider.*;
import jgm.gui.GUI;
import jgm.gui.tabs.*;

import java.util.Observer;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
 
public class SSUpdater implements Observer, Runnable, ConnectionListener {
	public  volatile boolean idle = true;
	private volatile boolean stop = false;
	private volatile boolean attached = false;

	private GliderConn conn = null;

	private ScreenshotTab tab;
 
	public Thread thread;
	
	public SSUpdater(ScreenshotTab t) {
		tab  = t;
		conn = new GliderConn();
	}

	public GliderConn getConn() {
		return conn;
	}
	
	public void connectionEstablished() {
		stop = false;
		thread = new Thread(this, "SSUpdater");
		thread.start();
	}
	
	public void connectionDied() {
		stop = true;
	}
	
	public void close() {
		stop = true;
		if (thread != null) thread.interrupt();
		if (conn != null) conn.close();
	}

	private static final int MAX_SIZE = 1048576;
	private static byte[] buff = new byte[MAX_SIZE];
	
	public boolean update() throws IOException {
	  synchronized (conn) {
		
		if (stop) return false;
		
		GUI.setStatusBarText("Updating screenshot...", true, false);
		GUI.setStatusBarProgress(0);
		
		conn.send("/capturescale " + cfg.screenshot.scale);
		conn.readLine(); // set scale successfully
		conn.readLine(); // ---
		conn.send("/capturequality " + cfg.screenshot.quality);
		conn.readLine(); // set quality successfully
		conn.readLine(); // ---
		conn.send("/capture");
		conn.readLine(); // info stating stuff about the datastream
//		System.out.println("Info: _" + conn.readLine() + "_"); 

		byte[] b = new byte[4];
		conn.read(b);

//		System.out.println("Read " + conn.read(b) + " for size");

		int size = jgm.Util.byteArrayToInt(b);
		//System.out.print("\nJPG Size before: " + size);
		//size &= 0x1FFFFF; // restrict to ~2 megs 
		int written = 0;

		//System.out.println(", after: " + size);

		if (size < 1 || size > MAX_SIZE) { // size invalid? wtf O.o
			String s = null;
			int z = 0;
			System.err.println("Invalid size: " + size + ", clearing stream");

			while (null != (s = conn.readLine())) {
//				if (z % 100 == 0)
//					System.out.println(s);
				z++;

				int l = s.length();
				if (l >= 3 && s.lastIndexOf("---") == l - 3) {
//					System.out.println("Found ---");
					break;
				}
			} // clear the stream
	
//			System.out.println(s); // print last line

			System.err.println("Clear Stream done");	

			GUI.hideStatusBarProgress();
			GUI.unlockStatusBarText();
			
			return false;
		}

		//System.out.println("Reading...");
		while (written < size) {
			int read = conn.read(buff, written, size - written);
			
			if (read == -1) break;

			written += read;
			
			//for (int i = 0; i < 8; i++)
			//	System.out.print('\b');
			//System.out.print(written);
			
			int percent = (int) (((float) written / (float) size) * 100);
			GUI.setStatusBarProgress(percent);
		}
		
		//System.out.println();

		conn.readLine(); // ---
//		System.out.println(conn.readLine()); // ---

//		System.out.println("Read " + written + " for image");

//		System.out.println("Making ss...");
		BufferedImage img =
			javax.imageio.ImageIO.read(
				new ByteArrayInputStream(buff, 0, size)
			);
		ImageIcon icon = new ImageIcon(img);
		tab.ssLabel.setIcon(icon);
		tab.ssLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

		GUI.revertStatusBarText();
		GUI.unlockStatusBarText();
		GUI.hideStatusBarProgress();
		
		return true;	  
	  }
	}

	public void run() {
		while (!stop) {
			try {
				// don't update if the tab isn't showing
				idle = false;
				
				try {
					while (attached && tab.isCurrentTab() && !update()) {}
				} catch (Exception e) {
					System.err.println("Stopping SSUpdater, Ex: " + e.getMessage());
					idle = true;
					GUI.revertStatusBarText();
					GUI.unlockStatusBarText();
					GUI.hideStatusBarProgress();
					Connector.disconnect();
					return;
				}
				
				idle = true;
				Thread.sleep(cfg.screenshot.updateInterval);
			} catch (InterruptedException e) {
				System.out.println(thread.getName() + " interrupted");
				Thread.interrupted();

				idle = true;
				
				if (stop) {
					return;
				} else {
					System.out.println("Refreshing screenshot immediately");
				}
			}
			
			idle = true;
		}
	}
	
	public void update(java.util.Observable obs, Object o) {
		StatusUpdater s = (StatusUpdater) o;
		attached = s.attached;
	}
}
