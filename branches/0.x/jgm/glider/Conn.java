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
package jgm.glider;

import jgm.*;

import java.util.logging.*;
import java.io.*;
import java.net.*;

/**
 * Represents a connection to the Glider remote client.
 * @author Tim
 * @since 0.1
 */

// TODO Figure out proper synchronization to eliminate
// Invalid LogEntry "Bye!" upon disconnect
public class Conn {
	static Logger log = Logger.getLogger(Conn.class.getName());
	
	private static int instances = 0;
	
	public String host;
	public int    port;
	public String password;
	
	private Socket         s;
	private PrintWriter    out;
	private InputStream    inStream;
	private BufferedReader in;
	
	public Conn(ServerManager sm) {
		host = sm.host;
		port = sm.port;
		password = sm.password;
		
		++instances;
	}
	
	public synchronized void connect()
		throws UnknownHostException, IOException {
		s = null; out = null; inStream = null; in = null;

		log.info("Connecting to " + host + "...");
		s   = new Socket(host, port);
		out = new PrintWriter(s.getOutputStream(), false);
		inStream = new BufferedInputStream(s.getInputStream());
		in  = new BufferedReader(
		          new InputStreamReader(inStream, "UTF-8"));
		send(password);
		in.readLine(); // ignore Authenticated OK line
		
		notifyAll();
	}

	public boolean isConnected() {
//		synchronized (s) {
			return s != null && s.isConnected() && !s.isOutputShutdown();
//		}
	}

	public InputStream getInStream() {
		return inStream;
	}

	public BufferedReader getIn() {
		return in;
	}

//	public PrintWriter getOut() {
//		return out;
//	}

	public String cmd(String cmd) throws IOException {
		send(cmd);
		
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		while (in.ready() && null != (line = in.readLine())) {
			if (line.equals("---"))
				break;
			
			sb.append(line);
		}
		
		return sb.toString();
	}
	
	public void send(String str) {
		while (!isConnected()) {}
		
		//try {
//			synchronized (out) {
				out.print(str + "\r\n"); out.flush();
//			}
		/*} catch (Exception e) {
			System.err.println("Error sending '" + str + "'. " + e.getMessage());
		}*/
	}

	public int read() throws IOException {
//		synchronized (inStream) {
			return inStream.read();
//		}
	}

	public int read(byte[] buff) throws IOException {
//		synchronized (inStream) {
			return inStream.read(buff);
//		}
	}

	public int read(byte[] buff, int off, int len) throws IOException {
//		synchronized (inStream) {
			return inStream.read(buff, off, len);
//		}
	}

	public long skip(long n) throws IOException {
//		synchronized (inStream) {
			return inStream.skip(n);
//		}
	}

	public String readLine() throws IOException {
//		synchronized (in) {
			return in.readLine();
//		}
	}

	public void close() {
		try {			
			if (isConnected()) {
				try {
					synchronized (s) {
						send("/exit");
						in.readLine(); // Bye!
//						Thread.sleep(500);
					}
				} catch (IOException e) {}
			}
			
			if (in != null) in.close();
			if (out != null) out.close();
			if (s != null) s.close();
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Exception during close", e);
		}
		
		in = null; out = null; s = null;
	}
	
	protected void finalize() {
		close();
	}
}
