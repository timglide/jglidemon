package jgm.httpd;

import java.util.Properties;
import java.util.logging.Logger;

public abstract class Handler {
	protected static Logger log = Logger.getLogger(Handler.class.getName());
	
	public abstract Response handle(String uri, String method, Properties headers, Properties params);
}
