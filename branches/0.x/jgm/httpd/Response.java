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

import jgm.HTTPD;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * HTTP response.
 * Return one of these from serve().
 */
public class Response
{
	public static final Response NOT_FOUND =
		new Response(HTTPD.HTTP_NOTFOUND, HTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
	
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public Response()
	{
		this.status = HTTPD.HTTP_OK;
	}

	/**
	 * Basic constructor.
	 */
	public Response( String status, String mimeType, InputStream data )
	{
		this.status = status;
		this.mimeType = mimeType;
		this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of
	 * given text.
	 */
	public Response( String status, String mimeType, String txt )
	{
		this.status = status;
		this.mimeType = mimeType;
		this.data = new ByteArrayInputStream( txt.getBytes());
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader( String name, String value )
	{
		header.put( name, value );
	}

	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	public String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader()
	 * to add lines.
	 */
	public java.util.Properties header = new java.util.Properties();
}
