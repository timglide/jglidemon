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
import java.util.jar.*;
import java.util.Properties;


/**
 * This class is for serving the static httpd files
 * once JGlideMon has been compiled and packaged into
 * a JAR file.
 * @author Tim
 */
public class JarFilesHandler extends Handler {
	static final String BASE_FOLDER = "jgm/resources/httpd/static/";
	JarFile jar;
	
	public JarFilesHandler(HTTPD httpd, File f) throws IOException {
		super(httpd);
		
		jar = new JarFile(f);
		
/*		java.util.Enumeration<JarEntry> e = jar.entries();

		while (e.hasMoreElements()) {
			System.out.println("--> " + e.nextElement());
		}*/
	}
	
	@Override
	public Response handle(String uri, String method, Properties headers,
			Properties params) {
		// Remove URL arguments
		uri = uri.trim().replace( File.separatorChar, '/' );
		if ( uri.indexOf( '?' ) >= 0 )
			uri = uri.substring(0, uri.indexOf( '?' ));

		// Prohibit getting out of current directory
		if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
			return new Response( HTTPD.HTTP_FORBIDDEN, HTTPD.MIME_PLAINTEXT,
								 "FORBIDDEN: Won't serve ../ for security reasons." );

		JarEntry je = jar.getJarEntry(BASE_FOLDER + uri);
		
		log.finer("Trying to serve jar://" + BASE_FOLDER + uri);
		
		if (je == null) {
			return Response.NOT_FOUND;
		}
		
		if (je.isDirectory()) {
			uri += "index.html";
			log.finer("Given a directory, checking jar://" + BASE_FOLDER + uri);
			je = jar.getJarEntry(BASE_FOLDER + uri);
			
			if (je == null) {
				return Response.NOT_FOUND;
			}
		}
		
		// Get MIME type from file name extension, if possible
		String mime = null;
		int dot = uri.lastIndexOf( '.' );
		if ( dot >= 0 )
			mime = HTTPD.theMimeTypes.get( uri.substring( dot + 1 ).toLowerCase());
		if ( mime == null )
			mime = HTTPD.MIME_DEFAULT_BINARY;
		
		Response ret = null;
		
		try {
			ret = new Response(HTTPD.HTTP_OK, mime, jar.getInputStream(je));
		} catch (IOException e) {
			ret = new Response( HTTPD.HTTP_INTERNALERROR, HTTPD.MIME_PLAINTEXT,
				"INTERNAL ERRROR: " + e.getClass().getName() + ": " + e.getMessage() );
		}
		
		return ret;
	}

}
