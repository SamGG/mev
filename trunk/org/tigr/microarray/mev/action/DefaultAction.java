/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: DefaultAction.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:40:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.action;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

public class DefaultAction extends AbstractAction {
    
    private ActionManager manager;
    
    /**
     * Constructs <code>DefaultAction</code> with a specified name
     * and an action command.
     */
    public DefaultAction(ActionManager manager, String name, String command) {
	this(manager, name, command, null);
    }
    
    /**
     * Constructs <code>DefaultAction</code> with a specified name,
     * an action command and a small icon.
     */
    public DefaultAction(ActionManager manager, String name, String command, ImageIcon smallIcon) {
	this.manager = manager;
	putValue(Action.NAME, name);
	putValue(Action.SHORT_DESCRIPTION, name);
	putValue(Action.ACTION_COMMAND_KEY, command);
	putValue(Action.SMALL_ICON, smallIcon);
    }
    
    /**
     * Constructs <code>DefaultAction</code> with a specified name,
     * an action command, small and large icons.
     */
    public DefaultAction(ActionManager manager, String name, String command, ImageIcon smallIcon, ImageIcon largeIcon) {
	this(manager, name, command, smallIcon);
	putValue(ActionManager.LARGE_ICON, largeIcon);
    }
    
    /**
     * Delegates this invokation to a wrapped action manager.
     * @see ActionManager
     */
    public void actionPerformed(ActionEvent e) {
	manager.forwardAction(new ActionEvent(this, e.getID(), (String)getValue(Action.ACTION_COMMAND_KEY)));
    }
}
