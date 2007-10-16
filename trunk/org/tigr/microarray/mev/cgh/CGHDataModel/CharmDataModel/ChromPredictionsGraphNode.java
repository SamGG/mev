package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.PredictionGraphNode;

import edu.umd.cs.piccolo.PNode;
/**
* This class implements a single chromosome and associated prediction
 * rectangles to be displayed on the ChARM UI display panel.
*
 * <p>Title: ChromPredictionsGraphNode</p>
 * <p>Description: This class implements a single chromosome and associated prediction
 * rectangles to be displayed on the ChARM UI display panel.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

public class ChromPredictionsGraphNode extends PNode implements IChromGraphNode {

private int chromNum;
private String experiment;
private String resultSet;
private int type;

private double lastDx;
private double lastDy;

private double chromWidth;

private ArrayList predNodes;

  /**
   * Class constructor.
   * @param chrom int- the chromosome number associated with this node
   * @param exp String- the experiment name associated with this node
   * @param resultSet String- corresponding prediction result set
   */
  public ChromPredictionsGraphNode(int chrom,String exp,String resultSet) {
    super();
    lastDx =0;
    lastDy=0;

    chromNum = chrom;
    experiment = exp;
    this.resultSet = resultSet;
    type = IChromGraphNode.TYPE_PREDICTIONS;
    predNodes = new ArrayList();
}

  /**
   * Required of type PNode.  Moves this node by amount specified in x and y directions.
   * @param dx double
   * @param dy double
   */
  public void translate(double dx, double dy) {
    super.translate(dx,dy);
    lastDx=dx;
    lastDy=dy;
  }

  /**
   * Reverses the previous translation.
   */
  public void reverseLastTranslation() {
    this.translate(-lastDx,-lastDy);
  }

  /**
   * Adds a window (prediction) to this node spanning from startx to endx coordinates.
   * @param currwindow Window
   * @param startx double
   * @param endx double
   */
  public void addPrediction (SegmentInfo currSeg, double startx, double endx) {
    double sign=1;
    if(currSeg.getDataMean() < 0) sign = -1;

    PredictionGraphNode predNode = new PredictionGraphNode(startx, endx, sign, currSeg.getType());
    predNodes.add(predNode);
    this.addChild(predNode);
  }

  /**
   * Draws/updates this node on the display.
   */
  public void render() {
    PredictionGraphNode currPred;

    for (int i = 0; i < predNodes.size(); i++){
      currPred = (PredictionGraphNode)predNodes.get(i);
      currPred.render();
      }
    }

    /**
     * Results in changing the selected status of all contained predictions within the specified
     * rectangle to <code>true</code>.
     * @param selectionRect Rectangle2D
     * @return ArrayList
     */
    public ArrayList selectChildren(Rectangle2D selectionRect) {
    ArrayList selectedIndices = new ArrayList();
    PredictionGraphNode currNode;

    for(int i=0; i<predNodes.size(); i++) {
      currNode = (PredictionGraphNode)predNodes.get(i);
      if(currNode.getGlobalFullBounds().intersects(selectionRect)) {
        currNode.setSelected(true);
        currNode.setChanged(true);
        currNode.render();
        selectedIndices.add(new Integer(i));
      }
    }
    return selectedIndices;
  }



   /**
    * Results in changing the selected status of all contained predictions within the specified
    * rectangle to <code>true</code>.
    *
    * @param unselectIndices ArrayList
    */
   public void unselectChildren(ArrayList unselectIndices) {
    for(int i=0; i<unselectIndices.size(); i++) {
      PredictionGraphNode currPred = (PredictionGraphNode)predNodes.get(((Integer)unselectIndices.get(i)).intValue());
      currPred.setSelected(false);
      currPred.setChanged(true);
      currPred.render();
    }
  }

  /**
   * Returns the width of this node.
   * @return double
   */
  public double getChromWidth() {
      return chromWidth;
    }

    /**
     * Sets the width of this node.
     * @param width double
     */
    public void setChromWidth(double width) {
      chromWidth = width;
    }

    /**
     * Returns the chromosome number associated with this node.
     * @return int
     */
    public int getChromosomeNumber() {
      return chromNum;
    }

    /**
     * Returns the experiment associated with this node.
     * @return String
     */
    public String getExperiment() {
      return experiment;
    }

    /**
     * Sets node type.
     * @param type int
     */
    public void setType(int type) {
      this.type = type;
    }

    /**
     * Returns node type.
     * @return int
     */
    public int getType() {
      return type;
    }

    /**
     * Returns the ResultContainer associated with this node.
     * @return String
     */
    public String getResultSet() {
      return resultSet;
    }

}
