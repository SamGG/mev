/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ISlideMetaData.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-05-02 16:56:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.util.swing.ProgressBar;

public interface ISlideMetaData {
    /**
     * Returns size of a microarray.
     */
    public int getSize();

    /**
     * Returns a spot base column.
     */
    public int getColumn(int index);

    /**
     * Returns a spot base row.
     */
    public int getRow(int index);

    /**
     * Returns number of a microarray columns.
     */
    public int getColumns();

    /**
     * Returns number of a microarray rows.
     */
    public int getRows();

    /**
     * Returns description of a specified microarray spot.
     * @deprecated
     */
    public String getValueAt(int index, int valueType);

    /**
     * Returns true if a specified spot has no zero values.
     */
    public boolean hasNoZeros(int index);

    /**
     * Sets a microarray non-zero flag.
     */
    public void setNonZero(boolean state);

    /**
     * Returns reference to an element with specified index.
     */
    public ISlideDataElement toSlideDataElement(int index);
    
    /**
     * Returns a list of field names for the gene annotation 
     * associated with this set of microarrays.
     * @return
     */
    public String[] getFieldNames();
    
    public void clearFieldNames();
    
    public void appendFieldNames(String[] fieldNames);
    
    public void setFieldNames(String[] fieldNames);
    
    public void updateFilledAnnFields();

    public String[] getAnnotationValue(int index, String attr);
    public AnnoAttributeObj getAnnotationObj(int index, String attr);
}
