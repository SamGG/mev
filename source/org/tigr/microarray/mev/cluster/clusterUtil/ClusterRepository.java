/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ClusterRepository.java,v $
 * $Revision: 1.14 $
 * $Date: 2006-05-02 16:56:57 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.SearchResultDialog;
import org.tigr.microarray.mev.cluster.clusterUtil.submit.SubmissionManager;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;

/** The ClusterRepository contains ClusterList objects created for
 * holding saved clusters particular analysis results.
 */
public class ClusterRepository extends Vector {
    
    public static final int GENE_CLUSTER = 0;
    public static final int EXPERIMENT_CLUSTER = 1;
    
    private int numberOfElements;
    /** Maintains a ClusterList array for each data element.
     * If an element is a member of a cluster then that element will
     * have a reference to it's cluster object.
     */
    private ClusterList [] elementClusters;
    
    /** A counter to assign cluster serial numbers as they are
     * added to the repository.
     */
    private int clusterSerialCounter = 0;
    /** true if the repository maintains gene clusters
     */
    private int totalClusters = 0;
    private int removedClusters = 0;
    
    private boolean geneClusterRepository = false;
    
    /** IFramework implementation
     */
    private IFramework framework;
    
    /**Stores list of currently selected cluster colors to prevent
     * repeated selections
     */
    public ArrayList<Color> clusterColors = new ArrayList<Color>();
    /**
     * Used by XMLEncoder/XMLDecoder in conjunction with getPersistenceDelegateArgs()
     * @param isGeneClusterRepository
     * @param numberOfElements
     * @param elementClusters
     * @param clusterSerialCounter
     */
    public ClusterRepository(Boolean isGeneClusterRepository, Integer numberOfElements, Integer clusterSerialCounter, ClusterList[] elementClusters) {
    	this.geneClusterRepository = isGeneClusterRepository.booleanValue();
    	this.numberOfElements = numberOfElements.intValue();
    	this.elementClusters = elementClusters;
    	this.clusterSerialCounter = clusterSerialCounter.intValue();
    }
    /**
     * Used by XMLEncoder/XMLDecoder in conjunction with getPersistenceDelegateArgs()
     * @param isGeneClusterRepository
     * @param numberOfElements
     * @param elementClusters
     * @param clusterSerialCounter
     */
    public ClusterRepository(Boolean isGeneClusterRepository, Integer numberOfElements, Integer clusterSerialCounter, ClusterList[] elementClusters, ArrayList clusterColors) {
    	this.geneClusterRepository = isGeneClusterRepository.booleanValue();
    	this.numberOfElements = numberOfElements.intValue();
    	this.elementClusters = elementClusters;
    	this.clusterSerialCounter = clusterSerialCounter.intValue();
    	this.clusterColors = clusterColors;
    }
    
    /** Creates new ClusterRepository with a specified element count
     */
    public ClusterRepository(int numberOfElements, IFramework framework) {
        this.numberOfElements = numberOfElements;
        this.framework = framework;
        this.elementClusters = new ClusterList[numberOfElements];
        this.addClusterList(new ClusterList("Cluster Ops."));
    }
    
    /** Creates new ClusterRepository with specified cluster type*/
    public ClusterRepository(int numberOfElements, IFramework framework, boolean isGeneClusterRepository) {
        this.numberOfElements = numberOfElements;
        this.framework = framework;
        this.elementClusters = new ClusterList[numberOfElements];
        this.geneClusterRepository = isGeneClusterRepository;
        this.addClusterList(new ClusterList("Cluster Ops."));
    }
    public ClusterRepository(int numberOfElements){
        this.numberOfElements = numberOfElements;
        this.elementClusters = new ClusterList[numberOfElements];
        this.geneClusterRepository = false;
        this.addClusterList(new ClusterList("Cluster Ops."));
    }
    
    public ArrayList getClusterColors(){
    	return this.clusterColors;
    }
        
    //EH getter methods added to support constructer used by XMLEncoder/XMLDecoder
    
    public int getNumberOfElements(){return numberOfElements;}
    public ClusterList[] getElementClusters(){
    	return elementClusters;
    }
    public int getClusterSerialCounter() {return clusterSerialCounter;}
      /*
     * Returns a Hashtable of all the Experiments contained within this ClusterRepository
     * indexed on the Experiment.getId();
       */
    public Hashtable getAllExperiments() {
    	Hashtable allExpts = new Hashtable();
    	for(int i=0; i<elementClusters.length; i++) {
    		try {
	    		for(int j=0; j<elementClusters[i].size(); j++){
	        		Cluster c = (Cluster)elementClusters[i].get(j);
	        		allExpts.put(new Integer(c.getExptID()), c.getExperiment());
	    		}
    		} catch (NullPointerException npe){}
    	}
    	return allExpts;
    }
    
    public int getTotalClusters(){
    	return (clusterSerialCounter-removedClusters);
    }
    
    public void populateExperiments(Hashtable allExpts){
    	for(int i=0; i<elementClusters.length; i++) {
    		try {
	    		for(int j=0; j<elementClusters[i].size(); j++){
	        		Cluster c = (Cluster)elementClusters[i].get(j);
	        		c.setExperiment((Experiment)allExpts.get(new Integer(c.getExptID())));
	    		}
    		} catch (NullPointerException npe){}
    	}    
    }
    
    /**
     *  Sets the repository's framework field
     */
    public void setFramework(IFramework framework) {
        this.framework = framework;
    }    
    /**
     *  Gets the repository's framework field
     */
    public IFramework getFramework() {
        return framework;
    }
    
    /**
     * Returns a boolean for whether two clusters represented by colors have overlapping genes
     * @param index the index of the ClusterList to which a gene belonging to a cluster of Color "color" belongs
     * @param color the Color of one cluster
     * @param c the Color of another cluster
     * @return
     */
    public boolean isColorOverlap(int index, Color color, Color c){
    	int cluster=-1;
    	for (int findColor=0; findColor<elementClusters[index].size(); findColor++){
    		if (elementClusters[index].getClusterAt(findColor).getClusterColor() == color)
    			cluster = findColor;
    	}
    	
    	for (int j=0; j<elementClusters[index].getClusterAt(cluster).getIndices().length; j++){
    	    for (int i=0; i<elementClusters[elementClusters[index].getClusterAt(cluster).getIndices()[j]].size(); i++){
    	    	if (elementClusters[elementClusters[index].getClusterAt(cluster).getIndices()[j]].getClusterAt(i).getClusterColor() == color) continue;
    			
    			if (elementClusters[elementClusters[index].getClusterAt(cluster).getIndices()[j]].getClusterAt(i).getClusterColor() == c) return true;
    		}
    	}
    	
    	return false;
    }
    
    /** Returns the total number of clusters 
     * visible in the experiment viewer
     * @return
     */
    public int getVisibleClusters(){
    	return clusterColors.size();
    }

    
    /** Returns the color of the last cluster to which the element
     * (index) was assigned
     */
    public Color getColor(int index){
        if(elementClusters[index] == null  || elementClusters[index].size() == 0)
            return null;
        Color color = elementClusters[index].lastCluster().getClusterColor();
        
        //if last color is showing color return the color
        if(color != null)
            return color;
            
        //if the last cluster is supressing color, then check earlier clusters.
        else {  
            //check for previous cluster colors
            Color [] colors = getColors(index);
            
            //jump out if only one cluster
            if(colors.length <= 1)
                return null;
            
            else {
                //skip last and try earlier clusters
                for(int i = colors.length-2; i >= 0; i--) {
                    if(colors[i] != null)
                        return colors[i];
                }
            }
        }
        return null;
    }
    
    /** Returns all cluster colors for the clusters to which
     * the element (index) was assigned
     */
    public Color [] getColors(int index){
        if(elementClusters[index] == null)
            return null;
        ClusterList list  = elementClusters[index];
        Color [] colors = new Color[list.size()];
        for(int i = 0; i < colors.length; i++){
            colors[i] = list.getClusterAt(i).getClusterColor();
        }
        return colors;
    }
    
    /** Returns the number of elements in the data set corresponding to
     * the repository.  (number of spots or experiments)
     */
    public int getDataElementCount(){
        return this.numberOfElements;
    }
    
    /** Returns true if the repository maintains gene clusters.
     */
    public boolean isGeneClusterRepository(){ return this.geneClusterRepository; }
    
    /** Returns a cluster list for the specified result index.
     */
    public ClusterList getClusterList(int index){
        if(isInRange(index))
            return ((ClusterList)elementAt(index));
        return null;
    }
    
    /** adds a provided Clusterlist
     */
    public void addClusterList(ClusterList list){
        add(list);
    }
    
    
    /** Adds a cluster to a specified ClusterList
     */
    public void addCluster(ClusterList list, Cluster cluster){
        if(cluster == null)
            return;
        
        //if not a cluster operation and cluster exists, modify cluster
        if(!((cluster.getSource()).equals("Cluster Op.")) && list.isClusterSaved(cluster.getClusterID(), cluster.getIndices())){
            Cluster savedCluster = list.getCluster(cluster.getClusterID());
            if(savedCluster == null){   //safety net
                list.addCluster(cluster);
                updateClusterMembership(cluster);
                return;
            }
            savedCluster.setClusterColor(cluster.getClusterColor());
            savedCluster.setClusterLabel(cluster.getClusterLabel());
            savedCluster.setClusterDescription(cluster.getClusterDescription());
            this.setClusterSerialCounter(this.getMaxClusterSerialNumber()-1);  //rollback counter
            moveClusterToEndInMembershipLists(savedCluster);
        } else {
            if(list == null){ // null cluster list for cluster operations. make one
                list = new ClusterList("Cluster Ops.");
                this.addClusterList(list);
            }
            list.addCluster(cluster);
            updateClusterMembership(cluster);
        }
    }
    
    /** Adds a cluster to a specified ClusterList
     */
    public void addSubCluster(ClusterList list, Cluster cluster){
        if(cluster == null)
            return;
/*
        if(!((cluster.getSource()).equals("Cluster Op.")) && list.isClusterSaved(cluster.getClusterID())){
            Cluster savedCluster = list.getCluster(cluster.getClusterID());
            if(savedCluster == null){   //safety net
                list.addCluster(cluster);
                updateClusterMembership(cluster);
                return;
            }
            savedCluster.setClusterColor(cluster.getClusterColor());
            savedCluster.setClusterLabel(cluster.getClusterLabel());
            savedCluster.setClusterDescription(cluster.getClusterDescription());
            this.setClusterSerialCounter(this.getMaxClusterSerialNumber()-1);  //rollback counter
        } else {
 **/
        list.addCluster(cluster);
        updateClusterMembership(cluster);
        //  }
    }
    
    /** Adds a cluster to elements cluster list for elements
     * contained in a cluster
     */
    private void updateClusterMembership(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] == null)
                elementClusters[indices[i]] = new ClusterList("element "+indices[i]);
            elementClusters[indices[i]].add(cluster);
        }
    }
    
    /** Moves a cluster to the rear of the elementCluster lists ClusterList
     * (This is so that getColor() commands for gene color get the latest color
     * assigned rather than the color of the last cluster assigned.  This is to handle
     * modification of an existing cluster's color attribute.)
     */
    private void moveClusterToEndInMembershipLists(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] == null)
                elementClusters[indices[i]] = new ClusterList("element "+indices[i]);
            //if it's in the list (and is should always be) remove it and put it back in
            //at the end.
            if(elementClusters[indices[i]].contains(cluster)){
                elementClusters[indices[i]].removeElement(cluster);
                elementClusters[indices[i]].addElement(cluster);
            }
        }
    }
    
    /** Removes clusters from the cluster list of each element
     * contained in the specified cluster.
     */
    private void removeClusterMembership(Cluster cluster){
        int [] indices = cluster.getIndices();
        for(int i = 0; i < indices.length; i++){
            if(elementClusters[indices[i]] != null)
                elementClusters[indices[i]].removeElement(cluster);
        }
    }
    
    /** Clears all cluster lists.
     */
    public void clearClusterLists(){
        for(int i = 0; i < size(); i++)
            this.getClusterList(i).clear();
        clearElementClusters();
        if(this.isGeneClusterRepository())
            framework.getData().deleteColors();
        else
            framework.getData().deleteExperimentColors();
    }
    
    /** Clears the cluster lists for all elements.
     * Postcondition is that all elements do not have
     * references to any clusters.
     */
    private void clearElementClusters(){
        for(int i = 0; i < numberOfElements; i++)
            this.elementClusters[i] = null;
    }
    
    public boolean isEmpty(){
        for(int i = 0; i < size(); i++)
            if(this.getClusterList(i).size() > 0)
                return false;
        return true;
    }
    
    /** Returns true if an index for a cluster list is in range.
     */
    protected boolean isInRange(int index){
        return (index > -1 && index < size());
    }
    
    /** Stores a cluster given the passed parameters.
     */
    public Cluster storeCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID, indices)){
            JOptionPane pane = new JOptionPane("Cluster has already been saved.  Would you like to " +
            "replace the existing attributes?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            pane.setVisible(true);
            int option = pane.getOptionType();
            if(option == JOptionPane.CANCEL_OPTION || option == JOptionPane.NO_OPTION)
                return null;
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID, null, null, getNextDefaultColor());
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, experiment);
        addCluster(list, cluster);
        
        return cluster;
    }
    
    /** Stores a clsuter given the supplied parameters.
     */
    public Cluster storeCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, DefaultMutableTreeNode clusterNode, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID, indices)){
            int option = JOptionPane.showConfirmDialog(new java.awt.Frame(), "Cluster has already been saved.  Would you like to " +
            "modify the existing attributes?", "Cluster Saved Alert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if(option == JOptionPane.NO_OPTION)
                return null;
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID, null, null, getNextDefaultColor());
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        
        if (clusterColors.contains(dialog.getColor())){
	        Object[] optionst = { "OK", "CANCEL" };
	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
        		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        		null, optionst, optionst[0]);
	        if (option==JOptionPane.CANCEL_OPTION) return null;
	        if (option==JOptionPane.OK_OPTION)
	        	return storeCluster(algorithmIndex, algorithmName, clusterID, indices, clusterNode,experiment);
        
        return null;
        }
        clusterColors.add(dialog.getColor());
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, clusterNode, experiment);
        addCluster(list, cluster);
        
        return cluster;
    }
    
    /**
     * Stores a cluster given the supplied parameters.
     */
    public Cluster storeSubCluster(int algorithmIndex, String algorithmName, String clusterID, int [] indices, DefaultMutableTreeNode clusterNode, Experiment experiment){
        
        ClusterList list =  findClusterList(algorithmName);
        boolean modification = false;
        
        if(list == null){
            list = new ClusterList(algorithmName);
            this.addClusterList(list);
        } else if(list.isClusterSaved(clusterID, indices)){
            if(list.getCluster(clusterID).doIndicesMatch(indices)){
                int option = JOptionPane.showConfirmDialog(new java.awt.Frame(), "Cluster has already been saved.  Would you like to " +
                "modify the existing attributes?", "Cluster Saved Alert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                modification = true;
                if(option == JOptionPane.NO_OPTION)
                    return null;
            }
        }
        
        ClusterAttributesDialog dialog = new ClusterAttributesDialog("Store Cluster Attributes", algorithmName, clusterID, null, null, getNextDefaultColor());
        if(dialog.showModal() != JOptionPane.OK_OPTION){
            return null;
        }
        if (clusterColors.contains(dialog.getColor())){
	        Object[] optionst = { "OK", "CANCEL" };
	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
        		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        		null, optionst, optionst[0]);
	        if (option==JOptionPane.CANCEL_OPTION) return null;
	        if (option==JOptionPane.OK_OPTION)
	        	return storeSubCluster(algorithmIndex, algorithmName, clusterID, indices, clusterNode,experiment);
        return null;
        
        }
        clusterColors.add(dialog.getColor());
        Color clusterColor = dialog.getColor();
        String clusterLabel = dialog.getLabel();
        String clusterDescription = dialog.getDescription();
        this.clusterSerialCounter++;
        Cluster cluster = new Cluster(indices, "Algorithm", clusterLabel, algorithmName, clusterID, clusterDescription, algorithmIndex, this.clusterSerialCounter, clusterColor, clusterNode, experiment);
        if(modification)
            addCluster(list, cluster);
        else
            addSubCluster(list, cluster);
        
        return cluster;
    }
    
    /** Returns the ClusterList given an algorithm result name
     * (i.e. KMC(2))
     */
    private ClusterList findClusterList(String algName){
        ClusterList curr;
        for(int i = 0; i < size(); i++){
            curr = this.getClusterList(i);
            if(curr.getAlgorithmName().equals(algName))
                return curr;
        }
        return null;
    }
    
    /** Returns a specialized cluster list responsible for holding
     * results from cluster operations.
     */
    public ClusterList getClusterOperationsList(){
        return findClusterList("Cluster Ops.");
    }
    
    /** Remove  a cluster given supplied parameters
     */
    public boolean removeCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return false;
        Cluster cluster = list.getCluster(clusterID);
        if(cluster != null){
        	cluster=null;
        	removedClusters++;
            int serialNumber = cluster.getSerialNumber();
            if(this.isGeneClusterRepository()){
                framework.getData().setProbesColor(indices, null);
                framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
            } else {
                framework.getData().setExperimentColor(indices, null);
                framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
            }
        } else {
            return false;
        }
        list.removeCluster(clusterID);
        removeElementClusters(indices, cluster);
        return true;
    }
    
    /** Remove  a cluster given supplied parameters
     */
    public boolean removeSubCluster(int [] indices, String algorithmName, String clusterID){
        ClusterList list = findClusterList(algorithmName);
        if(list == null || list.size() == 0)
            return false;
        
        Cluster cluster = null;
        Cluster temp = null;
        for(int i = 0; i < list.size(); i++){
            temp = list.getClusterAt(i);
            if(temp.doIndicesMatch(indices))
                cluster = temp;
        }
        if(cluster == null)
            return false;
        cluster=null;
    	removedClusters++;
        int serialNumber = cluster.getSerialNumber();
        if(this.isGeneClusterRepository()){
            framework.getData().setProbesColor(indices, null);
            framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        } else {
            framework.getData().setExperimentColor(indices, null);
            framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        }
        
        list.removeCluster(cluster);
        removeElementClusters(indices, cluster);
        return true;
    }
    
    
    /** Removes a cluster from elements cluster lists.
     */
    private void removeElementClusters(int [] indices, Cluster cluster){
        for(int i = 0; i < indices.length; i++){
            this.elementClusters[indices[i]].remove(cluster);
        }
    }
    
    /** Alters the color of a specified cluster (cluster serial number)
     */
    public void updateClusterColor(int serialNumber, Color color){
        Cluster cluster = getCluster(serialNumber);
        if(cluster != null){
            cluster.setClusterColor(color);
        }
    }
    
    /** Returns the cluster specified by the provided
     * serial number.
     */
    public Cluster getCluster2(int serialNumber){
        Cluster cluster = null;
        ClusterList list = null;
        for(int i = 0; i < size(); i++){
            list = this.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
            	cluster = list.getClusterAt(j);
                if(serialNumber == cluster.getSerialNumber()){
                    return cluster;
            }
        }
        }
        return cluster;
    }
    /** Returns the cluster specified by the provided
     * serial number.
     */
    public Cluster getCluster(int serialNumber){
        Cluster cluster = null;
        ClusterList list = null;
        for(int i = 0; i < size(); i++){
            list = this.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                if(serialNumber == list.getClusterAt(j).getSerialNumber()){
                    return list.getClusterAt(j);
                }
            }
        }
        return cluster;
    }
    
    /** Removes cluster specified by the serial number
     */
    public boolean removeCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        if(cluster == null)
            return false;
        
        ClusterList list;
        for(int i = 0; i < size(); i++){
            list = this.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                if(list.getClusterAt(j) == cluster){
                    list.removeCluster(serialNumber);
                }
            }
        }
        int [] indices = cluster.getIndices();
        removeClusterMembership(cluster);
        clusterColors.remove(cluster.getClusterColor());
        cluster=null;
    	removedClusters++;
        if(this.isGeneClusterRepository()){
            framework.getData().setProbesColor(indices, null);
            framework.addHistory("Remove Gene Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        } else {
            framework.getData().setExperimentColor(indices, null);
            framework.addHistory("Remove Experiment Cluster From Repository: Serial # "+String.valueOf(serialNumber));
        }
        return true;
    }
    
    /** Returns the next available cluster serial
     * number and reserves it's use.
     */
    public int takeNextClusterSerialNumber(){
        this.clusterSerialCounter++;
        return clusterSerialCounter;
    }
    
    /**
     *  Returns the value of the maximum serial number
     */
    public int getMaxClusterSerialNumber(){
        return clusterSerialCounter;
    }
    
    /**
     *  Sets the cluster serial number, next reserved number is value + 1
     */
    public void setClusterSerialCounter(int value){
        this.clusterSerialCounter = value;
    }
    
    /** Prints out a summary of the repository.
     */
    public void printRepository(){
    }
    
    /** Saves the specified cluster
     */
    public void saveCluster(int serialNumber){
        Cluster cluster = getCluster(serialNumber);
        try{
            if(geneClusterRepository)
                ExperimentUtil.saveGeneCluster(framework.getFrame(), framework.getData(), cluster.getIndices());
            else
                ExperimentUtil.saveExperimentCluster(framework.getFrame(), cluster.getExperiment(), framework.getData(), cluster.getIndices());
        } catch (Exception e){
            JOptionPane.showMessageDialog(framework.getFrame(), "Error saving cluster.  Cluster not saved.", "Save Error", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /** Creates a cluster by importing a gene list         
     */
    public Cluster createClusterFromList() {
	    return createClusterFromList(null);
    }
        
    /** Creates a cluster by importing a gene list         
     */
    public Cluster createClusterFromList(String[] genelist) {
        ListImportDialog dialog;
        String [] ids;
        String key;
        int [] newIndices;
        boolean [] matches;
        Experiment experiment = framework.getData().getExperiment();
        if(this.isGeneClusterRepository()) {
                dialog = new ListImportDialog(framework.getFrame(), this.framework.getData().getAllFilledAnnotationFields(), true, genelist);
//                dialog = new ListImportDialog(framework.getFrame(), this.framework.getData().getFieldNames(), true, genelist);
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                key = dialog.getFieldName();
                ids = dialog.getList();
                
                //look for matches for ids
                matches = new boolean[ids.length];
                
                newIndices = getMatchingIndices(experiment, key, ids, matches, true);
                
                if(newIndices != null && newIndices.length > 0) {

                    SearchResultDialog resultDialog = new SearchResultDialog(framework, newIndices, ids, matches, true);
                    
                    if(resultDialog.showModal() == JOptionPane.OK_OPTION) {
                        
                        int [] selectedIndices = resultDialog.getSelectedIndices();
                        
                        if(selectedIndices == null || selectedIndices.length < 1) {
                            return null;
                        }
                        
                        while (true){
                        ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Gene List", null, null, getNextDefaultColor());
                        if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
                            return null;
                        }
                        if (clusterColors.contains(clusterDialog.getColor())){
                	        Object[] optionst = { "OK", "CANCEL" };
                	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
                        		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        		null, optionst, optionst[0]);
                	        if (option==JOptionPane.CANCEL_OPTION) return null;
                	        if (option==JOptionPane.OK_OPTION)
	                	        	continue;
                        return null;
                        }
                        clusterColors.add(clusterDialog.getColor());
                        ClusterList list = getClusterOperationsList();
                        Color clusterColor = clusterDialog.getColor();
                        String clusterLabel = clusterDialog.getLabel();
                        String clusterDescription = clusterDialog.getDescription();
                        this.clusterSerialCounter++;
                        Cluster cluster = new Cluster(selectedIndices, "Cluster Op.", clusterLabel, "List Import", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
                        
                        addCluster(list, cluster);
                        return cluster;
                    	}
                        
                    } else {
                        return null;
                    }
                    
                } else {
                    JOptionPane.showMessageDialog(framework.getFrame(), "No genes matching the list entries were found.", "No Matches Found", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else{
            ISlideData slide = this.framework.getData().getFeature(0);
            if(slide == null)
                return null;
            
            Vector<String> slideNameKeys = slide.getSlideDataKeys();
            String [] slideNames = new String[slideNameKeys.size()];
            for(int i = 0; i < slideNames.length; i++) {
                slideNames[i] = (String)(slideNameKeys.elementAt(i));
            }
            
            dialog = new ListImportDialog(framework.getFrame(), slideNames, false, genelist);
            
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                
                key = dialog.getFieldName();
                ids = dialog.getList();
                
                //look for matches for ids
                matches = new boolean[ids.length];
                
                newIndices = getMatchingIndices(experiment, key, ids, matches, false);
                if(newIndices != null && newIndices.length > 0) {
                    
                    SearchResultDialog resultDialog = new SearchResultDialog(framework, newIndices, ids, matches, false);
                    
                    
                    if(resultDialog.showModal() == JOptionPane.OK_OPTION) {
                       int [] selectedIndices = resultDialog.getSelectedIndices();
                        
                        if(selectedIndices == null || selectedIndices.length < 1) {
                            return null;
                        }                        
                        while (true){
                        ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Sample List", null, null, getNextDefaultColor());
                        if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
                            return null;
                        }
                        if (clusterColors.contains(clusterDialog.getColor())){
                	        Object[] optionst = { "OK", "CANCEL" };
                	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
                        		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        		null, optionst, optionst[0]);
	                	        if (option==JOptionPane.CANCEL_OPTION) 
                	        	return null;
	                	        if (option==JOptionPane.OK_OPTION)
	                	        	continue;
                        return null;
                        }
                        clusterColors.add(clusterDialog.getColor());
                        ClusterList list = getClusterOperationsList();
                        Color clusterColor = clusterDialog.getColor();
                        String clusterLabel = clusterDialog.getLabel();
                        String clusterDescription = clusterDialog.getDescription();
                        this.clusterSerialCounter++;
                        Cluster cluster = new Cluster(selectedIndices, "List Import", clusterLabel, "List Import", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
                        
                        addCluster(list, cluster);
                        return cluster;
                    }
                    }
                } else {
                    JOptionPane.showMessageDialog(framework.getFrame(), "No samples matching the list entries were found.", "No Matches Found", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        return null;
    }
    
    /**  Creates a cluster by taking a user-defined upper and lower limit for a particular annotation type.
     * 
     */
    public ArrayList<Cluster> binCreateClusters(){

        ListImportDialog dialog;
        String [] annList;
        String key;
        String oldLabel = framework.getData().getCurrentSampleLabelKey();
        int [] newIndices;
        ArrayList<Cluster> clusterArray = new ArrayList<Cluster>();
        int [] allIndices;
        boolean [] matches;
        
        Experiment experiment = framework.getData().getExperiment();
        
        ISlideData slide = this.framework.getData().getFeature(0);
        if(slide == null)
            return null;
        
        Vector<String> slideNameKeys = slide.getSlideDataKeys();
        String [] slideNames = new String[slideNameKeys.size()];
        for(int i = 0; i < slideNames.length; i++) {
            slideNames[i] = (String)(slideNameKeys.elementAt(i));
        }
        
        if(!this.isGeneClusterRepository()) {
        	dialog = new ListImportDialog(framework.getFrame(), slideNames, false, true, true);
            
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
            	for (int clusterCount=0; clusterCount<dialog.getLowerLimit().length; clusterCount++){
	                key = dialog.getFieldName();
	                
	            	allIndices = experiment.getColumnIndicesCopy();
		            annList = new String[experiment.getNumberOfSamples()];
		            framework.getData().setSampleLabelKey(key);
		            ArrayList<String> noRepeatsLabelList = new ArrayList<String>();
		            matches = new boolean[annList.length];
		            for(int i = 0; i < allIndices.length; i++) {
		                annList[i] = framework.getData().getFullSampleName(i);
		                if (!noRepeatsLabelList.contains(annList[i]))
		                	noRepeatsLabelList.add(annList[i]);
		            }
	            	newIndices = getBinnedIndices(experiment, key, dialog.getLowerLimit()[clusterCount], dialog.getUpperLimit()[clusterCount], matches, false);
	                int[] selectedIndices = newIndices;
	                if(selectedIndices == null || selectedIndices.length < 1) {
	                	JOptionPane.showMessageDialog(framework.getFrame(), "No samples within the given limits for 'Cluster "+(clusterCount+1)+"' were found.", "No Samples Found", JOptionPane.INFORMATION_MESSAGE);
	                    continue;
	                }                        
	                
	                String clusterLabel = Float.toString(dialog.getLowerLimit()[clusterCount]) + "-"+Float.toString(dialog.getUpperLimit()[clusterCount]);
	                while (true){
	                    ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Gene List", key + " - " + clusterLabel, null, getNextDefaultColor());
	                    if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
	                        break;
	                    }
	                    if (clusterColors.contains(clusterDialog.getColor())){
	            	        Object[] optionst = { "OK", "CANCEL" };
	            	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
	                    		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
	                    		null, optionst, optionst[0]);
	            	        if (option==JOptionPane.CANCEL_OPTION) 
	            	        	break;
	            	        if (option==JOptionPane.OK_OPTION)
	                	        continue;
	            	        return null;
	                    }
	                    clusterColors.add(clusterDialog.getColor());
	                    ClusterList list = getClusterOperationsList();
	                    Color clusterColor = clusterDialog.getColor();
	                    clusterLabel = clusterDialog.getLabel();
	                    String clusterDescription = clusterDialog.getDescription();
	                    this.clusterSerialCounter++;
	                    Cluster cluster = new Cluster(selectedIndices, "Cluster Op.", clusterLabel, "Binned Cluster", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
	                    
	                    addCluster(list, cluster);
	                    clusterArray.add(cluster);
	                    break;
	                }
	        	}
                framework.getData().setSampleLabelKey(oldLabel);
            	return clusterArray;
            }
        }else{
            	dialog = new ListImportDialog(framework.getFrame(), this.framework.getData().getFieldNames(), true, true, true);
                
                if(dialog.showModal() == JOptionPane.OK_OPTION) {

                	for (int clusterCount=0; clusterCount<dialog.getLowerLimit().length; clusterCount++){
			            key = dialog.getFieldName();
		
			            allIndices = experiment.getRowMappingArrayCopy();
			            annList = new String[experiment.getNumberOfGenes()];
			            annList = framework.getData().getAnnotationList(key, allIndices);
			            matches = new boolean[annList.length];
		            	newIndices = getBinnedIndices(experiment, key, dialog.getLowerLimit()[clusterCount], dialog.getUpperLimit()[clusterCount], matches, true);
	                    int[] selectedIndices = newIndices;
	                    if(selectedIndices == null || selectedIndices.length < 1) {
	                    	JOptionPane.showMessageDialog(framework.getFrame(), "No genes within the given limits for 'Cluster "+(clusterCount+1)+"' were found.", "No Samples Found", JOptionPane.INFORMATION_MESSAGE);
	                        continue;
	                    }  
		                
		                String clusterLabel = Float.toString(dialog.getLowerLimit()[clusterCount]) + "-"+Float.toString(dialog.getUpperLimit()[clusterCount]);
		                while (true){
		                    ClusterAttributesDialog clusterDialog = new ClusterAttributesDialog("Store Cluster Attributes", "List Import", "Gene List", key + " - " + clusterLabel, null, getNextDefaultColor());
		                    if(clusterDialog.showModal() != JOptionPane.OK_OPTION){
		                        break;
		                    }
		                    if (clusterColors.contains(clusterDialog.getColor())){
		            	        Object[] optionst = { "OK", "CANCEL" };
		            	        int option = JOptionPane.showOptionDialog(null, "Cluster Color is already being used. Please select another Color.", "Duplicate Color Error", 
		                    		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
		                    		null, optionst, optionst[0]);
		            	        if (option==JOptionPane.CANCEL_OPTION) 
		            	        	break;
		            	        if (option==JOptionPane.OK_OPTION)
		                	        continue;
		            	        return null;
		                    }
		                    clusterColors.add(clusterDialog.getColor());
		                    ClusterList list = getClusterOperationsList();
		                    Color clusterColor = clusterDialog.getColor();
		                    clusterLabel = clusterDialog.getLabel();
		                    String clusterDescription = clusterDialog.getDescription();
		                    this.clusterSerialCounter++;
		                    Cluster cluster = new Cluster(selectedIndices, "Cluster Op.", clusterLabel, "Binned Cluster", "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
		                    
		                    addCluster(list, cluster);
		                    clusterArray.add(cluster);
		                    break;
		                }
	            	}
		            return clusterArray;
                }
            }
        return clusterArray;
    }

    
//**********************************************************************************************************************************************************
    public ArrayList<Cluster> autoCreateClusters(){

        ListImportDialog dialog;
        String [] annList;
        String key;
        String oldLabel = framework.getData().getCurrentSampleLabelKey();
        int [] newIndices;
        ArrayList<Cluster> clusterArray = new ArrayList<Cluster>();
        int [] allIndices;
        boolean [] matches;
        
        Experiment experiment = framework.getData().getExperiment();
        
        ISlideData slide = this.framework.getData().getFeature(0);
        if(slide == null)
            return null;
        
        Vector<String> slideNameKeys = slide.getSlideDataKeys();
        String [] slideNames = new String[slideNameKeys.size()];
        for(int i = 0; i < slideNames.length; i++) {
            slideNames[i] = (String)(slideNameKeys.elementAt(i));
        }
        
        if(!this.isGeneClusterRepository()) {
        	dialog = new ListImportDialog(framework.getFrame(), slideNames, false, true);
            
            if(dialog.showModal() == JOptionPane.OK_OPTION) {
                List selectedFields = dialog.getSelectedFields();
                for (int slidei = 0; slidei<selectedFields.getItemCount(); slidei++){
		            key = selectedFields.getItem(slidei);
                	allIndices = experiment.getColumnIndicesCopy();
		            annList = new String[experiment.getNumberOfSamples()];
		            framework.getData().setSampleLabelKey(key);
		            ArrayList<String> noRepeatsLabelList = new ArrayList<String>();
		            matches = new boolean[annList.length];
		            for(int i = 0; i < allIndices.length; i++) {
		                annList[i] = framework.getData().getFullSampleName(i);
		                if (!noRepeatsLabelList.contains(annList[i]))
		                	noRepeatsLabelList.add(annList[i]);
		            }
		            if (noRepeatsLabelList.size()>10){
		    	        Object[] optionst = { "OK", "CANCEL" };
		            	int option = JOptionPane.showOptionDialog(null, "Clustering by annotation type " + key + " will create " + noRepeatsLabelList.size() + " clusters.  Are you sure you want to continue?" , "High cluster count for annotation type",
		            			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
		            			null, optionst, optionst[0]);
		            	if (option!=JOptionPane.OK_OPTION)
            	        	continue;
		            }
		            String[][] labelArray = new String[noRepeatsLabelList.size()][1];
		            for (int i = 0; i<labelArray.length; i++){
		            	labelArray[i][0]=noRepeatsLabelList.get(i);
		            }
		            for (int label = 0; label<noRepeatsLabelList.size();label++){
		            	newIndices = getMatchingIndices(experiment, key, labelArray[label], matches, false);
	                    int[] selectedIndices = newIndices;
	                    if(selectedIndices == null || selectedIndices.length < 1) {
	                        return null;
	                    }                        
	                    Color nextColor = getNextDefaultColor();
	                  	clusterColors.add(nextColor);
	                    ClusterList list = getClusterOperationsList();
	                    Color clusterColor = nextColor;
	                    String clusterLabel = labelArray[label][0];
	                    String clusterDescription = null;
	                    this.clusterSerialCounter++;
	                    Cluster cluster = new Cluster(selectedIndices, "Auto Cluster", key + " - " + clusterLabel, key, "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
//	                    addCluster(list, cluster);

	                    list.addCluster(cluster);
	                    updateClusterMembership(cluster);
	                    clusterArray.add(cluster);
		            }
        		}
                framework.getData().setSampleLabelKey(oldLabel);
        	}
        }else{
            	dialog = new ListImportDialog(framework.getFrame(), this.framework.getData().getFieldNames(), true, true);
                
                if(dialog.showModal() == JOptionPane.OK_OPTION) {
                    List selectedFields = dialog.getSelectedFields();

                    for (int slidei = 0; slidei<selectedFields.getItemCount(); slidei++){
    		            key = selectedFields.getItem(slidei);
    	
    		            allIndices = experiment.getRowMappingArrayCopy();
    		            annList = new String[experiment.getNumberOfGenes()];
    		            annList = framework.getData().getAnnotationList(key, allIndices);
    		            ArrayList<String> noRepeatsLabelList = new ArrayList<String>();
    		            matches = new boolean[annList.length];
    		            for(int i = 0; i < allIndices.length; i++) {
    		                if (!noRepeatsLabelList.contains(annList[i]))
    		                	noRepeatsLabelList.add(annList[i]);
    		            }
    		            if (noRepeatsLabelList.size()>10){
    		    	        Object[] optionst = { "OK", "CANCEL" };
    		            	int option = JOptionPane.showOptionDialog(null, "Clustering by annotation type " + key + " will create " + noRepeatsLabelList.size() + " clusters.  Are you sure you want to continue?" , "High cluster count for annotation type",
    		            			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
    		            			null, optionst, optionst[0]);
    		            	if (option!=JOptionPane.OK_OPTION)
                	        	continue;
    		            }
    		            String[][] labelArray = new String[noRepeatsLabelList.size()][1];
    		            for (int i = 0; i<labelArray.length; i++){
    		            	labelArray[i][0]=noRepeatsLabelList.get(i);
    		            }
    		            for (int label = 0; label<noRepeatsLabelList.size();label++){
    		            	newIndices = getMatchingIndices(experiment, key, labelArray[label], matches, true);
    	                    int[] selectedIndices = newIndices;
    	                    if(selectedIndices == null || selectedIndices.length < 1) {
    	                        return null;
    	                    }  
    	                    Color nextColor = getNextDefaultColor();
		                  	clusterColors.add(nextColor);
    		                ClusterList list = getClusterOperationsList();
    		                Color clusterColor = nextColor;
    		                String clusterLabel = labelArray[label][0];
    		                String clusterDescription = null;
    		                this.clusterSerialCounter++;
    		                Cluster cluster = new Cluster(selectedIndices, "Auto Cluster", key + " - " + clusterLabel, key, "N/A", 
    		                		clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
//    		                addCluster(list, cluster);
    	                    list.addCluster(cluster);
    	                    updateClusterMembership(cluster);
    		                clusterArray.add(cluster);
    		            }
            		}
            	}
            }
        return clusterArray;
    }
    
    public ArrayList<Cluster> autoCreateClusters(int index){

        String [] annList;
        String key;
        int [] newIndices;
        ArrayList<Cluster> clusterArray = new ArrayList<Cluster>();
        int [] allIndices;
        boolean [] matches;
        
        Experiment experiment = framework.getData().getExperiment();
        
        ISlideData slide = this.framework.getData().getFeature(0);
        if(slide == null)
            return null;
        
        Vector<String> slideNameKeys = slide.getSlideDataKeys();
        String [] slideNames = new String[slideNameKeys.size()];
        for(int i = 0; i < slideNames.length; i++) {
            slideNames[i] = (String)(slideNameKeys.elementAt(i));
        }
        
        if(!this.isGeneClusterRepository()) {
        	key = framework.getData().getCurrentSampleLabelKey();
           	allIndices = experiment.getColumnIndicesCopy();
            annList = new String[experiment.getNumberOfSamples()];
            ArrayList<String> noRepeatsLabelList = new ArrayList<String>();
            matches = new boolean[annList.length];
            for(int i = 0; i < allIndices.length; i++) {
                annList[i] = framework.getData().getFullSampleName(i);
                if (!noRepeatsLabelList.contains(annList[i]))
                	noRepeatsLabelList.add(annList[i]);
            }
            if (noRepeatsLabelList.size()>10){
    	        Object[] optionst = { "OK", "CANCEL" };
            	int option = JOptionPane.showOptionDialog(null, "Clustering by annotation type " + key + " will create " + noRepeatsLabelList.size() + " clusters.  Are you sure you want to continue?" , "High cluster count for annotation type",
            			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            			null, optionst, optionst[0]);
            	if (option!=JOptionPane.OK_OPTION)
       	        	return null;
	        }
            String[][] labelArray = new String[noRepeatsLabelList.size()][1];
            for (int i = 0; i<labelArray.length; i++){
            	labelArray[i][0]=noRepeatsLabelList.get(i);
            }
            for (int label = 0; label<noRepeatsLabelList.size();label++){
            	newIndices = getMatchingIndices(experiment, key, labelArray[label], matches, false);
                int[] selectedIndices = newIndices;
                if(selectedIndices == null || selectedIndices.length < 1) {
                    return null;
                }            
                Color nextColor = getNextDefaultColor();
               	clusterColors.add(nextColor);
                ClusterList list = getClusterOperationsList();
                Color clusterColor = nextColor;
                String clusterLabel = labelArray[label][0];
                String clusterDescription = null;
                this.clusterSerialCounter++;
                Cluster cluster = new Cluster(selectedIndices, "Auto Cluster", key + " - " + clusterLabel, key, "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
//                addCluster(list, cluster);
                list.addCluster(cluster);
                updateClusterMembership(cluster);
                clusterArray.add(cluster);  
            }      		
        }else{
            key = this.framework.getData().getFieldNames()[index];
            allIndices = experiment.getRowMappingArrayCopy();
            annList = new String[experiment.getNumberOfGenes()];
            annList = framework.getData().getAnnotationList(key, allIndices);
            ArrayList<String> noRepeatsLabelList = new ArrayList<String>();
            matches = new boolean[annList.length];
            for(int i = 0; i < allIndices.length; i++) {
                if (!noRepeatsLabelList.contains(annList[i]))
                	noRepeatsLabelList.add(annList[i]);
            }
            if (noRepeatsLabelList.size()>10){
    	        Object[] optionst = { "OK", "CANCEL" };
            	int option = JOptionPane.showOptionDialog(null, "Clustering by annotation type " + key + " will create " + noRepeatsLabelList.size() + " clusters.  Are you sure you want to continue?" , "High cluster count for annotation type",
           			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
           			null, optionst, optionst[0]);
            	if (option!=JOptionPane.OK_OPTION)
       	        	return null;
            }
            String[][] labelArray = new String[noRepeatsLabelList.size()][1];
            for (int i = 0; i<labelArray.length; i++){
            	labelArray[i][0]=noRepeatsLabelList.get(i);
            }
            for (int label = 0; label<noRepeatsLabelList.size();label++){
            	newIndices = getMatchingIndices(experiment, key, labelArray[label], matches, true);
                int[] selectedIndices = newIndices;
                if(selectedIndices == null || selectedIndices.length < 1) {
                    return null;
                } 
              	Color nextColor = getNextDefaultColor();
               	clusterColors.add(nextColor);
                ClusterList list = getClusterOperationsList();
    		    Color clusterColor = nextColor;
    		    String clusterLabel = labelArray[label][0];
    		    String clusterDescription = null;
    		    this.clusterSerialCounter++;
    		    Cluster cluster = new Cluster(selectedIndices, "Auto Cluster", key + " - " + clusterLabel, key, "N/A", clusterDescription, list.getAlgorithmIndex(), this.clusterSerialCounter, clusterColor, experiment);
//    		    addCluster(list, cluster);
                list.addCluster(cluster);
                updateClusterMembership(cluster);
    		    clusterArray.add(cluster);
            }
        }
        return clusterArray;
    }
    
    
    
//--------------------------------------------------------------------------------------------------------------------------------------------------
    public Color getNextDefaultColor(){
    	Color color;
    	if (!clusterColors.contains(Color.green))
    		return Color.green;
    	if (!clusterColors.contains(Color.blue))
    		return Color.blue;
    	if (!clusterColors.contains(Color.red))
    		return Color.red;
    	if (!clusterColors.contains(Color.yellow))
    		return Color.yellow;
    	if (!clusterColors.contains(Color.orange))
    		return Color.orange;
    	if (!clusterColors.contains(Color.black))
    		return Color.black;
    	if (!clusterColors.contains(Color.pink))
    		return Color.pink;
    	while(true){
            int red = (int)(Math.random()*256);
            int green = (int)(Math.random()*256);
            int blue = (int)(Math.random()*256);
            color = new Color(red, green, blue);
        	if (!clusterColors.contains(color))
        		return color;
    	}
    }
    
    
    private int [] getMatchingIndices(Experiment experiment, String key, String [] ids, boolean geneIndices) {
        int [] indices = null;
        int [] allIndices;
        String [] annList;
        Vector indicesVector = new Vector();
        IData data = framework.getData();
        
        if(geneIndices) {
            allIndices = experiment.getRowMappingArrayCopy();
            annList = framework.getData().getAnnotationList(key, allIndices);
            Vector idVector = new Vector(annList.length);
            for(int i = 0 ; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i]))
                    indicesVector.addElement(new Integer(allIndices[i]));
            }
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
        } else {
            allIndices = experiment.getColumnIndicesCopy();
            annList = new String[experiment.getNumberOfSamples()];
            data.setSampleLabelKey(key);
            
            Vector idVector = new Vector(annList.length);
            for(int i = 0; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            
            for(int i = 0; i < allIndices.length; i++) {
                annList[i] = data.getFullSampleName(i);
            }
            
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i]))
                    indicesVector.addElement(new Integer(allIndices[i]));
            }
            
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
            
        }
        return indices;
    }
    
    private int [] getBinnedIndices(Experiment experiment, String key, float lowerLimit, float upperLimit, boolean [] matches, boolean geneIndices) {
        int [] indices = null;
        int [] allIndices;
        String [] annList;
        Vector<Integer> indicesVector= new Vector<Integer>();
        IData data = framework.getData();
        
        if(geneIndices) {
            allIndices = experiment.getRowMappingArrayCopy();
            annList = framework.getData().getAnnotationList(key, allIndices);
            for(int i = 0; i < annList.length; i++) {
            	try{
            		if (Float.parseFloat(annList[i])>= lowerLimit && Float.parseFloat(annList[i])<= upperLimit){
            			indicesVector.addElement(new Integer(allIndices[i]));
            		}
            		
            	} catch (Exception e){
            		//e.printStackTrace();  just ignore stacktrace if format is incorrect
            	}
            }
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
        } else {
            allIndices = experiment.getColumnIndicesCopy();
            annList = new String[experiment.getNumberOfSamples()];
            data.setSampleLabelKey(key);
            
            
            for(int i = 0; i < allIndices.length; i++) {
                annList[i] = data.getFullSampleName(i);
            }
            
            for(int i = 0; i < annList.length; i++) {
            	try{
            		if (Float.parseFloat(annList[i])>= lowerLimit && Float.parseFloat(annList[i])<= upperLimit){
            			indicesVector.addElement(new Integer(allIndices[i]));
            		}
            		
            	} catch (Exception e){
            		
            		//e.printStackTrace();  just ignore stacktrace if format is incorrect
            	}
            	
            }
            
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
        }
        return indices;
    }
    
    
    
    private int [] getMatchingIndices(Experiment experiment, String key, String [] ids, boolean [] matches, boolean geneIndices) {
        int [] indices = null;
        int [] allIndices;
        String [] annList;
        Vector indicesVector = new Vector();
        IData data = framework.getData();
        
        if(geneIndices) {
            allIndices = experiment.getRowMappingArrayCopy();
            annList = framework.getData().getAnnotationList(key, allIndices);
            Vector idVector = new Vector(annList.length);
            for(int i = 0 ; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i])) {
                    indicesVector.addElement(new Integer(allIndices[i]));
                    
                    //don't do this because it doesn't handle duplicate entries in the index list
                    //idIndex = idVector.indexOf(annList[i]);
                    //if(idIndex > -1 && idIndex < matches.length)
                    //    matches[idIndex] = true;
                    
                    //loop through to find all indices to mark all that are found
                    for(int j = 0; j < idVector.size(); j++) {
                        if(annList[i].equals((String)(idVector.elementAt(j))))
                            matches[j] = true;
                    }
                    
                }
            }
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }
            
        } else {
            allIndices = experiment.getColumnIndicesCopy();
            annList = new String[experiment.getNumberOfSamples()];
            String oldLabel = data.getCurrentSampleLabelKey();
            data.setSampleLabelKey(key);
            
            Vector idVector = new Vector(annList.length);
            for(int i = 0; i < ids.length; i++) {
                idVector.addElement(ids[i]);
            }
            
            for(int i = 0; i < allIndices.length; i++) {
                annList[i] = data.getFullSampleName(i);
            }
            
            for(int i = 0; i < annList.length; i++) {
                if(idVector.contains(annList[i])) {
                    indicesVector.addElement(new Integer(allIndices[i]));
                    //loop to id all indices that match in case of duplicates, see gene section above for alt.
                    for(int j = 0; j < idVector.size(); j++) {
                        if(annList[i].equals((String)(idVector.elementAt(j))))
                            matches[j] = true;
                    }
                }
            }
            
            indices = new int[indicesVector.size()];
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indicesVector.elementAt(i))).intValue();
            }

            data.setSampleLabelKey(oldLabel);
        }
        return indices;
    }
    
    public void submitCluster( Cluster cluster ) {
        SubmissionManager subManager = new SubmissionManager(this.framework, this);
        subManager.submit(cluster);
    }
    
    //EH Gaggle test
    public void broadcastGeneClusters( Cluster[] clusters ) {
        this.framework.broadcastGeneClusters(clusters);
    }
    
}
