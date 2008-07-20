package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PLine;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class GeneGraphNode extends PNode {
  private boolean isSelected = false;
  private boolean isChanged = false;
  private boolean mouseOver = false;

  private static float axisScale=1;
  private static float guiScaleFactor=1;

  private int geneIndex=0;
  private float geneValue=0;

  private PLine geneLine;
  private PPath mouseOverCircle;

  private float lastDx;
  private float lastDy;

  /**
   * Class constructor.
   * @param index int- index of this gene
   * @param value double- gene value (log-ratio)
   * @param tooltip String- tooltip associated with this gene (ID)
   */
  public GeneGraphNode(int index, double value, double scaleFactor, String tooltip) {
    super();

    geneIndex = index;
    geneValue = (float)value;

    geneLine = new PLine();
    mouseOverCircle = new PPath();

    this.addChild(geneLine);
    this.addChild(mouseOverCircle);
    this.addClientProperty("tooltip", tooltip);

    lastDx=0;
    lastDy=0;

    guiScaleFactor = (float)scaleFactor;

    isChanged = true;
    render();
  }

  /**
   * Indicates if state has changed since last rendering.
   * @return boolean
   */
  public boolean isChanged() {
    return isChanged;
  }

  /**
   * Sets changed status.
   * @param change boolean
   */
  public void setChanged(boolean change) {
    isChanged = change;
  }

  /**
   * Sets mouse over status variable.
   * @param mouse boolean
   */
  public void setMouseOver(boolean mouse) {
    mouseOver = mouse;
    isChanged = true;
  }

  /**
   * Draws/updates gene node on display.
   */
  public void render() {
    Color geneColor=null;

    if(isChanged) {
       if(mouseOver) {
        mouseOverCircle.setPathToEllipse((float)-2.0,(float)(-Math.min(geneValue*axisScale,guiScaleFactor)-2.0),(float)4.0,(float)4.0);
        mouseOverCircle.setStrokePaint(GraphProperties.MOUSE_OVER_COLOR);
        mouseOverCircle.setPaint(Color.BLACK);
        mouseOverCircle.setTransparency(1);
        this.addChild(mouseOverCircle);
      }
      else  {
        mouseOverCircle.setPathToRectangle(0, 0, 0, 0);
        mouseOverCircle.setTransparency(1);
        }

      this.removeChild(geneLine);
      geneLine = new PLine();
      if (Double.isNaN(geneValue)) {
        geneColor = GraphProperties.NAN_COLOR;
        geneLine.setStrokePaint(geneColor);
        geneLine.addPoint(0, 0, -.01);
        geneLine.addPoint(1, 0, .01);
      }
      else if (this.isSelected()) {
        if (geneValue >= 0) geneColor = GraphProperties.SELECTED_UP_COLOR;
        else geneColor = GraphProperties.SELECTED_DOWN_COLOR;
        geneLine.setStrokePaint(geneColor);
        geneLine.addPoint(0, 0, 0);
        geneLine.addPoint(1, 0, -Math.min(geneValue*axisScale,guiScaleFactor));
      }
      else {
        if (geneValue >= 0) geneColor = GraphProperties.UP_COLOR;
        else geneColor = GraphProperties.DOWN_COLOR;
        geneLine.setStrokePaint(geneColor);
        geneLine.addPoint(0, 0, 0);
        geneLine.addPoint(1, 0, -Math.min(geneValue*axisScale,guiScaleFactor));
      }
      this.addChild(geneLine);

      isChanged=false;
    }

  }

  /**
   * Indicates if this prediction node is currently selected.
   * @return boolean
   */
  public boolean isSelected() {
   return isSelected;
 }

 /**
  * Sets the selected status.
  * @param select boolean
  */
 public void setSelected(boolean select) {
   isSelected = select;
 }

 /**
  * Sets the index of this gene.
  * @param index int
  */
 public void setGeneIndex(int index) {
    geneIndex = index;
  }

  /**
   * Returns the index of this gene.
   * @return int
   */
  public int getGeneIndex() {
    return geneIndex;
  }

  /**
   * Sets the value associated with this gene.
   * @param val double
   */
  public void setGeneValue(double val) {
    geneValue = (float)val;
  }

  /**
   * Returns the value associated with this gene.
   * @return double
   */
  public double getGeneValue() {
    return geneValue;
  }

  /**
   * Sets the current y-axis scale.
   * @param scale double
   */
  public void setAxisScale(double scale) {
    axisScale = (float)scale;
  }

  /**
   * Required of type PNode.  Moves this prediction node by specified x and y amounts.
   * @param dx double
   * @param dy double
   */
  public void translate(double dx, double dy) {
    super.translate(dx,dy);
    lastDx=(float)dx;
    lastDy=(float)dy;
}

  /**
   * Reverses previous translation.
   */
  public void reverseLastTranslation() {
  this.translate(-lastDx,-lastDy);
}

}
