/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ExperimentUtil.java,v $
 * $Revision: 1.3 $
 * $Date: 2003-12-08 17:06:24 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import java.awt.Frame;
import javax.swing.JFileChooser;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * This class contains set of static methods to store
 * an experiment data.
 *
 * @version 1.0
 * @author Aleksey D.Rezantsev
 */
public class ExperimentUtil {
    
    public static final int INTEGER_TYPE = 10;
    public static final int FLOAT_TYPE = 11;
    public static final int DOUBLE_TYPE = 12;
    public static final int STRING_TYPE = 13;
    public static final int BOOLEAN_TYPE = 14;
    
    
    /**
     * Saves all the experiment values.
     */
    public static void saveExperiment(Frame frame, Experiment experiment, IData data) throws Exception {
        saveExperiment(frame, experiment, data, createDefaultRows(experiment.getNumberOfGenes()));
    }
    
    /**
     * Saves values from common experiment with specified rows.
     */
    public static void saveExperiment(Frame frame, IData data, int[] rows) throws Exception {
        saveExperiment(frame, data.getExperiment(), data, rows);
    }
    
    /**
     * Saves values from specified experiment and its rows.
     */
    public static void saveExperiment(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveCluster(file, experiment, data, rows);
        }
    }
    
    /**
     * Saves values from specified experiment and cluster.
     */
    public static void saveExperiment(Frame frame, Experiment experiment, IData data, int[][] clusters) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveCluster(aFile, experiment, data, clusters[i]);
            }
        }
    }
    
    public static void saveAllGeneClustersWithAux(Frame frame, Experiment experiment, IData data, int[][] clusters, String[] auxTitles, Object[][] auxData) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveGeneClusterWithAux(aFile, experiment, data, clusters[i], auxTitles, auxData);
            }
        }
    }  
    
    private static void saveGeneClusterWithAux(File file, Experiment experiment, IData data, int [] rows, String [] auxTitles, Object [][] auxData) throws Exception{
        int[] typeArray = getTypes(auxData);
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        
        if(fieldNames == null)
            return;
        
        out.print("Original row");
        out.print("\t");
        
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        for(int i = 0; i < auxTitles.length; i++){
            out.print("\t"+auxTitles[i]);
        }        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(i)));
        }
        /*
        for (int i=0; i<data.getFeaturesCount(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(i));
        }
         */
        //aux titles
        /*
        for(int i = 0; i < auxTitles.length; i++){
            out.print("\t"+auxTitles[i]);
        }
         **/
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //JCB handles cuttoffs, gets gene mapping
            out.print("\t");
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            
            for(int j = 0; j < auxData[0].length; j++){
                out.print("\t");//+auxData[rows[i]][j]);
                //System.out.println("auxData.length = " + auxData.length + ", auxData[0].length = " + auxData[0].length + ", i = " + i + ", j = " + j + ", rows[i] = " + rows[i]);
                printDataType(out, auxData[rows[i]][j], typeArray[j]);
            }    
            
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            /*
            for (int j=0; j<data.getFeaturesCount(); j++) {
                out.print("\t");
                out.print(Float.toString(data.getRatio(j, rows[i], IData.LOG)));
            }
             */
            
            out.print("\n");
        }
        out.flush();
        out.close();
    }    
    
    /**
     * Returns a file choosed by the user.
     */
    private static File getFile(Frame frame) {
        File file = null;
        final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int ret = fc.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }
    
    /**
     * Saves values from specified rows in IData.
     */
    public static void saveGeneCluster(Frame frame, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveGeneCluster(file, data, rows);
        }
    }
    
    private static void saveGeneCluster(File file, IData data, int [] rows) throws Exception{
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        
        if(fieldNames == null)
            return;
        
        out.print("Original row");
        out.print("\t");
        
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        //out.print("UniqueID\tName");
        for (int i=0; i<data.getFeaturesCount(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(i));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(rows[i] + 1));  //JCB handles cuttoffs, gets gene mapping
            out.print("\t");
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(rows[i], k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<data.getFeaturesCount(); j++) {
                out.print("\t");
                out.print(Float.toString(data.getRatio(j, rows[i], IData.LOG)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    
    /**
     * Saves values from specified rows in IData with specified auxilary header titles and data.
     * (rows are mapped IData indices, Experiment object is used for mapping)
     */
    public static void saveGeneClusterWithAux(Frame frame, Experiment experiment, IData data, int[] rows, String [] auxTitles, Object auxData[][]) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveGeneClusterWithAux(file, experiment, data, rows, auxTitles, auxData);
        }
    }
    
    /**
     *  Saves gene cluster and aux data.  Presumes rows are IData indices.
     */
    
    /*
    private static void saveGeneCluster(File file, IData data, int [] rows, String [] auxTitles, Object [][] auxData) throws Exception{
        int[] typeArray = getTypes(auxData);
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        
        if(fieldNames == null)
            return;
        
        out.print("Original row");
        out.print("\t");
        
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        //out.print("UniqueID\tName");
        for (int i=0; i<data.getFeaturesCount(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(i));
        }
        //aux titles
        for(int i = 0; i < auxTitles.length; i++){
            out.print("\t"+auxTitles[i]);
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(rows[i] + 1));  //JCB handles cuttoffs, gets gene mapping
            out.print("\t");
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(rows[i], k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<data.getFeaturesCount(); j++) {
                out.print("\t");
                out.print(Float.toString(data.getRatio(j, rows[i], IData.LOG)));
            }
            
            for(int j = 0; j < auxData[0].length; j++){
                out.print("\t");//+auxData[rows[i]][j]);
                printDataType(out, auxData[rows[i]][j], typeArray[i]);
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    */
    
    
    /**
     * Saves experiment data as a cluster.
     */
    
    private static void saveCluster(File file, Experiment experiment, IData data, int[] rows) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        if(fieldNames == null)
            return;
        out.print("Original row");
        out.print("\t");
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //JCB handles cuttoffs, gets gene mapping
            out.print("\t");
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    
    /**
     * Saves values from specified experiment cluster and its rows.
     */
    public static void saveExperimentCluster(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveExperimentCluster(file, experiment, data, rows);
        }
    }
    
    /**
     * Saves values from specified experiment cluster and its rows.
     */
    public static void saveAllExperimentClusters(Frame frame, Experiment experiment, IData data, int[][] clusters) throws Exception {
        
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveExperimentCluster(aFile, experiment, data, clusters[i]);
            }
        }
    }
    
    
    public static void saveAllExperimentClustersWithAux(Frame frame, Experiment experiment, IData data, int[][] clusters, String[] auxTitles, Object[][] auxData) throws Exception {
        
        File file = getFile(frame);
        if (file != null) {
            File aFile;
            for (int i=0; i<clusters.length; i++) {
                if (clusters[i] == null || clusters[i].length == 0) {
                    continue;
                }
                aFile = new File(file.getPath()+"-"+String.valueOf(i+1)+".txt");
                saveExperimentClusterWithAux(aFile, experiment, data, clusters[i], auxTitles, auxData);
            }
        }
    }    
    
    /**
     * Saves values from specified experiment cluster and its rows and auxillary data.
     */
    public static void saveExperimentClusterWithAux(Frame frame, Experiment experiment, IData data, int[] rows, String [] auxTitles, Object [][] auxData) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveExperimentClusterWithAux(file, experiment, data, rows, auxTitles, auxData);
        }
    }
    
    /**
     *  Saves experiment cluster with auxilary data
     */
    private static void saveExperimentClusterWithAux(File file, Experiment experiment, IData data, int[] experiments, String [] auxTitles, Object [][] auxData) throws Exception {
        int[] typeArray = getTypes(auxData);        
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        
        int numberOfGenes = experiment.getNumberOfGenes();
        
        out.print("Original row");
        out.print("\t");
        
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        //out.print("UniqueID\tName");
        for (int i=0; i<experiments.length; i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(experiments[i])));
        }
        out.print("\n");
        
        //aux titles
        //out.print("\t");        
        for(int i = 0; i < auxTitles.length; i++){
                out.print(auxTitles[i]+"\t");
                for(int k = 0; k < fieldNames.length; k++)
                    out.print("\t");
            
                //out.print(auxTitles[i]+"\t");
                
                for(int j = 0; j < experiments.length; j++){
                    printDataType(out, auxData[experiments[j]][i], typeArray[i]);
                    //out.print(auxData[experiments[j]][i]);
                    if (j < experiments.length - 1)
                        out.print("\t");
                }                
                out.print("\n");            
        }
        //out.print("\n");
        for (int i=0; i<numberOfGenes; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(i) + 1));  //JCB handles cuttoffs, gets gene mapping
            out.print("\t");
            for (int k = 0; k < fieldNames.length; k++) {
                //                out.print(data.getElementAttribute(rows[i], k));
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(i), k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<experiments.length; j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(i, experiment.getSampleIndex(experiments[j]))));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    
    /**
     *  Saves experiment cluster
     */
    private static void saveExperimentCluster(File file, Experiment experiment, IData data, int[] experiments) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        
        int numberOfGenes = experiment.getNumberOfGenes();
        
        out.print("Original row");
        out.print("\t");
        
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiments.length; i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(experiments[i])));
        }
        out.print("\n");
        for (int i=0; i<numberOfGenes; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(i) + 1));  //JCB handles cuttoffs, gets gene mapping
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                //                out.print(data.getElementAttribute(rows[i], k));
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(i), k));  //JCB in case of using cuttoffs, get mapping
                
                if (k < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<experiments.length; j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(i, experiment.getSampleIndex(experiments[j]))));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
    
    
    /**
     * Saves experiment data as a cluster.
     */
    /*
    private static void saveCluster(File file, Experiment experiment, IData data, int[] rows, ) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = TMEV.getFieldNames();
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            if (i < fieldNames.length - 1) {
                out.print("\t");
            }
        }
     
     
     
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            //out.print(data.getUniqueId(rows[i]));
            //out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(rows[i], k));
                if (i < fieldNames.length - 1) {
                    out.print("\t");
                }
            }
            for (int j=0; j<experiment.getNumberOfSamples(); j++) {
                out.print("\t");
                out.print(Float.toString(experiment.get(rows[i], j)));
            }
            out.print("\n");
        }
        out.flush();
        out.close();
    }
     */
    
    /**
     * Creates array of integers with increasing order.
     */
    private static int[] createDefaultRows(final int genes) {
        int[] rows = new int[genes];
        for (int i=0; i<genes; i++) {
            rows[i] = i;
        }
        return rows;
    }
    
    private static void printDataType(PrintWriter out, Object obj, int dataType) {
        switch(dataType) {
            case ExperimentUtil.BOOLEAN_TYPE:
                out.print(((Boolean)obj).booleanValue());
                break;
            case ExperimentUtil.DOUBLE_TYPE:
                out.print(((Double)obj).doubleValue());
                break;
            case ExperimentUtil.FLOAT_TYPE:
                out.print(((Float)obj).floatValue());
                break;
            case ExperimentUtil.INTEGER_TYPE:
                out.print(((Integer)obj).intValue());
                break;
            case ExperimentUtil.STRING_TYPE:
                out.print((String)obj);
                break;
            default: 
                out.print(obj);
                break;
        }
        
        return;
    }
    
    private static int[] getTypes (Object[][] objData) {
        int[] types = new int[objData[0].length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getObjectType(objData[0][i]);
            //Object 
        }
        return types;
    }
    
    private static int getObjectType(Object obj) {
        int obType = -1;
        if (obj instanceof Boolean) {
            return ExperimentUtil.BOOLEAN_TYPE;
        } else if (obj instanceof Double) {
            return ExperimentUtil.DOUBLE_TYPE;
        } else if (obj instanceof Float) {
            return ExperimentUtil.FLOAT_TYPE;
        } else if (obj instanceof Integer) {
            return ExperimentUtil.INTEGER_TYPE;
        } else if (obj instanceof String) {
            return ExperimentUtil.STRING_TYPE;
        } else {
            return obType;
        }
    }
}









