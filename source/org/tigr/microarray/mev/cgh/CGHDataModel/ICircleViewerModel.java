/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ICircleViewerModel.java
 *
 * Created on May 1, 2003, 1:16 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICircleViewerModel {
    public int getNumChromosomes();

    public int getNumDataPointsInChrom(int chrom);

    public Color getDataPointColor(int chrom, int bac, int experimentIndex);

    public CGHClone getCloneAt(int cloneIndex, int chromosomeIndex);
}
