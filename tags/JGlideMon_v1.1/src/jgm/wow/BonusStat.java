package jgm.wow;

import java.util.HashMap;
import java.util.Map;

public enum BonusStat {
	HEALTH(1, "%+d Health"),
	MANA(2, "%+d Mana"),
	AGILITY(3, "%+d Agility"),
	STRENGTH(4, "%+d Strength"),
	INTELLECT(5, "%+d Intellect"),
	SPIRIT(6, "%+d Spirit"),
	STAMINA(7, "%+d Stamina"),
	
	HEALTH_PER_FIVE(46, "Equip: Restores %s health per 5 sec."),
	ARMOR_PENETRATION(44, "Equip: Increases your armor penetration rating by %s."),
	ATTACK_POWER(38, "Equip: Increases attack power by %s."),
	SHIELD_BLOCK(15, "Equip: Increases your shield block rating by %s."),
	BLOCK_VALUE(48, "Equip: Increases the block value of your shield by %s."),
	MELEE_CRIT(19, "Equip: Improves melee critical strike rating by %s."),
	RANGED_CRIT(20, "Equip: Improves ranged critical strike rating by %s."),
	CRIT(32, "Equip: Increases your critical strike rating by %s."),
	SPELL_CRIT(21, "Equip: Improves spell critical strike rating by %s."),
	MELEE_CRIT_AVOIDANCE(25, "Equip: Improves melee critical avoidance rating by %s."),
	RANGED_CRIT_AVOIDANCE(26, "Equip: Improves ranged critical avoidance rating by %s."),
	CRIT_AVOIDANCE(34, "Equip: Improves critical avoidance rating by %s."),
	SPELL_CRIT_AVOIDANCE(27, "Equip: Improves spell critical avoidance rating by %s."),
	DEFENSE(12, "Equip: Increases defense rating by %s."),
	DODGE(13, "Equip: Increases your dodge rating by %s."),
	EXPERTISE(37, "Equip: Increases your expertise rating by %s."),
	FERAL_ATTACK_POWER(40, "Equip: Increases attack power by %s in Cat, Bear, Dire Bear, and Moonkin forms only."),
	MELEE_HASTE(28, "Equip: Improves melee haste rating by %s."),
	RANGED_HASTE(29, "Equip: Improves ranged haste rating by %s."),
	HASTE(36, "Equip: Increases your haste rating by %s."),
	SPELL_HASTE(30, "Equip: Improves spell haste rating by %s."),
	MELEE_HIT(16, "Equip: Improves melee hit rating by %s."),
	RANGED_HIT(17, "Equip: Improves ranged hit rating by %s."),
	HIT(31, "Equip: Increases your hit rating by %s."),
	SPELL_HIT(18, "Equip: Improves spell hit rating by %s."),
	MELEE_HIT_AVOIDANCE(22, "Equip: Improves melee hit avoidance rating by %s."),
	RANGED_HIT_AVOIDANCE(23, "Equip: Improves ranged hit avoidance rating by %s."),
	HIT_AVOIDANCE(33, "Equip: Improves hit avoidance rating by %s."),
	SPELL_HIT_AVOIDANCE(24, "Equip: Improves spell hit avoidance rating by %s."),
	MANA_PER_FIVE(43, "Equip: Restores %s mana per 5 sec."),
	MASTERY(49, "Equip: Increases your mastery rating by %s."),
	PARRY(14, "Equip: Increases your parry rating by %s."),
	RANGED_ATTACK_POWER(39, "Equip: Increases ranged attack power by %s."),
	RESILIENCE(35, "Equip: Increases your resilience rating by %s."),
	MAGIC_DAMAGE_DONE(41, "Equip: Increases damage done by magical spells and effects by up to %s."),
	MAGIC_HEALING_DONE(42, "Equip: Increases healing done by magical spells and effects by up to %s."),
	SPELL_PENETRATION(47, "Equip: Increases spell penetration by %s."),
	SPELL_POWER(45, "Equip: Increases spell power by %s.");
	
	// kludge, can't access static field BonusStat.var from constructor for some reason
	private static class MapHolder {
		public static final Map<Integer, BonusStat> statMap = new HashMap<Integer, BonusStat>();
	}
	
	public static BonusStat getById(Integer id) {
		return MapHolder.statMap.get(id);
	}
	
	public final int id;
	public final String text;
	
	private BonusStat(int id, String text) {
		this.id = id;
		this.text = text;
		MapHolder.statMap.put(id, this);
	}
	
	public boolean isNormalStat() {
		return id <= 7;
	}
	
	public boolean isEffectStat() {
		return id > 7;
	}
	
	public String format(Object ... args) {
		return String.format(text, args);
	}
}
