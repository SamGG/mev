/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHMultipleArrayDataFcd.java
 *
 * Created on April 00, 0003
 */

package org.tigr.microarray.mev.cgh.CGHDataGenerator;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.cluster.gui.IData;

//import org.abramson.microarray.cgh.CGHAnalyzerMeV;

/**
 * This class is used to make determinations about a CGHMultipleArrayData object
 * that must rely on other information.  For example, this class makes copy number
 * determinations based on data stored in a CGHMultipleArrayData object and parameters
 * stored in an IFramework object
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHCopyNumberCalculatorNoDyeSwap extends CGHCopyNumberCalculator{

    /** Creates a new instance of CGHMultipleArrayDataFcd */
    public CGHCopyNumberCalculatorNoDyeSwap(MultipleArrayData data /*, ICGHFramework framework, CGHMultipleArrayDataFcd fcd*/) {
        super(data /*, framework, fcd*/);
    }

    public int getCopyNumberDetermination(int experiment, int clone){
        return determineCopyNumberByThreshold((ISlideData)data.getFeaturesList().get(experiment), clone/*, ISlideData.CY3_SLIDES*/);
    }

    public int getCopyNumberDeterminationByLogCloneDistribution(int experiment, int clone){
        float pValue = data.getPValueByLogCloneDistribution(experiment, clone);
        if(pValue > cloneDistributionPThresh){
            return 1;
        }else if (pValue < 1 - cloneDistributionPThresh){
            return -1;
        }
        return IData.NO_COPY_CHANGE;
    }
}
