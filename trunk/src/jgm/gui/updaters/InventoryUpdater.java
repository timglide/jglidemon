package jgm.gui.updaters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jgm.ServerManager;
import jgm.glider.Command;
import jgm.glider.Conn;
import jgm.glider.ConnectionListener;
import jgm.glider.log.RawChatLogEntry;
import jgm.gui.tabs.LootTab;
import jgm.wow.Bag;
import jgm.wow.Inventory;
import jgm.wow.Item;
import jgm.wow.ItemSet;
import jgm.wow.PaperDoll;

public class InventoryUpdater implements ConnectionListener {
	static final Logger log = Logger.getLogger(InventoryUpdater.class.getName());
	
	private LootTab tab;
	private ServerManager sm;
	private Conn conn;
	private volatile boolean isUpdating = false;
	
	public InventoryUpdater(ServerManager sm, LootTab tab) {
		this.sm = sm;
		this.tab = tab;
		conn = new Conn(sm, "InventoryUpdater");
	}

	@Override
	public Conn getConn() {
		return conn;
	}

	@Override
	public void onConnecting() {
		
	}

	@Override
	public void onConnect() {
		tab.inventoryLootsTab.setEnabled(true);
		tab.inventoryTab.setEnabled(true);
	}

	@Override
	public void onDisconnecting() {
		tab.inventoryLootsTab.setEnabled(false);
		tab.inventoryTab.setEnabled(false);
	}

	@Override
	public void onDisconnect() {
		
	}

	@Override
	public void onDestroy() {
		if (conn != null) conn.close();
	}
	
	public void updateAsync() {
		if (isUpdating || null == conn || !conn.isConnected())
			return;
		
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				update();
				return null;
			}
		}.execute();
	}
	
	public synchronized void update() throws IOException {
		if (isUpdating || null == conn || !conn.isConnected())
			return;
		
		isUpdating = true;
		
		final Inventory i = new Inventory();
		Bag b;
		PaperDoll d = new PaperDoll();
		i.equipped = d;
		String line = null;
		Map<String, String> m = new HashMap<String, String>();
		
		synchronized(conn) {
			Command.getInventoryCommand("bags").send(conn);
			
			while ((line = conn.readLine()) != null) {
				if (line.equals("---")) break;

				String[] parts = line.split(":", 2);

				if (parts.length != 2) continue;

				m.put(parts[0], parts[1].trim());
			}
		}
		
		b = new Bag();
		b.items = new ItemSet[16];
		parseBag(m, 0, b.items);
		i.backpack = b;
		
		b = new Bag();
		parseBag(m, 1, b);
		i.bag1 = b;
		
		b = new Bag();
		parseBag(m, 2, b);
		i.bag2 = b;
		
		b = new Bag();
		parseBag(m, 3, b);
		i.bag3 = b;
		
		b = new Bag();
		parseBag(m, 4, b);
		i.bag4 = b;
		
		parseEquipped(m, d);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tab.update(i);
			}
		});
		
		isUpdating = false;
	}
	
	private Item parseItem(Map<String, String> m, String key) {
		ItemSet is = parseItemSet(m, key);
		return null != is ? is.getItem() : null;
	}
	
	private ItemSet parseItemSet(Map<String, String> m, String key) {
		String line = m.get(key);
		if (null == line) return null;

		RawChatLogEntry logEntry = new RawChatLogEntry("You create: " + line);
		return logEntry.getItemSet();
	}
	
	private void parseBag(Map<String, String> m, int bagIndex, Bag b) {
		String line = m.get(String.valueOf(bagIndex));
		if (null == line) return;
		
		String[] parts = line.split(":", 2);
		
		if (2 != parts.length) return;
		
		line = parts[1].trim();
		int slots;
		
		try {
			slots = Integer.parseInt(parts[0]);
		} catch (NumberFormatException nfe) {
			return;
		}
		
		RawChatLogEntry logEntry = new RawChatLogEntry("You create: " + line);
		b.bagItem = logEntry.getItem();
		
		b.items = new ItemSet[slots];
		parseBag(m, bagIndex, b.items);
		
		b.index = bagIndex;
	}
	
	private void parseBag(Map<String, String> m, int bagIndex, ItemSet[] items) {
		for (int ix = 0; ix < items.length; ix++) {
			items[ix] = parseItemSet(m, bagIndex + "|" + ix);
		}
	}
	
	private void parseEquipped(Map<String, String> m, PaperDoll d) {
		d.head = parseItem(m, "Head");
		d.neck = parseItem(m, "Neck");
		d.shoulder = parseItem(m, "Shoulder");
		d.back = parseItem(m, "Back");
		d.chest = parseItem(m, "Chest");
		d.shirt = parseItem(m, "Shirt");
		d.tabard = parseItem(m, "Tabard");
		d.wrist = parseItem(m, "Wrist");
		
		d.hands = parseItem(m, "Hands");
		d.waist = parseItem(m, "Waist");
		d.legs = parseItem(m, "Legs");
		d.feet = parseItem(m, "Feet");
		d.finger1 = parseItem(m, "Finger1");
		d.finger2 = parseItem(m, "Finger2");
		d.trinket1 = parseItem(m, "Trinket1");
		d.trinket2 = parseItem(m, "Trinket2");
		
		d.mainHand = parseItem(m, "MainHand");
		d.offHand = parseItem(m, "OffHand");
	}
}
