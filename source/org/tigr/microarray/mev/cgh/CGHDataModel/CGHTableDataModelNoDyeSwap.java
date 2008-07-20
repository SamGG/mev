/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHTableDataModelNoDyeSwap.java
 *
 * Created on July 5, 2003, 11:32 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHTableDataModelNoDyeSwap extends CGHTableDataModel {

    /** Creates a new instance of CGHTableDataModelDyeSwap */
    public CGHTableDataModelNoDyeSwap(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        super(/*fcd,*/ framework, experimentIndex, chromosomeIndex);
    }

    public int getColumnCount() {
        return adaptor.experimentIndices.length + getNumAnnotationCols();
    }

    public String getColumnDataName(int labelIndex) {
        return data.getSampleName(adaptor.experimentIndices[labelIndex]);
    }

    /**
     * Remember getRatio Glitch
     */
    public Object getDataValueAt(int row, int col) {
        int experimentIndex = col - getNumAnnotationCols();
        //Raktim
        //System.out.println("CGHTableDataModelNoDyeSwap, experimentIndex,  adaptor.experimentIndices[experimentIndex]: " + experimentIndex + " " + adaptor.experimentIndices[experimentIndex]);
        int cloneIndex = adaptor.getCloneIndex(row);
        switch (adaptor.getCloneValueType()){
            case CGHBrowserModelAdaptor.CLONE_VALUES_RATIOS:
            	//Bug with Expr index
                //return new Float(data.getRatio(experimentIndex, cloneIndex, IData.LOG));
            	return new Float(data.getRatio(adaptor.experimentIndices[experimentIndex], cloneIndex, IData.LOG));
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_RATIOS:
            	//Bug with Expr index
                //return new Float(data.getRatio(experimentIndex, cloneIndex, IData.LOG));
            	return new Float(data.getRatio(adaptor.experimentIndices[experimentIndex], cloneIndex, IData.LOG));
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                return new Float(data.getPValueByLogCloneDistribution(experimentIndex, cloneIndex));
        }
        return new Float(Float.NaN);
    }

}
