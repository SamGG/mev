/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Cluster.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-05-18 20:07:49 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.Color;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.util.HashSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.util.FloatMatrix;
//import org.tigr.microarray.mev.cluster.gui.impl.ptm.PTMExperimentHeader;

/** The Cluster class encapsulates information required to define a cluster.
 * Methods include standard set... and get... methods to access and alter
 * the cluster definition.
 */
public class Cluster {

    public static final int GENE_CLUSTER = 0;
    public static final int EXPERIMENT_CLUSTER = 1;
    
    /** Cluster color
     */    
    private Color clusterColor;
    /** Element indices in IData.
     */    
    private int [] indices;
    /** Indices to the Experiment Object's matrix
     */
    private int [] experimentIndices;
    /** Cluster source type
     */    
    private String source;
    /** Cluster origin node name
     */    
    private String clusterID;
    /** User defined cluster label
     */    
    private String clusterLabel;
    /** Algorithm node name
     */    
    private String algorithmName;
    /** Cluster remarks/description
     */    
    private String clusterDescription;
    /** Result index on result tree.
     */    
    private int algorithmIndex;
    /** Cluster sequential serial numbe
     */    
    private int serialNumber;
    /** Cluster node of origin, possibly null.
     */    
    private DefaultMutableTreeNode node;
    /** Cluster's Experiment of origin
     */
    private Experiment experiment;
    /** Node objects userObject
     */
    private Object userObject;
    /** boolean to indicate if cluster color should be displayed
     */
    private boolean isShowColor;

    //EH
    private int exptID = 0;

    /** 
     * EH Creates new cluster object
     * @deprecated Left in place for compatibility with saved analysis files from
     * an early release of v4.0b.
     */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, 
    		String clusterID, String clusterDescription, Integer index, 
			Integer serialNumber, Color clusterColor, Integer exptID) {
        this.indices = indices;
        this.source = source;
        this.clusterLabel = clusterLabel;
        this.clusterID = clusterID;
        this.algorithmName = algorithmName;
        this.algorithmIndex = index.intValue();
        this.clusterColor = clusterColor;
        this.clusterDescription = clusterDescription;
        this.serialNumber = serialNumber.intValue();
        this.exptID = exptID.intValue();
        this.isShowColor = true;
    }
    /** Creates new cluster object
     */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, int index, int serialNumber, Color clusterColor, Experiment experiment) {
    	this.indices = indices;
        this.source = source;
        this.clusterLabel = clusterLabel;
        this.clusterID = clusterID;
        this.algorithmName = algorithmName;
        this.algorithmIndex = index;
        this.clusterColor = clusterColor;
        this.clusterDescription = clusterDescription;
        this.serialNumber = serialNumber;
        this.experiment = experiment;
        this.isShowColor = true;
        this.experimentIndices = getIndicesMappedToExperiment();
    }
    
    /**
     * State-saving constructor for v4.0b - v4.3
     * @param indices
     * @param source
     * @param clusterLabel
     * @param algorithmName
     * @param clusterID
     * @param clusterDescription
     * @param index
     * @param serialNumber
     * @param clusterColor
     * @param node
     * @param experiment
     */
    public Cluster(FloatMatrix fm, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, Integer index, Integer serialNumber, Color clusterColor, DefaultMutableTreeNode node, Experiment experiment) {
    	this(getIndices(fm), source, clusterLabel, algorithmName, clusterID, clusterDescription, index.intValue(), serialNumber.intValue(), clusterColor, null, experiment);
    }    

    /**
     * state-saving cluster v4.4 and higher
     * @param indices
     * @param source
     * @param clusterLabel
     * @param algorithmName
     * @param clusterID
     * @param clusterDescription
     * @param index
     * @param serialNumber
     * @param clusterColor
     * @param node
     * @param experiment
     */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, Integer index, Integer serialNumber, Color clusterColor, DefaultMutableTreeNode node, Experiment experiment) {
    	this(indices, source, clusterLabel, algorithmName, clusterID, clusterDescription, index.intValue(), serialNumber.intValue(), clusterColor, null, experiment);
//      TODO
    }    
    
   /** Creates new cluster object
    */
    public Cluster(int [] indices, String source, String clusterLabel, String algorithmName, String clusterID, String clusterDescription, int index, int serialNumber, Color clusterColor, DefaultMutableTreeNode node, Experiment experiment) {
        this.indices = indices;
        this.source = source;
        this.clusterLabel = clusterLabel;
        this.clusterID = clusterID;
        this.algorithmName = algorithmName;
        this.algorithmIndex = index;
        this.clusterColor = clusterColor;
        this.clusterDescription = clusterDescription;
        this.serialNumber = serialNumber;
        if(node != null){
        	this.node = node;
        	this.userObject = node.getUserObject();
        }
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.isShowColor = true;
        this.experimentIndices = getIndicesMappedToExperiment();
    }
    
    /** Returns cluster color
     */    
    public Color getClusterColor(){ 
        if(this.isShowColor)
            return this.clusterColor;
        else
            return null;
    }
    
    /** Sets boolean to show color
     */
    public void enableShowColor(boolean show) {
        this.isShowColor = show;
    }
    /** returns true if color is to be shown
     */
    public boolean showColor() {
        return this.isShowColor;
    }
    /** Returns cluster indices
     */    
    public int []  getIndices(){ return this.indices; }
    /** Returns the cluster indices to the Experiment object associated with the cluster.
     */
    public int [] getExperimentIndices(){ return this.experimentIndices; }
    /** Returns source type
     */    
    public String getSource(){ return this.source; }
    /** Get user defined cluser label
     */    
    public String getClusterLabel(){ return this.clusterLabel; }
    /** Returns cluster node name.
     */    
    public String getClusterID(){ return this.clusterID; }
    /** Returns algorithm node name
     */    
    public String getAlgorithmName(){ return this.algorithmName; }
    /** Returns cluster description/remarks
     */    
    public String getClusterDescription(){ return this.clusterDescription; }
    /** Returns result index
     */    
    public int getAlgorithmIndex(){ return this.algorithmIndex; }
    /** Returns cluster serial number
     */    
    public int getSerialNumber(){ return this.serialNumber; }
    /** Returns the population of the cluster
     */    
    public int getSize(){ return this.indices.length; }
    /** Returns the node of origin or null if N/A
     */    
    public DefaultMutableTreeNode getNode(){ return node; }
    /** Returns the cluster's experiment
     */
    public Experiment getExperiment(){ return experiment; }
    /** Returns the cluster's node objects userObject
     */
    public Object getUserObject(){ return userObject; }
    /** Sets cluster color
     */    
    public void setClusterColor(Color color){ this.clusterColor = color; }
    /** Sets cluster label
     * @param clusterLabel Cluster label
     */    
    public void setClusterLabel(String clusterLabel){ this.clusterLabel = clusterLabel; }
    /** Sets cluster description
     */    
    public void setClusterDescription(String clusterDescription){ this.clusterDescription = clusterDescription; }
    /** Sets node of origin or reference node.
     * @param myNode node
     */    
    public void setNode(DefaultMutableTreeNode myNode){ node = myNode; }
    /** Sets the Experiment for the cluster
     */
    public void setExperiment(Experiment experiment){ 
    	this.experiment = experiment;
    	this.exptID = experiment.getId();
    	this.experimentIndices = getIndicesMappedToExperiment();
    }

    //EH
    public int getExptID(){return exptID;}
    
    /** Returns true if supplied element index is a
     * member of the cluster
     * @param index Element index
     * @return Returns boolean indicating membership
     */    
    public boolean isMember(int index){
        for(int i = 0; i < this.indices.length; i++)
            if(index == indices[i])
                return true;
        return false;
    }
    
    /** Removes an element index from cluster
     * membership.
     * @param memberIndex Index of member to remove.
     */    
    public boolean removeMember(int memberIndex){

        if(!isMember(memberIndex))
            return false;
        
        int [] newIndices = new int[this.indices.length - 1];
        int cnt = 0;
        for(int i = 0; i < indices.length; i++){
            if(indices[i] != memberIndex){
                newIndices[cnt] = indices[i];
                cnt++;
            }
        }
        return true;
    }
    
    /** Returns a Hashset which contains the indices
     * of the cluster
     */    
    public HashSet getHashSet(){
        HashSet set = new HashSet(indices.length);
        for(int i = 0; i < indices.length; i++){
            set.add(new Integer(indices[i]));
        }
        return set;
    }  
    
    /** Returns true if indices are a match
     *
     */
    public boolean doIndicesMatch(int [] indices){
        if(indices.length != this.indices.length)
            return false;        
        HashSet clusterSet = getHashSet();
        HashSet testSet = makeHashSet(indices);
        return clusterSet.containsAll(testSet);
    }
    
    private HashSet makeHashSet(int [] indices){
        HashSet set = new HashSet(indices.length);
        for( int i = 0; i < indices.length; i++ ){
            set.add(new Integer(indices[i]));
        }
        return set;
    }
    
    private int [] getIndicesMappedToExperiment(){
        int [] expIndices= new int[indices.length];
        int [] map = this.experiment.getRowMappingArrayCopy();
        int cnt = 0;
      /*  for(int i = 0; i < map.length; i++){
            if(map[i] == indices[cnt]){
                expIndices[cnt] = i;
                cnt++;
            }
        }
       **/
        int currIndex;
        for(int i = 0; i < expIndices.length; i++){
            currIndex = indices[i];
            
            for(int j = 0; j < map.length; j++){
                if(map[j] == currIndex){
                    expIndices[i] = j;
                }                    
            }
        }
        return expIndices;
    }
    private static class ClusterPersistenceDelegate extends PersistenceDelegate {
    
		protected Expression instantiate(Object o, Encoder encoder) {
			Cluster oldInstance = (Cluster)o;
			float[] findices = new float[oldInstance.indices.length];
			for(int i=0; i<oldInstance.indices.length; i++) {
				findices[i] = (float)oldInstance.indices[i];
			}
			FloatMatrix fm = new FloatMatrix(findices, findices.length);
			return new Expression(oldInstance, oldInstance.getClass(), "new", 
				new Object[]{fm, oldInstance.source, oldInstance.clusterLabel,
				oldInstance.algorithmName, oldInstance.clusterID, oldInstance.clusterDescription, 
				new Integer(oldInstance.algorithmIndex), new Integer(oldInstance.serialNumber), oldInstance.clusterColor, 
				oldInstance.node, oldInstance.experiment
				});
		}        
		public void initialize(Class type, Object oldInstance, Object newInstance, Encoder encoder) {
			return;
    }
    
        }            
	/**
	 * @return
	 */
	public static PersistenceDelegate getPersistenceDelegate() {
		return new ClusterPersistenceDelegate();
    }
    public static int[] getIndices(FloatMatrix fm) {
    	int[] temp = new int[fm.m];
    	for(int i=0; i<temp.length; i++) {
    		temp[i] = (int)fm.A[i][0];
    	}
    	return temp;
    }
}
