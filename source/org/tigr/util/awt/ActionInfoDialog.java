/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ActionInfoDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.Container;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class ActionInfoDialog extends JDialog {
    protected Vector listeners = new Vector();
    protected Container contentPane;
    
    public ActionInfoDialog(JFrame parent, boolean modal)    {
	super(parent, modal);
	contentPane = getContentPane();
    }
    
    public ActionInfoDialog(JFrame parent, String title, boolean modal) {
	super(parent, title, modal);
	contentPane = getContentPane();
    }
    
    public void addActionInfoListener(ActionInfoListener listener)    {
	listeners.addElement(listener);
    }
    
    public void removeActionInfoListener(ActionInfoListener listener)    {
	listeners.removeElement(listener);
    }
    
    public void fireEvent(ActionInfoEvent event)    {
	Vector targets = (Vector) listeners.clone();
	for (int i = 0; i < targets.size(); i++) {
	    ActionInfoListener listener = (ActionInfoListener) targets.elementAt(i);
	    listener.actionInfoPerformed(event);
	}
    }
}