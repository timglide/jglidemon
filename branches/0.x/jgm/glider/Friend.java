package jgm.glider;

import java.util.Date;

public class Friend implements Comparable<Friend> {
	public static enum Status {
		ADDING, REMOVING, FOLLOWING
	}
	
	public Date   timestamp = new Date();
	public String name;
	public String id;
	public String race;
	public Status status = null;
	public int    encounters = 1;
	public int    followingTimes = 0;
	
	public Friend(String name) {
		this(name, null, null, Status.REMOVING);
	}
	
	public Friend(String name, String id, String race, Status status) {
		this.name = name;
		this.id   = id;
		this.race = race;
		this.status = status;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		
		if (o instanceof Friend) {
			Friend f = (Friend) o;
			return name.equals(f.name) ||
				(id != null && id.equals(f.id));
		}
		
		return false;
	}
	
	public int compareTo(Friend f) {
		return -timestamp.compareTo(f.timestamp);
	}
	
	public void update(Friend f) {
		status = f.status;
		
		if (status == Status.ADDING)
			encounters++;
		
		if (status == Status.FOLLOWING)
			followingTimes++;
		
//		if (status != Status.REMOVING)
		timestamp = f.timestamp;
		
		if (null != id && null != f.id && id.equals(f.id) && name.equals("(unknown)"))
			name = f.name;
		if (null == id && null != f.id)
			id = f.id;
		if (null == race && null != f.race)
			race = f.race;
	}
}
