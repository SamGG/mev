package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;
/**
 * @author  Raktim Sinha
 */
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
* This interface defines the structure for a single data or prediction display chromosome.
*
 * <p>Title: ChromGraphNode </p>
 * <p>Description: This interface defines the structure for a single data or prediction display chromosome.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public interface IChromGraphNode {
  public static final int TYPE_DATA=1;
  public static final int TYPE_PREDICTIONS=2;

  public void translate(double dx, double dy);
  public void reverseLastTranslation();
  public void render();
  public ArrayList selectChildren(Rectangle2D selectionRect);
  public void unselectChildren(ArrayList unselectList);
  public int getType();
}
