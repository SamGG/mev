/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHBrowserModelAdaptor.java
 *
 * Created on July 4, 2003, 8:13 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

//import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHFeatureData;
import java.util.EventListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHBrowserModelAdaptor {
    public static final int ALL_EXPERIMENTS = -1;
    public static final int ALL_CHROMOSOMES = -1;

    public static final int CLONE_VALUES_P_SCORE = 0;
    public static final int CLONE_VALUES_DYE_SWAP = 1;
    public static final int CLONE_VALUES_LOG_AVERAGE_INVERTED= 2;
    public static final int CLONE_VALUES_LOG_DYE_SWAP = 3;

    public static final int CLONE_VALUES_RATIOS= 4;
    public static final int CLONE_VALUES_LOG_RATIOS = 5;

    int cloneValueType;

    EventListenerList listenerList = new EventListenerList();

    int[] mapCloneToIndex;
    int[] mapIndexToClone;

    int experimentIndices[];
    int chromosomeIndex;
    int numClones;
    IData data;

    /** Creates a new instance of CGHBrowserModelAdaptor */
    public CGHBrowserModelAdaptor(IData data, int experimentIndex, int chromosomeIndex, int cloneValueType) {
        this.data = data;
        setExperimentIndex(experimentIndex);
        setChromosomeIndex(chromosomeIndex);
        this.cloneValueType = cloneValueType;
        setNumClones(data.getNumDataPointsInChrom(chromosomeIndex));
    }

    /**
     * Generates a mapping between the clone indexes as stored in the data, and
     * their positions on the viewable chart or table
     */
    public void generateIndices(){

        mapCloneToIndex = new int[numClones];
        int[] mapTmpIndexToClone = new int[numClones];

        ISlideData[] sampleData = new ISlideData[experimentIndices.length];
        for(int i = 0; i < experimentIndices.length; i++){
            sampleData[i] = (ISlideData)data.getFeaturesList().get(experimentIndices[i]);
        }

        int chartIndex = 0;

        for(int i = 0; i < numClones; i++){
            boolean isMissing = true;
            for(int sampleIndex = 0; sampleIndex < sampleData.length; sampleIndex++){
                if(! sampleData[sampleIndex].isMissingData(getDataCloneIndex(i))){
                    isMissing = false;
                    break;
                }
            }

            if(isMissing){
                mapCloneToIndex[i] = -1;
            }else{
                mapCloneToIndex[i] = chartIndex;
                mapTmpIndexToClone[chartIndex] = i;
                chartIndex++;
            }
        }

        mapIndexToClone = new int[chartIndex];
        for(int i = 0; i < mapIndexToClone.length; i++){
            mapIndexToClone[i] = mapTmpIndexToClone[i];
        }
    }

    public int getSeriesSize(){
        return mapIndexToClone.length;
    }

    public void addChangeListener(ChangeListener listener){
        listenerList.add(ChangeListener.class, listener);
    }

    /** This method is just used to determine whether to get the relative or absolute
     * clone index from the data
     */
    public int getDataCloneIndex(int index){
        if(chromosomeIndex == ALL_CHROMOSOMES){
            return index;
        }else{
            return data.getCloneIndex(index, chromosomeIndex);
        }
    }

    /* Returns the index of a clone as stored in the data, given its relative chart index
     * @param coordinateIndex the index of the clone on the chart
     */
    public int getCloneIndex(int coordinateIndex){
        return getDataCloneIndex(mapIndexToClone[coordinateIndex]);
    }

    /* Returns the index on the chart of a clone
     * @param cloneIndex the index of the clone as stored in the data
     */
    public int getIndexOf(int cloneIndex){
        return mapCloneToIndex[cloneIndex];
    }

    /** Getter for property mapCloneToIndex.
     * @return Value of property mapCloneToIndex.
     */
    public int[] getMapCloneToIndex() {
        return this.mapCloneToIndex;
    }

    /** Setter for property mapCloneToIndex.
     * @param mapCloneToIndex New value of property mapCloneToIndex.
     */
    public void setMapCloneToIndex(int[] mapCloneToIndex) {
        this.mapCloneToIndex = mapCloneToIndex;
    }

    /** Getter for property mapIndexToClone.
     * @return Value of property mapIndexToClone.
     */
    public int[] getMapIndexToClone() {
        return this.mapIndexToClone;
    }

    /** Setter for property mapIndexToClone.
     * @param mapIndexToClone New value of property mapIndexToClone.
     */
    public void setMapIndexToClone(int[] mapIndexToClone) {
        this.mapIndexToClone = mapIndexToClone;
    }

    /** Getter for property experimentIndices.
     * @return Value of property experimentIndices.
     */
    public int[] getExperimentIndices() {
        return this.experimentIndices;
    }

    /** Setter for property experimentIndices.
     * @param experimentIndices New value of property experimentIndices.
     */
    public void setExperimentIndices(int[] experimentIndices) {
        this.experimentIndices = experimentIndices;
    }

    /** Getter for property chromosomeIndex.
     * @return Value of property chromosomeIndex.
     */
    public int getChromosomeIndex() {
        return chromosomeIndex;
    }

    public void setChromosomeIndex(int chromosomeIndex){
        this.chromosomeIndex = chromosomeIndex;
        if(chromosomeIndex == ALL_CHROMOSOMES){
            this.numClones = data.getFeaturesSize();
        }else{
            this.numClones = data.getNumDataPointsInChrom(chromosomeIndex);
        }

        generateIndices();
        fireDataChanged();
    }

    /** Getter for property numClones.
     * @return Value of property numClones.
     */
    public int getNumClones() {
        return numClones;
    }

    /** Setter for property numClones.
     * @param numClones New value of property numClones.
     */
    public void setNumClones(int numClones) {
        this.numClones = numClones;
    }

    /** Getter for property data.
     * @return Value of property data.
     */
    public IData getData() {
        return data;
    }

    /** Setter for property data.
     * @param data New value of property data.
     */
    public void setData(IData data) {
        this.data = data;
    }

    public void setExperimentIndex(int experimentIndex){
    	//Raktim
    	System.out.println("CGHBrowserModelAdaptor.setExperimentIndex experimentIndex: " + experimentIndex);
        if(experimentIndex == ALL_EXPERIMENTS){
            this.experimentIndices = new int[data.getFeaturesCount()];
            for(int i = 0; i < experimentIndices.length; i++){
                experimentIndices[i] = i;
            }
        }else{
            this.experimentIndices = new int[1];
            this.experimentIndices[0] = experimentIndex;
        }

        generateIndices();
        fireDataChanged();
    }


    public void fireDataChanged(){
        EventListener[] listeners = listenerList.getListeners(ChangeListener.class);
        for(int i = 0; i < listeners.length; i++){
            ((ChangeListener)listeners[i]).stateChanged(new ChangeEvent(new Object()));
        }
    }

    /** Getter for property cloneValueType.
     * @return Value of property cloneValueType.
     */
    public int getCloneValueType() {
        return cloneValueType;
    }

    /** Setter for property cloneValueType.
     * @param cloneValueType New value of property cloneValueType.
     */
    public void setCloneValueType(int cloneValueType) {
        this.cloneValueType = cloneValueType;
        fireDataChanged();
    }

}
