package jgm;

import java.util.logging.*;

public class Log {
	public static void reloadConfig() {
		System.setProperty(
			"java.util.logging.config.file",
			Config.getInstance().getBool("general", "debug") ? "logging.debug.properties" : "logging.properties"
		);
		
		try {
			LogManager.getLogManager().readConfiguration();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setLevel(Level l) {
		Logger.getLogger("jgm").setLevel(l);
	}
}
