/*
 * CGHPositionGraphSeparatedViewer.java
 *
 * Created on March 19, 2003, 10:34 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
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
//import org.abramson.microarray.cgh.CGHDataObj.*;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphSeparatedViewer extends javax.swing.JPanel implements ActionListener, ICGHViewer {

	IFramework framework;
    Insets insets = new Insets(10, 10, 10, 10);

    private BufferedImage negColorImage;
    private BufferedImage posColorImage;

    double unitLength;
    int elementWidth;

    int displayType;

    //Viewable components
    CGHPositionGraphSeparatedCanvas positionGraphLeft;
    CGHPositionGraphSeparatedCanvas positionGraphRight;
    CytoBandsCanvas cytoBandsCanvas;
    CGHPositionGraphSeparatedHeader header;

    //Models
    CGHPositionGraphDataModel positionGraphModel;
    CytoBandsModel cytoBandsModel;
    CGHAnnotationsModel annotationsModel;


    /** Creates a new instance of CGHPositionGraphSeparatedViewer */
    public CGHPositionGraphSeparatedViewer(IFramework framework) {
        this.framework = framework;
        initComponents();
    }

    private void initComponents(){
        //setLayout(new java.awt.BorderLayout());

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        header = new CGHPositionGraphSeparatedHeader(insets, this);
        positionGraphLeft = new CGHPositionGraphSeparatedCanvas(insets, true, CGHPositionGraphSeparatedCanvas.DELETIONS);
        positionGraphRight = new CGHPositionGraphSeparatedCanvas(insets);
        cytoBandsCanvas = new CytoBandsCanvas(insets);

        this.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        this.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
        //this.header.setNegativeAndPositiveColorImages(this.negColorImage, this.posColorImage);

        add(positionGraphLeft);
        add(cytoBandsCanvas);
        add(positionGraphRight);

        //add(cytoBandsCanvas, BorderLayout.CENTER);
        //add(positionGraphLeft, BorderLayout.WEST);
        //add(positionGraphRight, BorderLayout.EAST);
    }

    public void paint(Graphics g){
        checkUpdateSize();
        //updateSize();
        header.updateSize();
        super.paint(g);
        //drawAnnotations((Graphics2D)g);
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
        double viewerWidth = framework.getViewerBounds().getWidth() - cytoBandsCanvas.getPreferredSize().getWidth();
        viewerWidth /= 2;

        int rectWidth = (int)((viewerWidth - insets.left - insets.right) / positionGraphModel.getNumExperiments()) - rectSpacing;
        if(rectWidth > 80){
            rectWidth = 80;
        }
        return rectWidth;
    }

    private void updateSize(){

        //int width = getWidth();
        int posGraphWidth = (positionGraphModel.getNumExperiments() * (elementWidth + 5) + insets.left + insets.right) ;

        int width = posGraphWidth * 2;
        width += cytoBandsCanvas.getPreferredSize().getWidth();

        //int height = (int)  ((positionGraphModel.getMaxClonePosition() * unitLength) + (insets.top + insets.bottom ));
        int height = (int)  ((cytoBandsModel.getMaxPosition() * unitLength) + (insets.top + insets.bottom ));

        positionGraphLeft.setSize(posGraphWidth, height);
        positionGraphLeft.setPreferredSize(new Dimension(posGraphWidth, height));

        positionGraphRight.setSize(posGraphWidth, height);
        positionGraphRight.setPreferredSize(new Dimension(posGraphWidth, height));

        setSize(width, height);
	setPreferredSize(new Dimension(width, height));

        //repaint();
    }


    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    }

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
        repaint();
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
    }

    public void onMenuChanged(ICGHDisplayMenu menu) {

        positionGraphLeft.setShowFlankingRegions(menu.isShowFlankingRegions());
        positionGraphRight.setShowFlankingRegions(menu.isShowFlankingRegions());

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

        //Insets headerInsets = new Insets(0,0, 0, 0);

        //header.setInsets(headerInsets);

        updateSize();
        //updateSize();
    }

    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework){
        cytoBandsCanvas.onSelected();

        this.framework = framework;

        ICGHDisplayMenu cghMenu = this.framework.getCghDisplayMenu();
        ICGHCloneValueMenu cloneValueMenu = this.framework.getCghCloneValueMenu();

	onMenuChanged(cghMenu);
        onCloneValuesChanged(cloneValueMenu);
    }


    /** Setter for property positionGraphModel.
     * @param positionGraphModel New value of property positionGraphModel.
     */
    public void setPositionGraphModel(CGHPositionGraphDataModel positionGraphModel) {
        this.positionGraphModel = positionGraphModel;
        positionGraphLeft.setModel(positionGraphModel);
        positionGraphRight.setModel(positionGraphModel);
        header.setModel(positionGraphModel);
        cytoBandsCanvas.setChromosomeIndex(positionGraphModel.getChromosomeIndex());
    }

    public void setAnnotationsModel(CGHAnnotationsModel annotationsModel){
        this.annotationsModel = annotationsModel;
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
        positionGraphLeft.setUnitLength(unitLength);
        positionGraphRight.setUnitLength(unitLength);
        cytoBandsCanvas.setUnitLength(unitLength);
    }

    private void updateElementWidth(int elementWidth){
        positionGraphLeft.setElementWidth(elementWidth);
        positionGraphRight.setElementWidth(elementWidth);
        header.setElementWidth(elementWidth);
    }

    public void setDrsListener(IDataRegionSelectionListener drsListener){
        positionGraphLeft.setDrsListener(drsListener);
        positionGraphRight.setDrsListener(drsListener);
    }

    public void onThresholdsChanged(ICGHDisplayMenu menu) {
    }

    /** Getter for property positionGraphLeft.
     * @return Value of property positionGraphLeft.
     */
    public CGHPositionGraphSeparatedCanvas getPositionGraphLeft() {
        return positionGraphLeft;
    }

    /** Setter for property positionGraphLeft.
     * @param positionGraphLeft New value of property positionGraphLeft.
     */
    public void setPositionGraphLeft(CGHPositionGraphSeparatedCanvas positionGraphLeft) {
        this.positionGraphLeft = positionGraphLeft;
    }

    /** Getter for property positionGraphRight.
     * @return Value of property positionGraphRight.
     */
    public CGHPositionGraphSeparatedCanvas getPositionGraphRight() {
        return positionGraphRight;
    }

    /** Setter for property positionGraphRight.
     * @param positionGraphRight New value of property positionGraphRight.
     */
    public void setPositionGraphRight(org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph.CGHPositionGraphSeparatedCanvas positionGraphRight) {
        this.positionGraphRight = positionGraphRight;
    }

    /** Getter for property cytoBandsCanvas.
     * @return Value of property cytoBandsCanvas.
     */
    public org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph.CytoBandsCanvas getCytoBandsCanvas() {
        return cytoBandsCanvas;
    }

    /** Setter for property cytoBandsCanvas.
     * @param cytoBandsCanvas New value of property cytoBandsCanvas.
     */
    public void setCytoBandsCanvas(org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph.CytoBandsCanvas cytoBandsCanvas) {
        this.cytoBandsCanvas = cytoBandsCanvas;
    }

    public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
        this.positionGraphModel.onCloneValuesChanged(menu);
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
