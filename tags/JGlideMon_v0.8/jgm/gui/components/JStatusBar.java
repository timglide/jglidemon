package jgm.gui.components;

import java.awt.*;
import javax.swing.*;

public class JStatusBar extends jgm.gui.panes.Pane {
	private JLabel text = new JLabel();
	private JProgressBar progress = new JProgressBar();
	//private JLabel extra = new JLabel("        ");
	
	public JStatusBar() {
		Insets insets = new Insets(0, 2, 0, 2);
		javax.swing.border.Border b =
			BorderFactory.createLoweredBevelBorder();
		
		c.insets = insets; c.weightx = 1.0; c.weighty = 0.0;
		text.setBorder(b);
		add(text, c);
		
		c.gridx++; c.weightx = 0.001;
		progress.setVisible(false);
		progress.setBorder(b);
		progress.setBorderPainted(true);
		add(progress, c);
		
		//c.gridx++; c.weightx = 0.0;
		//extra.setBorder(b);
		//add(extra, c);
	}
	
	public void setText(String str) {
		text.setText("  " + str);
	}
	
	public JProgressBar getProgressBar() {
		return progress;
	}
	
	//public void setExtra(String str) {
	//	extra.setText("    " + str + "    ");
	//}
}
