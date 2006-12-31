package jgm.gui.updaters;

import jgm.cfg;

import jgm.glider.*;
import jgm.gui.tabs.*;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
 
public class SSUpdater implements Runnable, ConnectionListener {
	public boolean idle = false;
	private boolean stop = false;

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
		thread = new Thread(this, "SSUpdater");
		thread.start();
	}
	
	public void close() {
		stop = true;
		thread.interrupt();
		conn.close();
	}

	public boolean update() throws IOException {
	  synchronized (conn) {
		
		if (stop) return false;
				
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

//		System.out.println("Making ss...");
		BufferedImage img =
			javax.imageio.ImageIO.read(
				new ByteArrayInputStream(buff)
			);
		ImageIcon icon = new ImageIcon(img);
		tab.ssLabel.setIcon(icon);
		tab.ssLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

		return true;	  
	  }
	}

	public void run() {
		while (!stop) {
			try {
				// don't update if the tab isn't showing
				idle = false;
				
				try {
					while (tab.isCurrentTab() && !update()) {}
				} catch (IOException e) {
					e.printStackTrace();
					idle = true;
					return;
				}
				
				idle = true;
				Thread.sleep(cfg.screenshot.updateInterval);
			} catch (InterruptedException e) {
				System.out.println(thread.getName() + " interrupted");
				Thread.interrupted();
				
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
