package jgm.gui.components;

import javax.swing.*;

/**
 * A JPanel with a JLabel for gold, silver, and copper.
 * Also has a gold/silver/copper gif icon next to the number
 * and an optional prefix.
 * @author Tim
 * @since 0.1
 */
public class GoldPanel extends JPanel {
	private String      title;
	private int         money  = 0;
	private boolean     hideWhenZero = false;

	private        JLabel      ttlLabel = null;
	
	// 0, 1, 2 => gold, silver, copper
	private        JLabel[]    labels = null;
	private static ImageIcon[] icons  = null;
	
	/**
	 * Create a GoldPanel with the given prefix and
	 * an initial amount of 0.
	 * @param prefix
	 */
	public GoldPanel(String prefix) {
		this(0, prefix, false);
	}
	
	/**
	 * Create a GoldPanel with an initial amount of 0.
	 */
	public GoldPanel() {
		this(0, "", false);
	}
	
	/**
	 * Create a GoldPanel with the specified amount.
	 * @param gold The amount of gold
	 * @param silver The amount of silver
	 * @param copper The amount of copper
	 */
	public GoldPanel(int gold, int silver, int copper) {
		this(gscToC(gold, silver, copper), "", false);
	}
	
	/**
	 * Create a new GoldPanel.
	 * @param s A prefix to precede the gold icons with
	 * @param i The amount of copper to represend
	 * @param hide Whether to hide the panel when there is zero copper worth
	 * @param fontColor The foreground color of the labels
	 */
	public GoldPanel(String s, int i, boolean hide, java.awt.Color fontColor) {
		this(i, s, hide);
		setTextColor(fontColor);
	}
	
	public GoldPanel(int i, String s, boolean hide) {
		super();
		if (icons == null) {
			icons = new ImageIcon[3];
			icons[0] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/gold.gif")); 
			icons[1] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/silver.gif"));
			icons[2] = new ImageIcon(jgm.JGlideMon.class.getResource("resources/images/coins/copper.gif"));
		}
		
		title = s;
		hideWhenZero = hide;
		ttlLabel = new JLabel(title);
		add(ttlLabel);
		
		labels = new JLabel[3];
		for (int n = 0; n < labels.length; n++) {
			labels[n] = new JLabel("0",
				icons[n],
				JLabel.LEFT
			);
			labels[n].setHorizontalTextPosition(JLabel.LEFT);
			add(labels[n]);
		}
		
		setMoney(i);
	}
	
	public void setText(String s) {
		title = s;
		ttlLabel.setText(title);
	}
	
	public void setTextColor(java.awt.Color c) {		
		ttlLabel.setForeground(c);
		
		for (int n = 0; n < labels.length; n++) {
			labels[n].setForeground(c);
		}
	}
	
	public int getMoney() {
		return money;
	}
	
	public void setMoney(int c) {
		setMoney(cToGsc(c));
	}
	
	public void setMoney(int[] i) {
		setMoney(i[0], i[1], i[2]);
	}
	
	public void setMoney(int g, int s, int c) {
		money = gscToC(g, s, c);
		
		labels[0].setText(Integer.toString(g));
		labels[1].setText(Integer.toString(s));
		labels[2].setText(Integer.toString(c));
		
		if (g == 0) {
			labels[0].setVisible(false);
			
			if (s == 0) {
				labels[1].setVisible(false);
				
				if (c == 0 && this.hideWhenZero) {
					this.setVisible(false);
				} else {
					this.setVisible(true);
				}
			} else {
				labels[1].setVisible(true);
				this.setVisible(true);
			}
		} else {
			labels[0].setVisible(true);
			labels[1].setVisible(true);
			this.setVisible(true);
		}
	}
	
	public void addMoney(int c) {
		setMoney(money + c);
	}
	
	public void addMoney(int g, int s, int c) {
		addMoney(gscToC(g, s, c));
	}
	
	/**
	 * Convert seperate gold, silver, and copper values into
	 * a single copper value.
	 * @param gold The amount of gold
	 * @param silver The amount of silver
	 * @param copper The amount of copper
	 * @return The total amount of copper
	 */
	public static int gscToC(int gold, int silver, int copper) {
		return copper + silver * 100 + gold * 100 * 100;
	}
	
	/**
	 * Convert a single copper value to individual gold, silver,
	 * and copper values.
	 * @param copper The total amount of copper
	 * @return An array with 3 elements corresponding to 
	 * the amount of gold, silver, and copper.
	 */
	public static int[] cToGsc(int copper) {
		// i = 1234567 (123.45.67)
		
		int[] ret = new int[3];
		
		int i = copper / 100;       // 12345 (123.45)
		ret[0] = i / 100;           // 123
		ret[1] = i - ret[0] * 100;  // 12345 - 12300 = 45
		ret[2] = copper - i * 100;  // 1234567 - 1234500 = 67
		
		return ret;
	}
}
