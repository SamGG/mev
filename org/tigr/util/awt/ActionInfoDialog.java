/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ActionInfoDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.*;
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