/*
 * CGHCircleViewerPanel.java
 *
 * Created on October 10, 2002, 4:36 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHCircleViewer;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cgh.CGHDataModel.CGHCircleViewerModel;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHDataRegionInfo;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.GenomeBrowserLauncher;
import org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil.PositionDataRegionClickedPopup;
import org.tigr.microarray.mev.cgh.CGHListenerObj.IDataRegionSelectionListener;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.ICGHViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHCircleViewerPanel extends JPanel implements ICGHViewer, Cloneable, ActionListener{
    CGHCircleViewerModel model;
    int experimentIndex = 0;

    CGHDataRegionInfo selectedDataRegion;
    PositionDataRegionClickedPopup regionClickedPopup;
    IDataRegionSelectionListener drsListener;

    CGHCircleViewerHeader header;

    /** Creates new form CGHCircleViewerPanel */
    public CGHCircleViewerPanel(CGHCircleViewerModel model) {
        this.model = model;
        this.header = new CGHCircleViewerHeader(new Insets(15,15,0,0),model,this);

        regionClickedPopup = new PositionDataRegionClickedPopup(this);
        initComponents();
    }


    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     *
     */
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        setBackground(java.awt.Color.white);
        super.setBackground(java.awt.Color.white);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                //formMouseMoved(evt);
            }
        });
    }

    public void paint(Graphics g) {
        super.paint(g);
        header.updateSize();

        Graphics2D g2 = (Graphics2D) g;
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d = getSize();

        double diameter = Math.min(d.width, d.height);

        double halfWidth = diameter / 2;
        double halfHeight = diameter / 2;

        double midWidth = d.width / 2;
        double midHeight = d.height / 2;

        double rectWidth, rectHeight;
        rectHeight = rectWidth = halfWidth / 60;

        double angle;
        double sin;
        double cos;
        double widthScale = halfWidth;
        double heightScale = halfHeight;

        double widthInc = widthScale * .85 / model.getNumChromosomes();
        double heightInc = heightScale * .85 / model.getNumChromosomes();

        double x, y;

        int k = 0;
        int m;

        for(int curChrom = 0; curChrom < model.getNumChromosomes(); curChrom++){

            heightScale -= heightInc;
            widthScale -= heightInc;
            k++;
            m = 0;

            double totalDataPoints = model.getNumDataPointsInChrom(curChrom);
            Ellipse2D el;

            for(int curBac = 0; curBac < totalDataPoints; curBac++){
                angle = (double)360 * (double) m / (double)totalDataPoints;
                sin = Math.sin(Math.toRadians(angle));
                cos = Math.cos(Math.toRadians(angle));
                //y = midHeight + heightScale* sin;
                y = midHeight - heightScale* sin;
                //x = midWidth + widthScale *cos;
                x = midWidth - widthScale *cos;
                el = new Ellipse2D.Double(x, y, rectWidth, rectHeight);
                g2.setPaint(model.getDataPointColor(curChrom, curBac, experimentIndex));
                g2.fill(el);

                m++;
            }
        }
    }

    private String getSelectedPopupValue(int xCoord, int yCoord){

        CGHClone clone = getSelectedClone(xCoord, yCoord);

        if(clone == null){
            return null;
        }else{
            String retVal = "Chromosome: " + (CGHUtility.convertChromToString(clone.getChromosomeIndex() + 1, model.getCGHSpecies())) + " Probe " + clone.getName();
            return retVal;
        }
    }

    private CGHClone getSelectedClone(int xCoord, int yCoord){
        //Later change this method to do a reverse calculation
        Dimension d = getSize();

        double diameter = Math.min(d.width, d.height);

        double halfWidth = diameter / 2;
        double halfHeight = diameter / 2;

        double midWidth = d.width / 2;
        double midHeight = d.height / 2;

        double rectWidth, rectHeight;
        rectHeight = rectWidth = halfWidth / 60;

        double angle;
        double sin;
        double cos;
        double widthScale = halfWidth;
        double heightScale = halfHeight;

        double widthInc = widthScale * .85 / model.getNumChromosomes();
        double heightInc = heightScale * .85 / model.getNumChromosomes();

        double x, y;

        int k = 0;
        int m;

        for(int curChrom = 0; curChrom < model.getNumChromosomes(); curChrom++){

            heightScale -= heightInc;
            widthScale -= heightInc;
            k++;
            m = 0;

            double totalDataPoints = model.getNumDataPointsInChrom(curChrom);
            Ellipse2D el;

            for(int curBac = 0; curBac < totalDataPoints; curBac++){
                angle = (double)360 * (double) m / (double)totalDataPoints;
                sin = Math.sin(Math.toRadians(angle));
                cos = Math.cos(Math.toRadians(angle));
                y = midHeight - heightScale* sin;
                x = midWidth - widthScale *cos;
                el = new Ellipse2D.Double(x, y, rectWidth, rectHeight);

                if(el.contains(xCoord, yCoord)){
                    CGHClone clone = model.getCloneAt(curBac, curChrom);
                    return clone;
                }

                m++;
            }
        }
        return null;
    }

    public void setExperimentIndex(int experimentIndex){
        this.experimentIndex = experimentIndex;
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
        //System.out.println("hello world!");

        model.setMaxRatioScale(menu.getMaxRatioScale());
        model.setMinRatioScale(menu.getMinRatioScale());
        model.setNegColorImage(menu.getNegativeGradientImage());
        model.setPosColorImage(menu.getPositiveGradientImage());
        header.onMenuChanged(menu);
    }

    public void onMenuChanged(ICGHDisplayMenu menu) {
        setBackground(menu.getCircleViewerBackgroundColor());
    }

    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        ICGHDisplayMenu cghMenu = framework.getCghDisplayMenu();
        IDisplayMenu menu = framework.getDisplayMenu();
        ICGHCloneValueMenu cloneValueMenu = framework.getCghCloneValueMenu();
        onMenuChanged(menu);
        onMenuChanged(cghMenu);
        onCloneValuesChanged(cloneValueMenu);
        //repaint();
    }

    public void onThresholdsChanged(ICGHDisplayMenu menu) {
    }

    protected void formMouseClicked(java.awt.event.MouseEvent evt) {

        Point point = evt.getPoint();

        if(evt.getButton() == MouseEvent.BUTTON3  ){
        //if(evt.isPopupTrigger() ){
            CGHClone selectedClone = getSelectedClone(point.x, point.y);
            if(selectedClone != null){
                selectedDataRegion = new CGHDataRegionInfo(selectedClone, experimentIndex);
                regionClickedPopup.show(evt.getComponent(), point.x, point.y);
            }
        }else if(evt.getButton() == MouseEvent.BUTTON1){
            String sel = getSelectedPopupValue(point.x, point.y);
            if(sel != null){
                JPopupMenu popup = new JPopupMenu();
                popup.add(new JLabel(sel));
                popup.show(evt.getComponent(), point.x, point.y);
            }
        }

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

    public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
        this.model.onCloneValuesChanged(menu);
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

    // Variables declaration - do not modify
    // End of variables declaration

}

