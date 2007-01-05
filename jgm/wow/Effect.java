package jgm.wow;

public class Effect {
	public static final int UNKNOWN = 0;
	public static final int USE = 1;
	public static final int EQUIP = 2;
	public static final int CHANCE_ON_HIT = 3;
	
	public static final String[] TYPES = {
		"Unknown", "Use", "Equip", "Chance On Hit"
	};
	
	public int id;
	public int type = 0;
	public String text;
	
	public Effect() {
	}
	
	public String getText() {
		return text;
	}
	
	public String getTypeText() {
		try {
			return TYPES[type];
		} catch (Exception e) {
			return TYPES[UNKNOWN];
		}
	}
	
	public String toString() {
		return getTypeText() + ": " + getText();
	}
	
	public static boolean factory(Item item) {
		return EffectFactory.factory(item);
	}
}
