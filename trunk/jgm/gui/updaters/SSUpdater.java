package jgm.gui.updaters;

import jgm.cfg;

import jgm.glider.GliderConn;
import jgm.gui.tabs.*;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
 
public class SSUpdater extends Thread {
	public boolean idle = false;
	private boolean stop = false;

	private GliderConn conn = null;

	private ScreenshotTab tab;
 
	public SSUpdater(ScreenshotTab t) {
		super("SSUpdater");

		tab  = t;

		start();
	}

	public void close() {
		stop = true;
		this.interrupt();
		conn.close();
	}

	public boolean update() {
		if (conn == null) conn = new GliderConn();

	  synchronized (conn) {
		
		if (!conn.isConnected()) {
			try {
				conn.wait();
			} catch (InterruptedException e) {
				System.out.println(this.getName() + " interrupted");
			}
		}
		
		if (stop) return false;
		
	  try {
		
		System.gc();
		conn.send("/capturescale " + cfg.screenshot.scale);
		conn.readLine(); // set scale successfully
		conn.readLine(); // ---
		conn.send("/capture");
		conn.readLine(); // info stating stuff about the datastream
//		System.out.println("Info: _" + conn.readLine() + "_"); 

		byte[] b = new byte[4];
		conn.read(b);

//		System.out.println("Read " + conn.read(b) + " for size");

		int size    = jgm.Util.byteArrayToInt(b);
		int written = 0;

//		System.out.print("\nJPG Size: " + size + "\n_");

		if (size < 1 || size > 150000) { // size invalid? wtf O.o
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

			return false;
		}

		byte[] buff = new byte[size];

		while (written < size) {
			written += conn.read(buff, written, size - written);
		}

		conn.readLine(); // ---
//		System.out.println(conn.readLine()); // ---

//		System.out.println("Read " + written + " for image");

		try {
//			System.out.println("Making ss...");
			BufferedImage img =
				javax.imageio.ImageIO.read(
					new ByteArrayInputStream(buff)
				);
			ImageIcon icon = new ImageIcon(img);
			tab.ssLabel.setIcon(icon);
			tab.ssLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		} catch (IOException e) {
			e.printStackTrace();
			conn.wait();
		} catch (NullPointerException e) {
			System.err.println("NULL: " + e.getMessage());
		}

		return true;
	  } catch (Exception e) {
		  return false;
	  }
	  
	  }
	}

	public void run() {
		while (!stop) {
			try {
				// don't update if the tab isn't showing
				idle = false;
				while (tab.isCurrentTab() && !update()) {}
				idle = true;
				sleep(cfg.screenshot.updateInterval);
			} catch (InterruptedException e) {
				System.out.println(getName() + " interrupted");
				interrupted();
				
				if (stop) {
					return;
				} else {
					System.out.println("Refreshing screenshot immediately");
				}
			}
			
			idle = false;
		}
	}
}
