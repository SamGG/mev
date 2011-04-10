/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: LengthBiasPValues.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/08/10 15:45:20 $
 * $Author: dschlauch $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Arrays;


public class LengthBiasPValues {

	
	private float[] sigGenes;
	private float[] probWeightFunction;
	private int[][] genesets;
	private float numSig;
	private float percentSig;
	private float[] sigPerGeneSet, expectedSigPerGeneSet;
	
	/**
	 * 
	 * @param sig
	 * @param pwf
	 * @param genesets
	 */
	public LengthBiasPValues(float[] sig, float[] pwf, int[][] genesets){
		this.sigGenes = sig;
		this.probWeightFunction = pwf;
		this.genesets = genesets;
		numSig = 0;
		for (int i=0; i<sigGenes.length; i++)
			numSig = numSig+sigGenes[i];
		percentSig = numSig/sig.length ;
		if (probWeightFunction==null)
			probWeightFunction = createFakePWF();
		expectedSigPerGeneSet = calcExpectedSigPerGeneSet();
		
	}
	public float[] getExpectedSigPerGeneSet(){
		return expectedSigPerGeneSet;
	}
	public float[] getSigPerGeneSet(){
		return sigPerGeneSet;
	}
	private float[] calcExpectedSigPerGeneSet() {
		float[] expSigPer = new float[genesets.length];
		for (int i=0; i<genesets.length; i++){
			float gsTot = 0f;
			for (int j=0; j<genesets[i].length; j++){
				gsTot = gsTot + probWeightFunction[genesets[i][j]];
			}
			expSigPer[i]=gsTot;
		}
		return expSigPer;
	}
	/**
	 * For development purposes, creates fake PWF with no respect to transcript length...
	 * @return
	 */
	private float[] createFakePWF() {
		int numGenes = sigGenes.length;
		float[] pwf = new float[numGenes];
		for (int i=0; i<numGenes; i++){
			pwf[i] = percentSig;
		}
		return pwf;
	}
	
	/**
	 * Master function, returns an array of floats representing the p-values of each gene set.
	 * @param numPerms
	 * @return
	 */
	public float[] getPValuesForGeneSets(int numPerms){
		int numGeneSets = genesets.length;
		float[][] permSigPerGeneset = new float[numGeneSets][numPerms];
		float[] permSig=new float[0];
		for (int i=0; i<numPerms; i++){
			permSig = getPermSig(probWeightFunction);
			float[] sigPerGeneSet = getSigPerGeneSet(genesets, permSig);
			for (int j=0; j<numGeneSets; j++){
				permSigPerGeneset[j][i] = sigPerGeneSet[j];
			}
		}
		for (int i=0; i<numGeneSets; i++){
			Arrays.sort(permSigPerGeneset[i]);
		}
		sigPerGeneSet = getSigPerGeneSet(genesets, sigGenes);
		float[] geneSetPValues = new float[numGeneSets];
			
		for (int i=0; i<numGeneSets; i++){
			int j;
			for (j=0; j<permSigPerGeneset[i].length; j++){
				if (permSigPerGeneset[i][permSigPerGeneset[i].length-(j+1)] < sigPerGeneSet[i])
					break;
			}
			geneSetPValues[i] = (float)j/(float)numPerms;
		}
		return geneSetPValues;
	}
	
	/**
	 * For use in permutation analysis, this method uses the pwf to 
	 * generate a list of genes chosen to be differentially expressed 
	 * according to the pwf.
	 * @param pwf 
	 * @return an array of differentially expressed genes given the 
	 * null hypothesis of differential expression based on pwf.  Array 
	 * is of length=numGenes and populated by 1's and 0's.
	 */
	private float[] getPermSig(float[] pwf) {
		float[] permSig = new float[pwf.length];
		for (int i=0; i<pwf.length; i++){
			if (Math.random()>pwf[i])
				continue;
			permSig[i] = 1;
		}
		return permSig;
		
	}
	
	/**
	 * Alternative way of calculating permutations of differentially
	 * expressed gene lists.
	 * @param pwf
	 * @param sigGeneSize
	 * @return
	 */
	private int[] getPermSig2(float[] pwf, int sigGeneSize) {
		int[] permSig = new int[pwf.length];
		int i=0;
		while (i<sigGeneSize){
			int selected = (int)(Math.random()*permSig.length);
			if (permSig[selected]==1)
				continue;
			if (Math.random()>pwf[selected])
				continue;
			permSig[selected] = 1;
			i++;
		}
		return permSig;		
	}

	/**
	 * Given a set of gene sets, and a list of differentially expressed genes,
	 * this method finds the number of significant genes in each set.
	 * @param genesets set of gene sets
	 * @param sig int array of differentially expressed genes
	 * @return
	 */
	private float[] getSigPerGeneSet(int[][] genesets, float[] sig) {
		float[] sigPerGeneSet = new float[genesets.length];
		for (int i=0; i<genesets.length; i++){
			for (int j=0; j<genesets[i].length; j++){
				if (sig[genesets[i][j]]==1)
					sigPerGeneSet[i]++;
			}
		}
		return sigPerGeneSet;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		float[] sig = {1,1,1,1,1,0,1,0,1,0,0,0,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,1,0};
		float[] pwf = {	.1f,.2f,.5f,.1f,.61f,.6f,.7f,.02f,.4f,.5f,.4f,.3f,.13f,.1f,.2f,.05f,.1f,.11f,.6f,.7f,.02f,.04f,.5f,.4f,.3f,.13f,.1f,.2f,.05f,.1f,.11f,.6f};	
		int[][] genesets = 
			{	{0,1,3,5,7,9,11},
				{2,4,6,8,10,12},
				{13,14,15,16},
				{17,18,19,20,21,22,23,24,25,26},
				{27,28,29,30,31}	};
		
		int numPerms = 100000;
		
		LengthBiasPValues obj = new LengthBiasPValues(sig, pwf, genesets);
		obj.getPValuesForGeneSets(numPerms);
		for (int i=0; i<obj.sigPerGeneSet.length; i++){
			System.out.println(obj.sigPerGeneSet[i]);
		}
	}
}
