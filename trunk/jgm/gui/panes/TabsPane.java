package jgm.gui.panes;

import jgm.gui.dialogs.Config;
import jgm.gui.tabs.*;
import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

public class TabsPane extends Pane {
	public JTabbedPane  tabbedPane;

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

	public TabsPane() {
		super(new BorderLayout());

		tabbedPane = new JTabbedPane();

		screenshotTab = new ScreenshotTab();
		tabbedPane.addChangeListener(screenshotTab);
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

		add(tabbedPane, BorderLayout.CENTER);
	}

	private void addTab(Tab t) {
		tabbedPane.addTab(t.name, t);
	}

	public void update(StatusUpdater s) {

	}
}
