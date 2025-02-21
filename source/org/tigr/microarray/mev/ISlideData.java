/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ISlideData.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-05-02 16:56:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.tigr.microarray.mev.sampleannotation.ISampleAnnotation;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;

public interface ISlideData {
    
    // Normalization Modes
    public final static int NO_NORMALIZATION = 0;
    public final static int TOTAL_INTENSITY = 1;
    public final static int LEAST_SQUARES = 2;
    public final static int LINEAR_REGRESSION = 3;
    public final static int RATIO_STATISTICS_95 = 4;
    public final static int RATIO_STATISTICS_99 = 5;
    public final static int ITERATIVE_LOG = 6;
    public final static int LOWESS = 7;
    
    public final static int TOTAL_INTENSITY_LIST = 101;
    public final static int LEAST_SQUARES_LIST = 102;
    public final static int LINEAR_REGRESSION_LIST = 103;
    public final static int RATIO_STATISTICS_95_LIST = 104;
    public final static int RATIO_STATISTICS_99_LIST = 105;
    public final static int ITERATIVE_LOG_LIST = 106;
    public final static int LOWESS_LIST = 107;
    /**
     * CGH Constants
     */
    public static final int CY3_SLIDES = 0;
    public static final int CY5_SLIDES = 1;
    
    /**
     * Returns the name of a microarray.
     */
    public String getSlideDataName();
    
    /**
     * Sets a microarray name.
     */
    public void setSlideDataName(String slideDataName);
    
    /**
     * Sets the current label index.
     */

    //TODO may need to add a method signature for adding more than one key at once
    /*
    public void setDataLabelKeys(Vector key);
  */
    
    //TODO may need to disable this method.
    /**
     * Sets the current label index.
     */
    public void setDataLabelKey(String key);
    
    /**
     * Adds a new key and label value
     */
    public void addNewSampleLabel(String label, String value);
    
    /**
     * Returns the slide name keys.
     */
    public Vector<String> getSlideDataKeys();
 
    /**
     * Returns the slide data labels hash
     */
    public Hashtable getSlideDataLabels();
    
    /**
     * Sets the slide data label hash
     */
    public void setSlideDataLabels(Vector keys, Hashtable labels);
    
    /**
     * Returns size of a microarray.
     */
    public int getSize();
    
    /**
     * Returns a microarray normalization state.
     */
    public int getNormalizedState();
    
    /**
     * Sets a microarray normalization state.
     */
    public void setNormalizedState(int normalizedState);
    
    /**
     * Returns a microarray sort state.
     */
    public int getSortState();
    
    /**
     * Returns CY3 value for specified index.
     */
    public float getCY3(int index);
    
    /**
     * Returns CY5 value for specified index.
     */
    public float getCY5(int index);
    
    /**
     * Returns a microarray max CY3 value.
     */
    public float getMaxCY3();
    
    /**
     * Returns a microarray max CY5 value.
     */
    public float getMaxCY5();
    
    /**
     * Returns max intencity of specified type.
     */
    public float getMaxIntensity(int intensityType);
    
    /**
     * Returns min intencity of specified type.
     */
    public float getMinIntensity(int intensityType, boolean acceptZeros);
    
    /**
     * Returns a ratio value with specified index and log state.
     */
    public float getRatio(int index, int logState);
    
    /**
     * Returns a microarray max ratio value.
     */
    public float getMaxRatio();
    
    /**
     * Returns a microarray max ratio value, with specified log state.
     */
    public float getMaxRatio(int logState);
    
    /**
     * Returns a microarray max ratio value of specified intensities.
     */
    public float getMaxRatio(int intensityIndex1, int intensityIndex2, int logState);
    
    /**
     * Returns a microarray min ratio value.
     */
    public float getMinRatio();
    
    /**
     * Returns a microarray min ratio value with specified log state.
     */
    public float getMinRatio(int logState);
    
    /**
     * Returns a microarray min ratio value of specified intensities.
     */
    public float getMinRatio(int intensityIndex1, int intensityIndex2, int logState);
    
    /**
     * Returns a microarray max product value of specified intensities.
     */
    public float getMaxProduct(int intensityIndex1, int intensityIndex2);
    
    /**
     * Returns a microarray min product value of specified intensities.
     */
    public float getMinProduct(int intensityIndex1, int intensityIndex2, boolean acceptZeros);
    
    /**
     * Returns a microarray min product value of specified intensities and lower cutoffs.
     */
    public float getMinProduct(int intensityIndex1, int intensityIndex2, boolean acceptZeros, int lowCutoff);
    
    /**
     * Normalizes a microarray data.
     */
    public void applyNormalization(int normalizationMode, Properties properties);
    
    /**
     * Normalizes a microarray data.
     */
    public void applyNormalizationList(int normalizationMode);
    
    /**
     * Sets a microarray non-zero attribute.
     */
    public void setNonZero(boolean value);
    
    /**
     * Sets intensities for a spot with specified index.
     */
    public void setIntensities(int index, float cy3, float cy5);
    
    /**
     * Returns an <code>ISlideDataElement</code> by specified index.
     */
    public ISlideDataElement getSlideDataElement(int index);
    
    /**
     * Adds an <code>ISlideDataElement</code> to a microarray.
     */
    public void addSlideDataElement(ISlideDataElement element);
    
    /**
     * Returns a reference to a microarray meta data.
     */
    public ISlideMetaData getSlideMetaData();
    
    /**
     * Sets spot specific data such as spot area, QC scores, saturation factors, etc.
     */
    public void setSpotInformationData(String [] infoLabels, String [][] spotData);
    
    /**
     * Returns the <code>SpotInformationData</code> object.
     */
    public SpotInformationData getSpotInformationData();
    
    /**
     * Toggles the length of the displayed file name.
     */
    public void toggleNameLength();
    
    /**
     * Returns the full name (not truncated)
     */
    public String getFullSlideDataName();
    
    /**
     * Sets the data type attribute see static type variables in <code>IData</code>
     */
    public void setDataType(int type);
      
    /**
     * Returns the data type attribute
     */
    public int getDataType();
    
    /**
     * Sets the slide file name
     */
    public void setSlideFileName(String fileName);
    
    /**
     * Writes float values of intensities for this slide to an output stream.
     * @param dos output stream to write to
     */
    //public void writeIntensities(DataOutputStream dos) throws IOException;
    //public void loadIntensities(DataInputStream dis) throws IOException;
    /**
     * Returns the slide's file name
     */
    public String getSlideFileName();
    
    /**
     * Returns the detection status for the gene specified, Affy support
     */

    public String getDetection(int row);
    //add by wwang
    public float getPvalue(int row);
    public int getGenePixFlags(int row);

   
    /**
     * CGH Function from ICGHFeatureData
     * @param flankingRegions
     */
    public void setFlankingRegions(java.util.Vector[] flankingRegions);
    /**
     * CGH Function from ICGHFeatureData
     * @param chromosomeIndex
     * @return
     */
    public int getNumFlankingRegions(int chromosomeIndex);
    /**
     * CGH Function from ICGHFeatureData
     * @return
     */
    public java.util.Vector[] getFlankingRegions();
    /**
     * CGH Function from ICGHFeatureData
     * @param cloneIndex
     * @return
     */
    public boolean isMissingData(int cloneIndex);
    
    public String getSampleLabelKey();
    
    /**
     *@author sarita
     *Sets sample annotation object
     * 
     */
    
    public void setSampleAnnotation(SampleAnnotation sampAnn);
    
    /**
     * @author sarita
     * gets SampleAnnotation object
     * 
     */
    
    public SampleAnnotation getSampleAnnotation();
    	
    /**
     * @author sarita
     * getters and setters for SampleAnnotationModel.
     * 
     * 
     */
    public boolean isSampleAnnotationLoaded();
    public void setSampleAnnotationLoaded(boolean isAnnLoaded);
    
    
}
