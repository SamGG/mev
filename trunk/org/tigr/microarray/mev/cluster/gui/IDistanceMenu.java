/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IDistanceMenu.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:48 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;

/**
 * This interface is used to access to framework distance menu.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IDistanceMenu {
    
    /**
     * Returns true if absolute distance item is checked.
     */
    public boolean isAbsoluteDistance();
    
    /**
     * Returns code of a selected function.
     * These codes declared in Algorithm interface.
     *
     * @see Algorithm
     */
    public int getDistanceFunction();
    
    /**
     * Returns a human readable name of the specified function.
     */
    public String getFunctionName(int function);
}
