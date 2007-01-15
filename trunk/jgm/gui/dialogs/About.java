package jgm.gui.dialogs;

import jgm.JGlideMon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.zfqjava.swing.JLinkButton;

public class About extends Dialog implements ActionListener {
	private JLabel iconLabel;
	private JLabel text;
	
	private JLinkButton freeTtsLink;
	
	private JButton close;
	
	public About(Frame owner) {
		super(owner, "About JGlideMon");
		
		java.util.Random r = new java.util.Random();
		ImageIcon icon = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/stitch" + r.nextInt(2) + ".jpg"));
		iconLabel = new JLabel(icon);
		
		add(iconLabel, BorderLayout.WEST);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		
		text = new JLabel(
			"<html><br>JGlideMon " + JGlideMon.version + "<br>" +
			"By Tim<br><br>" +
			"</html>"
		);
		textPanel.add(text);
		
		textPanel.add(new JLabel("Text-to-speech provided by"));
		
		try {
			freeTtsLink =
				new JLinkButton("FreeTTS",
						new java.net.URL("http://freetts.sourceforge.net"));
			freeTtsLink.setBorder(BorderFactory.createEmptyBorder());
		} catch (java.net.MalformedURLException e) {}
		
		if (freeTtsLink != null)
			textPanel.add(freeTtsLink);
		
		textPanel.add(new JLabel("<html><br></html>"));
		
		add(textPanel, BorderLayout.CENTER);
		
		close = new JButton("Close");
		close.addActionListener(this);
		add(close, BorderLayout.SOUTH);
		
		makeVisible();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			dispose();
		}
	}
}
