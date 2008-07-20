package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.GeneGraphNode;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.nodes.PLine;
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class ChromDataGraphNode extends PNode implements IChromGraphNode {

  /**
   * Color of chromosome axis.
   */
  private static final Color AXIS_COLOR = Color.BLACK;

  private int chromNum;
  private String experiment;
  private int type;

  private double lastDx;
  private double lastDy;

  private double yAxisScale=1;

  private ArrayList geneNodes;
  private PLine axisLine;

  private float chromWidth;

  /**
   * Class constructor.
   * @param chrom int- chromosome number
   * @param exp String- experiment associated with this node
   */
  public ChromDataGraphNode(int chrom, String exp) {
    super();
    lastDx =0;
    lastDy=0;
    geneNodes = new ArrayList();

    axisLine = new PLine();

    chromNum = chrom;
    experiment = exp;
    type = IChromGraphNode.TYPE_DATA;
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
   * Adds gene nodes as children of this node.
   * @param geneData double[]- gene values of nodes to be added.
   * @param scaleFactor double- global scale factor
   * @param yAxisScale double- y-axis scale factor for gene values
   * @param geneNames ArrayList- gene IDs (needed for tooltips)
   */
  /*
  public void addGeneNodes(double[] geneData, double scaleFactor, double yAxisScale, ArrayList geneNames) {
    double currx=0;
    int dataLength = geneData.length;

    GeneGraphNode currNode = null;
    for(int i=0; i<geneData.length; i++) {
      currNode = new GeneGraphNode(i,geneData[i]*scaleFactor,scaleFactor,(String)geneNames.get(i));
      currNode.setAxisScale(yAxisScale);
      currNode.setChanged(true);
      currx = ((double)i/(double)dataLength)*chromWidth;
      currNode.translate(currx, 0);
      geneNodes.add(currNode);
      this.addChild(currNode);
    }
  }
*/
  /**
   * Adds gene nodes as children of this node.
   * @param geneData double[]- gene values of nodes to be added.
   * @param scaleFactor double- global scale factor
   * @param yAxisScale double- y-axis scale factor for gene values
   * @param geneNames ArrayList- gene IDs (needed for tooltips)
   */
  public void addGeneNodes(float[] geneData, float scaleFactor,float yAxisScale, ArrayList geneNames) {
    float currx=0;
    int dataLength = geneData.length;

    GeneGraphNode currNode = null;
    for(int i=0; i<geneData.length; i++) {
      currNode = new GeneGraphNode(i,geneData[i]*scaleFactor,scaleFactor,(String)geneNames.get(i));
      currNode.setAxisScale(yAxisScale);
      currNode.setChanged(true);
      currx = ((float)i/(float)dataLength)*chromWidth;
      currNode.translate(currx, 0);
      geneNodes.add(currNode);
      this.addChild(currNode);
    }
  }
  /**
   * Sets the current y-axis scale factor.
   * @param newScale double
   */
  public void setAxisScale(double newScale) {
     double currValue =0;
     GeneGraphNode currGene = null;
    for(int i=0; i<geneNodes.size(); i++) {
      currGene = ((GeneGraphNode)geneNodes.get(i));
      currGene.setAxisScale(newScale);
      currGene.setChanged(true);
    }
    yAxisScale = newScale;
  }


  public void addPredictionNodes(double[] geneData, double scaleFactor,double yAxisScale, ArrayList geneNames) {
    double currx=0;
    int dataLength = geneData.length;

    for(int i=0; i<geneData.length; i++) {
      GeneGraphNode currNode = new GeneGraphNode(i,geneData[i]*scaleFactor,scaleFactor,(String)geneNames.get(i));
      currx = ((double)i/(double)dataLength)*chromWidth;
      currNode.translate(currx, 0);
      geneNodes.add(currNode);
      this.addChild(currNode);
    }
  }


  /**
   * Draws/updates this node on the display.
   */
  public void render() {

    int numGenes = geneNodes.size();
    GeneGraphNode currGene;

    for (int i = 0; i < numGenes; i++){
      currGene = (GeneGraphNode)geneNodes.get(i);
      currGene.render();
      }
    }

    /**
     * Returns all gene children contained inside the specified rectangle.
     * @param rect Rectangle2D
     * @return PNode
     */
    public PNode getIntersectingChild(Rectangle2D rect) {
   GeneGraphNode currNode= null;
   GeneGraphNode intersectingNode = null;
   boolean found = false;
   int i=0;
   float geneWidth = this.getChromWidth()/geneNodes.size();

   while(i < geneNodes.size() && !found) {
     currNode = (GeneGraphNode)geneNodes.get(i);
     if(currNode.getGlobalFullBounds().intersects(rect)) {
       found = true;
       intersectingNode = currNode;
     }
     i++;
   }
   return (PNode)intersectingNode;
}

 /**
  * Sets tooltip presence status for the given gene index.
  * @param geneindex int
  * @param set boolean
  */
 public void setTooltip(int geneindex, boolean set) {
   GeneGraphNode currNode = (GeneGraphNode)geneNodes.get(geneindex);
   currNode.setMouseOver(set);
   currNode.render();
 }

 /**
  * Results in changing the selected status of all contained genes within the specified
  * rectangle to <code>true</code>.
  * @param selectionRect Rectangle2D
  * @return ArrayList
  */

 public ArrayList selectChildren(Rectangle2D selectionRect) {
      ArrayList selectedIndices = new ArrayList();
      GeneGraphNode currNode;

      for(int i=0; i<geneNodes.size(); i++) {
        currNode = (GeneGraphNode)geneNodes.get(i);
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
     * Results in changing the selected status of all contained genes within the specified
     * rectangle to <code>true</code>.
     *
     * @param unselectIndices ArrayList
     */
 public void unselectChildren(ArrayList geneIndices) {
        for(int i=0; i<geneIndices.size(); i++) {
          GeneGraphNode currGene = (GeneGraphNode)geneNodes.get(((Integer)geneIndices.get(i)).intValue());
          currGene.setSelected(false);
          currGene.setChanged(true);
          currGene.render();
        }
    }

    /**
     * Returns the width of this node.
     * @return double
     */
    public float getChromWidth() {
    return chromWidth;
  }

  /**
   * Sets the width of this node.
   * @param width double
   */
  public void setChromWidth(float width) {
    chromWidth = width;
    axisLine = new PLine();
    axisLine.setStrokePaint(Color.BLACK);
    axisLine.addPoint(0,0,0);
    axisLine.addPoint(1,chromWidth,0);
    this.addChild(axisLine);
  }

  /**
   * Returns the chromosome number associated with this node.
   * @return int
   */
  public int getChromosomeNumber() {
    return chromNum;
  }

  /**
   * Returns the experiment name associated with this node.
   * @return String
   */
  public String getExperiment() {
    return experiment;
  }

  /**
   * Returns the node type.
   * @return int
   */
  public int getType() {
    return type;
  }
}
