/*
 * CGHClone.java
 *
 * Created on September 20, 2002, 12:05 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

//import java.sql.ResultSet;
import java.util.ArrayList;

import org.tigr.microarray.mev.CGHSlideDataElement;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cluster.gui.IData;
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHClone implements ICGHDataRegion {
    public static final int NOT_FOUND = -1;


    /** Holds value of property name. */
    protected String name;

    /** Holds value of property chromosome. */
    private int chromosome;

    /** Holds value of property start. */
    protected int start;

    /** Holds value of property stop. */
    protected int stop;

    /** Additional/Optional property. Used in ChARM for holding a temp ratio */
    protected float ratio = 0.0f;
    
    /** Stores sorted index of clones **/
    protected int sortedIndex = 0;
    
    /** Creates a new instance of CGHClone */
    public CGHClone() {
    }

    /**
     * Rakitm
     * @param name
     * @param chromosome
     * @param start
     * @param stop
     */
    public CGHClone(String name, String chromosome, String start, String stop, int species){
        this.name = name;
        try {
        	this.chromosome = Integer.parseInt(chromosome);
        }
        catch (NumberFormatException e) {
        	this.chromosome = CGHUtility.convertStringToChrom(chromosome, species);
        	/*
        	if (chromosome.equalsIgnoreCase("X") || chromosome.equalsIgnoreCase("chrX")){
        		this.chromosome = 23;
        	}
        	else if (chromosome.equalsIgnoreCase("Y")){
        		this.chromosome = 24;
        	}
        	else {
        		System.out.print("CGHClone Exception : ");
        		e.printStackTrace();
        	}
        	*/
        }
        this.start = Integer.parseInt(start);
        this.stop = Integer.parseInt(stop);
    }

    /**
     *
     * @param name
     * @param chromosome
     * @param start
     * @param stop
     */
    public CGHClone(String name, int chromosome, int start, int stop){
        this.name = name;
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
    }

    /**
     *
     * @param name
     * @param chromosome
     * @param start
     * @param stop
     */
    public CGHClone(String name, String chromosome, int start, int stop, int species){
        this(name, CGHUtility.convertStringToChrom(chromosome, species), start, stop);
    }

    public String toString(){
        return this.name;
    }

    /*
    public void populate(ResultSet rs){
        try{

            this.name = rs.getString("name").trim().toUpperCase();
            this.chromosome = CGHUtility.convertStringToChrom(rs.getString("chrom"));
            this.start = rs.getInt("chromStart");
            this.stop = rs.getInt("chromEnd");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    */

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

    public int getChromosomeIndex(){
        return this.chromosome - 1;
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

    public boolean equals(Object obj){
    	CGHClone obj_T = (CGHClone)obj;
    	/*
    	System.out.println("CGHClone.equals()");
    	System.out.println("Name:  " + this.getName() + ", "+ obj.getName());
    	System.out.println("Chr:   " + this.getChromosome() + ", " + obj.getChromosome());
    	System.out.println("Start: " + this.getStart() + ", " + obj.getStart());
    	System.out.println("Stop:  " + this.getStop() + ", " + obj.getStop());
    	*/
    	if(this.getChromosome() == obj_T.getChromosome() &&
    			this.getName().equals(obj_T.getName()) &&
    			this.getStart() == obj_T.getStart()){
            return true;
        }else {
        	return false;
        }
    }

    public Object clone(){
    	return new CGHClone(this.getName().trim(), this.getChromosome(), this.getStart(), this.getStop());
    }
    
    /**
     * Used as a temporary placeholder for *any experiment.
     * ChARM addition for GeneList
     * @param ratio
     */
    public void setRatio (float ratio){
    	this.ratio = ratio;
    }
    
    /**
     * Used as a temporary placeholder for *any experiment.
     * ChARM addition for GeneList
     * @return
     */
    public float getRatio() {
    	return ratio;
    }
    
    /**
     * Field to hold the absolute index of the clone once sorted by Chr & Pos.
     * The Field id set up during the data loading once its sorted.
     * Setter 
     */
    public void setSortedIndex(int ind) {
    	sortedIndex = ind;
    }
    
    /**
     * Field to hold the absolute index of the clone once sorted by Chr & Pos.
     * Getter 
     */
    public int getSortedIndex() {
    	return sortedIndex;
    }
    
    /**
     * To Get extra Fiels Info
     */
    public String getDesc(IData data) {
    	ArrayList features = data.getFeaturesList();
    	ISlideData temp = (ISlideData)features.get(0);
    	CGHSlideDataElement sde_T1 = (CGHSlideDataElement)temp.getSlideDataElement(this.sortedIndex);
		return sde_T1.getDesc();
    }
}
