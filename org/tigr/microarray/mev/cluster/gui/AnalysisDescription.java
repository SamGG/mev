/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AnalysisDescription.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import javax.swing.Icon;

/**
 * This structure is used to describe an analysis implementation,
 * where an analysis is class which implements <code>IClusterGUI</code>
 * interface.
 *
 * @see IClusterGUI
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class AnalysisDescription {
    private String name;
    private Icon smallIcon; // 16x16
    private Icon largeIcon; // 32x32
    private String tooltip;
    private String classname;
    
    /**
     * Constructs an <code>AnalysisDescription</code> with specified
     * analysis name and class name.
     *
     * @param name the name of an algorithm.
     * @param classname the class name of the algorithm.
     */
    public AnalysisDescription(String name, String classname) {
	this(name, classname, null, null, null);
    }
    
    /**
     * Constructs an <code>AnalysisDescription</code> with specified
     * analysis name, class name, small icon, large icon and tooltip.
     *
     * @param name the name of an algorithm.
     * @param classname the class name of the algorithm.
     * @param smallIcon the small 16x16 icon.
     * @param largeIcon the small 32x32 icon.
     * @param tooltip the tooltip.
     */
    public AnalysisDescription(String name, String classname, Icon smallIcon, Icon largeIcon, String tooltip) {
	this.name = name;
	this.classname = classname;
	this.smallIcon = smallIcon;
	this.largeIcon = largeIcon;
	this.tooltip = tooltip;
    }
    
    /**
     * Sets small 16x16 icon.
     */
    public void setSmallIcon(Icon smallIcon) {
	this.smallIcon = smallIcon;
    }
    
    /**
     * Sets large 32x32 icon.
     */
    public void setLargeIcon(Icon largeIcon) {
	this.largeIcon = largeIcon;
    }
    
    /**
     * Sets tooltip.
     */
    public void setTooltip(String tooltip) {
	this.tooltip = tooltip;
    }
    
    /**
     * Returns name of the analysis implementation.
     */
    public String getName() {
	return name;
    }
    
    /**
     * Returns the analysis class name.
     */
    public String getClassName() {
	return classname;
    }
    
    /**
     * Returns analysis small 16x16 icon.
     */
    public Icon getSmallIcon() {
	return smallIcon;
    }
    
    /**
     * Returns analysis large 32x32 icon.
     */
    public Icon getLargeIcon() {
	return largeIcon;
    }
    
    /**
     * Returns the analysis tooltip.
     */
    public String getTooltip() {
	return tooltip;
    }
}
