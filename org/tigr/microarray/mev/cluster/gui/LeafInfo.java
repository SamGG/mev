/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: LeafInfo.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * This class is a structure, which is used to assign properties
 * for a tree node.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class LeafInfo {
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
	this(name, viewer, (JPopupMenu)null, userObject);
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
    public LeafInfo(String name, IViewer viewer, JPopupMenu popup, Object userObject) {
	this(name, viewer, popup, null);
	this.userObject = userObject;
    }
    
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
    
    /**
     * Returns the node viewer.
     */
    public IViewer getViewer() {
	return viewer;
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
     * Overriden to return the node name.
     */
    public String toString() {
	return name;
    }
}
