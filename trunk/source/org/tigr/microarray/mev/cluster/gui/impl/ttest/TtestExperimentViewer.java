/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TtestExperimentViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

public class TtestExperimentViewer extends ExperimentViewer {

    private Vector tValues, rawPValues, adjPValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    private int tTestDesign;    
    
    /**
     * Constructs a <code>TtestExperimentViewer</code> with specified
     * experiment and clusters.
     */
    public TtestExperimentViewer(Experiment experiment, int[][] clusters, int tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
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
        this.sdB =sdB;        
    }
    /**
     * @inheritDoc
     */ 
    public TtestExperimentViewer(Experiment e, int[][] clusters, 
    		Integer tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector rawPValues, Vector adjPValues, Vector tValues, Vector dfValues) {
    	super(e, clusters);
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
        this.sdB =sdB;               
     }
         
    public Expression getExpression(){
    	Object[] superExpressionArgs = super.getExpression().getArguments();
    	
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{superExpressionArgs[0], superExpressionArgs[1], /*superExpressionArgs[2], superExpressionArgs[3], superExpressionArgs[4], superExpressionArgs[5], */new Integer(this.tTestDesign), this.oneClassMeans, this.oneClassSDs, this.meansA, this.meansB, this.sdA, this.sdB, this.rawPValues, this.adjPValues, this.tValues, this.dfValues});
    }
         
     /**
     * Saves all the clusters.
     */
    public void saveClusters(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        saveExperiment(frame, getExperiment(), getData(), getClusters());
    }

    /**
     * Saves current cluster.
     */
    public void saveCluster(Frame frame) throws Exception {
        frame = frame == null ? JOptionPane.getFrameForComponent(this) : frame;
        saveExperiment(frame, getExperiment(), getData(), getCluster());
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
        //out.print("UniqueID\tName");        
        //out.print("\t");
        
        out.print("\t");
        out.print("Degrees of freedom\t");
        out.print("Raw p value\t");
        out.print("Adj p value");

        for (int i=0; i<experiment.getNumberOfSamples(); i++) {
            out.print("\t");
            out.print(data.getSampleName(experiment.getSampleIndex(i)));
        }
        out.print("\n");
        for (int i=0; i<rows.length; i++) {
            out.print(Integer.toString(experiment.getGeneIndexMappedToData(rows[i]) + 1));  //handles cutoffs
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
