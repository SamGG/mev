/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLCluster.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-04-10 18:41:37 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

import java.awt.Color;

public class HCLCluster {
    
    public int root; // root index
    public int firstElem;
    public int lastElem;
    public Color color; // color of the cluster.
    public String text; // cluster description.
    public boolean isGeneCluster;
    public int size;
    /**
     * Constructs a <code>HCLCluster</code> with specified root,
     * firstRow and lastRow indices.
     */
    public HCLCluster(int root, int firstElem, int lastElem) {
	this.root = root;
	this.firstElem = firstElem;
	this.lastElem = lastElem;
        this.isGeneCluster = true;
        this.size = (lastElem - firstElem) + 1;
    }
    
    public void setFinalSize(){
        this.size = (this.lastElem - this.firstElem) + 1;
    }
    
    //EH These methods and constructor are provided to present a JavaBean
    //interface for XMLEncoder/XMLDecoder serializing.
    public HCLCluster() {
    	this.isGeneCluster = true;
    }
    public void setRoot(int r){this.root = r;}
    public void setFirstElem(int fe) {
    	this.firstElem = fe;
    	try {
    		this.size = (lastElem = firstElem)+1;
    	} catch (NullPointerException npe){}
    }
    public void setLastElem(int le) {
    	this.lastElem = le;
    	try {
    		this.size = (lastElem = firstElem)+1;
    	} catch (NullPointerException npe){}
    }
    public int getRoot() {return this.root;}
    public int getFirstElem() {return this.firstElem;}
    public int getLastElem() {return this.lastElem;}
    //End beanifying methods

    
}
