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

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphSeparatedHeader extends CGHPositionGraphHeader {

    CGHPositionGraphSeparatedViewer viewer;

    /** Creates a new instance of CGHPositionGraphSeparatedHeader */
    public CGHPositionGraphSeparatedHeader(Insets insets, CGHPositionGraphSeparatedViewer viewer) {
        super(insets);
        this.viewer = viewer;
    }

    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (this.model == null || this.model.getNumExperiments() == 0) {
            return;
        }

        Graphics2D g = (Graphics2D)g1D;
        int width;
        width = model.getNumExperiments() * (elementWidth + rectSpacing) + elementWidth / 2 + insets.left;
        width *= 2;
        width += viewer.getCytoBandsCanvas().getWidth();

        g.drawImage(model.getNegColorImage(), viewer.getWidth() / 2 - (width / 2) , 0, (int)(width/2f), RECT_HEIGHT, null);
        g.drawImage(model.getPosColorImage(), viewer.getWidth() / 2, 0, (int)(width/2.0), RECT_HEIGHT, null);

        //g.drawImage(model.getNegColorImage(), insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        //g.drawImage(model.getPosColorImage(), (int)(width/2f)+insets.left, 0, (int)(width/2.0), RECT_HEIGHT, null);


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
        //int textWidth;
        int textWidth = hfm.stringWidth(String.valueOf(model.getMinRatioScale()));
        //g.drawString(String.valueOf(this.minValue), (viewer.getWidth() / 2) - (width / 2) - textWidth, RECT_HEIGHT+fHeight);
        g.drawString(String.valueOf(model.getMinRatioScale()), (viewer.getWidth() / 2) - (width / 2) - textWidth, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth("0:0");
        g.drawString("0:0", viewer.getWidth() / 2, RECT_HEIGHT+fHeight);
        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        //g.drawString(String.valueOf(this.maxValue), (viewer.getWidth() / 2) + (width / 2) , RECT_HEIGHT+fHeight);
        g.drawString(String.valueOf(model.getMaxRatioScale()), (viewer.getWidth() / 2) + (width / 2) , RECT_HEIGHT+fHeight);

        drawColumnHeaders(g);
    }

    private void drawColumnHeaders(Graphics2D g) {
        //final int size = data.getFeaturesCount();
        final int numExperiments = model.getNumExperiments();
        int rectSpacing = 5;

        if (numExperiments == 0) {
            return;
        }
        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        g.rotate(-Math.PI/2);
        String name;

        for (int exp = 0; exp < numExperiments; exp++) {
            name = model.getExperimentName(model.getExperimentIndexAt(exp));

            int xCoord = exp * (elementWidth + rectSpacing) + elementWidth / 2 + insets.left;
            //xCoord += leftPanelSize.width + cytoPanelSize.width;
            xCoord += viewer.getPositionGraphLeft().getWidth() + viewer.getCytoBandsCanvas().getWidth();
            g.drawString(name, insets.bottom - getSize().height +5, xCoord);

            //g.drawString(name, insets.bottom - getSize().height +5, insets.left + descent + (elementWidth+getSpacing())*feature + elementWidth/2);
        }

        for (int exp = 0; exp < numExperiments; exp++) {
            name = model.getExperimentName(model.getExperimentIndexAt(exp));

            //int xCoord = exp * (elementWidth + rectSpacing) + elementWidth / 2 + insets.left;
            int xCoord = (model.getNumExperiments() - exp - 1) * (elementWidth + rectSpacing) + elementWidth / 2 + insets.left;
            xCoord = viewer.getPositionGraphLeft().getWidth() - xCoord;
            //int xCoord = viewer.getPositionGraphLeft().getWidth() - ( (model.getNumExperiments() - exp - 1)* (elementWidth + rectSpacing) ) - insets.right;
            g.drawString(name, insets.bottom - getSize().height +5, xCoord);

            //g.drawString(name, insets.bottom - getSize().height +5, insets.left + descent + (elementWidth+getSpacing())*feature + elementWidth/2);
        }

        g.rotate(Math.PI/2);
    }

}
