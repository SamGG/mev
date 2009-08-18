/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TtestCentroidViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.awt.Frame;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.util.FloatMatrix;

public class TtestCentroidViewer extends CentroidViewer {
    private TTestResults results;
    
    /**
     * Create new viewer from ttestResultData
     */
    public TtestCentroidViewer(Experiment experiment, ClusterWrapper clusters, TTestResults results) {
    	super(experiment, clusters);
    	this.results = results;
    }
    /**
     * Construct a <code>TtestCentroidViewer</code> with specified experiment
     * and clusters.
     */
    public TtestCentroidViewer(Experiment experiment, int[][] clusters, int tTestDesign, 
    		Vector<Float> oneClassMeans, Vector<Float> oneClassSDs, Vector<Float> meansA, 
    		Vector<Float> meansB, Vector<Float> sdA, Vector<Float> sdB, Vector<Float> rawPValues, 
    		Vector<Float> adjPValues, Vector<Float> tValues, Vector<Float> dfValues) {
        super(experiment, clusters);
        results = createResults(tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB,
        		sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
    }
    private TTestResults createResults(int tTestDesign, Vector<Float> oneClassMeans, 
    		Vector<Float> oneClassSDs, Vector<Float> meansA, Vector<Float> meansB,
    		Vector<Float> sdA, Vector<Float> sdB, Vector<Float> rawPValues, 
    		Vector<Float> adjPValues, Vector<Float> tValues, Vector<Float> dfValues) {
    	TTestResults temp = new TTestResults();
    	temp = new TTestResults();
    	temp.setAdjPValuesMatrix(toFloatMatrix(adjPValues));
    	temp.setDfMatrix(toFloatMatrix(dfValues));
    	temp.setMeansAMatrix(toFloatMatrix(meansA));
    	temp.setMeansBMatrix(toFloatMatrix(meansB));
    	temp.setOneClassMeansMatrix(toFloatMatrix(oneClassMeans));
    	temp.setOneClassSDsMatrix(toFloatMatrix(oneClassSDs));
    	temp.setRawPValuesMatrix(toFloatMatrix(rawPValues));
    	temp.setSdAMatrix(toFloatMatrix(sdA));
    	temp.setSdBMatrix(toFloatMatrix(sdB));
    	temp.setTTestDesign(tTestDesign);
    	temp.setTValuesMatrix(toFloatMatrix(tValues));
    	return temp;
    }
    /**
     * State-saving constructor for loading saved analyses from MeV v4.4 - v4.4.1
     * @param e
     * @param clusters
     * @param variances
     * @param means
     * @param codes
     * @param templateVector
     * @param auxTitles
     * @param auxData
     */
    public TtestCentroidViewer(Experiment e, ClusterWrapper clusters, float[][] variances, float[][] means, float[][] codes,
   		 Integer tTestDesign, Vector<Float> oneClassMeans, Vector<Float> oneClassSDs, Vector<Float> meansA, Vector<Float> meansB, Vector<Float> sdA, Vector<Float> sdB, Vector<Float> rawPValues, Vector<Float> adjPValues, Vector<Float> tValues, Vector<Float> dfValues) {
    	this(e, clusters.getClusters(), variances, means, codes,
       		 tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB, sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
    } 

    private static FloatMatrix toFloatMatrix(Vector<Float> temp){
    	float[] test = new float[temp.size()];
    	for(int j=0; j<temp.size(); j++) {
    		test[j] = temp.get(j);
    	}
    	return new FloatMatrix(new float[][]{test});
    }    
    /**
     * State-saving constructor for loading saved analyses from MeV v4.0-4.3
     **/
    public TtestCentroidViewer(Experiment e, int[][] clusters, float[][] variances, float[][] means, float[][] codes,
    		 Integer tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
    	super(e, clusters, variances, means, codes);
        results = createResults(tTestDesign, oneClassMeans, oneClassSDs, meansA, meansB,
        		sdA, sdB, rawPValues, adjPValues, tValues, dfValues);
     }
    public Expression getExpression(){
    	Object[] superExpressionArgs = super.getExpression().getArguments();
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{superExpressionArgs[0], superExpressionArgs[1], results});
    }
    
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getClusters());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the viewer cluster.
     */
    protected void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getCluster());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Saves values from specified experiment and its rows.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[] rows) throws Exception {
        File file = getFile(frame);
        if (file != null) {
            saveCluster(file, experiment, data, rows);
        }
    }
    
    /**
     * Saves values from specified experiment and cluster.
     */
    public void saveExperiment(Frame frame, Experiment experiment, IData data, int[][] clusters) throws Exception {
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
    
    private void saveCluster(File file, Experiment experiment, IData data, int[] rows) throws Exception {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        String[] fieldNames = data.getFieldNames();
        out.print("Original row");
        out.print("\t");
        for (int i = 0; i < fieldNames.length; i++) {
            out.print(fieldNames[i]);
            out.print("\t");
        }
        if ((results.getTTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) || (results.getTTestDesign() == TtestInitDialog.PAIRED)) {        
            out.print("GroupA mean\t");
            out.print("GroupA std.dev.\t");
            out.print("GroupB mean\t");
            out.print("GroupB std.dev.\t");
            out.print("Absolute t value");
        } else if (results.getTTestDesign() == TtestInitDialog.ONE_CLASS) {
            out.print("Gene mean\t");
            out.print("Gene std.dev.\t");
            out.print("t value");
        }
        //out.print("\t");
        
        out.print("\t");
        out.print("Degrees of freedom\t");
        out.print("Raw p value\t");
        out.print("Adj p value");
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));
            //out.print(data.getUniqueId(rows[i]));
            out.print("\t");
            //out.print(data.getGeneName(rows[i]));
            for (int k = 0; k < fieldNames.length; k++) {
                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                //if (k < fieldNames.length - 1) {
                    out.print("\t");
                //}
            }
            if ((results.getTTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) || (results.getTTestDesign() == TtestInitDialog.PAIRED)) {            
                out.print(results.getMeansAMatrix().get(rows[i], 0) + "\t");
                out.print(results.getSdAMatrix().get(rows[i],0) + "\t");
                out.print(results.getMeansBMatrix().get(rows[i],0) + "\t");
                out.print(results.getSdBMatrix().get(rows[i],0) + "\t");
            } else if (results.getTTestDesign() == TtestInitDialog.ONE_CLASS) {
                out.print(results.getOneClassMeansMatrix().get(rows[i],0) + "\t");
                out.print(results.getOneClassSDsMatrix().get(rows[i],0) + "\t");
            } 
            //out.print("\t");
            out.print("" + results.getTValuesMatrix().get(rows[i],0));
            out.print("\t");
            out.print("" + results.getDfMatrix().get(rows[i],0));
            out.print("\t");            
            out.print("" + results.getRawPValuesMatrix().get(rows[i], 0));
            out.print("\t");            
            out.print("" + results.getAdjPValuesMatrix().get(rows[i],0));
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
    
}


