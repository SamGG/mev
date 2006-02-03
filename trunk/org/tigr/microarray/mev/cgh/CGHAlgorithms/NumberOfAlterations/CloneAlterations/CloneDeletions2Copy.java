/*
 * MostDeleted2Copy.java
 *
 * Created on June 15, 2003, 9:47 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.CloneAlterations;

import org.tigr.microarray.mev.cluster.gui.IData;

//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CloneDeletions2Copy extends CloneAlterations{

    /** Creates a new instance of MostDeleted2Copy */
    public CloneDeletions2Copy() {
        nodeName = "Clone Deletions 2 Copy";
    }

    protected boolean isAltered(int copyNumber) {
        if(copyNumber < -1 && copyNumber != IData.BAD_CLONE && copyNumber != IData.NO_COPY_CHANGE){
            //System.out.println("clone deletions 2 copy is altered: " + copyNumber);
            return true;
        }else{
            return false;
        }
    }
}
