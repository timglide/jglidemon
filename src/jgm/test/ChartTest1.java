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
package jgm.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import jgm.gui.components.PlayerChart;

public class ChartTest1 extends JFrame implements ActionListener {
	/** A menu item for closing the dialog */
	JMenuItem m_exit = null;
	
	/** Adds more data to the plot */
	JButton b_add = null;
	
	/** Removes data from the plot */
	JButton b_remove = null;
	
	/** The panel containing our chart */
	JPanel chartPanel = null;
	PlayerChart chart = null;
	
	/** Initializes the dialog and its chart in a ChartPanel dialog */
	public ChartTest1() {
		super();
		
		// Initialize GUI components
		this.setTitle("PlayerChart Test");
		
		// Make the dialog a reasonable size
		this.setSize(800, 600);
		
		// Allow resize!  It works seamlessly in Opechart2
		this.setResizable(true);
		
		// The content pane
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		this.setContentPane(contentPane);
		
		// Set up a menu on this dialog, just to show that everything plays
		// well together.
		JMenuBar menu_bar = new JMenuBar();
		JMenu file_menu = new JMenu();
		file_menu.setText("File");
		m_exit = new JMenuItem();
		m_exit.setText("Exit");
		file_menu.add(m_exit);
		menu_bar.add(file_menu);
		this.setJMenuBar(menu_bar);
		
		// Make sure that on window close, we dispose of this dialog
		this.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		
		// Set up this class as the action listener for our one menu item
		m_exit.addActionListener(this);
		
		
		
		chart = new PlayerChart(500);
		chart.startDummyData(10);
		
		
		contentPane.add(chart, BorderLayout.CENTER);
	}
		
	/** Listener for our single menu item and our two buttons.  The proper action
	 * is taken based on which component caused the event to occur.
	 */
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if(e.getSource() == m_exit) {
			System.exit(0);
		}
	}
	

	/** Main routine to start the demonstration dialog
	 * 
	 * @param args unnecessary command line arguments
	 */
	public static void main(String[] args) {
		
		// Make this application appear native
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { 
			System.out.println("Change of LNF failed...");
		}
		
		// Construct our bar chart dialog
		ChartTest1 temp = new ChartTest1();
		
		// Make the dialog visible
		temp.setVisible(true);
	}

}
