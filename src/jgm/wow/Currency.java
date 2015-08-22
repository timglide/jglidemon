package jgm.wow;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class Currency implements Cloneable {
	private static final Map<Integer, Currency> currencyCache = new HashMap<Integer, Currency>();
	
	public int id;
	public String name;
	public int count;
	public String iconPath = Item.DEFAULT_ICON;
	private transient ImageIcon icon = null;
	
	private Currency(int id, String name, int count) {
		this.id = id;
		this.name = name;
		this.count = count;
	}
	
	public ImageIcon getIcon()
	{
		if (null == icon) {
			icon = Item.getIcon(iconPath);
		}
		
		return icon;
	}
	
	@Override
	public String toString() {
		return "Currency[id=" + id + ", count=" + count + ", iconPath=" + iconPath + ", name=" + name + "]";
	}
	
	@Override
	public Currency clone() {
		try {
			return (Currency) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("clone not supported");
		}
	}
	
	public static Currency factory(int id, String name, int count)
	{
		Currency c = currencyCache.get(id);
		
		if (null != c) {
			c = c.clone();
			c.count = count;
			
			if (Item.DEFAULT_ICON.equals(c.iconPath))
				CurrencyFactory.factory(c);
			
			return c;
		}
		
		c = new Currency(id, name, count);
		
		CurrencyFactory.factory(c);
		
		if (id > 0)
			currencyCache.put(id, c.clone());
		
		return c;
	}
}
