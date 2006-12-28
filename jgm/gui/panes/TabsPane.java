package jgm.gui.panes;

import jgm.gui.tabs.*;
import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TabsPane extends Pane implements KeyListener {
	private JTabbedPane  tabbedPane;

	public ScreenshotTab screenshotTab;
	public LogTab        statusLog;
	public LootsTab      lootsTab;
	public LogTab        chatLog;
	public LogTab        urgentChatLog;
	public SendKeysTab   sendKeys;
	public LogTab        rawChatLog;
	public LogTab        combatLog;
	public LogTab        gliderLog;
	public LogTab        rawLog;
	public ConfigTab     config;

	public TabsPane() {
		super(new BorderLayout());

		tabbedPane = new JTabbedPane();

		screenshotTab = new ScreenshotTab();
		tabbedPane.addChangeListener(screenshotTab);
		tabbedPane.addKeyListener(this);
		addTab(screenshotTab);

		statusLog  = new LogTab("Status", tabbedPane);
		chatLog    = new LogTab("Chat Log", tabbedPane);
		urgentChatLog = new LogTab("Urgent Chat", tabbedPane);
		sendKeys   = new SendKeysTab();
		rawChatLog = new LogTab("Raw Chat Log", tabbedPane);
		combatLog  = new LogTab("Combat Log", tabbedPane);
		gliderLog  = new LogTab("Glider Log", tabbedPane);
		rawLog     = new LogTab("Raw Log", tabbedPane);

		addTab(statusLog);
		addTab(chatLog); addTab(urgentChatLog);
		addTab(sendKeys);
		addTab(rawChatLog); addTab(combatLog);
		addTab(gliderLog); addTab(rawLog);

		lootsTab = new LootsTab();
		addTab(lootsTab);

		config = new ConfigTab();
		addTab(config);
		
		add(tabbedPane, BorderLayout.CENTER);
		
		urgentChatLog.add(new jgm.glider.log.WhisperEntry("raw", "test", "xersees", 1, "Whisper"));
	}

	private void addTab(Tab t) {
		tabbedPane.addTab(t.name, t);
	}

	public void update(StatusUpdater s) {

	}
	
	public void keyPressed(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {
		switch (tabbedPane.getSelectedIndex()) {
			case 0:
				screenshotTab.keyReleased(e);
				break;
		}
	}
	
	public void keyTyped(KeyEvent e) {}
}
