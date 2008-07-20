package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;
/**
 * @author  Raktim Sinha
 */
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

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
