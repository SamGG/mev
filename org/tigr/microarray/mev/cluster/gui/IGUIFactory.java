/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: IGUIFactory.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import javax.swing.Action;

/**
 * Each gui distribution must contain class, which
 * implements this interface.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public interface IGUIFactory {
    
    /**
     * Returns the array of analysis descriptions.
     * @see AnalysisDescription
     */
    public AnalysisDescription[] getAnalysisDescriptions();
}
