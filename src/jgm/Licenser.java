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
package jgm;

import java.io.*;
import java.util.*;

/**
 * This prepends each .java file with the contents of
 * LicenseHeader.txt unless it has already been prepended.
 * @author Tim
 * @since 0.9.1
 */
public class Licenser {
	static final String LICENSE_START = "-----LICENSE START-----";
	static final String LICENSE_END   = "-----LICENSE END-----";
	
	static final int JAVA = 0;
	
	static final int PRE = 0;
	static final int LINE = 1;
	static final int POST = 2;
	
	static final String[][] commentData = {
		{"/*", " * ", " */"}
	};
	
	static List<String> licenseLines = null;
	
	public static void main(String[] args) throws Exception {
		licenseLines = new LinkedList<String>();
	    BufferedReader in =
	    	new BufferedReader(new FileReader("jgm/LicenseHeader.txt"));
	    
	    String line = null;
	    
	    while (null != (line = in.readLine())) {
	    	licenseLines.add(line);
	    }
	    
	    in.close();
	    
	    insertLicense(new File("./jgm"));
	}

	static void insertLicense(File dir) throws Exception {
		BufferedReader in = null;
		PrintWriter out = null;
		String line = null;
		
		for (File f :
			dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase().endsWith(".java");
				}
		})) {
			if (f.isDirectory()) {
				insertLicense(f);
				continue;
			}
			
			File to = new File(f.getAbsolutePath() + ".tmp");
			
			if (alreadyLicensed(f)) {
				System.out.println("Skipping licensed file " + f.getPath());
				continue;
			} else {
				System.out.println("Adding license to " + f.getPath());
			}

			in = new BufferedReader(new FileReader(f));
			out = new PrintWriter(new FileWriter(to));
			
			out.println(commentData[JAVA][PRE]);
			
			out.print(commentData[JAVA][LINE]);
			out.println(LICENSE_START);
			
			for (String s : licenseLines) {
				out.print(commentData[JAVA][LINE]);
				out.println(s);
			}

			out.print(commentData[JAVA][LINE]);
			out.println(LICENSE_END);			
			
			out.println(commentData[JAVA][POST]);
			
			while (null != (line = in.readLine())) {
				out.println(line);
			}
			
			out.close();
			in.close();

			f.delete();
			to.renameTo(f);
		}
	}
	
	static boolean alreadyLicensed(File f) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(f));
		boolean ret = false;
		
		try {
			String line = in.readLine();
			
			if (line.equals(commentData[JAVA][PRE])) {
				line = in.readLine();
				
				if (line.equals(commentData[JAVA][LINE] + LICENSE_START)) {
					ret = true;
				}
			}
		} finally {
			in.close();
		}
		
		return ret;
	}
}
