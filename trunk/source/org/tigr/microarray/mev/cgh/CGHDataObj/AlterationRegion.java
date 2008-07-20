/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * AlterationRegion.java
 *
 * Created on May 19, 2003, 12:21 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.text.NumberFormat;
import java.util.Vector;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class AlterationRegion {

    ICGHDataRegion dataRegion;
    int numAlterations = 0;
    //float percentAltered;
    int numSamples;
    Vector alteredExperiments = new Vector();

    int numAmplifications;
    int numDeletions;

    String name;
    float[] alteredExperimentValues;

    /** Creates a new instance of AlterationRegion */
    public AlterationRegion() {
    }

    /** Getter for property dataRegion.
     * @return Value of property dataRegion.
     */
    public ICGHDataRegion getDataRegion() {
        return dataRegion;
    }

    /** Setter for property dataRegion.
     * @param dataRegion New value of property dataRegion.
     */
    public void setDataRegion(ICGHDataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    /** Getter for property numAlterations.
     * @return Value of property numAlterations.
     */
    public int getNumAlterations() {
        return numAlterations;
    }

    /** Setter for property numAlterations.
     * @param numAlterations New value of property numAlterations.
     */
    public void setNumAlterations(int numAlterations) {
        this.numAlterations = numAlterations;
    }

    public void incrementAlterations(){
        this.numAlterations++;
    }

    /** Getter for property percentAltered.
     * @return Value of property percentAltered.
     */
    public float getPercentAltered() {
        return (float)numAlterations / (float)numSamples;
    }


    /** Getter for property alteredExperiments.
     * @return Value of property alteredExperiments.
     */
    public Vector getAlteredExperiments() {
        return alteredExperiments;
    }

    /** Setter for property alteredExperiments.
     * @param alteredExperiments New value of property alteredExperiments.
     */
    public void setAlteredExperiments(Vector alteredExperiments) {
        this.alteredExperiments = alteredExperiments;
    }

    /** Getter for property numSamples.
     * @return Value of property numSamples.
     */
    public int getNumSamples() {
        return numSamples;
    }

    /** Setter for property numSamples.
     * @param numSamples New value of property numSamples.
     */
    public void setNumSamples(int numSamples) {
        alteredExperimentValues = new float[numSamples];
        for(int i = 0; i < alteredExperimentValues.length; i++){
            alteredExperimentValues[i] = 0;
        }
        this.numSamples = numSamples;
    }

    public float[] getAlteredExperimentValues(){
        return this.alteredExperimentValues;
    }

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        NumberFormat nf = NumberFormat.getInstance();
        String name = "Chrom: " + (dataRegion.getChromosomeIndex() + 1);
        String startString = nf.format(dataRegion.getStart() / 1000000);
        //name += " Start: " + dataRegion.getStart();
        name += " Start: " + startString;
        String stopString = nf.format(dataRegion.getStop() / 1000000);
        //name += " Stop: " + dataRegion.getStop();
        name += " Stop: " + stopString;
        return name;
        //return name;
    }

    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }



    /** Getter for property numDeletions.
     * @return Value of property numDeletions.
     */
    public int getNumDeletions() {
        return numDeletions;
    }

    /** Setter for property numDeletions.
     * @param numDeletions New value of property numDeletions.
     */
    public void setNumDeletions(int numDeletions) {
        this.numDeletions = numDeletions;
    }

    /** Getter for property numAmplifications.
     * @return Value of property numAmplifications.
     */
    public int getNumAmplifications() {
        return numAmplifications;
    }

    /** Setter for property numAmplifications.
     * @param numAmplifications New value of property numAmplifications.
     */
    public void setNumAmplifications(int numAmplifications) {
        this.numAmplifications = numAmplifications;
    }

    /** Getter for property percentAltered.
     * @return Value of property percentAltered.
     */
    public float getPercentAmplified() {
        return (float)numAmplifications / (float)numSamples;
    }

    /** Getter for property percentAltered.
     * @return Value of property percentAltered.
     */
    public float getPercentDeleted() {
        return (float)numDeletions / (float)numSamples;
    }

}
