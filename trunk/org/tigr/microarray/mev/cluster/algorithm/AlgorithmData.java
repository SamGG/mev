/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: AlgorithmData.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm;

import java.util.*;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;

/**
 * This class is used to pass data and necessary parameters 
 * to a calculation algorithm, and to receive the result of 
 * calculation.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class AlgorithmData {

    private HashMap matrixes;
    private HashMap intArrays;
    private AlgorithmParameters parameters;
    private HashMap clusters;

    /**
     * Construct an <code>AlgorithmData</code>.
     */
    public AlgorithmData() {
        matrixes   = new HashMap();
        intArrays  = new HashMap();
        parameters = new AlgorithmParameters();
        clusters = new HashMap();

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
     * Gets a matrix of int values by its name.
     *
     * @param name the name of matrix.
     */
    public int[] getIntArray(String name) {
        return(int[])intArrays.get(name);
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
    public Map getClusters()   { return clusters;  }
}
