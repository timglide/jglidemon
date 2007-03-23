package jgm.wow;

import java.util.regex.*;

/**
 * Represents one of the 3 "mana" types, either
 * mana, rage, or energy. 
 * @author Tim
 * @since 0.4
 */
public enum ManaType {
	MANA   ("Mana"), 
	RAGE   ("Rage"),
	ENERGY ("Energy");

	private static final Pattern caster = Pattern.compile(".*\\((\\d+)%\\).*");
	private static final Pattern melee  = Pattern.compile(".*?(\\d+).*");
		
	private String type;
	
	private ManaType(String type) {
		this.type = type;
	}
	
	/**
	 * @return The appropriate pattern to match this mana type
	 */
	public Pattern getRegex() {
		switch (this) {
			case MANA: return caster;
			default:   return melee;
		}
	}
	
	public String toString() {
		return type;
	}
}
