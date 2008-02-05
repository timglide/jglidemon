package jgm;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

public class MakeDefProps {
	static Properties p = new Properties();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		doClass(Config.defaults.class, "");
		
		try {
			FileOutputStream fs = new FileOutputStream(
				"JGlideMon.defaults.properties");
			
			p.store(fs, "Default JGlideMon settings");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	static void doClass(Class c, String prefix) {
		for (Class cc : c.getClasses()) {
			prefix += cc.getSimpleName() + ".";
			
			for (Field f : cc.getFields()) {		
				try {
					p.setProperty(prefix + f.getName(), f.get(null).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			doClass(cc, prefix);
		}
	}
}
