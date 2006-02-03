/*
 * CGHPositionGraphCombinedCanvas.java
 *
 * Created on March 20, 2003, 12:04 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHBrowserModelAdaptor;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHDataRegionInfo;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.PositionDataRegionClickedPopup;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphCombinedCanvas extends CGHPositionGraphCanvas implements ActionListener{

    PositionDataRegionClickedPopup regionClickedPopup;

    IData data;

    int annotationsRectWidth = 10;

    public static final int COLUMN_NOT_FOUND = -1;
    public static final int ANNOTATIONS_COLUMN = -2;

    /** Creates a new instance of CGHPositionGraphCombinedCanvas */
    public CGHPositionGraphCombinedCanvas(Insets insets) {
        super(insets);
        regionClickedPopup = new PositionDataRegionClickedPopup(this);
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D)g;

        drawColumns(g2D);
    }

    private void drawColumns(Graphics2D g) {

        for (int column = 0; column < model.getNumExperiments(); column++) {
            int columnIndex = model.getExperimentIndexAt(column);
            drawColumn(g, columnIndex);
        }

        drawAnnotations(g);
    }

    /**
     * Draws a specified column.
     */
    private void drawColumn(Graphics2D g, int column) {
        int columnIndex = model.getExperimentIndexAt(column);

        for (int row = 0; row < model.getNumElements(); row++) {
            drawSlideDataElement(g, row, column, columnIndex);
        }

        if(showFlankingRegions){
            for(int flankingRegion = 0; flankingRegion < model.getNumFlankingRegions(columnIndex); flankingRegion++){
                drawFlankingRegion(g, flankingRegion, column, columnIndex);
            }
        }
    }

    private void drawSlideDataElement(Graphics2D g2, final int row, int column, int columnIndex) {

        int cloneStart = model.getStart(row);
        int cloneStop = model.getStop(row);

        Dimension d = getSize();

        double width = d.width;
        double height = d.height - 50;

        double maxVal = model.getMaxClonePosition();

        int rectX = insets.left + column * (elementWidth + rectSpacing);
        int rectY = (int) (insets.top + cloneStart * unitLength);
        double dRectHeight = (cloneStop - cloneStart) * unitLength;
        int rectHeight;
        if(dRectHeight < 1 && dRectHeight > 0){
            rectHeight = 1;
        }else{
            rectHeight = (int)dRectHeight;
        }

        Rectangle curRect = new Rectangle(rectX, rectY, elementWidth, rectHeight);

        g2.setPaint(model.getDataPointColor(row, columnIndex));
        g2.fill(curRect);
    }

    private void drawFlankingRegion(Graphics2D g2, int frIndex, int column, int columnIndex){

        FlankingRegion fr = model.getFlankingRegionAt(columnIndex, frIndex);

        int frStart = fr.getStart();
        int frStop = fr.getStop();

        Dimension d = getSize();

        double width = d.width;
        double height = d.height - 50;

        double maxVal = model.getMaxClonePosition();

        int rectX = insets.left + column * (elementWidth + rectSpacing);
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

    private void drawAnnotations(Graphics2D g2){
        int annotationsX = getAnnotationsXCoord();

        int fontSize = 8;

        for(int i = 0; i < annotationsModel.getNumAnnotations(); i++){
            ICGHDataRegion dataRegion = annotationsModel.getAnnotationAt(i);
            String name = dataRegion.getName();

            int start = dataRegion.getStart();
            int stop = dataRegion.getStop();

            int rectY = (int) (insets.top + start * unitLength);
            double dRectHeight = (stop - start) * unitLength;
            int rectHeight;
            if(dRectHeight < 1 && dRectHeight > 0){
                rectHeight = 1;
            }else{
                rectHeight = (int)dRectHeight;
            }

            Rectangle curRect = new Rectangle(annotationsX, rectY, annotationsRectWidth, rectHeight);
            g2.setPaint(annotationsModel.getAnnotationColorAt(i));
            g2.fill(curRect);

            g2.setFont(new Font("", 0, fontSize));
            g2.drawString(name, annotationsX + annotationsRectWidth + 5, rectY);
        }
    }

    protected void formMouseClicked(java.awt.event.MouseEvent evt) {

        if(evt.getButton() == MouseEvent.BUTTON3  ){
        //if(evt.isPopupTrigger() ){
            Point point = evt.getPoint();

            int selectedColumn = getSelectedColumn(point.x);
            int selectedPosition = getSelectedPosition(point.y);


            if(selectedColumn == COLUMN_NOT_FOUND || selectedPosition < 0 || selectedPosition > model.getMaxClonePosition()){
                selectedDataRegion = null;
                return;
            }

            if(selectedColumn == ANNOTATIONS_COLUMN){
                selectedDataRegion = getAnnotationAtLocation(selectedPosition);
            }else{
                int experimentIndex = model.getExperimentIndexAt(selectedColumn);

                if(isShowFlankingRegions()){
                    selectedDataRegion = getFlankingRegionAtLocation(experimentIndex, selectedPosition);
                }

                if(!isShowFlankingRegions() || selectedDataRegion == null){
                    selectedDataRegion = getCloneAtLocation(experimentIndex, selectedPosition);
                }
            }

            if(selectedDataRegion != null){
                regionClickedPopup.show(evt.getComponent(), evt.getX(), evt.getY());
            }

        }
    }


    private CGHDataRegionInfo getCloneAtLocation(int experimentIndex, int selectedPosition){

        int selectedRow = -1;
        for(int i = 0; i < model.getNumElements(); i++){
            if(selectedPosition >= model.getStart(i) && selectedPosition <= model.getStop(i)){
                selectedRow = i;
            }
        }

        if(selectedRow != -1){
            CGHClone selectedClone = model.getCloneAt(selectedRow);
            return new CGHDataRegionInfo(selectedClone, experimentIndex);
        }

        return null;
        //System.out.println("selected col " + selectedColumn + " position " + selectedPosition);
    }

    private CGHDataRegionInfo getFlankingRegionAtLocation(int experimentIndex, int selectedPosition){

        int selectedFrIndex = -1;
        for(int i = 0; i < model.getNumFlankingRegions(experimentIndex); i++){
            FlankingRegion fr = model.getFlankingRegionAt(experimentIndex, i);
            if(selectedPosition >= fr.getStart() && selectedPosition <= fr.getStop()){
                selectedFrIndex = i;
            }
        }

        if(selectedFrIndex != -1){
            FlankingRegion fr = model.getFlankingRegionAt(experimentIndex, selectedFrIndex);
            return new CGHDataRegionInfo(fr, experimentIndex);
        }

        return null;
    }

    private CGHDataRegionInfo getAnnotationAtLocation(int selectedPosition){
        int selectedAnnotationIndex = -1;

        for(int i = 0; i < annotationsModel.getNumAnnotations(); i++){
            ICGHDataRegion dataRegion = annotationsModel.getAnnotationAt(i);
            if(selectedPosition >= dataRegion.getStart() && selectedPosition <= dataRegion.getStop()){
                return new CGHDataRegionInfo(dataRegion, CGHBrowserModelAdaptor.ALL_EXPERIMENTS);
            }
        }

        return null;
    }

    private int getSelectedColumn(int xCoord){
        //Calculate if the point falls in any column
        for(int column = 0; column < model.getNumExperiments(); column++){
            int rectX = insets.left + column * (elementWidth + rectSpacing);
            if(xCoord >= rectX && xCoord <= rectX + elementWidth){
                return column;
            }
        }

        //check if it corresponds to an annotation
        int annotationsX = getAnnotationsXCoord();

        if(xCoord >= annotationsX && xCoord <= annotationsX + annotationsRectWidth){
            return ANNOTATIONS_COLUMN;
        }

        return COLUMN_NOT_FOUND;
    }

    private int getAnnotationsXCoord(){
        return insets.left + model.getNumExperiments() * (elementWidth + rectSpacing);
    }

    private int getSelectedPosition(int yCoord){
        return (int) ((yCoord - insets.top) / unitLength);
    }



    /** Getter for property data.
     * @return Value of property data.
     */
    public IData getData() {
        return data;
    }

    /** Setter for property data.
     * @param data New value of property data.
     */
    public void setData(IData data) {
        this.data = data;
    }

}
