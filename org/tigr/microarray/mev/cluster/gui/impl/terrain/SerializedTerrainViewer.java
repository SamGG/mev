package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.Frame;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;



/*
 * TerrainViewerWrapper.java
 *
 * Created on January 12, 2004, 4:10 PM
 */


/**
 *
 * @author  braisted
 */
public class SerializedTerrainViewer extends JPanel implements IViewer, java.io.Serializable {
    
    private TerrainViewer trn;
    private boolean isGenes;
    private Experiment experiment;
    private int [][] clusters;
    private float [][] weights;
    private float [][] locations;
    private float sigma;
    private int labelIndex;

    /** Creates a new instance of SerializedTerrainViewer */
    public SerializedTerrainViewer(boolean isGenes, IFramework framework, int[][] clusters, float[][] weights, float[][] locations, float sigma) {
        this.isGenes = isGenes;
        this.experiment = framework.getData().getExperiment();
        this.clusters = clusters;
        this.weights = weights;
        this.locations = locations;
        this.sigma = sigma;
        this.labelIndex = labelIndex;

        trn = new TerrainViewer(isGenes, experiment, clusters, weights, locations, sigma, framework.getDisplayMenu().getLabelIndex()); 
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeBoolean(this.isGenes);
        oos.writeObject(this.experiment);
        oos.writeObject(this.clusters);
        oos.writeObject(this.weights);
        oos.writeObject(this.locations);
        oos.writeFloat(this.sigma);
        oos.writeInt(this.labelIndex);
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.isGenes = ois.readBoolean();
        this.experiment = (Experiment)ois.readObject();
        this.clusters = (int [][])ois.readObject();
        this.weights = (float [][])ois.readObject();
        this.locations = (float [][])ois.readObject();
        this.sigma = ois.readFloat();
        this.labelIndex = ois.readInt();

        this.trn = new TerrainViewer(isGenes, experiment, clusters, weights, locations, sigma, labelIndex); 
    }
    
        
    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() { 
        return trn.getContentComponent();
    }    
        
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return trn.getCornerComponent(cornerIndex);
    }    
    
    /** Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return trn.getHeaderComponent();
    }
    
    /** Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return trn.getImage();
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return trn.getRowHeaderComponent();
    }
    
    /** Invoked when the framework is going to be closed.
     */
    public void onClosed() {
        trn.onClosed();
    }
    
    /** Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
        trn.onDataChanged(data);
    }
    
    /** Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
        trn.onDeselected();
    }
    
    /** Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
        trn.onMenuChanged(menu);
    }
    
    /** Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        trn.onSelected(framework);
    }
    
    /** Returns the viewer's clusters or null
     */
    public int[][] getClusters() {
        return null;
    }    
    
    /**  Returns the viewer's experiment or null
     */
    public Experiment getExperiment() {
        return null;
    }
    
}
