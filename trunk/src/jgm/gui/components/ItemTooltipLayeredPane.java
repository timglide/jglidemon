package jgm.gui.components;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLayeredPane;

import jgm.wow.ItemSet;

public class ItemTooltipLayeredPane extends JLayeredPane implements MouseMotionListener {
	private ItemTooltip	tooltip;

	public ItemTooltipLayeredPane(ItemTooltip tooltip) {
		this.tooltip = tooltip;
		add(tooltip, POPUP_LAYER);
		setLayout(new BorderLayout());
		addMouseMotionListener(this);
	}
	
	public ItemSet getItemSet() {
		if (!tooltip.isVisible())
			return null;
		return tooltip.getItemSet();
	}
	
	public void setItemSet(ItemSet i) {
		if (null != i) {
			tooltip.setItemSet(i);
			tooltip.revalidate();
			moveTooltip();
			tooltip.setVisible(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			tooltip.setVisible(false);
			tooltip.revalidate();
			setCursor(null);
		}
	}

	public void moveTooltip() {
		Point p = getMousePosition();
		
		if (null == p) return;
		
		int panelW = getWidth();
		int panelH = getHeight();
		int ttW    = tooltip.getWidth();
		int ttH    = tooltip.getHeight();
		
		// ensure tooltip is drawn within the window
		if (p.x + ttW >= panelW - 10)
			p.x = panelW - 10 - ttW;
		if (p.y + ttH >= panelH)
			p.y = panelH - ttH;
		
//		System.out.println("** Setting location to " + (p.x + 10) + "," + p.y);
		tooltip.setLocation(p.x + 10, p.y);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
        if (!tooltip.isVisible())
        	return;
        moveTooltip();
	}
}
