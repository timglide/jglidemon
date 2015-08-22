package jgm.wow;

import java.util.HashMap;
import java.util.Map;

public class Bag {
	public int index;
	public Item bagItem;
	public ItemSet[] items;
	
	private transient Map<Integer, ItemSet> itemSummary = null;
	private transient int freeSlots = -1;
	
	/**
	 * Returns a map of item ids to item sets indicating the total number
	 * of each item across all slots in this bag.
	 * @return
	 */
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
		
		if (null == items)
			 return itemSummary;
		
		ItemSet is;
		
		for (ItemSet cur : items) {
			if (null == cur) continue;
			
			if (null == (is = itemSummary.get(cur.getItem().id))) {
				is = new ItemSet(cur.getItem(), cur.getQuantity());
				itemSummary.put(cur.getItem().id, is);
			} else {
				is.addQuantity(cur);
			}
		}
		
		return itemSummary;
	}
	
	
	public int getFreeSlots() {
		if (null == items)
			return 0;
		
		if (freeSlots < 0) {
			freeSlots = getSlots();
			
			for (int i = 0; i < items.length; i++) {
				if (null != items[i])
					freeSlots--;
			}
		}
		
		return freeSlots;
	}
	
	public int getSlots() {
		return null != items ? items.length : 0;
	}
}
