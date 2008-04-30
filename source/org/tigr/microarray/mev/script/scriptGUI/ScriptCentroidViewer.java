/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ScriptCentroidViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2005-03-10 15:39:55 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;

public class ScriptCentroidViewer extends CentroidViewer implements java.io.Serializable {
    public static final long serialVersionUID = 1000102010301010001L;    
    
    /**
     * Construct a <code>KMCCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public ScriptCentroidViewer(Experiment experiment, int[][] clusters) {
	super(experiment, clusters);
    }
    
   
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { }    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {        
        PopupListener listener = new PopupListener();
		this.popup = createJPopupMenu(listener);
		getContentComponent().addMouseListener(listener);
    }
    
}
