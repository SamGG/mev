/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IData.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-03-10 20:38:25 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui;

import java.awt.Color;

import java.util.Vector;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;

public interface IData {
    //Log state
    public final static int LINEAR = 0;
    public final static int LOG = 1;
  
    public final static int DATA_TYPE_TWO_INTENSITY = 0;
    public final static int DATA_TYPE_RATIO_ONLY = 1;
    public final static int DATA_TYPE_AFFY_ABS = 2;
    public final static int DATA_TYPE_AFFY_REF = 3;
    public final static int DATA_TYPE_AFFY_MEAN = 4;
    public final static int DATA_TYPE_AFFY_MEDIAN = 5;
    
    public final static String DEFAULT_SAMPLE_ANNOTATION_KEY = "Default Slide Name";

    /**
     * Returns the experiment data (ratio values).
     * @see Experiment
     */
    public Experiment getExperiment();
    
    /**
     * Returns the experiment data (ratio values) without application of cutoffs.
     * @see Experiment
     */
    public Experiment getFullExperiment();
    
    /**
     * Returns count of features.
     */
    public int getFeaturesCount();
    
    /**
     * Returns size of features.
     */
    public int getFeaturesSize();

    /**
     * Retruns the indicated feature
     */
    public ISlideData getFeature(int index);

    /**
     * Returns the indicated ISlideDataElement
     */
    public ISlideDataElement getSlideDataElement(int row, int col);
        
    /**
     * Returns the integer identifying the type of input data
     */
    public int getDataType();
    
    /**
     * Returns CY3 value.
     */
    public float getCY3(int column, int row);
    
    /**
     * Returns CY5 value.
     */
    public float getCY5(int column, int row);
    
    /**
     * Returns max CY3 value.
     */
    public float getMaxCY3();
    
    /**
     * Returns max CY5 value.
     */
    public float getMaxCY5();
    
    /**
     * Returns ratio value.
     */
    public float getRatio(int column, int row, int logState);
    
    /**
     * Returns min ratio value.
     */
    public float getMinRatio();
    
    /**
     * Returns max ratio value
     */
    public float getMaxRatio();
    
    /**
     * Returns feature name.
     */
    public String getSampleName(int column);
    
    /**
     * Returns the slected sample annotation
     */
    public String getSampleAnnotation(int column, String key);
    
    /**
     * Returns full feature name.
     */
    public String getFullSampleName(int column);
    
    /**
     * Sets the experiment label index for the collection of features
     */
    public void setSampleLabelKey(String key);
    
    /**
     * Returns an element attribute.
     */
    public String getElementAttribute(int row, int attr);
    
    /**
     * Returns a probe column in micro array.
     */
    public int getProbeColumn(int column, int row);
    
    /**
     * Returns a probe row in micro array.
     */
    public int getProbeRow(int column, int row);
    
    /**
     * Returns a gene unique id.
     */
    public String getUniqueId(int row);
    
    /**
     * Returns a gene name.
     */
    public String getGeneName(int row);
    
    /**
     *Returns all the annotation fields
     */
    
    public String[] getFieldNames();
    
    /**
     *Returns all annotation field names associated with the loaded samples
     */
    public Vector getSampleAnnotationFieldNames();    
   
    /**
     * Returns sorted indices for specified column.
     */
    public int[] getSortedIndices(int column);
    
    
    //////////////////////////////////////////
    //                                      //
    //        color coding methods          //
    //                                      //
    //////////////////////////////////////////
    
    /**
     * Returns array of published colors.
     */
    public Color[] getColors();
    
    /**
     * Delete all the published colors.
     */
    public void deleteColors();
    
    /**
     * Returns public color by specified row.
     */
    public Color getProbeColor(int row);
    
    /**
     * Sets public color for specified rows.
     */
    public void setProbesColor(int[] rows, Color color);
    
    /**
     * Returns index of the public color for specified row.
     */
    public int getProbeColorIndex(int row);
    
    /**
     * Returns probe color indices
     */
    public int[] getColorIndices();
    
    /**
     * Returns count of rows which have public color index equals to colorIndex.
     */
    public int getColoredProbesCount(int colorIndex);
    
    /**
     * Delete all the published experiment colors.
     */
    public void deleteExperimentColors();
    
    /**
     * Returns color for specified column data
     */
    public Color getExperimentColor(int col);

    /**
     * Sets color for specified experiment indices 
     */
    public void setExperimentColor(int [] indices, Color color);
    
    /**
     * Returns index of the public experiment color for specified row.
     */
    public int getExperimentColorIndex(int row);
    
    /**
     * Returns experiment color indices
     */
    public int[] getExperimentColorIndices();
    
    /**
     * Returns count of rows which have public color index equals to colorIndex.
     */
    public int getColoredExperimentsCount(int colorIndex);
    
    /**
     * Returns array of published colors.
     */
    public Color[] getExperimentColors();
    
    /**
     * Returns an annotation array for the provided indices based on annotation key
     */
    public String [] getAnnotationList(String fieldName, int [] indices);
        
    /**
     * Returns true if loaded intensities are known to be median
     */
    public boolean areMedianIntensities();
    
    /**
     * Sets median intensity flag
     */
    public void setMedianIntensities(boolean areMedians);

}
