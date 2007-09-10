/*
 * CGHPositionGraphViewer.java
 *
 * Created on March 18, 2003, 9:08 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;

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

//import org.abramson.microarray.cgh.ICGHFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphCombinedViewer extends javax.swing.JPanel implements ActionListener, ICGHViewer{

    IFramework framework;
    IData data;
    Insets insets = new Insets(10, 10, 10, 10);

    private BufferedImage negColorImage;
    private BufferedImage posColorImage;

    double unitLength;
    int elementWidth;
    int annotationsWidth = 40;

    int displayType;

    //Viewable components
    CGHPositionGraphCombinedCanvas positionGraph;
    CytoBandsCanvas cytoBandsCanvas;
    CGHPositionGraphCombinedHeader header;

    //Models
    CGHPositionGraphDataModel positionGraphModel;
    CytoBandsModel cytoBandsModel;

    /** Creates a new instance of CGHPositionGraphViewer */
    public CGHPositionGraphCombinedViewer(IFramework framework, CGHPositionGraphCombinedHeader header) {
        this.framework = framework;
        this.header = header;
        initComponents();
        //this.header.setElementWidth(80);
    }

    private void initComponents(){
        setLayout(new java.awt.BorderLayout());

        //header = new CGHPositionGraphCombinedHeader();
        positionGraph = new CGHPositionGraphCombinedCanvas(insets);
        cytoBandsCanvas = new CytoBandsCanvas(new Insets(insets.top, 0, insets.bottom, 5));

        //this.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        //this.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
        //this.header.setNegativeAndPositiveColorImages(this.negColorImage, this.posColorImage);


        add(positionGraph, BorderLayout.CENTER);
        add(cytoBandsCanvas, BorderLayout.WEST);

        positionGraph.setData(framework.getData());
    }

    public void paint(Graphics g){
        checkUpdateSize();
        header.updateSize();
        header.repaint();
        super.paint(g);
    }

    private void checkUpdateSize(){
        if(framework.getCghDisplayMenu().getUnitLength() == ICGHDisplayMenu.FIT_SIZE){
            unitLength = calculateFitUnitLength();
            updateUnitLength(unitLength);
        }

        if(framework.getCghDisplayMenu().getElementWidth() == ICGHDisplayMenu.FIT_SIZE){
            elementWidth = calculateFitElementWidth();
            updateElementWidth(elementWidth);
        }

    }

    private double calculateFitUnitLength(){

        Rectangle rect = framework.getViewerBounds();

        double height = rect.getHeight();

        height -= (insets.bottom + insets.top);

        //double maxVal = positionGraphModel.getMaxClonePosition();
        double maxVal = cytoBandsModel.getMaxPosition();

        double unitLength = height / maxVal;

        return unitLength;
    }

    private int calculateFitElementWidth(){
        int rectSpacing = 5;
        double viewerWidth = framework.getViewerBounds().getWidth() - cytoBandsCanvas.getPreferredSize().getWidth() - annotationsWidth;
        int rectWidth = (int)((viewerWidth - insets.left - insets.right) / positionGraphModel.getNumExperiments()) - rectSpacing;
        if(rectWidth > 80){
            rectWidth = 80;
        }
        return rectWidth;
    }

    private void updateSize(){

        //int width = getWidth();
        int width = positionGraphModel.getNumExperiments() * (elementWidth + 5) + insets.left + insets.right;
        width += cytoBandsCanvas.getPreferredSize().getWidth();

        //int height = (int)  ((positionGraphModel.getMaxClonePosition() * unitLength) + (insets.top + insets.bottom ));
        int height = (int)  ((cytoBandsModel.getMaxPosition() * unitLength) + (insets.top + insets.bottom ));

        setSize(width, height);
        setPreferredSize(new Dimension(width, height));

        //repaint();
    }


    public void actionPerformed(java.awt.event.ActionEvent event){

    }

    /*********** methods to implement IViewer *************************/

    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return this;
    }

    /** Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return header;
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
        this.data = data;
        updateSize();
        //header.setData(data);
        //header.updateSize();

        //switched order
        header.setContentWidth(getSize().width);
        header.updateSize();
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
        header.onMenuChanged(menu);
    }

    public void onMenuChanged(ICGHDisplayMenu menu) {

        positionGraph.setShowFlankingRegions(menu.isShowFlankingRegions());

        if(menu.getUnitLength() == ICGHDisplayMenu.FIT_SIZE){
            unitLength = calculateFitUnitLength();
        }else{
            unitLength = menu.getUnitLength();
        }

        updateUnitLength(unitLength);

        if(menu.getElementWidth() == ICGHDisplayMenu.FIT_SIZE){
            elementWidth = calculateFitElementWidth();
        }else{
            elementWidth = menu.getElementWidth();
        }

        updateElementWidth(elementWidth);
        updateSize();

        Insets headerInsets = new Insets(0, (int) cytoBandsCanvas.getPreferredSize().getWidth() + insets.left, 0, 0);
        header.setInsets(headerInsets);
        header.setElementWidth(elementWidth);
        header.setContentWidth(getSize().width);
        updateSize();
        header.updateSize();
        header.repaint();

        //updateSize();
    }

    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework){
        cytoBandsCanvas.onSelected();

        this.framework = framework;

        ICGHDisplayMenu cghMenu = this.framework.getCghDisplayMenu();
        //header.setData(framework.getData());
        ICGHCloneValueMenu cloneValueMenu = this.framework.getCghCloneValueMenu();

        onMenuChanged(cghMenu);
        onDataChanged(this.framework.getData());
        onCloneValuesChanged(cloneValueMenu);
    }

    /** Getter for property positionGraphModel.
     * @return Value of property positionGraphModel.
     */
    public CGHPositionGraphDataModel getPositionGraphModel() {
        return positionGraphModel;
    }

    /** Setter for property positionGraphModel.
     * @param positionGraphModel New value of property positionGraphModel.
     */
    public void setPositionGraphModel(CGHPositionGraphDataModel positionGraphModel) {
        this.positionGraphModel = positionGraphModel;

        positionGraphModel.setNegColorImage(framework.getDisplayMenu().getNegativeGradientImage());
        positionGraphModel.setPosColorImage(framework.getDisplayMenu().getPositiveGradientImage());

        header.setModel(positionGraphModel);
        positionGraph.setModel(positionGraphModel);
        cytoBandsCanvas.setChromosomeIndex(positionGraphModel.getChromosomeIndex());
    }

    public void setAnnotationsModel(CGHAnnotationsModel annotationsModel){
        positionGraph.setAnnotationsModel(annotationsModel);
    }

    /** Getter for property cytoBandsModel.
     * @return Value of property cytoBandsModel.
     */
    public CytoBandsModel getCytoBandsModel() {
        return cytoBandsModel;
    }

    /** Setter for property cytoBandsModel.
     * @param cytoBandsModel New value of property cytoBandsModel.
     */
    public void setCytoBandsModel(CytoBandsModel cytoBandsModel) {
        this.cytoBandsModel = cytoBandsModel;
        cytoBandsCanvas.setModel(cytoBandsModel);
    }

    private void updateUnitLength(double unitLength){
        positionGraph.setUnitLength(unitLength);
        cytoBandsCanvas.setUnitLength(unitLength);
    }

    private void updateElementWidth(int elementWidth){
        positionGraph.setElementWidth(elementWidth);
        header.setElementWidth(elementWidth);
    }

    public void setDrsListener(IDataRegionSelectionListener drsListener){
        positionGraph.setDrsListener(drsListener);
    }

    public void onThresholdsChanged(ICGHDisplayMenu menu) {
    }


    public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
        this.positionGraphModel.onCloneValuesChanged(menu);
        //positionGraph.repaint();//repaint();
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

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}
}
