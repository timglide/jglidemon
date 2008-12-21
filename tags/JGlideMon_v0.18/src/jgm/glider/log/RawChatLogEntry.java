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
package jgm.glider.log;

import jgm.wow.*;

import java.util.regex.*;

public class RawChatLogEntry extends LogEntry {	
	private ItemSet itemSet = null;
	private int    money = 0;

	private String repFaction = null;
	private int repAmount = 0;
	
	private String skillName = null;
	private int skillLevel = 0;
	
	public RawChatLogEntry(String s) {
		super("RawChat", s);

		removeFormatting();
		parseMoney();
		parseItem();
		parseRep();
		parseSkill();
		
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

	@Override
	public String getText() {
		return text;
	}

	@Override
	public boolean supportsHtmlText() {
		return true;
	}
	
	@Override
	public String getHtmlText() {
		String preColor =
			this.hasItemSet()
			? COLOR_MAP.get("loot")
			: this.hasRep()
			  ? COLOR_MAP.get("rep")
			  : this.hasSkill()
			    ? COLOR_MAP.get("skill")
			    : null;
			    
		return super.getHtmlText(preColor);
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
	
	public boolean hasRep() {
		return repFaction != null;
	}
	
	public String getRepFaction() {
		return repFaction;
	}
	
	public int getRepAmount() {
		return repAmount;
	}
	
	public boolean hasSkill() {
		return skillName != null;
	}
	
	public String getSkillName() {
		return skillName;
	}
	
	public int getSkillLevel() {
		return skillLevel;
	}
	
	private static Pattern MONEY_PATTERN =
		Pattern.compile(
			".*(?:Received|You\\s+loot)\\s+(?:(\\d+\\s+Gold),?)?\\s*(?:(\\d+\\s+Silver),?)?\\s*(?:(\\d+\\s+Copper))?\\.?"	
		);
		
		/*
		 * group 1: gold
		 *       2: silver
		 *       3: copper
		 */
	
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
				
			if (parts[1].equals("Gold")) {
				gold = num;
			} else if (parts[1].equals("Silver")) {
				silver = num;
			} else {
				copper = num;
			}
		}
		
		money =
			jgm.gui.components.GoldPanel.gscToC(gold, silver, copper);
	}

	/* From http://www.wowwiki.com/ItemString
	 * item:itemId:enchantId:jewelId1:jewelId2:jewelId3:jewelId4:suffixId:uniqueId
	 */
	private static Pattern ITEM_PATTERN =
		Pattern.compile(
			".*You\\s+(?:receive\\s+(?:loot|item)|create):\\s+\\|Hitem:(\\d+)(?::-?\\d+)*?\\|h\\[(.*?)\\]\\|h(?:x(\\d+))?\\.*"
	);

		/* group 1: item id
		 *       2: item name
		 *       3: optional quantity
		 */

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

	// "Your %s reputation has increased by %d."
	private static final Pattern REP_REGEX =
		Pattern.compile("Reputation with (.+) (increased|decreased) by (\\d+)\\.");
	
	private void parseRep() {
		Matcher m = REP_REGEX.matcher(text);
		if (!m.matches()) return;
		
		repFaction = m.group(1);
		
		try {
			repAmount = Integer.parseInt(m.group(3));
		} catch (NumberFormatException e) {}
		
		if (m.group(2).equals("decreased")) {
			repAmount = -repAmount;
		}
	}
	
	private static final Pattern SKILL_REGEX =
		Pattern.compile("Your skill in (.+) has increased to (\\d+)\\.");
	
	private void parseSkill() {
		Matcher m = SKILL_REGEX.matcher(text);
		if (!m.matches()) return;
		
		skillName = m.group(1);
		
		try {
			skillLevel = Integer.parseInt(m.group(2));
		} catch (NumberFormatException e) {}
	}
}
