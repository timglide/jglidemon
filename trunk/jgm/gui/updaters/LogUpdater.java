package jgm.gui.updaters;

import jgm.glider.*;
import jgm.glider.log.*;
import jgm.gui.panes.TabsPane;
import jgm.gui.tabs.*;

import java.util.logging.*;
import java.io.*;

public class LogUpdater implements Runnable, ConnectionListener {
	static Logger log = Logger.getLogger(LogUpdater.class.getName());

	private volatile boolean stop = false;

	private Conn conn;

	private LogTab statusLog;
	private LogTab rawLog;
	private LogTab gliderLog;
	private LogTab rawChatLog;
	private ChatTab chatLog;
	private LogTab urgentChatLog;
	private LogTab combatLog;
	private LootsTab lootsTab;
 
	private Thread thread;
	
	public LogUpdater(TabsPane t) {
		statusLog  = t.statusLog;
		rawLog     = t.rawLog;
		gliderLog  = t.gliderLog;
		rawChatLog = t.rawChatLog;
		chatLog    = t.chatLog;
		urgentChatLog = t.urgentChatLog;
		combatLog  = t.combatLog;
		lootsTab   = t.lootsTab;
		
		conn = new Conn();
 	}

	public void close() {
		stop = true;
		if (thread != null) thread.interrupt();
		if (conn != null) conn.close();
	}

	public Conn getConn() {
		return conn;
	}
	
	public void connecting() {}
	
	public void connectionEstablished() {
		stop = false;
		thread = new Thread(this, "LogUpdater");
		thread.start();
	}
	
	public void disconnecting() {}
	
	public void connectionDied() {
		stop = true;
	}
	
	public void run() {
		try {
			conn.send("/log all");
			conn.readLine(); // Log mode added: all
			conn.readLine(); // ---
		} catch (IOException e) {
			log.fine("Stopping LogUpdater, IOE: " + e.getMessage());
			Connector.disconnect();
			return; // connection died
		}

		String   line = null;
		LogEntry e    = null;

		while (true) {
			if (stop) return;
			
			try {
				line = conn.readLine();
			} catch (Exception x) {
				log.fine("Stopping LogUpdater, Ex: " + x.getMessage());
				Connector.disconnect();
				return;
			}
			
			if (null == line || stop) return;

			e = LogEntry.factory(line);

			if (e == null) continue;

			if (jgm.JGlideMon.debug && rawLog != null) {
				rawLog.add(e);
			}

			if (e instanceof GliderLogEntry) {
				gliderLog.add(e);
				
				if (((GliderLogEntry) e).isAlert()) {
					urgentChatLog.add(e, true);
				}
			} else if (e instanceof StatusEntry) {
				statusLog.add(e);
			} else if (e instanceof ChatLogEntry) {
				ChatLogEntry e2 = (ChatLogEntry) e;
				
				if (e2.isUrgent()) {
					urgentChatLog.add(e, true);
				}
				
				chatLog.add(e2);
			} else if (e instanceof RawChatLogEntry) {
				if (jgm.JGlideMon.debug && rawChatLog != null) {
					rawChatLog.add(e);
				}

				RawChatLogEntry e2 = (RawChatLogEntry) e;

				if (e2.hasItemSet())  {
//					System.out.println("Adding item: " + e2.getItem().toString());
					lootsTab.add(e2.getItemSet());
				}
				
				if (e2.hasMoney()) {
					lootsTab.addMoney(e2.getMoney());
				}
			} else if (e instanceof CombatLogEntry) {
				combatLog.add(e);
			}
		}
	}
}
