/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GUIFactory.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-04-10 18:41:36 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.AnalysisDescription;
import org.tigr.microarray.mev.cluster.gui.IGUIFactory;

public class GUIFactory implements IGUIFactory {
    
    private ResourceBundle bundle;
    private static String BUNDLE_NAME = "org.tigr.microarray.mev.cluster.gui.impl.factory";
    
    public GUIFactory() {
	try {
	    bundle = ResourceBundle.getBundle(BUNDLE_NAME);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public AnalysisDescription[] getAnalysisDescriptions() {
	if (bundle == null) {
	    return null;
	}
	return createAnalysisDescription();
    }
    
    private AnalysisDescription[] createAnalysisDescription() {
	String names = null;
	try {
	    names = bundle.getString("gui.names");
	   
	} catch (MissingResourceException e) {
	    e.printStackTrace();
	}
	if (names == null) {
	    return null;
	}
	ArrayList list = new ArrayList();
	StringTokenizer tokenizer = new StringTokenizer(names, ":");
	String key;
	String name, clazz,category, tooltip;
	Icon smallIcon, largeIcon;
	while (tokenizer.hasMoreTokens()) {
	    try {
		key   = tokenizer.nextToken();
		if (key.trim().equals("")) {
		    continue;
		}
		name  = bundle.getString(key+".name").trim();
		clazz = bundle.getString(key+".class").trim();
		category=bundle.getString(key+".category").trim();
		tooltip = bundle.getString(key+".tooltip").trim();
		smallIcon = getIcon(bundle.getString(key+".smallIcon").trim());
		largeIcon = getIcon(bundle.getString(key+".largeIcon").trim());
		list.add(new AnalysisDescription(name, clazz, category, smallIcon, largeIcon, tooltip));
		} catch (Exception e) {
		System.out.println("Error while reading "+BUNDLE_NAME+".properties file: ");
		e.printStackTrace();
	    }
	}
	return(AnalysisDescription[])list.toArray(new AnalysisDescription[list.size()]);
    }
    /*
    private AnalysisDescription[] createAnalysisDescription() {
    	String names = null;
    	try {
    	    names = bundle.getString("gui.names");
    	    //System.out.print(names);
    	} catch (MissingResourceException e) {
    	    e.printStackTrace();
    	}
    	if (names == null) {
    	    return null;
    	}
    	ArrayList list = new ArrayList();
    	StringTokenizer tokenizer = new StringTokenizer(names, ":");
    	String key;
    	String name, clazz, tooltip;
    	Icon smallIcon, largeIcon;
    
    	int i=0;
    	while (tokenizer.hasMoreTokens()) {
    	    try {
    		key   = tokenizer.nextToken();
    		if (key.trim().equals("")) {
    		    continue;
    		}
    		name  = bundle.getString(key+".name").trim();
    		clazz = bundle.getString(key+".class").trim();
    		tooltip = bundle.getString(key+".tooltip").trim();
    		smallIcon = getIcon(bundle.getString(key+".smallIcon").trim());
    		largeIcon = getIcon(bundle.getString(key+".largeIcon").trim());
    		list.add(new AnalysisDescription(name, clazz, smallIcon, largeIcon, tooltip));
    		} catch (Exception e) {
    		System.out.println("Error while reading "+BUNDLE_NAME+".properties file: ");
    		e.printStackTrace();
    	    }
    	}
    	return(AnalysisDescription[])list.toArray(new AnalysisDescription[list.size()]);
        }
    */
    public static ImageIcon getIcon(String name) {
	URL url = GUIFactory.class.getResource("/org/tigr/microarray/mev/cluster/gui/impl/images/"+name);
	if (url == null)
	    return null;
	return new ImageIcon(url);
    }
    
}
