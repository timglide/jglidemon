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
import java.util.Properties;
import jgm.HTTPD;
import jgm.gui.tabs.ScreenshotTab;
import jgm.gui.updaters.SSUpdater;

public class ScreenshotHandler extends Handler {
	static ScreenshotTab tab = jgm.GUI.instance.tabsPane.screenshotTab;
	
	@Override
	public Response handle(String uri, String method, Properties headers, Properties params) {
		// updating of the screenshot is only done withon SSUpdater
		// otherwise very bad things could happen....
		
		if (SSUpdater.buff != null) {
			synchronized(SSUpdater.buff) {
				InputStream data = new ByteArrayInputStream(SSUpdater.buff);
				Response ret = new Response(HTTPD.HTTP_OK, "image/jpeg", data);
				ret.addHeader("Cache-Control", "no-cache, must-revalidate");
				ret.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
				return ret;
			}
		}
		
		return Response.NOT_FOUND;
	}

}
