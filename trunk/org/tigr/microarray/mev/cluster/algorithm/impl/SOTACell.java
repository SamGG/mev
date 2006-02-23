/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTACell.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:45 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class SOTACell{
    
    public FloatMatrix dataMatrix;
    
    public SOTACell parent;
    public SOTACell left;
    public SOTACell right;
    public SOTACell pred;  //thread cells
    public SOTACell succ;
    
    public  FloatMatrix centroidGene;
    
    public Vector members;
    public double cellDiversity;
    public double cellVariance;
    private int numberOfSamples;
    
    public boolean changedMembership;
    
    public SOTACell(int NumberOfSamples, FloatMatrix DataMatrix){
	
	dataMatrix = DataMatrix;
	numberOfSamples =NumberOfSamples;
	changedMembership = false;
	
	centroidGene = new FloatMatrix(1,numberOfSamples);
	
	for(int i = 0; i < numberOfSamples; i++)
	    centroidGene.set(0,i, 0);
	
	members = new Vector();
	cellDiversity = 0;
	cellVariance = 0;
	parent = left = right = pred = succ = null;
    }
    
    
    public void removeMember(int geneNum){
	for(int i = 0; i < members.size() ; i++){
	    if(geneNum == ((Integer)members.elementAt(i)).intValue()){
		members.removeElementAt(i);
		changedMembership = true;  //changedMemebership
		break;
	    }
	}
    }
    
    
    public void migrateCentroid(int geneIndex, float nu){
	
	for(int i = 0; i < numberOfSamples; i++){
	    if(!Float.isNaN(dataMatrix.get(geneIndex,i)))
		centroidGene.set(0,i, (float)(centroidGene.get(0,i) + ((nu)*( dataMatrix.get(geneIndex,i) - centroidGene.get(0,i) ) )));
	}
	
    }
    
    
    
    public float getColumnVar(int index){
	int n = members.size();
	float sum = 0;
	int geneIndex;
	int numOfNaN = 0;
	
	float currVal;
	for(int i = 0 ; i < n; i++){
	    geneIndex = ((Integer)(members.elementAt(i))).intValue();
	    currVal = dataMatrix.get(geneIndex, index);
	    
	    if(!Float.isNaN(currVal)){
		sum += Math.pow(currVal - centroidGene.get(0,index) , 2 );
		numOfNaN++;
	    }
	    
	}
	if(numOfNaN > 1)
	    return (float)Math.sqrt(sum/(numOfNaN-1));
	else
	    return 0;
    }
    
    
    public void addMember(int geneNum){
	
	members.add(new Integer(geneNum));
	changedMembership = true;
    }
    
    
    public SOTACell findSister(){
	
	if(this != null){
	    if(this.parent.left == this)
		return this.parent.right;
	    else
		return this.parent.left;
	}
	else
	    return null;
    }
} //end SOTACell
