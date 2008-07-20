/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ArrayViewerCanvas.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:44:16 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import org.tigr.util.awt.Drawable;

public abstract class ArrayViewerCanvas extends Drawable {
    public ArrayViewerCanvas(int preXSpacing, int postXSpacing, int preYSpacing, int postYSpacing) {
	super(0, preXSpacing + postXSpacing, 0, preYSpacing + postYSpacing);
    }
    
    protected String space(int val, int mark) {
	String retString = "";
	for (int i = 1; i < (mark - val + 3); i++) retString += " ";
	return retString;
    }
}
