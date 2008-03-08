package jgm.gui;

import jgm.gui.dialogs.Dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FullscreenSSWindow extends FullscreenWindow {
	final GUI gui;
	public JPanel ssPanel = null;
	
	public FullscreenSSWindow(GUI gui) {
		super(Color.BLACK);
		this.gui = gui;

		setLayout(new BorderLayout());
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FullscreenSSWindow.this.gui.menu.normalSS.doClick();
			}
		});
		
		JButton die = new JButton("Die");
		die.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		add(Dialog.makeNiceButtons(close, die), BorderLayout.SOUTH);
	}
	
	int lastGuiState = 0;
	
	public void doFullscreenSS(boolean state) {
		if (state) {
			gui.tabsPane.screenshotTab.select();
			ssPanel = gui.tabsPane.screenshotTab.removeContent();
			ssPanel.setOpaque(false);
			add(ssPanel, BorderLayout.CENTER);
			lastGuiState = gui.frame.getExtendedState();
//			gui.frame.setExtendedState(lastGuiState | JFrame.ICONIFIED);
			gui.frame.setIgnoreRepaint(true);
			setVisible(true);
			goFullscreen();
		} else {
			remove(ssPanel);
			gui.tabsPane.screenshotTab.restoreContent();
			goNormal();
//			gui.frame.setExtendedState(lastGuiState & ~JFrame.ICONIFIED);
			gui.frame.setIgnoreRepaint(false);
			setVisible(false);
		}
		
		gui.sm.ssUpdater.redoScale = true;
		
		validate();
		repaint();
	}
}
