/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CGHPositionGraphCombinedHeader.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-03 14:36:29 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * @author Adam Margolin
 * @author Raktim Sinha
 *
 */
public class CGHPositionGraphCombinedHeader extends CGHPositionGraphHeader {


    /**
     * Constructs a <code>MultipleArrayHeader</code> with specified
     * insets and trace space.
     */
    public CGHPositionGraphCombinedHeader(Insets insets) {
        super(insets);
    }

    /**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (this.model == null || this.model.getNumExperiments() == 0) {
            return;
        }

        Graphics2D g = (Graphics2D)g1D;
        int width;
        if(!this.isTracing)
            width = this.model.getNumExperiments() * this.elementWidth;
        else
            width = (this.model.getNumExperiments() - 1) * (this.elementWidth + rectSpacing) + this.elementWidth ;
        //g.drawImage(this.negColorImage, insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        //g.drawImage(this.posColorImage, (int)(width/2f)+insets.left, 0, (int)(width/2.0), RECT_HEIGHT, null);
        //System.out.println("header draw image");
        //BufferedImage negColorImage = model.getNegColorImage();
        //int rgb = negColorImage.getRGB(255, 0);
        //System.out.println("RGB = " + rgb);

        width = model.getNumExperiments() * (elementWidth + rectSpacing);// + insets.left;

        g.drawImage(model.getNegColorImage(), insets.left, 0, (int)(width/2f), RECT_HEIGHT, null);
        g.drawImage(model.getPosColorImage(), (int)(width/2f)+insets.left, 0, (int)(width/2.0), RECT_HEIGHT, null);
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
        g.drawString(String.valueOf(this.minValue), insets.left, RECT_HEIGHT+fHeight);
        //g.drawString(String.valueOf(model.getMinRatioScale()), insets.left, RECT_HEIGHT+fHeight);

        textWidth = hfm.stringWidth("0:0");
        g.drawString("0:0", (int)(width/2f)-textWidth/2 + insets.left, RECT_HEIGHT+fHeight);

        textWidth = hfm.stringWidth(String.valueOf(this.maxValue));
        g.drawString(String.valueOf(this.maxValue), (width-textWidth)+insets.left, RECT_HEIGHT+fHeight);


        drawColumnHeaders(g);
    }

    /**
     * Draws microarrays names.
     */
    private void drawColumnHeaders(Graphics2D g) {
        final int size = model.getNumExperiments();
        if (size == 0) {
            return;
        }

        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        g.rotate(-Math.PI/2);
        String name;
        for (int feature = 0; feature < size; feature++) {
            name = model.getExperimentName(model.getExperimentIndexAt(feature));
            int xCoord = feature * (elementWidth + rectSpacing) + elementWidth / 2 + insets.left;
            xCoord += descent;
            //g.drawString(name, insets.bottom - getSize().height +5, insets.left + descent + (elementWidth+getSpacing())*feature + elementWidth/2);
            g.drawString(name, insets.bottom - getSize().height +5, xCoord);
        }
        g.rotate(Math.PI/2);
    }


}
