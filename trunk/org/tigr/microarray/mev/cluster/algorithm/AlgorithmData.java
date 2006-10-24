/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmData.java,v $
 * $Revision: 1.6 $
 * $Date: 2006-10-24 16:27:59 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.util.FloatMatrix;

/**
 * This class is used to pass data and necessary parameters 
 * to a calculation algorithm, and to receive the result of 
 * calculation.
 */
public class AlgorithmData implements Serializable {

    private HashMap matrixes;
    private HashMap intMatrices;
    private HashMap intArrays;
    private HashMap stringArrays;
    private HashMap objectMatrices;
    private AlgorithmParameters parameters;
    private HashMap clusters;
    private HashMap resultMap;  //resultMap used in TEASE
    							//KEY: node number, value: algorithmData
    /**
     * Construct an <code>AlgorithmData</code>.
     */
    public AlgorithmData() {
        matrixes   = new HashMap();
        intMatrices = new HashMap();
        intArrays  = new HashMap();
        stringArrays = new HashMap();
        objectMatrices = new HashMap();
        parameters = new AlgorithmParameters();
        clusters = new HashMap();
        resultMap = new HashMap();
    }
    
    public void copy(AlgorithmData data) {
        this.matrixes   = data.matrixes;
        this.intMatrices = data.intMatrices;
        this.intArrays  = data.intArrays;
        this.stringArrays = data.stringArrays;
        this.objectMatrices = data.objectMatrices;
        this.parameters = data.parameters;
        this.clusters = data.clusters;
        this.resultMap = data.resultMap;
    }

    /**
     * Adds a matrix of float values by its name.
     *
     * @param name the name of the matrix.
     * @param matrix the <code>FloatMatrix</code> to be added.
     */
    public void addMatrix(String name, FloatMatrix matrix) {
        matrixes.put( name, matrix );
    }

        /**
     * Adds a matrix of int values by its name.
     *
     * @param name the name of the matrix.
     * @param matrix the <code>FloatMatrix</code> to be added.
     */
    public void addIntMatrix(String name, int [][] matrix) {
        intMatrices.put( name, matrix );
    } 
    
    /**
     * Adds a cluster by its name.
     *
     * @param name the name of the cluster.
     * @param cluster the <code>Cluster</code> to be added.
     */
    public void addCluster( String name, Cluster cluster ) {
        clusters.put( name, cluster );
    }

    /**
     * Gets a cluster by its name.
     *
     * @param name the name of the cluster.
     */
    public Cluster getCluster( String name ) {
        return(Cluster) clusters.get( name );
    }

    /**
     * Adds a parameter by its name.
     *
     * @param name the name of a parameter.
     * @param value the string which presents parameter value.
     */
    public void addParam( String name, String value ) {
        parameters.setProperty(name, value);
    }

    /**
     * Gets a matrix of float values by its name.
     *
     * @param name the name of <code>FloatMatrix</code>.
     */
    public FloatMatrix getMatrix(String name) {
        return(FloatMatrix)matrixes.get( name );
    }
    
    /**
     * Gets a matrix of float values by its name.
     *
     * @param name the name of <code>FloatMatrix</code>.
     */
    public int [][] getIntMatrix(String name) {
        return(int [][])intMatrices.get( name );
    }

    /**
     * Returns true if this data contains matrix.
     *
     * @param name the name of a matrix.
     */
    public boolean containsMatrix(String name) {
        return matrixes.containsKey(name);
    }

    /**
     * Adds a matrix of int values by its name
     *
     * @param name the name of the matrix.
     * @param intArray the array to be added.
     */
    public void addIntArray(String name, int[] intArray) {
        intArrays.put(name, intArray);
    }
    
    /**
     * Stores resultMap from TEASE analysis 
     * @param resultMap
     */
    public void addResultAlgorithmData(Integer key, AlgorithmData value) {
    	//System.out.println(key+ " added");
    	this.resultMap.put(key, value);
    }
    
    /**
     * Return the algorithmData corresponding to the key
     * @return value, algorithmData
     */
    public AlgorithmData getResultAlgorithmData(Object key) {
    	return (AlgorithmData)this.resultMap.get(key);
    }

    /**
     * Gets a matrix of int values by its name.
     *
     * @param name the name of matrix.
     */
    public int[] getIntArray(String name) {
        return(int[])intArrays.get(name);
    }


    /**
     * Adds a matrix of int values by its name
     *
     * @param name the name of the matrix.
     * @param intArray the array to be added.
     */
    public void addStringArray(String name, String [] stringArray) {
        stringArrays.put(name, stringArray);
    }

    /**
     * Gets a matrix of int values by its name.
     *
     * @param name the name of matrix.
     */
    public String[] getStringArray(String name) {
        return(String[])stringArrays.get(name);
    }
    
    public void addObjectMatrix(String name, Object objM [][]){
        this.objectMatrices.put(name, objM);
    }
    
    public Object [][] getObjectMatrix(String name){
        return (Object [][])(this.objectMatrices.get(name));
    }
    
    /**
     * Returns count of float matrixes.
     */
    public int size() {
        return matrixes.size();
    }

    /**
     * Returns float matrixes names.
     */
    public String[] getMatrixNames() {
        return getKeys(matrixes);
    }

    /**
     * Returns clusters names.
     */
    public String[] getClusterNames() {
        return getKeys(clusters);
    }

    private String[] getKeys(HashMap map) {
        Iterator iter = map.keySet().iterator();
        String[] result = new String[map.size()];
        int counter = 0;
        while (iter.hasNext())
            result[counter++] = (String)iter.next();
        return result;
    }

    /**
     * Gets parameters.
     */
    public AlgorithmParameters getParams() { return parameters;}

    // utility functions
    public Map getProperties() { return parameters.getMap(); }
    public Map getMatrixes()   { return matrixes; }
    public Map getIntArrays()  { return intArrays; }
    public Map getStringArrays() { return this.stringArrays; }
    public Map getObjectMatrices() { return this.objectMatrices; }
    public Map getClusters()   { return clusters;  }
  //CCC 4/6/06 for AMP
    public void setMatrixes(HashMap mtx) { matrixes = mtx;}
    public void setIntArrays(HashMap aintArray) {intArrays = aintArray;}   
    public void setParams(AlgorithmParameters ap) {parameters = ap;  }
   
 //CCC AMP end 
}
