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
		// force an invisible update if the tab isn't the current
		// tab, in which case it's already updating
		if (!tab.isCurrentTab()) {
			try {
				log.fine("Updating screenshot from HTTPD::ScreenshotHandler");
				jgm.JGlideMon.instance.ssUpdater.update();
			} catch (Throwable t) {
				log.log(java.util.logging.Level.WARNING, "Error updating screenshot from HTTPD::ScreenshotHandler", t);
				return Response.NOT_FOUND;
			}
		}
		
		if (SSUpdater.buff != null) {
			synchronized(SSUpdater.buff) {
				InputStream data = new ByteArrayInputStream(SSUpdater.buff);
				return new Response(HTTPD.HTTP_OK, "image/jpeg", data);
			}
		}
		
		return Response.NOT_FOUND;
	}

}
