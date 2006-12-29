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
