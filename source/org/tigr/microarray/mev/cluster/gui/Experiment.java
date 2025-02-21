/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Experiment.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-08-22 17:53:19 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.util.FloatMatrix;

/**
 * This class is used to store experiment data (ratio values).
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class Experiment {
    
    /**
    * EH this count is used as a unique identifier for each newly-created Experiment
    */ 
    public static int exptCount = 1;
    // matrix of ratio values
    private FloatMatrix matrix; 
    // data indices 
    private int[] columns;
    // gene indices to map from a FloatMatrix that is a subset of the
    //full dataset back to a row index in the MultipleArrayData.
    private int[] rowMapping;
    private int id;

    /**
     * State-saving constructor for v4.0 - 4.3. Kept for backwards-compatibility with
     * older saved state files.
     * @param columns
     * @param rows
     * @param id
     * @param fm
     */
    public Experiment(int[] columns, int[] rows, int id, FloatMatrix fm) {
    	this(null, columns, rows);
    	this.matrix = fm;
    }
    /**
     * State-saving constructor for v4.4 and up
     * @param columns
     * @param rows
     * @param id
     * @param fm
     */
    public Experiment(FloatMatrix columns, FloatMatrix rows, int id, FloatMatrix fm) {
    	this(null, Cluster.getIndices(columns), Cluster.getIndices(rows));
    	this.matrix = fm;
    }

    /**
     * Constructs an <code>Experiment</code> with specified
     * matrix of ratio values and columns indices.
     */
    public Experiment(FloatMatrix matrix, int[] columns) {
        this(matrix, columns, makeDefaultRowMapping( matrix.getRowDimension() ));
    }
    
    /**
     * Constructs an <code>Experiment</code> with specified
     * matrix of ratio values, columns indices, and row indices
     */
    public Experiment(FloatMatrix matrix, int[] columns, int[] rows){
    	exptCount++;
        this.matrix = matrix;
        this.columns = columns;
        this.rowMapping = rows;
        if (new Integer(this.id).equals(null))
        	this.id = exptCount;
        
       }
    
    public int[] getRows() {return rowMapping;}
    public int[] getColumns(){return columns;}

    
    public int getId(){return this.id;}
    
    public void fillMatrix(FloatMatrix fm){
    	this.matrix = fm;
    }
    

    
    /**
     * Makes default sequential mapping of row indices
     */
    private static int[] makeDefaultRowMapping(int numberOfRows){
        int [] rowMap = new int[numberOfRows];
        for(int i = 0; i < numberOfRows; i++){
            rowMap[i] = i;
        }
        return rowMap;
    }
    
    /**
     * Returns a copy of the row mapping array.  This array provides
     * a mapping from a row in the Experiment to a IData index.
     */
    public int [] getRowMappingArrayCopy(){
        int [] copyArray = new int[this.rowMapping.length];
        System.arraycopy(this.rowMapping, 0, copyArray, 0, copyArray.length);
        return copyArray;
    }
    
    /**
     *  Returns a copy of the column indices
     */
    public int [] getColumnIndicesCopy() {
        int [] copyArray = new int[this.columns.length];
        System.arraycopy(this.columns, 0, copyArray, 0, copyArray.length);
        return copyArray;
    }
    
    /**
     * Returns clone of this <code>Experiment</code>.
     */
    public Experiment copy() {
        int[] columns = new int[this.columns.length];
        System.arraycopy(this.columns, 0, columns, 0, this.columns.length);
        return new Experiment(matrix.copy(), columns, getRowMappingArrayCopy());
    }
    
    /**
     * Returns ratio value.
     */
    public float get(int i, int j) {
        return matrix.get(i, j);
    }
    
    /**
     * Returns array of ratio values.
     */
    public float[][] getValues() {
        return matrix.A;
    }
    
    /**
     * Returns float matrix of ratio values.
     */
    public FloatMatrix getMatrix() {
        return matrix;
    }
    
    /**
     * Returns column index in micro array.
     */
    public int getSampleIndex(int column) {
        return columns[column];
    }
    
    /**
     * Returns row index in MultipleArrayData that maps from passed row
     * index.
     */
    public int getGeneIndexMappedToData(int row){
        if( row >= 0 && row < rowMapping.length)
            return rowMapping[row];
        else
            return -1;
    }
    
    /**
     * Returns count of genes.
     */
    public int getNumberOfGenes(){
        return matrix.getRowDimension();
    }
    
    /**
     * Returns count of columns in this experiment.
     */
    public int getNumberOfSamples() {
        return matrix.getColumnDimension();
    }
    
    public float[] getMinAndMax(){
        float val = 0;
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        float [] minMax = new float[2];
        minMax[0] = Float.POSITIVE_INFINITY;
        minMax[1] = Float.NEGATIVE_INFINITY;
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < cols; col++){
                val = matrix.get(row,col);
                if(!Float.isNaN(val)){
                    if(val < minMax[0])
                        minMax[0] = val;
                    if(val > minMax[1])
                        minMax[1] = val;
                }
            }
        }
        return minMax;
    }
    
    public float getMaxAbsValue(){
        float [] minMax = getMinAndMax();
        return Math.max(Math.abs(minMax[0]), Math.abs(minMax[1]));
    }
}
