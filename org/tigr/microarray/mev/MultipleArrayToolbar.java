/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MultipleArrayToolbar.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-03-10 15:44:16 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.AbstractButton;

import org.tigr.microarray.mev.action.ActionManager;

public class MultipleArrayToolbar extends JToolBar {
    
    /**
     * Construct a <code>MultipleArrayToolbar</code> using
     * specified action manager.
     * @see ActionManager
     */
    public MultipleArrayToolbar(ActionManager manager) { 
	addAlgorithmActions(manager);
    }
    
    /**
     * Adds actions into the toolbar.
     */
    private void addAlgorithmActions(ActionManager manager) {
	int index = 0;
	Action action;
	while ((action = manager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(index)))!=null) {
	    add(action);
            if(index == 3 || index == 10 || index == 15 || index == 18 || index == 22)
                this.addSeparator();
	    index++;
            
	}
    }
    
    /**
     * Overriden from JToolBar.
     */
    public JButton add(Action a) {
	JButton button = super.add(a);
	button.setActionCommand((String)a.getValue(Action.ACTION_COMMAND_KEY));
	button.setIcon((Icon)a.getValue(ActionManager.LARGE_ICON));
        button.setFocusPainted(false);
      //  button.setBackground(java.awt.Color.white);
     //   button.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
	return button;
    }
    
    /**
     * Returns an array of buttons with the same action command.
     */
    private AbstractButton[] getButtons(String command) {
	ArrayList list = new ArrayList();
	Component[] components = getComponents();
	for (int i = 0; i < components.length; i++) {
	    if (components[i] instanceof AbstractButton) {
		if (((AbstractButton)components[i]).getActionCommand().equals(command))
		    list.add(components[i]);
	    }
	}
	return (AbstractButton[])list.toArray(new AbstractButton[list.size()]);
    }
    
    /**
     * Sets state of buttons with specified action command.
     */
    private void setEnable(String command, boolean enable) {
	AbstractButton[] buttons = getButtons(command);
	if (buttons == null || buttons.length < 1) {
	    return;
	}
	for (int i=0; i<buttons.length; i++) {
	    buttons[i].setEnabled(enable);            
	}
    }
    
    /**
     * Disables some buttons according to specified state.
     */
    public void systemDisable(int state) {
	switch (state) {
	    case TMEV.SYSTEM:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		setEnable(ActionManager.LOAD_FILE_COMMAND, false);
		setEnable(ActionManager.LOAD_EXPRESSION_COMMAND, false);
		setEnable(ActionManager.LOAD_DIRECTORY_COMMAND, false);
		break;
	    case TMEV.DATA_AVAILABLE:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		setEnable(ActionManager.SAVE_IMAGE_COMMAND, false);
		setEnable(ActionManager.PRINT_IMAGE_COMMAND, false);
		setEnable(ActionManager.ANALYSIS_COMMAND, false);
		break;
	    case TMEV.DB_AVAILABLE:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		break;
	    case TMEV.DB_LOGIN:
		setEnable(ActionManager.LOAD_DB_COMMAND, false);
		break;
	}
    }
    
    /**
     * Enables some buttons according to specified state.
     */
    public void systemEnable(int state) {
	switch (state) {
	    case TMEV.SYSTEM:
		setEnable(ActionManager.LOAD_FILE_COMMAND, true);
		setEnable(ActionManager.LOAD_DIRECTORY_COMMAND, true);
		setEnable(ActionManager.LOAD_EXPRESSION_COMMAND, true);
		break;
	    case TMEV.DATA_AVAILABLE:
		setEnable(ActionManager.SAVE_IMAGE_COMMAND, true);
		setEnable(ActionManager.PRINT_IMAGE_COMMAND, true);
		setEnable(ActionManager.ANALYSIS_COMMAND, true);
		break;
	    case TMEV.DB_AVAILABLE:
		break;
	    case TMEV.DB_LOGIN:
		setEnable(ActionManager.LOAD_DB_COMMAND, true);
		break;
	}
    }
    
}
