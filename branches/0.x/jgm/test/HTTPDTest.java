package jgm.test;

import jgm.*;

/**
 * Simple test for HTTPD. Note that this was used during
 * development but it won't run standalone anymore since 
 * it requires the main JGlideMon program running to retrieve
 * config settings.
 * 
 * @author Tim
 *
 */

public class HTTPDTest {
	public static void main(String[] args) {		
		try {
			new HTTPD(8080);
			HTTPD.instance.start();
		} catch (java.io.IOException e) {
			System.err.println("Port 8080 already in use");
			System.exit(1);
		}
		
		// wait for enter
		try { System.in.read(); } catch( Throwable t ) {};
		
		HTTPD.instance.stop();
	}
}
