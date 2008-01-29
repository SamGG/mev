/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterWorker.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:46 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.Experiment;

public class ClusterWorker {  
    
    /** The repository which contains the cluster operators
     *
     */
    ClusterRepository repository;
    /** Creates new ClusterWorker */
    public ClusterWorker( ClusterRepository repository ) {
        this.repository = repository;
    }
    
    /** Returns a Cluster containing the intersection
     * of member indices from the passed Cluster array.
     */
    public Cluster intersection(Cluster [] clusters) {
        Integer index;
        HashSet [] sets = new HashSet[clusters.length];
        
        int minSize = 0;
        
        for(int i = 0; i < sets.length; i++){
            sets[i] = clusters[i].getHashSet();
            minSize = Math.min(minSize, clusters[i].getSize());
        }
        
        HashSet resultSet = new HashSet(); //new HashSet(minSize);

        resultSet.addAll(intersect(sets));
     
        Iterator iterator = resultSet.iterator();
        int size = resultSet.size();
        int [] result = new int[size];
        for(int i = 0; i < size; i++){
            result[i] = ((Integer)(iterator.next())).intValue();
        }
        return buildResultCluster("Intersection", clusters, result);       
    }
    
    /** Accumulates cluster operation results and returns a cluster.
     */
    private Cluster buildResultCluster(String operation, Cluster [] clusters, int [] indices){
        String clusterNumbers = "";
        int i = 0;
        for(; i < clusters.length-1; i++)
            clusterNumbers += clusters[i].getSerialNumber()+":";
        if(clusters.length-1 > -1)
            clusterNumbers += clusters[clusters.length-1].getSerialNumber();
        
        //in the case that experiment clusters are being intersected with possibly
        //different gene members, the most restrictive gene set is preserved.
        Experiment experiment = getMinExperiment(clusters);

       ClusterAttributesDialog dialog = new ClusterAttributesDialog("Cluster Operation: "+operation+"( "+clusterNumbers+" )", "Cluster Op", operation+"( "+clusterNumbers+" )", null, null, Color.lightGray);
        if(dialog.showModal() == JOptionPane.OK_OPTION)        
            return new Cluster(indices, "Cluster Op.", dialog.getLabel(), operation+"( "+clusterNumbers+" )", "", dialog.getDescription(), -1, repository.takeNextClusterSerialNumber(), dialog.getColor(), experiment);            
        else
            return null;
    }
    
    /** Accumulates cluster operation results and returns a cluster.
     *
     */
    private Cluster buildBasicCluster(String operation, Cluster [] clusters, int [] indices){
        String clusterNumbers = "";
        int i = 0;
        for(; i < clusters.length-1; i++)
            clusterNumbers += clusters[i].getSerialNumber()+":";
        if(clusters.length-1 > -1)
            clusterNumbers += clusters[clusters.length-1].getSerialNumber();
        
        //in the case that experiment clusters are being intersected with possibly
        //different gene members, the most restrictive gene set is preserved.
        Experiment experiment = getMinExperiment(clusters);

        return new Cluster(indices, "Cluster Op.", "", operation+"( "+clusterNumbers+" )", "", "", -1, -1, Color.lightGray, experiment);
    }
    
    public Experiment getMinExperiment(Cluster [] clusters){
        Experiment smallestExp = clusters[0].getExperiment();
        Experiment currentExp = smallestExp;
        for(int i = 0; i < clusters.length; i++){
            currentExp = clusters[i].getExperiment();
            if(currentExp.getNumberOfGenes() < smallestExp.getNumberOfGenes())
                smallestExp = currentExp;
        }
        return smallestExp;
    }
    
    
    /** Helper method to perform cluster intersections
     */
    private HashSet intersect(HashSet [] sets){
        HashSet result = intersect(sets[0], sets[1]);
        
        if(sets.length == 2)
            return result;
        else{
            for(int i = 2; i < sets.length; i++){
                result = intersect(result, sets[i]);
            }
        }
        return result;
    }
    
    private HashSet intersect(HashSet setOne, HashSet setTwo){
        HashSet result = new HashSet();
        Iterator iterator = setOne.iterator();
        Integer currVal;
        while(iterator.hasNext()){
            currVal = (Integer)(iterator.next());
            if(setTwo.contains(currVal))
                result.add(currVal);
        }
        return result;
    }
    
    /** Returns a Cluster representing the union of the members of the
     * clusters argument
     */
    public Cluster union(Cluster [] clusters){
        int [] result = getUniqueIndices(clusters);
        return buildResultCluster("Union", clusters, result);
    }
    
    
    /** returns the unique indices among the indices of a group of clustes.
     */
    public int [] getUniqueIndices(Cluster [] clusters){
        
        int [][] indices = new int[clusters.length][];
        
        int count = 0;
        Cluster cluster;
        for(int i = 0; i < clusters.length; i++){
            cluster = clusters[i];
            indices[i] = cluster.getIndices();
            count += indices[i].length;
        }
        int [] resultIndices = new int[count];
        
        int cnt = 0;
        for(int i = 0; i < indices.length;i++){
            for(int j = 0; j < indices[i].length; j++){
                resultIndices[cnt] = indices[i][j];
                cnt++;
            }
        }
        resultIndices = makeIndicesUnique(resultIndices);
        return resultIndices;
        
    }
    
    /** Helper method to eliminate redundant indices.
     */
    private int [] makeIndicesUnique(int [] indices){
        
        int numberOfElements = repository.getDataElementCount();
        
        boolean [] indexCheck = new boolean[numberOfElements];
        
        int uniqueCount = 0;
        for(int i = 0; i < indices.length; i++){
            if(!indexCheck[indices[i]]){
                indexCheck[indices[i]] = true;
                uniqueCount++;
            }
        }
        int [] newIndices = new int[uniqueCount];
        int cnt = 0;
        for(int i = 0; i < indexCheck.length; i++){
            if(indexCheck[i]){
                newIndices[cnt] = i;
                cnt++;
            }
        }
        return newIndices;
    }
    
    /** Returns an array of cluster labels.
     */
    public String [] getClusterLabels(Cluster [] clusters){
        String [] labels = new String[clusters.length];
        for(int i = 0; i < labels.length; i++){
            labels[i] = clusters[i].getClusterLabel();
        }
        return labels;
    }
    
    public Cluster xor(Cluster [] clusters){
       // int serialNumber = this.repository.getMaxClusterSerialNumber();
        Cluster union = this.utilityUnion(clusters);
        Cluster inter = this.utilityIntersection(clusters);
       // this.repository.setClusterSerialCounter(serialNumber);  //roll back cluster serial numbers
                                                        //following cluster creation for union and intersections
        HashSet unionSet = union.getHashSet();
        HashSet interSet = inter.getHashSet();
        
        Iterator iterator = interSet.iterator();
        while(iterator.hasNext()){
            unionSet.remove(iterator.next());
        }
        return this.buildResultCluster("XOR", clusters, getIndices(unionSet));
    }
    
    
    /** Returns a Cluster representing the union of the members of the
     * clusters argument
     */
    public Cluster utilityUnion(Cluster [] clusters){
        int [] result = getUniqueIndices(clusters);
        return buildBasicCluster("Union", clusters, result);
    }
    
        /** Returns a Cluster containing the intersection
     * of member indices from the passed Cluster array.
     */
    public Cluster utilityIntersection(Cluster [] clusters) {
        Integer index;
        HashSet [] sets = new HashSet[clusters.length];
        
        int minSize = 0;
        
        for(int i = 0; i < sets.length; i++){
            sets[i] = clusters[i].getHashSet();
            minSize = Math.min(minSize, clusters[i].getSize());
        }
        
        HashSet resultSet = new HashSet(); //new HashSet(minSize);

        resultSet.addAll(intersect(sets));
     
        Iterator iterator = resultSet.iterator();
        int size = resultSet.size();
        int [] result = new int[size];
        for(int i = 0; i < size; i++){
            result[i] = ((Integer)(iterator.next())).intValue();
        }
        return buildBasicCluster("Intersection", clusters, result);       
    }
    
    private int [] getIndices(HashSet set){
        Object [] a = set.toArray();
        int [] indices = new int[a.length];
        for(int i = 0; i < a.length; i++)
            indices[i] = ((Integer)a[i]).intValue();
        return indices;      
    }   
}

