package jgm.gui.components;

import jgm.wow.*;

import java.awt.*;

import javax.swing.*;

/**
 * Represents an item tooltip similar to that found
 * when one mouseovers an item in-game.
 * @author Tim
 * @since 0.1
 */
public class ItemTooltip extends JPanel {
	public static final int PADDING = 20;
	
	private JPanel p; // internal panel
	
	private String title = "";
	private ItemSet itemSet = null;
	private Item item = null;
	
	private GridBagConstraints c;
	private JLabel titleLabel;
	private JLabel bindLabel;
	private JLabel type1Label; // slot
	private JLabel type2Label; // weapon type/armor type
	private JLabel damageLabel;
	private JLabel speedLabel;
	private JLabel dpsLabel;
	private JLabel armorLabel;
	private JLabel[] statLabels = new JLabel[5];
//	private JLabel duraLabel;
	private JLabel reqLvlLabel;
	private JLabel[] effectLabels = new JLabel[3];
	private static String merchantPriceFormat1 = "Sells For%s: ";
	private static String merchantPriceFormat2 = " (x%d)";
	private GoldPanel merchentBuyPricePanel;
	private static String stackPriceFormat = "Per Stack of %d: ";
	private GoldPanel stackPricePanel;
	private JLabel itemLvlLabel;
	private JLabel descriptionLabel;
//	private JLabel sourceLabel;
	
	public ItemTooltip(ItemSet i) {
		init();
		setItemSet(i);
	}
	
	public ItemTooltip(String s) {
		title = s;
		init();
	}
	
	private void init() {
		this.setMaximumSize(new Dimension(300, 600));
		p = new JPanel();
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		p.setLayout(new GridBagLayout());
		p.setOpaque(false);
		setBounds(0, 0, 250, 300);
		setBackground(Color.black);
		setOpaque(true);
		setVisible(false);
				
		titleLabel = new JLabel(title, JLabel.CENTER);
		titleLabel.setFont(Item.TITLE_FONT);
		titleLabel.setForeground(Color.WHITE);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0;
		p.add(titleLabel, c);
		
		bindLabel = new JLabel("Binds when picked up", JLabel.LEFT);
		bindLabel.setForeground(Color.WHITE);
		c.gridy++;
		p.add(bindLabel, c);
		
		type1Label = new JLabel("Two-hand", JLabel.LEFT);
		type1Label.setForeground(Color.WHITE);
		c.gridy++; c.gridwidth = 1;
		p.add(type1Label, c);
		
		type2Label = new JLabel("Staff", JLabel.RIGHT);
		type2Label.setForeground(Color.WHITE);
		c.gridx++;
		p.add(type2Label, c);
		
		damageLabel = new JLabel("94 - 140 Damage", JLabel.LEFT);
		damageLabel.setForeground(Color.WHITE);
		c.gridx = 0; c.gridy++;
		p.add(damageLabel, c);
		
		speedLabel = new JLabel("Sped 3.40", JLabel.RIGHT);
		speedLabel.setForeground(Color.WHITE);
		c.gridx++;
		p.add(speedLabel, c);
		
		dpsLabel = new JLabel("(34.7 Damage Per Second)");
		dpsLabel.setForeground(Color.WHITE);
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		p.add(dpsLabel, c);
		
		armorLabel = new JLabel("123 Armor");
		armorLabel.setForeground(Color.WHITE);
		c.gridy++;
		p.add(armorLabel, c);
		
		for (int n = 0; n < 5; n++) {
			statLabels[n] = new JLabel("+" + n + " Stat");
			statLabels[n].setForeground(Color.WHITE);
			c.gridy++;
			p.add(statLabels[n], c);
		}
		
/*		duraLabel = new JLabel("100/100 Durability", JLabel.LEFT);
		duraLabel.setForeground(Color.WHITE);
		c.gridy++;
		add(duraLabel, c);*/
		
		reqLvlLabel = new JLabel("Requires Level 34", JLabel.LEFT);
		reqLvlLabel.setForeground(Color.WHITE);
		c.gridy++;
		p.add(reqLvlLabel, c);
		
		for (int i = 0; i < 3; i++) {
			effectLabels[i] = new JLabel("Effect: some effect " + i);
			effectLabels[i].setForeground(Item.getColor(Item.UNCOMMON, true));
			c.gridy++;
			p.add(effectLabels[i], c);
		}
		
		descriptionLabel = new JLabel("\"Yellow text\"");
		descriptionLabel.setForeground(Item.GOLD);
		c.gridy++;
		p.add(descriptionLabel, c);
		
		merchentBuyPricePanel = new GoldPanel("Sell Price: ", 4777, true, Color.WHITE);
		merchentBuyPricePanel.setOpaque(false);
		c.gridy++;
		p.add(merchentBuyPricePanel, c);
		
		stackPricePanel = new GoldPanel("", 0, true, Color.WHITE);
		stackPricePanel.setOpaque(false);
		c.gridy++;
		p.add(stackPricePanel, c);
		
		itemLvlLabel = new JLabel("Item Level 39", JLabel.LEFT);
		itemLvlLabel.setForeground(Color.WHITE);
		c.gridy++;
		p.add(itemLvlLabel, c);
			
/*		sourceLabel = new JLabel("Source: Drop", JLabel.LEFT);
		sourceLabel.setForeground(Color.WHITE);
		c.gridy++;
		add(sourceLabel, c);*/
		
		add(p, BorderLayout.CENTER);
	}
	
	/**
	 * Update the tooltip to reflect a new item.
	 * @param item The item to update the tooltip to represent
	 */
	public void setItemSet(ItemSet i) {
		//System.out.println("In setItem()");
		if (item != null &&
			i.getItem().equals(item) &&
			itemSet.quantity == i.quantity) return;

		itemSet = i;
		item = i.getItem();
		//System.out.println("Resetting item");
		titleLabel.setText(item.name);
		Color c = item.getLightColor();
		titleLabel.setForeground(c);
		
		String s = null;
		
		updateLbl(bindLabel, item.getBindText());
		updateLbl(type1Label, item.getType1Text());
		updateLbl(type2Label, item.getType2Text());
		
		s = item.dmgLow > 0 && item.dmgHigh > 0
			? item.dmgLow + " - " + item.dmgHigh + " Damage  " : null;
		updateLbl(damageLabel, s);
		
		s = item.speed > 0
			? String.format("  Speed %01.2f", item.speed / 1000.0) : null;
		updateLbl(speedLabel, s);
		
		s = item.dmgLow > 0 && item.dmgHigh > 0 && item.speed > 0
			? String.format("(%01.1f damage per second)", ((item.dmgLow + item.dmgHigh) / 2.0) / (item.speed / 1000.0))
			: null;
		updateLbl(dpsLabel, s);
		
		s = item.armor > 0 ? (item.armor + " Armor") : null;
		updateLbl(armorLabel, s);
		
		for (int n = 0; n < 5; n++) {
			updateLbl(statLabels[n], item.getStatText(n));
		}
		
		s = item.requiredLevel > 0
			? "Requires Level " + item.requiredLevel : null;
		updateLbl(reqLvlLabel, s);
		
		for (int n = 0; n < 3; n++) {
			s = item.getEffectText(n);
			s = s != null
				? "<html>" + lineify(s) + "</html>" : null;
			updateLbl(effectLabels[n], s);
		}
		
		merchentBuyPricePanel.setText(String.format(merchantPriceFormat1,
			(itemSet.quantity > 1
			 ? String.format(merchantPriceFormat2, itemSet.quantity) : "")));
		merchentBuyPricePanel.setMoney(item.merchentBuyPrice * itemSet.quantity);
		
		stackPricePanel.setText(String.format(stackPriceFormat, item.stackSize));
		stackPricePanel.setMoney(item.merchentBuyPrice * ((item.stackSize > 1) ? item.stackSize : 0));
		
		s = item.itemLevel > 0 ? "Item Level " + item.itemLevel : null;
		updateLbl(itemLvlLabel, s);
		
		s = item.description != null
			? "<html>" + lineify('"' + item.description + '"') + "</html>" : null;
		updateLbl(descriptionLabel, s);
		
		Point     pt   = this.getLocation();
		Dimension pref = p.getPreferredSize();
		this.setBounds(
			pt.x, pt.y, pref.width + PADDING, pref.height + PADDING
		);
		
		revalidate();
	}
	
	private void updateLbl(JLabel l, String s) {
		//System.out.println("Updating " + l + " to " + s);
		if (s == null) {
			l.setText("");
			l.setVisible(false);
		} else {
			l.setText(s);
			l.setVisible(true);
		}
	}
	
	public static final int CHARS_PER_LINE = 35;
	
	/**
	 * Insert "&lt;br&gt;\n" in a String preferably every
	 * {@link #CHARS_PER_LINE} characters. The string will
	 * be split at the next space after CHARS_PER_LINE.
	 * @param s The String to add newlines to
	 * @return The String with newlines added
	 * @since 0.3
	 */
	public static final String lineify(String s) {
		String[] words = s.split(" ");
		StringBuffer out = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < words.length; i++) {
			if (sb.length() >= CHARS_PER_LINE) {
				out.append(sb);
				out.append("<br>\n");
				
				sb = new StringBuffer();
			}
			
			sb.append(words[i] + " ");
		}
		
		out.append(sb);
		
		return out.toString();
	}
}
