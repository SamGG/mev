/*
 * FlankingRegion.java
 *
 * Created on December 25, 2002, 7:00 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class FlankingRegion implements ICGHDataRegion{
    public static final int AMPLIFICATION = 0;
    public static final int DELETION = 1;
    public static final int DELETION_1_COPY = 2;
    public static final int DELETION_2_COPY = 3;
    public static final int AMPLIFICATION_1_COPY = 4;
    public static final int AMPLIFICATION_2_COPY = 5;


    /** Holds value of property start. */
    private int start;

    /** Holds value of property stop. */
    private int stop;

    /** Holds value of property type. */
    private int type;

    /** Holds value of property chromosome. */
    private int chromosome;

    /** Holds value of property specifier. */
    private int specifier;

    /** Holds value of property startClone. */
    private CGHClone startClone;

    /** Holds value of property stopClone. */
    private CGHClone stopClone;

    /** Creates a new instance of FlankingRegion */
    public FlankingRegion() {
    }

    public FlankingRegion(int start, int stop, int chromosome) {
        this(start, stop, -1, chromosome);
    }

    public FlankingRegion(int start, int stop, int type, int chromosome) {
        this.start = start;
        this.stop = stop;
        this.type = type;
        this.chromosome = chromosome;
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

    /** Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return this.type;
    }

    /** Setter for property type.
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }

    /** Getter for property chromosome.
     * @return Value of property chromosome.
     */
    public int getChromosomeIndex() {
        return this.chromosome;
    }

    public int getChromosomeNumber() {
        return this.chromosome + 1;
    }

    public int getChromosome() {
        return this.chromosome;
    }

    /** Setter for property chromosome.
     * @param chromosome New value of property chromosome.
     */
    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }

    /** Getter for property specifier.
     * @return Value of property specifier.
     */
    public int getSpecifier() {
        return this.specifier;
    }

    /** Setter for property specifier.
     * @param specifier New value of property specifier.
     */
    public void setSpecifier(int specifier) {
        this.specifier = specifier;
    }

    /** Getter for property startClone.
     * @return Value of property startClone.
     */
    public CGHClone getStartClone() {
        return this.startClone;
    }

    /** Setter for property startClone.
     * @param startClone New value of property startClone.
     */
    public void setStartClone(CGHClone startClone) {
        this.startClone = startClone;
    }

    /** Getter for property stopClone.
     * @return Value of property stopClone.
     */
    public CGHClone getStopClone() {
        return this.stopClone;
    }

    /** Setter for property stopClone.
     * @param stopClone New value of property stopClone.
     */
    public void setStopClone(CGHClone stopClone) {
        this.stopClone = stopClone;
    }

    public String getName(){
        return "";
    }
}
