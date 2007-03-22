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
public class Conn {
	static Logger log = Logger.getLogger(Conn.class.getName());
	
	private static int instances = 0;
	
	private Socket         s;
	private PrintWriter    out;
	private InputStream    inStream;
	private BufferedReader in;
	private static Config cfg;
	
	public Conn() {
		++instances;
		
		if (cfg == null) {
			cfg = jgm.Config.getInstance();
		}
	}
	
	public synchronized void connect()
		throws UnknownHostException, IOException {
		s = null; out = null; inStream = null; in = null;
		
		//try {
			log.info("Connecting to " + cfg.getString("net", "host") + "...");
			s   = new Socket(cfg.getString("net", "host"), cfg.getInt("net", "port"));
			out = new PrintWriter(s.getOutputStream(), false);
			inStream = new BufferedInputStream(s.getInputStream());
			in  = new BufferedReader(
			          new InputStreamReader(inStream));
			send(cfg.getString("net", "password"));
			in.readLine(); // ignore Authenticated OK line
			
			notifyAll();
		/*} catch (UnknownHostException e) {
			s = null; out = null; inStream = null; in = null;
			System.err.println("Cannot connect to " + cfg.net.host);
			//System.exit(1);
		} catch (IOException e) {
			s = null; out = null; inStream = null; in = null;
			System.err.println("Cannot initialize socket to " + cfg.net.host);
			System.err.println("  Error initializing I/O " + e.getMessage());
			//System.exit(1);
		}*/
	}

	public boolean isConnected() {
		return s != null && s.isConnected() && !s.isOutputShutdown();
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

	public void send(String str) {
		while (!isConnected()) {}
		
		//try {
			out.print(str + "\r\n"); out.flush();
		/*} catch (Exception e) {
			System.err.println("Error sending '" + str + "'. " + e.getMessage());
		}*/
	}

	public int read() throws IOException {
		return inStream.read();
	}

	public int read(byte[] buff) throws IOException {
		return inStream.read(buff);
	}

	public int read(byte[] buff, int off, int len) throws IOException {
		return inStream.read(buff, off, len);
	}

	public long skip(long n) throws IOException {
		return inStream.skip(n);
	}

	public String readLine() throws IOException {
		return in.readLine();
	}

	public void close() {
		try {			
			if (isConnected()) {
				try {
					send("/exit");
					in.readLine(); // Bye!
//					Thread.sleep(500);
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
