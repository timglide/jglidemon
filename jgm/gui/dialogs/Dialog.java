package jgm.gui.dialogs;

import java.awt.*;
import javax.swing.JDialog;

public abstract class Dialog extends JDialog {
	protected String title;
	protected GridBagConstraints c = new GridBagConstraints();
	
	public Dialog(Frame owner, String title) {
		super(owner, title, true);
		this.title = title;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());
	}
	
	protected final void makeVisible() {
		validate();
		pack();
		setLocationRelativeTo(null); // center
		setVisible(true);
	}
}
