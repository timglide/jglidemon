package jgm.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import jgm.glider.Status;
import jgm.gui.GUI;

public class OverviewTab extends Tab implements Clearable {
	private NumberFormat f = NumberFormat.getIntegerInstance();
	private StatsPanel sp;
	private MiscPanel mp;
	
	public OverviewTab(GUI gui) {
		super(gui, new BorderLayout(), "Overview");
		JPanel panel = new JPanel();
		FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 20, 20);
		fl.setAlignOnBaseline(true);
		panel.setLayout(fl);
		sp = new StatsPanel();
		sp.setBorder(BorderFactory.createTitledBorder("Statistics"));
		panel.add(sp);
		mp = new MiscPanel();
		mp.setBorder(BorderFactory.createTitledBorder("Miscellaneous"));
		panel.add(mp);
		add(new JScrollPane(panel), BorderLayout.CENTER);
	}
	
	private static void setError(JComponent label, JComponent text, boolean isError) {
		setBoldFont(label, isError);
		setError(text, isError);
	}
	
	private static void setError(JComponent c, boolean isError) {
		if (isError) {
			setBoldFont(c);
			c.setForeground(Color.RED);
		} else {
			setUnboldFont(c);
			c.setForeground(null);
		}
	}

	private static void setBoldFont(JComponent c, boolean bold) {
		if (bold) {
			setBoldFont(c);
		} else {
			setUnboldFont(c);
		}
	}
	
	private static void setBoldFont(JComponent c) {
		c.setFont(c.getFont().deriveFont(Font.BOLD));
	}
	
	private static void setUnboldFont(JComponent c) {
		c.setFont(c.getFont().deriveFont(c.getFont().getStyle() & ~Font.BOLD));
	}

	private void _(JLabel l, boolean b) {
		_(l, b ? "Yes" : "No");
	}

	private void _(JLabel l, Object o) {
		_(l, String.valueOf(o));
	}
	
	private void _(JLabel l, String text) {
		l.setText(text);
	}
	
	private void _(JLabel l, Number value) {
		l.setText(f.format(value));
	}
	
	public void incrementWhispers() {
		updateWhispers(sp.whisperCount + 1);
	}
	
	@Override
	public void clear(boolean clearingAll) {
		updateWhispers(0);
	}
	
	public void updateWhispers(int newAmount) {
		sp.whisperCount = newAmount;
		_(sp.wTotal, newAmount);
		setError(sp.wLbl, sp.wTotal, newAmount > 0);
	}
	
	@Override
	public void update(Status s) {
		if (s.attached) {
			_(sp.kTotal, s.kills);
			_(sp.kHour, s.killsPerHour);
			_(sp.lTotal, s.loots);
			_(sp.lHour, s.lootsPerHour);
			_(sp.dTotal, s.deaths);
			_(sp.dHour, s.deathsPerHour);
			
			_(sp.hTotal, s.honorGained);
			_(sp.hHour, s.honorPerHour);
			_(sp.bgWTotal, s.bgsWon);
			_(sp.bgWHour, s.bgsWonPerHour);
			_(sp.bgLTotal, s.bgsLost);
			_(sp.bgLHour, s.bgsLostPerHour);
			_(sp.bgTTotal, s.bgsCompleted);
			_(sp.bgTHour, s.bgsPerHour);
			
			_(sp.nTotal, s.nodes);
			_(sp.nHour, s.nodesPerHour);
			_(sp.sTotal, s.solves);
			_(sp.sHour, s.solvesPerHour);
			_(sp.fTotal, s.fish);
			_(sp.fHour, s.fishPerHour);
			
			
			_(mp.bText, s.mode);
			_(mp.gText, s.goalText);
			_(mp.sText, s.statusText);
			_(mp.pText, s.profile);
			
			_(mp.actText, s.accountName + ", " + s.realm);
			_(mp.zText, s.zone + (!s.zone.equals(s.map) ? ", " + s.map : ""));
			_(mp.szText, s.subZone);
			_(mp.lText, s.location);
		}
		
		
		setError(mp.cLbl, mp.cText, !gui.sm.connector.isConnected());
		_(mp.cText, gui.sm.connector.isConnected());
		
		setError(mp.aLbl, mp.aText, !s.attached);
		_(mp.aText, s.attached);
		
		setError(mp.rLbl, mp.rText, !s.running);
		_(mp.rText, s.running);
	}
	
	private class StatsPanel extends JPanel {
		int whisperCount = 0;
		
		JLabel
			totalLbl, hourLbl,
			wLbl, kLbl, lLbl, dLbl, hLbl, bgWLbl, bgLLbl, bgTLbl, nLbl, sLbl, fLbl,
			wTotal, kTotal, lTotal, dTotal, hTotal, bgWTotal, bgLTotal, bgTTotal, nTotal, sTotal, fTotal,
			wHour, kHour, lHour, dHour, hHour, bgWHour, bgLHour, bgTHour, nHour, sHour, fHour
			;
		
		StatsPanel() {
			super(new GridBagLayout());
			c = new GridBagConstraints();
			c.insets.right = 10;
			c.anchor = GridBagConstraints.BASELINE_LEADING;
			
			c.gridx = 1; c.gridy = 0;
			add(totalLbl = new JLabel("Total"), c);
			c.gridx++;
			add(hourLbl = new JLabel("Per Hour"), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(wLbl = new JLabel("Whispers: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(wTotal = new JLabel("0", JLabel.TRAILING), c);
			c.gridx++;
			add(wHour = new JLabel("-", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(kLbl = new JLabel("Kills: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(kTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(kHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(lLbl = new JLabel("Loots: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(lTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(lHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(dLbl = new JLabel("Deaths: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(dTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(dHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(hLbl = new JLabel("Honor Gained: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(hTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(hHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(bgWLbl = new JLabel("BGs Won: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(bgWTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(bgWHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(bgLLbl = new JLabel("BGs Lost: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(bgLTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(bgLHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(bgTLbl = new JLabel("BGs Completed: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(bgTTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(bgTHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(nLbl = new JLabel("Nodes Gathered: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(nTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(nHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(sLbl = new JLabel("Artifacts Solved: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(sTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(sHour = new JLabel("", JLabel.TRAILING), c);
			
			c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.BASELINE_LEADING;
			add(fLbl = new JLabel("Fish Fished: "), c);
			c.gridx++; c.anchor = GridBagConstraints.BASELINE_TRAILING;
			add(fTotal = new JLabel("", JLabel.TRAILING), c);
			c.gridx++;
			add(fHour = new JLabel("", JLabel.TRAILING), c);
		}
		
		@Override
		public int getBaseline(int width, int height) {
			return 1;
		}
	}
	
	private class MiscPanel extends JPanel {
		JLabel
			cLbl, aLbl, rLbl, actLbl, bLbl, gLbl, sLbl, pLbl, zLbl, szLbl, lLbl,
			cText, aText, rText, actText, bText, gText, sText, pText, zText, szText, lText;
		
		MiscPanel() {
			super(new MigLayout("", "[]0[]", ""));
			
			add(cLbl = new JLabel("Connected to Bot: "), "");
			add(cText = new JLabel(), "grow,wrap 0");
			
			add(aLbl = new JLabel("Bot Attached to WoW: "), "");
			add(aText = new JLabel(), "grow,wrap 0");
			
			add(rLbl = new JLabel("Bot is Running: "), "");
			add(rText = new JLabel(), "grow,wrap 0");
			
			add(actLbl = new JLabel("Account/Realm: "), "");
			add(actText = new JLabel(), "grow,wrap 0");
			
			add(bLbl = new JLabel("Bot: "), "");
			add(bText = new JLabel(), "grow,wrap 0");
			
			add(gLbl = new JLabel("Goal: "), "");
			add(gText = new JLabel(), "grow,wrap 0");
			
			add(sLbl = new JLabel("Status: "), "");
			add(sText = new JLabel(), "grow,wrap 0");
			
			add(pLbl = new JLabel("Profile: "), "");
			add(pText = new JLabel(), "grow,wrap 0");
			
			add(zLbl = new JLabel("Zone/Continent: "), "");
			add(zText = new JLabel(), "grow,wrap 0");
			
			add(szLbl = new JLabel("SubZone: "), "");
			add(szText = new JLabel(), "grow,wrap 0");
			
			add(lLbl = new JLabel("Location: "), "");
			add(lText = new JLabel(), "grow,wrap 0");
		}
		
		@Override
		public int getBaseline(int width, int height) {
			return 1;
		}
	}
}
