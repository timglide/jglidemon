package jgm.misc.wowwatermark;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;

import javax.imageio.ImageIO;

public class WMTest {
	public static void main(String[] args) throws Exception {
		new WMTest();
	}
	
	Kernel kernel;
	BufferedImage orig, filtered;
	
	WMTest() throws Exception {
		initKernel();
		orig = ImageIO.read(new File("."));
		filtered = new BufferedImage(orig.getWidth(), orig.getHeight(), BufferedImage.TYPE_INT_RGB);
		doSharpen();
	}
	
	private static void normalizeKernel(float[] kernel) {
		double n = 0;
		
		for (int i = 0; i < kernel.length; i++) {
			n += kernel[i];
			
			for (int j = 0; j < kernel.length; j++) {
				kernel[j] /= n;
			}
		}
	}
	
	private void initKernel() {
		float[] kernel = {
			0f, -1f, 0f,
			-1f, 5f, -1f,
			0f, -1f, 0f
		};
//		float[] kernel = {
//			.25f, -2, .25f,
//	        -2, 10,  -2,
//	        .25f, -2, .25f};
		
//		normalizeKernel(kernel);
		
		this.kernel = new Kernel(3, 3, kernel);
	}
	
	private void doSharpen()
	{
		BufferedImageOp sharpen = new ConvolveOp(kernel);
		sharpen.filter(orig, filtered);
	}
}
