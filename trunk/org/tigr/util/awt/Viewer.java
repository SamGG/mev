/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Viewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:26:21 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public abstract class Viewer extends JPanel implements Serializable, Printable {
    public static final long serialVersionUID = 100010301010001L;
    
    protected JFrame frame;
    protected Vector palette;
    protected int xOldEvent;
    protected int yOldEvent;
    public boolean mouseInside = false;
    
    public Viewer(JFrame frame) {
	this.frame = frame;
    }
    
    public Viewer() {}
    
    //Should override this method in subclasses
    public int print(Graphics g, PageFormat format, int pagenum) {return 0;}
    
    public void setVisible(boolean state){
	super.setVisible(state);
	if (frame != null) frame.setVisible(state);
    }
    
    public boolean hasFrame() {
	if (frame != null) return true;
	else return false;
    }
    
    public JFrame getFrame() {return this.frame;}
    public void setCursor(int cursor) {setCursor(Cursor.getPredefinedCursor(cursor));}
    public void setXOldEvent(int xEvent) {this.xOldEvent = xEvent;}
    public int getXOldEvent() {return this.xOldEvent;}
    public void setYOldEvent(int yEvent) {this.yOldEvent = yEvent;}
    public int getYOldEvent() {return this.yOldEvent;}
    
    //Creates the pseudo-false color palette
    public Vector buildPalette() {
	palette = new Vector(256);
	Color newColor;
	double r, g, b;
	
	newColor = new Color(0, 0, 0);
	palette.addElement(newColor);
	
	for (int i = 1; i < 256; i++) {
	    i = 255 - i;
	    
	    r = 0; g = 0; b = 0;
	    
	    if (i < 33) r = 255;
	    else if (i > 32 && i < 108) r = Math.abs( 255 * Math.cos((i - 32) * Math.PI / 151));
	    else if (i > 107) r = 0;
	    
	    if (i < 5) g = 0;
	    else if (i > 4 && i < 101) g = Math.abs((255 * Math.cos((i - 100) * Math.PI / 189)));
	    else if (i > 100 && i < 229) g = Math.abs((255 * Math.cos((i - 100) * Math.PI / 294)));
	    else if (i > 230) g = 0;
	    
	    if (i < 72) b = 0;
	    else if (i > 71 && i < 200) b = Math.abs((255 * Math.cos((i - 199) * Math.PI / 256)));
	    else if (i > 199) b = Math.abs((255 * Math.cos((i - 199) * Math.PI / 175)));
	    
	    newColor = new Color((float) r / 255, (float) g / 255, (float) b / 255);
	    palette.addElement(newColor);
	    
	    i = 255 - i;
	}
	
	return palette;
    }
}