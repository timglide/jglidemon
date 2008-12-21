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
package jgm.util;

import java.lang.reflect.Method;
import java.io.*;

import jgm.Config;

/**
 * Contains some utility functions 
 * @author Tim
 * @since 0.1
 */
public class Util {	
	/**
	 * Convert a raw (unsigned) byte to an int since
	 * Java doesn't actually have unsigned numbers.
	 * @param b The byte to convert
	 * @return The converted byte
	 */
	public static final int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	/**
	 * Convert an array with 4 bytes to the 32-bit integer
	 * that it would represent.
	 * @param buf An array of 4 bytes to convert
	 * @return The converted int
	 * @throws IllegalArgumentException If buf is not length 4
	 */
	public static final int byteArrayToInt(byte[] buf) 
		throws IllegalArgumentException {
		if (buf.length != 4) 
			throw new IllegalArgumentException("Need array of length 4");
		
		int i   = 0;
		int pos = 0;
/*		System.out.printf("Bytes: 0x%X 0x%X 0x%X 0x%X\n",
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]),
			unsignedByteToInt(buf[pos++]));
		pos = 0;
*/		i += unsignedByteToInt(buf[pos++]) << 0;
		i += unsignedByteToInt(buf[pos++]) << 8;
		i += unsignedByteToInt(buf[pos++]) << 16;
		i += unsignedByteToInt(buf[pos++]) << 24;

		return i;
		//return (int) ((Long.MAX_VALUE & ~((long) Integer.MAX_VALUE)) ^ ((long) i));
	}

	/**
	 * Swap an integer between big and little endian.
	 * @param i The integer to swap
	 * @return The swapped integer
	 */
	public final static int swapInt(int i) {
		return (i >>> 24) | (i << 24) | 
				((i << 8) & 0x00FF0000) | ((i >> 8) & 0x0000FF00);
	}
	
	/**
	 * Resize an ImageIcon to the specified size.
	 * @param icon The ImageIcon to resize
	 * @param width The new width
	 * @param height The new height
	 */
	public final static javax.swing.ImageIcon resizeIcon(javax.swing.ImageIcon icon, int width, int height) {
		java.awt.Image img = icon.getImage();
		java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		
		java.awt.Graphics g = bi.createGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		
		return new javax.swing.ImageIcon(bi);
	}
	
	/**
	 * Convert supplied number of milliseconds into hours, 
	 * minutes, and seconds.
	 * @param ms The number of milliseconds
	 * @return An array with hours, minutes, seconds
	 */
	public static int[] msToHMS(long ms) {
		int[] out = new int[3];
		
		int sec = (int) (ms / 1000); // seconds		
		int min = sec / 60;
		sec -= min * 60;
		int hr = min / 60;
		min -= hr * 60;
		
		out[0] = hr;
		out[1] = min;
		out[2] = sec;
		
		return out;
	}
	
	
	
/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 1.5                                        //
//  December 10, 2005                                  //
//  http://www.centerkey.com/java/browser/             //
//  Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////

	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		
		try {
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL =
					fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else { //assume Unix or Linux
				String[] browsers = {
					"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
				String browser = null;
				
				for (String s : browsers) {
					if (Runtime.getRuntime().exec(
						new String[] {"which", s}).waitFor() == 0) {
						browser = s;
						break;
					}
				}
				
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] {browser, url});
				}
			}
		} catch (Throwable e) {}
	}
	
	
	public static final File debugInfoFile = new File("DebugInfo.txt");
	
	public static void generateDebugInfo() {
		//FileWriter fw = null;
		//BufferedWriter bw = null;
		PrintWriter pw = null;
		
		FileOutputStream fs = null;
		PrintStream ps = null;
		
		try {
			//fw = new FileWriter(debugInfoFile);
			//bw = new BufferedWriter(fw);
			//pw = new PrintWriter(bw);
			
			fs = new FileOutputStream(debugInfoFile);
			ps = new PrintStream(fs);
			
			ps.println("Java Properties");
			ps.println("---------------");
			
			for (Object key : new java.util.TreeSet<Object>(System.getProperties().keySet())) {
				ps.println(key + ": " + System.getProperty(key.toString()));
			}
			
			
			ps.println();
			ps.println("JGlideMon Properties");
			ps.println("--------------------");
			
			ps.println("version: " + jgm.JGlideMon.version);
			ps.println("debug: " + Boolean.toString(jgm.JGlideMon.debug));
			ps.println(jgm.JGlideMon.revision);
			ps.println(jgm.JGlideMon.date);
			
			ps.println();
			ps.println("JGlideMon Config");
			ps.println("----------------");
						
			for (Object key : new java.util.TreeSet<Object>(Config.getProps().keySet())) {
				ps.println(key + ": " + Config.getProps().getProperty(key.toString()));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
			if (ps != null)
				ps.close();
		}
	}
}
