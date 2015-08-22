package jgm.wow;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	public Bag backpack;
	public Bag bag1;
	public Bag bag2;
	public Bag bag3;
	public Bag bag4;
	public PaperDoll equipped;

	private transient Map<Integer, ItemSet> itemSummary = null;
	
	public Map<Integer, ItemSet> getItemSummary() {
		if (null == itemSummary) {
			itemSummary = getItemSummary(null);
		}
		
		return itemSummary;
	}
	
	public Map<Integer, ItemSet> getItemSummary(Map<Integer, ItemSet> itemSummary) {
		if (null == itemSummary) {
			itemSummary = new HashMap<Integer, ItemSet>();
		}
		
		if (null != backpack)
			backpack.getItemSummary(itemSummary);
		if (null != bag1)
			bag1.getItemSummary(itemSummary);
		if (null != bag2)
			bag2.getItemSummary(itemSummary);
		if (null != bag3)
			bag3.getItemSummary(itemSummary);
		if (null != bag4)
			bag4.getItemSummary(itemSummary);
		
		return itemSummary;
	}
	
	public int getTotalFreeBagSlots() {
		int total = 0;
		
		if (null != backpack)
			total += backpack.getFreeSlots();
		if (null != bag1)
			total += bag1.getFreeSlots();
		if (null != bag2)
			total += bag2.getFreeSlots();
		if (null != bag3)
			total += bag3.getFreeSlots();
		if (null != bag4)
			total += bag4.getFreeSlots();
		
		return total;
	}
	
	public int getTotalBagSlots() {
		int total = 0;
		
		if (null != backpack)
			total += backpack.getSlots();
		if (null != bag1)
			total += bag1.getSlots();
		if (null != bag2)
			total += bag2.getSlots();
		if (null != bag3)
			total += bag3.getSlots();
		if (null != bag4)
			total += bag4.getSlots();
		
		return total;
	}
}
