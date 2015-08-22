package jgm.gui.components;

import java.awt.Color;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.border.AbstractBorder;

public class ImageBorder extends AbstractBorder {
	private static final Anchor[] ANCHORS = Anchor.values();
	
	private enum Anchor {
		TL, TC, TR,
		ML, MC,  MR,
		BL, BC, BR;

		public int getSrcWidth(ImageBorder b) {
			switch (this) {
				case TC:
				case MC:
				case BC: return 1;
				default: return b.cd.width;
			}
		}
		
		public int getSrcHeight(ImageBorder b) {
			switch (this) {
				case ML:
				case MC:
				case MR: return 1;
				default: return b.cd.height;
			}
		}
		
		public int getSrcX(ImageBorder b) {
			switch (this) {
				case TL:
				case ML:
				case BL: return 0;
				
				case TC:
				case MC:
				case BC: return b.cd.width;
				
				default: return b.cd.width + 1;
			}
		}
		
		public int getSrcY(ImageBorder b) {
			switch (this) {
				case TL:
				case TC:
				case TR: return 0;
				
				case ML:
				case MC:
				case MR: return b.cd.height;
				
				default: return b.cd.height + 1;
			}
		}
		
		public int getWidth(ImageBorder b, int width) {
			switch (this) {
				case TL:
				case ML:
				case BL:
				case TR:
				case MR:
				case BR: return b.cd.width;
				
				case TC:
				case MC:
				case BC: return width - (2 * b.cd.width);
				
			}
			
			throw new AssertionError();
		}

		public int getHeight(ImageBorder b, int height) {
			switch (this) {
				case TL:
				case TC:
				case TR:
				case BL:
				case BC:
				case BR: return b.cd.height;
				
				case ML:
				case MC:
				case MR: return height - (2 * b.cd.height);
				
			}
			
			throw new AssertionError();
		}
		
		public int getX(ImageBorder b, int x, int width) {
			switch (this) {
				case TL:
				case ML:
				case BL: return x;
				
				case TC:
				case MC:
				case BC: return x + b.cd.width;
				
				case TR:
				case MR:
				case BR: return x + (width - b.cd.width);
			}
			
			throw new AssertionError();
		}

		public int getY(ImageBorder b, int y, int height) {
			switch (this) {
				case TL:
				case TC:
				case TR: return y;
				
				case ML:
				case MC:
				case MR: return y + b.cd.height;
				
				case BL:
				case BC:
				case BR: return y + (height - b.cd.height);
			}
			
			throw new AssertionError();
		}
	}
	
	private BufferedImage img;
	private Dimension cd;

	public ImageBorder(BufferedImage img) {
		if (img.getWidth() % 2 == 0 || img.getHeight() % 2 == 0)
			throw new IllegalArgumentException("image must have odd width and height");
		if (img.getWidth() < 3 || img.getHeight() < 3)
			throw new IllegalArgumentException("image width and height must be at least 3 pixels");
		
		this.img = img;
		
		int halfWidth = img.getWidth() / 2;
		int halfHeight = img.getHeight() / 2;
		cd = new Dimension(halfWidth, halfHeight);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		if (null == insets)
			insets = new Insets(0, 0, 0, 0);
		insets.set(cd.height / 2, cd.width, cd.height / 2, cd.width);
		return insets;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {		
		/* x      x+cd.w     x+cd.w+cw     x+cd.w+cw+cd.w
		 *   cd.w      cw      cd.w
		 * +------+----------+------+         y
		 * | cd.w |    cw    | cw.w | cd.h
		 * +------+----------+------+         y+cd.h
		 * |      |          |      |
		 * |      |          |      | ch
		 * |      |          |      |
		 * +------+----------+------+         y+cd.h+ch
		 * |      |          |      | cd.h
		 * +------+----------+------+         y+cd.h+ch+cd.h
		 * 
		 */
		
//		System.out.println();
		for (Anchor a : ANCHORS) {
			draw(g, a, x, y, width, height);
		}
	}
	
	private void draw(Graphics g, Anchor a, int x, int y, int width, int height) {
		int ax = a.getX(this, x, width);
		int ay = a.getY(this, y, height);
		int aw = a.getWidth(this, width);
		int ah = a.getHeight(this, height);
		
		int sx = a.getSrcX(this);
		int sy = a.getSrcY(this);
		int sw = a.getSrcWidth(this);
		int sh = a.getSrcHeight(this);
		
//		System.out.printf("%s: x/y/w/h / ax/ay/aw/ay / sx/sy/sw/sh: %s/%s/%s/%s / %s/%s/%s/%s / %s/%s/%s/%s%n",
//				a,
//				x, y, width, height,
//				ax, ay, aw, ah,
//				sx, sy, sw, sh);
		
		g.drawImage(img,
				ax, ay, ax + aw, ay + ah,
				sx, sy, sx + sw, sy + sh, null);
	}
}
