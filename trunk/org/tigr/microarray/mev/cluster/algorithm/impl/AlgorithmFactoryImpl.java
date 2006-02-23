/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmFactoryImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:45 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;

public class AlgorithmFactoryImpl implements AlgorithmFactory {
    
    private static ResourceBundle bundle;
    private static String BUNDLE_NAME = "org.tigr.microarray.mev.cluster.algorithm.impl.factory";
    
    public Algorithm getAlgorithm(String name) throws AlgorithmException {
	if (name == null) {
	    throw new AlgorithmException("Algorithm name expected.");
	}
	if (bundle == null) {
	    throw new AlgorithmException("Can't find bundle for base name "+BUNDLE_NAME);
	}
	try {
	    Class clazz = Class.forName(bundle.getString(name).trim());
	    return(Algorithm)clazz.newInstance();
	} catch (MissingResourceException e) {
	    throw new AlgorithmException("There is no such algorithm: "+ name);
	} catch (ClassCastException e) {
	    throw new AlgorithmException("Error: org.tigr.microarray.mev.cluster.algorithm.Algorithm interface expected.");
	} catch (Exception e) {
	    throw new AlgorithmException(e);
	}
    }
    
    static {
	try {
	    bundle = ResourceBundle.getBundle(BUNDLE_NAME);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
