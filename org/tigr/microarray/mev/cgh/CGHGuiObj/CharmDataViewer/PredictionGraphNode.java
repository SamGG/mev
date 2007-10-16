package org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;

import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
/**
* This class implements a single prediction rectangle to be displayed on the ChARM UI
 * display panel.
 *
 * <p>Title: PredictionGraphNode</p>
 * <p>Description: This class implements a single prediction rectangle to be displayed on the ChARM UI
 * display panel.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

public class PredictionGraphNode extends PNode {

private static final Color SELECTED_DOWN_COLOR = Color.YELLOW;
private static final Color SELECTED_UP_COLOR = Color.MAGENTA;

private static final Color DOWN_COLOR = Color.GREEN;
private static final Color UP_COLOR = Color.RED;

private static final int PREDICTION_RECT_HEIGHT = 10;

private boolean isSelected = false;
private boolean isChanged = false;

private double startx;
private double endx;
private double sign;
private int type;

private PPath predRect;

private double lastDx;
private double lastDy;

  /**
   * Class constructor.
   * @param startx double- start x coordinate
   * @param endx double- end x coordinate
   * @param sign double- indicates positive or negative bias
   * @param type int- indicates Window.TYPE_AUTO or Window.TYPE_MANUAL
   */
  public PredictionGraphNode(double startx, double endx, double sign, int type) {
     super();
     this.startx = startx;
     this.endx = endx;
     this.sign = sign;
     this.type = type;

     lastDx=0;
     lastDy=0;

     predRect = new PPath();
     this.addChild(predRect);

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
    * Draws/updates prediction node on display.
    */
   public void render() {
     Color geneColor=null;

     if(isChanged) {
       this.removeChild(predRect);
       predRect = new PPath();

       if(type == SegmentInfo.TYPE_AUTO) predRect.setPathTo(new RoundRectangle2D.Double(startx,0.0,endx - startx,(double)this.PREDICTION_RECT_HEIGHT, 10, 10));
       else if(type == SegmentInfo.TYPE_MANUAL) predRect.setPathTo(new RoundRectangle2D.Double(startx,0.0,endx - startx,(double)this.PREDICTION_RECT_HEIGHT, 0, 0));

       if(this.isSelected()) {
         if(this.sign < 0) geneColor = this.SELECTED_DOWN_COLOR;
         else geneColor = this.SELECTED_UP_COLOR;
       }
       else {
         if(this.sign < 0) geneColor = this.DOWN_COLOR;
         else geneColor = this.UP_COLOR;
       }

       predRect.setPaint(geneColor);
       this.addChild(predRect);
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
    * Sets the starting x coordinate.
    * @param val double
    */
   public void setStartX(double val) {
     startx = val;
   }

   /**
    * Sets the ending x coordinate.
    * @param val double
    */
   public void setEndX(double val) {
     endx = val;
   }

   /**
    * Returns the starting x coordinate.
    * @return double
    */
   public double getStartX() {
     return startx;
   }

   /**
    * Returns the ending x coordinate.
    * @return double
    */
   public double getEndX() {
     return endx;
   }

   /**
    * Required of type PNode.  Moves this prediction node by specified x and y amounts.
    * @param dx double
    * @param dy double
    */
   public void translate(double dx, double dy) {
     super.translate(dx,dy);
     lastDx=dx;
     lastDy=dy;
 }

 /**
  * Reverses the last translation.
  */
 public void reverseLastTranslation() {
   this.translate(-lastDx,-lastDy);
 }



}
