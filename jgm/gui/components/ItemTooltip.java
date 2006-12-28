package jgm.gui.components;

import jgm.wow.Item;

import java.awt.*;

import javax.swing.*;

public class ItemTooltip extends JPanel {
	private JPanel p; // internal panel
	
	private String title = "";
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
	private GoldPanel merchentBuyPricePanel;
	private JLabel itemLvlLabel;
	private JLabel descriptionLabel;
//	private JLabel sourceLabel;
	
	public ItemTooltip(Item i) {
		init();
		setItem(i);
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
	
	public void setItem(Item i) {
		//System.out.println("In setItem()");
		if (item != null && i.equals(item)) return;

		item = i;
		//System.out.println("Resetting item");
		titleLabel.setText(item.name);
		Color c = item.getLightColor();
		titleLabel.setForeground(c);
		
		String s = null;
		
		updateLbl(bindLabel, i.getBindText());
		updateLbl(type1Label, i.getType1Text());
		updateLbl(type2Label, i.getType2Text());
		
		s = i.dmgLow > 0 && i.dmgHigh > 0
			? i.dmgLow + " - " + i.dmgHigh + " Damage  " : null;
		updateLbl(damageLabel, s);
		
		s = i.speed > 0
			? String.format("  Speed %01.1f", i.speed / 1000.0) : null;
		updateLbl(speedLabel, s);
		
		s = i.dmgLow > 0 && i.dmgHigh > 0 && i.speed > 0
			? String.format("(%01.2f damage per second)", ((i.dmgLow + i.dmgHigh) / 2.0) / (i.speed / 1000.0))
			: null;
		updateLbl(dpsLabel, s);
		
		s = i.armor > 0 ? (i.armor + " Armor") : null;
		updateLbl(armorLabel, s);
		
		for (int n = 0; n < 5; n++) {
			updateLbl(statLabels[n], i.getStatText(n));
		}
		
		s = i.requiredLevel > 0
			? "Requires Level " + i.requiredLevel : null;
		updateLbl(reqLvlLabel, s);
		
		for (int n = 0; n < 3; n++) {
			s = i.getEffectText(n);
			s = s != null
				? "<html>" + s.replaceAll("\\. ", ".<br>") + "</html>" : null;
			updateLbl(effectLabels[n], s);
		}
		
		merchentBuyPricePanel.setText("Sell Price" +
			(i.quantity > 1
			 ? " (x" + i.quantity + ")" : "") + ": ");
		merchentBuyPricePanel.setMoney(i.merchentBuyPrice * i.quantity);
		
		s = i.itemLevel > 0 ? "Item Level " + i.itemLevel : null;
		updateLbl(itemLvlLabel, s);
		
		s = i.description != null
			? '"' + i.description + '"' : null;
		updateLbl(descriptionLabel, s);
		
		Point     pt   = this.getLocation();
		Dimension pref = p.getPreferredSize();
		this.setBounds(
			pt.x, pt.y, pref.width + 20, pref.height + 20
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
}
