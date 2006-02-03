/*
 * CGHPositionGraphCanvas.java
 *
 * Created on March 18, 2003, 9:30 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHDataRegionInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.PositionDataRegionClickedPopup;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphSeparatedCanvas extends CGHPositionGraphCanvas implements ActionListener{

    PositionDataRegionClickedPopup regionClickedPopup;

    public static final int AMPLIFICATIONS = 0;
    public static final int DELETIONS = 1;

    boolean isReversed = false;
    private int flankingRegionType = AMPLIFICATIONS;

    /** Creates a new instance of CGHPositionGraphCanvas */
    public CGHPositionGraphSeparatedCanvas(Insets insets) {
        super(insets);
        regionClickedPopup = new PositionDataRegionClickedPopup(this);
    }

    public CGHPositionGraphSeparatedCanvas(Insets insets, boolean isReversed, int flankingRegionType) {
        super(insets);
        regionClickedPopup = new PositionDataRegionClickedPopup(this);
        this.flankingRegionType = flankingRegionType;
        this.isReversed = isReversed;
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D)g;
        drawColumns(g2D);
    }

    private void drawColumns(Graphics2D g) {
        for (int column = 0; column < model.getNumExperiments(); column++) {
            drawColumn(g, model.getExperimentIndexAt(column));
        }
    }

    /**
     * Draws a specified column.
     */
    private void drawColumn(Graphics2D g, int column) {
        int columnIndex = model.getExperimentIndexAt(column);
        //if(isReversed){
        //    for(int flankingRegion = model.getNumFlankingRegions(columnIndex) - 1; flankingRegion >= 0; flankingRegion--){
        //        drawFlankingRegion(g, flankingRegion, column, columnIndex);
        //    }
        //}else{
            for(int flankingRegion = 0; flankingRegion < model.getNumFlankingRegions(columnIndex); flankingRegion++){
                drawFlankingRegion(g, flankingRegion, column, columnIndex);
            }
        //}
    }


    private void drawFlankingRegion(Graphics2D g2, int frIndex, int column, int columnIndex){

        FlankingRegion fr = model.getFlankingRegionAt(columnIndex, frIndex);

        if(flankingRegionType == DELETIONS && fr.getType() == FlankingRegion.AMPLIFICATION){
            return;
        }

        if(flankingRegionType == AMPLIFICATIONS && fr.getType() == FlankingRegion.DELETION){
            return;
        }

        int frStart = fr.getStart();
        int frStop = fr.getStop();

        Dimension d = getSize();

        double width = d.width;
        double height = d.height - 50;

        double maxVal = model.getMaxClonePosition();

        int rectX = insets.left + column * (elementWidth + rectSpacing);

        if(isReversed){
            //System.out.println("Panel width " + getWidth());
            //System.out.println("Panel preferred width " + getPreferredSize().getWidth());
            //System.out.println("Panel size width " + getSize().getWidth());
            //rectX = getWidth() - rectX - elementWidth;
            //rectX = getWidth() - ( (model.getNumExperiments() - column - 1)* (elementWidth + rectSpacing) ) - insets.right;

            rectX = getWidth() - elementWidth - insets.right - ( (model.getNumExperiments() - column - 1)* (elementWidth + rectSpacing) );
        }

        int rectY = (int) (insets.top + frStart * unitLength);
        double dRectHeight = (frStop - frStart) * unitLength;
        int rectHeight;
        if(dRectHeight < 1 && dRectHeight > 0){
            rectHeight = 1;
        }else{
            rectHeight = (int)dRectHeight;
        }

        Rectangle curRect = new Rectangle(rectX, rectY, elementWidth, rectHeight);

        g2.setPaint(model.getFlankingRegionColor(columnIndex, frIndex));
        g2.fill(curRect);
    }

    /** Getter for property isReversed.
     * @return Value of property isReversed.
     */
    public boolean isIsReversed() {
        return isReversed;
    }

    /** Setter for property isReversed.
     * @param isReversed New value of property isReversed.
     */
    public void setIsReversed(boolean isReversed) {
        this.isReversed = isReversed;
    }

    /** Getter for property flankingRegionType.
     * @return Value of property flankingRegionType.
     */
    public int getFlankingRegionType() {
        return flankingRegionType;
    }

    /** Setter for property flankingRegionType.
     * @param flankingRegionType New value of property flankingRegionType.
     */
    public void setFlankingRegionType(int flankingRegionType) {
        this.flankingRegionType = flankingRegionType;
    }


    protected void formMouseClicked(java.awt.event.MouseEvent evt) {
        if(evt.getButton() == MouseEvent.BUTTON3  ){
        //if(evt.isPopupTrigger() ){
            Point point = evt.getPoint();

            int selectedColumn = getSelectedColumn(point.x);
            int selectedPosition = getSelectedPosition(point.y);

            if(selectedColumn == -1  || selectedPosition < 0 || selectedPosition > model.getMaxClonePosition()){
                selectedDataRegion = null;
                return;
            }

            int experimentIndex = model.getExperimentIndexAt(selectedColumn);

            selectedDataRegion = getFlankingRegionAtLocation(experimentIndex, selectedPosition);

            if(selectedDataRegion != null){
                regionClickedPopup.show(evt.getComponent(), evt.getX(), evt.getY());
            }

        }
        /*
        if(selectedDataRegion != null){
            System.out.println("Type " + selectedDataRegion.getDataRegion().getClass());
            System.out.println("Exp Index " + selectedDataRegion.getExperimentIndex());
        }else{
            System.out.println("Null");
        }*/
    }

    private int getSelectedColumn(int xCoord){
        //Calculate if the point falls in any column
        if(!isReversed){
            for(int column = 0; column < model.getNumExperiments(); column++){
                int rectX = insets.left + column * (elementWidth + rectSpacing);
                if(xCoord >= rectX && xCoord <= rectX + elementWidth){
                    return column;
                }
            }
        }else{
            for(int column = 0; column < model.getNumExperiments(); column++){
                //int rectX = insets.left + column * (elementWidth + rectSpacing);
                //rectX = getWidth() - rectX - elementWidth;

                int rectX = getWidth() - elementWidth - insets.right - ( (model.getNumExperiments() - column - 1)* (elementWidth + rectSpacing) );

                if(xCoord >= rectX && xCoord <= rectX + elementWidth){
                    return column;
                }
            }
        }
        return -1;
    }

    private int getSelectedPosition(int yCoord){
        return (int) ((yCoord - insets.top) / unitLength);
    }

    private CGHDataRegionInfo getFlankingRegionAtLocation(int experimentIndex, int selectedPosition){

        int selectedFrIndex = -1;
        for(int i = 0; i < model.getNumFlankingRegions(experimentIndex); i++){
            FlankingRegion fr = model.getFlankingRegionAt(experimentIndex, i);
            if(selectedPosition >= fr.getStart() && selectedPosition <= fr.getStop() && fr.getType() == this.flankingRegionType){
                selectedFrIndex = i;
            }
        }

        if(selectedFrIndex != -1){
            FlankingRegion fr = model.getFlankingRegionAt(experimentIndex, selectedFrIndex);
            return new CGHDataRegionInfo(fr, experimentIndex);
        }

        return null;
    }

 }
