package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
* This class implements a single text node to be displayed on the ChARM UI
 * display panel.
*
 * <p>Title: TextGraphNode</p>
 * <p>Description: This class implements a single text node to be displayed on the ChARM UI
 * display panel. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */
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
