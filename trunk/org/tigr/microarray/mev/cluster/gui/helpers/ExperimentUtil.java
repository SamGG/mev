/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ExperimentUtil.java,v $
 * $Revision: 1.6 $
 * $Date: 2004-07-27 19:59:16 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.io.*;
import java.io.File;
import java.io.IOException.*;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.Vector;

import java.awt.Frame;
import javax.swing.*;
import javax.swing.JFileChooser;

import org.tigr.util.StringSplitter;
import org.tigr.util.BrowserLauncher;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;

import org.tigr.microarray.mev.TMEV;

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
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
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
    public static void linkToURL(JFrame frame, Experiment experiment, IData data, int row)  /*throws Exception*/ {
        //NOTE: In this method, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs 
        try {
            File file = TMEV.getConfigurationFile("annotation_URLs.txt");
            //System.out.println("Found annotation file");
            AnnotationURLLinkDialog aDialog = new AnnotationURLLinkDialog(frame, false, experiment, data, row, file);
            aDialog.setVisible(true);
            //if (aDialog.isOkPressed()) lastSelectedAnnotationIndices = aDialog.getLastSelectedIndices();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not link to URL! Make sure \"annotation_URLs.txt\" file in \"config\" directory is in correct format", "Error", JOptionPane.ERROR_MESSAGE);
            //System.out.println("Did not find file");
            //JOptionPane.showMessageDialog(new JFrame(), "Could not find annotation_URLs.txt file in config directory", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void linkToURL(JFrame frame, Experiment experiment, IData data, int row, int[] lastSelectedIndices)  /*throws Exception*/ {
        //int[] indices = {0, 0};
        //NOTE: In this method, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs 
        try {
            File file = TMEV.getConfigurationFile("annotation_URLs.txt");
            System.out.println("Found annotation file");
            AnnotationURLLinkDialog aDialog = new AnnotationURLLinkDialog(frame, false, experiment, data, row, file, lastSelectedIndices);
            aDialog.setVisible(true);
            //if (aDialog.isOkPressed()) 
                //indices = aDialog.getLastSelectedIndices();            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not link to URL! Make sure \"annotation_URLs.txt\" file in \"config\" directory is in correct format", "Error", JOptionPane.ERROR_MESSAGE);            
            //System.out.println("Did not find file");
            //JOptionPane.showMessageDialog(new JFrame(), "Could not find annotation_URLs.txt file in config directory", "Error", JOptionPane.ERROR_MESSAGE);
        }
        //return indices;
    }    
    
    public static void linkToURL(JFrame frame, Experiment experiment, IData data, int row, String annotationKey, int[] lastSelectedIndices) {
  //NOTE: In this method, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs      
        try {
            File file = TMEV.getConfigurationFile("annotation_URLs.txt");
            if (annotationKey.equalsIgnoreCase("Stored Color")) {
                JOptionPane.showMessageDialog(new JFrame(), "Cannot link stored color to an URL. Pick a different field to link from", "Error", JOptionPane.ERROR_MESSAGE);
                
            } else {
                String[] fieldNames = data.getFieldNames();
                int currFieldIndex = -1;
                for (int i = 0; i < fieldNames.length; i++) {
                    if (annotationKey.equalsIgnoreCase(fieldNames[i])) {
                        currFieldIndex = i;
                        break;
                    }
                }
                String[][] annotationFields = getAnnotationFieldsFromFile(file);                
                //System.out.println("Found annotation file");
                String[] urlTemplates = annotationFields[0];
                String[] urlKeys = annotationFields[1];
                
                if (isFound(annotationKey, urlKeys)) {
                    int currKeyIndex =  -1;
                    for (int i =0; i < urlKeys.length; i++) {
                        if (annotationKey.equalsIgnoreCase(urlKeys[i])) {
                            currKeyIndex = i;
                            break;
                        }
                    }
                    String currURLTemplate = urlTemplates[currKeyIndex];
       //NOTE: In the following statement, the argument "row" is what's obtained AFTER applying getGeneIndexMappedToSelectedRows(); i.e., use as is; no need to re-map for cutoffs                            
                    String currentAnnotationString = data.getElementAttribute(row, currFieldIndex);                    
                    String currentURL = getCurrentURL(annotationKey, currentAnnotationString, currURLTemplate);
                    try {
                        BrowserLauncher.openURL(currentURL);                       
                    }  catch (IOException ie) {
                        JOptionPane.showMessageDialog(new JFrame(), ie.toString(),"Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(new JFrame(), "Browser could not be launched! Possible problem: the annotation format may not be appropriate for this URL type!","Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } else { // if annotationKey is not found in annotation_URLs.txt file
                    int[] indicesToUse = new int[2];
                    indicesToUse[0] = currFieldIndex; //top dropdown list in dialog defaults to current annotation field
                    indicesToUse[1] = lastSelectedIndices[1]; // bottom dropdown list in dialog defaults to last chosen
                    AnnotationURLLinkDialog aDialog = new AnnotationURLLinkDialog(frame, false, experiment, data, row, file, indicesToUse);
                    aDialog.setVisible(true);
                }

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not open browser! Possible problems: bad URL, or \"annotation_URLs.txt\" file in \"config\" directory is not in correct format", "Error", JOptionPane.ERROR_MESSAGE);
            //System.out.println("Did not find file");
            //JOptionPane.showMessageDialog(new JFrame(), "Could not find annotation_URLs.txt file in config directory", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        //return indices;
    }    
    
    private static String getCurrentURL(String currKey, String currAnn, String currTemplate) {
        //System.out.println("currKey = " + currKey);
        String urlToUse = "";
        if (currKey.equalsIgnoreCase("UniGene")) {
            String[] splitAnnotation = currAnn.split("\\.");
            /*
            System.out.println("splitAnnotation.length = " + splitAnnotation.length);
            for (int i = 0; i < splitAnnotation.length; i++) {
                System.out.print("splitAnnotation[" + i + "] = " + splitAnnotation[i]);
            }
             */
            String s1 = currTemplate.replaceAll("FIELD1", splitAnnotation[1]);
            urlToUse = s1.replaceAll("FIELD2", splitAnnotation[0]);
        } else {
            urlToUse = currTemplate.replaceAll("FIELD1", currAnn);
        }
        //System.out.println("url To use = " + urlToUse);
        return urlToUse;        
    }
    
    private static boolean isFound(String annKey, String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            if (annKey.equalsIgnoreCase(keys[i]))
                return true;
        }
        return false;
    }
    
    private static String[][] getAnnotationFieldsFromFile(File file) {
        String[][] annFields = new String[2][];
        Vector annotFieldsVector = new Vector();
        Vector urlKeysVector = new Vector();
        Vector urlTemplateVector = new Vector();
        //Vector urlDescriptionVector = new Vector();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader buff = new BufferedReader(fr);
            StringSplitter st = new StringSplitter('\t');
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) eof = true;
                else {
                    st.init(line);
                    urlKeysVector.add(st.nextToken());
                    urlTemplateVector.add(st.nextToken());
                    //urlDescriptionVector.add(st.nextToken());
                }
            }
            buff.close();
            /*
            String[] urlDescriptions = new String[urlDescriptionVector.size()];
            for (int i = 0; i < urlDescriptions.length; i++) {
                urlDescriptions[i] = (String)(urlDescriptionVector.get(i));
            }
             */

            String[] urlTemplates = new String[urlTemplateVector.size()];
            String[] urlKeys = new String[urlKeysVector.size()];
            
            for (int i = 0; i < urlTemplates.length; i++) {
                urlTemplates[i] = (String)(urlTemplateVector.get(i));
            }
            for (int i = 0; i < urlKeys.length; i++) {
                urlKeys[i] = (String)(urlKeysVector.get(i));
            }
            annFields[0] = urlTemplates;
            annFields[1] = urlKeys;
        } catch (java.io.FileNotFoundException fne) {
            JOptionPane.showMessageDialog(new JFrame(), "Could not find \"annotation_URLs.txt\" file in \"config\" directory", "Error", JOptionPane.ERROR_MESSAGE);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Incompatible \"annotation_URLs.txt\" file in \"config\" directory! Possible issues: extra newline characters, too many or too few tabs per line", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return annFields;
    }
    
    public static int[] lastSelectedAnnotationIndices = {0,0};
    
    /*
    public static int[] getLastSelectedAnnotationIndices() {
        return lastSelectedAnnotationIndices;
    }
     **/
    
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









