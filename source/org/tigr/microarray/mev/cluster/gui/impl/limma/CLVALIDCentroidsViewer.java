/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: BETRCentroidsViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:51:02 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.clvalid;

import java.awt.Frame;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidsViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class CLVALIDCentroidsViewer extends CentroidsViewer {

    private Vector fValues, rawPValues, adjPValues, dfNumValues, dfDenomValues, ssGroups, ssError;
    private float[][] geneGroupMeans, geneGroupSDs;
    
    /** Creates new BETRCentroidsViewer */
    public CLVALIDCentroidsViewer(Experiment experiment, int[][] clusters, float[][] geneGroupMeans, float[][] geneGroupSDs, Vector rawPValues, Vector adjPValues, Vector fValues, Vector ssGroups, Vector ssError, Vector dfNumValues, Vector dfDenomValues) {
        super(experiment, clusters);        
		initialize(geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
    }
	/**
	 * @inheritDoc
	 */
	public CLVALIDCentroidsViewer(CentroidViewer cv, float[][] geneGroupMeans, float[][] geneGroupSDs, Vector rawPValues, Vector adjPValues, Vector fValues, Vector ssGroups, Vector ssError, Vector dfNumValues, Vector dfDenomValues) {
		super(cv);
		initialize(geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
	}
	/**
	 * @inheritDoc
	 */
	public Expression getExpression(){
		Object[] parentExpressionArgs = super.getExpression().getArguments();
		return new Expression(this, this.getClass(), "new", 
				new Object[]{parentExpressionArgs[0], geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues});
	}
	
	public void initialize(float[][] geneGroupMeans, float[][] geneGroupSDs, Vector rawPValues, Vector adjPValues, Vector fValues, Vector ssGroups, Vector ssError, Vector dfNumValues, Vector dfDenomValues) {
        this.rawPValues = rawPValues;
        this.adjPValues = adjPValues;
        this.fValues = fValues;
        this.geneGroupMeans = geneGroupMeans;
        this.geneGroupSDs = geneGroupSDs;
        this.ssGroups = ssGroups;
        this.ssError = ssError;
        this.dfNumValues = dfNumValues;
        this.dfDenomValues = dfDenomValues;       
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
        for (int i = 0; i < geneGroupMeans[0].length; i++) {
            out.print("Group" + (i+1) + " mean\t");
            out.print("Group" + (i + 1) + " std.dev.\t");
        }        
        //out.print("\t");
        out.print("F ratio");
        out.print("\t");
        out.print("SS(Groups)\t");
        out.print("SS(Error)\t");
        out.print("df (Groups)\t");
        out.print("df (Error)\t");
        out.print("raw p value\t");
        out.print("adj p value");
        
        //out.print("UniqueID\tName");
        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getFullSampleName(experiment.getSampleIndex(i)));
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
            for (int j = 0; j < geneGroupMeans[rows[i]].length; j++) {
                out.print(geneGroupMeans[rows[i]][j] + "\t");
                out.print(geneGroupSDs[rows[i]][j] + "\t");
            }            
            //out.print("\t");
            out.print("" + ((Float)fValues.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)ssGroups.get(rows[i])).floatValue());
            out.print("\t");
            out.print("" + ((Float)ssError.get(rows[i])).floatValue());
            out.print("\t");            
            out.print("" + ((Float)dfNumValues.get(rows[i])).floatValue());
            out.print("\t"); 
            out.print("" + ((Float)dfDenomValues.get(rows[i])).floatValue());
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
        final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int ret = fc.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }
    

}
