/*
 * CytoBand.java
 *
 * Created on January 23, 2003, 5:40 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.sql.*;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CytoBand {

    /** Holds value of property chrom. */
    private int chromosome;

    /** Holds value of property chromStart. */
    private int chromStart;

    /** Holds value of property chromEnd. */
    private int chromEnd;

    /** Holds value of property name. */
    private String name;

    /** Holds value of property stain. */
    private String stain;

    /** Creates a new instance of CytoBand */
    public CytoBand() {
    }

    public CytoBand(String chromosome, int chromStart, int chromEnd, String name, String stain, int species){
        this.chromosome = CGHUtility.convertStringToChrom(chromosome, species);
        this.chromStart = chromStart;
        this.chromEnd = chromEnd;
        this.name = name;
        this.stain = stain;
    }

    public void populate(ResultSet rs, int species){
        try{
            this.chromosome = CGHUtility.convertStringToChrom(rs.getString("chrom"), species);
            this.chromStart = rs.getInt("chromStart");
            this.chromEnd = rs.getInt("chromEnd");
            this.name = rs.getString("name");
            this.stain = rs.getString("gieStain");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /** Getter for property chrom.
     * @return Value of property chrom.
     */
    public int getChromosome() {
        return this.chromosome;
    }

    /** Setter for property chrom.
     * @param chrom New value of property chrom.
     */
    public void setChromosome(int chrom) {
        this.chromosome = chrom;
    }

    /** Getter for property chromStart.
     * @return Value of property chromStart.
     */
    public int getChromStart() {
        return this.chromStart;
    }

    /** Setter for property chromStart.
     * @param chromStart New value of property chromStart.
     */
    public void setChromStart(int chromStart) {
        this.chromStart = chromStart;
    }

    /** Getter for property chromEnd.
     * @return Value of property chromEnd.
     */
    public int getChromEnd() {
        return this.chromEnd;
    }

    /** Setter for property chromEnd.
     * @param chromEnd New value of property chromEnd.
     */
    public void setChromEnd(int chromEnd) {
        this.chromEnd = chromEnd;
    }

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }

    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Getter for property stain.
     * @return Value of property stain.
     */
    public String getStain() {
        return this.stain;
    }

    /** Setter for property stain.
     * @param stain New value of property stain.
     */
    public void setStain(String stain) {
        this.stain = stain;
    }

}
