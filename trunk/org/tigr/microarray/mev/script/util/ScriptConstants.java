/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptConstants.java
 *
 * Created on March 4, 2004, 11:09 AM
 */

package org.tigr.microarray.mev.script.util;

/** ScriptConstants contains primary constants used under the
 * scripting packages.
 * @author braisted
 */
public class ScriptConstants {
    
    //need to modify to 
    /** Current document type string.
     */    
    public static final String DOCTYPE_STRING = "<!DOCTYPE TM4ML SYSTEM " +
              "\"mev_script_dtd.dtd\">";
    
    //cluster types
    public static final int CLUSTER_TYPE_GENE = 0;  
    public static final int CLUSTER_TYPE_EXPERIMENT = 1;
    
    //Algorithm types  
    public static final String ALGORITHM_TYPE_CLUSTER = "cluster";    
    public static final String ALGORITHM_TYPE_CLUSTER_GENES = "cluster-genes";    
    public static final String ALGORITHM_TYPE_CLUSTER_EXPERIMENTS = "cluster-experiments";
    public static final String ALGORITHM_TYPE_CLUSTER_SELECTION = "cluster-selection";
    public static final String ALGORITHM_TYPE_GENE_CLUSTER_SELECTION = "gene-cluster-selection";
    public static final String ALGORITHM_TYPE_EXPERIMENT_CLUSTER_SELECTION = "experiment-cluster-selection";
    public static final String ALGORITHM_TYPE_ADJUSTMENT = "data-adjustment";
    public static final String ALGORITHM_TYPE_NORMALIZATION = "data-normalization";
    public static final String ALGORITHM_TYPE_VISUALIZATION = "data-visualization";
    
    //Ouput data classes
    public static final String OUTPUT_DATA_CLASS_SINGLE_OUTPUT = "single-output";    
    public static final String OUTPUT_DATA_CLASS_MULTICLUSTER_OUTPUT = "multi-cluster-output";
    public static final String OUTPUT_DATA_CLASS_GENE_MULTICLUSTER_OUTPUT = "multi-gene-cluster-output";
    public static final String OUTPUT_DATA_CLASS_EXPERIMENT_MULTICLUSTER_OUTPUT = "multi-experiment-cluster-output";
    public static final String OUTPUT_DATA_CLASS_PARTITION_OUTPUT = "partition-output";
    public static final String OUTPUT_DATA_CLASS_CLUSTER_SELECTION_OUTPUT = "cluster-selection-output";

    public static final String MEV_COMMENT_INDENT = "            ";    

    //primary input data types
    private static final String INPUT_DATA_TYPE_MEV = "mev";
    private static final String INPUT_DATA_TYPE_TAV = "tav";
    private static final String INPUT_DATA_TYPE_STANFORD = "stanford";
    private static final String INPUT_DATA_TYPE_GENEPIX = "gpr";
    private static final String INPUT_DATA_TYPE_AFFY_ABS = "affy-abs";
    private static final String INPUT_DATA_TYPE_AFFY_REF = "affy-ref";
    private static final String INPUT_DATA_TYPE_AFFY_MEAN = "affy-mean";
    
    //primary inpt file types
    private static final String INPUT_FILE_TYPE_DATA = "data-file";
    private static final String INPUT_FILE_ANNOT_DATA = "data-file";
    private static final String INPUT_FILE_PREFERENCE_DATA = "preference-file";

    //output modes
    public static final int SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT = 0;
    public static final int SCRIPT_OUTPUT_MODE_FILE_OUTPUT = 1;
    public static final int SCRIPT_OUTPUT_MODE_EXTERNAL_OUTPUT = 2;
    
    //adjustement constants 
    //'spots'
    private static final int ADJUSTMENT_NORM_SPOTS = 101;
    private static final int ADJUSTMENT_DIV_SPOTS_RMS = 102;
    private static final int ADJUSTMENT_DIV_SPOTS_SD = 103;
    private static final int ADJUSTMENT_SPOTS_MEAN_CENTER = 104;
    private static final int ADJUSTMENT_SPOTS_MEDIANN_CENTER = 105;
    private static final int ADJUSTMENT_SPOTS_DIGITAL = 106;

    //'Exp'
    private static final int ADJUSTMENT_NORM_EXPS = 201;
    private static final int ADJUSTMENT_DIV_EXPS_RMS = 202;
    private static final int ADJUSTMENT_DIV_EXPS_SD = 203;
    private static final int ADJUSTMENT_EXPS_MEAN_CENTER = 204;
    private static final int ADJUSTMENT_EXPS_MEDIANN_CENTER = 205;
    private static final int ADJUSTMENT_EXPS_DIGITAL = 206;
    
    //Cutoff / Filtering constants
    private static final int ADJUSTMENT_EXP_PERCENT_CUTOFF = 301;
    private static final int ADJUSTMENT_EXP_LOWER_CUTOFF = 302;
    
    public String [] getAttributeNames (String ElementType) {
        return null;
    }
}
