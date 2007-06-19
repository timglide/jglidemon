package jgm.wow;

/**
 * 
 * @author Tim
 * @since 0.10
 */
public class Skill {
	public String timestamp = "";
	public String name = "";
	public int level = 0;
	
	public Skill(String timestamp, String name, int level) {
		this.timestamp = timestamp;
		this.name = name;
		this.level = level;
	}
	
	public void incr(Skill s) {
		this.timestamp = s.timestamp;
		this.level = s.level;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Skill)) return false;
		
		return ((Skill) o).name.equals(this.name);
	}
	
	public static java.util.Comparator<Skill> getLevelComparator() {
		return getLevelComparator(-1);
	}
	
	public static java.util.Comparator<Skill> getLevelComparator(final int sort) {
		return new java.util.Comparator<Skill>() {
			public int compare(Skill i1, Skill i2) {
				int ret = 0;
				if (i1.level < i2.level) ret = -1; else
				if (i1.level > i2.level) ret = 1; 

				if (ret == 0) {
					return i1.name.compareTo(i2.name);
				}

				return sort * ret;
			}
		};
	}
}
