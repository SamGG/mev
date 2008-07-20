/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * RefGeneLinkData.java
 *
 * Created on January 23, 2003, 1:51 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.sql.ResultSet;

import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class RefGeneLinkData implements IGeneData {

    /** Holds value of property chromosome. */
    private int chromosome;



    /** Holds value of property geneName. */
    private String geneName;

    /** Holds value of property locusLinkId. */
    private int locusLinkId;

    /** Holds value of property start. */
    private int start;

    /** Holds value of property stop. */
    private int stop;

    /** Creates a new instance of RefGeneLinkData */
    public RefGeneLinkData() {
    }

    /** Getter for property chromosome.
     * @return Value of property chromosome.
     */
    public int getChromosome() {
        return this.chromosome;
    }

    /** Setter for property chromosome.
     * @param chromosome New value of property chromosome.
     */
    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }


    /** Getter for property geneName.
     * @return Value of property geneName.
     */
    public String getGeneName() {
        return this.geneName;
    }

    /** Setter for property geneName.
     * @param geneName New value of property geneName.
     */
    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    /** Getter for property locusLinkId.
     * @return Value of property locusLinkId.
     */
    public int getLocusLinkId() {
        return this.locusLinkId;
    }

    /** Setter for property locusLinkId.
     * @param locusLinkId New value of property locusLinkId.
     */
    public void setLocusLinkId(int locusLinkId) {
        this.locusLinkId = locusLinkId;
    }

    public void populate(ResultSet rs, int species){
        try{
            this.chromosome = CGHUtility.convertStringToChrom(rs.getString("chrom"), species);
            this.start = rs.getInt("txStart");
            this.stop = rs.getInt("txEnd");
            this.geneName = rs.getString("name");
            //this.product = rs.getString("product");

            this.locusLinkId = rs.getInt("locusLinkId");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String toString(){
        return geneName;
    }

    public int getChromosomeIndex() {
        return this.chromosome - 1;
    }

    public String getName() {
        return this.geneName;
    }

    /** Getter for property start.
     * @return Value of property start.
     */
    public int getStart() {
        return this.start;
    }

    /** Setter for property start.
     * @param start New value of property start.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /** Getter for property stop.
     * @return Value of property stop.
     */
    public int getStop() {
        return this.stop;
    }

    /** Setter for property stop.
     * @param stop New value of property stop.
     */
    public void setStop(int stop) {
        this.stop = stop;
    }

}
