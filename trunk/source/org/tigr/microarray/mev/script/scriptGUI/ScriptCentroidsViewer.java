/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ScriptCentroidsViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:58 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.script.scriptGUI;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;

public class ScriptCentroidsViewer extends CentroidsViewer {
    
    /**
     * Constructs a <code>KMCCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public ScriptCentroidsViewer(Experiment experiment, int[][] clusters) {
        super(experiment, clusters);
    }

    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {        
        CentroidsViewer.PopupListener listener = new CentroidsViewer.PopupListener();
		this.popup = createJPopupMenu(listener);
		getContentComponent().addMouseListener(listener);
    }
}
