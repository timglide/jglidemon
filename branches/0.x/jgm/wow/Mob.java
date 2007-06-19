package jgm.wow;

/**
 * 
 * @author Tim
 * @since 0.10
 */
public class Mob {
	public String timestamp;
	public int number = 0;
	public int xp = 0;
	public String name = "UNKNOWN";
	
	public Mob(String name) {
		this("", name, 0, 0);
	}
	
	public Mob(String timestamp, String name, int number, int xp) {
		this.timestamp = timestamp;
		this.name = name;
		this.number = number;
		this.xp = xp;
	}
	
	public void incr(Mob mob) {
		this.xp += mob.xp;
		this.xp /= 2;
		this.number++;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Mob)) return false;
		
		return ((Mob) o).name.equals(this.name);
	}
	
	public static java.util.Comparator<Mob> getQuantityComparator() {
		return getNumberComparator(-1);
	}
	
	public static java.util.Comparator<Mob> getNumberComparator(final int sort) {
		return new java.util.Comparator<Mob>() {
			public int compare(Mob i1, Mob i2) {
				int ret = 0;
				if (i1.number < i2.number) ret = -1; else
				if (i1.number > i2.number) ret = 1; 

				if (ret == 0) {
					return i1.name.compareTo(i2.name);
				}

				return sort * ret;
			}
		};
	}
}
