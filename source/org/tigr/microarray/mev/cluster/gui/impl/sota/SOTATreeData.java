/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTATreeData.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-03-24 15:51:44 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.util.FloatMatrix;

public class SOTATreeData {

    public float[] nodeHeights;    
    public int[] leftChild;    
    public int[] rightChild;    
    public int[] nodePopulation;
    
    public boolean absolute;
    public int function;
    public float factor;
    
    public int [] clusterPopulation;
    public FloatMatrix clusterDiversity;
    public int [] cluster;
    public FloatMatrix centroidMatrix;
    
    /** Creates new SOTATreeData */
    public SOTATreeData() {
    }
    
    /**
     * XMLEncoder/Decoder constructor
     *
     */
    public SOTATreeData(float[] nodeHeights, int[]leftChild, int[] rightChild, 
    		int[] nodePopulation, Boolean absolute, Integer function, Float factor, 
			int[] clusterPopulation, FloatMatrix clusterDiversity, int[] cluster, FloatMatrix centroidMatrix){
    	this.nodeHeights = nodeHeights;
    	this.leftChild = leftChild;
    	this.rightChild = rightChild;
    	this.nodePopulation = nodePopulation;
    	this.absolute = absolute.booleanValue();
    	this.function = function.intValue();
    	this.factor = factor.floatValue();
    	this.clusterPopulation = clusterPopulation;
    	this.clusterDiversity = clusterDiversity;
    	this.cluster = cluster;
    	this.centroidMatrix = centroidMatrix;
    }
    public void setCentroidMatrix(FloatMatrix centroidMatrix){
    	this.centroidMatrix = centroidMatrix;
    }
}
