/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * IPositionGraphDataModel.java
 *
 * Created on March 16, 2003, 11:40 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface IPositionGraphDataModel {

    public int getNumElements();

    public int getNumExperiments();

    public String getExperimentName(int index);

    public int getMaxClonePosition();

    public Color getDataPointColor(int cloneIndex, int experimentIndex);

    public int getNumFlankingRegions(int expIndex);

    public FlankingRegion getFlankingRegionAt(int expIndex, int flankingRegionIndex);

    public int getStart(int index);

    public int getStop(int index);

    public int getChromosomeIndex();

    public CGHClone getCloneAt(int index);
}
