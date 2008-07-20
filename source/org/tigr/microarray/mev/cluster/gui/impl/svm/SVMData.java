/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SVMData.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-03-24 15:51:53 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;

public class SVMData {

    public SVMData() {
        constant = 1.0f;
        coefficient = 1.0f;
        power = 1.0f;
        diagonalFactor = 1.0f;
        convergenceThreshold = 0.00001f;
        radial = false;
        normalize = false;
        widthFactor = 1.0f;
        constrainWeights = true;
        positiveConstraint = 1.0f;
        negativeConstraint = 1.0f;
        positiveDiagonal = 0.0f;
        negativeDiagonal = 0.0f;
        classificationFile = null;
        classifyGenes = true;
        calculateHCL = false;
        calcSampleHCL = false;
        calcGeneHCL = false;
        hclMethod = 0;
        distanceFunction = Algorithm.EUCLIDEAN;
        useEditor = true;
    }
    
    public SVMData(SVMData data){
        constant = data.constant;
                coefficient = data.coefficient;
        power = data.power;
        diagonalFactor = data.diagonalFactor;
        convergenceThreshold = data.convergenceThreshold;
        radial = data.radial;
        normalize = data.normalize;
        widthFactor = data.widthFactor;
        constrainWeights = data.constrainWeights;
        positiveConstraint = data.positiveConstraint;
        negativeConstraint = data.negativeConstraint;
        positiveDiagonal = data.positiveDiagonal;
        negativeDiagonal = data.negativeDiagonal;
        classificationFile = data.classificationFile;
        classifyGenes = data.classifyGenes;
        calculateHCL = data.calculateHCL;
        calcSampleHCL = data.calcSampleHCL;
        calcGeneHCL = data.calcGeneHCL;
        hclMethod = data.hclMethod;
        distanceFunction = data.distanceFunction;
        useEditor = data.useEditor;
        seed = data.seed;
        objective1 = data.objective1;
        twoSquaredWidth = data.twoSquaredWidth;        
    }
    
    public float constant;                /* Constant to add to kernel. */
    public float coefficient;             /* Linear multiplier on kernel. */
    public float power;                   /* Power to raise the kernel to. */
    public float diagonalFactor;          /* This one is complicated... */
    public float convergenceThreshold;    /* Delta objective at which to stop. */
    public boolean radial;                /* Create radial basis kernel? */
    public boolean normalize;             /* Normalize rows of training matrix? */
    public float widthFactor;             /* Multiplicative factor on width. */
    public float positiveConstraint;      /* Maximum allowed weight for positives. */
    public float negativeConstraint;      /* Maximum allowed weight for negatives. */
    public boolean constrainWeights;      /* Prevent weights from exceeding 1? */
    public File classificationFile;
    public boolean useEditor;
    
    public int distanceFunction;
    public boolean absoluteDistance;
    public float positiveDiagonal;         /* Add to diagonal of kernel matrix. */
    public float negativeDiagonal;         /* Add to diagonal of kernel matrix. */
    
    public long seed = 0;
    public float objective1;
    public float twoSquaredWidth;
    public boolean classifyGenes;	   /* Classify genes, or if not genes then experiments */
    public boolean calculateHCL;
    public boolean calcGeneHCL;
    public boolean calcSampleHCL;
    public int hclMethod;
    
    public boolean validate(JDialog parent){
        if(diagonalFactor < 0){
            JOptionPane.showMessageDialog(parent, "Diagonal Factor must be >= 0", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(radial && widthFactor < 0){
            JOptionPane.showMessageDialog(parent, "Width Factor must be >= 0", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(convergenceThreshold <= 0){
            JOptionPane.showMessageDialog(parent, "Threshold must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
}
