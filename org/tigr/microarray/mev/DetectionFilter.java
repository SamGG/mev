/*
    DetectionFilter holds data required by MAD to filter out user specified affy data.
    Essentially it lists group membership of chips and the minimum number of (P)resent genes
    required across all members of each group in order to use that gene for further analysis

    LOOK INTO copy constructors!!!


    Patrick Cahan
    pcahan1@umbc.edu
*/
package org.tigr.microarray.mev;

import java.io.Serializable;

public class DetectionFilter implements Serializable {
    public static final long serialVersionUID = 100010201110001L;

    // number of (P)s of a gene in each group required
    // assume num of group = 2 for now
    private int[] num_required;
    private static int NUM_OF_GROUPS = 2;
    private boolean both;

    // index corresponds to file index returned by MAD.getsample(int)
    private int[] group_membership;

    // Initially place all files in group 1
    // and set num_required to total - 1
    public DetectionFilter(String[] names){
        num_required = new int[NUM_OF_GROUPS];
        group_membership = new int[names.length];

        // initialize all to group 0
        for (int i = 0; i < names.length; i++){
            group_membership[i] = 0;
        }

        set_num_required(0, 1);
        set_num_required(1, 1);
        both = false;
    }

    public int get_num_required(int group_index){
        return num_required[group_index];
    }

    public int get_group_membership(int file_index){
        return group_membership[file_index];
    }

    public boolean get_both(){
        return both;
    }

    public void set_num_required(int group_index, int required){
        num_required[group_index] = required;
    }

    public void set_group_membership(int group_index, int file_index){
        group_membership[file_index] = group_index;
        //System.out.println("file" + file_index + " is now in grp:" + group_membership[file_index]);
    }

    public void set_both(boolean use_both){
        this.both = use_both;
    }

    public boolean keep_gene(String[] detection_calls){
        // tally P calls in each group
        int[] present_calls = {0,0};
        for (int i = 0; i < detection_calls.length; i++){
            if (detection_calls[i].equalsIgnoreCase("P")){
                present_calls[ get_group_membership(i) ]++;
            }
        }


        if (this.get_both()){
          return ( (present_calls[0] >= get_num_required(0)) &&
                  (present_calls[1] >= get_num_required(1)));
        }
        else {
          return ( (present_calls[0] >= get_num_required(0)) ||
                  (present_calls[1] >= get_num_required(1)));
        }

    }

    public String toString(){
        String out = "Num Required Grp 1:" + get_num_required(0);
        out += "\nNum Required Grp 2:" + get_num_required(1);
        for (int i = 0; i < group_membership.length; i++){
            out += "\nsample: " + i + " is in group: " + get_group_membership(i);
        }
        return out;

    }
}