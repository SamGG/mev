/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHCircleViewerModel.java
 *
 * Created on October 10, 2002, 4:56 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHCircleViewerModel  extends CGHViewerDataModel implements ICircleViewerModel{

    /** Creates a new instance of CGHCircleViewerModel */
    //public CGHCircleViewerModel(CGHMultipleArrayDataFcd fcd) {
	public CGHCircleViewerModel(IFramework framework) {
        super(framework);
    }

    public Color getDataPointColor(int chrom, int bac, int experimentIndex){
        float value = data.getValue(experimentIndex, bac, chrom);
        return getColor(value);
    }

    public int getNumChromosomes(){
        return data.getNumChromosomes();
    }

    public int getNumDataPointsInChrom(int chrom){
        return data.getNumDataPointsInChrom(chrom);
    }


    public CGHClone getCloneAt(int cloneIndex, int chromosomeIndex) {
        return data.getCloneAt(cloneIndex, chromosomeIndex);
    }

}