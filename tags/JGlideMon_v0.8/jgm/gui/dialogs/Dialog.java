package jgm.gui.dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JDialog;

public abstract class Dialog extends JDialog {
	protected String title;
	protected GridBagConstraints c = new GridBagConstraints();
	
	public Dialog(Frame owner, String title) {
		super(owner, title, true);
		this.title = title;
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());
		
		this.addWindowListener(new WindowAdapter() {			
			public void windowActivated(WindowEvent e) {
				onShow();
			}
		});
	}
	
	protected final void makeVisible() {
		validate();
		pack();
		setLocationRelativeTo(null); // center
	}

	protected void onShow() {
		if (jgm.JGlideMon.debug)
			System.out.println("Showing dialog " + getClass().getName());
	}
}
