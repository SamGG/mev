/*
 * CGHPositionGraphViewer.java
 *
 * Created on March 19, 2003, 9:44 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHAnnotationsModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CGHPositionGraphDataModel;
import org.tigr.microarray.mev.cgh.CGHDataModel.CytoBandsModel;
import org.tigr.microarray.mev.cgh.CGHListenerObj.IDataRegionSelectionListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

//import  org.abramson.microarray.cgh.ICGHFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphViewer extends javax.swing.JPanel implements ActionListener, ICGHViewer {

    IFramework framework;
    CGHPositionGraphCombinedViewer combinedViewer;
    CGHPositionGraphSeparatedViewer separatedViewer;

    ICGHViewer viewer;

    CGHPositionGraphDataModel positionGraphModel;

    /** Creates a new instance of CGHPositionGraphViewer */
    //public CGHPositionGraphViewer(ICGHFramework framework, CGHPositionGraphCombinedHeader header) {
    public CGHPositionGraphViewer(IFramework framework, CGHPositionGraphCombinedHeader combinedHeader) {
        this.framework = framework;
        combinedViewer = new CGHPositionGraphCombinedViewer(framework, combinedHeader);
        separatedViewer = new CGHPositionGraphSeparatedViewer(framework);
        updateViewer(framework.getCghDisplayMenu().getDisplayType());

    }


    private void updateViewer(int viewerType){
        if(viewerType == ICGHDisplayMenu.DISPLAY_TYPE_COMBINED){
            this.viewer = combinedViewer;
        }else if(viewerType == ICGHDisplayMenu.DISPLAY_TYPE_SEPARATED){
            this.viewer = separatedViewer;
        }
    }

    public void setPositionGraphModel(CGHPositionGraphDataModel positionGraphModel) {
        this.positionGraphModel = positionGraphModel;
        combinedViewer.setPositionGraphModel(positionGraphModel);
        separatedViewer.setPositionGraphModel(positionGraphModel);
        onThresholdsChanged(framework.getCghDisplayMenu());
    }

    public void setAnnotationsModel(CGHAnnotationsModel annotationsModel){
        combinedViewer.setAnnotationsModel(annotationsModel);
        separatedViewer.setAnnotationsModel(annotationsModel);
    }

    public void setCytoBandsModel(CytoBandsModel cytoBandsModel) {
        combinedViewer.setCytoBandsModel(cytoBandsModel);
        separatedViewer.setCytoBandsModel(cytoBandsModel);
    }

    public void setDrsListener(IDataRegionSelectionListener drsListener){
        combinedViewer.setDrsListener(drsListener);
        separatedViewer.setDrsListener(drsListener);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    }

    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return viewer.getContentComponent();
    }

    /** Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return viewer.getHeaderComponent();
    }

    /** Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }

    /** Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }

    /** Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
        viewer.onDataChanged(data);
    }

    /** Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }

    /** Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
        combinedViewer.onMenuChanged(menu);
        positionGraphModel.setMaxRatioScale(menu.getMaxRatioScale());
	positionGraphModel.setMinRatioScale(menu.getMinRatioScale());
        positionGraphModel.setNegColorImage(menu.getNegativeGradientImage());
        positionGraphModel.setPosColorImage(menu.getPositiveGradientImage());
    }

    public void onThresholdsChanged(ICGHDisplayMenu menu){
        //positionGraphModel.setThresholds(menu.getAmpThresh(), menu.getDelThresh(),
        //    menu.getAmpThresh2Copy(), menu.getDelThresh2Copy());
        //viewer.repaint();
    }

    public void onMenuChanged(ICGHDisplayMenu menu) {
        setBackground(menu.getCircleViewerBackgroundColor());

        updateViewer(menu.getDisplayType());

        viewer.onMenuChanged(menu);
    }

    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        ICGHDisplayMenu cghMenu = this.framework.getCghDisplayMenu();
	//onMenuChanged(cghMenu);
        IDisplayMenu menu = framework.getDisplayMenu();
        onMenuChanged(menu);
        onMenuChanged(cghMenu);
        //updateViewer(cghMenu.getDisplayType());
        viewer.onSelected(framework);
    }

    public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
        viewer.onCloneValuesChanged(menu);
    }


	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}


	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}


	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}


	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return null;
	}


	public int getViewerType() {
		// TODO Auto-generated method stub
		return 0;
	}

}
