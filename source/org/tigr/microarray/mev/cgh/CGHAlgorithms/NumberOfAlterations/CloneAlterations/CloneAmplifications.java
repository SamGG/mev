/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CloneAmplifications.java
 *
 * Created on May 19, 2003, 2:14 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.CloneAlterations;

import org.tigr.microarray.mev.cluster.gui.IData;

//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CloneAmplifications extends CloneAlterations {

    /** Creates a new instance of CloneAmplifications */
    public CloneAmplifications() {
        nodeName = "CloneAmplifications";
    }

    protected boolean isAltered(int copyNumber) {
        if(copyNumber > 0 && copyNumber != IData.BAD_CLONE && copyNumber != IData.NO_COPY_CHANGE){
            return true;
        }else{
            return false;
        }
    }

}
