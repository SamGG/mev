package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;
/**
 * @author  Raktim Sinha
 */
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
public class TextGraphNode extends PText {

  private static final double[] SCALE_MAPPING = {.125,.25,.5,1,2,4,6};
  private static final double[] FONT_MAPPING = {96,48,24,12,6,3,1.5};
  private String text;
  private double endXPosition;

  /**
   * Class constructor.
   * @param text String- text to be displayed at this node
   * @param endPos double- x coordinate of the edge of this node
   */
  public TextGraphNode(String text, double endPos) {
    super(text);
    this.text = text;
    this.endXPosition = endPos;
  }

  public void paint(PPaintContext aPaintContext) {
          double scale = aPaintContext.getScale();

          double fontSize=0;
          int index=0;
          while(scale >= SCALE_MAPPING[index++] && index < SCALE_MAPPING.length);
          fontSize = FONT_MAPPING[index-1];
          this.setFont(new Font("myfont", Font.BOLD, (int)fontSize));

          Rectangle2D bounds = this.getGlobalFullBounds().getBounds2D();
          this.translate(this.endXPosition-(bounds.getX()+bounds.getWidth()),0);


          super.paint(aPaintContext);
  }

}
