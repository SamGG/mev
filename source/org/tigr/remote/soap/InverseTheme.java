/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.remote.soap;


import javax.swing.plaf.metal.*;
import javax.swing.plaf.ColorUIResource;

public class InverseTheme extends DefaultMetalTheme {

    protected ColorUIResource getPrimary1() {
        return new ColorUIResource(255,255,255);
    }
    protected ColorUIResource getPrimary2() {
        return new ColorUIResource(0,0,0);
    }
    protected ColorUIResource getPrimary3() {
        return new ColorUIResource(255,255,255);
    }

    // component borders
    protected ColorUIResource getSecondary1() {
        return new ColorUIResource(0,0,0);
    }
    // selected components (button down state)
    protected ColorUIResource getSecondary2() {
        return new ColorUIResource(0,0,0);
    }
    // component backgrounds
    protected ColorUIResource getSecondary3() {
        return new ColorUIResource(255,255,255);
    }



    //for label text
    public ColorUIResource getSystemTextColor() {
        return new ColorUIResource(0,0,0);
    }

    // background of selected text
    public ColorUIResource getTextHighlightColor() {
        return new ColorUIResource(0,0,0);
    }

    // foreground of selected text
    public ColorUIResource getHighlightedTextColor() {
        return new ColorUIResource(255,255,255);
    }



    public ColorUIResource getMenuBackground() {
        return new ColorUIResource(255,255,255);
    }
    public ColorUIResource getMenuForeground() {
        return new ColorUIResource(0,0,0);
    }

    public ColorUIResource getMenuSelectedBackground() {
        return new ColorUIResource(0,0,0);
    }
    public ColorUIResource getMenuSelectedForeground() {
        return new ColorUIResource(255,255,255);
    }

}
