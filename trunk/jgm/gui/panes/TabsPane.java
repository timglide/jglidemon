package jgm.gui.panes;

import jgm.Locale;
import jgm.gui.tabs.*;
import jgm.gui.updaters.StatusUpdater;

import java.awt.*;
import javax.swing.*;

public class TabsPane extends Pane implements jgm.locale.LocaleListener {
	public JTabbedPane  tabbedPane;

	public ScreenshotTab screenshotTab;
	public LogTab        statusLog;
	public LootsTab      lootsTab;
	
	public ChatTab       chatLog;
	
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
		tabbedPane.addKeyListener(screenshotTab);
		addTab(screenshotTab);
		
		statusLog  = new LogTab("Status", tabbedPane);
		chatLog    = new ChatTab();
		urgentChatLog = new LogTab("Urgent Log", tabbedPane);
		sendKeys   = new SendKeysTab();
		combatLog  = new LogTab("Combat Log", tabbedPane);
		gliderLog  = new LogTab("Glider Log", tabbedPane);
		
		if (jgm.JGlideMon.debug) {
			rawChatLog = new LogTab("Raw Chat Log", tabbedPane);
			rawLog     = new LogTab("Raw Log", tabbedPane);
		}

		addTab(statusLog);
		addTab(chatLog); addTab(urgentChatLog);
		addTab(sendKeys);
		
		if (jgm.JGlideMon.debug) {
			addTab(rawChatLog);
		}
		
		addTab(combatLog);
		addTab(gliderLog); 
		
		if (jgm.JGlideMon.debug) {
			addTab(rawLog);
		}

		lootsTab = new LootsTab();
		addTab(lootsTab);

		add(tabbedPane, BorderLayout.CENTER);
		
		localeChanged();
		Locale.addListener(this);
	}

	private void addTab(Tab t) {
		tabbedPane.addTab(t.name, t);
	}

	public void localeChanged() {
		Locale.setBase("MainWindow");
		
		//screenshotTab.localeChanged();
		statusLog.localeChanged("tab.status");
		//lootsTab.localeChanged();
		
		chatLog.localeChanged();
		
		urgentChatLog.localeChanged("tab.urgentlog");
		//sendKeys.localeChanged();
		
		if (rawChatLog != null)
			rawChatLog.localeChanged("tab.rawchat");
		
		combatLog.localeChanged("tab.combatlog");
		gliderLog.localeChanged("tab.gliderlog");
		
		if (rawLog != null)
			rawLog.localeChanged("tab.rawlog");
		
		Locale.clearBase();
	}
	
	public void update(StatusUpdater s) {
		lootsTab.update(s);
	}
}
