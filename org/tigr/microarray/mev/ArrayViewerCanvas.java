/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ArrayViewerCanvas.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:17 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.util.*;
import javax.swing.*;
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