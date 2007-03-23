package jgm.gui.updaters;

import jgm.*;

import jgm.glider.*;
import jgm.gui.tabs.*;

import java.util.Observer;
import java.util.logging.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
 
public class SSUpdater implements Observer, Runnable, ConnectionListener {
	static Logger log = Logger.getLogger(SSUpdater.class.getName());
	
	public  volatile boolean idle = true;
	private volatile boolean stop = false;
	private volatile boolean attached = false;
	public  volatile boolean sentSettings = false;
	
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
		sentSettings = false;
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

	//private static final int MAX_SIZE = 1048576;
	private static byte[] buff = null; //new byte[MAX_SIZE];
	
	public boolean update() throws IOException, InterruptedException {
	  synchronized (conn) {
		
		if (stop) return false;
		
		GUI.setStatusBarText("Updating screenshot...", true, false);
		GUI.setStatusBarProgress(0);
		
		int buffSize = (int) (cfg.getDouble("screenshot", "buffer") * 1048576);
		
		if (buff == null || buff.length != buffSize) {
			log.fine("Allocating ss buffer of size " + buffSize);
			buff = new byte[buffSize];
		}
		
		/* This timer will interrupt the screenshot updater
		 * in X seconds if it fails to update the screenshot.
		 */
		java.util.Timer timer = new java.util.Timer("SSWatcher");
		timer.schedule(new java.util.TimerTask() {
			final Thread t = Thread.currentThread();
			
			public void run() {
				log.warning("SSWatcher forcing abort of SS update...");
				t.interrupt();

				conn.close();
				conn = new Conn();
				
				try {
					//conn.getInStream().skip(conn.getInStream().available());
					conn.connect();
				} catch (Throwable e) {
					log.log(Level.WARNING, "SSWatcher", e);
					//System.err.println("SSWatcher: " + e.getClass().getName() + ": " + e.getMessage());
					Connector.disconnect();
				}
				
				GUI.revertStatusBarText();
				GUI.unlockStatusBarText();
				GUI.hideStatusBarProgress();
				
				this.cancel();
			}
		}, cfg.getInt("screenshot", "timeout") * 1000);
		
		if (!sentSettings) {
			log.finer("Sending screenshot settings");
			conn.send("/capturescale " + cfg.get("screenshot", "scale"));
			//System.out.println(conn.readLine()); // set scale successfully
			//System.out.println(conn.readLine()); // ---
			log.finer(conn.readLine()); // set scale successfully
			conn.readLine(); // ---
			conn.send("/capturequality " + cfg.get("screenshot", "quality"));
			//System.out.println(conn.readLine()); // set quality successfully
			//System.out.println(conn.readLine()); // ---
			log.finer(conn.readLine()); // set quality successfully
			conn.readLine(); // ---
		}
		
		conn.send("/capture");
		String line = conn.readLine(); // info stating stuff about the datastream
		
		if (!line.startsWith("Success")) {
			timer.cancel();
			
			log.fine("Didn't receive Success upon /capture. Got: " + line);
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

		if (size < 1 || size > buff.length) { // size invalid? wtf O.o
			timer.cancel();
			
			String s = null;
			int z = 0;
			log.fine("Invalid size: " + size + ", clearing stream");

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

			log.fine("Clear Stream done");	

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
		
		if (!sentSettings) {
			tab.ssLabel.setSize(icon.getIconWidth(), icon.getIconHeight());
			sentSettings = true;
		}
		
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
					log.fine(thread.getName() + " interrupted within update()");
				} catch (Exception e) {
					log.fine("Stopping SSUpdater, Ex: " + e.getMessage());
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
				log.fine(thread.getName() + " interrupted");
				Thread.interrupted();

				idle = true;
				
				if (stop) {
					return;
				} else {
					log.info("Refreshing screenshot immediately");
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
