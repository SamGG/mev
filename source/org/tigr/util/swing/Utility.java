/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Utility.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.Vector;

public class Utility {
    
    public Utility() {
    }
    
    static public void drawRainbow(Graphics2D g2, int x, int y, int Width, int Height, int Mode) {
	Vector Colors=new Vector();
	GradientPaint gp;
	switch (Mode) {
	    case 1: Colors.add(new Color(255,000,000));
	    Colors.add(new Color(255,255,000));
	    Colors.add(new Color(000,255,000));
	    Colors.add(new Color(000,255,255));
	    Colors.add(new Color(000,000,255));
	    Colors.add(new Color(255,000,255));
	    break;
	    case 2: Colors.add(new Color(255,000,000));
	    Colors.add(new Color(255,255,000));
	    Colors.add(new Color(000,000,128));
	    break;
	    case 3: Colors.add(new Color(255,000,255));
	    Colors.add(new Color(000,000,255));
	    Colors.add(new Color(000,255,255));
	    Colors.add(new Color(000,255,000));
	    Colors.add(new Color(255,255,000));
	    Colors.add(new Color(255,000,000));
	    break;
	    
	}
	double Step=Width/(double)(Colors.size()-1);
	for (int i=0; i<(Colors.size()-1); i++) {
	    gp=new GradientPaint(x+(int)Math.round(i*Step),0,(Color)Colors.get(i),x+(int)Math.round((i+1)*Step),0,(Color)Colors.get(i+1));
	    g2.setPaint(gp);
	    g2.drawRect(x+(int)Math.round(i*Step),y,(int)Step,Height);
	    g2.fillRect(x+(int)Math.round(i*Step),y,(int)Step,Height);
	}
    }
}
