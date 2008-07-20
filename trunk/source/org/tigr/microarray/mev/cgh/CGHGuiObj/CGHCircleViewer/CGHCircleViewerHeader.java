/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHPositionGraphSeparatedHeader.java
 *
 * Created on March 20, 2003, 1:26 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHCircleViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHCircleViewerModel;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHCircleViewerHeader extends JPanel{
    protected final int RECT_HEIGHT = 15;
    protected boolean isAntiAliasing = true;
    protected float maxValue;
    protected float minValue;

    CGHCircleViewerModel model;
    Insets insets;
    JPanel viewer;

    /** Creates a new instance of CGHPositionGraphSeparatedHeader */
    public CGHCircleViewerHeader(Insets insets, CGHCircleViewerModel model, JPanel viewer) {
        super();
        setBackground(Color.black);
        this.insets = insets;
        this.model = model;
        this.viewer = viewer;
        updateSize();
    }

    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (this.model == null){
            return;
        }
        int viewerWidth = Math.min(viewer.getWidth(), viewer.getHeight());
        Graphics2D g = (Graphics2D)g1D;
        int width;
        width = viewerWidth - (insets.left + insets.right) ;

        g.drawImage(model.getNegColorImage(), viewer.getWidth() / 2 - width / 2, 0, (int)(width/2f), RECT_HEIGHT, null);
        g.drawImage(model.getPosColorImage(), viewer.getWidth() / 2, 0, (int)(width/2.0), RECT_HEIGHT, null);

        //g.drawImage(model.getNegColorImage(), viewerWidth / 2 - width / 2, 0, (int)(width/2f), RECT_HEIGHT, null);
        //g.drawImage(model.getPosColorImage(), viewerWidth / 2, 0, (int)(width/2.0), RECT_HEIGHT, null);

        //g.drawImage(model.getNegColorImage(), insets.left, 0, (int)(width), RECT_HEIGHT, null);
        //g.drawImage(model.getPosColorImage(), (int)(width/2f)+insets.left, 0, (int)(width), RECT_HEIGHT, null);

        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        int fHeight = hfm.getHeight();

        g.setColor(Color.white);
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        int textWidth;
        //g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
        g.drawString(String.valueOf(this.minValue), (viewer.getWidth() / 2) - (width / 2), RECT_HEIGHT+fHeight);
        //g.drawString(String.valueOf(model.getMinRatioScale()), insets.left, RECT_HEIGHT+fHeight);

        textWidth = hfm.stringWidth("0:0");
        //g.drawString("0:0", (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);
        g.drawString("0:0", (int)(viewer.getWidth()/2) - (textWidth/2), RECT_HEIGHT+fHeight);

        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        //g.drawString(String.valueOf(this.maxValue), (viewerWidth - insets.right - insets.left -textWidth), RECT_HEIGHT+fHeight);
        g.drawString(String.valueOf(this.maxValue), (viewer.getWidth() / 2) + (width / 2) - textWidth, RECT_HEIGHT+fHeight);

    }

    void updateSize() {
        Graphics2D g = (Graphics2D)getGraphics();
        if (g == null) {
            return;
        }
        if (isAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        FontMetrics hfm = g.getFontMetrics();

        setSize(2, 10+this.RECT_HEIGHT+hfm.getHeight());
        setPreferredSize(new Dimension(2, 10+this.RECT_HEIGHT+hfm.getHeight()));
        //setSize(getWidth(), 10+this.RECT_HEIGHT+hfm.getHeight());
        //setPreferredSize(new Dimension(getWidth(), 10+this.RECT_HEIGHT+hfm.getHeight()));
    }

    /**
     * Sets min and max ratio values
     */
    public void setMinAndMaxRatios(float min, float max){
        this.minValue = min;
        this.maxValue = max;
        this.repaint();
    }

    public void onMenuChanged(IDisplayMenu menu) {

        model.setNegColorImage(menu.getNegativeGradientImage());
        model.setPosColorImage(menu.getPositiveGradientImage());

        setMinAndMaxRatios(menu.getMinRatioScale(), menu.getMaxRatioScale());
        updateSize();
    }
}
