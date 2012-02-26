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
package jgm.httpd;

import jgm.JGlideMon;
import jgm.ServerManager;
import jgm.util.Properties;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.net.*;

/**
 * This webserver is based on NanoHTTPD by Jarno Elonen
 * but it has been heavily modified. Note: some of the
 * original code has been spread out over several files.
 * http://elonen.iki.fi/code/nanohttpd/
 * @since 0.11
 */

/*
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java
 *
 * <p> NanoHTTPD version 1.1,
 * Copyright &copy; 2001,2005-2007 Jarno Elonen (elonen@iki.fi, http://iki.fi/elonen/)
 *
 * <p><b>Features + limitations: </b><ul>
 *
 *    <li> Only one Java file </li>
 *    <li> Java 1.1 compatible </li>
 *    <li> Released as open source, Modified BSD licence </li>
 *    <li> No fixed config files, logging, authorization etc. (Implement yourself if you need them.) </li>
 *    <li> Supports parameter parsing of GET and POST methods </li>
 *    <li> Supports both dynamic content and file serving </li>
 *    <li> Never caches anything </li>
 *    <li> Doesn't limit bandwidth, request time or simultaneous connections </li>
 *    <li> Default code serves files and shows all HTTP parameters and headers</li>
 *    <li> File server supports directory listing, index.html and index.htm </li>
 *    <li> File server does the 301 redirection trick for directories without '/'</li>
 *    <li> File server supports simple skipping for files (continue download) </li>
 *    <li> File server uses current directory as a web root </li>
 *    <li> File server serves also very long files without memory overhead </li>
 *    <li> Contains a built-in list of most common mime types </li>
 *    <li> All header names are converted lowercase so they don't vary between browsers/clients </li>
 *
 * </ul>
 *
 * <p><b>Ways to use: </b><ul>
 *
 *    <li> Run as a standalone app, serves files from current directory and shows requests</li>
 *    <li> Subclass serve() and embed to your own program </li>
 *    <li> Call serveFile() from serve() with your own base directory </li>
 *
 * </ul>
 *
 * See the end of the source file for distribution license
 * (Modified BSD licence)
 */
public class HTTPD implements Runnable
{
	public static SimpleDateFormat RFC822_DATE = 
    	new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
	
	protected static Logger log = Logger.getLogger(HTTPD.class.getName());
	
	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.<p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @parm uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @parm method	"GET", "POST" etc.
	 * @parm parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @parm header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve( String uri, String method, Properties headers, Properties params )
	{
		
		// get rid of leading/trailing slash
		if (uri.charAt(0) == '/')
			uri = uri.substring(1);
		if (uri.length() > 1 && uri.charAt(uri.length() - 1) == '/')
			uri = uri.substring(0, uri.length() - 1);
		
		if ("".equals(uri)) {
			String ua = headers.get("user-agent").toLowerCase();
			if(ua.matches(".*(android.+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")||ua.substring(0,4).matches("1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|e\\-|e\\/|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\\-|2|g)|yas\\-|your|zeto|zte\\-")) {
				uri = "static/mobile.html";
			}
		}
		
		String[] parts = uri.split("/", 2);
		
		Response ret = null;
		
//		System.out.println("parts:");
//		for (int i = 0; i < parts.length; i++)
//			System.out.println("\t" + parts[i]);
		
		if (null != handlers.get(parts[0])) {
			ret = handlers.get(parts[0]).handle(parts.length > 1 ? parts[1] : "", method, headers, params);
		} else {
			log.fine("Url not found: [" + uri + "], parts: " + Arrays.toString(parts));
			ret = Response.NOT_FOUND;
		}
		
		return ret;
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String
		HTTP_OK = "200 OK",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_NOTMODIFIED = "304 Not Modified",
		HTTP_UNAUTHORIZED = "401 Unauthorized",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String
		MIME_PLAINTEXT = "text/plain",
		MIME_HTML = "text/html",
		MIME_DEFAULT_BINARY = "application/octet-stream";

	public Thread thread = null;
	public ServerSocket ss = null;
	private volatile boolean run_thread = true;
	
	protected Map<String, Handler> handlers = new HashMap<String, Handler>();
	public final ServerManager sm;
	
	// ==================================================
	// Socket & server code
	// ==================================================
	
	/**
	 * Starts a HTTP server to given port.<p>
	 * Throws an IOException if the socket is already in use
	 */
	public HTTPD(ServerManager sm)
	{
		this.sm = sm;
		
		File jf = null;
		
		// this should account for the jar file being named
		// anything, not that people should be renaming stuff
		try {
			URI tmp = JGlideMon.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			
			if (!tmp.toString().endsWith(".jar")) throw new Exception();
			
			jf = new File(tmp);
		} catch (Throwable t) {
			jf = new File("JGlideMon.jar");
		}
		
		File f = new File("jgm/resources/httpd/static");
		// System.out.println("-=-=-=: " + jf.getAbsolutePath());
		
		try {
			if (jf.exists()) {
				log.finest("Reading static files from JAR: " + jf.getCanonicalPath());
				handlers.put("static", new JarFilesHandler(this, jf));
			} else {
				if (!f.exists()) {
					f = new File("bin/jgm/resources/httpd/static");
				}
				
				if (!f.exists()) {
					f = new File("src/jgm/resources/httpd/static");
				}
				
				if (!f.exists()) {
					f = null;
				}
					
				if (null != f) {
					log.finest("Reading static files from folder: " + f.getCanonicalPath());
					handlers.put("static", new FilesHandler(this, f));
				} else {
					log.warning("Unable to locate static files to serve");
				}
			}
		} catch (Throwable e) {
			log.log(java.util.logging.Level.WARNING, "Exception initiating HTTPD", e);
		}
		
		handlers.put("", handlers.get("static"));
		handlers.put("ajax", new AjaxHandler(this));
		handlers.put("screenshot", new ScreenshotHandler(this));
	}
	
	public void start() throws IOException {	
		if (thread != null) {
			throw new IllegalStateException("Cannot start httpd with thread != null");
		}
		
		if (ss != null) {
			throw new IllegalStateException("Cannot start httpd with ss != null");
		}
		
		int port = sm.getInt("web.port");
		
		log.info("Attempting to start httpd on port " + port);
		
		run_thread = true;

		ss = new ServerSocket(port);
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	
	public void stop() {
		if (ss != null) {
			try {
				ss.close();
			} catch (IOException e) {}
			ss = null;
		}
		
		log.info("Stopping httpd");
		run_thread = false;
		thread = null;
	}

	public void run() {
		try {
			while (run_thread)
				new HTTPSession(ss.accept());
		} catch (IOException ioe) {}
	}
	
	/**
	 * Starts as a standalone file server and waits for Enter.
	 */
//	public static void main( String[] args )
//	{
//		System.out.println( "NanoHTTPD 1.1 (C) 2001,2005-2007 Jarno Elonen\n" +
//							"(Command line options: [port] [--licence])\n" );
//
//		// Show licence if requested
//		int lopt = -1;
//		for ( int i=0; i<args.length; ++i )
//		if ( args[i].toLowerCase().endsWith( "licence" ))
//		{
//			lopt = i;
//			System.out.println( LICENCE + "\n" );
//		}
//
//		// Change port if requested
//		int port = 80;
//		if ( args.length > 0 && lopt != 0 )
//			port = Integer.parseInt( args[0] );
//
//		if ( args.length > 1 &&
//			 args[1].toLowerCase().endsWith( "licence" ))
//				System.out.println( LICENCE + "\n" );
//
//		try
//		{
//			new HTTPD( port );
//			HTTPD.instance.start();
//		}
//		catch( IOException ioe )
//		{
//			System.err.println( "Couldn't start server:\n" + ioe );
//			System.exit( -1 );
//		}
//		//nh.myFileDir = new File("");
//
//		System.out.println( "Now serving files in port " + port + " from \"" +
//							new File("").getAbsolutePath() + "\"" );
//		System.out.println( "Hit Enter to stop.\n" );
//
//		try { System.in.read(); } catch( Throwable t ) {};
//	}

	/**
	 * Handles one session, i.e. parses the HTTP request
	 * and returns the response.
	 */
	private class HTTPSession implements Runnable
	{
		public HTTPSession( Socket s )
		{
			mySocket = s;
			Thread t = new Thread( this );
			t.setDaemon( true );
			t.start();
		}

		public void run()
		{
			try
			{
				InputStream is = mySocket.getInputStream();
				if ( is == null) return;
				BufferedReader in = new BufferedReader( new InputStreamReader( is ));

				// Read the request line
				StringTokenizer st = new StringTokenizer( in.readLine());
				if ( !st.hasMoreTokens())
					sendError( HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html" );

				String method = st.nextToken();

				if ( !st.hasMoreTokens())
					sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );

				String uri = decodePercent( st.nextToken());

				// Decode parameters from the URI
				Properties parms = new Properties();
				String origUri = uri;
				int qmi = uri.indexOf( '?' );
				if ( qmi >= 0 )
				{
					decodeParms( uri.substring( qmi+1 ), parms );
					uri = decodePercent( uri.substring( 0, qmi ));
				}

				
				
				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names uppercase since they are
				// case insensitive and vary by client.
				Properties header = new Properties();
				if ( st.hasMoreTokens())
				{
					String line = in.readLine();
					while ( line.trim().length() > 0 )
					{
						int p = line.indexOf( ':' );
						header.put( line.substring(0,p).trim().toLowerCase(), line.substring(p+1).trim());
						line = in.readLine();
					}
				}

				// If the method is POST, there may be parameters
				// in data section, too, read it:
				if ( method.equalsIgnoreCase( "POST" ))
				{
					long size = 0x7FFFFFFFFFFFFFFFl;
					String contentLength = header.getProperty("content-length");
					if (contentLength != null)
					{
						try { size = Integer.parseInt(contentLength); }
						catch (NumberFormatException ex) {}
					}
					String postLine = "";
					char buf[] = new char[512];
					int read = in.read(buf);
					while ( read >= 0 && size > 0 && !postLine.endsWith("\r\n") )
					{
						size -= read;
						postLine += String.valueOf(buf, 0, read);
						if ( size > 0 )
							read = in.read(buf);
					}
					postLine = postLine.trim();
					decodeParms( postLine, parms );
				}

				
				// set some server variables
				// (this is done after parsing get and post vars
				if ( qmi >= 0 ) {
					parms.setProperty("QUERY_STRING", origUri.substring(qmi));
				} else {
					parms.setProperty("QUERY_STRING", "");
				}
				
				parms.setProperty("URI", origUri);
				
				
				// parse the auth header if provided
				// see http://en.wikipedia.org/wiki/Basic_authentication_scheme
				String auth = header.getProperty("authorization");
				if (auth != null) {
					if (auth.startsWith("Basic ")) auth = auth.substring(6);
					
					try {
//						System.out.println("decoding: " + auth + ";");
						auth = jgm.util.Base64.decodeString(auth);
						String[] parts = auth.split(":", 2);
						
//						System.out.println("user: " + parts[0] + "; pass: " + parts[1] + ";");
						
						parms.setProperty("HTTP_USER", parts[0]);
						parms.setProperty("HTTP_PASS", parts[1]);
					} catch (Throwable t) {
						parms.remove("HTTP_USER");
						parms.remove("HTTP_PASS");
					}
				}
				
				String logStr = method + " " + uri;
				
				Response r = null;
				
				// ensure password was provided
				if (parms.containsKey("HTTP_PASS") &&
					parms.getProperty("HTTP_PASS").equals(sm.password)) {
					r = serve(uri, method, header, parms);
				} else {
					r = new Response(HTTPD.HTTP_UNAUTHORIZED, HTTPD.MIME_PLAINTEXT, "Authorization required");
					r.addHeader("WWW-Authenticate", "Basic realm=\"JGlideMon " + JGlideMon.version + "\"");
				}
				
				if (r == null) {
					sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
					logStr += " - " + HTTP_INTERNALERROR;
				} else {
					String acceptEncoding = header.getProperty("accept-encoding");
					
					if (null != acceptEncoding && acceptEncoding.contains("gzip")) {
						r.addHeader("Content-Encoding", "gzip");
					}
					
					sendResponse( r.status, r.mimeType, r.header, r.data );
					logStr += " - " + r.status;
				}
				
				
				// for debugging
//				System.out.println(logStr);
				log.fine(logStr);
				
				/*Enumeration e = header.propertyNames();
				while ( e.hasMoreElements())
				{
					String value = (String)e.nextElement();
					log.finest( "  HDR: '" + value + "' = '" +
										header.getProperty( value ) + "'" );
				}
				e = parms.propertyNames();
				while ( e.hasMoreElements())
				{
					String value = (String)e.nextElement();
					log.finest( "  PRM: '" + value + "' = '" +
										parms.getProperty( value ) + "'" );
				}*/
				
				in.close();
			}
			catch ( IOException ioe )
			{
				try
				{
					sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				}
				catch ( Throwable t ) {}
			}
			catch ( InterruptedException ie )
			{
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent( String str ) throws InterruptedException
		{
			try
			{
				StringBuilder sb = new StringBuilder();
				for( int i=0; i<str.length(); i++ )
				{
				    char c = str.charAt( i );
				    switch ( c )
					{
				        case '+':
				            sb.append( ' ' );
				            break;
				        case '%':
			                sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
				            i += 2;
				            break;
				        default:
				            sb.append( c );
				            break;
				    }
				}
				return new String( sb.toString().getBytes());
			}
			catch( Exception e )
			{
				sendError( HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format
		 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Properties.
		 */
		private void decodeParms( String parms, Properties p )
			throws InterruptedException
		{
			if ( parms == null )
				return;

			StringTokenizer st = new StringTokenizer( parms, "&" );
			while ( st.hasMoreTokens())
			{
				String e = st.nextToken();
				int sep = e.indexOf( '=' );
				if ( sep >= 0 ) {
					p.put( decodePercent( e.substring( 0, sep )).trim(),
						   decodePercent( e.substring( sep+1 )));
				} else {
					p.put(decodePercent(e.trim()), "");
				}
			}
		}

		/**
		 * Returns an error message as a HTTP response and
		 * throws InterruptedException to stop furhter request processing.
		 */
		private void sendError( String status, String msg ) throws InterruptedException
		{
			sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse( String status, String mime, java.util.Properties header, InputStream data )
		{
			try
			{
				if ( status == null )
					throw new Error( "sendResponse(): Status can't be null." );

				OutputStream out = mySocket.getOutputStream();
				
				PrintWriter pw = new PrintWriter( out );
				pw.print("HTTP/1.0 " + status + " \r\n");

				if ( mime != null )
					pw.print("Content-Type: " + mime + "\r\n");

				if ( header == null || header.getProperty( "Date" ) == null )
					pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");

				if ( header != null )
				{
					header.remove("Content-Length");
					Enumeration<Object> e = header.keys();
					while ( e.hasMoreElements())
					{
						String key = (String)e.nextElement();
						String value = header.getProperty( key );
						pw.print( key + ": " + value + "\r\n");
					}
				}

				
				if ( data != null )
				{
					ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
					OutputStream contentOut = contentBytes;
					
					if ("gzip".equals(header.getProperty("Content-Encoding"))) {
						contentOut = new GZIPOutputStream(contentBytes);
					}
					
					byte[] buff = new byte[2048];
					while (true)
					{
						int read = data.read( buff, 0, buff.length);
						if (read <= 0)
							break;
						contentOut.write( buff, 0, read );
					}
					
					contentOut.flush();
					contentOut.close();

					pw.print("Content-Length: " + contentBytes.size() + "\r\n");
					pw.print("\r\n");
					pw.flush();
					
					contentBytes.writeTo(out);
					
					data.close();
				} else {
					pw.print("\r\n");
				}
				
				out.flush();
				out.close();
			}
			catch( IOException ioe )
			{
				log.log(Level.SEVERE, "Error sending response", ioe);
				// Couldn't write? No can do.
				try { mySocket.close(); } catch( Throwable t ) {}
			}
		}

		private Socket mySocket;
	};

	/**
	 * URL-encodes everything between "/"-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	public static String encodeUri( String uri )
	{
		String newUri = "";
		StringTokenizer st = new StringTokenizer( uri, "/ ", true );
		while ( st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if ( tok.equals( "/" ))
				newUri += "/";
			else if ( tok.equals( " " ))
				newUri += "%20";
			else
			{
				//newUri += URLEncoder.encode( tok );
				// For Java 1.4 you'll want to use this instead:
				try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( UnsupportedEncodingException uee ) {}
			}
		}
		return newUri;
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	public static Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();
	static
	{
		StringTokenizer st = new StringTokenizer(
			"htm		text/html "+
			"html		text/html "+
			"txt		text/plain "+
			"asc		text/plain "+
			"gif		image/gif "+
			"jpg		image/jpeg "+
			"jpeg		image/jpeg "+
			"png		image/png "+
			"mp3		audio/mpeg "+
			"m3u		audio/mpeg-url " +
			"pdf		application/pdf "+
			"doc		application/msword "+
			"ogg		application/x-ogg "+
			"zip		application/octet-stream "+
			"exe		application/octet-stream "+
			"class		application/octet-stream "+
			"js         text/text " +
			"css        text/css " + 
			"java       text/plain " +
			"xml        text/xml ");
		while ( st.hasMoreTokens())
			theMimeTypes.put( st.nextToken(), st.nextToken());
	}

	/**
	 * GMT date formatter
	 */
    public static java.text.SimpleDateFormat gmtFrmt;
	static
	{
		gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * The distribution licence
	 */
//	public static final String LICENCE =
//		"Copyright (C) 2001,2005 by Jarno Elonen <elonen@iki.fi>\n"+
//		"\n"+
//		"Redistribution and use in source and binary forms, with or without\n"+
//		"modification, are permitted provided that the following conditions\n"+
//		"are met:\n"+
//		"\n"+
//		"Redistributions of source code must retain the above copyright notice,\n"+
//		"this list of conditions and the following disclaimer. Redistributions in\n"+
//		"binary form must reproduce the above copyright notice, this list of\n"+
//		"conditions and the following disclaimer in the documentation and/or other\n"+
//		"materials provided with the distribution. The name of the author may not\n"+
//		"be used to endorse or promote products derived from this software without\n"+
//		"specific prior written permission. \n"+
//		" \n"+
//		"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"+
//		"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
//		"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"+
//		"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
//		"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
//		"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"+
//		"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"+
//		"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"+
//		"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"+
//		"OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}
