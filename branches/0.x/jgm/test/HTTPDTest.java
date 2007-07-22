package jgm.test;

import jgm.*;

public class HTTPDTest {
	public static void main(String[] args) {		
		try {
			new HTTPD(8080);
		} catch (java.io.IOException e) {
			System.err.println("Port 8080 already in use");
			System.exit(1);
		}
		
		// wait for enter
		try { System.in.read(); } catch( Throwable t ) {};
	}
}
