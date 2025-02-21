/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: LeafInfo.java,v $
 * $Revision: 1.8 $
 * $Date: 2007-05-03 14:08:45 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import javax.swing.JPopupMenu;

/**
 * This class is a structure, which is used to assign properties
 * for a tree node.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class LeafInfo implements java.io.Serializable {
        
//    public static final long serialVersionUID = 2020002L;
    
    // leaf name
    private String name;
    // it will be used to display a result
    private IViewer viewer;
    // popup menu for a result tree node
    private JPopupMenu popup;
    // tree node tooltip
    private String tooltip;
    // user object
    private Object userObject;
    // selected, as data source
    private boolean selectedDataSource;
    
    
    public LeafInfo() {  }
    /**
     * Constructs a <code>LeafInfo</code> with specified name.
     *
     * @param name the node name.
     */
    public LeafInfo(String name) {
	this(name, (IViewer)null);
    }
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name and
     * appropriated viewer.
     *
     * @param name the node name.
     * @param viewer an instance of <code>IViewer</code> interface.
     * @see IViewer
     */
    public LeafInfo(String name, IViewer viewer) {
	this(name, viewer, null);
    }
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name and
     * popup menu.
     *
     * @param name the node name.
     * @param popup the node popup menu.
     */
    public LeafInfo(String name, JPopupMenu popup) {
	this(name, (IViewer)null, popup);
    }
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name,
     * appropriated viewer and an user object.
     *
     * @param name the node name.
     * @param viewer an instance of <code>IViewer</code> interface.
     * @param userObject the reference to an user object.
     * @see IViewer
     * @see IFramework#getUserObject
     */
    public LeafInfo(String name, IViewer viewer, Object userObject) {
    	this(name, viewer, (JPopupMenu)null, userObject, false);
    }
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name,
     * appropriated viewer and popup menu.
     *
     * @param name the node name.
     * @param viewer an instance of <code>IViewer</code> interface.
     * @param popup the node popup menu.
     * @see IViewer
     */
    public LeafInfo(String name, IViewer viewer, JPopupMenu popup) {
	this(name, viewer, popup, null);
    }
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name,
     * appropriated viewer, popup menu and an user object.
     *
     * @param name the node name.
     * @param viewer an instance of <code>IViewer</code> interface.
     * @param popup the node popup menu.
     * @param userObject the reference to an user object.
     * @see IViewer
     * @see IFramework#getUserObject
     */
    public LeafInfo(String name, IViewer viewer, JPopupMenu popup, Object userObject, boolean selectedDataSource) {
	this(name, viewer, popup, null);
	this.userObject = userObject;
		this.selectedDataSource = selectedDataSource;
    }
    public static String[] getPersistenceDelegateArgs() {
    	return new String[] {"name", "viewer", "JPopupMenu", "userObject", "selectedDataSource"};
    }
    public boolean getSelectedDataSource(){return selectedDataSource;}
    
    /**
     * Constructs a <code>LeafInfo</code> with specified name,
     * appropriated viewer, popup menu and a tooltip.
     *
     * @param name the node name.
     * @param viewer an instance of <code>IViewer</code> interface.
     * @param popup the node popup menu.
     * @param tooltip the node tooltip.
     * @see IViewer
     */
    public LeafInfo(String name, IViewer viewer, JPopupMenu popup, String tooltip) {
	this.name = name;
	this.viewer = viewer;
	this.popup = popup;
	this.tooltip = tooltip;
    }
    
    public void setName(String val) {
        name = val;
    }
    public String getName() {
        return name;
    }
    
    /**
     * Returns the node viewer.
     */
    public IViewer getViewer() {
        return viewer;
    }
    
    public void setViewer(IViewer v) {
        this.viewer = v;
    }
    
    /**
     * Returns the node popup menu.
     */
    public JPopupMenu getJPopupMenu() {
	return popup;
    }
    
    /**
     * Returns the node tooltip.
     */
    public String getToolTip() {
	return tooltip;
    }
    
    
    /**
     * Sets the node user object.
     */
    public void setUserObject(Object userObject) {
	this.userObject = userObject;
    }
    
    /**
     * Returns the node user object.
     */
    public Object getUserObject() {
        return userObject;
    }
    
    /**
     * Returns true if it is the selected data source
     */
    public boolean isSelectedDataSource() {
        return selectedDataSource;
    }
    
    /**
     * sets the selected status for data source
     */
    public void setSelectedDataSource(boolean selected) {
        selectedDataSource = selected;
    }
    
    /**
     * Overriden to return the node name.
     */
    public String toString() {
	return name;
    }
    
    /**
     * Sets the JPopupMenu field
     */
    public void setPopupMenu(JPopupMenu menu){
        this.popup = menu;
    }
}
