package jgm.wow;

/**
 * 
 * @author Tim
 * @since 0.10
 */
public class Rep {
	public String timestamp = "";
	public String faction = "";
	public int amount = 0;
	
	public Rep(String timestamp, String faction, int amount) {
		this.timestamp = timestamp;
		this.faction = faction;
		this.amount = amount;
	}
	
	public void incr(Rep rep) {
		this.timestamp = rep.timestamp;
		this.amount += rep.amount;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Rep)) return false;
		
		return ((Rep) o).faction.equals(this.faction);
	}
	
	public static java.util.Comparator<Rep> getAmountComparator() {
		return getAmountComparator(-1);
	}
	
	public static java.util.Comparator<Rep> getAmountComparator(final int sort) {
		return new java.util.Comparator<Rep>() {
			public int compare(Rep i1, Rep i2) {
				int ret = 0;
				if (i1.amount < i2.amount) ret = -1; else
				if (i1.amount > i2.amount) ret = 1; 

				if (ret == 0) {
					return i1.faction.compareTo(i2.faction);
				}

				return sort * ret;
			}
		};
	}
}
