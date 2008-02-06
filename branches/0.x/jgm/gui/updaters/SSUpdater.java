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
package jgm.gui.updaters;

import jgm.*;

import jgm.glider.*;
import jgm.gui.tabs.*;

import java.util.Observer;
import java.util.logging.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
 
public class SSUpdater implements Observer, Runnable, ConnectionListener {
	static Logger log = Logger.getLogger(SSUpdater.class.getName());
	
	public  volatile boolean idle = true;
	private volatile boolean stop = false;
	private volatile boolean attached = false;
	public  volatile boolean sentSettings = false;
	public  volatile boolean redoScale = true; // so it will happen initially
	
	Conn conn = null;

	ScreenshotTab tab;
 
	public Thread thread;
	
	Config cfg;
	jgm.ServerManager sm;
	
	public SSUpdater(jgm.ServerManager sm, ScreenshotTab t) {
		this.sm = sm;
		cfg = jgm.Config.getInstance();
		tab  = t;
		conn = new Conn(sm);
	}

	public Conn getConn() {
		return conn;
	}
	
	public void onConnecting() {}
	
	public void onConnect() {
		sentSettings = false;
		stop = false;
		thread = new Thread(this, "SSUpdater");
		thread.start();
	}
	
	public void onDisconnecting() {}
	
	public void onDisconnect() {
		stop = true;
	}
	
	public void close() {
		stop = true;
		if (thread != null) thread.interrupt();
		if (conn != null) conn.close();
	}

	//private static final int MAX_SIZE = 1048576;
	public static byte[] buff = null; //new byte[MAX_SIZE];
	
	public boolean update() throws IOException, InterruptedException {
	  synchronized (conn) {
		
		if (stop) return false;
		
		GUI.setStatusBarText("Updating screenshot...", true, false);
		GUI.setStatusBarProgress(0);
		
		int buffSize = (int) (cfg.getDouble("screenshot.buffer") * 1048576);
		
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
				conn = new Conn(sm);
				
				try {
					//conn.getInStream().skip(conn.getInStream().available());
					conn.connect();
				} catch (Throwable e) {
					log.log(Level.WARNING, "SSWatcher", e);
					//System.err.println("SSWatcher: " + e.getClass().getName() + ": " + e.getMessage());
					sm.connector.disconnect();
				}
				
				GUI.revertStatusBarText();
				GUI.unlockStatusBarText();
				GUI.hideStatusBarProgress();
				
				this.cancel();
			}
		}, cfg.getInt("screenshot.timeout") * 1000);
		
		if (!sentSettings) {
			log.finer("Sending screenshot settings");
			conn.send("/capturescale " + cfg.get("screenshot.scale"));
			//System.out.println(conn.readLine()); // set scale successfully
			//System.out.println(conn.readLine()); // ---
			log.finer(conn.readLine()); // set scale successfully
			conn.readLine(); // ---
			conn.send("/capturequality " + cfg.get("screenshot.quality"));
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
		
		if (redoScale && cfg.getBool("screenshot.autoscale")) {
			log.fine("Attempting to set screenshot scale...");
			
			double curScale = cfg.getDouble("screenshot.scale") / 100;
			double newScale = curScale;
			int iwidth = icon.getIconWidth();
			int iheight = icon.getIconHeight();
			// double iratio = (double) iwidth / iheight;
			
			int realIWidth = (int) ((double) iwidth / curScale);
			int realIHeight = (int) ((double) iheight / curScale);
			
			// try to account for margin and width of border
//			int pwidth = tab.jsp.getWidth() - 20;
//			int pheight = tab.jsp.getHeight() - 20;

			int pwidth = tab.getWidth() - 20;
			int pheight = tab.getHeight() - 20;
			
			int dx = realIWidth - pwidth;
			int dy = realIHeight - pheight;
			
			log.finer(
				String.format(
					"Current: scale=%s; size=%sx%s; real size=%sx%s",
					curScale, iwidth, iheight, realIWidth, realIHeight
				)
			);
			
			if (dy >= dx) {
				newScale = (double) pheight / (double) realIHeight;
			} else {
				newScale = (double) pwidth / (double) realIWidth;
			}
			
			// -3 to be safe because there's a margin between the
			// screenshot image and the jscrollpane i don't know how
			// to 'properly' account for
			int newScaleInt = (int) (newScale * 100) /*- 3*/;
			
			// ensure it's within bounds
			newScaleInt = Math.min(newScaleInt, 99);
			newScaleInt = Math.max(newScaleInt, 10);
			
			log.fine("Setting scale to " + newScaleInt + "%");
			
			cfg.set("screenshot.scale", newScaleInt);
			redoScale = false;
			sentSettings = false;
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
					
					// update if either the screenshot tab is viewable or if the
					// webserver is enabled so that only one thread ever tries to
					// update the screenshot
					if (attached && (tab.isCurrentTab() || cfg.getBool("web.enabled"))) update();
				} catch (InterruptedException e) {
					log.fine(thread.getName() + " interrupted within update()");
				} catch (Exception e) {
					log.fine("Stopping SSUpdater, Ex: " + e.getMessage());
					idle = true;
					GUI.revertStatusBarText();
					GUI.unlockStatusBarText();
					GUI.hideStatusBarProgress();
					JGlideMon.getCurManager().connector.disconnect();
					return;
				}
				
				idle = true;
				Thread.sleep(cfg.getInt("screenshot.updateInterval"));
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
		Status s = (Status) o;
		attached = s.attached;
	}
}
