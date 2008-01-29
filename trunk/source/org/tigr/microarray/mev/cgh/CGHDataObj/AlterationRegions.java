/*
 * AlterationRegions.java
 *
 * Created on January 25, 2003, 12:28 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class AlterationRegions {
    int chromosome;

    /** Holds value of property alterationRegions. */
    private Vector alterationRegions;

    /** Creates a new instance of AlterationRegions */
    public AlterationRegions() {
        this.alterationRegions = new Vector();
    }

    public AlterationRegions(int chromosome) {
        this.alterationRegions = new Vector();
        this.chromosome = chromosome;
    }

    /** Getter for property alterationRegions.
     * @return Value of property alterationRegions.
     */
    public Vector getAlterationRegions() {
        return this.alterationRegions;
    }

    /** Setter for property alterationRegions.
     * @param alterationRegions New value of property alterationRegions.
     */
    public void setAlterationRegions(Vector alterationRegions) {
        this.alterationRegions = alterationRegions;
    }

    public AlterationRegion getAlterationRegion(int start, int stop, int type, int numSamples){
        if(stop < start){
            int tmp = start;
            start = stop;
            stop = tmp;
        }

        Iterator it = alterationRegions.iterator();

        AlterationRegion curRegion;
        while(it.hasNext()){
            curRegion = (AlterationRegion)it.next();

            if(curRegion.getDataRegion().getStart() == start && curRegion.getDataRegion().getStop() == stop){
                return curRegion;
            }
        }

        curRegion = new AlterationRegion();
        FlankingRegion flankingRegion = new FlankingRegion(start, stop, type, chromosome);
        curRegion.setDataRegion(flankingRegion);
        curRegion.setNumSamples(numSamples);

        alterationRegions.add(curRegion);

        return curRegion;
    }

    /** Getter for property chromosome.
     * @return Value of property chromosome.
     */
    public int getChromosome() {
        return chromosome;
    }

    /** Setter for property chromosome.
     * @param chromosome New value of property chromosome.
     */
    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }

}
