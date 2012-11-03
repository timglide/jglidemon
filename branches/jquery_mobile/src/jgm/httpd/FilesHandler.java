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

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

public class FilesHandler extends Handler {
	File baseDir;
	
	public FilesHandler(HTTPD httpd, File baseDir) {
		super(httpd);
		
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
			// not fully working yet :/
//			String ifModSince = headers.getProperty("if-modified-since");
//			
//			if (null != ifModSince) {
//				try {
//					Date lastMod = HTTPD.gmtFrmt.parse(ifModSince);
//					
//					if (f.lastModified() <= lastMod.getTime()) {
//						// the file isn't newer than the time specified in the header
//						return new Response(HTTPD.HTTP_NOTMODIFIED, null, (InputStream)null);
//					}
//				} catch (ParseException e) {
//					log.log(Level.FINE, "Couldn't parse if-mod-since date: " + ifModSince, e);
//					// return the file normally below
//				}
//			}
			
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf( '.' );
			if ( dot >= 0 )
				mime = HTTPD.theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
			if ( mime == null )
				mime = HTTPD.MIME_DEFAULT_BINARY;

			// Support (simple) skipping:
			long startFrom = 0;
			/*String range = headers.getProperty( "Range" );
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
			}*/

			FileInputStream fis = new FileInputStream( f );
			fis.skip( startFrom );
			Response r = new Response( HTTPD.HTTP_OK, mime, fis );
			//r.addHeader( "Content-Length", "" + (f.length() - startFrom));
			//r.addHeader( "Content-range", "" + startFrom + "-" +
			//			(f.length()-1) + "/" + f.length());
			
			r.addHeader("Last-Modified", HTTPD.gmtFrmt.format(f.lastModified()));
			return r;
		}
		catch( IOException ioe )
		{
			return new Response( HTTPD.HTTP_FORBIDDEN, HTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
		}
	}
}
