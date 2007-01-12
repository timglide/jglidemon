package jgm.gui;

import jgm.JGlideMon;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import com.zfqjava.swing.JLinkButton;

public class AboutFrame extends JFrame {
	private JLabel iconLabel;
	private JLabel text;
	
	private JLinkButton freeTtsLink;
	
	public AboutFrame() {
		super("About JGlideMon");
		
		setSize(350, 250);
		setResizable(false);
		setLocationRelativeTo(null); // center
		setLayout(new BorderLayout(20, 20));
		ImageIcon icon = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/stitch0.jpg"));
		iconLabel = new JLabel(icon);
		
		add(iconLabel, BorderLayout.WEST);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		
		text = new JLabel(
			"<html><br><br>JGlideMon " + JGlideMon.version + "<br>" +
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
		
		add(textPanel, BorderLayout.CENTER);
		
		addWindowListener(
			new WindowAdapter() {
				public void windowOpened(WindowEvent e) {
					java.util.Random r = new java.util.Random();
					iconLabel.setIcon(
						new ImageIcon(
							JGlideMon.class.getResource("resources/images/stitch/stitch" + r.nextInt(2) + ".jpg")));
				}
				
				public void windowClosing(WindowEvent e) {
					AboutFrame.this.setVisible(false);
				} // end WindowClosing
			}
		);
	}
}
