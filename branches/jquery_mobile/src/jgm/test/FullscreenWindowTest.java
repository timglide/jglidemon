package jgm.test;

import jgm.gui.FullscreenWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FullscreenWindowTest {
	public static void main(String[] args) {
		new FullscreenWindowTest();
	}
	
	FullscreenWindow window;
	
	public FullscreenWindowTest() {		
		window = new FullscreenWindow(Color.BLACK);
		
		JButton btn = new JButton("Close");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.goNormal();
				System.exit(0);
			}
		});
		
		window.setLayout(new FlowLayout());
		window.add(jgm.gui.dialogs.Dialog.makeNiceButtons(btn));
		
		window.setVisible(true);
		window.goFullscreen();
	}
}
