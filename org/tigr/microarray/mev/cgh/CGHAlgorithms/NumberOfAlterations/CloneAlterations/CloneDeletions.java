/*
 * CloneDeletions.java
 *
 * Created on May 19, 2003, 2:06 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.CloneAlterations;

import org.tigr.microarray.mev.cluster.gui.IData;

//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CloneDeletions extends CloneAlterations{


    /** Creates a new instance of CloneDeletions */
    public CloneDeletions() {
        nodeName = "Clone Deletions";
    }

    protected boolean isAltered(int copyNumber) {
        if(copyNumber < 0 && copyNumber != IData.BAD_CLONE && copyNumber != IData.NO_COPY_CHANGE){
            return true;
        }else{
            return false;
        }
    }

}
