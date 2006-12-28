package jgm.gui.updaters;

import jgm.glider.GliderConn;
import jgm.glider.log.*;
import jgm.gui.panes.TabsPane;
import jgm.gui.tabs.*;
 
public class LogUpdater extends Thread {
	private boolean stop = false;

	private GliderConn conn;

	private LogTab statusLog;
	private LogTab rawLog;
	private LogTab gliderLog;
	private LogTab rawChatLog;
	private LogTab chatLog;
	private LogTab urgentChatLog;
	private LogTab combatLog;
	private LootsTab lootsTab;
 
	public LogUpdater(TabsPane t) {
		super("LogUpdater");

		statusLog  = t.statusLog;
		rawLog     = t.rawLog;
		gliderLog  = t.gliderLog;
		rawChatLog = t.rawChatLog;
		chatLog    = t.chatLog;
		urgentChatLog = t.urgentChatLog;
		combatLog  = t.combatLog;
		lootsTab   = t.lootsTab;
 
		start();
	}

	public void close() {
		stop = true;
		this.interrupt();
		conn.close();
	}

	public void run() {
		conn = new GliderConn();

	  synchronized (conn) {
		
		while (true) {
			while (!conn.isConnected()) {
				try {
					conn.wait();
				} catch (InterruptedException e) {
					System.out.println("LogUpdater interrupted");
					interrupted();
				}
			}
			
			if (stop) return;
		
			conn.send("/log all");
			conn.readLine(); // Log mode added: all
			conn.readLine(); // ---

			String   line = null;
			LogEntry e    = null;

			while (null != (line = conn.readLine())) {
				if (stop) return;

				e = LogEntry.factory(line);

				if (e == null) continue;

				rawLog.add(e);

				if (e instanceof GliderLogEntry) {
					gliderLog.add(e);
				} else if (e instanceof StatusEntry) {
					statusLog.add(e);
				} else if (e instanceof ChatLogEntry) {
					ChatLogEntry e2 = (ChatLogEntry) e;
				
					if (e2 instanceof WhisperEntry) {
						urgentChatLog.add(e, true);
					}
				
					chatLog.add(e);
				} else if (e instanceof RawChatLogEntry) {
					rawChatLog.add(e);

					RawChatLogEntry e2 = (RawChatLogEntry) e;

					if (e2.hasItem())  {
//						System.out.println("Adding item: " + e2.getItem().toString());
						lootsTab.add(e2.getItem());
					}
				
					if (e2.hasMoney()) {
						lootsTab.addMoney(e2.getMoney());
					}
				} else if (e instanceof CombatLogEntry) {
					combatLog.add(e);
				}
			}
 
			System.err.println("Conn must have disconnected, reconnecting...");
		}
	  }
	}
}
