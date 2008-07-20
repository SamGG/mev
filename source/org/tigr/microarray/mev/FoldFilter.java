/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
    DetectionFilter holds data required by MAD to filter out user specified affy data.

    Patrick Cahan
    pcahan1@umbc.edu
*/
package org.tigr.microarray.mev;

import java.io.Serializable;

public class FoldFilter implements Serializable {
    public static final long serialVersionUID = 100010201100001L;

    private float fold_change;
    // index corresponds to file index returned by MAD.getsample(int)
    private int[] group_membership;
    private String divider;

    private static int NUM_OF_GROUPS = 3;
    private static String BOTH = "both";
    private static String GREATER_THAN = ">";
    private static String LESS_THAN = "<";
    private static float INITAL_FOLD_CHANGE = 2.0f;


    // Initially place all files in group 1
    public FoldFilter(String[] names){
        group_membership = new int[names.length];

        // initialize all in group 0
        for (int i = 0; i < names.length; i++){
            group_membership[i] = 0;
        }

        divider = BOTH;
	fold_change = INITAL_FOLD_CHANGE;
    }

    public void set_fold_change(float fold_change){
	this.fold_change = fold_change;
    }

    public float get_fold_change(){
	return fold_change;
    }

    public int get_group_membership(int file_index){
        return group_membership[file_index];
    }

    public String get_divider(){
        return divider;
    }

    public void set_group_membership(int group_index, int file_index){
        group_membership[file_index] = group_index;
        //System.out.println("file" + file_index + " is now in grp:" + group_membership[file_index]);
    }

    public void set_divider(String divider){
        this.divider = divider;
    }

    // returns the number of samples in a specified group
    public int get_num_members(int index){
	int num_members = 0;
	for (int i = 0; i < group_membership.length; i++ ){
	    if (get_group_membership(i) == index) {
		num_members++;
	    }
	}
	return num_members;
    }

    public boolean keep_gene(float[] signals){
        // get mean signal for each group
	float mean[] = new float[3];
	mean[0] = mean[1] = mean[2] = 0.0f;

        for (int i = 0; i < signals.length; i++){
            mean[get_group_membership(i)] += signals[i];
        }
        mean[0] = mean[0]/get_num_members(0);
	mean[1] = mean[1]/get_num_members(1);

	if (divider.equals(GREATER_THAN)){
	    return ( mean[0]/mean[1] > fold_change );
	}
	if (divider.equals(LESS_THAN)){
	    return ( mean[1]/mean[0] > fold_change );
	}
	else {
	    return ( (mean[0]/mean[1] > fold_change) || (mean[1]/mean[0] > fold_change) );
	}
    }

    public String toString(){
	String out = "x";
	if (divider.equals(BOTH)){
	    out = "All genes with a fold change of greater than: " + fold_change;
	}
	if (divider.equals(GREATER_THAN)){
	    out = "Group A > Group B by " + fold_change + " fold.";
	}
	if (divider.equals(LESS_THAN)){
	    out = "Group B > Group A by " + fold_change + "fold.";
	}
        for (int i = 0; i < group_membership.length; i++){
            out += "\nsample: " + i + " is in group: " + get_group_membership(i);
        }
        return out;
    }
}
