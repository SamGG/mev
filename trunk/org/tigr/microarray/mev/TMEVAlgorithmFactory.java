/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TMEVAlgorithmFactory.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.util.StringTokenizer;

import org.tigr.util.ConfMap;

import org.tigr.remote.RemoteAlgorithm;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.remote.communication.CommunicatorFactory;

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
	if (className != null && !className.equals("null")) {
	    try {
		Class clazz = Class.forName(className);
		localFactory = (AlgorithmFactory)clazz.newInstance();
	    } catch (Exception e) {
		System.out.println("Local factory not available, check the 'algorithm.factory.class' key in cfg file.");
		e.printStackTrace();
	    }
	} else {
	    System.out.println("Local factory not available, check the 'algorithm.factory.class' key in cfg file.");
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
