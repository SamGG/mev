/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TtestCentroidsViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:52:09 $
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
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.util.FloatMatrix;

public class TtestCentroidsViewer extends CentroidsViewer {

    private Vector tValues, rawPValues, adjPValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    private int tTestDesign;

    /**
     * Constructs a <code>TtestCentroidsViewer</code> for specified experiment
     * and clusters.
     */
    public TtestCentroidsViewer(Experiment experiment, int[][] clusters, int tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
        super(experiment, clusters);
        this.tTestDesign = tTestDesign;
        this.oneClassMeans = oneClassMeans;
        this.oneClassSDs = oneClassSDs;
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.sdA = sdA;
        this.sdB = sdB;
    }

	/**
	 * 
	 */
	public TtestCentroidsViewer(CentroidViewer cv, Integer tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
		super(cv);
        this.tTestDesign = tTestDesign.intValue();
        this.oneClassMeans = oneClassMeans;
        this.oneClassSDs = oneClassSDs;
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.sdA = sdA;
        this.sdB = sdB;     
     }
    public TtestCentroidsViewer(CentroidViewer cv, Integer tTestDesign, 
    		FloatMatrix oneClassMeans,
    		FloatMatrix oneClassSDs,
    		FloatMatrix rawPValues,
    		FloatMatrix adjPValues,
    		FloatMatrix tValues,
    		FloatMatrix dfValues,
    		FloatMatrix meansA,
    		FloatMatrix meansB,
    		FloatMatrix sdA,
    		FloatMatrix sdB) {
		super(cv);
        this.tTestDesign = tTestDesign.intValue();
        	if(oneClassMeans != null && oneClassMeans.A != null && oneClassMeans.A[0] != null)
        		this.oneClassMeans = toVector(oneClassMeans.A[0]);
        	if(oneClassSDs != null && oneClassSDs.A != null && oneClassSDs.A[0] != null)
        		this.oneClassSDs = toVector(oneClassSDs.A[0]);   
        	if(rawPValues != null && rawPValues.A != null && rawPValues.A[0] != null)  
        		this.rawPValues = toVector(rawPValues.A[0]);
        	if(adjPValues != null && adjPValues.A != null && adjPValues.A[0] != null)
        		this.adjPValues = toVector(adjPValues.A[0]);
        	if(tValues != null && tValues.A != null && tValues.A[0] != null)
        		this.tValues = toVector(tValues.A[0]);
        	if(dfValues != null && dfValues.A != null && dfValues.A[0] != null)
        		this.dfValues = toVector(dfValues.A[0]);
        	if(meansA != null && meansA.A != null && meansA.A[0] != null)
        		this.meansA = toVector(meansA.A[0]);
        	if(meansB != null && meansB.A != null && meansB.A[0] != null)
        		this.meansB = toVector(meansB.A[0]);
        	if(sdA != null && sdA.A != null && sdA.A[0] != null)
        		this.sdA = toVector(sdA.A[0]);
        	if(sdB != null && sdB.A != null && sdB.A[0] != null)
        		this.sdB = toVector(sdB.A[0]);
    }

    private static Vector<Float> toVector(float[] temp) {
    	Vector<Float> returnVector = new Vector<Float>(temp.length);
    	for(int i=0; i<temp.length; i++)
    		returnVector.add(i, temp[i]);
    	return returnVector;
    }
    private static FloatMatrix toFloatMatrix(Vector<Float> temp){
    	float[] test = new float[temp.size()];
    	for(int j=0; j<temp.size(); j++) {
    		test[j] = temp.get(j);
    	}
    	return new FloatMatrix(new float[][]{test});
    }    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.centroidViewer, new Integer(this.tTestDesign), //this.oneClassMeans, this.oneClassSDs, this.meansA, this.meansB, this.sdA, this.sdB, this.rawPValues, this.adjPValues, this.tValues, this.dfValues});
		toFloatMatrix(oneClassMeans), 
		toFloatMatrix(oneClassSDs), 
		toFloatMatrix(rawPValues), 
		toFloatMatrix(adjPValues),
		toFloatMatrix(tValues), 
		toFloatMatrix(dfValues),  
		toFloatMatrix(meansA), 
		toFloatMatrix(meansB), 
		toFloatMatrix(sdA), 
		toFloatMatrix(sdB), 
		});
    }
    /**
     * Saves all clusters.
     */
    protected void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        try {
            saveExperiment(frame, getExperiment(), getData(), getClusters());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
            //if (i < fieldNames.length - 1) {
                out.print("\t");
            //}
        }
        if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
            out.print("GroupA mean\t");
            out.print("GroupA std.dev.\t");
            out.print("GroupB mean\t");
            out.print("GroupB std.dev.\t");
            out.print("Absolute t value");
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
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
            if ((tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) || (tTestDesign == TtestInitDialog.PAIRED)) {
                out.print(((Float)meansA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdA.get(rows[i])).floatValue() + "\t");
                out.print(((Float)meansB.get(rows[i])).floatValue() + "\t");
                out.print(((Float)sdB.get(rows[i])).floatValue() + "\t");
            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                out.print(((Float)oneClassMeans.get(rows[i])).floatValue() + "\t");
                out.print(((Float)oneClassSDs.get(rows[i])).floatValue() + "\t");
            }
            //out.print("\t");
            out.print("" + ((Float)tValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)dfValues.get(rows[i])).intValue());
            out.print("\t");
            out.print("" + ((Float)rawPValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)adjPValues.get(rows[i])).floatValue());            
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
