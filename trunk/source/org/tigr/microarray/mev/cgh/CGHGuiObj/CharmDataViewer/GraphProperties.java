package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
/**
* This is a container class for the default display settings.
*
 * <p>Title: GraphProperties</p>
 * <p>Description: This is a container class for the default display settings.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

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
