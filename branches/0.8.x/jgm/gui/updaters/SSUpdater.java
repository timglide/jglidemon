package jgm.gui.updaters;

import jgm.Config;
import jgm.GUI;

import jgm.glider.*;
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

	private Conn conn = null;

	private ScreenshotTab tab;
 
	public Thread thread;
	
	private Config cfg;
	
	public SSUpdater(ScreenshotTab t) {
		cfg = jgm.Config.getInstance();
		tab  = t;
		conn = new Conn();
	}

	public Conn getConn() {
		return conn;
	}
	
	public void connecting() {}
	
	public void connectionEstablished() {
		stop = false;
		thread = new Thread(this, "SSUpdater");
		thread.start();
	}
	
	public void disconnecting() {}
	
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
	
	public boolean update() throws IOException, InterruptedException {
	  synchronized (conn) {
		
		if (stop) return false;
		
		GUI.setStatusBarText("Updating screenshot...", true, false);
		GUI.setStatusBarProgress(0);
		
		/* This timer will interrupt the screenshot updater
		 * in 5 seconds if it fails to update the screenshot.
		 */
		java.util.Timer timer = new java.util.Timer("SSWatcher");
		timer.schedule(new java.util.TimerTask() {
			final Thread t = Thread.currentThread();
			
			public void run() {
				System.out.println("SSWatcher forcing abort of SS update...");
				t.interrupt();

				conn.close();
				conn = new Conn();
				
				try {
					//conn.getInStream().skip(conn.getInStream().available());
					conn.connect();
				} catch (Throwable e) {
					System.err.println("SSWatcher: " + e.getClass().getName() + ": " + e.getMessage());
					Connector.disconnect();
				}
				
				GUI.revertStatusBarText();
				GUI.unlockStatusBarText();
				GUI.hideStatusBarProgress();
				
				this.cancel();
			}
		}, 5000);
		
		conn.send("/capturescale " + cfg.get("screenshot", "scale"));
		//System.out.println(conn.readLine()); // set scale successfully
		//System.out.println(conn.readLine()); // ---
		conn.readLine(); // set scale successfully
		conn.readLine(); // ---
		conn.send("/capturequality " + cfg.get("screenshot", "quality"));
		//System.out.println(conn.readLine()); // set quality successfully
		//System.out.println(conn.readLine()); // ---
		conn.readLine(); // set quality successfully
		conn.readLine(); // ---
		conn.send("/capture");
		String line = conn.readLine(); // info stating stuff about the datastream
		
		if (!line.startsWith("Success")) {
			timer.cancel();
			
			System.err.println("Didn't receive Success upon /capture. Got: " + line);
			GUI.revertStatusBarText();
			GUI.unlockStatusBarText();
			GUI.hideStatusBarProgress();
			return false;
		}
		
		//System.out.println("Info: " + line); 

		byte[] b = new byte[4];
		conn.read(b);
		//System.out.println("Read " + conn.read(b) + " for size");

		int size = jgm.Util.byteArrayToInt(b);
		//System.out.println("\nJPG Size: " + size);
		//size &= 0x1FFFFF; // restrict to ~2 megs 
		int written = 0;

		//System.out.println(", after: " + size);

		if (size < 1 || size > MAX_SIZE) { // size invalid? wtf O.o
			timer.cancel();
			
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

			GUI.revertStatusBarText();
			GUI.unlockStatusBarText();
			GUI.hideStatusBarProgress();
			
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
		//System.out.println(conn.readLine()); // ---

		timer.cancel();
		
		//System.out.println("Read " + written + " for image");

		//System.out.println("Making ss...");
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
					//while (attached && tab.isCurrentTab() && !update()) {}
					if (attached && tab.isCurrentTab()) update();
				} catch (InterruptedException e) {
					System.out.println(thread.getName() + " interrupted within update()");
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
				Thread.sleep(cfg.getInt("screenshot", "updateInterval"));
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
