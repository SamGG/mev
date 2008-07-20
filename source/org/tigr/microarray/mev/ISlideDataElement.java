/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ISlideDataElement.java,v $
 * $Revision: 1.6 $
 * $Date: 2007-12-19 21:39:34 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;

public interface ISlideDataElement {
    //Coordinate types
    public final static int BASE_ONLY = 0;
    public final static int META_SUB = 1;
    //Coordinate modifiers
    public final static int BASE = 0;
    public final static int META = 1;
    public final static int SUB = 2;
    //Intensity types
    public final static int CY3 = 0;
    public final static int CY5 = 1;
    
    /**
     * Returns clone of this element.
     */
    public ISlideDataElement copy();
    
    /**
     * Returns a ratio value for specified intensities.
     */
    public float getRatio(int intensityIndex1, int intensityIndex2, int logState);
    
    /**
     * Returns an intensity of specified type.
     */
    public float getIntensity(int intensityType);
    
    /**
     * Sets an intensity of specified type.
     */
    public void setIntensity(int intensityType, float value);
    
    /**
     * Sets true intensity of specified type.
     */
    public void setTrueIntensity(int intensityType, float value);
    
    /**
     * Returns an array of intensities.
     */
    public float[] getCurrentIntensity();
    
    /**
     * Returns true intensity of specified type.
     */
    public float getTrueIntensity(int intensityType);
    
    /**
     * Returns an array of true intensities.
     */
    public float[] getTrueIntensity();
    
    /**
     * Sets the extra fields (annotation), appends if fields exist
     */
    public void setExtraFields(String [] values);
    
    /**
     * Sets the UID field
     */
    public void setUID(String uid);
    
    /**
     * Returns the UID field
     */
    public String getUID();
    
    /**
     * Returns a spot meta column.
     */
    public int getColumn(int columnType);
    
    /**
     * Sets a spot meta column.
     */
    public void setColumn(int columnType, int value);
    
    /**
     * Returns a spot meta row.
     */
    public int getRow(int rowType);
    
    /**
     * Sets a spot meta row.
     */
    public void setRow(int rowType, int value);
    
    /**
     * Returns a spot location.
     */
    public int getLocation(int positionType, int columns);
    
    /**
     * Returns an array of spot meta columns.
     */
    public int[] getColumns();
    
    /**
     * Returns an array of spot meta rows.
     */
    public int[] getRows();
    
    /**
     * Returns a description by specified index.
     */
    public String getFieldAt(int index);
    
    /**
     * Returns string array of a spot meta data.
     */
    public String[] getExtraFields();
    
    /**
     * Sets a spot non-zero flag.
     */
    public void setNonZero(boolean state);
    
    /**
     * Returns a spot non-zero flag.
     */
    public boolean hasNoZeros();
    
    //pcahan
    public void setDetection(String detection);
    
    // pcahan
    public String getDetection();
    public boolean isNonZero();
    public boolean getIsNull();
 //wwang
    public void setPvalue(float value);
    public void setGenePixFlags(int value);
    public float getPvalue();
    public int getGenePixFlags();
    
    /**
     * Raktim - Return Annotation Object Model
     */
    public IAnnotation getElementAnnotation();
    //Sarita-- Annotation State saving 
    public void setElementAnnotation(IAnnotation obj);
    
}
