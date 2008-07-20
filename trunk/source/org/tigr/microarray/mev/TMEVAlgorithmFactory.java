/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TMEVAlgorithmFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.util.StringTokenizer;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.remote.RemoteAlgorithm;
import org.tigr.remote.communication.CommunicatorFactory;
import org.tigr.util.ConfMap;

public class TMEVAlgorithmFactory implements AlgorithmFactory {
    
    private ConfMap cfg;
    private AlgorithmFactory localFactory;
    private boolean isRemoteEnabled = false;
    
    /**
     * Constructs a <code>TMEVAlgorithmFactory</code> using specified
     * configuration.
     * @see ConfMap
     */
    public TMEVAlgorithmFactory(ConfMap cfg) {
    	this.cfg = cfg;
		// local
		String className = cfg.getString("algorithm.factory.class");
		if (className == null || className.equals("null")) {
			className = "org.tigr.microarray.mev.cluster.algorithm.impl.AlgorithmFactoryImpl";
		    System.out.println("Local factory not available, check the 'algorithm.factory.class' key in properties file.");
		}
	    try {
	    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    	Class clazz = Class.forName(className, true, cl);
	    	localFactory = (AlgorithmFactory)clazz.newInstance();
	    } catch (Exception e) {
	    	System.out.println("Local factory not available, check the 'algorithm.factory.class' key in properties file.");
	    	e.printStackTrace();
	    }
		// remote
		try {
		    CommunicatorFactory.init(cfg);
		    this.isRemoteEnabled = true;
		} catch (Exception e) {
		    System.out.println("Failed to configure remote execution.");
		    e.printStackTrace();
		}
    }
    
    /**
     * Returns an instance of algorithm by its name.
     * @throws AlgorithmException if specified algo is not accessible.
     */
    public Algorithm getAlgorithm(String name) throws AlgorithmException {
	if (name == null) {
	    throw new AlgorithmException("Algorithm name expected.");
	}
	if (isRemote(name)) {
	    if (this.isRemoteEnabled) {
		return new RemoteAlgorithm(name);
	    }
	    throw new AlgorithmException("Remote execution not available.");
	}
	if (this.localFactory == null) {
	    throw new AlgorithmException("Local execution not available.");
	}
	return this.localFactory.getAlgorithm(name);
    }
    
    /**
     * Returns true if algorithm with a specified name was configured
     * as remote.
     */
    private boolean isRemote(String name) {
	String remoute = this.cfg.getString("algorithm.remote");
	if (remoute == null) {
	    return false;
	}
	StringTokenizer tokenizer = new StringTokenizer(remoute, ":");
	String str;
	while (tokenizer.hasMoreTokens()) {
	    str = tokenizer.nextToken();
	    if (str.trim().toUpperCase().equals(name.toUpperCase())) {
		return true;
	    }
	}
	return false;
    }
}
