/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jun 2, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

/**
 * @author iVu
 */
public class USCDeltaRhoResult {
    private double delta;
    private double rho;
    private int mistake;
    private int correct;
    private int numGene;
    private boolean isNull;
    
    
    public USCDeltaRhoResult(double deltaP, double rhoP, int mistakeP, 
    		int correctP, int numGeneP) {
        this.delta = deltaP;
        this.rho = rhoP;
        this.mistake = mistakeP;
        this.correct = correctP;
        this.numGene = numGeneP;
        this.isNull = false;
    }
    public USCDeltaRhoResult() {
    	this.isNull = true;
    }
    
    
    public double getDelta() {
        return this.delta;
    }
    public double getRho() {
        return this.rho;
    }
    public int getMistake() {
        return this.mistake;
    }
    public int getCorrect() {
    	return this.correct;
    }
    public int getNumGene() {
        return this.numGene;
    }
    public boolean isNull() {
    	return this.isNull;
    }
}
