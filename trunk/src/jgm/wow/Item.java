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
package jgm.wow;

import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import java.util.*;
import java.util.logging.*;
import java.io.*;

import jgm.JGlideMon;

/**
 * Represents an in-game item.
 * @author Tim
 * @since 0.1
 */
public class Item implements Comparable<Item>, Serializable {
	static Logger log = Logger.getLogger(Item.class.getName());
	
	public static final Font TITLE_FONT = new Font(null, Font.BOLD, 20);
	
	private transient static Map<Integer, Item> itemCache = new HashMap<Integer, Item>();
	private transient static Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
	
	public static final int POOR = 0;
	public static final int COMMON = 1;
	public static final int UNCOMMON = 2;
	public static final int RARE = 3;
	public static final int EPIC = 4;
	public static final int LEGENDARY = 5;
	public static final int RELIC = 6;
	
	public static final Color GOLD = new Color(0xffd200);
	
	public static final int BIND_NONE = 0;
	public static final int BIND_PICKUP = 1;
	public static final int BIND_EQUIP = 2;
	public static final int BIND_QUEST = 4;
	
	public static final String[] BINDS = {
		null, "Binds when picked up", "Binds when equipped",
		null, "Quest Item"
	};
	
	public static final int STAT_AGILITY = 3;
	public static final int STAT_STRENGTH = 4;
	public static final int STAT_INTELLECT = 5;
	public static final int STAT_SPIRIT = 6;
	public static final int STAT_STAMINA = 7;
	
	public static final String[] STATS = {
		null, null, "", "Agility", "Strength", 
		"Intellect", "Spirit", "Stamina"
	};
	
	public static final int SLOT_AMMO = 0;
	public static final int SLOT_HEAD = 1;
	public static final int SLOT_NECK = 2;
	public static final int SLOT_SHOULDER = 3;
	public static final int SLOT_SHIRT = 4;
	public static final int SLOT_CHEST = 5;
	public static final int SLOT_BELT = 6;
	public static final int SLOT_LEGS = 7;
	public static final int SLOT_FEET = 8;
	public static final int SLOT_WRIST = 9;
	public static final int SLOT_GLOVES = 10;
	public static final int SLOT_FINGER1 = 11;
	public static final int SLOT_FINGER2 = 12;
	public static final int SLOT_TRINKET1 = 13;
	public static final int SLOT_TRINKET2 = 14;
	public static final int SLOT_BACK = 15;
	public static final int SLOT_MAIN_HAND = 16;
	public static final int SLOT_OFF_HAND = 17;
	public static final int SLOT_RANGED = 18;
	public static final int SLOT_TABARD = 19;
	public static final int SLOT_CHEST_ROBE = 20;
	
	public static final String[] SLOTS = {
		"Ammo", "Head", "Neck", "Shoulder", "Shirt", "Chest", "Waist",
		"Legs", "Feet", "Wrist", "Hands", "Finger", "Finger", "Trinket", "Trinket",
		"Back", "Main Hand", "Off Hand", "Ranged", "Tabard",
		"Chest (Robe)"
	};
	
	public static final int CLASS_WEAPON = 2;
	
	public static final int SUBCLASS_1H_AXE = 0;
	public static final int SUBCLASS_2H_AXE = 1;
	public static final int SUBCLASS_BOW = 2;
	public static final int SUBCLASS_GUN = 3;
	public static final int SUBCLASS_1H_MACE = 4;
	public static final int SUBCLASS_2H_MACE = 5;
	public static final int SUBCLASS_POLEARM = 6;
	public static final int SUBCLASS_1H_SWORD = 7;
	public static final int SUBCLASS_2H_SWORD = 8;
	public static final int SUBCLASS_STAFF = 10;
	public static final int SUBCLASS_FIST = 13;
	public static final int SUBCLASS_DAGGER = 15;
	public static final int SUBCLASS_THROWN = 16;
	public static final int SUBCLASS_CROSSBOW = 18;
	public static final int SUBCLASS_WAND = 19;
	public static final int SUBCLASS_FISHING_POLE = 20;
	
	public static final int CLASS_ARMOR = 4;
	
	public static final int SUBCLASS_MISC = 0;
	public static final int SUBCLASS_CLOTH = 1;
	public static final int SUBCLASS_LEATHER = 2;
	public static final int SUBCLASS_MAIL = 3;
	public static final int SUBCLASS_PLATE = 4;
	public static final int SUBCLASS_SHIELD = 6;
	public static final int SUBCLASS_LIBRAM = 7;
	public static final int SUBCLASS_IDOL = 8;
	public static final int SUBCLASS_TOTEM = 9;
	
	public static final int CLASS_PROJECTILE = 6;

	public static final int SUBCLASS_ARROW = 2;
	public static final int SUBCLASS_BULLET = 3;
	
	public static final String[] CLASSES = {
		null, null, "Weapon", null, "Armor",
		null, "Ammo"
	};
	
	public static final String[][] SUBCLASSES = {
		null,
		null,
		{"One-Hand Axe", "Two-Hand Axe", "Bow", "Gun",
		 "One-Hand Mace", "Two-Hand Mace", "Polearm",
		 "One-Hand Sword", "Two-Hand Sword", null,
		 "Staff", null, null, "Fist Weapon", null,
		 "Dagger", "Thrown", null, "Crossbow", "Wand",
		 "Fishing Pole"
		},
		null,
		{"Miscellaneous", "Cloth", "Leather", "Mail", "Plate",
		 null, "Shield", "Libram", "Idol", "Totem"
		},
		null,
		{null, null, "Arrow", "Bullet"}
	};
	
	public static final String ICON_BASE
		= "http://wow.allakhazam.com";

	/* whether we were able to connect to allakhazam
	 * to retrieve the item info
	 */
	public boolean retrievedInfo = false;
	
	public int id = 0;
	public int quality = POOR;
	public Quality quality_ = Quality.POOR;
	public String name;
	
	public String description = null;
	public int clazz;
	public int subclass;
	public int armor;
	public int binds;
	public int stackSize = 1;
	public int unique = 0;
	public int itemLevel;
	public int requiredLevel;
	public int dmgHigh;
	public int dmgLow;
	public int speed; // in ms
	public int merchentBuyPrice;
	public int slot;
	
	public static final int MAX_STATS = 5;
	
	public int[] stats = new int[MAX_STATS];
	public int[] stat_values = new int[MAX_STATS];
	
	public static final int MAX_EFFECTS = 3;
	
	public Effect[] effects = new Effect[MAX_EFFECTS];
	
	public String iconPath = "/images/icons/INV_Misc_QuestionMark.png";
	private transient ImageIcon icon = null;

	private Item(int i, String s) {
		name = s;
		id = i;
	}

	public String getBindText() {
		try {
			return BINDS[binds];
		} catch (Exception e) {
			return "Unknown (" + binds + ")";
		}
	}
	
	public String getType1Text() {
		try {
			switch (clazz) {
				case CLASS_ARMOR:
					switch (subclass) {
						case SUBCLASS_SHIELD:
							return SLOTS[SLOT_OFF_HAND];
						
						case SUBCLASS_TOTEM:
						case SUBCLASS_IDOL:
						case SUBCLASS_LIBRAM:
							return "Relic";
							
						default:
							return SLOTS[slot];
					}
					
				case CLASS_WEAPON:
					switch (subclass) {
						case SUBCLASS_STAFF:
						case SUBCLASS_2H_SWORD:
						case SUBCLASS_2H_AXE:
						case SUBCLASS_2H_MACE:
						case SUBCLASS_POLEARM:
							return "Two-Hand";
							
						case SUBCLASS_GUN:
						case SUBCLASS_BOW:
						case SUBCLASS_CROSSBOW:
						case SUBCLASS_WAND:
							return "Ranged";
					}
					
					return "One-Hand";
					//return SLOTS[slot];
					
				case CLASS_PROJECTILE:
					return CLASSES[clazz];
					
				default:
					return null;
					//return "Unknown (" + clazz + ")";
			}					
		} catch (Exception e) {
			return null;
			//return "Unknown (" + clazz + "," + slot + ")";
		}
	}
	
	public String getType2Text() {
		try {
			return SUBCLASSES[clazz][subclass];
		} catch (Exception e) {
			return null;
			//return "Unknown (" + clazz + "," + subclass + ")";
		}
	}
	
	public String getStatText(int n) {
		try {
			if (stats[n] > 0) {
				return String.format("%+d %s", stat_values[n], STATS[stats[n]]);
			}
		} catch (Exception e) {
			return "Unkown (" + stats[n] + "," + stat_values[n] + ")";
		}
		
		return null;
	}
	
	public String getEffectText(int n) {
		try {
			return effects[n].toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Color getDarkColor() {
		return quality_.darkColor;
	}
	
	public Color getLightColor() {
		return quality_.lightColor;
	}

	public ImageIcon getIcon() {
		if (icon == null) {
			if (iconCache.containsKey(iconPath)) {
				icon = iconCache.get(iconPath);
				return icon;
			}
			
			try {
				icon = new javax.swing.ImageIcon(
					   new java.net.URL(ICON_BASE + iconPath));
				icon = jgm.util.Util.resizeIcon(icon, 32, 32);
				iconCache.put(iconPath, icon);
			} catch (java.net.MalformedURLException e) {
				// shouldn't get in here...
				System.err.println("Unable to make icon in Item: " + e.getMessage());
				icon = null;
			}
		}

		return icon;
	}
	
	public void setEffect(int i, Effect e) {
		effects[i] = e;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Item &&
			this.id == ((Item) o).id) {
			return true;
		}

		return false;
	}

	public String toString() {
		return id + ":" + name + ":" + quality + ":" + iconPath;
	}

	public int compareTo(Item i) {
		return name.compareTo(i.name);
	}

	/**
	 * Create a new item.
	 * @param id The item id
	 * @param name The item's name
	 * @return An item representing the supplied parameters
	 */
	public static Item factory(int id, String name) {
		Item ret = null;

		if (itemCache.containsKey(id)) {
			ret = itemCache.get(id);
			
			if (ret.retrievedInfo)
				return ret;

			itemCache.remove(id);
		}
		
		ret = new Item(id, name);

		if (!ItemFactory.factory(id, ret)) return null;

		Effect.factory(ret);
		
		itemCache.put(id, ret);
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	// unavoidable when reading a serialized
	// generic object
	
	public static class Cache {
		public static final String
			iconFileName = "icons.cache",
			itemFileName = "items.cache";
		public static final File
			iconFile = new File(JGlideMon.dataDir, iconFileName),
			itemFile = new File(JGlideMon.dataDir, itemFileName);
		
		public static void saveIcons() {
			try {
				ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(iconFile)
				);
				
				os.writeObject(iconCache);
				os.close();
				
				log.fine("Saving icon cache to " + iconFile.getName());
			} catch (IOException e) {
				log.log(Level.SEVERE, "Error saving icon cache", e);
			}
		}
		
		public static void loadIcons() {
			try {
				File f = new File(iconFileName);
				
				if (f.exists())
					f.renameTo(iconFile);
				
				ObjectInputStream is = new ObjectInputStream(
					new FileInputStream(iconFile)
				);
				
				Object o = is.readObject();
				
				if (o instanceof HashMap) {
					iconCache = (HashMap) o;
				}
				
				is.close();
				
				log.fine("Loading icon cache from " + iconFile.getName());
			} catch (ClassNotFoundException e) {
				log.log(Level.WARNING, "Error loading icon cache", e);
			} catch (IOException e) {
				log.log(Level.WARNING, "Error loading icon cache", e);
			}
		}
		
		public static void clearIcons() {
			iconCache.clear();
			iconFile.delete();
		}
		
		public static void saveItems() {
			Item cur = null;
			
			/* only cache items that we've actually
			 * gotten the info for
			 */
			for (Integer i : itemCache.keySet()) {
				cur = itemCache.get(i);
				
				if (!cur.retrievedInfo) {
					itemCache.remove(cur);
				}
			}
			
			try {
				ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(itemFile)
				);
				
				os.writeObject(itemCache);
				os.close();
				
				log.fine("Saving item cache to " + itemFile.getName());
			} catch (IOException e) {
				log.log(Level.SEVERE, "Error saving item cache", e);
			}
		}
		
		public static void loadItems() {
			try {
				File f = new File(itemFileName);
				
				if (f.exists())
					f.renameTo(itemFile);
				
				ObjectInputStream is = new ObjectInputStream(
					new FileInputStream(itemFile)
				);
				
				Object o = is.readObject();
				
				if (o instanceof HashMap) {
					itemCache = (HashMap) o;
				}
				
				is.close();
				
				log.fine("Loading item cache from " + itemFile.getName());
			} catch (ClassNotFoundException e) {
				log.log(Level.WARNING, "Error loading item cache", e);
			} catch (IOException e) {
				log.log(Level.WARNING, "Error loading item cache", e);
			}
		}
		
		public static void clearItems() {
			itemCache.clear();
			itemFile.delete();
		}
	}
}
