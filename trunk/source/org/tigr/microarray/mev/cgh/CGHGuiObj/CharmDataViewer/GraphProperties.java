package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class GraphProperties {
  /**
   * Color of selected genes with values < 0.
   */
  public static final Color SELECTED_DOWN_COLOR = Color.YELLOW;
  /**
   * Color of selected genes with values > 0.
   */
 public static final Color SELECTED_UP_COLOR = Color.MAGENTA;
 /**
   * Color of non-selected genes with values < 0.
   */
  public static final Color DOWN_COLOR = Color.GREEN;
  /**
    * Color of non-selected genes with values > 0.
    */
  public static final Color UP_COLOR = Color.RED;
  /**
  * Color of NaN genes.
  */
  public static final Color NAN_COLOR = Color.GRAY;
  /**
  * Color of mouse-over text.
  */
  public static final Color MOUSE_OVER_COLOR = Color.YELLOW;

  public GraphProperties() {
  }

}
