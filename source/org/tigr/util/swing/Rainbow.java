/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Rainbow.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:27:48 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
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