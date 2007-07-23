package jgm.httpd;

import jgm.HTTPD;
import java.io.*;
import java.util.Properties;

public class FilesHandler extends Handler {
	File baseDir;
	
	public FilesHandler(File baseDir) {
		super();
		
		if (!baseDir.isDirectory())
			throw new IllegalArgumentException("baseDir must be a directory");
		
		this.baseDir = baseDir;
	}
	
	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 */
	@Override
	public Response handle(String uri, String method, Properties headers, Properties params) {
		// Make sure we won't die of an exception later
		if ( !baseDir.isDirectory())
			return new Response( HTTPD.HTTP_INTERNALERROR, HTTPD.MIME_PLAINTEXT,
								 "INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );

		// Remove URL arguments
		uri = uri.trim().replace( File.separatorChar, '/' );
		if ( uri.indexOf( '?' ) >= 0 )
			uri = uri.substring(0, uri.indexOf( '?' ));

		// Prohibit getting out of current directory
		if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
			return new Response( HTTPD.HTTP_FORBIDDEN, HTTPD.MIME_PLAINTEXT,
								 "FORBIDDEN: Won't serve ../ for security reasons." );

		File f = new File( baseDir, uri );
		
		if ( !f.exists())
			return Response.NOT_FOUND;

		// List the directory, if necessary
		if ( f.isDirectory())
		{
			// Browsers get confused without '/' after the
			// directory, send a redirect.
//			if ( !uri.endsWith( "/" ))
//			{
//				uri += "/";
//				Response r = new Response( HTTPD.HTTP_REDIRECT, HTTPD.MIME_HTML,
//										   "<html><body>Redirected: <a href=\"" + uri + "\">" +
//										   uri + "</a></body></html>");
//				r.addHeader( "Location", uri );
//				return r;
//			}

			// First try index.html and index.htm
			if ( new File( f, "index.html" ).exists())
				f = new File( baseDir, uri + "/index.html" );
			else if ( new File( f, "index.htm" ).exists())
				f = new File( baseDir, uri + "/index.htm" );

			// No index file, list the directory
			else if ( true )
			{
				String[] files = f.list();
				String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

				if ( uri.length() > 1 )
				{
					String u = uri.substring( 0, uri.length()-1 );
					int slash = u.lastIndexOf( '/' );
					if ( slash >= 0 && slash  < u.length())
						msg += "<b><a href=\"" + uri.substring(0, slash+1) + "\">..</a></b><br/>";
				}

				for ( int i=0; i<files.length; ++i )
				{
					File curFile = new File( f, files[i] );
					boolean dir = curFile.isDirectory();
					if ( dir )
					{
						msg += "<b>";
						files[i] += "/";
					}

					msg += "<a href=\"" + HTTPD.encodeUri( /*uri +*/ files[i] ) + "\">" +
						   files[i] + "</a>";

					// Show file size
					if ( curFile.isFile())
					{
						long len = curFile.length();
						msg += " &nbsp;<font size=2>(";
						if ( len < 1024 )
							msg += curFile.length() + " bytes";
						else if ( len < 1024 * 1024 )
							msg += curFile.length()/1024 + "." + (curFile.length()%1024/10%100) + " KB";
						else
							msg += curFile.length()/(1024*1024) + "." + curFile.length()%(1024*1024)/10%100 + " MB";

						msg += ")</font>";
					}
					msg += "<br/>";
					if ( dir ) msg += "</b>";
				}
				return new Response( HTTPD.HTTP_OK, HTTPD.MIME_HTML, msg );
			}
			else
			{
				return new Response( HTTPD.HTTP_FORBIDDEN, HTTPD.MIME_PLAINTEXT,
								 "FORBIDDEN: No directory listing." );
			}
		}

		try
		{
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf( '.' );
			if ( dot >= 0 )
				mime = HTTPD.theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
			if ( mime == null )
				mime = HTTPD.MIME_DEFAULT_BINARY;

			// Support (simple) skipping:
			long startFrom = 0;
			String range = headers.getProperty( "Range" );
			if ( range != null )
			{
				if ( range.startsWith( "bytes=" ))
				{
					range = range.substring( "bytes=".length());
					int minus = range.indexOf( '-' );
					if ( minus > 0 )
						range = range.substring( 0, minus );
					try	{
						startFrom = Long.parseLong( range );
					}
					catch ( NumberFormatException nfe ) {}
				}
			}

			FileInputStream fis = new FileInputStream( f );
			fis.skip( startFrom );
			Response r = new Response( HTTPD.HTTP_OK, mime, fis );
			r.addHeader( "Content-length", "" + (f.length() - startFrom));
			r.addHeader( "Content-range", "" + startFrom + "-" +
						(f.length()-1) + "/" + f.length());
			
			// prevent caching for the moment
			r.addHeader("Cache-Control", "no-cache, must-revalidate"); // HTTP/1.1
			r.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past
			return r;
		}
		catch( IOException ioe )
		{
			return new Response( HTTPD.HTTP_FORBIDDEN, HTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
		}
	}
}
