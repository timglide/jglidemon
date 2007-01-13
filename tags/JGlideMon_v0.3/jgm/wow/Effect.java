package jgm.wow;

/**
 * Represents an effect an item can have. These
 * include use, equip, and chance on hit.
 * @author Tim
 * @since 0.1
 */
public class Effect {
	public enum Type {
		UNKNOWN       ("Unknown"),
		USE           ("Use"),
		EQUIP         ("Equip"),
		CHANCE_ON_HIT ("Chance On Hit");
		
		private String text;
		
		private Type(String s) {
			text = s;
		}
		
		public String toString() {
			return text;
		}
	}
	
	public int id;
	public Type type = Type.UNKNOWN;
	public String text;
	
	public Effect() {
	}
	
	public String getText() {
		return text;
	}
	
	public String getTypeText() {
		return type.toString();
	}
	
	public String toString() {
		return getTypeText() + ": " + getText();
	}
	
	public static boolean factory(Item item) {
		return EffectFactory.factory(item);
	}
}
