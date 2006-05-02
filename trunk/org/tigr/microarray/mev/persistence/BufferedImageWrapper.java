
package org.tigr.microarray.mev.persistence;

import java.awt.image.BufferedImage;


/**
 * This is a stupid hack to get past a Java bug in saving images.
 * @author eleanora
 *
 */
public class BufferedImageWrapper {
	private BufferedImage bi;
	
	public BufferedImageWrapper(BufferedImage bi){this.bi = bi;}
	
	public BufferedImage getBufferedImage(){return bi;}

}










































 