/*
 * CGHPositionGraphCanvas.java
 *
 * Created on March 18, 2003, 9:30 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Color;
import java.awt.Insets;
import java.util.EventObject;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHAnnotationsModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHPositionGraphDataModel;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHDataRegionInfo;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.GenomeBrowserLauncher;
import org.tigr.microarray.mev.cgh.CGHListenerObj.IDataRegionSelectionListener;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class CGHPositionGraphCanvas extends JPanel{
    Insets insets;
    CGHPositionGraphDataModel model;
    CGHAnnotationsModel annotationsModel;
    IDataRegionSelectionListener drsListener;
    CGHDataRegionInfo selectedDataRegion = null;
    boolean showFlankingRegions;

    //find a better way to do this
    int elementWidth = 40;
    int rectSpacing = 5;

    double unitLength;


    /** Creates a new instance of CGHPositionGraphCanvas */
    public CGHPositionGraphCanvas(Insets insets) {
        setBackground(Color.black);
        this.insets = insets;

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
    }

    protected abstract void formMouseClicked(java.awt.event.MouseEvent evt);

    /** Getter for property model.
     * @return Value of property model.
     */
    public CGHPositionGraphDataModel getModel() {
        return model;
    }

    /** Setter for property model.
     * @param model New value of property model.
     */
    public void setModel(CGHPositionGraphDataModel model) {
        this.model = model;
    }

    /** Getter for property showFlankingRegions.
     * @return Value of property showFlankingRegions.
     */
    public boolean isShowFlankingRegions() {
        return showFlankingRegions;
    }

    /** Setter for property showFlankingRegions.
     * @param showFlankingRegions New value of property showFlankingRegions.
     */
    public void setShowFlankingRegions(boolean showFlankingRegions) {
        this.showFlankingRegions = showFlankingRegions;
    }

    /** Getter for property unitLength.
     * @return Value of property unitLength.
     */
    public double getUnitLength() {
        return unitLength;
    }

    /** Setter for property unitLength.
     * @param unitLength New value of property unitLength.
     */
    public void setUnitLength(double unitLength) {
        this.unitLength = unitLength;
    }

    /** Getter for property elementWidth.
     * @return Value of property elementWidth.
     */
    public int getElementWidth() {
        return elementWidth;
    }

    /** Setter for property elementWidth.
     * @param elementWidth New value of property elementWidth.
     */
    public void setElementWidth(int elementWidth) {
        this.elementWidth = elementWidth;
    }

    /** Getter for property drsListener.
     * @return Value of property drsListener.
     */
    public IDataRegionSelectionListener getDrsListener() {
        return drsListener;
    }

    /** Setter for property drsListener.
     * @param drsListener New value of property drsListener.
     */
    public void setDrsListener(IDataRegionSelectionListener drsListener) {
        this.drsListener = drsListener;
    }

    /** Getter for property annotationsModel.
     * @return Value of property annotationsModel.
     */
    public CGHAnnotationsModel getAnnotationsModel() {
        return annotationsModel;
    }

    /** Setter for property annotationsModel.
     * @param annotationsModel New value of property annotationsModel.
     */
    public void setAnnotationsModel(CGHAnnotationsModel annotationsModel) {
        this.annotationsModel = annotationsModel;
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        JMenuItem item = (JMenuItem)actionEvent.getSource();
        if(selectedDataRegion != null){
            if("Show Genes in Region".equals(item.getActionCommand())){
                drsListener.onShowGenes(new EventObject(selectedDataRegion));
            }else if("Show Browser".equals(item.getActionCommand())){
                drsListener.onShowBrowser(new EventObject(selectedDataRegion));
            }else if("Display Data Values".equals(item.getActionCommand())){
                drsListener.onDisplayDataValues(new EventObject(selectedDataRegion));
            }else if("Launch Ensembl".equals(item.getActionCommand())){
                GenomeBrowserLauncher.launchEnsembl(selectedDataRegion.getDataRegion(), model.getCGHSpecies());
            }else if("Launch Golden Path".equals(item.getActionCommand())){
                GenomeBrowserLauncher.launchGoldenPath(selectedDataRegion.getDataRegion(), model.getCGHSpecies());
            }else if("Launch NCBI Viewer".equals(item.getActionCommand())){
                GenomeBrowserLauncher.launchNCBIMapViewer(selectedDataRegion.getDataRegion(), model.getCGHSpecies());
            }
        }
    }

}
