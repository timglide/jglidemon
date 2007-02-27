package jgm.wow;

/**
 * Represents a quantity of a certain item.
 * @author Tim
 * @since 0.4
 */
public class ItemSet {
	public int quantity = 0;
	private Item item;
	
	public ItemSet(Item item, int quantity) {
		this.quantity = quantity;
		this.item = item;
	}

	public Item getItem() {
		return item;
	}

	public int getQuantity() {
		return quantity;
	}
	
	public void addQuantity(int i) {
		quantity += i;
	}

	public void addQuantity(ItemSet i) {
		addQuantity(i.quantity);
	}
	
	public boolean equals(Object o) {
		return this == o ||
			(o instanceof ItemSet &&
			 item.equals(((ItemSet) o).item));
	}
	
	/**
	 * @return A comparator that sorts items by quantity desc and name
	 */
	public static java.util.Comparator<ItemSet> getQuantityComparator() {
		return getQuantityComparator(-1);
	}

	/**
	 * @param sort 1 for asc, -1 for desc by quantity
	 * @return A comporator thats sorts items by quantity and name
	 */
	public static java.util.Comparator<ItemSet> getQuantityComparator(final int sort) {
		return new java.util.Comparator<ItemSet>() {
			public int compare(ItemSet i1, ItemSet i2) {
				int ret = 0;
				if (i1.quantity < i2.quantity) ret = -1; else
				if (i1.quantity > i2.quantity) ret = 1; 

				if (ret == 0) {
					return i1.item.name.compareTo(i2.item.name);
				}

				return sort * ret;
			}
		};
	}
	
	public static ItemSet factory(int id, String name, int quantity) {
		Item item = Item.factory(id, name);
		
		if (item == null) return null;
		
		return new ItemSet(item, quantity);
	}
}
