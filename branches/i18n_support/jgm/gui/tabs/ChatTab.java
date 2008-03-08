package jgm.gui.tabs;

import jgm.Locale;
import jgm.glider.log.*;

import java.awt.*;
import javax.swing.*;

public class ChatTab extends Tab implements jgm.locale.LocaleListener {
	private JTabbedPane tabs;
	public LogTab all;
	public LogTab whisper;
	public LogTab guild;
	
	public ChatTab() {
		super(new BorderLayout(), "Chat");
		
		tabs = new JTabbedPane();
		all = new LogTab("All Chat", tabs);
		whisper = new LogTab("Whisper/Say", tabs);
		guild = new LogTab("Guild", tabs);
		
		addTab(all);
		addTab(whisper);
		addTab(guild);
		
		add(tabs, BorderLayout.CENTER);
	}
	
	private void addTab(Tab t) {
		tabs.addTab(t.name, t);
	}
	
	public void add(ChatLogEntry e) {
		all.add(e);
		
		String channel = e.getChannel();
		if (channel == null) return;
		
		if (channel.equals("Whisper") || channel.equals("Say")) {
			whisper.add(e);
		} else if (channel.equals("Guild") || channel.equals("Officer")) {
			guild.add(e);
		}
	}
	
	public void localeChanged() {
		Locale.setBase("MainWindow");
		
		localeChanged("tab.chat");
		all.localeChanged("tab.chat.all");
		whisper.localeChanged("tab.chat.whispersay");
		guild.localeChanged("tab.chat.guild");
	}
}
