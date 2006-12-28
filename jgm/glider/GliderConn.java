package jgm.glider;

import jgm.cfg;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class GliderConn extends Thread {
	private static int instances = 0;
	
	private boolean        stop = false;
	private Socket         s;
	private PrintWriter    out;
	private InputStream    inStream;
	private BufferedReader in;

	public GliderConn() {
		super("GliderConn" + (++instances));
		start();
	}
	
	public void run() {
		while (!stop) {
			while (!isConnected()) {
				connect();
				
				if (!isConnected()) {
					try {
						System.out.println("  not connected, waiting");
						sleep(5000);
					} catch (InterruptedException e) {
						System.out.println(getName() + " interrupted");
					}
					
					if (stop) return;
				}
			}
			
			try {
				System.out.println(getName() + " tick");
				sleep(20000);
			} catch (InterruptedException e) {
				System.out.println(getName() + " interrupted");
				return;
			}
		}
	}
	
	public synchronized void connect() {
		s = null; out = null; inStream = null; in = null;
		
		try {
			System.out.println("Connecting to " + cfg.net.host + "...");
			s   = new Socket(cfg.net.host, cfg.net.port);
			out = new PrintWriter(s.getOutputStream(), true);
			inStream = new BufferedInputStream(s.getInputStream());
			in  = new BufferedReader(
			          new InputStreamReader(inStream));
			send(cfg.net.password);
			in.readLine(); // ignore Authenticated OK line
			
			notifyAll();
		} catch (UnknownHostException e) {
			s = null; out = null; inStream = null; in = null;
			System.err.println("Cannot connect to " + cfg.net.host);
			//System.exit(1);
		} catch (IOException e) {
			s = null; out = null; inStream = null; in = null;
			System.err.println("Cannot initialize socket to " + cfg.net.host);
			System.err.println("  Error initializing I/O " + e.getMessage());
			//System.exit(1);
		}
	}

	public boolean isConnected() {
		return s != null && s.isConnected() && !s.isOutputShutdown();
	}
	
/*	public GliderConn(JTextArea text) {
		this();

		jgm.gui.Console.redirect(in, text);

		send("/log all");
	}*/

	public InputStream getInStream() {
		return inStream;
	}

	public BufferedReader getIn() {
		return in;
	}

	public PrintWriter getOut() {
		return out;
	}

	public void send(String str) {
		while (!isConnected()) {}
		
		try {
			out.println(str);
		} catch (Exception e) {
			System.err.println("Error sending '" + str + "'. " + e.getMessage());
		}
	}

	public int read() {
		while (!isConnected()) {}
		
		try {
			return inStream.read();
		} catch (IOException e) {
			System.err.println("IOE: " + e.getMessage());
			return -1;
		}
	}

	public int read(byte[] buff) {
		while (!isConnected()) {}
		
		try {
			return inStream.read(buff);
		} catch (IOException e) {
			System.err.println("IOE: " + e.getMessage());
			return -1;
		}
	}

	public int read(byte[] buff, int off, int len) {
		while (!isConnected()) {}
		
		try {
			return inStream.read(buff, off, len);
		} catch (IOException e) {
			System.err.println("IOE: " + e.getMessage());
			return -1;
		}
	}

	public long skip(long n) {
		while (!isConnected()) {}
		
		try {
			return inStream.skip(n);
		} catch (IOException e) {
			System.err.println("IOE: " + e.getMessage());
			return 0;
		}
	}

	public String readLine() {
		while (!isConnected()) {}
		
		try {
			return in.readLine();
		} catch (IOException e) {
			System.err.println("IOE: " + e.getMessage());
			return null;
		}
	}

	public void close() {
		try {
			stop = true;
			this.interrupt();
			send("/exit");
			in.readLine(); // Bye!
//			Thread.sleep(500);
			in.close();
			out.close();
			s.close();
		} catch (Exception e) {}
	}
}
