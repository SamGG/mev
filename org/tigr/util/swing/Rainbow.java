/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Rainbow.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.io.*;
import java.awt.*;
import java.util.Vector;

public class Rainbow {
    
    public void drawRainbow(Graphics2D g2, int x, int y, int Width, int Height) {
	double Step=Width/5.0;
	Vector Colors=new Vector();
	Colors.add(new Color(255,000,000));
	Colors.add(new Color(255,255,000));
	Colors.add(new Color(000,255,000));
	Colors.add(new Color(000,255,255));
	Colors.add(new Color(000,000,255));
	Colors.add(new Color(255,000,255));
	GradientPaint gp;
	for (int i=0; i<5; i++) {
	    gp=new GradientPaint(x+(int)Math.round(i*Step),0,(Color)Colors.get(i),x+(int)Math.round((i+1)*Step),0,(Color)Colors.get(i+1));
	    g2.setPaint(gp);
	    g2.drawRect(x+(int)Math.round(i*Step),y,(int)Step,Height);
	    g2.fillRect(x+(int)Math.round(i*Step),y,(int)Step,Height);
	}
    }
    
}