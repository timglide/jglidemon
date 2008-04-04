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
package jgm.gui;

import java.io.*;
import javax.swing.*;
 
public class Console implements Runnable {
	public static void redirect(BufferedReader what, JTextArea where) {
		Console c = new Console(where, what);
		new Thread(c).start();
	}

	JTextArea displayPane;
	BufferedReader reader;
 
	/*private Console(JTextArea displayPane, PipedOutputStream pos) {
		this.displayPane = displayPane;
 
		try {
			PipedInputStream pis = new PipedInputStream(pos);
			reader = new BufferedReader(new InputStreamReader(pis));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}*/

	private Console(JTextArea displayPane, BufferedReader r) {
		this.displayPane = displayPane;
		reader = r;
	}

	public void run() {
		displayPane.setText("");
		displayPane.setEditable(false);
		String line = null;
 
		try {
			while ((line = reader.readLine()) != null) {
				displayPane.replaceSelection(line + "\n" );
				displayPane.setCaretPosition(displayPane.getDocument().getLength());
			}
 
			System.err.println("got out of while loop in Console");
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, "Error redirecting output: " + ioe.getMessage());
		}
	}
}
