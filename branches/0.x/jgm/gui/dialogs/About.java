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
package jgm.gui.dialogs;

import jgm.JGlideMon;
import jgm.gui.components.JLinkButton;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class About extends Dialog implements ActionListener {
	private JLabel iconLabel;
	private JLabel text;
	
	private JLinkButton gplLink;
	private JLinkButton freeTtsLink;
	
	private JButton close;
	
	private static java.util.Random r = new java.util.Random();
	
	public About(Frame owner) {
		super(owner, "About JGlideMon");
		
		ImageIcon icon = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/stitch" + r.nextInt(2) + ".jpg"));
		iconLabel = new JLabel(icon);
		
		this.setLayout(new BorderLayout(PADDING, PADDING));
		
		add(iconLabel, BorderLayout.WEST);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		
		text = new JLabel(
			"<html>JGlideMon " + JGlideMon.version + "<br>" +
			JGlideMon.revision + "<br>" +
			JGlideMon.date + "<br>" +
			"By Tim<br><br>Released under the GNU GPL"
		);
		textPanel.add(text);
		
		try {
			gplLink =
				new JLinkButton("More Info",
					new java.net.URL("http://www.gnu.org/licenses/gpl.html"));
			gplLink.setBorder(BorderFactory.createEmptyBorder());
			textPanel.add(gplLink);
		} catch (java.net.MalformedURLException e) {}
		
		// only if there is tts support
		if (jgm.util.Speech.isSupported()) {
			try {
				freeTtsLink =
					new JLinkButton("FreeTTS",
						new java.net.URL("http://freetts.sourceforge.net"));
				//	new JLinkButton("http://freetts.sourceforge.net");
				freeTtsLink.setBorder(BorderFactory.createEmptyBorder());
			} catch (java.net.MalformedURLException e) {}
			
			if (freeTtsLink != null) {
				textPanel.add(new JLabel("<html><br></html>"));
				textPanel.add(new JLabel("Text-to-speech provided by"));
				textPanel.add(freeTtsLink);
			}
		}
		
		add(textPanel, BorderLayout.CENTER);
		
		close = new JButton("Close");
		close.addActionListener(this);
		add(Dialog.makeNiceButtons(close), BorderLayout.SOUTH);
		
		makeVisible();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			setVisible(false);
		}
	}

	protected void onShow() {
		ImageIcon icon = new ImageIcon(
			JGlideMon.class.getResource("resources/images/stitch/stitch" + r.nextInt(2) + ".jpg"));
		iconLabel.setIcon(icon);
		iconLabel.revalidate();
	}
}
