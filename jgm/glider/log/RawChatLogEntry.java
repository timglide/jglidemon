package jgm.glider.log;

import jgm.Locale;
import jgm.wow.*;

import java.util.regex.*;

public class RawChatLogEntry extends LogEntry {
	static String GOLD = null;
	static String SILVER = null;
	static String COPPER = null;
	
	static {
		localeChanged();
		
		Locale.addListener(new jgm.locale.LocaleListener() {
			public void localeChanged() {
				RawChatLogEntry.localeChanged();
			}
		});
	}
	
	public static void localeChanged() {
		Locale.setBase("regex");
		
		try {
			GOLD = Locale._("loot.gold");
			SILVER = Locale._("loot.silver");
			COPPER = Locale._("loot.copper");
			
			MONEY_PATTERN = Pattern.compile(Locale._("loot.money"));
			ITEM_PATTERN = Pattern.compile(Locale._("loot.item"));
		} catch (Throwable e) {
			e.printStackTrace();
			
			Locale.setLocale(Locale.LOCALE_ENGLISH);
		}
	}
	
	private String text;
	private ItemSet itemSet = null;
	private int    money = 0;

	public RawChatLogEntry(String s) {
		super("RawChat", s);

		text = s;

		removeFormatting();
		parseMoney();
		parseItem();
		
		/*if (money > 0) {
			int[] parts = jgm.gui.components.GoldPanel.cToGsc(money);
			System.out.printf("Received %dg %ds %dc\n", parts[0], parts[1], parts[2]);
		}*/
		
		/*if (itemSet != null) {
			System.out.print("Received item [" + itemSet.getItem().name + "]");
			
			int qty = itemSet.getQuantity(); 
			if (qty > 0) {
				System.out.print("x" + qty);
			}
			
			System.out.println();
		}*/
	}

	public String getText() {
		return text;
	}

	public boolean hasItemSet() {
		return itemSet != null;
	}
	
	public ItemSet getItemSet() {
		return itemSet;
	}

	public boolean hasMoney() {
		return money > 0;
	}
	
	public int getMoney() {
		return money;
	}
	
	private static Pattern MONEY_PATTERN = null;
	
//	static {
//		MONEY_PATTERN = Pattern.compile(
//			".*(?:Received|You\\s+loot)\\s+(?:(\\d+\\s+Gold),?)?\\s*(?:(\\d+\\s+Silver),?)?\\s*(?:(\\d+\\s+Copper))?\\.?"	
//		);
		
		/*
		 * group 1: gold
		 *       2: silver
		 *       3: copper
		 */
//	}
	
	private void parseMoney() {
		Matcher m = MONEY_PATTERN.matcher(text);
		
		if (!m.matches()) return;
		
		int gold = 0, silver = 0, copper = 0;
		
		for (int i = 1; i <= 3; i++) {
			String s = m.group(i);
			if (s == null) continue;
			
			String[] parts = s.split(" ", 2);
			if (parts.length != 2) continue;
				
			int num = 0;
				
			try {
				num = Integer.parseInt(parts[0]);
			} catch (NumberFormatException e) {}
				
			if (parts[1].equals(GOLD)) {
				gold = num;
			} else if (parts[1].equals(SILVER)) {
				silver = num;
			} else {
				copper = num;
			}
		}
		
		money =
			jgm.gui.components.GoldPanel.gscToC(gold, silver, copper);
	}
	
	private static Pattern ITEM_PATTERN = null;

	/* From http://www.wowwiki.com/ItemString
	 * item:itemId:enchantId:jewelId1:jewelId2:jewelId3:jewelId4:suffixId:uniqueId
	 */
//	static {
//		ITEM_PATTERN = Pattern.compile(
//			".*You\\s+(?:receive\\s+(?:loot|item)|create):\\s+\\|Hitem:(\\d+)(?::-?\\d+)*?\\|h\\[(.*?)\\]\\|h(?:x(\\d+))?\\.*"
//		);

		/* group 1: item id
		 *       2: item name
		 *       3: optional quantity
		 */
//	}

	private void parseItem() {
		// |Hitem:1487:0:0:0:0:0:0:799645190|h[Conjured Pumpernickel]|h|rx4

		Matcher m = ITEM_PATTERN.matcher(text);
//		System.out.println("Checking for item: " + text);

		if (!m.matches()) return;

//		System.out.println("Found item match in RawChatLog");

		int id = 0, qty = 1;
		String name = null;

		try {
			id   = Integer.parseInt(m.group(1));
			name = m.group(2);
			qty  = Integer.parseInt(m.group(3));
		} catch (NumberFormatException e) {}
		  catch (NullPointerException e)  {}

		itemSet = ItemSet.factory(id, name, qty);
	}

	private static final String FORMATTING_REGEX = "\\|(?:c[0-9A-Fa-f]{8}|r)";

	private void removeFormatting() {
		 // |cFFFFFFFFWhite
		text = text.replaceAll(FORMATTING_REGEX, "");
	}
}
