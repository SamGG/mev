/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AnalysisAction.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-24 15:55:19 $
 * $Author: wwang67 $
 * $State: Exp $
 */
package org.tigr.microarray.mev.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;

public class AnalysisAction extends AbstractAction {
    
    private ActionManager manager;
    
    /**
     * Constructs an <code>AnalysisAction</code> from specified description.
     * @see AnalysisDescription
     */
    public AnalysisAction(ActionManager manager, AnalysisDescription desc) {
	this.manager = manager;
	putValue(Action.NAME, desc.getName());
	putValue(Action.SHORT_DESCRIPTION, desc.getTooltip());
	putValue(Action.ACTION_COMMAND_KEY, ActionManager.ANALYSIS_COMMAND);
	putValue(Action.SMALL_ICON, desc.getSmallIcon());
	putValue(ActionManager.LARGE_ICON, desc.getLargeIcon());
//	putValue(ActionManager.CATEGORY_ICON, desc.getCategoryIcon());
	putValue(ActionManager.PARAMETER, desc.getClassName());
	putValue(ActionManager.CATEGORY, desc.getCategory());
    }
    
    /**
     * Delegates this invokation to a wrapped action manager.
     * @see ActionManager
     */
    public void actionPerformed(ActionEvent e) {
	manager.forwardAction(new ActionEvent(this, e.getID(), (String)getValue(Action.ACTION_COMMAND_KEY)));
    }
    
}
