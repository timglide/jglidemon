package jgm.wow;

import java.awt.Color;
import javax.swing.ImageIcon;

public class Item implements Comparable<Item> {
	public static final int POOR = 0;
	public static final int COMMON = 1;
	public static final int UNCOMMON = 2;
	public static final int RARE = 3;
	public static final int EPIC = 4;
	public static final int LEGENDARY = 5;
	public static final int RELIC = 6;

	public static final Color[] DARK_COLORS = {
		Color.DARK_GRAY, Color.BLACK, Color.GREEN,
		Color.BLUE, new Color(128, 0, 128),
		Color.ORANGE, Color.RED
	};
	
	public static final Color[] LIGHT_COLORS = {
		Color.GRAY, Color.WHITE, new Color(0x1EFF00),
		new Color(0x0070DD), new Color(0xA434EE),
		new Color(0xD17C22), new Color(0xFF0000)
	};
	
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
	
	public static final String[] SLOTS = {
		"Ammo", "Head", "Neck", "Shoulder", "Shirt", "Chest", "Waist",
		"Legs", "Feet", "Wrist", "Hands", "Finger", "Finger", "Trinket", "Trinket",
		"Back", "Main Hand", "Off Hand", "Ranged", "Tabard"
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

	public int id = 0;
	public int quality = POOR;
	public int quantity = 0;
	
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
	
	public int[] stats = new int[5];
	public int[] stat_values = new int[5];
	
	public Effect[] effects = new Effect[3];
	
	public String iconPath = "/images/icons/INV_Misc_QuestionMark.png";
	private ImageIcon icon = null;

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
							return "Ranged";
					}
					return SLOTS[slot];
					
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
				return stat_values[n] + " " + STATS[stats[n]];
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
		return getColor(false);
	}
	
	public Color getLightColor() {
		return getColor(true);
	}
	
	public Color getColor(boolean light) {
		return getColor(quality, light);
	}

	public static Color getColor(int i, boolean light) {
		i = (i > RELIC)
			  ? RELIC
			  : (i < POOR)
			    ? POOR
				: i;
		return light ? LIGHT_COLORS[i] : DARK_COLORS[i];
	}

	public ImageIcon getIcon() {
		if (icon == null) {
			try {
				icon = new javax.swing.ImageIcon(
					   new java.net.URL(ICON_BASE + iconPath));
			} catch (java.net.MalformedURLException e) {
				System.err.println("Unable to make icon in Item: " + e.getMessage());
				icon = null;
			}
		}

		return icon;
	}

	public void addQuantity(int i) {
		quantity += i;
	}

	public void addQuantity(Item i) {
		addQuantity(i.quantity);
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

	public static java.util.Comparator<Item> getQuantityComparator() {
		return getQuantityComparator(-1);
	}

	/**
	 * Sort by quantity, ascending or descending and then by name.
	 */
	public static java.util.Comparator<Item> getQuantityComparator(final int sort) {
		return new java.util.Comparator<Item>() {
			public int compare(Item i1, Item i2) {
				int ret = 0;
				if (i1.quantity < i2.quantity) ret = -1; else
				if (i1.quantity > i2.quantity) ret = 1; 

				if (ret == 0) {
					return i1.name.compareTo(i2.name);
				}

				return sort * ret;
			}
		};
	}

	public static Item factory(int id, String name) {
		return factory(id, name, 0);
	}

	public static Item factory(int id, String name, int initialQuantity) {
		Item item = new Item(id, name);

		if (!ItemFactory.factory(id, item)) return null;

		item.quantity = initialQuantity;

		Effect.factory(item);
		
		return item;
	}
}
