/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ArrayViewerCanvas.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:45 $
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