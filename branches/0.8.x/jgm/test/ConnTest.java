package jgm.test;

import java.net.*;
import java.io.*;

/**
 * Tests connecting to Glider using a simplified
 * version of the code JGlideMon uses.
 * @author Tim
 */
public class ConnTest {
	public static void main(String[] args) throws Throwable {
		if (args.length != 3) {
			System.err.println("Usage: java -jar ConnTest.jar HOST PORT PASSWORD");
			System.exit(1);
		}

		String host = args[0];
		int    port = Integer.parseInt(args[1]);
		String pass = args[2];
		
		Socket         s;
		PrintStream    out;
		InputStream    inStream;
		BufferedReader in;
		
		System.out.println("Connecting to " + host + "...");
		
		s   = new Socket(host, port);
		
		System.out.println("Connected. Opening input and output...");
		
		out = new PrintStream(s.getOutputStream(), false);
		inStream = new BufferedInputStream(s.getInputStream());
		in  = new BufferedReader(
		          new InputStreamReader(inStream));
		
		System.out.println("IO opened. Sending password...");
		
		out.print(pass + "\r\n"); out.flush();
		in.readLine(); // ignore Authenticated OK line
		
		System.out.println("Authenticated. Retrieving status...");
		
		// retrieve status info
		out.print("/status\r\n"); out.flush();
		
		String line = null;
		
		while ((line = in.readLine()) != null) {
			if (line.equals("---")) break;
			System.out.println(line);
		}
		
		// clean up
		out.print("/exit\r\n"); out.flush();
		in.readLine(); // Bye!
		in.close();
		inStream.close();
		out.close();
		s.close();
	}
}
