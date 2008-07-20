/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

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










































 