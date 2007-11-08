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
				return new Response(HTTPD.HTTP_OK, "image/jpeg", data);
			}
		}
		
		return Response.NOT_FOUND;
	}

}
